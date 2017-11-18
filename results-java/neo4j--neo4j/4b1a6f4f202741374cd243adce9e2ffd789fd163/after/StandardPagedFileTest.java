/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.io.pagecache.impl.standard;

import org.junit.Test;

import org.neo4j.io.fs.StoreChannel;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PageLock;
import org.neo4j.io.pagecache.impl.common.OffsetTrackingCursor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.neo4j.io.pagecache.impl.standard.PageTable.PinnablePage;

public class StandardPagedFileTest
{

    @Test
    public void shouldLoadPage() throws Exception
    {
        // Given
        PageCursor cursor = new OffsetTrackingCursor();
        PageTable table = mock(PageTable.class);
        PinnablePage page = mock( PinnablePage.class );
        StoreChannel channel = mock( StoreChannel.class);
        StandardPageIO io = new StandardPageIO( channel );
        when( table.load( io, 12, PageLock.SHARED ) ).thenReturn( page );
        when( page.pin( io, 12, PageLock.SHARED ) ).thenReturn( true );
        StandardPagedFile file = new StandardPagedFile(table, channel);

        // When
        file.pin( cursor, PageLock.SHARED, 12 );

        // Then
        verify(table).load( io, 12, PageLock.SHARED );
    }


}