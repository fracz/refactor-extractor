/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.edge;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import org.neo4j.coreedge.catchup.storecopy.CopiedStoreRecovery;
import org.neo4j.coreedge.catchup.storecopy.LocalDatabase;
import org.neo4j.coreedge.catchup.storecopy.StoreFetcher;
import org.neo4j.coreedge.catchup.storecopy.StoreIdDownloadFailedException;
import org.neo4j.coreedge.core.state.machines.tx.ConstantTimeRetryStrategy;
import org.neo4j.coreedge.discovery.ClusterTopology;
import org.neo4j.coreedge.discovery.TopologyService;
import org.neo4j.coreedge.identity.MemberId;
import org.neo4j.coreedge.identity.StoreId;
import org.neo4j.coreedge.messaging.routing.AlwaysChooseFirstMember;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.NullLogProvider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.helpers.collection.Iterators.asSet;

public class EdgeStartupProcessTest
{
    private CopiedStoreRecovery copiedStoreRecovery = mock( CopiedStoreRecovery.class );
    private StoreFetcher storeFetcher = mock( StoreFetcher.class );
    private LocalDatabase localDatabase = mock( LocalDatabase.class );
    private TopologyService hazelcastTopology = mock( TopologyService.class );
    private ClusterTopology clusterTopology = mock( ClusterTopology.class );
    private Lifecycle txPulling = mock( Lifecycle.class );

    private MemberId memberId = new MemberId( UUID.randomUUID() );
    private StoreId localStoreId = new StoreId( 1, 2, 3, 4 );
    private StoreId otherStoreId = new StoreId( 5, 6, 7, 8 );
    private File storeDir = new File( "store-dir" );

    @Before
    public void commonMocking() throws StoreIdDownloadFailedException
    {
        when( localDatabase.storeDir() ).thenReturn( storeDir );
        when( localDatabase.storeId() ).thenReturn( localStoreId );
        when( hazelcastTopology.currentTopology() ).thenReturn( clusterTopology );
        when( clusterTopology.coreMembers() ).thenReturn( asSet( memberId ) );
    }

    @Test
    public void shouldReplaceEmptyStoreWithRemote() throws Throwable
    {
        // given
        when( localDatabase.isEmpty() ).thenReturn( true );
        when( storeFetcher.getStoreIdOf( any() ) ).thenReturn( otherStoreId );

        EdgeStartupProcess edgeStartupProcess = new EdgeStartupProcess( storeFetcher, localDatabase,
                txPulling, new AlwaysChooseFirstMember( hazelcastTopology ),
                new ConstantTimeRetryStrategy( 1, MILLISECONDS ), NullLogProvider.getInstance(),
                copiedStoreRecovery );

        // when
        edgeStartupProcess.start();

        // then
        verify( localDatabase ).stop();
        verify( storeFetcher ).copyStore( any(), any(), any() );
        verify( localDatabase, times( 2 ) ).start(); // once for initial start, once for after store copy
        verify( txPulling ).start();
    }

    @Test
    public void shouldNotStartWithMismatchedNonEmptyStore() throws Throwable
    {
        // given
        when( localDatabase.isEmpty() ).thenReturn( false );
        when( storeFetcher.getStoreIdOf( any() ) ).thenReturn( otherStoreId );

        EdgeStartupProcess edgeStartupProcess = new EdgeStartupProcess( storeFetcher, localDatabase,
                txPulling, new AlwaysChooseFirstMember( hazelcastTopology ),
                new ConstantTimeRetryStrategy( 1, MILLISECONDS ), NullLogProvider.getInstance(), copiedStoreRecovery );

        // when
        try
        {
            edgeStartupProcess.start();
            fail( "should have thrown" );
        }
        catch ( IllegalStateException ex )
        {
            // expected
        }

        // then
        verify( txPulling, never() ).start();
    }

    @Test
    public void shouldStartWithMatchingDatabase() throws Throwable
    {
        // given
        when( storeFetcher.getStoreIdOf( any() ) ).thenReturn( localStoreId );
        when( localDatabase.isEmpty() ).thenReturn( false );

        EdgeStartupProcess edgeStartupProcess = new EdgeStartupProcess( storeFetcher, localDatabase,
                txPulling, new AlwaysChooseFirstMember( hazelcastTopology ),
                new ConstantTimeRetryStrategy( 1, MILLISECONDS ), NullLogProvider.getInstance(),
                copiedStoreRecovery );

        // when
        edgeStartupProcess.start();

        // then
        verify( localDatabase ).start();
        verify( txPulling ).start();
    }

    @Test
    public void stopShouldStopTheDatabaseAndStopPolling() throws Throwable
    {
        // given
        when( storeFetcher.getStoreIdOf( any() ) ).thenReturn( localStoreId );
        when( localDatabase.isEmpty() ).thenReturn( false );

        EdgeStartupProcess edgeStartupProcess = new EdgeStartupProcess( storeFetcher, localDatabase,
                txPulling, new AlwaysChooseFirstMember( hazelcastTopology ),
                new ConstantTimeRetryStrategy( 1, MILLISECONDS ), NullLogProvider.getInstance(), copiedStoreRecovery );
        edgeStartupProcess.start();

        // when
        edgeStartupProcess.stop();

        // then
        verify( txPulling ).stop();
        verify( localDatabase ).stop();
    }
}