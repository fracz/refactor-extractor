package com.intellij.execution.impl;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.RunnerInfo;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RunDialog extends DialogWrapper {
  private final RunnerInfo myRunnerInfo;
  private final Project myProject;
  private RunConfigurable myConfigurable;
  private JComponent myCenterPanel;
  @NonNls public static String HELP_ID = "reference.dialogs.rundebug";

  public RunDialog(final Project project, final RunnerInfo runnerInfo) {
    super(project, true);
    myProject = project;
    myRunnerInfo = runnerInfo;

    final String title = runnerInfo.getId();
    setTitle(title);

    setOKButtonText(runnerInfo.getStartActionText());
    setOKButtonIcon(runnerInfo.getIcon());

    myConfigurable = new RunConfigurable(project, this);
    init();
    myConfigurable.reset();
  }

  protected Action[] createActions(){
    return new Action[]{getOKAction(),getCancelAction(),new ApplyAction(),getHelpAction()};
  }

  protected void doHelpAction() {
    HelpManager.getInstance().invokeHelp(HELP_ID);
  }

  protected String getDimensionServiceKey(){
    return "#com.intellij.execution.impl.RunDialog";
  }

  public JComponent getPreferredFocusedComponent() {
    return IdeFocusTraversalPolicy.getPreferredFocusedComponent(myCenterPanel);
  }

  protected void doOKAction(){
    try{
      myConfigurable.apply();
    }
    catch(ConfigurationException e){
      Messages.showMessageDialog(myProject, e.getMessage(), ExecutionBundle.message("invalid.data.dialog.title"), Messages.getErrorIcon());
      return;
    }
    super.doOKAction();
  }

  protected JComponent createCenterPanel() {
    myCenterPanel = myConfigurable.createComponent();
    return myCenterPanel;
  }

  public void setOKActionEnabled(final boolean isEnabled){
    super.setOKActionEnabled(isEnabled);
  }

  protected void dispose() {
    myConfigurable.disposeUIResources();
    super.dispose();
  }

  public static boolean editConfiguration(final Project project, final RunnerAndConfigurationSettingsImpl configuration, final String title) {
    final SingleConfigurationConfigurable<RunConfiguration> configurable = SingleConfigurationConfigurable.editSettings(configuration);
    final SingleConfigurableEditor dialog = new SingleConfigurableEditor(project, configurable);
    dialog.setTitle(title);
    dialog.show();
    return dialog.isOK();
  }

  private class ApplyAction extends AbstractAction {
    public ApplyAction() {
      super(ExecutionBundle.message("apply.action.name"));
    }

    public void actionPerformed(final ActionEvent event) {
      try{
        myConfigurable.apply();
      }
      catch(ConfigurationException e){
        Messages.showMessageDialog(myProject, e.getMessage(), ExecutionBundle.message("invalid.data.dialog.title"), Messages.getErrorIcon());
      }
    }
  }

  public RunnerInfo getRunnerInfo() {
    return myRunnerInfo;
  }
}