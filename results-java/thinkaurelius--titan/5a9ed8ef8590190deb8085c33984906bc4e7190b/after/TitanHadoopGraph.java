package com.thinkaurelius.titan.hadoop.formats.util;

import com.carrotsearch.hppc.cursors.LongObjectCursor;
import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.Entry;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.graphdb.database.RelationReader;
import com.thinkaurelius.titan.graphdb.internal.InternalRelationType;
import com.thinkaurelius.titan.graphdb.relations.RelationCache;
import com.thinkaurelius.titan.graphdb.types.TypeInspector;
import com.thinkaurelius.titan.hadoop.formats.util.input.SystemTypeInspector;
import com.thinkaurelius.titan.hadoop.formats.util.input.TitanHadoopSetup;
import com.thinkaurelius.titan.hadoop.formats.util.input.VertexReader;
import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerEdge;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class TitanHadoopGraph {

    private final TitanHadoopSetup setup;
    private final TypeInspector typeManager;
    private final SystemTypeInspector systemTypes;
    private final VertexReader vertexReader;
    private final boolean verifyVertexExistence = false;

    private static final Logger log =
            LoggerFactory.getLogger(TitanHadoopGraph.class);

    public TitanHadoopGraph(final TitanHadoopSetup setup) {
        this.setup = setup;
        this.typeManager = setup.getTypeInspector();
        this.systemTypes = setup.getSystemTypeInspector();
        this.vertexReader = setup.getVertexReader();
    }

    // Read a single row from the edgestore and create a TinkerVertex corresponding to the row
    // The neighboring vertices are represented by DetachedVertex instances
    protected TinkerVertex readHadoopVertex(final StaticBuffer key, Iterable<Entry> entries) {

        // Convert key to a vertex ID
        final long vertexId = vertexReader.getVertexId(key);
        Preconditions.checkArgument(vertexId > 0);

        // Create TinkerVertex
        TinkerGraph tg = TinkerGraph.open();

        boolean foundVertexState = !verifyVertexExistence;

        TinkerVertex tv = null;

        // Iterate over edgestore columns to find the vertex's label relation
        for (final Entry data : entries) {
            RelationReader relationReader = setup.getRelationReader(vertexId);
            final RelationCache relation = relationReader.parseRelation(data, false, typeManager);
            if (systemTypes.isVertexLabelSystemType(relation.typeId)) {
                // Found vertex Label
                long vertexLabelId = relation.getOtherVertexId();
                VertexLabel vl = typeManager.getExistingVertexLabel(vertexLabelId);
                // Create TinkerVertex with this label
                //tv = (TinkerVertex)tg.addVertex(T.label, vl.label(), T.id, vertexId);
                tv = getOrCreateVertex(vertexId, vl.name(), tg);
            }
        }

        // Added this following testing
        if (null == tv) {
            //tv = (TinkerVertex)tg.addVertex(T.id, vertexId);
            tv = getOrCreateVertex(vertexId, null, tg);
        }

        Preconditions.checkState(null != tv, "Unable to determine vertex label for vertex with ID %d", vertexId);

        // Iterate over and decode edgestore columns (relations) on this vertex
        for (final Entry data : entries) {
            try {
                RelationReader relationReader = setup.getRelationReader(vertexId);
                final RelationCache relation = relationReader.parseRelation(data, false, typeManager);
                if (systemTypes.isVertexExistsSystemType(relation.typeId)) {
                    foundVertexState = true;
                }

                if (systemTypes.isSystemType(relation.typeId)) continue; //Ignore system types
                final RelationType type = typeManager.getExistingRelationType(relation.typeId);
                if (((InternalRelationType)type).isInvisibleType()) continue; //Ignore hidden types

                // Decode and create the relation (edge or property)
                if (type.isPropertyKey()) {
                    // Decode property
                    Object value = relation.getValue();
                    Preconditions.checkNotNull(value);
                    // TODO set relation ID as a hidden property on the property
                    tv.property(type.name(), value);
//                    final StandardFaunusVertexProperty fprop = new StandardFaunusVertexProperty(relation.relationId, vertex, type.name(), value);
//                    vertex.addProperty(fprop);
//                    frel = fprop;
                } else {
                    TinkerEdge te;

                    // Decode edge
                    assert type.isEdgeLabel();
                    if (relation.direction.equals(Direction.IN)) {
                        // Calling DetachedVertex.addEdge throws UnsupportedOperationException
//                        DetachedVertex outV = new DetachedVertex(relation.getOtherVertexId(),
//                                Vertex.DEFAULT_LABEL /* We don't know the label of the other vertex, but one must be provided */,
//                                null /* No properties */,
//                                null /* No hidden properties */);
//                        // TODO set relation ID as a hidden property on the edge
//                        te = (TinkerEdge)outV.addEdge(type.name(), tv);

                        //TinkerVertex outV = (TinkerVertex)tg.addVertex(T.id, relation.getOtherVertexId()); // Default label is the best we can do
                        TinkerVertex outV = getOrCreateVertex(relation.getOtherVertexId(), null, tg);
                        te = (TinkerEdge)outV.addEdge(type.name(), tv);
                        //fedge = new StandardFaunusEdge(configuration, relation.relationId, relation.getOtherVertexId(), vertexId, type.name());
                    } else if (relation.direction.equals(Direction.OUT)) {
                        // Calling TinkerVertex.addEdge(label, DetachedVertex instance) throws a ClassCastException
//                        DetachedVertex inV = new DetachedVertex(relation.getOtherVertexId(),
//                                Vertex.DEFAULT_LABEL /* We don't know the label of the other vertex, but one must be provided */,
//                                null /* No properties */,
//                                null /* No hidden properties */);
//                        te = (TinkerEdge)tv.addEdge(type.name(), inV);
                        //TinkerVertex inV = (TinkerVertex)tg.addVertex(T.id, relation.getOtherVertexId());
                        TinkerVertex inV = getOrCreateVertex(relation.getOtherVertexId(), null, tg);
                        te = (TinkerEdge)tv.addEdge(type.name(), inV);
                        // TODO set relation ID as a hidden property on the edge
                        //fedge = new StandardFaunusEdge(configuration, relation.relationId, vertexId, relation.getOtherVertexId(), type.name());
                    } else {
                        throw new RuntimeException("Direction.BOTH is not supported");
                    }

                    if (relation.hasProperties()) {
                        // Load relation properties
                        for (final LongObjectCursor<Object> next : relation) {
                            assert next.value != null;
                            RelationType rt = typeManager.getExistingRelationType(next.key);
                            if (rt.isPropertyKey()) {
//                                PropertyKey pkey = (PropertyKey)vertex.getTypeManager().getPropertyKey(rt.name());
//                                log.debug("Retrieved key {} for name \"{}\"", pkey, rt.name());
//                                frel.property(pkey.label(), next.value);
                                te.property(rt.name(), next.value);
                            } else {
                                throw new RuntimeException("Metaedges are not supported");
//                                assert next.value instanceof Long;
//                                EdgeLabel el = (EdgeLabel)vertex.getTypeManager().getEdgeLabel(rt.name());
//                                log.debug("Retrieved ege label {} for name \"{}\"", el, rt.name());
//                                frel.setProperty(el, new FaunusVertex(configuration,(Long)next.value));
                            }
                        }
                    }
                }

//                // Iterate over and copy the relation's metaproperties
//                if (relation.hasProperties()) {
//                    // Load relation properties
//                    for (final LongObjectCursor<Object> next : relation) {
//                        assert next.value != null;
//                        RelationType rt = typeManager.getExistingRelationType(next.key);
//                        if (rt.isPropertyKey()) {
//                            PropertyKey pkey = (PropertyKey)vertex.getTypeManager().getPropertyKey(rt.name());
//                            log.debug("Retrieved key {} for name \"{}\"", pkey, rt.name());
//                            frel.property(pkey.label(), next.value);
//                        } else {
//                            assert next.value instanceof Long;
//                            EdgeLabel el = (EdgeLabel)vertex.getTypeManager().getEdgeLabel(rt.name());
//                            log.debug("Retrieved ege label {} for name \"{}\"", el, rt.name());
//                            frel.setProperty(el, new FaunusVertex(configuration,(Long)next.value));
//                        }
//                    }
//                    for (TitanRelation rel : frel.query().queryAll().relations())
//                        ((FaunusRelation)rel).setLifeCycle(ElementLifeCycle.Loaded);
//                }
//                frel.setLifeCycle(ElementLifeCycle.Loaded);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        //vertex.setLifeCycle(ElementLifeCycle.Loaded);

        /*Since we are filtering out system relation types, we might end up with vertices that have no incident relations.
         This is especially true for schema vertices. Those are filtered out.     */
        if (!foundVertexState) {
            log.trace("Vertex {} has unknown lifecycle state", vertexId);
            return null;
        } else if (!tv.edgeIterator(Direction.BOTH).hasNext() && !tv.propertyIterator().hasNext()) {
            log.trace("Vertex {} has no relations", vertexId);
            return null;
        }
        return tv;
    }

    public TinkerVertex getOrCreateVertex(final long vertexId, final String label, final TinkerGraph tg) {
        TinkerVertex v;

        try {
            v = (TinkerVertex)tg.v(vertexId);
        } catch (NoSuchElementException e) {
            if (null != label) {
                v = (TinkerVertex) tg.addVertex(T.label, label, T.id, vertexId);
            } else {
                v = (TinkerVertex) tg.addVertex(T.id, vertexId);
            }
        }

        return v;
    }

    public void close() {
        setup.close();
    }

}