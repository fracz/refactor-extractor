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
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataKind;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;

import java.sql.ResultSet;

/**
 * Oracle data type attribute
 */
public class OracleDataTypeAttribute extends OracleDataTypeMember implements DBSEntityAttribute
{

    private OracleDataType attrType;
    private OracleDataTypeModifier attrTypeMod;
    private Integer length;
    private Integer precision;
    private Integer scale;

    public OracleDataTypeAttribute(DBRProgressMonitor monitor, OracleDataType dataType, ResultSet dbResult)
    {
        super(dataType, dbResult);
        this.name = JDBCUtils.safeGetString(dbResult, "ATTR_NAME");
        this.number = JDBCUtils.safeGetInt(dbResult, "ATTR_NO");
        this.attrType = OracleDataType.resolveDataType(
            monitor,
            getDataSource(),
            JDBCUtils.safeGetString(dbResult, "ATTR_TYPE_OWNER"),
            JDBCUtils.safeGetString(dbResult, "ATTR_TYPE_NAME"));
        this.attrTypeMod = OracleDataTypeModifier.resolveTypeModifier(JDBCUtils.safeGetString(dbResult, "ATTR_TYPE_MOD"));
        this.length = JDBCUtils.safeGetInteger(dbResult, "LENGTH");
        this.precision = JDBCUtils.safeGetInteger(dbResult, "PRECISION");
        this.scale = JDBCUtils.safeGetInteger(dbResult, "SCALE");
    }

    @Property(viewable = true, editable = true, order = 3)
    public DBSDataType getAttrType()
    {
        return attrType;
    }

    @Property(viewable = true, editable = true, order = 4)
    public OracleDataTypeModifier getAttrTypeMod()
    {
        return attrTypeMod;
    }

    @Property(viewable = true, editable = true, order = 5)
    public Integer getLength()
    {
        return length;
    }

    @Override
    @Property(viewable = true, editable = true, order = 6)
    public int getPrecision()
    {
        return precision == null ? 0 : precision;
    }

    @Override
    public long getMaxLength()
    {
        return length;
    }

    @Override
    @Property(viewable = true, editable = true, order = 7)
    public int getScale()
    {
        return scale == null ? 0 : scale;
    }

    @Override
    public int getTypeID()
    {
        return attrType.getTypeID();
    }

    @Override
    public DBSDataKind getDataKind()
    {
        return attrType.getDataKind();
    }

    @Override
    public String getTypeName()
    {
        return attrType.getFullQualifiedName();
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public boolean isSequence()
    {
        return false;
    }

    @Override
    public int getOrdinalPosition()
    {
        return number;
    }

    @Override
    public String getDefaultValue()
    {
        return null;
    }
}