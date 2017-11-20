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
package uk.co.real_logic.aeron.util.control;

import uk.co.real_logic.aeron.util.Flyweight;

import java.nio.ByteOrder;

import static uk.co.real_logic.aeron.util.BitUtil.SIZE_OF_INT;

/**
 * Control message flyweight for any message that just needs to
 * represent a Triple of Session ID/Channel Id/Term ID. These are:
 *
 * <ul>
 *     <li>Request Term</li>
 *     <li>New Receive Buffer Notification</li>
 *     <li>New Send Buffer Notification</li>
 * </ul>
 *
 * <p>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          Session ID                           |
 * +---------------------------------------------------------------+
 * |                          Channel ID                           |
 * +---------------------------------------------------------------+
 * |                           Term ID                             |
 * +---------------------------------------------------------------+
 */
public class TripleMessageFlyweight extends Flyweight
{
    private static final int SESSION_ID_OFFSET = 0;
    private static final int CHANNEL_ID_FIELD_OFFSET = 4;
    private static final int TERM_ID_FIELD_OFFSET = 8;

    /**
     * return session id field
     * @return session id field
     */
    public long sessionId()
    {
        return uint32Get(offset + SESSION_ID_OFFSET, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * set session id field
     * @param sessionId field value
     * @return flyweight
     */
    public TripleMessageFlyweight sessionId(final long sessionId)
    {
        uint32Put(offset + SESSION_ID_OFFSET, (int)sessionId, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    /**
     * return channel id field
     *
     * @return channel id field
     */
    public long channelId()
    {
        return uint32Get(offset + CHANNEL_ID_FIELD_OFFSET, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * set channel id field
     *
     * @param channelId field value
     * @return flyweight
     */
    public TripleMessageFlyweight channelId(final long channelId)
    {
        uint32Put(offset + CHANNEL_ID_FIELD_OFFSET, channelId, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    /**
     * return termId field
     *
     * @return termId field
     */
    public long termId()
    {
        return uint32Get(offset + TERM_ID_FIELD_OFFSET, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * set termId field
     *
     * @param termId field value
     * @return flyweight
     */
    public TripleMessageFlyweight termId(final long termId)
    {
        uint32Put(offset + TERM_ID_FIELD_OFFSET, termId, ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public static int length()
    {
        return TERM_ID_FIELD_OFFSET + SIZE_OF_INT;
    }

}