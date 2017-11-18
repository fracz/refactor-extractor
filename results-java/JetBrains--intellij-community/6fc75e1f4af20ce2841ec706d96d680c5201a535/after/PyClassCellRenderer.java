package com.jetbrains.python.refactoring.classes.ui;

import com.intellij.openapi.util.Iconable;
import com.jetbrains.python.psi.PyClass;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dennis.Ushakov
 */
public class PyClassCellRenderer extends DefaultListCellRenderer {
  private final boolean myShowReadOnly;
  public PyClassCellRenderer() {
    setOpaque(true);
    myShowReadOnly = true;
  }

  public PyClassCellRenderer(boolean showReadOnly) {
    setOpaque(true);
    myShowReadOnly = showReadOnly;
  }

  public Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    return customizeRenderer(this, value, myShowReadOnly);
  }

  public static JLabel customizeRenderer(final JLabel cellRendererComponent, final Object value, final boolean showReadOnly) {
    PyClass aClass = (PyClass) value;
    cellRendererComponent.setText(aClass.getName());

    int flags = Iconable.ICON_FLAG_VISIBILITY;
    if (showReadOnly) {
      flags |= Iconable.ICON_FLAG_READ_STATUS;
    }
    Icon icon = aClass.getIcon(flags);
    if(icon != null) {
      cellRendererComponent.setIcon(icon);
    }
    return cellRendererComponent;
  }
}