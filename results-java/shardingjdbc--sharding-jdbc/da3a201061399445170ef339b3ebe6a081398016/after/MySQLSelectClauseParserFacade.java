package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractSelectClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;

/**
 * MySQL的SELECT从句解析器门面类.
 *
 * @author zhangliang
 */
public final class MySQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {

    public MySQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLDistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine),
                new MySQLTableClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine), new MySQLGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new MySQLOrderByClauseParser(lexerEngine), new MySQLSelectRestClauseParser(lexerEngine));
    }
}