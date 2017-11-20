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

import uk.co.real_logic.aeron.common.concurrent.AtomicCounter;
import uk.co.real_logic.aeron.driver.cmd.*;

import java.util.Queue;

/**
 * Proxy for writing into the Receiver Thread's command buffer.
 */
// TODO: put all the command object creation inside
public class ReceiverProxy
{
    private final Queue<? super Object> commandQueue;
    private final AtomicCounter failCount;

    public ReceiverProxy(final Queue<? super Object> commandQueue, final AtomicCounter failCount)
    {
        this.commandQueue = commandQueue;
        this.failCount = failCount;
    }

    public void addSubscription(final ReceiveChannelEndpoint mediaEndpoint, final int streamId)
    {
        offerCommand(new AddSubscriptionCmd(mediaEndpoint, streamId));
    }

    public void removeSubscription(final ReceiveChannelEndpoint mediaEndpoint, final int streamId)
    {
        offerCommand(new RemoveSubscriptionCmd(mediaEndpoint, streamId));
    }

    public void newConnection(final NewConnectionCmd e)
    {
        offerCommand(e);
    }

    public void removeConnection(final DriverConnection connection)
    {
        offerCommand(new RemoveConnectionCmd(connection.receiveChannelEndpoint(), connection));
    }

    public void registerMediaEndpoint(final RegisterReceiveChannelEndpointCmd cmd)
    {
        offerCommand(cmd);
    }

    public void closeMediaEndpoint(final CloseReceiveChannelEndpointCmd cmd)
    {
        offerCommand(cmd);
    }

    public void removePendingSetup(final RemovePendingSetupCmd cmd)
    {
        offerCommand(cmd);
    }

    public void closeSubscription(final DriverSubscription subscription)
    {
        offerCommand(new CloseSubscriptionCmd(subscription));
    }

    private void offerCommand(Object cmd)
    {
        while (!commandQueue.offer(cmd))
        {
            failCount.orderedIncrement();
            Thread.yield();
        }
    }

}