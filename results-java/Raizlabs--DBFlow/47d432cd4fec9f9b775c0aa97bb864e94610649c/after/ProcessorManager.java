package com.grosner.processor.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.grosner.processor.definition.*;
import com.grosner.processor.handler.BaseContainerHandler;
import com.grosner.processor.handler.Handler;
import com.grosner.processor.writer.DatabaseWriter;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ProcessorManager implements Handler{

    private ProcessingEnvironment mProcessingEnv;

    private List<String> mUniqueDatabases = Lists.newArrayList();

    private Map<String, String> mModelToDatabaseMap = Maps.newHashMap();

    private Map<String, TypeConverterDefinition> mTypeConverters = Maps.newHashMap();

    private Map<String, Map<String, ModelContainerDefinition>> mModelContainers = Maps.newHashMap();

    private Map<String, Map<String, TableDefinition>> mTableDefinitions = Maps.newHashMap();

    private Map<String, Map<String, ModelViewDefinition>> mModelViewDefinition = Maps.newHashMap();

    private Map<String, Map<Integer, List<MigrationDefinition>>> mMigrations = Maps.newHashMap();

    private List<DatabaseWriter> mManagerWriters = Lists.newArrayList();

    private List<BaseContainerHandler> mHandlers = new ArrayList<>();

    public ProcessorManager(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    public void addHandlers(BaseContainerHandler...containerHandlers) {
        for(BaseContainerHandler containerHandler: containerHandlers) {
            if(!mHandlers.contains(containerHandler)) {
                mHandlers.add(containerHandler);
            }
        }
    }

    public Messager getMessager() {
        return mProcessingEnv.getMessager();
    }

    public Types getTypeUtils() {
        return mProcessingEnv.getTypeUtils();
    }

    public Elements getElements() {
        return mProcessingEnv.getElementUtils();
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return mProcessingEnv;
    }

    public void addDatabase(String database) {
        if(!mUniqueDatabases.contains(database)) {
            mUniqueDatabases.add(database);
        }
    }

    public boolean hasOneDatabase() {
        return mUniqueDatabases.size() == 1;
    }

    public void addFlowManagerWriter(DatabaseWriter databaseWriter) {
        mManagerWriters.add(databaseWriter);
    }

    public List<DatabaseWriter> getManagerWriters() {
        return mManagerWriters;
    }

    public void addTypeConverterDefinition(TypeConverterDefinition definition) {
        mTypeConverters.put(definition.getModelClassQualifiedName(), definition);
    }

    public TypeConverterDefinition getTypeConverterDefinition(TypeElement typeElement) {
        return mTypeConverters.get(typeElement.getQualifiedName().toString());
    }

    public void addModelToDatabase(String modelName, String databaseName) {
        addDatabase(databaseName);
        mModelToDatabaseMap.put(modelName, databaseName);
    }

    public String getDatabase(String modelName) {
        return mModelToDatabaseMap.get(modelName);
    }

    public void addModelContainerDefinition(ModelContainerDefinition modelContainerDefinition) {
        String modelClassName = modelContainerDefinition.classElement.getSimpleName().toString();
        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = mModelContainers.get(getDatabase(modelClassName));
        if(modelContainerDefinitionMap == null) {
            modelContainerDefinitionMap = Maps.newHashMap();
            mModelContainers.put(getDatabase(modelClassName), modelContainerDefinitionMap);
        }
        modelContainerDefinitionMap.put(modelContainerDefinition.getModelClassQualifiedName(), modelContainerDefinition);
    }

    public ModelContainerDefinition getModelContainerDefinition(String databaseName, TypeElement typeElement) {
        return mModelContainers.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addTableDefinition(TableDefinition modelContainerDefinition) {
        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(modelContainerDefinition.databaseName);
        if(tableDefinitionMap == null) {
            tableDefinitionMap = Maps.newHashMap();
            mTableDefinitions.put(modelContainerDefinition.databaseName, tableDefinitionMap);
        }
        tableDefinitionMap.put(modelContainerDefinition.element.asType().toString(), modelContainerDefinition);
    }

    public TableDefinition getTableDefinition(String databaseName, TypeElement typeElement) {
        return mTableDefinitions.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public void addModelViewDefinition(ModelViewDefinition modelViewDefinition) {
        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(modelViewDefinition.databaseName);
        if(modelViewDefinitionMap == null) {
            modelViewDefinitionMap = Maps.newHashMap();
            mModelViewDefinition.put(modelViewDefinition.databaseName, modelViewDefinitionMap);
        }
        modelViewDefinitionMap.put(modelViewDefinition.element.asType().toString(), modelViewDefinition);
    }

    public ModelViewDefinition getModelViewDefinition(String databaseName, TypeElement typeElement) {
        return mModelViewDefinition.get(databaseName).get(typeElement.getQualifiedName().toString());
    }

    public Set<TypeConverterDefinition> getTypeConverters() {
        return Sets.newHashSet(mTypeConverters.values());
    }

    public Set<ModelContainerDefinition> getModelContainers(String databaseName) {
        if(hasOneDatabase()) {
            databaseName = "";
        }

        Map<String, ModelContainerDefinition> modelContainerDefinitionMap = mModelContainers.get(databaseName);
        if(modelContainerDefinitionMap!= null) {
            return  Sets.newHashSet(mModelContainers.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<TableDefinition> getTableDefinitions(String databaseName) {
        if(hasOneDatabase()) {
            databaseName = "";
        }

        Map<String, TableDefinition> tableDefinitionMap = mTableDefinitions.get(databaseName);
        if(tableDefinitionMap != null) {
            return Sets.newHashSet(mTableDefinitions.get(databaseName).values());
        }
        return Sets.newHashSet();
    }

    public Set<ModelViewDefinition> getModelViewDefinitions(String databaseName) {
        if(hasOneDatabase()) {
            databaseName = "";
        }

        Map<String, ModelViewDefinition> modelViewDefinitionMap = mModelViewDefinition.get(databaseName);
        if(modelViewDefinitionMap != null) {
            return Sets.newHashSet(mModelViewDefinition.get(databaseName).values());
        } else {
            return Sets.newHashSet();
        }
    }

    public void addMigrationDefinition(MigrationDefinition migrationDefinition) {
        Map<Integer, List<MigrationDefinition>> migrationDefinitionMap = mMigrations.get(migrationDefinition.databaseName);
        if(migrationDefinitionMap == null) {
            migrationDefinitionMap = Maps.newHashMap();
            mMigrations.put(migrationDefinition.databaseName, migrationDefinitionMap);
        }

        List<MigrationDefinition> migrationDefinitions = migrationDefinitionMap.get(migrationDefinition.version);
        if(migrationDefinitions == null) {
            migrationDefinitions = Lists.newArrayList();
            migrationDefinitionMap.put(migrationDefinition.version, migrationDefinitions);
        }

        if(!migrationDefinitions.contains(migrationDefinition)) {
            migrationDefinitions.add(migrationDefinition);
        }
    }

    public Map<Integer, List<MigrationDefinition>> getMigrationsForDatabase(String databaseName) {
        if(hasOneDatabase()) {
            databaseName = "";
        }

        Map<Integer, List<MigrationDefinition>> migrationDefinitions = mMigrations.get(databaseName);
        if(migrationDefinitions != null) {
            return migrationDefinitions;
        } else {
            return Maps.newHashMap();
        }
    }

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        for(BaseContainerHandler containerHandler: mHandlers) {
            containerHandler.handle(processorManager, roundEnvironment);
        }
    }
}