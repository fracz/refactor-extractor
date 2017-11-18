package com.intellij.diagnostic.logging;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.j2ee.extResources.EditLocationDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


  private final ColumnInfo<MyFilesTableRowElement, Boolean> IS_SHOW = new MyIsActiveColumnInfo();
  private final ColumnInfo<MyFilesTableRowElement, Pair<String, String>> FILE = new MyLogFileColumnInfo();

  public LogConfigurationPanel() {
    myModel = new ListTableModel<MyFilesTableRowElement>(new ColumnInfo[]{IS_SHOW, FILE});
    myFilesTable = new TableView(myModel);
    myFilesTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
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
    });
    myFilesTable.setColumnSelectionAllowed(false);
    myFilesTable.setShowGrid(false);
    myFilesTable.setDragEnabled(false);
    myFilesTable.setShowHorizontalLines(false);
    myFilesTable.setShowVerticalLines(false);
    myFilesTable.setIntercellSpacing(new Dimension(0, 0));
    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ArrayList<MyFilesTableRowElement> newList = new ArrayList<MyFilesTableRowElement>(myModel.getItems());
        newList.add(new MyFilesTableRowElement("", "", false));
        myModel.setItems(newList);
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
         myRemoveButton.setEnabled(myFilesTable.getSelectedRows() != null);
      }
    });
    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myFilesTable);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    myWholePanel.add(scrollPane, BorderLayout.CENTER);
    myWholePanel.add(myButtonsPanel, BorderLayout.EAST);
    myWholePanel.setBorder(BorderFactory.createTitledBorder("Log Files To Show In Console"));
    myWholePanel.setPreferredSize(new Dimension(-1, 150));
  }

  protected void resetEditorFrom(final RunConfigurationBase configuration) {
    clearModel();
    ArrayList<MyFilesTableRowElement> list = new ArrayList<MyFilesTableRowElement>();
    final Map<Pair<String, String>, Boolean> logFiles = configuration.getLogFiles();
    for (Pair<String, String> pair : logFiles.keySet()) {
      list.add(new MyFilesTableRowElement(pair.first, pair.second, logFiles.get(pair).booleanValue()));
    }
    myModel.setItems(list);
  }

  protected void applyEditorTo(final RunConfigurationBase configuration) throws ConfigurationException {
    myFilesTable.stopEditing();
    configuration.removeAllLogFiles();
    for (int i = 0; i < myModel.getRowCount(); i++) {
      Pair pair = (Pair)myModel.getValueAt(i, 1);
      if (Comparing.equal(pair.first,"")){
        continue;
      }
      if (Comparing.equal(pair.second,"")){
        pair = Pair.create(pair.first, pair.first);
      }
      final Boolean checked = (Boolean)myModel.getValueAt(i, 0);
      configuration.addLogFile((String)pair.first, (String)pair.second, checked.booleanValue());
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

  public void addLogFile(String file, String alias, boolean checked) {
    final java.util.List<MyFilesTableRowElement> itemsUnmodifiable = myModel.getItems();
    List<MyFilesTableRowElement> items = new ArrayList<MyFilesTableRowElement>();
    items.addAll(itemsUnmodifiable);
    items.add(new MyFilesTableRowElement(file, alias, checked));
    myModel.setItems(items);
  }

  private void clearModel() {
    myModel.setItems(new ArrayList<MyFilesTableRowElement>());
  }

  private static class MyFilesTableRowElement {
    private Pair<String, String> myPair;
    private boolean myEnabled;

    public MyFilesTableRowElement(String file, String alias, boolean enabled) {
      myPair = Pair.create(file, alias);
      myEnabled = enabled;
    }

    public boolean isEnabled() {
      return myEnabled;
    }

    public void setEnabled(final boolean enabled) {
      myEnabled = enabled;
    }

    public Pair<String, String> getPair() {
      return myPair;
    }

  }

  private class MyLogFileColumnInfo extends ColumnInfo<MyFilesTableRowElement, Pair<String, String>> {
    public MyLogFileColumnInfo() {
      super("Log File Entry");
    }

    public TableCellRenderer getRenderer(final MyFilesTableRowElement p0) {
      return new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
          final Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          setText(((Pair<String, String>)value).second);
          setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
          setBorder(null);
          return renderer;
        }
      };
    }

    public Pair<String, String> valueOf(final MyFilesTableRowElement object) {
      return object.getPair();
    }

    public TableCellEditor getEditor(final MyFilesTableRowElement item) {
      return new LogFileCellEditor(item.getPair());
    }

    public void setValue(final MyFilesTableRowElement o, final Pair<String, String> aValue) {
      o.myPair = aValue;
    }

    public boolean isCellEditable(final MyFilesTableRowElement o) {
      return true;
    }
  }

  private static class MyIsActiveColumnInfo extends ColumnInfo<MyFilesTableRowElement, Boolean> {
    private final static String NAME = "Is Active";
    protected MyIsActiveColumnInfo() {
      super(NAME);
    }

    public Class getColumnClass() {
      return Boolean.class;
    }

    public Boolean valueOf(final MyFilesTableRowElement object) {
      return new Boolean(object.isEnabled());
    }

    public boolean isCellEditable(MyFilesTableRowElement element) {
      return true;
    }

    public void setValue(MyFilesTableRowElement element, Boolean checked){
      element.setEnabled(checked.booleanValue());
    }

    public int getWidth(final JTable table) {
      FontMetrics metrics = table.getFontMetrics(table.getFont());
      return metrics.stringWidth(NAME) + 15;
    }
  }

  private static class LogFileCellEditor extends AbstractTableCellEditor {
    private final CellEditorComponentWithBrowseButton<JTextField> myComponent;
    private Pair<String, String> mySelectedPair;

    public LogFileCellEditor(Pair<String, String> pair) {
      mySelectedPair = pair;
      myComponent = new CellEditorComponentWithBrowseButton<JTextField>(new TextFieldWithBrowseButton(), this);
      getChildComponent().setEditable(false);
      getChildComponent().setBorder(null);
      myComponent.getComponentWithButton().getButton().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          EditLocationDialog dialog = new EditLocationDialog(null, true, "Edit Log Files Aliases", "Alias:", "Log File Location:") {
            protected FileChooserDescriptor getChooserDescriptor() {
              return BrowseFilesListener.SINGLE_FILE_DESCRIPTOR;
            }
          };
          dialog.init(mySelectedPair.second, mySelectedPair.first);
          dialog.show();
          if (dialog.isOK()) {
            final EditLocationDialog.Pair pair = dialog.getPair();
            if (pair != null) {
              String name = pair.getName();
              final String location = pair.getLocation();
              if (name == null || name.length() ==0){
                name = location;
              }
              mySelectedPair = Pair.create(location, name);
              JTextField textField = getChildComponent();
              textField.setText(name);
              textField.requestFocus();
            }
          }
        }
      });
    }

    public Object getCellEditorValue() {
      return mySelectedPair;
    }

    private JTextField getChildComponent() {
      return myComponent.getChildComponent();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      getChildComponent().setText(((Pair<String, String>)value).second);
      return myComponent;
    }
  }
}