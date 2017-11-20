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

package com.dangdang.ddframe.rdb.integrate.db.pstatement;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.sql.DatabaseTestSQL;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingDataBasesOnlyForPStatementWithAggregateTest extends AbstractShardingDataBasesOnlyDBUnitTest {

    private ShardingDataSource shardingDataSource;

    private DatabaseTestSQL sql;

    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
        sql = currentDatabaseSQL();
    }

    @Test
    public void assertSelectCountAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCount.xml", shardingDataSource.getConnection(), "t_order", sql.getSelectCountAliasSql());
    }

    @Test
    public void assertSelectCount() throws SQLException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.getSelectCountSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            if (isAggregationAliasSupport()) {
                assertThat(rs.getInt("COUNT(*)"), is(40));
            }
            assertThat(rs.getInt(1), is(40));
            assertFalse(rs.next());
        }
    }

    @Test
    public void assertSelectSumAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectSum.xml", shardingDataSource.getConnection(), "t_order", sql.getSelectSumAliasSql());
    }

    @Test
    public void assertSelectSum() throws SQLException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.getSelectSumSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            if (isAggregationAliasSupport()) {
                assertThat(rs.getLong("SUM(`user_id`)"), is(780L));
            }
            assertThat(rs.getLong(1), is(780L));
            assertFalse(rs.next());
        }
    }

    @Test
    public void assertSelectMaxAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectMax.xml", shardingDataSource.getConnection(), "t_order", sql.getSelectMaxAliasSql());
    }

    @Test
    public void assertSelectMax() throws SQLException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.getSelectMaxSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            if (isAggregationAliasSupport()) {
                assertThat(rs.getDouble("MAX(`user_id`)"), is(29D));
            }
            assertThat(rs.getDouble(1), is(29D));
            assertFalse(rs.next());
        }
    }

    @Test
    public void assertSelectMinAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectMin.xml", shardingDataSource.getConnection(), "t_order", sql.getSelectMinAliasSql());
    }

    @Test
    public void assertSelectMin() throws SQLException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.getSelectMinSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            if (isAggregationAliasSupport()) {
                assertThat(rs.getFloat("MIN(`user_id`)"), is(10F));
            }
            assertThat(rs.getFloat(1), is(10F));
            assertFalse(rs.next());
        }
    }

    @Test
    // TODO 改名 avg SHARDING_GEN_2 SHARDING_GEN_3
    public void assertSelectAvgAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectAvg.xml", shardingDataSource.getConnection(),
                "t_order", sql.getSelectAvgAliasSql());
    }

    @Test
    public void assertSelectAvgByName() throws SQLException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.getSelectAvgSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            if (isAggregationAliasSupport()) {
                assertThat(rs.getObject("AVG(`user_id`)"), Is.<Object>is(new BigDecimal("19.5000")));
            }
            assertThat(rs.getBigDecimal(1), Is.<Object>is(new BigDecimal("19.5000")));
            assertFalse(rs.next());
        }
    }

    @Test
    public void assertSelectCountWithBindingTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_0.xml",
                shardingDataSource.getConnection(), "t_order_item", sql.getSelectCountWithBindingTableSql(), 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_1.xml",
                shardingDataSource.getConnection(), "t_order_item", sql.getSelectCountWithBindingTableSql(), 1, 9, 1000, 1909);
    }
}