package com.thinkaurelius.titan.graphdb.relations;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.internal.AbstractElement;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalRelationType;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.system.ImplicitKey;
import com.tinkerpop.gremlin.structure.*;
import com.tinkerpop.gremlin.util.StreamFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractTypedRelation extends AbstractElement implements InternalRelation, Edge.Iterators, VertexProperty.Iterators {

    protected final InternalRelationType type;

    public AbstractTypedRelation(final long id, final RelationType type) {
        super(id);
        assert type != null && type instanceof InternalRelationType;
        this.type = (InternalRelationType) type;
    }

    @Override
    public InternalRelation it() {
        InternalVertex v = getVertex(0);
        if (v == v.it())
            return this;

        InternalRelation next = (InternalRelation) RelationIdentifier.get(this).findRelation(tx());
        if (next == null)
            throw new InvalidElementException("Relation has been removed", this);

        return next;
    }

    @Override
    public final StandardTitanTx tx() {
        return getVertex(0).tx();
    }

	/* ---------------------------------------------------------------
	 * Immutable Aspects of Relation
	 * ---------------------------------------------------------------
	 */

    @Override
    public Direction getDirection(TitanVertex vertex) {
        for (int i=0;i<getArity();i++) {
            if (it().getVertex(i).equals(vertex)) return EdgeDirection.fromPosition(i);
        }
        throw new IllegalArgumentException("Relation is not incident on vertex");
    }

    @Override
    public boolean isIncidentOn(TitanVertex vertex) {
        for (int i=0;i<getArity();i++) {
            if (it().getVertex(i).equals(vertex)) return true;
        }
        return false;
    }

    @Override
    public boolean isHidden() {
        return type.isHiddenType();
    }

    @Override
    public boolean isLoop() {
        return getArity()==2 && getVertex(0).equals(getVertex(1));
    }

    @Override
    public RelationType getType() {
        return type;
    }

    @Override
    public RelationIdentifier getId() {
        return RelationIdentifier.get(this);
    }

    /* ---------------------------------------------------------------
	 * Mutable Aspects of Relation
	 * ---------------------------------------------------------------
	 */

    @Override
    public <O> O removeProperty(String key) {
        if (!tx().containsRelationType(key)) return null;
        else return removeProperty(tx().getRelationType(key));
    }

    @Override
    public <O> O removeProperty(RelationType type) {
        Preconditions.checkArgument(!it().isRemoved(),"Cannot modified removed relation");
        return it().removePropertyDirect(type);
    }

    @Override
    public void setProperty(EdgeLabel label, TitanVertex vertex) {
        Preconditions.checkArgument(!it().isRemoved(),"Cannot modified removed relation");
        Preconditions.checkArgument(label.isUnidirected(),"Label must be unidirected");
        Preconditions.checkArgument(vertex!=null,"Vertex cannot be null");
        it().setPropertyDirect(label,vertex);
    }

    @Override
    public void setProperty(PropertyKey key, Object value) {
        Preconditions.checkArgument(!it().isRemoved(),"Cannot modified removed relation");
        it().setPropertyDirect(key,tx().verifyAttribute(key,value));
    }

    @Override
    public <V> Property<V> property(final String key, final V value) {
        setProperty(key,value);
        return new SimpleTitanProperty<V>(this,tx().getRelationType(key),value);
    }

    @Override
    public void setProperty(String key, Object value) {
        RelationType type = tx().getRelationType(key);
        if (type instanceof PropertyKey) setProperty((PropertyKey)type,value);
        else if (type instanceof EdgeLabel) {
            Preconditions.checkArgument(value instanceof TitanVertex,"Value must be a vertex");
            setProperty((EdgeLabel) type, (InternalVertex) value);
        } else if (type==null) {
            if (value instanceof TitanVertex) setProperty(tx().getOrCreateEdgeLabel(key),(TitanVertex)value);
            setProperty(tx().getOrCreatePropertyKey(key),value);
        } else throw new IllegalArgumentException("Invalid key argument: " + key);
    }

    @Override
    public <O> O getProperty(PropertyKey key) {
        if (key instanceof ImplicitKey) return ((ImplicitKey)key).computeProperty(this);
        return it().getPropertyDirect(key);
    }

    @Override
    public <O> O getProperty(String key) {
        RelationType type = tx().getRelationType(key);
        if (type==null) return null;
        else if (type.isPropertyKey()) return getProperty((PropertyKey) type);
        else return (O)getProperty((EdgeLabel)type);
    }

    @Override
    public TitanVertex getProperty(EdgeLabel label) {
        Object val = it().getPropertyDirect(label);
        if (val==null) return null;
        else if (val instanceof Number) return tx().getInternalVertex(((Number) val).longValue());
        else if (val instanceof TitanVertex) return (TitanVertex) val;
        else throw new IllegalStateException("Invalid object found instead of vertex: " + val);
    }

    @Override
    public <O> O getProperty(RelationType type) {
        if (type.isEdgeLabel()) return (O)getProperty((EdgeLabel)type);
        else return getProperty((PropertyKey)type);
    }


    /* ---------------------------------------------------------------
	 * Blueprints Iterators
	 * ---------------------------------------------------------------
	 */

    @Override
    public Iterator<Vertex> vertexIterator(Direction direction) {
        List<Vertex> vertices;
        if (direction==Direction.BOTH) {
            vertices = ImmutableList.of((Vertex)getVertex(0),getVertex(1));
        } else {
            vertices = ImmutableList.of((Vertex)getVertex(EdgeDirection.position(direction)));
        }
        return vertices.iterator();
    }

    public <V> Iterator<Property<V>> propertyIterator(boolean hidden, String... strings) {
        Iterator<RelationType> keys;
        if (strings==null || strings.length==0) {
            keys = getPropertyKeysDirect().iterator();
        } else {
            keys = Arrays.asList(strings).stream().map(s -> tx().getRelationType(s)).filter(rt -> rt!=null).iterator();
        }
        return StreamFactory.stream(keys)
                .filter( relationType -> hidden ^ !((InternalRelationType)relationType).isHiddenType())
                .map( relationType -> (Property<V>)new SimpleTitanProperty<V>(this,relationType,getProperty(relationType))).iterator();
    }

    @Override
    public <V> Iterator<Property<V>> propertyIterator(String... strings) {
        return propertyIterator(false,strings);
    }

    @Override
    public <V> Iterator<Property<V>> hiddenPropertyIterator(String... strings) {
        return propertyIterator(true,strings);
    }





}