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
package io.netty.channel;

import io.netty.buffer.ChannelBuffer;

import java.util.Queue;

public abstract class ChannelOutboundHandlerAdapter<O> extends ChannelHandlerAdapter
        implements ChannelOutboundHandler<O> {

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelFuture future) throws Exception {
        flush0(ctx, future);
    }

    static <O> void flush0(ChannelHandlerContext ctx, ChannelFuture future) {
        if (ctx.hasOutboundMessageBuffer()) {
            Queue<O> out = ctx.outboundMessageBuffer();
            Queue<Object> nextOut = ctx.nextOutboundMessageBuffer();
            for (;;) {
                O msg = out.poll();
                if (msg == null) {
                    break;
                }
                nextOut.add(msg);
            }
        } else {
            ChannelBuffer out = ctx.outboundByteBuffer();
            ChannelBuffer nextOut = ctx.nextOutboundByteBuffer();
            nextOut.writeBytes(out);
            out.discardReadBytes();
        }
        ctx.flush(future);
    }
}