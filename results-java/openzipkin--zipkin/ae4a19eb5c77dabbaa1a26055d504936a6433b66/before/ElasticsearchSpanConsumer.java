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
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.TimeUnit;
import zipkin.Codec;
import zipkin.Span;
import zipkin.storage.elasticsearch.InternalElasticsearchClient.IndexableSpan;
import zipkin.storage.guava.GuavaSpanConsumer;

import static zipkin.internal.ApplyTimestampAndDuration.guessTimestamp;

final class ElasticsearchSpanConsumer implements GuavaSpanConsumer {
  private static final byte[] TIMESTAMP_MILLIS_PREFIX = "{\"timestamp_millis\":".getBytes();

  private final InternalElasticsearchClient client;
  private final IndexNameFormatter indexNameFormatter;

  ElasticsearchSpanConsumer(InternalElasticsearchClient client,
      IndexNameFormatter indexNameFormatter) {
    this.client = client;
    this.indexNameFormatter = indexNameFormatter;
  }

  @Override
  public ListenableFuture<Void> accept(List<Span> spans) {
    return client.indexSpans(
        FluentIterable.from(spans).transform(new Function<Span, IndexableSpan>() {
          @Override
          public IndexableSpan apply(Span span) {
            return createSpanIndexRequest(span);
          }
        }).toList());
  }

  private IndexableSpan createSpanIndexRequest(Span span) {
    Long timestamp = guessTimestamp(span);
    long timestampMillis; // which index to store this span into
    final byte[] spanBytes;
    if (timestamp != null) {
      timestampMillis = TimeUnit.MICROSECONDS.toMillis(timestamp);
      spanBytes = prefixWithTimestampMillis(Codec.JSON.writeSpan(span), timestampMillis);
    } else {
      timestampMillis = System.currentTimeMillis();
      spanBytes = Codec.JSON.writeSpan(span);
    }
    String spanIndex = indexNameFormatter.indexNameForTimestamp(timestampMillis);
    return new IndexableSpan(spanIndex, spanBytes);
  }

  /**
   * In order to allow systems like Kibana to search by timestamp, we add a field "timestamp_millis"
   * when storing. The cheapest way to do this without changing the codec is prefixing it to the
   * json. For example. {"traceId":"... becomes {"timestamp_millis":12345,"traceId":"...
   */
  @VisibleForTesting
  static byte[] prefixWithTimestampMillis(byte[] input, long timestampMillis) {
    String dateAsString = Long.toString(timestampMillis);
    byte[] newSpanBytes =
        new byte[TIMESTAMP_MILLIS_PREFIX.length + dateAsString.length() + input.length];
    int pos = 0;
    System.arraycopy(TIMESTAMP_MILLIS_PREFIX, 0, newSpanBytes, pos, TIMESTAMP_MILLIS_PREFIX.length);
    pos += TIMESTAMP_MILLIS_PREFIX.length;
    for (int i = 0, length = dateAsString.length(); i < length; i++) {
      newSpanBytes[pos++] = (byte) dateAsString.charAt(i);
    }
    newSpanBytes[pos++] = ',';
    // starting at position 1 discards the old head of '{'
    System.arraycopy(input, 1, newSpanBytes, pos, input.length - 1);
    return newSpanBytes;
  }
}