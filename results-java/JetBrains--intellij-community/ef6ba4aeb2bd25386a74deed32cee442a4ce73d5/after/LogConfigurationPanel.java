package com.intellij.diagnostic.logging;

import com.intellij.diagnostic.DiagnosticBundle;
import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 * Date: Apr 22, 2005
 */
public class LogConfigurationPanel extends SettingsEditor<RunConfigurationBase> {
  private TableView myFilesTable;
  private ListTableModel myModel;
  private JPanel myWholePanel = new JPanel(new BorderLayout());
  private JButton myAddButton;
  private JButton myRemoveButton;
  private JPanel myButtonsPanel;


  private final ColumnInfo<LogFileOptions, Boolean> IS_SHOW = new MyIsActiveColumnInfo();
  private final ColumnInfo<LogFileOptions, LogFileOptions> FILE = new MyLogFileColumnInfo();
  private final ColumnInfo<LogFileOptions, Boolean> IS_SKIP_CONTENT = new MyIsSkippColumnInfo();

  public LogConfigurationPanel() {
    myModel = new ListTableModel<LogFileOptions>(new ColumnInfo[]{IS_SHOW, FILE, IS_SKIP_CONTENT});
    myFilesTable = new TableView(myModel);
    final TableCellRenderer booleanCellRenderer = new TableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
        final Component component = myFilesTable.getDefaultRenderer(Boolean.class)
          .getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                         row, column);
        if (component instanceof JComponent) {
          ((JComponent)component).setBorder(null);
        }
        return component;
      }
    };
    myFilesTable.getColumnModel().getColumn(0).setCellRenderer(booleanCellRenderer);
    myFilesTable.getColumnModel().getColumn(2).setCellRenderer(booleanCellRenderer);
    myFilesTable.setColumnSelectionAllowed(false);
    myFilesTable.setShowGrid(false);
    myFilesTable.setDragEnabled(false);
    myFilesTable.setShowHorizontalLines(false);
    myFilesTable.setShowVerticalLines(false);
    myFilesTable.setIntercellSpacing(new Dimension(0, 0));
    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ArrayList<LogFileOptions> newList = new ArrayList<LogFileOptions>(myModel.getItems());
        final LogFileOptions selectedNameLocation = showEditorDialog("", "", false);
        if (selectedNameLocation != null) {
          newList.add(new LogFileOptions(selectedNameLocation.getName(), selectedNameLocation.getPathPattern(), false, true, selectedNameLocation.isShowAll()));
          myModel.setItems(newList);
          int index = myModel.getRowCount() - 1;
          myModel.fireTableRowsInserted(index, index);
          myFilesTable.setRowSelectionInterval(index, index);
        }
      }
    });
    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TableUtil.stopEditing(myFilesTable);
        final int[] selected = myFilesTable.getSelectedRows();
        if (selected == null || selected.length == 0) return;
        for (int i = selected.length - 1; i >= 0; i--) {
          myModel.removeRow(selected[i]);
        }
        for (int i = selected.length - 1; i >= 0; i--) {
          int idx = selected[i];
          myModel.fireTableRowsDeleted(idx, idx);
        }
        int selection = selected[0];
        if (selection >= myModel.getRowCount()) {
          selection = myModel.getRowCount() - 1;
        }
        if (selection >= 0) {
          myFilesTable.setRowSelectionInterval(selection, selection);
        }
        myFilesTable.requestFocus();
      }
    });
    myRemoveButton.setEnabled(false);
    myFilesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
         myRemoveButton.setEnabled(myFilesTable.getSelectedRowCount() >=1);
      }
    });
    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myFilesTable);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    myWholePanel.add(scrollPane, BorderLayout.CENTER);
    myWholePanel.add(myButtonsPanel, BorderLayout.EAST);
    myWholePanel.setBorder(BorderFactory.createTitledBorder(DiagnosticBundle.message("log.monitor.group")));
    myWholePanel.setPreferredSize(new Dimension(-1, 150));
  }

  protected void resetEditorFrom(final RunConfigurationBase configuration) {
    clearModel();
    ArrayList<LogFileOptions> list = new ArrayList<LogFileOptions>();
    final ArrayList<LogFileOptions> logFiles = configuration.getLogFiles();
    for (LogFileOptions setting : logFiles) {
      list.add(new LogFileOptions(setting.getName(), setting.getPathPattern(), setting.isEnabled(), setting.isSkipContent(), setting.isShowAll()));
    }
    myModel.setItems(list);
  }

  protected void applyEditorTo(final RunConfigurationBase configuration) throws ConfigurationException {
    myFilesTable.stopEditing();
    configuration.removeAllLogFiles();
    for (int i = 0; i < myModel.getRowCount(); i++) {
      LogFileOptions pair = (LogFileOptions)myModel.getValueAt(i, 1);
      if (Comparing.equal(pair.getPathPattern(),"")){
        continue;
      }
      final Boolean checked = (Boolean)myModel.getValueAt(i, 0);
      final Boolean skipped = (Boolean)myModel.getValueAt(i, 2);
      configuration.addLogFile(pair.getPathPattern(), pair.getName(), checked.booleanValue(), skipped.booleanValue(), pair.isShowAll());
    }
  }

  protected JComponent createEditor() {
    return myWholePanel;
  }

  protected void disposeEditor() {

  }

  public JComponent getLoggerComponent() {
    return getComponent();
  }

  public void addLogFile(String file, String alias, boolean isShowAll, boolean checked, boolean skipContent) {
    final List<LogFileOptions> itemsUnmodifiable = myModel.getItems();
    List<LogFileOptions> items = new ArrayList<LogFileOptions>();
    items.addAll(itemsUnmodifiable);
    items.add(new LogFileOptions(alias, file, checked, skipContent, isShowAll));
    myModel.setItems(items);
  }

  private void clearModel() {
    myModel.setItems(new ArrayList<LogFileOptions>());
  }

 @Nullable
  private static LogFileOptions showEditorDialog(String name, String location, boolean showAll){
    EditLogPatternDialog dialog = new EditLogPatternDialog();
    dialog.init(name, location, showAll);
    dialog.show();
    if (dialog.isOK()) {
      location = dialog.getLogPattern();
      name = dialog.getName();
      showAll = dialog.isShowAllFiles();
      return new LogFileOptions(name, location, false, false, showAll);
    }
    return null;
  }

  private static class MyLogFileColumnInfo extends ColumnInfo<LogFileOptions, LogFileOptions> {
    public MyLogFileColumnInfo() {
      super(DiagnosticBundle.message("log.monitor.log.file.column"));
    }

    public TableCellRenderer getRenderer(final LogFileOptions p0) {
      return new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
          final Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          setText(((LogFileOptions)value).getName());
          setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
          setBorder(null);
          return renderer;
        }
      };
    }

    public LogFileOptions valueOf(final LogFileOptions object) {
      return object;
    }

    public TableCellEditor getEditor(final LogFileOptions item) {
      return new LogFileCellEditor(item);
    }

    public void setValue(final LogFileOptions o, final LogFileOptions aValue) {
      if (aValue != null) {
        o.setName(aValue.getName());
        o.setLast(!aValue.isShowAll());
        o.setPathPattern(aValue.getPathPattern());
      }
    }

    public boolean isCellEditable(final LogFileOptions o) {
      return true;
    }
  }

  private static class MyIsActiveColumnInfo extends ColumnInfo<LogFileOptions, Boolean> {
    private final static String NAME = DiagnosticBundle.message("log.monitor.is.active.column");
    protected MyIsActiveColumnInfo() {
      super(NAME);
    }

    public Class getColumnClass() {
      return Boolean.class;
    }

    public Boolean valueOf(final LogFileOptions object) {
      return object.isEnabled();
    }

    public boolean isCellEditable(LogFileOptions element) {
      return true;
    }

    public void setValue(LogFileOptions element, Boolean checked){
      element.setEnable(checked.booleanValue());
    }

    public int getWidth(final JTable table) {
      FontMetrics metrics = table.getFontMetrics(table.getFont());
      return metrics.stringWidth(NAME) + 15;
    }
  }

  private static class MyIsSkippColumnInfo extends ColumnInfo<LogFileOptions, Boolean> {
    private final static String NAME = DiagnosticBundle.message("log.monitor.is.skipped.column");

    protected MyIsSkippColumnInfo() {
      super(NAME);
    }

    public Class getColumnClass() {
      return Boolean.class;
    }

    public Boolean valueOf(final LogFileOptions element) {
      return element.isSkipContent();
    }

    public boolean isCellEditable(LogFileOptions element) {
      return true;
    }

    public void setValue(LogFileOptions element, Boolean skipped){
      element.setSkipContent(skipped.booleanValue());
    }

    public int getWidth(final JTable table) {
      FontMetrics metrics = table.getFontMetrics(table.getFont());
      return metrics.stringWidth(NAME) + 15;
    }
  }

  private static class LogFileCellEditor extends AbstractTableCellEditor {
    private final CellEditorComponentWithBrowseButton<JTextField> myComponent;
    private LogFileOptions myLogFileOptions;

    public LogFileCellEditor(LogFileOptions options) {
      myLogFileOptions = options;
      myComponent = new CellEditorComponentWithBrowseButton<JTextField>(new TextFieldWithBrowseButton(), this);
      getChildComponent().setEditable(false);
      getChildComponent().setBorder(null);
      myComponent.getComponentWithButton().getButton().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          final LogFileOptions newValue = showEditorDialog(myLogFileOptions.getName(), myLogFileOptions.getPathPattern(), myLogFileOptions.isShowAll());
          if (newValue != null) {
            myLogFileOptions = newValue;
          }
          JTextField textField = getChildComponent();
          textField.setText(myLogFileOptions.getName());
          textField.requestFocus();
        }
      });
    }

    public Object getCellEditorValue() {
      return myLogFileOptions;
    }

    private JTextField getChildComponent() {
      return myComponent.getChildComponent();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      getChildComponent().setText(((LogFileOptions)value).getName());
      return myComponent;
    }
  }
}