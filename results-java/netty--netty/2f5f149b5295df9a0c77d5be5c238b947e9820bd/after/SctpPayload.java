/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.sctp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://github.com/jestan">Jestan Nirojan</a>
 */
public final class SctpPayload {
    private final int streamIdentifier;
    private final int protocolIdentifier;
    private final ChannelBuffer payloadBuffer;

    /**
     * Essential data that is being carried within SCTP Data Chunk
     * @param streamIdentifier that you want to send the payload
     * @param protocolIdentifier of payload
     * @param payloadBuffer channel buffer
     */
    public SctpPayload(int streamIdentifier, int protocolIdentifier, ChannelBuffer payloadBuffer) {
        this.streamIdentifier = streamIdentifier;
        this.protocolIdentifier = protocolIdentifier;
        this.payloadBuffer = payloadBuffer;
    }

    public int getstreamIdentifier() {
        return streamIdentifier;
    }

    public int getProtocolIdentifier() {
        return protocolIdentifier;
    }

    public ChannelBuffer getPayloadBuffer() {
        if (payloadBuffer.readable()) {
            return payloadBuffer.slice();
        } else {
            return ChannelBuffers.EMPTY_BUFFER;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("SctpPayload{").
                append("streamIdentifier=").
                append(streamIdentifier).
                append(", protocolIdentifier=").
                append(protocolIdentifier).
                append(", payloadBuffer=").
                append(ChannelBuffers.hexDump(getPayloadBuffer())).
                append('}').toString();
    }
}