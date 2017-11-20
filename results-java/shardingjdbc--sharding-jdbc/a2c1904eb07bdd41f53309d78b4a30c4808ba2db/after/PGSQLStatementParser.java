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
package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAlterColumn;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithQuery;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGDeleteStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGShowStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.SQLUpdateParserFactory;
import com.alibaba.druid.util.JdbcConstants;

import java.util.List;

public class PGSQLStatementParser extends SQLStatementParser {

    public PGSQLStatementParser(final String sql) {
        super(new PGExprParser(sql));
    }

    @Override
    protected PGSelectParser createSQLSelectParser() {
        return new PGSelectParser(exprParser);
    }

    public PGDeleteStatement parseDeleteStatement() {
        getLexer().nextToken();
        PGDeleteStatement deleteStatement = new PGDeleteStatement();

        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.ONLY)) {
            getLexer().nextToken();
            deleteStatement.setOnly(true);
        }

        SQLName tableName = exprParser.name();

        deleteStatement.setTableName(tableName);

        if (getLexer().equalToken(Token.AS)) {
            accept(Token.AS);
        }
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            deleteStatement.setAlias(getLexer().getLiterals());
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.USING)) {
            getLexer().nextToken();
            while (true) {
                SQLName name = this.exprParser.name();
                deleteStatement.getUsing().add(name);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.CURRENT)) {
                getLexer().nextToken();
                accept(Token.OF);
                SQLName cursorName = this.exprParser.name();
                SQLExpr where = new SQLCurrentOfCursorExpr(cursorName);
                deleteStatement.setWhere(where);
            } else {
                SQLExpr where = this.exprParser.expr();
                deleteStatement.setWhere(where);
            }
        }

        if (getLexer().equalToken(Token.RETURNING)) {
            getLexer().nextToken();
            accept(Token.STAR);
            deleteStatement.setReturning(true);
        }

        return deleteStatement;
    }

    public boolean parseStatementListDialect(List<SQLStatement> statementList) {
        if (getLexer().equalToken(Token.WITH)) {
            SQLStatement stmt = parseWith();
            statementList.add(stmt);
            return true;
        }

        return false;
    }

    public PGWithClause parseWithClause() {
        getLexer().nextToken();

        PGWithClause withClause = new PGWithClause();

        if (getLexer().equalToken(Token.RECURSIVE)) {
            getLexer().nextToken();
            withClause.setRecursive(true);
        }

        while (true) {
            PGWithQuery withQuery = withQuery();
            withClause.getWithQuery().add(withQuery);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            } else {
                break;
            }
        }
        return withClause;
    }

    private PGWithQuery withQuery() {
        PGWithQuery withQuery = new PGWithQuery();

        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            withQuery.setName(new SQLIdentifierExpr("\"" + getLexer().getLiterals()
                    + "\""));
        } else {
            withQuery.setName(new SQLIdentifierExpr(getLexer().getLiterals()));
        }
        getLexer().nextToken();

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            while (true) {
                SQLExpr expr = this.exprParser.expr();
                withQuery.getColumns().add(expr);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }

            accept(Token.RIGHT_PAREN);
        }

        accept(Token.AS);

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            SQLStatement query;
            if (getLexer().equalToken(Token.SELECT)) {
                query = parseSelect();
            } else if (getLexer().equalToken(Token.INSERT)) {
                query = new PostgreSQLInsertParser(exprParser).parse();
            } else if (getLexer().equalToken(Token.UPDATE)) {
                query = SQLUpdateParserFactory.newInstance(exprParser, JdbcConstants.POSTGRESQL).parse();
            } else if (getLexer().equalToken(Token.DELETE)) {
                query = parseDeleteStatement();
            } else if (getLexer().equalToken(Token.VALUES)) {
                query = parseSelect();
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            withQuery.setQuery(query);

            accept(Token.RIGHT_PAREN);
        }

        return withQuery;
    }

    @Override
    protected PGSelectStatement parseSelect() {
        return new PGSelectStatement(createSQLSelectParser().select());
    }

    public SQLStatement parseWith() {
        PGWithClause with = parseWithClause();
        if (getLexer().equalToken(Token.INSERT)) {
            PGInsertStatement stmt = (PGInsertStatement) new PostgreSQLInsertParser(exprParser).parse();
            stmt.setWith(with);
            return stmt;
        }

        if (getLexer().equalToken(Token.SELECT)) {
            PGSelectStatement stmt = parseSelect();
            stmt.setWith(with);
            return stmt;
        }

        if (getLexer().equalToken(Token.DELETE)) {
            PGDeleteStatement stmt = parseDeleteStatement();
            stmt.setWith(with);
            return stmt;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected SQLAlterTableAlterColumn parseAlterColumn() {
        if (getLexer().equalToken(Token.COLUMN)) {
            getLexer().nextToken();
        }

        SQLColumnDefinition column = this.exprParser.parseColumn();

        SQLAlterTableAlterColumn alterColumn = new SQLAlterTableAlterColumn();
        alterColumn.setColumn(column);

        if (column.getDataType() == null && column.getConstraints().size() == 0) {
            if (getLexer().equalToken(Token.SET)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.NOT)) {
                    getLexer().nextToken();
                    accept(Token.NULL);
                    alterColumn.setSetNotNull(true);
                } else {
                    accept(Token.DEFAULT);
                    SQLExpr defaultValue = this.exprParser.expr();
                    alterColumn.setSetDefault(defaultValue);
                }
            } else if (getLexer().equalToken(Token.DROP)) {
                getLexer().nextToken();
                if (getLexer().equalToken(Token.NOT)) {
                    getLexer().nextToken();
                    accept(Token.NULL);
                    alterColumn.setDropNotNull(true);
                } else {
                    accept(Token.DEFAULT);
                    alterColumn.setDropDefault(true);
                }
            }
        }
        return alterColumn;
    }

    public SQLStatement parseShow() {
        accept(Token.SHOW);
        return new PGShowStatement(exprParser.expr());
    }
}