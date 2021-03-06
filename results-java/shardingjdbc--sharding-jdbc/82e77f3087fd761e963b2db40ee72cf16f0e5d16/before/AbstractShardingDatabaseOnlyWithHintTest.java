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

package com.dangdang.ddframe.rdb.integrate.type.sharding.hint.base;

import com.dangdang.ddframe.rdb.integrate.fixture.ComplexKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.complex.ComplexShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractShardingDatabaseOnlyWithHintTest extends AbstractHintTest {

    @Override
    protected ShardingRule getShardingRule(final Map.Entry<DatabaseType, Map<String, DataSource>> dataSourceEntry) {
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceEntry.getValue());
        TableRule orderTableRule = TableRule.builder("t_order").dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("t_order_item").dataSourceRule(dataSourceRule).build();
        return ShardingRule.builder(dataSourceRule).tableRules(orderTableRule, orderItemTableRule)
                .bindingTableRules(new BindingTableRule(orderTableRule, orderItemTableRule))
                .databaseShardingStrategy(new ComplexShardingStrategy(Collections.singletonList("user_id"), new ComplexKeysModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new NoneShardingStrategy()).build();
    }
}