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

import uk.co.real_logic.aeron.util.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.util.protocol.HeaderFlyweight;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Frame processing for sources
 */
public class SrcFrameHandler implements FrameHandler, AutoCloseable
{
    private final UdpTransport transport;
    private final UdpDestination destination;

    public SrcFrameHandler(final UdpDestination destination, final ReceiverThread loop) throws Exception
    {
        this.transport = new UdpTransport(this, destination.local(), loop);
        this.destination = destination;
    }

    public int send(final ByteBuffer buffer) throws Exception
    {
        return transport.sendTo(buffer, destination.remote());
    }

    public int sendTo(final ByteBuffer buffer, final InetSocketAddress addr) throws Exception
    {
        return transport.sendTo(buffer, addr);
    }

    public void close()
    {
        transport.close();
    }

    public UdpDestination destination()
    {
        return destination;
    }

    public void addSessionAndChannel(final long sessionId, final long channelId, final ByteBuffer buffer)
    {
        // TODO: this is called from the AdminAgent thread, so probably need to handle it by
    }

    public void removeSessionAndChannel(final long sessionId, final long channelId)
    {
        // TODO: this is called from the AdminAgent thread
        // TODO: the AdminAgent thread has removed it from its own destinationMap, so shutdown if needed.
    }

    public void onDataFrame(final DataHeaderFlyweight header, final InetSocketAddress srcAddr)
    {
        // we don't care, so just drop it silently.
    }

    public void onControlFrame(final HeaderFlyweight header, final InetSocketAddress srcAddr)
    {
        /* TODO:
           NAK - send retransmission, checking state of ignore timeout, checking that it is for us
           FCR - adjust what can be sent, perhaps unblock send thread
           Channel Announcement/Advertisement - ignore
         */
    }
}