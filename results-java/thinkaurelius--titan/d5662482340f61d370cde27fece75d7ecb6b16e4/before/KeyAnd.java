package com.thinkaurelius.titan.graphdb.query.keycondition;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Arrays;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class KeyAnd<K> implements KeyCondition<K> {

    private final KeyCondition<K>[] elements;

    public KeyAnd(KeyCondition<K>... elements) {
        Preconditions.checkNotNull(elements);
        Preconditions.checkArgument(elements.length>0);
        for (int i=0;i<elements.length;i++) Preconditions.checkNotNull(elements[i]);
        this.elements=elements;
    }

    public int size() {
        return elements.length;
    }

    public KeyCondition<K> get(int position) {
        return elements[position];
    }

    @Override
    public Iterable<KeyCondition<K>> getChildren() {
        return Arrays.asList(elements);
    }

    @Override
    public Type getType() {
        return Type.AND;
    }

    @Override
    public int hashCode() {
        int sum = 0;
        for (KeyCondition kp : elements) sum+=kp.hashCode();
        return new HashCodeBuilder().append(getType()).append(sum).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this==other) return true;
        else if (other==null) return false;
        else if (!getClass().isInstance(other)) return false;
        KeyAnd oth = (KeyAnd)other;
        if (elements.length!=oth.elements.length) return false;
        for (int i=0;i<elements.length;i++) {
            boolean foundEqual = false;
            for (int j=0;j<elements.length;j++) {
                if (elements[i].equals(oth.elements[(i+j)%elements.length])) {
                    foundEqual=true;
                    break;
                }
            }
            if (!foundEqual) return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("(");
        for (int i=0;i<elements.length;i++) {
            if (i>0) b.append(" AND ");
            b.append(elements);
        }
        b.append(")");
        return b.toString();
    }

    public static final<K> KeyAnd<K> of(KeyCondition<K>... elements) {
        return new KeyAnd<K>(elements);
    }

}