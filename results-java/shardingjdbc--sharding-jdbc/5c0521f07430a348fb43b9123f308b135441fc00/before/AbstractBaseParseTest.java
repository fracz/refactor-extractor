/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Asserts;
import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.Column;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class AbstractBaseParseTest {

    @Getter(AccessLevel.PROTECTED)
    private final String testCaseName;

    @Getter(AccessLevel.PROTECTED)
    private final String sql;

    private final String expectedSQL;

    private final Iterator<TableContext> expectedTables;

    private final Iterator<ConditionContext> expectedConditionContexts;

    private final Iterator<OrderByContext> orderByContexts;

    private final Iterator<GroupByContext> groupByContexts;

    private final Iterator<AggregationSelectItemContext> aggregationColumns;

    private final LimitContext limit;

    protected AbstractBaseParseTest(final String testCaseName, final String sql, final String expectedSQL,
                                 final Collection<TableContext> expectedTables, final Collection<ConditionContext> expectedConditionContext, final MergeContext expectedMergeContext) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.expectedSQL = expectedSQL;
        this.expectedTables = expectedTables.iterator();
        this.expectedConditionContexts = expectedConditionContext.iterator();
        this.orderByContexts = expectedMergeContext.getOrderByContexts().iterator();
        this.groupByContexts = expectedMergeContext.getGroupByContexts().iterator();
        this.aggregationColumns = expectedMergeContext.getAggregationColumns().iterator();
        this.limit = expectedMergeContext.getLimit();
    }

    protected static Collection<Object[]> dataParameters(final String path) {
        Collection<Object[]> result = new ArrayList<>();
        for (File each : new File(AbstractBaseParseTest.class.getClassLoader().getResource(path).getPath()).listFiles()) {
            result.addAll(dataParameters(each));
        }
        return result;
    }

    private static Collection<Object[]> dataParameters(final File file) {
        Asserts asserts = loadAsserts(file);
        Object[][] result = new Object[asserts.getAsserts().size()][6];
        for (int i = 0; i < asserts.getAsserts().size(); i++) {
            result[i] = getDataParameter(asserts.getAsserts().get(i));
        }
        return Arrays.asList(result);
    }

    private static Asserts loadAsserts(final File file) {
        try {
            return (Asserts) JAXBContext.newInstance(Asserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Object[] getDataParameter(final Assert assertObj) {
        Object[] result = new Object[6];
        result[0] = assertObj.getId();
        result[1] = assertObj.getSql();
        result[2] = assertObj.getExpectedSQL();
        result[3] = Lists.transform(assertObj.getTables(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.Table, TableContext>() {

            @Override
            public TableContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.Table input) {
                return new TableContext(input.getName(), Optional.of(input.getAlias()));
            }
        });
        if (null == assertObj.getConditionContexts()) {
            result[4] = Collections.<ConditionContext>emptyList();
        } else {
            result[4] = Lists.transform(assertObj.getConditionContexts(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.ConditionContext, ConditionContext>() {

                @Override
                public ConditionContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.ConditionContext input) {
                    ConditionContext result = new ConditionContext();
                    if (null == input.getConditions()) {
                        return result;
                    }
                    for (com.dangdang.ddframe.rdb.sharding.parser.jaxb.Condition each : input.getConditions()) {
                        Condition condition = new Condition(new Column(each.getColumnName(), each.getTableName()), BinaryOperator.valueOf(each.getOperator().toUpperCase()));
                        condition.getValues().addAll(Lists.transform(each.getValues(), new Function<Value, Comparable<?>>() {

                            @Override
                            public Comparable<?> apply(final Value input) {
                                return input.getValueWithType();
                            }
                        }));
                        if (null != each.getValueIndices()) {
                            condition.getValueIndices().addAll(each.getValueIndices());
                        }
                        result.add(condition);
                    }
                    return result;
                }
            });
        }
        MergeContext mergeContext = new MergeContext();
        if (null != assertObj.getOrderByColumns()) {
            mergeContext.getOrderByContexts().addAll(Lists.transform(assertObj.getOrderByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.OrderByColumn, OrderByContext>() {

                @Override
                public OrderByContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.OrderByColumn input) {
                    return Strings.isNullOrEmpty(input.getName()) ? new OrderByContext(input.getIndex(), OrderByType.valueOf(input.getOrderByType().toUpperCase()))
                            : new OrderByContext(input.getOwner(), input.getName(), OrderByType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getGroupByColumns()) {
            mergeContext.getGroupByContexts().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.GroupByColumn, GroupByContext>() {

                @Override
                public GroupByContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.GroupByColumn input) {
                    return new GroupByContext(
                            Optional.fromNullable(input.getOwner()), input.getName(), OrderByType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getAggregationColumns()) {
            mergeContext.getAggregationColumns().addAll(Lists.transform(assertObj.getAggregationColumns(),
                    new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn, AggregationSelectItemContext>() {

                        @Override
                        public AggregationSelectItemContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn input) {
                            AggregationSelectItemContext result = new AggregationSelectItemContext(input.getExpression(), Optional.fromNullable(input.getAlias()), -1,
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()));
                            if (null != input.getIndex()) {
                                result.setColumnIndex(input.getIndex());
                            }
                            for (com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn each : input.getDerivedColumns()) {
                                result.getDerivedAggregationSelectItemContexts().add(new AggregationSelectItemContext(each.getExpression(), Optional.fromNullable(each.getAlias()), -1,
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase())));
                            }
                            return result;
                        }
                }));
        }
        if (null != assertObj.getLimit()) {
            mergeContext.setLimit(new LimitContext(
                    assertObj.getLimit().getOffset(), assertObj.getLimit().getRowCount(), assertObj.getLimit().getOffsetParameterIndex(), assertObj.getLimit().getRowCountParameterIndex()));
        }
        result[5] = mergeContext;
        return result;
    }
}