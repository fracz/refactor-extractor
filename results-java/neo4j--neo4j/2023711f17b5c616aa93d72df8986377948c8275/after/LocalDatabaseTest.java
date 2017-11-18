/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.coreedge.catchup.storecopy;

import org.junit.Test;

import java.io.File;

import org.neo4j.coreedge.identity.StoreId;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.NeoStoreDataSource;
import org.neo4j.kernel.impl.transaction.log.TransactionIdStore;
import org.neo4j.kernel.impl.transaction.state.DataSourceManager;
import org.neo4j.kernel.internal.DatabaseHealth;
import org.neo4j.logging.NullLogProvider;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.function.Suppliers.singleton;

public class LocalDatabaseTest
{
    @Test
    public void shouldRetrieveStoreId() throws Throwable
    {
        // given
        StoreId storeId = new StoreId( 1, 2, 3, 4 );

        // when
        LocalDatabase localDatabase = createLocalDatabase( new org.neo4j.kernel.impl.store.StoreId( 1, 2, 5, 3, 4 ) );
        localDatabase.start();

        // then
        assertEquals( storeId, localDatabase.storeId() );
    }

    private LocalDatabase createLocalDatabase( org.neo4j.kernel.impl.store.StoreId storeId )
    {
        DataSourceManager dataSourceManager = mock( DataSourceManager.class );
        NeoStoreDataSource neoStoreDataSource = mock( NeoStoreDataSource.class );
        when( dataSourceManager.getDataSource() ).thenReturn( neoStoreDataSource );
        when( neoStoreDataSource.getStoreId() ).thenReturn( storeId );
        return new LocalDatabase( new File( "directory" ),
                new StoreFiles( mock( FileSystemAbstraction.class ) ), dataSourceManager,
                singleton( mock( TransactionIdStore.class ) ), () -> mock( DatabaseHealth.class ), NullLogProvider
                .getInstance() );
    }
}