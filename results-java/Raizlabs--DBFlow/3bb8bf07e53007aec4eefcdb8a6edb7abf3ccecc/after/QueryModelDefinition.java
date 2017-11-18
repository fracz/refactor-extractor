package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.LoadFromCursorMethod;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Description:
 */
public class QueryModelDefinition extends BaseTableDefinition {

    private static final String DBFLOW_QUERY_MODEL_TAG = "QueryModel";

    public static final String DBFLOW_TABLE_ADAPTER = "QueryModelAdapter";

    public String databaseName;

    public boolean allFields;

    public String adapterName;

    public boolean implementsLoadFromCursorListener = false;

    MethodDefinition[] methods;

    public QueryModelDefinition(Element typeElement,
                                ProcessorManager processorManager) {
        super(typeElement, processorManager);


        QueryModel queryModel = typeElement.getAnnotation(QueryModel.class);
        databaseName = queryModel.databaseName();
        databaseMethod = manager.getDatabaseWriter(databaseName);
        allFields = queryModel.allFields();
        adapterName = getModelClassName() + databaseMethod.classSeparator + DBFLOW_TABLE_ADAPTER;

        processorManager.addModelToDatabase(elementClassName, databaseName);

        implementsLoadFromCursorListener = ProcessorUtils
                .implementsClass(manager.getProcessingEnvironment(), ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(),
                        (TypeElement) element);

        setOutputClassName(databaseMethod.classSeparator + DBFLOW_QUERY_MODEL_TAG);

        methods = new MethodDefinition[]{
                new LoadFromCursorMethod(this, false, implementsLoadFromCursorListener)
        };

        createColumnDefinitions(((TypeElement) typeElement));
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.addPropertyDefinition(typeBuilder);
        }
    }

    @Override
    protected void createColumnDefinitions(TypeElement typeElement) {
        List<? extends Element> variableElements = manager.getElements().getAllMembers(typeElement);
        ColumnValidator columnValidator = new ColumnValidator();
        for (Element variableElement : variableElements) {

            // no private static or final fields
            boolean isValidColumn = allFields && (variableElement.getKind().isField() &&
                    !variableElement.getModifiers().contains(Modifier.STATIC) &&
                    !variableElement.getModifiers().contains(Modifier.PRIVATE) &&
                    !variableElement.getModifiers().contains(Modifier.FINAL));

            if (variableElement.getAnnotation(Column.class) != null || isValidColumn) {
                ColumnDefinition columnDefinition = new ColumnDefinition(manager, (VariableElement) variableElement);
                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition);
                }
            }
        }
    }

    @Override
    public List<ColumnDefinition> getPrimaryColumnDefinitions() {
        // Shouldn't include any
        return new ArrayList<>();
    }

    @Override
    public ClassName getPropertyClassName() {
        return outputClassName;
    }

    public String getQualifiedModelClassName() {
        return packageName + "." + getModelClassName();
    }

    public void writeAdapter(ProcessingEnvironment processingEnvironment) throws IOException {


        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassNames.QUERY_MODEL_ADAPTER, elementClassName));

        for (MethodDefinition method : methods) {
            MethodSpec methodSpec = method.getMethodSpec();
            if (methodSpec != null) {
                typeBuilder.addMethod(methodSpec);
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return new $T()", elementClassName).build());

        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();
        javaFile.writeTo(processingEnvironment.getFiler());
    }

}