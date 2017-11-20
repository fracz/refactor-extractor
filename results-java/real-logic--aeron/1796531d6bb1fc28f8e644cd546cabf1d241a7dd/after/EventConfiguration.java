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
package uk.co.real_logic.aeron.common.event;

import uk.co.real_logic.aeron.common.IoUtil;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.co.real_logic.aeron.common.event.EventCode.*;

/**
 * Common configuration elements between event loggers and event reader side
 */
public class EventConfiguration
{
    /**
     * Event Buffer location system property name
     */
    public static final String LOCATION_PROPERTY_NAME = "aeron.event.buffer.location";

    /**
     * Event Buffer size system property name
     */
    public static final String BUFFER_SIZE_PROPERTY_NAME = "aeron.event.buffer.size";

    /**
     * Event Buffer location deleted on exit system property name
     */
    public static final String DELETE_ON_EXIT_PROPERTY_NAME = "aeron.event.buffer.delete-on-exit";

    /**
     * Event tags system property name. This is either:
     * <ul>
     * <li>A comma separated list of EventCodes to enable</li>
     * <li>"all" which enables all the codes</li>
     * <li>"prod" which enables the codes specified by PRODUCTION_LOGGER_EVENT_CODES</li>
     * </ul>
     */
    public static final String ENABLED_LOGGER_EVENT_CODES_PROPERTY_NAME = "aeron.event.log";

    public static final Set<EventCode> PRODUCTION_LOGGER_EVENT_CODES =
        EnumSet.of(EXCEPTION,
                   ERROR_SENDING_HEARTBEAT_PACKET,
                   COULD_NOT_FIND_FRAME_HANDLER_FOR_NEW_CONNECTED_SUBSCRIPTION,
                   COULD_NOT_FIND_INTERFACE,
                   COULD_NOT_SEND_ENTIRE_RETRANSMIT,
                   MALFORMED_FRAME_LENGTH,
                   UNKNOWN_HEADER_TYPE,
                   ERROR_DELETING_FILE);

    public static final Set<EventCode> ADMIN_ONLY_EVENT_CODES =
        EnumSet.of(EXCEPTION,
            ERROR_SENDING_HEARTBEAT_PACKET,
            COULD_NOT_FIND_FRAME_HANDLER_FOR_NEW_CONNECTED_SUBSCRIPTION,
            COULD_NOT_FIND_INTERFACE,
            COULD_NOT_SEND_ENTIRE_RETRANSMIT,
            MALFORMED_FRAME_LENGTH,
            UNKNOWN_HEADER_TYPE,
            CMD_IN_ADD_PUBLICATION,
            CMD_IN_ADD_SUBSCRIPTION,
            CMD_IN_KEEPALIVE_CLIENT,
            CMD_IN_REMOVE_PUBLICATION,
            CMD_IN_REMOVE_SUBSCRIPTION,
            REMOVE_CONNECTION_CLEANUP,
            REMOVE_PUBLICATION_CLEANUP,
            REMOVE_SUBSCRIPTION_CLEANUP,
            CMD_OUT_NEW_PUBLICATION_BUFFER_NOTIFICATION,
            CMD_OUT_NEW_SUBSCRIPTION_BUFFER_NOTIFICATION,
            CMD_OUT_ON_INACTIVE_CONNECTION,
            CMD_OUT_ON_OPERATION_SUCCESS,
            ERROR_DELETING_FILE);

    public static final Set<EventCode> ALL_LOGGER_EVENT_CODES = EnumSet.allOf(EventCode.class);

    /**
     * Event Buffer default location
     */
    public static final String LOCATION_DEFAULT = IoUtil.tmpDirName() + "aeron" + File.separator + "event-buffer";

    /**
     * Event Buffer default size (in bytes)
     */
    public static final long BUFFER_SIZE_DEFAULT = 65536;

    /**
     * Maximum length of an event in bytes
     */
    public final static int MAX_EVENT_LENGTH = 2048;

    private static Pattern COMMA = Pattern.compile(",");

    public static long getEnabledEventCodes()
    {
        return makeTagBitSet(getEnabledEventCodes(System.getProperty(ENABLED_LOGGER_EVENT_CODES_PROPERTY_NAME)));
    }

    static long makeTagBitSet(final Set<EventCode> eventCodes)
    {
        return eventCodes.stream()
                         .mapToLong(EventCode::tagBit)
                         .reduce(0L, (acc, x) -> acc | x);
    }

    /**
     * Get the {@link Set} of {@link EventCode}s that are enabled for the logger.
     *
     * @param enabledLoggerEventCodes that can be "all", "prod" or a comma separated list.
     * @return the {@link Set} of {@link EventCode}s that are enabled for the logger.
     */
    static Set<EventCode> getEnabledEventCodes(final String enabledLoggerEventCodes)
    {
        if (enabledLoggerEventCodes == null)
        {
            return PRODUCTION_LOGGER_EVENT_CODES;
        }

        switch (enabledLoggerEventCodes)
        {
            case "all":
                return ALL_LOGGER_EVENT_CODES;

            case "prod":
                return PRODUCTION_LOGGER_EVENT_CODES;

            case "admin":
                return ADMIN_ONLY_EVENT_CODES;

            default:
                return COMMA.splitAsStream(enabledLoggerEventCodes)
                            .map(EventCode::valueOf)
                            .collect(Collectors.toSet());
        }
    }

}