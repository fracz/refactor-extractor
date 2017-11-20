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

import uk.co.real_logic.aeron.util.collections.Long2ObjectHashMap;
import uk.co.real_logic.aeron.util.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.util.protocol.HeaderFlyweight;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Frame processing for receivers
 */
public class RcvFrameHandler implements FrameHandler, AutoCloseable
{
    private final UdpTransport transport;
    private final UdpDestination destination;
    private final Long2ObjectHashMap<RcvChannelState> channelInterestMap;
    private final ReceiverThread receiverThread;

    public RcvFrameHandler(final UdpDestination destination,
                           final ReceiverThread receiverThread)
        throws Exception
    {
        final InetSocketAddress endpoint = destination.remote();
        final InetAddress mcastInterface = destination.local().getAddress();
        final int localPort = destination.local().getPort();

        this.transport = new UdpTransport(this, endpoint, mcastInterface, localPort, receiverThread);
        this.destination = destination;
        this.channelInterestMap = new Long2ObjectHashMap<>();
        this.receiverThread = receiverThread;
    }

    public int sendTo(final ByteBuffer buffer, final long sessionId, final long channelId) throws Exception
    {
        final RcvChannelState channel = channelInterestMap.get(channelId);
        if (null == channel)
        {
            return 0;
        }

        final RcvSessionState session = channel.getSessionState(sessionId);
        if (null == session)
        {
            return 0;
        }

        return sendTo(buffer, session.sourceAddress());
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

    public void addChannels(final long[] channelIdList)
    {
        for (final long channelId : channelIdList)
        {
            final RcvChannelState channel = channelInterestMap.get(channelId);

            if (null != channel)
            {
                channel.incrementReference();
            }
            else
            {
                channelInterestMap.put(channelId, new RcvChannelState(channelId));
            }
        }
    }

    public void removeChannels(final long[] channelIdList)
    {
        for (final long channelId : channelIdList)
        {
            final RcvChannelState channel = channelInterestMap.get(channelId);

            if (null != channel)
            {
                if (channel.decrementReference() == 0)
                {
                    channelInterestMap.remove(channelId);
                }
            }
        }
    }

    public int channelCount()
    {
        return channelInterestMap.size();
    }

    public void onDataFrame(final DataHeaderFlyweight header, final InetSocketAddress srcAddr)
    {
        final long sessionId = header.sessionId();
        final long channelId = header.channelId();
        final long termId = header.termId();

        final RcvChannelState channelState = channelInterestMap.get(channelId);
        if (null == channelState)
        {
            return;  // not interested in this channel at all
        }

        final RcvSessionState sessionState = channelState.getSessionState(sessionId);
        if (null != sessionState)
        {
            final ByteBuffer termBuffer = sessionState.termBuffer(termId);
            if (null != termBuffer)
            {
                // TODO: process the Data by placing it in the Term Buffer (hot path!)
                // TODO: loss detection not done in this thread. Done in adminThread
                return;
            }
        }
        else
        {
            // new session, so make it here and save srcAddr
            channelState.createSessionState(sessionId, srcAddr);
            // TODO: this is a new source, so go through message exchange, etc.
        }

        // ask admin thread to create buffer for destination, sessionId, channelId, and termId
        receiverThread.addRcvCreateTermBufferEvent(destination, sessionId, channelId, termId);
    }

    public void onControlFrame(final HeaderFlyweight header, final InetSocketAddress srcAddr)
    {
        /* TODO:
           NAK - back-off any pending NAK - senderThread
           SM - ignore
         */
    }

    // called by the receiver thread when event comes from admin thread that it created a term buffer
    private void onTermBufferCreatedEvent(final long sessionId,
                                          final long channelId,
                                          final long termId)
    {
        final RcvChannelState channelState = channelInterestMap.get(channelId);
        if (null == channelState)
        {
            // should not happen
            // TODO: log this and return
            return;
        }

        final RcvSessionState sessionState = channelState.getSessionState(sessionId);
        if (null == sessionState)
        {
            // should also not happen as it should have been added when we first saw the sessionId
            // TODO: log this and return
            return;
        }

        // TODO: change this to: bufferManagementStrategy.lookupReceiverTerm(destination, sessionId, channelId, termId)
        final ByteBuffer termBuffer = null;

        sessionState.termBuffer(termId, termBuffer);

        // at this point, we are open for business
    }
}