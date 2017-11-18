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
package zipkin.storage.elasticsearch.http;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import io.searchbox.action.Action;
import io.searchbox.action.GenericResultAbstractAction;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.cluster.Health;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.RootAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.Flush;
import io.searchbox.indices.template.GetTemplate;
import io.searchbox.indices.template.PutTemplate;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import zipkin.Codec;
import zipkin.DependencyLink;
import zipkin.Span;
import zipkin.internal.Lazy;
import zipkin.storage.elasticsearch.InternalElasticsearchClient;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.util.concurrent.Futures.transform;

/**
 * An implementation of {@link InternalElasticsearchClient} that wraps a {@link
 * io.searchbox.client.JestClient}
 */
public final class HttpClient extends InternalElasticsearchClient {

  /**
   * Because the {@link HttpClient} is forced to send the index names in the URL, too many indices
   * (or long names) can quickly exceed the 4096-byte limit commonly imposed on a single HTTP line.
   * This limit is not entirely within the control of Elasticsearch, either, as it may be imposed by
   * arbitrary HTTP-speaking middleware along the request's route. Some more details can be found on
   * this "wontfix" issue: https://github.com/elastic/elasticsearch/issues/7298
   */
  private static final int MAX_INDICES = 100;

  public static final class Builder extends InternalElasticsearchClient.Builder {
    Lazy<List<String>> hosts;
    List<HttpRequestInterceptor> postInterceptors = new ArrayList<>();
    boolean flushOnWrites;

    public Builder() {
      hosts(Collections.singletonList("http://localhost:9200"));
    }

    /**
     * Unnecessary, but we'll check it's not null for consistency's sake.
     */
    @Override public Builder cluster(String cluster) {
      checkNotNull(cluster, "cluster");
      return this;
    }

    /**
     * A list of elasticsearch nodes to connect to, in http://host:port or https://host:port
     * format. Defaults to "http://localhost:9200".
     */
    @Override public Builder hosts(Lazy<List<String>> hosts) {
      this.hosts = checkNotNull(hosts, "hosts");
      return this;
    }

    /** Adds a request interceptor to the end of the chain */
    public Builder addPostInterceptor(HttpRequestInterceptor interceptor) {
      postInterceptors.add(interceptor);
      return this;
    }

    @Override public Builder flushOnWrites(boolean flushOnWrites) {
      this.flushOnWrites = flushOnWrites;
      return this;
    }

    @Override public Factory buildFactory() {
      return new Factory(this);
    }
  }

  private static final class Factory implements InternalElasticsearchClient.Factory {
    final Lazy<List<String>> hosts;
    final List<HttpRequestInterceptor> postInterceptors;
    final boolean flushOnWrites;

    Factory(Builder builder) {
      this.hosts = builder.hosts;
      this.postInterceptors = ImmutableList.copyOf(builder.postInterceptors);
      this.flushOnWrites = builder.flushOnWrites;
    }

    @Override public InternalElasticsearchClient create(String allIndices) {
      return new HttpClient(this, allIndices);
    }

    @Override public String toString() {
      return new StringBuilder("{\"hosts\": [\"").append(Joiner.on("\", \"").join(hosts.get()))
              .append("\"]}").toString();
    }
  }

  final JestClient client;
  final String[] allIndices;
  final boolean flushOnWrites;

  HttpClient(Factory f, String allIndices) {
    JestClientFactory factory = new JestClientFactoryWithInterceptors(Collections.<HttpRequestInterceptor>emptyList(),
        f.postInterceptors);
    factory.setHttpClientConfig(new HttpClientConfig.Builder(f.hosts.get())
        .defaultMaxTotalConnectionPerRoute(6) // matches "regular" TransportClient node conns
        .maxTotalConnection(6 * 10) // would be 20 otherwise, or ~3 routes
        .connTimeout(10 * 1000)
        .readTimeout(10 * 1000)
        .multiThreaded(true)
        .gson(
            new GsonBuilder()
                .registerTypeAdapter(Span.class, SpanDeserializer.INSTANCE)
                .registerTypeAdapter(DependencyLink.class, DependencyLinkDeserializer.INSTANCE)
                .create())
        .build());
    this.client = factory.getObject();
    this.flushOnWrites = f.flushOnWrites;
    this.allIndices = new String[] {allIndices};
  }

  @Override
  public void ensureTemplate(String name, String indexTemplate) throws IOException {
    JestResult existingTemplate = client.execute(new GetTemplate.Builder(name).build());
    if (existingTemplate.isSucceeded()) {
      return;
    }
    client.execute(new PutTemplate.Builder(name, indexTemplate).build());
  }

  @Override
  public void clear(String index) throws IOException {
    client.execute(new DeleteIndex.Builder(index).build());
    client.execute(new Flush.Builder().addIndex(index).build());
  }

  @Override
  public ListenableFuture<Buckets> scanTraces(String[] indices, QueryBuilder query,
      AbstractAggregationBuilder... aggregations) {
    if (indices.length > MAX_INDICES) {
      query = QueryBuilders.indicesQuery(query, indices).noMatchQuery("none");
      indices = allIndices;
    }
    SearchSourceBuilder elasticQuery = new SearchSourceBuilder().query(query).size(0);

    for (AbstractAggregationBuilder aggregation : aggregations) {
      elasticQuery.aggregation(aggregation);
    }

    return transform(toGuava(
        lenientSearch(elasticQuery.toString())
            .addIndex(Arrays.asList(indices))
            .addType(SPAN)
            .build()
    ), AsRestBuckets.INSTANCE);
  }

  private enum AsRestBuckets implements Function<SearchResult, Buckets> {
    INSTANCE;

    @Override public Buckets apply(SearchResult input) {
      return new RestBuckets(input);
    }
  }

  @Override
  public ListenableFuture<List<Span>> findSpans(String[] indices, QueryBuilder query) {
    if (indices.length > MAX_INDICES) {
      query = QueryBuilders.indicesQuery(query, indices).noMatchQuery("none");
      indices = allIndices;
    }

    return transform(toGuava(
        lenientSearch(new SearchSourceBuilder().query(query)
            .size(InternalElasticsearchClient.MAX_RAW_SPANS)
            .toString())
            .addIndex(Arrays.asList(indices))
            .addType(SPAN)
            .build()), new Function<SearchResult, List<Span>>() {
      @Override public List<Span> apply(SearchResult input) {
        if (input.getTotal() == 0) return null;

        ImmutableList.Builder<Span> builder = ImmutableList.builder();
        for (SearchResult.Hit<Span, ?> hit : input.getHits(Span.class)) {
          builder.add(hit.source);
        }
        return builder.build();
      }
    });
  }

  @Override
  public ListenableFuture<Collection<DependencyLink>> findDependencies(String[] indices) {
    QueryBuilder query = QueryBuilders.matchAllQuery();
    if (indices.length > MAX_INDICES) {
      query = QueryBuilders.indicesQuery(query, indices).noMatchQuery("none");
      indices = allIndices;
    }

    Search.Builder search = lenientSearch(new SearchSourceBuilder().query(query).toString())
        .addIndex(Arrays.asList(indices))
        .addType(DEPENDENCY_LINK);

    return transform(toGuava(search.build()),
        new Function<SearchResult, Collection<DependencyLink>>() {
          @Override public Collection<DependencyLink> apply(SearchResult input) {
            ImmutableList.Builder<DependencyLink> builder = ImmutableList.builder();
            for (SearchResult.Hit<DependencyLink, ?> hit : input.getHits(DependencyLink.class)) {
              builder.add(hit.source);
            }
            return builder.build();
          }
        });
  }

  @Override
  public ListenableFuture<Void> indexSpans(List<IndexableSpan> spans) {
    if (spans.isEmpty()) return Futures.immediateFuture(null);

    // Create a bulk request when there is more than one span to store
    ListenableFuture<?> future;
    final Set<String> indices = new LinkedHashSet<>();
    if (spans.size() == 1) {
      IndexableSpan span = getOnlyElement(spans);
      future = toGuava(toIndexRequest(span));
      if (flushOnWrites) {
        indices.add(span.index);
      }
    } else {
      Bulk.Builder batch = new Bulk.Builder();
      for (IndexableSpan span : spans) {
        batch.addAction(toIndexRequest(span));
        if (flushOnWrites) {
          indices.add(span.index);
        }
      }
      future = toGuava(batch.build());
    }

    if (flushOnWrites) {
      future = transform(future, new AsyncFunction() {
        @Override public ListenableFuture apply(Object input) {
          return toGuava(new Flush.Builder().addIndex(indices).build());
        }
      });
    }

    return transform(future, Functions.<Void>constant(null));
  }

  @Override protected void ensureClusterReady(String catchAll) throws IOException {
    String status = client.execute(new IndicesHealth(new Health.Builder(), catchAll))
        .getJsonObject().get("status").getAsString();
    checkState(!"RED".equalsIgnoreCase(status), "Health status is RED");
  }

  private static final class IndicesHealth extends GenericResultAbstractAction {
    final String catchAll;

    IndicesHealth(Builder builder, String catchAll) {
      super(builder);
      this.catchAll = checkNotNull(catchAll, "catchAll");
      this.setURI(this.buildURI());
    }

    @Override
    public String getRestMethodName() {
      return "GET";
    }

    @Override
    protected String buildURI() {
      return super.buildURI() + "/_cluster/health/" + catchAll;
    }
  }

  private static Index toIndexRequest(IndexableSpan span) {
    return new Index.Builder(new String(span.data, Charsets.UTF_8))
        .index(span.index)
        .type(SPAN)
        .build();
  }

  @Override
  public void close() {
    client.shutdownClient();
  }

  /**
   * A Search request that matches the behavior of {@link IndicesOptions#lenientExpandOpen()}
   */
  private static Search.Builder lenientSearch(String query) {
    return new Search.Builder(query)
        .setParameter("ignore_unavailable", "true")
        .setParameter("allow_no_indices", "true")
        .setParameter("expand_wildcards", "open");
  }

  private <T extends JestResult> ListenableFuture<T> toGuava(Action<T> action) {
    final JestFuture<T> future = new JestFuture<T>();
    client.executeAsync(action, future);
    return future;
  }

  static final class JestFuture<T> extends AbstractFuture<T> implements JestResultHandler<T> {
    @Override public void completed(T result) {
      set(result);
    }

    @Override public void failed(Exception ex) {
      setException(ex);
    }
  }

  private enum SpanDeserializer implements JsonDeserializer<Span> {
    INSTANCE;

    @Override
    public Span deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      return Codec.JSON.readSpan(json.toString().getBytes(Charsets.UTF_8));
    }
  }

  private enum DependencyLinkDeserializer implements JsonDeserializer<DependencyLink> {
    INSTANCE;

    @Override
    public DependencyLink deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) {
      return Codec.JSON.readDependencyLink(json.toString().getBytes(Charsets.UTF_8));
    }
  }

  private static class RestBuckets implements Buckets {
    final SearchResult response;

    RestBuckets(SearchResult response) {
      this.response = response;
    }

    @Override
    public List<String> getBucketKeys(String name, String... nestedPath) {
      String[] path = ObjectArrays.concat(name, nestedPath);
      MetricAggregation aggregation = response.getAggregations();

      for (int i = 0; i < path.length - 1 && aggregation != null; i++) {
        aggregation = aggregation.getAggregation(path[i], RootAggregation.class);
      }
      if (aggregation == null) return ImmutableList.of();

      TermsAggregation t = aggregation.getTermsAggregation(path[path.length - 1]);
      if (t == null) return ImmutableList.of();
      return Lists.transform(t.getBuckets(), new Function<TermsAggregation.Entry, String>() {
        @Override
        public String apply(TermsAggregation.Entry input) {
          return input.getKey();
        }
      });
    }
  }

  private static class JestClientFactoryWithInterceptors extends JestClientFactory {
    private final List<HttpRequestInterceptor> preInterceptors;
    private final List<HttpRequestInterceptor> postInterceptors;

    private JestClientFactoryWithInterceptors(List<HttpRequestInterceptor> preInterceptors,
        List<HttpRequestInterceptor> postInterceptors) {
      this.preInterceptors = preInterceptors;
      this.postInterceptors = postInterceptors;
    }

    @Override protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
      for (HttpRequestInterceptor preInterceptor : preInterceptors) {
        builder.addInterceptorFirst(preInterceptor);
      }
      for (HttpRequestInterceptor postInterceptor : postInterceptors) {
        builder.addInterceptorLast(postInterceptor);
      }
      return builder;
    }

    @Override protected HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder builder) {
      for (HttpRequestInterceptor preInterceptor : preInterceptors) {
        builder.addInterceptorFirst(preInterceptor);
      }
      for (HttpRequestInterceptor postInterceptor : postInterceptors) {
        builder.addInterceptorLast(postInterceptor);
      }
      return builder;
    }
  }
}