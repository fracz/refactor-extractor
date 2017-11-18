package com.jetbrains.python.testing.pytest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.WriteExternalException;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author yole
 */
public class PyTestRunConfiguration extends AbstractPythonRunConfiguration {
  private String myTestToRun = "";
  private String myKeywords = "";

  private static final String TEST_TO_RUN_FIELD = "testToRun";
  private static final String KEYWORDS_FIELD = "keywords";

  public PyTestRunConfiguration(final String name, final RunConfigurationModule module, final ConfigurationFactory factory) {
    super(name, module, factory);
  }

  protected ModuleBasedConfiguration createInstance() {
    return new PyTestRunConfiguration(getName(), getConfigurationModule(), getFactory());
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new PyTestConfigurationEditor(getProject(), this);
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    return new PyTestCommandLineState(this, env);
  }

  public String getTestToRun() {
    return myTestToRun;
  }

  public void setTestToRun(String testToRun) {
    myTestToRun = testToRun;
  }

  public String getKeywords() {
    return myKeywords;
  }

  public void setKeywords(String keywords) {
    myKeywords = keywords;
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myTestToRun = JDOMExternalizerUtil.readField(element, TEST_TO_RUN_FIELD);
    myKeywords = JDOMExternalizerUtil.readField(element, KEYWORDS_FIELD);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, TEST_TO_RUN_FIELD, myTestToRun);
    JDOMExternalizerUtil.writeField(element, KEYWORDS_FIELD, myKeywords);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    final String path = getRunnerScriptPath();
    if (path == null || !new File(path).exists()) {
      throw new RuntimeConfigurationError("No py.test runner found in selected interpreter");
    }
  }

  @Nullable
  public String getRunnerScriptPath() {
    return findPyTestRunner(getSdkHome());
  }

  @Nullable
  public static String findPyTestRunner(final String sdkHome) {
    // HACK -- current getSdkHome logic is somehow broken, because interpreter and its home are not linked
    File bin_path = new File(sdkHome); // this is actually a binary path
    final String PY_TEST = "py.test" + (SystemInfo.isWindows ? ".exe" : "");
    // poke around and see if we got something like runner
    File bin_dir = bin_path.getParentFile();
    if (bin_dir == null) return null;
    File runner = new File(bin_dir, PY_TEST);
    if (runner.exists()) return runner.getPath();
    runner = new File(new File(bin_dir, "scripts"), PY_TEST);
    if (runner.exists()) return runner.getPath();
    runner = new File(new File(bin_dir.getParentFile(), "scripts"), PY_TEST);
    if (runner.exists()) return runner.getPath();
    runner = new File(new File(bin_dir.getParentFile(), "local"), PY_TEST);
    if (runner.exists()) return runner.getPath();
    runner = new File(new File (new File(bin_dir.getParentFile(), "local"), "bin"), PY_TEST);
    if (runner.exists()) return runner.getPath();
    return null;
  }

  @Override
  public String suggestedName() {
    return "py.test in " + getName();
  }
}