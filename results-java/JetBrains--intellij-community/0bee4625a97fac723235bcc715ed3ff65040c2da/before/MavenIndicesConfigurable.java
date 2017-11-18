package org.jetbrains.idea.maven.indices;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.AnimatedIcon;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MavenIndicesConfigurable extends BaseConfigurable {
  private Project myProject;
  private MavenProjectIndicesManager myManager;

  private JPanel myMainPanel;
  private JTable myTable;
  private JButton myUpdateButton;

  private Timer myRepaintTimer;

  private AnimatedIcon myUpdatingIcon;
  private Icon myWaitingIcon = IconLoader.getIcon("/process/step_passive.png");

  public MavenIndicesConfigurable(Project project) {
    myProject = project;
    myManager = MavenProjectIndicesManager.getInstance(myProject);

    myUpdatingIcon = new AsyncProcessIcon(IndicesBundle.message("maven.indices.updating"));

    myRepaintTimer = new Timer(
        AsyncProcessIcon.CYCLE_LENGTH / AsyncProcessIcon.COUNT,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int width = myTable.getColumnModel().getColumn(2).getWidth();
            myTable.repaint(myTable.getWidth() - width, 0, width, myTable.getHeight());
          }
        });

    configControls();
  }

  private void configControls() {
    myUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doUpdateIndex();
      }
    });

    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateButtonsState();
      }
    });
    updateButtonsState();
  }

  private void updateButtonsState() {
    boolean hasSelection = !myTable.getSelectionModel().isSelectionEmpty();
    myUpdateButton.setEnabled(hasSelection);
  }

  private void doUpdateIndex() {
    myManager.scheduleUpdate(getSelectedIndices());
  }

  private List<MavenIndex> getSelectedIndices() {
    List<MavenIndex> result = new ArrayList<MavenIndex>();
    for (int i : myTable.getSelectedRows()) {
      MyTableModel model = (MyTableModel)myTable.getModel();
      result.add(model.getIndex(i));
    }
    return result;
  }

  public String getDisplayName() {
    return IndicesBundle.message("maven.indices");
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public void apply() throws ConfigurationException {
  }

  public void reset() {
    myTable.setModel(new MyTableModel(myManager.getIndices()));
    myTable.getColumnModel().getColumn(0).setPreferredWidth(400);
    myTable.getColumnModel().getColumn(1).setPreferredWidth(50);
    myTable.getColumnModel().getColumn(2).setPreferredWidth(50);
    myTable.getColumnModel().getColumn(3).setPreferredWidth(20);

    myTable.setDefaultRenderer(MavenIndicesManager.IndexUpdatingState.class,
                               new MyIconCellRenderer());

    myRepaintTimer.start();
    myUpdatingIcon.resume();
  }

  public void disposeUIResources() {
    myUpdatingIcon.dispose();
    myRepaintTimer.stop();
  }

  private class MyTableModel extends AbstractTableModel {
    private final String[] COLUMNS =
        new String[]{
            IndicesBundle.message("maven.index.url"),
            IndicesBundle.message("maven.index.type"),
            IndicesBundle.message("maven.index.timestamp"),
            ""};

    private List<MavenIndex> myIndices;

    public MyTableModel(List<MavenIndex> indices) {
      myIndices = indices;
    }

    public int getColumnCount() {
      return COLUMNS.length;
    }

    @Override
    public String getColumnName(int index) {
      return COLUMNS[index];
    }

    public int getRowCount() {
      return myIndices.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 3) return MavenIndicesManager.IndexUpdatingState.class;
      return super.getColumnClass(columnIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      MavenIndex i = getIndex(rowIndex);
      switch (columnIndex) {
        case 0:
          return i.getRepositoryPathOrUrl();
        case 1:
          if (i.getKind() == MavenIndex.Kind.LOCAL) return "Local";
          return "Remote";
        case 2:
          long timestamp = i.getUpdateTimestamp();
          if (timestamp == -1) return IndicesBundle.message("maven.index.timestamp.unknown");
          return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(new Date(timestamp));
        case 3:
          return myManager.getUpdatingState(i);
      }
      throw new RuntimeException();
    }

    public MavenIndex getIndex(int rowIndex) {
      return myIndices.get(rowIndex);
    }
  }

  private class MyIconCellRenderer extends DefaultTableCellRenderer {
    MavenIndicesManager.IndexUpdatingState myState;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      myState = (MavenIndicesManager.IndexUpdatingState)value;
      return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Dimension size = getSize();
      switch (myState) {
        case UPDATING:
          myUpdatingIcon.setBackground(getBackground());
          myUpdatingIcon.setSize(size.width, size.height);
          myUpdatingIcon.paint(g);
          break;
        case WAITING:
          int x = (size.width - myWaitingIcon.getIconWidth()) / 2;
          int y = (size.height - myWaitingIcon.getIconHeight()) / 2;
          myWaitingIcon.paintIcon(this, g, x, y);
          break;
      }
    }
  }
}