/*
 * Copyright 2008 Network Engine for Objects in Lund AB [neotechnology.com]
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.centrality;

import java.util.Set;

import org.neo4j.graphalgo.shortestpath.CostEvaluator;
import org.neo4j.graphalgo.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.shortestpath.SingleSourceShortestPathDijkstra;
import org.neo4j.graphalgo.testUtil.Neo4jAlgoTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class StressCentralityTest extends Neo4jAlgoTestCase
{
    public StressCentralityTest( String name )
    {
        super( name );
    }

    protected SingleSourceShortestPath<Double> getSingleSourceShortestPath()
    {
        return new SingleSourceShortestPathDijkstra<Double>( 0.0, null,
            new CostEvaluator<Double>()
            {
                public Double getCost( Relationship relationship,
                    boolean backwards )
                {
                    return 1.0;
                }
            }, new org.neo4j.graphalgo.shortestpath.std.DoubleAdder(),
            new org.neo4j.graphalgo.shortestpath.std.DoubleComparator(),
            Direction.BOTH, MyRelTypes.R1 );
    }

    protected void assertCentrality( StressCentrality<Double> stressCentrality,
        String nodeId, Double value )
    {
        assertTrue( stressCentrality.getCentrality( graph.getNode( nodeId ) )
            .equals( value ) );
    }

    public void testBox()
    {
        graph.makeEdgeChain( "a,b,c,d,a" );
        StressCentrality<Double> stressCentrality = new StressCentrality<Double>(
            getSingleSourceShortestPath(), graph.getAllNodes() );
        stressCentrality.calculate();
        assertCentrality( stressCentrality, "a", 1.0 );
        assertCentrality( stressCentrality, "b", 1.0 );
        assertCentrality( stressCentrality, "c", 1.0 );
        assertCentrality( stressCentrality, "d", 1.0 );
    }

    public void testPlusShape()
    {
        graph.makeEdgeChain( "a,b,c" );
        graph.makeEdgeChain( "d,b,e" );
        StressCentrality<Double> stressCentrality = new StressCentrality<Double>(
            getSingleSourceShortestPath(), graph.getAllNodes() );
        stressCentrality.calculate();
        assertCentrality( stressCentrality, "a", 0.0 );
        assertCentrality( stressCentrality, "b", 6.0 );
        assertCentrality( stressCentrality, "c", 0.0 );
        assertCentrality( stressCentrality, "d", 0.0 );
        assertCentrality( stressCentrality, "e", 0.0 );
    }

    public void testChain()
    {
        graph.makeEdgeChain( "a,b,c,d,e" );
        StressCentrality<Double> stressCentrality = new StressCentrality<Double>(
            getSingleSourceShortestPath(), graph.getAllNodes() );
        stressCentrality.calculate();
        assertCentrality( stressCentrality, "a", 0.0 );
        assertCentrality( stressCentrality, "b", 3.0 );
        assertCentrality( stressCentrality, "c", 4.0 );
        assertCentrality( stressCentrality, "d", 3.0 );
        assertCentrality( stressCentrality, "e", 0.0 );
    }

    public void testStressUpdating()
    {
        graph.makeEdgeChain( "a,b,c,d,e,f" );
        new StressTest( getSingleSourceShortestPath(), graph.getAllNodes() )
            .test();
    }

    class StressTest extends StressCentrality<Double>
    {
        public StressTest(
            SingleSourceShortestPath<Double> singleSourceShortestPath,
            Set<Node> nodeSet )
        {
            super( singleSourceShortestPath, nodeSet );
        }

        public void test()
        {
            // avoid starting the real calculation by mistake
            this.doneCalculation = true;
            // set things up
            Node startNode = graph.getNode( "c" );
            singleSourceShortestPath.reset();
            singleSourceShortestPath.setStartNode( startNode );
            processShortestPaths( startNode, singleSourceShortestPath );
            Double adjustment = 0.5; // since direction is BOTH
            assertCentrality( this, "a", 0.0 * adjustment );
            assertCentrality( this, "b", 1.0 * adjustment );
            assertCentrality( this, "c", 0.0 * adjustment );
            assertCentrality( this, "d", 2.0 * adjustment );
            assertCentrality( this, "e", 1.0 * adjustment );
            assertCentrality( this, "f", 0.0 * adjustment );
        }
    }
}