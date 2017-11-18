package com.alibaba.druid.hdriver.impl.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;

import com.alibaba.druid.hdriver.impl.HBaseConnectionImpl;
import com.alibaba.druid.hdriver.impl.HPreparedStatementImpl;
import com.alibaba.druid.hdriver.impl.HResultSetMetaDataImpl;
import com.alibaba.druid.hdriver.impl.HScannerResultSetImpl;
import com.alibaba.druid.hdriver.impl.mapping.HMapping;
import com.alibaba.druid.hdriver.impl.mapping.HMappingDefaultImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;

public class SingleTableQueryExecutePlan extends SingleTableExecutePlan {

    private List<String>           columeNames = new ArrayList<String>();
    private List<SQLExpr>          conditions  = new ArrayList<SQLExpr>();

    private HResultSetMetaDataImpl resultMetaData;

    private HMapping               mapping;

    public SingleTableQueryExecutePlan(){

    }

    public HMapping getMapping() {
        return mapping;
    }

    public void setMapping(HMapping mapping) {
        this.mapping = mapping;
    }

    public List<SQLExpr> getConditions() {
        return conditions;
    }

    public void setConditions(List<SQLExpr> conditions) {
        this.conditions = conditions;
    }

    public List<String> getColumeNames() {
        return columeNames;
    }

    public HResultSetMetaDataImpl getResultMetaData() {
        return resultMetaData;
    }

    public void setResultMetaData(HResultSetMetaDataImpl resultMetaData) {
        this.resultMetaData = resultMetaData;
    }

    @Override
    public HScannerResultSetImpl executeQuery(HPreparedStatementImpl statement) throws SQLException {
        try {
            HMapping mapping = this.mapping;
            if (mapping == null) {
                mapping = new HMappingDefaultImpl();
            }

            HBaseConnectionImpl connection = statement.getConnection();
            String dbType = connection.getConnectProperties().getProperty("dbType");

            Scan scan = new Scan();

            for (SQLExpr item : conditions) {
                SQLBinaryOpExpr condition = (SQLBinaryOpExpr) item;
                String fieldName = ((SQLIdentifierExpr) condition.getLeft()).getName();
                Object value = SQLEvalVisitorUtils.eval(dbType, condition.getRight(), statement.getParameters());

                byte[] bytes = HBaseUtils.toBytes(value);
                if (mapping.isRow(fieldName)) {
                    if (condition.getOperator() == SQLBinaryOperator.GreaterThanOrEqual) {
                        scan.setStartRow(bytes);
                    } else if (condition.getOperator() == SQLBinaryOperator.LessThan) {
                        scan.setStopRow(bytes);
                    } else if (condition.getOperator() == SQLBinaryOperator.LessThanOrEqual) {
                        RowFilter filter = new RowFilter(CompareOp.LESS_OR_EQUAL, new BinaryComparator(bytes));
                        setFilter(scan, filter);
                    } else {
                        throw new SQLException("TODO");
                    }
                } else {
                    byte[] qualifier = mapping.getQualifier(fieldName);
                    byte[] family = mapping.getFamily(fieldName);

                    CompareOp compareOp;
                    if (condition.getOperator() == SQLBinaryOperator.Equality) {
                        compareOp = CompareOp.EQUAL;
                    } else if (condition.getOperator() == SQLBinaryOperator.GreaterThan) {
                        compareOp = CompareOp.GREATER;
                    } else if (condition.getOperator() == SQLBinaryOperator.GreaterThanOrEqual) {
                        compareOp = CompareOp.GREATER_OR_EQUAL;
                    } else if (condition.getOperator() == SQLBinaryOperator.LessThan) {
                        compareOp = CompareOp.LESS;
                    } else if (condition.getOperator() == SQLBinaryOperator.LessThanOrEqual) {
                        compareOp = CompareOp.LESS_OR_EQUAL;
                    } else if (condition.getOperator() == SQLBinaryOperator.NotEqual) {
                        compareOp = CompareOp.NOT_EQUAL;
                    } else {
                        throw new SQLException("TODO");
                    }

                    SingleColumnValueFilter filter = new SingleColumnValueFilter(family, qualifier, compareOp, bytes);
                    setFilter(scan, filter);
                }
            }

            HTableInterface htable = connection.getHTable(getTableName());
            ResultScanner scanner = htable.getScanner(scan);

            HScannerResultSetImpl resultSet = new HScannerResultSetImpl(statement, htable, scanner);
            resultSet.setMetaData(resultMetaData);

            return resultSet;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("executeQuery error", e);
        }
    }

    CompareOp toCompareOp(SQLBinaryOperator operator) {
        switch (operator) {
            case Equality:
                return CompareOp.EQUAL;
            case NotEqual:
                return CompareOp.NOT_EQUAL;
            case GreaterThan:
                return CompareOp.GREATER;
            case GreaterThanOrEqual:
                return CompareOp.GREATER_OR_EQUAL;
            case LessThan:
                return CompareOp.LESS;
            case LessThanOrEqual:
                return CompareOp.LESS_OR_EQUAL;
            default:
                throw new UnsupportedOperationException("TODO");
        }
    }

    void setFilter(Scan scan, Filter filter) {
        if (scan.getFilter() == null) {
            scan.setFilter(filter);
        } else if (scan.getFilter() instanceof FilterList) {
            FilterList filterList = (FilterList) scan.getFilter();
            filterList.addFilter(filter);
        } else {
            FilterList filterList = new FilterList(scan.getFilter(), filter);
            scan.setFilter(filterList);
        }
    }

}