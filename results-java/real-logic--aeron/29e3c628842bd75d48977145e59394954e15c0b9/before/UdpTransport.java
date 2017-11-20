/*
 * Copyright 2014 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.mediadriver;

import uk.co.real_logic.aeron.util.DataHeaderFlyweight;
import uk.co.real_logic.aeron.util.HeaderFlyweight;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

/**
 * Transport abstraction for UDP sources and receivers.
 *
 * We don't conflate the processing logic, or we at least try not to, into this object.
 *
 * Holds DatagramChannel, read Buffer, etc.
 */
public final class UdpTransport implements ReadHandler, AutoCloseable
{
    public static final int READ_BYTE_BUFFER_SZ = 4096; // TODO: this needs to be configured in some way

    private final ByteBuffer readByteBuffer;
    private final AtomicBuffer readBuffer;
    private final DatagramChannel channel;
    private final HeaderFlyweight header;
    private final DataHeaderFlyweight dataHeader;
    private final FrameHandler frameHandler;
    private final EventLoop eventLoop;
    private final SelectionKey registeredKey;

    public UdpTransport(final FrameHandler frameHandler, final InetSocketAddress local, final EventLoop eventLoop) throws Exception
    {
        this.readByteBuffer = ByteBuffer.allocateDirect(READ_BYTE_BUFFER_SZ);
        this.readBuffer = new AtomicBuffer(this.readByteBuffer);
        this.channel = DatagramChannel.open();
        this.header = new HeaderFlyweight();
        this.dataHeader = new DataHeaderFlyweight();
        this.frameHandler = frameHandler;
        this.eventLoop = eventLoop;
        if (local.getAddress().isMulticastAddress())
        {
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        }
        channel.bind(local);
        channel.configureBlocking(false);
        this.registeredKey = eventLoop.registerForRead(channel, this);
    }

    public int sendTo(final ByteBuffer buffer, final InetSocketAddress remote) throws Exception
    {
        return channel.send(buffer, remote);
    }

    public void close()
    {
        try
        {
            eventLoop.cancelRead(registeredKey);
            channel.close();
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onRead() throws Exception
    {
        readByteBuffer.clear();
        final InetSocketAddress srcAddr = (InetSocketAddress)channel.receive(readByteBuffer);
        final int len = readByteBuffer.position();
        int offset = 0;

        if (srcAddr == null)
        {
            return;
        }

        // parse through buffer for each Frame.
        while (offset < len)
        {
            header.reset(readBuffer, offset);

            // drop a version we don't know
            if (header.version() != HeaderFlyweight.CURRENT_VERSION)
            {
                continue;
            }

            if (header.headerType() == HeaderFlyweight.HDR_TYPE_DATA)
            {
                dataHeader.reset(readBuffer, offset);
                frameHandler.onDataFrame(dataHeader, srcAddr);
            }
            else
            {
                frameHandler.onControlFrame(header, srcAddr);
            }

            offset += header.frameLength();
        }
    }
}