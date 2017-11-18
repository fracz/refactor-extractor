package com.thinkaurelius.titan.graphdb.relations;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.core.RelationType;
import com.thinkaurelius.titan.core.TitanElement;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalRelationType;
import com.tinkerpop.gremlin.structure.Element;

import java.util.NoSuchElementException;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class SimpleTitanProperty<V> implements TitanProperty<V> {

    private final InternalRelationType type;
    private final V value;
    private final InternalRelation relation;

    public SimpleTitanProperty(InternalRelation relation, RelationType type, V value) {
        this.type = (InternalRelationType)type;
        this.value = value;
        this.relation = relation;
    }

    @Override
    public RelationType getType() {
        return type;
    }

    @Override
    public V value() throws NoSuchElementException {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isHidden() {
        return type.isHiddenType();
    }

    @Override
    public <E extends Element> E getElement() {
        return (E) relation;
    }

    @Override
    public void remove() {
        Preconditions.checkArgument(!relation.isRemoved(), "Cannot modified removed relation");
        relation.it().removePropertyDirect(type);
    }
}