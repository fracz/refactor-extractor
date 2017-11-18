/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.websocketx;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.URI;

/**
 * Base class for web socket client handshake implementations
 */
public abstract class WebSocketClientHandshaker {

    private final URI uri;

    private final WebSocketVersion version;

    private boolean handshakeComplete;

    private final String expectedSubprotocol;

    private String actualSubprotocol;

    protected final HttpHeaders customHeaders;

    private final int maxFramePayloadLength;

    /**
     * Base constructor
     *
     * @param uri
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subprotocol
     *            Sub protocol request sent to the server.
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @param maxFramePayloadLength
     *            Maximum length of a frame's payload
     */
    protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol,
                                        HttpHeaders customHeaders, int maxFramePayloadLength) {
        this.uri = uri;
        this.version = version;
        expectedSubprotocol = subprotocol;
        this.customHeaders = customHeaders;
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    /**
     * Returns the URI to the web socket. e.g. "ws://myhost.com/path"
     */
    public URI uri() {
        return uri;
    }

    /**
     * Version of the web socket specification that is being used
     */
    public WebSocketVersion version() {
        return version;
    }

    /**
     * Returns the max length for any frame's payload
     */
    public int maxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /**
     * Flag to indicate if the opening handshake is complete
     */
    public boolean isHandshakeComplete() {
        return handshakeComplete;
    }

    protected void setHandshakeComplete() {
        handshakeComplete = true;
    }

    /**
     * Returns the CSV of requested subprotocol(s) sent to the server as specified in the constructor
     */
    public String expectedSubprotocol() {
        return expectedSubprotocol;
    }

    /**
     * Returns the subprotocol response sent by the server. Only available after end of handshake.
     * Null if no subprotocol was requested or confirmed by the server.
     */
    public String actualSubprotocol() {
        return actualSubprotocol;
    }

    protected void setActualSubprotocol(String actualSubprotocol) {
        this.actualSubprotocol = actualSubprotocol;
    }

    /**
     * Begins the opening handshake
     *
     * @param channel
     *            Channel
     */
    public ChannelFuture handshake(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        return handshake(channel, channel.newPromise());
    }

    /**
     * Begins the opening handshake
     *
     * @param channel
     *            Channel
     * @param promise
     *            the {@link ChannelPromise} to be notified when the opening handshake is sent
     */
    public abstract ChannelFuture handshake(Channel channel, ChannelPromise promise);

    /**
     * Validates and finishes the opening handshake initiated by {@link #handshake}}.
     *
     * @param channel
     *            Channel
     * @param response
     *            HTTP response containing the closing handshake details
     */
    public abstract void finishHandshake(Channel channel, FullHttpResponse response);
}