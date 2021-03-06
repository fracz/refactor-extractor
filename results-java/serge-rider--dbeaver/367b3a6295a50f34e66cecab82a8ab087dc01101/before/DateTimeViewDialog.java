/*
 * Copyright (C) 2010-2013 Serge Rieder
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

package org.jkiss.dbeaver.ui.dialogs.data;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCDateTimeValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * DateTimeViewDialog
 */
public class DateTimeViewDialog extends ValueViewDialog {

    private DateTime dateEditor;
    private DateTime timeEditor;

    public DateTimeViewDialog(DBDValueController valueController) {
        super(valueController);
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Object value = getValueController().getValue();


        DBSTypedObject valueType = getValueController().getValueType();
        boolean isDate = valueType.getTypeID() == java.sql.Types.DATE;
        boolean isTime = valueType.getTypeID() == java.sql.Types.TIME;
        boolean isTimeStamp = valueType.getTypeID() == java.sql.Types.TIMESTAMP;

        Composite dialogGroup = (Composite)super.createDialogArea(parent);
        Composite panel = UIUtils.createPlaceholder(dialogGroup, isTimeStamp ? 2 : 3);
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        int style = SWT.BORDER;
        if (getValueController().isReadOnly()) {
            style |= SWT.READ_ONLY;
        }

        if (isDate || isTimeStamp) {
            UIUtils.createControlLabel(panel, "Date").setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            dateEditor = new DateTime(panel, SWT.CALENDAR | style);
        }
        if (isTime || isTimeStamp) {
            UIUtils.createControlLabel(panel, "Time").setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            timeEditor = new DateTime(panel, SWT.TIME | SWT.LONG | style);
        }

        if (dateEditor != null) {
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalAlignment = GridData.CENTER;
            dateEditor.setLayoutData(gd);
        }
        if (timeEditor != null) {
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalAlignment = GridData.CENTER;
            timeEditor.setLayoutData(gd);
        }
        primeEditorValue(value);

        Button button = UIUtils.createPushButton(panel, "Set Current", null);
        if (isTimeStamp) {
            GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
            gd.horizontalSpan = 2;
            button.setLayoutData(gd);
        }
        button.setEnabled(!getValueController().isReadOnly());
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                primeEditorValue(new Date());
            }
        });

        return dialogGroup;
    }

    @Override
    public void primeEditorValue(Object value)
    {
        if (value instanceof Date) {
            Calendar cl = Calendar.getInstance();
            cl.setTime((Date)value);
            if (dateEditor != null) {
                dateEditor.setDate(cl.get(Calendar.YEAR), cl.get(Calendar.MONTH), cl.get(Calendar.DAY_OF_MONTH));
            }
            if (timeEditor != null) {
                timeEditor.setTime(cl.get(Calendar.HOUR_OF_DAY), cl.get(Calendar.MINUTE), cl.get(Calendar.SECOND));
            }
        }
    }

    @Override
    public Object extractEditorValue()
    {
        return JDBCDateTimeValueHandler.getDate(dateEditor, timeEditor);
    }

    @Override
    public Control getControl()
    {
        return null;
    }

}