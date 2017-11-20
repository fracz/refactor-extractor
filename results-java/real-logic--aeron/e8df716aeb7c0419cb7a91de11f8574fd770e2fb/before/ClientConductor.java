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
package uk.co.real_logic.aeron.conductor;

import uk.co.real_logic.aeron.MediaDriverTimeoutException;
import uk.co.real_logic.aeron.Publication;
import uk.co.real_logic.aeron.RegistrationException;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.aeron.util.*;
import uk.co.real_logic.aeron.util.collections.ConnectionMap;
import uk.co.real_logic.aeron.util.command.LogBuffersMessageFlyweight;
import uk.co.real_logic.aeron.util.command.PublicationMessageFlyweight;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.util.concurrent.broadcast.CopyBroadcastReceiver;
import uk.co.real_logic.aeron.util.concurrent.logbuffer.LogAppender;
import uk.co.real_logic.aeron.util.concurrent.logbuffer.LogReader;
import uk.co.real_logic.aeron.util.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.util.protocol.ErrorFlyweight;
import uk.co.real_logic.aeron.util.status.BufferPositionIndicator;
import uk.co.real_logic.aeron.util.status.LimitBarrier;
import uk.co.real_logic.aeron.util.status.WindowedLimitBarrier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static uk.co.real_logic.aeron.util.BufferRotationDescriptor.BUFFER_COUNT;
import static uk.co.real_logic.aeron.util.command.ControlProtocolEvents.*;

/**
 * Client conductor takes responses and notifications from media driver and acts on them. As well as passes commands
 * to the media driver.
 */
public class ClientConductor extends Agent
{
    private static final int MAX_FRAME_LENGTH = 1024;

    public static final long AGENT_IDLE_MAX_SPINS = 5000;
    public static final long AGENT_IDLE_MAX_YIELDS = 100;
    public static final long AGENT_IDLE_MIN_PARK_NS = TimeUnit.NANOSECONDS.toNanos(10);
    public static final long AGENT_IDLE_MAX_PARK_NS = TimeUnit.MICROSECONDS.toNanos(100);

    private static final long NO_CORRELATION_ID = -1;

    private final CopyBroadcastReceiver driverBroadcastReceiver;
    private final BufferUsageStrategy bufferUsage;
    private final AtomicArray<Publication> publications = new AtomicArray<>();
    private final AtomicArray<Subscription> subscriptions = new AtomicArray<>();
    private final ConductorErrorHandler errorHandler;
    private final long awaitTimeout;
    private final long publicationWindow;
    private final ConnectionMap<String, Publication> publicationMap = new ConnectionMap<>(); // Guarded by this
    private final SubscriptionMap subscriptionMap = new SubscriptionMap();

    private final LogBuffersMessageFlyweight logBuffersMessage = new LogBuffersMessageFlyweight();

    private final PublicationMessageFlyweight publicationMessage = new PublicationMessageFlyweight();
    private final ErrorFlyweight errorHeader = new ErrorFlyweight();

    private final AtomicBuffer counterValuesBuffer;
    private final MediaDriverProxy mediaDriverProxy;
    private final Signal correlationSignal;

    private long activeCorrelationId; // Guarded by this
    private Publication addedPublication; // Guarded by this
    private RegistrationException registrationException; // Guarded by this

    public ClientConductor(final CopyBroadcastReceiver driverBroadcastReceiver,
                           final ConductorErrorHandler errorHandler,
                           final BufferUsageStrategy bufferUsageStrategy,
                           final AtomicBuffer counterValuesBuffer,
                           final MediaDriverProxy mediaDriverProxy,
                           final Signal correlationSignal,
                           final long awaitTimeout,
                           final long publicationWindow)
    {
        super(new AgentIdleStrategy(AGENT_IDLE_MAX_SPINS, AGENT_IDLE_MAX_YIELDS,
                                    AGENT_IDLE_MIN_PARK_NS, AGENT_IDLE_MAX_PARK_NS));

        this.counterValuesBuffer = counterValuesBuffer;

        this.correlationSignal = correlationSignal;
        this.mediaDriverProxy = mediaDriverProxy;
        this.driverBroadcastReceiver = driverBroadcastReceiver;
        this.bufferUsage = bufferUsageStrategy;
        this.errorHandler = errorHandler;
        this.awaitTimeout = awaitTimeout;
        this.publicationWindow = publicationWindow;
    }

    public int doWork()
    {
        int messageWorkCount = handleMessagesFromMediaDriver();
        return messageWorkCount + performBufferMaintenance();
    }

    public void close()
    {
        stop();
        bufferUsage.close();
    }

    public synchronized Publication addPublication(final String destination, final long channelId, final long sessionId)
    {
        Publication publication = publicationMap.get(destination, sessionId, channelId);

        if (publication == null)
        {
            activeCorrelationId = mediaDriverProxy.addPublication(destination, channelId, sessionId);

            final long startTime = System.currentTimeMillis();
            while (addedPublication == null)
            {
                correlationSignal.await(awaitTimeout);

                checkMediaDriverTimeout(startTime);
                checkRegistrationException();
            }

            publication = addedPublication;
            publicationMap.put(destination, sessionId, channelId, publication);
            addedPublication = null;
            activeCorrelationId = NO_CORRELATION_ID;
        }

        publication.incRef();

        return publication;
    }

    private void checkRegistrationException()
    {
        if (registrationException != null)
        {
            final RegistrationException exception = registrationException;
            registrationException = null;
            throw exception;
        }
    }

    public synchronized void releasePublication(final Publication publication)
    {
        final String destination = publication.destination();
        final long channelId = publication.channelId();
        final long sessionId = publication.sessionId();

        activeCorrelationId = mediaDriverProxy.removePublication(destination, channelId, sessionId);

        // TODO: wait for response from media driver

        // TODO:
        // bufferUsage.releasePublisherBuffers(destination, channelId, sessionId);
    }

    public synchronized Subscription addSubscription(final String destination,
                                                     final long channelId,
                                                     final Subscription.DataHandler handler)
    {

        Subscription subscription = subscriptionMap.get(destination, channelId);

        if (null == subscription)
        {
            subscription = new Subscription(this, handler, destination, channelId);

            subscriptions.add(subscription);
            subscriptionMap.put(destination, channelId, subscription);

            mediaDriverProxy.addSubscription(destination, channelId);
        }

        return subscription;
    }

    public synchronized void releaseSubscription(final Subscription subscription)
    {
        mediaDriverProxy.removeSubscription(subscription.destination(), subscription.channelId());

        subscriptionMap.remove(subscription.destination(), subscription.channelId());
        subscriptions.remove(subscription);

        // TODO: clean up logs
    }

    private int performBufferMaintenance()
    {
        int publicationWork = publications.forEach(0,
            (publication) ->
            {
                final long dirtyTermId = publication.dirtyTermId();
                if (dirtyTermId != Publication.NO_DIRTY_TERM)
                {
                    mediaDriverProxy.requestTerm(publication.destination(),
                                                 publication.sessionId(),
                                                 publication.channelId(),
                                                 dirtyTermId);
                }
                return 1;
            });

        return publicationWork + subscriptions.forEach(0, Subscription::processBufferScan);
    }

    private int handleMessagesFromMediaDriver()
    {
        return driverBroadcastReceiver.receive(
            (msgTypeId, buffer, index, length) ->
            {
                try
                {
                    switch (msgTypeId)
                    {
                        case ON_NEW_CONNECTED_SUBSCRIPTION:
                        case ON_NEW_PUBLICATION:
                            logBuffersMessage.wrap(buffer, index);

                            final String destination = logBuffersMessage.destination();

                            final long sessionId = logBuffersMessage.sessionId();
                            final long channelId = logBuffersMessage.channelId();
                            final long termId = logBuffersMessage.termId();
                            final int positionIndicatorId = logBuffersMessage.positionCounterId();

                            if (msgTypeId == ON_NEW_PUBLICATION)
                            {
                                if (logBuffersMessage.correlationId() != activeCorrelationId)
                                {
                                    break;
                                }

                                onNewPublication(destination, sessionId, channelId, termId, positionIndicatorId);
                            }
                            else
                            {
                                onNewConnectedSubscription(destination, sessionId, channelId, termId);
                            }
                            break;

                        case ERROR_RESPONSE:
                            handleErrorResponse(buffer, index, length);
                            break;

                        default:
                            break;
                    }
                }
                catch (final IOException ex)
                {
                    // TODO: log
                    ex.printStackTrace();
                }
            }
        );
    }

    private void handleErrorResponse(final AtomicBuffer buffer, final int index, final int length)
    {
        errorHeader.wrap(buffer, index);
        final ErrorCode errorCode = errorHeader.errorCode();
        switch (errorCode)
        {
            // Publication errors
            case PUBLICATION_CHANNEL_ALREADY_EXISTS:
            case GENERIC_ERROR_MESSAGE:
            //case INVALID_DESTINATION_IN_PUBLICATION:
            case PUBLICATION_CHANNEL_UNKNOWN:
                if (correlationId(buffer, errorHeader.offendingHeaderOffset()) == activeCorrelationId)
                {
                    registrationException = new RegistrationException(errorCode, errorHeader.errorMessage());
                }
                break;

            default:
                errorHandler.onErrorResponse(buffer, index, length);
                break;
        }
    }

    private long correlationId(final AtomicBuffer buffer, final int offset)
    {
        publicationMessage.wrap(buffer, offset);
        return publicationMessage.correlationId();
    }

    private void onNewPublication(final String destination,
                                  final long sessionId,
                                  final long channelId,
                                  final long termId,
                                  final int positionIndicatorId) throws IOException
    {
        final LogAppender[] logs = new LogAppender[BUFFER_COUNT];
        final AtomicBuffer[] headers = new AtomicBuffer[BUFFER_COUNT];

        for (int i = 0; i < BUFFER_COUNT; i++)
        {
            final AtomicBuffer logBuffer = newBuffer(logBuffersMessage, i);
            final AtomicBuffer stateBuffer = newBuffer(logBuffersMessage, i + BufferRotationDescriptor.BUFFER_COUNT);
            final byte[] header = DataHeaderFlyweight.createDefaultHeader(sessionId, channelId, termId);

            headers[i] = new AtomicBuffer(header);
            logs[i] = new LogAppender(logBuffer, stateBuffer, header, MAX_FRAME_LENGTH);
        }

        final LimitBarrier limit = limitBarrier(positionIndicatorId);
        final Publication publication =
            new Publication(this, destination, channelId, sessionId, termId, headers, logs, limit);
        publications.add(publication);
        addedPublication = publication;

        correlationSignal.signal();
    }

    private void onNewConnectedSubscription(final String destination,
                                            final long sessionId,
                                            final long channelId,
                                            final long currentTermId) throws IOException
    {
        final Subscription subscription = subscriptionMap.get(destination, channelId);
        if (null != subscription && !subscription.isConnected(sessionId))
        {
            final LogReader[] logs = new LogReader[BUFFER_COUNT];
            for (int i = 0; i < BUFFER_COUNT; i++)
            {
                logs[i] = newReader(i);
            }

            subscription.onBuffersMapped(sessionId, currentTermId, logs);
        }
    }

    private void checkMediaDriverTimeout(final long startTime)
        throws MediaDriverTimeoutException
    {
        if ((System.currentTimeMillis() - startTime) > awaitTimeout)
        {
            final String msg = String.format("No response from media driver within %d ms", awaitTimeout);
            throw new MediaDriverTimeoutException(msg);
        }
    }

    private LogReader newReader(final int index) throws IOException
    {
        final AtomicBuffer logBuffer = newBuffer(logBuffersMessage, index);
        final AtomicBuffer stateBuffer = newBuffer(logBuffersMessage, index + BufferRotationDescriptor.BUFFER_COUNT);

        return new LogReader(logBuffer, stateBuffer);
    }

    private AtomicBuffer newBuffer(final LogBuffersMessageFlyweight newBufferMessage, final int index)
        throws IOException
    {
        final String location = newBufferMessage.location(index);
        final int offset = newBufferMessage.bufferOffset(index);
        final int length = newBufferMessage.bufferLength(index);

        return bufferUsage.newBuffer(location, offset, length);
    }

    private LimitBarrier limitBarrier(final int positionIndicatorId)
    {
        final BufferPositionIndicator indicator = new BufferPositionIndicator(counterValuesBuffer, positionIndicatorId);
        return new WindowedLimitBarrier(indicator, publicationWindow);
    }
}