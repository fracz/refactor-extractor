/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.theoryinpractice.testng.ui.actions;

import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.Filter;
import com.intellij.execution.testframework.JavaAwareFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;
import com.theoryinpractice.testng.configuration.TestNGRunnableState;
import com.theoryinpractice.testng.model.TestNGConsoleProperties;
import com.theoryinpractice.testng.model.TestProxy;
import com.theoryinpractice.testng.ui.TestNGResults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RerunFailedTestsAction extends AnAction {
  private static final Logger LOG = Logger.getInstance("#com.intellij.execution.junit2.ui.actions.RerunFailedTestsAction");
  private TestNGResults myModel;
  private final TestNGConsoleProperties myConsoleProperties;
  private final RunnerSettings myRunnerSettings;
  private final ConfigurationPerRunnerSettings myConfigurationPerRunnerSettings;

  public RerunFailedTestsAction(final TestNGConsoleProperties consoleProperties,
                                final RunnerSettings runnerSettings,
                                final ConfigurationPerRunnerSettings configurationSettings) {
    super(ExecutionBundle.message("rerun.failed.tests.action.name"), ExecutionBundle.message("rerun.failed.tests.action.description"),
          IconLoader.getIcon("/runConfigurations/rerunFailedTests.png"));
    myConsoleProperties = consoleProperties;
    myRunnerSettings = runnerSettings;
    myConfigurationPerRunnerSettings = configurationSettings;
  }

  public void setResults(TestNGResults model) {
    myModel = model;
  }

  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(isActive(e));
  }

  private boolean isActive(AnActionEvent e) {
    DataContext dataContext = e.getDataContext();
    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) return false;
    if (myModel == null || myModel.getRoot() == null) return false;
    List<AbstractTestProxy> failed = getFailedTests();
    return !failed.isEmpty();
  }

  @NotNull
  private List<AbstractTestProxy> getFailedTests() {
    List<TestProxy> myAllTests = myModel.getRoot().getAllTests();
    return Filter.DEFECTIVE_LEAF.and(JavaAwareFilter.METHOD(myConsoleProperties.getProject())).select(myAllTests);
  }

  public void actionPerformed(AnActionEvent e) {
    final List<AbstractTestProxy> failed = getFailedTests();

    final DataContext dataContext = e.getDataContext();
    final TestNGConfiguration configuration = myConsoleProperties.getConfiguration();
    boolean isDebug = myConsoleProperties.isDebug();
    try {
      final RunProfile profile = new ModuleRunProfile() {
        public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
          return new TestNGRunnableState(env, configuration) {
            protected void fillTestObjects(final Map<PsiClass, Collection<PsiMethod>> classes, final Project project)
              throws CantRunException {
              for (AbstractTestProxy proxy : failed) {
                final Location location = proxy.getLocation(project);
                if (location != null) {
                  final PsiElement element = location.getPsiElement();
                  if (element instanceof PsiMethod) {
                    final PsiMethod psiMethod = (PsiMethod)element;
                    final PsiClass psiClass = psiMethod.getContainingClass();
                    Collection<PsiMethod> psiMethods = classes.get(psiClass);
                    if (psiMethods == null) {
                      psiMethods = new ArrayList<PsiMethod>();
                      classes.put(psiClass, psiMethods);
                    }
                    psiMethods.add(psiMethod);
                  }
                }
              }
            }
          };
        }

        public String getName() {
          return ExecutionBundle.message("rerun.failed.tests.action.name");
        }

        public void checkConfiguration() throws RuntimeConfigurationException {

        }

        public Module[] getModules() {
          return Module.EMPTY_ARRAY;
        }
      };

      final Executor executor = isDebug ? DefaultDebugExecutor.getDebugExecutorInstance() : DefaultRunExecutor.getRunExecutorInstance();
      final ProgramRunner runner = RunnerRegistry.getInstance().getRunner(executor.getId(), profile);
      LOG.assertTrue(runner != null);
      runner.execute(executor, new ExecutionEnvironment(profile, myRunnerSettings, myConfigurationPerRunnerSettings, dataContext));
    }
    catch (ExecutionException e1) {
      LOG.error(e1);
    }
  }
}