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
package org.jkiss.dbeaver.ui.actions.navigator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchPage;
import org.jkiss.dbeaver.ui.actions.DataSourceHandler;
import org.jkiss.dbeaver.ui.search.database.SearchDatabaseObjectsDialog;

@SuppressWarnings("restriction")
public class NavigatorHandlerObjectFind extends DataSourceHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        WorkbenchPage activePage = (WorkbenchPage)HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
        if (activePage == null) {
            return null;
        }

        final Shell activeShell = HandlerUtil.getActiveShell(event);
        SearchDatabaseObjectsDialog.open(
            activeShell,
            getDataSourceContainer(event, false));

        return null;
    }

}