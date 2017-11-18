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
package org.neo4j.kernel.impl.nioneo.xa.command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.kernel.api.exceptions.schema.MalformedSchemaRuleException;
import org.neo4j.kernel.impl.nioneo.store.AbstractDynamicStore;
import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.LabelTokenRecord;
import org.neo4j.kernel.impl.nioneo.store.NeoStoreRecord;
import org.neo4j.kernel.impl.nioneo.store.NodeRecord;
import org.neo4j.kernel.impl.nioneo.store.PropertyBlock;
import org.neo4j.kernel.impl.nioneo.store.PropertyKeyTokenRecord;
import org.neo4j.kernel.impl.nioneo.store.PropertyRecord;
import org.neo4j.kernel.impl.nioneo.store.PropertyType;
import org.neo4j.kernel.impl.nioneo.store.Record;
import org.neo4j.kernel.impl.nioneo.store.RelationshipGroupRecord;
import org.neo4j.kernel.impl.nioneo.store.RelationshipRecord;
import org.neo4j.kernel.impl.nioneo.store.RelationshipTypeTokenRecord;
import org.neo4j.kernel.impl.nioneo.store.SchemaRule;
import org.neo4j.kernel.impl.nioneo.xa.XaCommandReader;
import org.neo4j.kernel.impl.transaction.xaframework.ReadableLogChannel;

import static org.neo4j.helpers.collection.IteratorUtil.first;

public class PhysicalLogNeoXaCommandReaderV1 implements XaCommandReader
{
    private interface DynamicRecordAdder<T>
    {
        void add( T target, DynamicRecord record );
    }

    private ReadableLogChannel channel;

    @Override
    public Command read( ReadableLogChannel channel ) throws IOException
    {
        // for the reader to pick up
        this.channel = channel;

        byte commandType = 0;
        while( commandType == 0)
        {
            commandType = channel.get();
        }

        PhysicalNeoCommandReader reader = new PhysicalNeoCommandReader();
        Command command;

        switch ( commandType )
        {
            case NeoCommandType.NODE_COMMAND:
            {
                command = new Command.NodeCommand();
                break;
            }
            case NeoCommandType.PROP_COMMAND:
            {
                command = new Command.PropertyCommand();
                break;
            }
            case NeoCommandType.PROP_INDEX_COMMAND:
            {
                command = new Command.PropertyKeyTokenCommand();
                break;
            }
            case NeoCommandType.REL_COMMAND:
            {
                command = new Command.RelationshipCommand();
                break;
            }
            case NeoCommandType.REL_TYPE_COMMAND:
            {
                command = new Command.RelationshipTypeTokenCommand();
                break;
            }
            case NeoCommandType.LABEL_KEY_COMMAND:
            {
                command = new Command.LabelTokenCommand();
                break;
            }
            case NeoCommandType.NEOSTORE_COMMAND:
            {
                command = new Command.NeoStoreCommand();
                break;
            }
            case NeoCommandType.SCHEMA_RULE_COMMAND:
            {
                command = new Command.SchemaRuleCommand();
                break;
            }
            case NeoCommandType.REL_GROUP_COMMAND:
            {
                command = new Command.RelationshipGroupCommand();
                break;
            }
            case NeoCommandType.NONE:
            {
                command = null;
                break;
            }
            default:
            {
                throw new IOException( "Unknown command type[" + commandType + "]" );
            }
        }
        if ( command != null && !command.accept( reader ) )
        {
            return null;
        }
        return command;
    }

    private class PhysicalNeoCommandReader implements NeoCommandVisitor
    {
        @Override
        public boolean visitNodeCommand( Command.NodeCommand command ) throws IOException
        {
            long id = channel.getLong();

            NodeRecord before = readNodeRecord( id );
            if ( before == null )
            {
                return false;
            }

            NodeRecord after = readNodeRecord( id );
            if ( after == null )
            {
                return false;
            }

            if ( !before.inUse() && after.inUse() )
            {
                after.setCreated();
            }

            command.init( before, after );
            return true;
        }

        @Override
        public boolean visitRelationshipCommand( Command.RelationshipCommand command ) throws IOException
        {
            long id = channel.getLong();
            byte inUseFlag = channel.get();
            boolean inUse = false;
            if ( (inUseFlag & Record.IN_USE.byteValue()) == Record.IN_USE.byteValue() )
            {
                inUse = true;
            }
            else if ( (inUseFlag & Record.IN_USE.byteValue()) != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseFlag );
            }
            RelationshipRecord record;
            if ( inUse )
            {
                record = new RelationshipRecord( id, channel.getLong(), channel
                        .getLong(), channel.getInt() );
                record.setInUse( inUse );
                record.setFirstPrevRel( channel.getLong() );
                record.setFirstNextRel( channel.getLong() );
                record.setSecondPrevRel( channel.getLong() );
                record.setSecondNextRel( channel.getLong() );
                record.setNextProp( channel.getLong() );
                byte extraByte = channel.get();
                record.setFirstInFirstChain( (extraByte&0x1) > 0 );
                record.setFirstInSecondChain( (extraByte&0x2) > 0 );
            }
            else
            {
                record = new RelationshipRecord( id, -1, -1, -1 );
                record.setInUse( false );
            }
            command.init( record );
            return true;
        }

        @Override
        public boolean visitPropertyCommand( Command.PropertyCommand command ) throws IOException
        {
            // ID
            long id = channel.getLong(); // 8

            // BEFORE
            PropertyRecord before = readPropertyRecord( id );
            if ( before == null )
            {
                return false;
            }

            // AFTER
            PropertyRecord after = readPropertyRecord( id );
            if ( after == null )
            {
                return false;
            }

            command.init( before, after );
            return true;
        }

        @Override
        public boolean visitRelationshipGroupCommand( Command.RelationshipGroupCommand command ) throws IOException
        {
            long id = channel.getLong();
            byte inUseByte = channel.get();
            boolean inUse = inUseByte == Record.IN_USE.byteValue();
            if ( inUseByte != Record.IN_USE.byteValue() && inUseByte != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseByte );
            }
            int type = channel.getShort();
            RelationshipGroupRecord record = new RelationshipGroupRecord( id, type );
            record.setInUse( inUse );
            record.setNext( channel.getLong() );
            record.setFirstOut( channel.getLong() );
            record.setFirstIn( channel.getLong() );
            record.setFirstLoop( channel.getLong() );
            record.setOwningNode( channel.getLong() );
            command.init( record );
            return true;
        }

        @Override
        public boolean visitRelationshipTypeTokenCommand( Command.RelationshipTypeTokenCommand command ) throws IOException
        {
            // id+in_use(byte)+type_blockId(int)+nr_type_records(int)
            int id = channel.getInt();
            byte inUseFlag = channel.get();
            boolean inUse = false;
            if ( (inUseFlag & Record.IN_USE.byteValue()) ==
                    Record.IN_USE.byteValue() )
            {
                inUse = true;
            }
            else if ( inUseFlag != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseFlag );
            }
            RelationshipTypeTokenRecord record = new RelationshipTypeTokenRecord( id );
            record.setInUse( inUse );
            record.setNameId( channel.getInt() );
            int nrTypeRecords = channel.getInt();
            for ( int i = 0; i < nrTypeRecords; i++ )
            {
                DynamicRecord dr = readDynamicRecord();
                if ( dr == null )
                {
                    return false;
                }
                record.addNameRecord( dr );
            }
            command.init( record );
            return true;
        }

        @Override
        public boolean visitLabelTokenCommand( Command.LabelTokenCommand command ) throws IOException
        {
            // id+in_use(byte)+type_blockId(int)+nr_type_records(int)
            int id = channel.getInt();
            byte inUseFlag = channel.get();
            boolean inUse = false;
            if ( (inUseFlag & Record.IN_USE.byteValue()) ==
                    Record.IN_USE.byteValue() )
            {
                inUse = true;
            }
            else if ( inUseFlag != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseFlag );
            }
            LabelTokenRecord record = new LabelTokenRecord( id );
            record.setInUse( inUse );
            record.setNameId( channel.getInt() );
            int nrTypeRecords = channel.getInt();
            for ( int i = 0; i < nrTypeRecords; i++ )
            {
                DynamicRecord dr = readDynamicRecord();
                if ( dr == null )
                {
                    return false;
                }
                record.addNameRecord( dr );
            }
            command.init( record );
            return true;
        }

        @Override
        public boolean visitPropertyKeyTokenCommand( Command.PropertyKeyTokenCommand command ) throws IOException
        {
            // id+in_use(byte)+count(int)+key_blockId(int)
            int id = channel.getInt();
            byte inUseFlag = channel.get();
            boolean inUse = false;
            if ( (inUseFlag & Record.IN_USE.byteValue()) == Record.IN_USE
                    .byteValue() )
            {
                inUse = true;
            }
            else if ( inUseFlag != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseFlag );
            }
            PropertyKeyTokenRecord record = new PropertyKeyTokenRecord( id );
            record.setInUse( inUse );
            record.setPropertyCount( channel.getInt() );
            record.setNameId( channel.getInt() );
            if ( readDynamicRecords( record, PROPERTY_INDEX_DYNAMIC_RECORD_ADDER ) == -1 )
            {
                return false;
            }
            command.init( record );
            return true;
        }

        @Override
        public boolean visitSchemaRuleCommand( Command.SchemaRuleCommand command ) throws IOException
        {
            Collection<DynamicRecord> recordsBefore = new ArrayList<>();
            readDynamicRecords( recordsBefore, COLLECTION_DYNAMIC_RECORD_ADDER );

            Collection<DynamicRecord> recordsAfter = new ArrayList<>();
            readDynamicRecords( recordsAfter, COLLECTION_DYNAMIC_RECORD_ADDER );

            byte isCreated = channel.get();
            if ( 1 == isCreated )
            {
                for ( DynamicRecord record : recordsAfter )
                {
                    record.setCreated();
                }
            }

            SchemaRule rule = first( recordsAfter ).inUse() ?
                    readSchemaRule( recordsAfter ) :
                    readSchemaRule( recordsBefore );

            command.init( recordsBefore, recordsAfter, rule );
            return true;
        }

        @Override
        public boolean visitNeoStoreCommand( Command.NeoStoreCommand command ) throws IOException
        {
            long nextProp = channel.getLong();
            NeoStoreRecord record = new NeoStoreRecord();
            record.setNextProp( nextProp );
            command.init( record );
            return true;
        }

        private NodeRecord readNodeRecord( long id  )
                throws IOException
        {
            byte inUseFlag = channel.get();
            boolean inUse = false;
            if ( inUseFlag == Record.IN_USE.byteValue() )
            {
                inUse = true;
            }
            else if ( inUseFlag != Record.NOT_IN_USE.byteValue() )
            {
                throw new IOException( "Illegal in use flag: " + inUseFlag );
            }
            NodeRecord record;
            if ( inUse )
            {
                boolean dense = channel.get() == 1;
                record = new NodeRecord( id, dense, channel.getLong(), channel.getLong() );

                // labels
                long labelField = channel.getLong();
                Collection<DynamicRecord> dynamicLabelRecords = new ArrayList<>();
                readDynamicRecords( dynamicLabelRecords, COLLECTION_DYNAMIC_RECORD_ADDER );
                record.setLabelField( labelField, dynamicLabelRecords );
            }
            else
            {
                record = new NodeRecord( id, false, Record.NO_NEXT_RELATIONSHIP.intValue(),
                        Record.NO_NEXT_PROPERTY.intValue() );
            }

            record.setInUse( inUse );
            return record;
        }


        DynamicRecord readDynamicRecord() throws IOException
        {
            // id+type+in_use(byte)+nr_of_bytes(int)+next_block(long)
            long id = channel.getLong();
            assert id >= 0 && id <= ( 1l << 36 ) - 1 : id
                    + " is not a valid dynamic record id";
            int type = channel.getInt();
            byte inUseFlag = channel.get();
            boolean inUse = ( inUseFlag & Record.IN_USE.byteValue() ) != 0;

            DynamicRecord record = new DynamicRecord( id );
            record.setInUse( inUse, type );
            if ( inUse )
            {
                record.setStartRecord( ( inUseFlag & Record.FIRST_IN_CHAIN.byteValue() ) != 0 );
                int nrOfBytes = channel.getInt();
                assert nrOfBytes >= 0 && nrOfBytes < ( ( 1 << 24 ) - 1 ) : nrOfBytes
                        + " is not valid for a number of bytes field of a dynamic record";
                long nextBlock = channel.getLong();
                assert ( nextBlock >= 0 && nextBlock <= ( 1l << 36 - 1 ) )
                        || ( nextBlock == Record.NO_NEXT_BLOCK.intValue() ) : nextBlock
                        + " is not valid for a next record field of a dynamic record";
                record.setNextBlock( nextBlock );
                byte data[] = new byte[nrOfBytes];
                channel.get( data, nrOfBytes );
                record.setData( data );
            }
            return record;
        }

        <T> int readDynamicRecords( T target, DynamicRecordAdder<T> adder ) throws IOException
        {
            int numberOfRecords = channel.getInt();
            assert numberOfRecords >= 0;
            while ( numberOfRecords > 0 )
            {
                DynamicRecord read = readDynamicRecord();
                if ( read == null )
                {
                    return -1;
                }
                adder.add( target, read );
                numberOfRecords--;
            }
            return numberOfRecords;
        }

        private final DynamicRecordAdder<PropertyBlock> PROPERTY_BLOCK_DYNAMIC_RECORD_ADDER =
                new DynamicRecordAdder<PropertyBlock>()
                {
                    @Override
                    public void add( PropertyBlock target, DynamicRecord record )
                    {
                        record.setCreated();
                        target.addValueRecord( record );
                    }
                };

        private final DynamicRecordAdder<Collection<DynamicRecord>> COLLECTION_DYNAMIC_RECORD_ADDER =
                new DynamicRecordAdder<Collection<DynamicRecord>>()
                {
                    @Override
                    public void add( Collection<DynamicRecord> target, DynamicRecord record )
                    {
                        target.add( record );
                    }
                };

        private PropertyRecord readPropertyRecord( long id )
                throws IOException
        {
            // in_use(byte)+type(int)+key_indexId(int)+prop_blockId(long)+
            // prev_prop_id(long)+next_prop_id(long)
            PropertyRecord record = new PropertyRecord( id );
            byte inUseFlag = channel.get(); // 1
            long nextProp = channel.getLong(); // 8
            long prevProp = channel.getLong(); // 8
            record.setNextProp( nextProp );
            record.setPrevProp( prevProp );
            boolean inUse = false;
            if ( ( inUseFlag & Record.IN_USE.byteValue() ) == Record.IN_USE.byteValue() )
            {
                inUse = true;
            }
            boolean nodeProperty = true;
            if ( ( inUseFlag & Record.REL_PROPERTY.byteValue() ) == Record.REL_PROPERTY.byteValue() )
            {
                nodeProperty = false;
            }
            long primitiveId = channel.getLong(); // 8
            if ( primitiveId != -1 && nodeProperty )
            {
                record.setNodeId( primitiveId );
            }
            else if ( primitiveId != -1 )
            {
                record.setRelId( primitiveId );
            }
            int nrPropBlocks = channel.get();
            assert nrPropBlocks >= 0;
            if ( nrPropBlocks > 0 )
            {
                record.setInUse( true );
            }
            while ( nrPropBlocks-- > 0 )
            {
                PropertyBlock block = readPropertyBlock();
                if ( block == null )
                {
                    return null;
                }
                record.addPropertyBlock( block );
            }

            int deletedRecords =  readDynamicRecords( record, PROPERTY_DELETED_DYNAMIC_RECORD_ADDER );

            if ( deletedRecords == -1 )
            {
                return null;
            }


            assert deletedRecords >= 0;
            while ( deletedRecords-- > 0 )
            {
                DynamicRecord read = readDynamicRecord();
                if ( read == null )
                {
                    return null;
                }
                record.addDeletedRecord( read );
            }

            if ( ( inUse && !record.inUse() ) || ( !inUse && record.inUse() ) )
            {
                throw new IllegalStateException( "Weird, inUse was read in as "
                        + inUse
                        + " but the record is "
                        + record );
            }
            return record;
        }

        PropertyBlock readPropertyBlock() throws IOException
        {
            PropertyBlock toReturn = new PropertyBlock();
            byte blockSize = channel.get(); // the size is stored in bytes // 1
            assert blockSize > 0 && blockSize % 8 == 0 : blockSize
                    + " is not a valid block size value";
            // Read in blocks
            long[] blocks = readLongs( blockSize / 8 );
            assert blocks.length == blockSize / 8 : blocks.length
                    + " longs were read in while i asked for what corresponds to "
                    + blockSize;
            assert PropertyType.getPropertyType( blocks[0], false ).calculateNumberOfBlocksUsed(
                    blocks[0] ) == blocks.length : blocks.length
                    + " is not a valid number of blocks for type "
                    + PropertyType.getPropertyType(
                    blocks[0], false );
        /*
         *  Ok, now we may be ready to return, if there are no DynamicRecords. So
         *  we start building the Object
         */
            toReturn.setValueBlocks( blocks );

        /*
         * Read in existence of DynamicRecords. Remember, this has already been
         * read in the buffer with the blocks, above.
         */
            if ( readDynamicRecords( toReturn, PROPERTY_BLOCK_DYNAMIC_RECORD_ADDER ) == -1 )
            {
                return null;
            }

            return toReturn;
        }

        private long[] readLongs( int count ) throws IOException
        {
            long[] result = new long[count];
            for ( int i = 0; i < count; i++ )
            {
                result[i] = channel.getLong();
            }
            return result;
        }

        private SchemaRule readSchemaRule( Collection<DynamicRecord> recordsBefore )
        {
            // TODO: Why was this assertion here?
//            assert first(recordsBefore).inUse() : "Asked to deserialize schema records that were not in use.";

            SchemaRule rule;
            ByteBuffer deserialized = AbstractDynamicStore.concatData( recordsBefore, new byte[100] );
            try
            {
                rule = SchemaRule.Kind.deserialize( first( recordsBefore ).getId(), deserialized );
            }
            catch ( MalformedSchemaRuleException e )
            {
                return null;
            }
            return rule;
        }


        private final DynamicRecordAdder<PropertyRecord> PROPERTY_DELETED_DYNAMIC_RECORD_ADDER =
                new DynamicRecordAdder<PropertyRecord>()
                {
                    @Override
                    public void add( PropertyRecord target, DynamicRecord record )
                    {
                        assert !record.inUse() : record + " is kinda weird";
                        target.addDeletedRecord( record );
                    }
                };

        private final DynamicRecordAdder<PropertyKeyTokenRecord> PROPERTY_INDEX_DYNAMIC_RECORD_ADDER =
                new DynamicRecordAdder<PropertyKeyTokenRecord>()
                {
                    @Override
                    public void add( PropertyKeyTokenRecord target, DynamicRecord record )
                    {
                        target.addNameRecord( record );
                    }
                };

    }
}