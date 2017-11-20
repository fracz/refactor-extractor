/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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

import uk.co.real_logic.aeron.common.FeedbackDelayGenerator;
import uk.co.real_logic.aeron.common.protocol.DataHeaderFlyweight;
import uk.co.real_logic.aeron.driver.buffer.RawLogPartition;
import uk.co.real_logic.agrona.TimerWheel;
import uk.co.real_logic.agrona.concurrent.NanoClock;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.aeron.common.concurrent.logbuffer.LogRebuilder;
import uk.co.real_logic.aeron.common.event.EventLogger;
import uk.co.real_logic.agrona.status.PositionIndicator;
import uk.co.real_logic.agrona.status.PositionReporter;
import uk.co.real_logic.aeron.driver.buffer.RawLog;

import java.net.InetSocketAddress;
import java.util.List;

import static uk.co.real_logic.aeron.common.concurrent.logbuffer.FrameDescriptor.lengthOffset;
import static uk.co.real_logic.aeron.common.concurrent.logbuffer.LogBufferDescriptor.*;
import static uk.co.real_logic.aeron.driver.DriverConnection.Status.ACTIVE;

class DriverConnectionPadding1
{
    protected long p1, p2, p3, p4, p5, p6, p7;
}

class DriverConnectionConductorFields extends DriverConnectionPadding1
{
    protected long completedPosition;
    protected long subscribersPosition;
    protected long timeOfLastStatusChange;
}

class DriverConnectionPadding2 extends DriverConnectionConductorFields
{
    protected long p8, p9, p10, p11, p12, p13, p15;
}

class DriverConnectionHotFields extends DriverConnectionPadding2
{
    protected long lastPacketTimestamp;
    protected long lastStatusMessageTimestamp;
    protected long lastStatusMessagePosition;
}

class DriverConnectionPadding3 extends DriverConnectionHotFields
{
    protected long p16, p17, p18, p19, p20, p21, p22;
}

/**
 * State maintained for active sessionIds within a channel for receiver processing
 */
public class DriverConnection extends DriverConnectionPadding3 implements AutoCloseable
{
    public enum Status
    {
        INIT, ACTIVE, INACTIVE, LINGER
    }

    private final long correlationId;
    private final int sessionId;
    private final int streamId;
    private final int positionBitsToShift;
    private final int termLengthMask;
    private final int initialTermId;
    private final int currentWindowLength;
    private final int currentGain;

    private final RawLog rawLog;
    private final InetSocketAddress controlAddress;
    private final ReceiveChannelEndpoint channelEndpoint;
    private final EventLogger logger;
    private final SystemCounters systemCounters;
    private final NanoClock clock;
    private final UnsafeBuffer[] termBuffers;
    private final PositionReporter hwmPosition;
    private final InetSocketAddress sourceAddress;
    private final List<PositionIndicator> subscriberPositions;
    private final LossHandler lossHandler;

    private volatile long statusMessagePosition;
    private volatile Status status = Status.INIT;

    public DriverConnection(
        final long correlationId,
        final ReceiveChannelEndpoint channelEndpoint,
        final InetSocketAddress controlAddress,
        final int sessionId,
        final int streamId,
        final int initialTermId,
        final int activeTermId,
        final int initialTermOffset,
        final int initialWindowLength,
        final RawLog rawLog,
        final TimerWheel timerwheel,
        final FeedbackDelayGenerator lossFeedbackDelayGenerator,
        final List<PositionIndicator> subscriberPositions,
        final PositionReporter hwmPosition,
        final NanoClock clock,
        final SystemCounters systemCounters,
        final InetSocketAddress sourceAddress,
        final EventLogger logger)
    {
        this.correlationId = correlationId;
        this.channelEndpoint = channelEndpoint;
        this.controlAddress = controlAddress;
        this.sessionId = sessionId;
        this.streamId = streamId;
        this.rawLog = rawLog;
        this.subscriberPositions = subscriberPositions;
        this.hwmPosition = hwmPosition;
        this.systemCounters = systemCounters;
        this.sourceAddress = sourceAddress;
        this.logger = logger;

        this.clock = clock;
        final long time = clock.time();
        this.timeOfLastStatusChange = time;
        this.lastPacketTimestamp = time;

        termBuffers = rawLog.stream().map(RawLogPartition::termBuffer).toArray(UnsafeBuffer[]::new);
        this.lossHandler = new LossHandler(timerwheel, lossFeedbackDelayGenerator, this::onLossDetected);

        final int termCapacity = termBuffers[0].capacity();

        this.currentWindowLength = Math.min(termCapacity, initialWindowLength);
        this.currentGain = Math.min(currentWindowLength / 4, termCapacity / 4);

        this.termLengthMask = termCapacity - 1;
        this.positionBitsToShift = Integer.numberOfTrailingZeros(termCapacity);
        this.initialTermId = initialTermId;

        final long initialPosition = computePosition(activeTermId, initialTermOffset, positionBitsToShift, initialTermId);
        this.lastStatusMessagePosition = initialPosition - (currentGain + 1);
        this.statusMessagePosition = this.lastStatusMessagePosition;
        this.completedPosition = initialPosition;
        this.hwmPosition.position(initialPosition);
    }

    /**
     * {@inheritDoc}
     */
    public void close()
    {
        hwmPosition.close();
        rawLog.close();
        subscriberPositions.forEach(PositionIndicator::close);
    }

    public long correlationId()
    {
        return correlationId;
    }

    /**
     * The session id of the channel from a publisher.
     *
     * @return session id of the channel from a publisher.
     */
    public int sessionId()
    {
        return sessionId;
    }

    /**
     * The stream id of this connection within a channel.
     *
     * @return stream id of this connection within a channel.
     */
    public int streamId()
    {
        return streamId;
    }

    /**
     * Get the string representation of the channel URI.
     *
     * @return the string representation of the channel URI.
     */
    public String channelUriString()
    {
        return channelEndpoint.udpChannel().originalUriString();
    }

    /**
     * The address of the source associated with the connection.
     *
     * @return source address
     */
    public InetSocketAddress sourceAddress()
    {
        return sourceAddress;
    }

    /**
     * Remove this connection from the {@link DataPacketDispatcher} so it will process no further packets from the network.
     */
    public void removeFromDispatcher()
    {
        channelEndpoint.dispatcher().removeConnection(this);
    }

    /**
     * Does this connection match a given {@link ReceiveChannelEndpoint} and stream id?
     *
     * @param channelEndpoint to match by identity.
     * @param streamId        to match on value.
     * @return true on a match otherwise false.
     */
    public boolean matches(final ReceiveChannelEndpoint channelEndpoint, final int streamId)
    {
        return this.streamId == streamId && this.channelEndpoint == channelEndpoint;
    }

    /**
     * Get the {@link uk.co.real_logic.aeron.driver.buffer.RawLog} the back this connection.
     *
     * @return the {@link uk.co.real_logic.aeron.driver.buffer.RawLog} the back this connection.
     */
    public RawLog rawLogBuffers()
    {
        return rawLog;
    }

    /**
     * Return status of the connection. Retrieved by {@link DriverConductor}.
     *
     * @return status of the connection
     */
    public Status status()
    {
        return status;
    }

    /**
     * Set status of the connection. Set by {@link DriverConductor}.
     *
     * @param status of the connection
     */
    public void status(final Status status)
    {
        timeOfLastStatusChange = clock.time();
        this.status = status;
    }

    /**
     * Return time of last status change. Retrieved by {@link DriverConductor}.
     *
     * @return time of last status change
     */
    public long timeOfLastStatusChange()
    {
        return timeOfLastStatusChange;
    }

    /**
     * Called from the {@link DriverConductor}.
     *
     * @return if work has been done or not
     */
    public int trackCompletion()
    {
        long minSubscriberPosition = Long.MAX_VALUE;
        long maxSubscriberPosition = Long.MIN_VALUE;

        final List<PositionIndicator> subscriberPositions = this.subscriberPositions;
        for (int i = 0, size = subscriberPositions.size(); i < size; i++)
        {
            final long position = subscriberPositions.get(i).position();
            minSubscriberPosition = Math.min(minSubscriberPosition, position);
            maxSubscriberPosition = Math.max(maxSubscriberPosition, position);
        }

        subscribersPosition = minSubscriberPosition;

        final long oldCompletedPosition = this.completedPosition;
        final long completedPosition = Math.max(oldCompletedPosition, maxSubscriberPosition);

        final int positionBitsToShift = this.positionBitsToShift;
        final int index = indexByPosition(completedPosition, positionBitsToShift);

        int workCount = lossHandler.scan(
            termBuffers[index], completedPosition, hwmPosition.position(), termLengthMask, positionBitsToShift, initialTermId);

        final int completedTermOffset = (int)completedPosition & termLengthMask;
        final int completedOffset = lossHandler.completedOffset();
        final long newCompletedPosition = (completedPosition - completedTermOffset) + completedOffset;
        this.completedPosition = newCompletedPosition;

        if ((newCompletedPosition >>> positionBitsToShift) > (oldCompletedPosition >>> positionBitsToShift))
        {
            final UnsafeBuffer termBuffer = termBuffers[previousPartitionIndex(index)];
            termBuffer.setMemory(0, termBuffer.capacity(), (byte)0);
        }

        if ((minSubscriberPosition - statusMessagePosition) > currentGain)
        {
            this.statusMessagePosition = minSubscriberPosition;
        }

        return workCount;
    }

    /**
     * Called from the {@link DriverConductor} to determine if the subscribers have drained the connection yet.
     *
     * @return true if the subscribers have drained the connection stream.
     */
    public boolean isDrained()
    {
        return subscribersPosition >= completedPosition;
    }

    /**
     * Insert frame into term buffer.
     *
     * @param buffer for the data packet to insert into the appropriate term.
     * @param length of the data packet
     * @return number of bytes applied as a result of this insertion.
     */
    public int insertPacket(final int termId, final int termOffset, final UnsafeBuffer buffer, final int length)
    {
        int bytesReceived = length;
        final int positionBitsToShift = this.positionBitsToShift;
        final long packetBeginPosition = computePosition(termId, termOffset, positionBitsToShift, initialTermId);
        final long proposedPosition = packetBeginPosition + length;
        final long windowBeginPosition = lastStatusMessagePosition;

        if (isHeartbeat(buffer, length))
        {
            hwmCandidate(packetBeginPosition);
            systemCounters.heartbeatsReceived().orderedIncrement();
        }
        else if (isFlowControlUnderRun(windowBeginPosition, packetBeginPosition) ||
                 isFlowControlOverRun(windowBeginPosition, proposedPosition))
        {
            bytesReceived = 0;
        }
        else
        {
            final UnsafeBuffer termBuffer = termBuffers[indexByPosition(packetBeginPosition, positionBitsToShift)];
            LogRebuilder.insert(termBuffer, termOffset, buffer, 0, length);

            hwmCandidate(proposedPosition);
        }

        return bytesReceived;
    }

    /**
     * To be called from the {@link Receiver} to see if a connection should be garbage collected.
     *
     * @param now                       current time to check against.
     * @param connectionLivenessTimeout timeout for inactivity test.
     * @return true if still active otherwise false.
     */
    public boolean checkForActivity(final long now, final long connectionLivenessTimeout)
    {
        if (now > (lastPacketTimestamp + connectionLivenessTimeout))
        {
            status(Status.INACTIVE);

            return false;
        }

        return true;
    }

    /**
     * Called from the {@link Receiver} to send any pending Status Messages.
     *
     * @param now time in nanoseconds.
     * @param statusMessageTimeout for sending of Status Messages.
     * @return number of work items processed.
     */
    public int sendPendingStatusMessage(final long now, final long statusMessageTimeout)
    {
        if (ACTIVE == status)
        {
            final long statusMessagePosition = this.statusMessagePosition;
            if ((statusMessagePosition != lastStatusMessagePosition) || now > (lastStatusMessageTimestamp + statusMessageTimeout))
            {
                final int activeTermId = computeTermIdFromPosition(statusMessagePosition, positionBitsToShift, initialTermId);
                final int termOffset = (int)statusMessagePosition & termLengthMask;

                channelEndpoint.sendStatusMessage(
                    controlAddress, sessionId, streamId, activeTermId, termOffset, currentWindowLength, (byte)0);

                lastStatusMessageTimestamp = now;
                lastStatusMessagePosition = statusMessagePosition;
                systemCounters.statusMessagesSent().orderedIncrement();
            }
        }

        return 1;
    }

    /**
     * Remove a {@link PositionIndicator} for a subscriber that has been removed so it is not tracked for flow control.
     *
     * @param subscriberPosition for the subscriber that has been removed.
     */
    public void removeSubscription(final PositionIndicator subscriberPosition)
    {
        subscriberPositions.remove(subscriberPosition);
        subscriberPosition.close();
    }

    /**
     * Add a new subscriber to this connection so their position can be tracked for flow control.
     *
     * @param subscriberPosition for the subscriber to be added.
     */
    public void addSubscription(final PositionIndicator subscriberPosition)
    {
        subscriberPositions.add(subscriberPosition);
    }

    /**
     * The position up to which the current stream rebuild is complete for reception.
     *
     * @return the position up to which the current stream rebuild is complete for reception.
     */
    public long completedPosition()
    {
        return completedPosition;
    }

    /**
     * The initial term id this connection started at.
     *
     * @return the initial term id this connection started at.
     */
    public int initialTermId()
    {
        return initialTermId;
    }

    private boolean isHeartbeat(final UnsafeBuffer buffer, final int length)
    {
        return length == DataHeaderFlyweight.HEADER_LENGTH && buffer.getInt(lengthOffset(0)) == 0;
    }

    private void hwmCandidate(final long proposedPosition)
    {
        lastPacketTimestamp = clock.time();

        if (proposedPosition > hwmPosition.position())
        {
            hwmPosition.position(proposedPosition);
        }
    }

    private boolean isFlowControlUnderRun(final long windowBeginPosition, final long packetPosition)
    {
        final boolean isFlowControlUnderRun = packetPosition < windowBeginPosition;

        if (isFlowControlUnderRun)
        {
            systemCounters.flowControlUnderRuns().orderedIncrement();
        }

        return isFlowControlUnderRun;
    }

    private boolean isFlowControlOverRun(final long windowBeginPosition, final long proposedPosition)
    {
        boolean isFlowControlOverRun = proposedPosition > (windowBeginPosition + currentWindowLength);

        if (isFlowControlOverRun)
        {
            logger.logOverRun(proposedPosition, windowBeginPosition, currentWindowLength);
            systemCounters.flowControlOverRuns().orderedIncrement();
        }

        return isFlowControlOverRun;
    }

    private void onLossDetected(int termId, int termOffset, int length)
    {
        this.systemCounters.nakMessagesSent().orderedIncrement();  // TODO: move
        channelEndpoint.sendNak(controlAddress, sessionId, streamId, termId, termOffset, length);
    }
}