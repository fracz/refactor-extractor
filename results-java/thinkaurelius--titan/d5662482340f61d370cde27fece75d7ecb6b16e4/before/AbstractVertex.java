package com.thinkaurelius.titan.graphdb.vertices;


import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.internal.AbstractElement;
import com.thinkaurelius.titan.graphdb.internal.ElementLifeCycle;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.query.SimpleAtomicQuery;
import com.thinkaurelius.titan.graphdb.query.VertexQueryBuilder;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.system.SystemKey;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.*;

public abstract class AbstractVertex extends AbstractElement implements InternalVertex {

    private final StandardTitanTx tx;


    protected AbstractVertex(StandardTitanTx tx, long id) {
        super(id);
        Preconditions.checkNotNull(tx);
        this.tx = tx;
    }

    @Override
    public final InternalVertex it() {
        if (this.isRemoved()) throw new UnsupportedOperationException("Cannot access removed vertex");
        else if (!tx.isClosed()) return this;
        else return (InternalVertex)tx.getNextTx().getVertex(getID());
    }

    @Override
    public final StandardTitanTx tx() {
        if (this.isRemoved()) throw new UnsupportedOperationException("Cannot access removed vertex");
        else if (!tx.isClosed()) return tx;
        else return tx.getNextTx();
    }

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }

    @Override
    public Object getId() {
        if (hasId()) return Long.valueOf(getID());
        else return null;
    }

    @Override
    public boolean isModified() {
        return ElementLifeCycle.isModified(it().getLifeCycle());
    }


	/* ---------------------------------------------------------------
     * Changing Edges
	 * ---------------------------------------------------------------
	 */

    @Override
    public synchronized void remove() {
        for (TitanRelation r : SimpleAtomicQuery.queryAll(it()).relations()) {
            if (r.getType().equals(SystemKey.VertexState)) r.remove();
            else throw new IllegalStateException("Cannot remove node since it is still connected");
        }
    }

	/* ---------------------------------------------------------------
	 * TitanRelation Iteration/Access
	 * ---------------------------------------------------------------
	 */

    @Override
    public VertexQueryBuilder query() {
        return tx().query(it());
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> result = new HashSet<String>();
        for (TitanProperty p : getProperties()) {
            result.add(p.getPropertyKey().getName());
        }
        return result;
    }

    @Override
    public Object getProperty(TitanKey key) {
        Iterator<TitanProperty> iter = query().type(key).propertyIterator();
        if (key.isUnique(Direction.OUT)) {
            if (iter.hasNext()) return iter.next().getValue();
            else return null;
        } else {
            List<Object> result = new ArrayList<Object>();
            while (iter.hasNext()) {
                result.add(iter.next().getValue());
            }
            return result;
        }
    }

    @Override
    public Object getProperty(String key) {
        if (!tx().containsType(key)) return null;
        else return getProperty(tx().getPropertyKey(key));
    }

    @Override
    public <O> O getProperty(TitanKey key, Class<O> clazz) {
        Object result = getProperty(key);
        return clazz.cast(result);
    }

    @Override
    public <O> O getProperty(String key, Class<O> clazz) {
        if (!tx().containsType(key)) return null;
        else return getProperty(tx().getPropertyKey(key), clazz);
    }

    @Override
    public Iterable<TitanProperty> getProperties() {
        return query().properties();
    }

    @Override
    public Iterable<TitanProperty> getProperties(TitanKey key) {
        return query().type(key).properties();
    }

    @Override
    public Iterable<TitanProperty> getProperties(String key) {
        return query().keys(key).properties();
    }


    @Override
    public Iterable<TitanEdge> getEdges() {
        return query().titanEdges();
    }


    @Override
    public Iterable<TitanEdge> getTitanEdges(Direction dir, TitanLabel... labels) {
        return query().direction(dir).types(labels).titanEdges();
    }

    @Override
    public Iterable<Edge> getEdges(Direction dir, String... labels) {
        return query().direction(dir).labels(labels).edges();
    }

    @Override
    public Iterable<TitanRelation> getRelations() {
        return query().relations();
    }

    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return query().direction(direction).labels(labels).vertices();
    }


	/* ---------------------------------------------------------------
	 * TitanRelation Counts
	 * ---------------------------------------------------------------
	 */


    @Override
    public long getPropertyCount() {
        return query().propertyCount();
    }


    @Override
    public long getEdgeCount() {
        return query().count();
    }

    @Override
    public boolean isConnected() {
        return !Iterables.isEmpty(getEdges());
    }


	/* ---------------------------------------------------------------
	 * Convenience Methods for TitanElement Creation
	 * ---------------------------------------------------------------
	 */

    @Override
    public TitanProperty addProperty(TitanKey key, Object attribute) {
        return tx().addProperty(it(), key, attribute);
    }


    @Override
    public TitanProperty addProperty(String key, Object attribute) {
        return tx().addProperty(it(), key, attribute);
    }

    @Override
    public void setProperty(String key, Object value) {
        setProperty(tx().getPropertyKey(key),value);
    }

    @Override
    public void setProperty(final TitanKey key, Object value) {
        tx().setProperty(it(),key,value);
    }


    @Override
    public TitanEdge addEdge(TitanLabel label, TitanVertex vertex) {
        return tx().addEdge(it(), vertex, label);
    }

    @Override
    public TitanEdge addEdge(String label, TitanVertex vertex) {
        return tx().addEdge(it(), vertex, label);
    }

    @Override
    public Edge addEdge(String label, Vertex vertex) {
        return addEdge(label,(TitanVertex)vertex);
    }

    @Override
    public Object removeProperty(TitanType key) {
        Preconditions.checkArgument(key.isPropertyKey());
        Preconditions.checkArgument(key.isUnique(Direction.OUT));


        Object result = null;
        Iterator<TitanProperty> iter = query().type(key).propertyIterator();
        while (iter.hasNext()) {
            TitanProperty p = iter.next();
            result = p.getValue();
            p.remove();
        }
        return result;
    }

    @Override
    public Object removeProperty(String key) {
        if (!tx().containsType(key)) return null;
        else return removeProperty(tx().getPropertyKey(key));
    }

}