/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.errorhandling;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadExceptionCaughtInspection extends ExpressionInspection {
    /** @noinspection PublicField*/
    public String exceptionCheckString =
            "java.lang.NullPointerException" + "," +
            "java.lang.IllegalMonitorStateException" + "," +
            "java.lang.ArrayIndexOutOfBoundsException";

    private final List<String> exceptionsList = new ArrayList<String>(32);
    private final Object lock = new Object();

    {
        parseExceptionsString();
    }

    public void readSettings(Element element) throws InvalidDataException {
        super.readSettings(element);
        parseExceptionsString();
    }

    private void parseExceptionsString() {
        final String[] strings = exceptionCheckString.split(",");
        synchronized(lock){
            exceptionsList.clear();
            for(String string : strings){
                exceptionsList.add(string);
            }
        }
    }

    public void writeSettings(Element element) throws WriteExternalException {
        formatCallCheckString();
        super.writeSettings(element);
    }

    private void formatCallCheckString() {
        final StringBuffer buffer = new StringBuffer();
        synchronized(lock){
            boolean first = true;
            for(String exceptionName : exceptionsList){
                if(first){
                    first = false;
                } else{
                    buffer.append(',');
                }
                buffer.append(exceptionName);
            }
        }
        exceptionCheckString = buffer.toString();
    }

    public String getID(){
        return "ProhibitedExceptionCaught";
    }
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "bad.exception.caught.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.ERRORHANDLING_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "bad.exception.caught.problem.descriptor");
    }

    public JComponent createOptionsPanel() {
        final Form form = new Form();
        return form.getContentPanel();
    }

    public BaseInspectionVisitor buildVisitor() {
        return new BadExceptionCaughtVisitor();
    }

    private class BadExceptionCaughtVisitor extends BaseInspectionVisitor {

        public void visitTryStatement(@NotNull PsiTryStatement statement) {

            super.visitTryStatement(statement);
            final PsiParameter[] catchBlockParameters =
                    statement.getCatchBlockParameters();
            for(final PsiParameter parameter : catchBlockParameters){
                if(parameter == null){
                    continue;
                }
                final PsiType type = parameter.getType();
                final String text = type.getCanonicalText();
                if(text == null){
                    continue;
                }

                final List<String> exceptionListCopy;
                synchronized(lock){
                    exceptionListCopy = new ArrayList<String>(exceptionsList);
                }
                for(String exceptionClass : exceptionListCopy){
                    if(text.equals(exceptionClass)){
                        final PsiTypeElement typeElement =
                                parameter.getTypeElement();
                        registerError(typeElement);
                    }
                }
            }
        }
    }

    private class Form {
        JPanel contentPanel;
        JButton addButton;
        JButton deleteButton;
        JTable table;

        Form() {
            super();
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setRowSelectionAllowed(true);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            final ReturnCheckSpecificationTableModel model =
                    new ReturnCheckSpecificationTableModel();
            table.setModel(model);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final int listSize;
                    synchronized(lock){
                        listSize = exceptionsList.size();
                        exceptionsList.add("");
                    }
                    model.fireTableStructureChanged();

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            final Rectangle rect = table.getCellRect(listSize, 0, true);
                            table.scrollRectToVisible(rect);
                            table.editCellAt(listSize, 0);
                            final TableCellEditor editor = table.getCellEditor();
                            final Component component =
                                    editor.getTableCellEditorComponent(table, null, true, listSize, 0);
                            component.requestFocus();
                        }
                    });

                }
            });
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final int[] selectedRows = table.getSelectedRows();
                    if (selectedRows.length == 0) {
                        return;
                    }
                    final int row = selectedRows[selectedRows.length - 1] - 1;
                    Arrays.sort(selectedRows);
                    synchronized(lock){
                        for (int i = selectedRows.length - 1; i >= 0; i--) {
                            exceptionsList.remove(selectedRows[i]);
                        }
                    }
                    model.fireTableStructureChanged();
                    final int count = table.getRowCount();
                    if (count <= row) {
                        table.setRowSelectionInterval(count - 1, count - 1);
                    } else {
                        table.setRowSelectionInterval(row, row);
                    }
                }
            });
        }

        public JComponent getContentPanel() {
            return contentPanel;
        }
    }

    private class ReturnCheckSpecificationTableModel extends AbstractTableModel {

        public int getRowCount() {
            synchronized(lock){
                return exceptionsList.size();
            }
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int columnIndex) {
            return InspectionGadgetsBundle.message("exception.class.column.name");
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            synchronized(lock){
                return exceptionsList.get(rowIndex);
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            synchronized(lock){
                exceptionsList.set(rowIndex, (String)aValue);
            }
        }
    }
}