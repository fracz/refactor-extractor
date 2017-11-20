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

import org.junit.Test;
import uk.co.real_logic.aeron.util.control.ChannelMessageFlyweight;
import uk.co.real_logic.aeron.util.control.RemoveReceiverMessageFlyweight;
import uk.co.real_logic.aeron.util.control.RequestTermFlyweight;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.aeron.util.protocol.HeaderFlyweight.*;

public class AdminThreadTest
{

    public static final String DESTINATION = "udp://localhost:40123@localhost:40124";
    private final ByteBuffer sendBuffer = ByteBuffer.allocateDirect(256);
    private final AdminThread thread = new AdminThread(null, null, sendBuffer);

    @Test
    public void threadSendsAddChannelMessage()
    {
        threadSendsChannelMessage(() -> thread.sendAddChannel(DESTINATION, 1, 2), HDR_TYPE_ADD_CHANNEL);
    }

    @Test
    public void threadSendsRemoveChannelMessage()
    {
        threadSendsChannelMessage(() -> thread.sendRemoveChannel(DESTINATION, 1, 2), HDR_TYPE_REMOVE_CHANNEL);
    }

    private void threadSendsChannelMessage(final Runnable sendMessage, short type)
    {
        ChannelMessageFlyweight channelMessage = new ChannelMessageFlyweight();
        channelMessage.reset(sendBuffer);

        sendMessage.run();

        assertThat(channelMessage.headerType(), is(type));
        assertThat(channelMessage.destination(), is(DESTINATION));
        assertThat(channelMessage.sessionId(), is(1L));
        assertThat(channelMessage.channelId(), is(2L));
    }

    @Test
    public void threadSendsRemoveReceiverMessage()
    {
        RemoveReceiverMessageFlyweight removeReceiverMessage = new RemoveReceiverMessageFlyweight();
        removeReceiverMessage.reset(sendBuffer);

        thread.sendRemoveReceiver(DESTINATION);

        assertThat(removeReceiverMessage.headerType(), is(HDR_TYPE_REMOVE_RECEIVER));
        assertThat(removeReceiverMessage.destination(), is(DESTINATION));
    }

    @Test
    public void threadSendsRequestTermBufferMessage()
    {
        RequestTermFlyweight requestTermBuffer = new RequestTermFlyweight();
        requestTermBuffer.reset(sendBuffer);

        thread.sendRequestTerm(1L, 2L, 3L);

        assertThat(requestTermBuffer.headerType(), is(HDR_TYPE_REQUEST_TERM));
        assertThat(requestTermBuffer.sessionId(), is(1L));
        assertThat(requestTermBuffer.channelId(), is(2L));
        assertThat(requestTermBuffer.termId(), is(3L));
    }

}