package com.thinkaurelius.titan.hadoop;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.internal.OrderList;
import com.thinkaurelius.titan.graphdb.internal.RelationCategory;
import com.thinkaurelius.titan.graphdb.query.BackendQueryHolder;
import com.thinkaurelius.titan.graphdb.query.Query;
import com.thinkaurelius.titan.graphdb.query.condition.PredicateCondition;
import com.thinkaurelius.titan.graphdb.query.vertex.BaseVertexCentricQuery;
import com.thinkaurelius.titan.graphdb.query.vertex.BasicVertexCentricQueryBuilder;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.hadoop.conf.Configuration;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class FaunusVertexQueryFilter extends FaunusVertexQuery {

    private RelationCategory resultType;

    public FaunusVertexQueryFilter(FaunusTypeManager typeManager) {
        super(typeManager);
    }

    private static final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

    private boolean doesFilter = false;

    private void setDoesFilter(final boolean doesFilter) {
        this.doesFilter = doesFilter;
    }

    public boolean doesFilter() {
        return this.doesFilter;
    }

    public static final String TITAN_HADOOP_GRAPH_INPUT_VERTEX_QUERY_FILTER = "titan.hadoop.input.vertex-query-filter";

    public static FaunusVertexQueryFilter create(final Configuration configuration) {
        engine.put("v", new DummyVertex(FaunusTypeManager.getTypeManager(configuration)));
        try {
            FaunusVertexQueryFilter query = (FaunusVertexQueryFilter) engine.eval(configuration.get(TITAN_HADOOP_GRAPH_INPUT_VERTEX_QUERY_FILTER, "v.query().relations()"));
            if (null != configuration.get(TITAN_HADOOP_GRAPH_INPUT_VERTEX_QUERY_FILTER))
                query.setDoesFilter(true);
            return query;
        } catch (final Exception e) {
            throw new RuntimeException("VertexQueryFilter compilation error: " + e.getMessage(), e);
        }
    }

    private static class DummyVertex {

        private final FaunusTypeManager manager;

        private DummyVertex(FaunusTypeManager manager) {
            this.manager = manager;
        }

        public FaunusVertexQueryFilter query() {
            return new FaunusVertexQueryFilter(manager);
        }

    }

    public void filterRelationsOf(final FaunusVertex vertex) {
        Preconditions.checkArgument(resultType!=null,"Query filter has not been initialized correctly");
        if (!this.doesFilter) return;
        for (Direction dir : Direction.proper) {
            Iterator<FaunusRelation> iter = vertex.getAdjacency(dir).values().iterator();
            Predicate<FaunusRelation> filter = getFilter(vertex,resultType);
            while (iter.hasNext()) {
                if (!filter.apply(iter.next()))
                    iter.remove();
            }
        }
    }

    public SliceQuery determineQueryBounds(TitanGraph graph) {
        Preconditions.checkArgument(resultType!=null,"Query filter has not been initialized correctly");
        TitanTransaction tx = graph.newTransaction();
        try {
            QueryBounds qb = new SlicePredicateQueryBuilder((StandardTitanTx)tx,this).getQueryBounds(resultType);
            if (qb.isFitted) setDoesFilter(false);
            return qb.query;
        } finally {
            tx.rollback();
        }
    }

    @Override
    public TitanVertex getVertex(long vid) {
        throw new UnsupportedOperationException("Adjacency constraints are not supported by filter");
    }

    @Override
    public FaunusVertexQuery adjacent(TitanVertex vertex) {
        throw new UnsupportedOperationException("Adjacency constraints are not supported by filter");
    }

    @Override
    protected Iterable<FaunusRelation> getRelations(RelationCategory returnType) {
        resultType = returnType;
        return Collections.EMPTY_LIST;
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long propertyCount() {
        throw new UnsupportedOperationException();
    }


    public static class QueryBounds {

        public final SliceQuery query;
        public final boolean isFitted;

        private QueryBounds(SliceQuery query, boolean fitted) {
            this.query = query;
            isFitted = fitted;
        }
    }


    private static class SlicePredicateQueryBuilder extends BasicVertexCentricQueryBuilder<SlicePredicateQueryBuilder> {

        public SlicePredicateQueryBuilder(StandardTitanTx tx, FaunusVertexQueryFilter query) {
            super(tx);
            direction(query.dir);
            types(query.types);
            limit(query.limit);
            //constraints
            for (PredicateCondition<String, TitanRelation> constraint : query.constraints) {
                has(constraint.getKey(),constraint.getPredicate(),constraint.getValue());
            }
            //orders
            if (!query.orders.isEmpty()) {
                for (OrderList.OrderEntry entry : query.orders) {
                    orderBy(entry.getKey().getName(),entry.getOrder());
                }
            }
            assert query.adjacentVertex==null;
        }

        @Override
        protected SlicePredicateQueryBuilder getThis() {
            return this;
        }

        public QueryBounds getQueryBounds(RelationCategory resultType) {
            BaseVertexCentricQuery q = constructQuery(resultType);
            boolean isFitted = false;
            SliceQuery result;
            if (q.numSubQueries()==0) {
                //Query doesn't match anything
                throw new IllegalArgumentException("Query filter is invalid - does not match anything");
            } else if (q.numSubQueries()==1) {
                BackendQueryHolder<SliceQuery> bqh = q.getSubQuery(0);
                result = bqh.getBackendQuery().updateLimit(bqh.isSorted() ? limit : Query.NO_LIMIT);
                isFitted = bqh.isFitted() && bqh.isSorted();
            } else {
                //Find upper and lower bound
                StaticBuffer start=null, end=null;
                for (int i = 0; i < q.numSubQueries(); i++) {
                    SliceQuery sq = q.getSubQuery(i).getBackendQuery();
                    if (start==null || start.compareTo(sq.getSliceStart())>0) start=sq.getSliceStart();
                    if (end==null || end.compareTo(sq.getSliceEnd())<0) end=sq.getSliceEnd();
                }
                assert start!=null && end!=null && start.compareTo(end)<=0;
                result = new SliceQuery(start,end).setLimit(Query.NO_LIMIT);
            }

            return new QueryBounds(result,isFitted);
        }

    }

}