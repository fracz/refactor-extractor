/*
 * Copyright (c) 2002-2009 "Neo Technology,"
 *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.kernel.impl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

class IntArrayIterator implements Iterable<Relationship>,
    Iterator<Relationship>
{
    private Logger log = Logger
        .getLogger( IntArrayIterator.class.getName() );

    private Iterator<RelTypeElementIterator> typeIterator;
    private RelTypeElementIterator currentTypeIterator = null;
    private NodeImpl fromNode;
    private Direction direction = null;
    private Relationship nextElement = null;
    private final NodeManager nodeManager;
    private int position = 0;
    private final RelationshipType types[];

    private Set<String> visitedTypes = new HashSet<String>();

    IntArrayIterator( List<RelTypeElementIterator> rels, NodeImpl fromNode,
        Direction direction, NodeManager nodeManager, RelationshipType[] types )
    {
        this.typeIterator = rels.iterator();
        if ( typeIterator.hasNext() )
        {
            currentTypeIterator = typeIterator.next();
            visitedTypes.add( currentTypeIterator.getType() );
        }
        else
        {
            currentTypeIterator = new NullRelTypeElement();
        }
        this.fromNode = fromNode;
        this.direction = direction;
        this.nodeManager = nodeManager;
        this.types = types;
    }

//    IntArrayIterator( Iterator<RelTypeElementIterator> rels, NodeImpl fromNode,
//        Direction direction, NodeManager nodeManager )
//    {
//        this.typeIterator = rels;
//        this.fromNode = fromNode;
//        this.direction = direction;
//        this.nodeManager = nodeManager;
//    }

    public Iterator<Relationship> iterator()
    {
        return this;
    }

    public boolean hasNext()
    {
        if ( nextElement != null )
        {
            return true;
        }
        do
        {
            if ( currentTypeIterator.hasNext() )
            {
                int nextId = currentTypeIterator.next();
                try
                {
                    Relationship possibleElement = nodeManager
                        .getRelationshipById( nextId );
                    if ( direction == Direction.INCOMING
                        && possibleElement.getEndNode().equals( fromNode ) )
                    {
                        nextElement = possibleElement;
                        return true;
                    }
                    else if ( direction == Direction.OUTGOING
                        && possibleElement.getStartNode().equals( fromNode ) )
                    {
                        nextElement = possibleElement;
                        return true;
                    }
                    else if ( direction == Direction.BOTH )
                    {
                        nextElement = possibleElement;
                        return true;
                    }
                }
                catch ( NotFoundException e )
                {
                    log.log( Level.FINE,
                        "Unable to get relationship " + nextId, e );
                }
            }
            while ( !currentTypeIterator.hasNext() )
            {
                if ( typeIterator.hasNext() )
                {
                    currentTypeIterator = typeIterator.next();
                    visitedTypes.add( currentTypeIterator.getType() );
                }
                else
                {
                    boolean gotMore = fromNode.getMoreRelationships();
                    List<RelTypeElementIterator> list = Collections.EMPTY_LIST;
                    if ( types.length == 0 )
                    {
                        list = fromNode.getAllRelationships();
                    }
                    else
                    {
                        list = fromNode.getAllRelationshipsOfType( types );
                    }
                    Iterator<RelTypeElementIterator> itr = list.iterator();
                    while ( itr.hasNext() )
                    {
                        RelTypeElementIterator element = itr.next();
                        if ( visitedTypes.contains( element.getType() ) )
                        {
                            itr.remove();
                        }
                    }
                    typeIterator = list.iterator();
                    if ( typeIterator.hasNext() )
                    {
                        currentTypeIterator = typeIterator.next();
                        visitedTypes.add( currentTypeIterator.getType() );
                    }
                    if ( !gotMore )
                    {
                        break;
                    }
                }
            }
         } while ( currentTypeIterator.hasNext() );
        // no next element found
        return false;
    }

    public Relationship next()
    {
        hasNext();
        if ( nextElement != null )
        {
            Relationship elementToReturn = nextElement;
            nextElement = null;
            return elementToReturn;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}