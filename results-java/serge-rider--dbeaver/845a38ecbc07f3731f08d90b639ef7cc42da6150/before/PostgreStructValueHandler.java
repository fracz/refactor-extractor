/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ext.postgresql.model.data;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.postgresql.PostgreConstants;
import org.jkiss.dbeaver.ext.postgresql.PostgreUtils;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataType;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataTypeAttribute;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCStructImpl;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCStructStatic;
import org.jkiss.dbeaver.model.impl.jdbc.data.handlers.JDBCArrayValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.sql.Struct;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * PostgreArrayValueHandler
 */
public class PostgreStructValueHandler extends JDBCArrayValueHandler {
    static final Log log = Log.getLog(PostgreStructValueHandler.class);
    public static final PostgreStructValueHandler INSTANCE = new PostgreStructValueHandler();

    @Override
    public Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, Object object, boolean copy) throws DBCException
    {
        PostgreDataType structType = PostgreUtils.findDataType((PostgreDataSource)session.getDataSource(), type);
        try {
            Object value;
            if (object != null && object.getClass().getName().equals(PostgreConstants.PG_OBJECT_CLASS)) {
                value = PostgreUtils.extractValue(object);
            } else {
                value = "";
            }
            return convertStringToStruct(session, structType, (String) value);
        } catch (DBException e) {
            throw new DBCException("Error converting string to composite type", e, session.getDataSource());
        }
    }

    private JDBCStructStatic convertStringToStruct(@NotNull DBCSession session, @NotNull PostgreDataType compType, @NotNull String value) throws DBException {
        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1);
        }
        final Collection<PostgreDataTypeAttribute> attributes = compType.getAttributes(session.getProgressMonitor());
        if (attributes == null) {
            throw new DBException("Composite type '" + compType.getTypeName() + "' has no attributes");
        }
        String[] parsedValues = PostgreUtils.parseObjectString(value);
        if (parsedValues.length != attributes.size()) {
            log.debug("Number o attributes (" + attributes.size() + ") doesn't match actual number of parsed strings (" + parsedValues.length + ")");
        }
        Object[] attrValues = new Object[attributes.size()];

        Iterator<PostgreDataTypeAttribute> attrIter = attributes.iterator();
        for (int i = 0; i < parsedValues.length && attrIter.hasNext(); i++) {
            final PostgreDataTypeAttribute itemAttr = attrIter.next();
            attrValues[i] = PostgreUtils.convertStringToValue(itemAttr, parsedValues[i], true);
        }

        Struct contents = new JDBCStructImpl(compType.getTypeName(), attrValues);
        return new JDBCStructStatic(session, compType, contents);
    }

}