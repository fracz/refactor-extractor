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

package org.jkiss.dbeaver.ui.actions.datasource;

import org.jkiss.dbeaver.core.Log;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.jkiss.dbeaver.ui.actions.common.EmptyListAction;

import java.util.ArrayList;
import java.util.List;

public abstract class DataSourceMenuContributor extends CompoundContributionItem
{
    static final Log log = Log.getLog(DataSourceMenuContributor.class);

    @Override
    protected IContributionItem[] getContributionItems()
    {
        List<IContributionItem> menuItems = new ArrayList<IContributionItem>();
        fillContributionItems(menuItems);
        return menuItems.isEmpty() ? makeEmptyList() : menuItems.toArray(new IContributionItem[menuItems.size()]);
    }

    protected abstract void fillContributionItems(
        List<IContributionItem> menuItems);

    private static IContributionItem[] makeEmptyList(){
        return new IContributionItem[] { new ActionContributionItem(new EmptyListAction())};
    }

}