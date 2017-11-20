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
package uk.co.real_logic.aeron;

import uk.co.real_logic.aeron.common.TermHelper;
import uk.co.real_logic.aeron.common.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.LogAppender;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.LogBufferDescriptor;
import uk.co.real_logic.aeron.common.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.common.status.PositionIndicator;
import uk.co.real_logic.aeron.conductor.ClientConductor;
import uk.co.real_logic.aeron.conductor.ManagedBuffer;

import java.util.concurrent.atomic.AtomicInteger;

import static uk.co.real_logic.aeron.common.TermHelper.*;

/**
 * Publication end of a channel for publishing messages to subscribers.
 * <p>
 * Note: Publication instances are threadsafe and can be shared between publisher threads.
 */
public class Publication implements AutoCloseable
{
    private final ClientConductor clientConductor;
    private final String channel;
    private final int streamId;
    private final int sessionId;
    private final ManagedBuffer[] managedBuffers;
    private final LogAppender[] logAppenders;
    private final PositionIndicator limit;
    private final AtomicInteger activeTermId;
    private final int positionBitsToShift;
    private final int initialTermId;

    private final DataHeaderFlyweight dataHeader = new DataHeaderFlyweight();

    private int refCount = 1;
    private int activeIndex;

    public Publication(final ClientConductor clientConductor,
                       final String channel,
                       final int streamId,
                       final int sessionId,
                       final int initialTermId,
                       final LogAppender[] logAppenders,
                       final PositionIndicator limit,
                       final ManagedBuffer[] managedBuffers)
    {
        this.clientConductor = clientConductor;

        this.channel = channel;
        this.streamId = streamId;
        this.sessionId = sessionId;
        this.managedBuffers = managedBuffers;
        this.activeTermId = new AtomicInteger(initialTermId);
        this.logAppenders = logAppenders;
        this.limit = limit;
        this.activeIndex = termIdToBufferIndex(initialTermId);

        this.positionBitsToShift = Integer.numberOfTrailingZeros(logAppenders[0].capacity());
        this.initialTermId = initialTermId;
    }

    /**
     * Media address for delivery to the channel.
     *
     * @return Media address for delivery to the channel.
     */
    public String channel()
    {
        return channel;
    }

    /**
     * Stream identity for scoping within the channel media address.
     *
     * @return Stream identity for scoping within the channel media address.
     */
    public int streamId()
    {
        return streamId;
    }

    /**
     * Session under which messages are published.
     *
     * @return the session id for this publication.
     */
    public int sessionId()
    {
        return sessionId;
    }

    public void close()
    {
        release();
    }

    /**
     * Release this reference to the {@link Publication}. To be called by the end using publisher.
     * If all references are released then the associated buffers can be released.
     */
    public void release()
    {
        synchronized (clientConductor)
        {
            if (--refCount == 0)
            {
                clientConductor.releasePublication(this);
                closeBuffers();
            }
        }
    }

    /**
     * Accessed by the client conductor.
     */
    public void incRef()
    {
        synchronized (clientConductor)
        {
            ++refCount;
        }
    }

    /**
     * Non-blocking publish of a buffer containing a message.
     *
     * @param buffer containing message.
     * @return true if buffer is sent otherwise false.
     */
    public boolean offer(final AtomicBuffer buffer)
    {
        return offer(buffer, 0, buffer.capacity());
    }

    /**
     * Non-blocking publish of a partial buffer containing a message.
     *
     * @param buffer containing message.
     * @param offset offset in the buffer at which the encoded message begins.
     * @param length in bytes of the encoded message.
     * @return true if the message can be published otherwise false.
     */
    public boolean offer(final AtomicBuffer buffer, final int offset, final int length)
    {
        final LogAppender logAppender = logAppenders[activeIndex];
        final int currentTail = logAppender.tailVolatile();

        if (isWithinLimit(currentTail))
        {
            switch (logAppender.append(buffer, offset, length))
            {
                case SUCCESS:
                    return true;

                case TRIPPED:
                    nextTerm();
                    return offer(buffer, offset, length);
            }
        }

        return false;
    }

    private void closeBuffers()
    {
        for (final ManagedBuffer managedBuffer : managedBuffers)
        {
            try
            {
                managedBuffer.close();
            }
            catch (final Exception ex)
            {
                throw new IllegalStateException(ex);
            }
        }
    }

    private void nextTerm()
    {
        final int nextIndex = rotateNext(activeIndex);

        final LogAppender nextAppender = logAppenders[nextIndex];
        final int activeTermId = this.activeTermId.get();
        final int newTermId = activeTermId + 1;

        ensureClean(nextAppender);

        dataHeader.wrap(nextAppender.defaultHeader());
        dataHeader.termId(newTermId);

        this.activeTermId.lazySet(newTermId);
        final int previousIndex = rotatePrevious(activeIndex);
        activeIndex = nextIndex;
        logAppenders[previousIndex].statusOrdered(LogBufferDescriptor.NEEDS_CLEANING);
    }

    private boolean isWithinLimit(final int currentTail)
    {
        return position(currentTail) < limit.position();
    }

    private long position(final int currentTail)
    {
        return TermHelper.calculatePosition(activeTermId.get(), currentTail, positionBitsToShift, initialTermId);
    }
}