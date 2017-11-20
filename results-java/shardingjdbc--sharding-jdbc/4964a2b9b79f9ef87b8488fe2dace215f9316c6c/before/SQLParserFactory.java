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

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser.MySQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser.OracleParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.parser.PostgreSQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.parser.SQLServerParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLParserEngine;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SQL解析器工厂.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SQLParserFactory {

    /**
     * 创建解析器引擎对象.
     *
     * @param databaseType 数据库类型
     * @param sql SQL语句
     * @param parameters SQL中参数的值
     * @param shardingRule 分片规则
     * @return 解析器引擎对象
     * @throws SQLParserException SQL解析异常
     */
    public static SQLParseEngine create(final DatabaseType databaseType, final String sql, final List<Object> parameters, final ShardingRule shardingRule) throws SQLParserException {
        log.debug("Logic SQL: {}, {}", sql, parameters);
        SQLContext sqlContext = getSQLParserEngine(databaseType, sql, shardingRule, parameters).parseStatement();
        log.trace("Get {} SQL Statement", sqlContext.getClass().getName());
        return new SQLParseEngine(sqlContext);
    }

    private static SQLParserEngine getSQLParserEngine(final DatabaseType dbType, final String sql, final ShardingRule shardingRule, final List<Object> parameters) {
        SQLParser sqlParser;
        switch (dbType) {
            case H2:
            case MySQL:
                sqlParser = new MySQLParser(sql, shardingRule, parameters);
                break;
            case Oracle:
                sqlParser = new OracleParser(sql, shardingRule, parameters);
                break;
            case SQLServer:
                sqlParser = new SQLServerParser(sql, shardingRule, parameters);
                break;
            case PostgreSQL:
                sqlParser = new PostgreSQLParser(sql, shardingRule, parameters);
                break;
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
        return new SQLParserEngine(dbType, sqlParser, shardingRule, parameters);
    }
}