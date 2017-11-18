/*
 * Copyright (C) 2013, 2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.proxy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.pool.LeakTask;
import com.zaxxer.hikari.pool.PoolEntry;
import com.zaxxer.hikari.util.ClockSource;
import com.zaxxer.hikari.util.FastList;

/**
 * This is the proxy class for java.sql.Connection.
 *
 * @author Brett Wooldridge
 */
public abstract class ConnectionProxy implements IHikariConnectionProxy, ConnectionState
{
   private static final Logger LOGGER;
   private static final Set<String> SQL_ERRORS;
   private static final ClockSource clockSource;

   protected Connection delegate;

   private final LeakTask leakTask;
   private final PoolEntry poolEntry;
   private final FastList<Statement> openStatements;

   private int dirtyBits;
   private long lastAccess;
   private boolean isCommitStateDirty;

   public boolean isAutoCommit;
   public int networkTimeout;
   public int transactionIsolation;
   public String dbcatalog;
   public boolean isReadOnly;

   // static initializer
   static {
      LOGGER = LoggerFactory.getLogger(ConnectionProxy.class);
      clockSource = ClockSource.INSTANCE;

      SQL_ERRORS = new HashSet<>();
      SQL_ERRORS.add("57P01"); // ADMIN SHUTDOWN
      SQL_ERRORS.add("57P02"); // CRASH SHUTDOWN
      SQL_ERRORS.add("57P03"); // CANNOT CONNECT NOW
      SQL_ERRORS.add("01002"); // SQL92 disconnect error
      SQL_ERRORS.add("JZ0C0"); // Sybase disconnect error
      SQL_ERRORS.add("JZ0C1"); // Sybase disconnect error
   }

   protected ConnectionProxy(final PoolEntry poolEntry, final Connection connection, final FastList<Statement> openStatements, final LeakTask leakTask, final long now) {
      this.poolEntry = poolEntry;
      this.delegate = connection;
      this.openStatements = openStatements;
      this.leakTask = leakTask;
      this.lastAccess = now;
   }

   /** {@inheritDoc} */
   @Override
   public final String toString()
   {
      return new StringBuilder(64)
         .append(this.getClass().getSimpleName()).append('@').append(System.identityHashCode(this))
         .append(" wrapping ")
         .append(delegate).toString();
   }

   /** {@inheritDoc} */
   @Override
   public final boolean getAutoCommitState()
   {
      return isAutoCommit;
   }

   /** {@inheritDoc} */
   @Override
   public final String getCatalogState()
   {
      return dbcatalog;
   }

   /** {@inheritDoc} */
   @Override
   public final int getTransactionIsolationState()
   {
      return transactionIsolation;
   }

   /** {@inheritDoc} */
   @Override
   public final boolean getReadOnlyState()
   {
      return isReadOnly;
   }

   // ***********************************************************************
   //                      IHikariConnectionProxy methods
   // ***********************************************************************

   /** {@inheritDoc} */
   @Override
   public final PoolEntry getPoolEntry()
   {
      return poolEntry;
   }

   /** {@inheritDoc} */
   @Override
   public final SQLException checkException(final SQLException sqle)
   {
      String sqlState = sqle.getSQLState();
      if (sqlState != null) {
         boolean isForceClose = sqlState.startsWith("08") || SQL_ERRORS.contains(sqlState);
         if (isForceClose) {
            poolEntry.evict();
            LOGGER.warn("{} - Connection {} marked as broken because of SQLSTATE({}), ErrorCode({})",
                        poolEntry.getPoolName(), delegate, sqlState, sqle.getErrorCode(), sqle);
         }
         else {
            SQLException nse = sqle.getNextException();
            if (nse != null && nse != sqle) {
               checkException(nse);
            }
         }
      }
      return sqle;
   }

   /** {@inheritDoc} */
   @Override
   public final void untrackStatement(final Statement statement)
   {
      openStatements.remove(statement);
   }

   /** {@inheritDoc} */
   @Override
   public final void markCommitStateDirty()
   {
      if (isAutoCommit) {
         lastAccess = clockSource.currentTime();
      }
      else {
         isCommitStateDirty = true;
      }
   }

   // ***********************************************************************
   //                        Internal methods
   // ***********************************************************************

   private final <T extends Statement> T trackStatement(final T statement)
   {
      openStatements.add(statement);

      return statement;
   }

   private final void closeStatements()
   {
      final int size = openStatements.size();
      if (size > 0) {
         for (int i = 0; i < size; i++) {
            try {
               final Statement statement = openStatements.get(i);
               if (statement != null) {
                  statement.close();
               }
            }
            catch (SQLException e) {
               checkException(e);
            }
         }

         openStatements.clear();
      }
   }

   // **********************************************************************
   //                   "Overridden" java.sql.Connection Methods
   // **********************************************************************

   /** {@inheritDoc} */
   @Override
   public final void close() throws SQLException
   {
      if (delegate != ClosedConnection.CLOSED_CONNECTION) {
         leakTask.cancel();

         try {
            closeStatements();
            if (isCommitStateDirty && !isAutoCommit) {
               delegate.rollback();
               lastAccess = clockSource.currentTime();
               LOGGER.debug("{} - Executed rollback on connection {} due to dirty commit state on close().", poolEntry.getPoolName(), delegate);
            }

            if (dirtyBits != 0) {
               poolEntry.resetConnectionState(this, dirtyBits);
               lastAccess = clockSource.currentTime();
            }

            delegate.clearWarnings();
         }
         catch (SQLException e) {
            // when connections are aborted, exceptions are often thrown that should not reach the application
            if (!poolEntry.isEvicted()) {
               throw checkException(e);
            }
         }
         finally {
            delegate = ClosedConnection.CLOSED_CONNECTION;
            poolEntry.returnPoolEntry(lastAccess);
         }
      }
   }

   /** {@inheritDoc} */
   @Override
   public boolean isClosed() throws SQLException
   {
      return (delegate == ClosedConnection.CLOSED_CONNECTION);
   }

   /** {@inheritDoc} */
   @Override
   public Statement createStatement() throws SQLException
   {
      return ProxyFactory.getProxyStatement(this, trackStatement(delegate.createStatement()));
   }

   /** {@inheritDoc} */
   @Override
   public Statement createStatement(int resultSetType, int concurrency) throws SQLException
   {
      return ProxyFactory.getProxyStatement(this, trackStatement(delegate.createStatement(resultSetType, concurrency)));
   }

   /** {@inheritDoc} */
   @Override
   public Statement createStatement(int resultSetType, int concurrency, int holdability) throws SQLException
   {
      return ProxyFactory.getProxyStatement(this, trackStatement(delegate.createStatement(resultSetType, concurrency, holdability)));
   }

   /** {@inheritDoc} */
   @Override
   public CallableStatement prepareCall(String sql) throws SQLException
   {
      return ProxyFactory.getProxyCallableStatement(this, trackStatement(delegate.prepareCall(sql)));
   }

   /** {@inheritDoc} */
   @Override
   public CallableStatement prepareCall(String sql, int resultSetType, int concurrency) throws SQLException
   {
      return ProxyFactory.getProxyCallableStatement(this, trackStatement(delegate.prepareCall(sql, resultSetType, concurrency)));
   }

   /** {@inheritDoc} */
   @Override
   public CallableStatement prepareCall(String sql, int resultSetType, int concurrency, int holdability) throws SQLException
   {
      return ProxyFactory.getProxyCallableStatement(this, trackStatement(delegate.prepareCall(sql, resultSetType, concurrency, holdability)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql, autoGeneratedKeys)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int concurrency) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql, resultSetType, concurrency)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql, int resultSetType, int concurrency, int holdability) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql, resultSetType, concurrency, holdability)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql, columnIndexes)));
   }

   /** {@inheritDoc} */
   @Override
   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
   {
      return ProxyFactory.getProxyPreparedStatement(this, trackStatement(delegate.prepareStatement(sql, columnNames)));
   }

   /** {@inheritDoc} */
   @Override
   public void commit() throws SQLException
   {
      delegate.commit();
      isCommitStateDirty = false;
      lastAccess = clockSource.currentTime();
   }

   /** {@inheritDoc} */
   @Override
   public void rollback() throws SQLException
   {
      delegate.rollback();
      isCommitStateDirty = false;
      lastAccess = clockSource.currentTime();
   }

   /** {@inheritDoc} */
   @Override
   public void rollback(Savepoint savepoint) throws SQLException
   {
      delegate.rollback(savepoint);
      isCommitStateDirty = false;
      lastAccess = clockSource.currentTime();
   }

   /** {@inheritDoc} */
   @Override
   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      delegate.setAutoCommit(autoCommit);
      isAutoCommit = autoCommit;
      dirtyBits |= 0b00010;
   }

   /** {@inheritDoc} */
   @Override
   public void setReadOnly(boolean readOnly) throws SQLException
   {
      delegate.setReadOnly(readOnly);
      isReadOnly = readOnly;
      dirtyBits |= 0b00001;
   }

   /** {@inheritDoc} */
   @Override
   public void setTransactionIsolation(int level) throws SQLException
   {
      delegate.setTransactionIsolation(level);
      transactionIsolation = level;
      dirtyBits |= 0b00100;
   }

   /** {@inheritDoc} */
   @Override
   public void setCatalog(String catalog) throws SQLException
   {
      delegate.setCatalog(catalog);
      dbcatalog = catalog;
      dirtyBits |= 0b01000;
   }

   /** {@inheritDoc} */
   @Override
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      delegate.setNetworkTimeout(executor, milliseconds);
      networkTimeout = milliseconds;
      dirtyBits |= 0b10000;
   }

   /** {@inheritDoc} */
   @Override
   public final boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return iface.isInstance(delegate) || (delegate instanceof Wrapper && delegate.isWrapperFor(iface));
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("unchecked")
   public final <T> T unwrap(Class<T> iface) throws SQLException
   {
      if (iface.isInstance(delegate)) {
         return (T) delegate;
      }
      else if (delegate instanceof Wrapper) {
          return (T) delegate.unwrap(iface);
      }

      throw new SQLException("Wrapped connection is not an instance of " + iface);
   }
}