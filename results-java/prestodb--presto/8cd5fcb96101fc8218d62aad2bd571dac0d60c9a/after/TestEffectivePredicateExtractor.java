package com.facebook.presto.sql.planner;

import com.facebook.presto.connector.dual.DualColumnHandle;
import com.facebook.presto.connector.dual.DualTableHandle;
import com.facebook.presto.metadata.FunctionHandle;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.FilterNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.LimitNode;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanNodeId;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.SortNode;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import com.facebook.presto.sql.planner.plan.TopNNode;
import com.facebook.presto.sql.planner.plan.UnionNode;
import com.facebook.presto.sql.planner.plan.WindowNode;
import com.facebook.presto.sql.tree.BooleanLiteral;
import com.facebook.presto.sql.tree.ComparisonExpression;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.FunctionCall;
import com.facebook.presto.sql.tree.IsNullPredicate;
import com.facebook.presto.sql.tree.LongLiteral;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.sql.tree.QualifiedNameReference;
import com.facebook.presto.sql.tree.SortItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.weakref.jmx.com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.facebook.presto.sql.ExpressionUtils.and;
import static com.facebook.presto.sql.ExpressionUtils.extractConjuncts;
import static com.facebook.presto.sql.ExpressionUtils.or;

public class TestEffectivePredicateExtractor
{
    private static final Symbol A = new Symbol("a");
    private static final Symbol B = new Symbol("b");
    private static final Symbol C = new Symbol("c");
    private static final Symbol D = new Symbol("d");
    private static final Symbol E = new Symbol("e");
    private static final Symbol F = new Symbol("f");
    private static final Expression AE = symbolExpr(A);
    private static final Expression BE = symbolExpr(B);
    private static final Expression CE = symbolExpr(C);
    private static final Expression DE = symbolExpr(D);
    private static final Expression EE = symbolExpr(E);
    private static final Expression FE = symbolExpr(F);

    private Map<Symbol, ColumnHandle> scanAssignments;
    private TableScanNode baseTableScan;

    @BeforeMethod
    public void setUp()
            throws Exception
    {
        scanAssignments = ImmutableMap.<Symbol, ColumnHandle>builder()
                .put(A, new DualColumnHandle("a"))
                .put(B, new DualColumnHandle("b"))
                .put(C, new DualColumnHandle("c"))
                .put(D, new DualColumnHandle("d"))
                .put(E, new DualColumnHandle("e"))
                .put(F, new DualColumnHandle("f"))
                .build();

        baseTableScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(A, B, C, D, E, F),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );
    }

    @Test
    public void testAggregation()
            throws Exception
    {
        PlanNode node = new AggregationNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, DE),
                                equals(BE, EE),
                                equals(CE, FE),
                                lessThan(DE, number(10)),
                                lessThan(CE, DE),
                                greaterThan(AE, number(2)),
                                equals(EE, FE))),
                ImmutableList.of(A, B, C),
                ImmutableMap.of(C, fakeFunction("test"), D, fakeFunction("test")),
                ImmutableMap.of(C, fakeFunctionHandle("test"), D, fakeFunctionHandle("test")),
                AggregationNode.Step.FINAL);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Rewrite in terms of group by symbols
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        lessThan(AE, number(10)),
                        lessThan(BE, AE),
                        greaterThan(AE, number(2)),
                        equals(BE, CE)));
    }

    @Test
    public void testFilter()
            throws Exception
    {
        PlanNode node = filter(baseTableScan,
                and(
                        greaterThan(AE, new FunctionCall(QualifiedName.of("rand"), ImmutableList.<Expression>of())),
                        lessThan(BE, number(10))));

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Non-deterministic functions should be purged
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(lessThan(BE, number(10))));
    }

    @Test
    public void testProject()
            throws Exception
    {
        PlanNode node = new ProjectNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, BE),
                                equals(BE, CE),
                                lessThan(CE, number(10)))),
                ImmutableMap.of(D, AE, E, CE));

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Rewrite in terms of project output symbols
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        lessThan(DE, number(10)),
                        equals(DE, EE)));
    }

    @Test
    public void testTopN()
            throws Exception
    {
        PlanNode node = new TopNNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, BE),
                                equals(BE, CE),
                                lessThan(CE, number(10)))),
                1, ImmutableList.of(A), ImmutableMap.of(A, SortItem.Ordering.ASCENDING), true);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Pass through
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        equals(AE, BE),
                        equals(BE, CE),
                        lessThan(CE, number(10))));
    }

    @Test
    public void testLimit()
            throws Exception
    {
        PlanNode node = new LimitNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, BE),
                                equals(BE, CE),
                                lessThan(CE, number(10)))),
                1);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Pass through
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        equals(AE, BE),
                        equals(BE, CE),
                        lessThan(CE, number(10))));
    }

    @Test
    public void testSort()
            throws Exception
    {
        PlanNode node = new SortNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, BE),
                                equals(BE, CE),
                                lessThan(CE, number(10)))),
                ImmutableList.of(A), ImmutableMap.of(A, SortItem.Ordering.ASCENDING));

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Pass through
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        equals(AE, BE),
                        equals(BE, CE),
                        lessThan(CE, number(10))));
    }

    @Test
    public void testWindow()
            throws Exception
    {
        PlanNode node = new WindowNode(newId(),
                filter(baseTableScan,
                        and(
                                equals(AE, BE),
                                equals(BE, CE),
                                lessThan(CE, number(10)))),
                ImmutableList.of(A),
                ImmutableList.of(A),
                ImmutableMap.of(A, SortItem.Ordering.ASCENDING),
                ImmutableMap.<Symbol, FunctionCall>of(),
                ImmutableMap.<Symbol, FunctionHandle>of());

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Pass through
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        equals(AE, BE),
                        equals(BE, CE),
                        lessThan(CE, number(10))));
    }

    @Test
    public void testTableScan()
            throws Exception
    {
        PlanNode node = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(A, B, C),
                scanAssignments,
                and(greaterThan(BE, number(0)), equals(AE, DE), lessThan(DE, number(10)), lessThan(EE, number(3))),
                BooleanLiteral.TRUE_LITERAL);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Only those that can be written in terms of the output symbols are extracted
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(
                        greaterThan(BE, number(0)),
                        lessThan(AE, number(10))));
    }

    @Test
    public void testUnion()
            throws Exception
    {
        PlanNode node = new UnionNode(newId(),
                ImmutableList.<PlanNode>of(
                        filter(baseTableScan, greaterThan(AE, number(10))),
                        filter(baseTableScan, and(greaterThan(AE, number(10)), lessThan(AE, number(100)))),
                        filter(baseTableScan, and(greaterThan(AE, number(10)), lessThan(AE, number(100))))
                ),
                ImmutableListMultimap.of(A, B, A, C, A, E)
        );

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // Only the common conjuncts can be inferred through a Union
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(greaterThan(AE, number(10))));
    }

    @Test
    public void testInnerJoin()
            throws Exception
    {
        ImmutableList.Builder<JoinNode.EquiJoinClause> criteriaBuilder = ImmutableList.builder();
        criteriaBuilder.add(new JoinNode.EquiJoinClause(A, D));
        criteriaBuilder.add(new JoinNode.EquiJoinClause(B, E));
        List<JoinNode.EquiJoinClause> criteria = criteriaBuilder.build();

        TableScanNode leftScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(A, B, C),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        TableScanNode rightScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(D, E, F),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        PlanNode node = new JoinNode(newId(),
                JoinNode.Type.INNER,
                filter(leftScan,
                        and(
                                lessThan(BE, AE),
                                lessThan(CE, number(10)))),
                filter(rightScan,
                        and(
                                equals(DE, EE),
                                lessThan(FE, number(100)))),
                criteria);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // All predicates should be carried through
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(lessThan(BE, AE),
                        lessThan(CE, number(10)),
                        equals(DE, EE),
                        lessThan(FE, number(100)),
                        equals(AE, DE),
                        equals(BE, EE)));
    }

    @Test
    public void testLeftJoin()
            throws Exception
    {
        ImmutableList.Builder<JoinNode.EquiJoinClause> criteriaBuilder = ImmutableList.builder();
        criteriaBuilder.add(new JoinNode.EquiJoinClause(A, D));
        criteriaBuilder.add(new JoinNode.EquiJoinClause(B, E));
        List<JoinNode.EquiJoinClause> criteria = criteriaBuilder.build();

        TableScanNode leftScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(A, B, C),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        TableScanNode rightScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(D, E, F),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        PlanNode node = new JoinNode(newId(),
                JoinNode.Type.LEFT,
                filter(leftScan,
                        and(
                                lessThan(BE, AE),
                                lessThan(CE, number(10)))),
                filter(rightScan,
                        and(
                                equals(DE, EE),
                                lessThan(FE, number(100)))),
                criteria);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // All right side symbols should be checked against NULL
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(lessThan(BE, AE),
                        lessThan(CE, number(10)),
                        or(equals(DE, EE), and(isNull(DE), isNull(EE))),
                        or(lessThan(FE, number(100)), isNull(FE)),
                        or(equals(AE, DE), isNull(DE)),
                        or(equals(BE, EE), isNull(EE))));
    }

    @Test
    public void testRightJoin()
            throws Exception
    {
        ImmutableList.Builder<JoinNode.EquiJoinClause> criteriaBuilder = ImmutableList.builder();
        criteriaBuilder.add(new JoinNode.EquiJoinClause(A, D));
        criteriaBuilder.add(new JoinNode.EquiJoinClause(B, E));
        List<JoinNode.EquiJoinClause> criteria = criteriaBuilder.build();

        TableScanNode leftScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(A, B, C),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        TableScanNode rightScan = new TableScanNode(
                newId(),
                new DualTableHandle("default"),
                ImmutableList.of(D, E, F),
                scanAssignments,
                BooleanLiteral.TRUE_LITERAL,
                BooleanLiteral.TRUE_LITERAL
        );

        PlanNode node = new JoinNode(newId(),
                JoinNode.Type.RIGHT,
                filter(leftScan,
                        and(
                                lessThan(BE, AE),
                                lessThan(CE, number(10)))),
                filter(rightScan,
                        and(
                                equals(DE, EE),
                                lessThan(FE, number(100)))),
                criteria);

        Expression effectivePredicate = EffectivePredicateExtractor.extract(node);

        // All left side symbols should be checked against NULL
        Assert.assertEquals(conjunctsAsSet(effectivePredicate),
                set(or(lessThan(BE, AE), and(isNull(BE), isNull(AE))),
                        or(lessThan(CE, number(10)), isNull(CE)),
                        equals(DE, EE),
                        lessThan(FE, number(100)),
                        or(equals(AE, DE), isNull(AE)),
                        or(equals(BE, EE), isNull(BE))));
    }

    private static PlanNodeId newId()
    {
        return new PlanNodeId(UUID.randomUUID().toString());
    }

    private static FilterNode filter(PlanNode source, Expression predicate)
    {
        return new FilterNode(newId(), source, predicate);
    }

    private static Expression symbolExpr(Symbol symbol)
    {
        return new QualifiedNameReference(symbol.toQualifiedName());
    }

    private static Expression number(long number)
    {
        return new LongLiteral(String.valueOf(number));
    }

    private static ComparisonExpression equals(Expression expression1, Expression expression2)
    {
        return new ComparisonExpression(ComparisonExpression.Type.EQUAL, expression1, expression2);
    }

    private static ComparisonExpression lessThan(Expression expression1, Expression expression2)
    {
        return new ComparisonExpression(ComparisonExpression.Type.LESS_THAN, expression1, expression2);
    }

    private static ComparisonExpression greaterThan(Expression expression1, Expression expression2)
    {
        return new ComparisonExpression(ComparisonExpression.Type.GREATER_THAN, expression1, expression2);
    }

    private static IsNullPredicate isNull(Expression expression)
    {
        return new IsNullPredicate(expression);
    }

    private static FunctionCall fakeFunction(String name)
    {
        return new FunctionCall(QualifiedName.of("test"), ImmutableList.<Expression>of());
    }

    private static FunctionHandle fakeFunctionHandle(String name)
    {
        return new FunctionHandle(Math.abs(new Random().nextInt()), name);
    }

    private static Set<Expression> conjunctsAsSet(Expression expression)
    {
        return ImmutableSet.copyOf(extractConjuncts(expression));
    }

    private static <E> Set<E> set(E... elements)
    {
        return ImmutableSet.copyOf(elements);
    }
}