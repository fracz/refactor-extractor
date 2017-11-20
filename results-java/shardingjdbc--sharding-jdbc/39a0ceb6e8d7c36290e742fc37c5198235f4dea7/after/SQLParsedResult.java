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

package com.dangdang.ddframe.rdb.sharding.parser.result;

import com.dangdang.ddframe.rdb.sharding.parser.contstant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * SQL解析结果.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class SQLParsedResult {

    private final SQLType sqlType;

    private SQLBuilder sqlBuilder;

    private GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext();

    private LimitContext limit;

    private final ConditionContext conditionContext;

    private final Collection<TableContext> tables = new LinkedHashSet<>();

    private final List<OrderByContext> orderByContexts = new ArrayList<>();

    private final List<GroupByContext> groupByContexts = new ArrayList<>();

    private final List<AggregationSelectItemContext> aggregationColumns = new ArrayList<>();
}