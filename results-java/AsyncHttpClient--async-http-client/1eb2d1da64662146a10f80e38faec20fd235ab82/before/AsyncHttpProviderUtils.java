/*
 * Copyright (c) 2010-2012 Sonatype, Inc. All rights reserved.
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
package org.asynchttpclient.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpProvider;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Request;

/**
 * {@link org.asynchttpclient.AsyncHttpProvider} common utilities.
 */
public class AsyncHttpProviderUtils {

    public static final IOException REMOTELY_CLOSED_EXCEPTION = new IOException("Remotely Closed");

    static {
        REMOTELY_CLOSED_EXCEPTION.setStackTrace(new StackTraceElement[] {});
    }

    private final static byte[] NO_BYTES = new byte[0];

    public final static Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private final static String BODY_NOT_COMPUTED = "Response's body hasn't been computed by your AsyncHandler.";

    protected final static ThreadLocal<SimpleDateFormat[]> simpleDateFormat = new ThreadLocal<SimpleDateFormat[]>() {
        protected SimpleDateFormat[] initialValue() {

            return new SimpleDateFormat[] {
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US), // RFC1123
                    new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), // RFC1036
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US), // ASCTIME
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US), new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US),
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US), new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss Z", Locale.US) };
        }
    };

    public final static SimpleDateFormat[] get() {
        return simpleDateFormat.get();
    }

    public static final void validateSupportedScheme(URI uri) {
        final String scheme = uri.getScheme();
        if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("ws") && !scheme.equalsIgnoreCase("wss")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + uri + ", must be equal (ignoring case) to 'http', 'https', 'ws', or 'wss'");
        }
    }

    public final static URI createUri(String u) {
        URI uri = URI.create(u);
        validateSupportedScheme(uri);

        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("The URI path, of the URI " + uri + ", must be non-null");
        } else if (path.length() > 0 && path.charAt(0) != '/') {
            throw new IllegalArgumentException("The URI path, of the URI " + uri + ". must start with a '/'");
        } else if (path.length() == 0) {
            return URI.create(u + "/");
        }

        return uri;
    }

    public static String getBaseUrl(String url) {
        return getBaseUrl(createUri(url));
    }

    public final static String getBaseUrl(URI uri) {
        String url = uri.getScheme() + "://" + uri.getAuthority();
        int port = uri.getPort();
        if (port == -1) {
            port = getPort(uri);
            url += ":" + port;
        }
        return url;
    }

    public final static String getAuthority(URI uri) {
        String url = uri.getAuthority();
        int port = uri.getPort();
        if (port == -1) {
            port = getPort(uri);
            url += ":" + port;
        }
        return url;
    }

    public final static String contentToString(List<HttpResponseBodyPart> bodyParts, String charset) throws UnsupportedEncodingException {
        return new String(contentToBytes(bodyParts), charset);
    }

    /**
     * @param bodyParts NON EMPTY body part
     * @return
     * @throws UnsupportedEncodingException
     */
    public final static byte[] contentToBytes(List<HttpResponseBodyPart> bodyParts) throws UnsupportedEncodingException {
        final int partCount = bodyParts.size();
        if (partCount == 0) {
            return NO_BYTES;
        }
        if (partCount == 1) {
            return bodyParts.get(0).getBodyPartBytes();
        }
        int size = 0;
        ArrayList<byte[]> chunks = new ArrayList<byte[]>(partCount);
        for (HttpResponseBodyPart part : bodyParts) {
            byte[] chunk = part.getBodyPartBytes();
            size += chunk.length;
            chunks.add(chunk);
        }
        byte[] bytes = new byte[size];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, bytes, offset, chunk.length);
            offset += chunk.length;
        }
        return bytes;
    }

    /**
     * @param bodyParts NON EMPTY body part
     * @param maxLen
     * @return
     * @throws UnsupportedEncodingException
     */
    public final static byte[] contentToBytes(List<HttpResponseBodyPart> bodyParts, int maxLen) throws UnsupportedEncodingException {
        final int partCount = bodyParts.size();
        if (partCount == 0) {
            return NO_BYTES;
        }
        if (partCount == 1) {
            byte[] chunk = bodyParts.get(0).getBodyPartBytes();
            if (chunk.length <= maxLen) {
                return chunk;
            }
            byte[] result = new byte[maxLen];
            System.arraycopy(chunk, 0, result, 0, maxLen);
            return result;
        }
        int size = 0;
        byte[] result = new byte[maxLen];
        for (HttpResponseBodyPart part : bodyParts) {
            byte[] chunk = part.getBodyPartBytes();
            int amount = Math.min(maxLen - size, chunk.length);
            System.arraycopy(chunk, 0, result, size, amount);
            size += amount;
            if (size == maxLen) {
                return result;
            }
        }
        if (size < maxLen) {
            byte[] old = result;
            result = new byte[old.length];
            System.arraycopy(old, 0, result, 0, old.length);
        }
        return result;
    }

    /**
     * @param bodyParts NON EMPTY body part
     * @return
     */
    public final static InputStream contentAsStream(List<HttpResponseBodyPart> bodyParts) {
        switch (bodyParts.size()) {
        case 0:
            return new ByteArrayInputStream(NO_BYTES);
        case 1:
            return bodyParts.get(0).readBodyPartBytes();
        }
        Vector<InputStream> streams = new Vector<InputStream>(bodyParts.size());
        for (HttpResponseBodyPart part : bodyParts) {
            streams.add(part.readBodyPartBytes());
        }
        return new SequenceInputStream(streams.elements());
    }

    public final static String getHost(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            host = uri.getAuthority();
        }
        return host;
    }

    public final static URI getRedirectUri(URI uri, String location) {
        if (location == null)
            throw new IllegalArgumentException("URI " + uri + " was redirected to null location");

        URI locationURI = null;
        try {
            locationURI = new URI(location);
        } catch (URISyntaxException e) {
            // rich, we have a badly encoded location, let's try to encode the query params
            String[] parts = location.split("\\?");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Don't know how to turn this location into a proper URI:" + location, e);
            } else {
                StringBuilder properUrl = new StringBuilder(location.length()).append(parts[0]).append("?");

                String[] queryParams = parts[1].split("&");
                for (int i = 0; i < queryParams.length; i++) {
                    String queryParam = queryParams[i];
                    if (i != 0)
                        properUrl.append("&");
                    String[] nameValue = queryParam.split("=", 2);
                    UTF8UrlEncoder.appendEncoded(properUrl, nameValue[0]);
                    if (nameValue.length == 2) {
                        properUrl.append("=");
                        UTF8UrlEncoder.appendEncoded(properUrl, nameValue[1]);
                    }
                }

                locationURI = URI.create(properUrl.toString());
            }
        }

        URI redirectUri = uri.resolve(locationURI);

        String scheme = redirectUri.getScheme();

        if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equals("ws") && !scheme.equals("wss")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + redirectUri + ", must be equal (ignoring case) to 'ws, 'wss', 'http', or 'https'");
        }

        return redirectUri.normalize();
    }

    public final static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1)
            port = uri.getScheme().equals("http") || uri.getScheme().equals("ws") ? 80 : 443;
        return port;
    }

    public static String constructUserAgent(Class<? extends AsyncHttpProvider> httpProvider, AsyncHttpClientConfig config) {
        return new StringBuilder("AHC (").append(httpProvider.getSimpleName()).append(" - ").append(System.getProperty("os.name")).append(" - ")
                .append(System.getProperty("os.version")).append(" - ").append(System.getProperty("java.version")).append(" - ").append(Runtime.getRuntime().availableProcessors())
                .append(" core(s))").toString();
    }

    public static String parseCharset(String contentType) {
        for (String part : contentType.split(";")) {
            if (part.trim().startsWith("charset=")) {
                String[] val = part.split("=");
                if (val.length > 1) {
                    String charset = val[1].trim();
                    // Quite a lot of sites have charset="CHARSET",
                    // e.g. charset="utf-8". Note the quotes. This is
                    // not correct, but client should be able to handle
                    // it (all browsers do, Apache HTTP Client and Grizzly
                    // strip it by default)
                    // This is a poor man's trim("\"").trim("'")
                    return charset.replaceAll("\"", "").replaceAll("'", "");
                }
            }
        }
        return null;
    }

    public static int convertExpireField(String timestring) {
        String trimmedTimeString = removeQuotes(timestring.trim());

        for (SimpleDateFormat sdf : simpleDateFormat.get()) {
            Date date = sdf.parse(trimmedTimeString, new ParsePosition(0));
            if (date != null) {
                long now = System.currentTimeMillis();
                long maxAgeMillis = date.getTime() - now;
                return (int) (maxAgeMillis / 1000) + (maxAgeMillis % 1000 != 0 ? 1 : 0);
            }
        }

        throw new IllegalArgumentException("Not a valid expire field " + trimmedTimeString);
    }

    public final static String removeQuotes(String s) {
        if (MiscUtil.isNonEmpty(s)) {
            int start = 0;
            int end = s.length();
            boolean changed = false;

            if (s.charAt(0) == '"') {
                changed = true;
                start++;
            }

            if (s.charAt(s.length() - 1) == '"') {
                changed = true;
                end--;
            }

            if (changed)
                s = s.substring(start, end);
        }
        return s;
    }

    public static void checkBodyParts(int statusCode, Collection<HttpResponseBodyPart> bodyParts) {
        if (bodyParts == null || bodyParts.size() == 0) {

            // We allow empty body on 204
            if (statusCode == 204)
                return;

            throw new IllegalStateException(BODY_NOT_COMPUTED);
        }
    }

    public static String keepAliveHeaderValue(AsyncHttpClientConfig config) {
        return config.getAllowPoolingConnection() ? "keep-alive" : "close";
    }

    public static int requestTimeout(AsyncHttpClientConfig config, Request request) {
        return request.getRequestTimeoutInMs() != 0 ? request.getRequestTimeoutInMs() : config.getRequestTimeoutInMs();
    }
}