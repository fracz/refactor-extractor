package com.thinkaurelius.titan.graphdb.blueprints;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.relations.RelationIdentifier;
import com.thinkaurelius.titan.graphdb.types.IndexType;
import com.thinkaurelius.titan.graphdb.types.TitanTypeClass;
import com.thinkaurelius.titan.graphdb.types.system.SystemKey;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class TitanBlueprintsTransaction implements TitanTransaction {

    private static final Logger log =
            LoggerFactory.getLogger(TitanBlueprintsTransaction.class);


    @Override
    public void stopTransaction(Conclusion conclusion) {
        switch (conclusion) {
            case SUCCESS:
                commit();
                break;
            case FAILURE:
                rollback();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized conclusion: " + conclusion);
        }
    }

    @Override
    public Features getFeatures() {
        throw new UnsupportedOperationException("Not supported threaded transaction graph. Call on parent graph");
    }

    @Override
    public Vertex addVertex(Object id) {
        //Preconditions.checkArgument(id==null,"Titan does not support vertex id assignment");
        return addVertex();
    }

    @Override
    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        if (id instanceof Vertex) //allows vertices to be "re-attached" to the current transaction
            return getVertex(((Vertex) id).getId());

        final long longId;
        if (id instanceof Long) {
            longId = (Long) id;
        } else if (id instanceof Number) {
            longId = ((Number) id).longValue();
        } else {
            try {
                longId = Long.parseLong(id.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (longId <= 0)
            return null;
        else
            return getVertex(longId);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        TitanVertex v = (TitanVertex) vertex;
        //Delete all edges
        Iterator<TitanRelation> iter = v.getRelations().iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        v.remove();
    }


    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        //Preconditions.checkArgument(id==null,"Titan does not support edge id assignment");
        Preconditions.checkArgument(outVertex instanceof TitanVertex);
        Preconditions.checkArgument(inVertex instanceof TitanVertex);
        return addEdge((TitanVertex) outVertex, (TitanVertex) inVertex, label);
    }

    @Override
    public Edge getEdge(Object id) {
        if (id == null) throw ExceptionFactory.edgeIdCanNotBeNull();
        RelationIdentifier rid = null;
        if (id instanceof RelationIdentifier) rid = (RelationIdentifier) id;
        else if (id instanceof String) rid = RelationIdentifier.parse((String) id);
        else if (id instanceof long[]) rid = RelationIdentifier.get((long[]) id);
        else if (id instanceof int[]) rid = RelationIdentifier.get((int[]) id);
        else if (id instanceof Long) log.warn("");

        if (rid != null) return rid.findEdge(this);
        else return null;
    }

    @Override
    public void removeEdge(Edge edge) {
        ((TitanEdge) edge).remove();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
    }

    @Override
    public void shutdown() {
        commit();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, null);
    }

    // ########## INDEX HANDLING ###########################

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        throw new UnsupportedOperationException("Key indexes cannot be dropped");
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(elementClass==Element.class || elementClass==Vertex.class || elementClass==Edge.class,
                "Expected vertex, edge or element");

        if (indexParameters==null || indexParameters.length==0) {
            indexParameters = new Parameter[]{new Parameter(IndexType.STANDARD,true)};
        }

        if (containsType(key)) {
            TitanType type = getType(key);
            if (!type.isPropertyKey())
                throw new IllegalArgumentException("Key string does not denote a property key but a label");
            List<String> indexes = new ArrayList<String>(indexParameters.length);
            for (Parameter p : indexParameters) {
                Preconditions.checkArgument(p.getKey() instanceof String,"Invalid index argument: " + p);
                indexes.add((String) p.getKey());
            }
            boolean indexesCovered;
            if (elementClass==Element.class) {
                indexesCovered = hasIndexes((TitanKey)type,Vertex.class,indexes) &&
                        hasIndexes((TitanKey)type,Edge.class,indexes);
            } else {
                indexesCovered = hasIndexes((TitanKey)type,elementClass,indexes);
            }
            if (!indexesCovered)
                throw new UnsupportedOperationException("Cannot add an index to an already existing property key: " + type.getName());
        } else {
            TypeMaker tm = makeType().functional(false).name(key).dataType(Object.class);
            for (Parameter p : indexParameters) {
                Preconditions.checkArgument(p.getKey() instanceof String,"Invalid index argument: " + p);
                tm.indexed((String) p.getKey(),elementClass);
            }
            tm.makePropertyKey();
        }
    }

    private static final boolean hasIndexes(TitanKey key, Class<? extends Element> elementClass, List<String> indexes) {
        for (String index : indexes) {
            if (!Iterables.contains(key.getIndexes(elementClass),index)) return false;
        }
        return true;
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        Preconditions.checkArgument(elementClass==Vertex.class || elementClass==Edge.class, "Must provide either Vertex.class or Edge.class as an argument");

        Set<String> indexedkeys = new HashSet<String>();
        for (TitanVertex v : getVertices(SystemKey.TypeClass, TitanTypeClass.KEY)) {
            TitanKey k = (TitanKey) v;
            if (k.hasIndex(elementClass)) indexedkeys.add(k.getName());
        }
        return indexedkeys;
    }
}