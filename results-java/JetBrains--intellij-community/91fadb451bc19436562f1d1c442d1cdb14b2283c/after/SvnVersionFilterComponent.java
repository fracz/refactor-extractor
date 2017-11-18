/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.svn.history;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.versionBrowser.StandardVersionFilterComponent;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SvnVersionFilterComponent extends StandardVersionFilterComponent<ChangeBrowserSettings> {
  private JCheckBox myUseAuthorFilter;
  private JTextField myAuthorField;
  private JPanel myPanel;
  private JPanel myStandardPanel;
  private final Project myProject;

  public SvnVersionFilterComponent(Project project) {
    super(project);
    myStandardPanel.setLayout(new BorderLayout());
    myStandardPanel.add(getStandardPanel(), BorderLayout.CENTER);
    myProject = project;
    init(new ChangeBrowserSettings());
  }

  protected void updateAllEnabled(final ActionEvent e) {
    super.updateAllEnabled(e);
    updatePair(myUseAuthorFilter, myAuthorField, e);
  }

  protected void initValues(ChangeBrowserSettings settings) {
    super.initValues(settings);
    myUseAuthorFilter.setSelected(settings.USE_USER_FILTER);
    myAuthorField.setText(settings.USER);
  }

  public void saveValues(ChangeBrowserSettings settings) {
    super.saveValues(settings);
    settings.USER = myAuthorField.getText();
    settings.USE_USER_FILTER = myUseAuthorFilter.isSelected();
  }

  protected void installCheckBoxListener(final ActionListener filterListener) {
    super.installCheckBoxListener(filterListener);
    myUseAuthorFilter.addActionListener(filterListener);
    myAuthorField.addActionListener(filterListener);
  }

  public JPanel getPanel() {
    return myPanel;
  }

  @Nullable
  public String getAuthorFilter() {
    if (myUseAuthorFilter.isSelected() && myAuthorField.getText().length() > 0) {
      return myAuthorField.getText();
    }
    else {
      return null;
    }
  }

  public JComponent getComponent() {
    return getPanel();
  }
}