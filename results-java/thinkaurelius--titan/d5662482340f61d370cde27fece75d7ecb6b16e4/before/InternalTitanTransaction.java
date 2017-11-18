package com.thinkaurelius.titan.graphdb.transaction;

import cern.colt.list.AbstractLongList;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanQuery;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.graphdb.query.AtomicQuery;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;

public interface InternalTitanTransaction extends TitanTransaction {

    /**
     * Verifies that this transaction is open and that all given vertices are contained in this
     * transaction and available. Throws {@link IllegalArgumentException} if this is not the case.
     *
     * @param vertices
     */
    void verifyAccess(TitanVertex... vertices);

    /**
     * Returns the storage backend transaction handle issued to this transaction
     *
     * @return The storage backend transaction handle issued to this transaction
     */
    TransactionHandle getTxHandle();

    /**
     * Returns the configuration used by this graph transaction
     *
     * @return The configuration for this transaction
     */
    public TransactionConfig getTxConfiguration();

    /**
     * Starts a new edge query on the specified node
     *
     * @param n TitanVertex for which to create a new edge query
     * @return New edge query
     */
    TitanQuery query(InternalVertex n);


    // ######## TitanVertex / TitanRelation Loading  ############

    /**
     * Called to load all edges into the transaction that are needed to answer the provided edge query
     *
     * @param query TitanRelation query for which to load all edges
     */
    void loadRelations(AtomicQuery query);

    /**
     * Retrieves the node idAuthorities for the neighboring vertices addresed in the given neighborhood query
     *
     * @param query Neighborhood query for which to retrieve neighboring node idAuthorities
     * @return TitanVertex idAuthorities of neighbors
     */
    AbstractLongList getRawNeighborhood(AtomicQuery query);

    /**
     * Notifies the transaction that the specified edge has been deleted so
     * that the change can be persisted.
     *
     * @param relation Deleted edge
     */
    void deletedRelation(InternalRelation relation);

    /**
     * Notifies the transaction that the specified edge has been added so
     * that the change can be persisted.
     * <p/>
     * This method automatically calls {@link #loadedRelation}.
     *
     * @param relation Added edge
     */
    void addedRelation(InternalRelation relation);

    /**
     * Notifies the transaction that the specified edge has been loaded from disk
     * into this transaction so that any affected in-memory data structures can be
     * updated.
     *
     * @param relation Loaded edge
     */
    void loadedRelation(InternalRelation relation);

    /**
     * Checks whether the specified edge has been deleted in this transaction
     *
     * @param relation TitanRelation to check for deletion
     * @return true, if the edge has been deleted, else false
     */
    public boolean isDeletedRelation(InternalRelation relation);

    public boolean isDeletedRelation(long relationId);

    public boolean isDeletedVertex(long vertexId);

    // ######## TitanVertex / TitanRelation Loading  ############

    /**
     * Returns the node for an existing node id
     *
     * @param id TitanVertex id
     * @return TitanVertex associated with the specified id
     */
    InternalVertex getExistingVertex(long id);

    /**
     * Whenever a new entity gets created within the current transaction,
     * it has to be registered with the transaction using this method.
     *
     * @param n Newly created entity
     */
    void registerNewEntity(InternalVertex n);


    /**
     * Deletes vertices from transaction.
     *
     * @param n Deleted node
     */
    public void deleteVertex(InternalVertex n);

    /**
     * Retrieves all idAuthorities for vertices which have an incident property of the given type with the specified attribute value
     * <p/>
     * The given property key must have an hasIndex defined for this retrieval to succeed.
     *
     * @param type      Property key for which to retrieve vertices
     * @param attribute Attribute value for which to retrieve vertices
     * @return All idAuthorities for vertices which have an incident property of the given type with the specified attribute value
     * @throws IllegalArgumentException if the property type is not indexed.
     * @see com.thinkaurelius.titan.util.interval.AtomicInterval
     */
    public long[] getVertexIDsFromDisk(TitanKey type, Object attribute);


}