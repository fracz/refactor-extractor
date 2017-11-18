package com.intellij.application.options;

import com.intellij.openapi.options.Scheme;
import com.intellij.openapi.options.SchemesManager;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;


public abstract class SchemesToImportPopup<T extends Scheme> {

  private final Component myParent;

  public SchemesToImportPopup(final Component parent) {
    myParent = parent;
  }

  public void show(SchemesManager<T> schemesManager, Collection<String> currentSchemeNames) {
    Collection<T> schemes = schemesManager.loadScharedSchemes(currentSchemeNames);
    final JList list = new JList(createModel(schemes));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new RecentChangesListCellRenderer());



    Runnable selectAction = new Runnable() {
      public void run() {
        onSchemeSelected((T)list.getSelectedValue());
      }
    };

    showList(list, selectAction);
  }

  private ListModel createModel(Collection<T> cc) {
    DefaultListModel m = new DefaultListModel();
    for (Scheme c : cc) {
      m.addElement(c);
    }
    return m;
  }

  private void showList(JList list, Runnable selectAction) {
    new PopupChooserBuilder(list).
      setTitle(getTitle()).
      setItemChoosenCallback(selectAction).
      createPopup().
      showInCenterOf(myParent);
  }

  private String getTitle() {
    return "Import Scheme";
  }

  private static class RecentChangesListCellRenderer implements ListCellRenderer {
    private JPanel myPanel = new JPanel(new BorderLayout());
    private JLabel myNameLabel = new JLabel("", JLabel.LEFT);

    public RecentChangesListCellRenderer() {
      myPanel.add(myNameLabel, BorderLayout.CENTER);
    }

    public Component getListCellRendererComponent(JList l, Object val, int i, boolean isSelected, boolean cellHasFocus) {
      Scheme c = (Scheme)val;
      myNameLabel.setText(c.getName());

      updateColors(isSelected);
      return myPanel;
    }

    private void updateColors(boolean isSelected) {
      Color bg = isSelected ? UIUtil.getTableSelectionBackground() : UIUtil.getTableBackground();
      Color fg = isSelected ? UIUtil.getTableSelectionForeground() : UIUtil.getTableForeground();

      setColors(bg, fg, myPanel, myNameLabel);
    }

    private void setColors(Color bg, Color fg, JComponent... cc) {
      for (JComponent c : cc) {
        c.setBackground(bg);
        c.setForeground(fg);
      }
    }
  }

  abstract protected void onSchemeSelected(T scheme);

}