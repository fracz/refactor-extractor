package com.grosner.dbflow.structure;

import android.content.Context;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class defines the structure of the DB. It contains tables, type converters,
 * and other information pertaining to this DB.
 */
public class DBStructure {

    /**
     * Holds onto the {@link com.grosner.dbflow.structure.ModelView} for each class
     */
    private Map<Class<? extends BaseModelView>, ModelViewDefinition> mModelViews;

    /**
     * Holds onto the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} for each {@link com.grosner.dbflow.structure.Model}
     * class so we only created these once. Useful for many select statements on a specific table.
     */
    private Map<Class<? extends Model>, ConditionQueryBuilder> mPrimaryWhereQueryBuilderMap;

    /**
     * Holds onto the {@link java.lang.reflect.Constructor} for each {@link com.grosner.dbflow.structure.Model}
     * so we only need to retrieve these once to improve performance. This is used when we convert a {@link android.database.Cursor}
     * to a {@link com.grosner.dbflow.structure.Model}
     */
    private Map<Class<? extends Model>, Constructor<? extends Model>> mModelConstructorMap;

    private Map<Class<? extends Model>, ModelAdapter> mModelAdapterMap;

    /**
     * Holds the database information here.
     */
    private FlowManager mManager;

    /**
     * Will ignore subsequent calls to reset DB when this is true
     */
    private boolean isResetting = false;

    /**
     * Constructs a new instance with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link com.grosner.dbflow.config.DBConfiguration}
     *
     * @param flowManager     The db manager
     * @param dbConfiguration The configuration for this db
     */
    public DBStructure(FlowManager flowManager) {
        mManager = flowManager;
        initializeStructure(flowManager.getDbConfiguration());
    }

    /**
     * This will construct the runtime structure of our DB for reference while the app is running.
     *
     * @param dbConfiguration The configuration for this db
     */
    private void initializeStructure(DBConfiguration dbConfiguration) {
        mPrimaryWhereQueryBuilderMap = new HashMap<Class<? extends Model>, ConditionQueryBuilder>();
        mModelViews = new HashMap<Class<? extends BaseModelView>, ModelViewDefinition>();
        mModelAdapterMap = new HashMap<>();
        mModelConstructorMap = new HashMap<Class<? extends Model>, Constructor<? extends Model>>();

        List<Class<? extends Model>> modelList;
        if (dbConfiguration.hasModelClasses() || FlowManager.isMultipleDatabases()) {
            modelList = dbConfiguration.getModelClasses();
        } else {
            modelList = ScannedModelContainer.getInstance().getModelClasses();
        }

        // only add models if its a multitable setup
        if (modelList != null && FlowManager.isMultipleDatabases()) {
            ScannedModelContainer.addModelClassesToManager(mManager, modelList);
        }

        ScannedModelContainer.getInstance().applyModelListToFoundData(modelList, this);

    }

    /**
     * This will delete and recreate the whole stored database. WARNING: all data stored will be lost.
     *
     * @param context The applications context
     */
    public void reset(Context context) {
        if (!isResetting) {
            isResetting = true;
            context.deleteDatabase(mManager.getDbConfiguration().getDatabaseName());
            initializeStructure(mManager.getDbConfiguration());
            isResetting = false;
        }
    }

    /**
     * Returns the Where Primary key query string from the cache for a specific model.
     *
     * @param modelTable
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> ConditionQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> modelTable) {
        ConditionQueryBuilder<ModelClass> conditionQueryBuilder = getWhereQueryBuilderMap().get(modelTable);
        if (conditionQueryBuilder == null) {
            conditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(modelTable).emptyPrimaryConditions();
            getWhereQueryBuilderMap().put(modelTable, conditionQueryBuilder);
        }
        return conditionQueryBuilder;
    }

    /**
     * Returns the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} map for this database
     *
     * @return
     */
    public Map<Class<? extends Model>, ConditionQueryBuilder> getWhereQueryBuilderMap() {
        return mPrimaryWhereQueryBuilderMap;
    }

    /**
     * Returns the {@link java.lang.reflect.Constructor} for the {@link ModelClass}. It will add the constructor
     * to the map if it does not exist there already.
     *
     * @param modelClass
     * @param <ModelClass>
     * @return
     * @throws java.lang.RuntimeException when the default constructor does not exist.
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> Constructor<ModelClass> getConstructorForModel(Class<ModelClass> modelClass) throws NoSuchMethodException {
        Constructor<ModelClass> constructor = (Constructor<ModelClass>) getModelConstructorMap().get(modelClass);
        if (constructor == null) {
            constructor = modelClass.getDeclaredConstructor();

            //enable private constructors
            constructor.setAccessible(true);
            getModelConstructorMap().put(modelClass, constructor);
        }

        return constructor;
    }

    public Map<Class<? extends Model>, Constructor<? extends Model>> getModelConstructorMap() {
        return mModelConstructorMap;
    }

    /**
     * Adds a {@link com.grosner.dbflow.structure.ModelViewDefinition} to the structure for reference later.
     *
     * @param modelViewDefinition
     */
    @SuppressWarnings("unchecked")
    public void putModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        getModelViews().put(modelViewDefinition.getModelViewClass(), modelViewDefinition);
    }

    public void putModelAdapter(ModelAdapter modelAdapter) {
        mModelAdapterMap.put(modelAdapter.getModelClass(), modelAdapter);
    }

    public <ModelClass extends Model> ModelAdapter getModelAdapter(Class<ModelClass> modelClass) {
        ModelAdapter modelAdapter = mModelAdapterMap.get(modelClass);
        if(modelAdapter == null) {
            throw new RuntimeException("Model Adapter for: " + modelClass + " not found. Check your configuration and ensure it was annotated with @Table");
        }
        return modelAdapter;
    }

    /**
     * Returns the {@link com.grosner.dbflow.structure.ModelViewDefinition} map for this database
     *
     * @return
     */
    public Map<Class<? extends BaseModelView>, ModelViewDefinition> getModelViews() {
        return mModelViews;
    }

    public FlowManager getManager() {
        return mManager;
    }
}