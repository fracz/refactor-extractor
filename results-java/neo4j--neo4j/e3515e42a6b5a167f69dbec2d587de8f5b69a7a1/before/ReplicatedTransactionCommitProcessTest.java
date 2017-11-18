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
package org.neo4j.coreedge.raft.replication.tx;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import org.neo4j.coreedge.raft.replication.Replicator;
import org.neo4j.coreedge.raft.replication.session.LocalOperationId;
import org.neo4j.coreedge.raft.replication.session.LocalSessionPool;
import org.neo4j.coreedge.server.AdvertisedSocketAddress;
import org.neo4j.coreedge.server.CoreMember;
import org.neo4j.coreedge.server.core.CurrentReplicatedLockState;
import org.neo4j.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.kernel.impl.api.TransactionToApply;
import org.neo4j.kernel.impl.logging.NullLogService;
import org.neo4j.kernel.impl.transaction.TransactionRepresentation;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.neo4j.kernel.impl.transaction.tracing.CommitEvent.NULL;
import static org.neo4j.storageengine.api.TransactionApplicationMode.INTERNAL;

@SuppressWarnings("unchecked")
public class ReplicatedTransactionCommitProcessTest
{
    CoreMember coreMember = new CoreMember( new AdvertisedSocketAddress( "core:1" ),
            new AdvertisedSocketAddress( "raft:1" ) );

    @Test
    public void shouldReplicateOnlyOnceIfFirstAttemptSuccessful() throws Exception
    {
        // given
        Replicator replicator = mock( Replicator.class );
        ReplicatedTransactionStateMachine transactionStateMachine = mock( ReplicatedTransactionStateMachine.class );
        Future future = mock( Future.class );

        CurrentReplicatedLockState.LockSession lockSession = mock( CurrentReplicatedLockState.LockSession.class );
        when( lockSession.id() ).thenReturn( 0 );
        CurrentReplicatedLockState currentReplicatedLockState = mock( CurrentReplicatedLockState.class );
        when( currentReplicatedLockState.currentLockSession() ).thenReturn( lockSession );

        when( future.get( anyInt(), any( TimeUnit.class ) ) ).thenReturn( 23l );
        when( transactionStateMachine.getFutureTxId( any( LocalOperationId.class ) ) ).thenReturn( future );

        // when
        new ReplicatedTransactionCommitProcess( replicator, new LocalSessionPool( coreMember ),
                transactionStateMachine, 1, currentReplicatedLockState, NullLogService.getInstance() )
                .commit( tx(), NULL, INTERNAL );

        // then
        verify( replicator, times( 1 ) ).replicate( any( ReplicatedTransaction.class ) );
    }

    @Test
    public void shouldRetryReplicationIfFirstAttemptTimesOut() throws Exception
    {
        // given
        Replicator replicator = mock( Replicator.class );
        ReplicatedTransactionStateMachine transactionStateMachine = mock( ReplicatedTransactionStateMachine.class );
        Future future = mock( Future.class );

        CurrentReplicatedLockState.LockSession lockSession = mock( CurrentReplicatedLockState.LockSession.class );
        when( lockSession.id() ).thenReturn( 0 );
        CurrentReplicatedLockState currentReplicatedLockState = mock( CurrentReplicatedLockState.class );
        when( currentReplicatedLockState.currentLockSession() ).thenReturn( lockSession );

        when( transactionStateMachine.getFutureTxId( any( LocalOperationId.class ) ) ).thenReturn( future );
        when( future.get( anyInt(), any( TimeUnit.class ) ) ).thenThrow( TimeoutException.class ).thenReturn( 23l );

        // when
        new ReplicatedTransactionCommitProcess( replicator, new LocalSessionPool( coreMember ),
                transactionStateMachine, 1, currentReplicatedLockState, NullLogService.getInstance() )
                .commit( tx(), NULL, INTERNAL );

        // then
        verify( replicator, times( 2 ) ).replicate( any( ReplicatedTransaction.class ) );
    }

    @Test
    public void shouldNotRetryReplicationIfLockSessionChanges() throws Exception
    {
        // given
        Replicator replicator = mock( Replicator.class );
        ReplicatedTransactionStateMachine transactionStateMachine = mock( ReplicatedTransactionStateMachine.class );
        Future future = mock( Future.class );

        CurrentReplicatedLockState.LockSession lockSession = mock( CurrentReplicatedLockState.LockSession.class );
        when( lockSession.id() ).thenReturn( 0, 1 ); // Lock session id change.
        CurrentReplicatedLockState currentReplicatedLockState = mock( CurrentReplicatedLockState.class );
        when( currentReplicatedLockState.currentLockSession() ).thenReturn( lockSession );

        when( transactionStateMachine.getFutureTxId( any( LocalOperationId.class ) ) ).thenReturn( future );
        when( future.get( anyInt(), any( TimeUnit.class ) ) ).thenThrow( TimeoutException.class );

        // when
        try
        {
            new ReplicatedTransactionCommitProcess( replicator, new LocalSessionPool( coreMember ),
                    transactionStateMachine, 1, currentReplicatedLockState, NullLogService.getInstance() )
                    .commit( tx(), NULL, INTERNAL );
            fail( "Should have thrown ");
        }
        catch( TransactionFailureException e )
        {
            // expected
        }

        // then
        verify( replicator, times( 1 ) ).replicate( any( ReplicatedTransaction.class ) );
    }

    private TransactionToApply tx()
    {
        TransactionRepresentation tx = mock( TransactionRepresentation.class );
        when( tx.additionalHeader() ).thenReturn( new byte[]{} );
        return new TransactionToApply( tx );
    }
}