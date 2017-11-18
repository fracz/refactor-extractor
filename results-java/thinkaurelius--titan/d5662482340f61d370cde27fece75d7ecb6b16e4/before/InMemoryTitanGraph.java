package com.thinkaurelius.titan.graphdb.transaction;

import cern.colt.list.AbstractLongList;
import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanLabel;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.RecordIterator;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;
import com.thinkaurelius.titan.graphdb.blueprints.TitanFeatures;
import com.thinkaurelius.titan.graphdb.database.InternalTitanGraph;
import com.thinkaurelius.titan.graphdb.idmanagement.IDInspector;
import com.thinkaurelius.titan.graphdb.idmanagement.IDManager;
import com.thinkaurelius.titan.graphdb.query.AtomicQuery;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.relations.factory.StandardPersistedRelationFactory;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.vertices.factory.StandardVertexFactories;
import com.tinkerpop.blueprints.Features;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTitanGraph extends AbstractTitanTx implements InternalTitanGraph {

    private AtomicInteger idCounter;
    private final IDManager idManager;

    public InMemoryTitanGraph(TransactionConfig config) {
        super(null, StandardVertexFactories.DefaultInMemory, new StandardPersistedRelationFactory(), new InMemoryTypeManager(), config);
        idCounter = new AtomicInteger(0);
        idManager = new IDManager(1, 1);
        graphdb = this;
    }

    @Override
    public Features getFeatures() {
        return TitanFeatures.getInMemoryFeatures();
    }


    @Override
    public boolean isDeletedRelation(InternalRelation relation) {
        return relation.isRemoved();
    }

    @Override
    public boolean isDeletedRelation(long relationId) {
        throw new UnsupportedOperationException();
    }

    public boolean isReferenceVertexID(long vertexid) {
        return false;
    }

    @Override
    public boolean hasModifications() {
        return true;
    }

    @Override
    public void deletedRelation(InternalRelation relation) {
        super.deletedRelation(relation);
    }


    @Override
    public void addedRelation(InternalRelation relation) {
        super.addedRelation(relation);

    }

    @Override
    public void loadedRelation(InternalRelation relation) {
        super.loadedRelation(relation);
    }

    @Override
    public long[] getVertexIDsFromDisk(TitanKey type, Object attribute) {
        return new long[0];
    }


    @Override
    public void loadRelations(AtomicQuery query) {
        throw new UnsupportedOperationException("InMemory Transactions do not support edge loading");
    }

    @Override
    public AbstractLongList getRawNeighborhood(AtomicQuery query) {
        throw new UnsupportedOperationException("InMemory Transactions do not support disk retrieval");
    }

    @Override
    public StoreTransaction getTxHandle() {
        throw new UnsupportedOperationException("InMemory Transactions do not have transaction handles");
    }

    @Override
    public synchronized void commit() {
        //Simply ignore
    }

    @Override
    public synchronized void rollback() {
        throw new UnsupportedOperationException("Cannot rollback in memory graph transaction");
    }


	/* ---------------------------------------------------------------
     * InMemory InternalTitanGraph
	 * ---------------------------------------------------------------
	 */

    @Override
    public void shutdown() throws TitanException {
        //Nothing to do;
    }


    @Override
    public InternalTitanTransaction startTransaction(TransactionConfig config) {
        Preconditions.checkArgument(!getTxConfiguration().equals(config), "Cannot open in-memory transactions with non-default configuration.");
        return this;
    }

    @Override
    public TitanTransaction newTransaction() {
        return this;
    }

    @Override
    public boolean containsVertexID(long id, InternalTitanTransaction tx) {
        return false;
    }

    @Override
    public void assignID(InternalVertex vertex) {
        int id = idCounter.incrementAndGet();
        long newid = -1;
        if (vertex instanceof InternalRelation) {
            newid = idManager.getEdgeID(id, 0);
        } else if (vertex instanceof TitanKey) {
            newid = idManager.getPropertyTypeID(id, 0, 0);
        } else if (vertex instanceof TitanLabel) {
            newid = idManager.getRelationshipTypeID(id, 0, 0);
        } else {
            newid = idManager.getNodeID(id, 0);
        }
        assert newid > 0;
        vertex.setID(newid);
    }

	/* ---------------------------------------------------------------
	 * Unsupported InternalTitanGraph Methods
	 * ---------------------------------------------------------------
	 */

    @Override
    public IDInspector getIDInspector() {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }

    @Override
    public AbstractLongList getRawNeighborhood(AtomicQuery query,
                                               InternalTitanTransaction tx) {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }


    @Override
    public long[] indexRetrieval(Object attribute, TitanKey key, InternalTitanTransaction tx) {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }

    @Override
    public RecordIterator<Long> getVertexIDs(InternalTitanTransaction tx) {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }


    @Override
    public void loadRelations(AtomicQuery query, InternalTitanTransaction tx) {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }

    @Override
    public void save(Collection<InternalRelation> addedRelations,
                     Collection<InternalRelation> deletedRelations, InternalTitanTransaction tx)
            throws StorageException {
        throw new UnsupportedOperationException("Not supported for in-memory graph databases");
    }

}