/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.server.rrd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import javax.management.MalformedObjectNameException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.server.database.Database;
import org.neo4j.test.ImpermanentGraphDatabase;

public class NodeIdsInUseSampleableTest
{
    public Database db;
    public NodeIdsInUseSampleable sampleable;

    @Test
    public void emptyDbHasZeroNodesInUse() throws IOException, MalformedObjectNameException
    {
        assertThat( sampleable.getValue(), is( 1L ) ); // Reference node is
                                                       // always created in
                                                       // empty dbs
    }

    @Test
    public void addANodeAndSampleableGoesUp() throws IOException, MalformedObjectNameException
    {
        long oldValue = sampleable.getValue();

        createNode( db.graph );

        assertThat( sampleable.getValue(), greaterThan( oldValue ) );
    }

    private void createNode( AbstractGraphDatabase db )
    {
        Transaction tx = db.beginTx();
        db.createNode();
        tx.success();
        tx.finish();
    }

    @Before
    public void setUp() throws Exception
    {
        db = new Database( new ImpermanentGraphDatabase() );
        sampleable = new NodeIdsInUseSampleable( db );
    }

    @After
    public void shutdown()
    {
        db.shutdown();
    }
}