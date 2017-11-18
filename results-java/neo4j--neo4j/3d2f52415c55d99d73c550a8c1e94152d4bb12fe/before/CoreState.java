/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.causalclustering.core.state;

import java.io.IOException;
import java.util.Optional;

import org.neo4j.causalclustering.catchup.storecopy.LocalDatabase;
import org.neo4j.causalclustering.catchup.storecopy.StoreCopyFailedException;
import org.neo4j.causalclustering.core.consensus.RaftMachine;
import org.neo4j.causalclustering.core.consensus.RaftMessages;
import org.neo4j.causalclustering.core.consensus.log.pruning.LogPruner;
import org.neo4j.causalclustering.core.consensus.outcome.ConsensusOutcome;
import org.neo4j.causalclustering.core.state.machines.CoreStateMachines;
import org.neo4j.causalclustering.core.state.snapshot.CoreSnapshot;
import org.neo4j.causalclustering.core.state.snapshot.CoreStateDownloader;
import org.neo4j.causalclustering.identity.ClusterBinder;
import org.neo4j.causalclustering.identity.ClusterId;
import org.neo4j.causalclustering.identity.MemberId;
import org.neo4j.causalclustering.messaging.Inbound.MessageHandler;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;

import static java.util.concurrent.TimeUnit.MINUTES;

public class CoreState implements MessageHandler<RaftMessages.ClusterIdAwareMessage>, LogPruner, Lifecycle
{
    private final RaftMachine raftMachine;
    private final LocalDatabase localDatabase;
    private final Log log;
    private final ClusterBinder clusterBinder;
    private final CoreStateDownloader downloader;
    private final CommandApplicationProcess applicationProcess;
    private final CoreStateMachines coreStateMachines;
    private boolean allowMessageHandling;

    public CoreState(
            RaftMachine raftMachine,
            LocalDatabase localDatabase,
            ClusterBinder clusterBinder,
            LogProvider logProvider,
            CoreStateDownloader downloader,
            CommandApplicationProcess commandApplicationProcess,
            CoreStateMachines coreStateMachines )
    {
        this.raftMachine = raftMachine;
        this.localDatabase = localDatabase;
        this.clusterBinder = clusterBinder;
        this.downloader = downloader;
        this.log = logProvider.getLog( getClass() );
        this.applicationProcess = commandApplicationProcess;
        this.coreStateMachines = coreStateMachines;
    }

    public synchronized void handle( RaftMessages.ClusterIdAwareMessage clusterIdAwareMessage )
    {
        Optional<ClusterId> optionalClusterId = clusterBinder.get();
        if ( !allowMessageHandling || !optionalClusterId.isPresent() )
        {
            return;
        }

        ClusterId boundClusterId = optionalClusterId.get();
        ClusterId msgClusterId = clusterIdAwareMessage.clusterId();
        if ( msgClusterId.equals( boundClusterId ) )
        {
            try
            {
                ConsensusOutcome outcome = raftMachine.handle( clusterIdAwareMessage.message() );
                if ( outcome.needsFreshSnapshot() )
                {
                    downloadSnapshot( clusterIdAwareMessage.message().from() );
                }
                else
                {
                    notifyCommitted( outcome.getCommitIndex() );
                }
            }
            catch ( Throwable e )
            {
                log.error( "Error handling message", e );
                raftMachine.panic();
                localDatabase.panic( e );
            }
        }
        else
        {
            log.info( "Discarding message[%s] owing to mismatched clusterId. Expected: %s, Encountered: %s",
                    clusterIdAwareMessage.message(), boundClusterId, msgClusterId );
        }
    }

    private synchronized void notifyCommitted( long commitIndex )
    {
        applicationProcess.notifyCommitted( commitIndex );
    }

    /**
     * Attempts to download a fresh snapshot from another core instance.
     *
     * @param source The source address to attempt a download of a snapshot from.
     */
    private synchronized void downloadSnapshot( MemberId source )
    {
        try
        {
            applicationProcess.sync();
            downloader.downloadSnapshot( source, this );
        }
        catch ( InterruptedException | StoreCopyFailedException e )
        {
            log.error( "Failed to download snapshot", e );
        }
    }

    public synchronized CoreSnapshot snapshot() throws IOException, InterruptedException
    {
        return applicationProcess.snapshot( raftMachine );
    }

    public synchronized void installSnapshot( CoreSnapshot coreSnapshot ) throws Throwable
    {
        applicationProcess.installSnapshot( coreSnapshot, raftMachine );
        notifyAll();
    }

    @SuppressWarnings("unused") // used in embedded robustness testing
    public long lastApplied()
    {
        return applicationProcess.lastApplied();
    }

    @Override
    public void prune() throws IOException
    {
        raftMachine.handle( new RaftMessages.PruneRequest( applicationProcess.lastFlushed() ) );
    }

    @Override
    public synchronized void init() throws Throwable
    {
        localDatabase.init();
        applicationProcess.init();
    }

    @Override
    public synchronized void start() throws Throwable
    {
        // How can state be installed?
        // 1. Already installed (detected by checking on-disk state)
        // 2. Bootstrap (single selected server)
        // 3. Download from someone else (others)

        clusterBinder.bindToCluster( this::installSnapshot );
        allowMessageHandling = true;

        // TODO: Move haveState and CoreBootstrapper into CommandApplicationProcess, which perhaps needs a better name.
        // TODO: Include the None/Partial/Full in the move.

        long endTime = System.currentTimeMillis() + MINUTES.toMillis( 30 );
        while ( !haveState() )
        {
            if ( System.currentTimeMillis() > endTime )
            {
                throw new RuntimeException( "This machine failed to get the start state in time." );
            }

            wait( 1000 );
        }

        localDatabase.start();
        coreStateMachines.installCommitProcess( localDatabase.getCommitProcess() );
        applicationProcess.start();
        raftMachine.startTimers();
    }

    private boolean haveState()
    {
        // this is updated when a snapshot is installed and
        // the earliest snapshot is at 0
        return raftMachine.state().appendIndex() > -1;
    }

    @Override
    public synchronized void stop() throws Throwable
    {
        raftMachine.stopTimers();
        applicationProcess.stop();
        localDatabase.stop();
        allowMessageHandling = false;
    }

    @Override
    public synchronized void shutdown() throws Throwable
    {
        applicationProcess.shutdown();
        localDatabase.shutdown();
    }
}