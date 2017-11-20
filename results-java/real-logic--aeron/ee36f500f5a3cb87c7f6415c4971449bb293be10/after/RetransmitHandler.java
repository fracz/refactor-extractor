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

import uk.co.real_logic.aeron.common.FeedbackDelayGenerator;
import uk.co.real_logic.aeron.common.TermHelper;
import uk.co.real_logic.aeron.common.TimerWheel;
import uk.co.real_logic.aeron.common.collections.Long2ObjectHashMap;
import uk.co.real_logic.aeron.common.concurrent.OneToOneConcurrentArrayQueue;
import uk.co.real_logic.aeron.driver.cmd.RetransmitPublicationCmd;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * Tracking and handling of retransmit request, NAKs, for senders and receivers
 *
 * A max number of retransmits is permitted by {@link #MAX_RETRANSMITS}. Additional received NAKs will be
 * ignored if this maximum is reached. Each retransmit will have 1 timer.
 */
public class RetransmitHandler
{
    /** Maximum number of concurrent retransmits */
    public static final int MAX_RETRANSMITS = MediaDriver.MAX_RETRANSMITS_DEFAULT;

    private final TimerWheel timerWheel;
    private final Queue<RetransmitAction> retransmitActionPool = new OneToOneConcurrentArrayQueue<>(MAX_RETRANSMITS);
    private final Long2ObjectHashMap<RetransmitAction>  activeRetransmitByPositionMap = new Long2ObjectHashMap<>();
    private final FeedbackDelayGenerator delayGenerator;
    private final FeedbackDelayGenerator lingerTimeoutGenerator;
    private final AtomicLong lowestPosition = new AtomicLong();
    private final RetransmitSender retransmitSender;
    private final int initialTermId;
    private final int capacity;
    private final int positionBitsToShift;

    /**
     * Create a retransmit handler.
     *
     * @param timerWheel for timers
     * @param delayGenerator to use for delay determination
     * @param lingerTimeoutGenerator to use for linger timeout
     */
    public RetransmitHandler(final TimerWheel timerWheel,
                             final FeedbackDelayGenerator delayGenerator,
                             final FeedbackDelayGenerator lingerTimeoutGenerator,
                             final RetransmitSender retransmitSender,
                             final int initialTermId,
                             final int capacity)
    {
        this.timerWheel = timerWheel;
        this.delayGenerator = delayGenerator;
        this.lingerTimeoutGenerator = lingerTimeoutGenerator;
        this.retransmitSender = retransmitSender;
        this.initialTermId = initialTermId;
        this.capacity = capacity;
        this.positionBitsToShift = Integer.numberOfTrailingZeros(capacity);
        this.lowestPosition.lazySet(0);

        IntStream.range(0, MAX_RETRANSMITS).forEach((i) -> retransmitActionPool.offer(new RetransmitAction()));
    }

    public void close()
    {
        activeRetransmitByPositionMap.forEach(
            (position, retransmitAction) ->
            {
                retransmitAction.delayTimer.cancel();
                retransmitAction.lingerTimer.cancel();
            }
        );
    }

    /**
     * Called on reception of a NAK to start retransmits handling.
     *
     * @param termId from the NAK and the term id of the buffer to retransmit from
     * @param termOffset from the NAK and the offset of the data to retransmit
     * @param length of the missing data
     */
    public void onNak(final int termId, final int termOffset, final int length)
    {
        final long position = TermHelper.calculatePosition(termId, termOffset, positionBitsToShift, initialTermId);

        if (!retransmitActionPool.isEmpty() &&
            null == activeRetransmitByPositionMap.get(position) &&
            capacity > termOffset &&
            lowestPosition.get() <= termOffset)
        {
            final RetransmitAction retransmitAction = retransmitActionPool.poll();
            retransmitAction.termId = termId;
            retransmitAction.termOffset = termOffset;
            retransmitAction.length = Math.min(length, capacity - termOffset);
            retransmitAction.position = position;

            final long delay = determineRetransmitDelay();
            if (0 == delay)
            {
                perform(retransmitAction);
                retransmitAction.linger(determineLingerTimeout());
            }
            else
            {
                retransmitAction.delay(delay);
            }

            activeRetransmitByPositionMap.put(position, retransmitAction);
        }
    }

    /**
     * Called to indicate a retransmission is received that may obviate the need to send one ourselves.
     *
     * NOTE: Currently only called from unit tests. Would be used for retransmitting from receivers for NAK suppression
     *
     * @param termId of the data
     * @param termOffset of the data
     */
    public void onRetransmitReceived(final int termId, final int termOffset)
    {
        final long position = TermHelper.calculatePosition(termId, termOffset, positionBitsToShift, initialTermId);
        final RetransmitAction retransmitAction = activeRetransmitByPositionMap.get(position);

        if (null != retransmitAction && State.DELAYED == retransmitAction.state)
        {
            activeRetransmitByPositionMap.remove(position);
            retransmitAction.state = State.INACTIVE;
            retransmitActionPool.offer(retransmitAction);
            retransmitAction.delayTimer.cancel();
            // do not go into linger
        }
    }

    /**
     * Inform handler of term rotation by informing it of a new high term id value
     *
     * @param termId of the new high
     */
    public void highTermId(final int termId)
    {
        lowestPosition.lazySet(TermHelper.calculatePosition(termId - 1, 0, positionBitsToShift, initialTermId));
    }

    private long determineRetransmitDelay()
    {
        return delayGenerator.generateDelay();
    }

    private long determineLingerTimeout()
    {
        return lingerTimeoutGenerator.generateDelay();
    }

    private void perform(final RetransmitAction retransmitAction)
    {
        retransmitSender.send(retransmitAction.termId, retransmitAction.termOffset, retransmitAction.length);
    }

    private enum State
    {
        DELAYED,
        LINGERING,
        INACTIVE
    }

    class RetransmitAction
    {
        int termId;
        int termOffset;
        int length;
        long position;
        State state = State.INACTIVE;
        TimerWheel.Timer delayTimer = timerWheel.newBlankTimer();
        TimerWheel.Timer lingerTimer = timerWheel.newBlankTimer();

        public void delay(final long delay)
        {
            state = State.DELAYED;
            timerWheel.rescheduleTimeout(delay, TimeUnit.NANOSECONDS, delayTimer, this::onDelayTimeout);
        }

        public void linger(final long timeout)
        {
            state = State.LINGERING;
            timerWheel.rescheduleTimeout(timeout, TimeUnit.NANOSECONDS, lingerTimer, this::onLingerTimeout);
        }

        public void onDelayTimeout()
        {
            perform(this);
            linger(determineLingerTimeout());
        }

        public void onLingerTimeout()
        {
            state = State.INACTIVE;
            activeRetransmitByPositionMap.remove(position);
            retransmitActionPool.offer(this);
        }
    }
}