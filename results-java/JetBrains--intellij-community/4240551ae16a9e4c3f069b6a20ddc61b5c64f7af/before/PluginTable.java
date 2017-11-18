package com.intellij.ide.plugins;

import com.intellij.ui.table.TableHeaderRenderer;
import com.intellij.util.ui.SortableColumnModel;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: stathik
 * Date: Dec 11, 2003
 * Time: 4:19:20 PM
 * To change this template use Options | File Templates.
 */
//public class PluginTable <T> extends Table {
public class PluginTable <T> extends JTable {
  public PluginTable(final PluginTableModel model)
  {
    super(model);

    JTableHeader tableHeader = getTableHeader();
    tableHeader.setDefaultRenderer(new TableHeaderRenderer (model));
    tableHeader.addMouseListener(new MouseAdapter () {
      public void mouseClicked(MouseEvent e) {
        int column = getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());

        if (model.sortableProvider.getSortColumn() == column) {
          if (model.sortableProvider.getSortOrder() == SortableColumnModel.SORT_DESCENDING)
            model.sortableProvider.setSortOrder(SortableColumnModel.SORT_ASCENDING);
          else
            model.sortableProvider.setSortOrder(SortableColumnModel.SORT_DESCENDING);
        } else {
          model.sortableProvider.setSortOrder(SortableColumnModel.SORT_ASCENDING);
          model.sortableProvider.setSortColumn(column);
        }

        model.sortByColumn (column);

        getTableHeader().updateUI();
      }
    });
    tableHeader.setReorderingAllowed(false);

    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumn column = getColumnModel().getColumn(i);
      column.setCellRenderer(model.getColumnInfos() [i].getRenderer(null));
      //column.setPreferredWidth(PluginManagerColumnInfo.PREFERRED_WIDTH [i]);
    }

    //  Specify columns widths for particular columns:
    //  Icon/Status
    TableColumn column = getColumnModel().getColumn( 0 );
    column.setMinWidth( 30 );
    column.setMaxWidth( 30 );

    //  Downloads
    column = getColumnModel().getColumn( 2 );
    column.setMinWidth( 70 );
    column.setMaxWidth( 70 );

    //  Date:
    column = getColumnModel().getColumn( 3 );
    column.setMaxWidth( 80 );

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setShowGrid(false);
  }

  public T getObjectAt (int row) {
    return ((PluginTableModel <T>)getModel()).getObjectAt(row);
  }

  public T getSelectedObject () {
    T o = null;
    if (getSelectedRowCount() > 0) {
      o = getObjectAt(getSelectedRow());
    }
    return o;
  }

  public Object [] getElements () {
    return ((PluginTableModel<T>)getModel()).view.toArray();
  }
}