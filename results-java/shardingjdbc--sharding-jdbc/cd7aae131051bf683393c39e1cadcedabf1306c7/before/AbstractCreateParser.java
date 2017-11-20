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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.create;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.DDLStatement;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Create语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCreateParser implements SQLStatementParser {

    private final ShardingRule shardingRule;

    private final LexerEngine lexerEngine;

    private final TableSQLParser tableSQLParser;

    public AbstractCreateParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableSQLParser = new TableSQLParser(lexerEngine);
    }

    @Override
    public DDLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateAndKeyword());
        if (!lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
        }
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateTableAndTableName());
        DDLStatement result = new DDLStatement();
        tableSQLParser.parseSingleTable(result);
        return result;
    }

    protected abstract Keyword[] getSkippedKeywordsBetweenCreateAndKeyword();

    protected abstract Keyword[] getSkippedKeywordsBetweenCreateTableAndTableName();
}