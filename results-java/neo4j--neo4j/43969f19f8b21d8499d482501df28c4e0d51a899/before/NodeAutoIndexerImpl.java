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
package org.neo4j.kernel;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.index.Index;

class NodeAutoIndexerImpl extends AbstractAutoIndexerImpl<Node>
{
    static final String NODE_AUTO_INDEX = "node_auto_index";

    public NodeAutoIndexerImpl( EmbeddedGraphDbImpl gdb )
    {
        super( gdb );
    }

    @Override
    protected Iterable<PropertyEntry<Node>> getAssignedPropertiesOnCommit(
            TransactionData data )
    {
        return data.assignedNodeProperties();
    }

    @Override
    protected String getAutoIndexConfigListName()
    {
        return Config.NODE_KEYS_INDEXABLE;
    }

    @Override
    protected String getAutoIndexName()
    {
        return NODE_AUTO_INDEX;
    }

    @Override
    protected String getEnableConfigName()
    {
        return Config.NODE_AUTO_INDEXING;
    }

    @Override
    protected Index<Node> getIndexInternal()
    {
        return getGraphDbImpl().index().forNodes( NODE_AUTO_INDEX );
    }

    @Override
    protected Iterable<PropertyEntry<Node>> getRemovedPropertiesOnCommit(
            TransactionData data )
    {
        return data.removedNodeProperties();
    }
}