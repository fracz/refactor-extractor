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

package com.dangdang.ddframe.rdb.sharding.merger.memory;

import com.dangdang.ddframe.rdb.sharding.merger.memory.row.OrderByResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.memory.row.ResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.google.common.base.Optional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 基于内存排序的结果集抽象类.
 *
 * @author gaohongtao
 * @author zhangliang
 */
public final class OrderByMemoryResultSet extends AbstractMemoryResultSet {

    private final List<OrderItem> orderByItems;

    private Iterator<OrderByResultSetRow> orderByResultSetRows;

    public OrderByMemoryResultSet(final List<ResultSet> resultSets, final List<OrderItem> orderByItems) throws SQLException {
        super(resultSets);
        this.orderByItems = orderByItems;
    }

    @Override
    protected void initRows(final List<ResultSet> resultSets) throws SQLException {
        List<OrderByResultSetRow> orderByResultSetRows = new LinkedList<>();
        for (ResultSet each : resultSets) {
            while (each.next()) {
                orderByResultSetRows.add(new OrderByResultSetRow(each, orderByItems));
            }
        }
        Collections.sort(orderByResultSetRows);
        this.orderByResultSetRows = orderByResultSetRows.iterator();
    }

    @Override
    protected Optional<? extends ResultSetRow> nextRow() throws SQLException {
        if (orderByResultSetRows.hasNext()) {
            return Optional.of(orderByResultSetRows.next());
        }
        return Optional.absent();
    }
}