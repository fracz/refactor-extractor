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
package uk.co.real_logic.aeron.driver;

import uk.co.real_logic.aeron.common.TermHelper;
import uk.co.real_logic.aeron.common.TimerWheel;
import uk.co.real_logic.aeron.common.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.FrameDescriptor;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.LogBuffer;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.LogScanner;
import uk.co.real_logic.aeron.common.event.EventCode;
import uk.co.real_logic.aeron.common.event.EventLogger;
import uk.co.real_logic.aeron.common.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.common.protocol.HeaderFlyweight;
import uk.co.real_logic.aeron.common.status.BufferPositionReporter;
import uk.co.real_logic.aeron.driver.buffer.RawLog;
import uk.co.real_logic.aeron.driver.buffer.TermBuffers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static uk.co.real_logic.aeron.common.BitUtil.align;
import static uk.co.real_logic.aeron.common.TermHelper.termIdToBufferIndex;
import static uk.co.real_logic.aeron.common.concurrent.logbuffer.LogBufferDescriptor.*;

/**
 * Publication to be sent to registered subscribers.
 */
public class DriverPublication implements AutoCloseable
{
    /** Initial heartbeat timeout (cancelled by SM) */
    public static final int INITIAL_HEARTBEAT_TIMEOUT_MS = 100;
    public static final long INITIAL_HEARTBEAT_TIMEOUT_NS = MILLISECONDS.toNanos(INITIAL_HEARTBEAT_TIMEOUT_MS);

    /** Heartbeat after data sent */
    public static final int HEARTBEAT_TIMEOUT_MS = 500;
    public static final long HEARTBEAT_TIMEOUT_NS = MILLISECONDS.toNanos(HEARTBEAT_TIMEOUT_MS);

    private final TimerWheel timerWheel;

    private final int sessionId;
    private final int streamId;

    private final AtomicInteger activeTermId;

    private final AtomicLong timeOfLastSendOrHeartbeat;
    private long timeOfLastKeepaliveFromClient;

    private final int headerLength;
    private final int mtuLength;
    private final ByteBuffer scratchByteBuffer = ByteBuffer.allocateDirect(DataHeaderFlyweight.HEADER_LENGTH);
    private final AtomicBuffer scratchAtomicBuffer = new AtomicBuffer(scratchByteBuffer);
    private final LogScanner[] logScanners = new LogScanner[TermHelper.BUFFER_COUNT];
    private final RetransmitHandler[] retransmitHandlers = new RetransmitHandler[TermHelper.BUFFER_COUNT];

    private final ByteBuffer[] sendBuffers = new ByteBuffer[TermHelper.BUFFER_COUNT];
    private final ByteBuffer[] retransmitBuffers = new ByteBuffer[TermHelper.BUFFER_COUNT];

    private final SenderControlStrategy controlStrategy;
    private final AtomicLong positionLimit;
    private final SendChannelEndpoint mediaEndpoint;
    private final TermBuffers termBuffers;
    private final BufferPositionReporter limitReporter;

    private final DataHeaderFlyweight dataHeader = new DataHeaderFlyweight();
    private final DataHeaderFlyweight retransmitDataHeader = new DataHeaderFlyweight();
    private final DataHeaderFlyweight sendDataHeader = new DataHeaderFlyweight();

    private final int positionBitsToShift;
    private final int initialTermId;
    private final EventLogger logger;

    private int nextTermOffset = 0;
    private int activeIndex = 0;
    private int statusMessagesSeen = 0;
    private long nextOffsetPosition = 0;

    private final InetSocketAddress dstAddress;

    public DriverPublication(final SendChannelEndpoint mediaEndpoint,
                             final TimerWheel timerWheel,
                             final SenderControlStrategy controlStrategy,
                             final TermBuffers termBuffers,
                             final BufferPositionReporter limitReporter,
                             final int sessionId,
                             final int streamId,
                             final int initialTermId,
                             final int headerLength,
                             final int mtuLength,
                             final EventLogger logger)
    {
        this.mediaEndpoint = mediaEndpoint;
        this.termBuffers = termBuffers;
        this.logger = logger;
        this.dstAddress = mediaEndpoint.udpChannel().remoteData();
        this.controlStrategy = controlStrategy;
        this.timerWheel = timerWheel;
        this.limitReporter = limitReporter;
        this.sessionId = sessionId;
        this.streamId = streamId;
        this.headerLength = headerLength;
        this.mtuLength = mtuLength;
        this.activeIndex = termIdToBufferIndex(initialTermId);

        final RawLog[] rawLogs = termBuffers.buffers();
        for (int i = 0; i < rawLogs.length; i++)
        {
            logScanners[i] = newScanner(rawLogs[i]);
            sendBuffers[i] = duplicateLogBuffer(rawLogs[i]);
            retransmitBuffers[i] = duplicateLogBuffer(rawLogs[i]);
            retransmitHandlers[i] = newRetransmitHandler(rawLogs[i]);
        }

        final int termCapacity = logScanners[0].capacity();
        positionLimit = new AtomicLong(controlStrategy.initialPositionLimit(initialTermId, termCapacity));

        activeTermId = new AtomicInteger(initialTermId);

        final long now = timerWheel.now();
        timeOfLastSendOrHeartbeat = new AtomicLong(now);
        timeOfLastKeepaliveFromClient = now;

        this.positionBitsToShift = Integer.numberOfTrailingZeros(termCapacity);
        this.initialTermId = initialTermId;
        limitReporter.position(termCapacity / 2);
    }

    public void close()
    {
        termBuffers.close();
        limitReporter.close();
    }

    public int send()
    {
        int workCount = 0;
        try
        {
            final int availableWindow = (int)(positionLimit.get() - nextOffsetPosition);
            final int scanLimit = Math.min(availableWindow, mtuLength);

            LogScanner scanner = logScanners[activeIndex];
            workCount += scanner.scanNext(this::onSendFrame, scanLimit);

            if (scanner.isComplete())
            {
                activeIndex = TermHelper.rotateNext(activeIndex);
                activeTermId.lazySet(activeTermId.get() + 1);
                scanner = logScanners[activeIndex];
                scanner.seek(0);
            }

            limitReporter.position(calculatePosition(scanner.offset()) + scanner.capacity());
        }
        catch (final Exception ex)
        {
            logger.logException(ex);
        }

        return workCount;
    }

    public SendChannelEndpoint sendChannelEndpoint()
    {
        return mediaEndpoint;
    }

    public int sessionId()
    {
        return sessionId;
    }

    public int streamId()
    {
        return streamId;
    }

    /**
     * This is performed on the {@link DriverConductor} thread
     */
    public void onStatusMessage(final int termId,
                                final int highestContiguousTermOffset,
                                final int receiverWindowSize,
                                final InetSocketAddress address)
    {
        positionLimit.lazySet(
            controlStrategy.onStatusMessage(termId, highestContiguousTermOffset, receiverWindowSize, address));
        statusMessagesSeen++;
    }

    /**
     * This is performed on the {@link DriverConductor} thread
     */
    public void onNakFrame(final int termId, final long termOffset, final long length)
    {
        final int index = determineIndexByTermId(termId);

        if (-1 != index)
        {
            retransmitHandlers[index].onNak((int)termOffset, (int)length);
        }
    }

    /**
     * This is performed on the {@link DriverConductor} thread
     */
    public boolean heartbeatCheck()
    {
        final long timeout = statusMessagesSeen > 0 ? HEARTBEAT_TIMEOUT_NS : INITIAL_HEARTBEAT_TIMEOUT_NS;
        final long timeSinceLastHeartbeat = timerWheel.now() - timeOfLastSendOrHeartbeat.get();
        if (timeSinceLastHeartbeat > timeout)
        {
            sendHeartbeat();
            return true;
        }

        return false;
    }

    /**
     * This is performed on the {@link DriverConductor} thread
     */
    public int cleanLogBuffer()
    {
        for (final LogBuffer logBuffer : logScanners)
        {
            if (logBuffer.status() == NEEDS_CLEANING && logBuffer.compareAndSetStatus(NEEDS_CLEANING, IN_CLEANING))
            {
                logBuffer.clean();

                return 1;
            }
        }

        return 0;
    }

    public long timeOfLastKeepaliveFromClient()
    {
        return timeOfLastKeepaliveFromClient;
    }

    public void timeOfLastKeepaliveFromClient(final long time)
    {
        timeOfLastKeepaliveFromClient = time;
    }

    private ByteBuffer duplicateLogBuffer(final RawLog log)
    {
        final ByteBuffer buffer = log.logBuffer().duplicateByteBuffer();
        buffer.clear();

        return buffer;
    }

    private LogScanner newScanner(final RawLog log)
    {
        return new LogScanner(log.logBuffer(), log.stateBuffer(), headerLength);
    }

    private RetransmitHandler newRetransmitHandler(final RawLog rawLog)
    {
        return new RetransmitHandler(new LogScanner(rawLog.logBuffer(), rawLog.stateBuffer(), headerLength),
                                     timerWheel,
                                     DriverConductor.RETRANS_UNICAST_DELAY_GENERATOR,
                                     DriverConductor.RETRANS_UNICAST_LINGER_GENERATOR,
                                     this::onSendRetransmit,
                                     mtuLength);
    }

    private int determineIndexByTermId(final int termId)
    {
        if (termId == activeTermId.get())
        {
            return activeIndex;
        }
        else if (termId == activeTermId.get() - 1)
        {
            return TermHelper.rotatePrevious(activeIndex);
        }

        return -1;
    }

    /*
     * Function used as a lambda for LogScanner.AvailabilityHandler
     */
    private void onSendFrame(final AtomicBuffer buffer, final int offset, final int length)
    {
        // at this point sendBuffer wraps the same underlying
        // ByteBuffer as the buffer parameter
        final ByteBuffer sendBuffer = sendBuffers[activeIndex];

        sendBuffer.limit(offset + length);
        sendBuffer.position(offset);

        // if we have a 0 length data message, it is most likely padding, so we will want to adjust it before sending.
        // a padding frame may be embedded in a batch that is received. Don't need to adjust that one.
        if (DataHeaderFlyweight.HEADER_LENGTH == length)
        {
            sendDataHeader.wrap(buffer, offset);

            if (sendDataHeader.headerType() == PADDING_FRAME_TYPE)
            {
                final short flags = (short)(sendDataHeader.flags() | DataHeaderFlyweight.PADDING_FLAG);

                sendDataHeader.flags(flags);
                sendDataHeader.headerType(HeaderFlyweight.HDR_TYPE_DATA);
                // the frameLength field will be the length of the padding. But the PADDING flag tells the receiver
                // what to do.
            }
        }

        try
        {
            final int bytesSent = mediaEndpoint.sendTo(sendBuffer, dstAddress);
            if (length != bytesSent)
            {
                throw new IllegalStateException("could not send all of message: " + bytesSent + "/" + length);
            }

            updateTimeOfLastSendOrHeartbeat(timerWheel.now());

            nextTermOffset = align(offset + length, FrameDescriptor.FRAME_ALIGNMENT);

            nextOffsetPosition =
                TermHelper.calculatePosition(activeTermId.get(), nextTermOffset, positionBitsToShift, initialTermId);
        }
        catch (final Exception ex)
        {
            logger.logException(ex);
        }
    }

    /**
     * This is performed on the {@link DriverConductor} thread via the RetransmitHandler
     */
    private void onSendRetransmit(final AtomicBuffer buffer, final int offset, final int length)
    {
        // use retransmitBuffers, but need to know which one... so, use DataHeaderFlyweight to grab it
        retransmitDataHeader.wrap(buffer, offset);
        final int index = determineIndexByTermId(retransmitDataHeader.termId());

        if (-1 != index)
        {
            final ByteBuffer termRetransmitBuffer = retransmitBuffers[index];
            termRetransmitBuffer.position(offset);
            termRetransmitBuffer.limit(offset + length);

            try
            {
                final int bytesSent = mediaEndpoint.sendTo(termRetransmitBuffer, dstAddress);
                if (bytesSent != length)
                {
                    logger.log(EventCode.COULD_NOT_FIND_INTERFACE, termRetransmitBuffer, offset, length, dstAddress);
                }
            }
            catch (final Exception ex)
            {
                logger.logException(ex);
            }
        }
    }

    private void sendHeartbeat()
    {
        // called from conductor thread on timeout

        // used for both initial setup 0 length data as well as heartbeats

        // send 0 length data frame with current termOffset
        dataHeader.wrap(scratchAtomicBuffer, 0);

        dataHeader.sessionId(sessionId)
                  .streamId(streamId)
                  .termId(activeTermId.get())
                  .termOffset(nextTermOffset)
                  .frameLength(DataHeaderFlyweight.HEADER_LENGTH)
                  .headerType(HeaderFlyweight.HDR_TYPE_DATA)
                  .flags(DataHeaderFlyweight.BEGIN_AND_END_FLAGS)
                  .version(HeaderFlyweight.CURRENT_VERSION);

        scratchByteBuffer.position(0);
        scratchByteBuffer.limit(DataHeaderFlyweight.HEADER_LENGTH);

        try
        {
            final int bytesSent = mediaEndpoint.sendTo(scratchByteBuffer, dstAddress);
            if (DataHeaderFlyweight.HEADER_LENGTH != bytesSent)
            {
                logger.log(EventCode.ERROR_SENDING_HEARTBEAT_PACKET, scratchByteBuffer, 0,
                           DataHeaderFlyweight.HEADER_LENGTH, dstAddress);
            }

            updateTimeOfLastSendOrHeartbeat(timerWheel.now());
        }
        catch (final Exception ex)
        {
            logger.logException(ex);
        }
    }

    private long calculatePosition(final int currentTail)
    {
        return TermHelper.calculatePosition(activeTermId.get(), currentTail, positionBitsToShift, initialTermId);
    }

    private void updateTimeOfLastSendOrHeartbeat(final long time)
    {
        timeOfLastSendOrHeartbeat.lazySet(time);
    }
}