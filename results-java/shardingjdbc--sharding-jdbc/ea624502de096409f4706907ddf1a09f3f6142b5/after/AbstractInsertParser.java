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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.insert;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.ExpressionSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.GeneratedKeyToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.MultipleInsertValuesToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractInsertParser implements SQLStatementParser {

    @Getter(AccessLevel.PROTECTED)
    private final ShardingRule shardingRule;

    @Getter(AccessLevel.PROTECTED)
    private final CommonParser commonParser;

    @Getter(AccessLevel.PROTECTED)
    private final AbstractSQLParser sqlParser;

    private final ExpressionSQLParser expressionSQLParser;

    private final TableSQLParser tableSQLParser;

    private int columnsListLastPosition;

    private int afterValuesPosition;

    private int valuesListLastPosition;

    private int generateKeyColumnIndex = -1;

    public AbstractInsertParser(final ShardingRule shardingRule, final CommonParser commonParser, final AbstractSQLParser sqlParser) {
        this.shardingRule = shardingRule;
        this.commonParser = commonParser;
        this.sqlParser = sqlParser;
        expressionSQLParser = new ExpressionSQLParser(commonParser);
        tableSQLParser = new TableSQLParser(commonParser);
    }

    @Override
    public final DMLStatement parse() {
        commonParser.getLexer().nextToken();
        InsertStatement result = new InsertStatement();
        parseInto(result);
        parseColumns(result);
        if (commonParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot INSERT SELECT");
        }
        Collection<Keyword> valueKeywords = new LinkedList<>();
        valueKeywords.add(DefaultKeyword.VALUES);
        valueKeywords.addAll(Arrays.asList(getSynonymousKeywordsForValues()));
        if (commonParser.skipIfEqual(valueKeywords.toArray(new Keyword[valueKeywords.size()]))) {
            afterValuesPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
            parseValues(result);
            if (commonParser.equalAny(Symbol.COMMA)) {
                parseMultipleValues(result);
            }
        } else if (commonParser.skipIfEqual(getCustomizedInsertKeywords())) {
            parseCustomizedInsert(result);
        }
        appendGenerateKey(result);
        return result;
    }

    private void parseInto(final InsertStatement insertStatement) {
        if (commonParser.equalAny(getUnsupportedKeywordsBeforeInto())) {
            throw new SQLParsingUnsupportedException(commonParser.getLexer().getCurrentToken().getType());
        }
        commonParser.skipUntil(DefaultKeyword.INTO);
        commonParser.getLexer().nextToken();
        tableSQLParser.parseSingleTable(insertStatement);
        skipBetweenTableAndValues(insertStatement);
    }

    protected Keyword[] getUnsupportedKeywordsBeforeInto() {
        return new Keyword[0];
    }

    private void skipBetweenTableAndValues(final InsertStatement insertStatement) {
        while (commonParser.skipIfEqual(getSkippedKeywordsBetweenTableAndValues())) {
            commonParser.getLexer().nextToken();
            if (commonParser.equalAny(Symbol.LEFT_PAREN)) {
                commonParser.skipParentheses(insertStatement);
            }
        }
    }

    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[0];
    }

    private void parseColumns(final InsertStatement insertStatement) {
        Collection<Column> result = new LinkedList<>();
        if (commonParser.equalAny(Symbol.LEFT_PAREN)) {
            String tableName = insertStatement.getTables().getSingleTableName();
            Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
            int count = 0;
            do {
                commonParser.getLexer().nextToken();
                String columnName = SQLUtil.getExactlyValue(commonParser.getLexer().getCurrentToken().getLiterals());
                result.add(new Column(columnName, tableName));
                commonParser.getLexer().nextToken();
                if (generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName)) {
                    generateKeyColumnIndex = count;
                }
                count++;
            } while (!commonParser.equalAny(Symbol.RIGHT_PAREN) && !commonParser.equalAny(Assist.END));
            columnsListLastPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
            commonParser.getLexer().nextToken();
        }
        insertStatement.getColumns().addAll(result);
    }

    protected Keyword[] getSynonymousKeywordsForValues() {
        return new Keyword[0];
    }

    private void parseValues(final InsertStatement insertStatement) {
        commonParser.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        do {
            sqlExpressions.add(expressionSQLParser.parse(insertStatement));
        } while (commonParser.skipIfEqual(Symbol.COMMA));
        valuesListLastPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
        int count = 0;
        for (Column each : insertStatement.getColumns()) {
            SQLExpression sqlExpression = sqlExpressions.get(count);
            insertStatement.getConditions().add(new Condition(each, sqlExpression), shardingRule);
            if (generateKeyColumnIndex == count) {
                insertStatement.setGeneratedKey(createGeneratedKey(each, sqlExpression));
            }
            count++;
        }
        commonParser.accept(Symbol.RIGHT_PAREN);
    }

    private GeneratedKey createGeneratedKey(final Column column, final SQLExpression sqlExpression) {
        GeneratedKey result;
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            result = new GeneratedKey(column.getName(), ((SQLPlaceholderExpression) sqlExpression).getIndex(), null);
        } else if (sqlExpression instanceof SQLNumberExpression) {
            result = new GeneratedKey(column.getName(), -1, ((SQLNumberExpression) sqlExpression).getNumber());
        } else {
            throw new ShardingJdbcException("Generated key only support number.");
        }
        return result;
    }

    private void parseMultipleValues(final InsertStatement insertStatement) {
        insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
        MultipleInsertValuesToken valuesToken = new MultipleInsertValuesToken(afterValuesPosition);
        valuesToken.getValues().add(
                commonParser.getLexer().getInput().substring(afterValuesPosition, commonParser.getLexer().getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length()));
        while (commonParser.skipIfEqual(Symbol.COMMA)) {
            int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
            parseValues(insertStatement);
            insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
            int endPosition = commonParser.equalAny(Symbol.COMMA)
                    ? commonParser.getLexer().getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length() : commonParser.getLexer().getCurrentToken().getEndPosition();
            valuesToken.getValues().add(commonParser.getLexer().getInput().substring(beginPosition, endPosition));
        }
        insertStatement.getSqlTokens().add(valuesToken);
    }

    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[0];
    }

    protected void parseCustomizedInsert(final InsertStatement insertStatement) {
    }

    private void appendGenerateKey(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
        if (!generateKeyColumn.isPresent() || null != insertStatement.getGeneratedKey()) {
            return;
        }
        ItemsToken columnsToken = new ItemsToken(columnsListLastPosition);
        columnsToken.getItems().add(generateKeyColumn.get());
        insertStatement.getSqlTokens().add(columnsToken);
        insertStatement.getSqlTokens().add(new GeneratedKeyToken(valuesListLastPosition));
    }
}