package org.jetbrains.idea.maven.utils.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class MavenActionGroup extends DefaultActionGroup {
  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    boolean available = isAvailable(e);
    e.getPresentation().setEnabled(available);
    e.getPresentation().setVisible(available);
  }

  protected boolean isAvailable(AnActionEvent e) {
    return MavenActionUtil.getProject(e) != null && !MavenActionUtil.getMavenProjects(e).isEmpty();
  }
}