/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
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
package com.alibaba.druid.sql.repository;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateTableStatement;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.JdbcUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by wenshao on 03/06/2017.
 */
public class SchemaRepository {
    private Schema defaultSchema;
    protected String dbType;

    public SchemaRepository() {

    }

    public SchemaRepository(String dbType) {
        this.dbType = dbType;
    }

    private Map<String, Schema> schemas = new LinkedHashMap<String, Schema>();

    public String getDefaultSchemaName() {
        return getDefaultSchema().getName();
    }

    public void setDefaultSchema(String name) {
        if (name == null) {
            defaultSchema = null;
            return;
        }

        String normalizedName = SQLUtils.normalize(name)
                .toLowerCase();

        Schema defaultSchema = schemas.get(normalizedName);
        if (defaultSchema != null) {
            this.defaultSchema = defaultSchema;
            return;
        }

        if (defaultSchema == null) {
            if (this.defaultSchema != null
                    && this.defaultSchema.getName() == null) {
                this.defaultSchema.setName(name);

                schemas.put(normalizedName, this.defaultSchema);
                return;
            }

            defaultSchema = new Schema(this);
            defaultSchema.setName(name);
            schemas.put(normalizedName, defaultSchema);
            this.defaultSchema = defaultSchema;
        }
    }

    public Schema findSchema(String schema) {
        if (schema == null) {
            return null;
        }

        return schemas.get(
                SQLUtils.normalize(schema)
                .toLowerCase());
    }

    public Schema getDefaultSchema() {
        if (defaultSchema == null) {
            defaultSchema = new Schema(this);
        }

        return defaultSchema;
    }

    public void setDefaultSchema(Schema schema) {
        this.defaultSchema = schema;
    }

    public SchemaObject findTable(String tableName) {
        return getDefaultSchema().findTable(tableName);
    }

    public SchemaObject findTableOrView(String tableName) {
        return findTableOrView(tableName, true);
    }

    public SchemaObject findTableOrView(String tableName, boolean onlyCurrent) {
        Schema schema = getDefaultSchema();

        SchemaObject object = schema.findTableOrView(tableName);
        if (object != null) {
            return object;
        }

        for (Schema s : this.schemas.values()) {
            if (s == schema) {
                continue;
            }

            object = schema.findTableOrView(tableName);
            if (object != null) {
                return object;
            }
        }

        return null;
    }

    public Collection<Schema> getSchemas() {
        return schemas.values();
    }

    public SchemaObject findFunction(String functionName) {
        return getDefaultSchema().findFunction(functionName);
    }

    public void acceptDDL(String ddl) {
        acceptDDL(ddl, dbType);
    }

    public void acceptDDL(String ddl, String dbType) {
        getDefaultSchema().acceptDDL(ddl, dbType);
    }

    public void accept(SQLStatement stmt) {
        getDefaultSchema().accept(stmt);
    }

    public boolean isSequence(String name) {
        return getDefaultSchema().isSequence(name);
    }

    public SchemaObject findTable(SQLTableSource tableSource, String alias) {
        return getDefaultSchema().findTable(tableSource, alias);
    }

    public SQLColumnDefinition findColumn(SQLTableSource tableSource, SQLSelectItem selectItem) {
        return getDefaultSchema().findColumn(tableSource, selectItem);
    }

    public SQLColumnDefinition findColumn(SQLTableSource tableSource, SQLExpr expr) {
        return getDefaultSchema().findColumn(tableSource, expr);
    }

    public SchemaObject findTable(SQLTableSource tableSource, SQLSelectItem selectItem) {
        return getDefaultSchema().findTable(tableSource, selectItem);
    }

    public SchemaObject findTable(SQLTableSource tableSource, SQLExpr expr) {
        return getDefaultSchema().findTable(tableSource, expr);
    }

    public Map<String, SchemaObject> getTables(SQLTableSource x) {
        return getDefaultSchema().getTables(x);
    }

    public int getTableCount() {
        return getDefaultSchema().getTableCount();
    }

    public Map<String, SchemaObject> getObjects() {
        return getDefaultSchema().getObjects();
    }

    public int getViewCount() {
        return getDefaultSchema().getViewCount();
    }

    public String console(String input) {
        try {
            StringBuffer buf = new StringBuffer();

            List<SQLStatement> stmtList = SQLUtils.parseStatements(input, dbType);

            for (SQLStatement stmt : stmtList) {
                if (stmt instanceof MySqlShowColumnsStatement) {
                    MySqlShowColumnsStatement showColumns = ((MySqlShowColumnsStatement) stmt);
                    SQLName table = showColumns.getTable();
                    SchemaObject schemaObject = findTable(table);
                    MySqlCreateTableStatement createTableStmt = (MySqlCreateTableStatement) schemaObject.getStatement();
                    createTableStmt.showCoumns(buf);
                } else if (stmt instanceof MySqlShowCreateTableStatement) {
                    MySqlShowCreateTableStatement showCreateTableStmt = (MySqlShowCreateTableStatement) stmt;
                    SQLName table = showCreateTableStmt.getName();
                    SchemaObject schemaObject = findTable(table);
                    MySqlCreateTableStatement createTableStmt = (MySqlCreateTableStatement) schemaObject.getStatement();
                    createTableStmt.output(buf);
                } else {
                    this.getDefaultSchema().accept(stmt);
                }
            }

            return buf.toString();
        } catch (IOException ex) {
            throw new DruidRuntimeException("exeucte command error.", ex);
        }
    }

    public SchemaObject findTable(SQLName name) {
        if (name instanceof SQLIdentifierExpr) {
            return findTable(((SQLIdentifierExpr) name).getName());
        }

        if (name instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) name;
            String schema = propertyExpr.getOwnernName();
            String table = propertyExpr.getName();

            Schema schemaObj = findSchema(schema);
            if (schemaObj == null) {
                return null;
            }

            return schemaObj.findTable(table);
        }

        return null;
    }

    public SQLASTVisitor createResolveVisitor() {
        if (JdbcConstants.MYSQL.equals(dbType)) {
            return new MySqlResolveVisitor();
        }

        throw new DruidRuntimeException("dbType not support : " + dbType);
    }

    public class MySqlResolveVisitor extends MySqlASTVisitorAdapter {

        public boolean visit(MySqlSelectQueryBlock x) {
            List<SQLSelectItem> selectList = x.getSelectList();
            for (int i = selectList.size() - 1; i >= 0; i--) {
                SQLSelectItem selectItem = selectList.get(i);
                SQLExpr expr = selectItem.getExpr();
                if (expr instanceof SQLAllColumnExpr) {
                    SQLTableSource from = x.getFrom();
                    List<SQLSelectItem> columns = new ArrayList<SQLSelectItem>();
                    if (from instanceof SQLExprTableSource) {
                        SchemaObject table = findTable((SQLExprTableSource) from);
                        if (table != null) {
                            SQLCreateTableStatement createTableStmt = (SQLCreateTableStatement) table.getStatement();
                            for (SQLTableElement e : createTableStmt.getTableElementList()) {
                                if (e instanceof SQLColumnDefinition) {
                                    SQLColumnDefinition column = (SQLColumnDefinition) e;
                                    SQLIdentifierExpr name = (SQLIdentifierExpr) column.getName().clone();
                                    name.setResolvedColumn(column);
                                    columns.add(new SQLSelectItem(name));
                                }
                            }
                        }
                    }

                    for (SQLSelectItem column : columns) {
                        column.setParent(x);
                    }
                    selectList.remove(i);
                    selectList.addAll(i, columns);
                }
            }

            return super.visit(x);
        }
    }

    public SchemaObject findTable(SQLExprTableSource x) {
        if (x == null) {
            return null;
        }

        SQLExpr expr = x.getExpr();
        if (expr instanceof SQLName) {
            return findTable((SQLName) expr);
        }

        return null;
    }

    public class OracleResolveVisitor extends OracleASTVisitorAdapter {

    }
}