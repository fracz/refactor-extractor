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
package org.jkiss.dbeaver.ui.dialogs.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.runtime.jobs.DataSourceJob;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.navigator.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.controls.ColumnInfoPanel;
import org.jkiss.dbeaver.ui.dialogs.struct.EditDictionaryDialog;
import org.jkiss.dbeaver.ui.editors.data.DatabaseDataEditor;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * ValueViewDialog
 *
 * @author Serge Rider
 */
public abstract class ValueViewDialog extends Dialog implements DBDValueEditorDialog {

    static final Log log = LogFactory.getLog(ValueViewDialog.class);

    private static int dialogCount = 0;
    public static final String SETTINGS_SECTION_DI = "ValueViewDialog";

    private DBDValueController valueController;
    private DBSEntityReferrer refConstraint;
    private Text editor;
    private Table editorSelector;
    private boolean handleEditorChange;
    private SelectorLoaderJob loaderJob = null;
    private Object editedValue;
    private boolean columnInfoVisible = true;
    private ColumnInfoPanel columnPanel;
    private final IDialogSettings dialogSettings;
    private boolean opened;

    protected ValueViewDialog(DBDValueController valueController) {
        super(valueController.getValueSite().getShell());
        setShellStyle(SWT.SHELL_TRIM);
        this.valueController = valueController;
        dialogSettings = UIUtils.getDialogSettings(SETTINGS_SECTION_DI);
        if (dialogSettings.get(getInfoVisiblePrefId()) != null) {
            columnInfoVisible = dialogSettings.getBoolean(getInfoVisiblePrefId());
        }
        dialogCount++;
    }

    protected IDialogSettings getDialogSettings()
    {
        return dialogSettings;
    }

    protected boolean isForeignKey()
    {
        return getEnumerableConstraint() != null;
    }

    public DBDValueController getValueController() {
        return valueController;
    }

    @Override
    public void showValueEditor() {
        if (!opened) {
            open();
        } else {
            getShell().setFocus();
        }
    }

    @Override
    public void closeValueEditor() {
        if (this.valueController != null) {
            this.valueController.unregisterEditor(this);
            this.valueController = null;
        }
        this.setReturnCode(CANCEL);
        this.close();
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
/*
        SashForm sash = new SashForm(parent, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite dialogGroup = (Composite)super.createDialogArea(sash);
        dialogGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new ColumnInfoPanel(dialogGroup, SWT.BORDER, getValueController());
        Composite editorGroup = (Composite) super.createDialogArea(sash);
        editorGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        //editorGroup.setLayout(new GridLayout(1, false));
        return editorGroup;

*/
        Composite dialogGroup = (Composite)super.createDialogArea(parent);
        final Link columnHideLink = new Link(dialogGroup, SWT.NONE);
        columnHideLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                columnInfoVisible = !columnInfoVisible;
                dialogSettings.put(getInfoVisiblePrefId(), columnInfoVisible);
                initColumnInfoVisibility(columnHideLink);
                getShell().layout();
                int width = getShell().getSize().x;
                getShell().setSize(width, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            }
        });
        columnPanel = new ColumnInfoPanel(dialogGroup, SWT.BORDER, getValueController());
        columnPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        initColumnInfoVisibility(columnHideLink);

/*

        ExpandBar expandBar = new ExpandBar(dialogGroup, SWT.V_SCROLL);
        expandBar.setLayoutData(new GridData(GridData.FILL_BOTH));

        //expandBar.set
        ColumnInfoPanel columnPanel = new ColumnInfoPanel(expandBar, SWT.BORDER, getValueController());

        ExpandItem columnsItem = new ExpandItem(expandBar, SWT.NONE, 0);
        columnsItem.setText("Column information");
        columnsItem.setControl(columnPanel);
        columnsItem.setHeight(columnPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        expandBar.setSpacing(8);
*/

        return dialogGroup;
    }

    private void initColumnInfoVisibility(Link columnHideLink)
    {
        columnPanel.setVisible(columnInfoVisible);
        ((GridData)columnPanel.getLayoutData()).exclude = !columnInfoVisible;
        columnHideLink.setText("Column Info: (<a>" + (columnInfoVisible ? "hide" : "show") + "</a>)");
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, CoreMessages.dialog_value_view_button_save, true).setEnabled(!valueController.isReadOnly());
        createButton(parent, IDialogConstants.IGNORE_ID, CoreMessages.dialog_value_view_button_sat_null, false).setEnabled(!valueController.isReadOnly() && !DBUtils.isNullValue(valueController.getValue()));
        createButton(parent, IDialogConstants.CANCEL_ID, CoreMessages.dialog_value_view_button_cancel, false);
    }

    @Override
    protected void initializeBounds()
    {
        super.initializeBounds();

        Shell shell = getShell();

        String sizeString = dialogSettings.get(getDialogSizePrefId());
        if (!CommonUtils.isEmpty(sizeString) && sizeString.contains(":")) {
            int divPos = sizeString.indexOf(':');
            shell.setSize(new Point(
                Integer.parseInt(sizeString.substring(0, divPos)),
                Integer.parseInt(sizeString.substring(divPos + 1))));
        }

        Monitor primary = shell.getMonitor();
        Rectangle bounds = primary.getBounds ();
        Rectangle rect = shell.getBounds ();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 3;
        x += dialogCount * 20;
        y += dialogCount * 20;
        shell.setLocation (x, y);
    }

    private String getInfoVisiblePrefId()
    {
        return getClass().getSimpleName() + "-" +
            CommonUtils.escapeIdentifier(getValueController().getAttributeMetaData().getTypeName()) +
            "-columnInfoVisible";
    }

    private String getDialogSizePrefId()
    {
        return getClass().getSimpleName() + "-" +
            CommonUtils.escapeIdentifier(getValueController().getAttributeMetaData().getTypeName()) +
            "-dialogSize";
    }

    @Override
    public final int open()
    {
        try {
            opened = true;
            int result = super.open();
            if (result == IDialogConstants.OK_ID) {
                getValueController().updateValue(editedValue);
            }
            return result;
        } finally {
            dialogCount--;
            if (this.valueController != null) {
                this.valueController.unregisterEditor(this);
                this.valueController = null;
            }
        }
    }

    @Override
    protected void okPressed()
    {
        try {
            editedValue = getEditorValue();

            super.okPressed();
        }
        catch (Exception e) {
            UIUtils.showErrorDialog(getShell(), CoreMessages.dialog_value_view_dialog_error_updating_title, CoreMessages.dialog_value_view_dialog_error_updating_message, e);
            super.cancelPressed();
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        Point size = getShell().getSize();
        String sizeString = size.x + ":" + size.y;
        dialogSettings.put(getDialogSizePrefId(), sizeString);

        if (buttonId == IDialogConstants.IGNORE_ID) {
            if (!valueController.isReadOnly()) {
                editedValue = valueController.getValue();
                if (editedValue instanceof DBDValue) {
                    editedValue = ((DBDValue)editedValue).makeNull();
                } else {
                    editedValue = null;
                }
            }
            super.okPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        DBSAttributeBase meta = valueController.getAttributeMetaData();
        shell.setText(meta.getName());
    }

    protected abstract Object getEditorValue();

    private DBSEntityReferrer getEnumerableConstraint()
    {
        if (valueController instanceof DBDAttributeController) {
            DBSEntityReferrer constraint = DBUtils.getUniqueForeignConstraint(((DBDAttributeController)valueController).getAttributeMetaData());
            if (constraint instanceof DBSEntityAssociation &&
                ((DBSEntityAssociation)constraint).getReferencedConstraint() instanceof DBSConstraintEnumerable &&
                ((DBSConstraintEnumerable)((DBSEntityAssociation)constraint).getReferencedConstraint()).supportsEnumeration())
            {
                return constraint;
            }
        }
        return null;
    }

    protected void createEditorSelector(Composite parent, Text control)
    {
        if (!(valueController instanceof DBDAttributeController) || valueController.isReadOnly()) {
            return;
        }
        refConstraint = getEnumerableConstraint();
        if (refConstraint == null) {
            return;
        }

        this.editor = control;

        if (refConstraint instanceof DBSEntityAssociation) {
            final DBSEntityAssociation association = (DBSEntityAssociation)refConstraint;
            final DBSEntity refTable = association.getReferencedConstraint().getParentObject();
            Composite labelGroup = UIUtils.createPlaceholder(parent, 2);
            labelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_HORIZONTAL));
            Link dictLabel = new Link(labelGroup, SWT.NONE);
            dictLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            dictLabel.setText(NLS.bind(CoreMessages.dialog_value_view_label_dictionary, refTable.getName()));
            dictLabel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // Open
                    final IWorkbenchWindow window = valueController.getValueSite().getWorkbenchWindow();
                    DBeaverUI.runInUI(window, new DBRRunnableWithProgress() {
                        @Override
                        public void run(DBRProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException
                        {
                            DBNDatabaseNode tableNode = DBeaverCore.getInstance().getNavigatorModel().getNodeByObject(
                                monitor,
                                refTable,
                                true);
                            if (tableNode != null) {
                                NavigatorHandlerObjectOpen.openEntityEditor(tableNode, DatabaseDataEditor.class.getName(), window);
                            }
                        }
                    });
                }
            });

            Link hintLabel = new Link(labelGroup, SWT.NONE);
            hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            hintLabel.setText("(<a>Define Description</a>)");
            hintLabel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    EditDictionaryDialog dialog = new EditDictionaryDialog(getShell(), "Dictionary structure", refTable);
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        loaderJob.schedule();
                    }
                }
            });
        }

        editorSelector = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        editorSelector.setLinesVisible(true);
        editorSelector.setHeaderVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        //gd.widthHint = 300;
        //gd.grabExcessVerticalSpace = true;
        //gd.grabExcessHorizontalSpace = true;
        editorSelector.setLayoutData(gd);

        UIUtils.createTableColumn(editorSelector, SWT.LEFT, CoreMessages.dialog_value_view_column_value);
        UIUtils.createTableColumn(editorSelector, SWT.LEFT, CoreMessages.dialog_value_view_column_description);
        UIUtils.packColumns(editorSelector);

        editorSelector.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                TableItem[] selection = editorSelector.getSelection();
                if (selection != null && selection.length > 0) {
                    handleEditorChange = false;
                    Object value = selection[0].getData();
                    if (value instanceof Number) {
                        editor.setText(value.toString());
                    } else {
                        editor.setText(selection[0].getText());
                    }
                    handleEditorChange = true;
                }
            }
        });

        editor.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (handleEditorChange) {
                    if (loaderJob.getState() == Job.RUNNING) {
                        // Cancel it and create new one
                        loaderJob.cancel();
                        loaderJob = new SelectorLoaderJob();
                    }
                    if (loaderJob.getState() == Job.WAITING) {
                        loaderJob.setPattern(getEditorValue());
                    } else {
                        loaderJob.setPattern(getEditorValue());
                        loaderJob.schedule(500);
                    }
                }
            }
        });
        handleEditorChange = true;

        loaderJob = new SelectorLoaderJob();
        loaderJob.schedule(500);
    }

    private class SelectorLoaderJob extends DataSourceJob {

        private Object pattern;

        private SelectorLoaderJob()
        {
            super(
                CoreMessages.dialog_value_view_job_selector_name + valueController.getAttributeMetaData().getName() + " possible values",
                DBIcon.SQL_EXECUTE.getImageDescriptor(),
                valueController.getDataSource());
            setUser(false);
        }

        void setPattern(Object pattern)
        {
            this.pattern = pattern;
        }

        @Override
        protected IStatus run(DBRProgressMonitor monitor)
        {
            final Map<Object, String> keyValues = new TreeMap<Object, String>();
            try {
                DBDAttributeController attributeController = (DBDAttributeController)valueController;
                final DBSEntityAttribute tableColumn = attributeController.getAttributeMetaData().getAttribute(monitor);
                final DBSEntityAttributeRef fkColumn = DBUtils.getConstraintColumn(monitor, refConstraint, tableColumn);
                if (fkColumn == null) {
                    return Status.OK_STATUS;
                }
                DBSEntityAssociation association;
                if (refConstraint instanceof DBSEntityAssociation) {
                    association = (DBSEntityAssociation)refConstraint;
                } else {
                    return Status.OK_STATUS;
                }
                final DBSEntityAttribute refColumn = DBUtils.getReferenceAttribute(monitor, association, tableColumn);
                if (refColumn == null) {
                    return Status.OK_STATUS;
                }
                java.util.List<DBDAttributeValue> precedingKeys = null;
                Collection<? extends DBSEntityAttributeRef> allColumns = refConstraint.getAttributeReferences(monitor);
                if (allColumns.size() > 1) {
                    if (allColumns.iterator().next() != fkColumn) {
                        // Our column is not a first on in foreign key.
                        // So, fill uo preceeding keys
                        precedingKeys = new ArrayList<DBDAttributeValue>();
                        for (DBSEntityAttributeRef precColumn : allColumns) {
                            if (precColumn == fkColumn) {
                                // Enough
                                break;
                            }
                            DBCAttributeMetaData precMeta = attributeController.getRow().getAttributeMetaData(
                                    attributeController.getAttributeMetaData().getEntity(), precColumn.getAttribute().getName());
                            if (precMeta != null) {
                                Object precValue = attributeController.getRow().getAttributeValue(precMeta);
                                precedingKeys.add(new DBDAttributeValue(precColumn.getAttribute(), precValue));
                            }
                        }
                    }
                }
                final DBCExecutionContext context = getDataSource().openContext(
                        monitor,
                        DBCExecutionPurpose.UTIL,
                        NLS.bind(CoreMessages.dialog_value_view_context_name, fkColumn.getAttribute().getName()));
                try {
                    final DBSEntityConstraint refConstraint = association.getReferencedConstraint();
                    DBSConstraintEnumerable enumConstraint = (DBSConstraintEnumerable) refConstraint;
                    Collection<DBDLabelValuePair> enumValues = enumConstraint.getKeyEnumeration(
                        context,
                        refColumn,
                        pattern,
                        precedingKeys,
                        100);
                    for (DBDLabelValuePair pair : enumValues) {
                        keyValues.put(pair.getValue(), pair.getLabel());
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    UIUtils.runInUI(getShell(), new Runnable() {
                        @Override
                        public void run()
                        {
                            DBDValueHandler colHandler = DBUtils.findValueHandler(context, fkColumn.getAttribute());

                            if (editorSelector != null && !editorSelector.isDisposed()) {
                                editorSelector.setRedraw(false);
                                try {
                                    editorSelector.removeAll();
                                    for (Map.Entry<Object, String> entry : keyValues.entrySet()) {
                                        TableItem discItem = new TableItem(editorSelector, SWT.NONE);
                                        discItem.setText(0, colHandler.getValueDisplayString(fkColumn.getAttribute(), entry.getKey()));
                                        discItem.setText(1, entry.getValue());
                                        discItem.setData(entry.getKey());
                                    }

                                    if (editor != null && !editor.isDisposed()) {
                                        Object curValue = getEditorValue();
                                        TableItem curItem = null;
                                        for (TableItem item : editorSelector.getItems()) {
                                            if (item.getData() == curValue || (item.getData() != null && curValue != null && item.getData().equals(curValue))) {
                                                curItem = item;
                                                break;
                                            }
                                        }
                                        if (curItem != null) {
                                            editorSelector.select(editorSelector.indexOf(curItem));
                                            editorSelector.showSelection();
                                        }
                                    }

                                    UIUtils.maxTableColumnsWidth(editorSelector);
                                }
                                finally {
                                    editorSelector.setRedraw(true);
                                }
                            }
                        }
                    });
                }
                finally {
                    context.close();
                }

            } catch (DBException e) {
                // error
                // just ignore
                log.warn(e);
            }
            return Status.OK_STATUS;
        }
    }
}