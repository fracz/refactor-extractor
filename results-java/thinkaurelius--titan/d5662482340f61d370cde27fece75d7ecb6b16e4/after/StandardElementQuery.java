package com.thinkaurelius.titan.graphdb.query;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.query.keycondition.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StandardElementQuery implements Query<StandardElementQuery> {

    public enum Type {
        VERTEX, EDGE;

        public Class<? extends Element> getElementType() {
            switch(this) {
                case VERTEX: return Vertex.class;
                case EDGE: return Edge.class;
                default: throw new IllegalArgumentException();
            }
        }
    }

    private final KeyCondition<TitanKey> condition;
    private final Type type;
    private final String index;

    public StandardElementQuery(Type type, KeyCondition<TitanKey> condition, String index) {
        Preconditions.checkNotNull(condition);
        Preconditions.checkNotNull(type);
        this.condition = condition;
        this.type=type;
        this.index = index;
    }

    public StandardElementQuery(StandardElementQuery query, String index) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(StringUtils.isNotBlank(index));
        this.condition=query.condition;
        this.type=query.type;
        this.index=index;
    }

    public KeyCondition<TitanKey> getCondition() {
        return condition;
    }

    public Type getType() {
        return type;
    }

    public boolean hasIndex() {
        return index!=null;
    }

    public String getIndex() {
        Preconditions.checkArgument(hasIndex());
        return index;
    }

    @Override
    public int hashCode() {
        return condition.hashCode()*9676463 + type.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this==other) return true;
        else if (other==null) return false;
        else if (!getClass().isInstance(other)) return false;
        StandardElementQuery oth = (StandardElementQuery)other;
        return type==oth.type && condition.equals(oth.condition);
    }

    @Override
    public String toString() {
        return "["+condition.toString()+"]:"+type.toString();
    }

    @Override
    public boolean hasLimit() {
        return false;
    }

    @Override
    public int getLimit() {
        return Query.NO_LIMIT;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public Comparator getSortOrder() {
        return new ComparableComparator();
    }

    @Override
    public boolean hasUniqueResults() {
        return true;
    }

    public boolean matches(TitanElement element) {
        return matchesCondition(element,condition);
    }

    public static final<T extends TitanType> boolean matchesCondition(TitanElement element, KeyCondition<T> condition) {
        if (condition instanceof KeyAtom) {
            KeyAtom<T> atom = (KeyAtom<T>) condition;
            Object value = null;
            T type = atom.getKey();
            if (type.isPropertyKey()) value = element.getProperty((TitanKey)type);
            else value = ((TitanRelation)element).getProperty((TitanLabel)type);
            return atom.getRelation().satisfiesCondition(value,atom.getCondition());
        } else if (condition instanceof KeyNot) {
            return !matchesCondition(element, ((KeyNot) condition).getChild());
        } else if (condition instanceof KeyAnd) {
            for (KeyCondition c : ((KeyAnd<T>)condition).getChildren()) {
                if (!matchesCondition(element, c)) return false;
            }
            return true;
        } else if (condition instanceof KeyOr) {
            if (!condition.hasChildren()) return true;
            for (KeyCondition c : ((KeyOr<T>)condition).getChildren()) {
                if (matchesCondition(element, c)) return true;
            }
            return false;
        } else throw new IllegalArgumentException("Invalid condition: " + condition);
    }

}