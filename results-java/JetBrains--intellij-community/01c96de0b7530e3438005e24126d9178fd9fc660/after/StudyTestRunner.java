package com.jetbrains.edu.learning;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.edu.learning.course.Task;
import org.jetbrains.annotations.NotNull;

public abstract class StudyTestRunner {
  public static final String STUDY_PREFIX="#educational_plugin";
  public static final String TEST_OK = "test OK";
  private static final String TEST_FAILED = "FAILED + ";
  protected final Task myTask;
  protected final VirtualFile myTaskDir;

  public StudyTestRunner(@NotNull final Task task, @NotNull final VirtualFile taskDir) {
    myTask = task;
    myTaskDir = taskDir;
  }

  public abstract Process createCheckProcess(Project project, String executablePath) throws ExecutionException;

  @NotNull
  public String getTestsOutput(@NotNull final ProcessOutput processOutput) {
    for (String line : processOutput.getStdoutLines()) {
      if (line.contains(STUDY_PREFIX)) {
        if (line.contains(TEST_OK)) {
          continue;
        }
        int messageStart = line.indexOf(TEST_FAILED);
        return line.substring(messageStart + TEST_FAILED.length());
      }
    }

    return TEST_OK;
  }
}