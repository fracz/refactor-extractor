/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.sctp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;
import io.netty.channel.socket.SctpMessage;
import io.netty.handler.codec.EncoderException;

public class SctpMessageEncoder extends ChannelOutboundMessageHandlerAdapter<ByteBuf> {
    private final int streamIdentifier;
    private final int protocolIdentifier;

    public SctpMessageEncoder(int streamIdentifier, int protocolIdentifier) {
        this.streamIdentifier = streamIdentifier;
        this.protocolIdentifier = protocolIdentifier;
    }

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelFuture future) throws Exception {
        ByteBuf in = ctx.outboundByteBuffer();

        try {
            MessageBuf<Object> out = ctx.nextOutboundMessageBuffer();
            ByteBuf payload = Unpooled.buffer(in.readableBytes());
            payload.writeBytes(in);
            out.add(new SctpMessage(streamIdentifier, protocolIdentifier, payload));
            in.discardReadBytes();
        } catch (Throwable t) {
            ctx.fireExceptionCaught(new EncoderException(t));
        }

        ctx.flush(future);
    }
}