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

import javax.transaction.TransactionManager;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.impl.cache.AdaptiveCacheManager;
import org.neo4j.kernel.impl.persistence.IdGenerator;
import org.neo4j.kernel.impl.persistence.PersistenceManager;
import org.neo4j.kernel.impl.transaction.LockManager;

class ReadOnlyNodeManager extends NodeManager
{
    ReadOnlyNodeManager( AdaptiveCacheManager cacheManager, LockManager lockManager,
        LockReleaser lockReleaser, TransactionManager transactionManager,
        PersistenceManager persistenceManager, IdGenerator idGenerator,
        boolean useNewCaches )
    {
        super( cacheManager, lockManager, lockReleaser, transactionManager,
            persistenceManager, idGenerator, useNewCaches );
    }

    @Override
    public Node createNode()
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    public Relationship createRelationship( Node startNode, Node endNode,
        RelationshipType type )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    PropertyIndex createPropertyIndex( String key )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void deleteNode( NodeImpl node )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    int nodeAddProperty( NodeImpl node, PropertyIndex index, Object value )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void nodeChangeProperty( NodeImpl node, int propertyId, Object value )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void nodeRemoveProperty( NodeImpl node, int propertyId )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void deleteRelationship( RelationshipImpl rel )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    int relAddProperty( RelationshipImpl rel, PropertyIndex index, Object value )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void relChangeProperty( RelationshipImpl rel, int propertyId, Object value )
    {
        throw new ReadOnlyNeoException();
    }

    @Override
    void relRemoveProperty( RelationshipImpl rel, int propertyId )
    {
        throw new ReadOnlyNeoException();
    }
}