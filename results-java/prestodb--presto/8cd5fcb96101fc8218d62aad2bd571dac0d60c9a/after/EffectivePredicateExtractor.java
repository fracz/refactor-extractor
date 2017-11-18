package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.FilterNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.LimitNode;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanVisitor;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.SortNode;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import com.facebook.presto.sql.planner.plan.TopNNode;
import com.facebook.presto.sql.planner.plan.UnionNode;
import com.facebook.presto.sql.planner.plan.WindowNode;
import com.facebook.presto.sql.tree.BooleanLiteral;
import com.facebook.presto.sql.tree.ComparisonExpression;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.IsNullPredicate;
import com.facebook.presto.sql.tree.QualifiedNameReference;
import com.facebook.presto.sql.tree.TreeRewriter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.facebook.presto.sql.ExpressionUtils.and;
import static com.facebook.presto.sql.ExpressionUtils.combineConjuncts;
import static com.facebook.presto.sql.ExpressionUtils.extractConjuncts;
import static com.facebook.presto.sql.ExpressionUtils.or;
import static com.facebook.presto.sql.ExpressionUtils.stripNonDeterministicConjuncts;
import static com.facebook.presto.sql.planner.EqualityInference.createEqualityInference;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Computes the effective predicate at the top of the specified PlanNode
 *
 * Note: non-deterministic predicates can not be pulled up (so they will be ignored)
 */
public class EffectivePredicateExtractor
        extends PlanVisitor<Void, Expression>
{
    public static Expression extract(PlanNode node)
    {
        return node.accept(new EffectivePredicateExtractor(), null);
    }

    @Override
    protected Expression visitPlan(PlanNode node, Void context)
    {
        return BooleanLiteral.TRUE_LITERAL;
    }

    @Override
    public Expression visitAggregation(AggregationNode node, Void context)
    {
        Expression underlyingPredicate = node.getSource().accept(this, context);

        EqualityInference equalityInference = createEqualityInference(underlyingPredicate);

        ImmutableList.Builder<Expression> effectiveConjuncts = ImmutableList.builder();
        for (Expression conjunct : EqualityInference.nonInferrableConjuncts(underlyingPredicate)) {
            Expression rewritten = equalityInference.rewriteExpression(conjunct, in(node.getGroupBy()));
            if (rewritten != null) {
                effectiveConjuncts.add(rewritten);
            }
        }

        effectiveConjuncts.addAll(equalityInference.generateEqualitiesPartitionedBy(in(node.getGroupBy())).getScopeEqualities());

        return combineConjuncts(effectiveConjuncts.build());
    }

    @Override
    public Expression visitFilter(FilterNode node, Void context)
    {
        Expression underlyingPredicate = node.getSource().accept(this, context);

        Expression predicate = node.getPredicate();

        // Remove non-deterministic conjuncts
        predicate = stripNonDeterministicConjuncts(predicate);

        return combineConjuncts(predicate, underlyingPredicate);
    }

    @Override
    public Expression visitProject(ProjectNode node, Void context)
    {
        // TODO: add simple algebraic solver for projection translation (right now only considers identity projections)

        Expression underlyingPredicate = node.getSource().accept(this, context);

        Iterable<Expression> projectionEqualities = transform(node.getOutputMap().entrySet(), new Function<Map.Entry<Symbol, Expression>, Expression>()
        {
            @Override
            public Expression apply(Map.Entry<Symbol, Expression> entry)
            {
                QualifiedNameReference reference = new QualifiedNameReference(entry.getKey().toQualifiedName());
                Expression expression = entry.getValue();
                return new ComparisonExpression(ComparisonExpression.Type.EQUAL, reference, expression);
            }
        });

        EqualityInference projectionInference = createEqualityInference(combineConjuncts(projectionEqualities), underlyingPredicate);

        ImmutableList.Builder<Expression> effectiveConjuncts = ImmutableList.builder();
        for (Expression conjunct : EqualityInference.nonInferrableConjuncts(underlyingPredicate)) {
            Expression rewritten = projectionInference.rewriteExpression(conjunct, in(node.getOutputSymbols()));
            if (rewritten != null) {
                effectiveConjuncts.add(rewritten);
            }
        }
        effectiveConjuncts.addAll(projectionInference.generateEqualitiesPartitionedBy(in(node.getOutputSymbols())).getScopeEqualities());

        return combineConjuncts(effectiveConjuncts.build());
    }

    @Override
    public Expression visitTopN(TopNNode node, Void context)
    {
        return node.getSource().accept(this, context);
    }

    @Override
    public Expression visitLimit(LimitNode node, Void context)
    {
        return node.getSource().accept(this, context);
    }

    @Override
    public Expression visitTableScan(TableScanNode node, Void context)
    {
        // TODO: we can provide even better predicates if the metadata system is able to provide us with accurate bounds on the data sets
        Expression partitionPredicate = node.getPartitionPredicate();
        checkState(DeterminismEvaluator.isDeterministic(partitionPredicate));

        EqualityInference equalityInference = createEqualityInference(partitionPredicate);

        ImmutableList.Builder<Expression> effectiveConjuncts = ImmutableList.builder();
        for (Expression conjunct : EqualityInference.nonInferrableConjuncts(partitionPredicate)) {
            Expression rewritten = equalityInference.rewriteExpression(conjunct, in(node.getOutputSymbols()));
            if (rewritten != null) {
                effectiveConjuncts.add(rewritten);
            }
        }

        effectiveConjuncts.addAll(equalityInference.generateEqualitiesPartitionedBy(in(node.getOutputSymbols())).getScopeEqualities());

        return combineConjuncts(effectiveConjuncts.build());
    }

    @Override
    public Expression visitSort(SortNode node, Void context)
    {
        return node.getSource().accept(this, context);
    }

    @Override
    public Expression visitWindow(WindowNode node, Void context)
    {
        return node.getSource().accept(this, context);
    }

    @Override
    public Expression visitUnion(UnionNode node, Void context)
    {
        Expression firstUnderlyingPredicate = node.getSources().get(0).accept(this, context);
        // Rewrite in terms of output symbols
        Expression firstOutputPredicate = TreeRewriter.rewriteWith(new ExpressionSymbolInliner(node.outputSymbolMap(0)), firstUnderlyingPredicate);

        Set<Expression> conjuncts = ImmutableSet.copyOf(extractConjuncts(firstOutputPredicate));

        // Find the intersection of all predicates
        for (int i = 1; i < node.getSources().size(); i++) {
            Expression underlyingPredicate = node.getSources().get(i).accept(this, context);
            // Rewrite in terms of output symbols
            Expression outputPredicate = TreeRewriter.rewriteWith(new ExpressionSymbolInliner(node.outputSymbolMap(i)), underlyingPredicate);

            // TODO: use a more precise way to determine overlapping conjuncts (e.g. commutative predicates)
            conjuncts = Sets.intersection(conjuncts, ImmutableSet.copyOf(extractConjuncts(outputPredicate)));
        }

        return combineConjuncts(conjuncts);
    }

    @Override
    public Expression visitJoin(JoinNode node, Void context)
    {
        Expression leftPredicate = node.getLeft().accept(this, context);
        Expression rightPredicate = node.getRight().accept(this, context);

        List<Expression> joinConjuncts = new ArrayList<>();
        for (JoinNode.EquiJoinClause clause : node.getCriteria()) {
            joinConjuncts.add(new ComparisonExpression(ComparisonExpression.Type.EQUAL,
                    new QualifiedNameReference(clause.getLeft().toQualifiedName()),
                    new QualifiedNameReference(clause.getRight().toQualifiedName())));
        }

        switch (node.getType()) {
            case INNER:
                return combineConjuncts(ImmutableList.<Expression>builder()
                        .add(leftPredicate)
                        .add(rightPredicate)
                        .addAll(joinConjuncts)
                        .build());
            case LEFT:
                return combineConjuncts(ImmutableList.<Expression>builder()
                        .add(leftPredicate)
                        .addAll(transform(extractConjuncts(rightPredicate), expressionOrNullSymbols(in(node.getRight().getOutputSymbols()))))
                        .addAll(transform(joinConjuncts, expressionOrNullSymbols(in(node.getRight().getOutputSymbols()))))
                        .build());
            case RIGHT:
                return combineConjuncts(ImmutableList.<Expression>builder()
                        .add(rightPredicate)
                        .addAll(transform(extractConjuncts(leftPredicate), expressionOrNullSymbols(in(node.getLeft().getOutputSymbols()))))
                        .addAll(transform(joinConjuncts, expressionOrNullSymbols(in(node.getLeft().getOutputSymbols()))))
                        .build());
            default:
                throw new UnsupportedOperationException("Unknown join type: " + node.getType());
        }
    }

    private static Function<Expression, Expression> expressionOrNullSymbols(final Predicate<Symbol> nullSymbolScope)
    {
        return new Function<Expression, Expression>()
        {
            @Override
            public Expression apply(Expression expression)
            {
                Iterable<Symbol> symbols = filter(DependencyExtractor.extract(expression), nullSymbolScope);
                if (Iterables.isEmpty(symbols)) {
                    return expression;
                }

                ImmutableList.Builder<Expression> nullConjuncts = ImmutableList.builder();
                for (Symbol symbol : symbols) {
                    nullConjuncts.add(new IsNullPredicate(new QualifiedNameReference(symbol.toQualifiedName())));
                }
                return or(expression, and(nullConjuncts.build()));
            }
        };
    }
}