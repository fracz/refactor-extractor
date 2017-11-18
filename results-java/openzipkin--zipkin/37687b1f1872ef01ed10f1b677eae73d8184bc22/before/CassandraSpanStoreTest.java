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
package zipkin.storage.cassandra;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import zipkin.Span;
import zipkin.TestObjects;
import zipkin.internal.ApplyTimestampAndDuration;
import zipkin.storage.SpanStoreTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CassandraSpanStoreTest extends SpanStoreTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private final CassandraStorage storage;

  public CassandraSpanStoreTest() {
    this.storage = CassandraTestGraph.INSTANCE.storage.get();
  }

  CassandraSpanStoreTest(CassandraStorage storage) {
    this.storage = storage;
  }

  @Override protected CassandraStorage storage() {
    return storage;
  }

  @Override public void clear() {
    storage.clear();
  }

  /** Cassandra indexing is performed separately, allowing the raw span to be stored unaltered. */
  @Test
  public void rawTraceStoredWithoutAdjustments() {
    Span rawSpan = TestObjects.TRACE.get(0).toBuilder().timestamp(null).duration(null).build();
    accept(rawSpan);

    // At query time, timestamp and duration are added.
    assertThat(store().getTrace(rawSpan.traceId))
        .containsExactly(ApplyTimestampAndDuration.apply(rawSpan));

    // Unlike other stores, Cassandra can show that timestamp and duration weren't reported
    assertThat(store().getRawTrace(rawSpan.traceId))
        .containsExactly(rawSpan);
  }

  /**
   * The PRIMARY KEY of {@link Tables#SERVICE_NAME_INDEX} doesn't consider trace_id, so will only
   * see bucket count traces to a service per millisecond.
   */
  @Override public void getTraces_manyTraces() {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Expected size:<1000> but was:<10>");

    super.getTraces_manyTraces();
  }
}