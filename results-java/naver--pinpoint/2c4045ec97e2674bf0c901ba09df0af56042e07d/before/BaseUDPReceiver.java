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

package com.navercorp.pinpoint.collector.receiver.udp;

import com.codahale.metrics.Timer;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.thrift.io.*;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.*;

/**
 * @author emeroad
 * @author netspider
 */
public class BaseUDPReceiver extends AbstractUDPReceiver {
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(new HeaderTBaseDeserializerFactory());

    public BaseUDPReceiver(String receiverName, DispatchHandler dispatchHandler, String bindAddress, int port, int receiverBufferSize, int workerThreadSize, int workerThreadQueueSize) {
        super(receiverName, dispatchHandler, bindAddress, port, receiverBufferSize, workerThreadSize, workerThreadQueueSize);
    }

    @Override
    Runnable getPacketDispatcher(AbstractUDPReceiver receiver, DatagramPacket packet) {
        return new DispatchPacket(receiver, packet);
    }

    private class DispatchPacket implements Runnable {
        private final AbstractUDPReceiver receiver;
        private final DatagramPacket packet;

        private DispatchPacket(AbstractUDPReceiver receiver, DatagramPacket packet) {
            if (packet == null) {
                throw new NullPointerException("packet must not be null");
            }
            this.receiver = receiver;
            this.packet = packet;
        }

        @Override
        public void run() {
            Timer.Context time = receiver.getTimer().time();

            final HeaderTBaseDeserializer deserializer = (HeaderTBaseDeserializer) deserializerFactory.createDeserializer();
            TBase<?, ?> tBase = null;
            try {
                tBase = deserializer.deserialize(packet.getData());
                if (tBase instanceof L4Packet) {
                    if (logger.isDebugEnabled()) {
                        L4Packet packet = (L4Packet) tBase;
                        logger.debug("udp l4 packet {}", packet.getHeader());
                    }
                    return;
                }
                // Network port availability check packet
                if (tBase instanceof NetworkAvailabilityCheckPacket) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("received udp network availability check packet.");
                    }
                    responseOK();
                    return;
                }
                // dispatch signifies business logic execution
                receiver.getDispatchHandler().dispatchSendMessage(tBase);
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                // there are cases where invalid headers are received
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} tBase:{}", packet.getSocketAddress(), e.getMessage(), tBase, e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } finally {
                receiver.getDatagramPacketPool().returnObject(packet);
                // what should we do when an exception is thrown?
                time.stop();
            }
        }

        private void responseOK() {
            try {
                byte[] okBytes = NetworkAvailabilityCheckPacket.DATA_OK;
                DatagramPacket pongPacket = new DatagramPacket(okBytes, okBytes.length, packet.getSocketAddress());
                receiver.getSocket().send(pongPacket);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("pong error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }
    }

}