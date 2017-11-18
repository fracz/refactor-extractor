package com.alibaba.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alibaba.druid.sql.OracleTest;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

public class OracleMergeTest5 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "MERGE " + //
                     "INTO MEMBER_LAST_LOGIN M2 " + //
                     "USING MEMBER_LAST_LOGIN_HZ M1 ON (M1.ID = M2.ID) " + //
                     "  WHEN MATCHED THEN " + //
                     "      UPDATE SET M2.LAST_LOGIN_TIME = M1.LAST_LOGIN_TIME, M2.GMT_MODIFIED = M1.GMT_MODIFIED" + //
                     "        , M2.OWNER_SEQ = M1.OWNER_SEQ, M2.OWNER_MEMBER_ID = M1.OWNER_MEMBER_ID, M2.IP = M1.IP " + //
                     "  WHEN NOT MATCHED THEN " + //
                     "      INSERT VALUES (M1.ID, M1.GMT_CREATE, M1.GMT_MODIFIED, M1.OWNER_SEQ" + //
                     "        , M1.LAST_LOGIN_TIME, M1.OWNER_MEMBER_ID, M1.IP)";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        System.out.println(output(statementList));

        Assert.assertEquals(1, statementList.size());

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("relationships : " + visitor.getRelationships());

        Assert.assertEquals(2, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("employees")));
        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("bonuses")));

        Assert.assertEquals(5, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "employee_id")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "salary")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "department_id")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("bonuses", "employee_id")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("bonuses", "bonus")));
    }

}