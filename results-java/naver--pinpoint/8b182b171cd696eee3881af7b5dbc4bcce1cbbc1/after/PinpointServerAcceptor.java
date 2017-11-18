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

package com.navercorp.pinpoint.rpc.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.ServerClosePacket;
import com.navercorp.pinpoint.rpc.server.handler.ChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.server.handler.DoNothingChannelStateEventHandler;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.CpuUtils;
import com.navercorp.pinpoint.rpc.util.LoggerFactorySetup;
import com.navercorp.pinpoint.rpc.util.TimerFactory;

/**
 * @author Taejin Koo
 */
public class PinpointServerAcceptor implements PinpointServerConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long DEFAULT_TIMEOUTMILLIS = 3 * 1000;
    private static final int WORKER_COUNT = CpuUtils.workerCount();

    private volatile boolean released;

    private ServerBootstrap bootstrap;

    private InetAddress[] ignoreAddressList;

    private Channel serverChannel;
    private final ChannelGroup channelGroup = new DefaultChannelGroup("PinpointServerFactory");

    private final PinpointServerChannelHandler nettyChannelHandler = new PinpointServerChannelHandler();

    private ServerMessageListener messageListener = SimpleLoggingServerMessageListener.LISTENER;
    private ServerStreamChannelMessageListener serverStreamChannelMessageListener = DisabledServerStreamChannelMessageListener.INSTANCE;
    private List<ChannelStateChangeEventHandler> stateChangeEventHandler = new ArrayList<ChannelStateChangeEventHandler>();

    private final Timer healthCheckTimer;
    private final Timer requestManagerTimer;

    private long defaultRequestTimeout = DEFAULT_TIMEOUTMILLIS;

    static {
        LoggerFactorySetup.setupSlf4jLoggerFactory();
    }

    public PinpointServerAcceptor() {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;

        this.healthCheckTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-HealthCheckTimer", 50, TimeUnit.MILLISECONDS, 512);
        this.requestManagerTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-RequestManager", 50, TimeUnit.MILLISECONDS, 512);
    }

    private ServerBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector
        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Boss"));
        NioServerBossPool nioServerBossPool = new NioServerBossPool(boss, bossCount, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Worker"));
        NioWorkerPool nioWorkerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);

        NioServerSocketChannelFactory nioClientSocketChannelFactory = new NioServerSocketChannelFactory(nioServerBossPool, nioWorkerPool);
        return new ServerBootstrap(nioClientSocketChannelFactory);
    }

    private void setOptions(ServerBootstrap bootstrap) {
        // is read/write timeout necessary? don't need it because of NIO?
        // write timeout should be set through additional interceptor. write
        // timeout exists.

        // tcp setting
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // buffer setting
        bootstrap.setOption("child.sendBufferSize", 1024 * 64);
        bootstrap.setOption("child.receiveBufferSize", 1024 * 64);

        // bootstrap.setOption("child.soLinger", 0);
    }

    private void addPipeline(ServerBootstrap bootstrap) {
        ServerPipelineFactory serverPipelineFactory = new ServerPipelineFactory(nettyChannelHandler);
        bootstrap.setPipelineFactory(serverPipelineFactory);
    }

    void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory must not be null");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
    }

    public void bind(String host, int port) throws PinpointSocketException {
        if (released) {
            return;
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        this.serverChannel = bootstrap.bind(address);
        sendPing();
    }

    private DefaultPinpointServer createPinpointServer(Channel channel) {
        DefaultPinpointServer pinpointServer = new DefaultPinpointServer(channel, this);
        return pinpointServer;
    }

    @Override
    public long getDefaultRequestTimeout() {
        return defaultRequestTimeout;
    }

    public void setDefaultRequestTimeout(long defaultRequestTimeout) {
        this.defaultRequestTimeout = defaultRequestTimeout;
    }

    private boolean isIgnoreAddress(Channel channel) {
        if (ignoreAddressList == null) {
            return false;
        }
        final InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
        if (remoteAddress == null) {
            return false;
        }
        InetAddress address = remoteAddress.getAddress();
        for (InetAddress ignore : ignoreAddressList) {
            if (ignore.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public void setIgnoreAddressList(InetAddress[] ignoreAddressList) {
        AssertUtils.assertNotNull(ignoreAddressList, "ignoreAddressList must not be null");

        this.ignoreAddressList = ignoreAddressList;
    }

    @Override
    public ServerMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(ServerMessageListener messageListener) {
        AssertUtils.assertNotNull(messageListener, "messageListener must not be null");

        this.messageListener = messageListener;
    }

    @Override
    public List<ChannelStateChangeEventHandler> getStateChangeEventHandlers() {
        return stateChangeEventHandler;
    }

    public void addStateChangeEventHandler(ChannelStateChangeEventHandler stateChangeEventHandler) {
        AssertUtils.assertNotNull(stateChangeEventHandler, "stateChangeEventHandler must not be null");

        this.stateChangeEventHandler.add(stateChangeEventHandler);
    }

    @Override
    public ServerStreamChannelMessageListener getStreamMessageListener() {
        return serverStreamChannelMessageListener;
    }

    public void setServerStreamChannelMessageListener(ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        AssertUtils.assertNotNull(serverStreamChannelMessageListener, "serverStreamChannelMessageListener must not be null");

        this.serverStreamChannelMessageListener = serverStreamChannelMessageListener;
    }

    @Override
    public Timer getHealthCheckTimer() {
        return healthCheckTimer;
    }

    @Override
    public Timer getRequestManagerTimer() {
        return requestManagerTimer;
    }

    private void sendPing() {
        logger.debug("sendPing");
        final TimerTask pintTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (timeout.isCancelled()) {
                    newPingTimeout(this);
                    return;
                }

                final ChannelGroupFuture write = channelGroup.write(PingPacket.PING_PACKET);
                if (logger.isWarnEnabled()) {
                    write.addListener(new ChannelGroupFutureListener() {
                        private final ChannelFutureListener listener = new WriteFailFutureListener(logger, "ping write fail", "ping write success");

                        @Override
                        public void operationComplete(ChannelGroupFuture future) throws Exception {

                            if (logger.isWarnEnabled()) {
                                for (ChannelFuture channelFuture : future) {
                                    channelFuture.addListener(listener);
                                }
                            }
                        }
                    });
                }
                newPingTimeout(this);
            }
        };
        newPingTimeout(pintTask);
    }

    private void newPingTimeout(TimerTask pintTask) {
        try {
            logger.debug("newPingTimeout");
            healthCheckTimer.newTimeout(pintTask, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            // stop in case of timer stopped
            logger.debug("timer stopped. Caused:{}", e.getMessage());
        }
    }

    public void close() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }
        healthCheckTimer.stop();

        closePinpointServer();

        if (serverChannel != null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
            bootstrap = null;
        }

        // clear the request first and remove timer
        requestManagerTimer.stop();
    }

    private void closePinpointServer() {
        for (Channel channel : channelGroup) {
            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();

            if (pinpointServer != null) {
                pinpointServer.sendClosePacket();
            }
        }
    }

    public List<PinpointServer> getWritableServerList() {
        List<PinpointServer> pinpointServerList = new ArrayList<PinpointServer>();

        for (Channel channel : channelGroup) {
            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null) {
                if (pinpointServer.isEnableDuplexCommunication()) {
                    pinpointServerList.add(pinpointServer);
                }
            }
        }

        return pinpointServerList;
    }

    class PinpointServerChannelHandler extends SimpleChannelHandler {
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();
            logger.info("channelConnected channel:{}", channel);

            if (released) {
                logger.warn("already released. channel:{}", channel);
                channel.write(new ServerClosePacket()).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.getChannel().close();
                    }
                });
                return;
            }

            boolean isIgnore = isIgnoreAddress(channel);
            if (isIgnore) {
                logger.debug("channelConnected ignore address. channel:" + channel);
                return;
            }

            DefaultPinpointServer pinpointServer = createPinpointServer(channel);

            channel.setAttachment(pinpointServer);
            channelGroup.add(channel);

            pinpointServer.start();

            super.channelConnected(ctx, e);
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();

            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null) {
                pinpointServer.stop(released);
            }

            super.channelDisconnected(ctx, e);
        }

        // ChannelClose event may also happen when the other party close socket
        // first and Disconnected occurs
        // Should consider that.
        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            final Channel channel = e.getChannel();

            channelGroup.remove(channel);

            super.channelClosed(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            final Channel channel = e.getChannel();

            DefaultPinpointServer pinpointServer = (DefaultPinpointServer) channel.getAttachment();
            if (pinpointServer != null) {
                Object message = e.getMessage();

                pinpointServer.messageReceived(message);
            }

            super.messageReceived(ctx, e);
        }
    }

}