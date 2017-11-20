/*
 * Copyright 2014, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.net.stubby.transport;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.primitives.Bytes;
import com.google.net.stubby.GrpcFramingUtil;
import com.google.net.stubby.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Tests for {@link MessageFramer}
 */
@RunWith(JUnit4.class)
public class MessageFramerTest {

  public static final int TRANSPORT_FRAME_SIZE = 57;

  @Test
  public void testPayload() throws Exception {
    CapturingSink sink = new CapturingSink();
    MessageFramer framer = new MessageFramer(sink, TRANSPORT_FRAME_SIZE);
    byte[] payload = new byte[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    byte[] unframedStream =
        Bytes.concat(
            new byte[]{GrpcFramingUtil.PAYLOAD_FRAME},
            new byte[]{0, 0, 0, (byte) payload.length},
            payload);
    for (int i = 0; i < 1000; i++) {
      framer.writePayload(new ByteArrayInputStream(payload), payload.length);
      if ((i + 1) % 13 == 0) {
        // Test flushing periodically
        framer.flush();
      }
    }
    framer.flush();
    assertEquals(sink.deframedStream.length, unframedStream.length * 1000);
    for (int i = 0; i < 1000; i++) {
      assertArrayEquals(unframedStream,
          Arrays.copyOfRange(sink.deframedStream, i * unframedStream.length,
              (i + 1) * unframedStream.length));
    }
  }

  @Test
  public void testStatus() throws Exception {
    CapturingSink sink = new CapturingSink();
    MessageFramer framer = new MessageFramer(sink, TRANSPORT_FRAME_SIZE);
    byte[] unframedStream = Bytes.concat(
            new byte[]{GrpcFramingUtil.STATUS_FRAME},
            new byte[]{0, 0, 0, 2},  // Len is 2 bytes
            new byte[]{0, 13});  // Internal==13
    for (int i = 0; i < 1000; i++) {
      framer.writeStatus(Status.INTERNAL);
      if ((i + 1) % 13 == 0) {
        framer.flush();
      }
    }
    framer.flush();
    assertEquals(sink.deframedStream.length, unframedStream.length * 1000);
    for (int i = 0; i < 1000; i++) {
      assertArrayEquals(unframedStream,
          Arrays.copyOfRange(sink.deframedStream, i * unframedStream.length,
              (i + 1) * unframedStream.length));
    }
  }

  static class CapturingSink implements Framer.Sink<ByteBuffer> {

    byte[] deframedStream = new byte[0];

    @Override
    public void deliverFrame(ByteBuffer frame, boolean endOfMessage) {
      assertTrue(frame.remaining() <= TRANSPORT_FRAME_SIZE);
      // Frame must contain compression flag & 24 bit length
      int header = frame.getInt();
      byte flag = (byte) (header >>> 24);
      int length = header & 0xFFFFFF;
      assertTrue(TransportFrameUtil.isNotCompressed(flag));
      assertEquals(frame.remaining(), length);
      // Frame must exceed dictated transport frame size
      byte[] frameBytes = new byte[frame.remaining()];
      frame.get(frameBytes);
      deframedStream = Bytes.concat(deframedStream, frameBytes);
    }
  }
}