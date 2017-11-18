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
package zipkin.storage.elasticsearch;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import zipkin.DependencyLink;
import zipkin.Span;
import zipkin.internal.MergeById;
import zipkin.internal.Util;
import zipkin.storage.DependenciesTest;
import zipkin.storage.InMemorySpanStore;
import zipkin.storage.InMemoryStorage;
import zipkin.storage.StorageComponent;

import static zipkin.TestObjects.DAY;
import static zipkin.TestObjects.TODAY;
import static zipkin.internal.Util.midnightUTC;

public class ElasticsearchDependenciesTest extends DependenciesTest {

  private final ElasticsearchStorage storage;

  public ElasticsearchDependenciesTest() {
    this.storage = ElasticsearchTestGraph.INSTANCE.storage.get();
  }

  @Override protected StorageComponent storage() {
    return storage;
  }

  @Override public void clear() {
    storage.clear();
  }

  /**
   * The current implementation does not include dependency aggregation. It includes retrieval of
   * pre-aggregated links.
   *
   * <p>This uses {@link InMemorySpanStore} to prepare links and {@link #writeDependencyLinks(List,
   * long)}} to store them.
   */
  @Override
  public void processDependencies(List<Span> spans) {
    InMemoryStorage mem = new InMemoryStorage();
    mem.spanConsumer().accept(spans);
    List<DependencyLink> links = mem.spanStore().getDependencies(TODAY + DAY, null);

    // This gets or derives a timestamp from the spans
    long midnight = midnightUTC(MergeById.apply(spans).get(0).timestamp / 1000);
    writeDependencyLinks(links, midnight);
  }

  @VisibleForTesting void writeDependencyLinks(List<DependencyLink> links, long timestampMillis) {
    long midnight = Util.midnightUTC(timestampMillis);
    BulkRequestBuilder request = storage.client().prepareBulk();
    for (DependencyLink link : links) {
      request.add(storage.client().prepareIndex(
          storage.indexNameFormatter.indexNameForTimestamp(midnight),
          ElasticsearchConstants.DEPENDENCY_LINK)
          .setId(link.parent + "|" + link.child) // Unique constraint
          .setSource(
              "parent", link.parent,
              "child", link.child,
              "callCount", link.callCount));
    }
    request.execute().actionGet();
    storage.client().admin().indices().flush(new FlushRequest()).actionGet();
  }
}