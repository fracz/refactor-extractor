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

package com.dangdang.ddframe.rdb.sharding.routing;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.OrderAttrShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.PreciseOrderShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.RangeOrderShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.StandardShardingStrategy;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractBaseRouteSqlTest {

    @Getter(AccessLevel.PROTECTED)
    private ShardingRule shardingRule;

    @Before
    public void setRouteRuleContext() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule orderTableRule = TableRule.builder("order").actualTables("order_0", "order_1").dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("order_item").actualTables("order_item_0", "order_item_1").dataSourceRule(dataSourceRule).build();
        TableRule orderAttrTableRule = TableRule.builder("order_attr").actualTables("ds_0.order_attr_a", "ds_1.order_attr_b").dataSourceRule(dataSourceRule)
                .tableShardingStrategy(new StandardShardingStrategy("order_id", new OrderAttrShardingAlgorithm())).build();
        shardingRule = ShardingRule.builder(dataSourceRule).tableRules(orderTableRule, orderItemTableRule, orderAttrTableRule)
                .bindingTableRules(new BindingTableRule(orderTableRule, orderItemTableRule))
                .databaseShardingStrategy(new StandardShardingStrategy("order_id", new PreciseOrderShardingAlgorithm(), new RangeOrderShardingAlgorithm()))
                .tableShardingStrategy(new StandardShardingStrategy("order_id", new PreciseOrderShardingAlgorithm(), new RangeOrderShardingAlgorithm())).build();
    }

    protected void assertSingleTargetWithoutParameter(final String originSql, final String targetDataSource, final String targetSQL) {
        assertMultipleTargetsWithoutParameter(originSql, 1, Collections.singletonList(targetDataSource), Collections.singletonList(targetSQL));
    }

    protected void assertMultipleTargetsWithoutParameter(final String originSql, final int expectedSize, final Collection<String> targetDataSources, final Collection<String> targetSQLs) {
        ShardingContext shardingContext = new ShardingContext(getShardingRule(), DatabaseType.MySQL, null, false);
        SQLRouteResult actual = new StatementRoutingEngine(shardingContext).route(originSql);
        assertThat(actual.getExecutionUnits().size(), is(expectedSize));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {

            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getDataSource();
            }
        }));
        assertThat(actualDataSources, hasItems(targetDataSources.toArray(new String[targetDataSources.size()])));
        Collection<String> actualSQLs = Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {

            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getSql();
            }
        });
        assertThat(actualSQLs, hasItems(targetSQLs.toArray(new String[targetSQLs.size()])));
    }

    protected void assertSingleTargetWithParameters(final String originSql, final List<Object> parameters, final String targetDataSource, final String targetSQL) {
        assertMultipleTargetsWithParameters(originSql, parameters, 1, Collections.singletonList(targetDataSource), Collections.singletonList(targetSQL));
    }

    protected void assertMultipleTargetsWithParameters(
            final String originSql, final List<Object> parameters, final int expectedSize, final Collection<String> targetDataSources, final Collection<String> targetSQLs) {
        ShardingContext shardingContext = new ShardingContext(getShardingRule(), DatabaseType.MySQL, null, false);
        SQLRouteResult actual = new PreparedStatementRoutingEngine(originSql, shardingContext).route(parameters);
        assertThat(actual.getExecutionUnits().size(), is(expectedSize));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {

            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getDataSource();
            }
        }));
        assertThat(actualDataSources, hasItems(targetDataSources.toArray(new String[targetDataSources.size()])));
        Collection<String> actualSQLs = Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {

            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getSql();
            }
        });
        assertThat(actualSQLs, hasItems(targetSQLs.toArray(new String[targetSQLs.size()])));
    }
}