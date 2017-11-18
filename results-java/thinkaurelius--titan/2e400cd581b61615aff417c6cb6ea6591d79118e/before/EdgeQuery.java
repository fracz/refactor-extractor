
package com.thinkaurelius.titan.core;

import java.util.Iterator;

/**
 * EdgeQuery constructs and executes a query over incident edges for a fixed node.
 *
 * Using EdgeQuery consists of two parts:
 * <ol>
 * <li>Defining the query by specifying conditions on the edges to retrieve via the methods {@link #withEdgeType(EdgeType...)},
 * {@link #inDirection(com.thinkaurelius.titan.core.Direction)}, {@link #onlyModifiable()}.</li>
 * <li>Executing the query by asking for an {@link Iterable}, {@link java.util.Iterator}, or getting the number of edges that match the query</li>
 * </ol>
 *
 * It is important to note, that EdgeQuery only queries the edges incident on a fixed node that was specified when the EdgeQuery was
 * instantiated (e.g. through the transaction's {@link GraphTransaction#makeEdgeQuery(long)} method).
 *
 *
 * @author	Matthias Br&ouml;cheler (me@matthiasb.com);
 *
 *
 *
 *
 */
public interface EdgeQuery {

	/**
	 * Defines this edge query to query only for edges of the specified type.
	 * By default, an edge query includes all edges in the result set.
	 *
	 * @param type Edge type to query for
	 * @return This edge query
	 */
	public EdgeQuery withEdgeType(EdgeType... type);

    /**
     * Defines this edge query to query only for edges of the specified type.
     * By default, an edge query includes all edges in the result set.
     *
     * @param type Edge type to query for
     * @return This edge query
     */
    public EdgeQuery withEdgeType(String... type);

	/**
	 * Defines this edge query to query only for edges which type belongs to the specified group.
	 * Note that an EdgeTypeGroup is uniquely defined by its id. By default, all groups are considered.
	 *
	 * @param group EdgeTypeGroup to query edges for
	 * @return This edge query
	 * @see EdgeTypeGroup
	 */
	public EdgeQuery withEdgeTypeGroup(EdgeTypeGroup group);

	/**
	 * Defines this edge query to query only for edges in the given direction
	 *
	 * @param d Direction to query for
	 * @return This edge query
	 */
	public EdgeQuery inDirection(Direction d);

    /**
     * Defines this edge query to query only for edges that have an attached (i.e. incident)
     * property of the given type with the specified value.
     *
     * @param ptype Property type
     * @param value Value for the property of the given type to match
     * @return This edge query
     */
    public EdgeQuery withProperty(PropertyType ptype, Object value);

    /**
     * Defines this edge query to query only for edges that have an attached (i.e. incident)
     * property of the given type with the specified value.
     *
     * @param ptype Property type
     * @param value Value for the property of the given type to match
     * @return This edge query
     */
    public EdgeQuery withProperty(String ptype, Object value);

	/**
	 * Defines this edge query to query only for modifiable edges.
     * By default, an edge query considers all edges
	 *
	 * @return This edge query
	 */
	public EdgeQuery onlyModifiable();

    /**
     * Configures the edge query such that all outgoing edges are first loaded
     * into memory.
     *
     * By default, an implementation of EdgeQuery chooses the most efficient way to retrieve a node's edge.
     * For the case of a neighborhood query, the most efficient way is retrieving the neighborhood directly from the index
     * without loading any edges into memory. However, when repeatedly querying the same or similar node neighborhoods,
     * first loading the edges into memory and using them to answer subsequent queries can be more efficient.
     * Calling this method signals such cases to the EdgeQuery implementation.
     *
     * By default, the in memory retrieval flag is false.
     *
     * @return This edge query
     */
    public EdgeQuery inMemoryRetrieval();

    /**
     * Restricts the retrieved relationships to those which have an id and that id does not exceed the specified id.
     *
     * Otherwise, it behaves exactly like inMemoryRetrieval().
     *
     * @param maxRelationshipID maximum id of relationships to be retrieved.
     * @return This edge query
     *
     * @see #inMemoryRetrieval()
     */
    public EdgeQuery inMemoryRetrieval(long maxRelationshipID);

    /**
     * Sets the retrieval limit for this query.
     *
     * When setting a limit, executing this query will only retrieve the specified number of edges from memory.
     * The boolean flag defines the behavior when the limit is exceeded: Either nothing is returned to indicate
     * that retrieving the full neighborhood has failed or just the limited number of edges/neighbors is returned.
     *
     * @param limit Maximum number of edges to retrieve for this edge query
     * @param partialResult If true, invoking {@link #getNeighborhood()} or {@link #getNeighborhoodIDs()} will
     * only retrieve at most <i>limit</i> elements. If false, {@link NodeList#LimitExceeded} is returned
     * when the limit is exceeded.
     * @return This edge query
     */
    public EdgeQuery setRetrievalLimit(long limit, boolean partialResult);


	/**
	 * Returns an iterable over all incident relationships that match this query
	 *
	 * @return Iterable over all incident relationships that match this query
	 */
	public Iterable<Relationship> getRelationships();

	/**
	 * Returns an iterator over all incident relationships that match this query
	 *
	 * @return Iterator over all incident relationships that match this query
	 */
	public Iterator<Relationship> getRelationshipIterator();

	/**
	 * Returns an iterable over all incident properties that match this query
	 *
	 * @return Iterable over all incident properties that match this query
	 */
	public Iterable<Property> getProperties();

	/**
	 * Returns an iterator over all incident properties that match this query
	 *
	 * @return Iterator over all incident properties that match this query
	 */
	public Iterator<Property> getPropertyIterator();

	/**
	 * Returns an iterable over all incident edges that match this query
	 *
	 * @return Iterable over all incident edges that match this query
	 */
	public Iterable<Edge> getEdges();

	/**
	 * Returns an iterator over all incident edges that match this query
	 *
	 * @return Iterator over all incident edges that match this query
	 */
	public Iterator<Edge> getEdgeIterator();

	/**
	 * Returns the number of relationships that match this query
	 *
	 * @return Number of relationships that match this query
	 */
	public int noRelationships();

	/**
	 * Returns the number of properties that match this query
	 *
	 * @return Number of properties that match this query
	 */
	public int noProperties();

    /**
     * Retrieves the ids of all nodes connected to this query's fixed node by edges
     * matching the conditions defined in this query.
     *
     * Depending on the implementation, this method can be considerably faster than calling
     * {@link #getNeighborhood()}. Also, in some cases the retrieved list of node ids will be
     * ordered (use {@link NodeList#isSorted()} to check whether the list is sorted).
     *
     * @return A list of all node ids connected to this query's fixed node by matching edges
     * @throws com.thinkaurelius.titan.exceptions.InvalidNodeException if the center node or any of the neighboring nodes does not have an id.
     */
    public NodeIDList getNeighborhoodIDs();

    /**
     * Retrieves all nodes connected to this query's fixed node by edges
     * matching the conditions defined in this query.
     *
     * No guarantee is made as to the order in which the nodes are listed.
     *
     * @return A list of all nodes connected to this query's fixed node by matching edges
     */
    public NodeList getNeighborhood();

}