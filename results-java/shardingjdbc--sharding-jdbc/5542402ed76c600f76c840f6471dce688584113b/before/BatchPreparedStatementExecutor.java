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

package com.dangdang.ddframe.rdb.sharding.executor.type.batch;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 多线程执行批量预编译语句对象请求的执行器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class BatchPreparedStatementExecutor {

    private final ExecutorEngine executorEngine;

    private final Collection<BatchPreparedStatementUnit> batchPreparedStatementUnits;

    private final List<List<Object>> parameterSets;

    /**
     * 执行批量SQL.
     *
     * @return 执行结果
     */
    public int[] executeBatch() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeBatch");
        try {
            return accumulate(executorEngine.executeBatch(batchPreparedStatementUnits, parameterSets));
        } finally {
            MetricsContext.stop(context);
        }
    }

    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[parameterSets.size()];
        int count = 0;
        for (BatchPreparedStatementUnit each : batchPreparedStatementUnits) {
            for (Map.Entry<Integer, Integer> entry : each.getOuterAndInnerAddBatchCountMap().entrySet()) {
                result[entry.getKey()] += results.get(count)[entry.getValue()];
            }
            count++;
        }
        return result;
    }
}