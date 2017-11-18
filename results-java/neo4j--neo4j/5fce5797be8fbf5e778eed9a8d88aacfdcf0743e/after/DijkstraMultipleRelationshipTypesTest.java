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
package org.neo4j.graphalgo.shortestPath;

import org.neo4j.graphalgo.shortestpath.CostEvaluator;
import org.neo4j.graphalgo.shortestpath.Dijkstra;
import org.neo4j.graphalgo.shortestpath.std.DoubleAdder;
import org.neo4j.graphalgo.shortestpath.std.DoubleComparator;
import org.neo4j.graphalgo.testUtil.Neo4jAlgoTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class DijkstraMultipleRelationshipTypesTest extends Neo4jAlgoTestCase
{
    public DijkstraMultipleRelationshipTypesTest( String arg0 )
    {
        super( arg0 );
    }

    protected Dijkstra<Double> getDijkstra( String startNode, String endNode,
        RelationshipType... relTypes )
    {
        return new Dijkstra<Double>( 0.0, graph.getNode( startNode ), graph
            .getNode( endNode ), new CostEvaluator<Double>()
        {
            public Double getCost( Relationship relationship, boolean backwards )
            {
                return 1.0;
            }
        }, new DoubleAdder(), new DoubleComparator(), Direction.BOTH, relTypes );
    }

    public void runTest()
    {
        graph.setCurrentRelType( MyRelTypes.R1 );
        graph.makeEdgeChain( "a,b,c,d,e" );
        graph.setCurrentRelType( MyRelTypes.R2 );
        graph.makeEdges( "a,c" ); // first shortcut
        graph.setCurrentRelType( MyRelTypes.R3 );
        graph.makeEdges( "c,e" ); // second shortcut
        Dijkstra<Double> dijkstra;
        dijkstra = getDijkstra( "a", "e", MyRelTypes.R1 );
        assertTrue( dijkstra.getCost() == 4.0 );
        dijkstra = getDijkstra( "a", "e", MyRelTypes.R1, MyRelTypes.R2 );
        assertTrue( dijkstra.getCost() == 3.0 );
        dijkstra = getDijkstra( "a", "e", MyRelTypes.R1, MyRelTypes.R3 );
        assertTrue( dijkstra.getCost() == 3.0 );
        dijkstra = getDijkstra( "a", "e", MyRelTypes.R1, MyRelTypes.R2,
            MyRelTypes.R3 );
        assertTrue( dijkstra.getCost() == 2.0 );
    }
}