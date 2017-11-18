/**
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.kernel.impl.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.collection.primitive.PrimitiveLongSet;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.kernel.impl.transaction.command.Command;
import org.neo4j.kernel.impl.transaction.command.NeoCommandHandler;
import org.neo4j.kernel.impl.util.statistics.IntCounter;

import static org.neo4j.collection.primitive.Primitive.iterator;
import static org.neo4j.collection.primitive.Primitive.longSet;
import static org.neo4j.collection.primitive.PrimitiveLongCollections.emptyIterator;
import static org.neo4j.kernel.api.ReadOperations.ANY_LABEL;
import static org.neo4j.kernel.api.ReadOperations.ANY_RELATIONSHIP_TYPE;
import static org.neo4j.kernel.impl.store.NodeLabelsField.fieldPointsToDynamicRecordOfLabels;
import static org.neo4j.kernel.impl.store.NodeLabelsField.parseLabelsField;

public class CountsStoreApplier extends NeoCommandHandler.Adapter
{
    private final CountsAccessor.Updater countsUpdater;
    private final NodeStore nodeStore;
    private final Map<Integer/*labelId*/, IntCounter> labelDelta = new HashMap<>();
    private final Map<Integer/*typeId*/, IntCounter> relationshipTypeDelta = new HashMap<>();

    private int nodesDelta;
    private int relsDelta;

    public CountsStoreApplier( CountsAccessor.Updater countsUpdater, NodeStore nodeStore )
    {
        this.countsUpdater = countsUpdater;
        this.nodeStore = nodeStore;
    }

    @Override
    public void close()
    {
        countsUpdater.close();
    }

    private static <KEY> IntCounter counter( Map<KEY, IntCounter> map, KEY key )
    {
        IntCounter counter = map.get( key );
        if ( counter == null )
        {
            map.put( key, counter = new IntCounter() );
        }
        return counter;
    }

    private static PrimitiveLongIterator diff( long[] remove, long[] add )
    {
        if ( add == null || add.length == 0 )
        {
            return emptyIterator();
        }
        else if ( remove == null || remove.length == 0 )
        {
            return iterator( add );
        }
        else
        {
            return removeAll( addAll( longSet( add.length ), add ), remove ).iterator();
        }
    }

    private static PrimitiveLongSet addAll( PrimitiveLongSet target, long... all )
    {
        for ( long label : all )
        {
            target.add( label );
        }
        return target;
    }

    private static PrimitiveLongSet removeAll( PrimitiveLongSet target, long... all )
    {
        for ( long label : all )
        {
            target.remove( label );
        }
        return target;
    }

    @Override
    public boolean visitNodeCommand( Command.NodeCommand command ) throws IOException
    {
        NodeRecord before = command.getBefore(), after = command.getAfter();
        if ( !before.inUse() && after.inUse() )
        { // node added
            nodesDelta++;
        }
        else if ( before.inUse() && !after.inUse() )
        { // node deleted
            nodesDelta--;
        }
        if ( before.getLabelField() != after.getLabelField() ||
             fieldPointsToDynamicRecordOfLabels( before.getLabelField() ) )
        {
            long[] labelsBefore = labels( before );
            long[] labelsAfter = labels( after );
            for ( PrimitiveLongIterator added = diff( labelsBefore, labelsAfter ); added.hasNext(); )
            {
                label( added.next() ).increment();
            }
            for ( PrimitiveLongIterator removed = diff( labelsAfter, labelsBefore ); removed.hasNext(); )
            {
                label( removed.next() ).decrement();
            }
        }
        return false;
    }

    private IntCounter label( long label )
    {
        return counter( labelDelta, (int) label );
    }

    @Override
    public boolean visitRelationshipCommand( Command.RelationshipCommand command ) throws IOException
    {
        RelationshipRecord record = command.getRecord();
        if ( record.isCreated() )
        {
            relsDelta++;
            relationshipType( record.getType() ).increment();
        }
        else if ( !record.inUse() )
        {
            relsDelta--;
            relationshipType( record.getType() ).decrement();
        }
        return false;
    }

    @Override
    public boolean visitUpdateCountsCommand( Command.CountsCommand command ) throws IOException
    {
        countsUpdater.incrementRelationshipCount(
                command.startLabelId(), command.typeId(), command.endLabelId(), command.delta() );
        return false;
    }

    private IntCounter relationshipType( int type )
    {
        return counter( relationshipTypeDelta, type );
    }

    @Override
    public void apply()
    {
        // nodes
        countsUpdater.incrementNodeCount( ANY_LABEL, nodesDelta );
        for ( Map.Entry<Integer, IntCounter> label : labelDelta.entrySet() )
        {
            countsUpdater.incrementNodeCount( label.getKey(), label.getValue().value() );
        }

        // relationships
        countsUpdater.incrementRelationshipCount( ANY_LABEL, ANY_RELATIONSHIP_TYPE, ANY_LABEL, relsDelta );
        for ( Map.Entry<Integer, IntCounter> type : relationshipTypeDelta.entrySet() )
        {
            countsUpdater.incrementRelationshipCount( ANY_LABEL, type.getKey(), ANY_LABEL, type.getValue().value() );
        }
    }

    private long[] labels( NodeRecord node )
    {
        return node.inUse() ? parseLabelsField( node ).get( nodeStore ) : null;
    }
}