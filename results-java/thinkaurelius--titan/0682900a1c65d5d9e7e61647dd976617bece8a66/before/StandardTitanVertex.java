package com.thinkaurelius.titan.graphdb.vertices;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.InvalidElementException;
import com.thinkaurelius.titan.graphdb.adjacencylist.AdjacencyList;
import com.thinkaurelius.titan.graphdb.adjacencylist.AdjacencyListFactory;
import com.thinkaurelius.titan.graphdb.adjacencylist.InitialAdjListFactory;
import com.thinkaurelius.titan.graphdb.adjacencylist.ModificationStatus;
import com.thinkaurelius.titan.graphdb.query.AtomicQuery;
import com.thinkaurelius.titan.graphdb.relations.EdgeDirection;
import com.thinkaurelius.titan.graphdb.relations.InternalRelation;
import com.thinkaurelius.titan.graphdb.transaction.InternalTitanTransaction;
import com.tinkerpop.blueprints.Direction;

import java.util.concurrent.locks.ReentrantLock;

public class StandardTitanVertex extends AbstractTitanVertex {

	private volatile AdjacencyList inEdges;
	private volatile AdjacencyList outEdges;
    protected final ReentrantLock adjLock = new ReentrantLock();


	public StandardTitanVertex(InternalTitanTransaction g, AdjacencyListFactory adjList) {
		super(g);
		inEdges = adjList.emptyList();
		outEdges = adjList.emptyList();
	}

	@Override
	public boolean addRelation(InternalRelation e, boolean isNew) {
        assert isAvailable();
		Preconditions.checkArgument(e.isIncidentOn(this), "TitanRelation is not incident on this node!");

        boolean success = false;
        boolean loadIn = false;
        ModificationStatus status = new ModificationStatus();
        if (EdgeDirection.IN.impliedBy(e.getDirection(this))) {
            loadIn = true;
            adjLock.lock();
            try {
                inEdges = inEdges.addEdge(e,status);
            } finally {
                adjLock.unlock();
            }
            success = status.hasChanged();
        }
        if (EdgeDirection.OUT.impliedBy(e.getDirection(this))) {
            adjLock.lock();
            try {
                outEdges = outEdges.addEdge(e, e.getType().isFunctional(), status);
            } finally {
                adjLock.unlock();
            }
            if (status.hasChanged()) {
                if (loadIn && !success) throw new InvalidElementException("Could only load one direction of loop-edge",e);
                success=true;
            } else {
                if (loadIn && success) throw new InvalidElementException("Could only load one direction of loop-edge",e);
                success=false;
            }
        }
        return success;

	}

	@Override
	public Iterable<InternalRelation> getRelations(AtomicQuery query, boolean loadRemaining) {
		assert isAvailable();
		if (loadRemaining) ensureLoadedEdges(query);

		Iterable<InternalRelation> iter=AdjacencyList.Empty;
		for (EdgeDirection dir : EdgeDirection.values()) {
			if (!query.isAllowedDirection(dir)) continue;
			Iterable<InternalRelation> siter;
			switch(dir) {
			case OUT:
                siter = VertexUtil.getQuerySpecificIterable(outEdges, query);
				break;
			case IN:
                siter = VertexUtil.getQuerySpecificIterable(inEdges, query);
                //if (query.isAllowedDirection(EdgeDirection.OUT)) siter = VertexUtil.filterLoopEdges(siter,this);
				break;
			default: throw new AssertionError("Unrecognized direction: "+ dir);
			}
			if (iter==AdjacencyList.Empty) iter = siter;
			else if (siter!=AdjacencyList.Empty) iter = Iterables.concat(iter, siter);
		}

		iter = VertexUtil.filterByQuery(query, iter);
		return iter;
	}


	@Override
	public void removeRelation(InternalRelation e) {
		Preconditions.checkArgument(isAvailable() && e.isIncidentOn(this));
		Direction dir = e.getDirection(this);
		if (EdgeDirection.IN.impliedBy(dir)) inEdges.removeEdge(e,ModificationStatus.none);
		if (EdgeDirection.OUT.impliedBy(dir)) outEdges.removeEdge(e,ModificationStatus.none);
	}

	@Override
	public synchronized void remove() {
		super.remove();
		inEdges=InitialAdjListFactory.EmptyFactory.emptyList();
		outEdges=InitialAdjListFactory.EmptyFactory.emptyList();
	}


}