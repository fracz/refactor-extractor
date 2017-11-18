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

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Function;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Codec;
import zipkin.DependencyLink;
import zipkin.Span;
import zipkin.internal.CorrectForClockSkew;
import zipkin.internal.Dependencies;
import zipkin.internal.DependencyLinker;
import zipkin.internal.MergeById;
import zipkin.internal.Nullable;
import zipkin.storage.QueryRequest;
import zipkin.storage.guava.GuavaSpanStore;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.DiscreteDomain.integers;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.Futures.transform;
import static zipkin.internal.Util.getDays;
import static zipkin.internal.Util.midnightUTC;

public final class CassandraSpanStore implements GuavaSpanStore {
  private static final Logger LOG = LoggerFactory.getLogger(CassandraSpanStore.class);

  static final ListenableFuture<List<String>> EMPTY_LIST =
      immediateFuture(Collections.<String>emptyList());
  static final Ordering<List<Span>> TRACE_DESCENDING = Ordering.from(new Comparator<List<Span>>() {
    @Override
    public int compare(List<Span> left, List<Span> right) {
      return right.get(0).compareTo(left.get(0));
    }
  });

  private final int durationTtl;
  private final int maxTraceCols;
  private final int indexFetchMultiplier;
  private final Session session;
  private final TimestampCodec timestampCodec;
  private final Set<Integer> buckets;
  private final PreparedStatement selectTraces;
  private final PreparedStatement selectDependencies;
  private final PreparedStatement selectServiceNames;
  private final PreparedStatement selectSpanNames;
  private final PreparedStatement selectTraceIdsByServiceName;
  private final PreparedStatement selectTraceIdsByServiceNames;
  private final PreparedStatement selectTraceIdsBySpanName;
  private final PreparedStatement selectTraceIdsByAnnotation;
  private final PreparedStatement selectTraceIdsBySpanDuration;
  private final Function<ResultSet, Map<Long, Long>> traceIdToTimestamp;

  CassandraSpanStore(Session session, int bucketCount, int indexTtl, int maxTraceCols,
      int indexFetchMultiplier) {
    this.session = session;
    this.maxTraceCols = maxTraceCols;
    this.indexFetchMultiplier = indexFetchMultiplier;
    ProtocolVersion protocolVersion = session.getCluster()
        .getConfiguration().getProtocolOptions().getProtocolVersion();
    this.timestampCodec = new TimestampCodec(protocolVersion);
    this.buckets = ContiguousSet.create(Range.closedOpen(0, bucketCount), integers());

    selectTraces = session.prepare(
        QueryBuilder.select("trace_id", "span")
            .from("traces")
            .where(QueryBuilder.in("trace_id", QueryBuilder.bindMarker("trace_id")))
            .limit(QueryBuilder.bindMarker("limit_")));

    selectDependencies = session.prepare(
        QueryBuilder.select("dependencies")
            .from("dependencies")
            .where(QueryBuilder.in("day", QueryBuilder.bindMarker("days"))));

    selectServiceNames = session.prepare(
        QueryBuilder.select("service_name")
            .from(Tables.SERVICE_NAMES));

    selectSpanNames = session.prepare(
        QueryBuilder.select("span_name")
            .from(Tables.SPAN_NAMES)
            .where(QueryBuilder.eq("service_name", QueryBuilder.bindMarker("service_name")))
            .and(QueryBuilder.eq("bucket", QueryBuilder.bindMarker("bucket")))
            .limit(QueryBuilder.bindMarker("limit_")));

    selectTraceIdsByServiceName = session.prepare(
        QueryBuilder.select("ts", "trace_id")
            .from(Tables.SERVICE_NAME_INDEX)
            .where(QueryBuilder.eq("service_name", QueryBuilder.bindMarker("service_name")))
            .and(QueryBuilder.in("bucket", QueryBuilder.bindMarker("bucket")))
            .and(QueryBuilder.gte("ts", QueryBuilder.bindMarker("start_ts")))
            .and(QueryBuilder.lte("ts", QueryBuilder.bindMarker("end_ts")))
            .limit(QueryBuilder.bindMarker("limit_"))
            .orderBy(QueryBuilder.desc("ts")));

    selectTraceIdsBySpanName = session.prepare(
        QueryBuilder.select("ts", "trace_id")
            .from(Tables.SERVICE_SPAN_NAME_INDEX)
            .where(
                QueryBuilder.eq("service_span_name", QueryBuilder.bindMarker("service_span_name")))
            .and(QueryBuilder.gte("ts", QueryBuilder.bindMarker("start_ts")))
            .and(QueryBuilder.lte("ts", QueryBuilder.bindMarker("end_ts")))
            .limit(QueryBuilder.bindMarker("limit_"))
            .orderBy(QueryBuilder.desc("ts")));

    selectTraceIdsByAnnotation = session.prepare(
        QueryBuilder.select("ts", "trace_id")
            .from(Tables.ANNOTATIONS_INDEX)
            .where(QueryBuilder.eq("annotation", QueryBuilder.bindMarker("annotation")))
            .and(QueryBuilder.in("bucket", QueryBuilder.bindMarker("bucket")))
            .and(QueryBuilder.gte("ts", QueryBuilder.bindMarker("start_ts")))
            .and(QueryBuilder.lte("ts", QueryBuilder.bindMarker("end_ts")))
            .limit(QueryBuilder.bindMarker("limit_"))
            .orderBy(QueryBuilder.desc("ts")));

    int durationDefaultTtl = Schema.getKeyspaceMetadata(session)
        .getTable(Tables.SPAN_DURATION_INDEX)
        .getOptions()
        .getDefaultTimeToLive();

    this.durationTtl = durationDefaultTtl == 0 ? indexTtl : durationDefaultTtl;

    selectTraceIdsBySpanDuration = session.prepare(
        QueryBuilder.select("duration", "ts", "trace_id")
            .from(Tables.SPAN_DURATION_INDEX)
            .where(QueryBuilder.eq("service_name", QueryBuilder.bindMarker("service_name")))
            .and(QueryBuilder.eq("span_name", QueryBuilder.bindMarker("span_name")))
            .and(QueryBuilder.eq("bucket", QueryBuilder.bindMarker("time_bucket")))
            .and(QueryBuilder.lte("duration", QueryBuilder.bindMarker("max_duration")))
            .and(QueryBuilder.gte("duration", QueryBuilder.bindMarker("min_duration")))
            .orderBy(QueryBuilder.desc("duration")));

    if (protocolVersion.compareTo(ProtocolVersion.V4) < 0) {
      LOG.warn("Please update Cassandra to 2.2 or later, as some features may fail");
      // Log vs failing on "Partition KEY part service_name cannot be restricted by IN relation"
      selectTraceIdsByServiceNames = null;
    } else {
      selectTraceIdsByServiceNames = session.prepare(
          QueryBuilder.select("ts", "trace_id")
              .from(Tables.SERVICE_NAME_INDEX)
              .where(QueryBuilder.in("service_name", QueryBuilder.bindMarker("service_name")))
              .and(QueryBuilder.in("bucket", QueryBuilder.bindMarker("bucket")))
              .and(QueryBuilder.gte("ts", QueryBuilder.bindMarker("start_ts")))
              .and(QueryBuilder.lte("ts", QueryBuilder.bindMarker("end_ts")))
              .limit(QueryBuilder.bindMarker("limit_"))
              .orderBy(QueryBuilder.desc("ts")));
    }

    traceIdToTimestamp = new Function<ResultSet, Map<Long, Long>>() {
      @Override public Map<Long, Long> apply(ResultSet input) {
        Map<Long, Long> traceIdsToTimestamps = new LinkedHashMap<>();
        for (Row row : input) {
          traceIdsToTimestamps.put(row.getLong("trace_id"), timestampCodec.deserialize(row, "ts"));
        }
        return traceIdsToTimestamps;
      }
    };
  }

  /**
   * This fans out into a potentially large amount of requests, particularly if duration is set, but
   * also related to the amount of annotations queried. The returned future will fail if any of the
   * inputs fail.
   *
   * <p>When {@link QueryRequest#serviceName service name} is unset, service names will be
   * fetched eagerly, implying an additional query.
   *
   * <p>The duration query is the most expensive query in cassandra, as it turns into 1 request per
   * hour of {@link QueryRequest#lookback lookback}. Because many times lookback is set to a day,
   * this means 24 requests to the backend!
   *
   * <p>See https://github.com/openzipkin/zipkin-java/issues/200
   */
  @Override
  public ListenableFuture<List<List<Span>>> getTraces(final QueryRequest request) {
    // Over fetch on indexes as they don't return distinct (trace id, timestamp) rows.
    final int traceIndexFetchSize = request.limit * indexFetchMultiplier;
    ListenableFuture<Map<Long, Long>> traceIdToTimestamp;
    if (request.minDuration != null || request.maxDuration != null) {
      traceIdToTimestamp = getTraceIdsByDuration(request, traceIndexFetchSize);
    } else if (request.spanName != null) {
      traceIdToTimestamp = getTraceIdsBySpanName(request.serviceName, request.spanName,
          request.endTs * 1000, request.lookback * 1000, traceIndexFetchSize);
    } else if (request.serviceName != null) {
      traceIdToTimestamp = getTraceIdsByServiceNames(Collections.singletonList(request.serviceName),
          request.endTs * 1000, request.lookback * 1000, traceIndexFetchSize);
    } else {
      checkArgument(selectTraceIdsByServiceNames != null,
          "getTraces without serviceName requires Cassandra 2.2 or later");
      traceIdToTimestamp = transform(getServiceNames(),
          new AsyncFunction<List<String>, Map<Long, Long>>() {
            @Override public ListenableFuture<Map<Long, Long>> apply(List<String> serviceNames) {
              return getTraceIdsByServiceNames(serviceNames,
                  request.endTs * 1000, request.lookback * 1000, traceIndexFetchSize);
            }
          });
    }

    List<String> annotationKeys = CassandraUtil.annotationKeys(request);

    ListenableFuture<Set<Long>> traceIds;
    if (annotationKeys.isEmpty()) {
      // Simplest case is when there is no annotation query. Limit is valid since there's no AND
      // query that could reduce the results returned to less than the limit.
      traceIds = Futures.transform(traceIdToTimestamp, CassandraUtil.keyset());
    } else {
      // While a valid port of the scala cassandra span store (from zipkin 1.35), there is a fault.
      // each annotation key is an intersection, meaning we likely return < traceIndexFetchSize.
      List<ListenableFuture<Map<Long, Long>>> futureKeySetsToIntersect = new ArrayList<>();
      futureKeySetsToIntersect.add(traceIdToTimestamp);
      for (String annotationKey : annotationKeys) {
        futureKeySetsToIntersect.add(getTraceIdsByAnnotation(annotationKey,
            request.endTs * 1000, request.lookback * 1000, traceIndexFetchSize));
      }
      // We achieve the AND goal, by intersecting each of the key sets.
      traceIds = Futures.transform(allAsList(futureKeySetsToIntersect), CassandraUtil.intersectKeySets());
    }
    return transform(traceIds, new AsyncFunction<Set<Long>, List<List<Span>>>() {
      @Override public ListenableFuture<List<List<Span>>> apply(Set<Long> traceIds) {
        traceIds = FluentIterable.from(traceIds).limit(request.limit).toSet();
        return transform(getSpansByTraceIds(traceIds, maxTraceCols), AdjustTraces.INSTANCE);
      }

      @Override public String toString() {
        return "getSpansByTraceIds";
      }
    });
  }

  static String spanName(String nullableSpanName) {
    return nullableSpanName != null ? nullableSpanName : "";
  }

  enum AdjustTraces implements Function<Collection<List<Span>>, List<List<Span>>> {
    INSTANCE;

    @Override public List<List<Span>> apply(Collection<List<Span>> unmerged) {
      List<List<Span>> result = new ArrayList<>(unmerged.size());
      for (List<Span> spans : unmerged) {
        result.add(CorrectForClockSkew.apply(MergeById.apply(spans)));
      }
      return TRACE_DESCENDING.immutableSortedCopy(result);
    }
  }

  @Override public ListenableFuture<List<Span>> getRawTrace(long traceId) {
    return transform(getSpansByTraceIds(Collections.singleton(traceId), maxTraceCols),
        new Function<Collection<List<Span>>, List<Span>>() {
          @Override public List<Span> apply(Collection<List<Span>> encodedTraces) {
            if (encodedTraces.isEmpty()) return null;
            return encodedTraces.iterator().next();
          }
        });
  }

  @Override public ListenableFuture<List<Span>> getTrace(long traceId) {
    return transform(getRawTrace(traceId), new Function<List<Span>, List<Span>>() {
      @Override public List<Span> apply(List<Span> input) {
        if (input == null || input.isEmpty()) return null;
        return ImmutableList.copyOf(CorrectForClockSkew.apply(MergeById.apply(input)));
      }
    });
  }

  @Override public ListenableFuture<List<String>> getServiceNames() {
    try {
      BoundStatement bound = CassandraUtil.bindWithName(selectServiceNames, "select-service-names");
      return transform(session.executeAsync(bound), new Function<ResultSet, List<String>>() {
            @Override public List<String> apply(ResultSet input) {
              Set<String> serviceNames = new HashSet<>();
              for (Row row : input) {
                serviceNames.add(row.getString("service_name"));
              }
              return Ordering.natural().sortedCopy(serviceNames);
            }
          }
      );
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  @Override public ListenableFuture<List<String>> getSpanNames(String serviceName) {
    if (serviceName == null || serviceName.isEmpty()) return EMPTY_LIST;
    serviceName = checkNotNull(serviceName, "serviceName").toLowerCase();
    int bucket = 0;
    try {
      BoundStatement bound = CassandraUtil.bindWithName(selectSpanNames, "select-span-names")
          .setString("service_name", serviceName)
          .setInt("bucket", bucket)
          // no one is ever going to browse so many span names
          .setInt("limit_", 1000);

      return transform(session.executeAsync(bound), new Function<ResultSet, List<String>>() {
            @Override public List<String> apply(ResultSet input) {
              Set<String> spanNames = new HashSet<>();
              for (Row row : input) {
                spanNames.add(row.getString("span_name"));
              }
              return Ordering.natural().sortedCopy(spanNames);
            }
          }
      );
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  @Override public ListenableFuture<List<DependencyLink>> getDependencies(long endTs,
      @Nullable Long lookback) {
    List<Date> days = getDays(endTs, lookback);
    try {
      BoundStatement bound = CassandraUtil.bindWithName(selectDependencies, "select-dependencies")
          .setList("days", days);
      return transform(session.executeAsync(bound), ConvertDependenciesResponse.INSTANCE);
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  enum ConvertDependenciesResponse implements Function<ResultSet, List<DependencyLink>> {
    INSTANCE;

    @Override public List<DependencyLink> apply(ResultSet rs) {
      ImmutableList.Builder<DependencyLink> unmerged = ImmutableList.builder();
      for (Row row : rs) {
        ByteBuffer encodedDayOfDependencies = row.getBytes("dependencies");
        for (DependencyLink link : Dependencies.fromThrift(encodedDayOfDependencies).links) {
          unmerged.add(link);
        }
      }
      return DependencyLinker.merge(unmerged.build());
    }
  }

  /**
   * Get the available trace information from the storage system. Spans in trace should be sorted by
   * the first annotation timestamp in that span. First event should be first in the spans list. <p>
   * The return list will contain only spans that have been found, thus the return list may not
   * match the provided list of ids.
   */
  ListenableFuture<Collection<List<Span>>> getSpansByTraceIds(Set<Long> traceIds, int limit) {
    checkNotNull(traceIds, "traceIds");
    if (traceIds.isEmpty()) {
      Collection<List<Span>> result = Collections.emptyList();
      return immediateFuture(result);
    }

    try {
      BoundStatement bound = CassandraUtil.bindWithName(selectTraces, "select-traces")
          .setSet("trace_id", traceIds)
          .setInt("limit_", limit);

      bound.setFetchSize(Integer.MAX_VALUE);

      return transform(session.executeAsync(bound),
          new Function<ResultSet, Collection<List<Span>>>() {
            @Override public Collection<List<Span>> apply(ResultSet input) {
              Map<Long, List<Span>> spans = new LinkedHashMap<>();

              for (Row row : input) {
                long traceId = row.getLong("trace_id");
                if (!spans.containsKey(traceId)) {
                  spans.put(traceId, new ArrayList<Span>());
                }
                spans.get(traceId).add(Codec.THRIFT.readSpan(row.getBytes("span")));
              }

              return spans.values();
            }
          }
      );
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  ListenableFuture<Map<Long, Long>> getTraceIdsByServiceNames(List<String> serviceNames, long endTs,
      long lookback, int limit) {
    if (serviceNames.isEmpty()) return immediateFuture(Collections.<Long, Long>emptyMap());

    long startTs = endTs - lookback;
    try {
      // This guards use of "in" query to give people a little more time to move off Cassandra 2.1
      // Note that it will still fail when serviceNames.size() > 1
      BoundStatement bound = serviceNames.size() == 1 ?
          CassandraUtil.bindWithName(selectTraceIdsByServiceName, "select-trace-ids-by-service-name")
              .setString("service_name", serviceNames.get(0))
              .setSet("bucket", buckets)
              .setBytesUnsafe("start_ts", timestampCodec.serialize(startTs))
              .setBytesUnsafe("end_ts", timestampCodec.serialize(endTs))
              .setInt("limit_", limit) :
          CassandraUtil.bindWithName(selectTraceIdsByServiceNames, "select-trace-ids-by-service-names")
              .setList("service_name", serviceNames)
              .setSet("bucket", buckets)
              .setBytesUnsafe("start_ts", timestampCodec.serialize(startTs))
              .setBytesUnsafe("end_ts", timestampCodec.serialize(endTs))
              .setInt("limit_", limit);

      bound.setFetchSize(Integer.MAX_VALUE);

      return transform(session.executeAsync(bound), traceIdToTimestamp);
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  ListenableFuture<Map<Long, Long>> getTraceIdsBySpanName(String serviceName,
      String spanName, long endTs, long lookback, int limit) {
    checkArgument(serviceName != null, "serviceName required on spanName query");
    checkArgument(spanName != null, "spanName required on spanName query");
    String serviceSpanName = serviceName + "." + spanName;
    long startTs = endTs - lookback;
    try {
      BoundStatement bound = CassandraUtil.bindWithName(selectTraceIdsBySpanName, "select-trace-ids-by-span-name")
          .setString("service_span_name", serviceSpanName)
          .setBytesUnsafe("start_ts", timestampCodec.serialize(startTs))
          .setBytesUnsafe("end_ts", timestampCodec.serialize(endTs))
          .setInt("limit_", limit);

      return transform(session.executeAsync(bound), traceIdToTimestamp);
    } catch (RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  ListenableFuture<Map<Long, Long>> getTraceIdsByAnnotation(String annotationKey,
      long endTs, long lookback, int limit) {
    long startTs = endTs - lookback;
    try {
      BoundStatement bound =
          CassandraUtil.bindWithName(selectTraceIdsByAnnotation, "select-trace-ids-by-annotation")
              .setBytes("annotation", CassandraUtil.toByteBuffer(annotationKey))
              .setSet("bucket", buckets)
              .setBytesUnsafe("start_ts", timestampCodec.serialize(startTs))
              .setBytesUnsafe("end_ts", timestampCodec.serialize(endTs))
              .setInt("limit_", limit);

      bound.setFetchSize(Integer.MAX_VALUE);

      return transform(session.executeAsync(bound), new Function<ResultSet, Map<Long, Long>>() {
            @Override public Map<Long, Long> apply(ResultSet input) {
              Map<Long, Long> traceIdsToTimestamps = new LinkedHashMap<>();
              for (Row row : input) {
                traceIdsToTimestamps.put(row.getLong("trace_id"),
                    timestampCodec.deserialize(row, "ts"));
              }
              return traceIdsToTimestamps;
            }
          }
      );
    } catch (CharacterCodingException | RuntimeException ex) {
      return immediateFailedFuture(ex);
    }
  }

  /** Returns a map of trace id to timestamp (in microseconds) */
  ListenableFuture<Map<Long, Long>> getTraceIdsByDuration(QueryRequest request,
      int indexFetchSize) {
    checkArgument(request.serviceName != null, "serviceName required on duration query");
    long oldestData = (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(durationTtl)) * 1000;

    long startTs = Math.max((request.endTs - request.lookback) * 1000, oldestData);
    long endTs = Math.max(request.endTs * 1000, oldestData);

    int startBucket = CassandraUtil.durationIndexBucket(startTs);
    int endBucket = CassandraUtil.durationIndexBucket(endTs);
    if (startBucket > endBucket) {
      throw new IllegalArgumentException(
          "Start bucket (" + startBucket + ") > end bucket (" + endBucket + ")");
    }

    List<ListenableFuture<List<DurationRow>>> futures = new ArrayList<>();
    for (int i = startBucket; i <= endBucket; i++) { // range closed
      futures.add(oneBucketDurationQuery(request, i, startTs, endTs, indexFetchSize));
    }

    return transform(allAsList(futures),
        new Function<List<List<DurationRow>>, Map<Long, Long>>() {
          @Override public Map<Long, Long> apply(List<List<DurationRow>> input) {
            // find earliest startTs for each trace ID
            Map<Long, Long> result = new LinkedHashMap<>();
            for (DurationRow row : Iterables.concat(input)) {
              Long oldValue = result.get(row.trace_id);
              if (oldValue == null || oldValue > row.timestamp) {
                result.put(row.trace_id, row.timestamp);
              }
            }
            return Collections.unmodifiableMap(result);
          }
        });
  }

  ListenableFuture<List<DurationRow>> oneBucketDurationQuery(QueryRequest request, int bucket,
      final long startTs, final long endTs, int indexFetchSize) {
    String serviceName = request.serviceName;
    String spanName = spanName(request.spanName);
    long minDuration = request.minDuration;
    long maxDuration = request.maxDuration != null ? request.maxDuration : Long.MAX_VALUE;
    BoundStatement bound =
        CassandraUtil.bindWithName(selectTraceIdsBySpanDuration, "select-trace-ids-by-span-duration")
            .setInt("time_bucket", bucket)
            .setString("service_name", serviceName)
            .setString("span_name", spanName)
            .setLong("min_duration", minDuration)
            .setLong("max_duration", maxDuration);

    // optimistically setting fetch size to 'indexFetchSize' here. Since we are likely to filter
    // some results because their timestamps are out of range, we may need to fetch again.
    // TODO figure out better strategy
    bound.setFetchSize(indexFetchSize);

    return transform(session.executeAsync(bound), new Function<ResultSet, List<DurationRow>>() {
      @Override public List<DurationRow> apply(ResultSet rs) {
        ImmutableList.Builder<DurationRow> result = ImmutableList.builder();
        for (Row input : rs) {
          DurationRow row = new DurationRow(input);
          if (row.timestamp >= startTs && row.timestamp <= endTs) {
            result.add(row);
          }
        }
        return result.build();
      }
    });
  }

  class DurationRow {
    Long trace_id;
    Long duration;
    Long timestamp; // inflated back to microseconds

    DurationRow(Row row) {
      trace_id = row.getLong("trace_id");
      duration = row.getLong("duration");
      timestamp = timestampCodec.deserialize(row, "ts");
    }

    @Override public String toString() {
      return String.format("trace_id=%d, duration=%d, timestamp=%d", trace_id, duration, timestamp);
    }
  }
}