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
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

import java.util.ArrayList;
import java.util.List;

/**
 * OracleTableConstraint
 */
public abstract class OracleTableConstraintBase extends JDBCTableConstraint<OracleTableBase> {

    static final Log log = Log.getLog(OracleTableConstraintBase.class);

    private OracleObjectStatus status;
    private List<OracleTableConstraintColumn> columns;

    public OracleTableConstraintBase(OracleTableBase oracleTable, String name, DBSEntityConstraintType constraintType, OracleObjectStatus status, boolean persisted)
    {
        super(oracleTable, name, null, constraintType, persisted);
        this.status = status;
    }

    protected OracleTableConstraintBase(OracleTableBase oracleTableBase, String name, String description, DBSEntityConstraintType constraintType, boolean persisted)
    {
        super(oracleTableBase, name, description, constraintType, persisted);
    }

    @NotNull
    @Override
    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @NotNull
    @Property(viewable = true, editable = false, valueTransformer = DBObjectNameCaseTransformer.class, order = 3)
    @Override
    public DBSEntityConstraintType getConstraintType()
    {
        return constraintType;
    }

    @Property(viewable = true, editable = false, order = 9)
    public OracleObjectStatus getStatus()
    {
        return status;
    }

    @Override
    public List<OracleTableConstraintColumn> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }

    public void addColumn(OracleTableConstraintColumn column)
    {
        if (columns == null) {
            columns = new ArrayList<OracleTableConstraintColumn>();
        }
        this.columns.add(column);
    }

    void setColumns(List<OracleTableConstraintColumn> columns)
    {
        this.columns = columns;
    }

}