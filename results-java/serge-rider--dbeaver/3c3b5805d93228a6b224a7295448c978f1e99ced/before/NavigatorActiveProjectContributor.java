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
package org.jkiss.dbeaver.ui.actions.navigator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jkiss.dbeaver.core.DBeaverCore;

public class NavigatorActiveProjectContributor extends ContributionItem
{
    static final Log log = LogFactory.getLog(NavigatorActiveProjectContributor.class);

    @Override
    public void fill(Menu menu, int index)
    {
        createMenu(menu);
    }

    private void createMenu(final Menu menu)
    {
        final IProject activeProject = DBeaverCore.getInstance().getProjectRegistry().getActiveProject();
        for (final IProject project : DBeaverCore.getInstance().getLiveProjects()) {
            MenuItem txnItem = new MenuItem(menu, SWT.RADIO);
            txnItem.setText(project.getName());
            txnItem.setSelection(project == activeProject);
            txnItem.setData(project);
            txnItem.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    DBeaverCore.getInstance().getProjectRegistry().setActiveProject(project);
                }
            });
        }
    }

}