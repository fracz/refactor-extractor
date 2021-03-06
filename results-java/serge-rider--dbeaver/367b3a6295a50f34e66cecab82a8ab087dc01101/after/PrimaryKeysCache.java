/*
 * Copyright (C) 2010-2013 Serge Rieder
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
package org.jkiss.dbeaver.ext.generic.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.GenericConstants;
import org.jkiss.dbeaver.ext.generic.model.meta.GenericMetaObject;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCCompositeCache;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Index cache implementation
 */
class PrimaryKeysCache extends JDBCCompositeCache<GenericStructContainer, GenericTable, GenericPrimaryKey, GenericTableConstraintColumn> {

    private final GenericMetaObject pkObject;

    PrimaryKeysCache(TableCache tableCache)
    {
        super(
            tableCache,
            GenericTable.class,
            GenericUtils.getColumn(tableCache.getDataSource(), GenericConstants.OBJECT_PRIMARY_KEY, JDBCConstants.TABLE_NAME),
            GenericUtils.getColumn(tableCache.getDataSource(), GenericConstants.OBJECT_PRIMARY_KEY, JDBCConstants.PK_NAME));
        pkObject = tableCache.getDataSource().getMetaObject(GenericConstants.OBJECT_PRIMARY_KEY);
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCSession session, GenericStructContainer owner, GenericTable forParent)
        throws SQLException
    {
        try {
            return session.getMetaData().getPrimaryKeys(
                    owner.getCatalog() == null ? null : owner.getCatalog().getName(),
                    owner.getSchema() == null ? null : owner.getSchema().getName(),
                    forParent == null ? owner.getDataSource().getAllObjectsPattern() : forParent.getName())
                .getSource();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            if (forParent == null) {
                throw new SQLException("Global primary keys read not supported", e);
            } else {
                throw new SQLException(e);
            }
        }
    }

    @Override
    protected GenericPrimaryKey fetchObject(JDBCSession session, GenericStructContainer owner, GenericTable parent, String pkName, ResultSet dbResult)
        throws SQLException, DBException
    {
        return new GenericPrimaryKey(
            parent,
            pkName,
            null,
            DBSEntityConstraintType.PRIMARY_KEY,
            true);
    }

    @Override
    protected GenericTableConstraintColumn fetchObjectRow(
        JDBCSession session,
        GenericTable parent, GenericPrimaryKey object, ResultSet dbResult)
        throws SQLException, DBException
    {
        String columnName = GenericUtils.safeGetStringTrimmed(pkObject, dbResult, JDBCConstants.COLUMN_NAME);
        if (CommonUtils.isEmpty(columnName)) {
            return null;
        }
        int keySeq = GenericUtils.safeGetInt(pkObject, dbResult, JDBCConstants.KEY_SEQ);

        GenericTableColumn tableColumn = parent.getAttribute(session.getProgressMonitor(), columnName);
        if (tableColumn == null) {
            log.warn("Column '" + columnName + "' not found in table '" + parent.getFullQualifiedName() + "' for PK '" + object.getFullQualifiedName() + "'");
            return null;
        }

        return new GenericTableConstraintColumn(object, tableColumn, keySeq);
    }

    @Override
    protected void cacheChildren(GenericPrimaryKey primaryKey, List<GenericTableConstraintColumn> rows)
    {
        primaryKey.setColumns(rows);
    }
}