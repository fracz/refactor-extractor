package com.thinkaurelius.titan.hadoop;

import com.carrotsearch.hppc.LongArrayList;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.graphdb.internal.ElementLifeCycle;
import com.thinkaurelius.titan.graphdb.internal.RelationCategory;
import com.thinkaurelius.titan.graphdb.query.Query;
import com.thinkaurelius.titan.graphdb.query.condition.*;
import com.thinkaurelius.titan.graphdb.query.vertex.BaseVertexCentricQueryBuilder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class FaunusVertexQuery extends BaseVertexCentricQueryBuilder<FaunusVertexQuery> implements TitanVertexQuery<FaunusVertexQuery> {

    private FaunusPathElement baseElement;

    private boolean queryAll;

    public FaunusVertexQuery(FaunusPathElement element) {
        this(element.getTypeManager());
        this.baseElement = element;
    }

    protected FaunusVertexQuery(FaunusTypeManager typeManager) {
        super(typeManager);
        this.baseElement = null;
    }

    public FaunusVertexQuery queryAll() {
        queryAll=true;
        return getThis();
    }

    @Override
    protected FaunusVertexQuery getThis() {
        return this;
    }

    @Override
    protected TitanVertex getVertex(long vertexid) {
        return new FaunusVertex(baseElement.getConf(), vertexid);
    }

    /* ################################################
                        EXECUTION
     ################################################*/

    protected And<TitanRelation> getCondition(FaunusElement element, RelationCategory returnType) {
        FaunusTypeManager typeManager = element.getTypeManager();
        if (!(element instanceof FaunusVertex)) {
            Preconditions.checkArgument(dir!=Direction.IN,"Illegal direction for element: %s",dir);
            dir = Direction.OUT;
        }
        And<TitanRelation> conditions = new And<TitanRelation>(constraints.size()+4);
        if (types.length>0) {
            Or<TitanRelation> typeConstraints = new Or<TitanRelation>(types.length);
            for (String type : types) {
                FaunusRelationType rt = typeManager.getRelationType(type);
                if (rt!=null) {
                    typeConstraints.add(new RelationTypeCondition<TitanRelation>(rt));
                }
            }
            if (typeConstraints.isEmpty()) return null;
            else conditions.add(typeConstraints);
        }
        //Prepare constraints
        for (PredicateCondition<String,TitanRelation> constraint : constraints) {
            FaunusRelationType type = typeManager.getRelationType(constraint.getKey());

            if (type == null) {
                if (constraint.getPredicate() == Cmp.EQUAL && constraint.getValue() == null)
                    continue; //Ignore condition, its trivially satisfied
                else return null;
            }
            conditions.add(new PredicateCondition<FaunusRelationType,TitanRelation>(type,constraint.getPredicate(),constraint.getValue()));
        }
        //Add return type
        conditions.add(returnType);


        //Direction and adjacency condition
        Preconditions.checkArgument(returnType==RelationCategory.EDGE || adjacentVertex==null,
                "Adjacent vertex constraints only applies when querying for edges");
        if (element instanceof FaunusVertex) {
            if (dir!=Direction.BOTH) conditions.add(
                    new DirectionCondition<TitanRelation>((FaunusVertex)element,dir));
            if (adjacentVertex!=null) conditions.add(
                    new IncidenceCondition<TitanRelation>((FaunusVertex)element,adjacentVertex));
        } else {
            //Direction constraint == OUT is automatically true for elements
            if (adjacentVertex!=null) conditions.add(
                    new IncidenceDirectionCondition<TitanRelation>(dir.opposite(),adjacentVertex));
        }

        //System constraints and filters
        if (!queryAll) conditions.add(FILTER_HIDDEN_AND_REMOVED);

        return conditions;
    }

    protected Iterable<FaunusRelation> getRelations(FaunusElement element, RelationCategory returnType) {
        FaunusTypeManager typeManager = element.getTypeManager();

        final And<TitanRelation> condition = getCondition(element,returnType);
        if (condition==null) return Collections.EMPTY_LIST;

        Iterable<FaunusRelation> result=null;
        for (Direction direction : Direction.proper) {
            if (dir!=direction && dir!=Direction.BOTH) continue;

            SetMultimap<FaunusRelationType, FaunusRelation> adjacency = element.getAdjacency(dir);
            if (types.length==0) {
                result = adjacency.values();
            } else {
                for (String type : types) {
                    FaunusRelationType rt = typeManager.getRelationType(type);
                    Iterable<FaunusRelation> rels;
                    if (rt.isPropertyKey() && ((FaunusPropertyKey)rt).isImplicit()) {
                        FaunusPropertyKey key = (FaunusPropertyKey)rt;
                        rels = Lists.newArrayList((FaunusRelation)new SimpleFaunusProperty(key,key.computeImplicit(element)));
                    } else {
                        rels = adjacency.get(rt);
                    }
                    if (rt!=null) {
                        if (result==null) result=rels;
                        else result = Iterables.concat(result,rels);
                    }
                }
            }
        }

        result = new FilterIterable(condition, element, result);

        //Order
        if (!orders.isEmpty()) {
            ArrayList<FaunusRelation> allRels = Lists.newArrayList(result);
            Collections.sort(allRels,orders);
            result = new RemoveOriginalIterable(allRels, element);
        }
        //Limit
        if (limit!= Query.NO_LIMIT) {
            result = Iterables.limit(result,limit);
        }

        return result;
    }

    protected Predicate<FaunusRelation> getFilter(FaunusElement element, RelationCategory returnType) {
        final And<TitanRelation> condition = getCondition(element,returnType);
        if (condition==null) return Predicates.alwaysFalse();

        if (limit==Query.NO_LIMIT) {
            return new Predicate<FaunusRelation>() {
                @Override
                public boolean apply(@Nullable FaunusRelation faunusRelation) {
                    return condition.evaluate(faunusRelation);
                }
            };
        } else {
            final Set<FaunusRelation> matchingRels = Sets.newHashSet(getRelations(element,returnType));
            return new Predicate<FaunusRelation>() {
                @Override
                public boolean apply(@Nullable FaunusRelation faunusRelation) {
                    return matchingRels.contains(faunusRelation);
                }
            };
        }

    }

    protected Iterable<FaunusRelation> getRelations(RelationCategory returnType) {
        Preconditions.checkState(baseElement!=null,"Query not correctly initialized");
        return getRelations(baseElement,returnType);
    }

    //######### PROXY ############

    @Override
    public Iterable<Edge> edges() {
        return (Iterable)getRelations(RelationCategory.EDGE);
    }

    @Override
    public Iterable<TitanEdge> titanEdges() {
        return (Iterable)getRelations(RelationCategory.EDGE);
    }

    @Override
    public Iterable<TitanProperty> properties() {
        return (Iterable)getRelations(RelationCategory.PROPERTY);
    }

    @Override
    public Iterable<TitanRelation> relations() {
        return (Iterable)getRelations(RelationCategory.RELATION);
    }

    @Override
    public Iterable<Vertex> vertices() {
        return Iterables.transform(titanEdges(),new Function<TitanEdge, Vertex>() {
            @Nullable
            @Override
            public Vertex apply(@Nullable TitanEdge edge) {
                if (dir!=Direction.BOTH) return edge.getVertex(dir.opposite());
                else {
                    assert (edge instanceof FaunusVertex);
                    return edge.getOtherVertex((FaunusVertex)edge);
                }
            }
        });
    }

    @Override
    public VertexList vertexIds() {
        FaunusVertexList list = new FaunusVertexList();
        for (Vertex v : vertices()) list.add((FaunusVertex)v);
        return list;
    }

    @Override
    public long count() {
        return Iterables.size(titanEdges());
    }

    @Override
    public long propertyCount() {
        return Iterables.size(properties());
    }

    private static class FaunusVertexList implements VertexList {

        private boolean isSorted = false;
        private final List<FaunusVertex> list = new ArrayList<FaunusVertex>();

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public FaunusVertex get(int pos) {
            return list.get(pos);
        }

        public boolean add(FaunusVertex vertex) {
            isSorted = false;
            return list.add(vertex);
        }

        @Override
        public void sort() {
            Collections.sort(list);
            isSorted = true;
        }

        @Override
        public boolean isSorted() {
            return isSorted;
        }

        @Override
        public VertexList subList(int fromPosition, int length) {
            FaunusVertexList newList = new FaunusVertexList();
            newList.list.addAll(list.subList(fromPosition,fromPosition+length));
            return newList;
        }

        @Override
        public LongArrayList getIDs() {
            LongArrayList arr = new LongArrayList(size());
            for (FaunusVertex v : list) arr.add(v.getLongId());
            return arr;
        }

        @Override
        public long getID(int pos) {
            return get(pos).getLongId();
        }

        @Override
        public Iterator<TitanVertex> iterator() {
            return (Iterator)list.iterator();
        }
    }

    private static final SystemFilterCondition FILTER_HIDDEN_AND_REMOVED = new SystemFilterCondition();

    private static class SystemFilterCondition extends Literal<TitanRelation> implements Condition<TitanRelation> {

        @Override
        public boolean evaluate(TitanRelation relation) {
            FaunusRelation rel = (FaunusRelation)relation;
            return !rel.getType().isHidden() && !rel.isRemoved();
        }
    }

    private static class RemoveOriginalIterable implements Iterable<FaunusRelation> {

        private final Iterable<FaunusRelation> original;
        private final FaunusElement element;

        private RemoveOriginalIterable(Iterable<FaunusRelation> original, FaunusElement element) {
            this.original = original;
            this.element = element;
        }


        @Override
        public Iterator<FaunusRelation> iterator() {
            return new Iterator<FaunusRelation>() {

                private FaunusRelation current=null;
                private final Iterator<FaunusRelation> iter = original.iterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public FaunusRelation next() {
                    current = iter.next();
                    return current;
                }

                @Override
                public void remove() {
                    current.updateLifeCycle(ElementLifeCycle.Event.REMOVED);
                    if (current.isNew()) {
                        element.inAdjacency.remove(current.getType(),current);
                        element.outAdjacency.remove(current.getType(), current);
                    }
                    element.updateLifeCycle(ElementLifeCycle.Event.REMOVED_RELATION);
                }
            };
        }
    }

    private static class FilterIterable implements Iterable<FaunusRelation> {

        private final Condition<TitanRelation> condition;
        private final FaunusElement element;
        private final Iterable<FaunusRelation> original;

        private FilterIterable(Condition<TitanRelation> condition, FaunusElement element,
                               Iterable<FaunusRelation> original) {
            this.condition = condition;
            this.element = element;
            this.original = original;
        }

        @Override
        public Iterator<FaunusRelation> iterator() {
            return new Iterator<FaunusRelation>() {

                private final Iterator<FaunusRelation> iter = original.iterator();
                private FaunusRelation next = null;
                private FaunusRelation current = null;
                private boolean reachedEnd = false;

                @Override
                public boolean hasNext() {
                    while (next==null) {
                        if (!iter.hasNext()) return false;
                        FaunusRelation candidate = iter.next();
                        if (condition.evaluate(candidate)) next = candidate;
                    }
                    if (next==null) reachedEnd=true;
                    return next!=null;
                }

                @Override
                public FaunusRelation next() {
                    if (next==null) {
                        if (!hasNext()) throw new NoSuchElementException();
                    }
                    assert next!=null;
                    current = next;
                    next=null;
                    return current;
                }

                @Override
                public void remove() {
                    if (next==null && !reachedEnd) {
                        current.updateLifeCycle(ElementLifeCycle.Event.REMOVED);
                        if (current.isNew()) {
                            iter.remove();
                            element.inAdjacency.remove(current.getType(),current);
                            element.outAdjacency.remove(current.getType(),current);
                        }
                        element.updateLifeCycle(ElementLifeCycle.Event.REMOVED_RELATION);
                    } else throw new UnsupportedOperationException();
                }
            };
        }
    }


    //######### UNSUPPORTED ############


    @Override
    public QueryDescription describeForEdges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryDescription describeForProperties() {
        throw new UnsupportedOperationException();
    }
}