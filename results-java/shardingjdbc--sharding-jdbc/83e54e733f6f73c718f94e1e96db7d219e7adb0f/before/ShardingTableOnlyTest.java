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

package io.shardingjdbc.core.integrate.type.sharding;

import io.shardingjdbc.core.common.base.AbstractSQLAssertTest;
import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.integrate.fixture.PreciseModuloTableShardingAlgorithm;
import io.shardingjdbc.core.integrate.fixture.RangeModuloTableShardingAlgorithm;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingTableOnlyTest extends AbstractSQLAssertTest {

    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();

    public ShardingTableOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }

    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.tbl;
    }

    @Override
    protected List<String> getInitDataSetFiles() {
        return Collections.singletonList("integrate/dataset/sharding/tbl/init/tbl.xml");
    }

    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty()) {
            return shardingDataSources;
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setActualTables("t_order_${0..9}");
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setActualTables("t_order_item_${0..9}");
            orderItemTableRuleConfig.setKeyGeneratorClass("item_id");
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
            StandardShardingStrategyConfiguration tableShardingStrategyConfig = new StandardShardingStrategyConfiguration();
            tableShardingStrategyConfig.setShardingColumn("order_id");
            tableShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseModuloTableShardingAlgorithm.class.getName());
            tableShardingStrategyConfig.setRangeAlgorithmClassName(RangeModuloTableShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRuleConfig.build(each.getValue())));
        }
        return shardingDataSources;
    }

    @Before
    public void initDDLTables() throws SQLException {
        if (getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE") || getSql().startsWith("DROP")) {
            if (getSql().contains("TEMP")) {
                executeSql("CREATE TEMPORARY TABLE t_log(id int, status varchar(10))");
            } else {
                executeSql("CREATE TABLE t_log(id int, status varchar(10))");
            }
        }
    }

    @After
    public void cleanupDdlTables() throws SQLException {
        if (getSql().contains("TEMP") && DatabaseType.Oracle != getCurrentDatabaseType()) {
            return;
        }
        if (getSql().startsWith("CREATE") || getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE")) {
            executeSql("DROP TABLE t_log");
        }
    }

    private void executeSql(final String sql) throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getDataSources().entrySet()) {
            if (getCurrentDatabaseType() == each.getKey()) {
                try (Connection conn = each.getValue().getConnection();
                     Statement statement = conn.createStatement()) {
                    statement.execute(sql);
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
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