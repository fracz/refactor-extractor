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
package org.neo4j.kernel.impl.transaction.xaframework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.fs.StoreChannel;
import org.neo4j.kernel.impl.transaction.xaframework.log.entry.VersionAwareLogEntryReader;

public class ReaderLogVersionBridge implements LogVersionBridge
{
    private final FileSystemAbstraction fileSystem;
    private final PhysicalLogFiles logFiles;

    public ReaderLogVersionBridge( FileSystemAbstraction fileSystem, PhysicalLogFiles logFiles )
    {
        this.fileSystem = fileSystem;
        this.logFiles = logFiles;
    }

    @Override
    public VersionedStoreChannel next( VersionedStoreChannel channel ) throws IOException
    {
        PhysicalLogVersionedStoreChannel nextChannel;
        try
        {
            nextChannel = openLogChannel( new LogPosition( channel.getVersion() + 1, 0 ) );
        }
        catch ( FileNotFoundException e )
        {
            return channel;
        }
        // TODO read header properly
        channel.close();
        nextChannel.position( VersionAwareLogEntryReader.LOG_HEADER_SIZE );
        return nextChannel;
    }

    private PhysicalLogVersionedStoreChannel openLogChannel( LogPosition position ) throws IOException
    {
        long version = position.getLogVersion();
        final File fileToOpen = logFiles.getLogFileForVersion( version );
        final StoreChannel rawChannel = fileSystem.open( fileToOpen, "r" );
        final PhysicalLogVersionedStoreChannel channel = new PhysicalLogVersionedStoreChannel( rawChannel, version );
        channel.position( position.getByteOffset() );
        return channel;
    }

}