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

package com.dangdang.ddframe.rdb.sharding.routing.router;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.insert.InsertStatement;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.routing.type.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleTableRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 需要解析的SQL路由器.
 *
 * @author zhangiang
 */
@Slf4j
public final class ParsingSQLRouter implements SQLRouter {

    private final ShardingRule shardingRule;

    private final DatabaseType databaseType;

    private final List<Number> generatedKeys = new LinkedList<>();

    public ParsingSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
        databaseType = shardingContext.getDatabaseType();
    }

    @Override
    public SQLStatement parse(final String logicSQL, final int parametersSize) {
        SQLParsingEngine parsingEngine = new SQLParsingEngine(databaseType, logicSQL, shardingRule);
        Context context = MetricsContext.start("Parse SQL");
        log.debug("Logic SQL: {}", logicSQL);
        SQLStatement result = parsingEngine.parse();
        if (result instanceof InsertStatement) {
            ((InsertStatement) result).appendGenerateKeyToken(shardingRule, parametersSize);
        }
        MetricsContext.stop(context);
        return result;
    }

    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        final Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        if (sqlStatement instanceof InsertStatement && null != ((InsertStatement) sqlStatement).getGeneratedKey()) {
            processGeneratedKey(parameters, (InsertStatement) sqlStatement, result);
        }
        RoutingResult routingResult = route(parameters, sqlStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(logicSQL, sqlStatement);
        boolean isSingleRouting = routingResult.isSingleRouting();
        if (null != sqlStatement.getLimit()) {
            sqlStatement.getLimit().processParameters(parameters, !isSingleRouting);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(!isSingleRouting);
        result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(sqlBuilder));
        MetricsContext.stop(context);
        logSQLRouteResult(result, parameters);
        return result;
    }

    private RoutingResult route(final List<Object> parameters, final SQLStatement sqlStatement) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        if (1 == tableNames.size()) {
            return new SingleTableRouter(shardingRule, parameters, tableNames.iterator().next(), sqlStatement).route();
        }
        if (shardingRule.isAllBindingTables(tableNames)) {
            return new BindingTablesRouter(shardingRule, parameters, tableNames, sqlStatement).route();
        }
        // TODO 可配置是否执行笛卡尔积
        return new MixedTablesRouter(shardingRule, parameters, tableNames, sqlStatement).route();
    }

    private void logSQLRouteResult(final SQLRouteResult routeResult, final List<Object> parameters) {
        log.debug("final route result is {} target", routeResult.getExecutionUnits().size());
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSQL(), parameters);
        }
    }

    private void processGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement, final SQLRouteResult sqlRouteResult) {
        GeneratedKey generatedKey = insertStatement.getGeneratedKey();
        if (parameters.isEmpty()) {
            sqlRouteResult.getGeneratedKeys().add(generatedKey.getValue());
        } else if (parameters.size() == generatedKey.getIndex()) {
            Number key = shardingRule.generateKey(insertStatement.getTables().getSingleTableName());
            parameters.add(key);
            setGeneratedKeys(sqlRouteResult, key);
        } else if (-1 != generatedKey.getIndex()) {
            setGeneratedKeys(sqlRouteResult, (Number) parameters.get(generatedKey.getIndex()));
        }
    }

    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final Number generatedKey) {
        generatedKeys.add(generatedKey);
        sqlRouteResult.getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKeys().addAll(generatedKeys);
    }
}