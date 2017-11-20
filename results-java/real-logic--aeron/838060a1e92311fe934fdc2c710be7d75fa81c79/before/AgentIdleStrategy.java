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
package uk.co.real_logic.aeron.util;

import java.util.concurrent.locks.LockSupport;

/**
 * Idling strategy for Agent spin loop
 *
 * Spin for maxSpins
 * {@link Thread#yield()} for maxYields
 * {@link LockSupport#parkNanos(long)} on an exponential backoff to maxParkPeriodNs
 */
public class AgentIdleStrategy
{
    public enum State
    {
        NOT_IDLE, SPINNING, YIELDING, PARKING
    }

    private final long maxSpins;
    private final long maxYields;
    private final long minParkPeriodNs;
    private final long maxParkPeriodNs;

    private State state;

    private long spins;
    private long yields;
    private long parkPeriodNs;

    /**
     * Create a set of state tracking idle behavior
     *
     * @param maxSpins to perform before moving to {@link Thread#yield()}
     * @param maxYields to perform before moving to {@link LockSupport#parkNanos(long)}
     * @param minParkPeriodNs to use when initiating parking
     * @param maxParkPeriodNs to use when parking
     */
    public AgentIdleStrategy(final long maxSpins,
                             final long maxYields,
                             final long minParkPeriodNs,
                             final long maxParkPeriodNs)
    {
        this.maxSpins = maxSpins;
        this.maxYields = maxYields;
        this.minParkPeriodNs = minParkPeriodNs;
        this.maxParkPeriodNs = maxParkPeriodNs;

        this.spins = 0;
        this.yields = 0;
        this.state = State.NOT_IDLE;
    }

    /**
     * Perform current idle strategy (or not) depending on whether work has been done or not
     *
     * @param workCount performed in last duty cycle.
     */
    public void idle(final int workCount)
    {
        if (workCount > 0)
        {
            spins = 0;
            yields = 0;
            state = State.NOT_IDLE;
            return;
        }

        switch (state)
        {
            case NOT_IDLE:
                state = State.SPINNING;
                spins++;
                break;

            case SPINNING:
                if (++spins > maxSpins)
                {
                    state = State.YIELDING;
                    yields = 0;
                }
                break;

            case YIELDING:
                if (++yields > maxYields)
                {
                    state = State.PARKING;
                    parkPeriodNs = minParkPeriodNs;
                }
                else
                {
                    Thread.yield();
                }
                break;

            case PARKING:
                LockSupport.parkNanos(parkPeriodNs);
                parkPeriodNs = Math.min(parkPeriodNs << 1, maxParkPeriodNs);
                break;
        }
    }
}