package com.jetbrains.python.testing.pytest;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class PyTestCommandLineState extends PythonTestCommandLineStateBase {
  private final PyTestRunConfiguration myConfiguration;

  public PyTestCommandLineState(PyTestRunConfiguration configuration, ExecutionEnvironment env) {
    super(configuration, env);
    myConfiguration = configuration;
  }

  @Override
  protected void setRunnerPath(GeneralCommandLine cmd) {
    cmd.setExePath(myConfiguration.getRunnerScriptPath());
  }

  protected void addTestRunnerParameters(GeneralCommandLine cmd) {
    ParamsGroup script_params = cmd.getParametersList().getParamsGroup(GROUP_SCRIPT);
    assert script_params != null;
    script_params.addParameters("-p", "pytest_teamcity");
    script_params.addParameters(getTestSpecs());

  }

  @Override
  protected List<String> getTestSpecs() {
    List<String> specs = new ArrayList<String>();
    specs.add(myConfiguration.getTestToRun());
    String keywords = myConfiguration.getKeywords();
    if (!StringUtil.isEmptyOrSpaces(keywords)) {
      specs.add("-k");
      specs.add(keywords);
    }
    return specs;
  }
}