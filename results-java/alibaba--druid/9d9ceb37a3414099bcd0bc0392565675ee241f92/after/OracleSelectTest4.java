package com.alibaba.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alibaba.druid.sql.OracleTest;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

public class OracleSelectTest4 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "SELECT LPAD(' ',2*(LEVEL-1)) || last_name org_chart, " + //
                     "employee_id, manager_id, job_id " + //
                     "    FROM employees" + //
                     "    START WITH job_id = 'AD_PRES' " + //
                     "    CONNECT BY PRIOR employee_id = manager_id AND LEVEL <= 2;";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        print(statementList);

        Assert.assertEquals(1, statementList.size());

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("relationships : " + visitor.getRelationships());

        Assert.assertEquals(1, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("employees")));

        Assert.assertEquals(4, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "job_id")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "last_name")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "employee_id")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("employees", "manager_id")));
    }

}