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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser.MySQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser.OracleParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.parser.PostgreSQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.parser.SQLServerParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractStatementParserTest {

    protected final SQLParserEngine getSqlStatementParser(final DatabaseType dbType, final String actualSQL) {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLParser sqlParser;
        switch (dbType) {
            case H2:
            case MySQL:
                sqlParser = new MySQLParser(actualSQL, shardingRule, parameters);
                break;
            case Oracle:
                sqlParser = new OracleParser(actualSQL, shardingRule, parameters);
                break;
            case SQLServer:
                sqlParser = new SQLServerParser(actualSQL, shardingRule, parameters);
                break;
            case PostgreSQL:
                sqlParser = new PostgreSQLParser(actualSQL, shardingRule, parameters);
                break;
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
        return new SQLParserEngine(dbType, sqlParser, shardingRule, parameters);
    }

    protected final ShardingRule createShardingRule() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", dataSource);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule tableRule = TableRule.builder("TABLE_XXX").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(dataSourceRule)
                .tableShardingStrategy(new TableShardingStrategy(Arrays.asList("field1", "field2", "field3", "field4", "field5", "field6", "field7"), new NoneTableShardingAlgorithm())).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).build();
    }
}