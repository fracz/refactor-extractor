package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertIntoClauseParser;

/**
 * PostgreSQL的INSERT从句解析器门面类.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {

    public PostgreSQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLInsertIntoClauseParser(shardingRule, lexerEngine));
    }
}