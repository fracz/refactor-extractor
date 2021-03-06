/*
 * Copyright (C) 2010-2015 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ext.mssql.edit;

import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.ext.mssql.model.*;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.SQLTableManager;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;

/**
 * MSSQL table manager
 */
public class MSSQLTableManager extends SQLTableManager<MSSQLTableBase, MSSQLCatalog> implements DBEObjectRenamer<MSSQLTableBase> {

    private static final Class<?>[] CHILD_TYPES = {
        MSSQLTableColumn.class,
        MSSQLTableConstraint.class,
        MSSQLTableForeignKey.class,
        MSSQLTableIndex.class
    };

    @Nullable
    @Override
    public DBSObjectCache<MSSQLCatalog, MSSQLTableBase> getObjectsCache(MSSQLTableBase object)
    {
        return object.getContainer().getTableCache();
    }

    @Override
    protected MSSQLTable createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, MSSQLCatalog parent, Object copyFrom)
    {
        final MSSQLTable table = new MSSQLTable(parent);
        try {
            setTableName(parent, table);
            final MSSQLTable.AdditionalInfo additionalInfo = table.getAdditionalInfo(VoidProgressMonitor.INSTANCE);
            additionalInfo.setEngine(parent.getDataSource().getDefaultEngine());
            additionalInfo.setCharset(parent.getDefaultCharset());
            additionalInfo.setCollation(parent.getDefaultCollation());
        } catch (DBException e) {
            // Never be here
            log.error(e);
        }

        return table;
    }

    @Override
    protected DBEPersistAction[] makeObjectModifyActions(ObjectChangeCommand command)
    {
        StringBuilder query = new StringBuilder("ALTER TABLE "); //$NON-NLS-1$
        query.append(command.getObject().getFullQualifiedName()).append(" "); //$NON-NLS-1$
        appendTableModifiers(command.getObject(), command, query);

        return new DBEPersistAction[] {
            new SQLDatabasePersistAction(query.toString())
        };
    }

    @Override
    protected void appendTableModifiers(MSSQLTableBase tableBase, NestedObjectCommand tableProps, StringBuilder ddl)
    {
        if (tableBase instanceof MSSQLTable) {
            MSSQLTable table =(MSSQLTable)tableBase;
            try {
                final MSSQLTable.AdditionalInfo additionalInfo = table.getAdditionalInfo(VoidProgressMonitor.INSTANCE);
                if ((!table.isPersisted() || tableProps.getProperty("engine") != null) && additionalInfo.getEngine() != null) { //$NON-NLS-1$
                    ddl.append("\nENGINE=").append(additionalInfo.getEngine().getName()); //$NON-NLS-1$
                }
                if ((!table.isPersisted() || tableProps.getProperty("charset") != null) && additionalInfo.getCharset() != null) { //$NON-NLS-1$
                    ddl.append("\nDEFAULT CHARSET=").append(additionalInfo.getCharset().getName()); //$NON-NLS-1$
                }
                if ((!table.isPersisted() || tableProps.getProperty("collation") != null) && additionalInfo.getCollation() != null) { //$NON-NLS-1$
                    ddl.append("\nCOLLATE=").append(additionalInfo.getCollation().getName()); //$NON-NLS-1$
                }
                if ((!table.isPersisted() || tableProps.getProperty(DBConstants.PROP_ID_DESCRIPTION) != null) && table.getDescription() != null) {
                    ddl.append("\nCOMMENT='").append(table.getDescription().replace('\'', '"')).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if ((!table.isPersisted() || tableProps.getProperty("autoIncrement") != null) && additionalInfo.getAutoIncrement() > 0) { //$NON-NLS-1$
                    ddl.append("\nAUTO_INCREMENT=").append(additionalInfo.getAutoIncrement()); //$NON-NLS-1$
                }
            } catch (DBCException e) {
                log.error(e);
            }
        }
    }

    @Override
    protected DBEPersistAction[] makeObjectRenameActions(ObjectRenameCommand command)
    {
        return new DBEPersistAction[] {
            new SQLDatabasePersistAction(
                "Rename table",
                "RENAME TABLE " + command.getObject().getFullQualifiedName() + //$NON-NLS-1$
                    " TO " + DBUtils.getQuotedIdentifier(command.getObject().getDataSource(), command.getNewName())) //$NON-NLS-1$
        };
    }

    @Override
    public Class<?>[] getChildTypes()
    {
        return CHILD_TYPES;
    }

    @Override
    public void renameObject(DBECommandContext commandContext, MSSQLTableBase object, String newName) throws DBException
    {
        processObjectRename(commandContext, object, newName);
    }

/*
    public ITabDescriptor[] getTabDescriptors(IWorkbenchWindow workbenchWindow, final IDatabaseEditor activeEditor, final MSSQLTable object)
    {
        if (object.getContainer().isSystem()) {
            return null;
        }
        return new ITabDescriptor[] {
            new PropertyTabDescriptor(
                PropertiesContributor.CATEGORY_INFO,
                "table.ddl", //$NON-NLS-1$
                "DDL", //$NON-NLS-1$
                DBIcon.SOURCES.getImage(),
                new SectionDescriptor("default", "DDL") { //$NON-NLS-1$ //$NON-NLS-2$
                    public ISection getSectionClass()
                    {
                        return new MSSQLDDLViewEditor(activeEditor);
                    }
                })
        };
    }
*/

}