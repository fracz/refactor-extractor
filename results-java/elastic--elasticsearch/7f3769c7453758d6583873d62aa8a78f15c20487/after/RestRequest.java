/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.rest;

import org.apache.lucene.util.SetOnce;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.elasticsearch.common.unit.ByteSizeValue.parseBytesSizeValue;
import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;

public abstract class RestRequest implements ToXContent.Params {

    private static final DeprecationLogger DEPRECATION_LOGGER = new DeprecationLogger(Loggers.getLogger(RestRequest.class));
    // tchar pattern as defined by RFC7230 section 3.2.6
    private static final Pattern TCHAR_PATTERN = Pattern.compile("[a-zA-z0-9!#$%&'*+\\-.\\^_`|~]+");

    private final NamedXContentRegistry xContentRegistry;
    private final Map<String, String> params;
    private final Map<String, List<String>> headers;
    private final String rawPath;
    private final Set<String> consumedParams = new HashSet<>();
    private final SetOnce<XContentType> xContentType = new SetOnce<>();

    /**
     * Creates a new RestRequest
     * @param xContentRegistry the xContentRegistry to use when parsing XContent
     * @param uri the URI of the request that potentially contains request parameters
     * @param headers a map of the headers. This map should implement a Case-Insensitive hashing for keys as HTTP header names are case
     *                insensitive
     */
    public RestRequest(NamedXContentRegistry xContentRegistry, String uri, Map<String, List<String>> headers) {
        this.xContentRegistry = xContentRegistry;
        final Map<String, String> params = new HashMap<>();
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos < 0) {
            this.rawPath = uri;
        } else {
            this.rawPath = uri.substring(0, pathEndPos);
            RestUtils.decodeQueryString(uri, pathEndPos + 1, params);
        }
        this.params = params;
        this.headers = Collections.unmodifiableMap(headers);
        final List<String> contentType = getAllHeaderValues("Content-Type");
        final XContentType xContentType = parseContentType(contentType);
        if (xContentType != null) {
            this.xContentType.set(xContentType);
        }
    }

    /**
     * Creates a new RestRequest
     * @param xContentRegistry the xContentRegistry to use when parsing XContent
     * @param params the parameters of the request
     * @param path the path of the request. This should not contain request parameters
     * @param headers a map of the headers. This map should implement a Case-Insensitive hashing for keys as HTTP header names are case
     *                insensitive
     */
    public RestRequest(NamedXContentRegistry xContentRegistry, Map<String, String> params, String path, Map<String, List<String>> headers) {
        this.xContentRegistry = xContentRegistry;
        this.params = params;
        this.rawPath = path;
        this.headers = Collections.unmodifiableMap(headers);
        final List<String> contentType = getAllHeaderValues("Content-Type");
        final XContentType xContentType = parseContentType(contentType);
        if (xContentType != null) {
            this.xContentType.set(xContentType);
        }
    }

    public enum Method {
        GET, POST, PUT, DELETE, OPTIONS, HEAD
    }

    public abstract Method method();

    /**
     * The uri of the rest request, with the query string.
     */
    public abstract String uri();

    /**
     * The non decoded, raw path provided.
     */
    public String rawPath() {
        return rawPath;
    }

    /**
     * The path part of the URI (without the query string), decoded.
     */
    public final String path() {
        return RestUtils.decodeComponent(rawPath());
    }

    public abstract boolean hasContent();

    public abstract BytesReference content();

    /**
     * Get the value of the header or {@code null} if not found. This method only retrieves the first header value if multiple values are
     * sent. Use of {@link #getAllHeaderValues(String)} should be preferred
     */
    public final String header(String name) {
        List<String> values = headers.get(name);
        if (values != null && values.isEmpty() == false) {
            return values.get(0);
        }
        return null;
    }

    /**
     * Get all values for the header or {@code null} if the header was not found
     */
    public final List<String> getAllHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        return null;
    }

    /**
     * Get all of the headers and values associated with the headers. Modifications of this map are not supported.
     */
    public final Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * The {@link XContentType} that was parsed from the {@code Content-Type} header. This value will be {@code null} in the case of
     * a request without a valid {@code Content-Type} header, a request without content ({@link #hasContent()}, or a plain text request
     */
    @Nullable
    public final XContentType getXContentType() {
        return xContentType.get();
    }

    /**
     * Sets the {@link XContentType}
     */
    final void setXContentType(XContentType xContentType) {
        this.xContentType.set(xContentType);
    }

    @Nullable
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Nullable
    public SocketAddress getLocalAddress() {
        return null;
    }

    public final boolean hasParam(String key) {
        return params.containsKey(key);
    }

    @Override
    public final String param(String key) {
        consumedParams.add(key);
        return params.get(key);
    }

    @Override
    public final String param(String key, String defaultValue) {
        consumedParams.add(key);
        String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Map<String, String> params() {
        return params;
    }

    /**
     * Returns a list of parameters that have been consumed. This method returns a copy, callers
     * are free to modify the returned list.
     *
     * @return the list of currently consumed parameters.
     */
    List<String> consumedParams() {
        return consumedParams.stream().collect(Collectors.toList());
    }

    /**
     * Returns a list of parameters that have not yet been consumed. This method returns a copy,
     * callers are free to modify the returned list.
     *
     * @return the list of currently unconsumed parameters.
     */
    List<String> unconsumedParams() {
        return params
            .keySet()
            .stream()
            .filter(p -> !consumedParams.contains(p))
            .collect(Collectors.toList());
    }

    public float paramAsFloat(String key, float defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse float parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    public int paramAsInt(String key, int defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse int parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    public long paramAsLong(String key, long defaultValue) {
        String sValue = param(key);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse long parameter [" + key + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public boolean paramAsBoolean(String key, boolean defaultValue) {
        String rawParam = param(key);
        // Treat empty string as true because that allows the presence of the url parameter to mean "turn this on"
        if (rawParam != null && rawParam.length() == 0) {
            return true;
        } else {
            return Booleans.parseBoolean(rawParam, defaultValue);
        }
    }

    @Override
    public Boolean paramAsBoolean(String key, Boolean defaultValue) {
        return Booleans.parseBoolean(param(key), defaultValue);
    }

    public TimeValue paramAsTime(String key, TimeValue defaultValue) {
        return parseTimeValue(param(key), defaultValue, key);
    }

    public ByteSizeValue paramAsSize(String key, ByteSizeValue defaultValue) {
        return parseBytesSizeValue(param(key), defaultValue, key);
    }

    public String[] paramAsStringArray(String key, String[] defaultValue) {
        String value = param(key);
        if (value == null) {
            return defaultValue;
        }
        return Strings.splitStringByCommaToArray(value);
    }

    public String[] paramAsStringArrayOrEmptyIfAll(String key) {
        String[] params = paramAsStringArray(key, Strings.EMPTY_ARRAY);
        if (Strings.isAllOrWildcard(params)) {
            return Strings.EMPTY_ARRAY;
        }
        return params;
    }

    /**
     * Get the {@link NamedXContentRegistry} that should be used to create parsers from this request.
     */
    public NamedXContentRegistry getXContentRegistry() {
        return xContentRegistry;
    }

    /**
     * A parser for the contents of this request if there is a body, otherwise throws an {@link ElasticsearchParseException}. Use
     * {@link #applyContentParser(CheckedConsumer)} if you want to gracefully handle when the request doesn't have any contents. Use
     * {@link #contentOrSourceParamParser()} for requests that support specifying the request body in the {@code source} param.
     */
    public final XContentParser contentParser() throws IOException {
        BytesReference content = content();
        if (content.length() == 0) {
            throw new ElasticsearchParseException("Body required");
        } else if (xContentType.get() == null) {
            throw new IllegalStateException("unknown content type");
        }
        return xContentType.get().xContent().createParser(xContentRegistry, content);
    }

    /**
     * If there is any content then call {@code applyParser} with the parser, otherwise do nothing.
     */
    public final void applyContentParser(CheckedConsumer<XContentParser, IOException> applyParser) throws IOException {
        if (hasContent()) {
            try (XContentParser parser = contentParser()) {
                applyParser.accept(parser);
            }
        }
    }

    /**
     * Does this request have content or a {@code source} parameter? Use this instead of {@link #hasContent()} if this
     * {@linkplain RestHandler} treats the {@code source} parameter like the body content.
     */
    public final boolean hasContentOrSourceParam() {
        return hasContent() || hasParam("source");
    }

    /**
     * A parser for the contents of this request if it has contents, otherwise a parser for the {@code source} parameter if there is one,
     * otherwise throws an {@link ElasticsearchParseException}. Use {@link #withContentOrSourceParamParserOrNull(CheckedConsumer)} instead
     * if you need to handle the absence request content gracefully.
     */
    public final XContentParser contentOrSourceParamParser() throws IOException {
        Tuple<XContentType, BytesReference> tuple = contentOrSourceParam();
        BytesReference content = tuple.v2();
        if (content.length() == 0) {
            throw new ElasticsearchParseException("Body required");
        }
        return tuple.v1().xContent().createParser(xContentRegistry, content);
    }

    /**
     * Call a consumer with the parser for the contents of this request if it has contents, otherwise with a parser for the {@code source}
     * parameter if there is one, otherwise with {@code null}. Use {@link #contentOrSourceParamParser()} if you should throw an exception
     * back to the user when there isn't request content.
     */
    public final void withContentOrSourceParamParserOrNull(CheckedConsumer<XContentParser, IOException> withParser) throws IOException {
        Tuple<XContentType, BytesReference> tuple = contentOrSourceParam();
        BytesReference content = tuple.v2();
        XContentType xContentType = tuple.v1();
        if (content.length() > 0) {
            try (XContentParser parser = xContentType.xContent().createParser(xContentRegistry, content)) {
                withParser.accept(parser);
            }
        } else {
            withParser.accept(null);
        }
    }

    /**
     * Get the content of the request or the contents of the {@code source} param. Prefer {@link #contentOrSourceParamParser()} or
     * {@link #withContentOrSourceParamParserOrNull(CheckedConsumer)} if you need a parser.
     */
    public final Tuple<XContentType, BytesReference> contentOrSourceParam() {
        if (hasContent()) {
            if (xContentType.get() == null) {
                throw new IllegalStateException("unknown content type");
            }
            return new Tuple<>(xContentType.get(), content());
        }

        String source = param("source");
        String typeParam = param("source_content_type");
        if (source != null) {
            BytesArray bytes = new BytesArray(source);
            final XContentType xContentType;
            if (typeParam != null) {
                xContentType = parseContentType(Collections.singletonList(typeParam));
            } else {
                DEPRECATION_LOGGER.deprecated("Deprecated use of the [source] parameter without the [source_content_type] parameter. Use " +
                    "the [source_content_type] parameter to specify the content type of the source such as [application/json]");
                xContentType = XContentFactory.xContentType(bytes);
            }

            if (xContentType == null) {
                throw new IllegalStateException("could not determine source content type");
            }
            return new Tuple<>(xContentType, bytes);
        }
        return new Tuple<>(XContentType.JSON, BytesArray.EMPTY);
    }

    /**
     * Call a consumer with the parser for the contents of this request if it has contents, otherwise with a parser for the {@code source}
     * parameter if there is one, otherwise with {@code null}. Use {@link #contentOrSourceParamParser()} if you should throw an exception
     * back to the user when there isn't request content. This version allows for plain text content
     */
    @Deprecated
    public final void withContentOrSourceParamParserOrNullLenient(CheckedConsumer<XContentParser, IOException> withParser)
        throws IOException {
        if (hasContent() && xContentType.get() == null) {
            withParser.accept(null);
        }

        Tuple<XContentType, BytesReference> tuple = contentOrSourceParam();
        BytesReference content = tuple.v2();
        XContentType xContentType = tuple.v1();
        if (content.length() > 0) {
            try (XContentParser parser = xContentType.xContent().createParser(xContentRegistry, content)) {
                withParser.accept(parser);
            }
        } else {
            withParser.accept(null);
        }
    }

    /**
     * Get the content of the request or the contents of the {@code source} param without the xcontent type. This is useful the request can
     * accept non xcontent values.
     * @deprecated we should only take xcontent
     */
    @Deprecated
    public final BytesReference getContentOrSourceParamOnly() {
        if (hasContent()) {
            return content();
        }
        String source = param("source");
        if (source != null) {
            return new BytesArray(source);
        }
        return BytesArray.EMPTY;
    }

    /**
     * Parses the given content type string for the media type. This method currently ignores parameters.
     */
    // TODO stop ignoring parameters such as charset...
    private static XContentType parseContentType(List<String> header) {
        if (header == null || header.isEmpty()) {
            return null;
        } else if (header.size() > 1) {
            throw new IllegalArgumentException("only one Content-Type header should be provided");
        }

        String rawContentType = header.get(0);
        final String[] elements = rawContentType.split("[ \t]*;");
        if (elements.length > 0) {
            final String[] splitMediaType = elements[0].split("/");
            if (splitMediaType.length == 2 && TCHAR_PATTERN.matcher(splitMediaType[0]).matches()
                && TCHAR_PATTERN.matcher(splitMediaType[1].trim()).matches()) {
                return XContentType.fromMediaType(elements[0]);
            } else {
                throw new IllegalArgumentException("invalid Content-Type header [" + rawContentType + "]");
            }
        }
        throw new IllegalArgumentException("empty Content-Type header");
    }
}