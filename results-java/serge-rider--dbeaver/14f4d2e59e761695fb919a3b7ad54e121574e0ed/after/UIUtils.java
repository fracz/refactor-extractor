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
package org.jkiss.dbeaver.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.swt.IFocusService;
import org.jkiss.dbeaver.DBeaverConstants;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.runtime.RunnableWithResult;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.dialogs.StandardErrorDialog;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * UI Utils
 */
@SuppressWarnings("restriction")
public class UIUtils {

    static final Log log = LogFactory.getLog(UIUtils.class);

    public static final char PARAGRAPH_CHAR = (char)182;

    public static final VerifyListener INTEGER_VERIFY_LISTENER = new VerifyListener() {
        @Override
        public void verifyText(VerifyEvent e)
        {
            for (int i = 0; i < e.text.length(); i++) {
                char ch = e.text.charAt(i);
                if (!Character.isDigit(ch) && ch != '-' && ch != '+') {
                    e.doit = false;
                    return;
                }
            }
            e.doit = true;
        }
    };

    public static final VerifyListener NUMBER_VERIFY_LISTENER = new VerifyListener() {
        @Override
        public void verifyText(VerifyEvent e)
        {
            for (int i = 0; i < e.text.length(); i++) {
                char ch = e.text.charAt(i);
                if (!Character.isDigit(ch) && ch != '.' && ch != '-' && ch != 'e' && ch != 'E') {
                    e.doit = false;
                    return;
                }
            }
            e.doit = true;
        }
    };

    public static Object makeStringForUI(Object object)
    {
        if (object == null) {
            return ""; //$NON-NLS-1$
        }
        if (object instanceof Number) {
            return NumberFormat.getInstance().format(object);
        }
        Class<?> eClass = object.getClass();
        if (eClass.isArray()) {
            if (eClass == byte[].class)
                return Arrays.toString((byte[]) object);
            else if (eClass == short[].class)
                return Arrays.toString((short[]) object);
            else if (eClass == int[].class)
                return Arrays.toString((int[]) object);
            else if (eClass == long[].class)
                return Arrays.toString((long[]) object);
            else if (eClass == char[].class)
                return Arrays.toString((char[]) object);
            else if (eClass == float[].class)
                return Arrays.toString((float[]) object);
            else if (eClass == double[].class)
                return Arrays.toString((double[]) object);
            else if (eClass == boolean[].class)
                return Arrays.toString((boolean[]) object);
            else { // element is an array of object references
                return Arrays.deepToString((Object[])object);
            }
        }
        return object;
    }

    public static ToolItem createToolItem(ToolBar toolBar, String text, Image icon, final IAction action)
    {
        ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        item.setToolTipText(text);
        if (icon != null) {
            item.setImage(icon);
        }
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                action.run();
            }
        });
        return item;
    }

    public static TableColumn createTableColumn(Table table, int style, String text)
    {
        TableColumn column = new TableColumn(table, style);
        column.setText(text);
        return column;
    }

    public static void packColumns(Table table)
    {
        packColumns(table, false);
    }

    public static void packColumns(Table table, boolean fit)
    {
        table.setRedraw(false);
        try {
            int totalWidth = 0;
            final TableColumn[] columns = table.getColumns();
            for (TableColumn column : columns) {
                column.pack();
                totalWidth += column.getWidth();
            }
            final Rectangle clientArea = table.getClientArea();
            if (clientArea.width > 0 && totalWidth > clientArea.width) {
                int extraSpace = totalWidth - clientArea.width;
                for (TableColumn column : columns) {
                    int colWidth = column.getWidth();
                    if (colWidth > totalWidth / 3) {
                        // If some columns is too big (more than 33% of total width)
                        // Then shrink it to 30%
                        column.setWidth(totalWidth / 3);
                        totalWidth -= colWidth;
                        totalWidth += column.getWidth();
                    }
                }

                for (TableColumn tc : columns) {
                    double ratio = (double) tc.getWidth() / totalWidth;
                    int newWidth = (int) (tc.getWidth() - extraSpace * ratio);
                    tc.setWidth(newWidth);
                }
            } else if (fit && totalWidth < clientArea.width) {
                float extraSpace = (clientArea.width - totalWidth) / columns.length;
                for (TableColumn tc : columns) {
                    tc.setWidth((int) (tc.getWidth() + extraSpace));
                }
            }
        } finally {
            table.setRedraw(true);
        }
    }

    public static void packColumns(Tree tree)
    {
        packColumns(tree, false, null);
    }

    public static void packColumns(Tree tree, boolean fit, float[] ratios)
    {
        tree.setRedraw(false);
        try {
            // Check for disposed items
            // TODO: it looks like SWT error. Sometimes tree items are disposed and NPE is thrown from column.pack
            for (TreeItem item : tree.getItems()) {
                if (item.isDisposed()) {
                    return;
                }
            }
            int totalWidth = 0;
            final TreeColumn[] columns = tree.getColumns();
            for (TreeColumn column : columns) {
                column.pack();
                totalWidth += column.getWidth();
            }
            Rectangle clientArea = tree.getClientArea();
            if (clientArea.isEmpty()) {
                return;
            }
            if (fit) {
                if (totalWidth > clientArea.width) {
                    int extraSpace = totalWidth - clientArea.width;
                    for (TreeColumn tc : columns) {
                        double ratio = (double) tc.getWidth() / totalWidth;
                        tc.setWidth((int) (tc.getWidth() - extraSpace * ratio));
                    }
                } else if (totalWidth < clientArea.width) {
                    float extraSpace = clientArea.width - totalWidth;
                    if (columns.length > 0) {
                        if (ratios == null || ratios.length < columns.length) {
                            extraSpace /= columns.length;
                            extraSpace--;
                            for (TreeColumn tc : columns) {
                                tc.setWidth((int) (tc.getWidth() + extraSpace));
                            }
                        } else {
                            for (int i = 0; i < columns.length; i++) {
                                TreeColumn tc = columns[i];
                                tc.setWidth((int) (tc.getWidth() + extraSpace * ratios[i]));
                            }
                        }
                    }
                }
            }
        } finally {
            tree.setRedraw(true);
        }
    }

    public static void maxTableColumnsWidth(Table table)
    {
        table.setRedraw(false);
        try {
            int columnCount = table.getColumnCount();
            if (columnCount > 0) {
                int totalWidth = 0;
                final TableColumn[] columns = table.getColumns();
                for (TableColumn tc : columns) {
                    tc.pack();
                    totalWidth += tc.getWidth();
                }
                final Rectangle clientArea = table.getClientArea();
                if (totalWidth < clientArea.width) {
                    int extraSpace = clientArea.width - totalWidth;
                    extraSpace /= columnCount;
                    for (TableColumn tc : columns) {
                        tc.setWidth(tc.getWidth() + extraSpace);
                    }
                }
            }
        } finally {
            table.setRedraw(true);
        }
    }

    public static int getColumnAtPos(TableItem item, int x, int y)
    {
        int columnCount = item.getParent().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Rectangle rect = item.getBounds(i);
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public static int getColumnAtPos(TreeItem item, int x, int y)
    {
        int columnCount = item.getParent().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Rectangle rect = item.getBounds(i);
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public static void sortTable(Table table, Comparator<TableItem> comparator)
    {
        int columnCount = table.getColumnCount();
        TableItem[] items = table.getItems();
        for (int i = 1; i < items.length; i++) {
            for (int j = 0; j < i; j++) {
                TableItem item = items[i];
                if (comparator.compare(item, items[j]) < 0) {
                    String[] values = new String[columnCount];
                    for (int k = 0; k < columnCount; k++) {
                        values[k] = item.getText(k);
                    }
                    Object data = item.getData();
                    boolean checked = item.getChecked();
                    item.dispose();

                    item = new TableItem(table, SWT.NONE, j);
                    item.setText(values);
                    item.setData(data);
                    item.setChecked(checked);
                    items = table.getItems();
                    break;
                }
            }
        }
    }

    public static void dispose(Widget widget)
    {
        if (widget != null && !widget.isDisposed()) {
            try {
                widget.dispose();
            } catch (Exception e) {
                log.debug("widget dispose error", e);
            }
        }
    }

    public static void dispose(Resource resource)
    {
        if (resource != null && !resource.isDisposed()) {
            try {
                resource.dispose();
            } catch (Exception e) {
                log.debug("Resource dispose error", e);
            }
        }
    }

    public static void showMessageBox(final Shell shell, final String title, final String info, final int messageType)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {
                MessageBox messageBox = new MessageBox(shell, messageType | SWT.OK);
                messageBox.setMessage(info);
                messageBox.setText(title);
                messageBox.open();
            }
        };
        runInUI(shell, runnable);
    }

    public static boolean confirmAction(final Shell shell, final String title, final String question)
    {
        RunnableWithResult<Boolean> confirmer = new RunnableWithResult<Boolean>() {
            @Override
            public void run()
            {
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
                messageBox.setMessage(question);
                messageBox.setText(title);
                int response = messageBox.open();
                result = (response == SWT.YES);
            }
        };
        runInUI(shell, confirmer);
        return confirmer.getResult();
    }

    public static Font makeBoldFont(Font normalFont)
    {
        return modifyFont(normalFont, SWT.BOLD);
    }

    public static Font modifyFont(Font normalFont, int style)
    {
        FontData[] fontData = normalFont.getFontData();
        fontData[0].setStyle(fontData[0].getStyle() | style);
        return new Font(normalFont.getDevice(), fontData[0]);
    }

    public static Group createControlGroup(Composite parent, String label, int columns, int layoutStyle, int widthHint)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setText(label);

        GridData gd = new GridData(layoutStyle);
        if (widthHint > 0) {
            gd.widthHint = widthHint;
        }
        group.setLayoutData(gd);

        GridLayout gl = new GridLayout(columns, false);
        group.setLayout(gl);

        return group;
    }

    public static Label createControlLabel(Composite parent, String label)
    {
        Label textLabel = new Label(parent, SWT.NONE);
        textLabel.setText(label + ": "); //$NON-NLS-1$

        return textLabel;
    }

    public static Label createTextLabel(Composite parent, String label)
    {
        Label textLabel = new Label(parent, SWT.NONE);
        textLabel.setText(label);

        return textLabel;
    }

    public static Label createImageLabel(Composite parent, Image image)
    {
        Label imageLabel = new Label(parent, SWT.NONE);
        imageLabel.setImage(image);

        return imageLabel;
    }

    public static Text createLabelText(Composite parent, String label, String value)
    {
        return createLabelText(parent, label, value, SWT.BORDER);
    }

    public static Text createLabelText(Composite parent, String label, String value, int style)
    {
        return createLabelText(parent, label, value, style, new GridData(GridData.FILL_HORIZONTAL));
    }

    public static Text createLabelText(Composite parent, String label, String value, int style, Object layoutData)
    {
        createControlLabel(parent, label);

        Text text = new Text(parent, style);
        if (value != null) {
            text.setText(value);
        }

        if (layoutData != null) {
            text.setLayoutData(layoutData);
        }

        return text;
    }

    public static Spinner createLabelSpinner(Composite parent, String label, int value, int minimum, int maximum)
    {
        createControlLabel(parent, label);

        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setMinimum(minimum);
        spinner.setMaximum(maximum);
        spinner.setSelection(value);

        return spinner;
    }

    public static Button createLabelCheckbox(Composite parent, String label, boolean checked)
    {
        return createLabelCheckbox(parent, label, null, checked, SWT.NONE);
    }

    public static Button createLabelCheckbox(Composite parent, String label, String tooltip, boolean checked)
    {
        return createLabelCheckbox(parent, label, tooltip, checked, SWT.NONE);
    }

    public static Button createLabelCheckbox(Composite parent, String label, String tooltip, boolean checked, int style)
    {
        Label labelControl = createControlLabel(parent, label);
        //labelControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Button button = new Button(parent, SWT.CHECK | style);
        if (checked) {
            button.setSelection(true);
        }
        labelControl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e)
            {
                if (!button.isDisposed() && button.isVisible() && button.isEnabled()) {
                    button.setSelection(!button.getSelection());
                }
            }
        });

        if (tooltip != null) {
            labelControl.setToolTipText(tooltip);
            button.setToolTipText(tooltip);
        }
        return button;
    }

    public static Button createCheckbox(Composite parent, String label, boolean checked)
    {
        final Button button = new Button(parent, SWT.CHECK);
        button.setText(label);
        if (checked) {
            button.setSelection(true);
        }

        return button;
    }

    public static Combo createLabelCombo(Composite parent, String label, int style)
    {
        Label labelControl = createControlLabel(parent, label);
        //labelControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Combo combo = new Combo(parent, style);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return combo;
    }

    public static Button createToolButton(Composite parent, String text, SelectionListener selectionListener)
    {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (selectionListener != null) {
            button.addSelectionListener(selectionListener);
        }
        return button;
    }

    public static Shell getActiveShell()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        return workbench == null ? null : getShell(workbench.getActiveWorkbenchWindow());
    }

    public static Shell getShell(IShellProvider provider)
    {
        return provider == null ? null : provider.getShell();
    }

    public static Shell getShell(IWorkbenchPart part)
    {
        return part == null ? null : getShell(part.getSite());
    }

    public static Integer getTextInteger(Text text)
    {
        String str = text.getText();
        str = str.trim();
        if (str.length() == 0) {
            return null;
        }
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            log.debug(e);
            return null;
        }
    }

    public static IHandlerActivation registerKeyBinding(IServiceLocator serviceLocator, IAction action)
    {
        IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
        if (handlerService != null) {
            return handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
        } else {
            return null;
        }
    }

    public static Composite createPlaceholder(Composite parent, int columns)
    {
        return createPlaceholder(parent, columns, 0);
    }

    public static Composite createPlaceholder(Composite parent, int columns, int spacing)
    {
        Composite ph = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(columns, false);
        gl.verticalSpacing = spacing;
        gl.horizontalSpacing = spacing;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        ph.setLayout(gl);
        return ph;
    }

    public static Label createHorizontalLine(Composite parent)
    {
        Label horizontalLine = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
        return horizontalLine;
    }

    public static String getComboSelection(Combo combo)
    {
        int selectionIndex = combo.getSelectionIndex();
        if (selectionIndex < 0) {
            return null;
        }
        return combo.getItem(selectionIndex);
    }

    public static boolean setComboSelection(Combo combo, String value)
    {
        if (value == null) {
            return false;
        }
        int count = combo.getItemCount();
        for (int i = 0; i < count; i++) {
            if (value.equals(combo.getItem(i))) {
                combo.select(i);
                return true;
            }
        }
        return false;
    }

    public static Combo createEncodingCombo(Composite parent, String curCharset)
    {
        if (curCharset == null) {
            curCharset = ContentUtils.getDefaultFileEncoding();
        }
        Combo encodingCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        encodingCombo.setVisibleItemCount(30);
        SortedMap<String, Charset> charsetMap = Charset.availableCharsets();
        int index = 0;
        int defIndex = -1;
        for (String csName : charsetMap.keySet()) {
            Charset charset = charsetMap.get(csName);
            encodingCombo.add(charset.displayName());
            if (charset.displayName().equalsIgnoreCase(curCharset)) {
                defIndex = index;
            }
            if (defIndex < 0) {
                for (String alias : charset.aliases()) {
                    if (alias.equalsIgnoreCase(curCharset)) {
                        defIndex = index;
                    }
                }
            }
            index++;
        }
        if (defIndex >= 0) {
            encodingCombo.select(defIndex);
        } else {
            log.warn("Charset '" + curCharset + "' is not recognized"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return encodingCombo;
    }

    public static SashForm createPartDivider(final IWorkbenchPart workbenchPart, Composite parent, int style)
    {
        final SashForm sash = new SashForm(parent, style);
/*
        //sash.setSashWidth(10);
        final IWorkbenchWindow workbenchWindow = workbenchPart.getSite().getWorkbenchWindow();
        //sash.setBackground(sashActiveBackground);

        final IPartListener partListener = new IPartListener() {
            @Override
            public void partBroughtToTop(IWorkbenchPart part)
            {
            }

            @Override
            public void partOpened(IWorkbenchPart part)
            {
            }

            @Override
            public void partClosed(IWorkbenchPart part)
            {
            }

            @Override
            @SuppressWarnings("restriction")
            public void partActivated(IWorkbenchPart part)
            {
                if (part == workbenchPart) {
                    Color sashActiveBackground = workbenchWindow.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(
                        IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
                    if (sashActiveBackground == null) {
                        sashActiveBackground = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
                    }
                    sash.setBackground(sashActiveBackground);
                }
            }

            @Override
            public void partDeactivated(IWorkbenchPart part)
            {
                if (part == workbenchPart) {
                    sash.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                }
            }
        };

        final IPageListener pageListener = workbenchWindow.getActivePage() != null ? null : new IPageListener() {
            @Override
            public void pageActivated(IWorkbenchPage page)
            {
            }

            @Override
            public void pageOpened(IWorkbenchPage page)
            {
                page.addPartListener(partListener);
            }

            @Override
            public void pageClosed(IWorkbenchPage page)
            {
                page.removePartListener(partListener);
            }
        };
        if (pageListener != null) {
            // No active page yet, wait for it in listener
            workbenchWindow.addPageListener(pageListener);
        } else {
            // Add listener to active page
            workbenchWindow.getActivePage().addPartListener(partListener);
        }

        sash.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e)
            {
                if (pageListener != null) {
                    workbenchWindow.removePageListener(pageListener);
                }
                if (workbenchWindow.getActivePage() != null) {
                    workbenchWindow.getActivePage().removePartListener(partListener);
                }
            }
        });
*/

        return sash;
    }

    public static void showErrorDialog(
        Shell shell,
        String title,
        String message,
        Throwable error)
    {
        if (error != null) {
            log.error(error);
        }

        showErrorDialog(
            shell,
            title,
            error == null ? null : message,
            error == null ?
                new Status(IStatus.ERROR, DBeaverConstants.PLUGIN_ID, message) :
                RuntimeUtils.makeExceptionStatus(error));
    }

    public static void showErrorDialog(
        Shell shell,
        String title,
        String message)
    {
        showErrorDialog(shell, title, message, (Throwable) null);
    }

    public static void showErrorDialog(
        Shell shell,
        String title,
        String message,
        Collection<String> errorMessages)
    {
        //log.debug(message);
        java.util.List<Status> messageStatuses = new ArrayList<Status>(errorMessages.size());
        for (String error : errorMessages) {
            messageStatuses.add(new Status(
                Status.ERROR,
                DBeaverCore.getCorePluginID(),
                error));
        }
        MultiStatus status = new MultiStatus(
            DBeaverCore.getCorePluginID(),
            0,
            messageStatuses.toArray(new IStatus[messageStatuses.size()]),
            message,
            null);
        showErrorDialog(shell, title, message, status);
    }

    public static void showErrorDialog(
        final Shell shell,
        final String title,
        final String message,
        final IStatus status)
    {
        //log.debug(message);
        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {
                // Display the dialog
                StandardErrorDialog dialog = new StandardErrorDialog(
                    shell,
                    title,
                    message,
                    status,
                    IStatus.ERROR);
                dialog.open();
            }
        };
        runInUI(shell, runnable);
    }

    public static void runInUI(Shell shell, Runnable runnable)
    {
        final Display display = shell == null ? Display.getDefault() : shell.getDisplay();
        if (display.getThread() != Thread.currentThread()) {
            display.syncExec(runnable);
        } else {
            runnable.run();
        }
    }

    public static String formatMessage(String message, Object... args)
    {
        if (message == null) {
            return ""; //$NON-NLS-1$
        } else {
            return MessageFormat.format(message, args);
        }
    }

    public static Button createPushButton(Composite parent, String label, Image image)
    {
        Button button = new Button(parent, SWT.PUSH);
        if (label != null) {
            button.setText(label);
        }
        if (image != null) {
            button.setImage(image);
        }
        return button;
    }

    public static void setHelp(Control control, String pluginId, String helpContextID)
    {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
            control,
            pluginId + "." + helpContextID); //$NON-NLS-1$
    }

    public static void setHelp(Control control, String helpContextID)
    {
        setHelp(
            control,
            DBeaverConstants.PLUGIN_ID,
            helpContextID);
    }

    public static Text createOutputFolderChooser(final Composite parent, String label, ModifyListener changeListener)
    {
        UIUtils.createControlLabel(parent, label != null ? label : CoreMessages.dialog_export_wizard_output_label_directory);
        Composite chooserPlaceholder = UIUtils.createPlaceholder(parent, 2);
        chooserPlaceholder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Text directoryText = new Text(chooserPlaceholder, SWT.BORDER);
        directoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (changeListener != null) {
            directoryText.addModifyListener(changeListener);
        }

        final Runnable folderChooser = new Runnable() {
            @Override
            public void run()
            {
                DirectoryDialog dialog = new DirectoryDialog(parent.getShell(), SWT.NONE);
                dialog.setMessage(CoreMessages.dialog_export_wizard_output_dialog_directory_message);
                dialog.setText(CoreMessages.dialog_export_wizard_output_dialog_directory_text);
                String directory = directoryText.getText();
                if (!CommonUtils.isEmpty(directory)) {
                    dialog.setFilterPath(directory);
                }
                directory = dialog.open();
                if (directory != null) {
                    directoryText.setText(directory);
                }
            }
        };
        directoryText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e)
            {
                folderChooser.run();
            }
        });

        Button openFolder = new Button(chooserPlaceholder, SWT.PUSH | SWT.FLAT);
        openFolder.setImage(DBIcon.TREE_FOLDER.getImage());
        openFolder.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
        openFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                folderChooser.run();
            }
        });
        return directoryText;
    }


    public static String makeAnchor(String text)
    {
    	return "<a>" + text + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static <T> T findView(IWorkbenchWindow workbenchWindow, Class<T> viewClass)
    {
        IViewReference[] references = workbenchWindow.getActivePage().getViewReferences();
        for (IViewReference ref : references) {
            IViewPart view = ref.getView(false);
            if (view != null && viewClass.isAssignableFrom(view.getClass())) {
                return viewClass.cast(view);
            }
        }
        return null;
    }

    public static IViewPart findView(IWorkbenchWindow workbenchWindow, String viewId)
    {
        IViewReference[] references = workbenchWindow.getActivePage().getViewReferences();
        for (IViewReference ref : references) {
            if (ref.getId().equals(viewId)) {
                return ref.getView(false);
            }
        }
        return null;
    }

    public static void setClipboardContents(Display display, Transfer transfer, Object contents)
    {
        Clipboard clipboard = new Clipboard(display);
        clipboard.setContents(
            new Object[]{contents},
            new Transfer[]{transfer});
        clipboard.dispose();
    }

    public static void updateMainWindowTitle(IWorkbenchWindow window)
    {
        IProject activeProject = DBeaverCore.getInstance().getProjectRegistry().getActiveProject();
        IProduct product = Platform.getProduct();
        String title = product == null ? "Unknown" : product.getName(); //$NON-NLS-1$
        if (activeProject != null) {
            title += " - " + activeProject.getName(); //$NON-NLS-1$
        }
        IWorkbenchPage activePage = window.getActivePage();
        if (activePage != null) {
            IEditorPart activeEditor = activePage.getActiveEditor();
            if (activeEditor != null) {
                title += " - [ " + activeEditor.getTitle() + " ]";
            }
        }
        window.getShell().setText(title);
    }

    public static void showPreferencesFor(Shell shell, IAdaptable element, String defPageID)
    {
        PreferenceDialog propDialog = PreferencesUtil.createPropertyDialogOn(
            shell,
            element,
            defPageID,
            null,
            null);
        if (propDialog != null) {
            propDialog.open();
        }
    }

    public static void addFocusTracker(IServiceLocator serviceLocator, String controlID, Control control)
    {
        final IFocusService focusService = (IFocusService) serviceLocator.getService(IFocusService.class);
        if (focusService != null) {
            focusService.addFocusTracker(control, controlID);
        } else {
            log.debug("Focus service not found in " + serviceLocator);
        }
    }

    public static void removeFocusTracker(IServiceLocator serviceLocator, Control control)
    {
        if (PlatformUI.getWorkbench().isClosing()) {
            // TODO: it is a bug in eclipse. During workbench shutdown disposed service returned.
            return;
        }
        final IFocusService focusService = (IFocusService) serviceLocator.getService(IFocusService.class);
        if (focusService != null) {
            focusService.removeFocusTracker(control);
        } else {
            log.debug("Focus service not found in " + serviceLocator);
        }
    }

    public static IDialogSettings getDialogSettings(String dialogId)
    {
        IDialogSettings workbenchSettings = DBeaverActivator.getInstance().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(dialogId);
        if (section == null) {
            section = workbenchSettings.addNewSection(dialogId);
        }
        return section;
    }

    public static IWorkbenchPartSite getWorkbenchPartSite(IServiceLocator serviceLocator)
    {
        IWorkbenchPartSite partSite = (IWorkbenchPartSite) serviceLocator.getService(IWorkbenchPartSite.class);
        if (partSite == null) {
            IWorkbenchPart activePart = (IWorkbenchPart) serviceLocator.getService(IWorkbenchPart.class);
            if (activePart == null) {
                activePart = DBeaverUI.getActiveWorkbenchWindow().getActivePage().getActivePart();
            }
            if (activePart != null) {
                partSite = activePart.getSite();
            }
        }
        return partSite;
    }

    public static boolean isContextActive(String contextId)
    {
        Collection contextIds = ((IContextService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IContextService.class)).getActiveContextIds();
        for (Object id : contextIds) {
            if (contextId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static ISelectionProvider getSelectionProvider(IServiceLocator serviceLocator)
    {
        ISelectionProvider selectionProvider = (ISelectionProvider) serviceLocator.getService(IWorkbenchPartSite.class);
        if (selectionProvider != null) {
            return selectionProvider;
        }
        IWorkbenchPartSite partSite = getWorkbenchPartSite(serviceLocator);
        if (partSite == null) {
            IWorkbenchPart activePart = (IWorkbenchPart) serviceLocator.getService(IWorkbenchPart.class);
            if (activePart == null) {
                activePart = DBeaverUI.getActiveWorkbenchWindow().getActivePage().getActivePart();
            }
            if (activePart != null) {
                partSite = activePart.getSite();
            }
        }
        if (partSite != null) {
            return partSite.getSelectionProvider();
        } else {
            return null;
        }
    }

    public static void enableWithChildren(Composite composite, boolean enable)
    {
        composite.setEnabled(enable);
        for (Control child : composite.getChildren()) {
            if (child instanceof Composite) {
                enableWithChildren((Composite) child, enable);
            } else {
                child.setEnabled(enable);
            }
        }
    }

    public interface TableEditorController {
        void showEditor(TableItem item);
        void closeEditor(TableEditor tableEditor);
    }

    public static class ColumnTextEditorTraverseListener implements TraverseListener {
        private final Table table;
        private final TableEditor tableEditor;
        private final int columnIndex;
        private final TableEditorController controller;

        public ColumnTextEditorTraverseListener(Table table, TableEditor tableEditor, int columnIndex, TableEditorController controller)
        {
            this.table = table;
            this.tableEditor = tableEditor;
            this.columnIndex = columnIndex;
            this.controller = controller;
        }

        @Override
        public void keyTraversed(TraverseEvent e)
        {
            Text editor = (Text) tableEditor.getEditor();
            if (editor != null && editor.isDisposed()) {
                editor = null;
            }
            if (e.detail == SWT.TRAVERSE_RETURN) {
                if (editor != null) {
                    tableEditor.getItem().setText(columnIndex, editor.getText());
                    controller.closeEditor(tableEditor);
                } else {
                    TableItem[] selection = table.getSelection();
                    if (selection != null && selection.length >= 1) {
                        controller.showEditor(selection[0]);
                    }
                }
                e.doit = false;
                e.detail = SWT.TRAVERSE_NONE;
            } else if (e.detail == SWT.TRAVERSE_ESCAPE && editor != null) {
                controller.closeEditor(tableEditor);
                e.doit = false;
                e.detail = SWT.TRAVERSE_NONE;
            }
        }
    }

}