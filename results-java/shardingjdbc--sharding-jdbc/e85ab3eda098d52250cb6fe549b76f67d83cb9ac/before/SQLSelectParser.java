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
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionOperator;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.alibaba.druid.sql.lexer.Token;

import java.util.List;

public class SQLSelectParser extends SQLParser {

    protected SQLExprParser exprParser;

    public SQLSelectParser(SQLExprParser exprParser){
        super(exprParser.getLexer());
        this.exprParser = exprParser;
    }

    public SQLSelect select() {
        SQLSelect select = new SQLSelect();
        withSubquery(select);
        select.setQuery(query());
        select.setOrderBy(parseOrderBy());
        if (null == select.getOrderBy()) {
            select.setOrderBy(parseOrderBy());
        }
        while (getLexer().equalToken(Token.HINT)) {
            exprParser.parseHints(select.getHints());
        }
        return select;
    }

    protected void withSubquery(final SQLSelect select) {
        if (!getLexer().equalToken(Token.WITH)) {
            return;
        }
        getLexer().nextToken();
        SQLWithSubqueryClause withQueryClause = new SQLWithSubqueryClause();
        if (getLexer().equalToken(Token.RECURSIVE) || getLexer().identifierEquals("RECURSIVE")) {
            getLexer().nextToken();
            withQueryClause.setRecursive(true);
        }
        while (true) {
            SQLWithSubqueryClause.Entry entry = new SQLWithSubqueryClause.Entry();
            entry.setParent(withQueryClause);
            entry.setName((SQLIdentifierExpr) exprParser.name());
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                exprParser.names(entry.getColumns());
                accept(Token.RIGHT_PAREN);
            }
            accept(Token.AS);
            accept(Token.LEFT_PAREN);
            entry.setSubQuery(select());
            accept(Token.RIGHT_PAREN);
            withQueryClause.getEntries().add(entry);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                continue;
            }
            break;
        }
        select.setWithSubQuery(withQueryClause);
    }

    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            accept(Token.RIGHT_PAREN);
            return queryRest(select);
        }
        SQLSelectQueryBlock queryBlock = new SQLSelectQueryBlock();
        accept(Token.SELECT);
        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.DISTINCT)) {
            queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.UNIQUE)) {
            queryBlock.setDistionOption(SQLSetQuantifier.UNIQUE);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.ALL)) {
            queryBlock.setDistionOption(SQLSetQuantifier.ALL);
            getLexer().nextToken();
        }
        parseSelectList(queryBlock);
        parseFrom(queryBlock);
        parseWhere(queryBlock);
        parseGroupBy(queryBlock);
        return queryRest(queryBlock);
    }

    public SQLSelectQuery queryRest(SQLSelectQuery selectQuery) {
        if (getLexer().equalToken(Token.UNION)) {
            getLexer().nextToken();

            SQLUnionQuery union = createSQLUnionQuery();
            union.setLeft(selectQuery);

            if (getLexer().equalToken(Token.ALL)) {
                union.setOperator(SQLUnionOperator.UNION_ALL);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.DISTINCT)) {
                union.setOperator(SQLUnionOperator.DISTINCT);
                getLexer().nextToken();
            }
            SQLSelectQuery right = this.query();
            union.setRight(right);

            return unionRest(union);
        }

        if (getLexer().equalToken(Token.EXCEPT)) {
            getLexer().nextToken();

            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            union.setOperator(SQLUnionOperator.EXCEPT);

            SQLSelectQuery right = this.query();
            union.setRight(right);

            return union;
        }

        if (getLexer().equalToken(Token.INTERSECT)) {
            getLexer().nextToken();

            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            union.setOperator(SQLUnionOperator.INTERSECT);

            SQLSelectQuery right = this.query();
            union.setRight(right);

            return union;
        }

        if (getLexer().equalToken(Token.MINUS)) {
            getLexer().nextToken();

            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            union.setOperator(SQLUnionOperator.MINUS);

            SQLSelectQuery right = this.query();
            union.setRight(right);

            return union;
        }

        return selectQuery;
    }

    protected final SQLOrderBy parseOrderBy() {
        return exprParser.parseOrderBy();
    }

    protected SQLUnionQuery createSQLUnionQuery() {
        return new SQLUnionQuery();
    }

    public SQLUnionQuery unionRest(SQLUnionQuery union) {
        if (getLexer().equalToken(Token.ORDER)) {
            SQLOrderBy orderBy = this.exprParser.parseOrderBy();
            union.setOrderBy(orderBy);
            return unionRest(union);
        }
        return union;
    }

    protected void parseWhere(SQLSelectQueryBlock queryBlock) {
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            SQLExpr where = expr();
            queryBlock.setWhere(where);
        }
    }

    protected void parseGroupBy(SQLSelectQueryBlock queryBlock) {
        if (getLexer().equalToken(Token.GROUP)) {
            getLexer().nextToken();
            accept(Token.BY);

            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            while (true) {
                groupBy.addItem(expr());
                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }

                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.HAVING)) {
                getLexer().nextToken();

                groupBy.setHaving(expr());
            }

            queryBlock.setGroupBy(groupBy);
        } else if (getLexer().equalToken(Token.HAVING)) {
            getLexer().nextToken();

            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            groupBy.setHaving(this.expr());
            queryBlock.setGroupBy(groupBy);
        }
    }

    protected void parseSelectList(SQLSelectQueryBlock queryBlock) {
        final List<SQLSelectItem> selectList = queryBlock.getSelectList();
        while (true) {
            final SQLSelectItem selectItem = this.exprParser.parseSelectItem();
            selectList.add(selectItem);
            selectItem.setParent(queryBlock);

            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            }

            getLexer().nextToken();
        }
    }

    public void parseFrom(SQLSelectQueryBlock queryBlock) {
        if (!getLexer().equalToken(Token.FROM)) {
            return;
        }

        getLexer().nextToken();

        queryBlock.setFrom(parseTableSource());
    }

    public SQLTableSource parseTableSource() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLTableSource tableSource;
            if (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.WITH)) {
                SQLSelect select = select();
                accept(Token.RIGHT_PAREN);
                SQLSelectQuery query = queryRest(select.getQuery());
                if (query instanceof SQLUnionQuery) {
                    tableSource = new SQLUnionQueryTableSource((SQLUnionQuery) query);
                } else {
                    tableSource = new SQLSubqueryTableSource(select);
                }
            } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                tableSource = parseTableSource();
                accept(Token.RIGHT_PAREN);
            } else {
                tableSource = parseTableSource();
                accept(Token.RIGHT_PAREN);
            }

            return parseTableSourceRest(tableSource);
        }

        if (getLexer().equalToken(Token.SELECT)) {
            throw new ParserException("TODO");
        }

        SQLExprTableSource tableReference = new SQLExprTableSource();

        parseTableSourceQueryTableExpr(tableReference);
        return parseTableSourceRest(tableReference);
    }

    protected void parseTableSourceQueryTableExpr(SQLExprTableSource tableReference) {
        if (getLexer().equalToken(Token.LITERAL_ALIAS) || getLexer().equalToken(Token.IDENTIFIED)
            || getLexer().equalToken(Token.LITERAL_CHARS)) {
            tableReference.setExpr(this.exprParser.name());
            return;
        }

        tableReference.setExpr(expr());
    }

    protected SQLTableSource parseTableSourceRest(SQLTableSource tableSource) {
        if ((tableSource.getAlias() == null) || (tableSource.getAlias().length() == 0)) {
            if (!getLexer().equalToken(Token.LEFT) && !getLexer().equalToken(Token.RIGHT) && !getLexer().equalToken(Token.FULL)
                && !getLexer().identifierEquals("STRAIGHT_JOIN") && !getLexer().identifierEquals("CROSS") && !getLexer().equalToken(Token.OUTER)) {
                String alias = as();
                if (alias != null) {
                    tableSource.setAlias(alias);
                    return parseTableSourceRest(tableSource);
                }
            }
        }

        SQLJoinTableSource.JoinType joinType = null;

        if (getLexer().equalToken(Token.LEFT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }

            accept(Token.JOIN);
            joinType = SQLJoinTableSource.JoinType.LEFT_OUTER_JOIN;
        } else if (getLexer().equalToken(Token.RIGHT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }
            accept(Token.JOIN);
            joinType = SQLJoinTableSource.JoinType.RIGHT_OUTER_JOIN;
        } else if (getLexer().equalToken(Token.FULL)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }
            accept(Token.JOIN);
            joinType = SQLJoinTableSource.JoinType.FULL_OUTER_JOIN;
        } else if (getLexer().equalToken(Token.INNER)) {
            getLexer().nextToken();
            accept(Token.JOIN);
            joinType = SQLJoinTableSource.JoinType.INNER_JOIN;
        } else if (getLexer().equalToken(Token.JOIN)) {
            getLexer().nextToken();
            joinType = SQLJoinTableSource.JoinType.JOIN;
        } else if (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            joinType = SQLJoinTableSource.JoinType.COMMA;
        } else if (getLexer().identifierEquals("STRAIGHT_JOIN")) {
            getLexer().nextToken();
            joinType = SQLJoinTableSource.JoinType.STRAIGHT_JOIN;
        } else if (getLexer().identifierEquals("CROSS")) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.JOIN)) {
                getLexer().nextToken();
                joinType = SQLJoinTableSource.JoinType.CROSS_JOIN;
            } else if (getLexer().identifierEquals("APPLY")) {
                getLexer().nextToken();
                joinType = SQLJoinTableSource.JoinType.CROSS_APPLY;
            }
        } else if (getLexer().equalToken(Token.OUTER)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("APPLY")) {
                getLexer().nextToken();
                joinType = SQLJoinTableSource.JoinType.OUTER_APPLY;
            }
        }

        if (joinType != null) {
            SQLJoinTableSource join = new SQLJoinTableSource();
            join.setLeft(tableSource);
            join.setJoinType(joinType);
            join.setRight(parseTableSource());

            if (getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                join.setCondition(expr());
            } else if (getLexer().identifierEquals("USING")) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();
                    this.exprParser.exprList(join.getUsing(), join);
                    accept(Token.RIGHT_PAREN);
                } else {
                    join.getUsing().add(this.expr());
                }
            }

            return parseTableSourceRest(join);
        }

        return tableSource;
    }

    public SQLExpr expr() {
        return this.exprParser.expr();
    }
}