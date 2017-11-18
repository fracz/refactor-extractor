package com.grosner.dbflow.sql.builder;

import com.grosner.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ColumnNameNotFoundException extends RuntimeException {
    ColumnNameNotFoundException(String columnName, ModelAdapter tableStructure) {
        super("The column : " + columnName + " was not found for " + tableStructure.getTableName());
    }
}