package com.thinkaurelius.titan.hadoop;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.hadoop.mapreduce.util.EmptyConfiguration;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import org.apache.hadoop.conf.Configuration;

import javax.annotation.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.tinkerpop.blueprints.Direction.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class FaunusVertex extends FaunusPathElement implements TitanVertex {

    private FaunusVertexLabel vertexLabel = FaunusVertexLabel.DEFAULT_VERTEXLABEL;

    public FaunusVertex() {
        super(EmptyConfiguration.immutable(), -1l);
    }

    public FaunusVertex(final Configuration configuration) {
        super(configuration, -1l);
    }

    public FaunusVertex(final Configuration configuration, final long id) {
        super(configuration, id);
    }

    public FaunusVertex(final Configuration configuration, final DataInput in) throws IOException {
        super(configuration, -1l);
        this.readFields(in);
    }

    public void addAll(final FaunusVertex vertex) {
        this.id = vertex.getLongId();
        this.inAdjacency = vertex.inAdjacency;
        this.outAdjacency = vertex.outAdjacency;
        this.getPaths(vertex, false);
        this.lifecycle = vertex.getLifeCycle();
        this.addEdges(BOTH, vertex);
    }

    @Override
    void updateSchema(final FaunusSerializer.Schema schema) {
        super.updateSchema(schema);
        for (TitanRelation edge : query().queryAll().relations()) {
            assert edge instanceof StandardFaunusRelation;
            ((StandardFaunusRelation)edge).updateSchema(schema);
        }
    }

    //##################################
    // Vertex Label
    //##################################


    public void setVertexLabel(FaunusVertexLabel label) {
        this.vertexLabel = label;
    }

    @Override
    public String getLabel() {
        return vertexLabel.getName();
    }

    @Override
    public FaunusVertexLabel getVertexLabel() {
        return vertexLabel;
    }



    //##################################
    // Property Handling
    //##################################

    @Override
    public void setProperty(final FaunusRelationType type, final Object value) {
        Preconditions.checkArgument(type.isPropertyKey(),"Expected property key: "+type);
        super.setRelation(new StandardFaunusProperty(this, (FaunusPropertyKey) type, value));
    }

    public FaunusProperty addProperty(FaunusProperty property) {
        return (FaunusProperty)super.addRelation(property);
    }

    @Override
    public TitanProperty addProperty(PropertyKey key, Object value) {
        Preconditions.checkArgument(key instanceof FaunusProperty);
        return addProperty(new StandardFaunusProperty(this,(FaunusPropertyKey)key,value));
    }

    public FaunusProperty addProperty(final String key, final Object value) {
        FaunusPropertyKey type = getTypeManager().getPropertyKey(key);
        return (FaunusProperty)addProperty(type,value);
    }

    @Override
    public Iterable<TitanProperty> getProperties() {
        return query().properties();
    }

    @Override
    public Iterable<TitanProperty> getProperties(PropertyKey key) {
        return query().type(key).properties();
    }

    @Override
    public Iterable<TitanProperty> getProperties(String key) {
        return query().keys(key).properties();
    }

    @Override
    public long getPropertyCount() {
        return Iterables.size(getProperties());
    }

    public <T> Iterable<T> getPropertyValues(final String key) {
        FaunusPropertyKey type = getTypeManager().getPropertyKey(key);
        return Iterables.transform(query().type(type).properties(),new Function<TitanProperty, T>() {
            @Nullable
            @Override
            public T apply(@Nullable TitanProperty prop) {
                return prop.getValue();
            }
        });
    }

    //##################################
    // Edge Handling
    //##################################

    //================READING

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return query().direction(direction).labels(labels).vertices();
    }

    @Override
    public Iterable<TitanEdge> getTitanEdges(Direction d, EdgeLabel... labels) {
        return query().direction(d).types(labels).titanEdges();
    }

    @Override
    public Iterable<Edge> getEdges(final Direction direction, String... labels) {
        return query().direction(direction).labels(labels).edges();
    }

    @Override
    public Iterable<TitanEdge> getEdges() {
        return query().titanEdges();
    }

    @Override
    public Iterable<TitanRelation> getRelations() {
        return query().relations();
    }

    @Override
    public long getEdgeCount() {
        return query().count();
    }

    @Override
    public boolean isConnected() {
        return !Iterables.isEmpty(getEdges());
    }

    public Set<FaunusEdgeLabel> getEdgeLabels(final Direction direction) {
        return Sets.newHashSet(Iterables.transform(query().titanEdges(),new Function<TitanEdge, FaunusEdgeLabel>() {
            @Nullable
            @Override
            public FaunusEdgeLabel apply(@Nullable TitanEdge edge) {
                return (FaunusEdgeLabel)edge.getType();
            }
        }));
    }

    //================ADDING


    private void addEdges(final List<StandardFaunusEdge> edges) {
        for (FaunusEdge edge : edges) super.addRelation(edge);
    }

    public StandardFaunusEdge addEdge(Direction dir, final StandardFaunusEdge edge) {
        return addEdge(edge);
    }

    public StandardFaunusEdge addEdge(final StandardFaunusEdge edge) {
        edge.setConf(getConf());
        return (StandardFaunusEdge)super.addRelation(edge);
    }

    public void addEdges(final Direction direction, final FaunusVertex vertex) {
        for (TitanEdge edge : vertex.query().direction(direction).titanEdges()) {
            addEdge((StandardFaunusEdge)edge);
        }
    }

    public FaunusEdge addEdge(final String label, final Vertex inVertex) {
        return addEdge(label, (TitanVertex) inVertex);
    }

    public FaunusEdge addEdge(final Direction direction, final String label, final long otherVertexId) {
        if (direction == OUT)
            return this.addEdge(new StandardFaunusEdge(getConf(), getLongId(), otherVertexId, label));
        else if (direction == Direction.IN)
            return this.addEdge(new StandardFaunusEdge(getConf(), otherVertexId, getLongId(), label));
        else
            throw ExceptionFactory.bothIsNotSupported();
    }

    @Override
    public FaunusEdge addEdge(EdgeLabel label, TitanVertex vertex) {
        return addEdge(new StandardFaunusEdge(getConf(),getLongId(),vertex.getLongId(),(FaunusEdgeLabel)label));
    }

    @Override
    public FaunusEdge addEdge(String label, TitanVertex vertex) {
        return addEdge(getTypeManager().getEdgeLabel(label),vertex);
    }

    //================Modifiying

    public void removeEdgesToFrom(final Set<Long> ids) {
        for (Iterator<TitanEdge> iterator = query().titanEdges().iterator(); iterator.hasNext(); ) {
            TitanEdge next =  iterator.next();
            if (ids.contains(next.getOtherVertex(this).getLongId())) {
                iterator.remove();
            }
        }
    }

    private void removeAllEdges(final Direction dir, FaunusRelationType... types) {
        for (Iterator iterator = query().direction(dir).types(types).titanEdges().iterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }
    }

    public void removeEdges(final Tokens.Action action, final Direction direction, final String... stringLabels) {
        FaunusEdgeLabel labels[] = new FaunusEdgeLabel[stringLabels.length];
        for (int i = 0; i < stringLabels.length; i++) {
            labels[i]=getTypeManager().getEdgeLabel(stringLabels[i]);
        }

        if (action.equals(Tokens.Action.KEEP)) {
            for (Direction dir : Direction.proper) {
                if (direction == BOTH || direction == dir) {
                    if (labels.length > 0) {
                        Set<FaunusEdgeLabel> removal = getEdgeLabels(dir);
                        removal.removeAll(Arrays.asList(labels));
                        removeAllEdges(dir, removal.toArray(new FaunusRelationType[0]));
                    } else if (direction == dir) //drop all in opposite direction
                        removeAllEdges(dir.opposite(), getEdgeLabels(dir.opposite()).toArray(new FaunusRelationType[0]));
                }
            }
        } else {
            assert action.equals(Tokens.Action.DROP);
            if (labels.length==0) //drop all
                removeAllEdges(direction, getEdgeLabels(direction).toArray(new FaunusRelationType[0]));
            else
                removeAllEdges(direction, labels);
        }
    }

    //##################################
    // Serialization Proxy
    //##################################

    public void write(final DataOutput out) throws IOException {
        new FaunusSerializer(this.getConf()).writeVertex(this, out);
    }

    public void readFields(final DataInput in) throws IOException {
        new FaunusSerializer(this.getConf()).readVertex(this, in);
    }

    //##################################
    // General Utility
    //##################################

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }

    public static class MicroVertex extends MicroElement {

        private static final String V1 = "v[";
        private static final String V2 = "]";

        public MicroVertex(final long id) {
            super(id);
        }

        public String toString() {
            return V1 + this.id + V2;
        }
    }
}