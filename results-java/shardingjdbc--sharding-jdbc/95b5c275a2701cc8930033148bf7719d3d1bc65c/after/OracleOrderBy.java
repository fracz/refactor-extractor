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
package com.alibaba.druid.sql.dialect.oracle.ast;

import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleOrderBy extends SQLOrderBy {

    private boolean sibings;

    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor instanceof OracleASTVisitor) {
            accept0((OracleASTVisitor) visitor);
        } else {
            super.acceptInternal(visitor);
        }
    }

    protected void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getItems());
        }

        visitor.endVisit(this);
    }

    @Override
    public void output(final StringBuffer buffer) {
        buffer.append("ORDER ");
        if (this.sibings) {
            buffer.append("SIBLINGS ");
        }
        buffer.append("BY ");
        int i = 0;
        for (int size = getItems().size(); i < size; ++i) {
            if (i != 0) {
                buffer.append(", ");
            }
            getItems().get(i).output(buffer);
        }
    }
}