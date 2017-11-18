package com.thinkaurelius.titan.graphdb.edgequery;

/**
 * A PointInterval is a {@link Interval} with identical start and end points.
 *
 * In other words, the attribute values defining the end points of the interval are identical and both are inclusive.
 *
 * @author	Matthias Br&ouml;cheler (me@matthiasb.com);
 *
 *
 * @param <V> Type of attribute for which the point-interval is defined.
 */
class PointInterval<V> implements Interval<V> {

	private final V value;

	/**
	 * Constructs a new PointInterval for the given attribute value.
	 *
	 * @param value Attribute defining the point-interval
	 */
	public PointInterval(V value) {
		this.value=value;
	}

	@Override
	public boolean isPoint() {
		return true;
	}

	@Override
	public boolean isRange() {
		return false;
	}

	@Override
	public V getEndPoint() {
		return value;
	}

	@Override
	public V getStartPoint() {
		return value;
	}

	@Override
	public boolean endInclusive() {
		return true;
	}

	@Override
	public boolean startInclusive() {
		return true;
	}

	/**
	 * Constructs a new PointInterval for the given value
	 *
	 * @param <V> Type of PointInterval
	 * @param value Value for the constructed point interval
	 * @return PointInterval for given value
	 */
	public static final<V> PointInterval<V> of(V value) {
		return new PointInterval<V>(value);
	}

	@Override
	public boolean inInterval(Object obj) {
		return value.equals(obj);
	}

}