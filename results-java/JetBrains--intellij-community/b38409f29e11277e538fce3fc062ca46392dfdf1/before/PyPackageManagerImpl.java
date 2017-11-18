package com.jetbrains.python.packaging;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.SystemProperties;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.net.HttpConfigurable;
import com.jetbrains.python.PythonHelpersLocator;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.remote.PyRemoteInterpreterException;
import com.jetbrains.python.remote.PythonRemoteInterpreterManager;
import com.jetbrains.python.remote.PythonRemoteSdkAdditionalData;
import com.jetbrains.python.remote.RemoteFile;
import com.jetbrains.python.sdk.PySdkUtil;
import com.jetbrains.python.sdk.PythonSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author vlan
 */
public class PyPackageManagerImpl extends PyPackageManager {
  public static final int OK = 0;
  public static final int ERROR_WRONG_USAGE = 1;
  public static final int ERROR_NO_PIP = 2;
  public static final int ERROR_NO_DISTRIBUTE = 3;
  public static final int ERROR_INVALID_SDK = -1;
  public static final int ERROR_TOOL_NOT_FOUND = -2;
  public static final int ERROR_TIMEOUT = -3;
  public static final int ERROR_INVALID_OUTPUT = -4;
  public static final int ERROR_ACCESS_DENIED = -5;
  public static final int ERROR_EXECUTION = -6;
  public static final int ERROR_INTERRUPTED = -7;

  public static final String PACKAGE_PIP = "pip";
  public static final String PACKAGE_DISTRIBUTE = "distribute";
  public static final String PACKAGE_SETUPTOOLS = "setuptools";

  public static final Key<Boolean> RUNNING_PACKAGING_TASKS = Key.create("PyPackageRequirementsInspection.RunningPackagingTasks");

  private static final String PACKAGING_TOOL = "packaging_tool.py";
  private static final String VIRTUALENV = "virtualenv.py";
  private static final int TIMEOUT = 10 * 60 * 1000;

  private static final String BUILD_DIR_OPTION = "--build-dir";
  public static final String USE_USER_SITE = "--user";

  public static final String INSTALL = "install";
  public static final String UNINSTALL = "uninstall";
  public static final String UNTAR = "untar";

  // Bundled package management tools
  public static final String DISTRIBUTE = "distribute-0.6.27";
  public static final String PIP = "pip-1.1";

  private List<PyPackage> myPackagesCache = null;
  private PyExternalProcessException myExceptionCache = null;

  private Sdk mySdk;

  public static class UI {
    @Nullable private Listener myListener;
    @NotNull private Project myProject;
    @NotNull private Sdk mySdk;

    public interface Listener {
      void started();

      void finished(List<PyExternalProcessException> exceptions);
    }

    public UI(@NotNull Project project, @NotNull Sdk sdk, @Nullable Listener listener) {
      myProject = project;
      mySdk = sdk;
      myListener = listener;
    }

    public void installManagement(@NotNull final String name) {
      final String progressTitle;
      final String successTitle;
      progressTitle = "Installing package " + name;
      successTitle = "Packages installed successfully";
      run(new MultiExternalRunnable() {
        @Override
        public List<PyExternalProcessException> run(@NotNull ProgressIndicator indicator) {
          final List<PyExternalProcessException> exceptions = new ArrayList<PyExternalProcessException>();
          indicator.setText(String.format("Installing package '%s'...", name));
          final PyPackageManagerImpl manager = (PyPackageManagerImpl)PyPackageManagers.getInstance().forSdk(mySdk);
          try {
            manager.installManagement(name);
          }
          catch (PyExternalProcessException e) {
            exceptions.add(e);
          }
          return exceptions;
        }
      }, progressTitle, successTitle, "Installed package " + name,
          "Install package failed");
    }

    public void install(@NotNull final List<PyRequirement> requirements, @NotNull final List<String> extraArgs) {
      final String progressTitle;
      final String successTitle;
      progressTitle = "Installing packages";
      successTitle = "Packages installed successfully";
      run(new MultiExternalRunnable() {
        @Override
        public List<PyExternalProcessException> run(@NotNull ProgressIndicator indicator) {
          final int size = requirements.size();
          final List<PyExternalProcessException> exceptions = new ArrayList<PyExternalProcessException>();
          final PyPackageManagerImpl manager = (PyPackageManagerImpl)PyPackageManagers.getInstance().forSdk(mySdk);
          for (int i = 0; i < size; i++) {
            final PyRequirement requirement = requirements.get(i);
            if (myListener != null) {
              indicator.setText(String.format("Installing package '%s'...", requirement));
              indicator.setFraction((double)i / size);
            }
            try {
              manager.install(list(requirement), extraArgs);
            }
            catch (PyExternalProcessException e) {
              exceptions.add(e);
            }
          }
          manager.refresh();
          return exceptions;
        }
      }, progressTitle, successTitle, "Installed packages: " + PyPackageUtil.requirementsToString(requirements),
          "Install packages failed");
    }

    public void uninstall(@NotNull final List<PyPackage> packages) {
      final String packagesString = StringUtil.join(packages, new Function<PyPackage, String>() {
        @Override
        public String fun(PyPackage pkg) {
          return "'" + pkg.getName() + "'";
        }
      }, ", ");

      run(new MultiExternalRunnable() {
        @Override
        public List<PyExternalProcessException> run(@NotNull ProgressIndicator indicator) {
          final PyPackageManagerImpl manager = (PyPackageManagerImpl)PyPackageManagers.getInstance().forSdk(mySdk);
          try {
            manager.uninstall(packages);
            return list();
          }
          catch (PyExternalProcessException e) {
            return list(e);
          }
          finally {
            manager.refresh();
          }
        }
      }, "Uninstalling packages", "Packages uninstalled successfully", "Uninstalled packages: " + packagesString,
          "Uninstall packages failed");
    }

    private interface MultiExternalRunnable {
      List<PyExternalProcessException> run(@NotNull ProgressIndicator indicator);
    }

    private void run(@NotNull final MultiExternalRunnable runnable, @NotNull final String progressTitle,
                     @NotNull final String successTitle, @NotNull final String successDescription, @NotNull final String failureTitle) {
      ProgressManager.getInstance().run(new Task.Backgroundable(myProject, progressTitle, false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          indicator.setText(progressTitle + "...");
          final Ref<Notification> notificationRef = new Ref<Notification>(null);
          final String PACKAGING_GROUP_ID = "Packaging";
          final Application application = ApplicationManager.getApplication();
          if (myListener != null) {
            application.invokeLater(new Runnable() {
              @Override
              public void run() {
                myListener.started();
              }
            });
          }

          final List<PyExternalProcessException> exceptions = runnable.run(indicator);
          if (exceptions.isEmpty()) {
            notificationRef.set(new Notification(PACKAGING_GROUP_ID, successTitle, successDescription, NotificationType.INFORMATION));
          }
          else {
            final String progressLower = progressTitle.toLowerCase();
            final String firstLine = String.format("Error%s occurred when %s.", exceptions.size() > 1 ? "s" : "", progressLower);

            final String description = createDescription(exceptions, firstLine);
            notificationRef.set(new Notification(PACKAGING_GROUP_ID, failureTitle,
                                                 firstLine + " <a href=\"xxx\">Details...</a>",
                                                 NotificationType.ERROR,
                                                 new NotificationListener() {
                                                   @Override
                                                   public void hyperlinkUpdate(@NotNull Notification notification,
                                                                               @NotNull HyperlinkEvent event) {
                                                     PyPIPackageUtil.showError(myProject, failureTitle, description);
                                                   }
                                                 }));
          }
          application.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (myListener != null) {
                myListener.finished(exceptions);
              }
              final Notification notification = notificationRef.get();
              if (notification != null) {
                notification.notify(myProject);
              }
            }
          });
        }
      });
    }

    public static String createDescription(List<PyExternalProcessException> exceptions, String firstLine) {
      final StringBuilder b = new StringBuilder();
      b.append(firstLine);
      b.append("\n\n");
      for (PyExternalProcessException exception : exceptions) {
        b.append(exception.toString());
        b.append("\n");
      }
      return b.toString();
    }
  }

  @Override
  public void refresh() {
    final Application application = ApplicationManager.getApplication();
    application.invokeLater(new Runnable() {
      @Override
      public void run() {
        application.runWriteAction(new Runnable() {
          @Override
          public void run() {
            final VirtualFile[] files = mySdk.getRootProvider().getFiles(OrderRootType.CLASSES);
            for (VirtualFile file : files) {
              file.refresh(true, true);
            }
          }
        });
        PythonSdkType.getInstance().setupSdkPaths(mySdk);
        clearCaches();
      }
    });
  }

  private void installManagement(String name) throws PyExternalProcessException {
    final File helperFile = PythonHelpersLocator.getHelperFile(name + ".tar.gz");

    final String helpersPath = getHelperPath(name);

    ProcessOutput output = getHelperOutput(PACKAGING_TOOL, Lists.newArrayList(UNTAR, helpersPath), false, helperFile.getParent());

    if (output.getExitCode() != 0) {
      throw new PyExternalProcessException(output.getExitCode(), PACKAGING_TOOL,
                                           Lists.newArrayList(UNTAR), output.getStderr());
    }
    String dirName = FileUtil.toSystemDependentName(output.getStdout().trim());
    if (!dirName.endsWith(File.separator)) {
      dirName += File.separator;
    }
    final String fileName = dirName + name + File.separatorChar + "setup.py";
    try {
      output = getProcessOutput(fileName, Collections.<String>singletonList(INSTALL), true, dirName + name);
      final int retcode = output.getExitCode();
      if (output.isTimeout()) {
        throw new PyExternalProcessException(ERROR_TIMEOUT, fileName, Lists.newArrayList(INSTALL), "Timed out");
      }
      else if (retcode != 0) {
        final String stdout = output.getStdout();
        String message = output.getStderr();
        if (message.trim().isEmpty()) {
          message = stdout;
        }
        throw new PyExternalProcessException(retcode, fileName, Lists.newArrayList(INSTALL), message);
      }
    }
    finally {
      FileUtil.delete(new File(dirName)); //TODO: remove temp directory for remote interpreter
    }
  }

  PyPackageManagerImpl(@NotNull Sdk sdk) {
    mySdk = sdk;
    final Application app = ApplicationManager.getApplication();
    final MessageBusConnection connection = app.getMessageBus().connect();
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener.Adapter() {
      @Override
      public void after(@NotNull List<? extends VFileEvent> events) {
        final VirtualFile[] roots = mySdk.getRootProvider().getFiles(OrderRootType.CLASSES);
        for (VFileEvent event : events) {
          final VirtualFile file = event.getFile();
          if (file != null) {
            for (VirtualFile root : roots) {
              if (VfsUtilCore.isAncestor(root, file, false)) {
                clearCaches();
                return;
              }
            }
          }
        }
      }
    });
  }

  public Sdk getSdk() {
    return mySdk;
  }

  @Override
  public void install(String requirementString) throws PyExternalProcessException {
    install(Collections.singletonList(PyRequirement.fromString(requirementString)), Collections.<String>emptyList());
  }

  public void install(@NotNull List<PyRequirement> requirements, @NotNull List<String> extraArgs)
    throws PyExternalProcessException {
    final List<String> args = new ArrayList<String>();
    args.add(INSTALL);
    final File buildDir;
    try {
      buildDir = FileUtil.createTempDirectory("pycharm-packaging", null);
    }
    catch (IOException e) {
      throw new PyExternalProcessException(ERROR_ACCESS_DENIED, PACKAGING_TOOL, args, "Cannot create temporary build directory");
    }
    if (!extraArgs.contains(BUILD_DIR_OPTION)) {
      args.addAll(list(BUILD_DIR_OPTION, buildDir.getAbsolutePath()));
    }

    boolean useUserSite = extraArgs.contains(USE_USER_SITE);

    final String proxyString = getProxyString();
    if (proxyString != null) {
      args.add("--proxy");
      args.add(proxyString);
    }
    args.addAll(extraArgs);
    for (PyRequirement req : requirements) {
      args.addAll(req.toOptions());
    }
    try {
      runPythonHelper(PACKAGING_TOOL, args, !useUserSite);
    }
    finally {
      clearCaches();
      FileUtil.delete(buildDir);
    }
  }

  public void uninstall(@NotNull List<PyPackage> packages) throws PyExternalProcessException {
    try {
      final List<String> args = new ArrayList<String>();
      args.add(UNINSTALL);
      boolean canModify = true;
      for (PyPackage pkg : packages) {
        if (canModify) {
          canModify = FileUtil.ensureCanCreateFile(new File(pkg.getLocation()));
        }
        args.add(pkg.getName());
      }
      runPythonHelper(PACKAGING_TOOL, args, !canModify);
    }
    finally {
      clearCaches();
    }
  }

  public static String getUserSite() {
    if (SystemInfo.isWindows) {
      final String appdata = System.getenv("APPDATA");
      return appdata + File.separator + "Python";
    }
    else {
      final String userHome = SystemProperties.getUserHome();
      return userHome + File.separator + ".local";
    }
  }


  public boolean cacheIsNotNull() {
    return myPackagesCache != null;
  }

  @NotNull
  public synchronized List<PyPackage> getPackages() throws PyExternalProcessException {
    if (myPackagesCache == null) {
      if (myExceptionCache != null) {
        throw myExceptionCache;
      }

      try {
        final String output = runPythonHelper(PACKAGING_TOOL, list("list"));
        myPackagesCache = parsePackagingToolOutput(output);
        Collections.sort(myPackagesCache, new Comparator<PyPackage>() {
          @Override
          public int compare(PyPackage aPackage, PyPackage aPackage1) {
            return aPackage.getName().compareTo(aPackage1.getName());
          }
        });
      }
      catch (PyExternalProcessException e) {
        myExceptionCache = e;
        throw e;
      }
    }
    return myPackagesCache;
  }

  @Nullable
  public PyPackage findPackage(String name) throws PyExternalProcessException {
    for (PyPackage pkg : getPackages()) {
      if (name.equals(pkg.getName())) {
        return pkg;
      }
    }
    return null;
  }

  public boolean hasPip() {
    try {
      return findPackage(PACKAGE_PIP) != null;
    }
    catch (PyExternalProcessException e) {
      return false;
    }
  }

  @NotNull
  public String createVirtualEnv(@NotNull String destinationDir, boolean useGlobalSite) throws PyExternalProcessException {
    final List<String> args = new ArrayList<String>();
    final boolean usePyVenv = PythonSdkType.getLanguageLevelForSdk(mySdk).isAtLeast(LanguageLevel.PYTHON33);
    if (usePyVenv) {
      args.add("pyvenv");
      if (useGlobalSite) {
        args.add("--system-site-packages");
      }
      args.add(destinationDir);
      runPythonHelper(PACKAGING_TOOL, args);
    }
    else {
      if (useGlobalSite) {
        args.add("--system-site-packages");
      }
      args.addAll(list("--never-download", "--distribute", destinationDir));
      runPythonHelper(VIRTUALENV, args);
    }

    final String binary = PythonSdkType.getPythonExecutable(destinationDir);
    final String binaryFallback = destinationDir + File.separator + "bin" + File.separator + "python";
    final String path = (binary != null) ? binary : binaryFallback;

    if (usePyVenv) {
      // TODO: Still no 'packaging' and 'pysetup3' for Python 3.3rc1, see PEP 405
      final VirtualFile binaryFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
      if (binaryFile != null) {
        final ProjectJdkImpl tmpSdk = new ProjectJdkImpl("", PythonSdkType.getInstance());
        tmpSdk.setHomePath(path);
        final PyPackageManagerImpl manager = new PyPackageManagerImpl(tmpSdk);
        manager.installManagement(DISTRIBUTE);
        manager.installManagement(PIP);
      }
    }
    return path;
  }

  public static void deleteVirtualEnv(@NotNull String sdkHome) throws PyExternalProcessException {
    final File root = PythonSdkType.getVirtualEnvRoot(sdkHome);
    if (root != null) {
      FileUtil.delete(root);
    }
  }

  @Nullable
  public static List<PyRequirement> getRequirements(@NotNull Module module) {
    // TODO: Cache requirements, clear cache on requirements.txt or setup.py updates
    List<PyRequirement> requirements = getRequirementsFromTxt(module);
    if (requirements != null) {
      return requirements;
    }
    final List<String> lines = new ArrayList<String>();
    for (String name : PyPackageUtil.SETUP_PY_REQUIRES_KWARGS_NAMES) {
      final PyListLiteralExpression installRequires = PyPackageUtil.findSetupPyRequires(module, name);
      if (installRequires != null) {
        for (PyExpression e : installRequires.getElements()) {
          if (e instanceof PyStringLiteralExpression) {
            lines.add(((PyStringLiteralExpression)e).getStringValue());
          }
        }
      }
    }
    if (!lines.isEmpty()) {
      return PyRequirement.parse(StringUtil.join(lines, "\n"));
    }
    if (PyPackageUtil.findSetupPy(module) != null) {
      return Collections.emptyList();
    }
    return null;
  }

  @Nullable
  public static List<PyRequirement> getRequirementsFromTxt(Module module) {
    final VirtualFile requirementsTxt = PyPackageUtil.findRequirementsTxt(module);
    if (requirementsTxt != null) {
      return PyRequirement.parse(requirementsTxt);
    }
    return null;
  }

  private void clearCaches() {
    myPackagesCache = null;
    myExceptionCache = null;
  }

  private static <T> List<T> list(T... xs) {
    return Arrays.asList(xs);
  }

  @Nullable
  private String getProxyString() {
    final HttpConfigurable settings = HttpConfigurable.getInstance();
    if (settings != null && settings.USE_HTTP_PROXY) {
      final String credentials;
      if (settings.PROXY_AUTHENTICATION) {
        credentials = String.format("%s:%s@", settings.PROXY_LOGIN, settings.getPlainProxyPassword());
      }
      else {
        credentials = "";
      }
      return credentials + String.format("%s:%d", settings.PROXY_HOST, settings.PROXY_PORT);
    }
    return null;
  }

  @NotNull
  private String runPythonHelper(@NotNull final String helper,
                                 @NotNull final List<String> args, final boolean askForSudo) throws PyExternalProcessException {
    ProcessOutput output = getHelperOutput(helper, args, askForSudo, null);
    final int retcode = output.getExitCode();
    if (output.isTimeout()) {
      throw new PyExternalProcessException(ERROR_TIMEOUT, helper, args, "Timed out");
    }
    else if (retcode != 0) {
      final String message = output.getStderr() + "\n" + output.getStdout();
      throw new PyExternalProcessException(retcode, helper, args, message);
    }
    return output.getStdout();
  }

  @NotNull
  private String runPythonHelper(@NotNull final String helper,
                                 @NotNull final List<String> args) throws PyExternalProcessException {
    return runPythonHelper(helper, args, false);
  }


  private ProcessOutput getHelperOutput(@NotNull String helper,
                                        @NotNull List<String> args,
                                        final boolean askForSudo,
                                        @Nullable String parentDir)
    throws PyExternalProcessException {
    final String helperPath = getHelperPath(helper);

    if (helperPath == null) {
      throw new PyExternalProcessException(ERROR_TOOL_NOT_FOUND, helper, args, "Cannot find external tool");
    }
    return getProcessOutput(helperPath, args, askForSudo, parentDir);
  }

  private String getHelperPath(String helper) {
    String helperPath;
    final SdkAdditionalData sdkData = mySdk.getSdkAdditionalData();
    if (sdkData instanceof PythonRemoteSdkAdditionalData) {
      final PythonRemoteSdkAdditionalData remoteSdkData = (PythonRemoteSdkAdditionalData)sdkData;
      helperPath = new RemoteFile(remoteSdkData.getPyCharmHelpersPath(),
                                  helper).getPath();
    }
    else {
      helperPath = PythonHelpersLocator.getHelperPath(helper);
    }
    return helperPath;
  }

  private ProcessOutput getProcessOutput(@NotNull String helperPath,
                                         @NotNull List<String> args,
                                         boolean askForSudo,
                                         @Nullable String parentDir)
    throws PyExternalProcessException {
    final SdkAdditionalData sdkData = mySdk.getSdkAdditionalData();
    if (sdkData instanceof PythonRemoteSdkAdditionalData) {
      final PythonRemoteSdkAdditionalData remoteSdkData = (PythonRemoteSdkAdditionalData)sdkData;
      final PythonRemoteInterpreterManager manager = PythonRemoteInterpreterManager.getInstance();
      if (manager != null) {
        final List<String> cmdline = new ArrayList<String>();
        cmdline.add(mySdk.getHomePath());
        cmdline.add(RemoteFile.detectSystemByPath(mySdk.getHomePath()).createRemoteFile(helperPath).getPath());
        cmdline.addAll(args);
        try {
          if (askForSudo) {
            askForSudo = !manager.ensureCanWrite(null, remoteSdkData, remoteSdkData.getInterpreterPath());
          }
          ProcessOutput processOutput;
          do {
            processOutput = manager.runRemoteProcess(null, remoteSdkData, ArrayUtil.toStringArray(cmdline), askForSudo);
            if (askForSudo && processOutput.getStderr().contains("sudo: 3 incorrect password attempts")) {
              continue;
            }
            break;
          }
          while (true);
          return processOutput;
        }
        catch (PyRemoteInterpreterException e) {
          throw new PyExternalProcessException(ERROR_INVALID_SDK, helperPath, args, "Error running SDK: " + e.getMessage(), e);
        }
      }
      else {
        throw new PyExternalProcessException(ERROR_INVALID_SDK, helperPath, args,
                                             PythonRemoteInterpreterManager.WEB_DEPLOYMENT_PLUGIN_IS_DISABLED);
      }
    }
    else {
      final String homePath = mySdk.getHomePath();
      if (homePath == null) {
        throw new PyExternalProcessException(ERROR_INVALID_SDK, helperPath, args, "Cannot find interpreter for SDK");
      }
      if (parentDir == null) {
        parentDir = new File(homePath).getParent();
      }
      final List<String> cmdline = new ArrayList<String>();
      cmdline.add(homePath);

      cmdline.addAll(args);

      final boolean canCreate = FileUtil.ensureCanCreateFile(new File(mySdk.getHomePath()));
      if (!canCreate && !SystemInfo.isWindows && askForSudo) {   //is system site interpreter --> we need sudo privileges
        try {
          helperPath = StringUtil.replace(helperPath, " ", "\\ ");
          cmdline.add(1, helperPath);
          final ProcessOutput result = ExecUtil.sudoAndGetOutput(StringUtil.join(cmdline, " "),
                                                                 "Please enter your password to make changes in system packages: ",
                                                                 parentDir);
          String message = result.getStderr();
          if (result.getExitCode() != 0) {
            final String stdout = result.getStdout();
            if (StringUtil.isEmptyOrSpaces(message)) {
              message = stdout;
            }
            if (StringUtil.isEmptyOrSpaces(message)) {
              message = "Failed to perform action. Permission denied.";
            }
            throw new PyExternalProcessException(result.getExitCode(), helperPath, args, message);
          }
          if (SystemInfo.isMac && !StringUtil.isEmptyOrSpaces(message)) {
            throw new PyExternalProcessException(result.getExitCode(), helperPath, args, message);
          }
          return result;
        }
        catch (ExecutionException e) {
          throw new PyExternalProcessException(ERROR_EXECUTION, helperPath, args, e.getMessage());
        }
        catch (IOException e) {
          throw new PyExternalProcessException(ERROR_ACCESS_DENIED, helperPath, args, e.getMessage());
        }
      }
      else                 //vEnv interpreter
      {
        cmdline.add(1, helperPath);
        return PySdkUtil.getProcessOutput(parentDir, ArrayUtil.toStringArray(cmdline), TIMEOUT);
      }
    }
  }

  @NotNull
  private static List<PyPackage> parsePackagingToolOutput(@NotNull String s) throws PyExternalProcessException {
    final String[] lines = StringUtil.splitByLines(s);
    final List<PyPackage> packages = new ArrayList<PyPackage>();
    for (String line : lines) {
      final List<String> fields = StringUtil.split(line, "\t");
      if (fields.size() < 3) {
        throw new PyExternalProcessException(ERROR_INVALID_OUTPUT, PACKAGING_TOOL, Collections.<String>emptyList(),
                                             "Invalid output format");
      }
      final String name = fields.get(0);
      final String version = fields.get(1);
      final String location = fields.get(2);
      final List<PyRequirement> requirements = new ArrayList<PyRequirement>();
      if (fields.size() >= 4) {
        final String requiresLine = fields.get(3);
        final String requiresSpec = StringUtil.join(StringUtil.split(requiresLine, ":"), "\n");
        requirements.addAll(PyRequirement.parse(requiresSpec));
      }
      if (!"Python".equals(name)) {
        packages.add(new PyPackage(name, version, location, requirements));
      }
    }
    return packages;
  }


  @Override
  public void showInstallationError(Project project, String title, String description) {
    PyPIPackageUtil.showError(project, title, description);
  }
}