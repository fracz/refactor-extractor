package com.thinkaurelius.titan.graphdb.query;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.database.EdgeSerializer;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.internal.RelationType;
import com.thinkaurelius.titan.graphdb.query.condition.And;
import com.thinkaurelius.titan.graphdb.query.condition.Condition;
import com.thinkaurelius.titan.graphdb.query.condition.DirectionCondition;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class VertexCentricQueryBuilder extends AbstractVertexCentricQueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(VertexCentricQueryBuilder.class);

    private final InternalVertex vertex;

    public VertexCentricQueryBuilder(InternalVertex v, EdgeSerializer serializer) {
        super(serializer);
        Preconditions.checkNotNull(v);
        this.vertex=v;
    }

    /* ---------------------------------------------------------------
     * Query Construction
	 * ---------------------------------------------------------------
	 */

    final StandardTitanTx getTx() {
        return vertex.tx();
    }

    @Override
    public VertexCentricQueryBuilder has(TitanKey key, Object value) {
        super.has(key,value);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder has(TitanLabel label, TitanVertex vertex) {
        super.has(label,vertex);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder has(String type, Object value) {
        super.has(type,value);
        return this;
    }

    @Override
    public TitanVertexQuery hasNot(String key, Object value) {
        super.hasNot(key,value);
        return this;
    }

    @Override
    public TitanVertexQuery has(String key, Predicate predicate, Object value) {
        super.has(key,predicate,value);
        return this;
    }

    @Override
    public TitanVertexQuery has(TitanKey key, Predicate predicate, Object value) {
        super.has(key,predicate,value);
        return this;
    }

    @Override
    public TitanVertexQuery has(String key) {
        super.has(key);
        return this;
    }

    @Override
    public TitanVertexQuery hasNot(String key) {
        super.hasNot(key);
        return this;
    }

    @Override
    public <T extends Comparable<?>> VertexCentricQueryBuilder interval(TitanKey key, T start, T end) {
        super.interval(key,start,end);
        return this;
    }

    @Override
    public <T extends Comparable<?>> VertexCentricQueryBuilder interval(String key, T start, T end) {
        super.interval(key,start,end);
        return this;
    }

    @Override
    @Deprecated
    public <T extends Comparable<T>> VertexCentricQueryBuilder has(String key, T value, Compare compare) {
        super.has(key,value,compare);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder types(TitanType... types) {
        super.types(types);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder labels(String... labels) {
        super.labels(labels);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder keys(String... keys) {
        super.keys(keys);
        return this;
    }

    public VertexCentricQueryBuilder type(TitanType type) {
        super.type(type);
        return this;
    }

    public VertexCentricQueryBuilder type(String type) {
        super.type(type);
        return this;
    }

    @Override
    public VertexCentricQueryBuilder direction(Direction d) {
        super.direction(d);
        return this;
    }

    public VertexCentricQueryBuilder includeHidden() {
        super.includeHidden();
        return this;
    }

    @Override
    public VertexCentricQueryBuilder limit(int limit) {
        super.limit(limit);
        return this;
    }

    /* ---------------------------------------------------------------
     * Query Execution
	 * ---------------------------------------------------------------
	 */

    @Override
    protected EdgeSerializer.VertexConstraint getVertexConstraint() {
        //TODO: add constraint for other vertex
        return null;
    }


    protected VertexCentricQuery constructQuery(RelationType returnType) {
        BaseVertexCentricQuery vq = super.constructQuery(returnType);
        Condition<TitanRelation> condition = vq.getCondition();
        if (!vq.isEmpty()) {
            //Add other-vertex and direction related conditions
            And<TitanRelation> newcond = (condition instanceof And)?(And)condition : new And<TitanRelation>(condition);
            newcond.add(new DirectionCondition<TitanRelation>(vertex,getDirection()));
            //TODO: add incidence condition for other vertex
            condition=newcond;
        }
        return new VertexCentricQuery(vertex,condition,vq.getDirection(),vq.getQueries(),vq.getLimit());
    }

    public Iterable<TitanRelation> relations(RelationType returnType) {
        VertexCentricQuery query = constructQuery(returnType);
        QueryProcessor<VertexCentricQuery,TitanRelation,SliceQuery> processor =
                new QueryProcessor<VertexCentricQuery,TitanRelation,SliceQuery>(query,vertex.tx().edgeProcessor);
        return processor;
    }


    @Override
    public Iterable<TitanEdge> titanEdges() {
//        return Iterables.filter(relations(RelationType.EDGE),TitanEdge.class);
        return (Iterable)relations(RelationType.EDGE);
    }


    @Override
    public Iterable<TitanProperty> properties() {
        return (Iterable)relations(RelationType.PROPERTY);
    }

    @Override
    public Iterable<TitanRelation> relations() {
        return relations(RelationType.RELATION);
    }

    @Override
    public Iterable<Edge> edges() {
        return (Iterable)titanEdges();
    }



    @Override
    public long count() {
        return Iterables.size(titanEdges());
    }

    @Override
    public long propertyCount() {
        return Iterables.size(properties());
    }



    @Override
    public Iterable<Vertex> vertices() {
        return (Iterable)Iterables.transform(titanEdges(), new Function<TitanEdge, TitanVertex>() {
            @Nullable
            @Override
            public TitanVertex apply(@Nullable TitanEdge titanEdge) {
                return titanEdge.getOtherVertex(vertex);
            }
        });
    }

    @Override
    public VertexList vertexIds() {
        VertexArrayList vertices = new VertexArrayList();
        for (TitanEdge edge : titanEdges()) vertices.add(edge.getOtherVertex(vertex));
        return vertices;
    }


}