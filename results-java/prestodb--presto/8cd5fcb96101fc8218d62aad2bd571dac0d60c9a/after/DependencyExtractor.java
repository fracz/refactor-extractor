package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.tree.DefaultTraversalVisitor;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.QualifiedNameReference;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// TODO: a similar class exists in (TupleAnalyzer.DependencyExtractor)
public class DependencyExtractor
{
    public static Set<Symbol> extract(Expression expression)
    {
        return ImmutableSet.copyOf(extractOccurrences(expression));
    }

    public static List<Symbol> extractOccurrences(Expression expression)
    {
        ImmutableList.Builder<Symbol> builder = ImmutableList.builder();
        new Visitor().process(expression, builder);
        return builder.build();
    }

    private static class Visitor
            extends DefaultTraversalVisitor<Void, ImmutableList.Builder<Symbol>>
    {
        @Override
        protected Void visitQualifiedNameReference(QualifiedNameReference node, ImmutableList.Builder<Symbol> builder)
        {
            builder.add(Symbol.fromQualifiedName(node.getName()));
            return null;
        }
    }

}