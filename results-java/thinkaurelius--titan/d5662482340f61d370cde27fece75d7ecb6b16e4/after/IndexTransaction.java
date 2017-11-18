package com.thinkaurelius.titan.diskstorage.indexing;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.graphdb.query.keycondition.Relation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: add index transaction
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class IndexTransaction implements TransactionHandle {

    private static final int DEFAULT_OUTER_MAP_SIZE = 3;
    private static final int DEFAULT_INNER_MAP_SIZE = 5;

    private final IndexProvider index;
    private final TransactionHandle indexTx;
    private Map<String,Map<String,IndexMutation>> mutations;

    public IndexTransaction(final IndexProvider index) throws StorageException {
        Preconditions.checkNotNull(index);
        this.index=index;
        this.indexTx=index.beginTransaction();
        Preconditions.checkNotNull(indexTx);
        this.mutations = null;
    }

    public void add(String store, String docid, String key, Object value) {
        getIndexMutation(store,docid).addition(new IndexEntry(key,value));
    }

    public void delete(String store, String docid, String key) {
        getIndexMutation(store,docid).deletion(key);
    }

    private IndexMutation getIndexMutation(String store, String docid) {
        if (mutations==null) mutations = new HashMap<String,Map<String,IndexMutation>>(DEFAULT_OUTER_MAP_SIZE);
        Map<String,IndexMutation> storeMutations = mutations.get(store);
        if (storeMutations==null) {
            storeMutations = new HashMap<String,IndexMutation>(DEFAULT_INNER_MAP_SIZE);
            mutations.put(store,storeMutations);

        }
        IndexMutation m = storeMutations.get(docid);
        if (m==null) {
            m = new IndexMutation();
            storeMutations.put(docid, m);
        }
        return m;
    }


    public void register(String store, String key, Class<?> dataType) throws StorageException {
        index.register(store,key,dataType,indexTx);
    }

    public boolean covers(Class<?> dataType, Relation relation) {
        return index.supports(dataType, relation);
    }

    public List<String> query(IndexQuery query) throws StorageException {
        return index.query(query,indexTx);
    }

    @Override
    public void commit() throws StorageException {
        flushInternal();
        indexTx.commit();
    }

    @Override
    public void abort() throws StorageException {
        mutations=null;
        indexTx.abort();
    }

    @Override
    public void flush() throws StorageException {
        flushInternal();
        indexTx.flush();
    }

    private void flushInternal() throws StorageException {
        if (mutations!=null && !mutations.isEmpty()) {
            index.mutate(mutations,indexTx);
            mutations=null;
        }
    }

}