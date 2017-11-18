package org.jetbrains.idea.maven.utils;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.tasks.MavenTasksManager;

public abstract class MavenAction extends AnAction implements DumbAware {
  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(isAvailable(e));
  }

  protected boolean isAvailable(AnActionEvent e) {
    return getProject(e) != null;
  }

  protected Project getProject(AnActionEvent e) {
    return e.getData(PlatformDataKeys.PROJECT);
  }

  protected MavenProjectsManager getProjectsManager(AnActionEvent e) {
    return MavenProjectsManager.getInstance(getProject(e));
  }

  protected MavenTasksManager getTasksManager(AnActionEvent e) {
    return MavenTasksManager.getInstance(getProject(e));
  }

  protected VirtualFile getMavenProjectFile(AnActionEvent e) {
    return MavenUtil.getMavenProjectFileFromContext(e.getDataContext());
  }
}