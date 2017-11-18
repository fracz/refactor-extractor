package com.grosner.dbflow.sql.language;

import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Set<ModelClass extends Model> implements WhereBase<ModelClass> {

    private ConditionQueryBuilder<ModelClass> mConditionQueryBuilder;

    private Query mUpdate;

    public Set(Query update, Class<ModelClass> table) {
        mUpdate = update;
        mConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(table).setSeparator(",");
    }

    public Set<ModelClass> conditionQuery(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        if (conditionQueryBuilder != null) {
            mConditionQueryBuilder = conditionQueryBuilder;
        }
        return this;
    }

    public Set<ModelClass> conditions(Condition... conditions) {
        mConditionQueryBuilder.putConditions(conditions);
        return this;
    }

    public Where<ModelClass> where(Condition... conditions) {
        return where().andThese(conditions);
    }

    public Where<ModelClass> where() {
        return new Where<ModelClass>(this);
    }

    public Where<ModelClass> where(ConditionQueryBuilder<ModelClass> whereConditionBuilder) {
        return where().whereQuery(whereConditionBuilder);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder =
                new QueryBuilder(mUpdate.getQuery())
                        .append("SET ")
                        .append(mConditionQueryBuilder.getQuery()).appendSpace();
        return queryBuilder.getQuery();
    }

    @Override
    public Class<ModelClass> getTable() {
        return mConditionQueryBuilder.getTableClass();
    }

    @Override
    public Query getQueryBuilderBase() {
        return mUpdate;
    }
}