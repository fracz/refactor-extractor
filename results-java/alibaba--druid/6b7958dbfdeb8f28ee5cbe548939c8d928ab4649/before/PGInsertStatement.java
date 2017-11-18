package com.alibaba.druid.sql.dialect.postgresql.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class PGInsertStatement extends SQLInsertStatement implements PGSQLStatement {

    private static final long  serialVersionUID = 1L;

    private PGWithClause       with;
    private List<ValuesClause> valuesList       = new ArrayList<ValuesClause>();
    private SQLExpr            returning;

    public SQLExpr getReturning() {
        return returning;
    }

    public void setReturning(SQLExpr returning) {
        this.returning = returning;
    }

    public PGWithClause getWith() {
        return with;
    }

    public void setWith(PGWithClause with) {
        this.with = with;
    }

    public ValuesClause getValues() {
        if (valuesList.size() == 0) {
            return null;
        }
        return valuesList.get(0);
    }

    public void setValues(ValuesClause values) {
        if (valuesList.size() == 0) {
            valuesList.add(values);
        } else {
            valuesList.set(0, values);
        }
    }

    public List<ValuesClause> getValuesList() {
        return valuesList;
    }

    protected void accept0(SQLASTVisitor visitor) {
        accept0((PGASTVisitor) visitor);
    }

    @Override
    public void accept0(PGASTVisitor visitor) {
        if (visitor.visit(this)) {
            this.acceptChild(visitor, with);
            this.acceptChild(visitor, tableName);
            this.acceptChild(visitor, columns);
            this.acceptChild(visitor, valuesList);
            this.acceptChild(visitor, query);
            this.acceptChild(visitor, returning);
        }

        visitor.endVisit(this);
    }
}