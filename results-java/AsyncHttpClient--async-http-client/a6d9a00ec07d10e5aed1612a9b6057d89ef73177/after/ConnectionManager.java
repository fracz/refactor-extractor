/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.asynchttpclient.providers.grizzly;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.ConnectionPoolKeyStrategy;
import org.asynchttpclient.ConnectionsPool;
import org.asynchttpclient.ProxyServer;
import org.asynchttpclient.Request;
import org.asynchttpclient.util.ProxyUtils;
import org.glassfish.grizzly.CloseListener;
import org.glassfish.grizzly.CloseType;
import org.glassfish.grizzly.Closeable;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.nio.transport.TCPNIOConnectorHandler;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.utils.Futures;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ConnectionManager {

    private static final Attribute<Boolean> DO_NOT_CACHE =
        Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(ConnectionManager.class.getName());
    private final ConnectionsPool<String,Connection> pool;
    private final TCPNIOConnectorHandler connectionHandler;
    private final ConnectionMonitor connectionMonitor;
    private final GrizzlyAsyncHttpProvider provider;

    // -------------------------------------------------------- Constructors

    @SuppressWarnings("unchecked")
    ConnectionManager(final GrizzlyAsyncHttpProvider provider,
                      final TCPNIOTransport transport) {

        ConnectionsPool<String,Connection> connectionPool;
        this.provider = provider;
        final AsyncHttpClientConfig config = provider.clientConfig;
        if (config.getAllowPoolingConnection()) {
            ConnectionsPool pool = config.getConnectionsPool();
            if (pool != null) {
                //noinspection unchecked
                connectionPool = (ConnectionsPool<String, Connection>) pool;
            } else {
                connectionPool = new GrizzlyConnectionsPool((config));
            }
        } else {
            connectionPool = new NonCachingPool();
        }
        pool = connectionPool;
        connectionHandler = TCPNIOConnectorHandler.builder(transport).build();
        final int maxConns = provider.clientConfig.getMaxTotalConnections();
        connectionMonitor = new ConnectionMonitor(maxConns);


    }

    // ----------------------------------------------------- Private Methods

    static void markConnectionAsDoNotCache(final Connection c) {
        DO_NOT_CACHE.set(c, Boolean.TRUE);
    }

    static boolean isConnectionCacheable(final Connection c) {
        final Boolean canCache =  DO_NOT_CACHE.get(c);
        return ((canCache != null) ? canCache : false);
    }

    void doAsyncTrackedConnection(final Request request,
                                  final GrizzlyResponseFuture requestFuture,
                                  final CompletionHandler<Connection> connectHandler)
    throws IOException, ExecutionException, InterruptedException {
        Connection c = pool.poll(getPoolKey(request, requestFuture.getProxyServer()));
        if (c == null) {
            if (!connectionMonitor.acquire()) {
                throw new IOException("Max connections exceeded");
            }
            doAsyncConnect(request, requestFuture, connectHandler);
        } else {
            provider.touchConnection(c, request);
            connectHandler.completed(c);
        }

    }

    Connection obtainConnection(final Request request,
                                final GrizzlyResponseFuture requestFuture)
    throws IOException, ExecutionException, InterruptedException, TimeoutException {

        final Connection c = obtainConnection0(request, requestFuture, requestFuture.getProxyServer());
        markConnectionAsDoNotCache(c);
        return c;

    }

    void doAsyncConnect(final Request request,
                        final GrizzlyResponseFuture requestFuture,
                        final CompletionHandler<Connection> connectHandler)
    throws IOException, ExecutionException, InterruptedException {

        ProxyServer proxy = requestFuture.getProxyServer();
        final URI uri = request.getURI();
        String host = proxy != null ? proxy.getHost() : uri.getHost();
        int port = proxy != null ? proxy.getPort() : uri.getPort();
        if (request.getLocalAddress() != null) {
            connectionHandler.connect(new InetSocketAddress(host, GrizzlyAsyncHttpProvider
                    .getPort(uri, port)), new InetSocketAddress(request.getLocalAddress(), 0),
                    createConnectionCompletionHandler(request, requestFuture, connectHandler));
        } else {
            connectionHandler.connect(new InetSocketAddress(host, GrizzlyAsyncHttpProvider
                    .getPort(uri, port)),
                    createConnectionCompletionHandler(request, requestFuture, connectHandler));
        }

    }

    private Connection obtainConnection0(final Request request,
                                         final GrizzlyResponseFuture requestFuture,
                                         final ProxyServer proxy)
    throws IOException, ExecutionException, InterruptedException, TimeoutException {

        final URI uri = request.getURI();
        String host = proxy != null ? proxy.getHost() : uri.getHost();
        int port = proxy != null ? proxy.getPort() : uri.getPort();
        int cTimeout = provider.clientConfig.getConnectionTimeoutInMs();
        FutureImpl<Connection> future = Futures.createSafeFuture();
        CompletionHandler<Connection> ch = Futures.toCompletionHandler(future,
                createConnectionCompletionHandler(request, requestFuture, null));
        if (cTimeout > 0) {
            connectionHandler.connect(new InetSocketAddress(host, GrizzlyAsyncHttpProvider
                    .getPort(uri, port)),
                    ch);
            return future.get(cTimeout, TimeUnit.MILLISECONDS);
        } else {
            connectionHandler.connect(new InetSocketAddress(host, GrizzlyAsyncHttpProvider
                    .getPort(uri, port)),
                    ch);
            return future.get();
        }
    }

    boolean returnConnection(final Request request, final Connection c) {
        ProxyServer proxyServer = ProxyUtils.getProxyServer(
                provider.clientConfig, request);
        final boolean result = (DO_NOT_CACHE.get(c) == null
                                   && pool.offer(getPoolKey(request, proxyServer), c));
        if (result) {
            if (provider.resolver != null) {
                provider.resolver.setTimeoutMillis(c, IdleTimeoutFilter.FOREVER);
            }
        }
        return result;

    }


    boolean canReturnConnection(final Connection c) {

        return (DO_NOT_CACHE.get(c) != null || pool.canCacheConnection());

    }


    void destroy() {

        pool.destroy();

    }

    CompletionHandler<Connection> createConnectionCompletionHandler(final Request request,
                                                                    final GrizzlyResponseFuture future,
                                                                    final CompletionHandler<Connection> wrappedHandler) {
        return new CompletionHandler<Connection>() {
            public void cancelled() {
                if (wrappedHandler != null) {
                    wrappedHandler.cancelled();
                } else {
                    future.cancel(true);
                }
            }

            public void failed(Throwable throwable) {
                if (wrappedHandler != null) {
                    wrappedHandler.failed(throwable);
                } else {
                    future.abort(throwable);
                }
            }

            public void completed(Connection connection) {
                future.setConnection(connection);
                provider.touchConnection(connection, request);
                if (wrappedHandler != null) {
                    connection.addCloseListener(connectionMonitor);
                    wrappedHandler.completed(connection);
                }
            }

            public void updated(Connection result) {
                if (wrappedHandler != null) {
                    wrappedHandler.updated(result);
                }
            }
        };
    }

    private static String getPoolKey(final Request request, ProxyServer proxyServer) {
        final ConnectionPoolKeyStrategy keyStrategy = request.getConnectionPoolKeyStrategy();
        URI uri = proxyServer != null? proxyServer.getURI(): request.getURI();
        return keyStrategy.getKey(uri);
    }

    // ------------------------------------------------------ Nested Classes

    private static class ConnectionMonitor implements
            CloseListener<Closeable,CloseType> {

    private final Semaphore connections;

        // ------------------------------------------------------------ Constructors


        ConnectionMonitor(final int maxConnections) {
            if (maxConnections != -1) {
                connections = new Semaphore(maxConnections);
            } else {
                connections = null;
            }
        }

        // ----------------------------------- Methods from Connection.CloseListener


        public boolean acquire() {

            return (connections == null || connections.tryAcquire());

        }

        @Override
        public void onClosed(Closeable closeable, CloseType closeType) throws IOException {

            if (connections != null) {
                connections.release();
            }

        }

    } // END ConnectionMonitor

    private static final class NonCachingPool implements ConnectionsPool<String,Connection> {


        // ---------------------------------------- Methods from ConnectionsPool


        public boolean offer(String uri, Connection connection) {
            return false;
        }

        public Connection poll(String uri) {
            return null;
        }

        public boolean removeAll(Connection connection) {
            return false;
        }

        public boolean canCacheConnection() {
            return true;
        }

        public void destroy() {
            // no-op
        }

    } // END NonCachingPool

}