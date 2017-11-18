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
package org.neo4j.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.com.ComException;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.test.DbRepresentation;

public class TestBackup
{
    private final String serverPath = "target/var/serverdb";
    private final String otherServerPath = serverPath + "2";
    private final String backupPath = "target/var/backuedup-serverdb";

    @Before
    public void before() throws Exception
    {
        FileUtils.deleteDirectory( new File( serverPath ) );
        FileUtils.deleteDirectory( new File( otherServerPath ) );
        FileUtils.deleteDirectory( new File( backupPath ) );
    }

    // TODO MP: What happens if the server database keeps growing, virtually making the files endless?

    @Test
    public void makeSureFullFailsWhenDbExists() throws Exception
    {
        createInitialDataSet( serverPath );
        ServerInterface server = startServer( serverPath );
        OnlineBackup backup = OnlineBackup.from( "localhost" );
        createInitialDataSet( backupPath );
        try
        {
            backup.full( backupPath );
            fail( "Shouldn't be able to do full backup into existing db" );
        }
        catch ( Exception e )
        {
            // good
        }
        shutdownServer( server );
    }

    @Test
    public void makeSureIncrementalFailsWhenNoDb() throws Exception
    {
        createInitialDataSet( serverPath );
        ServerInterface server = startServer( serverPath );
        OnlineBackup backup = OnlineBackup.from( "localhost" );
        try
        {
            backup.incremental( backupPath );
            fail( "Shouldn't be able to do incremental backup into non-existing db" );
        }
        catch ( Exception e )
        {
            // Good
        }
        shutdownServer( server );
    }

    @Test
    public void fullThenIncremental() throws Exception
    {
        DbRepresentation initialDataSetRepresentation = createInitialDataSet( serverPath );
        ServerInterface server = startServer( serverPath );
        OnlineBackup backup = OnlineBackup.from( "localhost" );
        backup.full( backupPath );
        assertEquals( initialDataSetRepresentation, DbRepresentation.of( backupPath ) );
        shutdownServer( server );

        DbRepresentation furtherRepresentation = addMoreData( serverPath );
        server = startServer( serverPath );
        backup.incremental( backupPath );
        assertEquals( furtherRepresentation, DbRepresentation.of( backupPath ) );
        shutdownServer( server );
    }

    @Test
    public void makeSureStoreIdIsEnforced() throws Exception
    {
        // Create data set X on server A
        DbRepresentation initialDataSetRepresentation = createInitialDataSet( serverPath );
        ServerInterface server = startServer( serverPath );

        // Grab initial backup from server A
        OnlineBackup backup = OnlineBackup.from( "localhost" );
        backup.full( backupPath );
        assertEquals( initialDataSetRepresentation, DbRepresentation.of( backupPath ) );
        shutdownServer( server );

        // Create data set X+Y on server B
        createInitialDataSet( otherServerPath );
        addMoreData( otherServerPath );
        server = startServer( otherServerPath );

        // Try to grab incremental backup from server B.
        // Data should be OK, but store id check should prevent that.
        try
        {
            backup.incremental( backupPath );
            fail( "Shouldn't work" );
        }
        catch ( ComException e )
        { // Good
        }
        shutdownServer( server );

        // Just make sure incremental backup can be received properly from
        // server A, even after a failed attempt from server B
        DbRepresentation furtherRepresentation = addMoreData( serverPath );
        server = startServer( serverPath );
        backup.incremental( backupPath );
        assertEquals( furtherRepresentation, DbRepresentation.of( backupPath ) );
        shutdownServer( server );
    }

    private ServerInterface startServer( String path ) throws Exception
    {
        /*
        ServerProcess server = new ServerProcess();
        try
        {
            server.startup( Pair.of( path, "true" ) );
        }
        catch ( Throwable e )
        {
            // TODO Auto-generated catch block
            throw new RuntimeException( e );
        }
        */
        ServerInterface server = new EmbeddedServer( path, "true" );
        server.awaitStarted();
        return server;
    }

    private void shutdownServer( ServerInterface server ) throws Exception
    {
        server.shutdown();
        Thread.sleep( 1000 );
    }

    private DbRepresentation addMoreData( String path )
    {
        GraphDatabaseService db = startGraphDatabase( path );
        Transaction tx = db.beginTx();
        Node node = db.createNode();
        node.setProperty( "backup", "Is great" );
        db.getReferenceNode().createRelationshipTo( node,
                DynamicRelationshipType.withName( "LOVES" ) );
        tx.success();
        tx.finish();
        DbRepresentation result = DbRepresentation.of( db );
        db.shutdown();
        return result;
    }

    private GraphDatabaseService startGraphDatabase( String path )
    {
        return new EmbeddedGraphDatabase( path, stringMap(
                Config.KEEP_LOGICAL_LOGS, "true" ) );
    }

    private DbRepresentation createInitialDataSet( String path )
    {
        GraphDatabaseService db = startGraphDatabase( path );
        Transaction tx = db.beginTx();
        Node node = db.createNode();
        node.setProperty( "myKey", "myValue" );
        db.getReferenceNode().createRelationshipTo( node,
                DynamicRelationshipType.withName( "KNOWS" ) );
        tx.success();
        tx.finish();
        DbRepresentation result = DbRepresentation.of( db );
        db.shutdown();
        return result;
    }
}