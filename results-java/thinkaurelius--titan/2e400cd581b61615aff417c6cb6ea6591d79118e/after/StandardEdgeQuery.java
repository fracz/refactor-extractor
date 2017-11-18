package com.thinkaurelius.titan.graphdb.edgequery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.edges.EdgeDirection;
import com.thinkaurelius.titan.graphdb.transaction.GraphTx;
import com.thinkaurelius.titan.graphdb.vertices.InternalNode;
import com.thinkaurelius.titan.graphdb.vertices.RemovableEdgeIterable;
import com.thinkaurelius.titan.graphdb.vertices.RemovableEdgeIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StandardEdgeQuery implements InternalEdgeQuery {

    private final GraphTx tx;
	private InternalNode node;
    private long nodeid;

    private boolean inMemoryRetrieval=false;

    private Direction dir;
    private EdgeType type;
    private EdgeTypeGroup group;

    private boolean queryProps;
    private boolean queryRelships;
    private boolean queryHidden;
    private boolean queryUnmodifiable;

    private long limit = Long.MAX_VALUE;

    private Map<PropertyType,Interval<?>> propertyConstraints;

    public StandardEdgeQuery(GraphTx tx) {
        this.tx=tx;

        dir = null;
        type = null;
        group = null;
        queryProps=true;
        queryRelships=true;
        queryHidden=false;
        queryUnmodifiable=true;
        propertyConstraints = null;
    }

    public StandardEdgeQuery(InternalNode n) {
        this(n.getTransaction());
        Preconditions.checkNotNull(n);


        node = n;
        if (node.hasID()) nodeid=node.getID();
        else nodeid = -1;
    }

    public StandardEdgeQuery(GraphTx tx, long nodeid) {
        this(tx);

        Preconditions.checkArgument(nodeid>0);
        //if (!tx.containsNode(nodeid)) throw new InvalidNodeException("Node with given ID does not exist: " + nodeid);
        node = null;
        this.nodeid = nodeid;
    }

    StandardEdgeQuery(StandardEdgeQuery q) {
        dir = q.dir;
        type = q.type;
        group = q.group;
        queryProps=q.queryProps;
        queryRelships=q.queryRelships;
        queryHidden=q.queryHidden;
        queryUnmodifiable=q.queryUnmodifiable;

        node = q.node;
        nodeid = q.nodeid;
        tx = q.tx;

        inMemoryRetrieval=q.inMemoryRetrieval;
        limit = q.limit;
        if (q.propertyConstraints ==null) propertyConstraints =null;
        else propertyConstraints = new HashMap<PropertyType,Interval<?>>(q.propertyConstraints);
    }

	StandardEdgeQuery(InternalNode node, StandardEdgeQuery q) {
		this(q);
		this.node=node;
	}

	@Override
	public StandardEdgeQuery copy() {
		StandardEdgeQuery q = new StandardEdgeQuery(this);
		return q;
	}

	public static final StandardEdgeQuery queryAll(InternalNode node) {
		return new StandardEdgeQuery(node).includeHidden();
	}

	/* ---------------------------------------------------------------
	 * Query Construction
	 * ---------------------------------------------------------------
	 */

    private<T> StandardEdgeQuery withPropertyConstraint(PropertyType ptype,Interval<T> interval) {
        Preconditions.checkNotNull(ptype);
        Preconditions.checkNotNull(interval);
        if (propertyConstraints == null) propertyConstraints = new HashMap<PropertyType,Interval<?>>(5);
        propertyConstraints.put(ptype, interval);
        return this;
    }

    @Override
    public<T> StandardEdgeQuery withProperty(PropertyType ptype,T value) {
        return withPropertyConstraint(ptype,new PointInterval<T>(value));
    }

    @Override
    public<T> StandardEdgeQuery withProperty(String ptype,T value) {
        if (!tx.containsEdgeType(ptype)) throw new IllegalArgumentException("Unknown property type: " + ptype);
        return withProperty(tx.getPropertyType(ptype),value);
    }

    @Override
    public<T> EdgeQuery withPropertyIn(PropertyType ptype, Comparable<T> start, Comparable<T> end) {
        return withPropertyConstraint(ptype, new Range(start, end));
    }

    @Override
    public<T> EdgeQuery withPropertyIn(String ptype, Comparable<T> start, Comparable<T> end) {
        if (!tx.containsEdgeType(ptype)) throw new IllegalArgumentException("Unknown property type: " + ptype);
        return withPropertyIn(tx.getPropertyType(ptype), start, end);
    }

    @Override
    public StandardEdgeQuery withEdgeType(String... types) {
        //TODO: Filter out non-existent edge types. Handle special case where resulting set is empty
        Preconditions.checkNotNull(tx);
        EdgeType[] etypes = new EdgeType[types.length];
        for (int i=0;i<types.length;i++) {
            etypes[i]=tx.getEdgeType(types[i]);
        }
        return withEdgeType(etypes);
    }

	@Override
    public StandardEdgeQuery withEdgeType(EdgeType... types) {
        Preconditions.checkArgument(types.length==1,"Currently, only single edge types are supported!");
        EdgeType type = types[0];
        Preconditions.checkNotNull(type);
        this.type = type;
        group = null;
        if (type.isRelationshipType()) {
            relationshipsOnly();
            if (dir==null) {
                switch(type.getDirectionality()) {
                    case Undirected: dir=Direction.Undirected; break;
                    case Directed: dir = Direction.Both; break;
                    case Unidirected: dir = Direction.Out; break;
                    default: throw new AssertionError();
                }
            }
        }
        else {
            assert type.isPropertyType();
            propertiesOnly();
        }
        return this;
    }

	@Override
	public StandardEdgeQuery withEdgeTypeGroup(EdgeTypeGroup group) {
        Preconditions.checkNotNull(group);
        this.group=group;
        type = null;
        allEdges();
		return this;
	}


	@Override
	public StandardEdgeQuery inDirection(Direction d) {
        dir = d;
		return this;
	}

	@Override
	public StandardEdgeQuery onlyModifiable() {
        queryUnmodifiable=false;
		return this;
	}

	@Override
	public StandardEdgeQuery includeHidden() {
        queryHidden=true;
		return this;
	}

    @Override
    public StandardEdgeQuery setRetrievalLimit(long limit) {
        this.limit=limit;
        return this;
    }

    @Override
    public StandardEdgeQuery inMemoryRetrieval() {
        this.inMemoryRetrieval=true;
        return this;
    }



    void propertiesOnly() {
        dir = Direction.Out;
        queryProps=true;
        queryRelships=false;
    }

    void relationshipsOnly() {
        queryProps=false;
        queryRelships=true;
    }

    void allEdges() {
        queryProps=true;
        queryRelships=true;
    }


    /* ---------------------------------------------------------------
      * Query Inspection
      * ---------------------------------------------------------------
      */

    @Override
    public boolean hasEdgeTypeCondition() {
        return type!=null;
    }

    @Override
    public EdgeType getEdgeTypeCondition() {
        if (!hasEdgeTypeCondition()) throw new IllegalStateException("This query does not have a edge type condition!");
        return type;
    }


    @Override
    public EdgeTypeGroup getEdgeTypeGroupCondition() {
        if (!hasEdgeTypeGroupCondition()) throw new IllegalStateException("This query does not have a edge type group condition!");
        return group;
    }

    @Override
    public boolean hasEdgeTypeGroupCondition() {
        return group!=null;
    }

    @Override
    public boolean hasDirectionCondition() {
        return dir!=null;
    }

    @Override
    public Direction getDirectionCondition() {
        if (!hasDirectionCondition()) throw new IllegalStateException("This query does not have a direction condition!");
        return dir;
    }

    @Override
    public boolean isAllowedDirection(EdgeDirection _dir) {
        if (dir==null) return true;
        else return dir.isAllowedDirection(_dir);
    }

    @Override
    public boolean queryProperties() {
        return queryProps;
    }

    @Override
    public boolean queryRelationships() {
        return queryRelships;
    }


    @Override
    public boolean queryHidden() {
        return queryHidden;
    }

    @Override
    public boolean queryUnmodifiable() {
        return queryUnmodifiable;
    }

    @Override
    public long getNodeID() {
        if (nodeid>0) return nodeid;
        else throw new IllegalStateException("NodeID has not been set for query!");
    }

    @Override
    public InternalNode getNode() {
        return node;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public boolean hasPropertyConstraints() {
        return propertyConstraints!=null && !propertyConstraints.isEmpty();
    }

    @Override
    public Map<PropertyType,Interval<?>> getPropertyConstraints() {
        if (propertyConstraints==null) return new HashMap<PropertyType, Interval<?>>();
        else return propertyConstraints;
    }

	/* ---------------------------------------------------------------
	 * Query Execution
	 * ---------------------------------------------------------------
	 */


	@Override
	public Iterable<Property> getProperties() {
		propertiesOnly();
		return new RemovableEdgeIterable<Property>(node.getEdges(this,true));
	}


	@Override
	public Iterator<Property> getPropertyIterator() {
		propertiesOnly();
		return new RemovableEdgeIterator<Property>(node.getEdges(this,true).iterator());
	}


	@Override
	public Iterator<Relationship> getRelationshipIterator() {
		relationshipsOnly();
		return new RemovableEdgeIterator<Relationship>(node.getEdges(this,true).iterator());
	}


	@Override
	public Iterable<Relationship> getRelationships() {
		relationshipsOnly();
		return new RemovableEdgeIterable<Relationship>(node.getEdges(this,true));
	}

	@Override
	public Iterator<Edge> getEdgeIterator() {
		allEdges();
		return new RemovableEdgeIterator<Edge>(node.getEdges(this,true).iterator());
	}

	@Override
	public Iterable<Edge> getEdges() {
		allEdges();
		return new RemovableEdgeIterable<Edge>(node.getEdges(this,true));
	}

	@Override
	public int noRelationships() {
		relationshipsOnly();
		return Iterators.size(getRelationshipIterator());
	}

	@Override
	public int noProperties() {
		propertiesOnly();
		return Iterators.size(getPropertyIterator());
	}



    //############## Neighborhood ############

    private boolean retrieveInMemory() {
        if (inMemoryRetrieval) return true;
        else {
            if (node!=null) {
                if (node.isLoaded() && node.hasID()) return false;
                else return true;
            } else {
                if (tx.hasModifications()) return true;
                else return false;
            }
        }
    }

    @Override
    public NodeList getNeighborhood() {
        Preconditions.checkNotNull(tx);
        if (retrieveInMemory()) {
            return retrieveFromMemory(new NodeArrayList());
        } else {
            return getNeighborhoodIDs();
        }
    }

    private NodeListInternal retrieveFromMemory(NodeListInternal nodes) {
        if (node==null) node = tx.getExistingNode(nodeid);
        StandardEdgeQuery q = new StandardEdgeQuery(node,this);
        Iterator<Relationship> iter = q.getRelationshipIterator();
        while (iter.hasNext()) {
            Relationship next = iter.next();
            nodes.add(next.getOtherNode(node));
        }
        return nodes;
    }

    public NodeList getNeighborhoodIDs() {
        Preconditions.checkNotNull(tx);
        Preconditions.checkArgument(node==null || (!node.isNew() && !node.isModified()),
                "Cannot query for raw neighborhood on new or modified node.");
        if (!retrieveInMemory()) {
            Preconditions.checkArgument(nodeid>0,"The node id could not be determined!");
            return new NodeLongList(tx,tx.getRawNeighborhood(this),true);
        } else {
            return retrieveFromMemory(new NodeLongList(tx));
        }
    }



}