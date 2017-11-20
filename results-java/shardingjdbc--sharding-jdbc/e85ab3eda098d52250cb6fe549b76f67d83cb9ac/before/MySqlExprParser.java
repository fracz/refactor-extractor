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
package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey.Match;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey.On;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey.Option;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalUnit;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr.SearchModifier;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.util.JdbcConstants;

public class MySqlExprParser extends SQLExprParser {

    private static final String[] AGGREGATE_FUNCTIONS = {"MAX", "MIN", "COUNT", "SUM", "AVG", "STDDEV", "GROUP_CONCAT"};

    public MySqlExprParser(final String sql) {
        super(new MySqlLexer(sql), JdbcConstants.MYSQL, AGGREGATE_FUNCTIONS);
        getLexer().nextToken();
    }

    public SQLExpr relationalRest(SQLExpr expr) {
        if (getLexer().identifierEquals("REGEXP")) {
            getLexer().nextToken();
            SQLExpr rightExp = equality();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.RegExp, rightExp, JdbcConstants.MYSQL);
        }

        return super.relationalRest(expr);
    }

    public SQLExpr multiplicativeRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.IDENTIFIER)  && "MOD".equalsIgnoreCase(getLexer().getLiterals())) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.Modulus, rightExp, JdbcConstants.MYSQL);
        }

        return super.multiplicativeRest(expr);
    }

    public SQLExpr notRationalRest(SQLExpr expr) {
        if (getLexer().identifierEquals("REGEXP")) {
            getLexer().nextToken();
            SQLExpr rightExp = primary();

            rightExp = relationalRest(rightExp);

            return new SQLBinaryOpExpr(expr, SQLBinaryOperator.NotRegExp, rightExp, JdbcConstants.MYSQL);
        }

        return super.notRationalRest(expr);
    }

    public SQLExpr primary() {
        final Token tok = getLexer().getToken();

        if (getLexer().identifierEquals("outfile")) {
            getLexer().nextToken();
            SQLExpr file = primary();
            SQLExpr expr = new MySqlOutFileExpr(file);

            return primaryRest(expr);

        }

        switch (tok) {
            case LITERAL_ALIAS:
                String aliasValue = getLexer().getLiterals();
                getLexer().nextToken();
                return primaryRest(new SQLCharExpr(aliasValue));
            case VARIANT:
                SQLVariantRefExpr varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals());
                getLexer().nextToken();
                if (varRefExpr.getName().equalsIgnoreCase("@@global")) {
                    accept(Token.DOT);
                    varRefExpr = new SQLVariantRefExpr(getLexer().getLiterals(), true);
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                } else if (varRefExpr.getName().equals("@@") && getLexer().equalToken(Token.LITERAL_CHARS)) {
                    varRefExpr.setName("@@'" + getLexer().getLiterals() + "'");
                    getLexer().nextToken();
                }
                return primaryRest(varRefExpr);
            case VALUES:
                getLexer().nextToken();
                if (!getLexer().equalToken(Token.LEFT_PAREN)) {
                    throw new ParserException("syntax error, illegal values clause");
                }
                return this.methodRest(new SQLIdentifierExpr("VALUES"), true);
            case BINARY:
                getLexer().nextToken();
                if (getLexer().equalToken(Token.COMMA) || getLexer().equalToken(Token.SEMI) || getLexer().equalToken(Token.EOF)) {
                    return new SQLIdentifierExpr("BINARY");
                } else {
                    SQLUnaryExpr binaryExpr = new SQLUnaryExpr(SQLUnaryOperator.BINARY, expr());
                    return primaryRest(binaryExpr);
                }
            case CACHE:
            case GROUP:
                getLexer().nextToken();
                return primaryRest(new SQLIdentifierExpr(getLexer().getLiterals()));
            default:
                return super.primary();
        }

    }

    public final SQLExpr primaryRest(SQLExpr expr) {
        if (expr == null) {
            throw new IllegalArgumentException("expr");
        }

        if (getLexer().equalToken(Token.LITERAL_CHARS)) {
            if (expr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
                String ident = identExpr.getName();

                if (ident.equalsIgnoreCase("x")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr = new SQLHexExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.equalsIgnoreCase("b")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr = new SQLBinaryExpr(charValue);

                    return primaryRest(expr);
                } else if (ident.startsWith("_")) {
                    String charValue = getLexer().getLiterals();
                    getLexer().nextToken();

                    MySqlCharExpr mysqlCharExpr = new MySqlCharExpr(charValue);
                    mysqlCharExpr.setCharset(identExpr.getName());
                    if (getLexer().identifierEquals("COLLATE")) {
                        getLexer().nextToken();

                        String collate = getLexer().getLiterals();
                        mysqlCharExpr.setCollate(collate);
                        accept(Token.IDENTIFIER);
                    }

                    expr = mysqlCharExpr;

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLCharExpr) {
                SQLMethodInvokeExpr concat = new SQLMethodInvokeExpr("CONCAT");
                concat.addParameter(expr);
                do {
                    String chars = getLexer().getLiterals();
                    concat.addParameter(new SQLCharExpr(chars));
                    getLexer().nextToken();
                } while (getLexer().equalToken(Token.LITERAL_CHARS) || getLexer().equalToken(Token.LITERAL_ALIAS));
                expr = concat;
            }
        } else if (getLexer().equalToken(Token.IDENTIFIER)) {
            if (expr instanceof SQLHexExpr) {
                if ("USING".equalsIgnoreCase(getLexer().getLiterals())) {
                    getLexer().nextToken();
                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException("syntax error, illegal hex");
                    }
                    String charSet = getLexer().getLiterals();
                    getLexer().nextToken();
                    expr.getAttributes().put("USING", charSet);

                    return primaryRest(expr);
                }
            } else if ("COLLATE".equalsIgnoreCase(getLexer().getLiterals())) {
                getLexer().nextToken();

                if (getLexer().equalToken(Token.EQ)) {
                    getLexer().nextToken();
                }

                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException("syntax error");
                }
                String collate = getLexer().getLiterals();
                getLexer().nextToken();
                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.COLLATE, new SQLIdentifierExpr(collate), JdbcConstants.MYSQL);
                return primaryRest(expr);
            } else if (expr instanceof SQLVariantRefExpr) {
                if ("COLLATE".equalsIgnoreCase(getLexer().getLiterals())) {
                    getLexer().nextToken();

                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException("syntax error");
                    }

                    String collate = getLexer().getLiterals();
                    getLexer().nextToken();

                    expr.putAttribute("COLLATE", collate);

                    return primaryRest(expr);
                }
            } else if (expr instanceof SQLIntegerExpr) {
                SQLIntegerExpr intExpr = (SQLIntegerExpr) expr;
                String binaryString = getLexer().getLiterals();
                if (intExpr.getNumber().intValue() == 0 && binaryString.startsWith("b")) {
                    getLexer().nextToken();
                    expr = new SQLBinaryExpr(binaryString.substring(1));

                    return primaryRest(expr);
                }
            }
        }

        if (getLexer().equalToken(Token.LEFT_PAREN) && expr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
            String ident = identExpr.getName();

            if ("EXTRACT".equalsIgnoreCase(ident)) {
                getLexer().nextToken();

                if (!getLexer().equalToken(Token.IDENTIFIER)) {
                    throw new ParserException("syntax error");
                }

                String unitVal = getLexer().getLiterals();
                MySqlIntervalUnit unit = MySqlIntervalUnit.valueOf(unitVal.toUpperCase());
                getLexer().nextToken();

                accept(Token.FROM);

                SQLExpr value = expr();

                MySqlExtractExpr extract = new MySqlExtractExpr();
                extract.setValue(value);
                extract.setUnit(unit);
                accept(Token.RIGHT_PAREN);

                expr = extract;

                return primaryRest(expr);
            } else if ("SUBSTRING".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);
                while (true) {
                    SQLExpr param = expr();
                    methodInvokeExpr.addParameter(param);

                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                    } else if (getLexer().equalToken(Token.FROM)) {
                        getLexer().nextToken();
                        SQLExpr from = expr();
                        methodInvokeExpr.addParameter(from);

                        if (getLexer().equalToken(Token.FOR)) {
                            getLexer().nextToken();
                            SQLExpr forExpr = expr();
                            methodInvokeExpr.addParameter(forExpr);
                        }
                        break;
                    } else if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                        break;
                    } else {
                        throw new ParserException("syntax error");
                    }
                }

                accept(Token.RIGHT_PAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("TRIM".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);

                if (getLexer().equalToken(Token.IDENTIFIER)) {
                    String flagVal = getLexer().getLiterals();
                    if ("LEADING".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "LEADING");
                    } else if ("BOTH".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.getAttributes().put("TRIM_TYPE", "BOTH");
                    } else if ("TRAILING".equalsIgnoreCase(flagVal)) {
                        getLexer().nextToken();
                        methodInvokeExpr.putAttribute("TRIM_TYPE", "TRAILING");
                    }
                }

                SQLExpr param = expr();
                methodInvokeExpr.addParameter(param);

                if (getLexer().equalToken(Token.FROM)) {
                    getLexer().nextToken();
                    SQLExpr from = expr();
                    methodInvokeExpr.putAttribute("FROM", from);
                }

                accept(Token.RIGHT_PAREN);
                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("MATCH".equalsIgnoreCase(ident)) {
                getLexer().nextToken();
                MySqlMatchAgainstExpr matchAgainstExpr = new MySqlMatchAgainstExpr();

                if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                    getLexer().nextToken();
                } else {
                    exprList(matchAgainstExpr.getColumns(), matchAgainstExpr);
                    accept(Token.RIGHT_PAREN);
                }

                acceptIdentifier("AGAINST");

                accept(Token.LEFT_PAREN);
                SQLExpr against = primary();
                matchAgainstExpr.setAgainst(against);

                if (getLexer().equalToken(Token.IN)) {
                    getLexer().nextToken();
                    if (getLexer().identifierEquals("NATURAL")) {
                        getLexer().nextToken();
                        acceptIdentifier("LANGUAGE");
                        acceptIdentifier("MODE");
                        if (getLexer().equalToken(Token.WITH)) {
                            getLexer().nextToken();
                            acceptIdentifier("QUERY");
                            acceptIdentifier("EXPANSION");
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION);
                        } else {
                            matchAgainstExpr.setSearchModifier(SearchModifier.IN_NATURAL_LANGUAGE_MODE);
                        }
                    } else if (getLexer().identifierEquals("BOOLEAN")) {
                        getLexer().nextToken();
                        acceptIdentifier("MODE");
                        matchAgainstExpr.setSearchModifier(SearchModifier.IN_BOOLEAN_MODE);
                    } else {
                        throw new ParserException("TODO");
                    }
                } else if (getLexer().equalToken(Token.WITH)) {
                    throw new ParserException("TODO");
                }

                accept(Token.RIGHT_PAREN);

                expr = matchAgainstExpr;

                return primaryRest(expr);
            } else if (("CONVERT".equalsIgnoreCase(ident))||("CHAR".equalsIgnoreCase(ident))) {
                getLexer().nextToken();
                SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(ident);

                if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                    exprList(methodInvokeExpr.getParameters(), methodInvokeExpr);
                }

                if (getLexer().identifierEquals("USING")) {
                    getLexer().nextToken();
                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException("syntax error");
                    }
                    String charset = getLexer().getLiterals();
                    getLexer().nextToken();
                    methodInvokeExpr.putAttribute("USING", charset);
                }

                accept(Token.RIGHT_PAREN);

                expr = methodInvokeExpr;

                return primaryRest(expr);
            } else if ("POSITION".equalsIgnoreCase(ident)) {
                accept(Token.LEFT_PAREN);
                SQLExpr subStr = this.primary();
                accept(Token.IN);
                SQLExpr str = this.expr();
                accept(Token.RIGHT_PAREN);

                SQLMethodInvokeExpr locate = new SQLMethodInvokeExpr("LOCATE");
                locate.addParameter(subStr);
                locate.addParameter(str);

                expr = locate;
                return primaryRest(expr);
            }
        }

        if (getLexer().equalToken(Token.VARIANT) && "@".equals(getLexer().getLiterals())) {
            getLexer().nextToken();
            MySqlUserName userName = new MySqlUserName();
            if (expr instanceof SQLCharExpr) {
                userName.setUserName(expr.toString());
            } else {
                userName.setUserName(((SQLIdentifierExpr) expr).getName());
            }

            if (getLexer().equalToken(Token.LITERAL_CHARS)) {
                userName.setHost("'" + getLexer().getLiterals() + "'");
            } else {
                userName.setHost(getLexer().getLiterals());
            }
            getLexer().nextToken();
            return userName;
        }

        if (getLexer().equalToken(Token.ERROR)) {
            throw new ParserException("syntax error, token: " + getLexer().getToken() + " " + getLexer().getLiterals() + ", currentPosition : " + getLexer().getCurrentPosition());
        }

        return super.primaryRest(expr);
    }

    public SQLSelectParser createSelectParser() {
        return new MySqlSelectParser(this);
    }

    protected SQLExpr parseInterval() {
        accept(Token.INTERVAL);

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("INTERVAL");
            if (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                exprList(methodInvokeExpr.getParameters(), methodInvokeExpr);
            }

            accept(Token.RIGHT_PAREN);

            return primaryRest(methodInvokeExpr);
        } else {
            SQLExpr value = expr();

            if (!getLexer().equalToken(Token.IDENTIFIER)) {
                throw new ParserException("Syntax error");
            }

            String unit = getLexer().getLiterals();
            getLexer().nextToken();

            MySqlIntervalExpr intervalExpr = new MySqlIntervalExpr();
            intervalExpr.setValue(value);
            intervalExpr.setUnit(MySqlIntervalUnit.valueOf(unit.toUpperCase()));

            return intervalExpr;
        }
    }

    public SQLColumnDefinition parseColumn() {
        MySqlSQLColumnDefinition column = new MySqlSQLColumnDefinition();
        column.setName(name());
        column.setDataType(parseDataType());

        return parseColumnRest(column);
    }

    public SQLColumnDefinition parseColumnRest(SQLColumnDefinition column) {
        if (getLexer().equalToken(Token.ON)) {
            getLexer().nextToken();
            accept(Token.UPDATE);
            SQLExpr expr = this.expr();
            ((MySqlSQLColumnDefinition) column).setOnUpdate(expr);
        }

        if (getLexer().identifierEquals("AUTO_INCREMENT")) {
            getLexer().nextToken();
            if (column instanceof MySqlSQLColumnDefinition) {
                ((MySqlSQLColumnDefinition) column).setAutoIncrement(true);
            }
            return parseColumnRest(column);
        }

        if (getLexer().identifierEquals("precision") && column.getDataType().getName().equalsIgnoreCase("double")) {
            getLexer().nextToken();
        }

        if (getLexer().identifierEquals("PARTITION")) {
            throw new ParserException("syntax error " + getLexer().getToken() + " " + getLexer().getLiterals());
        }

        if (getLexer().identifierEquals("STORAGE")) {
            getLexer().nextToken();
            SQLExpr expr = expr();
            if (column instanceof MySqlSQLColumnDefinition) {
                ((MySqlSQLColumnDefinition) column).setStorage(expr);
            }
        }

        super.parseColumnRest(column);

        return column;
    }

    protected SQLDataType parseDataTypeRest(SQLDataType dataType) {
        super.parseDataTypeRest(dataType);

        if (getLexer().identifierEquals("UNSIGNED")) {
            getLexer().nextToken();
            dataType.getAttributes().put("UNSIGNED", true);
        }

        if (getLexer().identifierEquals("ZEROFILL")) {
            getLexer().nextToken();
            dataType.getAttributes().put("ZEROFILL", true);
        }

        return dataType;
    }

    public SQLExpr orRest(SQLExpr expr) {

        while (true) {
            if (getLexer().equalToken(Token.OR) || getLexer().equalToken(Token.DOUBLE_BAR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();

                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanOr, rightExp, JdbcConstants.MYSQL);
            } else if (getLexer().equalToken(Token.XOR)) {
                getLexer().nextToken();
                SQLExpr rightExp = and();

                expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.BooleanXor, rightExp, JdbcConstants.MYSQL);
            } else {
                break;
            }
        }

        return expr;
    }

    public SQLExpr additiveRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.PLUS)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicative();

            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Add, rightExp, JdbcConstants.MYSQL);
            expr = additiveRest(expr);
        } else if (getLexer().equalToken(Token.SUB)) {
            getLexer().nextToken();
            SQLExpr rightExp = multiplicative();

            expr = new SQLBinaryOpExpr(expr, SQLBinaryOperator.Subtract, rightExp, JdbcConstants.MYSQL);
            expr = additiveRest(expr);
        }

        return expr;
    }

    public SQLAssignItem parseAssignItem() {
        SQLAssignItem item = new SQLAssignItem();

        SQLExpr var = primary();

        String ident = null;
        if (var instanceof SQLIdentifierExpr) {
            ident = ((SQLIdentifierExpr) var).getName();

            if ("GLOBAL".equalsIgnoreCase(ident)) {
                ident = getLexer().getLiterals();
                getLexer().nextToken();
                var = new SQLVariantRefExpr(ident, true);
            } else if ("SESSION".equalsIgnoreCase(ident)) {
                ident = getLexer().getLiterals();
                getLexer().nextToken();
                var = new SQLVariantRefExpr(ident, false);
            } else {
                var = new SQLVariantRefExpr(ident);
            }
        }
        if ("NAMES".equalsIgnoreCase(ident)) {
        } else if ("CHARACTER".equalsIgnoreCase(ident)) {
            var = new SQLIdentifierExpr("CHARACTER SET");
            accept(Token.SET);
            if (getLexer().equalToken(Token.EQ)) {
                getLexer().nextToken();
            }
        } else {
            if (getLexer().equalToken(Token.COLON_EQ)) {
                getLexer().nextToken();
            } else {
                accept(Token.EQ);
            }
        }
        item.setValue(this.expr());
        item.setTarget(var);
        return item;
    }

    public SQLName nameRest(SQLName name) {
        if (getLexer().equalToken(Token.VARIANT) && "@".equals(getLexer().getLiterals())) {
            getLexer().nextToken();
            MySqlUserName userName = new MySqlUserName();
            userName.setUserName(((SQLIdentifierExpr) name).getName());

            if (getLexer().equalToken(Token.LITERAL_CHARS)) {
                userName.setHost("'" + getLexer().getLiterals() + "'");
            } else {
                userName.setHost(getLexer().getLiterals());
            }
            getLexer().nextToken();
            return userName;
        }
        return super.nameRest(name);
    }

    public Limit parseLimit() {
        if (getLexer().equalToken(Token.LIMIT)) {
            getLexer().nextToken();

            MySqlSelectQueryBlock.Limit limit = new MySqlSelectQueryBlock.Limit();

            SQLExpr temp = this.expr();
            if (getLexer().equalToken(Token.COMMA)) {
                limit.setOffset(temp);
                getLexer().nextToken();
                limit.setRowCount(this.expr());
            } else if (getLexer().identifierEquals("OFFSET")) {
                limit.setRowCount(temp);
                getLexer().nextToken();
                limit.setOffset(this.expr());
            } else {
                limit.setRowCount(temp);
            }
            return limit;
        }

        return null;
    }

    @Override
    public MySqlPrimaryKey parsePrimaryKey() {
        accept(Token.PRIMARY);
        accept(Token.KEY);

        MySqlPrimaryKey primaryKey = new MySqlPrimaryKey();

        if (getLexer().identifierEquals("USING")) {
            getLexer().nextToken();
            primaryKey.setIndexType(getLexer().getLiterals());
            getLexer().nextToken();
        }

        accept(Token.LEFT_PAREN);
        while (true) {
            primaryKey.getColumns().add(this.expr());
            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            } else {
                getLexer().nextToken();
            }
        }
        accept(Token.RIGHT_PAREN);

        return primaryKey;
    }

    public MySqlUnique parseUnique() {
        accept(Token.UNIQUE);

        if (getLexer().equalToken(Token.KEY)) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
        }

        MySqlUnique unique = new MySqlUnique();

        if (!getLexer().equalToken(Token.LEFT_PAREN)) {
            SQLName indexName = name();
            unique.setIndexName(indexName);
        }

        accept(Token.LEFT_PAREN);
        while (true) {
            unique.getColumns().add(this.expr());
            if (!getLexer().equalToken(Token.COMMA)) {
                break;
            } else {
                getLexer().nextToken();
            }
        }
        accept(Token.RIGHT_PAREN);

        if (getLexer().identifierEquals("USING")) {
            getLexer().nextToken();
            unique.setIndexType(getLexer().getLiterals());
            getLexer().nextToken();
        }

        return unique;
    }

    public MysqlForeignKey parseForeignKey() {
        accept(Token.FOREIGN);
        accept(Token.KEY);

        MysqlForeignKey fk = new MysqlForeignKey();

        if (!getLexer().equalToken(Token.LEFT_PAREN)) {
            SQLName indexName = name();
            fk.setIndexName(indexName);
        }

        accept(Token.LEFT_PAREN);
        this.names(fk.getReferencingColumns());
        accept(Token.RIGHT_PAREN);

        accept(Token.REFERENCES);

        fk.setReferencedTableName(this.name());

        accept(Token.LEFT_PAREN);
        this.names(fk.getReferencedColumns());
        accept(Token.RIGHT_PAREN);

        if (getLexer().identifierEquals("MATCH")) {
            if (getLexer().identifierEquals("FULL")) {
                fk.setReferenceMatch(Match.FULL);
            } else if (getLexer().identifierEquals("PARTIAL")) {
                fk.setReferenceMatch(Match.PARTIAL);
            } else if (getLexer().identifierEquals("SIMPLE")) {
                fk.setReferenceMatch(Match.SIMPLE);
            }
        }

        if (getLexer().equalToken(Token.ON)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.DELETE)) {
                fk.setReferenceOn(On.DELETE);
            } else if (getLexer().equalToken(Token.UPDATE)) {
                fk.setReferenceOn(On.UPDATE);
            } else {
                throw new ParserException("syntax error, expect DELETE or UPDATE, actual " + getLexer().getToken() + " " + getLexer().getLiterals());
            }
            getLexer().nextToken();

            if (getLexer().equalToken(Token.RESTRICT)) {
                fk.setReferenceOption(Option.RESTRICT);
            } else if (getLexer().identifierEquals("CASCADE")) {
                fk.setReferenceOption(Option.CASCADE);
            } else if (getLexer().equalToken(Token.SET)) {
                accept(Token.NULL);
                fk.setReferenceOption(Option.SET_NULL);
            } else if (getLexer().identifierEquals("ON")) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("ACTION")) {
                    fk.setReferenceOption(Option.NO_ACTION);
                } else {
                    throw new ParserException("syntax error, expect ACTION, actual " + getLexer().getToken() + " " + getLexer().getLiterals());
                }
            }
            getLexer().nextToken();
        }
        return fk;
    }

    protected SQLAggregateExpr parseAggregateExprRest(SQLAggregateExpr aggregateExpr) {
        if (getLexer().equalToken(Token.ORDER)) {
            SQLOrderBy orderBy = this.parseOrderBy();
            aggregateExpr.putAttribute("ORDER BY", orderBy);
        }
        if (getLexer().identifierEquals("SEPARATOR")) {
            getLexer().nextToken();

            SQLExpr seperator = this.primary();

            aggregateExpr.putAttribute("SEPARATOR", seperator);
        }
        return aggregateExpr;
    }

    public MySqlSelectGroupByExpr parseSelectGroupByItem() {
        MySqlSelectGroupByExpr item = new MySqlSelectGroupByExpr();

        item.setExpr(expr());

        if (getLexer().equalToken(Token.ASC)) {
            getLexer().nextToken();
            item.setType(SQLOrderingSpecification.ASC);
        } else if (getLexer().equalToken(Token.DESC)) {
            getLexer().nextToken();
            item.setType(SQLOrderingSpecification.DESC);
        }

        return item;
    }
}