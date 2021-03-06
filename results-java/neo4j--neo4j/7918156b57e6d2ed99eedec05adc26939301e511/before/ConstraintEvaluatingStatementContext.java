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

import org.neo4j.kernel.api.ConstraintViolationKernelException;
import org.neo4j.kernel.api.StatementContext;

public class ConstraintEvaluatingStatementContext extends DelegatingStatementContext
{
    public ConstraintEvaluatingStatementContext( StatementContext delegate )
    {
        super( delegate );
    }

    @Override
    public long getOrCreateLabelId( String label ) throws ConstraintViolationKernelException
    {
        // KISS - but refactor into a general purpose constraint checker later on
        if ( label == null || label.length() == 0 )
        {
            throw new ConstraintViolationKernelException(
                    String.format( "%s is not a valid label name. Only non-empty strings are allowed.",
                            label == null ? "null" : "'" + label + "'" ) );
        }

        return delegate.getOrCreateLabelId( label );
    }

    @Override
    public void addIndexRule( long labelId, long propertyKey ) throws ConstraintViolationKernelException
    {
        for ( long existingRule : getIndexedProperties( labelId ) )
        {
            if ( existingRule == propertyKey )
            {
                throw new ConstraintViolationKernelException("Property " + propertyKey +
                        " is already indexed for label " + labelId + ".");
            }
        }
        delegate.addIndexRule( labelId, propertyKey );
    }
}