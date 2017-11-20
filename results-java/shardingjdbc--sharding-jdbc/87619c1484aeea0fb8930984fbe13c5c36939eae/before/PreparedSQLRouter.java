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

package com.dangdang.ddframe.rdb.sharding.router;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 预解析的SQL路由器.
 *
 * @author gaohongtao
 */
public final class PreparedSQLRouter {

    private final String logicSQL;

    private final RouteEngine routeEngine;

    private final ShardingRule shardingRule;

    private SQLContext sqlContext;

    public PreparedSQLRouter(final String logicSQL, final ShardingContext shardingContext) {
        this.logicSQL = logicSQL;
        routeEngine = shardingContext.getRouteEngine();
        shardingRule = shardingContext.getShardingRule();
    }

    /**
     * SQL路由.
     * 当第一次路由时进行SQL解析,之后的路由复用第一次的解析结果.
     *
     * @param parameters SQL中的参数
     * @return 路由结果
     */
    public SQLRouteResult route(final List<Object> parameters) {
        if (null == sqlContext) {
            sqlContext = routeEngine.parse(logicSQL, parameters);
        } else {
            List<Number> generatedIds = generateId();
            parameters.addAll(generatedIds);
        }
        return routeEngine.route(logicSQL, sqlContext, parameters);
    }

    private List<Number> generateId() {
        if (!(sqlContext instanceof InsertSQLContext)) {
            return Collections.emptyList();
        }
        Optional<TableRule> tableRuleOptional = shardingRule.tryFindTableRule(sqlContext.getTables().iterator().next().getName());
        if (!tableRuleOptional.isPresent()) {
            return Collections.emptyList();
        }
        TableRule tableRule = tableRuleOptional.get();
        GeneratedKeyContext generatedKeyContext = ((InsertSQLContext) sqlContext).getGeneratedKeyContext();
        List<Number> result = new ArrayList<>(generatedKeyContext.getColumns().size());
        for (String each : generatedKeyContext.getColumns()) {
            Number generatedId = tableRule.generateId(each);
            result.add(generatedId);
            generatedKeyContext.putValue(each, generatedId);
        }
        return result;
    }
}
