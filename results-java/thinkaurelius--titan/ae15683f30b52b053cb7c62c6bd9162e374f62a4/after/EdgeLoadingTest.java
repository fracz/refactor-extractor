package com.thinkaurelius.titan.graphdb.loading;

import com.thinkaurelius.titan.graphdb.loadingstatus.DefaultLoadingStatus;
import com.thinkaurelius.titan.graphdb.loadingstatus.LoadingStatus;
import com.thinkaurelius.titan.graphdb.query.SimpleAtomicQuery;
import com.thinkaurelius.titan.graphdb.transaction.InMemoryTitanGraph;
import com.thinkaurelius.titan.graphdb.transaction.TransactionConfig;
import com.thinkaurelius.titan.graphdb.types.system.SystemLabel;
import com.tinkerpop.blueprints.Direction;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EdgeLoadingTest {

    @Test
    public void edgeLoadingTest() {
        InMemoryTitanGraph tx = new InMemoryTitanGraph(new TransactionConfig());

        LoadingStatus status1 = DefaultLoadingStatus.AllLoaded;
        LoadingStatus status2 = DefaultLoadingStatus.NothingLoaded;
        SimpleAtomicQuery q = new SimpleAtomicQuery(tx, 1);
        assertTrue(status1.hasLoadedEdges(q));
        assertFalse(status2.hasLoadedEdges(q));
        assertTrue(status1 == status1.loadedEdges(q));
        assertFalse(status2 == status2.loadedEdges(q));

        LoadingStatus status = status2;
        assertTrue(status.loadedEdges(q).hasLoadedEdges(q));
        q.direction(Direction.OUT);
        assertFalse(status.hasLoadedEdges(q));
        status2 = status.loadedEdges(q);
        assertTrue(status2.hasLoadedEdges(q));
        q.type(SystemLabel.TYPE);
        assertTrue(status2.hasLoadedEdges(q));
        assertFalse(status.hasLoadedEdges(q));
    }

}