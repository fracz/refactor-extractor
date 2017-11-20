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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLOrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AbstractOrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.GroupBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectListSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;

/**
 * SQLServer Select语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerSelectParser extends AbstractSelectParser {

    private final DistinctSQLParser distinctSQLParser;

    private final SelectListSQLParser selectListSQLParser;

    private final GroupBySQLParser groupBySQLParser;

    private final AbstractOrderBySQLParser orderBySQLParser;

    public SQLServerSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new SQLServerWhereSQLParser(lexerEngine));
        distinctSQLParser = new DistinctSQLParser(lexerEngine);
        selectListSQLParser = new SQLServerSelectListSQLParser(shardingRule, lexerEngine);
        groupBySQLParser = new GroupBySQLParser(lexerEngine);
        orderBySQLParser = new MySQLOrderBySQLParser(lexerEngine);
    }

    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        distinctSQLParser.parse();
        parseTop(selectStatement);
        selectListSQLParser.parse(selectStatement, getItems());
        parseFrom(selectStatement);
        parseWhere(selectStatement);
        groupBySQLParser.parse(selectStatement);
        parseHaving();
        orderBySQLParser.parse(selectStatement);
        parseOffset(selectStatement);
        parseFor();
    }

    private void parseTop(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(SQLServerKeyword.TOP)) {
            return;
        }
        int beginPosition = getLexerEngine().getCurrentToken().getEndPosition();
        if (!getLexerEngine().skipIfEqual(Symbol.LEFT_PAREN)) {
            beginPosition = getLexerEngine().getCurrentToken().getEndPosition() - getLexerEngine().getCurrentToken().getLiterals().length();
        }
        SQLExpression sqlExpression = getExpressionSQLParser().parse(selectStatement);
        getLexerEngine().skipIfEqual(Symbol.RIGHT_PAREN);
        LimitValue rowCountValue;
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            rowCountValue = new LimitValue(rowCount, -1);
            selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else {
            throw new SQLParsingException(getLexerEngine());
        }
        if (getLexerEngine().equalAny(SQLServerKeyword.PERCENT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PERCENT);
        }
        getLexerEngine().skipIfEqual(DefaultKeyword.WITH, SQLServerKeyword.TIES);
        if (null == selectStatement.getLimit()) {
            Limit limit = new Limit(false);
            limit.setRowCount(rowCountValue);
            selectStatement.setLimit(limit);
        } else {
            selectStatement.getLimit().setRowCount(rowCountValue);
        }
    }

    private void parseOffset(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(SQLServerKeyword.OFFSET)) {
            return;
        }
        int offsetValue = -1;
        int offsetIndex = -1;
        if (getLexerEngine().equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getLexerEngine().getCurrentToken().getLiterals());
        } else if (getLexerEngine().equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            selectStatement.increaseParametersIndex();
        } else {
            throw new SQLParsingException(getLexerEngine());
        }
        getLexerEngine().nextToken();
        Limit limit = new Limit(true);
        if (getLexerEngine().skipIfEqual(DefaultKeyword.FETCH)) {
            getLexerEngine().nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            getLexerEngine().nextToken();
            if (getLexerEngine().equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(getLexerEngine().getCurrentToken().getLiterals());
            } else if (getLexerEngine().equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                selectStatement.increaseParametersIndex();
            } else {
                throw new SQLParsingException(getLexerEngine());
            }
            getLexerEngine().nextToken();
            getLexerEngine().nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }

    private void parseFor() {
        if (!getLexerEngine().skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        if (getLexerEngine().equalAny(SQLServerKeyword.BROWSE)) {
            getLexerEngine().nextToken();
        } else if (getLexerEngine().skipIfEqual(SQLServerKeyword.XML)) {
            while (true) {
                if (getLexerEngine().equalAny(SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.XMLSCHEMA)) {
                    getLexerEngine().nextToken();
                } else if (getLexerEngine().skipIfEqual(SQLServerKeyword.ELEMENTS)) {
                    getLexerEngine().skipIfEqual(SQLServerKeyword.XSINIL);
                } else {
                    break;
                }
                if (getLexerEngine().equalAny(Symbol.COMMA)) {
                    getLexerEngine().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new SQLParsingUnsupportedException(getLexerEngine().getCurrentToken().getType());
        }
    }

    @Override
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.WITH)) {
            getLexerEngine().skipParentheses(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }
}