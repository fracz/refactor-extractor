/*
 * Copyright (c) 2002-2010 "Neo Technology,"
 *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.kernel.impl.transaction.xaframework;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import javax.transaction.xa.Xid;

import org.neo4j.kernel.impl.transaction.XidImpl;

class LogIoUtils
{
    static LogEntry.Start readTxStartEntry( ByteBuffer buf,
            ReadableByteChannel channel, long position ) throws IOException
    {
        buf.clear();
        buf.limit( 1 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        byte globalIdLength = buf.get();
        // get the branchId id
        buf.clear();
        buf.limit( 1 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        byte branchIdLength = buf.get();
        byte globalId[] = new byte[globalIdLength];
        ByteBuffer tmpBuffer = ByteBuffer.wrap( globalId );
        if ( channel.read( tmpBuffer ) != globalId.length )
        {
            return null;
        }
        byte branchId[] = new byte[branchIdLength];
        tmpBuffer = ByteBuffer.wrap( branchId );
        if ( channel.read( tmpBuffer ) != branchId.length )
        {
            return null;
        }
        // get the tx identifier
        buf.clear();
        buf.limit( 4 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        // get the format id
        buf.clear();
        buf.limit( 4 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int formatId = buf.getInt();
        // re-create the transaction
        Xid xid = new XidImpl( globalId, branchId, formatId );
        return new LogEntry.Start( xid, identifier, position );
    }

    static LogEntry.Prepare readTxPrepareEntry( ByteBuffer buf,
            ReadableByteChannel channel ) throws IOException
    {
        buf.clear();
        buf.limit( 4 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        return new LogEntry.Prepare( identifier );
    }

    static LogEntry.OnePhaseCommit readTxOnePhaseCommit( ByteBuffer buf,
            ReadableByteChannel channel ) throws IOException
    {
        buf.clear();
        buf.limit( 12 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        long txId = buf.getLong();
        return new LogEntry.OnePhaseCommit( identifier, txId );
    }

    static LogEntry.Done readTxDoneEntry( ByteBuffer buf,
            ReadableByteChannel channel ) throws IOException
    {
        buf.clear();
        buf.limit( 4 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        return new LogEntry.Done( identifier );
    }

    static LogEntry.TwoPhaseCommit readTxTwoPhaseCommit( ByteBuffer buf,
            ReadableByteChannel channel ) throws IOException
    {
        buf.clear();
        buf.limit( 12 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        long txId = buf.getLong();
        return new LogEntry.TwoPhaseCommit( identifier, txId );
    }

    static LogEntry.Command readTxCommand(
            ByteBuffer buf, ReadableByteChannel channel, XaCommandFactory cf )
        throws IOException
    {
        buf.clear();
        buf.limit( 4 );
        if ( channel.read( buf ) != buf.limit() )
        {
            return null;
        }
        buf.flip();
        int identifier = buf.getInt();
        XaCommand command = cf.readCommand( channel, buf );
        if ( command == null )
        {
            return null;
        }
        return new LogEntry.Command( identifier, command );
    }
}