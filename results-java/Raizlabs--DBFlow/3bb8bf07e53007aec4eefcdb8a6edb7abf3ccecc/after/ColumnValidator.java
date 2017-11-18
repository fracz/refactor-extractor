package com.raizlabs.android.dbflow.processor.validator;

import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.EnumColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyReferenceDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.List;

/**
 * Description: Ensures the integrity of the annotation processor for columns.
 */
public class ColumnValidator implements Validator<ColumnDefinition> {

    private ColumnDefinition autoIncrementingPrimaryKey;

    @Override
    public boolean validate(ProcessorManager processorManager, ColumnDefinition columnDefinition) {

        boolean success = true;

        if (columnDefinition.columnName == null || columnDefinition.columnName.isEmpty()) {
            success = false;
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                    columnDefinition.columnFieldName, columnDefinition.columnName,
                    columnDefinition.elementTypeName);
        }

        if (columnDefinition.columnAccess instanceof EnumColumnAccess) {
            if (columnDefinition.isPrimaryKey) {
                success = false;
                processorManager.logError("Enums cannot be primary keys. Column: %1s and type: %1s", columnDefinition.columnName,
                        columnDefinition.elementTypeName);
            } else if (columnDefinition instanceof ForeignKeyColumnDefinition) {
                success = false;
                processorManager.logError("Enums cannot be foreign keys. Column: %1s and type: %1s", columnDefinition.columnName,
                        columnDefinition.elementTypeName);
            }
        }

        if (columnDefinition instanceof ForeignKeyColumnDefinition) {
            List<ForeignKeyReferenceDefinition> references = ((ForeignKeyColumnDefinition) columnDefinition).foreignKeyReferenceDefinitionList;
            if (references == null || references.isEmpty()) {
                success = false;
                processorManager.logError(
                        "Foreign Key for field %1s is missing it's references. Column: %1s and type: %1s",
                        columnDefinition.columnFieldName, columnDefinition.columnName,
                        columnDefinition.elementTypeName);
            }

            if (columnDefinition.column.name()
                    .length() > 0) {
                success = false;
                processorManager.logError("Foreign Key %1s cannot specify the column() field. " +
                                "Use a @ForeignKeyReference(columnName = {NAME} instead. Column: %1s and type: %1s",
                        columnDefinition.columnFieldName, columnDefinition.columnName,
                        columnDefinition.elementTypeName);
            }

            //if (references != null && references.size() > 1 &&
            //        (!columnDefinition.isModel && !columnDefinition.fieldIsModelContainer)) {
            //    success = false;
            //    processorManager.logError(
            //            "Foreign key %1s cannot specify more than 1 reference for a non-model field. Column: %1s and type: %1s",
            //            columnDefinition.columnFieldName, columnDefinition.columnName,
            //            columnDefinition.columnFieldType);
            //}

        } else {
            if (autoIncrementingPrimaryKey != null && columnDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.");
                success = false;
            }

            //if (columnDefinition.isModel) {
            //    processorManager.logError("Primary keys cannot be Model objects for Column: %1s and type: %1s",
            //            columnDefinition.columnName, columnDefinition.columnFieldType);
            //    success = false;
            //}

            if (columnDefinition.isPrimaryKeyAutoIncrement) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition;
                } else if (!autoIncrementingPrimaryKey.equals(columnDefinition)) {
                    processorManager.logError(
                            "Only one autoincrementing primary key is allowed on a table. Found Column: %1s and type: %1s",
                            columnDefinition.columnName, columnDefinition.elementTypeName);
                    success = false;
                }
            }
        }

        //if (!(columnDefinition instanceof ForeignKeyColumnDefinition)  && (columnDefinition.isModel || columnDefinition.fieldIsModelContainer)) {
        //    processorManager.logError(
        //            "A Model or ModelContainer field must be a @ForeignKeyReference. Found Column: %1s and type: %1s",
        //            columnDefinition.columnName, columnDefinition.columnFieldType);
        //}

        return success;
    }
}