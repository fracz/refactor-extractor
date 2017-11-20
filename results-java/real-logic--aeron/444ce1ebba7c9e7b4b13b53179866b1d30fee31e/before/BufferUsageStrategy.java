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
package uk.co.real_logic.aeron.conductor;

import uk.co.real_logic.aeron.util.command.NewBufferMessageFlyweight;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;

import java.io.IOException;

/**
 * Interface for encapsulating the strategy of mapping ByteBuffers for Session, Channel, and Term.
 *
 * This corresponds to BufferManagementStrategy, but doesn't deal with creating the files
 * which need to be mapped.
 */
public interface BufferUsageStrategy
{

    default AtomicBuffer newBuffer(final NewBufferMessageFlyweight newBufferMessage, final int index)
            throws IOException
    {
        final String location = newBufferMessage.location(index);
        final int offset = newBufferMessage.bufferOffset(index);
        final int length = newBufferMessage.bufferLength(index);
        return newBuffer(location, offset, length);
    }

    AtomicBuffer newBuffer(final String location,
                           final int offset,
                           final int length) throws IOException;

    int releaseBuffers(final String location,
                       final int offset,
                       final int length);

    void close();
}