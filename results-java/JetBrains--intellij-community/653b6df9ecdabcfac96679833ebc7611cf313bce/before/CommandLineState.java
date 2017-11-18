/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
package com.intellij.execution.configurations;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public abstract class CommandLineState implements RunnableState {
  private static final Logger LOG = Logger.getInstance("#com.intellij.execution.configurations.CommandLineState");
  private TextConsoleBuilder myConsoleBuilder;
  private Module[] myModulesToCompile = Module.EMPTY_ARRAY;

  private RunnerSettings myRunnerSettings;
  private ConfigurationPerRunnerSettings myConfigurationSettings;

  protected CommandLineState(RunnerSettings runnerSettings, ConfigurationPerRunnerSettings configurationSettings) {
    myRunnerSettings = runnerSettings;
    myConfigurationSettings = configurationSettings;
  }

  public RunnerSettings getRunnerSettings() {
    return myRunnerSettings;
  }

  public ConfigurationPerRunnerSettings getConfigurationSettings() {
    return myConfigurationSettings;
  }

  public ExecutionResult execute(@NotNull ProgramRunner runner) throws ExecutionException {
    final ProcessHandler processHandler = startProcess();
    final TextConsoleBuilder builder = getConsoleBuilder();
    final ConsoleView console = builder != null ? builder.getConsole() : null;
    if (console != null) {
      console.attachToProcess(processHandler);
    }
    return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
  }

  protected abstract OSProcessHandler startProcess() throws ExecutionException;

  protected AnAction[] createActions(final ConsoleView console, final ProcessHandler processHandler) {
    if (console == null || !console.canPause()) {
      return new AnAction[0];
    }
    return new AnAction[]{new PauseOutputAction(console, processHandler)};
  }

  protected TextConsoleBuilder getConsoleBuilder() {
    return myConsoleBuilder;
  }

  public void setConsoleBuilder(final TextConsoleBuilder consoleBuilder) {
    myConsoleBuilder = consoleBuilder;
  }

  public Module[] getModulesToCompile() {
    return myModulesToCompile;
  }

  public void setModulesToCompile(Module[] modulesToCompile) {
    if (modulesToCompile == null) modulesToCompile = Module.EMPTY_ARRAY;
    for (final Module module : modulesToCompile) {
      LOG.assertTrue(module != null);
    }
    myModulesToCompile = modulesToCompile;
  }

  public void setModuleToCompile(final Module module) {
    if (module == null) return;
    setModulesToCompile(new Module[]{module});
  }

  protected static class PauseOutputAction extends ToggleAction {
    private final ConsoleView myConsole;
    private final ProcessHandler myProcessHandler;

    public PauseOutputAction(final ConsoleView console, final ProcessHandler processHandler) {
      super(ExecutionBundle.message("run.configuration.pause.output.action.name"), null, IconLoader.getIcon("/actions/pause.png"));
      myConsole = console;
      myProcessHandler = processHandler;
    }

    public boolean isSelected(final AnActionEvent event) {
      return myConsole.isOutputPaused();
    }

    public void setSelected(final AnActionEvent event, final boolean flag) {
      myConsole.setOutputPaused(flag);
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          update(event);
        }
      });
    }

    public void update(final AnActionEvent event) {
      super.update(event);
      final Presentation presentation = event.getPresentation();
      final boolean isRunning = myProcessHandler != null && !myProcessHandler.isProcessTerminated();
      if (isRunning) {
        presentation.setEnabled(true);
      }
      else {
        if (!myConsole.canPause()) {
          presentation.setEnabled(false);
          return;
        }
        if (!myConsole.hasDeferredOutput()) {
          presentation.setEnabled(false);
        }
        else {
          presentation.setEnabled(true);
          myConsole.performWhenNoDeferredOutput(new Runnable() {
            public void run() {
              update(event);
            }
          });
        }
      }
    }
  }
}