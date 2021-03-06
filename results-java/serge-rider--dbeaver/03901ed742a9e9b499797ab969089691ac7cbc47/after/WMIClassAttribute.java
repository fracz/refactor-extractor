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
package org.jkiss.dbeaver.ext.wmi.model;

import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.ui.IObjectImageProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSDataKind;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.utils.CommonUtils;
import org.jkiss.wmi.service.WMIConstants;
import org.jkiss.wmi.service.WMIException;
import org.jkiss.wmi.service.WMIObjectAttribute;

/**
 * Class property
 */
public class WMIClassAttribute extends WMIClassElement<WMIObjectAttribute> implements DBSEntityAttribute, IObjectImageProvider
{
    protected WMIClassAttribute(WMIClass wmiClass, WMIObjectAttribute attribute)
    {
        super(wmiClass, attribute);
    }

    @Override
    @Property(viewable = true, order = 10)
    public String getTypeName()
    {
        return element.getTypeName();
    }

    @Override
    public int getTypeID()
    {
        return element.getType();
    }

    @Override
    public DBSDataKind getDataKind()
    {
        return getDataKindById(element.getType());
    }

    @Override
    public int getScale()
    {
        return 0;
    }

    @Override
    public int getPrecision()
    {
        return 0;
    }

    @Override
    public long getMaxLength()
    {
        try {
            Object maxLengthQ = getQualifiedObject().getQualifier(WMIConstants.Q_MaxLen);
            if (maxLengthQ instanceof Number) {
                return ((Number) maxLengthQ).longValue();
            }
        } catch (WMIException e) {
            log.warn(e);
        }
        return 0;
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

    public boolean isKey() throws DBException
    {
        return getFlagQualifier(WMIConstants.Q_Key) || getFlagQualifier(WMIConstants.Q_CIM_Key);
    }

    @Override
    @Property(viewable = true, order = 20)
    public String getDefaultValue()
    {
        return CommonUtils.toString(element.getValue());
    }

    @Override
    public Image getObjectImage()
    {
        return getPropertyImage(element.getType());
    }

    public static Image getPropertyImage(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBIcon.TYPE_NUMBER.getImage();
            case WMIConstants.CIM_BOOLEAN:
                return DBIcon.TYPE_BOOLEAN.getImage();
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBIcon.TYPE_STRING.getImage();
            case WMIConstants.CIM_DATETIME:
                return DBIcon.TYPE_DATETIME.getImage();
            default:
                return DBIcon.TYPE_UNKNOWN.getImage();
        }
    }

    public static DBSDataKind getDataKindById(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBSDataKind.NUMERIC;
            case WMIConstants.CIM_BOOLEAN:
                return DBSDataKind.BOOLEAN;
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBSDataKind.STRING;
            case WMIConstants.CIM_DATETIME:
                return DBSDataKind.DATETIME;
            default:
                return DBSDataKind.OBJECT;
        }
    }

}