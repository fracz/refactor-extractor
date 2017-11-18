/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.stresstests.workload;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Resource;
import org.neo4j.kernel.stresstests.mutation.RandomMutation;

public class Workload implements Resource
{
    private final GraphDatabaseService db;
    private final int threads;
    private final SyncMonitor sync;
    private final Worker worker;
    private final ExecutorService executor;

    public Workload( GraphDatabaseService db, RandomMutation randomMutation, int threads )
    {
        this.db = db;
        this.threads = threads;
        this.sync = new SyncMonitor( threads );
        this.worker = new Worker( db, randomMutation, sync, 100 );
        this.executor = Executors.newCachedThreadPool();
    }

    public interface TransactionThroughput
    {
        TransactionThroughput NONE = new TransactionThroughput()
        {
            @Override
            public void report( long transactions, long timeSlotMillis, long timeElapsedMillis )
            {
                // ignore
            }
        };

        void report( long transactions, long timeSlotMillis, long timeElapsedMillis );
    }

    public void run( long runningTimeMillis, TransactionThroughput throughput )
            throws InterruptedException
    {
        for ( int i = 0; i < threads; i++ )
        {
            executor.submit( worker );
        }

        long now = System.currentTimeMillis();
        long finishLine = runningTimeMillis + now;
        long lastReport = TimeUnit.SECONDS.toMillis( 10 ) + now;
        long previousTransactionCount = 0;
        do
        {
            Thread.sleep( 1000 );
            now = System.currentTimeMillis();
            if ( lastReport <= now )
            {
                long currentTransactionCount = sync.transactions();
                long diff = currentTransactionCount - previousTransactionCount;
                throughput.report( diff, 1000, finishLine - now );
                previousTransactionCount = currentTransactionCount;
                lastReport = TimeUnit.SECONDS.toMillis( 10 ) + now;
            }
        }
        while ( now < finishLine );

        if ( lastReport < now)
        {
            long diff = sync.transactions() - previousTransactionCount;
            throughput.report( diff, 1000, finishLine - now );
        }

        sync.stopAndWaitWorkers();
    }

    @Override
    public void close()
    {
        executor.shutdown();
    }
}