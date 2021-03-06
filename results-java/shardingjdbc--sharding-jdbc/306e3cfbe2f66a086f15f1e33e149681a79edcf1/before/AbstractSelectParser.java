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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.context.CommonSelectItemContext;
import com.alibaba.druid.sql.context.GroupByContext;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class AbstractSelectParser extends SQLParser {

    @Getter
    private SQLExprParser exprParser;

    @Getter
    private final ShardingRule shardingRule;

    @Getter
    private final List<Object> parameters;

    @Getter
    @Setter
    private int parametersIndex;

    public AbstractSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer());
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.exprParser = exprParser;
    }

    /**
     * 解析查询.
     *
     * @return 解析结果
     */
    public final SelectSQLContext parse() {
        SelectSQLContext result = new SelectSQLContext(getLexer().getInput());
        query(result);
        result.getOrderByContexts().addAll(exprParser.parseOrderBy());
        customizedSelect(result);
        return result;
    }

    protected void customizedSelect(final SelectSQLContext sqlContext) {
    }

    protected SQLSelectQuery query(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query(sqlContext);
            getLexer().accept(Token.RIGHT_PAREN);
            queryRest();
            return select;
        }
        SQLSelectQueryBlock queryBlock = new SQLSelectQueryBlock();
        getLexer().accept(Token.SELECT);
        getLexer().skipIfEqual(Token.COMMENT);
        parseDistinct(sqlContext);
        parseSelectList(sqlContext);
        parseFrom(sqlContext);
        parseWhere(sqlContext);
        parseGroupBy(sqlContext);
        queryRest();
        return queryBlock;
    }

    protected final void parseDistinct(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.DISTINCT, Token.DISTINCTROW, Token.UNION)) {
            sqlContext.setDistinct(true);
            getLexer().nextToken();
            if (hasDistinctOn() && getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                getLexer().skipParentheses();
            }
        } else if (getLexer().equalToken(Token.ALL)) {
            getLexer().nextToken();
        }
    }

    protected boolean hasDistinctOn() {
        return false;
    }

    protected final void parseSelectList(final SelectSQLContext sqlContext) {
        int index = 1;
        do {
            SQLSelectItem selectItem = exprParser.parseSelectItem(index, sqlContext);
            index++;
            sqlContext.getItemContexts().add(selectItem.getSelectItemContext());
            if (selectItem.getSelectItemContext() instanceof CommonSelectItemContext) {
                if (((CommonSelectItemContext) selectItem.getSelectItemContext()).isStar()) {
                    sqlContext.setContainStar(true);
                }
            }
        } while (getLexer().skipIfEqual(Token.COMMA));
        sqlContext.setSelectListLastPosition(getLexer().getCurrentPosition() - getLexer().getLiterals().length());
    }

    protected void queryRest() {
        if (getLexer().equalToken(Token.UNION) || getLexer().equalToken(Token.EXCEPT) || getLexer().equalToken(Token.INTERSECT) || getLexer().equalToken(Token.MINUS)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    protected final void parseWhere(final SelectSQLContext sqlContext) {
        if (sqlContext.getTables().isEmpty()) {
            return;
        }
        ParserUtil parserUtil = new ParserUtil(exprParser, parameters, sqlContext, 0);
        Optional<ConditionContext> conditionContext = parserUtil.parseWhere(getParseContext(sqlContext));
        if (conditionContext.isPresent()) {
            sqlContext.getConditionContexts().add(conditionContext.get());
        }
        parametersIndex = parserUtil.getParametersIndex();
    }

    private ParseContext getParseContext(final SQLContext sqlContext) {
        ParseContext result = new ParseContext(1);
        result.setShardingRule(shardingRule);
        for (TableContext each : sqlContext.getTables()) {
            result.setCurrentTable(each.getName(), each.getAlias());
        }
        return result;
    }

    protected void parseGroupBy(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.GROUP)) {
            getLexer().nextToken();
            getLexer().accept(Token.BY);
            while (true) {
                addGroupByItem(exprParser.expr(), sqlContext);
                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }
                getLexer().nextToken();
            }
            while (getLexer().equalToken(Token.WITH) || getLexer().getLiterals().equalsIgnoreCase("ROLLUP")) {
                getLexer().nextToken();
            }
            if (getLexer().equalToken(Token.HAVING)) {
                getLexer().nextToken();
                exprParser.expr();
            }
        } else if (getLexer().equalToken(Token.HAVING)) {
            getLexer().nextToken();
            exprParser.expr();
        }
    }

    protected final void addGroupByItem(final SQLExpr sqlExpr, final SelectSQLContext sqlContext) {
        OrderByColumn.OrderByType orderByType = OrderByColumn.OrderByType.ASC;
        if (getLexer().equalToken(Token.ASC)) {
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.DESC)) {
            getLexer().nextToken();
            orderByType = OrderByColumn.OrderByType.DESC;
        }
        if (sqlExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr expr = (SQLPropertyExpr) sqlExpr;
            sqlContext.getGroupByContexts().add(new GroupByContext(Optional.of(SQLUtil.getExactlyValue(expr.getOwner().toString())), SQLUtil.getExactlyValue(expr.getSimpleName()), orderByType));
        } else if (sqlExpr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr expr = (SQLIdentifierExpr) sqlExpr;
            sqlContext.getGroupByContexts().add(new GroupByContext(Optional.<String>absent(), SQLUtil.getExactlyValue(expr.getSimpleName()), orderByType));
        }
    }

    public final void parseFrom(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            parseTableSource(sqlContext);
        }
    }

    public List<TableContext> parseTableSource(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        parseTableFactor(sqlContext);
        parseJoinTable(sqlContext);
        return sqlContext.getTables();
    }

    protected final void parseTableFactor(final SelectSQLContext sqlContext) {
        int beginPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        String literals = getLexer().getLiterals();
        getLexer().nextToken();
        if (getLexer().skipIfEqual(Token.DOT)) {
            getLexer().nextToken();
            as();
            return;
        }
        // FIXME 根据shardingRule过滤table
        sqlContext.getSqlTokens().add(new TableToken(beginPosition, literals, SQLUtil.getExactlyValue(literals)));
        sqlContext.getTables().add(new TableContext(literals, SQLUtil.getExactlyValue(literals), as()));
    }

    protected void parseJoinTable(final SelectSQLContext sqlContext) {
        if (isJoin()) {
            parseTableSource(sqlContext);
            if (getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                int leftStartPosition = getLexer().getCurrentPosition();
                SQLExpr sqlExpr = exprParser.expr();
                if (sqlExpr instanceof SQLBinaryOpExpr) {
                    SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
                    if (binaryOpExpr.getLeft() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) binaryOpExpr.getLeft();
                        for (TableContext each : sqlContext.getTables()) {
                            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().toString()))) {
                                sqlContext.getSqlTokens().add(new TableToken(leftStartPosition, sqlPropertyExpr.getOwner().toString(), SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().toString())));
                            }
                        }
                    }
                    if (binaryOpExpr.getRight() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) binaryOpExpr.getRight();
                        for (TableContext each : sqlContext.getTables()) {
                            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().toString()))) {
                                sqlContext.getSqlTokens().add(new TableToken(binaryOpExpr.getRightStartPosition(), sqlPropertyExpr.getOwner().toString(), SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().toString())));
                            }
                        }
                    }
                }
            } else if (getLexer().skipIfEqual(Token.USING)) {
                getLexer().skipParentheses();
            }
            parseJoinTable(sqlContext);
        }
    }
}