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

package io.shardingjdbc.core.common.base;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.keygen.fixture.IncrementKeyGenerator;
import io.shardingjdbc.core.routing.fixture.PreciseOrderShardingAlgorithm;
import io.shardingjdbc.core.routing.fixture.RangeOrderShardingAlgorithm;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public abstract class AbstractShardingJDBCDatabaseAndTableTest extends AbstractSQLTest {

    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();

    private DatabaseType databaseType;

    public AbstractShardingJDBCDatabaseAndTableTest(final DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Before
    public void initShardingDataSources() throws SQLException {
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setActualTables("t_order_0, t_order_1");
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setActualTables("t_order_item_0, t_order_item_1");
            orderItemTableRuleConfig.setKeyGeneratorColumnName("item_id");
            orderItemTableRuleConfig.setKeyGeneratorClass(IncrementKeyGenerator.class.getName());
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            TableRuleConfiguration configTableRuleConfig = new TableRuleConfiguration();
            configTableRuleConfig.setLogicTable("t_config");
            shardingRuleConfig.getTableRuleConfigs().add(configTableRuleConfig);
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("user_id", PreciseOrderShardingAlgorithm.class.getName(), RangeOrderShardingAlgorithm.class.getName()));
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(
                    new StandardShardingStrategyConfiguration("order_id", PreciseOrderShardingAlgorithm.class.getName(), RangeOrderShardingAlgorithm.class.getName()));
            ShardingRule shardingRule = shardingRuleConfig.build(each.getValue());
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
    }

    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/jdbc/jdbc_0.xml",
                "integrate/dataset/jdbc/jdbc_1.xml");
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<DatabaseType> dataParameters() {
        return getDatabaseTypes();
    }

    @Override
    protected DatabaseType getCurrentDatabaseType() {
        return databaseType;
    }

    protected ShardingDataSource getShardingDataSource() {
        return shardingDataSources.get(databaseType);
    }

    @AfterClass
    public static void clear() {
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}