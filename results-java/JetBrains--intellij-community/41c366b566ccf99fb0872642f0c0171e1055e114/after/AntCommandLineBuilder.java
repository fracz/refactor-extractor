package com.intellij.lang.ant.config.execution;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.ide.macro.Macro;
import com.intellij.lang.ant.config.AntConfiguration;
import com.intellij.lang.ant.config.impl.AntBuildFileImpl;
import com.intellij.lang.ant.config.impl.AntInstallation;
import com.intellij.lang.ant.config.impl.BuildFileProperty;
import com.intellij.lang.ant.config.impl.GlobalAntConfiguration;
import com.intellij.lang.ant.resources.AntBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.rt.ant.execution.AntMain2;
import com.intellij.rt.ant.execution.IdeaAntLogger2;
import com.intellij.rt.ant.execution.IdeaInputHandler;
import com.intellij.util.PathUtil;
import com.intellij.util.config.AbstractProperty;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntCommandLineBuilder {
  private GlobalAntConfiguration myConfiguration;
  private final ArrayList<String> myTargets = new ArrayList<String>();
  private JavaParameters myCommandLine = new JavaParameters();
  private String myBuildFilePath;
  private List<BuildFileProperty> myProperties;
  private boolean myDone = false;
  @NonNls private final ArrayList<String> myExpandedProperties = new ArrayList<String>();
  @NonNls private static final String INPUT_HANDLER_PARAMETER = "-inputhandler";

  public void calculateProperties(final DataContext dataContext) throws Macro.ExecutionCancelledException {
    for (BuildFileProperty property : myProperties) {
      String value = property.getPropertyValue();
      value = myConfiguration.getMacroManager().expandMacrosInString(value, true, dataContext);
      value = myConfiguration.getMacroManager().expandMacrosInString(value, false, dataContext);
      myExpandedProperties.add("-D" + property.getPropertyName() + "=" + value);
    }
  }

  public void addTarget(String targetName) {
    myTargets.add(targetName);
  }

  public void setBuildFile(AbstractProperty.AbstractPropertyContainer container, File buildFile) throws CantRunException {
    String jdkName = AntBuildFileImpl.CUSTOM_JDK_NAME.get(container);
    ProjectJdk jdk;
    myConfiguration = GlobalAntConfiguration.INSTANCE.get(container);
    if (jdkName != null && jdkName.length() > 0) {
      jdk = myConfiguration.findJdk(jdkName);
    }
    else {
      jdkName = AntConfiguration.DEFAULT_JDK_NAME.get(container);
      if (jdkName == null || jdkName.length() == 0) {
        throw new CantRunException(AntBundle.message("project.jdk.not.specified.error.message"));
      }
      jdk = myConfiguration.findJdk(jdkName);
    }
    if (jdk == null) {
      throw new CantRunException(AntBundle.message("jdk.with.name.not.configured.error.message", jdkName));
    }
    VirtualFile homeDirectory = jdk.getHomeDirectory();
    if (homeDirectory == null) {
      throw new CantRunException(AntBundle.message("jdk.with.name.bad.configured.error.message", jdkName));
    }
    myCommandLine.setJdk(jdk);

    ParametersList vmParametersList = myCommandLine.getVMParametersList();
    vmParametersList.add("-Xmx" + AntBuildFileImpl.MAX_HEAP_SIZE.get(container) + "m");

    final AntInstallation antInstallation = AntBuildFileImpl.ANT_INSTALLATION.get(container);
    if (antInstallation == null) {
      throw new CantRunException(AntBundle.message("ant.installation.not.configured.error.message"));
    }

    final String antHome = AntInstallation.HOME_DIR.get(antInstallation.getProperties());
    vmParametersList.add("-Dant.home=" + antHome);

    String[] urls = jdk.getRootProvider().getUrls(OrderRootType.CLASSES);
    final String jdkHome = homeDirectory.getPath().replace('/', File.separatorChar);
    @NonNls final String pathToJre = jdkHome + File.separator + "jre" + File.separator;
    for (String url : urls) {
      final String path = PathUtil.toPresentableUrl(url);
      if (!path.startsWith(pathToJre)) {
        myCommandLine.getClassPath().add(path);
      }
    }

    myCommandLine.getClassPath().addAllFiles(AntBuildFileImpl.ALL_CLASS_PATH.get(container));
    final String toolsJar = jdk.getToolsPath();
    if (toolsJar != null) {
      myCommandLine.getClassPath().add(toolsJar);
    }
    PathUtilEx.addRtJar(myCommandLine.getClassPath());

    myCommandLine.setMainClass(AntMain2.class.getName());
    ParametersList programParameters = myCommandLine.getProgramParametersList();
    programParameters.add("-logger", IdeaAntLogger2.class.getName());
    programParameters.addParametersString(AntBuildFileImpl.ANT_COMMAND_LINE_PARAMETERS.get(container));
    if (!programParameters.getList().contains(INPUT_HANDLER_PARAMETER)) {
      programParameters.add(INPUT_HANDLER_PARAMETER, IdeaInputHandler.class.getName());
    }

    myProperties = AntBuildFileImpl.ANT_PROPERTIES.get(container);

    myBuildFilePath = buildFile.getAbsolutePath();
    myCommandLine.setWorkingDirectory(buildFile.getParent());
  }

  public JavaParameters getCommandLine() {
    if (myDone) return myCommandLine;
    ParametersList programParameters = myCommandLine.getProgramParametersList();
    programParameters.addAll(myExpandedProperties);
    programParameters.add("-buildfile", myBuildFilePath);
    programParameters.addAll(myTargets);
    myDone = true;
    return myCommandLine;
  }

  public void addTargets(String[] targets) {
    myTargets.addAll(Arrays.asList(targets));
  }

  public String[] getTargets() {
    return myTargets.toArray(new String[myTargets.size()]);
  }
}