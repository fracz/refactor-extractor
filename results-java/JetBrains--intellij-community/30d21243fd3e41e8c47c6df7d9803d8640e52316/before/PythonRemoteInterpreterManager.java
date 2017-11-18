package com.jetbrains.python.remote;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.python.PythonHelpersLocator;
import com.jetbrains.python.debugger.remote.PyPathMappingSettings;
import com.jetbrains.python.remote.ui.PyRemoteProjectSettings;
import com.jetbrains.python.sdk.PySkeletonGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author traff
 */
public abstract class PythonRemoteInterpreterManager {
  public final static ExtensionPointName<PythonRemoteInterpreterManager> EP_NAME =
    ExtensionPointName.create("Pythonid.remoteInterpreterManager");
  public static final String WEB_DEPLOYMENT_PLUGIN_IS_DISABLED =
    "Remote interpreter can't be executed. Please enable WebDeployment plugin.";

  public abstract ProcessHandler startRemoteProcess(@Nullable Project project,
                                                    @NotNull PythonRemoteSdkAdditionalData data,
                                                    @NotNull GeneralCommandLine commandLine,
                                                    @Nullable
                                                    PyPathMappingSettings mappingSettings)
    throws PyRemoteInterpreterException;

  public abstract ProcessHandler startRemoteProcessWithPid(@Nullable Project project,
                                                           @NotNull PythonRemoteSdkAdditionalData data,
                                                           @NotNull GeneralCommandLine commandLine,
                                                           @Nullable
                                                           PyPathMappingSettings mappingSettings)
    throws PyRemoteInterpreterException;

  @Nullable
  public abstract Sdk addRemoteSdk(Project project, Sdk[] existingSdks);

  public abstract ProcessOutput runRemoteProcess(@Nullable Project project,
                                                 PythonRemoteSdkAdditionalData data,
                                                 String[] command,
                                                 boolean askForSudo)
    throws PyRemoteInterpreterException;

  @NotNull
  public abstract PyRemoteSshProcess createRemoteProcess(@Nullable Project project,
                                                         @NotNull PythonRemoteSdkAdditionalData data,
                                                         @NotNull GeneralCommandLine commandLine, boolean allocatePty)
    throws PyRemoteInterpreterException;

  public abstract boolean editSdk(@NotNull Project project, @NotNull SdkModificator sdkModificator, Sdk[] existingSdks);

  public abstract PySkeletonGenerator createRemoteSkeletonGenerator(@Nullable Project project, String path, @NotNull Sdk sdk);

  public abstract boolean ensureCanWrite(@Nullable Object projectOrComponent, PythonRemoteSdkAdditionalData data, String path);

  @Nullable
  public abstract PyRemoteProjectSettings showRemoteProjectSettingsDialog(VirtualFile baseDir, PythonRemoteSdkAdditionalData data);

  public abstract void createDeployment(Project project,
                                        VirtualFile projectDir,
                                        PyRemoteProjectSettings settings,
                                        PythonRemoteSdkAdditionalData data);

  public abstract void copyFromRemote(Project project,
                                      PythonRemoteSdkAdditionalData data,
                                      List<PyPathMappingSettings.PyPathMapping> mappings);

  @Nullable
  public static PythonRemoteInterpreterManager getInstance() {
    if (EP_NAME.getExtensions().length > 0) {
      return EP_NAME.getExtensions()[0];
    }
    else {
      return null;
    }
  }

  public static void addUnbuffered(ParamsGroup exeGroup) {
    for (String param : exeGroup.getParametersList().getParameters()) {
      if ("-u".equals(param)) {
        return;
      }
    }
    exeGroup.addParameter("-u");
  }

  public static String toSystemDependent(String path, boolean isWin) {
    char separator = isWin ? '\\' : '/';
    return FileUtil.toSystemIndependentName(path).replace('/', separator);
  }

  public static void addHelpersMapping(@NotNull PythonRemoteSdkAdditionalData data, @Nullable PyPathMappingSettings newMappingSettings) {
    if (newMappingSettings == null) {
      newMappingSettings = new PyPathMappingSettings();
    }
    newMappingSettings.addMapping(PythonHelpersLocator.getHelpersRoot().getPath(), data.getPyCharmHelpersPath());
  }

  public static class PyRemoteInterpreterExecutionException extends ExecutionException {

    public PyRemoteInterpreterExecutionException() {
      super(WEB_DEPLOYMENT_PLUGIN_IS_DISABLED);
    }
  }

  public static class PyRemoteInterpreterRuntimeException extends RuntimeException {

    public PyRemoteInterpreterRuntimeException() {
      super(WEB_DEPLOYMENT_PLUGIN_IS_DISABLED);
    }
  }
}
