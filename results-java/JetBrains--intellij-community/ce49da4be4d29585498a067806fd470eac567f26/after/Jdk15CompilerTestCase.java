package com.intellij.compiler;

import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerOptions;

import java.io.File;

/**
 * @author Eugene Zhuravlev
 *         Date: Feb 26, 2004
 */
public abstract class Jdk15CompilerTestCase extends CompilerTestCase{
  private boolean myUseExternalCompiler;
  private String myAdditionalOptions;

  protected Jdk15CompilerTestCase(String groupName) {
    super(groupName);
  }

  @Override
  protected void setUp() throws Exception {
    final String compilerHome = CompilerConfigurationImpl.getTestsExternalCompilerHome();
    if (compilerHome == null || compilerHome.length() == 0) {
      throw new Exception("Property \"" + CompilerConfigurationImpl.TESTS_EXTERNAL_COMPILER_HOME_PROPERTY_NAME + "\" must be specified in order to run JDK 1.5 compiler tests");
    }
    if (!new File(compilerHome).exists()) {
      throw new Exception("The home directory for tests external compiler does not exist: " + compilerHome);
    }

    super.setUp();

    final JpsJavaCompilerOptions javacSettings = JavacConfiguration.getOptions(myProject, JavacConfiguration.class);
    myUseExternalCompiler = javacSettings.isTestsUseExternalCompiler();
    myAdditionalOptions = javacSettings.ADDITIONAL_OPTIONS_STRING;

    javacSettings.setTestsUseExternalCompiler(true);
    javacSettings.ADDITIONAL_OPTIONS_STRING = "-source 1.5";
  }

  @Override
  protected void tearDown() throws Exception {
    final JpsJavaCompilerOptions javacSettings = JavacConfiguration.getOptions(myProject, JavacConfiguration.class);
    javacSettings.setTestsUseExternalCompiler(myUseExternalCompiler);
    javacSettings.ADDITIONAL_OPTIONS_STRING = myAdditionalOptions;
    super.tearDown();
  }
}