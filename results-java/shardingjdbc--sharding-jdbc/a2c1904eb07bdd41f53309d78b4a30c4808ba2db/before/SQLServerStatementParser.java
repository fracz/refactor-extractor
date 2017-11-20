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

package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLConstraint;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerDeclareItem;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerBlockStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerCommitStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerDeclareStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerExecStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerExecStatement.SQLServerParameter;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerIfStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerRollbackStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerSetStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerSetTransactionIsolationLevelStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerWaitForStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import java.util.Collection;
import java.util.List;

public class SQLServerStatementParser extends SQLStatementParser {

    public SQLServerStatementParser(final String sql) {
        super(new SQLServerExprParser(sql));
    }

    @Override
    protected SQLSelectParser createSQLSelectParser() {
        return new SQLServerSelectParser(exprParser);
    }

    public boolean parseStatementListDialect(List<SQLStatement> statementList) {
        if (getLexer().equalToken(Token.WITH)) {
            SQLStatement stmt = parseSelect();
            statementList.add(stmt);
            return true;
        }

        if (getLexer().identifierEquals("EXEC") || getLexer().identifierEquals("EXECUTE")) {
            getLexer().nextToken();

            SQLServerExecStatement execStmt = new SQLServerExecStatement();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                this.parseExecParameter(execStmt.getParameters(), execStmt);
                accept(Token.RIGHT_PAREN);
            } else {
                SQLName sqlNameName = this.exprParser.name();

                if (getLexer().equalToken(Token.EQ)) {
                    getLexer().nextToken();
                    execStmt.setReturnStatus(sqlNameName);
                    execStmt.setModuleName(this.exprParser.name());
                } else {
                    execStmt.setModuleName(sqlNameName);
                }

                this.parseExecParameter(execStmt.getParameters(), execStmt);
            }
            statementList.add(execStmt);
            return true;
        }

        if (getLexer().equalToken(Token.DECLARE)) {
            statementList.add(this.parseDeclare());
            return true;
        }

        if (getLexer().equalToken(Token.IF)) {
            statementList.add(this.parseIf());
            return true;
        }

        if (getLexer().equalToken(Token.BEGIN)) {
            statementList.add(this.parseBlock());
            return true;
        }

        if (getLexer().equalToken(Token.COMMIT)) {
            statementList.add(this.parseCommit());
            return true;
        }

        if (getLexer().identifierEquals("WAITFOR")) {
            statementList.add(this.parseWaitFor());
            return true;
        }

        return false;
    }
    /**
     * SQLServer parse Parameter statement support out type
     */
    public void parseExecParameter(Collection<SQLServerParameter> exprCol, SQLObject parent)
    {
    if (getLexer().equalToken(Token.RIGHT_PAREN) || getLexer().equalToken(Token.RIGHT_BRACKET)) {
            return;
        }

        if (getLexer().equalToken(Token.EOF)) {
            return;
        }
    SQLServerParameter param=new SQLServerParameter();
        SQLExpr expr=this.exprParser.expr();
        expr.setParent(parent);
        param.setExpr(expr);
        if(getLexer().equalToken(Token.OUT)) {
            param.setType(true);
            accept(Token.OUT);
        }
        exprCol.add(param);
        while (getLexer().equalToken(Token.COMMA)) {
        getLexer().nextToken();
        param=new SQLServerParameter();
        expr=this.exprParser.expr();
            expr.setParent(parent);
            param.setExpr(expr);
        if (getLexer().equalToken(Token.OUT)) {
            param.setType(true);
            accept(Token.OUT);
        }
        exprCol.add(param);
        }
    }

    public SQLStatement parseDeclare() {
        this.accept(Token.DECLARE);

        SQLServerDeclareStatement declareStatement = new SQLServerDeclareStatement();

        while (true) {
            SQLServerDeclareItem item = new  SQLServerDeclareItem();
            declareStatement.getItems().add(item);

            item.setName(this.exprParser.name());

            if (getLexer().equalToken(Token.AS)) {
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.TABLE)) {
                getLexer().nextToken();
                item.setType(SQLServerDeclareItem.Type.TABLE);

                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();

                    while (true) {
                        if (getLexer().equalToken(Token.IDENTIFIER)
                            || getLexer().equalToken(Token.LITERAL_ALIAS)) {
                            SQLColumnDefinition column = this.exprParser.parseColumn();
                            item.getTableElementList().add(column);
                        } else if (getLexer().equalToken(Token.PRIMARY)
                                   || getLexer().equalToken(Token.UNIQUE)
                                   || getLexer().equalToken(Token.CHECK)
                                   || getLexer().equalToken(Token.CONSTRAINT)) {
                            SQLConstraint constraint = this.exprParser.parseConstraint();
                            constraint.setParent(item);
                            item.getTableElementList().add((SQLTableElement) constraint);
                        } else if (getLexer().equalToken(Token.TABLESPACE)) {
                            throw new ParserUnsupportedException(getLexer().getToken());
                        } else {
                            SQLColumnDefinition column = this.exprParser.parseColumn();
                            item.getTableElementList().add(column);
                        }

                        if (getLexer().equalToken(Token.COMMA)) {
                            getLexer().nextToken();

                            if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                                break;
                            }
                            continue;
                        }

                        break;
                    }
                    accept(Token.RIGHT_PAREN);
                }
                break;
            } else if (getLexer().equalToken(Token.CURSOR)) {
                item.setType(SQLServerDeclareItem.Type.CURSOR);
                getLexer().nextToken();
            } else {
                item.setType(SQLServerDeclareItem.Type.LOCAL);
                item.setDataType(this.exprParser.parseDataType());
                if (getLexer().equalToken(Token.EQ)) {
                    getLexer().nextToken();
                    item.setValue(this.exprParser.expr());
                }
            }

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            } else {
                break;
            }
        }
        return declareStatement;
    }

    @Override
    protected SQLServerUpdateStatement createUpdateStatement() {
        return new SQLServerUpdateStatement();
    }

    protected void parseCustomizedParserBetweenUpdateAndTable(final SQLUpdateStatement updateStatement) {
        SQLServerTop top = getExprParser().parseTop();
        if (null != top) {
            ((SQLServerUpdateStatement) updateStatement).setTop(top);
        }
    }

    @Override
    protected void parseCustomizedParserBetweenSetAndWhere(final SQLUpdateStatement updateStatement) {
        SQLServerOutput output = getExprParser().parserOutput();
        if (null != output) {
            ((SQLServerUpdateStatement) updateStatement).setOutput(output);
        }
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            ((SQLServerUpdateStatement) updateStatement).setFrom(getExprParser().createSelectParser().parseTableSource());
        }
    }

    public SQLServerExprParser getExprParser() {
        return (SQLServerExprParser) exprParser;
    }

    public SQLStatement parseSet() {
        accept(Token.SET);

        if (getLexer().identifierEquals("TRANSACTION")) {
            getLexer().nextToken();
            acceptIdentifier("ISOLATION");
            acceptIdentifier("LEVEL");

            SQLServerSetTransactionIsolationLevelStatement stmt = new SQLServerSetTransactionIsolationLevelStatement();

            if (getLexer().identifierEquals("READ")) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("UNCOMMITTED")) {
                    stmt.setLevel("READ UNCOMMITTED");
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("COMMITTED")) {
                    stmt.setLevel("READ COMMITTED");
                    getLexer().nextToken();
                } else {
                    throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
                }
            } else if (getLexer().identifierEquals("SERIALIZABLE")) {
                stmt.setLevel("SERIALIZABLE");
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("SNAPSHOT")) {
                stmt.setLevel("SNAPSHOT");
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("REPEATABLE")) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("READ")) {
                    stmt.setLevel("REPEATABLE READ");
                    getLexer().nextToken();
                } else {
                    throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
                }
            } else {
                throw new ParserException("UNKOWN TRANSACTION LEVEL : " + getLexer().getLiterals());
            }

            return stmt;
        }

        if (getLexer().identifierEquals("STATISTICS")) {
            getLexer().nextToken();

            SQLServerSetStatement stmt = new SQLServerSetStatement();

            if (getLexer().identifierEquals("IO") || getLexer().identifierEquals("XML") || getLexer().identifierEquals("PROFILE")
                || getLexer().identifierEquals("TIME")) {
                stmt.getItem().setTarget(new SQLIdentifierExpr("STATISTICS " + getLexer().getLiterals().toUpperCase()));

                getLexer().nextToken();
                if (getLexer().equalToken(Token.ON)) {
                    stmt.getItem().setValue(new SQLIdentifierExpr("ON"));
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("OFF")) {
                    stmt.getItem().setValue(new SQLIdentifierExpr("OFF"));
                    getLexer().nextToken();
                }
            }
            return stmt;
        }

        if (getLexer().equalToken(Token.VARIANT)) {
            SQLSetStatement stmt = new SQLSetStatement(getDbType());
            parseAssignItems(stmt.getItems(), stmt);
            return stmt;
        } else {
            SQLServerSetStatement stmt = new SQLServerSetStatement();
            stmt.getItem().setTarget(this.exprParser.expr());

            if (getLexer().equalToken(Token.ON)) {
                stmt.getItem().setValue(new SQLIdentifierExpr("ON"));
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("OFF")) {
                stmt.getItem().setValue(new SQLIdentifierExpr("OFF"));
                getLexer().nextToken();
            } else {
                stmt.getItem().setValue(this.exprParser.expr());
            }
            return stmt;
        }
    }

    public SQLServerIfStatement parseIf() {
        accept(Token.IF);
        SQLServerIfStatement stmt = new SQLServerIfStatement();
        stmt.setCondition(this.exprParser.expr());
        stmt.getStatements().add(parseStatement());
        if(getLexer().equalToken(Token.SEMI)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.ELSE)) {
            getLexer().nextToken();
            SQLServerIfStatement.Else elseItem = new SQLServerIfStatement.Else();
            elseItem.getStatements().add(parseStatement());
            stmt.setElseItem(elseItem);
        }
        return stmt;
    }

    public SQLServerBlockStatement parseBlock() {
        SQLServerBlockStatement block = new SQLServerBlockStatement();
        accept(Token.BEGIN);
        block.getStatementList().addAll(parseStatementList());
        accept(Token.END);
        return block;
    }

    public SQLServerCommitStatement parseCommit() {
        acceptIdentifier("COMMIT");

        SQLServerCommitStatement stmt = new SQLServerCommitStatement();

        if (getLexer().identifierEquals("WORK")) {
            getLexer().nextToken();
            stmt.setWork(true);
        }

        if (getLexer().identifierEquals("TRAN") || getLexer().identifierEquals("TRANSACTION")) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.IDENTIFIER) || getLexer().equalToken(Token.VARIANT)) {
                stmt.setTransactionName(this.exprParser.expr());
            }

            if (getLexer().equalToken(Token.WITH)) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                acceptIdentifier("DELAYED_DURABILITY");
                accept(Token.EQ);
                stmt.setDelayedDurability(this.exprParser.expr());
                accept(Token.RIGHT_PAREN);
            }

        }

        return stmt;
    }

    public SQLServerRollbackStatement parseRollback() {
        acceptIdentifier("ROLLBACK");

        SQLServerRollbackStatement stmt = new SQLServerRollbackStatement();

        if (getLexer().identifierEquals("WORK")) {
            getLexer().nextToken();
            stmt.setWork(true);
        }

        if (getLexer().identifierEquals("TRAN") || getLexer().identifierEquals("TRANSACTION")) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.IDENTIFIER) || getLexer().equalToken(Token.VARIANT)) {
                stmt.setName(this.exprParser.expr());
            }


        }

        return stmt;
    }

    public SQLServerWaitForStatement parseWaitFor() {
        acceptIdentifier("WAITFOR");

        SQLServerWaitForStatement stmt = new SQLServerWaitForStatement();

        if (getLexer().identifierEquals("DELAY")) {
            getLexer().nextToken();
            stmt.setDelay(this.exprParser.expr());
        }

        if (getLexer().identifierEquals("TIME")) {
            getLexer().nextToken();
            stmt.setTime(this.exprParser.expr());
        }

        if (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("TIMEOUT")) {
                getLexer().nextToken();
                stmt.setTimeout(this.exprParser.expr());
            }
        }

        return stmt;
    }
}