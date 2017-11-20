package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.List;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser {

    @Getter
    private final SQLExprParser exprParser;

    @Getter
    private final ShardingRule shardingRule;

    @Getter
    private final List<Object> parameters;

    private int parametersIndex;

    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }

    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public UpdateSQLContext parse() {
        UpdateSQLContext result = new UpdateSQLContext(exprParser.getLexer().getInput());
        exprParser.getLexer().nextToken();
        skipBetweenUpdateAndTable();
        exprParser.parseSingleTable(result);
        parseSetItems(result);
        exprParser.getLexer().skipUntil(Token.WHERE);
        Optional<ConditionContext> conditionContext = new ParserUtil(exprParser, shardingRule, parameters, result, parametersIndex).parseWhere();
        if (conditionContext.isPresent()) {
            result.getConditionContexts().add(conditionContext.get());
        }
        return result;
    }

    protected abstract void skipBetweenUpdateAndTable();

    private void parseSetItems(final UpdateSQLContext sqlContext) {
        exprParser.getLexer().accept(Token.SET);
        do {
            parseSetItem(sqlContext);
        } while (exprParser.getLexer().skipIfEqual(Token.COMMA));
    }

    private void parseSetItem(final UpdateSQLContext sqlContext) {
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