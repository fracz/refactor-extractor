/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ning.http.client.providers;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpProvider;
import com.ning.http.client.AsyncStreamingHandler;
import com.ning.http.client.AsyncStreamingHandler.ResponseComplete;
import com.ning.http.client.ByteArrayPart;
import com.ning.http.client.Cookie;
import com.ning.http.client.FilePart;
import com.ning.http.client.Headers;
import com.ning.http.client.HttpContent;
import com.ning.http.client.HttpResponseBody;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.Part;
import com.ning.http.client.Request;
import com.ning.http.client.RequestType;
import com.ning.http.client.StringPart;
import com.ning.http.collection.Pair;
import com.ning.http.url.Url;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jboss.netty.channel.Channels.pipeline;

@ChannelPipelineCoverage(value = "one")
public class NettyAsyncHttpProvider extends SimpleChannelUpstreamHandler implements AsyncHttpProvider {
    private final static Logger log = LogManager.getLogger(NettyAsyncHttpProvider.class);
    private final ClientBootstrap bootstrap;
    private final static int MAX_BUFFERRED_BYTES = 8192;

    private volatile int redirectCount = 0;
    private final AsyncHttpClientConfig config;

    public NettyAsyncHttpProvider(AsyncHttpClientConfig config) {

        // TODO: Should we expose the Executors.
        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                config.executorService()));
        this.config = config;
    }

    void configure() {
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());

                if (config.isCompressionEnabled()) {
                    pipeline.addLast("inflater", new HttpContentDecompressor());
                }

                pipeline.addLast("httpProcessor", NettyAsyncHttpProvider.this);
                return pipeline;
            }
        });
    }

    Channel performConnect(Url url) throws IOException {
        log.debug("performConnect: " + url.toString());
        configure();
        ChannelFuture channelFuture = null;

        if (config.getProxyServer() == null) {
            channelFuture = bootstrap.connect(
                    new InetSocketAddress(url.getHost(), url.getPort()));
        } else {
            channelFuture = bootstrap.connect(
                    new InetSocketAddress(config.getProxyServer().getHost(), config.getProxyServer().getPort()));
        }

        // Blocking connect
        Channel channel = channelFuture.awaitUninterruptibly().getChannel();

        if (!channel.isConnected()) {
            throw new ConnectException("Connection refused: " + url.toString());
        }
        return channel;
    }

    @SuppressWarnings("deprecation")
    HttpRequest construct(Request request, HttpMethod m, Url url) throws IOException {
        String host = url.getHost();

        if (request.getVirtualHost() != null) {
            host = request.getVirtualHost();
        }

        HttpRequest nettyRequest = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, m, url.getPath());
        nettyRequest.setHeader(HttpHeaders.Names.HOST, host + ":" + url.getPort());

        Headers h = request.getHeaders();
        if (h != null) {
            Iterator<Pair<String, String>> i = h.iterator();
            Pair<String, String> p;
            while (i.hasNext()) {
                p = i.next();
                if ("host" .equalsIgnoreCase(p.getFirst())) {
                    continue;
                }
                String key = p.getFirst() == null ? "": p.getFirst();
                String value = p.getSecond() == null ? "": p.getSecond();

                nettyRequest.setHeader(key, value);
            }
        }

        String ka = config.getKeepAlive() ? "keep-alive" : "close";
        nettyRequest.setHeader(HttpHeaders.Names.CONNECTION, ka);
        if (config.getProxyServer() != null) {
            nettyRequest.setHeader("Proxy-Connection", ka);
        }

        if (config.getUserAgent() != null) {
            nettyRequest.setHeader("User-Agent", config.getUserAgent());
        }

        if (request.getCookies() != null && !request.getCookies().isEmpty()) {
            CookieEncoder httpCookieEncoder = new CookieEncoder(false);
            Iterator<Cookie> ic = request.getCookies().iterator();
            Cookie c;
            org.jboss.netty.handler.codec.http.Cookie cookie;
            while (ic.hasNext()) {
                c = ic.next();
                cookie = new DefaultCookie(c.getName(), c.getValue());
                cookie.setPath(c.getPath());
                cookie.setMaxAge(c.getMaxAge());
                cookie.setDomain(c.getDomain());
                httpCookieEncoder.addCookie(cookie);
            }
            nettyRequest.setHeader(HttpHeaders.Names.COOKIE, httpCookieEncoder.encode());
        }

        if (config.isCompressionEnabled()) {
            nettyRequest.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        }

        switch (request.getType()) {
            case POST:
                if (request.getByteData() != null) {
                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(request.getByteData().length));
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(request.getByteData()));
                } else if (request.getStringData() != null) {
                    // TODO: Not sure we need to reconfigure that one.
                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(request.getStringData().length()));
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(request.getStringData(), "UTF-8"));
                } else if (request.getStreamData() != null) {
                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(request.getStreamData().available()));
                    byte[] b = new byte[(int) request.getStreamData().available()];
                    request.getStreamData().read(b);
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(b));
                } else if (request.getParams() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (final Entry<String, String> param : request.getParams().entrySet()) {
                        sb.append(param.getKey());
                        sb.append("=");
                        sb.append(param.getValue());
                        sb.append("&");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(sb.length()));
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(sb.toString().getBytes()));
                } else if (request.getParts() != null) {
                    int lenght = computeAndSetContentLength(request, nettyRequest);

                    if (lenght == -1) {
                        lenght = MAX_BUFFERRED_BYTES;
                    }

                    /**
                     * This is quite ugly to mix and match with Apache Client,
                     * but the fastest way for now
                     * TODO: Remove this dependency.
                     */
                    PostMethod post = new PostMethod(request.getUrl());
                    MultipartRequestEntity mre = createMultipartRequestEntity(request.getParts(), post.getParams());

                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, mre.getContentType());
                    nettyRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(mre.getContentLength()));

                    ChannelBuffer b = ChannelBuffers.dynamicBuffer((int) lenght);
                    mre.writeRequest(new ChannelBufferOutputStream(b));
                    nettyRequest.setContent(b);
                } else if (request.getEntityWriter() != null) {
                    computeAndSetContentLength(request, nettyRequest);

                    ChannelBuffer b = ChannelBuffers.dynamicBuffer((int) request.getLength());
                    request.getEntityWriter().writeEntity(new ChannelBufferOutputStream(b));
                    nettyRequest.setContent(b);
                }
                break;
            case PUT:
                if (request.getByteData() != null) {
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(request.getByteData()));
                } else if (request.getStringData() != null) {
                    nettyRequest.setContent(ChannelBuffers.copiedBuffer(request.getStringData(), "UTF-8"));
                }
                break;
        }

        if (nettyRequest.getHeader(HttpHeaders.Names.CONTENT_TYPE) == null) {
            nettyRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "txt/html; charset=utf-8");
        }

        log.debug("Constructed request: " + nettyRequest);
        return nettyRequest;
    }

    public void close() {
        config.reaper().shutdown();
        config.executorService().shutdown();
    }

    Url createUrl(String u) {
        URI uri = URI.create(u);
        final String scheme = uri.getScheme();
        if (!scheme.equalsIgnoreCase("http")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + u
                    + ", must be equal (ignoring case) to 'http'");
        }

        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ", must be non-null");
        } else if (path.length() == 0) {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ", must be present");
        } else if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("The URI path, of the URI " + uri
                    + ". must start with a '/'");
        }

        int port = (uri.getPort() == -1) ? 80 : uri.getPort();
        return new Url(uri.getScheme(), uri.getHost(), port, uri.getPath(), uri.getQuery());
    }

    public <T> Future<T> execute(final Request request, final AsyncHandler<T> handler) throws IOException {
        Url url = createUrl(request.getUrl());

        Channel channel = performConnect(url);
        HttpRequest nettyRequest = null;
        switch (request.getType()) {
            case GET:
                nettyRequest = construct(request, HttpMethod.GET, url);
                break;
            case POST:
                nettyRequest = construct(request, HttpMethod.POST, url);
                break;
            case DELETE:
                nettyRequest = construct(request, HttpMethod.DELETE, url);
                break;
            case PUT:
                nettyRequest = construct(request, HttpMethod.PUT, url);
                break;
            case HEAD:
                nettyRequest = construct(request, HttpMethod.HEAD, url);
                break;
        }
        log.debug("Executing the execute operation: " + handler);

        final NettyResponseFuture<T> f = new NettyResponseFuture<T>(url, request, handler, nettyRequest, config.getRequestTimeout());

        channel.getConfig().setConnectTimeoutMillis((int) config.getConnectionTimeoutInMs());
        channel.getPipeline().getContext(NettyAsyncHttpProvider.class).setAttachment(f);

        channel.write(nettyRequest);
        config.reaper().schedule(new Callable<Object>() {
            public Object call() {
                if (!f.isDone()) {
                    f.onThrowable(new TimeoutException());
                }
                return null;
            }

        }, config.getRequestTimeout(), TimeUnit.MILLISECONDS);

        return f;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        /**
         * Discard in memory bytes if the HttpContent.interrupt() has been invoked.
         */
        if (ctx.getAttachment() instanceof DiscardEvent) {
            ctx.getChannel().setReadable(false);
            return;
        }

        NettyResponseFuture<?> future = (NettyResponseFuture<?>) ctx.getAttachment();
        Request request = future.getRequest();
        NettyAsyncResponse<?> asyncResponse = future.getAsyncResponse();
        HttpRequest nettyRequest = future.getNettyRequest();
        AsyncHandler<?> handler = future.getAsyncHandler();
        ChannelBuffer buf = asyncResponse.getBuffer();

        if (e.getMessage() instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();

            if (config.isRedirectEnabled()
                    && (response.getStatus().getCode() == 302 || response.getStatus().getCode() == 301)
                    && (redirectCount + 1) < config.getMaxRedirects()) {
                HttpRequest r = construct(request, map(request.getType()), createUrl(response.getHeader(HttpHeaders.Names.LOCATION)));
                ctx.getChannel().write(r);
                return;
            }
            redirectCount = 0;

            if (buf == null) {
                buf = ChannelBuffers.dynamicBuffer(MAX_BUFFERRED_BYTES);
                asyncResponse.setBuffer(buf);
            }

            asyncResponse.setResponse(response);
            log.debug("Status: " + response.getStatus());
            log.debug("Version: " + response.getProtocolVersion());
            log.debug("\"");
            if (!response.getHeaderNames().isEmpty()) {
                for (String name : response.getHeaderNames()) {
                    log.debug("Header: " + name + " = " + response.getHeaders(name));
                }
                log.debug("\"");
            }

            if (handler != null) {
                if (update(handler, new HttpResponseHeaders(asyncResponse), ctx)) {
                    finishUpdate(asyncResponse,ctx);
                    return;
                } else if (!response.isChunked()){
                    update(handler, new HttpResponseBody(asyncResponse), ctx);
                    finishUpdate(asyncResponse,ctx);
                    return;
                }
            }

            if (!response.isChunked() || response.getStatus().getCode() != 200 || nettyRequest.getMethod().equals(HttpMethod.HEAD)) {
                asyncResponse.getFuture().done();
            }

        } else if (e.getMessage() instanceof HttpChunk) {
            HttpChunk chunk = (HttpChunk) e.getMessage();

            // Just in case the headers arrive after the body
            if (buf == null) {
                buf = ChannelBuffers.dynamicBuffer(MAX_BUFFERRED_BYTES);
                asyncResponse.setBuffer(buf);
            }

            buf.writeBytes(chunk.getContent());

            if (chunk.isLast()) {
                asyncResponse.getFuture().done();
                if (chunk instanceof HttpChunkTrailer) {
                    asyncResponse.setTrailingHeaders((HttpChunkTrailer) chunk);
                }
            }

            if (handler != null) {
                if (update(handler, new HttpResponseBody(asyncResponse, chunk), ctx) || chunk.isLast()) {
                    finishUpdate(asyncResponse,ctx);
                    return;
                }
            }
        }
    }

    private void finishUpdate(NettyAsyncResponse<?> asyncResponse, ChannelHandlerContext ctx) {
        ctx.setAttachment(new DiscardEvent());
        asyncResponse.getFuture().done();
        ctx.getChannel().setReadable(false);
    }

    private final boolean update(AsyncHandler<?> handler, HttpContent c, ChannelHandlerContext ctx) {
        boolean isComplete = false;
        if (handler instanceof AsyncStreamingHandler) {
            try {
                ((AsyncStreamingHandler) handler).onContentReceived(c);
                isComplete = false;
            } catch (ResponseComplete rc) {
                isComplete = true;
            }
        }
        return isComplete;
    }

    //Simple marker for stopping publishing bytes.
    private final class DiscardEvent {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();

        log.debug("I/O Exception during read or execute: ", e.getCause());
        if (ctx.getAttachment() instanceof NettyResponseFuture<?>) {
            NettyResponseFuture<?> future = (NettyResponseFuture<?>) ctx.getAttachment();
            NettyAsyncResponse<?> asyncResponse = future.getAsyncResponse();

            if (asyncResponse != null && asyncResponse.getFuture() != null)
                asyncResponse.getFuture().onThrowable(cause);
        }

        log.debug(e);
        log.debug(ch);
    }

    int computeAndSetContentLength(Request request, HttpRequest r) {
        int lenght = (int) request.getLength();
        if (lenght == -1 && r.getHeader(HttpHeaders.Names.CONTENT_LENGTH) != null) {
            lenght = Integer.valueOf(r.getHeader(HttpHeaders.Names.CONTENT_LENGTH));
        }

        if (lenght != -1) {
            r.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(lenght));
        }
        return lenght;
    }

    /**
     * Map CommonsHttp Method to Netty Method.
     *
     * @param type
     * @return
     */
    HttpMethod map(RequestType type) {
        switch (type) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case DELETE:
                return HttpMethod.DELETE;
            case PUT:
                return HttpMethod.PUT;
            case HEAD:
                return HttpMethod.HEAD;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Map Netty Method to CommonsHttp Method.
     *
     * @param m
     * @return
     */
    HttpMethodBase reverseMap(HttpMethod m) {
        if (m == HttpMethod.GET) {
            return new GetMethod();
        } else if (m == HttpMethod.POST) {
            return new PostMethod();
        } else if (m == HttpMethod.DELETE) {
            return new DeleteMethod();
        } else if (m == HttpMethod.PUT) {
            return new PutMethod();
        } else if (m == HttpMethod.HEAD) {
            return new HeadMethod();
        } else {
            return null;
        }
    }

    /**
     * This is quite ugly has the code is coming from the HTTPClient.
     *
     * @param params
     * @param methodParams
     * @return
     * @throws java.io.FileNotFoundException
     */
    private MultipartRequestEntity createMultipartRequestEntity(List<Part> params, HttpMethodParams methodParams) throws FileNotFoundException {
        org.apache.commons.httpclient.methods.multipart.Part[] parts = new org.apache.commons.httpclient.methods.multipart.Part[params.size()];
        int i = 0;

        for (Part part : params) {
            if (part instanceof StringPart) {
                parts[i] = new org.apache.commons.httpclient.methods.multipart.StringPart(part.getName(),
                        ((StringPart) part).getValue(),
                        "UTF-8");
            } else if (part instanceof FilePart) {
                parts[i] = new org.apache.commons.httpclient.methods.multipart.FilePart(part.getName(),
                        ((FilePart) part).getFile(),
                        ((FilePart) part).getMimeType(),
                        ((FilePart) part).getCharSet());

            } else if (part instanceof ByteArrayPart) {
                PartSource source = new ByteArrayPartSource(((ByteArrayPart) part).getFileName(), ((ByteArrayPart) part).getData());
                parts[i] = new org.apache.commons.httpclient.methods.multipart.FilePart(part.getName(),
                        source,
                        ((ByteArrayPart) part).getMimeType(),
                        ((ByteArrayPart) part).getCharSet());

            } else if (part == null) {
                throw new NullPointerException("Part cannot be null");
            } else {
                throw new IllegalArgumentException(String.format("Unsupported part type for multipart parameter %s",
                        part.getName()));
            }
            ++i;
        }
        return new MultipartRequestEntity(parts, methodParams);
    }

}