/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.elasticsearch.index.cache.filter;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.cache.filter.weighted.WeightedFilterCache;
import org.elasticsearch.index.settings.IndexSettings;
import org.elasticsearch.index.shard.AbstractIndexShardComponent;
import org.elasticsearch.index.shard.ShardId;

/**
 */
public class ShardFilterCache extends AbstractIndexShardComponent {

    final CounterMetric evictionsMetric = new CounterMetric();
    final CounterMetric totalMetric = new CounterMetric();

    @Inject
    public ShardFilterCache(ShardId shardId, @IndexSettings Settings indexSettings) {
        super(shardId, indexSettings);
    }

    public FilterCacheStats stats() {
        return new FilterCacheStats(totalMetric.count(), evictionsMetric.count());
    }

    public void onCached(WeightedFilterCache.FilterCacheKey cacheKey, long sizeInBytes) {
        totalMetric.inc(sizeInBytes);
    }

    public void onRemoval(WeightedFilterCache.FilterCacheKey cacheKey, boolean evicted, long sizeInBytes) {
        if (evicted) {
            evictionsMetric.inc();
        }
        totalMetric.dec(sizeInBytes);
    }
}