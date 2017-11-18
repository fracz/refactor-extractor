package com.grosner.dbflow.structure.container;

import android.database.Cursor;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.InternalAdapter;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ContainerAdapter<ModelClass extends Model> implements InternalAdapter<ModelClass> {

    public abstract void loadFromCursor(Cursor cursor,ModelContainer<ModelClass, ?> modelContainer);

    public abstract void save(boolean async, ModelContainer<ModelClass, ?> modelContainer, int saveMode);

    public abstract boolean exists(ModelContainer<ModelClass, ?> modelContainer);

    public abstract void delete(boolean async, ModelContainer<ModelClass, ?> modelContainer);

    @Override
    public abstract Class<ModelClass> getModelClass();

    @Override
    public abstract String getTableName();

    public abstract ModelClass toModel(ModelContainer<ModelClass, ?> modelContainer);

    public abstract ConditionQueryBuilder<ModelClass> getPrimaryModelWhere(ModelContainer<ModelClass, ?> modelContainer);


}