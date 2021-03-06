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
package org.jkiss.dbeaver.model.impl.jdbc.dbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.ui.IObjectImageProvider;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceInfo;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCDataType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.model.struct.rdb.DBSCatalog;
import org.jkiss.dbeaver.model.struct.rdb.DBSSchema;
import org.jkiss.dbeaver.model.struct.rdb.DBSTable;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JDBCColumnMetaData
 */
public class JDBCColumnMetaData implements DBCAttributeMetaData, IObjectImageProvider
{
    static final Log log = LogFactory.getLog(JDBCColumnMetaData.class);

    private JDBCResultSetMetaData resultSetMeta;
    private int index;
    private boolean notNull;
    private long displaySize;
    private String label;
    private String name;
    private int precision;
    private int scale;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private int typeID;
    private String typeName;
    private boolean readOnly;
    private boolean writable;
    private JDBCTableMetaData tableMetaData;
    private DBSEntityAttribute tableColumn;

    JDBCColumnMetaData(JDBCResultSetMetaData resultSetMeta, int index)
        throws SQLException
    {
        this.resultSetMeta = resultSetMeta;

        DBPObject rsSource = this.resultSetMeta.getResultSet().getSource();
        DBSObject dataContainer = rsSource instanceof DBCStatement ? ((DBCStatement)rsSource).getDataContainer() : null;
        DBSTable ownerTable = null;
        if (dataContainer instanceof DBSTable) {
            ownerTable = (DBSTable)dataContainer;
        }
        this.index = index;

        ResultSetMetaData metaData = resultSetMeta.getJdbcMetaData();
        this.label = metaData.getColumnLabel(index);
        this.name = metaData.getColumnName(index);
        boolean hasData = false;

        String fetchedTableName = null;
        try {
            fetchedTableName = metaData.getTableName(index);
        } catch (SQLException e) {
            log.debug(e);
        }
        String fetchedCatalogName = null;
        try {
            fetchedCatalogName = metaData.getCatalogName(index);
        } catch (SQLException e) {
            log.debug(e);
        }
        String fetchedSchemaName = null;
        try {
            fetchedSchemaName = metaData.getSchemaName(index);
        } catch (SQLException e) {
            log.debug(e);
        }
        // Check for tables name
        // Sometimes [DBSPEC: Informix] it contains schema/catalog name inside
        if (!CommonUtils.isEmpty(fetchedTableName) && CommonUtils.isEmpty(fetchedCatalogName) && CommonUtils.isEmpty(fetchedSchemaName)) {
            final DBPDataSource dataSource = resultSetMeta.getResultSet().getContext().getDataSource();
            final DBPDataSourceInfo dsInfo = dataSource.getInfo();
            if (!DBUtils.isQuotedIdentifier(dataSource, fetchedTableName)) {
                final String catalogSeparator = dsInfo.getCatalogSeparator();
                final int catDivPos = fetchedTableName.indexOf(catalogSeparator);
                if (catDivPos != -1 && (dsInfo.getCatalogUsage() & DBPDataSourceInfo.USAGE_DML) != 0) {
                    // Catalog in table name - extract it
                    fetchedCatalogName = fetchedTableName.substring(0, catDivPos);
                    fetchedTableName = fetchedTableName.substring(catDivPos + catalogSeparator.length());
                }
                final char structSeparator = dsInfo.getStructSeparator();
                final int schemaDivPos = fetchedTableName.indexOf(structSeparator);
                if (schemaDivPos != -1 && (dsInfo.getSchemaUsage() & DBPDataSourceInfo.USAGE_DML) != 0) {
                    // Schema in table name - extract it
                    fetchedSchemaName = fetchedTableName.substring(0, schemaDivPos);
                    fetchedTableName = fetchedTableName.substring(schemaDivPos + 1);
                }
            }
        }

        if (ownerTable != null) {
            // Get column using void monitor because all columns MUST be already read
            try {
                this.tableColumn = ownerTable.getAttribute(VoidProgressMonitor.INSTANCE, name);
            }
            catch (DBException e) {
                log.warn(e);
            }
            if (this.tableColumn != null) {
                this.notNull = this.tableColumn.isRequired();
                this.displaySize = this.tableColumn.getMaxLength();
                DBSObject tableParent = ownerTable.getParentObject();
                DBSObject tableGrandParent = tableParent == null ? null : tableParent.getParentObject();
                this.catalogName = tableParent instanceof DBSCatalog ? tableParent.getName() : tableGrandParent instanceof DBSCatalog ? tableGrandParent.getName() : null;
                this.schemaName = tableParent instanceof DBSSchema ? tableParent.getName() : null;
                this.tableName = fetchedTableName;
                this.typeID = this.tableColumn.getTypeID();
                this.typeName = this.tableColumn.getTypeName();
                this.readOnly = false;
                this.writable = true;
                this.precision = this.tableColumn.getPrecision();
                this.scale = this.tableColumn.getScale();

                try {
                    this.tableMetaData = resultSetMeta.getTableMetaData(ownerTable);
                    if (this.tableMetaData != null) {
                        this.tableMetaData.addColumn(this);
                    }
                }
                catch (DBException e) {
                    log.warn(e);
                }
                hasData = true;
            }

        }

        if (!hasData) {
            this.notNull = metaData.isNullable(index) == ResultSetMetaData.columnNoNulls;
            try {
                this.displaySize = metaData.getColumnDisplaySize(index);
            } catch (SQLException e) {
                this.displaySize = 0;
            }
            this.catalogName = fetchedCatalogName;
            this.schemaName = fetchedSchemaName;
            this.tableName = fetchedTableName;
            this.typeID = metaData.getColumnType(index);
            this.typeName = metaData.getColumnTypeName(index);
            this.readOnly = metaData.isReadOnly(index);
            this.writable = metaData.isWritable(index);

            try {
                this.precision = metaData.getPrecision(index);
            } catch (Exception e) {
                // NumberFormatException occurred in Oracle on BLOB columns
                this.precision = 0;
            }
            try {
                this.scale = metaData.getScale(index);
            } catch (Exception e) {
                this.scale = 0;
            }

            try {
                if (!CommonUtils.isEmpty(this.tableName)) {
                    this.tableMetaData = resultSetMeta.getTableMetaData(catalogName, schemaName, tableName);
                    if (this.tableMetaData != null) {
                        this.tableMetaData.addColumn(this);
                    }
                }
            }
            catch (DBException e) {
                log.warn(e);
            }
        }
    }

    JDBCResultSetMetaData getResultSetMeta()
    {
        return resultSetMeta;
    }

    @Override
    public int getIndex()
    {
        return index;
    }

    @Override
    public boolean isRequired()
    {
        return notNull;
    }

    @Override
    public long getMaxLength()
    {
        return displaySize;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getPrecision()
    {
        return precision;
    }

    @Override
    public int getScale()
    {
        return scale;
    }

    @Override
    public String getTableName()
    {
        return tableMetaData != null ? tableMetaData.getEntityName() : tableName;
    }

    @Override
    public String getCatalogName()
    {
        return catalogName;
    }

    @Override
    public String getSchemaName()
    {
        return schemaName;
    }

    @Override
    public int getTypeID()
    {
        return typeID;
    }

    @Override
    public DBSDataKind getDataKind()
    {
        return JDBCDataType.getDataKind(typeName, typeID);
    }

    @Override
    public String getTypeName()
    {
        return typeName;
    }

    @Override
    public boolean isReadOnly()
    {
        return readOnly;
    }

    @Override
    public JDBCTableMetaData getEntity()
    {
        return tableMetaData;
    }

    @Override
    public DBSEntityAttribute getAttribute(DBRProgressMonitor monitor)
        throws DBException
    {
        if (tableColumn != null) {
            return tableColumn;
        }
        if (tableMetaData == null) {
            return null;
        }
        tableColumn = tableMetaData.getEntity(monitor).getAttribute(monitor, name);
        return tableColumn;
    }

    @Override
    public boolean isReference(DBRProgressMonitor monitor)
        throws DBException
    {
        DBSEntityAttribute tableColumn = getAttribute(monitor);
        if (tableColumn == null) {
            return false;
        }
        DBSTable table = tableMetaData.getEntity(monitor);
        if (table == null) {
            return false;
        }
        Collection<? extends DBSEntityAssociation> foreignKeys = table.getAssociations(monitor);
        if (foreignKeys != null) {
            for (DBSEntityAssociation fk : foreignKeys) {
                if (fk instanceof DBSEntityReferrer && DBUtils.getConstraintColumn(monitor, (DBSEntityReferrer)fk, tableColumn) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<DBSEntityReferrer> getReferrers(DBRProgressMonitor monitor)
        throws DBException
    {
        List<DBSEntityReferrer> refs = new ArrayList<DBSEntityReferrer>();
        DBSEntityAttribute tableColumn = getAttribute(monitor);
        if (tableColumn == null) {
            return refs;
        }
        DBSEntity table = tableMetaData.getEntity(monitor);
        if (table == null) {
            return refs;
        }
        Collection<? extends DBSEntityAssociation> foreignKeys = table.getAssociations(monitor);
        if (foreignKeys != null) {
            for (DBSEntityAssociation fk : foreignKeys) {
                if (fk instanceof DBSEntityReferrer && DBUtils.getConstraintColumn(monitor, (DBSEntityReferrer) fk, tableColumn) != null) {
                    refs.add((DBSEntityReferrer)fk);
                }
            }
        }
        return refs;
    }

    @Override
    public Image getObjectImage()
    {
        if (tableColumn instanceof IObjectImageProvider) {
            return ((IObjectImageProvider) tableColumn).getObjectImage();
        }
        return DBUtils.getDataIcon(this).getImage();
    }

    @Override
    public String toString()
    {
        StringBuilder db = new StringBuilder();
        if (!CommonUtils.isEmpty(catalogName)) {
            db.append(catalogName).append('.');
        }
        if (!CommonUtils.isEmpty(schemaName)) {
            db.append(schemaName).append('.');
        }
        if (!CommonUtils.isEmpty(tableName)) {
            db.append(tableName).append('.');
        }
        if (!CommonUtils.isEmpty(name)) {
            db.append(name);
        }
        if (!CommonUtils.isEmpty(label)) {
            db.append(" as ").append(label);
        }
        return db.toString();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof JDBCColumnMetaData)) {
            return false;
        }
        JDBCColumnMetaData col = (JDBCColumnMetaData)obj;
        return
            index == col.index &&
            notNull == col.notNull &&
            displaySize == col.displaySize &&
            CommonUtils.equalObjects(label, col.label) &&
            CommonUtils.equalObjects(name, col.name) &&
            precision == col.precision &&
            scale == col.scale &&
            CommonUtils.equalObjects(catalogName, col.catalogName) &&
            CommonUtils.equalObjects(schemaName, col.schemaName) &&
            CommonUtils.equalObjects(tableName, col.tableName) &&
            typeID == col.typeID &&
            CommonUtils.equalObjects(typeName, col.typeName) &&
            readOnly == col.readOnly &&
            writable == col.writable;
    }

}