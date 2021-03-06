/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class SQLStatementParser extends SQLParser {

    private final ShardingRule shardingRule;

    private final List<Object> parameters;

    private final SQLExprParser exprParser;

    public SQLStatementParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.exprParser = exprParser;
    }

    /**
     * 解析SQL.
     *
     * @return SQL解析对象
     */
    public SQLStatement parseStatement() {
        if (getLexer().equalToken(Token.SEMI)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.WITH)) {
            parseWith();
        }
        if (getLexer().equalToken(Token.SELECT)) {
            return new SQLSelectStatement(createSQLSelectParser(shardingRule, parameters).select(), getDbType());
        }
        if (getLexer().equalToken(Token.INSERT)) {
            return SQLInsertParserFactory.newInstance(shardingRule, parameters, exprParser, getDbType()).parse();
        }
        if (getLexer().equalToken(Token.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(shardingRule, parameters, exprParser, getDbType()).parse();
        }
        if (getLexer().equalToken(Token.DELETE)) {
            return SQLDeleteParserFactory.newInstance(shardingRule, parameters, exprParser, getDbType()).parse();
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    private void parseWith() {
        getLexer().nextToken();
        do {
            parseWithQuery();
            if (getLexer().equalToken(Token.EOF)) {
                return;
            }
        } while (getLexer().equalToken(Token.COMMA));
    }

    private void parseWithQuery() {
        while (!getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EOF)) {
                return;
            }
        }
        accept(Token.AS);
        accept(Token.LEFT_PAREN);
        while (!getLexer().equalToken(Token.RIGHT_PAREN)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EOF)) {
                return;
            }
        }
        accept(Token.RIGHT_PAREN);
    }

    protected abstract SQLSelectParser createSQLSelectParser(final ShardingRule shardingRule, final List<Object> parameters);
}