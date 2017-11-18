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

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class LazyClientTest {

  @Test
  public void testToString() {
    LazyClient lazyClient = new LazyClient(ElasticsearchStorage.builder()
        .cluster("cluster")
        .hosts(asList("host1", "host2")));

    assertThat(lazyClient)
        .hasToString("{\"clusterName\": \"cluster\", \"hosts\": [\"host1\", \"host2\"]}");
  }

  @Test
  public void defaultShardAndReplicaCount() {
    LazyClient lazyClient = new LazyClient(ElasticsearchStorage.builder());

    assertThat(lazyClient.indexTemplate)
        .contains("    \"index.number_of_shards\": 5,\n"
            + "    \"index.number_of_replicas\": 1,");
  }

  @Test
  public void overrideShardAndReplicaCount() {
    LazyClient lazyClient = new LazyClient(ElasticsearchStorage.builder()
        .indexShards(30)
        .indexReplicas(0));

    assertThat(lazyClient.indexTemplate)
        .contains("    \"index.number_of_shards\": 30,\n"
            + "    \"index.number_of_replicas\": 0,");
  }

  @Test
  public void portDefaultsTo9300() {
    try (LazyClient lazyClient = new LazyClient(ElasticsearchStorage.builder()
        .hosts(asList("localhost")))) {

      assertThat(((NativeClient) lazyClient.get()).client.transportAddresses())
          .extracting(TransportAddress::getPort)
          .containsOnly(9300);

    } catch (NoNodeAvailableException e) {
      throw new AssumptionViolatedException(e.getMessage());
    }
  }
}