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
package uk.co.real_logic.aeron.util.concurrent.logbuffer;

import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;

import static uk.co.real_logic.aeron.util.BitUtil.align;
import static uk.co.real_logic.aeron.util.concurrent.logbuffer.FrameDescriptor.*;
import static uk.co.real_logic.aeron.util.concurrent.logbuffer.LogBufferDescriptor.*;

/**
 * Log buffer appender which supports many producers concurrently writing an append-only log.
 *
 * Messages are appending to a log using a framing protocol as described in {@link FrameDescriptor}.
 * If a message is larger than what will fit in a single frame will be fragmented up to {@link #maxMessageLength()}.
 */
public class LogBufferAppender
{
    private final AtomicBuffer logBuffer;
    private final AtomicBuffer stateBuffer;
    private final BaseMessageHeaderFlyweight header;
    private final byte[] defaultHeader;
    private final int capacity;
    private final int maxMessageLength;
    private final int maxFrameLength;

    /**
     * Construct a view over a log buffer and state buffer for appending frames.
     *
     * @param logAtomicBuffer for where events are stored.
     * @param stateAtomicBuffer for where the state of writers is stored manage concurrency.
     * @param header flyweight for manipulating the message header.
     * @param defaultHeader to be applied for each frame logged.
     */
    public LogBufferAppender(final AtomicBuffer logAtomicBuffer,
                             final AtomicBuffer stateAtomicBuffer,
                             final BaseMessageHeaderFlyweight header,
                             final byte[] defaultHeader,
                             final int maxFrameLength)
    {
        checkLogBuffer(logAtomicBuffer);
        checkStateBuffer(stateAtomicBuffer);
        checkDefaultHeader(defaultHeader);
        checkMaxFrameLength(maxFrameLength);

        this.logBuffer = logAtomicBuffer;
        this.stateBuffer = stateAtomicBuffer;
        this.capacity = logAtomicBuffer.capacity();
        this.header = header;
        this.defaultHeader = defaultHeader;
        this.maxFrameLength = maxFrameLength;
        this.maxMessageLength = FrameDescriptor.calculateMaxMessageLength(capacity);
    }

    /**
     * The capacity of the underlying log buffer.
     *
     * @return the capacity of the underlying log buffer.
     */
    public int capacity()
    {
        return capacity;
    }

    /**
     * The maximum length of a message that can be recorded in the log.
     *
     * @return the maximum length of a message that can be recorded in the log.
     */
    public int maxMessageLength()
    {
        return maxMessageLength;
    }

    /**
     * The maximum length of a frame, including header, that can be recorded in the log.
     *
     * @return the maximum length of a frame, including header, that can be recorded in the log.
     */
    public int maxFrameLength()
    {
        return maxFrameLength;
    }

    /**
     * Get the current value of the tail counter for the log.
     *
     * @return the current value of the tail counter for the log.
     */
    public int tail()
    {
        final int tail = stateBuffer.getIntVolatile(TAIL_COUNTER_OFFSET);
        return Math.min(tail, capacity);
    }

    /**
     * Append a message to the log if sufficient capacity exists.
     *
     * @param srcBuffer containing the encoded message.
     * @param srcOffset at which the encoded message begins.
     * @param length of the message in bytes.
     * @return true if appended in the log otherwise false if the capacity is exhausted.
     * @throws IllegalArgumentException if the length is greater than {@link #maxMessageLength()}
     */
    public boolean append(final AtomicBuffer srcBuffer, final int srcOffset, final int length)
    {
        checkMessageLength(length);

        final int frameLength = align(length + defaultHeader.length, FRAME_ALIGNMENT);
        final int frameOffset = getTailAndAdd(frameLength);

        if (frameOffset >= capacity)
        {
            return false;
        }

        if (frameOffset + frameLength > capacity)
        {
            appendPaddingFrame(frameOffset);
            return false;
        }

        logBuffer.putBytes(frameOffset, defaultHeader, 0, defaultHeader.length);
        logBuffer.putBytes(frameOffset + defaultHeader.length, srcBuffer, srcOffset, length);

        header.wrap(logBuffer, frameOffset)
              .beginFragment(true)
              .endFragment(true)
              .sequenceNumber(frameOffset)
              .putLengthOrdered(frameLength);

        return true;
    }

    private void appendPaddingFrame(final int frameOffset)
    {
        logBuffer.putBytes(frameOffset, defaultHeader, 0, defaultHeader.length);

        header.wrap(logBuffer, frameOffset)
              .beginFragment(true)
              .endFragment(true)
              .sequenceNumber(frameOffset)
              .putLengthOrdered(capacity - frameOffset);
    }

    private int getTailAndAdd(final int delta)
    {
        return stateBuffer.getAndAddInt(TAIL_COUNTER_OFFSET, delta);
    }

    private void checkMessageLength(final int length)
    {
        if (length > maxMessageLength)
        {
            final String msg = String.format("encoded event exceeds maxMessageLength of %d, length=%d",
                                             Integer.valueOf(maxMessageLength), Integer.valueOf(length));

            throw new IllegalArgumentException(msg);
        }
    }
}