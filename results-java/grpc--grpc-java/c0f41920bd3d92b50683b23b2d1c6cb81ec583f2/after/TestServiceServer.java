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

package com.google.net.stubby.testing.integration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.net.stubby.ServerImpl;
import com.google.net.stubby.ServerInterceptors;
import com.google.net.stubby.testing.TestUtils;
import com.google.net.stubby.transport.netty.NettyServerBuilder;

import io.netty.handler.ssl.SslContext;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a single {@code TestService}. The server can be started
 * with any one of the supported transports.
 */
public class TestServiceServer {
  private static final int DEFAULT_NUM_THREADS = 100;
  private static final int STARTUP_TIMEOUT_MILLS = 5000;
  private static final String RPC_PORT_ARG = "--port";
  private static final String TRANSPORT_ARG = "--transport";
  private static final String GRPC_VERSION_ARG = "--grpc_version";
  private final TestServiceImpl testService;

  /** Supported transport protocols */
  public enum Transport {
    HTTP2_NETTY, HTTP2_NETTY_TLS
  }

  private static final Logger log = Logger.getLogger(TestServiceServer.class.getName());

  private final ScheduledExecutorService executor;
  private final int port;
  private final ServerImpl server;

  /**
   * Constructs the GRPC server.
   *
   * @param transport the transport over which to send GRPC frames.
   * @param port the port to be used for RPC communications.
   */
  public TestServiceServer(Transport transport, int port)
      throws Exception {
    Preconditions.checkNotNull(transport, "transport");
    this.executor = Executors.newScheduledThreadPool(DEFAULT_NUM_THREADS);
    this.port = port;

    // Create the GRPC service.
    testService = new TestServiceImpl(executor);

    switch (transport) {
      case HTTP2_NETTY:
        server = createServer(false);
        break;
      case HTTP2_NETTY_TLS:
        server = createServer(true);
        break;
      default:
        throw new IllegalArgumentException("Unsupported transport: " + transport);
    }
  }

  public void start() throws Exception {
    server.startAsync();
    server.awaitRunning(STARTUP_TIMEOUT_MILLS, TimeUnit.MILLISECONDS);
    log.info("GRPC server started.");
  }

  public void stop() throws Exception {
    log.info("GRPC server stopping...");
    server.stopAsync();
    server.awaitTerminated();
    MoreExecutors.shutdownAndAwaitTermination(executor, 5, TimeUnit.SECONDS);
  }

  /**
   * The main application allowing this server to be launched from the command line. Accepts the
   * following arguments:
   * <p>
   * --transport=<HTTP2_NETTY|HTTP2_NETTY_TLS> Identifies the transport
   * over which GRPC frames should be sent. <br>
   * --port=<port number> The port number for RPC communications.
   */
  public static void main(String[] args) throws Exception {
    Map<String, String> argMap = parseArgs(args);
    Transport transport = getTransport(argMap);
    int port = getPort(RPC_PORT_ARG, argMap);

    // TODO(user): Remove. Ideally stop passing the arg in scripts first.
    if (getGrpcVersion(argMap) != 2) {
      System.err.println("Only grpc_version=2 is supported");
      System.exit(1);
    }

    final TestServiceServer server = new TestServiceServer(transport, port);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          server.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    server.start();
  }

  private static Transport getTransport(Map<String, String> argMap) {
    String value = argMap.get(TRANSPORT_ARG.toLowerCase());
    Preconditions.checkNotNull(value, "%s argument must be provided.", TRANSPORT_ARG);
    Transport transport = Transport.valueOf(value.toUpperCase().trim());
    System.out.println(TRANSPORT_ARG + " set to: " + transport);
    return transport;
  }

  private static int getPort(String argName, Map<String, String> argMap) {
    String value = argMap.get(argName.toLowerCase());
    if (value != null) {
      int port = Integer.parseInt(value);
      System.out.println(argName + " set to port: " + port);
      return port;
    }

    int port = Util.pickUnusedPort();
    System.out.println(argName + " not, provided. Using port: " + port);
    return port;
  }

  private static int getGrpcVersion(Map<String, String> argMap) {
    String value = argMap.get(GRPC_VERSION_ARG.toLowerCase());
    if (value == null) {
      return 2;
    }
    int version = Integer.parseInt(value);
    System.out.println(GRPC_VERSION_ARG + " set to version: " + version);
    return version;
  }

  private static Map<String, String> parseArgs(String[] args) {
    Map<String, String> argMap = Maps.newHashMap();
    for (String arg : args) {
      String[] parts = arg.split("=");
      Preconditions.checkArgument(parts.length == 2, "Failed parsing argument: %s", arg);
      argMap.put(parts[0].toLowerCase().trim(), parts[1].trim());
    }

    return argMap;
  }

  private ServerImpl createServer(boolean enableSSL) throws Exception {
    SslContext sslContext = null;
    if (enableSSL) {
      String dir = "integration-testing/certs";
      sslContext = SslContext.newServerContext(
          new File(dir + "/server1.pem"),
          new File(dir + "/server1.key"));
    }
    return NettyServerBuilder.forPort(port)
        .executor(executor)
        .sslContext(sslContext)
        .addService(ServerInterceptors.intercept(TestServiceGrpc.bindService(testService),
              TestUtils.echoRequestHeadersInterceptor(Util.METADATA_KEY)))
        .build();
  }
}