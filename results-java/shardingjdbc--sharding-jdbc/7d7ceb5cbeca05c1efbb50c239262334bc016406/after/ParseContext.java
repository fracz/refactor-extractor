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

package com.dangdang.ddframe.rdb.sharding.parser.visitor;

import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 解析过程的上下文对象.
 *
 * @author zhangliang
 */
@Getter
public final class ParseContext {

    private static final String SHARDING_GEN_ALIAS = "sharding_gen_%s";

    private final SQLParsedResult parsedResult = new SQLParsedResult();

    private final Collection<SelectItemContext> selectItems = new HashSet<>();

    private boolean hasAllColumn;

    private int derivedColumnOffset;

    /**
     * 将求平均值函数的补列加入解析上下文.
     *
     * @param avgColumn 求平均值的列
     */
    public List<AggregationColumn> addDerivedColumnsForAvgColumn(final AggregationColumn avgColumn) {
        List<AggregationColumn> result = new ArrayList<>(2);
        AggregationColumn countColumn = getDerivedCountColumn(avgColumn);
        addDerivedColumnForAvgColumn(avgColumn, countColumn);
        result.add(countColumn);
        AggregationColumn sumColumn = getDerivedSumColumn(avgColumn);
        addDerivedColumnForAvgColumn(avgColumn, sumColumn);
        result.add(sumColumn);
        return result;
    }

    private void addDerivedColumnForAvgColumn(final AggregationColumn avgColumn, final AggregationColumn derivedColumn) {
        avgColumn.getDerivedColumns().add(derivedColumn);
        parsedResult.getMergeContext().getAggregationColumns().add(derivedColumn);
    }

    private AggregationColumn getDerivedCountColumn(final AggregationColumn avgColumn) {
        String expression = avgColumn.getExpression().replaceFirst("(?i)" + AggregationType.AVG.toString(), AggregationType.COUNT.toString());
        return new AggregationColumn(expression, AggregationType.COUNT, Optional.of(generateDerivedColumnAlias()), avgColumn.getOption());
    }

    private String generateDerivedColumnAlias() {
        return String.format(SHARDING_GEN_ALIAS, ++derivedColumnOffset);
    }

    private AggregationColumn getDerivedSumColumn(final AggregationColumn avgColumn) {
        String expression = avgColumn.getExpression().replaceFirst("(?i)" + AggregationType.AVG.toString(), AggregationType.SUM.toString());
        if (avgColumn.getOption().isPresent()) {
            expression = expression.replaceFirst(avgColumn.getOption().get() + " ", "");
        }
        return new AggregationColumn(expression, AggregationType.SUM, Optional.of(generateDerivedColumnAlias()), Optional.<String>absent());
    }

    /**
     * 将排序列加入解析上下文.
     *
     * @param index 列顺序索引
     * @param orderByType 排序类型
     */
    public void addOrderByColumn(final int index, final OrderByType orderByType) {
        parsedResult.getMergeContext().getOrderByColumns().add(new OrderByColumn(index, orderByType));
    }

    /**
     * 将排序列加入解析上下文.
     *
     * @param owner 列拥有者
     * @param name 列名称
     * @param orderByType 排序类型
     *
     * @return 排序列
     */
    public OrderByColumn addOrderByColumn(final Optional<String> owner, final String name, final OrderByType orderByType) {
        String rawName = owner.isPresent() ? SQLUtil.getExactlyValue(owner.get()) + "." + SQLUtil.getExactlyValue(name) : SQLUtil.getExactlyValue(name);
        OrderByColumn result = new OrderByColumn(owner, SQLUtil.getExactlyValue(name), getAlias(rawName), orderByType);
        parsedResult.getMergeContext().getOrderByColumns().add(result);
        return result;
    }

    private Optional<String> getAlias(final String name) {
        if (hasAllColumn) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItemContext each : selectItems) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.of(generateDerivedColumnAlias());
    }

    /**
     * 将分组列加入解析上下文.
     *
     * @param owner 列拥有者
     * @param name 列名称
     * @param orderByType 排序类型
     *
     * @return 分组列
     */
    // TODO rename addGroupByColumns => addGroupByColumn
    public GroupByColumn addGroupByColumns(final Optional<String> owner, final String name, final OrderByType orderByType) {
        String rawName = owner.isPresent() ? SQLUtil.getExactlyValue(owner.get()) + "." + SQLUtil.getExactlyValue(name) : SQLUtil.getExactlyValue(name);
        GroupByColumn result = new GroupByColumn(owner, SQLUtil.getExactlyValue(name), getAlias(rawName), orderByType);
        parsedResult.getMergeContext().getGroupByColumns().add(result);
        return result;
    }

    /**
     * 注册SELECT语句中声明的列名称或别名.
     *
     * @param selectItem SELECT语句中声明的列名称或别名
     */
    public void registerSelectItem(final String selectItem) {
        String rawItemExpr = SQLUtil.getExactlyValue(selectItem);
        if ("*".equals(rawItemExpr)) {
            hasAllColumn = true;
            return;
        }
        selectItems.add(new CommonSelectItemContext(rawItemExpr, null, 0, false));
    }
}