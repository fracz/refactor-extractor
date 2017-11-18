package com.jetbrains.python.testing;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.PythonCommandLineState;
import com.jetbrains.python.run.PythonTracebackFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
public abstract class PythonTestCommandLineStateBase extends PythonCommandLineState {
  protected final AbstractPythonRunConfiguration myConfiguration;

  public PythonTestCommandLineStateBase(AbstractPythonRunConfiguration configuration, ExecutionEnvironment env) {
    super(configuration, env, Collections.<Filter>emptyList());
    myConfiguration = configuration;
  }

  @NotNull
  protected ConsoleView createAndAttachConsole(Project project, ProcessHandler processHandler, Executor executor)
    throws ExecutionException {
    final ConsoleView consoleView = SMTestRunnerConnectionUtil.attachRunner("PythonUnitTestRunner", processHandler, this, myConfiguration,
                                                                            executor);
    consoleView.addMessageFilter(new PythonTracebackFilter(project, myConfiguration.getWorkingDirectory()));
    return consoleView;
  }

  public GeneralCommandLine generateCommandLine() throws ExecutionException {
    GeneralCommandLine cmd = super.generateCommandLine();

    if (!StringUtil.isEmptyOrSpaces(myConfiguration.getWorkingDirectory())) {
      cmd.setWorkDirectory(myConfiguration.getWorkingDirectory());
    }

    ParamsGroup exe_options = cmd.getParametersList().getParamsGroup(GROUP_EXE_OPTIONS);
    assert exe_options != null;
    exe_options.addParametersString(myConfiguration.getInterpreterOptions());
    addTestRunnerParameters(cmd);

    return cmd;
  }

  protected abstract void addTestRunnerParameters(GeneralCommandLine cmd);
  protected abstract List<String> getTestSpecs();
}