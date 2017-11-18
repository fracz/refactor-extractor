package com.thinkaurelius.titan.graphdb.blueprints;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.InternalTitanGraph;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.edgetypes.TitanTypeClass;
import com.thinkaurelius.titan.graphdb.edgetypes.system.SystemKey;
import com.thinkaurelius.titan.graphdb.transaction.TransactionConfig;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class TitanBlueprintsGraph implements InternalTitanGraph {

    // ########## TRANSACTION HANDLING ###########################

    private final ThreadLocal<TitanTransaction> txs =  new ThreadLocal<TitanTransaction>() {

        protected TitanTransaction initialValue() {
            return null;
        }

    };

    private final WeakHashMap<TitanTransaction,Boolean> openTx = new WeakHashMap<TitanTransaction, Boolean>(4);

    @Override
    public void stopTransaction(final Conclusion conclusion) {
        TitanTransaction tx = txs.get();
        if (tx==null || tx.isClosed())
            throw new IllegalStateException("No running transaction.");
        tx.stopTransaction(conclusion);
        txs.remove();
        openTx.remove(tx);
    }

    private TitanTransaction internalStartTransaction() {
        TitanTransaction tx = (TitanTransaction) startThreadTransaction();
        txs.set(tx);
        openTx.put(tx,Boolean.TRUE);
        return tx;
    }

    @Override
    public void startTransaction() {
        getAutoStartTx();
    }

    private TitanTransaction getAutoStartTx() {
        TitanTransaction tx = txs.get();
        if (tx==null) {
            tx=internalStartTransaction();
        }
        return tx;
    }


    @Override
    public synchronized void shutdown() {
        for (TitanTransaction tx : openTx.keySet()) {
            tx.commit();
        }
        openTx.clear();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this,null);
    }


    // ########## INDEX HANDLING ###########################

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        throw new UnsupportedOperationException("Key indexes cannot be dropped.");
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(elementClass.equals(Vertex.class),"Only vertex indexing is supported");

        TitanTransaction tx = getAutoStartTx();
        if (tx.containsType(key)) {
            TitanType type = tx.getType(key);
            if (!type.isPropertyKey()) throw new IllegalArgumentException("Key does not denote a property key but a label!");
            if (!((TitanKey)type).hasIndex()) throw new UnsupportedOperationException("Need to define particular key as indexed before it is being used!");
        } else {
            tx.makeType().functional(false).name(key).dataType(Object.class).indexed().makePropertyKey();
        }
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        Preconditions.checkArgument(elementClass.equals(Vertex.class),"Only vertex indexing is supported");

        Set<String> indexedkeys = new HashSet<String>();
        TitanTransaction tx = getAutoStartTx();
        for (TitanVertex v : tx.getVertices(SystemKey.TypeClass, TitanTypeClass.KEY)) {
            assert v instanceof TitanKey;
            TitanKey k = (TitanKey)v;
            if (k.hasIndex()) indexedkeys.add(k.getName());
        }
        return indexedkeys;
    }

    // ########## FEATURES ###########################

    @Override
    public Features getFeatures() {
        Features features = TitanFeatures.getBaselineTitanFeatures();
        GraphDatabaseConfiguration config = ((StandardTitanGraph)this).getConfiguration();
        features.supportsSerializableObjectProperty = config.hasSerializeAll();
        return features;
    }

    // ########## TRANSACTIONAL FORWARDING ###########################

    @Override
    public Vertex addVertex(Object id) {
        return getAutoStartTx().addVertex(id);
    }

    @Override
    public Vertex getVertex(Object id) {
        return getAutoStartTx().getVertex(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        getAutoStartTx().removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return getAutoStartTx().getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return getAutoStartTx().getVertices(key,value);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return getAutoStartTx().addEdge(id,outVertex,inVertex,label);
    }

    @Override
    public Edge getEdge(Object id) {
        return getAutoStartTx().getEdge(id);
    }

    @Override
    public void removeEdge(Edge edge) {
        getAutoStartTx().removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return getAutoStartTx().getEdges();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return getAutoStartTx().getEdges(key,value);
    }

}