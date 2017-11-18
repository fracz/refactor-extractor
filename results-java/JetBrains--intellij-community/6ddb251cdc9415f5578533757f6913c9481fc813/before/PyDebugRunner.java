package com.jetbrains.python.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.python.PythonHelpersLocator;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.run.PythonCommandLineState;
import com.jetbrains.python.run.PythonRunConfiguration;
import com.jetbrains.python.sdk.PythonSdkFlavor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author yole
 */
public class PyDebugRunner extends GenericProgramRunner {
  public static final String PY_DEBUG_RUNNER = "PyDebugRunner";

  @NotNull
  public String getRunnerId() {
    return PY_DEBUG_RUNNER;
  }

  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof AbstractPythonRunConfiguration;
  }

  protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final ServerSocket serverSocket;
    try {
      //noinspection SocketOpenedButNotSafelyClosed
      serverSocket = new ServerSocket(0);
    }
    catch (IOException e) {
      throw new ExecutionException("Failed to find free socket port", e);
    }

    final PythonCommandLineState pyState = (PythonCommandLineState)state;
    final int serverLocalPort = serverSocket.getLocalPort();
    RunProfile profile = env.getRunProfile();
    final ExecutionResult result = pyState.execute(executor, createCommandLinePatchers(pyState, profile, serverLocalPort));

    final XDebugSession session = XDebuggerManager.getInstance(project).
      startSession(this, env, contentToReuse, new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          return new PyDebugProcess(session, serverSocket, result.getExecutionConsole(), result.getProcessHandler());
        }
      });
    return session.getRunContentDescriptor();
  }

  private CommandLinePatcher createRunConfigPatcher(RunProfileState state, RunProfile profile) {
    CommandLinePatcher runConfigPatcher = null;
    if (state instanceof PythonCommandLineState && profile instanceof PythonRunConfiguration) {
      runConfigPatcher = (PythonRunConfiguration)profile;
    }
    return runConfigPatcher;
  }

  public CommandLinePatcher[] createCommandLinePatchers(final PythonCommandLineState state, RunProfile profile, final int serverLocalPort) {
    return new CommandLinePatcher[]{createDebugServerPatcher(state, serverLocalPort), createRunConfigPatcher(state, profile)};
  }

  private CommandLinePatcher createDebugServerPatcher(final PythonCommandLineState pyState, final int serverLocalPort) {
    return new CommandLinePatcher() {
      public void patchCommandLine(GeneralCommandLine commandLine) {

        final String[] debugger_args = new String[]{
          PythonHelpersLocator.getHelperPath("pydev/pydevd.py"),
          "--client", "127.0.0.1",
          "--port", String.valueOf(serverLocalPort),
          "--file"
        };
        // script name is the last parameter; all other params are for python interpreter; insert just before name
        final ParametersList parameters_list = commandLine.getParametersList();

        ParamsGroup debug_params = parameters_list.getParamsGroup(PythonCommandLineState.GROUP_DEBUGGER);
        assert debug_params != null;
        ParamsGroup exe_params = parameters_list.getParamsGroup(PythonCommandLineState.GROUP_EXE_OPTIONS);
        assert exe_params != null;

        final PythonSdkFlavor flavor = pyState.getConfig().getSdkFlavor();
        if (flavor != null) {
          for (String option : flavor.getExtraDebugOptions()) exe_params.addParameter(option);
        }
        for (String s : debugger_args) debug_params.addParameter(s);
      }
    };
  }
}