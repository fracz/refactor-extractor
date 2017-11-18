package com.thinkaurelius.titan.diskstorage.common;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.ConsistencyLevel;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class AbstractStoreTransaction implements StoreTransaction {

    private final ConsistencyLevel consistencyLevel;

    public AbstractStoreTransaction(ConsistencyLevel level) {
        Preconditions.checkNotNull(level);
        consistencyLevel = level;
    }

    @Override
    public void commit() throws StorageException {
    }

    @Override
    public void abort() throws StorageException {
    }

    @Override
    public void flush() throws StorageException {
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

}