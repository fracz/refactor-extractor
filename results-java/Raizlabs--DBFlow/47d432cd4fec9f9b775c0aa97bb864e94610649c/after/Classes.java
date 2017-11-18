package com.grosner.processor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Classes {

    public static final String CURSOR = "android.database.Cursor";

    public static final String CONTENT_VALUES = "android.content.ContentValues";

    public static final String BASE_PACKAGE = "com.grosner.dbflow.";

    public static final String STRUCTURE = BASE_PACKAGE + "structure.";

    public static final String CONTAINER = STRUCTURE + "container.";

    public static final String SQL = BASE_PACKAGE + "sql.";

    public static final String CONFIG = BASE_PACKAGE + "config.";

    public static final String BUILDER = SQL + "builder.";

    public static final String MODEL_ADAPTER = STRUCTURE + "ModelAdapter";

    public static final String MODEL_CONTAINER = CONTAINER + "ModelContainer";

    public static final String MODEL = STRUCTURE + "Model";

    public static final String CONDITION_QUERY_BUILDER = BUILDER + "ConditionQueryBuilder";

    public static final String SELECT = SQL + "language.Select";

    public static final String FLOW_MANAGER = CONFIG + "FlowManager";

    public static final String SQL_UTILS = SQL + "SqlUtils";

    public static final String CONDITION = BUILDER + "Condition";

    public static final String TYPE_CONVERTER = "com.grosner.dbflow.converter.TypeConverter";

    public static final String MODEL_VIEW = STRUCTURE + "BaseModelView";

    public static final String DELETE = SQL + "language.Delete";

    public static final String CONTAINER_ADAPTER = CONTAINER + "ContainerAdapter";

    public static final String MODEL_CONTAINER_UTILS = CONTAINER + "ModelContainerUtils";

    public static final String TRANSACTION_MANAGER = BASE_PACKAGE + "runtime.TransactionManager";

    public static final String PROCESS_MODEL_INFO = BASE_PACKAGE + "runtime.transaction.process.ProcessModelInfo";

    public static final String DBTRANSACTION_INFO = BASE_PACKAGE + "runtime.DBTransactionInfo";

    public static final String FLOW_MANAGER_PACKAGE = "com.grosner.dbflow.config";

    public static final String FLOW_MANAGER_STATIC_CLASS_NAME = "FlowManager$Holder";

    public static final String FLOW_MANAGER_CLASS_NAME = "FlowManager$Generated";

    public static final String FLOW_MANAGER_INTERFACE = "BaseFlowManager";

    public static final String MAP = "java.util.Map";

    public static final String HASH_MAP = "java.util.HashMap";

    public static final String LIST = "java.util.List";

    public static final String ARRAY_LIST = "java.util.ArrayList";

    public static final String FLOW_MANAGER_STATIC_INTERFACE = FLOW_MANAGER_PACKAGE + ".FlowManagerHolder";

    public static final String BASE_FLOW_MANAGER = FLOW_MANAGER_PACKAGE + ".BaseFlowManager";

    public static final String MODEL_VIEW_ADAPTER = STRUCTURE + "ModelViewAdapter";

    public static final String MIGRATION = SQL + "migration.Migration";
}