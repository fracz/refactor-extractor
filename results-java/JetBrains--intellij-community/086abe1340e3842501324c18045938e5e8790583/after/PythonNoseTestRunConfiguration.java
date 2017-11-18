package com.jetbrains.python.testing.nosetest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.jetbrains.python.testing.AbstractPythonTestRunConfiguration;
import com.jetbrains.python.testing.PyTestFrameworksUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * User: catherine
 */
public class PythonNoseTestRunConfiguration extends AbstractPythonTestRunConfiguration
                                          implements PythonNoseTestRunConfigurationParams {
  private String myParams = ""; // parameters for nosetests
  protected String myTitle = "Nosetests";
  public PythonNoseTestRunConfiguration(RunConfigurationModule module,
                                        ConfigurationFactory configurationFactory,
                                        String name) {
    super(module, configurationFactory, name);
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new PythonNoseTestRunConfiguration(getConfigurationModule(), getFactory(), getName());
  }

  @Override
  protected SettingsEditor<? extends RunConfiguration> createConfigurationEditor() {
    return new PythonNoseTestRunConfigurationEditor(getProject(), this);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myParams = JDOMExternalizerUtil.readField(element, "PARAMS");
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, "PARAMS", myParams);
  }

  @Override
  protected String getTitle() {
    return myTitle;
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return new PythonNoseTestCommandLineState(this, env);
  }

  public static void copyParams(PythonNoseTestRunConfigurationParams source, PythonNoseTestRunConfigurationParams target) {
    AbstractPythonTestRunConfiguration.copyParams(source.getTestRunConfigurationParams(), target.getTestRunConfigurationParams());
    target.setParams(source.getParams());
  }

  public String getParams() {
    return myParams;
  }

  public void setParams(String pattern) {
    myParams = pattern;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    if (!PyTestFrameworksUtil.isNoseTestInstalled(getProject(), getSdkHome()))
      throw new RuntimeConfigurationError("No nosetest runner found in selected interpreter");
  }
}