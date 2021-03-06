package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.SQLEvalConstants;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 语句解析器工具.
 *
 * @author zhangliang
 */
@AllArgsConstructor
public class ParserUtil {

    @Getter
    private final SQLExprParser exprParser;

    private final ShardingRule shardingRule;

    private final List<Object> parameters;

    private final SQLContext sqlContext;

    @Getter
    private int parametersIndex;

    public Optional<ConditionContext> parseWhere() {
        if (exprParser.getLexer().skipIfEqual(Token.WHERE)) {
            ParseContext parseContext = getParseContext();
            parseConditions(parseContext);
            return Optional.of(parseContext.getCurrentConditionContext());
        }
        return Optional.absent();
    }

    private ParseContext getParseContext() {
        ParseContext result = new ParseContext(1);
        result.setShardingRule(shardingRule);
        for (TableContext each : sqlContext.getTables()) {
            result.setCurrentTable(each.getName(), each.getAlias());
        }
        return result;
    }

    private void parseConditions(final ParseContext parseContext) {
        do {
            parseCondition(parseContext);
        } while (exprParser.getLexer().skipIfEqual(Token.AND));
        if (exprParser.getLexer().equalToken(Token.OR)) {
            throw new ParserUnsupportedException(exprParser.getLexer().getToken());
        }
    }

    private void parseCondition(final ParseContext parseContext) {
        SQLExpr left = getSqlExprWithVariant();
        if (exprParser.getLexer().equalToken(Token.EQ)) {
            parseEqualCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.IN)) {
            parseInCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.BETWEEN)) {
            parseBetweenCondition(parseContext, left);
        } else if (exprParser.getLexer().equalToken(Token.LT) || exprParser.getLexer().equalToken(Token.GT)
                || exprParser.getLexer().equalToken(Token.LT_EQ) || exprParser.getLexer().equalToken(Token.GT_EQ)) {
            parserOtherCondition();
        }
    }

    private void parseEqualCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        SQLExpr right = getSqlExprWithVariant();
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if (1 == sqlContext.getTables().size() || left instanceof SQLPropertyExpr) {
            parseContext.addCondition(left, Condition.BinaryOperator.EQUAL, Collections.singletonList(right), parameters);
        }
    }

    private void parseInCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        exprParser.getLexer().accept(Token.LEFT_PAREN);
        List<SQLExpr> rights = new LinkedList<>();
        do {
            if (exprParser.getLexer().equalToken(Token.COMMA)) {
                exprParser.getLexer().nextToken();
            }
            rights.add(getSqlExprWithVariant());
        } while (!exprParser.getLexer().equalToken(Token.RIGHT_PAREN));
        parseContext.addCondition(left, Condition.BinaryOperator.IN, rights, parameters);
        exprParser.getLexer().nextToken();
    }

    private void parseBetweenCondition(final ParseContext parseContext, final SQLExpr left) {
        exprParser.getLexer().nextToken();
        List<SQLExpr> rights = new LinkedList<>();
        rights.add(getSqlExprWithVariant());
        exprParser.getLexer().accept(Token.AND);
        rights.add(getSqlExprWithVariant());
        parseContext.addCondition(left, Condition.BinaryOperator.BETWEEN, rights, parameters);
    }

    private void parserOtherCondition() {
        exprParser.getLexer().nextToken();
        getSqlExprWithVariant();
    }

    private SQLExpr getSqlExprWithVariant() {
        SQLExpr result = parseSQLExpr();
        if (result instanceof SQLVariantRefExpr) {
            ((SQLVariantRefExpr) result).setIndex(++parametersIndex);
            result.getAttributes().put(SQLEvalConstants.EVAL_VALUE, parameters.get(parametersIndex - 1));
            result.getAttributes().put(SQLEvalConstants.EVAL_VAR_INDEX, parametersIndex - 1);
        }
        return result;
    }

    private SQLExpr parseSQLExpr() {
        String literals = exprParser.getLexer().getLiterals();
        if (exprParser.getLexer().equalToken(Token.IDENTIFIER)) {
            String tableName = sqlContext.getTables().get(0).getName();
            if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(exprParser.getLexer().getCurrentPosition() - literals.length(), literals, tableName));
            }
            exprParser.getLexer().nextToken();
            if (exprParser.getLexer().equalToken(Token.DOT)) {
                exprParser.getLexer().nextToken();
                SQLExpr result = new SQLPropertyExpr(new SQLIdentifierExpr(literals), exprParser.getLexer().getLiterals());
                exprParser.getLexer().nextToken();
                return result;
            }
            return new SQLIdentifierExpr(literals);
        }
        SQLExpr result = getSQLExpr(literals);
        exprParser.getLexer().nextToken();
        return result;
    }

    private SQLExpr getSQLExpr(final String literals) {
        if (exprParser.getLexer().equalToken(Token.VARIANT) || exprParser.getLexer().equalToken(Token.QUESTION)) {
            return new SQLVariantRefExpr("?");
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_CHARS)) {
            return new SQLCharExpr(literals);
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_NCHARS)) {
            return new SQLNCharExpr(literals);
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_INT)) {
            return new SQLIntegerExpr(Integer.parseInt(literals));
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_FLOAT)) {
            return new SQLNumberExpr(Double.parseDouble(literals));
        }
        if (exprParser.getLexer().equalToken(Token.LITERAL_HEX)) {
            return new SQLNumberExpr(Integer.parseInt(literals, 16));
        }
        throw new ParserUnsupportedException(exprParser.getLexer().getToken());
    }
}