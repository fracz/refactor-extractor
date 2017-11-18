package com.thinkaurelius.titan.graphdb.fulgora;

import com.google.common.base.Predicate;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.EntryList;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.internal.ElementLifeCycle;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.query.vertex.VertexCentricQueryBuilder;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.system.ImplicitKey;
import com.thinkaurelius.titan.graphdb.types.system.SystemRelationType;
import com.thinkaurelius.titan.graphdb.types.system.SystemTypeManager;
import com.thinkaurelius.titan.util.datastructures.Retriever;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class FulgoraNeighborVertex implements InternalVertex, Vertex.Iterators {

    private final long id;
    private final FulgoraExecutor executor;

    public FulgoraNeighborVertex(long id, FulgoraExecutor executor) {
        this.id = id;
        this.executor = executor;
    }

    private static UnsupportedOperationException getAccessException() {
        return new UnsupportedOperationException();
    }


    @Override
    public<A> A getProperty(String key) {
        if (key.equals(executor.stateKey)) {
            return (A)executor.getVertexState(getLongId());
        }
        SystemRelationType t = SystemTypeManager.getSystemType(key);
        if (t!=null && t instanceof ImplicitKey) return ((ImplicitKey)t).computeProperty(this);
        throw getAccessException();
    }

    @Override
    public <O> O getProperty(PropertyKey key) {
        if (key instanceof ImplicitKey) return ((ImplicitKey)key).computeProperty(this);
        throw getAccessException();
    }

    @Override
    public String getLabel() {
        return getVertexLabel().getName();
    }

    @Override
    public VertexLabel getVertexLabel() {
        throw getAccessException();
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public Object getId() {
        return getLongId();
    }

    @Override
    public long getLongId() {
        return id;
    }

    @Override
    public boolean hasId() {
        return true;
    }


    @Override
    public Set<String> getPropertyKeys() {
        throw getAccessException();
    }

    @Override
    public <O> O removeProperty(String key) {
        throw getAccessException();
    }

    @Override
    public <O> O removeProperty(RelationType type) {
        throw getAccessException();
    }

    @Override
    public void remove() {
        throw getAccessException();
    }

    @Override
    public void setProperty(String key, Object value) {
        throw getAccessException();
    }

    @Override
    public void setProperty(PropertyKey key, Object value) {
        throw getAccessException();
    }

    @Override
    public InternalVertex it() {
        return this;
    }

    @Override
    public StandardTitanTx tx() {
        return executor.tx();
    }

    @Override
    public void setId(long id) {
        throw getAccessException();
    }

    @Override
    public byte getLifeCycle() {
        return ElementLifeCycle.Loaded;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void removeRelation(InternalRelation e) {
        throw getAccessException();
    }

    @Override
    public boolean addRelation(InternalRelation e) {
        throw getAccessException();
    }

    @Override
    public List<InternalRelation> getAddedRelations(Predicate<InternalRelation> query) {
        throw getAccessException();
    }

    @Override
    public EntryList loadRelations(SliceQuery query, Retriever<SliceQuery, EntryList> lookup) {
        throw getAccessException();
    }

    @Override
    public boolean hasLoadedRelations(SliceQuery query) {
        throw getAccessException();
    }

    @Override
    public boolean hasRemovedRelations() {
        return false;
    }

    @Override
    public boolean hasAddedRelations() {
        return false;
    }

    @Override
    public TitanEdge addEdge(EdgeLabel label, TitanVertex vertex) {
        throw getAccessException();
    }

    @Override
    public TitanEdge addEdge(String label, TitanVertex vertex) {
        throw getAccessException();
    }

    @Override
    public TitanVertexProperty addProperty(PropertyKey key, Object attribute) {
        throw getAccessException();
    }

    @Override
    public TitanVertexProperty addProperty(String key, Object attribute) {
        throw getAccessException();
    }

    @Override
    public VertexCentricQueryBuilder query() {
        throw getAccessException();
    }

    @Override
    public Edge addEdge(String s, Vertex vertex, Object... keyValues) {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanVertexProperty> getProperties() {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanVertexProperty> getProperties(PropertyKey key) {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanVertexProperty> getProperties(String key) {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanEdge> getTitanEdges(Direction d, EdgeLabel... labels) {
        throw getAccessException();
    }

    @Override
    public Iterable<Edge> getEdges(Direction d, String... labels) {
        throw getAccessException();
    }

    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... strings) {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanEdge> getEdges() {
        throw getAccessException();
    }

    @Override
    public Iterable<TitanRelation> getRelations() {
        throw getAccessException();
    }

    @Override
    public long getEdgeCount() {
        throw getAccessException();
    }

    @Override
    public long getPropertyCount() {
        throw getAccessException();
    }

    @Override
    public boolean isConnected() {
        throw getAccessException();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    /* ---------------------------------------------------------------
	 * TinkPop Iterators Method
	 * ---------------------------------------------------------------
	 */

    @Override
    public Vertex.Iterators iterators() {
        return this;
    }

    @Override
    public Iterator<Edge> edgeIterator(Direction direction, int i, String... strings) {
        throw getAccessException();
    }

    @Override
    public Iterator<Vertex> vertexIterator(Direction direction, int i, String... strings) {
        throw getAccessException();
    }

    @Override
    public <V> Iterator<VertexProperty<V>> propertyIterator(String... strings) {
        throw getAccessException();
    }

    @Override
    public <V> Iterator<VertexProperty<V>> hiddenPropertyIterator(String... strings) {
        throw getAccessException();
    }
}