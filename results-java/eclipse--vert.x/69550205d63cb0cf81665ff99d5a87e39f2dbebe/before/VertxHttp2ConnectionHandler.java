/*
 * Copyright (c) 2011-2013 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.core.http.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class VertxHttp2ConnectionHandler extends Http2ConnectionHandler {

  final Http2ConnectionBase connection;

  public VertxHttp2ConnectionHandler(
      Http2ConnectionDecoder decoder,
      Http2ConnectionEncoder encoder,
      Http2Settings initialSettings,
      Function<VertxHttp2ConnectionHandler, Http2ConnectionBase> a) {
    super(decoder, encoder, initialSettings);
    connection = a.apply(this);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    cause.printStackTrace();
    ctx.close();
  }


  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    connection.getContext().executeFromIO(connection::handleClosed);
  }

  @Override
  protected void onConnectionError(ChannelHandlerContext ctx, Throwable cause, Http2Exception http2Ex) {
    connection.getContext().executeFromIO(() -> {
      connection.handleConnectionError(cause);
    });
    // Default behavior send go away
    super.onConnectionError(ctx, cause, http2Ex);
  }

  @Override
  protected void onStreamError(ChannelHandlerContext ctx, Throwable cause, Http2Exception.StreamException http2Ex) {
    connection.getContext().executeFromIO(() -> {
      connection.handleStreamError(http2Ex.streamId(), http2Ex);
    });
    // Default behavior reset stream
    super.onStreamError(ctx, cause, http2Ex);
  }
}