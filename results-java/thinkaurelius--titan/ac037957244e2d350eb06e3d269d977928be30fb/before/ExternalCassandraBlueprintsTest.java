package com.thinkaurelius.titan.blueprints;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.diskstorage.cassandra.CassandraLocalhostHelper;
import com.thinkaurelius.titan.diskstorage.cassandra.CassandraThriftStorageManager;
import com.tinkerpop.blueprints.Graph;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class ExternalCassandraBlueprintsTest extends LocalBlueprintsTest {

    public static CassandraLocalhostHelper ch = new CassandraLocalhostHelper();

    @Override
    public void startUp() {
        ch.startCassandra();
    }

    @Override
    public void shutDown() {
        ch.stopCassandra();
    }

    @Override
    public Graph generateGraph() {
        Graph graph = TitanFactory.open(ch.getConfiguration());
        return graph;
    }

    @Override
    public void cleanUp() {
        CassandraThriftStorageManager.dropKeyspace(
                CassandraThriftStorageManager.KEYSPACE_DEFAULT,
                "127.0.0.1",
                CassandraThriftStorageManager.PORT_DEFAULT);
    }


}