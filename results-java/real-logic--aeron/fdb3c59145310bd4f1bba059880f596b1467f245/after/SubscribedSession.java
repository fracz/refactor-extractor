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

import uk.co.real_logic.aeron.mediadriver.buffer.BufferRotator;
import uk.co.real_logic.aeron.mediadriver.buffer.LogBuffers;
import uk.co.real_logic.aeron.util.BufferRotationDescriptor;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.util.concurrent.logbuffer.*;
import uk.co.real_logic.aeron.util.protocol.DataHeaderFlyweight;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import static uk.co.real_logic.aeron.util.BufferRotationDescriptor.CLEAN_WINDOW;
import static uk.co.real_logic.aeron.util.BufferRotationDescriptor.UNKNOWN_TERM_ID;

/**
 * State maintained for active sessionIds within a channel for receiver processing
 */
public class SubscribedSession
{
    private final InetSocketAddress srcAddr;
    private final long sessionId;
    private final long channelId;

    private final AtomicLong cleanedTermId = new AtomicLong(UNKNOWN_TERM_ID);
    private final AtomicLong currentTermId = new AtomicLong(UNKNOWN_TERM_ID);
    private int currentBufferId = 0;

    private BufferRotator rotator;
    private TermRebuilder[] rebuilders;
    private LossHandler lossHandler;

    public SubscribedSession(final long sessionId, final long channelId, final InetSocketAddress srcAddr)
    {
        this.srcAddr = srcAddr;
        this.sessionId = sessionId;
        this.channelId = channelId;
    }

    public void termBuffer(final long initialTermId, final BufferRotator rotator, final LossHandler lossHandler)
    {
        cleanedTermId.lazySet(initialTermId + CLEAN_WINDOW);
        currentTermId.lazySet(initialTermId);
        this.rotator = rotator;
        rebuilders = rotator.buffers()
                            .map(TermRebuilder::new)
                            .toArray(TermRebuilder[]::new);
        this.lossHandler = lossHandler;
    }

    public InetSocketAddress sourceAddress()
    {
        return srcAddr;
    }

    public long sessionId()
    {
        return sessionId;
    }

    public long channelId()
    {
        return channelId;
    }

    public void rebuildBuffer(final DataHeaderFlyweight header, final AtomicBuffer buffer, final long length)
    {
        final long termId = header.termId();
        final long currentTermId = this.currentTermId.get();

        if (termId == currentTermId)
        {
            final TermRebuilder rebuilder = rebuilders[currentBufferId];
            rebuilder.insert(buffer, 0, (int)length);
        }
        else if (termId == (currentTermId + 1))
        {
            this.currentTermId.lazySet(termId);
            currentBufferId = BufferRotationDescriptor.rotateId(currentBufferId);
            TermRebuilder rebuilder = rebuilders[currentBufferId];
            while (rebuilder.tailVolatile() != 0)
            {
                // TODO:
                Thread.yield();
            }

            rebuilder.insert(buffer, 0, (int)length);
        }
        else
        {
            // TODO: log or monitor this case
            System.out.println("Unexpected Term Id " + currentTermId + ":" + termId);
        }
    }

    static class TermRebuilder
    {
        private final LogRebuilder logRebuilder;
        private final StateViewer stateViewer;

        public TermRebuilder(final LogBuffers buffer)
        {
            final AtomicBuffer stateBuffer = buffer.stateBuffer();
            stateViewer = new StateViewer(stateBuffer);
            logRebuilder = new LogRebuilder(buffer.logBuffer(), stateBuffer);
        }

        public int tailVolatile()
        {
            return stateViewer.tailVolatile();
        }

        public void insert(final AtomicBuffer buffer, final int offset, final int length)
        {
            logRebuilder.insert(buffer, offset, length);
        }
    }

    /**
     * Called from the MediaConductor.
     *
     * @return if work has been done or not
     */
    public boolean processBufferRotation()
    {
        boolean rotated = false;
        final long currentTermId = this.currentTermId.get();
        final long expectedTermId = currentTermId + CLEAN_WINDOW;
        final long cleanedTermId = this.cleanedTermId.get();

        // TODO: I don't think this works like it should be working... + had to add check on cleanedTermId for unknown
        if (currentTermId != UNKNOWN_TERM_ID && cleanedTermId != UNKNOWN_TERM_ID && expectedTermId > cleanedTermId)
        {
            try
            {
                rotator.rotate();
                this.cleanedTermId.lazySet(cleanedTermId + 1);
                rotated = true;
            }
            catch (final IOException ex)
            {
                // TODO; log
                ex.printStackTrace();
            }
        }

        return rotated;
    }

    /**
     * Called from the MediaConductor.
     *
     * @return if work has been done or not
     */
    public boolean scanForGaps()
    {
        if (null != lossHandler)
        {
            lossHandler.scan();
        }
        // TODO: change return to indicate whether we want to have service soon - would be OK to scan lazily
        return false;
    }
}