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
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingDatabaseAndTableDynamicTest extends AbstractSQLAssertTest {

    private static boolean isShutdown;

    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();

    public ShardingDatabaseAndTableDynamicTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
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
            DataSourceRule dataSourceRule = new DataSourceRule(each.getValue());
            TableRule orderTableRule = TableRule.builder("t_order").dynamic(true).dataSourceRule(dataSourceRule).build();
            TableRule orderItemTableRule = TableRule.builder("t_order_item").dynamic(true).dataSourceRule(dataSourceRule).build();
            ShardingRule shardingRule = ShardingRule.builder(dataSourceRule).tableRules(orderTableRule, orderItemTableRule)
                    .bindingTableRules(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule)))
                    .databaseShardingStrategy(new StandardShardingStrategy("user_id", new PreciseModuloDatabaseShardingAlgorithm(), new RangeModuloDatabaseShardingAlgorithm()))
                    .tableShardingStrategy(
                            new StandardShardingStrategy("order_id", new PreciseDynamicModuloTableShardingAlgorithm("t_order_"), new RangeDynamicModuloTableShardingAlgorithm("t_order_"))).build();
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