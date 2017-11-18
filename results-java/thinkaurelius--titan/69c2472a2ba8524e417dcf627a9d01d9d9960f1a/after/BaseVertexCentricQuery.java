package com.thinkaurelius.titan.graphdb.query.vertex;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.TitanRelation;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.query.BackendQueryHolder;
import com.thinkaurelius.titan.graphdb.query.BaseQuery;
import com.thinkaurelius.titan.graphdb.query.QueryUtil;
import com.thinkaurelius.titan.graphdb.query.condition.Condition;
import com.thinkaurelius.titan.graphdb.query.condition.FixedCondition;
import com.tinkerpop.blueprints.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * The base implementation for {@link VertexCentricQuery} which does not yet contain a reference to the
 * base vertex of the query. This query is constructed by {@link AbstractVertexCentricQueryBuilder#constructQuery(com.thinkaurelius.titan.graphdb.internal.RelationCategory)}
 * and then later extended by single or multi-vertex query which add the vertex to the query.
 * </p>
 * This class override many methods in {@link com.thinkaurelius.titan.graphdb.query.ElementQuery} - check there
 * for a description.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class BaseVertexCentricQuery extends BaseQuery {

    /**
     * The condition of this query in QNF
     */
    protected final Condition<TitanRelation> condition;
    /**
     * The individual component {@link SliceQuery} of this query. This query is considered an OR
     * of the individual components (possibly filtered by the condition if not fitted).
     */
    protected final List<BackendQueryHolder<SliceQuery>> queries;
    /**
     * The direction condition of this query. This is duplicated from the condition for efficiency reasons.
     */
    protected final Direction direction;

    public BaseVertexCentricQuery(Condition<TitanRelation> condition, Direction direction,
                                  List<BackendQueryHolder<SliceQuery>> queries,
                                  int limit) {
        super(limit);
        Preconditions.checkArgument(condition != null && queries != null && direction != null);
        Preconditions.checkArgument(QueryUtil.isQueryNormalForm(condition) && limit>=0);
        this.condition = condition;
        this.queries = queries;
        this.direction=direction;
    }

    protected BaseVertexCentricQuery(BaseVertexCentricQuery query) {
        this(query.getCondition(), query.getDirection(), query.getQueries(), query.getLimit());
    }

    /**
     * Construct an empty query
     */
    protected BaseVertexCentricQuery() {
        this(new FixedCondition<TitanRelation>(false), Direction.BOTH, new ArrayList<BackendQueryHolder<SliceQuery>>(0),0);
    }

    public static BaseVertexCentricQuery emptyQuery() {
        return new BaseVertexCentricQuery();
    }

    public Condition<TitanRelation> getCondition() {
        return condition;
    }

    public Direction getDirection() {
        return direction;
    }

    protected List<BackendQueryHolder<SliceQuery>> getQueries() {
        return queries;
    }

    public boolean isEmpty() {
        return getLimit()<=0;
    }

    public int numSubQueries() {
        return queries.size();
    }

    public BackendQueryHolder<SliceQuery> getSubQuery(int position) {
        return queries.get(position);
    }

    public boolean matches(TitanRelation relation) {
        return condition.evaluate(relation);
    }

    @Override
    public String toString() {
        String s = "["+condition.toString()+"]";
        if (hasLimit()) s+=":"+getLimit();
        return s;
    }

}