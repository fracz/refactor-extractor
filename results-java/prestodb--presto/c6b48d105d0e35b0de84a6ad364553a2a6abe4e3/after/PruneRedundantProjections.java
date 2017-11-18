package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.compiler.Slot;
import com.facebook.presto.sql.compiler.SlotReference;
import com.facebook.presto.sql.tree.Expression;
import com.google.common.collect.Iterables;

import java.util.Map;

/**
 * Removes pure identity projections (e.g., Project $0 := $0, $1 := $1, ...)
 */
public class PruneRedundantProjections
        extends PlanOptimizer
{
    @Override
    public PlanNode optimize(PlanNode plan)
    {
        return plan.accept(new Visitor(), null);
    }

    private static class Visitor
            extends PlanVisitor<Void, PlanNode>
    {
        @Override
        public PlanNode visitProject(ProjectNode node, Void context)
        {
            PlanNode source = node.getSource().accept(this, context);

            boolean canElide = true;
            for (Map.Entry<Slot, Expression> entry : node.getOutputMap().entrySet()) {
                Expression expression = entry.getValue();
                Slot slot = entry.getKey();
                if (!(expression instanceof SlotReference && ((SlotReference) expression).getSlot().equals(slot))) {
                    canElide = false;
                    break;
                }
            }

            if (canElide) {
                return source;
            }

            return new ProjectNode(source, node.getOutputMap());
        }

        @Override
        public PlanNode visitAggregation(AggregationNode node, Void context)
        {
            PlanNode source = node.getSource().accept(this, context);
            return new AggregationNode(source, node.getGroupBy(), node.getAggregations(), node.getFunctionInfos());
        }

        @Override
        public PlanNode visitAlign(AlignNode node, Void context)
        {
            return node;
        }

        @Override
        public PlanNode visitColumnScan(ColumnScan node, Void context)
        {
            return node;
        }

        @Override
        public PlanNode visitFilter(FilterNode node, Void context)
        {
            PlanNode source = node.getSource().accept(this, context);
            return new FilterNode(source, node.getPredicate(), node.getOutputs());
        }

        @Override
        public PlanNode visitOutput(OutputPlan node, Void context)
        {
            PlanNode source = node.getSource().accept(this, context);
            return new OutputPlan(source, node.getColumnNames());
        }

        @Override
        protected PlanNode visitPlan(PlanNode node, Void context)
        {
            throw new UnsupportedOperationException("not yet implemented: " + getClass().getName());
        }
    }
}