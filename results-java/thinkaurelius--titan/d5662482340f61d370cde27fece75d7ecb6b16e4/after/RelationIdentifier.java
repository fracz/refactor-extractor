package com.thinkaurelius.titan.graphdb.relations;

import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.util.encoding.LongEncoding;
import com.tinkerpop.blueprints.Direction;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public final class RelationIdentifier {

    public static final String TOSTRING_DELIMITER = ":";

    private final long outVertexId;
    private final long typeId;
    private final long relationId;

    private RelationIdentifier(final long outVertexId, final long typeId, final long relationId) {
        this.outVertexId = outVertexId;
        this.typeId = typeId;
        this.relationId = relationId;
    }

    static final RelationIdentifier get(InternalRelation r) {
        if (r.hasId()) {
            return new RelationIdentifier(r.getVertex(0).getID(),
                    r.getType().getID(),
                    r.getID());
        } else return null;
    }

    static final RelationIdentifier get(TitanProperty property) {
        if (property.hasId()) {
            return new RelationIdentifier(property.getVertex().getID(),
                    property.getPropertyKey().getID(),
                    property.getID());
        } else return null;
    }

    static final RelationIdentifier get(TitanEdge edge) {
        if (edge.hasId()) {
            return new RelationIdentifier(edge.getVertex(Direction.OUT).getID(),
                    edge.getTitanLabel().getID(),
                    edge.getID());
        } else return null;
    }

    public long[] getLongRepresentation() {
        long[] r = new long[3];
        r[0]=outVertexId;
        r[1]=typeId;
        r[2]=relationId;
        return r;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(relationId).hashCode() +
                Long.valueOf(outVertexId).hashCode() * 743 +
                Long.valueOf(typeId).hashCode() * 3011;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        else if (!getClass().isInstance(other)) return false;
        RelationIdentifier oth = (RelationIdentifier) other;
        return relationId == oth.relationId && outVertexId == oth.outVertexId && typeId == oth.typeId;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(LongEncoding.encode(relationId)).append(TOSTRING_DELIMITER).append(LongEncoding.encode(outVertexId))
                .append(TOSTRING_DELIMITER).append(LongEncoding.encode(typeId));
        return s.toString();
    }

    TitanRelation findRelation(TitanTransaction tx) {
        TitanVertex v = tx.getVertex(outVertexId);
        if (v == null) return null;
        TitanVertex type = tx.getVertex(typeId);
        if (type == null) return null;
        if (!(type instanceof TitanType))
            throw new IllegalArgumentException("Invalid RelationIdentifier: typeID does not reference a type");
        for (TitanRelation r : v.query().types((TitanType)type).direction(Direction.OUT).relations()) {
            if (r.getID() == relationId) return r;
        }
        return null;
    }

    public TitanEdge findEdge(TitanTransaction tx) {
        TitanRelation r = findRelation(tx);
        if (r==null) return null;
        else if (r instanceof TitanEdge) return (TitanEdge)r;
        else throw new UnsupportedOperationException("Referenced relation is a property not an edge");
    }

    public TitanProperty findProperty(TitanTransaction tx) {
        TitanRelation r = findRelation(tx);
        if (r==null) return null;
        else if (r instanceof TitanProperty) return (TitanProperty)r;
        else throw new UnsupportedOperationException("Referenced relation is a edge not a property");
    }

    public static final RelationIdentifier parse(String id) {
        String[] elements = id.split(TOSTRING_DELIMITER);
        if (elements.length != 3) return null;
        try {
            return new RelationIdentifier(LongEncoding.decode(elements[1]),
                    LongEncoding.decode(elements[2]),
                    LongEncoding.decode(elements[0]));
        } catch (NumberFormatException e) {
            //throw new IllegalArgumentException("Invalid id - each token expected to be a number",e);
            return null;
        }
    }

    public static final RelationIdentifier get(long[] ids) {
        if (ids.length != 3) return null;
        for (int i = 0; i < 3; i++) {
            //Preconditions.checkArgument(idAuthorities[i]>=0,"Non-negative numbers expected");
            if (ids[i] < 0) return null;
        }
        return new RelationIdentifier(ids[0], ids[1], ids[2]);
    }

    public static final RelationIdentifier get(int[] ids) {
        if (ids.length != 3) return null;
        for (int i = 0; i < 3; i++) {
            //Preconditions.checkArgument(idAuthorities[i]>=0,"Non-negative numbers expected");
            if (ids[i] < 0) return null;
        }
        return new RelationIdentifier(ids[0], ids[1], ids[2]);
    }

}