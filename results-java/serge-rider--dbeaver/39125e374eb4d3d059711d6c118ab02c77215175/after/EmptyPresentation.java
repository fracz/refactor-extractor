/*
 * Copyright (C) 2010-2015 Serge Rieder
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

package org.jkiss.dbeaver.ui.controls.resultset.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetPresentation;

/**
 * Empty presentation.
 * Used when RSV has no results (initially).
 */
public class EmptyPresentation implements IResultSetPresentation {

    private Composite placeholder;

    @Override
    public void createPresentation(IResultSetController controller, Composite parent) {
        placeholder = new Composite(parent, SWT.NONE);
    }

    @Override
    public Control getControl() {
        return placeholder;
    }

    @Override
    public void refreshData(boolean refreshMetadata) {

    }

    @Override
    public void updateValueView() {

    }

}