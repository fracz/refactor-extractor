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

package com.dangdang.ddframe.rdb.sharding.routing.type.simple;

import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 简单路由数据源.
 *
 * @author zhangliang
 */
@Getter
@ToString
public class SimpleRoutingDataSource {

    private final String dataSource;

    private final List<TableUnit> tableUnits = new ArrayList<>();

    public SimpleRoutingDataSource(final String dataSource, final TableUnit... tableUnits) {
        this.dataSource = dataSource;
        this.tableUnits.addAll(Arrays.asList(tableUnits));
    }

    Set<String> getLogicTableNames() {
        Set<String> result = new HashSet<>(tableUnits.size());
        result.addAll(Lists.transform(tableUnits, new Function<TableUnit, String>() {

            @Override
            public String apply(final TableUnit input) {
                return input.getLogicTableName();
            }
        }));
        return result;
    }

    List<Set<String>> getActualTableGroups(final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>();
        for (String logicTable : logicTables) {
            Set<String> actualTables = getActualTables(logicTable);
            if (!actualTables.isEmpty()) {
                result.add(actualTables);
            }
        }
        return result;
    }

    private Set<String> getActualTables(final String logicTable) {
        Set<String> result = new HashSet<>();
        for (TableUnit each : tableUnits) {
            if (each.getLogicTableName().equals(logicTable)) {
                result.add(each.getActualTableName());
            }
        }
        return result;
    }

    Optional<TableUnit> findTableUnits(final String actualTable) {
        for (TableUnit each : tableUnits) {
            if (each.getActualTableName().equals(actualTable)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}