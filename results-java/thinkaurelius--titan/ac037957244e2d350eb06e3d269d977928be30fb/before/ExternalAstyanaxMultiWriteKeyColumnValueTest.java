package com.thinkaurelius.titan.diskstorage.astyanax;

import com.thinkaurelius.titan.StorageSetup;
import com.thinkaurelius.titan.diskstorage.MultiWriteKeyColumnValueStoreTest;
import com.thinkaurelius.titan.diskstorage.StorageManager;
import com.thinkaurelius.titan.testutil.CassandraUtil;

public class ExternalAstyanaxMultiWriteKeyColumnValueTest extends MultiWriteKeyColumnValueStoreTest {

    @Override
    public StorageManager openStorageManager() {
        return new AstyanaxStorageManager(StorageSetup.getLocalStorageConfiguration());
    }

	@Override
	public void cleanUp() {
		CassandraUtil.dropKeyspace(AstyanaxStorageManager.KEYSPACE_DEFAULT);
	}
}