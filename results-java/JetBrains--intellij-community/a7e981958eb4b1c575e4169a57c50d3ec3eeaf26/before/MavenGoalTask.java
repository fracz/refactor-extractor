package org.jetbrains.idea.maven.tasks;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.runner.MavenRunnerParameters;

import java.util.Arrays;

public class MavenGoalTask implements Cloneable, Comparable {
  public String pomPath;
  public String goal;

  public MavenGoalTask() {
  }

  public MavenGoalTask(final String pomPath, final String goal) {
    this.pomPath = pomPath;
    this.goal = goal;
  }

  protected MavenGoalTask clone() {
    return new MavenGoalTask(pomPath, goal);
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final MavenGoalTask task = (MavenGoalTask)o;

    if (goal != null ? !goal.equals(task.goal) : task.goal != null) return false;
    if (pomPath != null ? !pomPath.equals(task.pomPath) : task.pomPath != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (pomPath != null ? pomPath.hashCode() : 0);
    result = 31 * result + (goal != null ? goal.hashCode() : 0);
    return result;
  }

  public String toString(){
    return pomPath + "#" + goal;
  }

  public int compareTo(Object o) {
    return toString().compareTo(o.toString());
  }

  @Nullable
  public MavenRunnerParameters createRunnerParameters(final MavenProjectsManager projectsManager) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(pomPath);
    if (virtualFile != null) {
      return new MavenRunnerParameters(true, virtualFile.getParent().getPath(),
                                       Arrays.asList(goal), projectsManager.getActiveProfiles());
    }
    return null;
  }
}