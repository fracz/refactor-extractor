/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket.oio;

import io.netty.buffer.ChannelBuffer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelBufferHolder;
import io.netty.channel.ChannelBufferHolders;
import io.netty.channel.ChannelException;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.NotYetConnectedException;

public class OioSocketChannel extends AbstractOioChannel
                              implements SocketChannel {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(OioSocketChannel.class);

    private final Socket socket;
    private final SocketChannelConfig config;
    private final ChannelBufferHolder<?> out = ChannelBufferHolders.byteBuffer();
    private PushbackInputStream is;
    private OutputStream os;

    public OioSocketChannel() {
        this(new Socket());
    }

    public OioSocketChannel(Socket socket) {
        this(null, null, socket);
    }

    public OioSocketChannel(Channel parent, Integer id, Socket socket) {
        super(parent, id);
        this.socket = socket;
        config = new DefaultSocketChannelConfig(socket);

        boolean success = false;
        try {
            if (socket.isConnected()) {
                is = new PushbackInputStream(socket.getInputStream());
                os = socket.getOutputStream();
            }
            socket.setSoTimeout(1000);
            success = true;
        } catch (Exception e) {
            throw new ChannelException("failed to initialize a socket", e);
        } finally {
            if (!success) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.warn("Failed to close a socket.", e);
                }
            }
        }
    }

    @Override
    public SocketChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return !socket.isClosed();
    }

    @Override
    public boolean isActive() {
        return !socket.isClosed() && socket.isConnected();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ChannelBufferHolder<Object> firstOut() {
        return (ChannelBufferHolder<Object>) out;
    }

    @Override
    protected SocketAddress localAddress0() {
        return socket.getLocalSocketAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        socket.bind(localAddress);
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress,
            SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            socket.bind(localAddress);
        }

        boolean success = false;
        try {
            socket.connect(remoteAddress, config().getConnectTimeoutMillis());
            is = new PushbackInputStream(socket.getInputStream());
            os = socket.getOutputStream();
            success = true;
        } finally {
            if (!success) {
                doClose();
            }
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        socket.close();
    }

    @Override
    protected int doReadBytes(ChannelBuffer buf) throws Exception {
        if (socket.isClosed()) {
            return -1;
        }
        int b;
        try {
            b = is.read();
            if (b < 0) {
                return -1;
            }
            is.unread(b);

            int available = is.available();
            buf.ensureWritableBytes(available);
            return buf.writeBytes(is, available);
        } catch (SocketTimeoutException e) {
            return 0;
        }
    }

    @Override
    protected int doWriteBytes(ChannelBuffer buf) throws Exception {
        OutputStream os = this.os;
        if (os == null) {
            throw new NotYetConnectedException();
        }
        final int length = buf.readableBytes();
        buf.readBytes(os, length);
        return length;
    }
}