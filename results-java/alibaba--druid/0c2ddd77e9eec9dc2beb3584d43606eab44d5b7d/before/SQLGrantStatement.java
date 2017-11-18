/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
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
package com.alibaba.druid.sql.ast.statement;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class SQLGrantStatement extends SQLStatementImpl {

    private final List<SQLExpr> privileges = new ArrayList<SQLExpr>();

    private SQLExpr             on;
    private SQLExpr             to;

    // mysql
    private SQLExpr             maxQueriesPerHour;
    private SQLExpr             maxUpdatesPerHour;
    private SQLExpr             maxConnectionsPerHour;
    private SQLExpr             maxUserConnections;

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, on);
            acceptChild(visitor, to);
        }
        visitor.endVisit(this);
    }

    public SQLExpr getOn() {
        return on;
    }

    public void setOn(SQLExpr on) {
        this.on = on;
    }

    public SQLExpr getTo() {
        return to;
    }

    public void setTo(SQLExpr to) {
        this.to = to;
    }

    public List<SQLExpr> getPrivileges() {
        return privileges;
    }

    public void addPrivileges(String privilege) {
        SQLIdentifierExpr expr = new SQLIdentifierExpr(privilege);
        expr.setParent(this);
        this.privileges.add(expr);
    }

    public SQLExpr getMaxQueriesPerHour() {
        return maxQueriesPerHour;
    }

    public void setMaxQueriesPerHour(SQLExpr maxQueriesPerHour) {
        this.maxQueriesPerHour = maxQueriesPerHour;
    }

    public SQLExpr getMaxUpdatesPerHour() {
        return maxUpdatesPerHour;
    }

    public void setMaxUpdatesPerHour(SQLExpr maxUpdatesPerHour) {
        this.maxUpdatesPerHour = maxUpdatesPerHour;
    }

    public SQLExpr getMaxConnectionsPerHour() {
        return maxConnectionsPerHour;
    }

    public void setMaxConnectionsPerHour(SQLExpr maxConnectionsPerHour) {
        this.maxConnectionsPerHour = maxConnectionsPerHour;
    }

    public SQLExpr getMaxUserConnections() {
        return maxUserConnections;
    }

    public void setMaxUserConnections(SQLExpr maxUserConnections) {
        this.maxUserConnections = maxUserConnections;
    }

}