package com.jetbrains.python.testing.pytest;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.run.AbstractPyCommonOptionsForm;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.PyCommonOptionsFormFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;

/**
 * @author yole
 */
public class PyTestConfigurationEditor extends SettingsEditor<PyTestRunConfiguration> {
  private JPanel myMainPanel;
  private JPanel myCommonOptionsPlaceholder;
  private JTextField myKeywordsTextField;
  private TextFieldWithBrowseButton myTestScriptTextField;
  private JTextField myParamsTextField;
  private JLabel myKeyLabel;
  private JLabel myTargetLabel;
  private JLabel myParamLabel;
  private final AbstractPyCommonOptionsForm myCommonOptionsForm;
  private final Project myProject;

  public PyTestConfigurationEditor(final Project project, PyTestRunConfiguration configuration) {
    myProject = project;
    myCommonOptionsForm = PyCommonOptionsFormFactory.getInstance().createForm(configuration);
    myCommonOptionsPlaceholder.add(myCommonOptionsForm.getMainPanel());

    String title = PyBundle.message("runcfg.unittest.dlg.select.script.path");
    final FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory
      .createSingleFileOrFolderDescriptor();
    fileChooserDescriptor.setTitle(title);
    myTestScriptTextField.addBrowseFolderListener(title, null, myProject, fileChooserDescriptor);

    myTargetLabel.setLabelFor(myTestScriptTextField);
  }

  protected void resetEditorFrom(PyTestRunConfiguration s) {
    AbstractPythonRunConfiguration.copyParams(s, myCommonOptionsForm);
    myKeywordsTextField.setText(s.getKeywords());
    myTestScriptTextField.setText(toSystemIndependentName(s.getTestToRun()));
    myParamsTextField.setText(s.getParams());
  }

  protected void applyEditorTo(PyTestRunConfiguration s) throws ConfigurationException {
    AbstractPythonRunConfiguration.copyParams(myCommonOptionsForm, s);
    s.setTestToRun(toSystemIndependentName(myTestScriptTextField.getText().trim()));
    s.setKeywords(myKeywordsTextField.getText().trim());
    s.setParams(myParamsTextField.getText().trim());
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void disposeEditor() {
  }
}