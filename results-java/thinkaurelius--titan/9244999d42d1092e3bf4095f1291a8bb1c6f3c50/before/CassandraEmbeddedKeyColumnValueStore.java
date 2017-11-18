package com.thinkaurelius.titan.diskstorage.cassandra.embedded;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.thinkaurelius.titan.util.time.TimestampProvider;
import com.thinkaurelius.titan.diskstorage.*;
import com.thinkaurelius.titan.diskstorage.cassandra.utils.CassandraHelper;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.*;
import com.thinkaurelius.titan.diskstorage.util.RecordIterator;
import com.thinkaurelius.titan.diskstorage.util.StaticArrayBuffer;
import com.thinkaurelius.titan.diskstorage.util.StaticArrayEntry;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.filter.IDiskAtomFilter;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.dht.*;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.IsBootstrappingException;
import org.apache.cassandra.exceptions.RequestTimeoutException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.*;

import static com.thinkaurelius.titan.diskstorage.cassandra.CassandraTransaction.getTx;

public class CassandraEmbeddedKeyColumnValueStore implements KeyColumnValueStore {

    private static final Logger log = LoggerFactory.getLogger(CassandraEmbeddedKeyColumnValueStore.class);

    private final String keyspace;
    private final String columnFamily;
    private final CassandraEmbeddedStoreManager storeManager;
    private final TimestampProvider times;
    private final CassandraEmbeddedGetter entryGetter;

    public CassandraEmbeddedKeyColumnValueStore(
            String keyspace,
            String columnFamily,
            CassandraEmbeddedStoreManager storeManager) throws RuntimeException {
        this.keyspace = keyspace;
        this.columnFamily = columnFamily;
        this.storeManager = storeManager;
        this.times = this.storeManager.getTimestampProvider();
        entryGetter = new CassandraEmbeddedGetter(storeManager.getMetaDataSchema(columnFamily),times);
    }

    @Override
    public void close() throws StorageException {
    }

    @Override
    public void acquireLock(StaticBuffer key, StaticBuffer column,
                            StaticBuffer expectedValue, StoreTransaction txh) throws StorageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyIterator getKeys(KeyRangeQuery keyRangeQuery, StoreTransaction txh) throws StorageException {
        IPartitioner partitioner = StorageService.getPartitioner();

        // see rant about this in Astyanax implementation
        if (partitioner instanceof RandomPartitioner || partitioner instanceof Murmur3Partitioner)
            throw new PermanentStorageException("This operation is only supported when byte-ordered partitioner is used.");

        return new RowIterator(keyRangeQuery, storeManager.getPageSize(), txh);
    }

    @Override
    public KeyIterator getKeys(SliceQuery query, StoreTransaction txh) throws StorageException {
        return new RowIterator(getMinimumToken(), getMaximumToken(), query, storeManager.getPageSize(), txh);
    }

    private List<Row> getKeySlice(Token start,
                                  Token end,
                                  @Nullable SliceQuery sliceQuery,
                                  int pageSize,
                                  long timestamp) throws StorageException {
        IPartitioner<?> partitioner = StorageService.getPartitioner();

        SliceRange columnSlice = new SliceRange();
        if (sliceQuery == null) {
            columnSlice.setStart(ArrayUtils.EMPTY_BYTE_ARRAY)
                    .setFinish(ArrayUtils.EMPTY_BYTE_ARRAY)
                    .setCount(5);
        } else {
            columnSlice.setStart(sliceQuery.getSliceStart().asByteBuffer())
                    .setFinish(sliceQuery.getSliceEnd().asByteBuffer())
                    .setCount(sliceQuery.hasLimit() ? sliceQuery.getLimit() : Integer.MAX_VALUE);
        }
        /* Note: we need to fetch columns for each row as well to remove "range ghosts" */
        SlicePredicate predicate = new SlicePredicate().setSlice_range(columnSlice);

        RowPosition startPosition = start.minKeyBound(partitioner);
        RowPosition endPosition = end.minKeyBound(partitioner);

        List<Row> rows;

        try {
            CFMetaData cfm = Schema.instance.getCFMetaData(keyspace, columnFamily);
            IDiskAtomFilter filter = ThriftValidation.asIFilter(predicate, cfm, null);
            RangeSliceCommand cmd = new RangeSliceCommand(keyspace, columnFamily, timestamp, filter, new Bounds<RowPosition>(startPosition, endPosition), pageSize);

            rows = StorageProxy.getRangeSlice(cmd, ConsistencyLevel.QUORUM);
        } catch (Exception e) {
            throw new PermanentStorageException(e);
        }

        return rows;
    }

    @Override
    public List<KeyRange> getLocalKeyPartition() throws StorageException {
        return storeManager.getLocalKeyPartition();
    }


    @Override
    public String getName() {
        return columnFamily;
    }

    @Override
    public EntryList getSlice(KeySliceQuery query, StoreTransaction txh) throws StorageException {

        final long ts = getTime(txh);

        SliceQueryFilter sqf = new SliceQueryFilter(query.getSliceStart().asByteBuffer(), query.getSliceEnd().asByteBuffer(), false, query.getLimit() + (query.hasLimit()?1:0));
        ReadCommand sliceCmd = new SliceFromReadCommand(keyspace, query.getKey().asByteBuffer(), columnFamily, ts, sqf);

        List<Row> slice = read(sliceCmd, getTx(txh).getReadConsistencyLevel().getDB());

        if (null == slice || 0 == slice.size())
            return EntryList.EMPTY_LIST;

        int sliceSize = slice.size();
        if (1 < sliceSize)
            throw new PermanentStorageException("Received " + sliceSize + " rows for single key");

        Row r = slice.get(0);

        if (null == r) {
            log.warn("Null Row object retrieved from Cassandra StorageProxy");
            return EntryList.EMPTY_LIST;
        }

        ColumnFamily cf = r.cf;

        if (null == cf) {
            log.debug("null ColumnFamily (\"{}\")", columnFamily);
            return EntryList.EMPTY_LIST;
        }

        if (cf.isMarkedForDelete())
            return EntryList.EMPTY_LIST;

        return CassandraHelper.makeEntryList(
                Iterables.filter(cf.getSortedColumns(), new FilterDeletedColumns(ts)),
                entryGetter,
                query.getSliceEnd(),
                query.getLimit());

    }

    private class FilterDeletedColumns implements Predicate<Column> {

        private final long ts;

        private FilterDeletedColumns(long ts) {
            this.ts = ts;
        }

        @Override
        public boolean apply(Column input) {
            return !input.isMarkedForDelete(ts);
        }
    }

    @Override
    public Map<StaticBuffer,EntryList> getSlice(List<StaticBuffer> keys, SliceQuery query, StoreTransaction txh) throws StorageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mutate(StaticBuffer key, List<Entry> additions,
                       List<StaticBuffer> deletions, StoreTransaction txh) throws StorageException {
        Map<StaticBuffer, KCVMutation> mutations = ImmutableMap.of(key, new
                KCVMutation(additions, deletions));
        mutateMany(mutations, txh);
    }


    public void mutateMany(Map<StaticBuffer, KCVMutation> mutations,
                           StoreTransaction txh) throws StorageException {
        storeManager.mutateMany(ImmutableMap.of(columnFamily, mutations), txh);
    }

    private static List<Row> read(ReadCommand cmd, org.apache.cassandra.db.ConsistencyLevel clvl) throws StorageException {
        ArrayList<ReadCommand> cmdHolder = new ArrayList<ReadCommand>(1);
        cmdHolder.add(cmd);
        return read(cmdHolder, clvl);
    }

    private static List<Row> read(List<ReadCommand> cmds, org.apache.cassandra.db.ConsistencyLevel clvl) throws StorageException {
        try {
            return StorageProxy.read(cmds, clvl);
        } catch (UnavailableException e) {
            throw new TemporaryStorageException(e);
        } catch (RequestTimeoutException e) {
            throw new PermanentStorageException(e);
        } catch (IsBootstrappingException e) {
            throw new TemporaryStorageException(e);
        } catch (InvalidRequestException e) {
            throw new PermanentStorageException(e);
        }
    }

    private static class CassandraEmbeddedGetter implements StaticArrayEntry.GetColVal<Column,ByteBuffer> {

        private final EntryMetaData[] schema;
        private final TimestampProvider times;

        private CassandraEmbeddedGetter(EntryMetaData[] schema, TimestampProvider times) {
            this.schema = schema;
            this.times = times;
        }

        @Override
        public ByteBuffer getColumn(Column element) {
            return org.apache.cassandra.utils.ByteBufferUtil.clone(element.name());
        }

        @Override
        public ByteBuffer getValue(Column element) {
            return org.apache.cassandra.utils.ByteBufferUtil.clone(element.value());
        }

        @Override
        public EntryMetaData[] getMetaSchema(Column element) {
            return schema;
        }

        @Override
        public Object getMetaData(Column element, EntryMetaData meta) {
            switch (meta) {
                case TIMESTAMP:
                    return element.timestamp();
                case TTL:
                    return (long) ((element instanceof ExpiringColumn)
                                    ? ((ExpiringColumn) element).getTimeToLive()
                                    : 0);
                default:
                    throw new UnsupportedOperationException("Unsupported meta data: " + meta);
            }
        }
    }

    private long getTime(StoreTransaction txh) {
        //TODO: Use actual start time and do so consistently across the different adapters
        return times.getTime().getNativeTimestamp();
    }

    private class RowIterator implements KeyIterator {
        private final Token maximumToken;
        private final SliceQuery sliceQuery;
        private final StoreTransaction txh;

        private Iterator<Row> keys;
        private ByteBuffer lastSeenKey = null;
        private Row currentRow;
        private int pageSize;
        private final long ts;

        private boolean isClosed;

        public RowIterator(KeyRangeQuery keyRangeQuery, int pageSize, StoreTransaction txh) throws StorageException {
            this(StorageService.getPartitioner().getToken(keyRangeQuery.getKeyStart().asByteBuffer()),
                    StorageService.getPartitioner().getToken(keyRangeQuery.getKeyEnd().asByteBuffer()),
                    keyRangeQuery,
                    pageSize,
                    txh);
        }

        public RowIterator(Token minimum, Token maximum, SliceQuery sliceQuery, int pageSize, StoreTransaction txh) throws StorageException {
            this.ts = getTime(txh);
            this.keys = getRowsIterator(getKeySlice(minimum, maximum, sliceQuery, pageSize, ts));
            this.pageSize = pageSize;
            this.sliceQuery = sliceQuery;
            this.maximumToken = maximum;
            this.txh = txh;
        }

        @Override
        public boolean hasNext() {
            try {
                return hasNextInternal();
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public StaticBuffer next() {
            ensureOpen();

            if (!hasNext())
                throw new NoSuchElementException();

            currentRow = keys.next();
            ByteBuffer currentKey = currentRow.key.key.duplicate();

            try {
                return StaticArrayBuffer.of(currentKey);
            } finally {
                lastSeenKey = currentKey;
            }
        }

        @Override
        public void close() {
            isClosed = true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RecordIterator<Entry> getEntries() {
            ensureOpen();

            if (sliceQuery == null)
                throw new IllegalStateException("getEntries() requires SliceQuery to be set.");

            return new RecordIterator<Entry>() {
                final Iterator<Entry> columns = CassandraHelper.makeEntryIterator(
                        Iterables.filter(currentRow.cf.getSortedColumns(), new FilterDeletedColumns(ts)),
                        entryGetter,
                        sliceQuery.getSliceEnd(),
                        sliceQuery.getLimit());

                 //cfToEntries(currentRow.cf, sliceQuery).iterator();

                @Override
                public boolean hasNext() {
                    ensureOpen();
                    return columns.hasNext();
                }

                @Override
                public Entry next() {
                    ensureOpen();
                    return columns.next();
                }

                @Override
                public void close() {
                    isClosed = true;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

        }

        private final boolean hasNextInternal() throws StorageException {
            ensureOpen();

            if (keys == null)
                return false;

            boolean hasNext = keys.hasNext();

            if (!hasNext && lastSeenKey != null) {
                Token lastSeenToken = StorageService.getPartitioner().getToken(lastSeenKey.duplicate());

                // let's check if we reached key upper bound already so we can skip one useless call to Cassandra
                if (maximumToken != getMinimumToken() && lastSeenToken.equals(maximumToken)) {
                    return false;
                }

                List<Row> newKeys = getKeySlice(StorageService.getPartitioner().getToken(lastSeenKey), maximumToken, sliceQuery, pageSize, ts);

                keys = getRowsIterator(newKeys, lastSeenKey);
                hasNext = keys.hasNext();
            }

            return hasNext;
        }

        private void ensureOpen() {
            if (isClosed)
                throw new IllegalStateException("Iterator has been closed.");
        }

        private Iterator<Row> getRowsIterator(List<Row> rows) {
            if (rows == null)
                return null;

            return Iterators.filter(rows.iterator(), new Predicate<Row>() {
                @Override
                public boolean apply(@Nullable Row row) {
                    return !(row == null || row.cf == null || row.cf.isMarkedForDelete() || row.cf.hasOnlyTombstones(ts));
                }
            });
        }

        private Iterator<Row> getRowsIterator(List<Row> rows, final ByteBuffer exceptKey) {
            Iterator<Row> rowIterator = getRowsIterator(rows);

            if (rowIterator == null)
                return null;

            return Iterators.filter(rowIterator, new Predicate<Row>() {
                @Override
                public boolean apply(@Nullable Row row) {
                    return row != null && !row.key.key.equals(exceptKey);
                }
            });
        }
    }

    private static Token getMinimumToken() throws PermanentStorageException {
        IPartitioner partitioner = StorageService.getPartitioner();

        if (partitioner instanceof RandomPartitioner) {
            return ((RandomPartitioner) partitioner).getMinimumToken();
        } else if (partitioner instanceof Murmur3Partitioner) {
            return ((Murmur3Partitioner) partitioner).getMinimumToken();
        } else if (partitioner instanceof ByteOrderedPartitioner) {
            //TODO: This makes the assumption that its an EdgeStore (i.e. 8 byte keys)
            return new BytesToken(com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil.zeroByteBuffer(8));
        } else {
            throw new PermanentStorageException("Unsupported partitioner: " + partitioner);
        }
    }

    private static Token getMaximumToken() throws PermanentStorageException {
        IPartitioner partitioner = StorageService.getPartitioner();

        if (partitioner instanceof RandomPartitioner) {
            return new BigIntegerToken(RandomPartitioner.MAXIMUM);
        } else if (partitioner instanceof Murmur3Partitioner) {
            return new LongToken(Murmur3Partitioner.MAXIMUM);
        } else if (partitioner instanceof ByteOrderedPartitioner) {
            //TODO: This makes the assumption that its an EdgeStore (i.e. 8 byte keys)
            return new BytesToken(com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil.oneByteBuffer(8));
        } else {
            throw new PermanentStorageException("Unsupported partitioner: " + partitioner);
        }
    }
}