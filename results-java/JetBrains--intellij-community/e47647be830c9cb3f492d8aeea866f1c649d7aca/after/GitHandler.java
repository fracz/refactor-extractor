/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
package git4idea.commands;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.git4idea.ssh.GitSSHGUIHandler;
import org.jetbrains.git4idea.ssh.GitSSHService;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A handler for git commands
 */
public abstract class GitHandler {
  /**
   * Error codes that are ignored for the handler
   */
  private final HashSet<Integer> myIgnoredErrorCodes = new HashSet<Integer>();
  /**
   * Error list
   */
  private final List<VcsException> myErrors = Collections.synchronizedList(new LinkedList<VcsException>());
  /**
   * the logger
   */
  private static final Logger log = Logger.getInstance(GitHandler.class.getName());
  /**
   * a command line
   */
  private final GeneralCommandLine myCommandLine;
  /**
   * wrapped process handler
   */
  // note that access is safe beacause it accessed in unsychronized block only after process is started, and it does not change after that
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private OSProcessHandler myHandler;
  /**
   * process
   */
  private Process myProcess;
  /**
   * If true, the standard output is not copied to viresion control console
   */
  private boolean myStdoutSuppressed;
  /**
   * If true, the standard error is not copied to viresion control console
   */
  private boolean myStderrSuppressed;
  /**
   * the contenxt project (might be a default project)
   */
  private final Project myProject;
  /**
   * the working directory
   */
  private final File myWorkingDirectory;
  /**
   * the flag indicating that environment has been cleaned up, by default is true because there is nothing to clean
   */
  private boolean myEnvironmentCleanedUp = true;
  /**
   * the handler number
   */
  private int myHandlerNo;
  /**
   * if true process might be cancelled
   */
  // note that access is safe beacause it accessed in unsychronized block only after process is started, and it does not change after that
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private boolean myIsCancellable = true;
  /**
   * exit code or null if exit code is not yet availale
   */
  private Integer myExitCode;
  /**
   * Character set to use for IO
   */
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) @NonNls private Charset myCharset = Charset.forName("UTF-8");
  /**
   * No ssh flag
   */
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private boolean myNoSSHFlag = false;
  /**
   * listeners
   */
  private final EventDispatcher<GitHandlerListener> myListeners = EventDispatcher.create(GitHandlerListener.class);
  /**
   * if true, the command execution is not logged in version control view
   */
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private boolean mySilent;
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String CLONE = "clone";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String RM = "rm";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String ADD = "add";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String ANNOTATE = "annotate";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String COMMIT = "commit";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String CONFIG = "config";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String FETCH = "fetch";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String SHOW = "show";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String LOG = "log";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String INIT = "init";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String BRANCH = "branch";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String MERGE = "merge";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String PUSH = "push";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String LS_REMOTE = "ls-remote";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String REMOTE = "remote";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String REV_LIST = "rev-list";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String CHECKOUT = "checkout";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String TAG = "tag";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String PULL = "pull";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String LS_FILES = "ls-files";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String DIFF = "diff";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String VERSION = "version";
  /**
   * The constant for git command {@value}
   */
  @NonNls public static final String STASH = "stash";
  /**
   * The vcs object
   */
  protected final GitVcs myVcs;

  /**
   * A constructor
   *
   * @param project   a project
   * @param directory a process directory
   * @param command   a command to execute (if empty string, the parameter is ignored)
   */
  protected GitHandler(@NotNull Project project, @NotNull File directory, @NotNull String command) {
    myProject = project;
    GitVcsSettings settings = GitVcsSettings.getInstance(project);
    myVcs = GitVcs.getInstance(project);
    if (myVcs != null) {
      myVcs.checkVersion();
    }
    myWorkingDirectory = directory;
    myCommandLine = new GeneralCommandLine();
    myCommandLine.setExePath(settings.GIT_EXECUTABLE);
    myCommandLine.setWorkingDirectory(myWorkingDirectory);
    if (command.length() > 0) {
      myCommandLine.addParameter(command);
    }
  }

  /**
   * A constructor
   *
   * @param project a project
   * @param vcsRoot a process directory
   * @param command a command to execute
   */
  protected GitHandler(final Project project, final VirtualFile vcsRoot, final String command) {
    this(project, GitUtil.getIOFile(vcsRoot), command);
  }

  /**
   * Add error code to ignored list
   *
   * @param code the code to ignore
   */
  public void ignoreErrorCode(int code) {
    myIgnoredErrorCodes.add(code);
  }

  /**
   * Check if error code should be ignored
   *
   * @param code a code to check
   * @return true if error code is ignorable
   */
  public boolean isIgnoredErrorCode(int code) {
    return myIgnoredErrorCodes.contains(code);
  }


  /**
   * add error to the error list
   *
   * @param ex VcsExetpion
   */
  public void addError(VcsException ex) {
    myErrors.add(ex);
  }

  /**
   * @return unmodifiable list of errors.
   */
  public List<VcsException> errors() {
    return Collections.unmodifiableList(myErrors);
  }

  /**
   * @return a context project
   */
  public Project project() {
    return myProject;
  }

  /**
   * Set SSH flag. This flag should be set to true for commands that never interact with remote repositories.
   *
   * @param value if value is true, the custom ssh is not used for the command.
   */
  @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
  public void setNoSSH(boolean value) {
    checkNotStarted();
    myNoSSHFlag = value;
  }

  /**
   * @return true if SSH is not invoked by this command.
   */
  @SuppressWarnings({"WeakerAccess"})
  public boolean isNoSSH() {
    return myNoSSHFlag;
  }

  /**
   * Add listner to handler
   *
   * @param listener a listener
   */
  protected void addListener(GitHandlerListener listener) {
    myListeners.addListener(listener);
  }

  /**
   * End option parameters and start file paths. The method adds {@code "--"} parameter.
   */
  public void endOptions() {
    myCommandLine.addParameter("--");
  }

  /**
   * Add string parameters
   *
   * @param parameters a parameters to add
   */
  @SuppressWarnings({"WeakerAccess"})
  public void addParameters(@NonNls @NotNull String... parameters) {
    checkNotStarted();
    for (String s : parameters) {
      myCommandLine.addParameter(translateParameter(s));
    }
  }

  /**
   * Add filepath parameters. The parameters are made relative to the working directory
   *
   * @param parameters a parameters to add
   * @throws IllegalArgumentException if some path is not under root.
   */
  public void addRelativePaths(@NotNull FilePath... parameters) {
    addRelativePaths(Arrays.asList(parameters));
  }

  /**
   * Add filepath parameters. The parameters are made relative to the working directory
   *
   * @param filePaths a parameters to add
   * @throws IllegalArgumentException if some path is not under root.
   */
  @SuppressWarnings({"WeakerAccess"})
  public void addRelativePaths(@NotNull final Collection<FilePath> filePaths) {
    checkNotStarted();
    for (FilePath path : filePaths) {
      myCommandLine.addParameter(translateParameter(GitUtil.relativePath(myWorkingDirectory, path)));
    }
  }

  /**
   * Add virtual file parameters. The parameters are made relative to the working directory
   *
   * @param files a parameters to add
   * @throws IllegalArgumentException if some path is not under root.
   */
  @SuppressWarnings({"WeakerAccess"})
  public void addRelativeFiles(@NotNull final Collection<VirtualFile> files) {
    checkNotStarted();
    for (VirtualFile file : files) {
      myCommandLine.addParameter(translateParameter(GitUtil.relativePath(myWorkingDirectory, file)));
    }
  }

  /**
   * check that process is not started yet
   *
   * @throws IllegalStateException if process has been already started
   */
  private void checkNotStarted() {
    if (isStarted()) {
      throw new IllegalStateException("The process has been already started");
    }
  }

  /**
   * check that process is started
   *
   * @throws IllegalStateException if process has not been started
   */
  protected final void checkStarted() {
    if (!isStarted()) {
      throw new IllegalStateException("The process is not started yet");
    }
  }

  /**
   * @return true if process is started
   */
  public final synchronized boolean isStarted() {
    return myProcess != null;
  }


  /**
   * Set new value of cacellable flag (by default true)
   *
   * @param value a new value of the flag
   */
  public void setCancellable(boolean value) {
    checkNotStarted();
    myIsCancellable = value;
  }

  /**
   * @return cancellable state
   */
  public boolean isCancellable() {
    return myIsCancellable;
  }

  /**
   * Start process
   */
  public synchronized void start() {
    checkNotStarted();
    try {
      // setup environment
      if (!myProject.isDefault() && !mySilent) {
        GitVcs.getInstance(myProject).showCommandLine(printableCommandLine());
      }
      if (log.isDebugEnabled()) {
        log.debug("running git: " + myCommandLine.getCommandLineString() + " in " + myWorkingDirectory);
      }
      final Map<String, String> env = new HashMap<String, String>(System.getenv());
      if (!myNoSSHFlag) {
        GitSSHService ssh = GitSSHService.getInstance();
        env.put(GitSSHService.GIT_SSH_ENV, ssh.getScriptPath().getPath());
        myHandlerNo = ssh.registerHandler(new GitSSHGUIHandler(myProject));
        myEnvironmentCleanedUp = false;
        env.put(GitSSHService.SSH_HANDLER_ENV, Integer.toString(myHandlerNo));
      }
      myCommandLine.setEnvParams(env);
      // start process
      myProcess = myCommandLine.createProcess();
      myHandler = new OSProcessHandler(myProcess, myCommandLine.getCommandLineString()) {
        @Override
        public Charset getCharset() {
          return myCharset == null ? super.getCharset() : myCharset;
        }
      };
      myHandler.addProcessListener(new ProcessListener() {
        public void startNotified(final ProcessEvent event) {
          // do nothing
        }

        public void processTerminated(final ProcessEvent event) {
          final int exitCode = event.getExitCode();
          setExitCode(exitCode);
          cleanupEnv();
          GitHandler.this.processTerminated(exitCode);
          myListeners.getMulticaster().processTerminted(exitCode);
        }

        public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed) {
          // do nothing
        }

        public void onTextAvailable(final ProcessEvent event, final Key outputType) {
          GitHandler.this.onTextAvailable(event.getText(), outputType);
        }
      });
      myHandler.startNotify();
    }
    catch (Throwable t) {
      cleanupEnv();
      myListeners.getMulticaster().startFailed(t);
    }
  }

  /**
   * Notification for handler to handle process exit event
   *
   * @param exitCode a exit code.
   */
  protected abstract void processTerminated(int exitCode);

  /**
   * @return a command line with full path to executable replace to "git"
   */
  public String printableCommandLine() {
    final GeneralCommandLine line = myCommandLine.clone();
    line.setExePath("git");
    return restoreParameter(line.getCommandLineString());
  }

  /**
   * This method is invoked when some text is available
   *
   * @param text       an available text
   * @param outputType output type
   */
  protected abstract void onTextAvailable(final String text, final Key outputType);

  /**
   * Cancel activity
   */
  public synchronized void cancel() {
    checkStarted();
    if (!myIsCancellable) {
      throw new IllegalStateException("The process is not cancellable.");
    }
    try {
      myHandler.destroyProcess();
    }
    catch (Exception e) {
      log.warn("Exception during cancel", e);
    }
  }

  /**
   * @return exit code for process if it is available
   */
  public synchronized int getExitCode() {
    if (myExitCode == null) {
      throw new IllegalStateException("Exit code is not yet available");
    }
    return myExitCode.intValue();
  }

  /**
   * @param exitCode a exit code for process
   */
  private synchronized void setExitCode(int exitCode) {
    myExitCode = exitCode;
  }

  /**
   * Cleanup environment
   */
  private synchronized void cleanupEnv() {
    if (!myNoSSHFlag && !myEnvironmentCleanedUp) {
      GitSSHService ssh = GitSSHService.getInstance();
      myEnvironmentCleanedUp = true;
      ssh.unregisterHander(myHandlerNo);
    }
  }

  /**
   * Wait for process termination
   */
  public void waitFor() {
    checkStarted();
    myHandler.waitFor();
  }

  /**
   * Set silent mode. When handler is silient, it does not logs command in version control console.
   * Note that this option also suppresses stderr and stdout copying.
   *
   * @param silent a new value of the flag
   * @see #setStderrSuppressed(boolean)
   * @see #setStdoutSuppressed(boolean)
   */
  @SuppressWarnings({"SameParameterValue"})
  public void setSilent(final boolean silent) {
    checkNotStarted();
    mySilent = silent;
    setStderrSuppressed(true);
    setStdoutSuppressed(true);
  }

  /**
   * @return a charcter set to use for IO
   */
  public Charset getCharset() {
    return myCharset;
  }

  /**
   * Set character set for IO
   *
   * @param charset a character set
   */
  @SuppressWarnings({"SameParameterValue"})
  public void setCharset(final Charset charset) {
    myCharset = charset;
  }

  /**
   * @return true if stadnard output is not copied to the console
   */
  public boolean isStdoutSuppressed() {
    return myStdoutSuppressed;
  }

  /**
   * Set flag specifing if stdout shoud be copied to the console
   *
   * @param stdoutSuppressed true if output is not copied to the console
   */
  public void setStdoutSuppressed(final boolean stdoutSuppressed) {
    checkNotStarted();
    myStdoutSuppressed = stdoutSuppressed;
  }

  /**
   * @return true if stadnard output is not copied to the console
   */
  public boolean isStderrSuppressed() {
    return myStderrSuppressed;
  }

  /**
   * Set flag specifing if stderr shoud be copied to the console
   *
   * @param stderrSuppressed true if error output is not copied to the console
   */
  public void setStderrSuppressed(final boolean stderrSuppressed) {
    checkNotStarted();
    myStderrSuppressed = stderrSuppressed;
  }

  /**
   * Translate parameter
   *
   * @param s a string to translate
   * @return a translated string
   */
  public static String translateParameter(String s) {
    return s.replaceAll("([\\{}])", "\\\\$1");
  }

  /**
   * Restore parameter
   *
   * @param s a string to translate
   * @return a translated string
   */
  public static String restoreParameter(String s) {
    return s.replaceAll("\\\\([{}])", "$1");
  }


}