package com.alibaba.druid.pool;

import java.sql.Connection;

import junit.framework.TestCase;

public class TestMockPerf extends TestCase {

    private DruidDataSource dataSource;

    protected void setUp() throws Exception {
        dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mock:xx");
        dataSource.setInitialSize(3);
        dataSource.setMinIdle(3);
        dataSource.setMaxActive(20);
        dataSource.init();
    }

    public void test_perf() throws Exception {
        for (int i = 0; i < 10; ++i) {
            long startMillis = System.currentTimeMillis();
            perf();
            long millis = System.currentTimeMillis() - startMillis;
            System.out.println("millis : " + millis);
        }
    }

    public void perf() throws Exception {
        for (int i = 0; i < 1000 * 1000; ++i) {
            Connection conn = dataSource.getConnection();
            conn.close();
        }
    }
}