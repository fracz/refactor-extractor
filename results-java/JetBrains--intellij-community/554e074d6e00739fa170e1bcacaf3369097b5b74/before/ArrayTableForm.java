/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.jetbrains.python.debugger.array;

import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * @author amarch
 */
public class ArrayTableForm {
  private EditorTextField mySliceTextField;
  private JCheckBox myColoredCheckbox;
  private EditorTextField myFormatTextField;
  private JBScrollPane myScrollPane;
  private JLabel myFormatLabel;
  private JPanel myFormatPanel;
  private JPanel myMainPanel;
  private JBTable myTable;
  private JBTable myBusyTable;
  private final Project myProject;


  private static final String DATA_LOADING_IN_PROCESS = "Please wait, load array data.";
  private static final String NOT_APPLICABLE = "View not applicable for ";

  public ArrayTableForm(@NotNull Project project) {
    myProject = project;
  }

  private void createUIComponents() {
    mySliceTextField = new EditorTextField("", myProject, PythonFileType.INSTANCE);

    myTable = new JBTableWithRowHeaders();
    myTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    myTable.setRowSelectionAllowed(false);
    myTable.setTableHeader(new CustomTableHeader(myTable));
    myTable.getTableHeader().setDefaultRenderer(new ColumnHeaderRenderer());
    myTable.getTableHeader().setReorderingAllowed(false);

    myScrollPane = PagingTableModel.LazyViewport.createLazyScrollPaneFor(myTable);
    JTable rowTable = new JBTableWithRowHeaders.RowHeaderTable(myTable);
    myScrollPane.setRowHeaderView(rowTable);
    myScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER,
                           rowTable.getTableHeader());

    ((JBTableWithRowHeaders)myTable).setRowHeaderTable((JBTableWithRowHeaders.RowHeaderTable)rowTable);

    myFormatTextField = new EditorTextField("", myProject, PythonFileType.INSTANCE);

    myBusyTable = new JBTable(new DefaultTableModel());
    myBusyTable.getEmptyText().setText("");
    myBusyTable.setBackground(myBusyTable.getTableHeader().getBackground());
    myBusyTable.setBorder(BorderFactory.createEmptyBorder());
  }

  public EditorTextField getSliceTextField() {
    return mySliceTextField;
  }

  public EditorTextField getFormatTextField() {
    return myFormatTextField;
  }

  public JBTable getTable() {
    return myTable;
  }

  public JCheckBox getColoredCheckbox() {
    return myColoredCheckbox;
  }

  public void setDefaultStatus() {
    if (myTable != null) {
      myTable.getEmptyText().setText(DATA_LOADING_IN_PROCESS);
    }
  }

  public void setNotApplicableStatus(XValueNodeImpl node) {
    myTable.getEmptyText().setText(NOT_APPLICABLE + node.getName());
  }

  public JComponent getMainPanel() {
    return myMainPanel;
  }

  public JBScrollPane getScrollPane() {
    return myScrollPane;
  }

  public void setBusy(boolean busy) {
    myBusyTable.setPaintBusy(busy);
  }

  public static class CustomTableHeader extends JTableHeader {

    public CustomTableHeader(JTable table) {
      super();
      setColumnModel(table.getColumnModel());
      table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          repaint();
        }
      });
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
      repaint();
    }
  }

  public static class ColumnHeaderRenderer extends DefaultTableHeaderCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      super.getTableCellRendererComponent(table, value, selected, focused, row, column);
      int selectedColumn = table.getSelectedColumn();
      if (selectedColumn == column) {
        setFont(getFont().deriveFont(Font.BOLD));
      }
      return this;
    }
  }

  public static class DefaultTableHeaderCellRenderer extends DefaultTableCellRenderer {

    public DefaultTableHeaderCellRenderer() {
      setHorizontalAlignment(CENTER);
      setHorizontalTextPosition(LEFT);
      setVerticalAlignment(BOTTOM);
      setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value,
                                          isSelected, hasFocus, row, column);
      JTableHeader tableHeader = table.getTableHeader();
      if (tableHeader != null) {
        setForeground(tableHeader.getForeground());
      }
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      return this;
    }
  }
}