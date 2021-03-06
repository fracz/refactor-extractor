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

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLDefaultExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SQLExprParser extends SQLParser {

    private final Set<String> aggregateFunctions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public SQLExprParser(final Lexer lexer, final String dbType, final String... aggregateFunctions) {
        super(lexer, dbType);
        this.aggregateFunctions.addAll(Arrays.asList(aggregateFunctions));
    }

    public final SQLExpr expr(final SQLObject parent) {
        SQLExpr result = expr();
        result.setParent(parent);
        return result;
    }

    public SQLExpr expr() {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr sqlExpr = new SQLAllColumnExpr();
            if (getLexer().equalToken(Token.DOT)) {
                getLexer().nextToken();
                accept(Token.STAR);
                return new SQLPropertyExpr(sqlExpr, "*");
            }
            return sqlExpr;
        }
        SQLExpr expr = primary();
        if (getLexer().equalToken(Token.COMMA)) {
            return expr;
        }
        return exprRest(expr);
    }

    public SQLExpr primary() {
        SQLExpr sqlExpr = null;
        Token token = getLexer().getToken();
        switch (token) {
            case LEFT_PAREN:
                getLexer().nextToken();
                sqlExpr = expr();
                if (getLexer().equalToken(Token.COMMA)) {
                    SQLListExpr listExpr = new SQLListExpr();
                    listExpr.getItems().add(sqlExpr);
                    do {
                        getLexer().nextToken();
                        listExpr.getItems().add(expr());
                    } while (getLexer().equalToken(Token.COMMA));
                    sqlExpr = listExpr;
                }
                accept(Token.RIGHT_PAREN);
                break;
            case INSERT:
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.LEFT_PAREN)) {
                    throw new ParserException(getLexer());
                }
                sqlExpr = new SQLIdentifierExpr("INSERT");
                break;
            case IDENTIFIER:
                sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case NEW:
                throw new ParserUnsupportedException(getLexer().getToken());
            case LITERAL_INT:
                sqlExpr = new SQLIntegerExpr(getLexer().integerValue());
                getLexer().nextToken();
                break;
            case LITERAL_FLOAT:
                sqlExpr = new SQLNumberExpr(getLexer().decimalValue());
                getLexer().nextToken();
                break;
            case LITERAL_CHARS:
                sqlExpr = new SQLCharExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case LITERAL_NCHARS:
                sqlExpr = new SQLNCharExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case VARIANT:
                SQLVariantRefExpr varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals());
                getLexer().nextToken();
                if (varRefExpr.getName().equals("@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                }
                sqlExpr = varRefExpr;
                break;
            case DEFAULT:
                sqlExpr = new SQLDefaultExpr();
                getLexer().nextToken();
                break;
            case DUAL:
            case KEY:
            case DISTINCT:
            case LIMIT:
            case SCHEMA:
            case COLUMN:
            case IF:
            case END:
            case COMMENT:
            case COMPUTE:
            case ENABLE:
            case DISABLE:
            case INITIALLY:
            case SEQUENCE:
            case USER:
            case EXPLAIN:
            case WITH:
            case GRANT:
            case REPLACE:
            case INDEX:
            case MODEL:
            case PCTFREE:
            case INITRANS:
            case MAXTRANS:
            case SEGMENT:
            case CREATION:
            case IMMEDIATE:
            case DEFERRED:
            case STORAGE:
            case NEXT:
            case MINEXTENTS:
            case MAXEXTENTS:
            case MAXSIZE:
            case PCTINCREASE:
            case FLASH_CACHE:
            case CELL_FLASH_CACHE:
            case KEEP:
            case NONE:
            case LOB:
            case STORE:
            case ROW:
            case CHUNK:
            case CACHE:
            case NOCACHE:
            case LOGGING:
            case NOCOMPRESS:
            case KEEP_DUPLICATES:
            case EXCEPTIONS:
            case PURGE:
            case FULL:
            case TO:
            case IDENTIFIED:
            case PASSWORD:
            case BINARY:
            case WINDOW:
            case OFFSET:
            case SHARE:
            case START:
            case CONNECT:
            case MATCHED:
            case ERRORS:
            case REJECT:
            case UNLIMITED:
            case BEGIN:
            case EXCLUSIVE:
            case MODE:
            case ADVISE:
            case VIEW:
            case ESCAPE:
            case OVER:
            case ORDER:
            case CONSTRAINT:
            case TYPE:
            case OPEN:
            case REPEAT:
                sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                getLexer().nextToken();
                break;
            case CASE:
                SQLCaseExpr caseExpr = new SQLCaseExpr();
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.WHEN)) {
                    caseExpr.setValueExpr(expr());
                }
                accept(Token.WHEN);
                SQLExpr testExpr = expr();
                accept(Token.THEN);
                SQLExpr valueExpr = expr();
                SQLCaseExpr.Item caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                caseExpr.addItem(caseItem);
                while (getLexer().equalToken(Token.WHEN)) {
                    getLexer().nextToken();
                    testExpr = expr();
                    accept(Token.THEN);
                    valueExpr = expr();
                    caseItem = new SQLCaseExpr.Item(testExpr, valueExpr);
                    caseExpr.getItems().add(caseItem);
                }
                if (getLexer().equalToken(Token.ELSE)) {
                    getLexer().nextToken();
                    caseExpr.setElseExpr(expr());
                }
                accept(Token.END);
                sqlExpr = caseExpr;
                break;
            case EXISTS:
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                sqlExpr = new SQLExistsExpr(createSelectParser().select(), false);
                accept(Token.RIGHT_PAREN);
                break;
            case NOT:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.EXISTS)) {
                    getLexer().nextToken();
                    accept(Token.LEFT_PAREN);
                    sqlExpr = new SQLExistsExpr(createSelectParser().select(), true);
                    accept(Token.RIGHT_PAREN);
                } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();
                    SQLExpr notTarget = expr();
                    accept(Token.RIGHT_PAREN);
                    notTarget = exprRest(notTarget);
                    sqlExpr = new SQLNotExpr(notTarget);
                    return primaryRest(sqlExpr);
                } else {
                    sqlExpr = new SQLNotExpr(relational());
                }
                break;
            case SELECT:
                sqlExpr = new SQLQueryExpr(createSelectParser().select());
                break;
            case CAST:
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                SQLCastExpr cast = new SQLCastExpr();
                cast.setExpr(expr());
                accept(Token.AS);
                cast.setDataType(parseDataType());
                accept(Token.RIGHT_PAREN);
                sqlExpr = cast;
                break;
            case SUB:
                getLexer().nextToken();
                switch (getLexer().getToken()) {
                    case LITERAL_INT:
                        Number integerValue = getLexer().integerValue();
                        if (integerValue instanceof Integer) {
                            int intVal = integerValue.intValue();
                            if (intVal == Integer.MIN_VALUE) {
                                integerValue = ((long) intVal) * -1;
                            } else {
                                integerValue = intVal * -1;
                            }
                        } else if (integerValue instanceof Long) {
                            long longVal = integerValue.longValue();
                            if (longVal == 2147483648L) {
                                integerValue = (int) (longVal * -1);
                            } else {
                                integerValue = longVal * -1;
                            }
                        } else {
                            integerValue = ((BigInteger) integerValue).negate();
                        }
                        sqlExpr = new SQLIntegerExpr(integerValue);
                        getLexer().nextToken();
                        break;
                    case LITERAL_FLOAT:
                        sqlExpr = new SQLNumberExpr(getLexer().decimalValue().negate());
                        getLexer().nextToken();
                        break;
                    case IDENTIFIER: // 当负号后面为字段的情况
                        sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                        getLexer().nextToken();
                        if (getLexer().equalToken(Token.LEFT_PAREN) || getLexer().equalToken(Token.LEFT_BRACKET)) {
                            sqlExpr = primaryRest(sqlExpr);
                        }
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, sqlExpr);
                        break;
                    case QUESTION:
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, new SQLVariantRefExpr("?"));
                        getLexer().nextToken();
                        break;
                    case LEFT_PAREN:
                        getLexer().nextToken();
                        sqlExpr = expr();
                        accept(Token.RIGHT_PAREN);
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Negative, sqlExpr);
                        break;
                    default:
                        throw new ParserUnsupportedException(getLexer().getToken());
                }
                break;
            case PLUS:
                getLexer().nextToken();
                switch (getLexer().getToken()) {
                    case LITERAL_INT:
                        sqlExpr = new SQLIntegerExpr(getLexer().integerValue());
                        getLexer().nextToken();
                        break;
                    case LITERAL_FLOAT:
                        sqlExpr = new SQLNumberExpr(getLexer().decimalValue());
                        getLexer().nextToken();
                        break;
                    case IDENTIFIER: // 当+号后面为字段的情况
                        sqlExpr = new SQLIdentifierExpr(getLexer().getLiterals());
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Plus, sqlExpr);
                        getLexer().nextToken();
                        break;
                    case LEFT_PAREN:
                        getLexer().nextToken();
                        sqlExpr = expr();
                        accept(Token.RIGHT_PAREN);
                        sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Plus, sqlExpr);
                        break;
                    default:
                        throw new ParserUnsupportedException(getLexer().getToken());
                }
                break;
            case TILDE:
                getLexer().nextToken();
                sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Compl, expr());
                break;
            case QUESTION:
                getLexer().nextToken();
                SQLVariantRefExpr quesVarRefExpr = new SQLVariantRefExpr("?");
                quesVarRefExpr.setIndex(getLexer().nextVarIndex());
                sqlExpr = quesVarRefExpr;
                break;
            case LEFT:
                sqlExpr = new SQLIdentifierExpr("LEFT");
                getLexer().nextToken();
                break;
            case RIGHT:
                sqlExpr = new SQLIdentifierExpr("RIGHT");
                getLexer().nextToken();
                break;
            case DATABASE:
                sqlExpr = new SQLIdentifierExpr("DATABASE");
                getLexer().nextToken();
                break;
            case LOCK:
                sqlExpr = new SQLIdentifierExpr("LOCK");
                getLexer().nextToken();
                break;
            case NULL:
                sqlExpr = new SQLNullExpr();
                getLexer().nextToken();
                break;
            case BANG:
                getLexer().nextToken();
                SQLExpr bangExpr = primary();
                sqlExpr = new SQLUnaryExpr(SQLUnaryOperator.Not, bangExpr);
                break;
            case LITERAL_HEX:
                sqlExpr = new SQLHexExpr(getLexer().getTerm().getValue());
                getLexer().nextToken();
                break;
            case INTERVAL:
                sqlExpr = parseInterval();
                break;
            case COLON:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                    sqlExpr = new SQLVariantRefExpr(":\"" + getLexer().getLiterals() + "\"");
                    getLexer().nextToken();
                }
                break;
            case ANY:
                sqlExpr = parseAny();
                break;
            case SOME:
                sqlExpr = parseSome();
                break;
            case ALL:
                sqlExpr = parseAll();
                break;
            case LITERAL_ALIAS:
                sqlExpr = new SQLIdentifierExpr('"' + getLexer().getLiterals() + '"');
                getLexer().nextToken();
                break;
            case EOF:
                throw new ParserException(getLexer());
            case TRUE:
                getLexer().nextToken();
                sqlExpr = new SQLBooleanExpr(true);
                break;
            case FALSE:
                getLexer().nextToken();
                sqlExpr = new SQLBooleanExpr(false);
                break;
            default:
                throw new ParserException(getLexer(), token);
        }
        return primaryRest(sqlExpr);
    }

    public SQLExpr exprRest(SQLExpr expr) {
        expr = bitXorRest(expr);
        expr = multiplicativeRest(expr);
        expr = additiveRest(expr);
        expr = shiftRest(expr);
        expr = bitAndRest(expr);
        expr = bitOrRest(expr);
        expr = inRest(expr);
        expr = relationalRest(expr);
        expr = equalityRest(expr);
        expr = andRest(expr);
        expr = orRest(expr);
        return expr;
    }

    public SQLExpr bitXorRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.CARET)) {
            getLexer().nextToken();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseXor, primary(), getDbType());
            expr = bitXorRest(expr);
        }
        return expr;
    }

    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Multiply, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.SLASH)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Divide, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        } else if (getLexer().equalToken(Token.PERCENT)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitXorRest(primary());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp, getDbType());
            expr = multiplicativeRest(expr);
        }
        return expr;
    }

    protected SQLExpr parseAll() {
        SQLAllExpr result = new SQLAllExpr();
        getLexer().nextToken();
        accept(Token.LEFT_PAREN);
        SQLSelect allSubQuery = createSelectParser().select();
        result.setSubQuery(allSubQuery);
        accept(Token.RIGHT_PAREN);
        allSubQuery.setParent(result);
        return result;
    }

    protected SQLExpr parseSome() {
        SQLSomeExpr result = new SQLSomeExpr();
        getLexer().nextToken();
        accept(Token.LEFT_PAREN);
        SQLSelect someSubQuery = createSelectParser().select();
        result.setSubQuery(someSubQuery);
        accept(Token.RIGHT_PAREN);
        someSubQuery.setParent(result);
        return result;
    }

    protected SQLExpr parseAny() {
        getLexer().nextToken();
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            accept(Token.LEFT_PAREN);
            if (getLexer().equalToken(Token.IDENTIFIER)) {
                SQLExpr expr = this.expr();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("ANY");
                methodInvokeExpr.addParameter(expr);
                accept(Token.RIGHT_PAREN);
                return methodInvokeExpr;
            }
            SQLAnyExpr anyExpr = new SQLAnyExpr();
            SQLSelect anySubQuery = createSelectParser().select();
            anyExpr.setSubQuery(anySubQuery);
            accept(Token.RIGHT_PAREN);
            anySubQuery.setParent(anyExpr);
            return anyExpr;
        } else {
            return new SQLIdentifierExpr("ANY");
        }
    }

    protected SQLExpr parseInterval() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    public SQLSelectParser createSelectParser() {
        return new SQLSelectParser(this);
    }

    public SQLExpr primaryRest(SQLExpr expr) {
        if (null == expr) {
            throw new IllegalArgumentException("expr");
        }
        if (getLexer().equalToken(Token.OF)) {
            if (expr instanceof SQLIdentifierExpr) {
                String name = ((SQLIdentifierExpr) expr).getSimpleName();
                if ("CURRENT".equalsIgnoreCase(name)) {
                    getLexer().nextToken();
                    SQLName cursorName = this.name();
                    return new SQLCurrentOfCursorExpr(cursorName);
                }
            }
        }
        if (getLexer().equalToken(Token.DOT)) {
            getLexer().nextToken();
            if (expr instanceof SQLCharExpr) {
                String text = ((SQLCharExpr) expr).getText();
                expr = new SQLIdentifierExpr(text);
            }
            expr = dotRest(expr);
            return primaryRest(expr);
        } else {
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                return methodRest(expr, true);
            }
        }
        return expr;
    }

    protected SQLExpr methodRest(final SQLExpr expr, final boolean acceptLeftParen) {
        if (acceptLeftParen) {
            accept(Token.LEFT_PAREN);
        }
        if (expr instanceof SQLName || expr instanceof SQLDefaultExpr) {
            String methodName;
            SQLMethodInvokeExpr methodInvokeExpr;
            if (expr instanceof SQLPropertyExpr) {
                methodName = ((SQLPropertyExpr) expr).getSimpleName();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName, ((SQLPropertyExpr) expr).getOwner());
            } else {
                methodName = expr.toString();
                methodInvokeExpr = new SQLMethodInvokeExpr(methodName);
            }
            if (aggregateFunctions.contains(methodName)) {
                return parseAggregateExpr(methodName);
            }
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
            }
            accept(Token.RIGHT_PAREN);
            if (getLexer().equalToken(Token.OVER)) {
                SQLAggregateExpr aggregateExpr = new SQLAggregateExpr(methodName);
                aggregateExpr.getArguments().addAll(methodInvokeExpr.getParameters());
                over(aggregateExpr);
                return primaryRest(aggregateExpr);
            }
            return primaryRest(methodInvokeExpr);
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    protected SQLExpr dotRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.STAR)) {
            getLexer().nextToken();
            expr = new SQLPropertyExpr(expr, "*");
        } else {
            String name;
            if (getLexer().equalToken(Token.IDENTIFIER) || getLexer().equalToken(Token.LITERAL_CHARS) || getLexer().equalToken(Token.LITERAL_ALIAS)) {
                name = getLexer().getLiterals();
                getLexer().nextToken();
            } else if (getLexer().containsToken()) {
                name = getLexer().getLiterals();
                getLexer().nextToken();
            } else {
                throw new ParserException(getLexer());
            }
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(name, expr);
                if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                    getLexer().nextToken();
                } else {
                    if (getLexer().equalToken(Token.PLUS)) {
                        methodInvokeExpr.addParameter(new SQLIdentifierExpr("+"));
                        getLexer().nextToken();
                    } else {
                        methodInvokeExpr.getParameters().addAll(exprList(methodInvokeExpr));
                    }
                    accept(Token.RIGHT_PAREN);
                }
                expr = methodInvokeExpr;
            } else {
                expr = new SQLPropertyExpr(expr, name);
            }
        }
        expr = primaryRest(expr);
        return expr;
    }

    public final void names(final Collection<SQLName> exprCol) {
        if (getLexer().equalToken(Token.RIGHT_BRACE)) {
            return;
        }
        if (getLexer().equalToken(Token.EOF)) {
            return;
        }
        SQLName name = name();
        exprCol.add(name);
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            name = name();
            exprCol.add(name);
        }
    }

    public final List<SQLExpr> exprList(final SQLObject parent) {
        List<SQLExpr> result = new LinkedList<>();
        if (getLexer().equalToken(Token.RIGHT_PAREN) || getLexer().equalToken(Token.RIGHT_BRACKET) || getLexer().equalToken(Token.EOF)) {
            return result;
        }
        result.add(expr(parent));
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            result.add(expr(parent));
        }
        return result;
    }

    public SQLName name() {
        String identifierName;
        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            identifierName = '"' + getLexer().getLiterals() + '"';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            identifierName = getLexer().getLiterals();
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            identifierName = '\'' + getLexer().getLiterals() + '\'';
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.VARIANT)) {
            identifierName = getLexer().getLiterals();
            getLexer().nextToken();
        } else {
            switch (getLexer().getToken()) {
                case MODEL:
                case PCTFREE:
                case INITRANS:
                case MAXTRANS:
                case SEGMENT:
                case CREATION:
                case IMMEDIATE:
                case DEFERRED:
                case STORAGE:
                case NEXT:
                case MINEXTENTS:
                case MAXEXTENTS:
                case MAXSIZE:
                case PCTINCREASE:
                case FLASH_CACHE:
                case CELL_FLASH_CACHE:
                case KEEP:
                case NONE:
                case LOB:
                case STORE:
                case ROW:
                case CHUNK:
                case CACHE:
                case NOCACHE:
                case LOGGING:
                case NOCOMPRESS:
                case KEEP_DUPLICATES:
                case EXCEPTIONS:
                case PURGE:
                case INITIALLY:
                case END:
                case COMMENT:
                case ENABLE:
                case DISABLE:
                case SEQUENCE:
                case USER:
                case ANALYZE:
                case OPTIMIZE:
                case GRANT:
                case REVOKE:
                //binary有很多含义，getLexer()识别了这个token，实际上应该当做普通IDENTIFIER
                case BINARY:
                    identifierName = getLexer().getLiterals();
                    getLexer().nextToken();
                    break;
                default:
                    throw new ParserException(getLexer());
            }
        }
        SQLName name = new SQLIdentifierExpr(identifierName);
        name = nameRest(name);
        return name;
    }

    public SQLName nameRest(SQLName name) {
        if (getLexer().equalToken(Token.DOT)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.KEY)) {
                name = new SQLPropertyExpr(name, "KEY");
                getLexer().nextToken();
                return name;
            }

            if (!getLexer().equalToken(Token.LITERAL_ALIAS) && !getLexer().equalToken(Token.IDENTIFIER)
                && (!getLexer().containsToken())) {
                throw new ParserException(getLexer());
            }

            if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
                name = new SQLPropertyExpr(name, '"' + getLexer().getLiterals() + '"');
            } else {
                name = new SQLPropertyExpr(name, getLexer().getLiterals());
            }
            getLexer().nextToken();
            name = nameRest(name);
        }
        return name;
    }

    protected SQLAggregateExpr parseAggregateExpr(final String methodName) {
        String upperCaseMethodName = methodName.toUpperCase();
        SQLAggregateExpr result;
        if (getLexer().equalToken(Token.ALL)) {
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.ALL);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.DISTINCT)) {
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.DISTINCT);
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("DEDUPLICATION")) { //just for nut
            result = new SQLAggregateExpr(upperCaseMethodName, SQLAggregateOption.DEDUPLICATION);
            getLexer().nextToken();
        } else {
            result = new SQLAggregateExpr(upperCaseMethodName);
        }
        result.getArguments().addAll(exprList(result));
        parseAggregateExprRest(result);
        accept(Token.RIGHT_PAREN);
        if (getLexer().equalToken(Token.OVER)) {
            over(result);
        }
        return result;
    }

    protected void over(final SQLAggregateExpr aggregateExpr) {
        getLexer().nextToken();
        SQLOver over = new SQLOver();
        accept(Token.LEFT_PAREN);
        if (getLexer().equalToken(Token.PARTITION) || getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            accept(Token.BY);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                over.getPartitionBy().addAll(exprList(over));
                accept(Token.RIGHT_PAREN);
            } else {
                over.getPartitionBy().addAll(exprList(over));
            }
        }
        over.setOrderBy(parseOrderBy());
        accept(Token.RIGHT_PAREN);
        aggregateExpr.setOver(over);
    }

    protected SQLAggregateExpr parseAggregateExprRest(final SQLAggregateExpr aggregateExpr) {
        return aggregateExpr;
    }

    public SQLOrderBy parseOrderBy() {
        if (!getLexer().equalToken(Token.ORDER)) {
            return null;
        }
        SQLOrderBy result = new SQLOrderBy();
        getLexer().nextToken();
        accept(Token.BY);
        result.addItem(parseSelectOrderByItem());
        while (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            result.addItem(parseSelectOrderByItem());
        }
        return result;
    }

    public SQLSelectOrderByItem parseSelectOrderByItem() {
        SQLSelectOrderByItem result = new SQLSelectOrderByItem(expr());
        if (getLexer().equalToken(Token.ASC)) {
            getLexer().nextToken();
            result.setType(SQLOrderingSpecification.ASC);
        } else if (getLexer().equalToken(Token.DESC)) {
            getLexer().nextToken();
            result.setType(SQLOrderingSpecification.DESC);
        }
        return result;
    }

    public final SQLExpr bitAndRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.AMP)) {
            getLexer().nextToken();
            SQLExpr rightExp = shift();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseAnd, rightExp, getDbType());
        }
        return expr;
    }

    public final SQLExpr bitOrRest(SQLExpr expr) {
        while (getLexer().equalToken(Token.BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = bitAndRest(shift());
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BitwiseOr, rightExp, getDbType());
            expr = bitAndRest(expr);
        }
        return expr;
    }

    public final SQLExpr equality() {
        return equalityRest(bitOrRest(bitAndRest(shift())));
    }

    public SQLExpr equalityRest(SQLExpr expr) {
        SQLExpr rightExp;
        if (getLexer().equalToken(Token.EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = equalityRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Equality, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = equalityRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
            rightExp = expr();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Assignment, rightExp, getDbType());
        }
        return expr;
    }

    public final SQLExpr inRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.IN)) {
            getLexer().nextToken();
            SQLInListExpr inListExpr = new SQLInListExpr(expr, false);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                inListExpr.getTargetList().addAll(exprList(inListExpr));
                accept(Token.RIGHT_PAREN);
            } else {
                SQLExpr itemExpr = primary();
                itemExpr.setParent(inListExpr);
                inListExpr.getTargetList().add(itemExpr);
            }
            expr = inListExpr;
            if (inListExpr.getTargetList().size() == 1) {
                SQLExpr targetExpr = inListExpr.getTargetList().get(0);
                if (targetExpr instanceof SQLQueryExpr) {
                    SQLInSubQueryExpr inSubQueryExpr = new SQLInSubQueryExpr();
                    inSubQueryExpr.setExpr(inListExpr.getExpr());
                    inSubQueryExpr.setSubQuery(((SQLQueryExpr) targetExpr).getSubQuery());
                    expr = inSubQueryExpr;
                }
            }
        }
        return expr;
    }

    public final SQLExpr additive() {
        return additiveRest(multiplicativeRest(bitXorRest(primary())));
    }

    public SQLExpr additiveRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.PLUS)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Add, rightExp, getDbType());
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_BAR)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Concat, rightExp, getDbType());
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.SUB)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicativeRest(bitXorRest(primary()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Subtract, rightExp, getDbType());
            expr = additiveRest(expr);
        }
        return expr;
    }

    public final SQLExpr shift() {
        return shiftRest(additive());
    }

    public SQLExpr shiftRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.DOUBLE_LT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LeftShift, rightExp, getDbType());
            expr = shiftRest(expr);
        } else if (getLexer().equalToken(Token.DOUBLE_GT)) {
            getLexer().nextToken();
            SQLExpr rightExp = additive();
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RightShift, rightExp, getDbType());
            expr = shiftRest(expr);
        }
        return expr;
    }

    public SQLExpr and() {
        return andRest(relational());
    }

    public SQLExpr andRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.AND) || getLexer().equalToken(Token.DOUBLE_AMP)) {
                getLexer().nextToken();
                SQLExpr rightExp = relational();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanAnd, rightExp, getDbType());
            } else {
                break;
            }
        }
        return expr;
    }

    public SQLExpr or() {
        return orRest(and());
    }

    public SQLExpr orRest(SQLExpr expr) {
        while (true) {
            if (getLexer().equalToken(Token.OR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanOr, rightExp, getDbType());
            } else if (getLexer().equalToken(Token.XOR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanXor, rightExp, getDbType());
            } else {
                break;
            }
        }
        return expr;
    }

    public SQLExpr relational() {
        return relationalRest(equality());
    }

    public SQLExpr relationalRest(SQLExpr expr) {
        SQLExpr rightExp;
        if (getLexer().equalToken(Token.LT)) {
            SQLBinaryOperator op = SQLBinaryOperator.LessThan;
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
                op = SQLBinaryOperator.LessThanOrEqual;
            }
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, op, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_EQ_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrEqualOrGreaterThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.GT)) {
            SQLBinaryOperator op = SQLBinaryOperator.GreaterThan;
            getLexer().nextToken();
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
                op = SQLBinaryOperator.GreaterThanOrEqual;
            }
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, op, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.GT_EQ)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.GreaterThanOrEqual, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_LT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLessThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.BANG_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotGreaterThan, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LT_GT)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.LessThanOrGreater, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            rightExp = bitOrRest(bitAndRest(shift()));
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Like, rightExp, getDbType());
            if (getLexer().equalToken(Token.ESCAPE)) {
                getLexer().nextToken();
                rightExp = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp, getDbType());
            }
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            rightExp = equality();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.RLike, rightExp, getDbType());
        } else if (getLexer().equalToken(Token.NOT)) {
            getLexer().nextToken();
            expr = notRationalRest(expr);
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            SQLExpr beginExpr = bitOrRest(bitAndRest(shift()));
            accept(Token.AND);
            SQLExpr endExpr = bitOrRest(bitAndRest(shift()));
            expr = new SQLBetweenExpr(expr, beginExpr, endExpr);
        } else if (getLexer().equalToken(Token.IS)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.NOT)) {
                getLexer().nextToken();
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.IsNot, rightExpr, getDbType());
            } else {
                SQLExpr rightExpr = primary();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Is, rightExpr, getDbType());
            }
        } else if (getLexer().equalToken(Token.IN)) {
            expr = inRest(expr);
        }
        return expr;
    }

    public SQLExpr notRationalRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.LIKE)) {
            getLexer().nextToken();
            SQLExpr rightExp = equality();
            rightExp = relationalRest(rightExp);
            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotLike, rightExp, getDbType());
            if (getLexer().getToken() == Token.ESCAPE) {
                getLexer().nextToken();
                rightExp = expr();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Escape, rightExp, getDbType());
            }
        } else if (getLexer().getToken() == Token.IN) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            SQLInListExpr inListExpr = new SQLInListExpr(expr, true);
            inListExpr.getTargetList().addAll(exprList(inListExpr));
            expr = inListExpr;
            accept(Token.RIGHT_PAREN);
            if (inListExpr.getTargetList().size() == 1) {
                SQLExpr targetExpr = inListExpr.getTargetList().get(0);
                if (targetExpr instanceof SQLQueryExpr) {
                    SQLInSubQueryExpr inSubQueryExpr = new SQLInSubQueryExpr();
                    inSubQueryExpr.setNot(true);
                    inSubQueryExpr.setExpr(inListExpr.getExpr());
                    inSubQueryExpr.setSubQuery(((SQLQueryExpr) targetExpr).getSubQuery());
                    expr = inSubQueryExpr;
                }
            }
            expr = relationalRest(expr);
            return expr;
        } else if (getLexer().equalToken(Token.BETWEEN)) {
            getLexer().nextToken();
            SQLExpr beginExpr = bitOrRest(bitAndRest(shift()));
            accept(Token.AND);
            SQLExpr endExpr = bitOrRest(bitAndRest(shift()));
            expr = new SQLBetweenExpr(expr, true, beginExpr, endExpr);
            return expr;
        } else if (getLexer().identifierEquals("RLIKE")) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();
            rightExp = relationalRest(rightExp);
            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRLike, rightExp, getDbType());
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        return expr;
    }

    public SQLDataType parseDataType() {
        if (getLexer().equalToken(Token.DEFAULT) || getLexer().equalToken(Token.NOT) || getLexer().equalToken(Token.NULL)) {
            return null;
        }
        String typeName = name().toString();
        if (isCharType(typeName)) {
            SQLCharacterDataType charType = new SQLCharacterDataType(typeName);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                SQLExpr arg = this.expr();
                arg.setParent(charType);
                charType.getArguments().add(arg);
                accept(Token.RIGHT_PAREN);
            }
            return parseCharTypeRest(charType);
        }
        if ("character".equalsIgnoreCase(typeName) && "varying".equalsIgnoreCase(getLexer().getLiterals())) {
            typeName += ' ' + getLexer().getLiterals();
            getLexer().nextToken();
        }
        SQLDataType dataType = new SQLDataTypeImpl(typeName);
        return parseDataTypeRest(dataType);
    }

    protected SQLDataType parseDataTypeRest(final SQLDataType dataType) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            dataType.getArguments().addAll(exprList(dataType));
            accept(Token.RIGHT_PAREN);
        }
        return dataType;
    }

    protected boolean isCharType(final String dataTypeName) {
        return "char".equalsIgnoreCase(dataTypeName)
               || "varchar".equalsIgnoreCase(dataTypeName)
               || "nchar".equalsIgnoreCase(dataTypeName)
               || "nvarchar".equalsIgnoreCase(dataTypeName)
               || "tinytext".equalsIgnoreCase(dataTypeName)
               || "text".equalsIgnoreCase(dataTypeName)
               || "mediumtext".equalsIgnoreCase(dataTypeName)
               || "longtext".equalsIgnoreCase(dataTypeName);
    }

    protected SQLDataType parseCharTypeRest(final SQLCharacterDataType charType) {
        if (getLexer().equalToken(Token.BINARY)) {
            charType.setHasBinary(true);
            getLexer().nextToken();
        }
        if (getLexer().identifierEquals("CHARACTER")) {
            getLexer().nextToken();
            accept(Token.SET);
            if (!getLexer().equalToken(Token.IDENTIFIER) && !getLexer().equalToken(Token.LITERAL_CHARS)) {
                throw new ParserException(getLexer());
            }
            charType.setCharSetName(getLexer().getLiterals());
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.BINARY)) {
            charType.setHasBinary(true);
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (getLexer().getLiterals().equalsIgnoreCase("COLLATE")) {
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException(getLexer());
                }
                charType.setCollate(getLexer().getLiterals());
                getLexer().nextToken();
            }
        }
        return charType;
    }

    public List<SQLCommentHint> parseHints() {
        List<SQLCommentHint> result = new ArrayList<>();
        if (getLexer().equalToken(Token.HINT)) {
            result.add(new SQLCommentHint(getLexer().getLiterals()));
            getLexer().nextToken();
        }
        return result;
    }

    public SQLSelectItem parseSelectItem() {
        SQLExpr expr;
        boolean connectByRoot = false;
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (getLexer().identifierEquals("CONNECT_BY_ROOT")) {
                connectByRoot = true;
                getLexer().nextToken();
            }
            expr = new SQLIdentifierExpr(getLexer().getLiterals());
            getLexer().nextTokenCommaOrRightParen();
            if (!getLexer().equalToken(Token.COMMA)) {
                expr = primaryRest(expr);
                expr = exprRest(expr);
            }
        } else {
            expr = expr();
        }
        return new SQLSelectItem(expr, as(), connectByRoot);
    }
}