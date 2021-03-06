package com.thinkaurelius.titan.diskstorage.cassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.thinkaurelius.titan.diskstorage.*;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.Entry;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.MultiWriteKeyColumnValueStore;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.Mutation;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStore;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransactionHandle;
import com.thinkaurelius.titan.diskstorage.locking.PermanentLockingException;
import com.thinkaurelius.titan.diskstorage.locking.consistentkey.ConsistentKeyLockStore;
import com.thinkaurelius.titan.diskstorage.locking.consistentkey.LockConfig;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.SliceByNamesReadCommand;
import org.apache.cassandra.db.SliceFromReadCommand;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.scheduler.IRequestScheduler;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.UnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.locking.consistentkey.LocalLockMediator;
import com.thinkaurelius.titan.diskstorage.util.TimeUtility;

public class CassandraEmbeddedOrderedKeyColumnValueStore
	implements KeyColumnValueStore, MultiWriteKeyColumnValueStore {

	private final String keyspace;
	private final String columnFamily;
	private final ConsistencyLevel readConsistencyLevel;
	private final ConsistencyLevel writeConsistencyLevel;
	private final LockConfig internals;
    private final IRequestScheduler requestScheduler;

	private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
	private static final Logger log = LoggerFactory
			.getLogger(CassandraEmbeddedOrderedKeyColumnValueStore.class);



	public CassandraEmbeddedOrderedKeyColumnValueStore (
			String keyspace,
			String columnFamily,
            ConsistencyLevel readConsistencyLevel,
            ConsistencyLevel writeConsistencyLevel,
            CassandraEmbeddedOrderedKeyColumnValueStore lockStore,
            LocalLockMediator llm,
            byte[] rid,
            int lockRetryCount,
            long lockWaitMS,
            long lockExpireMS) throws RuntimeException {
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;
		this.readConsistencyLevel = readConsistencyLevel;
		this.writeConsistencyLevel = writeConsistencyLevel;
        this.requestScheduler = DatabaseDescriptor.getRequestScheduler();

		if (null != llm && null != lockStore) {
			this.internals = new ConsistentKeyLockStore(this, lockStore, llm,
					rid, lockRetryCount, lockWaitMS, lockExpireMS);
		} else {
			this.internals = null;
		}
	}

	@Override
	public void close() throws StorageException {
		// TODO Auto-generated method stub
	}

	@Override
	public ByteBuffer get(ByteBuffer key, ByteBuffer column,
			StoreTransactionHandle txh) throws StorageException {

		QueryPath slicePath = new QueryPath(columnFamily);

		SliceByNamesReadCommand namesCmd = new SliceByNamesReadCommand(
				keyspace, key.duplicate(), slicePath, Arrays.asList(column.duplicate()));

		ConsistencyLevel clvl = getConsistencyLevel(txh, Operation.READ);
		List<Row> rows = read(namesCmd, clvl);

		if (null == rows || 0 == rows.size())
			return null;

		if (1 < rows.size())
			throw new PermanentStorageException("Received " + rows.size()
					+ " rows from a single-key-column cassandra read");

		assert 1 == rows.size();

		Row r = rows.get(0);

		if (null == r) {
			log.warn("Null Row object retrieved from Cassandra StorageProxy");
			return null;
		}

		ColumnFamily cf = r.cf;
		if (null == cf)
			return null;

		if (cf.isMarkedForDelete())
			return null;

		IColumn c = cf.getColumn(column.duplicate());
		if (null == c)
			return null;

		// These came up during testing
		if (c.isMarkedForDelete())
			return null;

		return org.apache.cassandra.utils.ByteBufferUtil.clone(c.value());
	}

	@Override
	public boolean containsKeyColumn(ByteBuffer key, ByteBuffer column,
			StoreTransactionHandle txh) throws StorageException {
		return null != get(key, column, txh);
	}

	@Override
	public boolean isLocalKey(ByteBuffer key) {
		return true;  // TODO check ring
	}

	@Override
	public void mutate(ByteBuffer key, List<Entry> additions,
			List<ByteBuffer> deletions, StoreTransactionHandle txh) throws StorageException {

    	Map<ByteBuffer, Mutation> mutations =
    			new HashMap<ByteBuffer, Mutation>(1);

    	Mutation m = new
                Mutation(additions, deletions);

    	mutations.put(key, m);

    	mutateMany(mutations, txh);
	}

	@Override
	public void acquireLock(ByteBuffer key, ByteBuffer column,
			ByteBuffer expectedValue, StoreTransactionHandle txh) throws StorageException {

		CassandraETransaction ctxh = (CassandraETransaction)txh;
		if (ctxh.isMutationStarted()) {
			throw new PermanentLockingException("Attempted to obtain a lock after one or more mutations");
		}

		ctxh.writeBlindLockClaim(internals, key, column, expectedValue);
	}

	@Override
	public boolean containsKey(ByteBuffer key, StoreTransactionHandle txh) throws StorageException {

		QueryPath slicePath = new QueryPath(columnFamily);
        ReadCommand sliceCmd = new SliceFromReadCommand(
                keyspace,          // Keyspace name
                key.duplicate(),   // Row key
                slicePath,         // ColumnFamily
                EMPTY_BYTE_BUFFER, // Start column name (empty means begin at first result)
                EMPTY_BYTE_BUFFER, // End column name (empty means max out the count)
                false,             // Reverse results? (false=no)
                1);                // Max count of Columns to return
        ConsistencyLevel clvl = getConsistencyLevel(txh, Operation.READ);

        List<Row> rows = read(sliceCmd, clvl);

        if (null == rows || 0 == rows.size())
        	return false;

        /*
		 * Find at least one live column
		 *
		 * Note that the rows list may contain arbitrarily many
		 * marked-for-delete elements. Therefore, we can't assume that we're
		 * dealing with a singleton even though we set the maximum column count
		 * to 1.
		 */
        for (Row r : rows) {
        	if (null == r || null == r.cf)
        		continue;

        	if (r.cf.isMarkedForDelete())
        		continue;

        	for (IColumn ic : r.cf)
        		if (!ic.isMarkedForDelete())
        			return true;
        }

        return false;
	}

	@Override
	public List<Entry> getSlice(ByteBuffer key, ByteBuffer columnStart,
			ByteBuffer columnEnd, int limit, StoreTransactionHandle txh) throws StorageException {

		QueryPath slicePath = new QueryPath(columnFamily);
        ReadCommand sliceCmd = new SliceFromReadCommand(
                keyspace,        // Keyspace name
                key.duplicate(), // Row key
                slicePath,       // ColumnFamily
                columnStart.duplicate(),     // Start column name (empty means begin at first result)
                columnEnd.duplicate(),       // End column name (empty means max out the count)
                false,           // Reverse results? (false=no)
                limit);          // Max count of Columns to return

        ConsistencyLevel clvl = getConsistencyLevel(txh, Operation.READ);

        List<Row> slice = read(sliceCmd, clvl);

        if (null == slice || 0 == slice.size())
        	return new ArrayList<Entry>(0);

        int sliceSize = slice.size();
        if (1 < sliceSize)
        	throw new PermanentStorageException("Received " + sliceSize + " rows for single key");

        Row r = slice.get(0);

		if (null == r) {
			log.warn("Null Row object retrieved from Cassandra StorageProxy");
			return new ArrayList<Entry>(0);
		}

        ColumnFamily cf = r.cf;

        if (null == cf) {
        	log.warn("null ColumnFamily (\"{}\")", columnFamily);
        	return new ArrayList<Entry>(0);
        }

        if (cf.isMarkedForDelete())
        	return new ArrayList<Entry>(0);

        return cfToEntries(cf, columnStart, columnEnd);
	}

	@Override
	public List<Entry> getSlice(ByteBuffer key, ByteBuffer columnStart,
			ByteBuffer columnEnd, StoreTransactionHandle txh) throws StorageException {
		return getSlice(key, columnStart, columnEnd, Integer.MAX_VALUE, txh);
	}

	/*
	 * This implementation can't handle counter columns.
	 *
	 * The private method internal_batch_mutate in CassandraServer as of 1.1.3
	 * provided most of the following method after transaction handling.
	 */
	@Override
	public void mutateMany(Map<ByteBuffer, Mutation> mutations,
			StoreTransactionHandle txh) throws StorageException {
    	if (null == mutations)
    		return;

    	// null txh means a CassandraTransaction is calling this method
    	if (null != txh) {
    		// non-null txh -> make sure locks are valid
    		CassandraETransaction ctxh = (CassandraETransaction)txh;
    		if (! ctxh.isMutationStarted()) {
    			// This is the first mutate call in the transaction
    			ctxh.mutationStarted();
    			// Verify all blind lock claims now
    			ctxh.verifyAllLockClaims(); // throws GSE and unlocks everything on any lock failure
    		}
    	}

		long deletionTimestamp = TimeUtility.getApproxNSSinceEpoch(false);
		long additionTimestamp = TimeUtility.getApproxNSSinceEpoch(true);

		List<RowMutation> commands = new ArrayList<RowMutation>(mutations.size());

		for (Map.Entry<ByteBuffer, Mutation> titanMutation : mutations.entrySet()) {
			ByteBuffer key = titanMutation.getKey().duplicate();
			Mutation mut = titanMutation.getValue();

			RowMutation rm = new RowMutation(keyspace, key);

			if (mut.hasAdditions()) {
				for (Entry e : mut.getAdditions()) {
					QueryPath path = new QueryPath(columnFamily, null, e.getColumn().duplicate());
					rm.add(path, e.getValue().duplicate(), additionTimestamp);
				}
			}

			if (mut.hasDeletions()) {
				for (ByteBuffer col : mut.getDeletions()) {
					QueryPath path = new QueryPath(columnFamily, null, col.duplicate());
					rm.delete(path, deletionTimestamp);
				}
			}

			commands.add(rm);
		}

		ConsistencyLevel clvl = getConsistencyLevel(txh, Operation.WRITE);

		mutate(commands, clvl);
	}

	private List<Row> read(ReadCommand cmd, ConsistencyLevel clvl) throws StorageException {
		ArrayList<ReadCommand> cmdHolder = new ArrayList<ReadCommand>(1);
		cmdHolder.add(cmd);
		return read(cmdHolder, clvl);
	}

	private List<Row> read(List<ReadCommand> cmds, ConsistencyLevel clvl) throws StorageException {
		try {
			return StorageProxy.read(cmds, clvl);
		} catch (IOException e) {
			throw new PermanentStorageException(e);
		} catch (UnavailableException e) {
			throw new TemporaryStorageException(e);
		} catch (TimeoutException e) {
			throw new TemporaryStorageException(e);
		} catch (InvalidRequestException e) {
			throw new PermanentStorageException(e);
		}
	}

	private ConsistencyLevel getConsistencyLevel(StoreTransactionHandle txh, Operation op) {

		if (null == txh) {
			return ConsistencyLevel.QUORUM;
		}

		if (op.equals(Operation.WRITE)) {
			return writeConsistencyLevel;
		} else {
			return readConsistencyLevel;
		}
	}

	private List<Entry> cfToEntries(ColumnFamily cf, ByteBuffer columnStart,
			ByteBuffer columnEnd) throws StorageException {

		assert ! cf.isMarkedForDelete();

		// Estimate size of Entry list, ignoring deleted columns
		int resultSize = 0;
		for (ByteBuffer col : cf.getColumnNames()) {
			IColumn icol = cf.getColumn(col);
			if (null == icol)
				throw new PermanentStorageException("Unexpected null IColumn");

			if (icol.isMarkedForDelete())
				continue;

			resultSize++;
		}

		// Instantiate return collection
		List<Entry> result = new ArrayList<Entry>(resultSize);

		// Populate Entries into return collection
		for (ByteBuffer col : cf.getColumnNames()) {

			IColumn icol = cf.getColumn(col);
			if (null == icol)
				throw new PermanentStorageException("Unexpected null IColumn");

			if (icol.isMarkedForDelete())
				continue;

			ByteBuffer name = org.apache.cassandra.utils.ByteBufferUtil.clone(icol.name());
			ByteBuffer value = org.apache.cassandra.utils.ByteBufferUtil.clone(icol.value());

			if (columnEnd.equals(name))
				continue;

			result.add(new Entry(name, value));
		}

		return result;
	}

	private void mutate(List<RowMutation> cmds, ConsistencyLevel clvl) throws StorageException {
		try {
			schedule(DatabaseDescriptor.getRpcTimeout());
			try {
				StorageProxy.mutate(cmds, clvl);
			} finally {
				release();
			}
		} catch (UnavailableException ex) {
			throw new TemporaryStorageException(ex);
		} catch (TimeoutException ex) {
			log.debug("Cassandra TimeoutException", ex);
			throw new TemporaryStorageException(ex);
		}
	}

    private void schedule(long timeoutMS) throws TimeoutException
    {
        requestScheduler.queue(Thread.currentThread(), "default", DatabaseDescriptor.getRpcTimeout());
    }

    /**
     * Release count for the used up resources
     */
    private void release()
    {
        requestScheduler.release();
    }

    private static enum Operation { READ, WRITE; }

}