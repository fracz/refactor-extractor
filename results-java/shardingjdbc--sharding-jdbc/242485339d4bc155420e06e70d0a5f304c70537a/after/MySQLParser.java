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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;

import java.util.List;

public class MySQLParser extends SQLParser {

    public MySQLParser(final String sql, final ShardingRule shardingRule, final List<Object> parameters) {
        super( new MySQLLexer(sql), shardingRule, parameters);
        getLexer().nextToken();
    }

    public LimitContext parseLimit(final int parametersIndex, final SelectSQLContext sqlContext) {
        skipIfEqual(MySQLKeyword.LIMIT);
        int valueIndex = -1;
        int valueBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int value;
        boolean isParameterForValue = false;
        if (equalAny(Literals.INT)) {
            value = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            valueIndex = parametersIndex;
            value = (int) getParameters().get(valueIndex);
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (skipIfEqual(Symbol.COMMA)) {
            return getLimitContextWithComma(parametersIndex, sqlContext, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (skipIfEqual(MySQLKeyword.OFFSET)) {
            return getLimitContextWithOffset(parametersIndex, sqlContext, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, valueIndex);
    }

    private LimitContext getLimitContextWithComma(final int parametersIndex, final SelectSQLContext sqlContext, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int rowCountBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int rowCount;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (equalAny(Literals.INT)) {
            rowCount = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCount + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            rowCountIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            rowCount = (int) getParameters().get(rowCountIndex);
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(rowCountBeginPosition, rowCount));
        }
        if (value < 0 || rowCount < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, rowCount, valueIndex, rowCountIndex);
    }

    private LimitContext getLimitContextWithOffset(final int parametersIndex, final SelectSQLContext sqlContext, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int offsetBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int offset;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (equalAny(Literals.INT)) {
            offset = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offset + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            offset = (int) getParameters().get(offsetIndex);
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForOffset) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0 || offset < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(offset, value, offsetIndex, valueIndex);
    }
}