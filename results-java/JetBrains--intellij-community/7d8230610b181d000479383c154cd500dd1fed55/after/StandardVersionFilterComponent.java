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
package com.intellij.openapi.vcs.versionBrowser;

import com.intellij.openapi.project.Project;
import com.michaelbaranov.microba.calendar.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class StandardVersionFilterComponent<T extends ChangeBrowserSettings> implements ChangesBrowserSettingsEditor<T> {
  private JPanel myPanel;

  public JPanel getVersionNumberPanel() {
    return myVersionNumberPanel;
  }

  public JPanel getDatePanel() {
    return myDatePanel;
  }

  public Component getStandardPanel() {
    return myPanel;
  }

  private JCheckBox myUseDateBeforeFilter;
  private JTextField myNumBefore;
  private JCheckBox myUseDateAfterFilter;
  private JCheckBox myUseNumBeforeFilter;
  private JCheckBox myUseNumAfterFilter;
  private JTextField myNumAfter;
  private JPanel myDatePanel;
  private JPanel myVersionNumberPanel;
  private DatePicker myDateAfter;
  private DatePicker myDateBefore;

  private final Project myProject;
  private T mySettings;

  public StandardVersionFilterComponent(Project project) {
    myProject = project;

    myDateAfter.setDateFormat(SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM));
    myDateBefore.setDateFormat(SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM));
  }

  protected void init(final T settings) {
    installCheckBoxesListeners();
    initValues(settings);
    updateAllEnabled(null);
  }

  private void installCheckBoxesListeners() {
    final ActionListener filterListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateAllEnabled(e);
      }
    };


    installCheckBoxListener(filterListener);
  }

  protected static void updatePair(JCheckBox checkBox, JComponent textField, ActionEvent e) {
    textField.setEnabled(checkBox.isSelected());
    if (e != null && e.getSource() instanceof JCheckBox && ((JCheckBox)e.getSource()).isSelected()) {
      final Object source = e.getSource();
      if (source == checkBox && checkBox.isSelected()) {
        textField.requestFocus();
      }
    }

  }

  protected void updateAllEnabled(final ActionEvent e) {
    updatePair(myUseDateBeforeFilter, myDateBefore, e);
    updatePair(myUseDateAfterFilter, myDateAfter, e);
    updatePair(myUseNumBeforeFilter, myNumBefore, e);
    updatePair(myUseNumAfterFilter, myNumAfter, e);
  }

  protected void initValues(T settings) {
    myUseDateBeforeFilter.setSelected(settings.USE_DATE_BEFORE_FILTER);
    myUseDateAfterFilter.setSelected(settings.USE_DATE_AFTER_FILTER);
    myUseNumBeforeFilter.setSelected(settings.USE_CHANGE_BEFORE_FILTER);
    myUseNumAfterFilter.setSelected(settings.USE_CHANGE_AFTER_FILTER);

    try {
      myDateBefore.setDate(settings.getDateBefore());
      myDateAfter.setDate(settings.getDateAfter());
    }
    catch (PropertyVetoException e) {
      // TODO: handle?
    }
    myNumBefore.setText(settings.CHANGE_BEFORE);
    myNumAfter.setText(settings.CHANGE_AFTER);


  }
  public void saveValues(T settings) {
    settings.USE_DATE_BEFORE_FILTER = myUseDateBeforeFilter.isSelected();
    settings.USE_DATE_AFTER_FILTER = myUseDateAfterFilter.isSelected();
    settings.USE_CHANGE_BEFORE_FILTER = myUseNumBeforeFilter.isSelected();
    settings.USE_CHANGE_AFTER_FILTER = myUseNumAfterFilter.isSelected();

    settings.setDateBefore(myDateBefore.getDate());
    settings.setDateAfter(myDateAfter.getDate());
    settings.CHANGE_BEFORE = myNumBefore.getText();
    settings.CHANGE_AFTER = myNumAfter.getText();
  }

  protected void installCheckBoxListener(final ActionListener filterListener) {
    myUseDateBeforeFilter.addActionListener(filterListener);
    myUseDateAfterFilter.addActionListener(filterListener);
    myUseNumBeforeFilter.addActionListener(filterListener);
    myUseNumAfterFilter.addActionListener(filterListener);
  }

  public T getSettings() {
    saveValues(mySettings);
    return mySettings;
  }

  public void setSettings(T settings) {
    mySettings = settings;
    initValues(settings);
  }
}

