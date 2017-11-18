package org.jetbrains.idea.maven.navigator.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.idea.maven.navigator.MavenProjectsNavigator;
import org.jetbrains.idea.maven.utils.MavenToggleAction;

public abstract class MavenProjectsNavigatorAction extends MavenToggleAction {
  @Override
  protected boolean doIsSelected(AnActionEvent e) {
    return isSelected(getNavigator(e));
  }

  @Override
  public void setSelected(AnActionEvent e, boolean state) {
    setSelected(getNavigator(e), state);
  }

  private MavenProjectsNavigator getNavigator(AnActionEvent e) {
    return MavenProjectsNavigator.getInstance(getProject(e));
  }

  protected abstract boolean isSelected(MavenProjectsNavigator navigator);

  protected abstract void setSelected(MavenProjectsNavigator navigator, boolean value);
}