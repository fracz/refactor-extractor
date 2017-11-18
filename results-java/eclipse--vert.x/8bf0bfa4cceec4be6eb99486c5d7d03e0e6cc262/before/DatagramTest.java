/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.tests.core;

import org.junit.After;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.DatagramSocketOptions;
import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.net.impl.SocketDefaults;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.vertx.java.tests.core.TestUtils.*;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class DatagramTest extends VertxTestBase {

  private volatile DatagramSocket peer1;
  private volatile DatagramSocket peer2;

  @After
  public void after() throws Exception {
    if (peer1 != null) {
      CountDownLatch latch = new CountDownLatch(2);
      peer1.close(ar -> {
        assertTrue(ar.succeeded());
        latch.countDown();
        if (peer2 != null) {
          peer2.close(ar2 -> {
            assertTrue(ar2.succeeded());
            latch.countDown();
          });
        } else {
          latch.countDown();
        }
      });
      latch.await(10L, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testSendReceive() {
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2.exceptionHandler(t -> fail(t.getMessage()));
    peer2.listen(1234, "127.0.0.1", ar -> {
      assertTrue(ar.succeeded());
      Buffer buffer = randomBuffer(128);
      peer2.dataHandler(packet -> {
        assertEquals(buffer, packet.data());
        testComplete();
      });
      peer1.send(buffer, 1234, "127.0.0.1", ar2 -> assertTrue(ar2.succeeded()));
    });
    await();
  }

  @Test
  public void testListenHostPort() {
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2.listen(1234, "127.0.0.1", ar -> {
      assertTrue(ar.succeeded());
      testComplete();
    });
    await();
  }

  @Test
  public void testListenPort() {
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2.listen(1234, ar -> {
      assertTrue(ar.succeeded());
      testComplete();
    });
    await();
  }

  @Test
  public void testListenInetSocketAddress() {
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2.listen(1234, "127.0.0.1", ar -> {
      assertTrue(ar.succeeded());
      testComplete();
    });
    await();
  }

  @Test
  public void testListenSamePortMultipleTimes() {
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2.listen(1234, "127.0.0.1", ar1 -> {
      assertTrue(ar1.succeeded());
      peer1.listen(1234, "127.0.0.1", ar2 -> {
        assertTrue(ar2.failed());
        testComplete();
      });
    });
    await();
  }

  @Test
  public void testEcho() {
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer1.exceptionHandler(t -> fail(t.getMessage()));
    peer2.exceptionHandler(t -> fail(t.getMessage()));
    peer2.listen(1234, "127.0.0.1", ar -> {
      assertTrue(ar.succeeded());
      Buffer buffer = randomBuffer(128);
      peer2.dataHandler(packet -> {
        assertEquals("127.0.0.1", packet.sender().getHostAddress());
        assertEquals(1235, packet.sender().getPort());
        assertEquals(buffer, packet.data());
        peer2.send(packet.data(), 1235, "127.0.0.1", ar2 -> assertTrue(ar2.succeeded()));
      });
      peer1.listen(1235, "127.0.0.1", ar2 -> {
        peer1.dataHandler(packet -> {
          assertEquals(buffer, packet.data());
          assertEquals("127.0.0.1", packet.sender().getHostAddress());
          assertEquals(1234, packet.sender().getPort());
          testComplete();
        });
        peer1.send(buffer, 1234, "127.0.0.1", ar3 -> assertTrue(ar3.succeeded()));
      });
    });
    await();
  }

  @Test
  public void testSendAfterCloseFails() {
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer1.close(ar -> {
      assertTrue(ar.succeeded());
      peer1.send("Test", 1234, "127.0.0.1", ar2 -> {
        assertTrue(ar2.failed());
        peer1 = null;
        peer2.close(ar3 -> {
          assertTrue(ar3.succeeded());
            peer2.send("Test", 1234, "127.0.0.1", ar4 -> {
              assertTrue(ar4.failed());
              peer2 = null;
              testComplete();
            });
          });
      });
    });
    await();
  }

  @Test
  public void testBroadcast() {
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions().setBroadcast(true));
    peer2 = vertx.createDatagramSocket(null, new DatagramSocketOptions().setBroadcast(true));
    peer2.exceptionHandler(t -> fail(t.getMessage()));
    peer2.listen(1234, "0.0.0.0", ar1 -> {
      assertTrue(ar1.succeeded());
      Buffer buffer = randomBuffer(128);
      peer2.dataHandler(packet -> {
        assertEquals(buffer, packet.data());
        testComplete();
      });
      peer1.send(buffer, 1234, "255.255.255.255", ar2 -> {
        assertTrue(ar2.succeeded());
      });
    });
    await();
  }

  @Test
  public void testBroadcastFailsIfNotConfigured() {
    peer1 = vertx.createDatagramSocket(null, new DatagramSocketOptions());
    peer1.send("test", 1234, "255.255.255.255", ar -> {
      assertTrue(ar.failed());
      testComplete();
    });
    await();
  }

  @Test
  public void testMulticastJoinLeave() throws Exception {
    Buffer buffer = randomBuffer(128);
    String groupAddress = "230.0.0.1";
    String iface = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1")).getName();
    AtomicBoolean received = new AtomicBoolean();
    peer1 = vertx.createDatagramSocket(InternetProtocolFamily.IPv4, new DatagramSocketOptions().setMulticastNetworkInterface(iface));
    peer2 = vertx.createDatagramSocket(InternetProtocolFamily.IPv4, new DatagramSocketOptions().setMulticastNetworkInterface(iface));

    peer1.dataHandler(packet -> {
      assertEquals(buffer, packet.data());
      received.set(true);
    });

    peer1.listen(1234, ar1 -> {
      assertTrue(ar1.succeeded());
      peer1.listenMulticastGroup(groupAddress, iface, null, ar2 -> {
        assertTrue(ar2.succeeded());
        peer2.send(buffer, 1234, groupAddress, ar3 -> {
          assertTrue(ar3.succeeded());
          // leave group in 1 second so give it enough time to really receive the packet first
          vertx.setTimer(1000, id -> {
            peer1.unlistenMulticastGroup(groupAddress, iface, null, ar4 -> {
              assertTrue(ar4.succeeded());
              AtomicBoolean receivedAfter = new AtomicBoolean();
              peer1.dataHandler(packet -> {
                // Should not receive any more event as it left the group
                receivedAfter.set(true);
              });
              peer2.send(buffer, 1234, groupAddress, ar5 -> {
                assertTrue(ar5.succeeded());
                // schedule a timer which will check in 1 second if we received a message after the group
                // was left before
                vertx.setTimer(1000, id2 -> {
                  assertFalse(receivedAfter.get());
                  assertTrue(received.get());
                  testComplete();
                });
              });
            });
          });
        });
      });
    });
    await();
  }

  @Test
  public void testOptions() {
    DatagramSocketOptions options = new DatagramSocketOptions();
    assertEquals(-1, options.getSendBufferSize());
    int rand = randomPositiveInt();
    assertEquals(options, options.setSendBufferSize(rand));
    assertEquals(rand, options.getSendBufferSize());
    try {
      options.setSendBufferSize(0);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }
    try {
      options.setSendBufferSize(-123);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }

    assertEquals(-1, options.getReceiveBufferSize());
    rand = randomPositiveInt();
    assertEquals(options, options.setReceiveBufferSize(rand));
    assertEquals(rand, options.getReceiveBufferSize());
    try {
      options.setReceiveBufferSize(0);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }
    try {
      options.setReceiveBufferSize(-123);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }

    assertFalse(options.isReuseAddress());
    assertEquals(options, options.setReuseAddress(true));
    assertTrue(options.isReuseAddress());

    assertEquals(-1, options.getTrafficClass());
    rand = 23;
    assertEquals(options, options.setTrafficClass(rand));
    assertEquals(rand, options.getTrafficClass());
    try {
      options.setTrafficClass(-1);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }
    try {
      options.setTrafficClass(256);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }

    assertFalse(options.isBroadcast());
    assertEquals(options, options.setBroadcast(true));
    assertTrue(options.isBroadcast());

    assertTrue(options.isLoopbackModeDisabled());
    assertEquals(options, options.setLoopbackModeDisabled(false));
    assertFalse(options.isLoopbackModeDisabled());

    assertEquals(-1, options.getMulticastTimeToLive());
    rand = randomPositiveInt();
    assertEquals(options, options.setMulticastTimeToLive(rand));
    assertEquals(rand, options.getMulticastTimeToLive());
    try {
      options.setMulticastTimeToLive(-1);
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      // OK
    }

    assertNull(options.getMulticastNetworkInterface());
    String randString = randomUnicodeString(100);
    assertEquals(options, options.setMulticastNetworkInterface(randString));
    assertEquals(randString, options.getMulticastNetworkInterface());

    testComplete();
  }

  @Test
  public void testOptionsCopied() {
    DatagramSocketOptions options = new DatagramSocketOptions();
    options.setReuseAddress(true);
    peer1 = vertx.createDatagramSocket(null, options);
    peer2 = vertx.createDatagramSocket(null, options);
    // Listening on same address:port so will only work if reuseAddress = true
    // Set to false, but because options are copied internally should still work
    options.setReuseAddress(false);
    peer1.listen(1234, "127.0.0.1", ar -> {
      assertTrue(ar.succeeded());
      peer2.listen(1234, "127.0.0.1", ar2 -> {
        assertTrue(ar2.succeeded());
        testComplete();
      });
    });
    await();
  }
}