package com.intellij.mock;

import com.intellij.application.options.ReplacePathToMacroMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ExpandMacroToPathMap;
import com.intellij.openapi.components.impl.stores.IProjectStore;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.PomModel;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockProject extends MockComponentManager implements ProjectEx {
  public MockProject() {
    super(ApplicationManager.getApplication().getPicoContainer());
  }

  public void dispose() {
  }

  public boolean isSavePathsRelative() {
    return false;
  }

  public void setSavePathsRelative(boolean b) {
  }

  public boolean isDefault() {
    return false;
  }

  public PomModel getModel() {
    return getComponent(PomModel.class);
  }

  public boolean isDummy() {
    return false;
  }

  @NotNull
  public IProjectStore getStateStore() {
    return new MockProjectStore();
  }

  public boolean isOpen() {
    return false;
  }

  public boolean isInitialized() {
    return false;
  }

  public ReplacePathToMacroMap getMacroReplacements() {
    return null;
  }

  public ExpandMacroToPathMap getExpandMacroReplacements() {
    return null;
  }

  public VirtualFile getProjectFile() {
    return null;
  }

  public String getName() {
    return null;
  }

  @Nullable
  @NonNls
  public String getPresentableUrl() {
    return null;
  }

  @NotNull
  @NonNls
  public String getLocationHash() {
    return "mock";
  }

  public String getProjectFilePath() {
    return null;
  }

  public VirtualFile getWorkspaceFile() {
    return null;
  }

  @Nullable
  public VirtualFile getBaseDir() {
    return null;
  }

  public void save() {
  }

  public GlobalSearchScope getAllScope() {
    return null;
  }

  public GlobalSearchScope getProjectScope() {
    return null;
  }

}