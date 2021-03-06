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

package org.elasticsearch.action.admin.indices.stats;

import com.google.common.collect.Iterators;
import org.elasticsearch.index.shard.ShardId;

import java.util.Iterator;

/**
 */
public class IndexShardStats implements Iterable<ShardStats> {

    private final ShardId shardId;

    private final ShardStats[] shards;

    IndexShardStats(ShardId shardId, ShardStats[] shards) {
        this.shardId = shardId;
        this.shards = shards;
    }

    public ShardId shardId() {
        return this.shardId;
    }

    public ShardId getShardId() {
        return shardId();
    }

    public ShardStats[] shards() {
        return shards;
    }

    public ShardStats[] getShards() {
        return shards;
    }

    public ShardStats getAt(int position) {
        return shards[position];
    }

    @Override
    public Iterator<ShardStats> iterator() {
        return Iterators.forArray(shards);
    }

    private CommonStats total = null;

    public CommonStats total() {
        if (total != null) {
            return total;
        }
        CommonStats stats = new CommonStats();
        for (ShardStats shard : shards) {
            stats.add(shard.stats());
        }
        total = stats;
        return stats;
    }

    private CommonStats primary = null;

    public CommonStats primary() {
        if (primary != null) {
            return primary;
        }
        CommonStats stats = new CommonStats();
        for (ShardStats shard : shards) {
            if (shard.shardRouting().primary()) {
                stats.add(shard.stats());
            }
        }
        primary = stats;
        return stats;
    }
}