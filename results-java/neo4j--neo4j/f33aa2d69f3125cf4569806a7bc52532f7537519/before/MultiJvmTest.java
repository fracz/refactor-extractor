/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package slavetest;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.shell.impl.RmiLocation;

public class MultiJvmTest extends AbstractHaTest
{
    private static final int MASTER_PORT = 8990;

    private List<StandaloneDbCom> jvms = new ArrayList<StandaloneDbCom>();
    private int maxSecondsToWaitToJvmStart = 10;

    @Override
    protected void addDb( Map<String, String> config ) throws Exception
    {
        int machineId = jvms.size();
        File slavePath = dbPath( machineId );
        StandaloneDbCom slaveJvm = spawnJvm( slavePath, MASTER_PORT + machineId, machineId,
                buildExtraArgs( config ) );
        jvms.add( slaveJvm );
    }

    @Override
    protected void setMaxTimeToWaitForDbStart( int seconds )
    {
        this.maxSecondsToWaitToJvmStart = seconds;
    }

    protected static String[] buildExtraArgs( Map<String, String> config )
    {
        List<String> list = new ArrayList<String>();
        for ( Map.Entry<String, String> entry : config.entrySet() )
        {
            list.add( "-" + entry.getKey() );
            list.add( entry.getValue() );
        }
        return list.toArray( new String[list.size()] );
    }

    @After
    public void shutdownDbsAndVerify() throws Exception
    {
        shutdownDbs();

        GraphDatabaseService masterDb = new EmbeddedGraphDatabase( dbPath( 0 ).getAbsolutePath() );
        try
        {
            for ( int i = 1; i < jvms.size(); i++ )
            {
                GraphDatabaseService slaveDb = new EmbeddedGraphDatabase( dbPath( i ).getAbsolutePath() );
                try
                {
                    verify( masterDb, slaveDb );
                }
                finally
                {
                    slaveDb.shutdown();
                }
            }
        }
        finally
        {
            masterDb.shutdown();
        }
    }

    protected void shutdownDbs() throws Exception
    {
        for ( StandaloneDbCom slave : jvms )
        {
            slave.initiateShutdown();
        }
        for ( int i = 0; i < jvms.size(); i++ )
        {
            waitUntilShutdownFileFound( dbPath( i ) );
        }
    }

    protected void waitUntilShutdownFileFound( File slavePath ) throws Exception
    {
        File file = new File( slavePath, "shutdown" );
        while ( !file.exists() )
        {
            Thread.sleep( 100 );
        }
    }

    protected StandaloneDbCom spawnJvm( File path, int port, int machineId,
            String... extraArgs ) throws Exception
    {
        Collection<String> list = new ArrayList<String>( Arrays.asList(
                "java", "-cp", System.getProperty( "java.class.path" ),
                StandaloneDb.class.getName(),
                "-path", path.getAbsolutePath(),
                "-port", "" + port,
                "-id", "" + machineId,
                "-master-id", "0" ) );
        list.addAll( Arrays.asList( extraArgs ) );
        Runtime.getRuntime().exec( list.toArray( new String[list.size()] ) );
        return awaitJvmStarted( port );
    }

    private StandaloneDbCom awaitJvmStarted( int port ) throws RemoteException
    {
        long startTime = System.currentTimeMillis();
        RmiLocation location = RmiLocation.location( "localhost", port, "interface" );
        RemoteException latestException = null;
        StandaloneDbCom result = null;
        while ( result == null && (System.currentTimeMillis() - startTime) < 1000*maxSecondsToWaitToJvmStart )
        {
            try
            {
                result = (StandaloneDbCom) location.getBoundObject();
            }
            catch ( RemoteException e )
            {
                latestException = e;
                // OK, just retry
                try
                {
                    Thread.sleep( 200 );
                }
                catch ( InterruptedException ee )
                { // OK
                }
            }
        }
        if ( result == null )
        {
            throw latestException;
        }
        return result;
    }

    @Override
    protected void pullUpdates( int... slaves ) throws Exception
    {
        if ( slaves.length == 0 )
        {
            for ( int i = 1; i < jvms.size(); i++ )
            {
                jvms.get( i ).pullUpdates();
            }
        }
        else
        {
            for ( int slave : slaves )
            {
                jvms.get( slave+1 ).pullUpdates();
            }
        }
    }

    @Override
    protected <T> T executeJob( Job<T> job, int onSlave ) throws Exception
    {
        return jvms.get( onSlave+1 ).executeJob( job );
    }

    @Override
    protected <T> T executeJobOnMaster( Job<T> job ) throws Exception
    {
        return jvms.get( 0 ).executeJob( job );
    }

    @Override
    protected void startUpMaster( Map<String, String> config ) throws Exception
    {
        Map<String, String> newConfig = new HashMap<String, String>( config );
        newConfig.put( "master", "true" );
        StandaloneDbCom com = spawnJvm( dbPath( 0 ), MASTER_PORT, 0, buildExtraArgs( newConfig ) );
        if ( jvms.isEmpty() )
        {
            jvms.add( com );
        }
        else
        {
            jvms.set( 0, com );
        }
        Thread.sleep( 1000 );
    }

    @Override
    protected Job<Void> getMasterShutdownDispatcher()
    {
        return new CommonJobs.ShutdownJvm( jvms.get( 0 ) );
    }

    @Override
    protected Fetcher<DoubleLatch> getDoubleLatch() throws Exception
    {
        return new MultiJvmDLFetcher();
    }
}