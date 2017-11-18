package com.alibaba.druid.sql.dialect.mysql.ast.statement;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlObjectImpl;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;

@SuppressWarnings("serial")
public class MySqlPartitioningDef extends MySqlObjectImpl {

    private SQLName name;

    private Values  values;

    private SQLExpr dataDirectory;
    private SQLExpr indexDirectory;

    @Override
    public void accept0(MySqlASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, values);
            acceptChild(visitor, dataDirectory);
            acceptChild(visitor, indexDirectory);
        }
        visitor.endVisit(this);
    }

    public SQLExpr getIndexDirectory() {
        return indexDirectory;
    }

    public void setIndexDirectory(SQLExpr indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public SQLExpr getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(SQLExpr dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public Values getValues() {
        return values;
    }

    public void setValues(Values values) {
        this.values = values;
    }

    public SQLName getName() {
        return name;
    }

    public void setName(SQLName name) {
        this.name = name;
    }

    public static abstract class Values extends MySqlObjectImpl {

        private final List<SQLExpr> items = new ArrayList<SQLExpr>();

        public List<SQLExpr> getItems() {
            return items;
        }
    }

    public static class LessThanValues extends Values {

        @Override
        public void accept0(MySqlASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, getItems());
            }
            visitor.endVisit(this);
        }

    }

    public static class InValues extends Values {

        @Override
        public void accept0(MySqlASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, getItems());
            }
            visitor.endVisit(this);
        }

    }
}