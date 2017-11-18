/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package org.vertx.java.core.datagram.impl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.DatagramSocketOptions;
import org.vertx.java.core.impl.FutureResultImpl;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.net.SocketAddress;
import org.vertx.java.core.net.impl.ConnectionBase;

import java.net.*;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public class DatagramSocketImpl extends ConnectionBase
        implements DatagramSocket {

  private Handler<Void> drainHandler;
  private Handler<org.vertx.java.core.datagram.DatagramPacket> dataHandler;

  public DatagramSocketImpl(VertxInternal vertx, org.vertx.java.core.datagram.InternetProtocolFamily family,
                            DatagramSocketOptions options) {
    super(vertx, createChannel(family, new DatagramSocketOptions(options)), vertx.getOrCreateContext());
    channel().config().setOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, true);
    context.getEventLoop().register(channel);
    channel.pipeline().addLast("handler", new DatagramServerHandler(this.vertx, this));
    channel().config().setMaxMessagesPerRead(1);
  }

  @Override
  public DatagramSocket listenMulticastGroup(String multicastAddress, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      addListener(channel().joinGroup(InetAddress.getByName(multicastAddress)), handler);
    } catch (UnknownHostException e) {
      notifyException(handler, e);
    }
    return this;
  }

  @Override
  public DatagramSocket listenMulticastGroup(String multicastAddress, String networkInterface, String source, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      InetAddress sourceAddress;
      if (source == null) {
        sourceAddress = null;
      } else {
        sourceAddress = InetAddress.getByName(source);
      }
      addListener(channel().joinGroup(InetAddress.getByName(multicastAddress),
              NetworkInterface.getByName(networkInterface), sourceAddress), handler);
    } catch (Exception e) {
      notifyException(handler, e);
    }
    return this;
  }

  @Override
  public DatagramSocket unlistenMulticastGroup(String multicastAddress, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      addListener(channel().leaveGroup(InetAddress.getByName(multicastAddress)), handler);
    } catch (UnknownHostException e) {
      notifyException(handler, e);
    }
    return this;
  }

  @Override
  public DatagramSocket unlistenMulticastGroup(String multicastAddress, String networkInterface, String source, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      InetAddress sourceAddress;
      if (source == null) {
        sourceAddress = null;
      } else {
        sourceAddress = InetAddress.getByName(source);
      }
      addListener(channel().leaveGroup(InetAddress.getByName(multicastAddress),
              NetworkInterface.getByName(networkInterface), sourceAddress), handler);
    } catch (Exception e) {
      notifyException(handler, e);
    }
    return this;
  }

  @Override
  public DatagramSocket blockMulticastGroup(String multicastAddress, String networkInterface, String sourceToBlock, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      InetAddress sourceAddress;
      if (sourceToBlock == null) {
        sourceAddress = null;
      } else {
        sourceAddress = InetAddress.getByName(sourceToBlock);
      }
      addListener(channel().block(InetAddress.getByName(multicastAddress),
              NetworkInterface.getByName(networkInterface), sourceAddress), handler);
    } catch (Exception e) {
      notifyException(handler, e);
    }
    return  this;
  }

  @Override
  public DatagramSocket blockMulticastGroup(String multicastAddress, String sourceToBlock, Handler<AsyncResult<DatagramSocket>> handler) {
    try {
      addListener(channel().block(InetAddress.getByName(multicastAddress), InetAddress.getByName(sourceToBlock)), handler);
    } catch (UnknownHostException e) {
      notifyException(handler, e);
    }
    return this;
  }

  @Override
  public DatagramSocket listen(int port, String address, Handler<AsyncResult<DatagramSocket>> handler) {
    return listen(new SocketAddress(port, address), handler);
  }

  @Override
  public DatagramSocket listen(int port, Handler<AsyncResult<DatagramSocket>> handler) {
    return listen(new SocketAddress(port, "0.0.0.0"), handler);
  }

  private DatagramSocket listen(SocketAddress local, Handler<AsyncResult<DatagramSocket>> handler) {
    InetSocketAddress is = new InetSocketAddress(local.getHostAddress(), local.getPort());
    ChannelFuture future = channel().bind(is);
    addListener(future, handler);
    return this;
  }

  @Override
  public DatagramSocket dataHandler(Handler<org.vertx.java.core.datagram.DatagramPacket> handler) {
    dataHandler = handler;
    return this;
  }

  final void handleMessage(org.vertx.java.core.datagram.DatagramPacket message) {
    if (dataHandler != null) {
      dataHandler.handle(message);
    }
  }

  @Override
  public DatagramSocket exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @SuppressWarnings("unchecked")
  final void addListener(ChannelFuture future, Handler<AsyncResult<DatagramSocket>> handler) {
    if (handler != null) {
      future.addListener(new DatagramChannelFutureListener<>(this, handler, vertx, context));
    }
  }

  @SuppressWarnings("unchecked")
  public DatagramSocket pause() {
    doPause();
    return this;
  }

  @SuppressWarnings("unchecked")
  public DatagramSocket resume() {
    doResume();
    return this;
  }

  @Override
  protected void handleInterestedOpsChanged() {
    if (drainHandler != null) {
      drainHandler.handle(null);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public DatagramSocket setWriteQueueMaxSize(int maxSize) {
    doSetWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return doWriteQueueFull();
  }

  @Override
  @SuppressWarnings("unchecked")
  public DatagramSocket drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public DatagramSocket send(Buffer packet, int port, String host, Handler<AsyncResult<DatagramSocket>> handler) {
    ChannelFuture future = channel().writeAndFlush(new DatagramPacket(packet.getByteBuf(), new InetSocketAddress(host, port)));
    addListener(future, handler);
    return this;
  }

  @Override
  public DatagramSocket send(String str, int port, String host, Handler<AsyncResult<DatagramSocket>> handler) {
    return send(new Buffer(str), port, host, handler);
  }

  @Override
  public DatagramSocket send(String str, String enc, int port, String host, Handler<AsyncResult<DatagramSocket>> handler) {
    return send(new Buffer(str, enc), port, host, handler);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> handler) {
    // make sure everything is flushed out on close
    endReadAndFlush();
    ChannelFuture future = channel.close();
    if (handler != null) {
      future.addListener(new DatagramChannelFutureListener<>(null, handler, vertx, context));
    }
  }

  protected DatagramChannel channel() {
    return (DatagramChannel) channel;
  }

  private static NioDatagramChannel createChannel(org.vertx.java.core.datagram.InternetProtocolFamily family,
                                                  DatagramSocketOptions options) {
    NioDatagramChannel channel;
    if (family == null) {
      channel = new NioDatagramChannel();
    } else {
      switch (family) {
        case IPv4:
          channel = new NioDatagramChannel(InternetProtocolFamily.IPv4);
          break;
        case IPv6:
          channel = new NioDatagramChannel(InternetProtocolFamily.IPv6);
          break;
        default:
          channel = new NioDatagramChannel();
      }
    }
    if (options.getSendBufferSize() != -1) {
      channel.config().setSendBufferSize(options.getSendBufferSize());
    }
    if (options.getReceiveBufferSize() != -1) {
      channel.config().setReceiveBufferSize(options.getReceiveBufferSize());
    }
    channel.config().setReuseAddress(options.isReuseAddress());
    if (options.getTrafficClass() != -1) {
      channel.config().setTrafficClass(options.getTrafficClass());
    }
    channel.config().setBroadcast(options.isBroadcast());
    channel.config().setLoopbackModeDisabled(options.isLoopbackModeDisabled());
    if (options.getMulticastTimeToLive() != -1) {
      channel.config().setTimeToLive(options.getMulticastTimeToLive());
    }
    if (options.getMulticastNetworkInterface() != null) {
      try {
        channel.config().setNetworkInterface(NetworkInterface.getByName(options.getMulticastNetworkInterface()));
      } catch (SocketException e) {
        throw new IllegalArgumentException("Could not find network interface with name " + options.getMulticastNetworkInterface());
      }
    }
    return channel;
  }


  private void notifyException(final Handler<AsyncResult<DatagramSocket>> handler, final Throwable cause) {
    if (context.isOnCorrectWorker(channel().eventLoop())) {
      try {
        vertx.setContext(context);
        handler.handle(new FutureResultImpl<>(cause));
      } catch (Throwable t) {
        context.reportException(t);
      }
    } else {
      context.execute(() -> handler.handle(new FutureResultImpl<>(cause)));
    }
  }
}