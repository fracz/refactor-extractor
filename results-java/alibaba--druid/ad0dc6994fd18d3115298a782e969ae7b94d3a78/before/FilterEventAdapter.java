/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.filter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Properties;

import com.alibaba.druid.proxy.jdbc.CallableStatementProxy;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.JdbcParameter.TYPE;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public abstract class FilterEventAdapter extends FilterAdapter {

    public FilterEventAdapter(){
    }

    public ConnectionProxy connection_connect(FilterChain chain, Properties info) throws SQLException {
        connection_connectBefore(chain, info);

        ConnectionProxy connection = super.connection_connect(chain, info);

        connection_connectAfter(connection);

        return connection;
    }

    public void connection_connectBefore(FilterChain chain, Properties info) {

    }

    public void connection_connectAfter(ConnectionProxy connection) {

    }

    @Override
    public StatementProxy connection_createStatement(FilterChain chain, ConnectionProxy connection) throws SQLException {
        StatementProxy statement = super.connection_createStatement(chain, connection);

        statementCreateAfter(statement);

        return statement;
    }

    @Override
    public StatementProxy connection_createStatement(FilterChain chain, ConnectionProxy connection, int resultSetType,
                                                     int resultSetConcurrency) throws SQLException {
        StatementProxy statement = super.connection_createStatement(chain, connection, resultSetType,
                                                                    resultSetConcurrency);

        statementCreateAfter(statement);

        return statement;
    }

    @Override
    public StatementProxy connection_createStatement(FilterChain chain, ConnectionProxy connection, int resultSetType,
                                                     int resultSetConcurrency, int resultSetHoldability)
                                                                                                        throws SQLException {
        StatementProxy statement = super.connection_createStatement(chain, connection, resultSetType,
                                                                    resultSetConcurrency, resultSetHoldability);

        statementCreateAfter(statement);

        return statement;
    }

    @Override
    public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql)
                                                                                                                   throws SQLException {
        CallableStatementProxy statement = super.connection_prepareCall(chain, connection, sql);

        statementPrepareCallAfter(statement);

        return statement;
    }

    @Override
    public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql,
                                                         int resultSetType, int resultSetConcurrency)
                                                                                                     throws SQLException {
        CallableStatementProxy statement = super.connection_prepareCall(chain, connection, sql, resultSetType,
                                                                        resultSetConcurrency);

        statementPrepareCallAfter(statement);

        return statement;
    }

    @Override
    public CallableStatementProxy connection_prepareCall(FilterChain chain, ConnectionProxy connection, String sql,
                                                         int resultSetType, int resultSetConcurrency,
                                                         int resultSetHoldability) throws SQLException {
        CallableStatementProxy statement = super.connection_prepareCall(chain, connection, sql, resultSetType,
                                                                        resultSetConcurrency, resultSetHoldability);

        statementPrepareCallAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql)
                                                                                                                        throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection,
                                                              String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql, autoGeneratedKeys);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection,
                                                              String sql, int resultSetType, int resultSetConcurrency)
                                                                                                                      throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql, resultSetType,
                                                                             resultSetConcurrency);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection,
                                                              String sql, int resultSetType, int resultSetConcurrency,
                                                              int resultSetHoldability) throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql, resultSetType,
                                                                             resultSetConcurrency, resultSetHoldability);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection,
                                                              String sql, int[] columnIndexes) throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql, columnIndexes);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection,
                                                              String sql, String[] columnNames) throws SQLException {
        PreparedStatementProxy statement = super.connection_prepareStatement(chain, connection, sql, columnNames);

        statementPrepareAfter(statement);

        return statement;
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        statementExecuteBefore(statement, sql);

        try {
            boolean firstResult = super.statement_execute(chain, statement, sql);

            statementExecuteAfter(statement, sql, firstResult);

            return firstResult;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
                                                                                                                    throws SQLException {
        statementExecuteBefore(statement, sql);

        try {
            boolean firstResult = super.statement_execute(chain, statement, sql, autoGeneratedKeys);

            this.statementExecuteAfter(statement, sql, firstResult);

            return firstResult;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int columnIndexes[])
                                                                                                                  throws SQLException {
        statementExecuteBefore(statement, sql);

        try {
            boolean firstResult = super.statement_execute(chain, statement, sql, columnIndexes);

            this.statementExecuteAfter(statement, sql, firstResult);

            return firstResult;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, String columnNames[])
                                                                                                                   throws SQLException {
        statementExecuteBefore(statement, sql);

        try {
            boolean firstResult = super.statement_execute(chain, statement, sql, columnNames);

            this.statementExecuteAfter(statement, sql, firstResult);

            return firstResult;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException {
        statementExecuteBatchBefore(statement);

        try {
            int[] result = super.statement_executeBatch(chain, statement);

            statementExecuteBatchAfter(statement, result);

            return result;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, statement.getBatchSql(), error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, statement.getBatchSql(), error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, statement.getBatchSql(), error);
            throw error;
        }
    }

    @Override
    public ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql)
                                                                                                         throws SQLException {
        statementExecuteQueryBefore(statement, sql);

        try {
            ResultSetProxy resultSet = super.statement_executeQuery(chain, statement, sql);

            statementExecuteQueryAfter(statement, sql, resultSet);
            resultSetOpenAfter(resultSet);

            return resultSet;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        statementExecuteUpdateBefore(statement, sql);

        try {
            int updateCount = super.statement_executeUpdate(chain, statement, sql);

            statementExecuteUpdateAfter(statement, sql, updateCount);

            return updateCount;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
                                                                                                                      throws SQLException {
        statementExecuteUpdateBefore(statement, sql);

        try {
            int updateCount = super.statement_executeUpdate(chain, statement, sql, autoGeneratedKeys);

            statementExecuteUpdateAfter(statement, sql, updateCount);

            return updateCount;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int columnIndexes[])
                                                                                                                    throws SQLException {
        statementExecuteUpdateBefore(statement, sql);

        try {
            int updateCount = super.statement_executeUpdate(chain, statement, sql, columnIndexes);

            statementExecuteUpdateAfter(statement, sql, updateCount);

            return updateCount;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, String columnNames[])
                                                                                                                     throws SQLException {
        statementExecuteUpdateBefore(statement, sql);

        try {
            int updateCount = super.statement_executeUpdate(chain, statement, sql, columnNames);

            statementExecuteUpdateAfter(statement, sql, updateCount);

            return updateCount;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    @Override
    public ResultSetProxy statement_getGeneratedKeys(FilterChain chain, StatementProxy statement) throws SQLException {
        ResultSetProxy resultSet = super.statement_getGeneratedKeys(chain, statement);

        if (resultSet != null) {
            resultSetOpenAfter(resultSet);
        }

        return resultSet;
    }

    @Override
    public ResultSetProxy statement_getResultSet(FilterChain chain, StatementProxy statement) throws SQLException {
        ResultSetProxy resultSet = super.statement_getResultSet(chain, statement);

        if (resultSet != null) {
            resultSetOpenAfter(resultSet);
        }

        return resultSet;
    }

    @Override
    public boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        try {
            statementExecuteBefore(statement, statement.getSql());

            boolean firstResult = super.preparedStatement_execute(chain, statement);

            this.statementExecuteAfter(statement, statement.getSql(), firstResult);

            return firstResult;

        } catch (SQLException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        }

    }

    @Override
    public ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement)
                                                                                                             throws SQLException {
        try {
            statementExecuteQueryBefore(statement, statement.getSql());

            ResultSetProxy resultSet = super.preparedStatement_executeQuery(chain, statement);

            statementExecuteQueryAfter(statement, statement.getSql(), resultSet);

            resultSetOpenAfter(resultSet);

            return resultSet;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        }
    }

    @Override
    public int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        try {
            statementExecuteUpdateBefore(statement, statement.getSql());

            int updateCount = super.preparedStatement_executeUpdate(chain, statement);

            statementExecuteUpdateAfter(statement, statement.getSql(), updateCount);

            return updateCount;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        }
    }

    protected void statementCreateAfter(StatementProxy statement) {

    }

    protected void statementPrepareAfter(PreparedStatementProxy statement) {

    }

    protected void statementPrepareCallAfter(CallableStatementProxy statement) {

    }

    protected void resultSetOpenAfter(ResultSetProxy resultSet) {

    }

    protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {

    }

    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {

    }

    protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {

    }

    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {

    }

    protected void statementExecuteBefore(StatementProxy statement, String sql) {

    }

    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {

    }

    protected void statementExecuteBatchBefore(StatementProxy statement) {

    }

    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {

    }

    // ////
    @Override
    public void preparedStatement_setArray(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           Array x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.ARRAY, x);
        super.preparedStatement_setArray(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setAsciiStream(FilterChain chain, PreparedStatementProxy statement,
                                                 int parameterIndex, InputStream x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.AsciiInputStream, x);
        super.preparedStatement_setAsciiStream(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setAsciiStream(FilterChain chain, PreparedStatementProxy statement,
                                                 int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.AsciiInputStream, x, length);
        super.preparedStatement_setAsciiStream(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setAsciiStream(FilterChain chain, PreparedStatementProxy statement,
                                                 int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.AsciiInputStream, x, length);
        super.preparedStatement_setAsciiStream(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setBigDecimal(FilterChain chain, PreparedStatementProxy statement,
                                                int parameterIndex, BigDecimal x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.DECIMAL, x);
        super.preparedStatement_setBigDecimal(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setBinaryStream(FilterChain chain, PreparedStatementProxy statement,
                                                  int parameterIndex, InputStream x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.BinaryInputStream, x);
        super.preparedStatement_setBinaryStream(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setBinaryStream(FilterChain chain, PreparedStatementProxy statement,
                                                  int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.BinaryInputStream, x, length);
        super.preparedStatement_setBinaryStream(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setBinaryStream(FilterChain chain, PreparedStatementProxy statement,
                                                  int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.BinaryInputStream, x, length);
        super.preparedStatement_setBinaryStream(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setBlob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Blob x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BLOB, x);
        super.preparedStatement_setBlob(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setBlob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          InputStream x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BLOB, x);
        super.preparedStatement_setBlob(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setBlob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          InputStream x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BLOB, x, length);
        super.preparedStatement_setBlob(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setBoolean(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                             boolean x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BOOLEAN, x);
        super.preparedStatement_setBoolean(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setByte(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          byte x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.TINYINT, x);
        super.preparedStatement_setByte(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setBytes(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           byte[] x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BINARY, x);
        super.preparedStatement_setBytes(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setCharacterStream(FilterChain chain, PreparedStatementProxy statement,
                                                     int parameterIndex, Reader x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.CharacterInputStream, x);
        super.preparedStatement_setCharacterStream(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setCharacterStream(FilterChain chain, PreparedStatementProxy statement,
                                                     int parameterIndex, Reader x, int length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.CharacterInputStream, x, length);
        super.preparedStatement_setCharacterStream(chain, statement, parameterIndex, x, length);
    }

    @Override
    public void preparedStatement_setCharacterStream(FilterChain chain, PreparedStatementProxy statement,
                                                     int parameterIndex, Reader x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.CharacterInputStream, x, length);
        super.preparedStatement_setCharacterStream(chain, statement, parameterIndex, x, length);
    }

    @Override
    public void preparedStatement_setClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Clob x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.CLOB, x);
        super.preparedStatement_setClob(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Reader x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.CLOB, x);
        super.preparedStatement_setClob(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Reader x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.CLOB, x, length);
        super.preparedStatement_setClob(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setDate(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Date x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.DATE, x);
        super.preparedStatement_setDate(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setDate(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Date x, Calendar cal) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.DATE, x, cal);
        super.preparedStatement_setDate(chain, statement, parameterIndex, x, cal);

    }

    @Override
    public void preparedStatement_setDouble(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            double x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.DOUBLE, x);
        super.preparedStatement_setDouble(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setFloat(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           float x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.FLOAT, x);
        super.preparedStatement_setFloat(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setInt(FilterChain chain, PreparedStatementProxy statement, int parameterIndex, int x)
                                                                                                                        throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.INTEGER, x);
        super.preparedStatement_setInt(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setLong(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          long x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.BIGINT, x);
        super.preparedStatement_setLong(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setNCharacterStream(FilterChain chain, PreparedStatementProxy statement,
                                                      int parameterIndex, Reader x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.NCharacterInputStream, x);
        super.preparedStatement_setNCharacterStream(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setNCharacterStream(FilterChain chain, PreparedStatementProxy statement,
                                                      int parameterIndex, Reader x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.NCharacterInputStream, x, length);
        super.preparedStatement_setNCharacterStream(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setNClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           NClob x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NCLOB, x);
        super.preparedStatement_setNClob(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setNClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           Reader x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NCLOB, x);
        super.preparedStatement_setNClob(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setNClob(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           Reader x, long length) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NCLOB, x, length);
        super.preparedStatement_setNClob(chain, statement, parameterIndex, x, length);

    }

    @Override
    public void preparedStatement_setNString(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                             String x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NVARCHAR, x);
        super.preparedStatement_setNString(chain, statement, parameterIndex, x);

    }

    @Override
    public void preparedStatement_setNull(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          int sqlType) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NULL);
        super.preparedStatement_setNull(chain, statement, parameterIndex, sqlType);
    }

    @Override
    public void preparedStatement_setNull(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          int sqlType, String typeName) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.NULL, sqlType, typeName);
        super.preparedStatement_setNull(chain, statement, parameterIndex, sqlType, typeName);
    }

    @Override
    public void preparedStatement_setObject(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            Object x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.JAVA_OBJECT, x);
        super.preparedStatement_setObject(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setObject(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            Object x, int targetSqlType) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.JAVA_OBJECT, x, targetSqlType);
        super.preparedStatement_setObject(chain, statement, parameterIndex, x, targetSqlType);
    }

    @Override
    public void preparedStatement_setObject(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.JAVA_OBJECT, x, targetSqlType,
                                             scaleOrLength);
        super.preparedStatement_setObject(chain, statement, parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void preparedStatement_setRef(FilterChain chain, PreparedStatementProxy statement, int parameterIndex, Ref x)
                                                                                                                        throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.REF, x);
        super.preparedStatement_setRef(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setRowId(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           RowId x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.ROWID, x);
        super.preparedStatement_setRowId(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setSQLXML(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            SQLXML x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.SQLXML, x);
        super.preparedStatement_setSQLXML(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setShort(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                           short x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.SMALLINT, x);
        super.preparedStatement_setShort(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setString(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                            String x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.VARCHAR, x);
        super.preparedStatement_setString(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setTime(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Time x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.TIME, x);
        super.preparedStatement_setTime(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setTime(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                          Time x, Calendar cal) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.TIME, x, cal);
        super.preparedStatement_setTime(chain, statement, parameterIndex, x, cal);
    }

    @Override
    public void preparedStatement_setTimestamp(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                               Timestamp x) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.TIMESTAMP, x);
        super.preparedStatement_setTimestamp(chain, statement, parameterIndex, x);
    }

    @Override
    public void preparedStatement_setTimestamp(FilterChain chain, PreparedStatementProxy statement, int parameterIndex,
                                               Timestamp x, Calendar cal) throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, Types.TIMESTAMP, x, cal);
        super.preparedStatement_setTimestamp(chain, statement, parameterIndex, x, cal);
    }

    @Override
    public void preparedStatement_setURL(FilterChain chain, PreparedStatementProxy statement, int parameterIndex, URL x)
                                                                                                                        throws SQLException {
        preparedStatement_setParameterBefore(statement, parameterIndex, TYPE.URL, x);
        super.preparedStatement_setURL(chain, statement, parameterIndex, x);
    }

    protected void preparedStatement_setParameterBefore(PreparedStatementProxy statement, int parameterIndex,
                                                        int sqlType, Object... values) {
    }

    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {

    }
}