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

import org.neo4j.graphalgo.shortestpath.CostEvaluator;
import org.neo4j.graphalgo.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.shortestpath.SingleSourceShortestPathDijkstra;
import org.neo4j.graphalgo.shortestpath.std.DoubleAdder;
import org.neo4j.graphalgo.testUtil.Neo4jAlgoTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

public class ParallellCentralityCalculationTest extends Neo4jAlgoTestCase
{
    public ParallellCentralityCalculationTest( String arg0 )
    {
        super( arg0 );
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

    protected void assertCentrality(
        ShortestPathBasedCentrality<Double,Double> centrality, String nodeId,
        Double value )
    {
        assertTrue( centrality.getCentrality( graph.getNode( nodeId ) ).equals(
            value ) );
    }

    public void testPlusShape()
    {
        // Make graph
        graph.makeEdgeChain( "a,b,c" );
        graph.makeEdgeChain( "d,b,e" );
        SingleSourceShortestPath<Double> singleSourceShortestPath = getSingleSourceShortestPath();
        ParallellCentralityCalculation<Double> pcc = new ParallellCentralityCalculation<Double>(
            singleSourceShortestPath, graph.getAllNodes() );
        BetweennessCentrality<Double> betweennessCentrality = new BetweennessCentrality<Double>(
            singleSourceShortestPath, graph.getAllNodes() );
        StressCentrality<Double> stressCentrality = new StressCentrality<Double>(
            singleSourceShortestPath, graph.getAllNodes() );
        ClosenessCentrality<Double> closenessCentrality = new ClosenessCentrality<Double>(
            singleSourceShortestPath, new DoubleAdder(), 0.0, graph
                .getAllNodes(), new CostDivider<Double>()
            {
                public Double divideByCost( Double d, Double c )
                {
                    return d / c;
                }

                public Double divideCost( Double c, Double d )
                {
                    return c / d;
                }
            } );
        pcc.addCalculation( betweennessCentrality );
        pcc.addCalculation( stressCentrality );
        pcc.addCalculation( closenessCentrality );
        pcc.calculate();
        // for ( Node node : graph.getAllNodes() )
        // {
        // System.out.println( "Dependency: " + graph.getNodeId( node ) + " "
        // + stressCentrality.getCentrality( node ) );
        // }
        assertCentrality( betweennessCentrality, "a", 0.0 );
        assertCentrality( betweennessCentrality, "b", 6.0 );
        assertCentrality( betweennessCentrality, "c", 0.0 );
        assertCentrality( betweennessCentrality, "d", 0.0 );
        assertCentrality( betweennessCentrality, "e", 0.0 );
        assertCentrality( stressCentrality, "a", 0.0 );
        assertCentrality( stressCentrality, "b", 6.0 );
        assertCentrality( stressCentrality, "c", 0.0 );
        assertCentrality( stressCentrality, "d", 0.0 );
        assertCentrality( stressCentrality, "e", 0.0 );
        assertCentrality( closenessCentrality, "a", 1.0 / 7 );
        assertCentrality( closenessCentrality, "b", 1.0 / 4 );
        assertCentrality( closenessCentrality, "c", 1.0 / 7 );
        assertCentrality( closenessCentrality, "d", 1.0 / 7 );
        assertCentrality( closenessCentrality, "e", 1.0 / 7 );
    }
}