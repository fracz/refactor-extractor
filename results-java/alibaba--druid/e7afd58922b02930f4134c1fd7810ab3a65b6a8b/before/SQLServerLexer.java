package com.alibaba.druid.sql.dialect.sqlserver.parser;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.sql.parser.Keywords;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;

public class SQLServerLexer extends Lexer {

    public final static Keywords DEFAULT_SQL_SERVER_KEYWORDS;

    static {
        Map<String, Token> map = new HashMap<String, Token>();
        map.put("EXISTS", Token.EXISTS);
        map.put("THEN", Token.THEN);
        map.put("AS", Token.AS);
        map.put("GROUP", Token.GROUP);
        map.put("BY", Token.BY);
        map.put("HAVING", Token.HAVING);
        map.put("DELETE", Token.DELETE);
        map.put("ORDER", Token.ORDER);
        map.put("INDEX", Token.INDEX);
        map.put("FOR", Token.FOR);
        map.put("SCHEMA", Token.SCHEMA);
        map.put("FOREIGN", Token.FOREIGN);
        map.put("REFERENCE", Token.REFERENCE);
        map.put("REFERENCES", Token.REFERENCES);
        map.put("CHECK", Token.CHECK);
        map.put("PRIMARY", Token.PRIMARY);
        map.put("KEY", Token.KEY);
        map.put("CONSTRAINT", Token.CONSTRAINT);
        map.put("DEFAULT", Token.DEFAULT);
        map.put("VIEW", Token.VIEW);
        map.put("CREATE", Token.CREATE);
        map.put("VALUES", Token.VALUES);
        map.put("ALTER", Token.ALTER);
        map.put("TABLE", Token.TABLE);
        map.put("DROP", Token.DROP);
        map.put("SET", Token.SET);
        map.put("INTO", Token.INTO);
        map.put("UPDATE", Token.UPDATE);
        map.put("NULL", Token.NULL);
        map.put("IS", Token.IS);
        map.put("NOT", Token.NOT);
        map.put("SELECT", Token.SELECT);
        map.put("INSERT", Token.INSERT);
        map.put("FROM", Token.FROM);
        map.put("WHERE", Token.WHERE);
        map.put("AND", Token.AND);
        map.put("OR", Token.OR);
        map.put("XOR", Token.XOR);
        map.put("DISTINCT", Token.DISTINCT);
        map.put("UNIQUE", Token.UNIQUE);
        map.put("ALL", Token.ALL);
        map.put("UNION", Token.UNION);
        map.put("INTERSECT", Token.INTERSECT);
        map.put("MINUS", Token.MINUS);
        map.put("INNER", Token.INNER);
        map.put("LEFT", Token.LEFT);
        map.put("RIGHT", Token.RIGHT);
        map.put("FULL", Token.FULL);
        map.put("ON", Token.ON);
        map.put("OUTER", Token.OUTER);
        map.put("JOIN", Token.JOIN);
        map.put("NEW", Token.NEW);
        map.put("CASE", Token.CASE);
        map.put("WHEN", Token.WHEN);
        map.put("END", Token.END);
        map.put("WHEN", Token.WHEN);
        map.put("ELSE", Token.ELSE);
        map.put("EXISTS", Token.EXISTS);
        map.put("CAST", Token.CAST);
        map.put("IN", Token.IN);
        map.put("ASC", Token.ASC);
        map.put("DESC", Token.DESC);
        map.put("LIKE", Token.LIKE);
        map.put("ESCAPE", Token.ESCAPE);
        map.put("BETWEEN", Token.BETWEEN);
        map.put("INTERVAL", Token.INTERVAL);
        map.put("LOCK", Token.LOCK);
        map.put("SOME", Token.SOME);
        map.put("ANY", Token.ANY);

        map.put("WITH", Token.WITH);
        map.put("CURSOR", Token.CURSOR);
        map.put("TOP", Token.TOP);
        map.put("USE", Token.USE);

        DEFAULT_SQL_SERVER_KEYWORDS = new Keywords(map);
    }

    public SQLServerLexer(char[] input, int inputLength, boolean skipComment){
        super(input, inputLength, skipComment);
        super.keywods = DEFAULT_SQL_SERVER_KEYWORDS;
    }

    public SQLServerLexer(String input){
        super(input);
        super.keywods = DEFAULT_SQL_SERVER_KEYWORDS;
    }
}