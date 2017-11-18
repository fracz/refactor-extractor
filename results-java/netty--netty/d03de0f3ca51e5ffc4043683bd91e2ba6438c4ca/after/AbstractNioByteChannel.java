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
package io.netty.channel.socket.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInputShutdownEvent;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

abstract class AbstractNioByteChannel extends AbstractNioChannel {

    protected AbstractNioByteChannel(
            Channel parent, Integer id, SelectableChannel ch) {
        super(parent, id, ch, SelectionKey.OP_READ);
    }

    @Override
    protected NioByteUnsafe newUnsafe() {
        return new NioByteUnsafe();
    }

    private final class NioByteUnsafe extends AbstractNioUnsafe {
        @Override
        public void read() {
            assert eventLoop().inEventLoop();

            final ChannelPipeline pipeline = pipeline();
            final ByteBuf byteBuf = pipeline.inboundByteBuffer();
            boolean closed = false;
            boolean read = false;
            try {
                expandReadBuffer(byteBuf);
                loop: for (;;) {
                    int localReadAmount = doReadBytes(byteBuf);
                    if (localReadAmount > 0) {
                        read = true;
                    } else if (localReadAmount < 0) {
                        closed = true;
                        break;
                    }

                    switch (expandReadBuffer(byteBuf)) {
                    case 0:
                        // Read all - stop reading.
                        break loop;
                    case 1:
                        // Keep reading until everything is read.
                        break;
                    case 2:
                        // Let the inbound handler drain the buffer and continue reading.
                        if (read) {
                            read = false;
                            pipeline.fireInboundBufferUpdated();
                            if (!byteBuf.writable()) {
                                throw new IllegalStateException(
                                        "an inbound handler whose buffer is full must consume at " +
                                        "least one byte.");
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                if (read) {
                    read = false;
                    pipeline.fireInboundBufferUpdated();
                }
                pipeline().fireExceptionCaught(t);
                if (t instanceof IOException) {
                    close(voidFuture());
                }
            } finally {
                if (read) {
                    pipeline.fireInboundBufferUpdated();
                }
                if (closed) {
                    setInputShutdown();
                    if (isOpen()) {
                        if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
                            suspendReadTask.run();
                            pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                        } else {
                            close(voidFuture());
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void doFlushByteBuffer(ByteBuf buf) throws Exception {
        if (!buf.readable()) {
            // Reset reader/writerIndex to 0 if the buffer is empty.
            buf.clear();
            return;
        }

        for (int i = config().getWriteSpinCount() - 1; i >= 0; i --) {
            int localFlushedAmount = doWriteBytes(buf, i == 0);
            if (localFlushedAmount > 0) {
                break;
            }
            if (!buf.readable()) {
                // Reset reader/writerIndex to 0 if the buffer is empty.
                buf.clear();
                break;
            }
        }
    }

    protected abstract int doReadBytes(ByteBuf buf) throws Exception;
    protected abstract int doWriteBytes(ByteBuf buf, boolean lastSpin) throws Exception;

    // 0 - not expanded because the buffer is writable
    // 1 - expanded because the buffer was not writable
    // 2 - could not expand because the buffer was at its maximum although the buffer is not writable.
    private static int expandReadBuffer(ByteBuf byteBuf) {
        final int writerIndex = byteBuf.writerIndex();
        final int capacity = byteBuf.capacity();
        if (capacity != writerIndex) {
            return 0;
        }

        final int maxCapacity = byteBuf.maxCapacity();
        if (capacity == maxCapacity) {
            return 2;
        }

        // FIXME: Magic number
        final int increment = 4096;

        if (writerIndex + increment > maxCapacity) {
            // Expand to maximum capacity.
            byteBuf.capacity(maxCapacity);
        } else {
            // Expand by the increment.
            byteBuf.ensureWritableBytes(increment);
        }

        return 1;
    }
}