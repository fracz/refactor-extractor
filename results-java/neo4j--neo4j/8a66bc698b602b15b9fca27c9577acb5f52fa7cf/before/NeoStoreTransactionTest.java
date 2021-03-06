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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.Pair;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.DefaultIdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.api.direct.AllEntriesLabelScanReader;
import org.neo4j.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.kernel.api.index.NodePropertyUpdate;
import org.neo4j.kernel.api.labelscan.LabelScanReader;
import org.neo4j.kernel.api.labelscan.LabelScanStore;
import org.neo4j.kernel.api.labelscan.NodeLabelUpdate;
import org.neo4j.kernel.api.properties.DefinedProperty;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.CommandApplierFacade;
import org.neo4j.kernel.impl.api.KernelSchemaStateStore;
import org.neo4j.kernel.impl.api.TransactionRepresentationCommitProcess;
import org.neo4j.kernel.impl.api.TransactionRepresentationStoreApplier;
import org.neo4j.kernel.impl.api.index.IndexUpdates;
import org.neo4j.kernel.impl.api.index.IndexingService;
import org.neo4j.kernel.impl.core.CacheAccessBackDoor;
import org.neo4j.kernel.impl.locking.Lock;
import org.neo4j.kernel.impl.locking.LockService;
import org.neo4j.kernel.impl.locking.Locks;
import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;
import org.neo4j.kernel.impl.nioneo.store.IndexRule;
import org.neo4j.kernel.impl.nioneo.store.NeoStore;
import org.neo4j.kernel.impl.nioneo.store.NodeRecord;
import org.neo4j.kernel.impl.nioneo.store.NodeStore;
import org.neo4j.kernel.impl.nioneo.store.PropertyBlock;
import org.neo4j.kernel.impl.nioneo.store.PropertyRecord;
import org.neo4j.kernel.impl.nioneo.store.Record;
import org.neo4j.kernel.impl.nioneo.store.RelationshipGroupRecord;
import org.neo4j.kernel.impl.nioneo.store.RelationshipRecord;
import org.neo4j.kernel.impl.nioneo.store.SchemaRule;
import org.neo4j.kernel.impl.nioneo.store.SchemaStore;
import org.neo4j.kernel.impl.nioneo.store.StoreFactory;
import org.neo4j.kernel.impl.nioneo.store.TransactionIdStore;
import org.neo4j.kernel.impl.nioneo.xa.command.Command;
import org.neo4j.kernel.impl.nioneo.xa.command.Command.NodeCommand;
import org.neo4j.kernel.impl.nioneo.xa.command.Command.PropertyCommand;
import org.neo4j.kernel.impl.nioneo.xa.command.Command.RelationshipGroupCommand;
import org.neo4j.kernel.impl.nioneo.xa.command.Command.SchemaRuleCommand;
import org.neo4j.kernel.impl.nioneo.xa.command.NeoCommandHandler;
import org.neo4j.kernel.impl.nioneo.xa.command.NeoTransactionIndexApplier;
import org.neo4j.kernel.impl.nioneo.xa.command.NeoTransactionStoreApplier;
import org.neo4j.kernel.impl.transaction.KernelHealth;
import org.neo4j.kernel.impl.transaction.xaframework.LogicalTransactionStore;
import org.neo4j.kernel.impl.transaction.xaframework.PhysicalTransactionRepresentation;
import org.neo4j.kernel.impl.transaction.xaframework.TransactionAppender;
import org.neo4j.kernel.impl.transaction.xaframework.TransactionRepresentation;
import org.neo4j.kernel.logging.SingleLoggingService;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.test.EphemeralFileSystemRule;
import org.neo4j.test.PageCacheRule;
import org.neo4j.unsafe.batchinsert.LabelScanWriter;

import static java.lang.Integer.parseInt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.helpers.collection.Iterables.count;
import static org.neo4j.helpers.collection.IteratorUtil.asCollection;
import static org.neo4j.helpers.collection.IteratorUtil.asSet;
import static org.neo4j.helpers.collection.IteratorUtil.emptyIterator;
import static org.neo4j.helpers.collection.IteratorUtil.first;
import static org.neo4j.helpers.collection.MapUtil.stringMap;
import static org.neo4j.kernel.IdType.NODE;
import static org.neo4j.kernel.IdType.RELATIONSHIP;
import static org.neo4j.kernel.api.index.NodePropertyUpdate.add;
import static org.neo4j.kernel.api.index.NodePropertyUpdate.change;
import static org.neo4j.kernel.api.index.NodePropertyUpdate.remove;
import static org.neo4j.kernel.api.index.SchemaIndexProvider.NO_INDEX_PROVIDER;
import static org.neo4j.kernel.impl.api.index.TestSchemaIndexProviderDescriptor.PROVIDER_DESCRIPTOR;
import static org.neo4j.kernel.impl.nioneo.store.IndexRule.indexRule;
import static org.neo4j.kernel.impl.nioneo.store.StoreFactory.configForStoreDir;
import static org.neo4j.kernel.impl.nioneo.store.TransactionIdStore.BASE_TX_ID;
import static org.neo4j.kernel.impl.nioneo.store.UniquenessConstraintRule.uniquenessConstraintRule;
import static org.neo4j.kernel.impl.util.StringLogger.DEV_NULL;

public class NeoStoreTransactionTest
{
    public static final String LONG_STRING = "string value long enough not to be stored as a short string";

    @Test
    public void shouldValidateConstraintIndexAsPartOfPrepare() throws Exception
    {
        // GIVEN
        TransactionRecordState writeTransaction = newWriteTransaction().first();

        final long indexId = neoStore.getSchemaStore().nextId();
        final long constraintId = neoStore.getSchemaStore().nextId();

        writeTransaction.createSchemaRule( uniquenessConstraintRule( constraintId, 1, 1, indexId ) );

        // WHEN
        writeTransaction.extractCommands( new ArrayList<Command>() );

        // THEN
        verify( mockIndexing ).validateIndex( indexId );
    }

    @Test
    public void shouldAddSchemaRuleToCacheWhenApplyingTransactionThatCreatesOne() throws Exception
    {
        // GIVEN
        TransactionRecordState writeTransaction = newWriteTransaction().first();

        // WHEN
        final long ruleId = neoStore.getSchemaStore().nextId();
        IndexRule schemaRule = indexRule( ruleId, 10, 8, PROVIDER_DESCRIPTOR );
        writeTransaction.createSchemaRule( schemaRule );
        commitProcess().commit( transactionRepresentationOf( writeTransaction ) );

        // THEN
        verify( cacheAccessBackDoor ).addSchemaRule( schemaRule );
    }

    private PhysicalTransactionRepresentation transactionRepresentationOf( TransactionRecordState writeTransaction )
            throws TransactionFailureException
    {
        List<Command> commands = new ArrayList<>();
        writeTransaction.extractCommands( commands );
        return new PhysicalTransactionRepresentation( commands );
    }

    @Test
    public void shouldRemoveSchemaRuleFromCacheWhenApplyingTransactionThatDeletesOne() throws Exception
    {
        // GIVEN
        SchemaStore schemaStore = neoStore.getSchemaStore();
        int labelId = 10, propertyKey = 10;
        IndexRule rule = indexRule( schemaStore.nextId(), labelId, propertyKey, PROVIDER_DESCRIPTOR );
        Collection<DynamicRecord> records = schemaStore.allocateFrom( rule );
        for ( DynamicRecord record : records )
        {
            schemaStore.updateRecord( record );
        }
        long ruleId = first( records ).getId();
        TransactionRecordState writeTransaction = newWriteTransaction().first();

        // WHEN
        writeTransaction.dropSchemaRule( rule );
        commitProcess().commit( transactionRepresentationOf( writeTransaction ) );

        // THEN
        verify( cacheAccessBackDoor ).removeSchemaRuleFromCache( ruleId );
    }

    @Test
    public void shouldMarkDynamicLabelRecordsAsNotInUseWhenLabelsAreReInlined() throws Exception
    {
        // GIVEN
        final long nodeId = neoStore.getNodeStore().nextId();

        // A transaction that creates labels that just barely fit to be inlined
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        writeTransaction.nodeCreate( nodeId );

        writeTransaction.addLabelToNode( 7, nodeId );
        writeTransaction.addLabelToNode( 11, nodeId );
        writeTransaction.addLabelToNode( 12, nodeId );
        writeTransaction.addLabelToNode( 15, nodeId );
        writeTransaction.addLabelToNode( 23, nodeId );
        writeTransaction.addLabelToNode( 27, nodeId );
        writeTransaction.addLabelToNode( 50, nodeId );

        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);


        // WHEN
        // I then remove multiple labels
        writeTransaction = newWriteTransaction().first();

        writeTransaction.removeLabelFromNode( 11, nodeId );
        writeTransaction.removeLabelFromNode( 23, nodeId );

        transactionCommands = transactionRepresentationOf( writeTransaction );

        commitProcess().commit( transactionCommands );

        // THEN
        // The dynamic label record should be part of what is logged, and it should be set to not in use anymore.
        final AtomicBoolean nodeCommandsExist = new AtomicBoolean(false);

        transactionCommands.accept( new NeoCommandHandler.HandlerVisitor(new NeoCommandHandler.Adapter()
        {
            @Override
            public boolean visitNodeCommand( NodeCommand command ) throws IOException
            {
                nodeCommandsExist.set( true );
                Collection<DynamicRecord> beforeDynLabels = command.getAfter().getDynamicLabelRecords();
                assertThat( beforeDynLabels.size(), equalTo( 1 ) );
                assertThat( beforeDynLabels.iterator().next().inUse(), equalTo( false ) );
                return true;
            }
        } ));

        assertTrue( "No node commands found", nodeCommandsExist.get() );
    }

    @Test
    public void shouldReUseOriginalDynamicRecordWhenInlinedAndThenExpandedLabelsInSameTx() throws Exception
    {
        // GIVEN
        final long nodeId = neoStore.getNodeStore().nextId();

        // A transaction that creates labels that just barely fit to be inlined
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        writeTransaction.nodeCreate( nodeId );

        writeTransaction.addLabelToNode( 16, nodeId );
        writeTransaction.addLabelToNode( 29, nodeId );
        writeTransaction.addLabelToNode( 32, nodeId );
        writeTransaction.addLabelToNode( 41, nodeId );
        writeTransaction.addLabelToNode( 44, nodeId );
        writeTransaction.addLabelToNode( 45, nodeId );
        writeTransaction.addLabelToNode( 50, nodeId );
        writeTransaction.addLabelToNode( 51, nodeId );
        writeTransaction.addLabelToNode( 52, nodeId );

        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        // I remove enough labels to inline them, but then add enough new labels to expand it back to dynamic
        writeTransaction = newWriteTransaction().first();

        writeTransaction.removeLabelFromNode( 50, nodeId );
        writeTransaction.removeLabelFromNode( 51, nodeId );
        writeTransaction.removeLabelFromNode( 52, nodeId );
        writeTransaction.addLabelToNode( 60, nodeId );
        writeTransaction.addLabelToNode( 61, nodeId );
        writeTransaction.addLabelToNode( 62, nodeId );

        transactionCommands = transactionRepresentationOf( writeTransaction );

        commitProcess().commit( transactionCommands );

        final AtomicBoolean nodeCommandsExist = new AtomicBoolean(false);

        transactionCommands.accept( new NeoCommandHandler.HandlerVisitor(new NeoCommandHandler.Adapter()
        {
            @Override
            public boolean visitNodeCommand( NodeCommand command ) throws IOException
            {
                nodeCommandsExist.set( true );
                DynamicRecord before = command.getBefore().getDynamicLabelRecords().iterator().next();
                DynamicRecord after = command.getAfter().getDynamicLabelRecords().iterator().next();

                assertThat( before.getId(), equalTo( after.getId() ) );
                assertThat( after.inUse(), equalTo( true ) );

                return true;
            }
        } ));

        assertTrue( "No node commands found", nodeCommandsExist.get() );
    }

    @Test
    public void shouldRemoveSchemaRuleWhenRollingBackTransaction() throws Exception
    {
        // GIVEN
        TransactionRecordState writeTransaction = newWriteTransaction().first();

        // WHEN
        final long ruleId = neoStore.getSchemaStore().nextId();
        writeTransaction.createSchemaRule( indexRule( ruleId, 10, 7, PROVIDER_DESCRIPTOR ) );
        transactionRepresentationOf( writeTransaction );
        // rollback simply means do not commit

        // THEN
        verifyNoMoreInteractions( cacheAccessBackDoor );
    }

    @Test
    public void shouldWriteProperBeforeAndAfterPropertyRecordsWhenAddingProperty() throws Exception
    {
        // THEN
        Visitor<Command, RuntimeException> verifier = new Visitor<Command, RuntimeException>()
        {
            @Override
            public boolean visit( Command element )
            {
                if ( element instanceof PropertyCommand )
                {
                    PropertyRecord before = ((PropertyCommand) element).getBefore();
                    assertFalse( before.inUse() );
                    assertEquals( Collections.<PropertyBlock>emptyList(), before.getPropertyBlocks() );

                    PropertyRecord after = ((PropertyCommand) element).getAfter();
                    assertTrue( after.inUse() );
                    assertEquals( 1, count( after.getPropertyBlocks() ) );
                }
                return true;
            }
        };

        // GIVEN
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int nodeId = 1;
//        writeTransaction.setCommitTxId( nodeId );
        writeTransaction.nodeCreate( nodeId );
        int propertyKey = 1;
        Object value = 5;

        // WHEN
        writeTransaction.nodeAddProperty( nodeId, propertyKey, value );
        transactionRepresentationOf( writeTransaction );
    }

    // TODO change property record
    // TODO remove property record

    @Test
    public void shouldConvertAddedPropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        CapturingIndexingService indexingService = new CapturingIndexingService();
        TransactionRecordState writeTransaction = newWriteTransaction( indexingService ).first();
        int propertyKey1 = 1, propertyKey2 = 2;
        Object value1 = "first", value2 = 4;

        // WHEN
        writeTransaction.nodeCreate( nodeId );
        writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                add( nodeId, propertyKey1, value1, none ),
                add( nodeId, propertyKey2, value2, none ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertChangedPropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        int nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2;
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        DefinedProperty property1 = writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        DefinedProperty property2 = writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        Object newValue1 = "new", newValue2 = "new 2";
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.nodeChangeProperty( nodeId, property1.propertyKeyId(), newValue1 );
        writeTransaction.nodeChangeProperty( nodeId, property2.propertyKeyId(), newValue2 );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                change( nodeId, propertyKey1, value1, none, newValue1, none ),
                change( nodeId, propertyKey2, value2, none, newValue2, none ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertRemovedPropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        int nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2;
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        DefinedProperty property1 = writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        DefinedProperty property2 = writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.nodeRemoveProperty( nodeId, property1.propertyKeyId() );
        writeTransaction.nodeRemoveProperty( nodeId, property2.propertyKeyId() );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                remove( nodeId, propertyKey1, value1, none ),
                remove( nodeId, propertyKey2, value2, none ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertLabelAdditionToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2, labelId = 3;
        long[] labelIds = new long[] {labelId};
        Object value1 = LONG_STRING, value2 = LONG_STRING.getBytes();
        writeTransaction.nodeCreate( nodeId );
        writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.addLabelToNode( labelId, nodeId );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                add( nodeId, propertyKey1, value1, labelIds ),
                add( nodeId, propertyKey2, value2, labelIds ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertMixedLabelAdditionAndSetPropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2, labelId1 = 3, labelId2 = 4;
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.addLabelToNode( labelId1, nodeId );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit( transactionCommands );

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        writeTransaction.addLabelToNode( labelId2, nodeId );
        transactionCommands = transactionRepresentationOf( writeTransaction );
	 	commitProcess( indexingService ).commit( transactionCommands );

        // THEN
        assertEquals( asSet(
                add( nodeId, propertyKey1, value1, new long[] {labelId2} ),
                add( nodeId, propertyKey2, value2, new long[]{labelId2} ),
                add( nodeId, propertyKey2, value2, new long[]{labelId1, labelId2} ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertLabelRemovalToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2, labelId = 3;
        long[] labelIds = new long[] {labelId};
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        writeTransaction.addLabelToNode( labelId, nodeId );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.removeLabelFromNode( labelId, nodeId );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                remove( nodeId, propertyKey1, value1, labelIds ),
                remove( nodeId, propertyKey2, value2, labelIds ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertMixedLabelRemovalAndRemovePropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2, labelId1 = 3, labelId2 = 4;
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        DefinedProperty property1 = writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        writeTransaction.addLabelToNode( labelId1, nodeId );
        writeTransaction.addLabelToNode( labelId2, nodeId );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.nodeRemoveProperty( nodeId, property1.propertyKeyId() );
        writeTransaction.removeLabelFromNode( labelId2, nodeId );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                remove( nodeId, propertyKey1, value1, new long[] {labelId1, labelId2} ),
                remove( nodeId, propertyKey2, value2, new long[] {labelId2} ) ),

                indexingService.updates );
    }

    @Test
    public void shouldConvertMixedLabelRemovalAndAddPropertyToNodePropertyUpdates() throws Exception
    {
        // GIVEN
        long nodeId = 0;
        TransactionRecordState writeTransaction = newWriteTransaction().first();
        int propertyKey1 = 1, propertyKey2 = 2, labelId1 = 3, labelId2 = 4;
        Object value1 = "first", value2 = 4;
        writeTransaction.nodeCreate( nodeId );
        writeTransaction.nodeAddProperty( nodeId, propertyKey1, value1 );
        writeTransaction.addLabelToNode( labelId1, nodeId );
        writeTransaction.addLabelToNode( labelId2, nodeId );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess().commit(transactionCommands);

        // WHEN
        CapturingIndexingService indexingService = new CapturingIndexingService();
        writeTransaction = newWriteTransaction( indexingService ).first();
        writeTransaction.nodeAddProperty( nodeId, propertyKey2, value2 );
        writeTransaction.removeLabelFromNode( labelId2, nodeId );
        transactionCommands = transactionRepresentationOf( writeTransaction );
		commitProcess( indexingService ).commit(transactionCommands);

        // THEN
        assertEquals( asSet(
                add( nodeId, propertyKey2, value2, new long[]{labelId1} ),
                remove( nodeId, propertyKey1, value1, new long[]{labelId2} ),
                remove( nodeId, propertyKey2, value2, new long[]{labelId2} ) ),

                indexingService.updates );
    }

    @Test
    public void shouldUpdateHighIdsOnRecoveredTransaction() throws Exception
    {
        // GIVEN
        TransactionRecordState tx = newWriteTransaction().first();
        int nodeId = 5, relId = 10, relationshipType = 3, propertyKeyId = 4, ruleId = 8;

        // WHEN
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( relationshipType, "type" );
        tx.relationshipCreate( relId, 0, nodeId, nodeId );
        tx.relAddProperty( relId, propertyKeyId,
                new long[] {1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60, 1l << 60} );
        tx.createPropertyKeyToken( "key", propertyKeyId );
        tx.nodeAddProperty( nodeId, propertyKeyId,
                "something long and nasty that requires dynamic records for sure I would think and hope. Ok then åäö%!=" );
        for ( int i = 0; i < 10; i++ )
        {
            tx.addLabelToNode( 10000 + i, nodeId );
        }
        tx.createSchemaRule( indexRule( ruleId, 100, propertyKeyId, PROVIDER_DESCRIPTOR ) );

        PhysicalTransactionRepresentation toCommit = transactionRepresentationOf( tx );
        RecoveryCreatingCopyingNeoCommandHandler recoverer = new RecoveryCreatingCopyingNeoCommandHandler();
        toCommit.accept( recoverer );

        commitRecovered( recoverer.getAsRecovered(), 3 );

        // THEN
        assertEquals( "NodeStore", nodeId+1, neoStore.getNodeStore().getHighId() );
        assertEquals( "DynamicNodeLabelStore", 2, neoStore.getNodeStore().getDynamicLabelStore().getHighId() );
        assertEquals( "RelationshipStore", relId+1, neoStore.getRelationshipStore().getHighId() );
        assertEquals( "RelationshipTypeStore", relationshipType+1, neoStore.getRelationshipTypeTokenStore().getHighId() );
        assertEquals( "RelationshipType NameStore", 2, neoStore.getRelationshipTypeTokenStore().getNameStore().getHighId() );
        assertEquals( "PropertyStore", 2, neoStore.getPropertyStore().getHighId() );
        assertEquals( "PropertyStore DynamicStringStore", 2, neoStore.getPropertyStore().getStringStore().getHighId() );
        assertEquals( "PropertyStore DynamicArrayStore", 2, neoStore.getPropertyStore().getArrayStore().getHighId() );
        assertEquals( "PropertyIndexStore", propertyKeyId+1, neoStore.getPropertyKeyTokenStore().getHighId() );
        assertEquals( "PropertyKeyToken NameStore", 2, neoStore.getPropertyStore().getPropertyKeyTokenStore().getNameStore().getHighId() );
        assertEquals( "SchemaStore", ruleId+1, neoStore.getSchemaStore().getHighId() );
    }

    @Test
    public void createdSchemaRuleRecordMustBeWrittenHeavy() throws Exception
    {
        // GIVEN
        TransactionRecordState tx = newWriteTransaction().first();
        long ruleId = 0;
        int labelId = 5, propertyKeyId = 7;
        SchemaRule rule = indexRule( ruleId, labelId, propertyKeyId, PROVIDER_DESCRIPTOR );

        // WHEN
        tx.createSchemaRule( rule );
        PhysicalTransactionRepresentation transactionCommands = transactionRepresentationOf( tx );

        transactionCommands.accept( new NeoCommandHandler.HandlerVisitor(new NeoCommandHandler.Adapter()
        {
            @Override
            public boolean visitSchemaRuleCommand( SchemaRuleCommand command ) throws IOException
            {
                for ( DynamicRecord record : command.getRecordsAfter() )
                {
                    assertFalse( record + " should have been heavy", record.isLight() );
                }
                return true;
            }
        } ));
    }

    @Test
    public void shouldWriteProperPropertyRecordsWhenOnlyChangingLinkage() throws Exception
    {
        /* There was an issue where GIVEN:
         *
         *   Legend: () = node, [] = property record
         *
         *   ()-->[0:block{size:1}]
         *
         * WHEN adding a new property record in front of if, not changing any data in that record i.e:
         *
         *   ()-->[1:block{size:4}]-->[0:block{size:1}]
         *
         * The state of property record 0 would be that it had loaded value records for that block,
         * but those value records weren't heavy, so writing that record to the log would fail
         * w/ an assertion data != null.
         */

        // GIVEN
        TransactionRecordState tx = newWriteTransaction().first();
        int nodeId = 0;
        tx.nodeCreate( nodeId );
        int index = 0;
        tx.nodeAddProperty( nodeId, index, string( 70 ) ); // will require a block of size 1
        commitProcess().commit( transactionRepresentationOf( tx ) );

        // WHEN
        Visitor<Command, IOException> verifier = new NeoCommandHandler.HandlerVisitor(new NeoCommandHandler.Adapter()
        {
            @Override
            public boolean visitPropertyCommand( PropertyCommand command ) throws IOException
            {
                // THEN
                verifyPropertyRecord( command.getBefore() );
                verifyPropertyRecord( command.getAfter() );
                return true;
            }

            private void verifyPropertyRecord( PropertyRecord record )
            {
                if ( record.getPrevProp() != Record.NO_NEXT_PROPERTY.intValue() )
                {
                    for ( PropertyBlock block : record.getPropertyBlocks() )
                    {
                        assertTrue( block.isLight() );
                    }
                }
            }
        });
        tx = newWriteTransaction( mockIndexing).first();
        int index2 = 1;
        tx.nodeAddProperty( nodeId, index2, string( 40 ) ); // will require a block of size 4
        PhysicalTransactionRepresentation representation = transactionRepresentationOf( tx );
        representation.accept( verifier );
        commitProcess().commit( representation );
    }

    @Test
    public void shouldCreateEqualNodePropertyUpdatesOnRecoveryOfCreatedNode() throws Exception
    {
        /* There was an issue where recovering a tx where a node with a label and a property
         * was created resulted in two exact copies of NodePropertyUpdates. */

        // GIVEN
        long nodeId = 0;
        int labelId = 5, propertyKeyId = 7;
        NodePropertyUpdate expectedUpdate = NodePropertyUpdate.add( nodeId, propertyKeyId, "Neo", new long[] {labelId} );

        // -- an index
        long ruleId = 0;
        TransactionRecordState tx = newWriteTransaction().first();
        SchemaRule rule = indexRule( ruleId, labelId, propertyKeyId, PROVIDER_DESCRIPTOR );
        tx.createSchemaRule( rule );
        commitProcess().commit(transactionRepresentationOf( tx ));

        // -- and a tx creating a node with that label and property key
        IteratorCollector<NodePropertyUpdate> indexUpdates = new IteratorCollector<>( 0 );
        doAnswer( indexUpdates ).when( mockIndexing ).updateIndexes( any( IndexUpdates.class ) );
        tx = newWriteTransaction().first();
        tx.nodeCreate( nodeId );
        tx.addLabelToNode( labelId, nodeId );
        tx.nodeAddProperty( nodeId, propertyKeyId, "Neo" );
        PhysicalTransactionRepresentation representation = transactionRepresentationOf( tx );
        RecoveryCreatingCopyingNeoCommandHandler recoverer = new RecoveryCreatingCopyingNeoCommandHandler();
        representation.accept( recoverer );
        commitProcess().commit( representation );
        verify( mockIndexing, times( 1 ) ).updateIndexes( any( IndexUpdates.class ) );
        indexUpdates.assertContent( expectedUpdate );

        reset( mockIndexing );
        indexUpdates = new IteratorCollector<>( 0 );
        doAnswer( indexUpdates ).when( mockIndexing ).updateIndexes( any( IndexUpdates.class ) );

        // WHEN
        // -- later recovering that tx, there should be only one update
        commitRecovered( recoverer.getAsRecovered(), 2 );
        verify( mockIndexing, times( 1 ) ).updateIndexes( any( IndexUpdates.class ) );
        indexUpdates.assertContent( expectedUpdate );
    }

    @Test
    public void shouldLockUpdatedNodes() throws Exception
    {
        // given
        NodeStore nodeStore = neoStore.getNodeStore();
        long[] nodes = { // allocate ids
                nodeStore.nextId(),
                nodeStore.nextId(),
                nodeStore.nextId(),
                nodeStore.nextId(),
                nodeStore.nextId(),
                nodeStore.nextId(),
                nodeStore.nextId(),
        };
        // create the node records that we will modify in our main tx.
        {
            TransactionRecordState tx = newWriteTransaction().first();
            for ( int i = 1; i < nodes.length - 1; i++ )
            {
                tx.nodeCreate( nodes[i] );
            }
            tx.nodeAddProperty( nodes[3], 0, "old" );
            tx.nodeAddProperty( nodes[4], 0, "old" );
            commitProcess().commit(transactionRepresentationOf( tx ));
            reset( locks ); // reset the lock counts
        }

        // These are the changes we want to assert locking on
        TransactionRecordState tx = newWriteTransaction().first();
        tx.nodeCreate( nodes[0] );
        tx.addLabelToNode( 0, nodes[1] );
        tx.nodeAddProperty( nodes[2], 0, "value" );
        tx.nodeChangeProperty( nodes[3], 0, "value" );
        tx.nodeRemoveProperty( nodes[4], 0 );
        tx.nodeDelete( nodes[5] );

        tx.nodeCreate( nodes[6] );
        tx.addLabelToNode( 0, nodes[6] );
        tx.nodeAddProperty( nodes[6], 0, "value" );

        // when
        commitProcess().commit(transactionRepresentationOf( tx ));

        // then
        // create node, NodeCommand == 1 update
        verify( locks, times( 1 ) ).acquireNodeLock( nodes[0], LockService.LockType.WRITE_LOCK );
        // add label, NodeCommand == 1 update
        verify( locks, times( 1 ) ).acquireNodeLock( nodes[1], LockService.LockType.WRITE_LOCK );
        // add property, NodeCommand and PropertyCommand == 2 updates
        verify( locks, times( 2 ) ).acquireNodeLock( nodes[2], LockService.LockType.WRITE_LOCK );
        // update property, in place, PropertyCommand == 1 update
        verify( locks, times( 1 ) ).acquireNodeLock( nodes[3], LockService.LockType.WRITE_LOCK );
        // remove property, updates the Node and the Property == 2 updates
        verify( locks, times( 2 ) ).acquireNodeLock( nodes[4], LockService.LockType.WRITE_LOCK );
        // delete node, single NodeCommand == 1 update
        verify( locks, times( 1 ) ).acquireNodeLock( nodes[5], LockService.LockType.WRITE_LOCK );
        // create and add-label goes into the NodeCommand, add property is a PropertyCommand == 2 updates
        verify( locks, times( 2 ) ).acquireNodeLock( nodes[6], LockService.LockType.WRITE_LOCK );
    }

    @Test
    public void shouldConvertToDenseNodeRepresentationWhenHittingThresholdWithDifferentTypes() throws Exception
    {
        // GIVEN a node with a total of denseNodeThreshold-1 relationships
        instantiateNeoStore( 50 );
        Pair<TransactionRecordState, NeoStoreTransactionContext> transactionContextPair =
                newWriteTransaction();
        TransactionRecordState tx = transactionContextPair.first();
        NeoStoreTransactionContext txCtx = transactionContextPair.other();
        long nodeId = nextId( NODE );
        int typeA = 0, typeB = 1, typeC = 2;
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( typeA, "A" );
        createRelationships( tx, nodeId, typeA, OUTGOING, 6 );
        createRelationships( tx, nodeId, typeA, INCOMING, 7 );

        tx.createRelationshipTypeToken( typeB, "B" );
        createRelationships( tx, nodeId, typeB, OUTGOING, 8 );
        createRelationships( tx, nodeId, typeB, INCOMING, 9 );

        tx.createRelationshipTypeToken( typeC, "C" );
        createRelationships( tx, nodeId, typeC, OUTGOING, 10 );
        createRelationships( tx, nodeId, typeC, INCOMING, 10 );
        // here we're at the edge
        assertFalse( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );

        // WHEN creating the relationship that pushes us over the threshold
        createRelationships( tx, nodeId, typeC, INCOMING, 1 );

        // THEN the node should have been converted into a dense node
        assertTrue( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeA, 6, 7 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeB, 8, 9 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 10, 11 );
    }

    @Test
    public void shouldConvertToDenseNodeRepresentationWhenHittingThresholdWithTheSameTypeDifferentDirection()
            throws Exception
    {
        // GIVEN a node with a total of denseNodeThreshold-1 relationships
        instantiateNeoStore( 49 );
        Pair<TransactionRecordState, NeoStoreTransactionContext> transactionContextPair =
                newWriteTransaction();
        TransactionRecordState tx = transactionContextPair.first();
        NeoStoreTransactionContext txCtx = transactionContextPair.other();
        long nodeId = nextId( NODE );
        int typeA = 0;
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( typeA, "A" );
        createRelationships( tx, nodeId, typeA, OUTGOING, 24 );
        createRelationships( tx, nodeId, typeA, INCOMING, 25 );

        // here we're at the edge
        assertFalse( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );

        // WHEN creating the relationship that pushes us over the threshold
        createRelationships( tx, nodeId, typeA, INCOMING, 1 );

        // THEN the node should have been converted into a dense node
        assertTrue( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeA, 24, 26 );
    }

    @Test
    public void shouldConvertToDenseNodeRepresentationWhenHittingThresholdWithTheSameTypeSameDirection()
            throws Exception
    {
        // GIVEN a node with a total of denseNodeThreshold-1 relationships
        instantiateNeoStore( 8 );
        Pair<TransactionRecordState, NeoStoreTransactionContext> transactionContextPair =
                newWriteTransaction();
        TransactionRecordState tx = transactionContextPair.first();
        NeoStoreTransactionContext txCtx = transactionContextPair.other();
        long nodeId = nextId( NODE );
        int typeA = 0;
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( typeA, "A" );
        createRelationships( tx, nodeId, typeA, OUTGOING, 8 );

        // here we're at the edge
        assertFalse( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );

        // WHEN creating the relationship that pushes us over the threshold
        createRelationships( tx, nodeId, typeA, OUTGOING, 1 );

        // THEN the node should have been converted into a dense node
        assertTrue( txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData().isDense() );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeA, 9, 0 );
    }

    @Test
    public void shouldMaintainCorrectDataWhenDeletingFromDenseNodeWithOneType() throws Exception
    {
        // GIVEN a node with a total of denseNodeThreshold-1 relationships
        instantiateNeoStore( 13 );
        Pair<TransactionRecordState, NeoStoreTransactionContext> transactionContextPair =
                newWriteTransaction();
        TransactionRecordState tx = transactionContextPair.first();
        NeoStoreTransactionContext txCtx = transactionContextPair.other();
        int nodeId = (int) nextId( NODE ), typeA = 0;
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( typeA, "A" );
        long[] relationshipsCreated = createRelationships( tx, nodeId, typeA, INCOMING, 15 );

        //WHEN
        deleteRelationship( tx, relationshipsCreated[0] );

        // THEN the node should have been converted into a dense node
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeA, 0, 14 );
    }

    @Test
    public void shouldMaintainCorrectDataWhenDeletingFromDenseNodeWithManyTypes() throws Exception
    {
        // GIVEN a node with a total of denseNodeThreshold-1 relationships
        instantiateNeoStore( 1 );
        Pair<TransactionRecordState, NeoStoreTransactionContext> transactionAndContextPair =
                newWriteTransaction();
        TransactionRecordState tx = transactionAndContextPair.first();
        NeoStoreTransactionContext txCtx = transactionAndContextPair.other();
        long nodeId = nextId( NODE );
        int typeA = 0, typeB = 12, typeC = 600;
        tx.nodeCreate( nodeId );
        tx.createRelationshipTypeToken( typeA, "A" );
        long[] relationshipsCreatedAIncoming = createRelationships( tx, nodeId, typeA, INCOMING, 1 );
        long[] relationshipsCreatedAOutgoing = createRelationships( tx, nodeId, typeA, OUTGOING, 1 );

        tx.createRelationshipTypeToken( typeB, "B" );
        long[] relationshipsCreatedBIncoming = createRelationships( tx, nodeId, typeB, INCOMING, 1 );
        long[] relationshipsCreatedBOutgoing = createRelationships( tx, nodeId, typeB, OUTGOING, 1 );

        tx.createRelationshipTypeToken( typeC, "C" );
        long[] relationshipsCreatedCIncoming = createRelationships( tx, nodeId, typeC, INCOMING, 1 );
        long[] relationshipsCreatedCOutgoing = createRelationships( tx, nodeId, typeC, OUTGOING, 1 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedAIncoming[0] );

        // THEN
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeA, 1, 0 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeB, 1, 1 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 1, 1 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedAOutgoing[0] );

        // THEN
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeA );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeB, 1, 1 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 1, 1 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedBIncoming[0] );

        // THEN
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeA );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeB, 1, 0 );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 1, 1 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedBOutgoing[0] );

        // THEN
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeA );
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeB );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 1, 1 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedCIncoming[0] );

        // THEN
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeA );
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeB );
        assertDenseRelationshipCounts( tx, txCtx, nodeId, typeC, 1, 0 );

        // WHEN
        deleteRelationship( tx, relationshipsCreatedCOutgoing[0] );

        // THEN
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeA );
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeB );
        assertRelationshipGroupDoesNotExist( txCtx, txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), typeC );
    }

    @Test
    public void movingBilaterallyOfTheDenseNodeThresholdIsConsistent() throws Exception
    {
        // GIVEN
        instantiateNeoStore( 10 );
        final long nodeId = neoStore.getNodeStore().nextId();

        TransactionRecordState writeTransaction = newWriteTransaction().first();
        writeTransaction.nodeCreate( nodeId );

        int typeA = (int) neoStore.getRelationshipTypeTokenStore().nextId();
        writeTransaction.createRelationshipTypeToken( typeA, "A" );
        createRelationships( writeTransaction, nodeId, typeA, INCOMING, 20 );

        commitProcess().commit(transactionRepresentationOf( writeTransaction ));
        writeTransaction = newWriteTransaction().first();

        int typeB = 1;
        writeTransaction.createRelationshipTypeToken( typeB, "B" );


        // WHEN
        // i remove enough relationships to become dense and remove enough to become not dense
        long[] relationshipsOfTypeB = createRelationships( writeTransaction, nodeId, typeB, OUTGOING, 5 );
        for ( long relationshipToDelete : relationshipsOfTypeB )
        {
            deleteRelationship( writeTransaction, relationshipToDelete );
        }

        PhysicalTransactionRepresentation tx = transactionRepresentationOf( writeTransaction );
        commitProcess().commit(tx);

        // THEN
        // The dynamic label record in before should be the same id as in after, and should be in use
        final AtomicBoolean foundRelationshipGroupInUse = new AtomicBoolean();

        tx.accept( new NeoCommandHandler.HandlerVisitor(new NeoCommandHandler.Adapter()
        {
            @Override
            public boolean visitRelationshipGroupCommand(
                    RelationshipGroupCommand command ) throws IOException
            {
                if ( command.getRecord().inUse() )
                {
                    if ( !foundRelationshipGroupInUse.get() )
                    {
                        foundRelationshipGroupInUse.set( true );
                    }
                    else
                    {
                        fail();
                    }
                }
                return true;
            }
        } ));

        assertTrue( "Did not create relationship group command", foundRelationshipGroupInUse.get() );
    }

    @Test
    public void shouldSortRelationshipGroups() throws Exception
    {
        // GIVEN a node with group of type 10
        instantiateNeoStore( 1 );
        int type5 = 5, type10 = 10, type15 = 15;
        {
            TransactionRecordState tx = newWriteTransaction().first();
            neoStore.getRelationshipTypeTokenStore().setHighId( 16 );
            tx.createRelationshipTypeToken( type5, "5" );
            tx.createRelationshipTypeToken( type10, "10" );
            tx.createRelationshipTypeToken( type15, "15" );
            commitProcess().commit(transactionRepresentationOf( tx ));
        }
        long nodeId = neoStore.getNodeStore().nextId();
        {
            TransactionRecordState tx = newWriteTransaction().first();
            long otherNode1Id = neoStore.getNodeStore().nextId();
            long otherNode2Id = neoStore.getNodeStore().nextId();
            tx.nodeCreate( nodeId );
            tx.nodeCreate( otherNode1Id );
            tx.nodeCreate( otherNode2Id );
            tx.relationshipCreate( neoStore.getRelationshipStore().nextId(), type10, nodeId, otherNode1Id );
            // This relationship will cause the switch to dense
            tx.relationshipCreate( neoStore.getRelationshipStore().nextId(), type10, nodeId, otherNode2Id );
            commitProcess().commit(transactionRepresentationOf( tx ));
            // Just a little validation of assumptions
            assertRelationshipGroupsInOrder( nodeId, type10 );
        }

        // WHEN inserting a relationship of type 5
        {
            TransactionRecordState tx = newWriteTransaction().first();
            long otherNodeId = neoStore.getNodeStore().nextId();
            tx.nodeCreate( otherNodeId );
            tx.relationshipCreate( neoStore.getRelationshipStore().nextId(), type5, nodeId, otherNodeId );
            commitProcess().commit(transactionRepresentationOf( tx ));
        }

        // THEN that group should end up first in the chain
        assertRelationshipGroupsInOrder( nodeId, type5, type10 );

        // WHEN inserting a relationship of type 15
        {
            TransactionRecordState tx = newWriteTransaction().first();
            long otherNodeId = neoStore.getNodeStore().nextId();
            tx.nodeCreate( otherNodeId );
            tx.relationshipCreate( neoStore.getRelationshipStore().nextId(), type15, nodeId, otherNodeId );
            commitProcess().commit(transactionRepresentationOf( tx ));
        }

        // THEN that group should end up last in the chain
        assertRelationshipGroupsInOrder( nodeId, type5, type10, type15 );
    }

    private void assertRelationshipGroupsInOrder( long nodeId, int... types )
    {
        NodeRecord node = neoStore.getNodeStore().getRecord( nodeId );
        assertTrue( "Node should be dense, is " + node, node.isDense() );
        long groupId = node.getNextRel();
        int cursor = 0;
        List<RelationshipGroupRecord> seen = new ArrayList<>();
        while ( groupId != Record.NO_NEXT_RELATIONSHIP.intValue() )
        {
            RelationshipGroupRecord group = neoStore.getRelationshipGroupStore().getRecord( groupId );
            seen.add( group );
            assertEquals( "Invalid type, seen groups so far " + seen, types[cursor++], group.getType() );
            groupId = group.getNext();
        }
        assertEquals( "Not enough relationship group records found in chain for " + node, types.length, cursor );
    }

    private static void assertRelationshipGroupDoesNotExist( NeoStoreTransactionContext txCtx, NodeRecord node, int type )
    {
        assertNull( txCtx.getRelationshipGroup( node, type ) );
    }

    private static void assertDenseRelationshipCounts( TransactionRecordState tx, NeoStoreTransactionContext txCtx,
                                                       long nodeId, int type, int outCount, int inCount )
    {
        RelationshipGroupRecord group = txCtx.getRelationshipGroup(
                txCtx.getNodeRecords().getOrLoad( nodeId, null ).forReadingData(), type ).forReadingData();
        assertNotNull( group );

        RelationshipRecord rel;
        long relId = group.getFirstOut();
        if ( relId != Record.NO_NEXT_RELATIONSHIP.intValue() )
        {
            rel = txCtx.getRelRecords().getOrLoad( relId, null ).forReadingData();
            // count is stored in the back pointer of the first relationship in the chain
            assertEquals( "Stored relationship count for OUTGOING differs", outCount, rel.getFirstPrevRel() );
            assertEquals( "Manually counted relationships for OUTGOING differs", outCount,
                    manuallyCountRelationships( txCtx, nodeId, relId ) );
        }

        relId = group.getFirstIn();
        if ( relId != Record.NO_NEXT_RELATIONSHIP.intValue() )
        {
            rel = txCtx.getRelRecords().getOrLoad( relId, null ).forReadingData();
            assertEquals( "Stored relationship count for INCOMING differs", inCount, rel.getSecondPrevRel() );
            assertEquals( "Manually counted relationships for INCOMING differs", inCount,
                    manuallyCountRelationships( txCtx, nodeId, relId ) );
        }
    }

    private static int manuallyCountRelationships( NeoStoreTransactionContext txCtx, long nodeId,
            long firstRelId )
    {
        int count = 0;
        long relId = firstRelId;
        while ( relId != Record.NO_NEXT_RELATIONSHIP.intValue() )
        {
            count++;
            RelationshipRecord record = txCtx.getRelRecords().getOrLoad( relId, null ).forReadingData();
            relId = record.getFirstNode() == nodeId ? record.getFirstNextRel() : record.getSecondNextRel();
        }
        return count;
    }

    private long nextId( IdType type )
    {
        return idGeneratorFactory.get( type ).nextId();
    }

    private long[] createRelationships( TransactionRecordState tx, long nodeId, int type, Direction direction, int count )
    {
        long[] result = new long[ count ];
        for ( int i = 0; i < count; i++ )
        {
            long otherNodeId = nextId( NODE );
            tx.nodeCreate( otherNodeId );
            long first = direction == OUTGOING ? nodeId : otherNodeId;
            long other = direction == INCOMING ? nodeId : otherNodeId;
            long relId = nextId( RELATIONSHIP );
            result[i] = relId;
            tx.relationshipCreate( relId, type, first, other );
        }
        return result;
    }

    private void deleteRelationship( TransactionRecordState tx, long relId )
    {
        tx.relDelete( relId );
    }

    private String string( int length )
    {
        StringBuilder result = new StringBuilder();
        char ch = 'a';
        for ( int i = 0; i < length; i++ )
        {
            result.append( (char)((ch + (i%10))) );
        }
        return result.toString();
    }

    @ClassRule
    public static PageCacheRule pageCacheRule = new PageCacheRule();
    @Rule public EphemeralFileSystemRule fs = new EphemeralFileSystemRule();
    private Config config;
    @SuppressWarnings("deprecation")
    private final DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
    private NeoStore neoStore;
    private LockService locks;
    private CacheAccessBackDoor cacheAccessBackDoor;
    private final List<Lock> lockMocks = new ArrayList<>();

    private IndexingService mockIndexing;
    private PropertyLoader propertyLoader;

    @Before
    public void before() throws Exception
    {
        instantiateNeoStore( parseInt( GraphDatabaseSettings.dense_node_threshold.getDefaultValue() ) );
    }

    private void instantiateNeoStore( int denseNodeThreshold ) throws Exception
    {
        if ( neoStore != null )
        {
            fs.clear();
        }

        config = new Config( stringMap(
                GraphDatabaseSettings.dense_node_threshold.name(), "" + denseNodeThreshold ) );

        File storeDir = new File( "dir" );
        config = configForStoreDir( config, storeDir );
        PageCache pageCache = pageCacheRule.getPageCache( fs.get(), config );
        StoreFactory storeFactory = new StoreFactory(
                config,
                idGeneratorFactory,
                pageCache,
                fs.get(),
                DEV_NULL,
                new Monitors() );
        neoStore = storeFactory.createNeoStore();
        lockMocks.clear();
        locks = mock( LockService.class, new Answer()
        {
            @Override
            public synchronized Object answer( final InvocationOnMock invocation ) throws Throwable
            {
                // This is necessary because finalize() will also be called
                if ( invocation.getMethod().getName().equals( "acquireNodeLock" ) )
                {
                    final Lock mock = mock( Lock.class, new Answer()
                    {
                        @Override
                        public Object answer( InvocationOnMock invocationOnMock ) throws Throwable
                        {
                            return null;
                        }
                    } );
                    lockMocks.add( mock );
                    return mock;
                }
                else
                {
                    return null;
                }
            }
        } );

        cacheAccessBackDoor = mock( CacheAccessBackDoor.class );
        mockIndexing = mock( IndexingService.class );
	}

    private TransactionRepresentationCommitProcess commitProcess() throws InterruptedException, ExecutionException, IOException
    {
    	return commitProcess( mockIndexing );
    }

    private long nextTxId = BASE_TX_ID + 1;

    private TransactionRepresentationCommitProcess commitProcess( IndexingService indexing ) throws InterruptedException, ExecutionException, IOException
    {
    	TransactionAppender appenderMock = mock( TransactionAppender.class );
        Future<Long> futureMock = mock( Future.class );
        when( futureMock.get() ).thenReturn( nextTxId++ );
        when( appenderMock.append( Matchers.<TransactionRepresentation>any()) ).thenReturn( futureMock );
        LogicalTransactionStore txStoreMock = mock ( LogicalTransactionStore.class );
        when( txStoreMock.getAppender() ).thenReturn(appenderMock);
        LabelScanStore labelScanStore = mock( LabelScanStore.class );
        when (labelScanStore.newWriter()).thenReturn( mock(LabelScanWriter.class) );
        TransactionRepresentationStoreApplier applier = new TransactionRepresentationStoreApplier(
                indexing, labelScanStore, neoStore, cacheAccessBackDoor, locks, null, null );

        // Call this just to make sure the counters have been initialized.
        // This is only a problem in a mocked environment like this.
        neoStore.nextCommittingTransactionId();

        return new TransactionRepresentationCommitProcess( txStoreMock, mock( KernelHealth.class ),
                neoStore, applier, false );
    }

    @After
    public void shouldReleaseAllLocks()
    {
        for ( Lock lock : lockMocks )
        {
            verify( lock ).release();
        }
    }

    private Pair<TransactionRecordState, NeoStoreTransactionContext> newWriteTransaction()
    {
        return newWriteTransaction( mockIndexing );
    }

    private Pair<TransactionRecordState, NeoStoreTransactionContext> newWriteTransaction( IndexingService indexing )
    {
        NeoStoreTransactionContext context =
                new NeoStoreTransactionContext( mock( NeoStoreTransactionContextSupplier.class ), neoStore );
        context.bind( mock( Locks.Client.class ) );
        TransactionRecordState result = new TransactionRecordState( 0l, neoStore,
                new IntegrityValidator( neoStore, indexing ), context );

        return Pair.of( result, context );
    }

    private class CapturingIndexingService extends IndexingService
    {
        private final Set<NodePropertyUpdate> updates = new HashSet<>();

        public CapturingIndexingService()
        {
            super( null,
                    new DefaultSchemaIndexProviderMap( NO_INDEX_PROVIDER ),
                    new NeoStoreIndexStoreView( locks, neoStore ),
                    null,
                    new KernelSchemaStateStore(), Collections.<IndexRule>emptyList(),
                    new SingleLoggingService( DEV_NULL ), IndexingService.NO_MONITOR
                );
        }

        @Override
        public void updateIndexes( IndexUpdates updates )
        {
            this.updates.addAll( asCollection( updates ) );
        }
    }

    private static final long[] none = new long[0];

    private void commitRecovered( TransactionRepresentation recoveredTx, long txId ) throws Exception
    {

        NeoTransactionStoreApplier storeApplier = new NeoTransactionStoreApplier(
                neoStore, mockIndexing, cacheAccessBackDoor, locks, txId, true );

        LabelScanStore labelScanStore = mock( LabelScanStore.class );
        when (labelScanStore.newWriter()).thenReturn( mock(LabelScanWriter.class) );
        NeoTransactionIndexApplier indexApplier = new NeoTransactionIndexApplier( mockIndexing,
                labelScanStore, neoStore.getNodeStore(), neoStore.getPropertyStore(),
                cacheAccessBackDoor, propertyLoader );
        try ( CommandApplierFacade applier = new CommandApplierFacade( storeApplier, indexApplier,
                mock( NeoCommandHandler.class ) ) )
        {
            recoveredTx.accept( applier );
        }
    }

    public static final LabelScanStore NO_LABEL_SCAN_STORE = new LabelScanStore()
    {
        @Override
        public LabelScanReader newReader()
        {
            return LabelScanReader.EMPTY;
        }

        @Override
        public LabelScanWriter newWriter()
        {
            return LabelScanWriter.EMPTY;
        }

        @Override
        public void stop()
        {   // Do nothing
        }

        @Override
        public void start()
        {   // Do nothing
        }

        @Override
        public void shutdown()
        {   // Do nothing
        }

        @Override
        public void recover( Iterator<NodeLabelUpdate> updates )
        {   // Do nothing
        }

        @Override
        public AllEntriesLabelScanReader newAllEntriesReader()
        {
            return null;
        }

        @Override
        public ResourceIterator<File> snapshotStoreFiles()
        {
            return emptyIterator();
        }

        @Override
        public void init()
        {   // Do nothing
        }

        @Override
        public void force()
        {   // Do nothing
        }
    };

    private class IteratorCollector<T> implements Answer<Object>
    {
        private final int arg;
        private final List<T> elements = new ArrayList<>();

        public IteratorCollector( int arg )
        {
            this.arg = arg;
        }

        @SafeVarargs
        public final void assertContent( T... expected )
        {
            assertEquals( Arrays.asList( expected ), elements );
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object answer( InvocationOnMock invocation ) throws Throwable
        {
            Object iterator = invocation.getArguments()[arg];
            if ( iterator instanceof Iterable )
            {
                iterator = ((Iterable) iterator).iterator();
            }
            if ( iterator instanceof Iterator )
            {
                collect( (Iterator) iterator );
            }
            return null;
        }

        private void collect( Iterator<T> iterator )
        {
            while ( iterator.hasNext() )
            {
                elements.add( iterator.next() );
            }
        }
    }

    public static class RecoveryCreatingCopyingNeoCommandHandler implements Visitor<Command, IOException>
    {
    	private final List<Command> commands = new LinkedList<Command>();

        @Override
        public boolean visit( Command element ) throws IOException
        {
            commands.add(element);
            return true;
        }

		public TransactionRepresentation getAsRecovered()
		{
			return new PhysicalTransactionRepresentation( commands );
		}
    }
}