package org.neo4j.kernel.ha;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.Pair;
import org.neo4j.helpers.Predicate;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.DeadlockDetectedException;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.impl.core.LockReleaser;
import org.neo4j.kernel.impl.ha.FailedResponse;
import org.neo4j.kernel.impl.ha.IdAllocation;
import org.neo4j.kernel.impl.ha.LockResult;
import org.neo4j.kernel.impl.ha.LockStatus;
import org.neo4j.kernel.impl.ha.Master;
import org.neo4j.kernel.impl.ha.Response;
import org.neo4j.kernel.impl.ha.SlaveContext;
import org.neo4j.kernel.impl.ha.TransactionStream;
import org.neo4j.kernel.impl.ha.TransactionStreams;
import org.neo4j.kernel.impl.nioneo.store.IdGenerator;
import org.neo4j.kernel.impl.transaction.IllegalResourceException;
import org.neo4j.kernel.impl.transaction.LockManager;
import org.neo4j.kernel.impl.transaction.LockType;
import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;

/**
 * This is the real master code that executes on a master. The actual
 * communication over network happens in {@link MasterClient} and
 * {@link MasterServer}.
 */
public class MasterImpl implements Master
{
    private static final int ID_GRAB_SIZE = 1000;

    private static final Predicate<Long> ALL = new Predicate<Long>()
    {
        public boolean accept( Long item )
        {
            return true;
        }
    };

    private final GraphDatabaseService graphDb;
    private final Config graphDbConfig;

    private final Map<TxIdElement, Transaction> transactions =
            Collections.synchronizedMap( new HashMap<TxIdElement, Transaction>() );

    public MasterImpl( GraphDatabaseService db )
    {
        this.graphDb = db;
        this.graphDbConfig = getConfig( db );
    }

    public GraphDatabaseService getGraphDb()
    {
        return this.graphDb;
    }

    private <T extends PropertyContainer> Response<LockResult> acquireLock( SlaveContext context,
            int eventIdentifier, LockGrabber lockGrabber, T... entities )
    {
        TxIdElement tx = new TxIdElement( context.machineId(), eventIdentifier );
        Transaction otherTx = suspendOtherAndResumeThis( tx );
        try
        {
            LockManager lockManager = graphDbConfig.getLockManager();
            LockReleaser lockReleaser = graphDbConfig.getLockReleaser();
            for ( T entity : entities )
            {
                lockGrabber.grab( lockManager, lockReleaser, entity );
            }
            return packResponse( context, new LockResult( LockStatus.OK_LOCKED ), ALL );
        }
        catch ( DeadlockDetectedException e )
        {
            return packResponse( context, new LockResult( e.getMessage() ), ALL );
        }
        catch ( IllegalResourceException e )
        {
            return packResponse( context, new LockResult( LockStatus.NOT_LOCKED ), ALL );
        }
        finally
        {
            suspendThisAndResumeOther( otherTx );
        }
    }

    private Transaction getTx( TxIdElement txId )
    {
        return transactions.get( txId );
    }

    private Transaction beginTx( TxIdElement txId )
    {
        try
        {
            TransactionManager txManager = graphDbConfig.getTxModule().getTxManager();
            txManager.begin();
            Transaction tx = txManager.getTransaction();
            transactions.put( txId, tx );
            return tx;
        }
        catch ( NotSupportedException e )
        {
            throw new RuntimeException( e );
        }
        catch ( SystemException e )
        {
            throw new RuntimeException( e );
        }
    }

    Transaction suspendOtherAndResumeThis( TxIdElement txId )
    {
        try
        {
            TransactionManager txManager = graphDbConfig.getTxModule().getTxManager();
            Transaction otherTx = txManager.getTransaction();
            Transaction transaction = getTx( txId );
            if ( otherTx != null && otherTx == transaction )
            {
                return null;
            }
            else
            {
                if ( otherTx != null )
                {
                    txManager.suspend();
                }
                if ( transaction == null )
                {
                    beginTx( txId );
                }
                else
                {
                    txManager.resume( transaction );
                }
                return otherTx;
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    void suspendThisAndResumeOther( Transaction otherTx )
    {
        try
        {
            TransactionManager txManager = graphDbConfig.getTxModule().getTxManager();
            txManager.suspend();
            if ( otherTx != null )
            {
                txManager.resume( otherTx );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    void rollbackThisAndResumeOther( TxIdElement txId, Transaction otherTx )
    {
        try
        {
            TransactionManager txManager = graphDbConfig.getTxModule().getTxManager();
            txManager.rollback();
            transactions.remove( txId );
            if ( otherTx != null )
            {
                txManager.resume( otherTx );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    public Response<LockResult> acquireNodeReadLock( SlaveContext context, int eventIdentifier,
            long... nodes )
    {
        return acquireLock( context, eventIdentifier, READ_LOCK_GRABBER, nodesById( nodes ) );
    }

    public Response<LockResult> acquireNodeWriteLock( SlaveContext context, int eventIdentifier,
            long... nodes )
    {
        return acquireLock( context, eventIdentifier, WRITE_LOCK_GRABBER, nodesById(nodes) );
    }

    public Response<LockResult> acquireRelationshipReadLock( SlaveContext context, int eventIdentifier,
            long... relationships )
    {
        return acquireLock( context, eventIdentifier, READ_LOCK_GRABBER, relationshipsById(relationships) );
    }

    public Response<LockResult> acquireRelationshipWriteLock( SlaveContext context, int eventIdentifier,
            long... relationships )
    {
        return acquireLock( context, eventIdentifier, WRITE_LOCK_GRABBER, relationshipsById(relationships) );
    }

    private Node[] nodesById( long[] ids )
    {
        Node[] result = new Node[ids.length];
        for ( int i = 0; i < ids.length; i++ )
        {
            result[i] = new LockableNode( (int) ids[i] );
        }
        return result;
    }

    private Relationship[] relationshipsById( long[] ids )
    {
        Relationship[] result = new Relationship[ids.length];
        for ( int i = 0; i < ids.length; i++ )
        {
            result[i] = new LockableRelationship( (int) ids[i] );
        }
        return result;
    }

    private static Config getConfig( GraphDatabaseService db )
    {
        try
        {
            // Quite ugly :)
            return (Config) db.getClass().getDeclaredMethod( "getConfig" ).invoke( db );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public IdAllocation allocateIds( IdType idType )
    {
        IdGenerator generator = graphDbConfig.getIdGeneratorFactory().get( idType );
        return new IdAllocation( generator.nextIdBatch( ID_GRAB_SIZE ),
                generator.getHighId(), generator.getDefragCount() );
    }

    public Response<Long> commitSingleResourceTransaction( SlaveContext context,
            int eventIdentifier, String resource, TransactionStream transactionStream )
    {
        TxIdElement tx = new TxIdElement( context.machineId(), eventIdentifier );
        Transaction otherTx = suspendOtherAndResumeThis( tx );
        try
        {
            XaDataSource dataSource = graphDbConfig.getTxModule()
                    .getXaDataSourceManager().getXaDataSource( resource );
            // Always exactly one transaction (ReadableByteChannel)
            final long txId = dataSource.applyPreparedTransaction(
                    transactionStream.getChannels().iterator().next() );
            Predicate<Long> notThisTx = new Predicate<Long>()
            {
                public boolean accept( Long item )
                {
                    return item != txId;
                }
            };
            return packResponse( context, txId, notThisTx );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return new FailedResponse<Long>();
        }
        finally
        {
            suspendThisAndResumeOther( otherTx );
            // Since the master-transaction carries no actual state, just locks
            // we would like to release the locks... and it's best done by just
            // rolling back the tx
//            rollbackThisAndResumeOther( otherTx );
        }
    }

    public Response<Void> doneCommitting( SlaveContext context, int eventIdentifier )
    {
        TxIdElement tx = new TxIdElement( context.machineId(), eventIdentifier );
        Transaction otherTx = suspendOtherAndResumeThis( tx );
        rollbackThisAndResumeOther( tx, otherTx );
        return packResponse( context, null, ALL );
    }

    public Response<Integer> createRelationshipType( SlaveContext context, String name )
    {
        // Does this type exist already?
        Integer id = graphDbConfig.getRelationshipTypeHolder().getIdFor( name );
        if ( id != null )
        {
            // OK, return
            return packResponse( context, id, ALL );
        }

        // No? Create it then
        id = graphDbConfig.getRelationshipTypeCreator().getOrCreate(
                graphDbConfig.getTxModule().getTxManager(),
                graphDbConfig.getIdGeneratorModule().getIdGenerator(),
                graphDbConfig.getPersistenceModule().getPersistenceManager(),
                graphDbConfig.getRelationshipTypeHolder(), name );
        return packResponse( context, id, ALL );
    }

    private <T> Response<T> packResponse( SlaveContext context, T response,
            Predicate<Long> filter )
    {
        try
        {
            TransactionStreams streams = new TransactionStreams();
            for ( Pair<String, Long> txEntry : context.lastAppliedTransactions() )
            {
                String resourceName = txEntry.first();
                XaDataSource dataSource = graphDbConfig.getTxModule().getXaDataSourceManager()
                        .getXaDataSource( resourceName );
                long masterLastTx = dataSource.getLastCommittedTxId();
                Collection<ReadableByteChannel> channels = new ArrayList<ReadableByteChannel>();
                for ( long txId = txEntry.other()+1; txId <= masterLastTx; txId++ )
                {
                    if ( filter.accept( txId ) )
                    {
                        channels.add( dataSource.getCommittedTransaction( txId ) );
                    }
                }
                streams.add( resourceName, new TransactionStream( channels ) );
            }
            return new Response<T>( response, streams );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return new FailedResponse<T>();
        }
    }

    public Response<Void> pullUpdates( SlaveContext context )
    {
        return packResponse( context, null, ALL );
    }

    public Response<Void> rollbackTransaction( SlaveContext context, int eventIdentifier )
    {
        TxIdElement txId = new TxIdElement( context.machineId(), eventIdentifier );
        Transaction otherTx = suspendOtherAndResumeThis( txId );
        try
        {
            Transaction tx = transactions.remove( txId );
            if ( tx == null )
            {
                throw new RuntimeException( "Shouldn't happen" );
            }
            graphDbConfig.getTxModule().getTxManager().rollback();
            return packResponse( context, null, ALL );
        }
        catch ( IllegalStateException e )
        {
            throw new RuntimeException( e );
        }
        catch ( SecurityException e )
        {
            throw new RuntimeException( e );
        }
        catch ( SystemException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            suspendThisAndResumeOther( otherTx );
        }
    }

    public void rollbackOngoingTransactions( SlaveContext context )
    {
        int slaveMachineId = context.machineId();
        Collection<TxIdElement> rolledbackTxs = new ArrayList<TxIdElement>();
        for ( Map.Entry<TxIdElement, Transaction> entry : transactions.entrySet() )
        {
            if ( entry.getKey().machineId == slaveMachineId )
            {
                System.out.println( "Rolling back ongoing tx " + entry );
                Transaction otherTx = suspendOtherAndResumeThis( entry.getKey() );
                rollbackThisAndResumeOther( entry.getKey(), otherTx );
                rolledbackTxs.add( entry.getKey() );
            }
        }

        for ( TxIdElement element : rolledbackTxs )
        {
            transactions.remove( element );
        }
    }

    private static final class TxIdElement
    {
        private final int machineId;
        private final int eventIdentifier;
        private final int hashCode;

        TxIdElement( int machineId, int eventIdentifier )
        {
            this.machineId = machineId;
            this.eventIdentifier = eventIdentifier;
            this.hashCode = calculateHashCode();
        }

        private int calculateHashCode()
        {
            return (machineId << 20) | eventIdentifier;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        @Override
        public boolean equals( Object obj )
        {
            TxIdElement other = (TxIdElement) obj;
            return other.machineId == machineId && other.eventIdentifier == eventIdentifier;
        }
    }

    private static interface LockGrabber
    {
        void grab( LockManager lockManager, LockReleaser lockReleaser, Object entity );
    }

    private static LockGrabber READ_LOCK_GRABBER = new LockGrabber()
    {
        public void grab( LockManager lockManager, LockReleaser lockReleaser, Object entity )
        {
            lockManager.getReadLock( entity );
            lockReleaser.addLockToTransaction( entity, LockType.READ );
        }
    };

    private static LockGrabber WRITE_LOCK_GRABBER = new LockGrabber()
    {
        public void grab( LockManager lockManager, LockReleaser lockReleaser, Object entity )
        {
            lockManager.getWriteLock( entity );
            lockReleaser.addLockToTransaction( entity, LockType.WRITE );
        }
    };

    // =====================================================================
    // Just some methods which aren't really used when running a HA cluster,
    // but exposed so that other tools can reach that information.
    // =====================================================================

    public Map<Integer, Collection<Integer>> getOngoingTransactions()
    {
        Map<Integer, Collection<Integer>> result = new HashMap<Integer, Collection<Integer>>();
        for ( TxIdElement txId : transactions.keySet() )
        {
            Collection<Integer> identifiers = result.get( txId.machineId );
            if ( identifiers == null )
            {
                identifiers = new ArrayList<Integer>();
                result.put( txId.machineId, identifiers );
            }
            identifiers.add( txId.eventIdentifier );
        }
        return result;
    }
}