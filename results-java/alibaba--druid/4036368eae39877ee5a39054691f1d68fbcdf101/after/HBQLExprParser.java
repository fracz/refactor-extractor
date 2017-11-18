package com.alibaba.druid.hdriver.impl.hbql.parser;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class HBQLExprParser extends SQLExprParser {

    public HBQLExprParser(String sql) throws ParserException{
        super(new HBQLLexer(sql));
        this.lexer.nextToken();
    }

    public HBQLExprParser(Lexer lexer){
        super(lexer);
    }
}