package org.jetbrains.plugins.ipnb.editor.panels.code;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbPanel;
import org.jetbrains.plugins.ipnb.format.cells.output.StreamCellOutput;

import javax.swing.*;

public class StreamPanel extends IpnbPanel {

  @NotNull private final StreamCellOutput myCell;

  public StreamPanel(@NotNull final StreamCellOutput cell) {
    myCell = cell;
    myViewPanel = createViewPanel();
    add(myViewPanel);
  }

  @Override
  protected JComponent createViewPanel() {
    JTextArea textArea = new JTextArea(myCell.getSourceAsString());
    textArea.setEditable(false);
    return textArea;
  }
}