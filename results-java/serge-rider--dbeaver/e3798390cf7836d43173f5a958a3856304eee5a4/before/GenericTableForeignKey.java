/*
 * Copyright (C) 2010-2014 Serge Rieder
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

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableForeignKey;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyDefferability;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * GenericTableForeignKey
 */
public class GenericTableForeignKey extends JDBCTableForeignKey<GenericTable, GenericPrimaryKey>
{
    private DBSForeignKeyDefferability defferability;
    private List<GenericTableForeignKeyColumnTable> columns;

    public GenericTableForeignKey(
        GenericTable table,
        String name,
        @Nullable String remarks,
        GenericPrimaryKey referencedKey,
        DBSForeignKeyModifyRule deleteRule,
        DBSForeignKeyModifyRule updateRule,
        DBSForeignKeyDefferability defferability,
        boolean persisted)
    {
        super(table, name, remarks, referencedKey, deleteRule, updateRule, persisted);
        this.defferability = defferability;
    }

    @NotNull
    @Override
    public GenericDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Property(viewable = true, order = 7)
    public DBSForeignKeyDefferability getDefferability()
    {
        return defferability;
    }

    @Override
    public Collection<GenericTableForeignKeyColumnTable> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }

    public void addColumn(GenericTableForeignKeyColumnTable column)
    {
        if (columns == null) {
            columns = new ArrayList<GenericTableForeignKeyColumnTable>();
        }
        this.columns.add(column);
    }

    void setColumns(List<GenericTableForeignKeyColumnTable> columns)
    {
        this.columns = columns;
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getCatalog(),
            getTable().getSchema(),
            getTable(),
            this);
    }
}