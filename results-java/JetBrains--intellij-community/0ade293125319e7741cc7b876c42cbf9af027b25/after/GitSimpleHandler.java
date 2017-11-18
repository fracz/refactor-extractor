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

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.i18n.GitBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simple Git hanlder that accumulates stdout and stderr and has nothing on stdin.
 * The handler executes commands sychronously with cancellable progress indicator.
 * <p/>
 * The class also includes a number of static utility methods that represent some
 * simple commands.
 */
public class GitSimpleHandler extends GitHandler {
  /**
   * Stderr output
   */
  private final StringBuilder myStderr = new StringBuilder();
  /**
   * Stdout output
   */
  private final StringBuilder myStdout = new StringBuilder();

  /**
   * A constructor
   *
   * @param project   a project
   * @param directory a process directory
   * @param command   a command to execute
   */
  @SuppressWarnings({"WeakerAccess"})
  public GitSimpleHandler(@NotNull Project project, @NotNull File directory, @NotNull @NonNls String command) {
    super(project, directory, command);
  }

  /**
   * A constructor
   *
   * @param project   a project
   * @param directory a process directory
   * @param command   a command to execute
   */
  @SuppressWarnings({"WeakerAccess"})
  public GitSimpleHandler(final Project project, final VirtualFile directory, @NonNls final String command) {
    super(project, directory, command);
  }

  /**
   * A constructor
   *
   * @param project   a project
   * @param directory a process directory
   */
  public GitSimpleHandler(final Project project, final File directory) {
    super(project, directory, "");
  }

  /**
   * {@inheritDoc}
   */
  protected void onTextAvailable(final String text, final Key outputType) {
    if (ProcessOutputTypes.STDOUT == outputType) {
      myStdout.append(text);
    }
    else if (ProcessOutputTypes.STDERR == outputType) {
      myStderr.append(text);
    }
  }

  /**
   * @return stderr contents
   */
  public String getStderr() {
    return myStderr.toString();
  }

  /**
   * @return stdout contents
   */
  public String getStdout() {
    return myStdout.toString();
  }

  /**
   * Execute withot UI. If UI interactions are required (for example SSH popups or progress dialog), use {@link git4idea.commands.GitHandlerUtil} methods.
   *
   * @return a value if process was successful
   * @throws com.intellij.openapi.vcs.VcsException
   *          exception if process failed to start.
   */
  public String run() throws VcsException {
    if (!isNoSSH()) {
      throw new IllegalStateException("Commands that require SSH could not be run using this method");
    }
    final VcsException[] ex = new VcsException[1];
    final String[] result = new String[1];
    addListener(new GitHandlerListener() {
      public void processTerminted(final int exitCode) {
        if (exitCode == 0 || isIgnoredErrorCode(exitCode)) {
          result[0] = getStdout();
        }
        else {
          String msg = getStderr();
          if (msg.length() == 0) {
            msg = getStdout();
          }
          if (msg.length() == 0) {
            msg = GitBundle.message("git.error.exit", exitCode);
          }
          ex[0] = new VcsException(msg);
        }
      }

      public void startFailed(final Throwable exception) {
        ex[0] = new VcsException("Process failed to start: " + exception.toString(), exception);
      }
    });
    start();
    if (isStarted()) {
      waitFor();
    }
    if (ex[0] != null) {
      throw ex[0];
    }
    return result[0];
  }

  /**
   * Prepare check repository handler. To do this ls-remote command is executed and attempts to match
   * master tag. This will likely return only single entry or none, if there is no master
   * branch. Stdout output is ignored. Stderr is used to construct exception message and shown
   * in error message box if exit is negative.
   *
   * @param project the project
   * @param url     the url to check
   * @return a simple handler that does the task
   */
  public static GitSimpleHandler checkRepository(Project project, final String url) {
    GitSimpleHandler handler = new GitSimpleHandler(project, new File("."), "ls-remote");
    handler.addParameters(url, "master");
    return handler;
  }

  /**
   * Prepare delete files handler.
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to delete
   * @return a simple handler that does the task
   */
  public static GitSimpleHandler delete(Project project, VirtualFile root, List<FilePath> files) {
    GitSimpleHandler handler = new GitSimpleHandler(project, root, "rm");
    handler.addRelativePaths(files);
    handler.setNoSSH(true);
    return handler;
  }

  /**
   * Prepare add files handler.
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a simple handler that does the task
   */
  public static GitSimpleHandler addFiles(Project project, VirtualFile root, Collection<VirtualFile> files) {
    GitSimpleHandler handler = new GitSimpleHandler(project, root, "add");
    handler.addRelativeFiles(files);
    handler.setNoSSH(true);
    return handler;
  }

  /**
   * Prepare add files handler.
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a simple handler that does the task
   */
  public static GitSimpleHandler addFiles(Project project, VirtualFile root, VirtualFile... files) {
    return addFiles(project, root, Arrays.asList(files));
  }

  /**
   * Prepare add files handler.
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a simple handler that does the task
   */
  public static GitSimpleHandler addPaths(Project project, VirtualFile root, Collection<FilePath> files) {
    GitSimpleHandler handler = new GitSimpleHandler(project, root, "add");
    handler.addRelativePaths(files);
    handler.setNoSSH(true);
    return handler;
  }

}