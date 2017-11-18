/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.junit;

import java.io.IOException;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zipkin.DependencyLink;
import zipkin.QueryRequest;
import zipkin.Span;
import zipkin.SpanStore;
import zipkin.internal.JsonCodec;
import zipkin.internal.Nullable;

/**
 * Implements the span store interface by forwarding requests over http.
 *
 * <p>Intentionally synchronous to reuse span store tests as http interface tests.
 */
final class HttpSpanStore implements SpanStore {

  static final JsonCodec JSON_CODEC = new JsonCodec();
  private final OkHttpClient client = new OkHttpClient();
  private final HttpUrl baseUrl;

  /**
   * @param baseUrl Ex "http://localhost:9411"
   */
  HttpSpanStore(String baseUrl) {
    this.baseUrl = HttpUrl.parse(baseUrl);
  }

  @Override
  public void accept(List<Span> spans) {
    byte[] spansInJson = JSON_CODEC.writeSpans(spans);
    call(new Request.Builder()
        .url(baseUrl.resolve("/api/v1/spans"))
        .post(RequestBody.create(MediaType.parse("application/json"), spansInJson)).build()
    );
  }

  @Override
  public List<List<Span>> getTraces(QueryRequest request) {
    HttpUrl.Builder url = baseUrl.newBuilder("/api/v1/traces?serviceName=" + request.serviceName);
    maybeAddQueryParam(url, "spanName", request.spanName);
    maybeAddQueryParam(url, "annotationQuery", request.toAnnotationQuery());
    maybeAddQueryParam(url, "minDuration", request.minDuration);
    maybeAddQueryParam(url, "maxDuration", request.maxDuration);
    maybeAddQueryParam(url, "endTs", request.endTs);
    maybeAddQueryParam(url, "lookback", request.lookback);
    maybeAddQueryParam(url, "limit", request.limit);
    Response response = call(new Request.Builder().url(url.build()).build());
    return JSON_CODEC.readTraces(responseBytes(response));
  }

  @Override
  public List<Span> getTrace(long traceId) {
    return getTrace(traceId, false);
  }

  @Override
  public List<Span> getRawTrace(long traceId) {
    return getTrace(traceId, true);
  }

  private List<Span> getTrace(long id, boolean raw) {
    Response response = call(new Request.Builder()
        .url(baseUrl.resolve(String.format("/api/v1/trace/%016x%s", id, raw ? "?raw" : "")))
        .build());
    if (response.code() == 404) {
      return null;
    }
    return JSON_CODEC.readSpans(responseBytes(response));
  }

  @Override
  public List<String> getServiceNames() {
    Response response = call(new Request.Builder()
        .url(baseUrl.resolve("/api/v1/services")).build());
    return JSON_CODEC.readStrings(responseBytes(response));
  }

  @Override
  public List<String> getSpanNames(String serviceName) {
    Response response = call(new Request.Builder()
        .url(baseUrl.resolve("/api/v1/spans?serviceName=" + serviceName)).build());
    return JSON_CODEC.readStrings(responseBytes(response));
  }

  @Override
  public List<DependencyLink> getDependencies(long endTs, @Nullable Long lookback) {
    HttpUrl.Builder url = baseUrl.newBuilder("/api/v1/dependencies?endTs=" + endTs);
    if (lookback != null) url.addQueryParameter("lookback", lookback.toString());
    Response response = call(new Request.Builder().url(url.build()).build());
    return JSON_CODEC.readDependencyLinks(responseBytes(response));
  }

  Response call(Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  void maybeAddQueryParam(HttpUrl.Builder builder, String name, @Nullable Object value) {
    if (value != null) builder.addQueryParameter(name, value.toString());
  }

  byte[] responseBytes(Response response) {
    if (response.code() != 200) throw new RuntimeException("unexpected response: " + response);
    try {
      return response.body().bytes();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}