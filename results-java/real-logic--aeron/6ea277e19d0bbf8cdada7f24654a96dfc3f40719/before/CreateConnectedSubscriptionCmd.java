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
package uk.co.real_logic.aeron.driver.cmd;

import uk.co.real_logic.aeron.driver.*;

public class CreateConnectedSubscriptionCmd
{
    private final UdpDestination udpDestination;
    private final long sessionId;
    private final long channelId;
    private final long termId;
    private final StatusMessageSender statusMessageSender;
    private final NakMessageSender nakMessageSender;

    public CreateConnectedSubscriptionCmd(final UdpDestination udpDestination,
                                          final long sessionId,
                                          final long channelId,
                                          final long termId,
                                          final StatusMessageSender statusMessageSender,
                                          final NakMessageSender nakMessageSender)
    {
        this.udpDestination = udpDestination;
        this.sessionId = sessionId;
        this.channelId = channelId;
        this.termId = termId;
        this.statusMessageSender = statusMessageSender;
        this.nakMessageSender = nakMessageSender;
    }

    public UdpDestination udpDestination()
    {
        return udpDestination;
    }

    public long sessionId()
    {
        return sessionId;
    }

    public long channelId()
    {
        return channelId;
    }

    public long termId()
    {
        return termId;
    }

    public StatusMessageSender sendSmHandler()
    {
        return statusMessageSender;
    }

    public NakMessageSender sendNakHandler()
    {
        return nakMessageSender;
    }
}