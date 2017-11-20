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

import uk.co.real_logic.aeron.driver.cmd.*;

import java.util.Queue;

/**
 * Proxy for writing into the Sender Thread's command buffer.
 */
public class SenderProxy
{
    private final Queue<? super Object> commandQueue;

    public SenderProxy(final Queue<? super Object> commandQueue)
    {
        this.commandQueue = commandQueue;
    }

    public boolean retransmit(final RetransmitPublicationCmd cmd)
    {
        return commandQueue.offer(cmd);
    }

    public boolean closePublication(final ClosePublicationCmd cmd)
    {
        return commandQueue.offer(cmd);
    }
}