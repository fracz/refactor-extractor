/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.type.sharding;

import com.dangdang.ddframe.rdb.common.base.AbstractSQLAssertTest;
import com.dangdang.ddframe.rdb.common.env.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.StandardShardingStrategy;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingDatabaseAndTableTest extends AbstractSQLAssertTest {

    private static boolean isShutdown;

    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();

    public ShardingDatabaseAndTableTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }

    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.dbtbl;
    }

    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/dbtbl/init/dbtbl_0.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_1.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_2.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_3.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_4.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_5.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_6.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_7.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_8.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_9.xml");
    }

    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            DataSourceRule dataSourceRule = new DataSourceRule(each.getValue(), "dataSource_dbtbl_0");
            TableRule orderTableRule = TableRule.builder("t_order").actualTables(Arrays.asList(
                    "t_order_0",
                    "t_order_1",
                    "t_order_2",
                    "t_order_3",
                    "t_order_4",
                    "t_order_5",
                    "t_order_6",
                    "t_order_7",
                    "t_order_8",
                    "t_order_9")).dataSourceRule(dataSourceRule).build();
            TableRule orderItemTableRule = TableRule.builder("t_order_item").actualTables(Arrays.asList(
                    "t_order_item_0",
                    "t_order_item_1",
                    "t_order_item_2",
                    "t_order_item_3",
                    "t_order_item_4",
                    "t_order_item_5",
                    "t_order_item_6",
                    "t_order_item_7",
                    "t_order_item_8",
                    "t_order_item_9")).dataSourceRule(dataSourceRule).build();
            TableRule configRule = TableRule.builder("t_config").dataSourceRule(dataSourceRule).build();
            ShardingRule shardingRule = ShardingRule.builder(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule, configRule))
                    .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                    .databaseShardingStrategy(new StandardShardingStrategy("user_id", new PreciseModuloDatabaseShardingAlgorithm(), new RangeModuloDatabaseShardingAlgorithm()))
                    .tableShardingStrategy(new StandardShardingStrategy("order_id", new PreciseModuloTableShardingAlgorithm(), new RangeModuloTableShardingAlgorithm())).build();
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
        return shardingDataSources;
    }

    @AfterClass
    public static void clear() {
        isShutdown = true;
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}