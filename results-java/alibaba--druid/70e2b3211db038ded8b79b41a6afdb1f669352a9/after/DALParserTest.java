/**
 * (created at 2011-5-20)
 */
package com.alibaba.druid.bvt.sql.cobar;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDescribeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionIsolationLevelStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowAuthorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinaryLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCharacterSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCollationStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowContributorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateDatabaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateEventStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateFunctionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTriggerStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateViewStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEngineStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEnginesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowErrorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowGrantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterLogsStatement;
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

    public void test_setNames() throws Exception {
        String sql = "SET names default ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetNamesStatement set = (MySqlSetNamesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET NAMES DEFAULT", output);
    }

    public void test_setNames_1() throws Exception {
        String sql = "SET NAMEs 'utf8' collatE \"latin1_danish_ci\" ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetNamesStatement set = (MySqlSetNamesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET NAMES utf8 COLLATE latin1_danish_ci", output);
    }

    public void test_setNames_2() throws Exception {
        String sql = "SET NAMEs utf8 ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetNamesStatement set = (MySqlSetNamesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET NAMES utf8", output);
    }

    public void test_setCharSet() throws Exception {
        String sql = "SET CHARACTEr SEt 'utf8'  ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetCharSetStatement set = (MySqlSetCharSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET CHARACTER SET utf8", output);
    }

    public void test_setCharSet_1() throws Exception {
        String sql = "SET CHARACTEr SEt DEFaULT  ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlSetCharSetStatement set = (MySqlSetCharSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(set);
        Assert.assertEquals("SET CHARACTER SET DEFAULT", output);
    }

    public void test_show_authors() throws Exception {
        String sql = "shoW authors ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowAuthorsStatement show = (MySqlShowAuthorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW AUTHORS", output);
    }

    public void test_show_binaryLogs() throws Exception {
        String sql = "SHOW BINARY LOGS ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowBinaryLogsStatement show = (MySqlShowBinaryLogsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW BINARY LOGS", output);
    }

    public void test_show_masterLogs() throws Exception {
        String sql = "SHOW MASTER LOGS ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowMasterLogsStatement show = (MySqlShowMasterLogsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW MASTER LOGS", output);
    }

    public void test_show_collation() throws Exception {
        String sql = "SHOW collation";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCollationStatement show = (MySqlShowCollationStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLLATION", output);
    }

    public void test_show_collation_1() throws Exception {
        String sql = "SHOW Collation like 'var1'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCollationStatement show = (MySqlShowCollationStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLLATION LIKE 'var1'", output);
    }

    public void test_show_collation_2() throws Exception {
        String sql = "SHOW COLLATION WHERE `Default` = 'Yes'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCollationStatement show = (MySqlShowCollationStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLLATION WHERE `Default` = 'Yes'", output);
    }

    public void test_show_collation_3() throws Exception {
        String sql = "SHOW collation where Collation like 'big5%'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCollationStatement show = (MySqlShowCollationStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLLATION WHERE Collation LIKE 'big5%'", output);
    }

    public void test_binaryLog() throws Exception {
        String sql = "SHOW binlog events in 'a' from 1 limit 1,2  ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowBinLogEventsStatement show = (MySqlShowBinLogEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW BINLOG EVENTS IN 'a' FROM 1 LIMIT 1, 2", output);
    }

    public void test_binaryLog_1() throws Exception {
        String sql = "SHOW binlog events from 1 limit 1,2";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowBinLogEventsStatement show = (MySqlShowBinLogEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW BINLOG EVENTS FROM 1 LIMIT 1, 2", output);
    }

    public void test_binaryLog_2() throws Exception {
        String sql = "SHOW binlog events ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowBinLogEventsStatement show = (MySqlShowBinLogEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW BINLOG EVENTS", output);
    }

    public void test_show_character_set() throws Exception {
        String sql = "SHOW CHARACTER SET like 'var' ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCharacterSetStatement show = (MySqlShowCharacterSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CHARACTER SET LIKE 'var'", output);
    }


    public void test_show_character_set_1() throws Exception {
        String sql = "SHOW CHARACTER SET where Charset = 'big5'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCharacterSetStatement show = (MySqlShowCharacterSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CHARACTER SET WHERE Charset = 'big5'", output);
    }


    public void test_show_character_set_2() throws Exception {
        String sql = "SHOW CHARACTER SET";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCharacterSetStatement show = (MySqlShowCharacterSetStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CHARACTER SET", output);
    }

    public void test_show_columns() throws Exception {
        String sql = "SHOW full columns from tb1 from db1 like 'var' ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowColumnsStatement show = (MySqlShowColumnsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FULL COLUMNS FROM db1.tb1 LIKE 'var'", output);
    }

    public void test_show_columns_1() throws Exception {
        String sql = "show columns from events where Field = 'name'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowColumnsStatement show = (MySqlShowColumnsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLUMNS FROM events WHERE Field = 'name'", output);
    }

    public void test_show_columns_2() throws Exception {
        String sql = "SHOW COLUMNS FROM City";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowColumnsStatement show = (MySqlShowColumnsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COLUMNS FROM City", output);
    }

    public void test_show_columns_3() throws Exception {
        String sql = "SHOW full columns from db1.tb1 like 'var' ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowColumnsStatement show = (MySqlShowColumnsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FULL COLUMNS FROM db1.tb1 LIKE 'var'", output);
    }

    public void test_show_contributors() throws Exception {
        String sql = "SHOW contributors";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowContributorsStatement show = (MySqlShowContributorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CONTRIBUTORS", output);
    }

    public void test_show_create_database() throws Exception {
        String sql = "SHOW CREATE DATABASE db_name";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateDatabaseStatement show = (MySqlShowCreateDatabaseStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE DATABASE db_name", output);
    }

    public void test_show_create_event() throws Exception {
        String sql = "SHOW CREATE event db_name";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateEventStatement show = (MySqlShowCreateEventStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE EVENT db_name", output);
    }

    public void test_show_create_function() throws Exception {
        String sql = "SHOW CREATE function x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateFunctionStatement show = (MySqlShowCreateFunctionStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE FUNCTION x", output);
    }

    public void test_show_create_PROCEDURE() throws Exception {
        String sql = "SHOW CREATE PROCEDURE x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateProcedureStatement show = (MySqlShowCreateProcedureStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE PROCEDURE x", output);
    }

    public void test_show_create_table() throws Exception {
        String sql = "SHOW CREATE table x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateTableStatement show = (MySqlShowCreateTableStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE TABLE x", output);
    }

    public void test_show_create_trigger() throws Exception {
        String sql = "SHOW CREATE trigger x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateTriggerStatement show = (MySqlShowCreateTriggerStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE TRIGGER x", output);
    }

    public void test_show_create_view() throws Exception {
        String sql = "SHOW CREATE VIEW x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowCreateViewStatement show = (MySqlShowCreateViewStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW CREATE VIEW x", output);
    }

    public void test_show_databases() throws Exception {
        String sql = "SHOW DATABASES";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowDatabasesStatement show = (MySqlShowDatabasesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW DATABASES", output);
    }

    public void test_show_databases_1() throws Exception {
        String sql = "SHOW DATABASES LIKE 'a%'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowDatabasesStatement show = (MySqlShowDatabasesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW DATABASES LIKE 'a%'", output);
    }

    public void test_show_databases_2() throws Exception {
        String sql = "SHOW DATABASES WHERE `Database` = 'mysql'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowDatabasesStatement show = (MySqlShowDatabasesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW DATABASES WHERE `Database` = 'mysql'", output);
    }

    public void test_show_engine() throws Exception {
        String sql = "SHOW ENGINE INNODB STATUS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEngineStatement show = (MySqlShowEngineStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ENGINE INNODB STATUS", output);
    }

    public void test_show_engine_1() throws Exception {
        String sql = "SHOW ENGINE PERFORMANCE_SCHEMA STATUS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEngineStatement show = (MySqlShowEngineStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ENGINE PERFORMANCE_SCHEMA STATUS", output);
    }

    public void test_show_engine_2() throws Exception {
        String sql = "SHOW ENGINE INNODB mutex";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEngineStatement show = (MySqlShowEngineStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ENGINE INNODB MUTEX", output);
    }

    public void test_show_engines() throws Exception {
        String sql = "SHOW ENGINES";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEnginesStatement show = (MySqlShowEnginesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ENGINES", output);
    }

    public void test_show_engines_1() throws Exception {
        String sql = "SHOW STORAGE ENGINES";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEnginesStatement show = (MySqlShowEnginesStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW STORAGE ENGINES", output);
    }

    public void test_show_errors() throws Exception {
        String sql = "SHOW COUNT(*) ERRORS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowErrorsStatement show = (MySqlShowErrorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW COUNT(*) ERRORS", output);
    }

    public void test_show_errors_1() throws Exception {
        String sql = "SHOW ERRORS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowErrorsStatement show = (MySqlShowErrorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ERRORS", output);
    }

    public void test_show_errors_2() throws Exception {
        String sql = "SHOW ERRORS limit 1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowErrorsStatement show = (MySqlShowErrorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ERRORS LIMIT 1", output);
    }

    public void test_show_errors_3() throws Exception {
        String sql = "SHOW ERRORS limit 1, 2";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowErrorsStatement show = (MySqlShowErrorsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW ERRORS LIMIT 1, 2", output);
    }

    public void test_show_events() throws Exception {
        String sql = "SHOW EVENTS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEventsStatement show = (MySqlShowEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW EVENTS", output);
    }

    public void test_show_events_1() throws Exception {
        String sql = "SHOW EVENTS from x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEventsStatement show = (MySqlShowEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW EVENTS FROM x", output);
    }

    public void test_show_events_2() throws Exception {
        String sql = "SHOW EVENTS in x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEventsStatement show = (MySqlShowEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW EVENTS FROM x", output);
    }

    public void test_show_events_3() throws Exception {
        String sql = "SHOW EVENTS in x like '%'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEventsStatement show = (MySqlShowEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW EVENTS FROM x LIKE '%'", output);
    }

    public void test_show_events_4() throws Exception {
        String sql = "SHOW EVENTS in x where 1 = 1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowEventsStatement show = (MySqlShowEventsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW EVENTS FROM x WHERE 1 = 1", output);
    }

    public void test_show_function_code() throws Exception {
        String sql = "SHOW function code x";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowFunctionCodeStatement show = (MySqlShowFunctionCodeStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FUNCTION CODE x", output);
    }

    public void test_show_function_status() throws Exception {
        String sql = "SHOW function status like '%'";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowFunctionStatusStatement show = (MySqlShowFunctionStatusStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FUNCTION STATUS LIKE '%'", output);
    }

    public void test_show_function_status_1() throws Exception {
        String sql = "SHOW function status where 1 = 1";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowFunctionStatusStatement show = (MySqlShowFunctionStatusStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FUNCTION STATUS WHERE 1 = 1", output);
    }

    public void test_show_function_status_2() throws Exception {
        String sql = "SHOW function status ";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowFunctionStatusStatement show = (MySqlShowFunctionStatusStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW FUNCTION STATUS", output);
    }

    public void test_show_grants() throws Exception {
        String sql = "SHOW GRANTS FOR 'root'@'localhost';";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowGrantsStatement show = (MySqlShowGrantsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW GRANTS FOR 'root'@'localhost'", output);
    }

    public void test_show_grants_1() throws Exception {
        String sql = "SHOW GRANTS";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowGrantsStatement show = (MySqlShowGrantsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW GRANTS", output);
    }

    public void test_show_grants_2() throws Exception {
        String sql = "SHOW GRANTS FOR CURRENT_USER";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowGrantsStatement show = (MySqlShowGrantsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW GRANTS FOR CURRENT_USER", output);
    }

    public void test_show_grants_3() throws Exception {
        String sql = "SHOW GRANTS FOR CURRENT_USER()";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        MySqlShowGrantsStatement show = (MySqlShowGrantsStatement) parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        String output = SQLUtils.toMySqlString(show);
        Assert.assertEquals("SHOW GRANTS FOR CURRENT_USER()", output);
    }

//
//    public void testShow() throws Exception {
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