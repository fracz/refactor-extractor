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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.parsing.mysql.MySQLPreparedStatementForOneParameterTest;
import com.dangdang.ddframe.rdb.sharding.parsing.mysql.MySQLPreparedStatementForTowParametersTest;
import com.dangdang.ddframe.rdb.sharding.parsing.mysql.MySQLStatementTest;
import com.dangdang.ddframe.rdb.sharding.parsing.mysql.OrParseTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.lexer.MySQLLexerTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.lexer.OracleLexerTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.lexer.PostgreSQLLexerTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.lexer.SQLServerLexerTest;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.DeleteStatementParserTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.InsertStatementParserTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.UpdateStatementParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LexerTest.class,
        MySQLLexerTest.class,
        OracleLexerTest.class,
        SQLServerLexerTest.class,
        PostgreSQLLexerTest.class,
        InsertStatementParserTest.class,
        UpdateStatementParserTest.class,
        DeleteStatementParserTest.class,
        MySQLStatementTest.class,
        MySQLPreparedStatementForOneParameterTest.class,
        MySQLPreparedStatementForTowParametersTest.class,
        OrParseTest.class,
        UnsupportedParseTest.class
    })
public class AllParserTests {
}