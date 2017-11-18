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

import org.neo4j.kernel.api.StatementContext;
import org.neo4j.kernel.api.TransactionContext;
import org.neo4j.kernel.impl.api.state.TxState;

public class StateHandlingTransactionContext extends DelegatingTransactionContext
{
    private final PersistenceCache persistenceCache;
    private final TxState state;
    private final SchemaCache schemaCache;

    private final UpdateableSchemaState schemaState;

    public StateHandlingTransactionContext( TransactionContext actual, TxState state,
                                            PersistenceCache persistenceCache,
                                            SchemaCache schemaCache,
                                            UpdateableSchemaState schemaState )
    {
        super( actual );
        this.persistenceCache = persistenceCache;
        this.schemaCache = schemaCache;
        this.schemaState = schemaState;
        this.state = state;
    }

    @Override
    public StatementContext newStatementContext()
    {
        // Store stuff
        StatementContext result = super.newStatementContext();
        // + Caching
        result = new CachingStatementContext( result, persistenceCache, schemaCache );
        // + Transaction-local state awareness
        result = new StateHandlingStatementContext( result, new SchemaStateOperations( result, schemaState ), state );
        // done
        return result;
    }

    @Override
    public void commit()
    {
        // - Ensure transaction is committed to disk at this point
        super.commit();

        // - commit changes from tx state to the cache
        // TODO: This should be done by log application, not by this level of the stack.
        persistenceCache.apply( state );
    }
}