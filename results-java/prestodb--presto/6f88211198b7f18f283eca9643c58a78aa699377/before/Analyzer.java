package com.facebook.presto.sql.compiler;

import com.facebook.presto.metadata.ColumnMetadata;
import com.facebook.presto.metadata.FunctionInfo;
import com.facebook.presto.metadata.TableMetadata;
import com.facebook.presto.sql.ExpressionFormatter;
import com.facebook.presto.sql.tree.AliasedExpression;
import com.facebook.presto.sql.tree.AliasedRelation;
import com.facebook.presto.sql.tree.AllColumns;
import com.facebook.presto.sql.tree.AstVisitor;
import com.facebook.presto.sql.tree.DefaultTraversalVisitor;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.FunctionCall;
import com.facebook.presto.sql.tree.Join;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.sql.tree.QualifiedNameReference;
import com.facebook.presto.sql.tree.Query;
import com.facebook.presto.sql.tree.Relation;
import com.facebook.presto.sql.tree.Select;
import com.facebook.presto.sql.tree.Subquery;
import com.facebook.presto.sql.tree.Table;
import com.facebook.presto.sql.tree.TreeRewriter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;

public class Analyzer
{
    private final SessionMetadata metadata;

    public Analyzer(SessionMetadata metadata)
    {
        this.metadata = metadata;
    }

    public AnalysisResult analyze(Node node)
    {
        return analyze(node, new AnalysisContext());
    }

    private AnalysisResult analyze(Node node, AnalysisContext context)
    {
        StatementAnalyzer analyzer = new StatementAnalyzer(metadata);
        return analyzer.process(node, context);
    }

    private static class StatementAnalyzer
            extends AstVisitor<AnalysisResult, AnalysisContext>
    {
        private final SessionMetadata metadata;

        private StatementAnalyzer(SessionMetadata metadata)
        {
            this.metadata = metadata;
        }

        @Override
        protected AnalysisResult visitQuery(Query query, AnalysisContext context)
        {
            Preconditions.checkArgument(!query.getSelect().isDistinct(), "not yet implemented: DISTINCT");
            Preconditions.checkArgument(query.getHaving() == null, "not yet implemented: HAVING");
            Preconditions.checkArgument(query.getOrderBy().isEmpty(), "not yet implemented: ORDER BY");
            Preconditions.checkArgument(query.getLimit() == null, "not yet implemented: LIMIT");
            Preconditions.checkArgument(query.getFrom().size() == 1, "not yet implemented: multiple FROM relations");

            // analyze FROM clause
            Relation relation = Iterables.getOnlyElement(query.getFrom());
            TupleDescriptor sourceDescriptor = new RelationAnalyzer(metadata).process(relation, context);

            AnalyzedExpression predicate = null;
            if (query.getWhere() != null) {
                predicate = analyzePredicate(query.getWhere(), sourceDescriptor);
            }

            AnalyzedOutput output = analyzeSelect(query.getSelect(), context.getSlotAllocator(), sourceDescriptor);
            List<AnalyzedExpression> groupBy = analyzeGroupBy(query.getGroupBy(), sourceDescriptor);
            List<AnalyzedAggregation> aggregations = analyzeAggregations(query.getGroupBy(), query.getSelect(), sourceDescriptor);

            return AnalysisResult.newInstance(context, output, predicate, groupBy, aggregations);
        }

        private AnalyzedExpression analyzePredicate(Expression predicate, TupleDescriptor sourceDescriptor)
        {
            return new ExpressionAnalyzer(metadata).analyze(predicate, sourceDescriptor);
        }

        private AnalyzedOutput analyzeSelect(Select select, SlotAllocator allocator, TupleDescriptor descriptor)
        {
            ImmutableList.Builder<Expression> expressions = ImmutableList.builder();
            ImmutableList.Builder<Optional<QualifiedName>> names = ImmutableList.builder();
            for (Expression expression : select.getSelectItems()) {
                if (expression instanceof AllColumns) {
                    // expand * and T.*
                    Optional<QualifiedName> starPrefix = ((AllColumns) expression).getPrefix();
                    for (NamedSlot slot : descriptor.getSlots()) {
                        Optional<QualifiedName> slotName = slot.getName();
                        if (!starPrefix.isPresent() || slotName.isPresent() && slotName.get().getPrefix().get().hasSuffix(starPrefix.get())) {
                            names.add(slotName);
                            expressions.add(new SlotReference(slot.getSlot()));
                        }
                    }
                }
                else {
                    Optional<QualifiedName> alias = Optional.absent();
                    if (expression instanceof AliasedExpression) {
                        alias = Optional.of(QualifiedName.of(((AliasedExpression) expression).getAlias()));
                        expression = ((AliasedExpression) expression).getExpression();
                    }
                    else if (expression instanceof QualifiedNameReference) {
                        alias = Optional.of(((QualifiedNameReference) expression).getSuffix());
                    }

                    names.add(alias);
                    expressions.add(expression);
                }
            }

            ImmutableList.Builder<Slot> slots = ImmutableList.builder();
            ImmutableMap.Builder<Slot, AnalyzedExpression> outputExpressions = ImmutableMap.builder();
            for (Expression expression : expressions.build()) {
                AnalyzedExpression analysis = new ExpressionAnalyzer(metadata).analyze(expression, descriptor);

                Slot slot = allocator.newSlot(analysis);

                slots.add(slot);
                outputExpressions.put(slot, analysis);
            }

            return new AnalyzedOutput(new TupleDescriptor(names.build(), slots.build()), outputExpressions.build());
        }

        private List<AnalyzedExpression> analyzeGroupBy(List<Expression> groupBy, TupleDescriptor descriptor)
        {
            ImmutableList.Builder<AnalyzedExpression> builder = ImmutableList.builder();
            for (Expression expression : groupBy) {
                builder.add(new ExpressionAnalyzer(metadata).analyze(expression, descriptor));
            }
            return builder.build();
        }

        private List<AnalyzedAggregation> analyzeAggregations(List<Expression> groupBy, Select select, TupleDescriptor descriptor)
        {
            if (!groupBy.isEmpty() && Iterables.any(select.getSelectItems(), instanceOf(AllColumns.class))) {
                throw new SemanticException(select, "Wildcard selector not supported when GROUP BY is present"); // TODO: add support for SELECT T.*, count() ... GROUP BY T.* (maybe?)
            }

            List<Expression> scalarTerms = new ArrayList<>();
            ImmutableList.Builder<AnalyzedAggregation> aggregateTermsBuilder = ImmutableList.builder();
            for (Expression term : select.getSelectItems()) {
                AggregateAnalyzer analyzer = new AggregateAnalyzer(metadata, descriptor);

                List<AnalyzedAggregation> aggregations = analyzer.analyze(term);
                if (aggregations.isEmpty()) {
                    scalarTerms.add(term);
                }
                else {
                    aggregateTermsBuilder.addAll(aggregations);
                }
            }

            List<AnalyzedAggregation> aggregateTerms = aggregateTermsBuilder.build();

            if (!groupBy.isEmpty()) {
                Iterable<Expression> notInGroupBy = Iterables.filter(scalarTerms, not(in(groupBy)));
                if (!Iterables.isEmpty(notInGroupBy)) {
                    throw new SemanticException(select, "Expressions must appear in GROUP BY clause or be used in an aggregate function: %s", Iterables.transform(notInGroupBy, ExpressionFormatter.expressionFormatterFunction()));
                }
            }
            else {
                // if we this is an aggregation query and some terms are not aggregates and there's no group by clause...
                if (!scalarTerms.isEmpty() && !aggregateTerms.isEmpty()) {
                    throw new SemanticException(select, "Mixing of GROUP columns with no GROUP columns is illegal if there is no GROUP BY clause: %s", Iterables.transform(scalarTerms, ExpressionFormatter.expressionFormatterFunction()));
                }
            }

            return aggregateTerms;
        }
    }

    private static class AggregateAnalyzer
            extends DefaultTraversalVisitor<Void, FunctionCall>
    {
        private final SessionMetadata metadata;
        private final TupleDescriptor descriptor;

        private List<AnalyzedAggregation> aggregations;

        public AggregateAnalyzer(SessionMetadata metadata, TupleDescriptor descriptor)
        {
            this.metadata = metadata;
            this.descriptor = descriptor;
        }

        public List<AnalyzedAggregation> analyze(Expression expression)
        {
            aggregations = new ArrayList<>();
            process(expression, null);

            return aggregations;
        }

        @Override
        protected Void visitFunctionCall(FunctionCall node, FunctionCall enclosingAggregate)
        {
            ImmutableList.Builder<AnalyzedExpression> argumentsAnalysis = ImmutableList.builder();
            ImmutableList.Builder<Type> argumentTypes = ImmutableList.builder();
            for (Expression expression : node.getArguments()) {
                AnalyzedExpression analysis = new ExpressionAnalyzer(metadata).analyze(expression, descriptor);
                argumentsAnalysis.add(analysis);
                argumentTypes.add(analysis.getType());
            }

            FunctionInfo info = metadata.getFunction(node.getName(), argumentTypes.build());

            if (info != null && info.isAggregate()) {
                if (enclosingAggregate != null) {
                    throw new SemanticException(node, "Cannot nest aggregate functions: %s", ExpressionFormatter.toString(enclosingAggregate));
                }

                FunctionCall rewritten = TreeRewriter.rewriteWith(new NameToSlotRewriter(descriptor), node);
                aggregations.add(new AnalyzedAggregation(info, argumentsAnalysis.build(), rewritten));
                return super.visitFunctionCall(node, node); // visit children
            }

            return super.visitFunctionCall(node, null);
        }
    }

    private static class RelationAnalyzer
            extends DefaultTraversalVisitor<TupleDescriptor, AnalysisContext>
    {
        private final SessionMetadata metadata;

        private RelationAnalyzer(SessionMetadata metadata)
        {
            this.metadata = metadata;
        }

        @Override
        protected TupleDescriptor visitTable(Table table, AnalysisContext context)
        {
            TableMetadata tableMetadata = metadata.getTable(table.getName());

            if (tableMetadata == null) {
                throw new SemanticException(table, "Cannot resolve table '%s'", table.getName());
            }

            ImmutableList.Builder<NamedSlot> slots = ImmutableList.builder();
            for (ColumnMetadata column : tableMetadata.getColumns()) {
                Slot slot = context.getSlotAllocator().newSlot(Type.fromRaw(column.getType()));
                QualifiedName name = QualifiedName.of(tableMetadata.getCatalogName(), tableMetadata.getSchemaName(), tableMetadata.getTableName(), column.getName());

                slots.add(new NamedSlot(Optional.of(name), slot));
            }

            TupleDescriptor descriptor = new TupleDescriptor(slots.build());
            context.registerTable(table, descriptor);
            return descriptor;
        }

        @Override
        protected TupleDescriptor visitAliasedRelation(AliasedRelation relation, AnalysisContext context)
        {
            if (relation.getColumnNames() != null && !relation.getColumnNames().isEmpty()) {
                throw new UnsupportedOperationException("not yet implemented: column mappings in relation alias");
            }

            TupleDescriptor child = process(relation.getRelation(), context);

            ImmutableList.Builder<NamedSlot> builder = ImmutableList.builder();
            for (NamedSlot slot : child.getSlots()) {
                Optional<QualifiedName> name = slot.getName();
                if (name.isPresent()) {
                    name = Optional.of(QualifiedName.of(relation.getAlias(), name.get().getSuffix()));
                }

                builder.add(new NamedSlot(name, slot.getSlot()));
            }

            return new TupleDescriptor(builder.build());
        }

        @Override
        protected TupleDescriptor visitSubquery(Subquery node, AnalysisContext context)
        {
            AnalysisResult analysis = new Analyzer(metadata).analyze(node.getQuery(), new AnalysisContext(context.getSlotAllocator()));

            context.registerInlineView(node, analysis);
            return analysis.getOutputDescriptor();
        }

        @Override
        protected TupleDescriptor visitJoin(Join node, AnalysisContext context)
        {
            throw new UnsupportedOperationException("not yet implemented: JOIN");
        }
    }

}