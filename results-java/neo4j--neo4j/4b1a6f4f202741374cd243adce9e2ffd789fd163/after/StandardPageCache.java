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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.fs.StoreChannel;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.io.pagecache.impl.common.OffsetTrackingCursor;

/**
 * Your average run-of-the-mill page cache.
 */
public class StandardPageCache implements PageCache
{
    private final FileSystemAbstraction fs;
    private final Map<File, StandardPagedFile> pagedFiles = new HashMap<>();
    private final PageTable table = null;

    public StandardPageCache( FileSystemAbstraction fs, long memoryPoolSizeInBytes )
    {
        this.fs = fs;
    }

    @Override
    public synchronized PagedFile map( File file, int pageSize, int transitionalPeriodRecordSize ) throws IOException
    {
        StandardPagedFile pagedFile = pagedFiles.get( file );
        if( pagedFile == null)
        {
            StoreChannel channel = fs.open( file, "rw" );
            pagedFile = new StandardPagedFile( table, channel );
            pagedFiles.put( file, pagedFile );
        }
        return pagedFile;
    }

    @Override
    public PageCursor newCursor()
    {
        return new OffsetTrackingCursor();
    }

    @Override
    public void close()
    {

    }
}