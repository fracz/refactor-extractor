package com.jetbrains.python.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Leonid Shalupov
 */
public abstract class PythonCommandLineState extends CommandLineState {
  private AbstractPythonRunConfiguration myConfig;
  private final List<Filter> myFilters;

  public AbstractPythonRunConfiguration getConfig() {
    return myConfig;
  }

  public PythonCommandLineState(AbstractPythonRunConfiguration runConfiguration, ExecutionEnvironment env, List<Filter> filters) {
    super(env);
    myConfig = runConfiguration;
    myFilters = filters;
  }

  @Override
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    return execute(null);
  }

  public ExecutionResult execute(CommandLinePatcher patcher) throws ExecutionException {
    final ProcessHandler processHandler = startProcess(patcher);
    final ConsoleView console = createAndAttachConsole(getConfig().getProject(), processHandler);

    return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
  }

  @NotNull
  protected ConsoleView createAndAttachConsole(Project project, ProcessHandler processHandler) throws ExecutionException {
    final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    for (Filter filter : myFilters) {
      consoleBuilder.addFilter(filter);
    }

    final ConsoleView consoleView = consoleBuilder.getConsole();
    consoleView.attachToProcess(processHandler);
    return consoleView;
  }

  protected OSProcessHandler startProcess() throws ExecutionException {
    return startProcess(null);
  }

  protected OSProcessHandler startProcess(CommandLinePatcher patcher) throws ExecutionException {
    GeneralCommandLine commandLine = generateCommandLine();
    if (patcher != null) {
      patcher.patchCommandLine(commandLine);
    }

    final OSProcessHandler processHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  protected GeneralCommandLine generateCommandLine() {
    GeneralCommandLine commandLine = new GeneralCommandLine();

    setRunnerPath(commandLine);

    buildCommandLineParameters(commandLine);

    commandLine.setEnvParams(myConfig.getEnvs());
    commandLine.setPassParentEnvs(myConfig.isPassParentEnvs());
    return commandLine;
  }

  protected void setRunnerPath(GeneralCommandLine commandLine) {
    commandLine.setExePath(myConfig.getInterpreterPath());
  }

  protected void buildCommandLineParameters(GeneralCommandLine commandLine) {
  }
}