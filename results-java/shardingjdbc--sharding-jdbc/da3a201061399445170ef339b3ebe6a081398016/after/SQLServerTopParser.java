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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.ExpressionClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;

/**
 * SQLServer Top解析器.
 *
 * @author zhangliang
 */
public final class SQLServerTopParser implements SQLClauseParser {

    private final LexerEngine lexerEngine;

    private final ExpressionClauseParser expressionClauseParser;

    public SQLServerTopParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }

    /**
     * 解析TOP.
     *
     * @param selectStatement Select SQL语句对象
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(SQLServerKeyword.TOP)) {
            return;
        }
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        if (!lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
            beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        }
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
        LimitValue rowCountValue;
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            rowCountValue = new LimitValue(rowCount, -1);
            selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        if (lexerEngine.equalAny(SQLServerKeyword.PERCENT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PERCENT);
        }
        lexerEngine.skipIfEqual(DefaultKeyword.WITH, SQLServerKeyword.TIES);
        if (null == selectStatement.getLimit()) {
            Limit limit = new Limit(false);
            limit.setRowCount(rowCountValue);
            selectStatement.setLimit(limit);
        } else {
            selectStatement.getLimit().setRowCount(rowCountValue);
        }
    }
}