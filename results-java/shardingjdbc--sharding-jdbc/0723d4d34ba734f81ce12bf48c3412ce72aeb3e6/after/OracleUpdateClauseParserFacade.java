package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleTableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;

/**
 * Oracle的UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class OracleUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {

    public OracleUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableClauseParser(shardingRule, lexerEngine), new SetItemsClauseParser(lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}