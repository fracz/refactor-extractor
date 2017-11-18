package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.tree.ComparisonExpression;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.NodeRewriter;
import com.facebook.presto.sql.tree.TreeRewriter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.facebook.presto.sql.ExpressionUtils.extractConjuncts;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

/**
 * Makes equality based inferences to rewrite Expressions and generate equality sets in terms of specified symbol scopes
 */
public class EqualityInference
{
    // Ordering used to determine Expression preference when determining canonicals
    private static final Ordering<Expression> CANONICAL_ORDERING = Ordering.from(new Comparator<Expression>()
    {
        @Override
        public int compare(Expression expression1, Expression expression2)
        {
            // Current cost heuristic:
            // 1) Prefer fewer input symbols
            // 2) Prefer smaller expression trees
            // 3) Ordering.arbitrary() - creates a stable consistent ordering (extremely useful for unit testing)
            // TODO: be more precise in determining the cost of an expression
            return ComparisonChain.start()
                    .compare(DependencyExtractor.extractOccurrences(expression1).size(), DependencyExtractor.extractOccurrences(expression2).size())
                    .compare(SubExpressionExtractor.extract(expression1).size(), SubExpressionExtractor.extract(expression2).size())
                    .compare(expression1, expression2, Ordering.arbitrary())
                    .result();
        }
    });

    private final SetMultimap<Expression, Expression> equalitySets; // Indexed by canonical expression
    private final Map<Expression, Expression> canonicalMap; // Map each known expression to canonical expression

    private EqualityInference(Iterable<Set<Expression>> equalityGroups)
    {
        ImmutableSetMultimap.Builder<Expression, Expression> setBuilder = ImmutableSetMultimap.builder();
        for (Set<Expression> equalityGroup : equalityGroups) {
            if (!equalityGroup.isEmpty()) {
                setBuilder.putAll(CANONICAL_ORDERING.min(equalityGroup), equalityGroup);
            }
        }
        equalitySets = setBuilder.build();

        ImmutableMap.Builder<Expression, Expression> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<Expression, Expression> entry : equalitySets.entries()) {
            Expression canonical = entry.getKey();
            Expression expression = entry.getValue();
            mapBuilder.put(expression, canonical);
        }
        canonicalMap = mapBuilder.build();
    }

    /**
     * Attempts to rewrite an Expression in terms of the symbols allowed by the symbol scope
     * given the known equalities. Returns null if unsuccessful.
     */
    public Expression rewriteExpression(Expression expression, Predicate<Symbol> symbolScope)
    {
        checkArgument(DeterminismEvaluator.isDeterministic(expression), "Only deterministic expressions may be considered for rewrite");
        return rewriteExpression(expression, symbolScope, true);
    }

    private Expression rewriteExpression(Expression expression, Predicate<Symbol> symbolScope, boolean allowFullReplacement)
    {
        Iterable<Expression> subExpressions = SubExpressionExtractor.extract(expression);
        if (!allowFullReplacement) {
            subExpressions = filter(subExpressions, not(equalTo(expression)));
        }

        ImmutableMap.Builder<Expression, Expression> expressionRemap = ImmutableMap.builder();
        for (Expression subExpression : subExpressions) {
            Expression canonical = getScopedCanonical(subExpression, symbolScope);
            if (canonical != null) {
                expressionRemap.put(subExpression, canonical);
            }
        }

        // Perform a naive single-pass traversal to try to rewrite non-compliant portions of the tree. Prefers to replace
        // larger subtrees over smaller subtrees
        // TODO: this rewrite can probably be made more sophisticated
        Expression rewritten = TreeRewriter.rewriteWith(new ExpressionNodeInliner(expressionRemap.build()), expression);
        if (!symbolToExpressionPredicate(symbolScope).apply(rewritten)) {
            // If the rewritten is still not compliant with the symbol scope, just give up
            return null;
        }
        return rewritten;
    }

    /**
     * Dumps the inference equalities as equality expressions that are partitioned by the symbolScope.
     */
    public EqualityPartition generateEqualitiesPartitionedBy(Predicate<Symbol> symbolScope)
    {
        Set<Expression> scopeEqualities = new HashSet<>();
        Set<Expression> inverseScopeEqualities = new HashSet<>();
        Set<Expression> scopeStraddlingEqualities = new HashSet<>();

        for (Collection<Expression> equalitySet : equalitySets.asMap().values()) {
            Set<Expression> scopeExpressions = new HashSet<>();
            Set<Expression> inverseScopeExpressions = new HashSet<>();
            Set<Expression> unpartitionableExpressions = new HashSet<>();

            // Try to push each expression into one side of the scope
            for (Expression expression : equalitySet) {
                Expression scopeRewritten = rewriteExpression(expression, symbolScope, false);
                if (scopeRewritten != null) {
                    scopeExpressions.add(scopeRewritten);
                }
                Expression inverseScopeRewritten = rewriteExpression(expression, not(symbolScope), false);
                if (inverseScopeRewritten != null) {
                    inverseScopeExpressions.add(inverseScopeRewritten);
                }
                if (scopeRewritten == null && inverseScopeRewritten == null) {
                    unpartitionableExpressions.add(expression);
                }
            }

            // Compile the equality expressions on each side of the scope
            Expression matchingCanonical = getCanonical(scopeExpressions);
            if (scopeExpressions.size() >= 2) {
                for (Expression expression : filter(scopeExpressions, not(equalTo(matchingCanonical)))) {
                    scopeEqualities.add(new ComparisonExpression(ComparisonExpression.Type.EQUAL, matchingCanonical, expression));
                }
            }
            Expression inverseCanonical = getCanonical(inverseScopeExpressions);
            if (inverseScopeExpressions.size() >= 2) {
                for (Expression expression : filter(inverseScopeExpressions, not(equalTo(inverseCanonical)))) {
                    inverseScopeEqualities.add(new ComparisonExpression(ComparisonExpression.Type.EQUAL, inverseCanonical, expression));
                }
            }

            // Compile the scope straddling equality expressions
            List<Expression> connectingExpressions = new ArrayList<>();
            connectingExpressions.add(matchingCanonical);
            connectingExpressions.add(inverseCanonical);
            connectingExpressions.addAll(unpartitionableExpressions);
            connectingExpressions = ImmutableList.copyOf(filter(connectingExpressions, Predicates.notNull()));
            Expression connectingCanonical = getCanonical(connectingExpressions);
            if (connectingCanonical != null) {
                for (Expression expression : filter(connectingExpressions, not(equalTo(connectingCanonical)))) {
                    scopeStraddlingEqualities.add(new ComparisonExpression(ComparisonExpression.Type.EQUAL, connectingCanonical, expression));
                }
            }
        }

        return new EqualityPartition(scopeEqualities, inverseScopeEqualities, scopeStraddlingEqualities);
    }

    /**
     * Returns the most preferrable expression to be used as the canonical expression
     */
    private static Expression getCanonical(Iterable<Expression> expressions)
    {
        if (Iterables.isEmpty(expressions)) {
            return null;
        }
        return CANONICAL_ORDERING.min(expressions);
    }

    /**
     * Returns a canonical expression that is fully contained by the symbolScope and that is equivalent
     * to the specified expression. Returns null if unable to to find a canonical.
     */
    @VisibleForTesting
    Expression getScopedCanonical(Expression expression, Predicate<Symbol> symbolScope)
    {
        Expression canonicalIndex = canonicalMap.get(expression);
        if (canonicalIndex == null) {
            return null;
        }
        return getCanonical(filter(equalitySets.get(canonicalIndex), symbolToExpressionPredicate(symbolScope)));
    }

    private static Predicate<Expression> symbolToExpressionPredicate(final Predicate<Symbol> symbolScope)
    {
        return new Predicate<Expression>()
        {
            @Override
            public boolean apply(Expression expression)
            {
                return Iterables.all(DependencyExtractor.extract(expression), symbolScope);
            }
        };
    }

    /**
     * Determines whether an Expression may be successfully applied to the equality inference
     */
    public static Predicate<Expression> inferrableEqualityExpression()
    {
        return new Predicate<Expression>()
        {
            @Override
            public boolean apply(Expression expression)
            {
                if (DeterminismEvaluator.isDeterministic(expression) && expression instanceof ComparisonExpression) {
                    ComparisonExpression comparison = (ComparisonExpression) expression;
                    if (comparison.getType() == ComparisonExpression.Type.EQUAL) {
                        // We should only consider equalities that have distinct left and right components
                        return !comparison.getLeft().equals(comparison.getRight());
                    }
                }
                return false;
            }
        };
    }

    /**
     * Provides a convenience Iterable of Expression conjuncts which have not been added to the inference
     */
    public static Iterable<Expression> nonInferrableConjuncts(Expression expression)
    {
        return filter(extractConjuncts(expression), not(inferrableEqualityExpression()));
    }

    /**
     * Extracts and returns the set of all expression subtrees within an Expression
     */
    private static class SubExpressionExtractor
    {
        public static Set<Expression> extract(Expression expression)
        {
            ImmutableSet.Builder<Expression> builder = ImmutableSet.builder();
            // Borrow the TreeRewriter as a way to walk the Expression tree with a visitor that still respects the type inheritance
            // We actually don't care about the rewrite result, just the context that gets updated through the walk
            TreeRewriter.rewriteWith(new Visitor(), expression, builder);
            return builder.build();
        }

        private static class Visitor
                extends NodeRewriter<ImmutableSet.Builder<Expression>>
        {
            @Override
            public Node rewriteExpression(Expression node, ImmutableSet.Builder<Expression> builder, TreeRewriter<ImmutableSet.Builder<Expression>> treeRewriter)
            {
                builder.add(node);
                return null;
            }
        }
    }

    public static EqualityInference createEqualityInference(Expression... expressions)
    {
        EqualityInference.Builder builder = new EqualityInference.Builder();
        for (Expression expression : expressions) {
            builder.extractInferrableEqualities(expression);
        }
        return builder.build();
    }

    public static class EqualityPartition
    {
        private final List<Expression> scopeEqualities;
        private final List<Expression> inverseScopeEqualities;
        private final List<Expression> scopeStraddlingEqualities;

        public EqualityPartition(Iterable<Expression> scopeEqualities, Iterable<Expression> inverseScopeEqualities, Iterable<Expression> scopeStraddlingEqualities)
        {
            this.scopeEqualities = ImmutableList.copyOf(checkNotNull(scopeEqualities, "scopeEqualities is null"));
            this.inverseScopeEqualities = ImmutableList.copyOf(checkNotNull(inverseScopeEqualities, "inverseScopeEqualities is null"));
            this.scopeStraddlingEqualities = ImmutableList.copyOf(checkNotNull(scopeStraddlingEqualities, "scopeStraddlingEqualities is null"));
        }

        public List<Expression> getScopeEqualities()
        {
            return scopeEqualities;
        }

        public List<Expression> getInverseScopeEqualities()
        {
            return inverseScopeEqualities;
        }

        public List<Expression> getScopeStraddlingEqualities()
        {
            return scopeStraddlingEqualities;
        }
    }

    public static class Builder
    {
        private final Map<Expression, Expression> map = new HashMap<>();
        private final Multimap<Expression, Expression> reverseMap = HashMultimap.create();

        public Builder extractInferrableEqualities(Expression expression)
        {
            return addAllEqualities(filter(extractConjuncts(expression), inferrableEqualityExpression()));
        }

        public Builder addAllEqualities(Iterable<Expression> expressions)
        {
            for (Expression expression : expressions) {
                addEquality(expression);
            }
            return this;
        }

        public Builder addEquality(Expression expression)
        {
            checkArgument(inferrableEqualityExpression().apply(expression), "Expression must be a simple equality: " + expression);

            ComparisonExpression comparison = (ComparisonExpression) expression;
            addEquality(comparison.getLeft(), comparison.getRight());
            return this;
        }

        public Builder addEquality(Expression expression1, Expression expression2)
        {
            checkArgument(!expression1.equals(expression2), "need to provide equality between different expressions");
            checkArgument(DeterminismEvaluator.isDeterministic(expression1), "Expression must be deterministic: " + expression1);
            checkArgument(DeterminismEvaluator.isDeterministic(expression2), "Expression must be deterministic: " + expression2);

            Expression canonical1 = canonicalize(expression1);
            Expression canonical2 = canonicalize(expression2);

            if (!canonical1.equals(canonical2)) {
                map.put(canonical1, canonical2);
                reverseMap.put(canonical2, canonical1);
            }
            return this;
        }

        private Expression canonicalize(Expression expression)
        {
            while (map.containsKey(expression)) {
                expression = map.get(expression);
            }
            return expression;
        }

        private void collectEqualities(Expression expression, ImmutableSet.Builder<Expression> builder)
        {
            builder.add(expression);
            for (Expression childExpression : reverseMap.get(expression)) {
                collectEqualities(childExpression, builder);
            }
        }

        private Set<Expression> extractEqualExpressions(Expression expression)
        {
            ImmutableSet.Builder<Expression> builder = ImmutableSet.builder();
            collectEqualities(canonicalize(expression), builder);
            return builder.build();
        }

        public EqualityInference build()
        {
            HashSet<Expression> seenCanonicals = new HashSet<>();
            ImmutableList.Builder<Set<Expression>> builder = ImmutableList.builder();
            for (Expression expression : map.keySet()) {
                Expression canonical = canonicalize(expression);
                if (!seenCanonicals.contains(canonical)) {
                    builder.add(extractEqualExpressions(canonical));
                    seenCanonicals.add(canonical);
                }
            }
            return new EqualityInference(builder.build());
        }
    }
}