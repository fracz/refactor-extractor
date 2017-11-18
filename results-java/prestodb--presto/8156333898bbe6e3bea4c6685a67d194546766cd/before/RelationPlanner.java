package com.facebook.presto.sql.planner;

import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.TableHandle;
import com.facebook.presto.sql.analyzer.Analysis;
import com.facebook.presto.sql.analyzer.EquiJoinClause;
import com.facebook.presto.sql.analyzer.Field;
import com.facebook.presto.sql.analyzer.FieldOrExpression;
import com.facebook.presto.sql.analyzer.Session;
import com.facebook.presto.sql.analyzer.TupleDescriptor;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import com.facebook.presto.sql.tree.AliasedRelation;
import com.facebook.presto.sql.tree.DefaultTraversalVisitor;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.Join;
import com.facebook.presto.sql.tree.QualifiedNameReference;
import com.facebook.presto.sql.tree.Query;
import com.facebook.presto.sql.tree.Subquery;
import com.facebook.presto.sql.tree.Table;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.facebook.presto.sql.analyzer.EquiJoinClause.leftGetter;
import static com.facebook.presto.sql.analyzer.EquiJoinClause.rightGetter;

class RelationPlanner
        extends DefaultTraversalVisitor<RelationPlan, Void>
{
    private final Analysis analysis;
    private final SymbolAllocator symbolAllocator;
    private final PlanNodeIdAllocator idAllocator;
    private final Metadata metadata;
    private final Session session;

    RelationPlanner(Analysis analysis, SymbolAllocator symbolAllocator, PlanNodeIdAllocator idAllocator, Metadata metadata, Session session)
    {
        Preconditions.checkNotNull(analysis, "analysis is null");
        Preconditions.checkNotNull(symbolAllocator, "symbolAllocator is null");
        Preconditions.checkNotNull(idAllocator, "idAllocator is null");
        Preconditions.checkNotNull(metadata, "metadata is null");
        Preconditions.checkNotNull(session, "session is null");

        this.analysis = analysis;
        this.symbolAllocator = symbolAllocator;
        this.idAllocator = idAllocator;
        this.metadata = metadata;
        this.session = session;
    }

    @Override
    protected RelationPlan visitTable(Table node, Void context)
    {
        if (!node.getName().getPrefix().isPresent()) {
            Query namedQuery = analysis.getNamedQuery(node);
            if (namedQuery != null) {
                RelationPlan subPlan = process(namedQuery, null);
                return new RelationPlan(subPlan.getRoot(), analysis.getOutputDescriptor(node), subPlan.getOutputSymbols());
            }
        }

        TupleDescriptor descriptor = analysis.getOutputDescriptor(node);
        TableHandle handle = analysis.getTableHandle(node);

        ImmutableList.Builder<Symbol> outputSymbols = ImmutableList.builder();
        ImmutableMap.Builder<Symbol, ColumnHandle> columns = ImmutableMap.builder();
        for (int i = 0; i < descriptor.getFields().size(); i++) {
            Field field = descriptor.getFields().get(i);
            Symbol symbol = symbolAllocator.newSymbol(field.getName().get(), field.getType());

            outputSymbols.add(symbol);
            columns.put(symbol, analysis.getColumn(field));
        }

        return new RelationPlan(new TableScanNode(idAllocator.getNextId(), handle, columns.build()), descriptor, outputSymbols.build());
    }

    @Override
    protected RelationPlan visitAliasedRelation(AliasedRelation node, Void context)
    {
        RelationPlan subPlan = process(node.getRelation(), context);

        TupleDescriptor outputDescriptor = analysis.getOutputDescriptor(node);

        return new RelationPlan(subPlan.getRoot(), outputDescriptor, subPlan.getOutputSymbols());
    }

    @Override
    protected RelationPlan visitJoin(Join node, Void context)
    {
        RelationPlan leftPlan = process(node.getLeft(), context);
        RelationPlan rightPlan = process(node.getRight(), context);

        List<EquiJoinClause> criteria = analysis.getJoinCriteria(node);

        // Add projections for join criteria
        PlanBuilder leftPlanBuilder = appendProjections(leftPlan, Iterables.transform(criteria, leftGetter()));
        PlanBuilder rightPlanBuilder = appendProjections(rightPlan, Iterables.transform(criteria, rightGetter()));

        ImmutableList.Builder<JoinNode.EquiJoinClause> clauses = ImmutableList.builder();
        for (EquiJoinClause clause : criteria) {
            Symbol leftSymbol = leftPlanBuilder.translate(clause.getLeft());
            Symbol rightSymbol = rightPlanBuilder.translate(clause.getRight());

            clauses.add(new JoinNode.EquiJoinClause(leftSymbol, rightSymbol));
        }

        List<Symbol> outputSymbols = ImmutableList.<Symbol>builder()
                .addAll(leftPlan.getOutputSymbols())
                .addAll(rightPlan.getOutputSymbols())
                .build();

        return new RelationPlan(new JoinNode(idAllocator.getNextId(), leftPlanBuilder.getRoot(), rightPlanBuilder.getRoot(), clauses.build()), analysis.getOutputDescriptor(node), outputSymbols);
    }

    @Override
    protected RelationPlan visitSubquery(Subquery node, Void context)
    {
        return process(node.getQuery(), context);
    }

    @Override
    protected RelationPlan visitQuery(Query node, Void context)
    {
        PlanBuilder subPlan = new QueryPlanner(analysis, symbolAllocator, idAllocator, metadata, session).process(node, null);

        TupleDescriptor outputDescriptor = analysis.getOutputDescriptor(node);

        ImmutableList.Builder<Symbol> outputSymbols = ImmutableList.builder();
        for (FieldOrExpression fieldOrExpression : analysis.getOutputExpressions(node)) {
            outputSymbols.add(subPlan.translate(fieldOrExpression));
        }

        return new RelationPlan(subPlan.getRoot(), outputDescriptor, outputSymbols.build());
    }

    private PlanBuilder appendProjections(RelationPlan subPlan, Iterable<Expression> expressions)
    {
        TranslationMap translations = new TranslationMap(subPlan, analysis);

        // Make field->symbol mapping from underlying relation plan available for translations
        // This makes it possible to rewrite FieldOrExpressions that reference fields from the underlying tuple directly
        translations.setFieldMappings(subPlan.getOutputSymbols());

        ImmutableMap.Builder<Symbol, Expression> projections = ImmutableMap.builder();

        // add an identity projection for underlying plan
        for (Symbol symbol : subPlan.getRoot().getOutputSymbols()) {
            Expression expression = new QualifiedNameReference(symbol.toQualifiedName());
            projections.put(symbol, expression);
        }

        for (Expression expression : expressions) {
            Symbol symbol = symbolAllocator.newSymbol(expression, analysis.getType(expression));

            projections.put(symbol, translations.rewrite(expression));
            translations.put(expression, symbol);
        }

        return new PlanBuilder(translations, new ProjectNode(idAllocator.getNextId(), subPlan.getRoot(), projections.build()));
    }
}