/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster;

import javax.annotation.PreDestroy;

import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.cluster.route.DefaultRouteHandler;
import com.navercorp.pinpoint.collector.cluster.route.LoggingFilter;
import com.navercorp.pinpoint.collector.cluster.route.RequestEvent;
import com.navercorp.pinpoint.collector.cluster.route.RouteResult;
import com.navercorp.pinpoint.collector.cluster.route.RouteStatus;
import com.navercorp.pinpoint.collector.cluster.route.StreamEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.BasicStreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

/**
 * @author koo.taejin
 */
public class ClusterPointRouter implements MessageListener, ServerStreamChannelMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository;

    private final DefaultRouteHandler routeHandler;
    private final StreamRouteHandler streamRouteHandler;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public ClusterPointRouter() {
        this.targetClusterPointRepository = new ClusterPointRepository<TargetClusterPoint>();

        LoggingFilter loggingFilter = new LoggingFilter();

        this.routeHandler = new DefaultRouteHandler(targetClusterPointRepository);
        this.routeHandler.addRequestFilter(loggingFilter.getRequestFilter());
        this.routeHandler.addResponseFilter(loggingFilter.getResponseFilter());

        this.streamRouteHandler = new StreamRouteHandler(targetClusterPointRepository);
        this.streamRouteHandler.addRequestFilter(loggingFilter.getStreamCreateFilter());
        this.streamRouteHandler.addResponseFilter(loggingFilter.getResponseFilter());
    }

    @PreDestroy
    public void stop() {
    }

    @Override
    public void handleSend(SendPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);
    }

    @Override
    public void handleRequest(RequestPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            handleRouteRequestFail("Protocol decoding failed.", packet, channel);
        } else if (request instanceof TCommandTransfer) {
            handleRouteRequest((TCommandTransfer) request, packet, channel);
        } else {
            handleRouteRequestFail("Unknown error.", packet, channel);
        }
    }

    @Override
    public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            return StreamCreateFailPacket.PACKET_ERROR;
        } else if (request instanceof TCommandTransfer) {
            return handleStreamRouteCreate((TCommandTransfer) request, packet, streamChannelContext);
        } else {
            return StreamCreateFailPacket.UNKNWON_ERROR;
        }
    }

    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        streamRouteHandler.close(streamChannelContext);
    }

    private boolean handleRouteRequest(TCommandTransfer request, RequestPacket requestPacket, Channel channel) {
        byte[] payload = ((TCommandTransfer) request).getPayload();
        TBase command = deserialize(payload);

        RouteResult routeResult = routeHandler.onRoute(new RequestEvent((TCommandTransfer) request, channel, requestPacket.getRequestId(), command));

        if (RouteStatus.OK == routeResult.getStatus()) {
            channel.write(new ResponsePacket(requestPacket.getRequestId(), routeResult.getResponseMessage().getMessage()));
            return true;
        } else {
            TResult result = new TResult(false);
            result.setMessage(routeResult.getStatus().getReasonPhrase());

            channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
            return false;
        }
    }

    private void handleRouteRequestFail(String message, RequestPacket requestPacket, Channel channel) {
        TResult tResult = new TResult(false);
        tResult.setMessage(message);

        channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
    }

    private short handleStreamRouteCreate(TCommandTransfer request, StreamCreatePacket packet, ServerStreamChannelContext streamChannelContext) {
        byte[] payload = ((TCommandTransfer) request).getPayload();
        TBase command = deserialize(payload);

        RouteResult routeResult = streamRouteHandler.onRoute(new StreamEvent((TCommandTransfer) request, streamChannelContext, command));

        RouteStatus status = routeResult.getStatus();
        switch (status) {
            case OK:
                return BasicStreamPacket.SUCCESS;
            case BAD_REQUEST:
                return StreamCreateFailPacket.PACKET_ERROR;
            case NOT_FOUND:
                return StreamCreateFailPacket.ROUTE_NOT_FOUND;
            case NOT_ACCEPTABLE:
                return StreamCreateFailPacket.ROUTE_PACKET_UNSUPPORT;
            case NOT_ACCEPTABLE_UNKNOWN:
                return StreamCreateFailPacket.ROUTE_CONNECTION_ERROR;
            case NOT_ACCEPTABLE_AGENT_TYPE:
                return StreamCreateFailPacket.ROUTE_TYPE_UNKOWN;
            default:
                return StreamCreateFailPacket.UNKNWON_ERROR;
        }
    }

    public ClusterPointRepository<TargetClusterPoint> getTargetClusterPointRepository() {
        return targetClusterPointRepository;
    }

    private byte[] serialize(TBase result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }

    private TBase deserialize(byte[] objectData) {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, null);
    }

}