package com.thinkaurelius.titan.diskstorage.hbase;

import com.thinkaurelius.titan.HBaseStorageSetup;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.MultiWriteKeyColumnValueStoreTest;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import org.junit.BeforeClass;

import java.io.IOException;

public class HBaseMultiWriteStoreTest extends MultiWriteKeyColumnValueStoreTest {
    @BeforeClass
    public static void startHBase() throws IOException {
        HBaseStorageSetup.startHBase();
    }

    public KeyColumnValueStoreManager openStorageManager() throws BackendException {
        return new HBaseStoreManager(HBaseStorageSetup.getHBaseConfiguration());
    }
}