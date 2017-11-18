package com.thinkaurelius.titan.graphdb.query.graph;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.graphdb.database.IndexSerializer;
import com.thinkaurelius.titan.graphdb.internal.ElementCategory;
import com.thinkaurelius.titan.graphdb.internal.InternalType;
import com.thinkaurelius.titan.graphdb.internal.OrderList;
import com.thinkaurelius.titan.graphdb.query.*;
import com.thinkaurelius.titan.graphdb.query.condition.*;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class GraphCentricQueryBuilder implements TitanGraphQuery {

    private static final Logger log = LoggerFactory.getLogger(GraphCentricQueryBuilder.class);

    private final StandardTitanTx tx;
    private final IndexSerializer serializer;
    private List<PredicateCondition<String, TitanElement>> constraints;
    private OrderList orders = new OrderList();
    private int limit = Query.NO_LIMIT;

    public GraphCentricQueryBuilder(StandardTitanTx tx, IndexSerializer serializer) {
        Preconditions.checkNotNull(tx);
        Preconditions.checkNotNull(serializer);
        this.tx = tx;
        this.serializer = serializer;
        this.constraints = new ArrayList<PredicateCondition<String, TitanElement>>(5);
    }

    /* ---------------------------------------------------------------
     * Query Construction
	 * ---------------------------------------------------------------
	 */

    private TitanGraphQuery has(String key, TitanPredicate predicate, Object condition) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(predicate);
        Preconditions.checkArgument(predicate.isValidCondition(condition), "Invalid condition: %s", condition);
        constraints.add(new PredicateCondition<String, TitanElement>(key, predicate, condition));
        return this;
    }

    @Override
    public TitanGraphQuery has(String key, com.tinkerpop.blueprints.Predicate predicate, Object condition) {
        Preconditions.checkNotNull(key);
        TitanPredicate titanPredicate = TitanPredicate.Converter.convert(predicate);
        return has(key, titanPredicate, condition);
    }

    @Override
    public TitanGraphQuery has(TitanKey key, TitanPredicate predicate, Object condition) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(predicate);
        return has(key.getName(), predicate, condition);
    }

    @Override
    public TitanGraphQuery has(String key) {
        return has(key, Cmp.NOT_EQUAL, (Object) null);
    }

    @Override
    public TitanGraphQuery hasNot(String key) {
        return has(key, Cmp.EQUAL, (Object) null);
    }

    @Override
    @Deprecated
    public <T extends Comparable<T>> TitanGraphQuery has(String s, T t, Compare compare) {
        return has(s, compare, t);
    }

    @Override
    public TitanGraphQuery has(String key, Object value) {
        return has(key, Cmp.EQUAL, value);
    }

    @Override
    public TitanGraphQuery hasNot(String key, Object value) {
        return has(key, Cmp.NOT_EQUAL, value);
    }

    @Override
    public <T extends Comparable<?>> TitanGraphQuery interval(String s, T t1, T t2) {
        has(s, Cmp.GREATER_THAN_EQUAL, t1);
        return has(s, Cmp.LESS_THAN, t2);
    }

    @Override
    public TitanGraphQuery limit(final int limit) {
        Preconditions.checkArgument(limit >= 0, "Non-negative limit expected: %s", limit);
        this.limit = limit;
        return this;
    }

    @Override
    public TitanGraphQuery orderBy(String key, Order order) {
        return orderBy(tx.getPropertyKey(key), order);
    }

    @Override
    public TitanGraphQuery orderBy(TitanKey key, Order order) {
        Preconditions.checkArgument(Comparable.class.isAssignableFrom(key.getDataType()),
                "Can only order on keys with comparable data type. [%s] has datatype [%s]", key.getName(), key.getDataType());
        Preconditions.checkArgument(key.getCardinality()==Cardinality.SINGLE, "Ordering is undefined on multi-valued key [%s]", key.getName());
        Preconditions.checkArgument(!orders.containsKey(key.getName()));
        orders.add(key, order);
        return this;
    }

    /* ---------------------------------------------------------------
     * Query Execution
	 * ---------------------------------------------------------------
	 */

    @Override
    public Iterable<Vertex> vertices() {
        GraphCentricQuery query = constructQuery(ElementCategory.VERTEX);
        return Iterables.filter(new QueryProcessor<GraphCentricQuery, TitanElement, JointIndexQuery>(query, tx.elementProcessor), Vertex.class);
    }

    @Override
    public Iterable<Edge> edges() {
        GraphCentricQuery query = constructQuery(ElementCategory.EDGE);
        return Iterables.filter(new QueryProcessor<GraphCentricQuery, TitanElement, JointIndexQuery>(query, tx.elementProcessor), Edge.class);
    }

    @Override
    public Iterable<TitanProperty> properties() {
        GraphCentricQuery query = constructQuery(ElementCategory.PROPERTY);
        return Iterables.filter(new QueryProcessor<GraphCentricQuery, TitanElement, JointIndexQuery>(query, tx.elementProcessor), TitanProperty.class);
    }

    /* ---------------------------------------------------------------
     * Query Construction
	 * ---------------------------------------------------------------
	 */

    private static final int DEFAULT_NO_LIMIT = 100;
    private static final int MAX_BASE_LIMIT = 20000;
    private static final int HARD_MAX_LIMIT = 50000;

    private static final double EQUAL_CONDITION_SCORE = 4;
    private static final double OTHER_CONDITION_SCORE = 1;
    private static final double ORDER_MATCH = 2;
    private static final double ALREADY_MATCHED_ADJUSTOR = 0.1;
    private static final double CARDINALITY_SINGE_SCORE = 1000;
    private static final double CARDINALITY_OTHER_SCORE = 1000;

    private GraphCentricQuery constructQuery(final ElementCategory resultType) {
        Preconditions.checkNotNull(resultType);
        if (limit == 0) return GraphCentricQuery.emptyQuery(resultType);

        //Prepare constraints
        And<TitanElement> conditions = QueryUtil.constraints2QNF(tx, constraints);
        if (conditions == null) return GraphCentricQuery.emptyQuery(resultType);

        orders.makeImmutable();
        if (orders.isEmpty()) orders = OrderList.NO_ORDER;

        final Set<IndexType> indexCandidates = new HashSet<IndexType>();
        ConditionUtil.traversal(conditions,new Predicate<Condition<TitanElement>>() {
            @Override
            public boolean apply(@Nullable Condition<TitanElement> condition) {
                if (condition instanceof PredicateCondition) {
                    TitanType type = ((PredicateCondition<TitanType,TitanElement>)condition).getKey();
                    Preconditions.checkArgument(type!=null && type.isPropertyKey());
                    Iterables.addAll(indexCandidates,Iterables.filter(((InternalType) type).getKeyIndexes(), new Predicate<IndexType>() {
                        @Override
                        public boolean apply(@Nullable IndexType indexType) {
                            return indexType.getElement()==resultType;
                        }
                    }));
                }
                return true;
            }
        });

        JointIndexQuery jointQuery = new JointIndexQuery();
        boolean isSorted=false;
        Set<Condition> coveredClauses = Sets.newHashSet();
        while (true) {
            IndexType bestCandidate = null;
            double candidateScore = 0.0;
            Set<Condition> candidateSubcover = null;
            boolean candidateSupportsSort = false;
            Object candidateSubcondition = null;

            for (IndexType index : indexCandidates) {
                Set<Condition> subcover = Sets.newHashSet();
                Object subcondition;
                boolean supportsSort = false;
                if (index.isInternalIndex()) {
                    subcondition = indexCover((InternalIndexType) index,conditions,subcover);
                } else {
                    subcondition = indexCover((ExternalIndexType) index,conditions,serializer,subcover);
                    if (coveredClauses.isEmpty() && !orders.isEmpty()
                            && indexCoversOrder((ExternalIndexType)index,orders)) supportsSort=true;
                }
                if (subcover.isEmpty()) continue;
                assert subcondition!=null;
                double score = 0.0;
                for (Condition c : subcover) {
                    double s = (c instanceof PredicateCondition && ((PredicateCondition)c).getPredicate()==Cmp.EQUAL)?
                            EQUAL_CONDITION_SCORE:OTHER_CONDITION_SCORE;
                    if (coveredClauses.contains(c)) s=s*ALREADY_MATCHED_ADJUSTOR;
                    score+=s;
                    if (index.isInternalIndex())
                        score+=((InternalIndexType)index).getCardinality()==Cardinality.SINGLE?
                                CARDINALITY_SINGE_SCORE:CARDINALITY_OTHER_SCORE;
                }
                if (supportsSort) score+=ORDER_MATCH;
                if (score>candidateScore) {
                    candidateScore=score;
                    bestCandidate=index;
                    candidateSubcover = subcover;
                    candidateSubcondition = subcondition;
                    candidateSupportsSort = supportsSort;
                }
            }
            if (bestCandidate!=null) {
                if (coveredClauses.isEmpty()) isSorted=candidateSupportsSort;
                coveredClauses.addAll(candidateSubcover);
                if (bestCandidate.isInternalIndex()) {
                    jointQuery.add((InternalIndexType)bestCandidate,
                            serializer.getQuery((InternalIndexType)bestCandidate,(Object[])candidateSubcondition));
                } else {
                    jointQuery.add((ExternalIndexType)bestCandidate,
                            serializer.getQuery((ExternalIndexType)bestCandidate,(Condition)candidateSubcondition,orders));
                }
            } else {
                break;
            }
            /* TODO: smarter optimization:
            - use in-memory histograms to estimate selectivity of PredicateConditions and filter out low-selectivity ones
                    if they would result in an individual index call (better to filter afterwards in memory)
            - move OR's up and extend GraphCentricQuery to allow multiple JointIndexQuery for proper or'ing of queries
            */

        }

        BackendQueryHolder<JointIndexQuery> query;
        if (!coveredClauses.isEmpty()) {
            int indexLimit = limit == Query.NO_LIMIT ? DEFAULT_NO_LIMIT : Math.min(MAX_BASE_LIMIT, limit);
            indexLimit = Math.min(HARD_MAX_LIMIT, QueryUtil.adjustLimitForTxModifications(tx, coveredClauses.size(), indexLimit));
            jointQuery.setLimit(indexLimit);
            query = new BackendQueryHolder<JointIndexQuery>(jointQuery, coveredClauses.size()==conditions.numChildren(), isSorted, null);
        } else {
            query = new BackendQueryHolder<JointIndexQuery>(new JointIndexQuery(), false, false, null);
        }

        return new GraphCentricQuery(resultType, conditions, orders, query, limit);
    }

    private static final boolean indexCoversOrder(ExternalIndexType index, OrderList orders) {
        for (int i = 0; i < orders.size(); i++) {
            if (!index.indexesKey(orders.getKey(i))) return false;
        }
        return true;
    }

    public static final Object[] indexCover(final InternalIndexType index, Condition<TitanElement> condition, Set<Condition> covered) {
        assert QueryUtil.isQueryNormalForm(condition);
        assert condition instanceof And;
        if (index.getStatus()!= SchemaStatus.ENABLED) return null;
        IndexField[] fields = index.getFieldKeys();
        Object[] indexCover = new Object[fields.length];
        Condition[] coveredClauses = new Condition[fields.length];
        for (int i = 0; i < fields.length; i++) {
            IndexField field = fields[i];
            for (Condition<TitanElement> subclause : condition.getChildren()) {
                if (subclause instanceof PredicateCondition) {
                    PredicateCondition<TitanType, TitanElement> atom = (PredicateCondition) condition;
                    if (atom.getPredicate()==Cmp.EQUAL && atom.getValue()!=null && atom.getKey().equals(field.getFieldKey())) {
                        indexCover[i]=atom.getValue();
                        coveredClauses[i]=subclause;
                    }
                }
            }
            if (indexCover[i]==null) return null; //Couldn't find a match
        }
        assert indexCover!=null;
        covered.addAll(Arrays.asList(coveredClauses));
        return indexCover;
    }

    public static final Condition<TitanElement> indexCover(final ExternalIndexType index, Condition<TitanElement> condition,
                                                           final IndexSerializer indexInfo, final Set<Condition> covered) {
        assert QueryUtil.isQueryNormalForm(condition);
        assert condition instanceof And;
        And<TitanElement> subcondition = new And<TitanElement>(condition.numChildren());
        for (Condition<TitanElement> subclause : condition.getChildren()) {
            if (coversAll(index,subclause,indexInfo)) {
                subcondition.add(subclause);
                covered.add(subclause);
            }
        }
        return subcondition.isEmpty()?null:subcondition;
    }

    private static final boolean coversAll(final ExternalIndexType index, Condition<TitanElement> condition, IndexSerializer indexInfo) {
        if (condition.getType()==Condition.Type.LITERAL) {
            if (!(condition instanceof  PredicateCondition)) return false;
            PredicateCondition<TitanType, TitanElement> atom = (PredicateCondition) condition;
            if (atom.getValue()==null) return false;

            Preconditions.checkArgument(atom.getKey().isPropertyKey());
            TitanKey key = (TitanKey) atom.getKey();
            ParameterIndexField[] fields = index.getFieldKeys();
            ParameterIndexField match = null;
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getStatus()!= SchemaStatus.DISABLED) continue;
                if (fields[i].getFieldKey().equals(key)) match = fields[i];
            }
            if (match==null) return false;
            return indexInfo.supports(index,match,atom.getPredicate());
        } else {
            for (Condition<TitanElement> child : condition.getChildren()) {
                if (!coversAll(index,child,indexInfo)) return false;
            }
            return true;
        }
    }


}