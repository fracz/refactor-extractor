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
package org.neo4j.kernel.impl.store.record;

import org.neo4j.kernel.api.schema.NodePropertyDescriptor;
import org.neo4j.kernel.api.constraints.NodePropertyConstraint;

public abstract class NodePropertyConstraintRule extends PropertyConstraintRule
{
    protected final NodePropertyDescriptor descriptor;

    public NodePropertyConstraintRule( long id, NodePropertyDescriptor descriptor, Kind kind )
    {
        super( id, kind );
        this.descriptor = descriptor;
    }

    @Override
    public final NodePropertyDescriptor descriptor()
    {
        return descriptor;
    }

    @Override
    public final int getLabel()
    {
        return descriptor.getLabelId();
    }

    @Override
    public final int getRelationshipType()
    {
        throw new IllegalStateException( "Constraint rule is associated with nodes" );
    }

    @Override
    public boolean containsPropertyKeyId( int propertyKeyId )
    {
        return this.descriptor.getPropertyKeyId() == propertyKeyId;
    }

    public boolean matches(NodePropertyDescriptor descriptor)
    {
        return this.descriptor.equals( descriptor );
    }

    @Override
    public abstract NodePropertyConstraint toConstraint();

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        if ( !super.equals( o ) )
        {
            return false;
        }
        return descriptor.equals( ((NodePropertyConstraintRule) o).descriptor );
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + descriptor.hashCode();
    }
}