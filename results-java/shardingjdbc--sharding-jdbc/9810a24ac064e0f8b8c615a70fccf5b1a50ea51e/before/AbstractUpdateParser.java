package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractUpdateParser {

    private final SQLExprParser exprParser;

    private final UpdateSQLContext sqlContext;

    private int parametersIndex;

    public AbstractUpdateParser(final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new UpdateSQLContext(exprParser.getLexer().getInput());
    }

    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public UpdateSQLContext parse() {
        exprParser.getLexer().nextToken();
        skipBetweenUpdateAndTable();
        exprParser.parseSingleTable(sqlContext);
        parseSetItems();
        exprParser.getLexer().skipUntil(Token.WHERE);
        exprParser.setParametersIndex(parametersIndex);
        Optional<ConditionContext> conditionContext = exprParser.parseWhere(sqlContext);
        if (conditionContext.isPresent()) {
            sqlContext.getConditionContexts().add(conditionContext.get());
        }
        return sqlContext;
    }

    protected abstract void skipBetweenUpdateAndTable();

    private void parseSetItems() {
        exprParser.getLexer().accept(Token.SET);
        do {
            parseSetItem();
        } while (exprParser.getLexer().skipIfEqual(Token.COMMA));
    }

    private void parseSetItem() {
        if (exprParser.getLexer().equalToken(Token.LEFT_PAREN)) {
            exprParser.getLexer().skipParentheses();
        } else {
            int beginPosition = exprParser.getLexer().getCurrentPosition();
            String literals = exprParser.getLexer().getLiterals();
            exprParser.getLexer().nextToken();
            String tableName = sqlContext.getTables().get(0).getName();
            if (exprParser.getLexer().skipIfEqual(Token.DOT) && tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals, tableName));
                exprParser.getLexer().nextToken();
            }
        }
        exprParser.getLexer().skipIfEqual(Token.EQ, Token.COLON_EQ);

        // TODO 解析condition expr
//        exprParser.parseComparisonCondition(sqlContext, new ParseContext(0));
//        parametersIndex = exprParser.getParametersIndex();



        SQLExpr value = exprParser.expr();
        if (value instanceof SQLBinaryOpExpr) {
            if (((SQLBinaryOpExpr) value).getLeft() instanceof SQLVariantRefExpr) {
                parametersIndex++;
            }
            if (((SQLBinaryOpExpr) value).getRight() instanceof SQLVariantRefExpr) {
                parametersIndex++;
            }
            // TODO 二元操作替换table token
        } else if (value instanceof SQLListExpr) {
            for (SQLExpr each : ((SQLListExpr) value).getItems()) {
                if (each instanceof SQLVariantRefExpr) {
                    parametersIndex++;
                }
            }
        } else if (value instanceof SQLVariantRefExpr) {
            parametersIndex++;
        }
    }
}