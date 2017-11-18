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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledExecutorService;

import com.zaxxer.hikari.pool.PoolBagEntry;

/**
 * The interface used by the Connection proxy and through which all interaction
 * by other classes flow.
 *
 * @author Brett Wooldridge
 */
public interface IHikariConnectionProxy extends Connection
{
   /**
    * Get the ConcurrentBag entry that is associated in the pool with the underlying connection.
    *
    * @return the PoolBagEntry
    */
   PoolBagEntry getPoolBagEntry();

   /**
    * Catpure the stack and start leak detection.
    *
    * @param leakThreshold the number of milliseconds before a leak is reported
    * @param houseKeepingExecutorService the executor service to run the leak detection task with
    */
   void captureStack(long leakThreshold, ScheduledExecutorService houseKeepingExecutorService);

   /**
    * Check if the provided SQLException contains a SQLSTATE that indicates
    * a disconnection from the server.
    *
    * @param sqle the SQLException to check
    */
   void checkException(SQLException sqle);

   /**
    * Return the broken state of the connection.  If checkException() detected
    * a broken connection, this method will return true, otherwise false.
    *
    * @return the broken state of the connection
    */
   boolean isBrokenConnection();

   /**
    * Called by Statement and its subclasses when they are closed to remove them
    * from the tracking list.
    *
    * @param statement the Statement to remove from tracking
    */
   void untrackStatement(Statement statement);
}