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

import uk.co.real_logic.aeron.admin.ClientAdminThreadCursor;
import uk.co.real_logic.aeron.admin.TermBufferNotifier;
import uk.co.real_logic.aeron.util.AtomicArray;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aeron Channel
 */
public class Channel implements AutoCloseable
{
    private static final long UNKNOWN_TERM_ID = -1L;

    private final String destination;
    private final ClientAdminThreadCursor adminThread;
    private final TermBufferNotifier bufferNotifier;
    private final long channelId;
    private final long sessionId;
    private final AtomicArray<Channel> channels;

    private final AtomicLong currentTermId;

    public Channel(final String destination,
                   final ClientAdminThreadCursor adminCursor,
                   final TermBufferNotifier bufferNotifier,
                   final long channelId,
                   final long sessionId,
                   final AtomicArray<Channel> channels)
    {
        this.destination = destination;
        this.adminThread = adminCursor;
        this.bufferNotifier = bufferNotifier;
        this.channelId = channelId;
        this.sessionId = sessionId;
        this.channels = channels;
        currentTermId = new AtomicLong(UNKNOWN_TERM_ID);
    }

    public long channelId()
    {
        return channelId;
    }

    private boolean hasTerm()
    {
        return currentTermId.get() != UNKNOWN_TERM_ID;
    }

    /**
     * Non blocking message send
     *
     * @param buffer
     * @return
     */
    public boolean offer(final ByteBuffer buffer)
    {
        return offer(buffer, buffer.position(), buffer.remaining());
    }

    public boolean offer(final ByteBuffer buffer, final int offset, final int length)
    {
        if (!hasTerm())
        {
            return false;
        }

        // TODO
        return true;
    }

    public void send(final ByteBuffer buffer) throws BufferExhaustedException
    {
        send(buffer, buffer.position(), buffer.remaining());
    }

    public void send(final ByteBuffer buffer, final int offset, final int length) throws BufferExhaustedException
    {
        if (!hasTerm())
        {
            throw new BufferExhaustedException("Unable to send: awaiting buffer creation");
        }
        // TODO
    }

    public void blockingSend(final ByteBuffer buffer)
    {
        blockingSend(buffer, buffer.position(), buffer.remaining());
    }

    public void blockingSend(final ByteBuffer buffer, final int offset, final int length)
    {
        // TODO: Is this necessary?
    }

    private void requestTerm(final long termId)
    {
        adminThread.sendRequestTerm(channelId, termId, destination);
    }

    private void startTerm()
    {
        bufferNotifier.termBufferBlocking(currentTermId.get());
    }

    private void rollTerm()
    {
        bufferNotifier.endOfTermBuffer(currentTermId.get());
        currentTermId.incrementAndGet();
        requestTerm(currentTermId.get() + 1);
        startTerm();
    }

    public void close() throws Exception
    {
        channels.remove(this);
        adminThread.sendRemoveChannel(destination, channelId);
    }

    public boolean matches(final String destination, final long sessionId, final long channelId)
    {
        return destination.equals(this.destination) && this.sessionId == sessionId && this.channelId == channelId;
    }

    public TermBufferNotifier bufferNotifier()
    {
        return bufferNotifier;
    }

    public void newTermBufferMapped(final long termId, final ByteBuffer buffer)
    {
        bufferNotifier.newTermBufferMapped(termId, buffer);
        if (!hasTerm())
        {
            currentTermId.lazySet(termId);
        }
    }

    public boolean hasSessionId(final long sessionId)
    {
        return this.sessionId == sessionId;
    }
}