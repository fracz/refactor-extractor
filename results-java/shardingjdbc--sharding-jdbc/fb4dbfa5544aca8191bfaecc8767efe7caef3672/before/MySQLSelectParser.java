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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AbstractOrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.GroupBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.HavingSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectListSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectRestSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * MySQL Select语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLSelectParser extends AbstractSelectParser {

    private final DistinctSQLParser distinctSQLParser;

    private final SelectListSQLParser selectListSQLParser;

    private final WhereSQLParser whereSQLParser;

    private final GroupBySQLParser groupBySQLParser;

    private final HavingSQLParser havingSQLParser;

    private final AbstractOrderBySQLParser orderBySQLParser;

    private final MySQLLimitSQLParser limitSQLParser;

    private final SelectRestSQLParser selectRestSQLParser;

    public MySQLSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
        distinctSQLParser = new MySQLDistinctSQLParser(lexerEngine);
        selectListSQLParser = new SelectListSQLParser(shardingRule, lexerEngine);
        whereSQLParser = new WhereSQLParser(lexerEngine);
        groupBySQLParser = new MySQLGroupBySQLParser(lexerEngine);
        havingSQLParser = new HavingSQLParser(lexerEngine);
        orderBySQLParser = new MySQLOrderBySQLParser(lexerEngine);
        limitSQLParser = new MySQLLimitSQLParser(lexerEngine);
        selectRestSQLParser = new MySQLSelectRestSQLParser(lexerEngine);
    }

    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        distinctSQLParser.parse();
        skipBeforeSelectList();
        selectListSQLParser.parse(selectStatement, getItems());
        parseFrom(selectStatement);
        whereSQLParser.parse(getShardingRule(), selectStatement, getItems());
        groupBySQLParser.parse(selectStatement);
        havingSQLParser.parse();
        orderBySQLParser.parse(selectStatement);
        limitSQLParser.parse(selectStatement);
        selectRestSQLParser.parse();
    }

    private void skipBeforeSelectList() {
        getLexerEngine().skipAll(MySQLKeyword.HIGH_PRIORITY, DefaultKeyword.STRAIGHT_JOIN, MySQLKeyword.SQL_SMALL_RESULT, MySQLKeyword.SQL_BIG_RESULT, MySQLKeyword.SQL_BUFFER_RESULT,
                MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_CALC_FOUND_ROWS);
    }

    @Override
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (getLexerEngine().equalAny(DefaultKeyword.USING)) {
            return;
        }
        if (getLexerEngine().equalAny(DefaultKeyword.USE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getLexerEngine().equalAny(OracleKeyword.IGNORE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getLexerEngine().equalAny(OracleKeyword.FORCE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }

    private void skipIndexHint(final SelectStatement selectStatement) {
        if (getLexerEngine().equalAny(DefaultKeyword.INDEX)) {
            getLexerEngine().nextToken();
        } else {
            getLexerEngine().accept(DefaultKeyword.KEY);
        }
        if (getLexerEngine().equalAny(DefaultKeyword.FOR)) {
            getLexerEngine().nextToken();
            if (getLexerEngine().equalAny(DefaultKeyword.JOIN)) {
                getLexerEngine().nextToken();
            } else if (getLexerEngine().equalAny(DefaultKeyword.ORDER)) {
                getLexerEngine().nextToken();
                getLexerEngine().accept(DefaultKeyword.BY);
            } else {
                getLexerEngine().accept(DefaultKeyword.GROUP);
                getLexerEngine().accept(DefaultKeyword.BY);
            }
        }
        getLexerEngine().skipParentheses(selectStatement);
    }
}