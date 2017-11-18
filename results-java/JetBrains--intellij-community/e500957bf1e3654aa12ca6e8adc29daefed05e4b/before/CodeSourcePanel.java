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
package org.jetbrains.plugins.ipnb.editor.panels.code;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.editor.IpnbEditorUtil;
import org.jetbrains.plugins.ipnb.editor.panels.EditorPanel;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbFilePanel;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author traff
 */
public class CodeSourcePanel extends IpnbPanel implements EditorPanel {
  private Editor myEditor;
  @NotNull private final Project myProject;
  @NotNull private final CodePanel myParent;
  @NotNull private final String mySource;

  public CodeSourcePanel(@NotNull final Project project, @NotNull final CodePanel parent, @NotNull final String source) {
    myProject = project;
    myParent = parent;
    mySource = source;
    final JComponent panel = createViewPanel();
    add(panel);
  }

  @Override
  @NotNull
  public Editor getEditor() {
    return myEditor;
  }

  @Override
  protected JComponent createViewPanel() {
    final JPanel panel = new JPanel(new VerticalFlowLayout(FlowLayout.LEFT, true, true));
    panel.setBackground(UIUtil.isUnderDarcula() ? IpnbEditorUtil.getBackground() : Gray._247);

    if (mySource.startsWith("%"))
      myEditor = IpnbEditorUtil.createPlainCodeEditor(myProject, mySource);
    else
      myEditor = IpnbEditorUtil.createPythonCodeEditor(myProject, mySource);

    final JComponent contentComponent = myEditor.getContentComponent();
    contentComponent.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_BACK_SPACE ||
            keyCode == KeyEvent.VK_DELETE)
        panel.revalidate();
        panel.repaint();
      }
    });

    contentComponent.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        final Container ipnbFilePanel = myParent.getParent();
        if (ipnbFilePanel instanceof IpnbFilePanel) {
          ((IpnbFilePanel)ipnbFilePanel).setSelectedCell(myParent);
          myParent.switchToEditing();
        }
        UIUtil.requestFocus(contentComponent);
      }
    });

    final JComponent component = myEditor.getComponent();
    panel.add(component);
    contentComponent.setPreferredSize(new Dimension(IpnbEditorUtil.PANEL_WIDTH, panel.getPreferredSize().height));
    setBorder(BorderFactory.createLineBorder(JBColor.lightGray, 1, true));
    return panel;
  }
}