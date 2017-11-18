package com.intellij.execution.application;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.DefaultCoverageFileProvider;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.junit.RefactoringListeners;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationConfiguration extends CoverageEnabledConfiguration implements RunJavaConfiguration, SingleClassConfiguration, RefactoringListenerProvider {
  private static final Logger LOG = Logger.getInstance("com.intellij.execution.application.ApplicationConfiguration");

  public String MAIN_CLASS_NAME;
  public String VM_PARAMETERS;
  public String PROGRAM_PARAMETERS;
  public String WORKING_DIRECTORY;
  public boolean ALTERNATIVE_JRE_PATH_ENABLED;
  public String ALTERNATIVE_JRE_PATH;
  public boolean ENABLE_SWING_INSPECTOR;

  public String ENV_VARIABLES;
  private Map<String,String> myEnvs = new LinkedHashMap<String, String>();
  public boolean PASS_PARENT_ENVS = true;

  public ApplicationConfiguration(final String name, final Project project, ApplicationConfigurationType applicationConfigurationType) {
    super(name, new JavaRunConfigurationModule(project, true), applicationConfigurationType.getConfigurationFactories()[0]);
  }

  public void setMainClass(final PsiClass psiClass) {
    setMainClassName(JavaExecutionUtil.getRuntimeQualifiedName(psiClass));
    setModule(JavaExecutionUtil.findModule(psiClass));
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final JavaCommandLineState state = new MyJavaCommandLineState(env);
    state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
    return state;
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    SettingsEditorGroup<ApplicationConfiguration> group = new SettingsEditorGroup<ApplicationConfiguration>();
    group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new ApplicationConfigurable2(getProject()));
    group.addEditor(ExecutionBundle.message("coverage.tab.title"), new ApplicationCoverageConfigurable(getProject()));
    group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel());
    return group;
  }

  @Nullable
  public String getGeneratedName() {
    if (MAIN_CLASS_NAME == null) {
      return null;
    }
    return JavaExecutionUtil.getPresentableClassName(MAIN_CLASS_NAME, getConfigurationModule());
  }

  public void setGeneratedName() {
    setName(getGeneratedName());
  }

  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    return RefactoringListeners.
      getClassOrPackageListener(element, new RefactoringListeners.SingleClassConfigurationAccessor(this));
  }

  @Nullable
  public PsiClass getMainClass() {
    return getConfigurationModule().findClass(MAIN_CLASS_NAME);
  }

  public boolean isGeneratedName() {
    if (MAIN_CLASS_NAME == null || MAIN_CLASS_NAME.length() == 0) {
      return JavaExecutionUtil.isNewName(getName());
    }
    return Comparing.equal(getName(), getGeneratedName());
  }

  public String suggestedName() {
    return ExecutionUtil.shortenName(JavaExecutionUtil.getShortClassName(MAIN_CLASS_NAME), 6) + ".main()";
  }

  public void setMainClassName(final String qualifiedName) {
    final boolean generatedName = isGeneratedName();
    MAIN_CLASS_NAME = qualifiedName;
    if (generatedName) setGeneratedName();
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    if (ALTERNATIVE_JRE_PATH_ENABLED){
      if (ALTERNATIVE_JRE_PATH == null ||
          ALTERNATIVE_JRE_PATH.length() == 0 ||
          !JavaSdkImpl.checkForJre(ALTERNATIVE_JRE_PATH)){
        throw new RuntimeConfigurationWarning(ExecutionBundle.message("jre.path.is.not.valid.jre.home.error.mesage", ALTERNATIVE_JRE_PATH));
      }
    }
    final JavaRunConfigurationModule configurationModule = getConfigurationModule();
    final PsiClass psiClass = configurationModule.checkModuleAndClassName(MAIN_CLASS_NAME, ExecutionBundle.message("no.main.class.specified.error.text"));
    if (!PsiMethodUtil.hasMainMethod(psiClass)) {
      throw new RuntimeConfigurationWarning(ExecutionBundle.message("main.method.not.found.in.class.error.message", MAIN_CLASS_NAME));
    }
  }

  public void setProperty(final int property, final String value) {
    switch (property) {
      case PROGRAM_PARAMETERS_PROPERTY:
        PROGRAM_PARAMETERS = value;
        break;
      case VM_PARAMETERS_PROPERTY:
        VM_PARAMETERS = value;
        break;
      case WORKING_DIRECTORY_PROPERTY:
        WORKING_DIRECTORY = ExternalizablePath.urlValue(value);
        break;
      default:
        throw new RuntimeException("Unknown property: " + property);
    }
  }

  public String getProperty(final int property) {
    switch (property) {
      case PROGRAM_PARAMETERS_PROPERTY:
        return PROGRAM_PARAMETERS;
      case VM_PARAMETERS_PROPERTY:
        return VM_PARAMETERS;
      case WORKING_DIRECTORY_PROPERTY:
        return getWorkingDirectory();
      default:
        throw new RuntimeException("Unknown property: " + property);
    }
  }

  private String getWorkingDirectory() {
    return ExternalizablePath.localPathValue(WORKING_DIRECTORY);
  }

  public Collection<Module> getValidModules() {
    return JavaRunConfigurationModule.getModulesForClass(getProject(), MAIN_CLASS_NAME);
  }

  protected ModuleBasedConfiguration createInstance() {
    return new ApplicationConfiguration(getName(), getProject(), ApplicationConfigurationType.getInstance());
  }

  protected boolean isMergeDataByDefault() {
    return true;
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    DefaultJDOMExternalizer.readExternal(this, element);
    readModule(element);
    EnvironmentVariablesComponent.readExternal(element, getEnvs());
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    DefaultJDOMExternalizer.writeExternal(this, element);
    writeModule(element);
    EnvironmentVariablesComponent.writeExternal(element, getEnvs());
  }

  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(final Map<String, String> envs) {
    this.myEnvs = envs;
  }

  private class MyJavaCommandLineState extends JavaCommandLineState {
    private CoverageSuite myCurrentCoverageSuite;

    public MyJavaCommandLineState(final ExecutionEnvironment environment) {
      super(environment);
    }

    protected JavaParameters createJavaParameters() throws ExecutionException {
      final JavaParameters params = new JavaParameters();
      params.setupEnvs(getEnvs(), PASS_PARENT_ENVS);
      final int classPathType = JavaParametersUtil.getClasspathType(getConfigurationModule(), MAIN_CLASS_NAME, false);
      JavaParametersUtil.configureModule(getConfigurationModule(), params, classPathType, ALTERNATIVE_JRE_PATH_ENABLED ? ALTERNATIVE_JRE_PATH : null);
      JavaParametersUtil.configureConfiguration(params, ApplicationConfiguration.this);

      params.setMainClass(MAIN_CLASS_NAME);
      for(RunConfigurationExtension ext: Extensions.getExtensions(RunConfigurationExtension.EP_NAME)) {
        if (ext instanceof JavaRunConfigurationExtension) {
          ((JavaRunConfigurationExtension)ext).updateJavaParameters(ApplicationConfiguration.this, params);
        }
      }

      if (!(getRunnerSettings().getData() instanceof DebuggingRunnerData) && isCoverageEnabled()) {
        final String coverageFileName = getCoverageFilePath();
        final long lastCoverageTime = System.currentTimeMillis();
        String name = getName();
        if (name == null) name = getGeneratedName();
        final CoverageDataManager coverageDataManager = CoverageDataManager.getInstance(getProject());
        myCurrentCoverageSuite = coverageDataManager.addCoverageSuite(
          name,
          new DefaultCoverageFileProvider(coverageFileName),
          getCoveragePatterns(),
          lastCoverageTime,
          getSuiteToMergeWith(), getCoverageRunner());
        appendCoverageArgument(params);
      }

      return params;
    }

    @Override
    protected OSProcessHandler startProcess() throws ExecutionException {
      final OSProcessHandler handler = super.startProcess();
      for(RunConfigurationExtension ext: Extensions.getExtensions(RunConfigurationExtension.EP_NAME)) {
        ext.handleStartProcess(ApplicationConfiguration.this, handler);
      }

      handler.addProcessListener(new ProcessAdapter() {
        public void processTerminated(final ProcessEvent event) {
          final CoverageDataManager coverageDataManager = CoverageDataManager.getInstance(getProject());
          if (myCurrentCoverageSuite != null) {
            coverageDataManager.coverageGathered(myCurrentCoverageSuite);
          }
        }
      });

      return handler;
    }
  }
}