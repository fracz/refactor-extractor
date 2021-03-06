package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;

/**
 * Oracle 表从句解析器.
 *
 * @author zhangliang
 */
public final class OracleTableReferencesClauseParser extends TableReferencesClauseParser {

    public OracleTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }

    @Override
    protected void parseTableSource(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.ONLY)) {
            getLexerEngine().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression(sqlStatement, isSingleTableOnly);
            getLexerEngine().skipIfEqual(Symbol.RIGHT_PAREN);
            parseFlashbackQueryClause();
        } else {
            parseQueryTableExpression(sqlStatement, isSingleTableOnly);
            parsePivotClause(sqlStatement);
            parseFlashbackQueryClause();
        }
    }

    private void parseQueryTableExpression(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parseDbLink();
        parsePartitionExtensionClause();
        parseSampleClause();
    }

    private void parseDbLink() {
        if (getLexerEngine().equalAny(Symbol.AT)) {
            throw new SQLParsingUnsupportedException(getLexerEngine().getCurrentToken().getType());
        }
    }

    private void parsePartitionExtensionClause() {
        if (getLexerEngine().equalAny(OracleKeyword.PARTITION, OracleKeyword.SUBPARTITION)) {
            throw new SQLParsingUnsupportedException(getLexerEngine().getCurrentToken().getType());
        }
    }

    private void parseSampleClause() {
        if (getLexerEngine().equalAny(OracleKeyword.SAMPLE)) {
            throw new SQLParsingUnsupportedException(getLexerEngine().getCurrentToken().getType());
        }
    }

    private void parseFlashbackQueryClause() {
        if (isFlashbackQueryClauseForVersions() || isFlashbackQueryClauseForAs()) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        }
    }

    private boolean isFlashbackQueryClauseForVersions() {
        return getLexerEngine().skipIfEqual(OracleKeyword.VERSIONS) && getLexerEngine().skipIfEqual(DefaultKeyword.BETWEEN);
    }

    private boolean isFlashbackQueryClauseForAs() {
        return getLexerEngine().skipIfEqual(DefaultKeyword.AS) && getLexerEngine().skipIfEqual(DefaultKeyword.OF)
                && (getLexerEngine().skipIfEqual(OracleKeyword.SCN) || getLexerEngine().skipIfEqual(OracleKeyword.TIMESTAMP));
    }

    private void parsePivotClause(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.PIVOT)) {
            getLexerEngine().skipIfEqual(OracleKeyword.XML);
            getLexerEngine().skipParentheses(sqlStatement);
        } else if (getLexerEngine().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getLexerEngine().skipIfEqual(OracleKeyword.INCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            } else if (getLexerEngine().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            }
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
}