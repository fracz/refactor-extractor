package com.alibaba.druid.hbase.exec;

import java.sql.SQLException;

import com.alibaba.druid.hbase.jdbc.HPreparedStatement;
import com.alibaba.druid.hbase.jdbc.HResultSet;

public class ExecutePlanAdapter implements ExecutePlan {

    @Override
    public HResultSet executeQuery(HPreparedStatement statement) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean execute(HPreparedStatement statement) throws SQLException {
        throw new UnsupportedOperationException();
    }

}