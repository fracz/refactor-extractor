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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.HavingSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.OrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectRestSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * Oracle Select语句解析器.
 *
 * @author zhangliang
 */
public final class OracleSelectParser extends AbstractSelectParser {

    private final OracleHierarchicalQueryClauseParser hierarchicalQueryClauseParser;

    private final HavingSQLParser havingSQLParser;

    private final OracleModelClauseParser modelClauseParser;

    private final OrderBySQLParser orderBySQLParser;

    private final OracleForParser forParser;

    private final SelectRestSQLParser selectRestSQLParser;

    public OracleSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine,
                new OracleDistinctSQLParser(lexerEngine), new OracleSelectListSQLParser(shardingRule, lexerEngine), new OracleTableSQLParser(shardingRule, lexerEngine),
                new OracleWhereSQLParser(lexerEngine), new OracleGroupBySQLParser(lexerEngine));
        hierarchicalQueryClauseParser = new OracleHierarchicalQueryClauseParser(shardingRule, lexerEngine);
        havingSQLParser = new HavingSQLParser(lexerEngine);
        modelClauseParser = new OracleModelClauseParser(lexerEngine);
        orderBySQLParser = new OracleOrderBySQLParser(lexerEngine);
        forParser = new OracleForParser(lexerEngine);
        selectRestSQLParser = new SelectRestSQLParser(lexerEngine);
    }

    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        parseSelectList(selectStatement, getItems());
        parseFrom(selectStatement);
        parseWhere(getShardingRule(), selectStatement, getItems());
        hierarchicalQueryClauseParser.parse(selectStatement);
        parseGroupBy(selectStatement);
        havingSQLParser.parse();
        modelClauseParser.parse(selectStatement);
        orderBySQLParser.parse(selectStatement);
        forParser.parse(selectStatement);
        selectRestSQLParser.parse();
    }
}