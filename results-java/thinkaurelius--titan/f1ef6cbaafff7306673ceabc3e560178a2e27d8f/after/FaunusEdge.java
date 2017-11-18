package com.thinkaurelius.faunus;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FaunusEdge extends FaunusElement implements Edge {

    protected long outVertex;
    protected long inVertex;
    private FaunusType label;

    public FaunusEdge() {
        this(false);
    }

    public FaunusEdge(final boolean enablePaths) {
        super(-1l);
        this.label = FaunusType.LINK;
        this.enablePath(enablePaths);
    }

    public FaunusEdge(final DataInput in) throws IOException {
        super(-1l);
        this.readFields(in);
    }

    public FaunusEdge(final long outVertex, final long inVertex, final String label) {
        this(-1l, outVertex, inVertex, label);
    }

    public FaunusEdge(final long id, final long outVertex, final long inVertex, final String label) {
        super(id);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        setLabel(label);
    }

    public FaunusEdge reuse(final long id, final long outVertex, final long inVertex, final String label) {
        super.reuse(id);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.setLabel(label);
        return this;
    }

    @Override
    void updateSchema(FaunusSerializer.Schema schema) {
        super.updateSchema(schema);
        schema.add(label);
    }

    @Override
    public Vertex getVertex(final Direction direction) {
        if (OUT.equals(direction)) {
            return new FaunusVertex(this.outVertex);
        } else if (IN.equals(direction)) {
            return new FaunusVertex(this.inVertex);
        } else {
            throw ExceptionFactory.bothIsNotSupported();
        }
    }

    public long getVertexId(final Direction direction) {
        if (OUT.equals(direction)) {
            return this.outVertex;
        } else if (IN.equals(direction)) {
            return this.inVertex;
        } else {
            throw ExceptionFactory.bothIsNotSupported();
        }
    }

    @Override
    public String getLabel() {
        return label.getName();
    }

    public FaunusType getType() {
        return label;
    }

    final void setLabel(FaunusType label) {
        Preconditions.checkNotNull(label);
        this.label = label;
    }

    final void setLabel(String label) {
        setLabel(FaunusType.DEFAULT_MANAGER.get(label));
    }

    //##################################
    // Serialization Proxy
    //##################################

    public void write(final DataOutput out) throws IOException {
        FaunusSerializer.DEFAULT_SERIALIZER.writeEdge(this, out);
    }

    public void readFields(final DataInput in) throws IOException {
        FaunusSerializer.DEFAULT_SERIALIZER.readEdge(this, in);

    }

    //##################################
    // General Utility
    //##################################

    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }


    public static class MicroEdge extends MicroElement {

        private static final String E1 = "e[";
        private static final String E2 = "]";

        public MicroEdge(final long id) {
            super(id);
        }

        public String toString() {
            return E1 + this.id + E2;
        }
    }
}