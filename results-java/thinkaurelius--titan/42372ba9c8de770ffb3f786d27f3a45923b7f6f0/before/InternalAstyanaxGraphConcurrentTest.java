package com.thinkaurelius.titan.graphdb.astyanax;

import com.thinkaurelius.titan.StorageSetup;
import com.thinkaurelius.titan.diskstorage.cassandra.CassandraDaemonWrapper;
import com.thinkaurelius.titan.graphdb.TitanGraphConcurrentTest;
import com.thinkaurelius.titan.graphdb.TitanGraphTest;
import org.junit.BeforeClass;

public class InternalAstyanaxGraphConcurrentTest extends TitanGraphConcurrentTest {

	@BeforeClass
	public static void startCassandra() {
    	CassandraDaemonWrapper.start(StorageSetup.cassandraYamlPath);
	}

	public InternalAstyanaxGraphConcurrentTest() {
		super(StorageSetup.getAstyanaxGraphConfiguration());
	}

}