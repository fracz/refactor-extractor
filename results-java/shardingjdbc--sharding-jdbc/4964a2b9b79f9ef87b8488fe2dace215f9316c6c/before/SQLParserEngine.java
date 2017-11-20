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

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.delete.SQLDeleteParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.insert.SQLInsertParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.select.SQLSelectParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.update.SQLUpdateParserFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * SQL解析引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParserEngine {

    private final DatabaseType dbType;

    private final SQLParser exprParser;

    private final ShardingRule shardingRule;

    private final List<Object> parameters;

    /**
     * 解析SQL.
     *
     * @return SQL解析对象
     */
    public SQLContext parseStatement() {
        exprParser.skipIfEqual(Symbol.SEMI);
        if (exprParser.equalAny(DefaultKeyword.WITH)) {
            skipWith();
        }
        if (exprParser.equalAny(DefaultKeyword.SELECT)) {
            return SQLSelectParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.equalAny(DefaultKeyword.INSERT)) {
            return SQLInsertParserFactory.newInstance(shardingRule, parameters, exprParser, dbType).parse();
        }
        if (exprParser.equalAny(DefaultKeyword.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.equalAny(DefaultKeyword.DELETE)) {
            return SQLDeleteParserFactory.newInstance(exprParser, dbType).parse();
        }
        throw new ParserUnsupportedException(exprParser.getLexer().getCurrentToken().getType());
    }

    private void skipWith() {
        exprParser.getLexer().nextToken();
        do {
            exprParser.skipUntil(DefaultKeyword.AS);
            exprParser.accept(DefaultKeyword.AS);
            exprParser.skipParentheses();
        } while (exprParser.skipIfEqual(Symbol.COMMA));
    }
}