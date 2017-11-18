/*
 * Druid - a distributed column store.
 * Copyright (C) 2012, 2013  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.druid.query.ingestmetadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.druid.common.utils.JodaUtils;
import io.druid.query.BaseQuery;
import io.druid.query.DataSource;
import io.druid.query.Query;
import io.druid.query.Result;
import io.druid.query.spec.MultipleIntervalSegmentSpec;
import io.druid.query.spec.QuerySegmentSpec;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 */
public class IngestMetadataQuery extends BaseQuery<Result<IngestMetadataResultValue>>
{
  public static final Interval MY_Y2K_INTERVAL = new Interval(
      new DateTime("0000-01-01"),
      new DateTime("3000-01-01")
  );

  public static String MAX_INGESTED_EVENT_TIME = "maxIngestedEventTime";


  @JsonCreator
  public IngestMetadataQuery(
      @JsonProperty("dataSource") DataSource dataSource,
      @JsonProperty("intervals") QuerySegmentSpec querySegmentSpec,
      @JsonProperty("context") Map<String, Object> context
  )
  {
    super(
        dataSource,
        (querySegmentSpec == null) ? new MultipleIntervalSegmentSpec(Arrays.asList(MY_Y2K_INTERVAL))
                                   : querySegmentSpec,
        context
    );
  }

  @Override
  public boolean hasFilters()
  {
    return false;
  }

  @Override
  public String getType()
  {
    return Query.INGEST_METADATA;
  }

  @Override
  public IngestMetadataQuery withOverriddenContext(Map<String, Object> contextOverrides)
  {
    return new IngestMetadataQuery(
        getDataSource(),
        getQuerySegmentSpec(),
        computeOverridenContext(contextOverrides)
    );
  }

  @Override
  public IngestMetadataQuery withQuerySegmentSpec(QuerySegmentSpec spec)
  {
    return new IngestMetadataQuery(
        getDataSource(),
        spec,
        getContext()
    );
  }

  @Override
  public Query<Result<IngestMetadataResultValue>> withDataSource(DataSource dataSource)
  {
    return new IngestMetadataQuery(
        dataSource,
        getQuerySegmentSpec(),
        getContext()
    );
  }

  public Iterable<Result<IngestMetadataResultValue>> buildResult(DateTime timestamp, DateTime maxIngestedEventTime)
  {
    List<Result<IngestMetadataResultValue>> results = Lists.newArrayList();
    Map<String, Object> result = Maps.newHashMap();

    if (maxIngestedEventTime != null) {
      result.put(MAX_INGESTED_EVENT_TIME, maxIngestedEventTime);
    }
    if (!result.isEmpty()) {
      results.add(new Result<>(timestamp, new IngestMetadataResultValue(result)));
    }

    return results;
  }

  public Iterable<Result<IngestMetadataResultValue>> mergeResults(List<Result<IngestMetadataResultValue>> results)
  {
    if (results == null || results.isEmpty()) {
      return Lists.newArrayList();
    }

    DateTime max = new DateTime(JodaUtils.MIN_INSTANT);
    for (Result<IngestMetadataResultValue> result : results) {
      DateTime currMaxIngestedEventTime = result.getValue().getMaxIngestedEventTime();
      if (currMaxIngestedEventTime != null && currMaxIngestedEventTime.isAfter(max)) {
        max = currMaxIngestedEventTime;
      }
    }

    return buildResult(max, max);
  }

  @Override
  public String toString()
  {
    return "IngestMetadataQuery{" +
           "dataSource='" + getDataSource() + '\'' +
           ", querySegmentSpec=" + getQuerySegmentSpec() +
           ", duration=" + getDuration() +
           '}';
  }

}