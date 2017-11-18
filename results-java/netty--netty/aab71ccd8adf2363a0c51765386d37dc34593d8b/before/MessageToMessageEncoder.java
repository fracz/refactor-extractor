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
package io.netty.handler.codec;

import io.netty.channel.ChannelBufferHolder;
import io.netty.channel.ChannelBufferHolders;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;

import java.util.Queue;

public abstract class MessageToMessageEncoder<I, O> extends ChannelOutboundHandlerAdapter<I> {

    @Override
    public ChannelBufferHolder<I> newOutboundBuffer(
            ChannelHandlerContext ctx) throws Exception {
        return ChannelBufferHolders.messageBuffer();
    }

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelFuture future) throws Exception {
        Queue<I> in = ctx.outboundMessageBuffer();
        boolean notify = false;
        for (;;) {
            try {
                Object msg = in.poll();
                if (msg == null) {
                    break;
                }

                if (!isEncodable(msg)) {
                    ctx.nextOutboundMessageBuffer().add(msg);
                    notify = true;
                    continue;
                }

                @SuppressWarnings("unchecked")
                I imsg = (I) msg;
                O omsg = encode(ctx, imsg);
                if (omsg == null) {
                    // encode() might be waiting for more inbound messages to generate
                    // an aggregated message - keep polling.
                    continue;
                }

                if (CodecUtil.unfoldAndAdd(ctx, omsg, false)) {
                    notify = true;
                }
            } catch (Throwable t) {
                if (t instanceof CodecException) {
                    ctx.fireExceptionCaught(t);
                } else {
                    ctx.fireExceptionCaught(new EncoderException(t));
                }
            }
        }

        if (notify) {
            ctx.flush(future);
        }
    }

    /**
     * Returns {@code true} if and only if the specified message can be encoded by this encoder.
     *
     * @param msg the message
     */
    public boolean isEncodable(Object msg) throws Exception {
        return true;
    }

    public abstract O encode(ChannelHandlerContext ctx, I msg) throws Exception;
}