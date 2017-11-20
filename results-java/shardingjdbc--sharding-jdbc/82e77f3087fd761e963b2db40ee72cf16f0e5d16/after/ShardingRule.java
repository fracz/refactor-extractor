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

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Databases and tables sharding rule configuration.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingRule {

    private final DataSourceRule dataSourceRule;

    private final Collection<TableRule> tableRules;

    private final Collection<BindingTableRule> bindingTableRules;

    private final ShardingStrategy defaultDatabaseShardingStrategy;

    private final ShardingStrategy defaultTableShardingStrategy;

    private final KeyGenerator defaultKeyGenerator;

    private ShardingRule(
            final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final Collection<BindingTableRule> bindingTableRules,
            final ShardingStrategy defaultDatabaseShardingStrategy, final ShardingStrategy defaultTableShardingStrategy, final KeyGenerator defaultKeyGenerator) {
        this.dataSourceRule = dataSourceRule;
        this.tableRules = null == tableRules ? Collections.<TableRule>emptyList() : tableRules;
        this.bindingTableRules = null == bindingTableRules ? Collections.<BindingTableRule>emptyList() : bindingTableRules;
        this.defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategy ? new NoneShardingStrategy() : defaultDatabaseShardingStrategy;
        this.defaultTableShardingStrategy = null == defaultTableShardingStrategy ? new NoneShardingStrategy() : defaultTableShardingStrategy;
        this.defaultKeyGenerator = null == defaultKeyGenerator ? KeyGeneratorFactory.createKeyGenerator(DefaultKeyGenerator.class) : defaultKeyGenerator;
    }

    /**
     * Get sharding rule builder.
     *
     * @param dataSourceRule data source rule
     * @return sharding rule builder
     */
    public static ShardingRuleBuilder builder(final DataSourceRule dataSourceRule) {
        return new ShardingRuleBuilder(dataSourceRule);
    }

    /**
     * Try to find table rule though logic table name.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> tryFindTableRule(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }

    /**
     * Find table rule though logic table name.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = tryFindTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (dataSourceRule.getDefaultDataSource().isPresent()) {
            return createTableRuleWithDefaultDataSource(logicTableName);
        }
        throw new ShardingJdbcException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
    }

    private TableRule createTableRuleWithDefaultDataSource(final String logicTableName) {
        Map<String, DataSource> defaultDataSourceMap = new HashMap<>(1, 1);
        defaultDataSourceMap.put(dataSourceRule.getDefaultDataSourceName(), dataSourceRule.getDefaultDataSource().get());
        return TableRule.builder(logicTableName)
                .dataSourceRule(new DataSourceRule(defaultDataSourceMap)).databaseShardingStrategy(new NoneShardingStrategy()).tableShardingStrategy(new NoneShardingStrategy()).build();
    }

    /**
     * Get database sharding strategy.
     *
     * <p>
     * Use default database sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return database sharding strategy
     */
    public ShardingStrategy getDatabaseShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategy() ? defaultDatabaseShardingStrategy : tableRule.getDatabaseShardingStrategy();
    }

    /**
     * Get table sharding strategy.
     *
     * <p>
     * Use default table sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return table sharding strategy
     */
    public ShardingStrategy getTableShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategy() ? defaultTableShardingStrategy : tableRule.getTableShardingStrategy();
    }

    /**
     * Adjust logic tables is all belong to binding tables.
     *
     * @param logicTables names of logic tables
     * @return logic tables is all belong to binding tables or not
     */
    public boolean isAllBindingTables(final Collection<String> logicTables) {
        Collection<String> bindingTables = filterAllBindingTables(logicTables);
        return !bindingTables.isEmpty() && bindingTables.containsAll(logicTables);
    }

    /**
     * Filter all binding tables.
     *
     * @param logicTables names of logic tables
     * @return names for filtered binding tables
     */
    public Collection<String> filterAllBindingTables(final Collection<String> logicTables) {
        if (logicTables.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTables);
        if (!bindingTableRule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<String> result = new ArrayList<>(bindingTableRule.get().getAllLogicTables());
        result.retainAll(logicTables);
        return result;
    }

    private Optional<BindingTableRule> findBindingTableRule(final Collection<String> logicTables) {
        for (String each : logicTables) {
            Optional<BindingTableRule> result = findBindingTableRule(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }

    /**
     * Get binding table rule via logic table name.
     *
     * @param logicTable logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTable) {
        for (BindingTableRule each : bindingTableRules) {
            if (each.hasLogicTable(logicTable)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }

    /**
     * Adjust is sharding column or not.
     *
     * @param column column object
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final Column column) {
        if (defaultDatabaseShardingStrategy.getShardingColumns().contains(column.getName()) || defaultTableShardingStrategy.getShardingColumns().contains(column.getName())) {
            return true;
        }
        for (TableRule each : tableRules) {
            if (!each.getLogicTable().equalsIgnoreCase(column.getTableName())) {
                continue;
            }
            if (null != each.getDatabaseShardingStrategy() && each.getDatabaseShardingStrategy().getShardingColumns().contains(column.getName())) {
                return true;
            }
            if (null != each.getTableShardingStrategy() && each.getTableShardingStrategy().getShardingColumns().contains(column.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * get generated key's column name.
     *
     * @param logicTableName logic table name
     * @return generated key's column name
     */
    public Optional<String> getGenerateKeyColumn(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
                return Optional.fromNullable(each.getGenerateKeyColumn());
            }
        }
        return Optional.absent();
    }

    /**
     * Generate key.
     *
     * @param logicTableName logic table name
     * @return generated key
     */
    public Number generateKey(final String logicTableName) {
        Optional<TableRule> tableRule = tryFindTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingJdbcException("Cannot find strategy for generate keys.");
        }
        if (null != tableRule.get().getKeyGenerator()) {
            return tableRule.get().getKeyGenerator().generateKey();
        }
        return defaultKeyGenerator.generateKey();
    }

    /**
     * Sharding rule builder.
     */
    @RequiredArgsConstructor
    public static class ShardingRuleBuilder {

        private final DataSourceRule dataSourceRule;

        private final Collection<TableRule> tableRules = new LinkedList<>();

        private final Collection<BindingTableRule> bindingTableRules = new LinkedList<>();

        private ShardingStrategy defaultDatabaseShardingStrategy;

        private ShardingStrategy defaultTableShardingStrategy;

        private KeyGenerator defaultKeyGenerator;

        /**
         * Build table rules.
         *
         * @param tableRules table rules
         * @return this builder
         */
        public ShardingRuleBuilder tableRules(final TableRule... tableRules) {
            this.tableRules.clear();
            this.tableRules.addAll(Arrays.asList(tableRules));
            return this;
        }

        /**
         * Build binding table rules.
         *
         * @param bindingTableRules binding table rules
         * @return this builder
         */
        public ShardingRuleBuilder bindingTableRules(final BindingTableRule... bindingTableRules) {
            this.bindingTableRules.clear();
            this.bindingTableRules.addAll(Arrays.asList(bindingTableRules));
            return this;
        }

        /**
         * Build default database strategy.
         *
         * @param defaultDatabaseShardingStrategy default database strategy
         * @return this builder
         */
        public ShardingRuleBuilder defaultDatabaseShardingStrategy(final ShardingStrategy defaultDatabaseShardingStrategy) {
            this.defaultDatabaseShardingStrategy = defaultDatabaseShardingStrategy;
            return this;
        }

        /**
         * Build default table strategy.
         *
         * @param defaultTableShardingStrategy default table strategy
         * @return this builder
         */
        public ShardingRuleBuilder defaultTableShardingStrategy(final ShardingStrategy defaultTableShardingStrategy) {
            this.defaultTableShardingStrategy = defaultTableShardingStrategy;
            return this;
        }

        /**
         * Build default key generator.
         *
         * @param defaultKeyGenerator default key generator
         * @return this builder
         */
        public ShardingRuleBuilder defaultKeyGenerator(final KeyGenerator defaultKeyGenerator) {
            this.defaultKeyGenerator = defaultKeyGenerator;
            return this;
        }

        /**
         * Build sharding rule.
         *
         * @return built sharding rule
         */
        public ShardingRule build() {
            return new ShardingRule(dataSourceRule, tableRules, bindingTableRules, defaultDatabaseShardingStrategy, defaultTableShardingStrategy, defaultKeyGenerator);
        }
    }
}