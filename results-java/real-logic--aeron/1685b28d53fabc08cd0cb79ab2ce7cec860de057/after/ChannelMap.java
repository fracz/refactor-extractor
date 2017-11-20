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
package uk.co.real_logic.aeron.mediadriver;

import uk.co.real_logic.aeron.util.collections.Long2ObjectHashMap;

import java.util.HashMap;
import java.util.Map;

import static uk.co.real_logic.aeron.util.collections.CollectionUtil.getOrDefault;

/**
 * Map for storing information about channels. These are keyed
 * by a triple of destination/session/channel.
 */
public class ChannelMap<T>
{
    private final Map<UdpDestination, Long2ObjectHashMap<Long2ObjectHashMap<T>>> map;

    public ChannelMap()
    {
        map = new HashMap<>();
    }

    public T get(final UdpDestination destination, final long sessionId, final long channelId)
    {
        final Long2ObjectHashMap<Long2ObjectHashMap<T>> sessionMap = map.get(destination);
        if (sessionMap == null)
        {
            return null;
        }

        final Long2ObjectHashMap<T> channelMap = sessionMap.get(sessionId);
        if (channelMap == null)
        {
            return null;
        }

        return channelMap.get(channelId);
    }

    public T put(final UdpDestination destination, final long sessionId, final long channelId, final T value)
    {
        final Long2ObjectHashMap<Long2ObjectHashMap<T>> sessionMap
            = getOrDefault(map, destination, ignore -> new Long2ObjectHashMap<>());
        final Long2ObjectHashMap<T> channelMap = sessionMap.getOrDefault(sessionId, Long2ObjectHashMap::new);
        return channelMap.put(channelId, value);
    }

    public T remove(final UdpDestination destination, final long sessionId, final long channelId)
    {
        final Long2ObjectHashMap<Long2ObjectHashMap<T>> sessionMap = map.get(destination);
        if (sessionMap == null)
        {
            return null;
        }

        final Long2ObjectHashMap<T> channelMap = sessionMap.get(sessionId);
        if (channelMap == null)
        {
            return null;
        }

        // TODO: consider remove empty parent lookup structures to avoid memory leaks

        return channelMap.remove(channelId);
    }

}