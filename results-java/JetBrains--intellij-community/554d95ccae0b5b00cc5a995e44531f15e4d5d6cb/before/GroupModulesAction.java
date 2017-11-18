package org.jetbrains.idea.maven.navigator.action;

import org.jetbrains.idea.maven.navigator.MavenProjectsNavigator;

public class GroupModulesAction extends MavenProjectsNavigatorAction {
  @Override
  public boolean isSelected(MavenProjectsNavigator navigator) {
    return navigator.getGroupModules();
  }

  @Override
  public void setSelected(MavenProjectsNavigator navigator, boolean value) {
    navigator.setGroupModules(value);
  }
}