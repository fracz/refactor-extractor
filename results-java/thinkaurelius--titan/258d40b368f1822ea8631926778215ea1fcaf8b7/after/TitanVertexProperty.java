
package com.thinkaurelius.titan.core;

import com.tinkerpop.gremlin.structure.VertexProperty;

/**
 * TitanProperty is a {@link TitanRelation} connecting a vertex to a value.
 * TitanProperty extends {@link TitanRelation}, with methods for retrieving the property's value and key.
 *
 * @author Matthias Br&ouml;cheler (me@matthiasb.com);
 * @see TitanRelation
 * @see PropertyKey
 */
public interface TitanVertexProperty<V> extends TitanRelation, VertexProperty<V>, TitanProperty<V> {

    /**
     * Returns the vertex on which this property is incident.
     *
     * @return The vertex of this property.
     */
    @Override
    public TitanVertex getElement();

    @Override
    public default PropertyKey getType() {
        return getPropertyKey();
    }

    public PropertyKey getPropertyKey();

    /**
     * Returns the value of this property (possibly cast to the expected type).
     *
     * @return value of this property
     * @throws ClassCastException if the value cannot be cast to the expected type
     */
    public<O> O getValue();

    public default V value() {
        return getValue();
    }

}