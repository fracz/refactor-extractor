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

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ResultSetMergeContextTest {

    @Test
    public void assertNewResultSetMergeContext() throws SQLException {
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(
                Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col", "count_col", "avg_col", "sharding_gen_1", "sharding_gen_2")))), createSQLStatement());
        assertThat(actual.getSqlStatement().getOrderByList().get(0).getIndex(), is(1));
        assertThat(actual.getSqlStatement().getGroupByList().get(0).getIndex(), is(2));
        assertThat(actual.getSqlStatement().getAggregationSelectItems().get(0).getIndex(), is(3));
        assertThat(actual.getSqlStatement().getAggregationSelectItems().get(1).getIndex(), is(4));
        assertThat(actual.getSqlStatement().getAggregationSelectItems().get(1).getDerivedAggregationSelectItems().get(0).getIndex(), is(5));
        assertThat(actual.getSqlStatement().getAggregationSelectItems().get(1).getDerivedAggregationSelectItems().get(1).getIndex(), is(6));
    }

    private SQLStatement createSQLStatement() {
        SelectStatement result = new SelectStatement();
        result.getOrderByList().add(new OrderItem("order_col", OrderType.ASC, Optional.<String>absent()));
        result.getGroupByList().add(new OrderItem("group_col", OrderType.ASC, Optional.<String>absent()));
        result.getItems().add(MergerTestUtil.createAggregationColumn(AggregationType.COUNT, "count_col", "count_col", 3, -1, -1));
        result.getItems().add(MergerTestUtil.createAggregationColumn(AggregationType.AVG, "avg_col", "avg_col", 4, 5, 6));
        return result;
    }
}