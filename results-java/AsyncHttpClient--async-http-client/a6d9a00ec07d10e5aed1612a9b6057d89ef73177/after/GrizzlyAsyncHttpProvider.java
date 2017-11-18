/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.asynchttpclient.providers.grizzly;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.org.jboss.netty.handler.codec.http.CookieDecoder;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpProvider;
import org.asynchttpclient.AsyncHttpProviderConfig;
import org.asynchttpclient.Body;
import org.asynchttpclient.BodyGenerator;
import org.asynchttpclient.Cookie;
import org.asynchttpclient.FluentCaseInsensitiveStringsMap;
import org.asynchttpclient.FluentStringsMap;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.MaxRedirectException;
import org.asynchttpclient.ProxyServer;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.UpgradeHandler;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.ResponseFilter;
import org.asynchttpclient.listener.TransferCompletionHandler;
import org.asynchttpclient.ntlm.NTLMEngine;
import org.asynchttpclient.websocket.WebSocket;
import org.asynchttpclient.websocket.WebSocketByteListener;
import org.asynchttpclient.websocket.WebSocketCloseCodeReasonListener;
import org.asynchttpclient.websocket.WebSocketListener;
import org.asynchttpclient.websocket.WebSocketPingListener;
import org.asynchttpclient.websocket.WebSocketPongListener;
import org.asynchttpclient.websocket.WebSocketTextListener;
import org.asynchttpclient.websocket.WebSocketUpgradeHandler;
import org.asynchttpclient.multipart.MultipartRequestEntity;
import org.asynchttpclient.util.AsyncHttpProviderUtils;
import org.asynchttpclient.util.AuthenticatorUtils;
import org.asynchttpclient.util.ProxyUtils;
import org.asynchttpclient.util.SslUtils;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.FileTransfer;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeStorage;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.FilterChainEvent;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.ContentEncoding;
import org.glassfish.grizzly.http.EncodingFilter;
import org.glassfish.grizzly.http.GZipContentEncoding;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.npn.ClientSideNegotiator;
import org.glassfish.grizzly.spdy.NextProtoNegSupport;
import org.glassfish.grizzly.spdy.SpdyFramingFilter;
import org.glassfish.grizzly.spdy.SpdyHandlerFilter;
import org.glassfish.grizzly.spdy.SpdyMode;
import org.glassfish.grizzly.spdy.SpdySession;
import org.glassfish.grizzly.ssl.SSLBaseFilter;
import org.glassfish.grizzly.ssl.SSLConnectionContext;
import org.glassfish.grizzly.ssl.SSLUtils;
import org.glassfish.grizzly.utils.Charsets;
import org.glassfish.grizzly.http.util.CookieSerializerUtils;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.utils.BufferOutputStream;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.HandShake;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.SimpleWebSocket;
import org.glassfish.grizzly.websockets.Version;
import org.glassfish.grizzly.websockets.WebSocketClientFilter;
import org.glassfish.grizzly.websockets.WebSocketHolder;
import org.glassfish.grizzly.websockets.draft06.ClosingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.asynchttpclient.providers.grizzly.GrizzlyAsyncHttpProvider.SwitchingSSLFilter.SSLSwitchingEvent;
import static org.asynchttpclient.providers.grizzly.GrizzlyAsyncHttpProviderConfig.Property;
import static org.asynchttpclient.providers.grizzly.GrizzlyAsyncHttpProviderConfig.Property.MAX_HTTP_PACKET_HEADER_SIZE;
import static org.asynchttpclient.util.MiscUtil.isNonEmpty;

/**
 * A Grizzly 2.0-based implementation of {@link AsyncHttpProvider}.
 *
 * @author The Grizzly Team
 * @since 1.7.0
 */
@SuppressWarnings("rawtypes")
public class GrizzlyAsyncHttpProvider implements AsyncHttpProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(GrizzlyAsyncHttpProvider.class);
    private static final boolean SEND_FILE_SUPPORT;
    static {
        SEND_FILE_SUPPORT = configSendFileSupport();
    }
    private final Attribute<HttpTransactionContext> REQUEST_STATE_ATTR =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(HttpTransactionContext.class.getName());

    private final BodyHandlerFactory bodyHandlerFactory = new BodyHandlerFactory();

    final TCPNIOTransport clientTransport;
    final AsyncHttpClientConfig clientConfig;
    private final ConnectionManager connectionManager;

    DelayedExecutor.Resolver<Connection> resolver;
    private DelayedExecutor timeoutExecutor;

    private final static NTLMEngine ntlmEngine = new NTLMEngine();

    // ------------------------------------------------------------ Constructors


    public GrizzlyAsyncHttpProvider(final AsyncHttpClientConfig clientConfig) {

        this.clientConfig = clientConfig;
        final TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        clientTransport = builder.build();
        initializeTransport(clientConfig);
        connectionManager = new ConnectionManager(this, clientTransport);
        try {
            clientTransport.start();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }


    // ------------------------------------------ Methods from AsyncHttpProvider


    /**
     * {@inheritDoc}
     */
    public <T> ListenableFuture<T> execute(final Request request,
            final AsyncHandler<T> handler) throws IOException {

        final ProxyServer proxy = ProxyUtils.getProxyServer(clientConfig, request);
        final GrizzlyResponseFuture<T> future = new GrizzlyResponseFuture<T>(this, request, handler, proxy);
        future.setDelegate(SafeFutureImpl.<T>create());
        final CompletionHandler<Connection>  connectHandler = new CompletionHandler<Connection>() {
            @Override
            public void cancelled() {
                future.cancel(true);
            }

            @Override
            public void failed(final Throwable throwable) {
                future.abort(throwable);
            }

            @Override
            public void completed(final Connection c) {
                try {
                    execute(c, request, handler, future);
                } catch (Exception e) {
                    failed(e);
                }
            }

            @Override
            public void updated(final Connection c) {
                // no-op
            }
        };

        try {
            connectionManager.doAsyncTrackedConnection(request, future, connectHandler);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(e.toString(), e);
            }
        }

        return future;
    }

    /**
     * {@inheritDoc}
     */
    public void close() {

        try {
            connectionManager.destroy();
            clientTransport.stop();
            final ExecutorService service = clientConfig.executorService();
            if (service != null) {
                service.shutdown();
            }
            if (timeoutExecutor != null) {
                timeoutExecutor.stop();
            }
        } catch (IOException ignored) { }

    }


    /**
     * {@inheritDoc}
     */
    public Response prepareResponse(HttpResponseStatus status,
                                    HttpResponseHeaders headers,
                                    List<HttpResponseBodyPart> bodyParts) {

        return new GrizzlyResponse(status, headers, bodyParts);

    }


    // ------------------------------------------------------- Protected Methods


    @SuppressWarnings({"unchecked"})
    protected <T> ListenableFuture<T> execute(final Connection c,
                                              final Request request,
                                              final AsyncHandler<T> handler,
                                              final GrizzlyResponseFuture<T> future)
    throws IOException {

        try {
            if (getHttpTransactionContext(c) == null) {
                setHttpTransactionContext(c,
                        new HttpTransactionContext(future, request, handler));
            }
            c.write(request, createWriteCompletionHandler(future));
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(e.toString(), e);
            }
        }

        return future;
    }


    protected void initializeTransport(final AsyncHttpClientConfig clientConfig) {

        final FilterChainBuilder fcb = FilterChainBuilder.stateless();
        fcb.add(new AsyncHttpClientTransportFilter());

        final int timeout = clientConfig.getRequestTimeoutInMs();
        if (timeout > 0) {
            int delay = 500;
            if (timeout < delay) {
                delay = timeout - 10;
                if (delay <= 0) {
                    delay = timeout;
                }
            }
            timeoutExecutor = IdleTimeoutFilter.createDefaultIdleDelayedExecutor(delay, TimeUnit.MILLISECONDS);
            timeoutExecutor.start();
            final IdleTimeoutFilter.TimeoutResolver timeoutResolver =
                    new IdleTimeoutFilter.TimeoutResolver() {
                        @Override
                        public long getTimeout(FilterChainContext ctx) {
                            final HttpTransactionContext context =
                                    GrizzlyAsyncHttpProvider.this.getHttpTransactionContext(ctx.getConnection());
                            if (context != null) {
                                if (context.isWSRequest) {
                                    return clientConfig.getWebSocketIdleTimeoutInMs();
                                }
                                int requestTimeout = AsyncHttpProviderUtils.requestTimeout(clientConfig, context.request);
                                if (requestTimeout > 0) {
                                    return requestTimeout;
                                }
                            }
                            return timeout;
                        }
                    };
            final IdleTimeoutFilter timeoutFilter = new IdleTimeoutFilter(timeoutExecutor,
                    timeoutResolver,
                    new IdleTimeoutFilter.TimeoutHandler() {
                        public void onTimeout(Connection connection) {
                            timeout(connection);
                        }
                    });
            fcb.add(timeoutFilter);
            resolver = timeoutFilter.getResolver();
        }

        SSLContext context = clientConfig.getSSLContext();
        boolean defaultSecState = (context != null);
        if (context == null) {
            try {
                context = SslUtils.getSSLContext();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        final SSLEngineConfigurator configurator =
                new SSLEngineConfigurator(context,
                        true,
                        false,
                        false);
        final SwitchingSSLFilter filter = new SwitchingSSLFilter(configurator, defaultSecState);
        ProtocolHandshakeListener handshakeListener =
                                                new ProtocolHandshakeListener();
        filter.addHandshakeListener(handshakeListener);
        fcb.add(filter);
        GrizzlyAsyncHttpProviderConfig providerConfig =
                        (GrizzlyAsyncHttpProviderConfig) clientConfig.getAsyncHttpProviderConfig();

        boolean npnEnabled = NextProtoNegSupport.isEnabled();
        boolean spdyEnabled = clientConfig.isSpdyEnabled();

        if (spdyEnabled) {
            // if NPN isn't available, check to see if it has been explicitly
            // disabled.  If it has, we assume the user knows what they are doing
            // and we enable SPDY without NPN - this effectively disables standard
            // HTTP/1.1 support.
            if (!npnEnabled && providerConfig != null) {
                if ((Boolean) providerConfig.getProperty(Property.NPN_ENABLED)) {
                    // NPN hasn't been disabled, so it's most likely a configuration problem.
                    // Log a warning and disable spdy support.
                    LOGGER.warn("Next Protocol Negotiation support is not available.  SPDY support has been disabled.");
                    spdyEnabled = false;
                }
            }
        }

        final AsyncHttpClientEventFilter eventFilter;
        final EventHandler handler = new EventHandler(this);
        if (providerConfig != null) {
            eventFilter =
                    new AsyncHttpClientEventFilter(handler,
                                                   (Integer) providerConfig
                                                           .getProperty(
                                                                   MAX_HTTP_PACKET_HEADER_SIZE));
        } else {
            eventFilter = new AsyncHttpClientEventFilter(handler);
        }
        handler.cleanup = eventFilter;
        ContentEncoding[] encodings = eventFilter.getContentEncodings();
        if (encodings.length > 0) {
            for (ContentEncoding encoding : encodings) {
                eventFilter.removeContentEncoding(encoding);
            }
        }
        if (clientConfig.isCompressionEnabled()) {
            eventFilter.addContentEncoding(
                    new GZipContentEncoding(512,
                                            512,
                                            new ClientEncodingFilter()));
        }
        fcb.add(eventFilter);
        final AsyncHttpClientFilter clientFilter =
                new AsyncHttpClientFilter(clientConfig);
        fcb.add(clientFilter);
        fcb.add(new WebSocketClientFilter());


        if (providerConfig != null) {
            final TransportCustomizer customizer = (TransportCustomizer)
                    providerConfig.getProperty(Property.TRANSPORT_CUSTOMIZER);
            if (customizer != null) {
                customizer.customize(clientTransport, fcb);
            } else {
                doDefaultTransportConfig();
            }
        } else {
            doDefaultTransportConfig();
        }

        // FilterChain for the standard HTTP case has been configured, we now
        // copy it and modify for SPDY purposes.
        if (spdyEnabled) {
            FilterChainBuilder spdyFilterChain =
                    createSpdyFilterChain(fcb, npnEnabled);
            ProtocolNegotiator pn =
                    new ProtocolNegotiator(spdyFilterChain.build());
            NextProtoNegSupport.getInstance()
                    .setClientSideNegotiator(clientTransport, pn);
        }

        // Don't limit the number of bytes the client can have queued to write.
        clientTransport.getAsyncQueueIO().getWriter().setMaxPendingBytesPerConnection(-1);

        // Install the HTTP filter chain.
        clientTransport.setProcessor(fcb.build());

    }


    // ------------------------------------------------- Package Private Methods


    void touchConnection(final Connection c, final Request request) {

        int requestTimeout = AsyncHttpProviderUtils.requestTimeout(clientConfig, request);
        if (requestTimeout > 0) {
            if (resolver != null) {
                resolver.setTimeoutMillis(c, System.currentTimeMillis() + requestTimeout);
            }
        }

    }


    // --------------------------------------------------------- Private Methods


    private FilterChainBuilder createSpdyFilterChain(final FilterChainBuilder fcb,
                                                     final boolean npnEnabled) {

        FilterChainBuilder spdyFcb = FilterChainBuilder.stateless();
        spdyFcb.addAll(fcb);
        int idx = spdyFcb.indexOfType(SSLFilter.class);
        Filter f = spdyFcb.get(idx);


        // Adjust the SSLFilter to support NPN
        if (npnEnabled) {
            SSLBaseFilter sslBaseFilter = (SSLBaseFilter) f;
            NextProtoNegSupport.getInstance().configure(sslBaseFilter);
        }

        // Remove the HTTP Client filter - this will be replaced by the
        // SPDY framing and handler filters.
        idx = spdyFcb.indexOfType(HttpClientFilter.class);
        spdyFcb.set(idx, new SpdyFramingFilter());
        final SpdyMode spdyMode = ((npnEnabled) ? SpdyMode.NPN : SpdyMode.PLAIN);
        AsyncSpdyClientEventFilter spdyFilter =
                new AsyncSpdyClientEventFilter(new EventHandler(this),
                                               spdyMode,
                                               clientConfig.executorService());
        spdyFilter.setInitialWindowSize(clientConfig.getSpdyInitialWindowSize());
        spdyFilter.setMaxConcurrentStreams(clientConfig.getSpdyMaxConcurrentStreams());
        spdyFcb.add(idx + 1, spdyFilter);

        // Remove the WebSocket filter - not currently supported.
        idx = spdyFcb.indexOfType(WebSocketClientFilter.class);
        spdyFcb.remove(idx);

        return spdyFcb;
    }


    private static boolean configSendFileSupport() {

        return !((System.getProperty("os.name").equalsIgnoreCase("linux")
                && !linuxSendFileSupported())
                || System.getProperty("os.name").equalsIgnoreCase("HP-UX"));
    }


    private static boolean linuxSendFileSupported() {
        final String version = System.getProperty("java.version");
        if (version.startsWith("1.6")) {
            int idx = version.indexOf('_');
            if (idx == -1) {
                return false;
            }
            final int patchRev = Integer.parseInt(version.substring(idx + 1));
            return (patchRev >= 18);
        } else {
            return version.startsWith("1.7") || version.startsWith("1.8");
        }
    }

    private void doDefaultTransportConfig() {
        final ExecutorService service = clientConfig.executorService();
        if (service != null) {
            clientTransport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
            clientTransport.setWorkerThreadPool(service);
        } else {
            clientTransport.setIOStrategy(SameThreadIOStrategy.getInstance());
        }
    }

    private <T> CompletionHandler<WriteResult> createWriteCompletionHandler(final GrizzlyResponseFuture<T> future) {
        return new CompletionHandler<WriteResult>() {

            public void cancelled() {
                future.cancel(true);
            }

            public void failed(Throwable throwable) {
                future.abort(throwable);
            }

            public void completed(WriteResult result) {
            }

            public void updated(WriteResult result) {
                // no-op
            }

        };
    }


    void setHttpTransactionContext(final AttributeStorage storage,
                                   final HttpTransactionContext httpTransactionState) {

        if (httpTransactionState == null) {
            REQUEST_STATE_ATTR.remove(storage);
        } else {
            REQUEST_STATE_ATTR.set(storage, httpTransactionState);
        }

    }

    HttpTransactionContext getHttpTransactionContext(final AttributeStorage storage) {

        return REQUEST_STATE_ATTR.get(storage);

    }


    void timeout(final Connection c) {

        final HttpTransactionContext context = getHttpTransactionContext(c);
        setHttpTransactionContext(c, null);
        context.abort(new TimeoutException("Timeout exceeded"));

    }

    static int getPort(final URI uri, final int p) {
        int port = p;
        if (port == -1) {
            final String protocol = uri.getScheme().toLowerCase();
            if ("http".equals(protocol) || "ws".equals(protocol)) {
                port = 80;
            } else if ("https".equals(protocol) || "wss".equals(protocol)) {
                port = 443;
            } else {
                throw new IllegalArgumentException("Unknown protocol: " + protocol);
            }
        }
        return port;
    }


    @SuppressWarnings({"unchecked"})
    boolean sendRequest(final FilterChainContext ctx,
                     final Request request,
                     final HttpRequestPacket requestPacket)
    throws IOException {

        boolean isWriteComplete = true;

        if (requestHasEntityBody(request)) {
            final HttpTransactionContext context = getHttpTransactionContext(ctx.getConnection());
            BodyHandler handler = bodyHandlerFactory.getBodyHandler(request);
            if (requestPacket.getHeaders().contains(Header.Expect)
                    && requestPacket.getHeaders().getValue(1).equalsIgnoreCase("100-Continue")) {
                // We have to set the content-length now as the headers will be flushed
                // before the FileBodyHandler is invoked.  If we don't do it here, and
                // the user didn't explicitly set the length, then the transfer-encoding
                // will be chunked and zero-copy file transfer will not occur.
                final File f = request.getFile();
                if (f != null) {
                    requestPacket.setContentLengthLong(f.length());
                }
                handler = new ExpectHandler(handler);
            }
            context.bodyHandler = handler;
            isWriteComplete = handler.doHandle(ctx, request, requestPacket);
        } else {
            HttpContent content = HttpContent.builder(requestPacket).last(true).build();
            ctx.write(content, ctx.getTransportContext().getCompletionHandler());
//            FilterChainContext newContext = new FilterChainContext();
//            FilterChain fc = (FilterChain) ctx.getConnection().getProcessor();
//            final FilterChainContext newFilterChainContext =
//                            fc.obtainFilterChainContext(
//                                    ctx.getConnection(),
//                                    ctx.getStartIdx(),
//                                    fc.size(),
//                                    ctx.getFilterIdx());
//
//                    newFilterChainContext.setAddressHolder(ctx.getAddressHolder());
//                    newFilterChainContext.getInternalContext().setProcessor(fc);
//                    //newFilterChainContext.setMessage(ctx.getMessage());
//            newContext.write(content, ctx.getTransportContext().getCompletionHandler());
        }
        LOGGER.debug("REQUEST: {}", requestPacket);

        return isWriteComplete;
    }


    private static boolean requestHasEntityBody(final Request request) {

        final String method = request.getMethod();
        return (Method.POST.matchesMethod(method)
                || Method.PUT.matchesMethod(method)
                || Method.PATCH.matchesMethod(method)
                || Method.DELETE.matchesMethod(method));

    }


    // ----------------------------------------------------------- Inner Classes


    private interface StatusHandler {

        public enum InvocationStatus {
            CONTINUE,
            STOP
        }

        boolean handleStatus(final HttpResponsePacket httpResponse,
                             final HttpTransactionContext httpTransactionContext,
                             final FilterChainContext ctx);

        boolean handlesStatus(final int statusCode);

    } // END StatusHandler


    final class HttpTransactionContext {

        final AtomicInteger redirectCount = new AtomicInteger(0);

        final int maxRedirectCount;
        final boolean redirectsAllowed;
        final GrizzlyAsyncHttpProvider provider =
                GrizzlyAsyncHttpProvider.this;

        Request request;
        String requestUrl;
        AsyncHandler handler;
        BodyHandler bodyHandler;
        StatusHandler statusHandler;
        StatusHandler.InvocationStatus invocationStatus =
                StatusHandler.InvocationStatus.CONTINUE;
        GrizzlyResponseStatus responseStatus;
        GrizzlyResponseFuture future;
        String lastRedirectURI;
        AtomicLong totalBodyWritten = new AtomicLong();
        AsyncHandler.STATE currentState;

        String wsRequestURI;
        boolean isWSRequest;
        HandShake handshake;
        ProtocolHandler protocolHandler;
        WebSocket webSocket;
        boolean establishingTunnel;


        // -------------------------------------------------------- Constructors


        HttpTransactionContext(final GrizzlyResponseFuture future,
                               final Request request,
                               final AsyncHandler handler) {

            this.future = future;
            this.request = request;
            this.handler = handler;
            redirectsAllowed = provider.clientConfig.isRedirectEnabled();
            maxRedirectCount = provider.clientConfig.getMaxRedirects();
            this.requestUrl = request.getUrl();

        }


        // ----------------------------------------------------- Private Methods


        HttpTransactionContext copy() {
            final HttpTransactionContext newContext =
                    new HttpTransactionContext(future,
                                               request,
                                               handler);
            newContext.invocationStatus = invocationStatus;
            newContext.bodyHandler = bodyHandler;
            newContext.currentState = currentState;
            newContext.statusHandler = statusHandler;
            newContext.lastRedirectURI = lastRedirectURI;
            newContext.redirectCount.set(redirectCount.get());
            return newContext;

        }


        void abort(final Throwable t) {
            if (future != null) {
                future.abort(t);
            }
        }

        void done(final Callable c) {
            if (future != null) {
                future.done(c);
            }
        }

        @SuppressWarnings({"unchecked"})
        void result(Object result) {
            if (future != null) {
                future.delegate.result(result);
                future.done(null);
            }
        }

        boolean isTunnelEstablished(final Connection c) {
            return c.getAttributes().getAttribute("tunnel-established") != null;
        }


        void tunnelEstablished(final Connection c) {
            c.getAttributes().setAttribute("tunnel-established", Boolean.TRUE);
        }


    } // END HttpTransactionContext


    // ---------------------------------------------------------- Nested Classes


    static final class ProtocolHandshakeListener implements
            SSLBaseFilter.HandshakeListener {


        static ConcurrentHashMap<Connection,HandshakeCompleteListener> listeners =
                new ConcurrentHashMap<Connection,HandshakeCompleteListener>();


        // --------------------------------------- Method from HandshakeListener


        @Override
        public void onStart(Connection connection) {
            // no-op
        }

        @Override
        public void onComplete(Connection connection) {
            final HandshakeCompleteListener listener = listeners.get(connection);
            if (listener != null) {
                removeListener(connection);
                listener.complete();
            }
        }


        // --------------------------------------------- Package Private Methods


        public static void addListener(final Connection c,
                                       final HandshakeCompleteListener listener) {
            listeners.putIfAbsent(c, listener);
        }

        static void removeListener(final Connection c) {
            listeners.remove(c);
        }
    }

    private static interface HandshakeCompleteListener {
        void complete();
    }


    private static final class ProtocolNegotiator implements ClientSideNegotiator {


        private static final String SPDY = "spdy/3";
        private static final String HTTP = "HTTP/1.1";

        private final FilterChain spdyFilterChain;
        private final SpdyHandlerFilter spdyHandlerFilter;


        // -------------------------------------------------------- Constructors

        private ProtocolNegotiator(final FilterChain spdyFilterChain) {
            this.spdyFilterChain = spdyFilterChain;
            int idx = spdyFilterChain.indexOfType(SpdyHandlerFilter.class);
            spdyHandlerFilter = (SpdyHandlerFilter) spdyFilterChain.get(idx);
        }


        // ----------------------------------- Methods from ClientSideNegotiator


        @Override
        public boolean wantNegotiate(SSLEngine engine) {
            GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::wantNegotiate");
            return true;
        }

        @Override
        public String selectProtocol(SSLEngine engine, LinkedHashSet<String> strings) {
            GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::selectProtocol: " + strings);
            final Connection connection = NextProtoNegSupport.getConnection(engine);

            // Give preference to SPDY/3.  If not available, check for HTTP as a
            // fallback
            if (strings.contains(SPDY)) {
                GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::selecting: " + SPDY);
                SSLConnectionContext sslCtx =
                                        SSLUtils.getSslConnectionContext(connection);
                                sslCtx.setNewConnectionFilterChain(spdyFilterChain);
                final SpdySession spdySession =
                        new SpdySession(connection, false, spdyHandlerFilter);
                spdySession.setLocalInitialWindowSize(spdyHandlerFilter.getInitialWindowSize());
                spdySession.setLocalMaxConcurrentStreams(spdyHandlerFilter.getMaxConcurrentStreams());

                SpdySession.bind(connection, spdySession);
                return SPDY;
            } else if (strings.contains(HTTP)) {
                GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::selecting: " + HTTP);
                // Use the default HTTP FilterChain.
                return HTTP;
            } else {
                GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::selecting NONE");
                // no protocol support.  Will close the connection when
                // onNoDeal is invoked
                return "";
            }
        }

        @Override
        public void onNoDeal(SSLEngine engine) {
            GrizzlyAsyncHttpProvider.LOGGER.info("ProtocolSelector::onNoDeal");
            final Connection connection = NextProtoNegSupport.getConnection(engine);
            connection.closeSilently();
        }
    }


    private static final class ContinueEvent implements FilterChainEvent {

        private final HttpTransactionContext context;


        // -------------------------------------------------------- Constructors


        ContinueEvent(final HttpTransactionContext context) {

            this.context = context;

        }


        // --------------------------------------- Methods from FilterChainEvent


        @Override
        public Object type() {
            return ContinueEvent.class;
        }

    } // END ContinueEvent


    private final class AsyncHttpClientTransportFilter extends TransportFilter {

        @Override
        public NextAction handleRead(FilterChainContext ctx) throws IOException {
            final HttpTransactionContext context = getHttpTransactionContext(ctx.getConnection());
            if (context == null) {
                return super.handleRead(ctx);
            }
            ctx.getTransportContext().setCompletionHandler(new CompletionHandler() {
                @Override
                public void cancelled() {

                }

                @Override
                public void failed(Throwable throwable) {
                    if (throwable instanceof EOFException) {
                        context.abort(new IOException("Remotely Closed"));
                    }
                }

                @Override
                public void completed(Object result) {
                }

                @Override
                public void updated(Object result) {
                }
            });
            return super.handleRead(ctx);
        }

        @Override
        public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
            final HttpTransactionContext context = getHttpTransactionContext(ctx.getConnection());
            if (context != null) {
                context.abort(error.getCause());
            }
        }
    } // END AsyncHttpClientTransportFilter


    private final class AsyncHttpClientFilter extends BaseFilter {


        private final AsyncHttpClientConfig config;


        // -------------------------------------------------------- Constructors


        AsyncHttpClientFilter(final AsyncHttpClientConfig config) {

            this.config = config;

        }


        // --------------------------------------------- Methods from BaseFilter


        @Override
        public NextAction handleWrite(final FilterChainContext ctx)
        throws IOException {

            Object message = ctx.getMessage();
            if (message instanceof Request) {
                ctx.setMessage(null);
                if (!sendAsGrizzlyRequest((Request) message, ctx)) {
                    return ctx.getSuspendAction();
                }
            } else if (message instanceof Buffer) {
                return ctx.getInvokeAction();
            }

            return ctx.getStopAction();
        }

        @Override
        public NextAction handleEvent(final FilterChainContext ctx,
                                      final FilterChainEvent event)
        throws IOException {

            final Object type = event.type();
            if (type == ContinueEvent.class) {
                final ContinueEvent continueEvent = (ContinueEvent) event;
                ((ExpectHandler) continueEvent.context.bodyHandler).finish(ctx);
            }

            return ctx.getStopAction();

        }


        // ----------------------------------------------------- Private Methods


        private boolean sendAsGrizzlyRequest(final Request request,
                                             final FilterChainContext ctx)
        throws IOException {

            final HttpTransactionContext httpCtx = getHttpTransactionContext(ctx.getConnection());
            if (isUpgradeRequest(httpCtx.handler) && isWSRequest(httpCtx.requestUrl)) {
                httpCtx.isWSRequest = true;
                convertToUpgradeRequest(httpCtx);
            }
            final URI uri = httpCtx.request.getURI();
            final HttpRequestPacket.Builder builder = HttpRequestPacket.builder();
            final String scheme = uri.getScheme();
            boolean secure = isSecure(scheme);
            builder.method(request.getMethod());
            builder.protocol(Protocol.HTTP_1_1);
            addHostHeader(request, uri, builder);
            final ProxyServer proxy = ProxyUtils.getProxyServer(config, request);
            final boolean useProxy = proxy != null;
            if (useProxy) {
                if ((secure || httpCtx.isWSRequest) && !httpCtx.isTunnelEstablished(ctx.getConnection())) {
                    ctx.notifyDownstream(new SSLSwitchingEvent(false, ctx.getConnection(), null));
                    secure = false;
                    httpCtx.establishingTunnel = true;
                    builder.method(Method.CONNECT);
                    builder.uri(AsyncHttpProviderUtils.getAuthority(uri));
                } else if (secure && config.isUseRelativeURIsWithSSLProxies()){
                    builder.uri(uri.getPath());
                } else {
                    builder.uri(uri.toString());
                }
            } else {
                builder.uri(uri.getPath());
            }
            if (requestHasEntityBody(request)) {
                final long contentLength = request.getContentLength();
                if (contentLength > 0) {
                    builder.contentLength(contentLength);
                    builder.chunked(false);
                } else {
                    builder.chunked(true);
                }
            }

            HttpRequestPacket requestPacket;
            if (httpCtx.isWSRequest && !httpCtx.establishingTunnel) {
                try {
                    final URI wsURI = new URI(httpCtx.wsRequestURI);
                    httpCtx.protocolHandler = Version.DRAFT17.createHandler(true);
                    httpCtx.handshake = httpCtx.protocolHandler.createHandShake(wsURI);
                    requestPacket = (HttpRequestPacket)
                            httpCtx.handshake.composeHeaders().getHttpHeader();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid WS URI: " + httpCtx.wsRequestURI);
                }
            } else {
                requestPacket = builder.build();
            }
            requestPacket.setSecure(secure);
            if (!useProxy && !httpCtx.isWSRequest) {
                addQueryString(request, requestPacket);
            }
            addHeaders(request, requestPacket, proxy);
            addCookies(request, requestPacket);

            final AsyncHandler h = httpCtx.handler;
            if (h != null) {
                if (TransferCompletionHandler.class.isAssignableFrom(
                        h.getClass())) {
                    final FluentCaseInsensitiveStringsMap map =
                            new FluentCaseInsensitiveStringsMap(
                                    request.getHeaders());
                    TransferCompletionHandler.class.cast(h)
                            .transferAdapter(new GrizzlyTransferAdapter(map));
                }
            }
            final HttpRequestPacket requestPacketLocal = requestPacket;
            final Callable<Boolean> action = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    FilterChainContext sendingCtx = ctx;

                    // Check to see if the ProtocolNegotiator has given
                    // us a different FilterChain to use.
                    SSLConnectionContext sslCtx =
                            SSLUtils.getSslConnectionContext(ctx.getConnection());
                    if (sslCtx != null) {
                        FilterChain fc = sslCtx.getNewConnectionFilterChain();

                        if (fc != null) {
                            // Create a new FilterChain context using the new
                            // FilterChain.
                            // TODO:  We need to mark this connection somehow
                            //        as being only suitable for this type of
                            //        request.
                            sendingCtx = obtainProtocolChainContext(ctx, fc);
                        }
                    }
                    return sendRequest(sendingCtx, request, requestPacketLocal);
                }
            };
            if (secure) {
                ctx.notifyDownstream(new SSLSwitchingEvent(true, ctx.getConnection(), action));
                return false;
            }
            try {
                return action.call();
            } catch (Exception e) {
                httpCtx.abort(e);
                return true;
            }

        }

        private FilterChainContext obtainProtocolChainContext(
                final FilterChainContext ctx,
                final FilterChain completeProtocolFilterChain) {

            final FilterChainContext newFilterChainContext =
                    completeProtocolFilterChain.obtainFilterChainContext(
                            ctx.getConnection(),
                            ctx.getStartIdx() + 1,
                            completeProtocolFilterChain.size(),
                            ctx.getFilterIdx() + 1);

            newFilterChainContext.setAddressHolder(ctx.getAddressHolder());
            newFilterChainContext.setMessage(ctx.getMessage());
            newFilterChainContext.getInternalContext().setIoEvent(
                    ctx.getInternalContext().getIoEvent());
            ctx.getConnection().setProcessor(completeProtocolFilterChain);
            return newFilterChainContext;
        }

        private boolean isSecure(String scheme) {
            return "https".equals(scheme) || "wss".equals(scheme);
        }

        private void addHostHeader(final Request request,
                                   final URI uri,
                                   final HttpRequestPacket.Builder builder) {
            String host = request.getVirtualHost();
            if (host != null) {
                builder.header(Header.Host, host);
            } else {
                if (uri.getPort() == -1) {
                    builder.header(Header.Host, uri.getHost());
                } else {
                    builder.header(Header.Host, uri.getHost() + ':' + uri.getPort());
                }
            }
        }

        private boolean isUpgradeRequest(final AsyncHandler handler) {
            return (handler instanceof UpgradeHandler);
        }


        private boolean isWSRequest(final String requestUri) {
            return (requestUri.charAt(0) == 'w' && requestUri.charAt(1) == 's');
        }


        private void convertToUpgradeRequest(final HttpTransactionContext ctx) {
            final int colonIdx = ctx.requestUrl.indexOf(':');

            if (colonIdx < 2 || colonIdx > 3) {
                throw new IllegalArgumentException("Invalid websocket URL: " + ctx.requestUrl);
            }

            final StringBuilder sb = new StringBuilder(ctx.requestUrl);
            sb.replace(0, colonIdx, ((colonIdx == 2) ? "http" : "https"));
            ctx.wsRequestURI = ctx.requestUrl;
            ctx.requestUrl = sb.toString();
        }


       /* private ProxyServer getProxyServer(Request request) {

            ProxyServer proxyServer = request.getProxyServer();
            if (proxyServer == null) {
                proxyServer = config.getProxyServer();
            }
            return proxyServer;

        }*/


        private void addHeaders(final Request request,
                                final HttpRequestPacket requestPacket,
                                final ProxyServer proxy) throws IOException {

            final FluentCaseInsensitiveStringsMap map = request.getHeaders();
            if (isNonEmpty(map)) {
                for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
                    final String headerName = entry.getKey();
                    final List<String> headerValues = entry.getValue();
                    if (isNonEmpty(headerValues)) {
                        for (final String headerValue : headerValues) {
                            requestPacket.addHeader(headerName, headerValue);
                        }
                    }
                }
            }

            final MimeHeaders headers = requestPacket.getHeaders();
            if (!headers.contains(Header.Connection)) {
                //final boolean canCache = context.provider.clientConfig.getAllowPoolingConnection();
                requestPacket.addHeader(Header.Connection, /*(canCache ? */"keep-alive" /*: "close")*/);
            }

            if (!headers.contains(Header.Accept)) {
                requestPacket.addHeader(Header.Accept, "*/*");
            }

            if (!headers.contains(Header.UserAgent)) {
                requestPacket.addHeader(Header.UserAgent, config.getUserAgent());
            }

            boolean avoidProxy = ProxyUtils.avoidProxy(proxy, request);
            if (!avoidProxy) {
                if (!requestPacket.getHeaders().contains(Header.ProxyConnection)) {
                    requestPacket.setHeader(Header.ProxyConnection, "keep-alive");
                }

                if(null == requestPacket.getHeader(Header.ProxyAuthorization) )
                {
                 requestPacket.setHeader(Header.ProxyAuthorization, AuthenticatorUtils.computeBasicAuthentication(proxy));
                }

            }


        }


        private void addCookies(final Request request,
                                final HttpRequestPacket requestPacket) {

            final Collection<Cookie> cookies = request.getCookies();
            if (isNonEmpty(cookies)) {
                StringBuilder sb = new StringBuilder(128);
                org.glassfish.grizzly.http.Cookie[] gCookies =
                        new org.glassfish.grizzly.http.Cookie[cookies.size()];
                convertCookies(cookies, gCookies);
                CookieSerializerUtils.serializeClientCookies(sb, gCookies);
                requestPacket.addHeader(Header.Cookie, sb.toString());
            }

        }


        private void convertCookies(final Collection<Cookie> cookies,
                                    final org.glassfish.grizzly.http.Cookie[] gCookies) {
            int idx = 0;
            for (final Cookie cookie : cookies) {
                final org.glassfish.grizzly.http.Cookie gCookie =
                        new org.glassfish.grizzly.http.Cookie(cookie.getName(), cookie.getValue());
                gCookie.setDomain(cookie.getDomain());
                gCookie.setPath(cookie.getPath());
                gCookie.setVersion(cookie.getVersion());
                gCookie.setMaxAge(cookie.getMaxAge());
                gCookie.setSecure(cookie.isSecure());
                gCookies[idx] = gCookie;
                idx++;
            }

        }


        private void addQueryString(final Request request,
                                    final HttpRequestPacket requestPacket) {

            final FluentStringsMap map = request.getQueryParams();
            if (isNonEmpty(map)) {
                StringBuilder sb = new StringBuilder(128);
                for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
                    final String name = entry.getKey();
                    final List<String> values = entry.getValue();
                    if (isNonEmpty(values)) {
                        try {
                            for (int i = 0, len = values.size(); i < len; i++) {
                                final String value = values.get(i);
                                if (isNonEmpty(value)) {
                                    sb.append(URLEncoder.encode(name, "UTF-8")).append('=')
                                        .append(URLEncoder.encode(values.get(i), "UTF-8")).append('&');
                                } else {
                                    sb.append(URLEncoder.encode(name, "UTF-8")).append('&');
                                }
                            }
                        } catch (UnsupportedEncodingException ignored) {
                        }
                    }
                }
                sb.setLength(sb.length() - 1);
                String queryString = sb.toString();

                requestPacket.setQueryString(queryString);
            }

        }

    } // END AsyncHttpClientFiler


    private interface Cleanup {

        void cleanup(final FilterChainContext ctx);

    }


    private static final class AsyncHttpClientEventFilter extends HttpClientFilter implements Cleanup {


        private final EventHandler eventHandler;

        // -------------------------------------------------------- Constructors


        AsyncHttpClientEventFilter(final EventHandler eventHandler) {
            this(eventHandler, DEFAULT_MAX_HTTP_PACKET_HEADER_SIZE);
        }


        AsyncHttpClientEventFilter(final EventHandler eventHandler,
                                   final int maxHeaderSize) {

            super(maxHeaderSize);
            this.eventHandler = eventHandler;
        }


        @Override
        public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
            eventHandler.exceptionOccurred(ctx, error);
        }

        @Override
        protected void onHttpContentParsed(HttpContent content, FilterChainContext ctx) {
            eventHandler.onHttpContentParsed(content, ctx);
        }

        @Override
        protected void onHttpHeadersEncoded(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onHttpHeadersEncoded(httpHeader, ctx);
        }

        @Override
        protected void onHttpContentEncoded(HttpContent content, FilterChainContext ctx) {
            eventHandler.onHttpContentEncoded(content, ctx);
        }

        @Override
        protected void onInitialLineParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onInitialLineParsed(httpHeader, ctx);
        }

        @Override
        protected void onHttpHeaderError(HttpHeader httpHeader, FilterChainContext ctx, Throwable t) throws IOException {
            eventHandler.onHttpHeaderError(httpHeader, ctx, t);
        }

        @Override
        protected void onHttpHeadersParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onHttpHeadersParsed(httpHeader, ctx);
        }

        @Override
        protected boolean onHttpPacketParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            return eventHandler.onHttpPacketParsed(httpHeader, ctx);
        }

        @Override
        public void cleanup(final FilterChainContext ctx) {
            clearResponse(ctx.getConnection());
        }
    } // END AsyncHttpClientEventFilter


    private static final class AsyncSpdyClientEventFilter extends SpdyHandlerFilter implements Cleanup {


        private final EventHandler eventHandler;

        // -------------------------------------------------------- Constructors


        AsyncSpdyClientEventFilter(final EventHandler eventHandler,
                                   SpdyMode mode,
                                   ExecutorService threadPool) {
            super(mode, threadPool);
            this.eventHandler = eventHandler;
        }

        @Override
        public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
            eventHandler.exceptionOccurred(ctx, error);
        }

        @Override
        protected void onHttpContentParsed(HttpContent content, FilterChainContext ctx) {
            eventHandler.onHttpContentParsed(content, ctx);
        }

        @Override
        protected void onHttpHeadersEncoded(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onHttpHeadersEncoded(httpHeader, ctx);
        }

        @Override
        protected void onHttpContentEncoded(HttpContent content, FilterChainContext ctx) {
            eventHandler.onHttpContentEncoded(content, ctx);
        }

        @Override
        protected void onInitialLineParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onInitialLineParsed(httpHeader, ctx);
        }

        @Override
        protected void onHttpHeaderError(HttpHeader httpHeader, FilterChainContext ctx, Throwable t) throws IOException {
            eventHandler.onHttpHeaderError(httpHeader, ctx, t);
        }

        @Override
        protected void onHttpHeadersParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            eventHandler.onHttpHeadersParsed(httpHeader, ctx);
        }

        @Override
        protected boolean onHttpPacketParsed(HttpHeader httpHeader, FilterChainContext ctx) {
            return eventHandler.onHttpPacketParsed(httpHeader, ctx);
        }

        @Override
        public void cleanup(FilterChainContext ctx) {

        }

    } // END AsyncSpdyClientEventFilter


    private static final class EventHandler {

        private static final Map<Integer, StatusHandler> HANDLER_MAP =
                new HashMap<Integer, StatusHandler>();

        static {
            HANDLER_MAP.put(HttpStatus.UNAUTHORIZED_401.getStatusCode(),
                    AuthorizationHandler.INSTANCE);
            HANDLER_MAP.put(HttpStatus.PROXY_AUTHENTICATION_REQUIRED_407.getStatusCode(),
                    ProxyAuthorizationHandler.INSTANCE);
            HANDLER_MAP.put(HttpStatus.MOVED_PERMANENTLY_301.getStatusCode(),
                    RedirectHandler.INSTANCE);
            HANDLER_MAP.put(HttpStatus.FOUND_302.getStatusCode(),
                    RedirectHandler.INSTANCE);
            HANDLER_MAP.put(HttpStatus.TEMPORARY_REDIRECT_307.getStatusCode(),
                    RedirectHandler.INSTANCE);
        }


        private final GrizzlyAsyncHttpProvider provider;
        Cleanup cleanup;


        // -------------------------------------------------------- Constructors


        EventHandler(final GrizzlyAsyncHttpProvider provider) {
            this.provider = provider;
        }


        // ----------------------------------------------------- Event Callbacks


        public void exceptionOccurred(FilterChainContext ctx, Throwable error) {

            provider.getHttpTransactionContext(ctx.getConnection()).abort(error);

        }


        protected void onHttpContentParsed(HttpContent content,
                                           FilterChainContext ctx) {

            final HttpTransactionContext context =
                    provider.getHttpTransactionContext(ctx.getConnection());
            final AsyncHandler handler = context.handler;
            if (handler != null && context.currentState != AsyncHandler.STATE.ABORT) {
                try {
                    context.currentState = handler.onBodyPartReceived(
                            new GrizzlyResponseBodyPart(content,
                                    null,
                                    ctx.getConnection(),
                                    provider));
                } catch (Exception e) {
                    handler.onThrowable(e);
                }
            }

        }

        @SuppressWarnings("UnusedParameters")
        protected void onHttpHeadersEncoded(HttpHeader httpHeader, FilterChainContext ctx) {
            final HttpTransactionContext context = provider.getHttpTransactionContext(ctx.getConnection());
            final AsyncHandler handler = context.handler;
            if (handler != null) {
                if (TransferCompletionHandler.class.isAssignableFrom(handler.getClass())) {
                    ((TransferCompletionHandler) handler).onHeaderWriteCompleted();
                }
            }
        }

        protected void onHttpContentEncoded(HttpContent content, FilterChainContext ctx) {
            final HttpTransactionContext context = provider.getHttpTransactionContext(ctx.getConnection());
            final AsyncHandler handler = context.handler;
            if (handler != null) {
                if (TransferCompletionHandler.class.isAssignableFrom(handler.getClass())) {
                    final int written = content.getContent().remaining();
                    final long total = context.totalBodyWritten.addAndGet(written);
                    ((TransferCompletionHandler) handler).onContentWriteProgress(
                            written,
                            total,
                            content.getHttpHeader().getContentLength());
                }
            }
        }

        protected void onInitialLineParsed(HttpHeader httpHeader,
                                           FilterChainContext ctx) {

            //super.onInitialLineParsed(httpHeader, ctx);
            if (httpHeader.isSkipRemainder()) {
                return;
            }
            final Connection connection = ctx.getConnection();
            final HttpTransactionContext context =
                    provider.getHttpTransactionContext(connection);
            final int status = ((HttpResponsePacket) httpHeader).getStatus();
            if (context.establishingTunnel && HttpStatus.OK_200.statusMatches(status)) {
                return;
            }
            if (HttpStatus.CONINTUE_100.statusMatches(status)) {
                ctx.notifyUpstream(new ContinueEvent(context));
                return;
            }


            if (context.statusHandler != null && !context.statusHandler.handlesStatus(status)) {
                context.statusHandler = null;
                context.invocationStatus = StatusHandler.InvocationStatus.CONTINUE;
            } else {
                context.statusHandler = null;
            }
            if (context.invocationStatus == StatusHandler.InvocationStatus.CONTINUE) {
                if (HANDLER_MAP.containsKey(status)) {
                    context.statusHandler = HANDLER_MAP.get(status);
                }
                if (context.statusHandler instanceof RedirectHandler) {
                    if (!isRedirectAllowed(context)) {
                        context.statusHandler = null;
                    }
                }
            }
            if (isRedirectAllowed(context)) {
                if (isRedirect(status)) {
                    if (context.statusHandler == null) {
                        context.statusHandler = RedirectHandler.INSTANCE;
                    }
                    context.redirectCount.incrementAndGet();
                    if (redirectCountExceeded(context)) {
                        httpHeader.setSkipRemainder(true);
                        context.abort(new MaxRedirectException());
                    }
                } else {
                    if (context.redirectCount.get() > 0) {
                        context.redirectCount.set(0);
                    }
                }
            }
            final GrizzlyResponseStatus responseStatus =
                    new GrizzlyResponseStatus((HttpResponsePacket) httpHeader,
                            context.request.getURI(),
                            provider);
            context.responseStatus = responseStatus;
            if (context.statusHandler != null) {
                return;
            }
            if (context.currentState != AsyncHandler.STATE.ABORT) {

                try {
                    final AsyncHandler handler = context.handler;
                    if (handler != null) {
                        context.currentState = handler.onStatusReceived(responseStatus);
                        if (context.isWSRequest && context.currentState == AsyncHandler.STATE.ABORT) {
                            httpHeader.setSkipRemainder(true);
                            context.abort(new HandshakeException("Upgrade failed"));
                        }
                    }
                } catch (Exception e) {
                    httpHeader.setSkipRemainder(true);
                    context.abort(e);
                }
            }

        }


        protected void onHttpHeaderError(final HttpHeader httpHeader,
                                         final FilterChainContext ctx,
                                         final Throwable t) throws IOException {

            t.printStackTrace();
            httpHeader.setSkipRemainder(true);
            final HttpTransactionContext context =
                    provider.getHttpTransactionContext(ctx.getConnection());
            context.abort(t);
        }

        @SuppressWarnings({"unchecked"})
        protected void onHttpHeadersParsed(HttpHeader httpHeader,
                                           FilterChainContext ctx) {

            //super.onHttpHeadersParsed(httpHeader, ctx);
            LOGGER.debug("RESPONSE: {}", httpHeader);
            if (httpHeader.containsHeader(Header.Connection)) {
                if ("close".equals(httpHeader.getHeader(Header.Connection))) {
                    ConnectionManager.markConnectionAsDoNotCache(ctx.getConnection());
                }
            }
            final HttpTransactionContext context = provider.getHttpTransactionContext(ctx.getConnection());

            if (httpHeader.isSkipRemainder() || (context.establishingTunnel && context.statusHandler==null)) {
                return;
            }

            final AsyncHandler handler = context.handler;
            final List<ResponseFilter> filters = context.provider.clientConfig.getResponseFilters();
            final GrizzlyResponseHeaders responseHeaders = new GrizzlyResponseHeaders((HttpResponsePacket) httpHeader,
                                    null,
                                    provider);
            if (!filters.isEmpty()) {
                FilterContext fc = new FilterContext.FilterContextBuilder()
                        .asyncHandler(handler).request(context.request)
                        .responseHeaders(responseHeaders)
                        .responseStatus(context.responseStatus).build();
                try {
                    for (final ResponseFilter f : filters) {
                        fc = f.filter(fc);
                    }
                } catch (Exception e) {
                    context.abort(e);
                }
                if (fc.replayRequest()) {
                    httpHeader.setSkipRemainder(true);
                    final Request newRequest = fc.getRequest();
                    final AsyncHandler newHandler = fc.getAsyncHandler();
                    try {
                        final ConnectionManager m =
                                context.provider.connectionManager;
                        final Connection c =
                                m.obtainConnection(newRequest,
                                                   context.future);
                        final HttpTransactionContext newContext =
                                context.copy();
                        context.future = null;
                        provider.setHttpTransactionContext(c, newContext);
                        try {
                            context.provider.execute(c,
                                                     newRequest,
                                                     newHandler,
                                                     context.future);
                        } catch (IOException ioe) {
                            newContext.abort(ioe);
                        }
                    } catch (Exception e) {
                        context.abort(e);
                    }
                    return;
                }
            }
            if (context.statusHandler != null && context.invocationStatus == StatusHandler.InvocationStatus.CONTINUE) {
                final boolean result = context.statusHandler.handleStatus(((HttpResponsePacket) httpHeader),
                                                                          context,
                                                                          ctx);
                if (!result) {
                    httpHeader.setSkipRemainder(true);
                    return;
                }
            }
            if (context.isWSRequest) {
                try {
                    //in case of DIGEST auth protocol handler is null and just returning here is working
                    if(context.protocolHandler == null)
                    {
                        return;
                        //context.protocolHandler = Version.DRAFT17.createHandler(true);
                        //context.currentState = AsyncHandler.STATE.UPGRADE;
                    }

                    context.protocolHandler.setConnection(ctx.getConnection());

                    final GrizzlyWebSocketAdapter webSocketAdapter = createWebSocketAdapter(context);
                    context.webSocket = webSocketAdapter;
                    SimpleWebSocket ws = webSocketAdapter.gWebSocket;
                    if (context.currentState == AsyncHandler.STATE.UPGRADE) {
                        httpHeader.setChunked(false);
                        ws.onConnect();
                        WebSocketHolder.set(ctx.getConnection(),
                                            context.protocolHandler,
                                            ws);
                        ((WebSocketUpgradeHandler) context.handler).onSuccess(context.webSocket);
                        final int wsTimeout = context.provider.clientConfig.getWebSocketIdleTimeoutInMs();
                        IdleTimeoutFilter.setCustomTimeout(ctx.getConnection(),
                                ((wsTimeout <= 0)
                                        ? IdleTimeoutFilter.FOREVER
                                        : wsTimeout),
                                TimeUnit.MILLISECONDS);
                        context.result(handler.onCompleted());
                    } else {
                        httpHeader.setSkipRemainder(true);
                        ((WebSocketUpgradeHandler) context.handler).
                                onClose(context.webSocket,
                                        1002,
                                        "WebSocket protocol error: unexpected HTTP response status during handshake.");
                        context.result(null);
                    }
                } catch (Exception e) {
                    httpHeader.setSkipRemainder(true);
                    context.abort(e);
                }
            } else {
                if (context.currentState != AsyncHandler.STATE.ABORT) {
                    try {
                        context.currentState = handler.onHeadersReceived(
                                responseHeaders);
                    } catch (Exception e) {
                        httpHeader.setSkipRemainder(true);
                        context.abort(e);
                    }
                }
            }

        }

        @SuppressWarnings("unchecked")
        protected boolean onHttpPacketParsed(HttpHeader httpHeader, FilterChainContext ctx) {

            boolean result;
            final String proxy_auth = httpHeader.getHeader(Header.ProxyAuthenticate);

            if (httpHeader.isSkipRemainder()) {
                if (!ProxyAuthorizationHandler.isNTLMSecondHandShake(proxy_auth)) {
                    cleanup.cleanup(ctx);
                    cleanup(ctx, provider);
                    return false;
                } else {
                    //super.onHttpPacketParsed(httpHeader, ctx);
                    cleanup.cleanup(ctx);
                    httpHeader.getProcessingState().setKeepAlive(true);
                    return false;
                }
            }

            //result = super.onHttpPacketParsed(httpHeader, ctx);
            if (cleanup != null) {
                cleanup.cleanup(ctx);
            }
            result = false;

            final HttpTransactionContext context = provider.getHttpTransactionContext(ctx.getConnection());
            if (context.establishingTunnel
                    && HttpStatus.OK_200.statusMatches(
                        ((HttpResponsePacket) httpHeader).getStatus())) {
                context.establishingTunnel = false;
                final Connection c = ctx.getConnection();
                context.tunnelEstablished(c);
                try {
                    context.provider.execute(c,
                            context.request,
                            context.handler,
                            context.future);
                    return result;
                } catch (IOException e) {
                    context.abort(e);
                    return result;
                }
            } else {
                cleanup(ctx, provider);
                final AsyncHandler handler = context.handler;
                if (handler != null) {
                    try {
                        context.result(handler.onCompleted());
                    } catch (Exception e) {
                        context.abort(e);
                    }
                } else {
                    context.done(null);
                }

                return result;
            }
        }


        // ----------------------------------------------------- Private Methods

        private static GrizzlyWebSocketAdapter createWebSocketAdapter(final HttpTransactionContext context) {
            SimpleWebSocket ws = new SimpleWebSocket(context.protocolHandler);
            AsyncHttpProviderConfig config = context.provider.clientConfig.getAsyncHttpProviderConfig();
            boolean bufferFragments = true;
            if (config instanceof GrizzlyAsyncHttpProviderConfig) {
                bufferFragments = (Boolean) ((GrizzlyAsyncHttpProviderConfig) config).getProperty(Property.BUFFER_WEBSOCKET_FRAGMENTS);
            }

            return new GrizzlyWebSocketAdapter(ws, bufferFragments);
        }

        private static boolean isRedirectAllowed(final HttpTransactionContext ctx) {
            boolean allowed = ctx.request.isRedirectEnabled();
            if (ctx.request.isRedirectOverrideSet()) {
                return allowed;
            }
            if (!allowed) {
                allowed = ctx.redirectsAllowed;
            }
            return allowed;
        }

        private static HttpTransactionContext cleanup(final FilterChainContext ctx,
                                                      final GrizzlyAsyncHttpProvider provider) {

            final Connection c = ctx.getConnection();
            final HttpTransactionContext context =
                    provider.getHttpTransactionContext(c);
            context.provider.setHttpTransactionContext(c, null);
            if (!context.provider.connectionManager.canReturnConnection(c)) {
                context.abort(new IOException("Maximum pooled connections exceeded"));
            } else {
                if (!context.provider.connectionManager.returnConnection(context.request, c)) {
                    ctx.getConnection().close();
                }
            }

            return context;

        }


        private static boolean redirectCountExceeded(final HttpTransactionContext context) {

            return (context.redirectCount.get() > context.maxRedirectCount);

        }


        private static boolean isRedirect(final int status) {

            return HttpStatus.MOVED_PERMANENTLY_301.statusMatches(status)
                    || HttpStatus.FOUND_302.statusMatches(status)
                    || HttpStatus.SEE_OTHER_303.statusMatches(status)
                    || HttpStatus.TEMPORARY_REDIRECT_307.statusMatches(status);

        }


        // ------------------------------------------------------- Inner Classes


        private static final class AuthorizationHandler implements StatusHandler {

            private static final AuthorizationHandler INSTANCE =
                    new AuthorizationHandler();

            // -------------------------------------- Methods from StatusHandler


            public boolean handlesStatus(int statusCode) {
                return (HttpStatus.UNAUTHORIZED_401.statusMatches(statusCode));
            }

            @SuppressWarnings({"unchecked"})
            public boolean handleStatus(final HttpResponsePacket responsePacket,
                                     final HttpTransactionContext httpTransactionContext,
                                     final FilterChainContext ctx) {

                final String auth = responsePacket.getHeader(Header.WWWAuthenticate);
                if (auth == null) {
                    throw new IllegalStateException("401 response received, but no WWW-Authenticate header was present");
                }

                Realm realm = httpTransactionContext.request.getRealm();
                if (realm == null) {
                    realm = httpTransactionContext.provider.clientConfig.getRealm();
                }
                if (realm == null) {
                    httpTransactionContext.invocationStatus = InvocationStatus.STOP;
                    return true;
                }

                responsePacket.setSkipRemainder(true); // ignore the remainder of the response

                final Request req = httpTransactionContext.request;
                realm = new Realm.RealmBuilder().clone(realm)
                                .setScheme(realm.getAuthScheme())
                                .setUri(httpTransactionContext.request.getURI().getPath())
                                .setMethodName(req.getMethod())
                                .setUsePreemptiveAuth(true)
                                .parseWWWAuthenticateHeader(auth)
                                .build();
                if (auth.toLowerCase().startsWith("basic")) {
                    req.getHeaders().remove(Header.Authorization.toString());
                    try {
                        req.getHeaders().add(Header.Authorization.toString(),
                                             AuthenticatorUtils.computeBasicAuthentication(realm));
                    } catch (UnsupportedEncodingException ignored) {
                    }
                } else if (auth.toLowerCase().startsWith("digest")) {
                    req.getHeaders().remove(Header.Authorization.toString());
                    try {
                        req.getHeaders().add(Header.Authorization.toString(),
                                             AuthenticatorUtils.computeDigestAuthentication(realm));
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalStateException("Digest authentication not supported", e);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("Unsupported encoding.", e);
                    }
                } else {
                    throw new IllegalStateException("Unsupported authorization method: " + auth);
                }

                final ConnectionManager m = httpTransactionContext.provider.connectionManager;
                try {
                    final Connection c = m.obtainConnection(req,
                                                            httpTransactionContext.future);
                    final HttpTransactionContext newContext =
                            httpTransactionContext.copy();
                    httpTransactionContext.future = null;
                    httpTransactionContext.provider.setHttpTransactionContext(c, newContext);
                    newContext.invocationStatus = InvocationStatus.STOP;
                    try {
                        httpTransactionContext.provider.execute(c,
                                                                req,
                                                                httpTransactionContext.handler,
                                                                httpTransactionContext.future);
                        return false;
                    } catch (IOException ioe) {
                        newContext.abort(ioe);
                        return false;
                    }
                } catch (Exception e) {
                    httpTransactionContext.abort(e);
                }
                httpTransactionContext.invocationStatus = InvocationStatus.STOP;
                return false;
            }

        } // END AuthorizationHandler

        private static final class ProxyAuthorizationHandler implements StatusHandler {

            private static final ProxyAuthorizationHandler INSTANCE =
                    new ProxyAuthorizationHandler();

            // -------------------------------------- Methods from StatusHandler


            public boolean handlesStatus(int statusCode) {
                return (HttpStatus.PROXY_AUTHENTICATION_REQUIRED_407.statusMatches(statusCode));
            }

            @SuppressWarnings({"unchecked"})
            public boolean handleStatus(final HttpResponsePacket responsePacket,
                                     final HttpTransactionContext httpTransactionContext,
                                     final FilterChainContext ctx) {

                final String proxy_auth = responsePacket.getHeader(Header.ProxyAuthenticate);
                if (proxy_auth == null) {
                    throw new IllegalStateException("407 response received, but no Proxy Authenticate header was present");
                }

                final Request req = httpTransactionContext.request;
                ProxyServer proxyServer = httpTransactionContext.provider.clientConfig.getProxyServer();
                String principal = proxyServer.getPrincipal();
                String password = proxyServer.getPassword();
                Realm realm = new Realm.RealmBuilder().setPrincipal(principal)
                                .setPassword(password)
                                .setUri("/")
                                .setMethodName("CONNECT")
                                .setUsePreemptiveAuth(true)
                                .parseProxyAuthenticateHeader(proxy_auth)
                                .build();
                if (proxy_auth.toLowerCase().startsWith("basic")) {
                    req.getHeaders().remove(Header.ProxyAuthenticate.toString());
                    req.getHeaders().remove(Header.ProxyAuthorization.toString());
                    try {
                        req.getHeaders().add(Header.ProxyAuthorization.toString(),
                                             AuthenticatorUtils.computeBasicAuthentication(realm));
                    } catch (UnsupportedEncodingException ignored) {
                    }
                } else if (proxy_auth.toLowerCase().startsWith("digest")) {
                    req.getHeaders().remove(Header.ProxyAuthenticate.toString());
                    req.getHeaders().remove(Header.ProxyAuthorization.toString());
                    try {
                        req.getHeaders().add(Header.ProxyAuthorization.toString(),
                                             AuthenticatorUtils.computeDigestAuthentication(realm));
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalStateException("Digest authentication not supported", e);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException("Unsupported encoding.", e);
                    }
                }else if (proxy_auth.toLowerCase().startsWith("ntlm")) {

                    req.getHeaders().remove(Header.ProxyAuthenticate.toString());
                    req.getHeaders().remove(Header.ProxyAuthorization.toString());

                    String msg;
                    try {

                        if(isNTLMFirstHandShake(proxy_auth))
                        {
                            msg = ntlmEngine.generateType1Msg(proxyServer.getNtlmDomain(), "");
                        }else {
                            String serverChallenge = proxy_auth.trim().substring("NTLM ".length());
                            msg = ntlmEngine.generateType3Msg(principal, password, proxyServer.getNtlmDomain(), proxyServer.getHost(), serverChallenge);
                        }

                        req.getHeaders().add(Header.ProxyAuthorization.toString(), "NTLM " + msg);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }   else if (proxy_auth.toLowerCase().startsWith("negotiate")){
                    //this is for kerberos
                    req.getHeaders().remove(Header.ProxyAuthenticate.toString());
                    req.getHeaders().remove(Header.ProxyAuthorization.toString());

                }else {
                    throw new IllegalStateException("Unsupported authorization method: " + proxy_auth);
                }

                final ConnectionManager m = httpTransactionContext.provider.connectionManager;
                InvocationStatus tempInvocationStatus = InvocationStatus.STOP;

                try {

                    if(isNTLMFirstHandShake(proxy_auth))
                    {
                        tempInvocationStatus = InvocationStatus.CONTINUE;

                    }

                    if(proxy_auth.toLowerCase().startsWith("negotiate"))
                    {
                        final Connection c = m.obtainConnection(req, httpTransactionContext.future);
                        final HttpTransactionContext newContext = httpTransactionContext.copy();
                        httpTransactionContext.future = null;
                        httpTransactionContext.provider.setHttpTransactionContext(c, newContext);

                        newContext.invocationStatus = tempInvocationStatus;

                        String challengeHeader;
                        String server = proxyServer.getHost();

                        challengeHeader = GSSSPNEGOWrapper.generateToken(server);

                        req.getHeaders().add(Header.ProxyAuthorization.toString(), "Negotiate " + challengeHeader);


                        return exceuteRequest(httpTransactionContext, req, c,
                                newContext);
                    }else if(isNTLMSecondHandShake(proxy_auth))
                    {
                        final Connection c = ctx.getConnection();
                        final HttpTransactionContext newContext = httpTransactionContext.copy();

                        httpTransactionContext.future = null;
                        httpTransactionContext.provider.setHttpTransactionContext(c, newContext);

                        newContext.invocationStatus = tempInvocationStatus;
                        httpTransactionContext.establishingTunnel = true;

                        return exceuteRequest(httpTransactionContext, req, c,
                                newContext);

                    }
                    else{
                        final Connection c = m.obtainConnection(req, httpTransactionContext.future);
                        final HttpTransactionContext newContext = httpTransactionContext.copy();
                        httpTransactionContext.future = null;
                        httpTransactionContext.provider.setHttpTransactionContext(c, newContext);

                        newContext.invocationStatus = tempInvocationStatus;

                        //NTLM needs the same connection to be used for exchange of tokens
                        return exceuteRequest(httpTransactionContext, req, c,
                                newContext);
                    }
                } catch (Exception e) {
                    httpTransactionContext.abort(e);
                }
                httpTransactionContext.invocationStatus = tempInvocationStatus;
                return false;
            }

            private boolean exceuteRequest(
                    final HttpTransactionContext httpTransactionContext,
                    final Request req, final Connection c,
                    final HttpTransactionContext newContext) {
                try {
                    httpTransactionContext.provider.execute(c,
                                                            req,
                                                            httpTransactionContext.handler,
                                                            httpTransactionContext.future);
                    return false;
                } catch (IOException ioe) {
                    newContext.abort(ioe);
                    return false;
                }
            }

            public static boolean isNTLMSecondHandShake(final String proxy_auth) {
                return (proxy_auth != null && proxy_auth.toLowerCase().startsWith("ntlm") && !proxy_auth.equalsIgnoreCase("ntlm"));
            }
            public static boolean isNTLMFirstHandShake(final String proxy_auth) {
                return (proxy_auth.equalsIgnoreCase("ntlm"));
            }


        } // END AuthorizationHandler


        private static final class RedirectHandler implements StatusHandler {

            private static final RedirectHandler INSTANCE = new RedirectHandler();


            // ------------------------------------------ Methods from StatusHandler


            public boolean handlesStatus(int statusCode) {
                return (isRedirect(statusCode));
            }

            @SuppressWarnings({"unchecked"})
            public boolean handleStatus(final HttpResponsePacket responsePacket,
                                        final HttpTransactionContext httpTransactionContext,
                                        final FilterChainContext ctx) {

                final String redirectURL = responsePacket.getHeader(Header.Location);
                if (redirectURL == null) {
                    throw new IllegalStateException("redirect received, but no location header was present");
                }

                URI orig;
                if (httpTransactionContext.lastRedirectURI == null) {
                    orig = httpTransactionContext.request.getURI();
                } else {
                    orig = AsyncHttpProviderUtils.getRedirectUri(httpTransactionContext.request.getURI(),
                                                                 httpTransactionContext.lastRedirectURI);
                }
                httpTransactionContext.lastRedirectURI = redirectURL;
                Request requestToSend;
                URI uri = AsyncHttpProviderUtils.getRedirectUri(orig, redirectURL);
                if (!uri.toString().equalsIgnoreCase(orig.toString())) {
                    requestToSend = newRequest(uri,
                                               responsePacket,
                                               httpTransactionContext,
                                               sendAsGet(responsePacket,  httpTransactionContext));
                } else {
                    httpTransactionContext.statusHandler = null;
                    httpTransactionContext.invocationStatus = InvocationStatus.CONTINUE;
                        try {
                            httpTransactionContext.handler.onStatusReceived(httpTransactionContext.responseStatus);
                        } catch (Exception e) {
                            httpTransactionContext.abort(e);
                        }
                    return true;
                }

                final ConnectionManager m = httpTransactionContext.provider.connectionManager;
                try {
                    final Connection c = m.obtainConnection(requestToSend,
                                                            httpTransactionContext.future);
                    final HttpTransactionContext newContext =
                            httpTransactionContext.copy();
                    httpTransactionContext.future = null;
                    newContext.invocationStatus = InvocationStatus.CONTINUE;
                    newContext.request = requestToSend;
                    newContext.requestUrl = requestToSend.getUrl();
                    httpTransactionContext.provider.setHttpTransactionContext(c, newContext);
                    httpTransactionContext.provider.execute(c,
                                                            requestToSend,
                                                            newContext.handler,
                                                            newContext.future);
                    return false;
                } catch (Exception e) {
                    httpTransactionContext.abort(e);
                }

                httpTransactionContext.invocationStatus = InvocationStatus.CONTINUE;
                return true;

            }


            // ------------------------------------------------- Private Methods

            private boolean sendAsGet(final HttpResponsePacket response,
                                      final HttpTransactionContext ctx) {
                final int statusCode = response.getStatus();
                return !(statusCode < 302 || statusCode > 303)
                          && !(statusCode == 302
                             && ctx.provider.clientConfig.isStrict302Handling());
            }

        } // END RedirectHandler


        // ----------------------------------------------------- Private Methods


        private static Request newRequest(final URI uri,
                                          final HttpResponsePacket response,
                                          final HttpTransactionContext ctx,
                                          boolean asGet) {

            final RequestBuilder builder = new RequestBuilder(ctx.request);
            if (asGet) {
                builder.setMethod("GET");
            }
            builder.setUrl(uri.toString());

            if (ctx.provider.clientConfig.isRemoveQueryParamOnRedirect()) {
                builder.setQueryParameters(null);
            }
            for (String cookieStr : response.getHeaders().values(Header.Cookie)) {
                for (Cookie c : CookieDecoder.decode(cookieStr)) {
                    builder.addOrReplaceCookie(c);
                }
            }
            return builder.build();

        }


    } // END AsyncHttpClientEventFilter


    private static final class ClientEncodingFilter implements EncodingFilter {


        // ----------------------------------------- Methods from EncodingFilter


        public boolean applyEncoding(HttpHeader httpPacket) {

           httpPacket.addHeader(Header.AcceptEncoding, "gzip");
           return true;

        }


        public boolean applyDecoding(HttpHeader httpPacket) {

            final HttpResponsePacket httpResponse = (HttpResponsePacket) httpPacket;
            final DataChunk bc = httpResponse.getHeaders().getValue(Header.ContentEncoding);
            return bc != null && bc.indexOf("gzip", 0) != -1;

        }


    } // END ClientContentEncoding


    private static interface BodyHandler {

        static int MAX_CHUNK_SIZE = 8192;

        boolean handlesBodyType(final Request request);

        boolean doHandle(final FilterChainContext ctx,
                      final Request request,
                      final HttpRequestPacket requestPacket) throws IOException;

    } // END BodyHandler


    private final class BodyHandlerFactory {

        private final BodyHandler[] HANDLERS = new BodyHandler[] {
            new StringBodyHandler(),
            new ByteArrayBodyHandler(),
            new ParamsBodyHandler(),
            new EntityWriterBodyHandler(),
            new StreamDataBodyHandler(),
            new PartsBodyHandler(),
            new FileBodyHandler(),
            new BodyGeneratorBodyHandler()
        };

        public BodyHandler getBodyHandler(final Request request) {
            for (final BodyHandler h : HANDLERS) {
                if (h.handlesBodyType(request)) {
                    return h;
                }
            }
            return new NoBodyHandler();
        }

    } // END BodyHandlerFactory


    private static final class ExpectHandler implements BodyHandler {

        private final BodyHandler delegate;
        private Request request;
        private HttpRequestPacket requestPacket;

        // -------------------------------------------------------- Constructors


        private ExpectHandler(final BodyHandler delegate) {

            this.delegate = delegate;

        }


        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(Request request) {
            return delegate.handlesBodyType(request);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(FilterChainContext ctx, Request request, HttpRequestPacket requestPacket) throws IOException {
            this.request = request;
            this.requestPacket = requestPacket;
            ctx.write(requestPacket, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            return true;
        }

        public void finish(final FilterChainContext ctx) throws IOException {
            delegate.doHandle(ctx, request, requestPacket);
        }

    } // END ContinueHandler


    private final class ByteArrayBodyHandler implements BodyHandler {


        // -------------------------------------------- Methods from BodyHandler

        public boolean handlesBodyType(final Request request) {
            return (request.getByteData() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            String charset = request.getBodyEncoding();
            if (charset == null) {
                charset = Charsets.ASCII_CHARSET.name();
            }
            final byte[] data = new String(request.getByteData(), charset).getBytes(charset);
            final MemoryManager mm = ctx.getMemoryManager();
            final Buffer gBuffer = Buffers.wrap(mm, data);
            if (requestPacket.getContentLength() == -1) {
                    if (!clientConfig.isCompressionEnabled()) {
                        requestPacket.setContentLengthLong(data.length);
                    }
                }
            final HttpContent content = requestPacket.httpContentBuilder().content(gBuffer).build();
            content.setLast(true);
            ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            return true;
        }
    }


    private final class StringBodyHandler implements BodyHandler {


        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return (request.getStringData() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            String charset = request.getBodyEncoding();
            if (charset == null) {
                charset = Charsets.ASCII_CHARSET.name();
            }
            final byte[] data = request.getStringData().getBytes(charset);
            final MemoryManager mm = ctx.getMemoryManager();
            final Buffer gBuffer = Buffers.wrap(mm, data);
            if (requestPacket.getContentLength() == -1) {
                if (!clientConfig.isCompressionEnabled()) {
                    requestPacket.setContentLengthLong(data.length);
                }
            }
            final HttpContent content = requestPacket.httpContentBuilder().content(gBuffer).build();
            content.setLast(true);
            ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            return true;
        }

    } // END StringBodyHandler


    private static final class NoBodyHandler implements BodyHandler {


        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return false;
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            final HttpContent content = requestPacket.httpContentBuilder().content(Buffers.EMPTY_BUFFER).build();
            content.setLast(true);
            ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            return true;
        }

    } // END NoBodyHandler


    private final class ParamsBodyHandler implements BodyHandler {


        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            final FluentStringsMap params = request.getParams();
            return isNonEmpty(params);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            if (requestPacket.getContentType() == null) {
                requestPacket.setContentType("application/x-www-form-urlencoded");
            }
            StringBuilder sb = null;
            String charset = request.getBodyEncoding();
            if (charset == null) {
                charset = Charsets.ASCII_CHARSET.name();
            }
            final FluentStringsMap params = request.getParams();
            if (!params.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                    String name = entry.getKey();
                    List<String> values = entry.getValue();
                    if (isNonEmpty(values)) {
                        if (sb == null) {
                            sb = new StringBuilder(128);
                        }
                        for (String value : values) {
                            if (sb.length() > 0) {
                                sb.append('&');
                            }
                            sb.append(URLEncoder.encode(name, charset))
                                    .append('=').append(URLEncoder.encode(value, charset));
                        }
                    }
                }
            }
            if (sb != null) {
                final byte[] data = sb.toString().getBytes(charset);
                final MemoryManager mm = ctx.getMemoryManager();
                final Buffer gBuffer = Buffers.wrap(mm, data);
                final HttpContent content = requestPacket.httpContentBuilder().content(gBuffer).build();
                if (requestPacket.getContentLength() == -1) {
                    if (!clientConfig.isCompressionEnabled()) {
                        requestPacket.setContentLengthLong(data.length);
                    }
                }
                content.setLast(true);
                ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            }
            return true;
        }

    } // END ParamsBodyHandler


    private static final class EntityWriterBodyHandler implements BodyHandler {

        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return (request.getEntityWriter() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            final MemoryManager mm = ctx.getMemoryManager();
            Buffer b = mm.allocate(512);
            BufferOutputStream o = new BufferOutputStream(mm, b, true);
            final Request.EntityWriter writer = request.getEntityWriter();
            writer.writeEntity(o);
            b = o.getBuffer();
            b.trim();
            if (b.hasRemaining()) {
                final HttpContent content = requestPacket.httpContentBuilder().content(b).build();
                content.setLast(true);
                ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            }

            return true;
        }

    } // END EntityWriterBodyHandler


    private static final class StreamDataBodyHandler implements BodyHandler {

        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return (request.getStreamData() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            final MemoryManager mm = ctx.getMemoryManager();
            Buffer buffer = mm.allocate(512);
            final byte[] b = new byte[512];
            int read;
            final InputStream in = request.getStreamData();
            try {
                in.reset();
            } catch (IOException ioe) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ioe.toString(), ioe);
                }
            }
            if (in.markSupported()) {
                in.mark(0);
            }

            while ((read = in.read(b)) != -1) {
                if (read > buffer.remaining()) {
                    buffer = mm.reallocate(buffer, buffer.capacity() + 512);
                }
                buffer.put(b, 0, read);
            }
            buffer.trim();
            if (buffer.hasRemaining()) {
                final HttpContent content = requestPacket.httpContentBuilder().content(buffer).build();
                buffer.allowBufferDispose(false);
                content.setLast(true);
                ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            }

            return true;
        }

    } // END StreamDataBodyHandler


    private static final class PartsBodyHandler implements BodyHandler {

        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return isNonEmpty(request.getParts());
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            MultipartRequestEntity mre =
                    AsyncHttpProviderUtils.createMultipartRequestEntity(
                            request.getParts(),
                            request.getHeaders());
            requestPacket.setContentLengthLong(mre.getContentLength());
            requestPacket.setContentType(mre.getContentType());
            final MemoryManager mm = ctx.getMemoryManager();
            Buffer b = mm.allocate(512);
            BufferOutputStream o = new BufferOutputStream(mm, b, true);
            mre.writeRequest(o);
            b = o.getBuffer();
            b.trim();
            if (b.hasRemaining()) {
                final HttpContent content = requestPacket.httpContentBuilder().content(b).build();
                content.setLast(true);
                ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            }

            return true;
        }

    } // END PartsBodyHandler


    private final class FileBodyHandler implements BodyHandler {

        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return (request.getFile() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                                final Request request,
                                final HttpRequestPacket requestPacket)
        throws IOException {

            final File f = request.getFile();
            requestPacket.setContentLengthLong(f.length());
            final HttpTransactionContext context = getHttpTransactionContext(ctx.getConnection());
            if (!SEND_FILE_SUPPORT || requestPacket.isSecure()) {
                final FileInputStream fis = new FileInputStream(request.getFile());
                final MemoryManager mm = ctx.getMemoryManager();
                AtomicInteger written = new AtomicInteger();
                boolean last = false;
                try {
                    for (byte[] buf = new byte[MAX_CHUNK_SIZE]; !last; ) {
                        Buffer b = null;
                        int read;
                        if ((read = fis.read(buf)) < 0) {
                            last = true;
                            b = Buffers.EMPTY_BUFFER;
                        }
                        if (b != Buffers.EMPTY_BUFFER) {
                            written.addAndGet(read);
                            b = Buffers.wrap(mm, buf, 0, read);
                        }

                        final HttpContent content =
                                requestPacket.httpContentBuilder().content(b).
                                        last(last).build();
                        ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
                    }
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
                }
            } else {
                // write the headers
                ctx.write(requestPacket, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
                ctx.write(new FileTransfer(f), new EmptyCompletionHandler<WriteResult>() {

                    @Override
                    public void updated(WriteResult result) {
                        final AsyncHandler handler = context.handler;
                        if (handler != null) {
                            if (TransferCompletionHandler.class.isAssignableFrom(handler.getClass())) {
                                // WriteResult keeps a track of the total amount written,
                                // so we need to calculate the delta ourselves.
                                final long resultTotal = result.getWrittenSize();
                                final long written = resultTotal - context.totalBodyWritten.get();
                                final long total = context.totalBodyWritten.addAndGet(written);
                                ((TransferCompletionHandler) handler).onContentWriteProgress(
                                        written,
                                        total,
                                        requestPacket.getContentLength());
                            }
                        }
                    }
                });
            }

            return true;
        }

    } // END FileBodyHandler


    private static final class BodyGeneratorBodyHandler implements BodyHandler {

        // -------------------------------------------- Methods from BodyHandler


        public boolean handlesBodyType(final Request request) {
            return (request.getBodyGenerator() != null);
        }

        @SuppressWarnings({"unchecked"})
        public boolean doHandle(final FilterChainContext ctx,
                             final Request request,
                             final HttpRequestPacket requestPacket)
        throws IOException {

            final BodyGenerator generator = request.getBodyGenerator();
            final Body bodyLocal = generator.createBody();
            final long len = bodyLocal.getContentLength();
            if (len > 0) {
                requestPacket.setContentLengthLong(len);
            } else {
                requestPacket.setChunked(true);
            }

            final MemoryManager mm = ctx.getMemoryManager();
            boolean last = false;

            while (!last) {
                Buffer buffer = mm.allocate(MAX_CHUNK_SIZE);
                buffer.allowBufferDispose(true);

                final long readBytes = bodyLocal.read(buffer.toByteBuffer());
                if (readBytes > 0) {
                    buffer.position((int) readBytes);
                    buffer.trim();
                } else {
                    buffer.dispose();

                    if (readBytes < 0) {
                        last = true;
                        buffer = Buffers.EMPTY_BUFFER;
                    } else {
                        // pass the context to bodyLocal to be able to
                        // continue body transferring once more data is available
                        if (generator instanceof FeedableBodyGenerator) {
                            ((FeedableBodyGenerator) generator).initializeAsynchronousTransfer(ctx, requestPacket);
                            return false;
                        } else {
                            throw new IllegalStateException("BodyGenerator unexpectedly returned 0 bytes available");
                        }
                    }
                }

                final HttpContent content =
                        requestPacket.httpContentBuilder().content(buffer).
                                last(last).build();
                ctx.write(content, ((!requestPacket.isCommitted()) ? ctx.getTransportContext().getCompletionHandler() : null));
            }

            return true;
        }

    } // END BodyGeneratorBodyHandler


    static final class SwitchingSSLFilter extends SSLFilter {

        private final boolean secureByDefault;
        final Attribute<Boolean> CONNECTION_IS_SECURE =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(SwitchingSSLFilter.class.getName());

        // -------------------------------------------------------- Constructors


        SwitchingSSLFilter(final SSLEngineConfigurator clientConfig,
                           final boolean secureByDefault) {

            super(null, clientConfig);
            this.secureByDefault = secureByDefault;

        }


        // ---------------------------------------------- Methods from SSLFilter


        @Override
        protected void notifyHandshakeFailed(Connection connection, Throwable t) {
            throw new RuntimeException(t);
        }

        @Override
        public NextAction handleEvent(final FilterChainContext ctx, FilterChainEvent event) throws IOException {

            if (event.type() == SSLSwitchingEvent.class) {
                final SSLSwitchingEvent se = (SSLSwitchingEvent) event;

                if (se.secure) {
                    if (se.action != null) {
                        ProtocolHandshakeListener.addListener(
                                ctx.getConnection(),
                                new HandshakeCompleteListener() {
                                    @Override
                                    public void complete() {
                                        try {
                                            se.action.call();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                        );
                    }
                    handshake(ctx.getConnection(), null);
                }
                setSecureStatus(se.connection, se.secure);
                return ctx.getStopAction();
            }
            return ctx.getInvokeAction();

        }

        @Override
        public NextAction handleRead(FilterChainContext ctx) throws IOException {

            if (isSecure(ctx.getConnection())) {
                return super.handleRead(ctx);
            }
            return ctx.getInvokeAction();

        }

        @Override
        public NextAction handleWrite(FilterChainContext ctx) throws IOException {

            if (isSecure(ctx.getConnection())) {
                return super.handleWrite(ctx);
            }
            return ctx.getInvokeAction();

        }

        @Override
        public void onFilterChainChanged(FilterChain filterChain) {
            // no-op
        }

        // ----------------------------------------------------- Private Methods


        private boolean isSecure(final Connection c) {

            Boolean secStatus = CONNECTION_IS_SECURE.get(c);
            if (secStatus == null) {
                secStatus = secureByDefault;
            }
            return secStatus;

        }

        private void setSecureStatus(final Connection c, final boolean secure) {
            CONNECTION_IS_SECURE.set(c, secure);
        }


        // ------------------------------------------------------ Nested Classes

        static final class SSLSwitchingEvent implements FilterChainEvent {

            final boolean secure;
            final Connection connection;
            final Callable<Boolean> action;

            // ---------------------------------------------------- Constructors


            SSLSwitchingEvent(final boolean secure,
                              final Connection c,
                              final Callable<Boolean> action) {

                this.secure = secure;
                connection = c;
                this.action = action;

            }

            // ----------------------------------- Methods from FilterChainEvent


            @Override
            public Object type() {
                return SSLSwitchingEvent.class;
            }

        } // END SSLSwitchingEvent

    } // END SwitchingSSLFilter

    private static final class GrizzlyTransferAdapter extends TransferCompletionHandler.TransferAdapter {


        // -------------------------------------------------------- Constructors


        public GrizzlyTransferAdapter(FluentCaseInsensitiveStringsMap headers) throws IOException {
            super(headers);
        }


        // ---------------------------------------- Methods from TransferAdapter


        @Override
        public void getBytes(byte[] bytes) {
            // TODO implement
        }

    } // END GrizzlyTransferAdapter


    private static final class GrizzlyWebSocketAdapter implements WebSocket {

        private final SimpleWebSocket gWebSocket;
        private final boolean bufferFragments;

        // -------------------------------------------------------- Constructors


        GrizzlyWebSocketAdapter(final SimpleWebSocket gWebSocket,
                                final boolean bufferFragements) {
            this.gWebSocket = gWebSocket;
            this.bufferFragments = bufferFragements;
        }


        // ------------------------------------------ Methods from AHC WebSocket


        @Override
        public WebSocket sendMessage(byte[] message) {
            gWebSocket.send(message);
            return this;
        }

        @Override
        public WebSocket stream(byte[] fragment, boolean last) {
            if (isNonEmpty(fragment)) {
                gWebSocket.stream(last, fragment, 0, fragment.length);
            }
            return this;
        }

        @Override
        public WebSocket stream(byte[] fragment, int offset, int len, boolean last) {
            if (isNonEmpty(fragment)) {
                gWebSocket.stream(last, fragment, offset, len);
            }
            return this;
        }

        @Override
        public WebSocket sendTextMessage(String message) {
            gWebSocket.send(message);
            return this;
        }

        @Override
        public WebSocket streamText(String fragment, boolean last) {
            gWebSocket.stream(last, fragment);
            return this;
        }

        @Override
        public WebSocket sendPing(byte[] payload) {
            gWebSocket.sendPing(payload);
            return this;
        }

        @Override
        public WebSocket sendPong(byte[] payload) {
            gWebSocket.sendPong(payload);
            return this;
        }

        @Override
        public WebSocket addWebSocketListener(WebSocketListener l) {
            gWebSocket.add(new AHCWebSocketListenerAdapter(l, this));
            return this;
        }

        @Override
        public WebSocket removeWebSocketListener(WebSocketListener l) {
            gWebSocket.remove(new AHCWebSocketListenerAdapter(l, this));
            return this;
        }

        @Override
        public boolean isOpen() {
            return gWebSocket.isConnected();
        }

        @Override
        public void close() {
            gWebSocket.close();
        }

    } // END GrizzlyWebSocketAdapter


    private static final class AHCWebSocketListenerAdapter implements org.glassfish.grizzly.websockets.WebSocketListener {

        private final WebSocketListener ahcListener;
        private final GrizzlyWebSocketAdapter webSocket;
        private final StringBuilder stringBuffer;
        private final ByteArrayOutputStream byteArrayOutputStream;

        // -------------------------------------------------------- Constructors


        AHCWebSocketListenerAdapter(final WebSocketListener ahcListener,
                                    final GrizzlyWebSocketAdapter webSocket) {
            this.ahcListener = ahcListener;
            this.webSocket = webSocket;
            if (webSocket.bufferFragments) {
                stringBuffer = new StringBuilder();
                byteArrayOutputStream = new ByteArrayOutputStream();
            } else {
                stringBuffer = null;
                byteArrayOutputStream = null;
            }
        }


        // ------------------------------ Methods from Grizzly WebSocketListener


        @Override
        public void onClose(org.glassfish.grizzly.websockets.WebSocket gWebSocket, DataFrame dataFrame) {
            try {
                if (WebSocketCloseCodeReasonListener.class.isAssignableFrom(ahcListener.getClass())) {
                    ClosingFrame cf = ClosingFrame.class.cast(dataFrame);
                    WebSocketCloseCodeReasonListener.class.cast(ahcListener).onClose(webSocket, cf.getCode(), cf.getReason());
                } else {
                    ahcListener.onClose(webSocket);
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onConnect(org.glassfish.grizzly.websockets.WebSocket gWebSocket) {
            try {
                ahcListener.onOpen(webSocket);
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onMessage(org.glassfish.grizzly.websockets.WebSocket webSocket, String s) {
            try {
                if (WebSocketTextListener.class.isAssignableFrom(ahcListener.getClass())) {
                    WebSocketTextListener.class.cast(ahcListener).onMessage(s);
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onMessage(org.glassfish.grizzly.websockets.WebSocket webSocket, byte[] bytes) {
            try {
                if (WebSocketByteListener.class.isAssignableFrom(ahcListener.getClass())) {
                    WebSocketByteListener.class.cast(ahcListener).onMessage(bytes);
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onPing(org.glassfish.grizzly.websockets.WebSocket webSocket, byte[] bytes) {
            try {
                if (WebSocketPingListener.class.isAssignableFrom(ahcListener.getClass())) {
                    WebSocketPingListener.class.cast(ahcListener).onPing(bytes);
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onPong(org.glassfish.grizzly.websockets.WebSocket webSocket, byte[] bytes) {
            try {
                if (WebSocketPongListener.class.isAssignableFrom(ahcListener.getClass())) {
                    WebSocketPongListener.class.cast(ahcListener).onPong(bytes);
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onFragment(org.glassfish.grizzly.websockets.WebSocket webSocket, String s, boolean last) {
            try {
                if (this.webSocket.bufferFragments) {
                    synchronized (this.webSocket) {
                        stringBuffer.append(s);
                        if (last) {
                            if (WebSocketTextListener.class.isAssignableFrom(ahcListener.getClass())) {
                                final String message = stringBuffer.toString();
                                stringBuffer.setLength(0);
                                WebSocketTextListener.class.cast(ahcListener).onMessage(message);
                            }
                        }
                    }
                } else {
                    if (WebSocketTextListener.class.isAssignableFrom(ahcListener.getClass())) {
                        WebSocketTextListener.class.cast(ahcListener).onFragment(s, last);
                    }
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public void onFragment(org.glassfish.grizzly.websockets.WebSocket webSocket, byte[] bytes, boolean last) {
            try {
                if (this.webSocket.bufferFragments) {
                    synchronized (this.webSocket) {
                        byteArrayOutputStream.write(bytes);
                        if (last) {
                            if (WebSocketByteListener.class.isAssignableFrom(ahcListener.getClass())) {
                                final byte[] bytesLocal = byteArrayOutputStream.toByteArray();
                                byteArrayOutputStream.reset();
                                WebSocketByteListener.class.cast(ahcListener).onMessage(bytesLocal);
                            }
                        }
                    }
                } else {
                    if (WebSocketByteListener.class.isAssignableFrom(ahcListener.getClass())) {
                        WebSocketByteListener.class.cast(ahcListener).onFragment(bytes, last);
                    }
                }
            } catch (Throwable e) {
                ahcListener.onError(e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AHCWebSocketListenerAdapter that = (AHCWebSocketListenerAdapter) o;

            if (ahcListener != null ? !ahcListener.equals(that.ahcListener) : that.ahcListener != null)
                return false;
            //noinspection RedundantIfStatement
            if (webSocket != null ? !webSocket.equals(that.webSocket) : that.webSocket != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = ahcListener != null ? ahcListener.hashCode() : 0;
            result = 31 * result + (webSocket != null ? webSocket.hashCode() : 0);
            return result;
        }
    } // END AHCWebSocketListenerAdapter



    public static void main(String[] args) {
        AsyncHttpClientConfig config =
                new AsyncHttpClientConfig.Builder().setSpdyEnabled(
                        true).build();
        AsyncHttpClient client =
                new AsyncHttpClient(new GrizzlyAsyncHttpProvider(config),
                                    config);
        try {
            client.prepareGet("https://www.google.com")
                    .execute(new AsyncHandler<Object>() {
                        @Override
                        public void onThrowable(Throwable t) {
                            t.printStackTrace();
                        }

                        @Override
                        public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
                        throws Exception {
                            System.out.println(
                                    new String(bodyPart.getBodyPartBytes(),
                                               "UTF-8"));
                            return STATE.CONTINUE;
                        }

                        @Override
                        public STATE onStatusReceived(HttpResponseStatus responseStatus)
                        throws Exception {
                            System.out
                                    .println(
                                            responseStatus.getStatusCode());
                            return STATE.CONTINUE;
                        }

                        @Override
                        public STATE onHeadersReceived(HttpResponseHeaders headers)
                        throws Exception {
                            System.out.println(headers.toString());
                            return STATE.CONTINUE;
                        }

                        @Override
                        public Object onCompleted() throws Exception {
                            System.out.println("REQUEST COMPLETE");
                            return null;
                        }
                    }).get();
            client.prepareGet("https://forum-en.guildwars2.com/forum")
                                .execute(new AsyncHandler<Object>() {
                                    @Override
                                    public void onThrowable(Throwable t) {
                                        t.printStackTrace();
                                    }

                                    @Override
                                    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
                                    throws Exception {
                                        System.out.println(
                                                new String(
                                                        bodyPart.getBodyPartBytes(),
                                                        "UTF-8"));
                                        return STATE.CONTINUE;
                                    }

                                    @Override
                                    public STATE onStatusReceived(HttpResponseStatus responseStatus)
                                    throws Exception {
                                        System.out
                                                .println(
                                                        responseStatus.getStatusCode());
                                        return STATE.CONTINUE;
                                    }

                                    @Override
                                    public STATE onHeadersReceived(HttpResponseHeaders headers)
                                    throws Exception {
                                        System.out.println(headers.toString());
                                        return STATE.CONTINUE;
                                    }

                                    @Override
                                    public Object onCompleted()
                                    throws Exception {
                                        System.out.println("REQUEST COMPLETE");
                                        return null;
                                    }
                                }).get();
            System.exit(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


}


