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

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLCharExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIgnoreExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.insert.AbstractInsertParser;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {

    public MySQLInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }

    @Override
    protected void parseCustomizedInsert() {
        parseInsertSet();
    }

    private void parseInsertSet() {
        Collection<String> autoIncrementColumns = getShardingRule().getAutoIncrementColumns(getSqlContext().getTables().get(0).getName());
        ConditionContext conditionContext = new ConditionContext();
        do {
            getExprParser().getLexer().nextToken();
            Condition.Column column = getColumn(autoIncrementColumns);
            getExprParser().getLexer().nextToken();
            getExprParser().accept(Symbol.EQ);
            SQLExpr sqlExpr;
            if (getExprParser().equalAny(Literals.INT)) {
                sqlExpr = new SQLNumberExpr(Integer.parseInt(getExprParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getExprParser().equalAny(Literals.FLOAT)) {
                sqlExpr = new SQLNumberExpr(Double.parseDouble(getExprParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getExprParser().equalAny(Literals.CHARS)) {
                sqlExpr = new SQLCharExpr(getExprParser().getLexer().getCurrentToken().getLiterals());
            } else if (getExprParser().equalAny(DefaultKeyword.NULL)) {
                sqlExpr = new SQLIgnoreExpr();
            } else if (getExprParser().equalAny(Symbol.QUESTION)) {
                sqlExpr = new SQLPlaceholderExpr(getExprParser().getParametersIndex(), getExprParser().getParameters().get(getExprParser().getParametersIndex()));
                getExprParser().setParametersIndex(getExprParser().getParametersIndex() + 1);
            } else {
                throw new UnsupportedOperationException("");
            }
            getExprParser().getLexer().nextToken();
            if (getExprParser().equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                if (getShardingRule().isShardingColumn(column)) {
                    conditionContext.add(new Condition(column, sqlExpr));
                }
            } else {
                getExprParser().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getExprParser().equalAny(Symbol.COMMA));
        getSqlContext().setConditionContext(conditionContext);
    }

    @Override
    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Sets.<TokenType>newHashSet(MySQLKeyword.PARTITION);
    }

    @Override
    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES, MySQLKeyword.VALUE);
    }

    @Override
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.SET);
    }
}