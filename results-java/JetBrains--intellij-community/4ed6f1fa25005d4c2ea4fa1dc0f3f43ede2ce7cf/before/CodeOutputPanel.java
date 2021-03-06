package org.jetbrains.plugins.ipnb.editor.panels.code;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbPanel;

import javax.swing.*;

public class CodeOutputPanel extends IpnbPanel {
  @NotNull private final String myOutputText;

  public CodeOutputPanel(@NotNull String outputText) {
    myOutputText = outputText;
    myViewPanel = createViewPanel();
    add(myViewPanel);
  }

  protected JComponent createViewPanel() {
    JTextArea textArea = new JTextArea(myOutputText);
    textArea.setEditable(false);
    return textArea;
  }
}