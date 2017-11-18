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
package org.neo4j.kernel.impl.nioneo.xa;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.neo4j.kernel.impl.nioneo.xa.command.LogReader;
import org.neo4j.kernel.impl.nioneo.xa.command.PhysicalLogNeoXaCommandReader;
import org.neo4j.kernel.impl.transaction.xaframework.LogEntry;
import org.neo4j.kernel.impl.transaction.xaframework.LogEntryReaderv1;
import org.neo4j.kernel.impl.util.Consumer;
import org.neo4j.kernel.impl.util.Cursor;

public class RecoveryLogDeserializer implements LogReader<FileChannel>
{
    private final ByteBuffer scratch;
    private final LogEntryReaderv1 logEntryReader;

    public RecoveryLogDeserializer( ByteBuffer scratch )
    {
        this.scratch = scratch;
        logEntryReader = new LogEntryReaderv1();
        logEntryReader.setCommandReader( new PhysicalLogNeoXaCommandReader( scratch ) );
        logEntryReader.setByteBuffer( scratch );
    }

    public void setXaCommandReader( XaCommandReader xaCommandReader )
    {
        logEntryReader.setCommandReader( xaCommandReader );
    }

    public Cursor<LogEntry, IOException> cursor( FileChannel channel )
    {
        return new RecoveryCursor( channel );
    }

    private class RecoveryCursor implements Cursor<LogEntry, IOException>
    {
        private final FileChannel channel;

        private RecoveryCursor( FileChannel channel )
        {
            this.channel = channel;
        }

        @Override
        public boolean next( Consumer<LogEntry, IOException> consumer ) throws IOException
        {
            // TODO SETUP LOG ENTRY READER
            long position = channel.position();

            scratch.clear();
            scratch.limit( 1 );
            if ( channel.read( scratch ) != scratch.limit() )
            {
                return false;
            }
            scratch.flip();

            byte type = scratch.get();

            if ( type < 0 )
            {
                scratch.clear();
                scratch.limit( 1 );
                if ( channel.read( scratch ) != scratch.limit() )
                {
                    return false;
                }
                scratch.flip();

                type = scratch.get();
            }

            LogEntry entry = logEntryReader.readLogEntry( type, channel );
            if ( entry instanceof LogEntry.Start )
            {
                ((LogEntry.Start) entry).setStartPosition( position );
            }
            else if ( entry == null )
            {
                return false;
            }

            consumer.accept( entry );

            return true;
        }

        @Override
        public void close() throws IOException
        {
            channel.close();
        }
    }
}