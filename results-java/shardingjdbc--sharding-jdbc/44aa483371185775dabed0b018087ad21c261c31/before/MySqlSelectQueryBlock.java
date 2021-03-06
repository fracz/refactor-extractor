/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.dialect.mysql.ast.statement;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class MySqlSelectQueryBlock extends SQLSelectQueryBlock {

    private SQLOrderBy orderBy;

    private Limit limit;

    private SQLName procedureName;

    private final List<SQLExpr> procedureArgumentList = new ArrayList<>(2);

    public void setLimit(final Limit limit) {
        if (null != limit) {
            limit.setParent(this);
        }
        this.limit = limit;
    }

    @NoArgsConstructor
    @Getter
    public static class Limit extends SQLObjectImpl {

        private SQLExpr rowCount;

        private SQLExpr offset;

        public Limit(final SQLExpr rowCount) {
            this.setRowCount(rowCount);
        }

        public void setRowCount(final SQLExpr rowCount) {
            if (null != rowCount) {
                rowCount.setParent(this);
            }
            this.rowCount = rowCount;
        }

        public void setOffset(final SQLExpr offset) {
            if (null != offset) {
                offset.setParent(this);
            }
            this.offset = offset;
        }

    }
}