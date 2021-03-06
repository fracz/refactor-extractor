/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeEvent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler.PriorKnowledgeUpgradeEvent;
import io.netty.handler.codec.http2.Http2Stream.State;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link CleartextHttp2ServerUpgradeHandler}
 */
public class CleartextHttp2ServerUpgradeHandlerTest {
    private EmbeddedChannel channel;

    private Http2FrameListener frameListener;

    private Http2ConnectionHandler http2ConnectionHandler;

    private List<Object> userEvents;

    @Before
    public void setUp() {
        frameListener = mock(Http2FrameListener.class);

        http2ConnectionHandler = new Http2ConnectionHandlerBuilder().frameListener(frameListener).build();

        UpgradeCodecFactory upgradeCodecFactory = new UpgradeCodecFactory() {
            @Override
            public UpgradeCodec newUpgradeCodec(CharSequence protocol) {
                return new Http2ServerUpgradeCodec(http2ConnectionHandler);
            }
        };

        userEvents = new ArrayList<Object>();

        HttpServerCodec httpServerCodec = new HttpServerCodec();
        HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(httpServerCodec, upgradeCodecFactory);

        CleartextHttp2ServerUpgradeHandler handler = new CleartextHttp2ServerUpgradeHandler(
                httpServerCodec, upgradeHandler, http2ConnectionHandler);
        channel = new EmbeddedChannel(handler, new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                userEvents.add(evt);
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        channel.finishAndReleaseAll();
    }

    @Test
    public void priorKnowledge() throws Exception {
        channel.writeInbound(Http2CodecUtil.connectionPrefaceBuf());

        ByteBuf settingsFrame = settingsFrameBuf();

        assertFalse(channel.writeInbound(settingsFrame));

        assertEquals(1, userEvents.size());
        assertTrue(userEvents.get(0) instanceof PriorKnowledgeUpgradeEvent);

        assertEquals(100, http2ConnectionHandler.connection().local().maxActiveStreams());
        assertEquals(65535, http2ConnectionHandler.connection().local().flowController().initialWindowSize());

        verify(frameListener).onSettingsRead(
                any(ChannelHandlerContext.class), eq(expectedSettings()));
    }

    @Test
    public void upgrade() throws Exception {
        String upgradeString = "GET / HTTP/1.1\r\n" +
                               "Host: example.com\r\n" +
                               "Connection: Upgrade, HTTP2-Settings\r\n" +
                               "Upgrade: h2c\r\n" +
                               "HTTP2-Settings: AAMAAABkAAQAAP__\r\n\r\n";
        ByteBuf upgrade = Unpooled.buffer().writeBytes(upgradeString.getBytes(CharsetUtil.US_ASCII));

        assertFalse(channel.writeInbound(upgrade));

        assertEquals(1, userEvents.size());

        Object userEvent = userEvents.get(0);
        assertTrue(userEvent instanceof UpgradeEvent);
        assertEquals("h2c", ((UpgradeEvent) userEvent).protocol());
        ReferenceCountUtil.release(userEvent);

        assertEquals(100, http2ConnectionHandler.connection().local().maxActiveStreams());
        assertEquals(65535, http2ConnectionHandler.connection().local().flowController().initialWindowSize());

        assertEquals(1, http2ConnectionHandler.connection().numActiveStreams());
        assertNotNull(http2ConnectionHandler.connection().stream(1));

        Http2Stream stream = http2ConnectionHandler.connection().stream(1);
        assertEquals(State.HALF_CLOSED_REMOTE, stream.state());
        assertFalse(stream.isHeadersSent());
    }

    @Test
    public void priorKnowledgeInFragments() throws Exception {
        ByteBuf connectionPreface = Http2CodecUtil.connectionPrefaceBuf();
        assertFalse(channel.writeInbound(connectionPreface.readBytes(5), connectionPreface));

        ByteBuf settingsFrame = settingsFrameBuf();
        assertFalse(channel.writeInbound(settingsFrame));

        assertEquals(1, userEvents.size());
        assertTrue(userEvents.get(0) instanceof PriorKnowledgeUpgradeEvent);

        assertEquals(100, http2ConnectionHandler.connection().local().maxActiveStreams());
        assertEquals(65535, http2ConnectionHandler.connection().local().flowController().initialWindowSize());

        verify(frameListener).onSettingsRead(
                any(ChannelHandlerContext.class), eq(expectedSettings()));
    }

    @Test
    public void downgrade() throws Exception {
        String requestString = "GET / HTTP/1.1\r\n" +
                         "Host: example.com\r\n\r\n";
        ByteBuf inbound = Unpooled.buffer().writeBytes(requestString.getBytes(CharsetUtil.US_ASCII));

        assertTrue(channel.writeInbound(inbound));

        Object firstInbound = channel.readInbound();
        assertTrue(firstInbound instanceof HttpRequest);
        HttpRequest request = (HttpRequest) firstInbound;
        assertEquals(HttpMethod.GET, request.method());
        assertEquals("/", request.uri());
        assertEquals(HttpVersion.HTTP_1_1, request.protocolVersion());
        assertEquals(new DefaultHttpHeaders().add("Host", "example.com"), request.headers());

        ((LastHttpContent) channel.readInbound()).release();

        assertNull(channel.readInbound());
    }

    private static ByteBuf settingsFrameBuf() {
        ByteBuf settingsFrame = Unpooled.buffer();
        settingsFrame.writeMedium(12); // Payload length
        settingsFrame.writeByte(0x4); // Frame type
        settingsFrame.writeByte(0x0); // Flags
        settingsFrame.writeInt(0x0); // StreamId
        settingsFrame.writeShort(0x3);
        settingsFrame.writeInt(100);
        settingsFrame.writeShort(0x4);
        settingsFrame.writeInt(65535);

        return settingsFrame;
    }

    private static Http2Settings expectedSettings() {
        return new Http2Settings().maxConcurrentStreams(100).initialWindowSize(65535);
    }
}