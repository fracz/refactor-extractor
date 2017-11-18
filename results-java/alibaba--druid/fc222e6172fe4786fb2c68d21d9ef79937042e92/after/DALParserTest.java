/**
 * (created at 2011-5-20)
 */
package com.alibaba.druid.bvt.sql.cobar;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDescribeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionIsolationLevelStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.Token;


/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class DALParserTest extends TestCase {

    public void testdesc() throws Exception {
        String sql = "desc tb1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlDescribeStatement desc = parser.parseDescribe();
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(desc);
        Assert.assertEquals("DESC tb1", output);
    }

    public void testdesc_1() throws Exception {
        String sql = "desc db.tb1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlDescribeStatement desc = parser.parseDescribe();
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(desc);
        Assert.assertEquals("DESC db.tb1", output);
    }

    public void testSet_1() throws Exception {
        String sql = "seT sysVar1 = ? ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSetStatement set = (SQLSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET @@sysVar1 = ?", output);
    }

    public void testSet_2() throws Exception {
        String sql = "SET `sysVar1` = ?, @@gloBal . `var2` :=1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSetStatement set = (SQLSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET @@`sysVar1` = ?, @@global.`var2` = 1", output);
    }

    public void testSet_3() throws Exception {
        String sql = "SET @usrVar1 := ?, @@`var2` =1, @@var3:=?, @'var\\'3'=?";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSetStatement set = (SQLSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET @usrVar1 = ?, @@`var2` = 1, @@var3 = ?, @'var\\'3' = ?", output);
    }

    public void testSet_4() throws Exception {
        String sql = "SET GLOBAL var1=1, SESSION var2:=2";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSetStatement set = (SQLSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET @@global.var1 = 1, @@var2 = 2", output);
    }

    public void testSet_5() throws Exception {
        String sql = "SET @@GLOBAL. var1=1, SESSION var2:=2";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLSetStatement set = (SQLSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET @@global.var1 = 1, @@var2 = 2", output);
    }

    public void testSetTxn_0() throws Exception {
        String sql = "SET transaction ISOLATION LEVEL READ UNCOMMITTED";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetTransactionIsolationLevelStatement set = (MySqlSetTransactionIsolationLevelStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED", output);
    }

    public void testSetTxn_1() throws Exception {
        String sql = "SET global transaction ISOLATION LEVEL READ COMMITTED";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetTransactionIsolationLevelStatement set = (MySqlSetTransactionIsolationLevelStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED", output);
    }

    public void testSetTxn_2() throws Exception {
        String sql = "SET transaction ISOLATION LEVEL REPEATABLE READ ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetTransactionIsolationLevelStatement set = (MySqlSetTransactionIsolationLevelStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ", output);
    }

    public void testSetTxn_3() throws Exception {
        String sql = "SET session transaction ISOLATION LEVEL SERIALIZABLE";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetTransactionIsolationLevelStatement set = (MySqlSetTransactionIsolationLevelStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE", output);
    }

//    public void testSet() throws Exception {
//
//
//        sql = "SET names default ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        set = (DALSetNamesStatement) parser.set();
//        parser.match(Token.EOF);
//        output = output2MySQL(set, sql);
//        Assert.assertEquals("SET NAMES DEFAULT", output);
//
//        sql = "SET NAMEs 'utf8' collatE \"latin1_danish_ci\" ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        set = (DALSetNamesStatement) parser.set();
//        parser.match(Token.EOF);
//        output = output2MySQL(set, sql);
//        Assert.assertEquals("SET NAMES utf8 COLLATE latin1_danish_ci", output);
//
//        sql = "SET NAMEs utf8  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        set = (DALSetNamesStatement) parser.set();
//        parser.match(Token.EOF);
//        output = output2MySQL(set, sql);
//        Assert.assertEquals("SET NAMES utf8", output);
//
//        sql = "SET CHARACTEr SEt 'utf8'  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        set = (DALSetCharacterSetStatement) parser.set();
//        parser.match(Token.EOF);
//        output = output2MySQL(set, sql);
//        Assert.assertEquals("SET CHARACTER SET utf8", output);
//
//        sql = "SET CHARACTEr SEt DEFaULT  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        set = (DALSetCharacterSetStatement) parser.set();
//        parser.match(Token.EOF);
//        output = output2MySQL(set, sql);
//        Assert.assertEquals("SET CHARACTER SET DEFAULT", output);
//    }
//
//    public void testShow() throws Exception {
//        String sql = "shoW authors  ";
//        SQLLexer lexer = new SQLLexer(sql);
//        DALParser parser = new DALParser(lexer, new SQLExprParser(lexer));
//        DALShowStatement show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        String output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW AUTHORS", output);
//
//        sql = "SHOW BINARY LOGS  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW BINARY LOGS", output);
//
//        sql = "SHOW MASTER LOGS  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW BINARY LOGS", output);
//
//        sql = "SHOW binlog events in 'a' from 1 limit 1,2  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW BINLOG EVENTS IN 'a' FROM 1 LIMIT 1, 2", output);
//
//        sql = "SHOW binlog events from 1 limit 1,2  ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW BINLOG EVENTS FROM 1 LIMIT 1, 2", output);
//
//        sql = "SHOW binlog events ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW BINLOG EVENTS", output);
//
//        sql = "SHOW CHARACTER SET like 'var' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CHARACTER SET LIKE 'var'", output);
//
//        sql = "SHOW CHARACTER SET where (select a from tb)in(a) ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CHARACTER SET WHERE (SELECT a FROM tb) IN (a)", output);
//
//        sql = "SHOW CHARACTER SET ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CHARACTER SET", output);
//
//        sql = "SHOW collation ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COLLATION", output);
//
//        sql = "SHOW Collation like 'var1' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COLLATION LIKE 'var1'", output);
//
//        sql = "SHOW collation where avg((select * from (tb1,tb2)))!=100 ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COLLATION WHERE AVG(SELECT * FROM tb1, tb2) != 100", output);
//
//        sql = "SHOW full columns from tb1 from db1 like 'var' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL COLUMNS FROM db1.tb1 LIKE 'var'", output);
//
//        sql = "SHOW full columns from tb1 from db1 where count(col)>10 ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL COLUMNS FROM db1.tb1 WHERE COUNT(col) > 10", output);
//
//        sql = "SHOW columns from tb1 from db1 like 'var' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COLUMNS FROM db1.tb1 LIKE 'var'", output);
//
//        sql = "SHOW columns from tb1 like 'var' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COLUMNS FROM tb1 LIKE 'var'", output);
//
//        sql = "SHOW full columns from tb1 from db1 ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL COLUMNS FROM db1.tb1", output);
//
//        sql = "SHOW contributors ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CONTRIBUTORS", output);
//
//        sql = "SHOW CREATE DATABASE db_name ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE DATABASE db_name", output);
//
//        sql = "SHOW create event expr ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE EVENT expr", output);
//
//        sql = "SHOW create function func ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE FUNCTION func", output);
//
//        sql = "SHOW create procedure pro ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE PROCEDURE pro", output);
//
//        sql = "SHOW create table tb ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE TABLE tb", output);
//
//        sql = "SHOW create trigger tri ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE TRIGGER tri", output);
//
//        sql = "SHOW create view view";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW CREATE VIEW view", output);
//
//        sql = "SHOW databases like 'var'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW DATABASES LIKE 'var'", output);
//
//        sql = "SHOW databases where (select * from `select`)is not null";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW DATABASES WHERE (SELECT * FROM `select`) IS NOT NULL", output);
//
//        sql = "SHOW databases ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW DATABASES", output);
//
//        sql = "SHOW engine innodb status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ENGINE INNODB STATUS", output);
//
//        sql = "SHOW engine innodb mutex";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ENGINE INNODB MUTEX", output);
//
//        sql = "SHOW engine performance_schema status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ENGINE PERFORMANCE SCHEMA STATUS", output);
//
//        sql = "SHOW engines";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ENGINES", output);
//
//        sql = "SHOW storage engines";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ENGINES", output);
//
//        sql = "SHOW errors";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ERRORS", output);
//
//        sql = "SHOW errors limit 1 ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW ERRORS LIMIT 0, 1", output);
//
//        sql = "SHOW count(*) errors";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COUNT(*) ERRORS", output);
//
//        sql = "SHOW events";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW EVENTS", output);
//
//        sql = "SHOW events from expr like 'var' ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW EVENTS FROM expr LIKE 'var'", output);
//
//        sql = "SHOW events from expr where expr1";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW EVENTS FROM expr WHERE expr1", output);
//
//        sql = "SHOW function code expr";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FUNCTION CODE expr", output);
//
//        sql = "SHOW function status like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FUNCTION STATUS LIKE 'expr'", output);
//
//        sql = "SHOW function status where a is true";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FUNCTION STATUS WHERE a IS TRUE", output);
//
//        sql = "SHOW function status ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FUNCTION STATUS", output);
//
//        sql = "SHOW grants for 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GRANTS FOR 'expr'", output);
//
//        sql = "SHOW grants";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GRANTS", output);
//
//        sql = "SHOW index from tb1 from db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW INDEX IN db.tb1", output);
//
//        sql = "SHOW index from tb1 in db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW INDEX IN db.tb1", output);
//
//        sql = "SHOW index in tb1 from db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW INDEX IN db.tb1", output);
//
//        sql = "SHOW index in tb1 in db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW INDEX IN db.tb1", output);
//
//        sql = "SHOW indexes from tb1";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW INDEXES IN tb1", output);
//
//        sql = "SHOW keys in tb1";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW KEYS IN tb1", output);
//
//        sql = "SHOW master status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW MASTER STATUS", output);
//
//        sql = "SHOW open tables from db like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW OPEN TABLES FROM db LIKE 'expr'", output);
//
//        sql = "SHOW open tables from db where tb is not null";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW OPEN TABLES FROM db WHERE tb IS NOT NULL", output);
//
//        sql = "SHOW open tables from db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW OPEN TABLES FROM db", output);
//
//        sql = "SHOW open tables in db";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW OPEN TABLES FROM db", output);
//
//        sql = "SHOW open tables";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW OPEN TABLES", output);
//
//        sql = "SHOW plugins";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PLUGINS", output);
//
//        sql = "SHOW privileges";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PRIVILEGES", output);
//
//        sql = "SHOW procedure code proc";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROCEDURE CODE proc", output);
//
//        sql = "SHOW procedure status like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROCEDURE STATUS LIKE 'expr'", output);
//
//        sql = "SHOW procedure status where (a||b)*(a&&b)";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROCEDURE STATUS WHERE (a OR b) * (a AND b)", output);
//
//        sql = "SHOW procedure status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROCEDURE STATUS", output);
//
//        sql = "SHOW processlist";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROCESSLIST", output);
//
//        sql = "SHOW full processlist";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL PROCESSLIST", output);
//
//        sql = "SHOW profiles";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROFILES", output);
//
//        sql =
//                "SHOW profile all,block io,context switches,cpu,ipc,memory,"
//                        + "page faults,source,swaps for query 2 limit 1 offset 2";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROFILE ALL, BLOCK IO, CONTEXT SWITCHES, CPU, IPC, MEMORY, "
//                            + "PAGE FAULTS, SOURCE, SWAPS FOR QUERY 2 LIMIT 2, 1", output);
//
//        sql = "SHOW profile";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROFILE", output);
//
//        sql = "SHOW profile all,block io,context switches,cpu,ipc," + "memory,page faults,source,swaps for query 2";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROFILE ALL, BLOCK IO, CONTEXT SWITCHES, CPU, IPC, "
//                            + "MEMORY, PAGE FAULTS, SOURCE, SWAPS FOR QUERY 2", output);
//
//        sql = "SHOW profile all for query 2";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW PROFILE ALL FOR QUERY 2", output);
//
//        sql = "SHOW slave hosts";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SLAVE HOSTS", output);
//
//        sql = "SHOW slave status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SLAVE STATUS", output);
//
//        sql = "SHOW global status like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL STATUS LIKE 'expr'", output);
//
//        sql = "SHOW global status where ${abc}";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL STATUS WHERE ${abc}", output);
//
//        sql = "SHOW session status like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS LIKE 'expr'", output);
//
//        sql = "SHOW session status where ?";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS WHERE ?", output);
//
//        sql = "SHOW status like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS LIKE 'expr'", output);
//
//        sql = "SHOW status where 0b10^b'11'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS WHERE b'10' ^ b'11'", output);
//
//        sql = "SHOW status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS", output);
//
//        sql = "SHOW global status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL STATUS", output);
//
//        sql = "SHOW session status";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION STATUS", output);
//
//        sql = "SHOW table status from db like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLE STATUS FROM db LIKE 'expr'", output);
//
//        sql = "SHOW table status in db where (select a)>(select b)";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLE STATUS FROM db WHERE (SELECT a) > (SELECT b)", output);
//
//        sql = "SHOW table status from db where id1=a||b";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLE STATUS FROM db WHERE id1 = a OR b", output);
//
//        sql = "SHOW table status ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLE STATUS", output);
//
//        sql = "SHOW tables from db like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLES FROM db LIKE 'expr'", output);
//
//        sql = "SHOW tables in db where !a";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLES FROM db WHERE ! a", output);
//
//        sql = "SHOW tables like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLES LIKE 'expr'", output);
//
//        sql = "SHOW tables where log((select a))=b";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLES WHERE LOG(SELECT a) = b", output);
//
//        sql = "SHOW tables ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TABLES", output);
//
//        sql = "SHOW full tables from db like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL TABLES FROM db LIKE 'expr'", output);
//
//        sql = "SHOW full tables in db where id1=abs((select a))";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL TABLES FROM db WHERE id1 = ABS(SELECT a)", output);
//
//        sql = "SHOW full tables ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW FULL TABLES", output);
//
//        sql = "SHOW triggers from db like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TRIGGERS FROM db LIKE 'expr'", output);
//
//        sql = "SHOW triggers in db where strcmp('test1','test2')";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TRIGGERS FROM db WHERE STRCMP('test1', 'test2')", output);
//
//        sql = "SHOW triggers ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW TRIGGERS", output);
//
//        sql = "SHOW global variables like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL VARIABLES LIKE 'expr'", output);
//
//        sql = "SHOW global variables where ~a is null";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL VARIABLES WHERE ~ a IS NULL", output);
//
//        sql = "SHOW session variables like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES LIKE 'expr'", output);
//
//        sql = "SHOW session variables where a*b+1=c";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES WHERE a * b + 1 = c", output);
//
//        sql = "SHOW variables like 'expr'";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES LIKE 'expr'", output);
//
//        sql = "SHOW variables where a&&b";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES WHERE a AND b", output);
//
//        sql = "SHOW variables";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES", output);
//
//        sql = "SHOW global variables";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW GLOBAL VARIABLES", output);
//
//        sql = "SHOW session variables";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW SESSION VARIABLES", output);
//
//        sql = "SHOW warnings limit 1,2 ";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW WARNINGS LIMIT 1, 2", output);
//
//        sql = "SHOW warnings";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW WARNINGS", output);
//
//        sql = "SHOW count(*) warnings";
//        lexer = new SQLLexer(sql);
//        parser = new DALParser(lexer, new SQLExprParser(lexer));
//        show = (DALShowStatement) parser.show();
//        parser.match(Token.EOF);
//        output = output2MySQL(show, sql);
//        Assert.assertEquals("SHOW COUNT(*) WARNINGS", output);
//    }
}