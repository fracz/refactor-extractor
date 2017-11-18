package com.thinkaurelius.titan.graphdb.berkeleyje;

import com.thinkaurelius.titan.StorageSetup;
import com.thinkaurelius.titan.diskstorage.berkeleydb.je.BerkeleyJEHelper;
import com.thinkaurelius.titan.graphdb.TitanGraphPerformanceTest;

public class BerkeleyJEGraphPerformanceTest extends TitanGraphPerformanceTest {

	public BerkeleyJEGraphPerformanceTest() {
		super(StorageSetup.getBerkeleyJEGraphConfiguration(),0,1,false);
	}

    @Override
    public void cleanUp() {
        BerkeleyJEHelper.clearEnvironment(StorageSetup.getHomeDirFile());
    }
}