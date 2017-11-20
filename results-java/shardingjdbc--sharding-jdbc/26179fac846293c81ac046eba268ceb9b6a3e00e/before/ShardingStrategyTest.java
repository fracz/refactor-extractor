/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.strategy.common;

import com.dangdang.ddframe.rdb.sharding.api.PreciseShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestComplexKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestPreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestRangeShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.sharding.NoneShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.StandardShardingStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingStrategyTest {

    private final Collection<String> targets = Sets.newHashSet("1", "2", "3");

    @Test
    public void assertDoStaticShardingWithoutShardingColumns() {
        ShardingStrategy strategy = new ShardingStrategy(Sets.newHashSet("column"), new NoneShardingAlgorithm());
        assertThat(strategy.doStaticSharding(targets, Collections.<ShardingValue>emptySet()), is(targets));
    }

    @Test
    public void assertDoStaticShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(strategy.doRangeSharding(targets, new RangeShardingValue<>("logicTable", "column", Range.open("1", "3"))),
                is((Collection<String>) Lists.newArrayList("1", "2", "3")));
    }

    @Test
    public void assertDoStaticShardingForMultipleKeys() {
        ShardingStrategy strategy = new ShardingStrategy(Collections.singletonList("column"), new TestComplexKeysShardingAlgorithm());
        assertThat(strategy.doStaticSharding(targets, Collections.<ShardingValue>singletonList(new PreciseShardingValue<>("logicTable", "column", "1"))),
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }

    @Test(expected = IllegalStateException.class)
    public void assertDoDynamicShardingWithoutShardingColumns() {
        ShardingStrategy strategy = new ShardingStrategy(Sets.newHashSet("column"), null);
        strategy.doDynamicSharding(Collections.<ShardingValue>emptySet());
    }

    @Test
    public void assertDoDynamicShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(strategy.doRangeSharding(Collections.<String>emptyList(), new RangeShardingValue<>("logicTable", "column", Range.open("1", "3"))),
                is((Collection<String>) Lists.newArrayList("1", "2", "3")));
    }

    @Test
    public void assertDoDynamicShardingForMultipleKeys() {
        ShardingStrategy strategy = new ShardingStrategy(Collections.singletonList("column"), new TestComplexKeysShardingAlgorithm());
        assertThat(strategy.doDynamicSharding(Collections.<ShardingValue>singletonList(new PreciseShardingValue<>("logicTable", "column", "1"))),
                is((Collection<String>) Collections.<String>emptySet()));
    }
}