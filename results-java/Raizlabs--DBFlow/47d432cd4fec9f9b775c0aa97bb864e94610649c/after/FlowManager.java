package com.grosner.dbflow.config;

import android.content.Context;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.migration.Migration;
import com.grosner.dbflow.structure.*;
import com.grosner.dbflow.structure.container.ContainerAdapter;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: Holds information about the database and wraps some of the methods.
 */
public class FlowManager {

    private static Context context;

    private static FlowManagerHolder mManagerHolder;

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The table name, which can be different than the {@link com.grosner.dbflow.structure.Model} class name
     */
    @SuppressWarnings("unchecked")
    public static String getTableName(Class<? extends Model> table) {
        ModelAdapter modelAdapter = getModelAdapter(table);
        String tableName = null;
        if(modelAdapter == null) {
            ModelViewAdapter modelViewAdapter = getManagerForTable(table).getModelViewAdapterForTable((Class<? extends BaseModelView>) table);
            if(modelViewAdapter != null) {
                tableName = modelViewAdapter.getViewName();
            }
        } else {
            tableName = modelAdapter.getTableName();
        }
        return tableName;
    }

    /**
     * Returns the corresponding {@link com.grosner.dbflow.config.FlowManager} for the specified model
     *
     * @param table
     * @return
     */
    public static BaseFlowManager getManagerForTable(Class<? extends Model> table) {
        if(mManagerHolder == null) {
            try {
                mManagerHolder = (FlowManagerHolder) Class.forName("com.grosner.dbflow.config.FlowManager$Holder").newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        BaseFlowManager flowManager = mManagerHolder.getFlowManagerForTable(table);
        if (flowManager == null) {
            throw new InvalidDBConfiguration();
        }
        return flowManager;
    }

    /**
     * Returns the corresponding {@link com.grosner.dbflow.config.FlowManager} for the specified model
     *
     * @param table
     * @return
     */
    public static BaseFlowManager getManager(String databaseName) {
        if(mManagerHolder == null) {
            try {
                mManagerHolder = (FlowManagerHolder) Class.forName("com.grosner.dbflow.config.FlowManager$Holder").newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        BaseFlowManager flowManager = mManagerHolder.getFlowManager(databaseName);
        if (flowManager == null) {
            throw new InvalidDBConfiguration();
        }
        return flowManager;
    }

    /**
     * Returns the primary where query for a specific table. Its the WHERE statement containing columnName = ?.
     *
     * @param table The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The primary where query
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ConditionQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> table) {
        return getManagerForTable(table).getModelAdapterForTable(table).getPrimaryModelWhere();
    }

    /**
     * Will throw an exception if this class is not initialized yet in {@link #initialize(android.content.Context, DBConfiguration)}
     *
     * @return
     */
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return context;
    }

    public static void init(Context context) {
        FlowManager.context = context;
    }

    /**
     * Returns the specific {@link com.grosner.dbflow.converter.TypeConverter} for this model. It defines
     * how the class is stored in the DB
     *
     * @param modelClass   The class that implements {@link com.grosner.dbflow.structure.Model}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    public static TypeConverter getTypeConverterForClass(Class<?> modelClass) {
        return mManagerHolder.getTypeConverterForClass(modelClass);
    }

    // region Getters

    /**
     * Releases references to the structure, configuration, and closes the DB.
     */
    public static synchronized void destroy() {
        context = null;
    }

    /**
     * Returns the model adapter for the specified table. Used in loading and modifying the model class.
     * @param modelClass The class of the table
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelAdapter<ModelClass > getModelAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getManagerForTable(modelClass).getModelAdapterForTable(modelClass);
    }

    /**
     * Returns the container adapter for the specified table. These are only generated when you specify {@link com.grosner.dbflow.annotation.ContainerAdapter}
     * in your model class so it can be used for containers. These are not generated by default as a means to save space.
     * @param modelClass The class of the table
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ContainerAdapter<ModelClass> getContainerAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getManagerForTable(modelClass).getModelContainerAdapterForTable(modelClass);
    }

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the {@link com.grosner.dbflow.annotation.ModelView} annotation.
     * @param modelViewClass The class of the VIEW
     * @param <ModelViewClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <ModelViewClass extends BaseModelView<? extends Model>> ModelViewAdapter<? extends Model, ModelViewClass> getModelViewAdapter(Class<ModelViewClass> modelViewClass) {
        return FlowManager.getManagerForTable(modelViewClass).getModelViewAdapterForTable(modelViewClass);
    }

    static Map<Integer, List<Migration>> getMigrations(String databaseName) {
        return getManager(databaseName).getMigrations();
    }

    // endregion

}