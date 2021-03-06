/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IMenuManager;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.data.DBDValueEditor;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.properties.PropertySourceAbstract;

import java.sql.SQLException;
import java.sql.Struct;

/**
 * JDBC Struct value handler.
 * Handle STRUCT types.
 *
 * @author Serge Rider
 */
public class JDBCStructValueHandler extends JDBCAbstractValueHandler {

    static final Log log = LogFactory.getLog(JDBCStructValueHandler.class);

    public static final JDBCStructValueHandler INSTANCE = new JDBCStructValueHandler();

    @Override
    public int getFeatures()
    {
        return FEATURE_VIEWER | FEATURE_EDITOR | FEATURE_SHOW_ICON;
    }

    /**
     * NumberFormat is not thread safe thus this method is synchronized.
     */
    @Override
    public synchronized String getValueDisplayString(DBSTypedObject column, Object value)
    {
        JDBCStruct struct = (JDBCStruct)value;
        return struct == null || struct.isNull() ? DBConstants.NULL_VALUE_LABEL : "[" + struct.getTypeName() + "]";
    }

    @Override
    protected Object getColumnValue(
        DBCExecutionContext context,
        JDBCResultSet resultSet,
        DBSTypedObject column,
        int columnIndex)
        throws DBCException, SQLException
    {
        Object value = resultSet.getObject(columnIndex);
        String typeName;
        if (value instanceof Struct) {
            typeName = ((Struct) value).getSQLTypeName();
        } else {
            typeName = column.getTypeName();
        }
        DBSDataType dataType = null;
        try {
            dataType = DBUtils.resolveDataType(context.getProgressMonitor(), context.getDataSource(), typeName);
        } catch (DBException e) {
            log.error("Error resolving data type '" + typeName + "'", e);
        }
        if (value == null) {
            return new JDBCStruct(dataType, null);
        } else if (value instanceof Struct) {
            return new JDBCStruct(dataType, (Struct) value);
        } else {
            throw new DBCException("Unsupported struct type: " + value.getClass().getName());
        }
    }

    @Override
    protected void bindParameter(
        JDBCExecutionContext context,
        JDBCPreparedStatement statement,
        DBSTypedObject paramType,
        int paramIndex,
        Object value)
        throws DBCException, SQLException
    {
        throw new DBCException("Unsupported value type: " + value);
    }

    @Override
    public Class getValueObjectType()
    {
        return Struct.class;
    }

    @Override
    public Object copyValueObject(DBCExecutionContext context, DBSTypedObject column, Object value)
        throws DBCException
    {
        return null;
    }

/*
    public String getValueDisplayString(DBSTypedObject column, Object value)
    {
        if (value instanceof JDBCStruct) {
            String displayString = value.toString();
            if (displayString != null) {
                return displayString;
            }
        }
        return super.getValueDisplayString(column, value);
    }
*/

    @Override
    public void fillContextMenu(IMenuManager menuManager, final DBDValueController controller)
        throws DBCException
    {
    }

    @Override
    public void fillProperties(PropertySourceAbstract propertySource, DBDValueController controller)
    {
        try {
            Object value = controller.getValue();
            if (value instanceof JDBCStruct) {
                propertySource.addProperty(
                    "sql_type", //$NON-NLS-1$
                    CoreMessages.model_jdbc_type_name,
                    ((JDBCStruct)value).getTypeName());
            }
        }
        catch (Exception e) {
            log.warn("Could not extract struct value information", e); //$NON-NLS-1$
        }
    }

    @Override
    public DBDValueEditor createEditor(final DBDValueController controller)
        throws DBException
    {
        return null;
    }

}