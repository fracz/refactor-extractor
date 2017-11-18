package com.thinkaurelius.titan.graphdb.transaction;

import cern.colt.list.AbstractLongList;
import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.exceptions.GraphStorageException;
import com.thinkaurelius.titan.graphdb.database.GraphDB;
import com.thinkaurelius.titan.graphdb.edgequery.InternalEdgeQuery;
import com.thinkaurelius.titan.graphdb.edges.InternalEdge;
import com.thinkaurelius.titan.graphdb.edges.factory.InMemoryEdgeFactory;
import com.thinkaurelius.titan.graphdb.edgetypes.manager.InMemoryEdgeTypeManager;
import com.thinkaurelius.titan.graphdb.idmanagement.IDInspector;
import com.thinkaurelius.titan.graphdb.idmanagement.IDManager;
import com.thinkaurelius.titan.graphdb.vertices.InternalNode;
import com.thinkaurelius.titan.graphdb.vertices.factory.StandardNodeFactories;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryGraphDB extends AbstractGraphTx implements GraphDB {

    private AtomicInteger idCounter;
    private final IDManager idManager;

    public InMemoryGraphDB() {
		super(null,StandardNodeFactories.DefaultInMemory, new InMemoryEdgeFactory(), new InMemoryEdgeTypeManager(), GraphTransactionConfig.AutoEdgeTypes);
        idCounter=new AtomicInteger(0);
        idManager = new IDManager(1,1);
        graphdb=this;
	}


	@Override
	public boolean isDeletedEdge(InternalEdge e) {
		return e.isDeleted();
	}


	public boolean isReferenceNodeID(long nodeID) {
		return false;
	}

	@Override
	public boolean containsNode(long id) {
		return false;
	}

	@Override
	public boolean hasModifications() {
		return true;
	}

	@Override
	public Node getNode(long id) {
		return getExistingNode(id);
	}

	@Override
	public InternalNode getExistingNode(long id) {
		throw new IllegalArgumentException("Node with given ID does not exist: " + id);
	}


	@Override
	public void deletedEdge(InternalEdge edge) {
		super.deletedEdge(edge);
	}


	@Override
	public void addedEdge(InternalEdge edge) {
		super.addedEdge(edge);

	}

	@Override
	public void loadedEdge(InternalEdge edge) {
		super.loadedEdge(edge);
	}

	@Override
	public long[] getNodeIDsByAttributeFromDisk(PropertyType type, Object attribute) {
		return new long[0];
	}


	@Override
	public void loadEdges(InternalEdgeQuery query) {
		throw new UnsupportedOperationException("InMemory Transactions do not support edge loading!");
	}

	@Override
	public AbstractLongList getRawNeighborhood(InternalEdgeQuery query) {
		throw new UnsupportedOperationException("InMemory Transactions do not support disk retrieval!");
	}

	@Override
	public TransactionHandle getTxHandle() {
		throw new UnsupportedOperationException("InMemory Transactions do not have transaction handles!");
	}

    @Override
    public synchronized void rollingCommit() {
        super.rollingCommit();
    }

	@Override
	public synchronized void commit() {
		super.commit();
	}

	@Override
	public synchronized void abort() {
		super.abort();
	}


	/* ---------------------------------------------------------------
	 * InMemory GraphDB
	 * ---------------------------------------------------------------
	 */

	@Override
	public void close() throws GraphStorageException {
		//Nothing to do;
	}


	@Override
	public GraphTx startTransaction(GraphTransactionConfig config) {
		Preconditions.checkArgument(!getTxConfiguration().equals(config),"Cannot open in-memory transactions with non-default configuration.");
		return this;
	}

	@Override
	public GraphTransaction startTransaction() {
		return this;
	}

	@Override
	public boolean containsNodeID(long id, GraphTx tx) {
		return false;
	}

    @Override
    public void assignID(InternalNode node) {
        int id = idCounter.incrementAndGet();
        long newid = -1;
        if (node instanceof InternalEdge) {
            newid = idManager.getEdgeID(id);
        } else if (node instanceof PropertyType) {
            newid = idManager.getPropertyTypeID(id,0,0);
        } else if (node instanceof RelationshipType) {
            newid = idManager.getRelationshipTypeID(id,0,0);
        } else {
            newid = idManager.getNodeID(id,0);
        }
        assert newid>0;
        node.setID(newid);
    }

    @Override
	public GraphDatabaseConfiguration getConfiguration() {
		return null;
	}


	/* ---------------------------------------------------------------
	 * Unsupported GraphDB Methods
	 * ---------------------------------------------------------------
	 */

	@Override
	public IDInspector getIDInspector() {
		throw new UnsupportedOperationException("Not supported for in-memory graph databases.");
	}

	@Override
	public AbstractLongList getRawNeighborhood(InternalEdgeQuery query,
			GraphTx tx) {
		throw new UnsupportedOperationException("Not supported for in-memory graph databases.");
	}


	@Override
	public long[] indexRetrieval(Object attribute, PropertyType pt, GraphTx tx) {
		throw new UnsupportedOperationException("Not supported for in-memory graph databases.");
	}


	@Override
	public void loadEdges(InternalEdgeQuery query, GraphTx tx) {
		throw new UnsupportedOperationException("Not supported for in-memory graph databases.");
	}

	@Override
	public boolean save(Collection<InternalEdge> addedEdges,
			Collection<InternalEdge> deletedEdges, GraphTx tx)
			throws GraphStorageException {
		throw new UnsupportedOperationException("Not supported for in-memory graph databases.");
	}



}