/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.sctp;

import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.ServerSocketChannelConfig;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.jboss.netty.channel.Channels.fireChannelOpen;

/**
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://github.com/jestan">Jestan Nirojan</a>
 *
 * @version $Rev$, $Date$
 *
 */
class SctpServerChannelImpl extends AbstractServerChannel
                             implements SctpServerChannel {

    private static final InternalLogger logger =
        InternalLoggerFactory.getInstance(SctpServerChannelImpl.class);

    final com.sun.nio.sctp.SctpServerChannel socket;
    final Lock shutdownLock = new ReentrantLock();
    volatile Selector selector;
    private final ServerSocketChannelConfig config;

    private volatile boolean bound;

    SctpServerChannelImpl(
            ChannelFactory factory,
            ChannelPipeline pipeline,
            ChannelSink sink) {

        super(factory, pipeline, sink);

        try {
            socket = com.sun.nio.sctp.SctpServerChannel.open();
        } catch (IOException e) {
            throw new ChannelException(
                    "Failed to open a server socket.", e);
        }

        try {
            socket.configureBlocking(false);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                logger.warn(
                        "Failed to close a partially initialized socket.", e2);
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }

        config = new SctpServerChannelConfig(socket);

        fireChannelOpen(this);
    }

    @Override
    public ServerSocketChannelConfig getConfig() {
        return config;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        try {
            return (InetSocketAddress) socket.getAllLocalAddresses().iterator().next();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public boolean isBound() {
        return isOpen() && bound;
    }

    public void setBound() {
        bound = true;
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }
}