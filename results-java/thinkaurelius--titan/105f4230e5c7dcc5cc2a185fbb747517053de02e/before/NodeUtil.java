package com.thinkaurelius.titan.graphdb.vertices;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.TitanRelation;
import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.exceptions.QueryException;
import com.thinkaurelius.titan.graphdb.adjacencylist.AdjacencyList;
import com.thinkaurelius.titan.graphdb.edgequery.InternalTitanQuery;
import com.thinkaurelius.titan.graphdb.edges.InternalRelation;
import com.thinkaurelius.titan.util.interval.AtomicInterval;
import com.tinkerpop.blueprints.Direction;

import java.util.Map;

public class NodeUtil {

	public static void checkAccessbility(InternalTitanVertex v) {
		Preconditions.checkArgument(v.isAccessible(),"TitanVertex is not accessible!");
	}

	public static void checkAvailability(InternalTitanVertex v) {
		Preconditions.checkArgument(v.isAvailable(),"TitanVertex is not available!");
	}

	public static Iterable<InternalRelation> filterQueryQualifications(final InternalTitanQuery query, Iterable<InternalRelation> iter ) {
		if (iter==AdjacencyList.Empty) return iter;

		if (query.queryHidden() && query.queryUnmodifiable() && query.queryProperties()
				&& query.queryRelationships() && !query.hasConstraints()) return iter;
		if (!query.queryProperties() && !query.queryRelationships())
			throw new QueryException("Query excludes both: properties and relationships");


		return Iterables.filter(iter,  new Predicate<InternalRelation>(){

				@Override
				public boolean apply(InternalRelation e) {
					if (!query.queryProperties() && e.isProperty()) return false;
					if (!query.queryRelationships() && e.isEdge()) return false;
					if (!query.queryHidden() && e.isHidden()) return false;
					if (!query.queryUnmodifiable() && !e.isModifiable()) return false;
                    if (query.hasConstraints()) {

                        int count = 0;
                        Map<String,Object> constraints = query.getConstraints();
                        for (TitanRelation ie : e.getRelations()) {
                            if (constraints.containsKey(ie.getType().getName())) {
                                Object o = constraints.get(ie.getType().getName());
                                if (o==null) return false;
                                if (ie.isEdge()) {
                                    if (o.equals(((TitanEdge) ie).getVertex(Direction.IN))) count++;
                                } else {
                                    assert ie.isProperty();
                                    Object attribute = ((TitanProperty)ie).getAttribute();
                                    assert attribute!=null;
                                    assert o instanceof AtomicInterval;
                                    AtomicInterval iv = (AtomicInterval)o;
                                    if (iv.isPoint() && iv.getStartPoint().equals(attribute))
                                        count++;
                                    else {
                                        assert iv.isRange();
                                        if (((Comparable)iv.getStartPoint()).compareTo(attribute)<=0 &&
                                                ((Comparable)iv.getEndPoint()).compareTo(attribute)==1) count++;
                                    }
                                }
                            }
                        }
                        //TODO: There is a potential issue with double counting. Is this realistic for labeled edges (i.e. do we need to consider this)?
                        if (count<constraints.size()) return false;
                    }
					return true;
				}

		});

	}


	public static final Iterable<InternalRelation> filterLoopEdges(Iterable<InternalRelation> iter, final InternalTitanVertex v) {
		if (iter==AdjacencyList.Empty) return iter;
		else return Iterables.filter(iter,new Predicate<InternalRelation>(){

			@Override
			public boolean apply(InternalRelation edge) {
				if (edge.isSelfLoop()) return false;
				else return true;
			}}
		);

	}


    public static final Iterable<InternalRelation> getQuerySpecificIterable(AdjacencyList edges, InternalTitanQuery query) {
        assert query.isAtomic();
        if (query.hasEdgeTypeCondition()) {
            assert query.getTypeCondition()!=null;
            return edges.getEdges(query.getTypeCondition());
        } else if (query.hasEdgeTypeGroupCondition()) {
            return edges.getEdges(query.getEdgeTypeGroupCondition());
        } else {
            return edges.getEdges();
        }
    }


	/**
	 * Checks whether the two given vertices have the same id(s).
	 * Note that this method assumes that both v1,v2 are not NULL.
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static final boolean equalIDs(InternalTitanVertex v1, InternalTitanVertex v2) {
		if (v1.hasID() && v2.hasID()) {
			return v1.getID()==v2.getID();
		} else return false;
	}

	public static final int getIDHashCode(InternalTitanVertex v1) {
		if (v1.hasID()) {
			long id = v1.getID();
			return 37*31 + (int)(id ^ (id >>>32));
		} else throw new IllegalArgumentException("Given node does not have an ID!");
	}


}