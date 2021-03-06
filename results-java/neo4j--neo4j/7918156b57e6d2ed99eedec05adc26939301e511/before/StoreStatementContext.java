/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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

import static org.neo4j.helpers.collection.Iterables.filter;
import static org.neo4j.helpers.collection.Iterables.map;

import java.util.Collections;
import java.util.Iterator;

import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.helpers.Function;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.kernel.api.ConstraintViolationKernelException;
import org.neo4j.kernel.api.LabelNotFoundKernelException;
import org.neo4j.kernel.api.PropertyKeyIdNotFoundException;
import org.neo4j.kernel.api.PropertyKeyNotFoundException;
import org.neo4j.kernel.api.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.StatementContext;
import org.neo4j.kernel.impl.core.KeyNotFoundException;
import org.neo4j.kernel.impl.core.PropertyIndexManager;
import org.neo4j.kernel.impl.nioneo.store.IndexRule;
import org.neo4j.kernel.impl.nioneo.store.InvalidRecordException;
import org.neo4j.kernel.impl.nioneo.store.NeoStore;
import org.neo4j.kernel.impl.nioneo.store.NodeRecord;
import org.neo4j.kernel.impl.nioneo.store.NodeStore;
import org.neo4j.kernel.impl.nioneo.store.SchemaRule;
import org.neo4j.kernel.impl.nioneo.store.SchemaRule.Kind;
import org.neo4j.kernel.impl.nioneo.store.SchemaStore;
import org.neo4j.kernel.impl.nioneo.store.UnderlyingStorageException;
import org.neo4j.kernel.impl.persistence.PersistenceManager;

public class StoreStatementContext implements StatementContext
{
    private final PropertyIndexManager propertyIndexManager;
    private final PersistenceManager persistenceManager;
    private final NeoStore neoStore;

    public StoreStatementContext( PropertyIndexManager propertyIndexManager,
            PersistenceManager persistenceManager, NeoStore neoStore )
    {
        assert neoStore != null : "No neoStore provided";
        this.propertyIndexManager = propertyIndexManager;
        this.persistenceManager = persistenceManager;
        this.neoStore = neoStore;
    }

    @Override
    public long getOrCreateLabelId( String label ) throws ConstraintViolationKernelException
    {
        try
        {
            return propertyIndexManager.getOrCreateId( label );
        }
        catch ( TransactionFailureException e )
        {
            // Temporary workaround for the property store based label
            // implementation. Actual
            // implementation should not depend on internal kernel exception
            // messages like this.
            if ( e.getCause() != null && e.getCause() instanceof UnderlyingStorageException
                    && e.getCause().getMessage().equals( "Id capacity exceeded" ) )
            {
                throw new ConstraintViolationKernelException(
                        "The maximum number of labels available has been reached, cannot create more labels.", e );
            }
            else
            {
                throw e;
            }
        }
    }

    @Override
    public long getLabelId( String label ) throws LabelNotFoundKernelException
    {
        try
        {
            return propertyIndexManager.getIdByKeyName( label );
        }
        catch ( KeyNotFoundException e )
        {
            throw new LabelNotFoundKernelException( label, e );
        }
    }

    @Override
    public boolean addLabelToNode( long labelId, long nodeId )
    {
        if ( isLabelSetOnNode( labelId, nodeId ) )
            return false;

        persistenceManager.addLabelToNode( labelId, nodeId );
        return true;
    }

    @Override
    public boolean isLabelSetOnNode( long labelId, long nodeId )
    {
        try
        {
            for ( Long existingLabel : persistenceManager.getLabelsForNode( nodeId ) )
                if ( existingLabel.longValue() == labelId )
                    return true;
            return false;
        }
        catch ( InvalidRecordException e )
        {
            return false;
        }
    }

    @Override
    public Iterable<Long> getLabelsForNode( long nodeId )
    {
        try
        {
            return persistenceManager.getLabelsForNode( nodeId );
        }
        catch ( InvalidRecordException e )
        {   // TODO Might hide invalid dynamic record problem. It's here because this method
            // might get called with a nodeId that doesn't exist.
            return Collections.emptyList();
        }
    }

    @Override
    public String getLabelName( long labelId ) throws LabelNotFoundKernelException
    {
        try
        {
            return propertyIndexManager.getKeyById( (int) labelId ).getKey();
        }
        catch ( KeyNotFoundException e )
        {
            throw new LabelNotFoundKernelException( "Label by id " + labelId, e );
        }
    }

    @Override
    public boolean removeLabelFromNode( long labelId, long nodeId )
    {
        if ( !isLabelSetOnNode( labelId, nodeId ) )
            return false;

        persistenceManager.removeLabelFromNode( labelId, nodeId );
        return true;
    }

    @Override
    public Iterable<Long> getNodesWithLabel( final long labelId )
    {
        final NodeStore nodeStore = neoStore.getNodeStore();
        final long highestId = nodeStore.getHighestPossibleIdInUse();
        return new Iterable<Long>()
        {
            @Override
            public Iterator<Long> iterator()
            {
                return new PrefetchingIterator<Long>()
                {
                    private long id = 0;

                    @Override
                    protected Long fetchNextOrNull()
                    {
                        while ( id <= highestId )
                        {
                            NodeRecord node = nodeStore.forceGetRecord( id++ );
                            for ( long label : nodeStore.getLabelsForNode( node ) )
                                if ( label == labelId )
                                    return node.getId();
                        }
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public void close( boolean successful )
    {
    }

    @Override
    public void addIndexRule( long labelId, long propertyKey ) throws ConstraintViolationKernelException
    {
        SchemaStore schemaStore = neoStore.getSchemaStore();
        long id = schemaStore.nextId();
        persistenceManager.createSchemaRule(
                new IndexRule( id, labelId, IndexRule.State.POPULATING, new long[] {propertyKey} ));
    }

    @Override
    public void dropIndexRule( final long labelId, final long propertyKey ) throws ConstraintViolationKernelException
    {
        SchemaStore schemaStore = neoStore.getSchemaStore();
        Iterator<SchemaRule> filtered = filter( new Predicate<SchemaRule>()
        {
            @Override
            public boolean accept( SchemaRule rule )
            {
                return
                    rule.getLabel() == labelId
                            && rule.getKind() == Kind.INDEX_RULE
                            && matchesKey( propertyKey, ((IndexRule)rule).getPropertyKeys() );
            }

            private boolean matchesKey(long propertyKey, long[] ruleKeys) {
                return ruleKeys.length == 1 && ruleKeys[0] == propertyKey;
            }

        }, neoStore.getSchemaStore().loadAll() ).iterator();

        if (! filtered.hasNext())
            throw new ConstraintViolationKernelException("Unknown Index");

        IndexRule rule = (IndexRule) filtered.next();

        if (filtered.hasNext())
            throw new ConstraintViolationKernelException("Found more than one matching index");

        persistenceManager.dropSchemaRule( new IndexRule( rule.getId(), labelId, rule.getState(),
                new long[] {propertyKey} ) );
    }

    @Override
    public Iterable<Long> getIndexedProperties( final long labelId )
    {
        Iterable<IndexRule> indexRules = indexRulesForLabel( labelId );
        return map( new Function<IndexRule, Long>()
        {
            @Override
            public Long apply( IndexRule from )
            {
                return from.getPropertyKeys()[0];
            }
        }, indexRules );
    }

    private Iterable<IndexRule> indexRulesForLabel( final long labelId )
    {
        Iterable<SchemaRule> filtered = filter( new Predicate<SchemaRule>()
        {
            @Override
            public boolean accept( SchemaRule rule )
            {
                return rule.getLabel() == labelId && rule.getKind() == Kind.INDEX_RULE;
            }
        }, neoStore.getSchemaStore().loadAll() );

        return map( new Function<SchemaRule, IndexRule>()
        {
            @Override
            public IndexRule apply( SchemaRule from )
            {
                return (IndexRule) from;
            }
        }, filtered );
    }

    @Override
    public IndexRule.State getIndexState( long labelId, long propertyKey )
            throws LabelNotFoundKernelException, PropertyKeyNotFoundException, SchemaRuleNotFoundException
    {
        for ( IndexRule rule : indexRulesForLabel( labelId ) )
        {
            if ( rule.getPropertyKeys()[0] == propertyKey )
            {
                return rule.getState();
            }
        }

        throw new SchemaRuleNotFoundException( "No index rule for label:" + labelId + " and property key:" + propertyKey + " found." );
    }

    @Override
    public long getOrCreatePropertyKeyId( String propertyKey )
    {
        return propertyIndexManager.getOrCreateId( propertyKey );
    }

    @Override
    public long getPropertyKeyId( String propertyKey ) throws PropertyKeyNotFoundException
    {
        try
        {
            return propertyIndexManager.getIdByKeyName( propertyKey );
        }
        catch ( KeyNotFoundException e )
        {
            throw new PropertyKeyNotFoundException( propertyKey, e );
        }
    }

    @Override
    public String getPropertyKeyName( long propertyId ) throws PropertyKeyIdNotFoundException
    {
        try
        {
            return propertyIndexManager.getKeyById( (int) propertyId ).getKey();
        }
        catch ( KeyNotFoundException e )
        {
            throw new PropertyKeyIdNotFoundException( propertyId, e );
        }
    }
}