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

import uk.co.real_logic.aeron.util.AtomicArray;
import uk.co.real_logic.aeron.util.collections.Long2ObjectHashMap;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.util.event.EventCode;
import uk.co.real_logic.aeron.util.event.EventLogger;
import uk.co.real_logic.aeron.util.protocol.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Frame processing for data
 */
public class DataFrameHandler implements FrameHandler, AutoCloseable
{
    private static final EventLogger LOGGER = new EventLogger(DataFrameHandler.class);

    private final UdpTransport transport;
    private final UdpDestination destination;
    private final Long2ObjectHashMap<Subscription> subscriptionByChannelIdMap = new Long2ObjectHashMap<>();
    private final MediaConductorProxy conductorProxy;
    private final AtomicArray<SubscribedSession> subscribedSessions;
    private final ByteBuffer smBuffer = ByteBuffer.allocateDirect(StatusMessageFlyweight.HEADER_LENGTH);
    private final ByteBuffer nakBuffer = ByteBuffer.allocateDirect(NakFlyweight.HEADER_LENGTH);
    private final StatusMessageFlyweight smHeader = new StatusMessageFlyweight();
    private final NakFlyweight nakHeader = new NakFlyweight();

    public DataFrameHandler(final UdpDestination destination,
                            final NioSelector nioSelector,
                            final MediaConductorProxy conductorProxy,
                            final AtomicArray<SubscribedSession> subscribedSessions)
        throws Exception
    {
        this.subscribedSessions = subscribedSessions;
        this.transport = new UdpTransport(this, destination, nioSelector);
        this.destination = destination;
        this.conductorProxy = conductorProxy;
    }

    public void close()
    {
        transport.close();
    }

    public UdpDestination destination()
    {
        return destination;
    }

    public Long2ObjectHashMap<Subscription> subscriptionMap()
    {
        return subscriptionByChannelIdMap;
    }

    public void addSubscriptions(final long[] channelIds)
    {
        for (final long channelId : channelIds)
        {
            Subscription subscription = subscriptionByChannelIdMap.get(channelId);

            if (null == subscription)
            {
                subscription = new Subscription(destination, channelId, conductorProxy, subscribedSessions);
                subscriptionByChannelIdMap.put(channelId, subscription);
            }

            subscription.incRef();
        }
    }

    public void removeSubscriptions(final long[] channelIds)
    {
        for (final long channelId : channelIds)
        {
            final Subscription subscription = subscriptionByChannelIdMap.get(channelId);

            if (subscription == null)
            {
                throw new SubscriptionNotRegisteredException("No subscription registered on " + channelId);
            }

            if (subscription.decRef() == 0)
            {
                subscriptionByChannelIdMap.remove(channelId);
                subscription.close();
            }
        }
    }

    public int subscribedChannelCount()
    {
        return subscriptionByChannelIdMap.size();
    }

    public void onDataFrame(final DataHeaderFlyweight header,
                            final AtomicBuffer buffer,
                            final long length,
                            final InetSocketAddress srcAddress)
    {
        final long channelId = header.channelId();
        final Subscription subscription = subscriptionByChannelIdMap.get(channelId);

        if (null != subscription)
        {
            final long sessionId = header.sessionId();
            final long termId = header.termId();
            final SubscribedSession subscribedSession = subscription.getSubscribedSession(sessionId);

            if (null != subscribedSession)
            {
                if (header.frameLength() > DataHeaderFlyweight.HEADER_LENGTH)
                {
                    subscribedSession.rebuildBuffer(header, buffer, length);
                }
            }
            else
            {
                subscription.createSubscribedSession(sessionId, srcAddress);
                conductorProxy.createTermBuffer(destination(), sessionId, channelId, termId);
            }
        }
    }

    public void onStatusMessageFrame(final StatusMessageFlyweight header,
                                     final AtomicBuffer buffer,
                                     final long length,
                                     final InetSocketAddress srcAddress)
    {
        // this should be on the data channel and shouldn't include SMs, so ignore.
    }

    public void onNakFrame(final NakFlyweight header, final AtomicBuffer buffer,
                           final long length, final InetSocketAddress srcAddress)
    {
        // this should be on the data channel and shouldn't include Naks, so ignore.
    }

    public void onSubscriptionReady(final NewReceiveBufferEvent event, final LossHandler lossHandler)
    {
        final Subscription subscription = subscriptionByChannelIdMap.get(event.channelId());
        if (null == subscription)
        {
            throw new IllegalStateException("channel not found");
        }

        final SubscribedSession subscriberSession = subscription.getSubscribedSession(event.sessionId());
        if (null == subscriberSession)
        {
            throw new IllegalStateException("session not found");
        }

        lossHandler.sendNakHandler(
                (termId, termOffset, length) -> sendNak(subscriberSession, (int)termId, termOffset, length));

        subscriberSession.termBuffer(event.termId(), event.bufferRotator(), lossHandler);

        // now we are all setup, so send an SM to allow the source to send if it is waiting
        // TODO: grab initial term offset from data and store in subscriberSession somehow (per TermID)
        // TODO: need a strategy object to track the initial receiver window to send in the SMs.
        sendStatusMessage(subscriberSession, event.termId(), 0, 1000);
    }

    private void sendStatusMessage(final SubscribedSession session, final long termId,
                                   final int termOffset, final int window)
    {
        smHeader.wrap(smBuffer, 0);
        smHeader.sessionId(session.sessionId())
                .channelId(session.channelId())
                .termId(termId)
                .highestContiguousTermOffset(termOffset)
                .receiverWindow(window)
                .headerType(HeaderFlyweight.HDR_TYPE_SM)
                .frameLength(StatusMessageFlyweight.HEADER_LENGTH)
                .flags((byte) 0)
                .version(HeaderFlyweight.CURRENT_VERSION);

        smBuffer.position(0);
        smBuffer.limit(smHeader.frameLength());

        LOGGER.log(EventCode.FRAME_OUT, smBuffer, smHeader.frameLength());

        try
        {
            if (transport.sendTo(smBuffer, controlAddress(session)) < smHeader.frameLength())
            {
                throw new IllegalStateException("could not send all of SM");
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void sendNak(final SubscribedSession session, final int termId, final int termOffset, final int length)
    {
        nakHeader.wrap(nakBuffer, 0);
        nakHeader.channelId(session.channelId())
                 .sessionId(session.sessionId())
                 .termId(termId)
                 .termOffset(termOffset)
                 .length(length)
                 .frameLength(NakFlyweight.HEADER_LENGTH)
                 .headerType(HeaderFlyweight.HDR_TYPE_NAK)
                 .flags((byte)0)
                 .version(HeaderFlyweight.CURRENT_VERSION);

        nakBuffer.position(0);
        nakBuffer.limit(nakHeader.frameLength());

        LOGGER.log(EventCode.FRAME_OUT, nakBuffer, nakHeader.frameLength());

        try
        {
            if (transport.sendTo(nakBuffer, controlAddress(session)) < nakHeader.frameLength())
            {
                throw new IllegalStateException("could not send all of NAK");
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private InetSocketAddress controlAddress(final SubscribedSession session)
    {
        if (transport.isMulticast())
        {
            return destination().remoteControl();
        }

        return session.sourceAddress();
    }
}