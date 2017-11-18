package com.thinkaurelius.titan.graphdb.internal;

/**
 * Constants to specify the ordering of a result set in queries.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public enum Order {

    /**
     * Increasing
     */
    ASC,
    /**
     * Decreasing
     */
    DESC;

    /**
     * Modulates the result of a {@link Comparable#compareTo(Object)} execution for this specific
     * order, i.e. it negates the result if the order is {@link #DESC}.
     *
     * @param compare
     * @return
     */
    public int modulateNaturalOrder(int compare) {
        switch (this) {
            case ASC:
                return compare;
            case DESC:
                return -compare;
            default:
                throw new AssertionError("Unrecognized order: " + this);
        }
    }

    /**
     * The default order when none is specified
     */
    public static final Order DEFAULT = ASC;

    public com.tinkerpop.gremlin.structure.Order getTP() {
        switch (this) {
            case ASC :return com.tinkerpop.gremlin.structure.Order.incr;
            case DESC: return com.tinkerpop.gremlin.structure.Order.decr;
            default: throw new AssertionError();
        }
    }

    public static Order convert(com.tinkerpop.gremlin.structure.Order order) {
        switch(order) {
            case incr: return ASC;
            case decr: return DESC;
            default: throw new AssertionError();
        }
    }

}