package com.jetbrains.python.console;

import com.google.common.collect.ImmutableMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.console.LanguageConsoleViewImpl;
import com.intellij.execution.process.CommandLineArgumentsProvider;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.execution.runners.ConsoleExecuteActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.psi.PsiElement;
import com.intellij.util.net.NetUtils;
import com.jetbrains.django.run.Runner;
import com.jetbrains.python.PythonHelpersLocator;
import com.jetbrains.python.console.pydev.ICallback;
import com.jetbrains.python.console.pydev.InterpreterResponse;
import com.jetbrains.python.console.pydev.PydevConsoleCommunication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author oleg
 */
public class PydevConsoleRunner extends AbstractConsoleRunnerWithHistory {
  private final int[] myPorts;
  private PydevConsoleCommunication myPydevConsoleCommunication;
  public static Key<PydevConsoleCommunication> CONSOLE_KEY = new Key<PydevConsoleCommunication>("PYDEV_CONSOLE_KEY");
  private static final String PYTHON_ENV_COMMAND = "import sys; print('Python %s on %s' % (sys.version, sys.platform))\n";

  protected PydevConsoleRunner(@NotNull final Project project,
                               @NotNull final String consoleTitle,
                               @NotNull final CommandLineArgumentsProvider commandLineArgumentsProvider,
                               @Nullable final String workingDir,
                               int[] ports) {
    super(project, consoleTitle, commandLineArgumentsProvider, workingDir);
    myPorts = ports;
  }

  public static void run(@NotNull final Project project,
                         @NotNull final Sdk sdk,
                         final String consoleTitle,
                         final String projectRoot,
                         final String... statements2execute) {
    final int[] ports;
    try {
      // File "pydev/console/pydevconsole.py", line 223, in <module>
      // port, client_port = sys.argv[1:3]
      ports = NetUtils.findAvailableSocketPorts(2);
    }
    catch (IOException e) {
      ExecutionHelper.showErrors(project, Arrays.<Exception>asList(e), consoleTitle, null);
      return;
    }
    final ArrayList<String> args = new ArrayList<String>();
    args.add(sdk.getHomePath());
    final String versionString = sdk.getVersionString();
    if (versionString == null || !versionString.toLowerCase().contains("jython")) {
      args.add("-u");
    }
    args.add(FileUtil.toSystemDependentName(PythonHelpersLocator.getHelperPath("pydev/console/pydevconsole.py")));
    for (int port : ports) {
      args.add(String.valueOf(port));
    }
    final CommandLineArgumentsProvider provider = new CommandLineArgumentsProvider() {
      public String[] getArguments() {
        return args.toArray(new String[args.size()]);
      }

      public boolean passParentEnvs() {
        return false;
      }

      public Map<String, String> getAdditionalEnvs() {
        return ImmutableMap.of("PYTHONIOENCODING", "utf-8");
      }
    };

    final PydevConsoleRunner consoleRunner = new PydevConsoleRunner(project, consoleTitle, provider, projectRoot, ports);
    try {
      consoleRunner.initAndRun(statements2execute);
    }
    catch (ExecutionException e) {
      ExecutionHelper.showErrors(project, Arrays.<Exception>asList(e), consoleTitle, null);
    }
  }

  @Override
  protected LanguageConsoleViewImpl createConsoleView() {
    return new PydevLanguageConsoleView(getProject(), getConsoleTitle());
  }

  @Override
  protected Process createProcess(CommandLineArgumentsProvider provider) throws ExecutionException {
    final Process server = Runner.createProcess(getWorkingDir(), provider.getAdditionalEnvs(), provider.getArguments());
    try {
      myPydevConsoleCommunication = new PydevConsoleCommunication(getProject(), myPorts[0], server, myPorts[1]);
    }
    catch (Exception e) {
      throw new ExecutionException(e.getMessage());
    }
    return server;
  }

  @Override
  protected PyConsoleProcessHandler createProcessHandler(final Process process, final String commandLine) {
    return new PyConsoleProcessHandler(process, getConsoleView().getConsole(), commandLine,
                                       CharsetToolkit.UTF8_CHARSET);
  }

  public void initAndRun(final String... statements2execute) throws ExecutionException {
    super.initAndRun();

    // Propagate console communication to language console
    ((PydevLanguageConsoleView)getConsoleView()).setPydevConsoleCommunication(myPydevConsoleCommunication);

    // Required timeout for establishing socket connection
    try {
      Thread.sleep(3000);
    }
    catch (InterruptedException e) {
      // Ignore
    }

    // Make executed statements visible to developers
    final LanguageConsoleImpl console = getConsoleView().getConsole();
    PyConsoleHighlightingUtil.processOutput(console, PYTHON_ENV_COMMAND, ProcessOutputTypes.SYSTEM);
    getConsoleExecuteActionHandler().processLine(PYTHON_ENV_COMMAND);
    for (String statement : statements2execute) {
      PyConsoleHighlightingUtil.processOutput(console, statement + "\n", ProcessOutputTypes.SYSTEM);
      getConsoleExecuteActionHandler().processLine(statement + "\n");
    }
  }

  @Override
  protected AnAction createStopAction() {
    final AnAction generalStopAction = super.createStopAction();
    final AnAction stopAction = new AnAction() {
      @Override
      public void update(AnActionEvent e) {
        generalStopAction.update(e);
      }

      @Override
      public void actionPerformed(AnActionEvent e) {
        if (myPydevConsoleCommunication != null) {
          final AnActionEvent furtherActionEvent =
            new AnActionEvent(e.getInputEvent(), e.getDataContext(), e.getPlace(),
                              e.getPresentation(), e.getActionManager(), e.getModifiers());
          try {
            myPydevConsoleCommunication.close();
            // waiting for REPL communication before destroying process handler
            Thread.sleep(300);
          }
          catch (Exception e1) {
            // Ignore
          }
          generalStopAction.actionPerformed(furtherActionEvent);
        }
      }
    };
    stopAction.copyFrom(generalStopAction);
    return stopAction;
  }

  @NotNull
  @Override
  protected ConsoleExecuteActionHandler createConsoleExecuteActionHandler() {
    return new PydevConsoleExecuteActionHandler(getConsoleView(), getProcessHandler(), new ConsoleCommandExecutor() {
      @Override
      public boolean isWaitingForInput() {
        return myPydevConsoleCommunication.waitingForInput;
      }

      @Override
      public void execInterpreter(String s, ICallback<Object, InterpreterResponse> callback) {
        myPydevConsoleCommunication.execInterpreter(s, callback);
      }
    });
  }


  public static boolean isInPydevConsole(final PsiElement element) {
    return element instanceof PydevConsoleElement || getConsoleCommunication(element) != null;
  }

  @Nullable
  public static PydevConsoleCommunication getConsoleCommunication(final PsiElement element) {
    return element.getContainingFile().getCopyableUserData(CONSOLE_KEY);
  }
}