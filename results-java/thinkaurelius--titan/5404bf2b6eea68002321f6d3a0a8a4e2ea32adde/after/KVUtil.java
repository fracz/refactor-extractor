package com.thinkaurelius.titan.diskstorage.keycolumnvalue.keyvalue;

import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;
import com.thinkaurelius.titan.diskstorage.util.RecordIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for interacting with {@link KeyValueStore}.
 *
 * @author Matthias Br&ouml;cheler (me@matthiasb.com);
 */
public class KVUtil {

    public static final List<KeyValueEntry> getSlice(OrderedKeyValueStore store, StaticBuffer keyStart, StaticBuffer keyEnd, StoreTransaction txh) throws StorageException {
        return convert(store.getSlice(keyStart,keyEnd,KeySelector.SelectAll,txh));
    }

    public static final List<KeyValueEntry> getSlice(OrderedKeyValueStore store, StaticBuffer keyStart, StaticBuffer keyEnd, int limit, StoreTransaction txh) throws StorageException {
        return convert(store.getSlice(keyStart,keyEnd,new LimitedSelector(limit),txh));
    }

    public static final List<KeyValueEntry> convert(RecordIterator<KeyValueEntry> iter) throws StorageException {
        List<KeyValueEntry> entries = new ArrayList<KeyValueEntry>();
        while (iter.hasNext()) {
            entries.add(iter.next());
        }
        iter.close();
        return entries;
    }


}