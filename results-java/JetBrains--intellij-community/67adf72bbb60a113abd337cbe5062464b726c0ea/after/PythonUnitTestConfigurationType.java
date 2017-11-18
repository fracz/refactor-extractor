package com.jetbrains.python.testing.unittest;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.jetbrains.python.PyBundle;

import javax.swing.*;

/**
 * @author Leonid Shalupov
 */
public class PythonUnitTestConfigurationType extends ConfigurationTypeBase {

  public static PythonUnitTestConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(PythonUnitTestConfigurationType.class);
  }

  public PythonUnitTestConfigurationType() {
    super("PythonUnitTestConfigurationType",
          PyBundle.message("runcfg.unittest.display_name"),
          PyBundle.message("runcfg.unittest.description"),
          ICON);
    addFactory(new PythonUnitTestConfigurationFactory(this));
  }

  private final static Icon ICON = IconLoader.getIcon(
    "/com/jetbrains/python/icons/python.png");

  private static class PythonUnitTestConfigurationFactory extends ConfigurationFactory {
    protected PythonUnitTestConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PythonUnitTestRunConfiguration(new RunConfigurationModule(project), this, "");
    }
  }
}