package com.grosner.processor.handler;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Database;
import com.grosner.processor.Classes;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.writer.DatabaseWriter;
import com.grosner.processor.writer.FlowManagerHolderWriter;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowManagerHandler extends BaseContainerHandler<Database> {


    public static final Set<Modifier> FIELD_MODIFIERS = Sets.newHashSet(Modifier.PRIVATE, Modifier.FINAL);
    public static final Set<Modifier> FIELD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

    public static final Set<Modifier> METHOD_MODIFIERS = Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL);
    public static final Set<Modifier> METHOD_MODIFIERS_STATIC = Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    public static final String MODEL_FIELD_NAME = "mModels";
    public static final String MODEL_ADAPTER_MAP_FIELD_NAME = "mModelAdapters";
    public static final String MODEL_CONTAINER_ADAPTER_MAP_FIELD_NAME = "mModelContainerAdapters";
    public static final String TYPE_CONVERTER_MAP_FIELD_NAME = "mTypeConverters";
    public static final String MODEL_VIEW_FIELD_NAME = "mModelViews";
    public static final String FLOW_SQL_LITE_OPEN_HELPER_FIELD_NAME = "mHelper";
    public static final String IS_RESETTING = "isResetting";
    public static final String MANAGER_MAP_NAME = "mManagerMap";
    public static final String MANAGER_NAME_MAP = "mManagerNameMap";
    public static final String MIGRATION_FIELD_NAME = "mMigrationMap";

    public static final String MODEL_VIEW_ADAPTER_MAP_FIELD_NAME = "mModelViewAdapterMap";

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        super.handle(processorManager, roundEnvironment);
        if (roundEnvironment.processingOver()) {
            try {
                JavaWriter staticFlowManager = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                        .createSourceFile(Classes.FLOW_MANAGER_PACKAGE + "." + Classes.FLOW_MANAGER_STATIC_CLASS_NAME).openWriter());
                new FlowManagerHolderWriter(processorManager).write(staticFlowManager);

                staticFlowManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected Class<Database> getAnnotationClass() {
        return Database.class;
    }


    @Override
    protected void onProcessElement(ProcessorManager processorManager, String packageName, Element element) {
        try {

            DatabaseWriter managerWriter = new DatabaseWriter(processorManager, packageName, element);
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                    .createSourceFile(managerWriter.getFQCN()).openWriter());
            managerWriter.write(javaWriter);
            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}