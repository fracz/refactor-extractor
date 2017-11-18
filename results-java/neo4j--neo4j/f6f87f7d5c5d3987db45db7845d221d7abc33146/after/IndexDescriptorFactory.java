/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.kernel.api.schema;

import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.SchemaWriteOperations;
import org.neo4j.kernel.api.exceptions.schema.IllegalTokenNameException;
import org.neo4j.kernel.api.exceptions.schema.TooManyLabelsException;
import org.neo4j.kernel.impl.store.record.IndexRule;

import static org.neo4j.kernel.impl.coreapi.schema.PropertyNameUtils.getOrCreatePropertyKeyIds;
import static org.neo4j.kernel.impl.coreapi.schema.PropertyNameUtils.getPropertyKeyIds;

/**
 * Factory methods for creating IndexDescriptors that are either single property or composite.
 */
public class IndexDescriptorFactory
{
    public static IndexDescriptor from( NodePropertyDescriptor descriptor )
    {
        return descriptor.isComposite() ? new CompositeIndexDescriptor( descriptor.getLabelId(),
                descriptor.getPropertyKeyIds() ) : new SinglePropertyIndexDescriptor( descriptor.getLabelId(),
                descriptor.getPropertyKeyId() );
    }

    public static IndexDescriptor from( IndexRule rule )
    {
        return from( rule.descriptor() );
    }

    public static NodePropertyDescriptor getNodePropertyDescriptor( int labelId, int[] propertyKeyIds )
    {
        return (propertyKeyIds.length > 1) ? new NodeMultiPropertyDescriptor( labelId, propertyKeyIds )
                                           : new NodePropertyDescriptor( labelId, propertyKeyIds[0] );
    }

    public static NodePropertyDescriptor getNodePropertyDescriptor( int labelId, int propertyKeyId )
    {
        return new NodePropertyDescriptor( labelId, propertyKeyId );
    }

    public static NodePropertyDescriptor getOrCreateTokens( SchemaWriteOperations schemaWriteOperations,
            IndexDefinition indexDefinition ) throws IllegalTokenNameException, TooManyLabelsException
    {
        int labelId = schemaWriteOperations.labelGetOrCreateForName( indexDefinition.getLabel().name() );
        int[] propertyKeyIds = getOrCreatePropertyKeyIds( schemaWriteOperations, indexDefinition );

        return propertyKeyIds.length > 1 ? new NodeMultiPropertyDescriptor( labelId, propertyKeyIds )
                                         : new NodePropertyDescriptor( labelId, propertyKeyIds[0] );
    }

    public static NodePropertyDescriptor getTokens( ReadOperations readOperations, IndexDefinition indexDefinition )
    {
        int labelId = readOperations.labelGetForName( indexDefinition.getLabel().name() );
        int[] propertyKeyIds = getPropertyKeyIds( readOperations, indexDefinition.getPropertyKeys() );

        return propertyKeyIds.length > 1 ? new NodeMultiPropertyDescriptor( labelId, propertyKeyIds )
                                         : new NodePropertyDescriptor( labelId, propertyKeyIds[0] );
    }
}