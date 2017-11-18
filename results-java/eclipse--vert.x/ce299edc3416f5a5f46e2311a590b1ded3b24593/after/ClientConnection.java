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

package org.vertx.java.core.http.impl;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.ReferenceCountUtil;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketConnectOptions;
import org.vertx.java.core.http.WebSocketVersion;
import org.vertx.java.core.http.impl.ws.WebSocketFrameInternal;
import org.vertx.java.core.impl.DefaultContext;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.net.impl.ConnectionBase;
import org.vertx.java.core.net.impl.DefaultNetSocket;
import org.vertx.java.core.net.impl.VertxNetHandler;

import java.net.URI;
import java.util.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class ClientConnection extends ConnectionBase {
  private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);

  final DefaultHttpClient client;
  final String hostHeader;
  private final boolean ssl;
  private final String host;
  private final int port;
  private final ConnectionLifeCycleListener listener;
  private WebSocketClientHandshaker handshaker;
  private volatile DefaultHttpClientRequest currentRequest;
  // Requests can be pipelined so we need a queue to keep track of requests
  private final Queue<DefaultHttpClientRequest> requests = new ArrayDeque<>();
  private volatile DefaultHttpClientResponse currentResponse;
  private volatile DefaultHttpClientRequest requestForResponse;

  private DefaultWebSocket ws;

  ClientConnection(VertxInternal vertx, DefaultHttpClient client, Channel channel, boolean ssl, String host,
                   int port, DefaultContext context, ConnectionLifeCycleListener listener) {
    super(vertx, channel, context);
    this.client = client;
    this.ssl = ssl;
    this.host = host;
    this.port = port;
    if ((port == 80 && !ssl) || (port == 443 && ssl)) {
      this.hostHeader = host;
    } else {
      this.hostHeader = host + ':' + port;
    }
    this.listener = listener;
  }

  void toWebSocket(WebSocketConnectOptions options,
                   int maxWebSocketFrameSize,
                   final Handler<WebSocket> wsConnect) {
    if (ws != null) {
      throw new IllegalStateException("Already websocket");
    }

    try {
      URI wsuri = new URI(options.getRequestURI());
      if (!wsuri.isAbsolute()) {
        // Netty requires an absolute url
        wsuri = new URI((ssl ? "https:" : "http:") + "//" + host + ":" + port + options.getRequestURI());
      }
      io.netty.handler.codec.http.websocketx.WebSocketVersion version;
      if (options.getVersion() == WebSocketVersion.HYBI_00) {
        version = io.netty.handler.codec.http.websocketx.WebSocketVersion.V00;
      } else if (options.getVersion() == WebSocketVersion.HYBI_08) {
        version = io.netty.handler.codec.http.websocketx.WebSocketVersion.V08;
      } else if (options.getVersion() == WebSocketVersion.RFC6455) {
        version = io.netty.handler.codec.http.websocketx.WebSocketVersion.V13;
      } else {
        throw new IllegalArgumentException("Invalid version");
      }
      HttpHeaders nettyHeaders;
      if (options.getHeaders() != null) {
        nettyHeaders = new DefaultHttpHeaders();
        for (Map.Entry<String, String> entry: options.getHeaders()) {
          nettyHeaders.add(entry.getKey(), entry.getValue());
        }
      } else {
        nettyHeaders = null;
      }
      String wsSubProtocols = null;
      if (options.getSubProtocols() != null && !options.getSubProtocols().isEmpty()) {
        StringBuilder sb = new StringBuilder();

        Iterator<String> protocols = options.getSubProtocols().iterator();
        while (protocols.hasNext()) {
          sb.append(protocols.next());
          if (protocols.hasNext()) {
            sb.append(",");
          }
        }
        wsSubProtocols = sb.toString();
      }
      handshaker = WebSocketClientHandshakerFactory.newHandshaker(wsuri, version, wsSubProtocols, false,
                                                                  nettyHeaders, maxWebSocketFrameSize);
      final ChannelPipeline p = channel.pipeline();
      p.addBefore("handler", "handshakeCompleter", new HandshakeInboundHandler(wsConnect));
      handshaker.handshake(channel).addListener(future -> {
        if (!future.isSuccess()) {
          client.handleException((Exception) future.cause());
        }
      });
    } catch (Exception e) {
      handleException(e);
    }
  }

  private final class HandshakeInboundHandler extends ChannelInboundHandlerAdapter {
    private final Handler<WebSocket> wsConnect;
    private final DefaultContext context;
    private FullHttpResponse response;
    private boolean handshaking = true;
    private final Queue<Object> buffered = new ArrayDeque<>();
    public HandshakeInboundHandler(final Handler<WebSocket> wsConnect) {
      this.wsConnect = wsConnect;
      this.context = vertx.getContext();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      context.execute(ctx.channel().eventLoop(), () -> {
        // if still handshaking this means we not got any response back from the server and so need to notify the client
        // about it as otherwise the client would never been notified.
        if (handshaking) {
          handleException(new WebSocketHandshakeException("Connection closed while handshake in process"));
        }
      });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
      context.execute(ctx.channel().eventLoop(), () -> {
        if (handshaker != null && handshaking) {
          if (msg instanceof HttpResponse) {
            HttpResponse resp = (HttpResponse) msg;
            if (resp.getStatus().code() != 101) {
              handleException(new WebSocketHandshakeException("Websocket connection attempt returned HTTP status code " + resp.getStatus().code()));
              return;
            }
            response = new DefaultFullHttpResponse(resp.getProtocolVersion(), resp.getStatus());
            response.headers().add(resp.headers());
          }

          if (msg instanceof HttpContent) {
            if (response != null) {
              response.content().writeBytes(((HttpContent) msg).content());
              if (msg instanceof LastHttpContent) {
                response.trailingHeaders().add(((LastHttpContent) msg).trailingHeaders());
                try {
                  handshakeComplete(ctx, response);
                  channel.pipeline().remove(HandshakeInboundHandler.this);
                  for (;;) {
                    Object m = buffered.poll();
                    if (m == null) {
                      break;
                    }
                    ctx.fireChannelRead(m);
                  }
                } catch (WebSocketHandshakeException e) {
                  close();
                  handleException(e);
                }
              }
            }
          }
        } else {
          buffered.add(msg);
        }
      });
    }

    private void handleException(WebSocketHandshakeException e) {
      handshaking = false;
      buffered.clear();
      client.handleException(e);
    }

    private void handshakeComplete(ChannelHandlerContext ctx, FullHttpResponse response) {
      handshaking = false;
      ChannelHandler handler = ctx.pipeline().get(HttpContentDecompressor.class);
      if (handler != null) {
        // remove decompressor as its not needed anymore once connection was upgraded to websockets
        ctx.pipeline().remove(handler);
      }
      ws = new DefaultWebSocket(vertx, ClientConnection.this);
      handshaker.finishHandshake(channel, response);
      log.debug("WebSocket handshake complete");
      wsConnect.handle(ws);
    }
  }

  public void closeHandler(Handler<Void> handler) {
    this.closeHandler = handler;
  }


  boolean isClosed() {
    return !channel.isOpen();
  }

  int getOutstandingRequestCount() {
    return requests.size();
  }

  //TODO - combine these with same in ServerConnection and NetSocket
  @Override
  public void handleInterestedOpsChanged() {
    try {
      if (!doWriteQueueFull()) {
        if (currentRequest != null) {
          setContext();
          currentRequest.handleDrained();
        } else if (ws != null) {
          ws.writable();
        }
      }
    } catch (Throwable t) {
      handleHandlerException(t);
    }
  }


  void handleResponse(HttpResponse resp) {
    if (resp.getStatus().code() == 100) {
      //If we get a 100 continue it will be followed by the real response later, so we don't remove it yet
      requestForResponse = requests.peek();
    } else {
      requestForResponse = requests.poll();
    }
    if (requestForResponse == null) {
      throw new IllegalStateException("No response handler");
    }
    setContext();
    DefaultHttpClientResponse nResp = new DefaultHttpClientResponse(vertx, requestForResponse, this, resp);
    currentResponse = nResp;
    requestForResponse.handleResponse(nResp);
  }

  void handleResponseChunk(Buffer buff) {
    setContext();
    try {
      currentResponse.handleChunk(buff);
    } catch (Throwable t) {
      handleHandlerException(t);
    }
  }

  void handleResponseEnd(LastHttpContent trailer) {
    setContext();
    try {
      currentResponse.handleEnd(trailer);
    } catch (Throwable t) {
      handleHandlerException(t);
    }
    // We don't signal response end for a 100-continue response as a real response will follow
    // Also we keep the connection open for an HTTP CONNECT
    if (currentResponse.statusCode() != 100 && requestForResponse.getRequest().getMethod() != HttpMethod.CONNECT) {
      listener.responseEnded(this);
    }
  }

  void handleWsFrame(WebSocketFrameInternal frame) {
    if (ws != null) {
      setContext();
      ws.handleFrame(frame);
    }
  }

  protected void handleClosed() {
    super.handleClosed();
    if (ws != null) {
      ws.handleClosed();
    }
  }

  protected DefaultContext getContext() {
    return super.getContext();
  }

  @Override
  protected void handleException(Throwable e) {
    super.handleException(e);
    if (currentRequest != null) {
      currentRequest.handleException(e);
    } else if (currentResponse != null) {
      currentResponse.handleException(e);
    }
  }

  void setCurrentRequest(DefaultHttpClientRequest req) {
    if (currentRequest != null) {
      throw new IllegalStateException("Connection is already writing a request");
    }
    this.currentRequest = req;
    this.requests.add(req);
  }

  void endRequest() {
    if (currentRequest == null) {
      throw new IllegalStateException("No write in progress");
    }
    currentRequest = null;
    listener.requestEnded(this);
  }

  NetSocket createNetSocket() {
    // connection was upgraded to raw TCP socket
    DefaultNetSocket socket = new DefaultNetSocket(vertx, channel, context, client.getSslHelper(), true);
    Map<Channel, DefaultNetSocket> connectionMap = new HashMap<>(1);
    connectionMap.put(channel, socket);

    // Flush out all pending data
    endReadAndFlush();


    // remove old http handlers and replace the old handler with one that handle plain sockets
    ChannelPipeline pipeline = channel.pipeline();
    ChannelHandler inflater = pipeline.get(HttpContentDecompressor.class);
    if (inflater != null) {
      pipeline.remove(inflater);
    }
    pipeline.remove("codec");
    pipeline.replace("handler", "handler", new VertxNetHandler(client.getVertx(), connectionMap) {
      @Override
      public void exceptionCaught(ChannelHandlerContext chctx, Throwable t) throws Exception {
        // remove from the real mapping
        client.removeChannel(channel);
        super.exceptionCaught(chctx, t);
      }

      @Override
      public void channelInactive(ChannelHandlerContext chctx) throws Exception {
        // remove from the real mapping
        client.removeChannel(channel);
        super.channelInactive(chctx);
      }

      @Override
      public void channelRead(ChannelHandlerContext chctx, Object msg) throws Exception {
        if (msg instanceof HttpContent) {
          ReferenceCountUtil.release(msg);
          return;
        }
        super.channelRead(chctx, msg);
      }
    });
    return socket;
  }
}