/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.kernel.impl.nioneo.store;

import java.nio.ByteBuffer;
import java.util.Random;

public final class StoreId
{
    private static final Random r = new Random( System.currentTimeMillis() );
    private final long creationTime;
    private final long randomId;

    public StoreId()
    {
        this( System.currentTimeMillis(), r.nextLong() );
    }

    public StoreId( long creationTime, long randomId )
    {
        this.creationTime = creationTime;
        this.randomId = randomId;
    }

    public long getCreationTime()
    {
        return creationTime;
    }

    public long getRandomId()
    {
        return randomId;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof StoreId )
        {
            StoreId that = (StoreId) obj;
            return that.creationTime == this.creationTime && that.randomId == this.randomId;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (int) ( creationTime ^ randomId );
    }

    public byte[] serialize()
    {
        return ByteBuffer.wrap( new byte[16] ).putLong( creationTime ).putLong( randomId ).array();
    }

    @Override
    public String toString()
    {
        return "StoreId[time:" + creationTime + ", id:" + randomId + "]";
    }

    public static StoreId deserialize( byte[] data )
    {
        assert data.length == 16 : "unexpected data";
        ByteBuffer buffer = ByteBuffer.wrap( data );
        return deserialize( buffer );
    }

    public static StoreId deserialize( ByteBuffer buffer )
    {
        long creationTime = buffer.getLong();
        long randomId = buffer.getLong();
        return new StoreId( creationTime, randomId );
    }
}