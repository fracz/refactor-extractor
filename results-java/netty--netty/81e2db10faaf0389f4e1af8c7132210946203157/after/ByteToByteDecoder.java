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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnsafeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

public abstract class ByteToByteDecoder extends ChannelInboundByteHandlerAdapter {

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        callDecode(ctx, in, ctx.nextInboundByteBuffer());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf in = ctx.inboundByteBuffer();
        ByteBuf out = ctx.nextInboundByteBuffer();
        if (!in.readable()) {
            callDecode(ctx, in, out);
        }

        int oldOutSize = out.readableBytes();
        try {
            decodeLast(ctx, in, out);
        } catch (Throwable t) {
            if (t instanceof CodecException) {
                ctx.fireExceptionCaught(t);
            } else {
                ctx.fireExceptionCaught(new DecoderException(t));
            }
        }

        if (out.readableBytes() > oldOutSize) {
            ctx.fireInboundBufferUpdated();
        }

        ctx.fireChannelInactive();
    }

    private void callDecode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        int oldOutSize = out.readableBytes();
        while (in.readable()) {
            int oldInSize = in.readableBytes();
            try {
                decode(ctx, in, out);
            } catch (Throwable t) {
                if (t instanceof CodecException) {
                    ctx.fireExceptionCaught(t);
                } else {
                    ctx.fireExceptionCaught(new DecoderException(t));
                }
            }
            if (oldInSize == in.readableBytes()) {
                break;
            }
        }

        ((UnsafeByteBuf) in).discardSomeReadBytes();
        if (out.readableBytes() > oldOutSize) {
            ctx.fireInboundBufferUpdated();
        }
    }

    public abstract void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception;

    public void decodeLast(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        decode(ctx, in, out);
    }
}