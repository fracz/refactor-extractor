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
package org.jkiss.dbeaver.ext.generic.edit;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.generic.model.*;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.JDBCTableManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

/**
 * Generic table manager
 */
public class GenericTableManager extends JDBCTableManager<GenericTable, GenericStructContainer> {

    private static final Class<?>[] CHILD_TYPES = {
        GenericTableColumn.class,
        GenericPrimaryKey.class,
        GenericTableForeignKey.class,
        GenericTableIndex.class
    };

    @Override
    protected DBSObjectCache<? extends DBSObject, GenericTable> getObjectsCache(GenericTable object)
    {
        return object.getContainer().getTableCache();
    }

    @Override
    public Class<?>[] getChildTypes()
    {
        return CHILD_TYPES;
    }

    @Override
    protected GenericTable createDatabaseObject(IWorkbenchWindow workbenchWindow, IEditorPart activeEditor, DBECommandContext context, GenericStructContainer parent, Object copyFrom)
    {
        final GenericTable table = new GenericTable(parent);
        table.setName(DBObjectNameCaseTransformer.transformName(parent, "NewTable"));

        return table;
    }

    public IDatabasePersistAction[] getTableDDL(DBRProgressMonitor monitor, GenericTable table) throws DBException
    {
        GenericTableColumnManager tcm = new GenericTableColumnManager();
        GenericPrimaryKeyManager pkm = new GenericPrimaryKeyManager();
        GenericIndexManager im = new GenericIndexManager();

        StructCreateCommand command = makeCreateCommand(table);
        // Aggregate nested column, constraint and index commands
        for (GenericTableColumn column : CommonUtils.safeCollection(table.getAttributes(monitor))) {
            command.aggregateCommand(tcm.makeCreateCommand(column));
        }
        try {
            for (GenericPrimaryKey primaryKey : CommonUtils.safeCollection(table.getConstraints(monitor))) {
                command.aggregateCommand(pkm.makeCreateCommand(primaryKey));
            }
        } catch (DBException e) {
            // Ignore primary keys
            log.debug(e);
        }
        try {
            for (GenericTableIndex index : CommonUtils.safeCollection(table.getIndexes(monitor))) {
                command.aggregateCommand(im.makeCreateCommand(index));
            }
        } catch (DBException e) {
            // Ignore indexes
            log.debug(e);
        }
        return command.getPersistActions();
    }

}