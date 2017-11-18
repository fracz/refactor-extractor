package com.intellij.lang.ant.config.impl;

import com.intellij.lang.ant.config.*;
import com.intellij.lang.ant.config.actions.TargetAction;
import com.intellij.lang.ant.psi.AntFile;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntTarget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AntBuildModelImpl implements AntBuildModelBase {

  private final AntBuildFile myFile;

  public AntBuildModelImpl(final AntBuildFile buildFile) {
    myFile = buildFile;
  }

  @Nullable
  public String getDefaultTargetName() {
    final AntTarget target = getAntProject().getDefaultTarget();
    return (target == null) ? "" : target.getName();
  }

  @Nullable
  public String getName() {
    return getAntProject().getName();
  }

  public AntBuildTarget[] getTargets() {
    final List<AntBuildTargetBase> list = getTargetsList();
    return list.toArray(new AntBuildTargetBase[list.size()]);
  }

  public AntBuildTarget[] getFilteredTargets() {
    final List<AntBuildTargetBase> filtered = new ArrayList<AntBuildTargetBase>();
    for (final AntBuildTargetBase buildTarget : getTargetsList()) {
      if (myFile.isTargetVisible(buildTarget)) {
        filtered.add(buildTarget);
      }
    }
    return (filtered.size() == 0) ? AntBuildTargetBase.EMPTY_ARRAY : filtered.toArray(new AntBuildTargetBase[filtered.size()]);
  }

  @Nullable
  public String getDefaultTargetActionId() {
    if (getDefaultTargetName() == null) return null;
    final String projectName = getName();
    if (projectName == null || projectName.trim().length() == 0) return null;
    return TargetAction.ACTION_ID_PREFIX + projectName;

  }

  public AntBuildFileBase getBuildFile() {
    return (AntBuildFileBase)myFile;
  }

  @Nullable
  public AntBuildTargetBase findTarget(final String name) {
    final AntTarget antTarget = getAntProject().getTarget(name);
    if (antTarget != null) {
      for (final AntBuildTargetBase buildTarget : getTargetsList()) {
        if (buildTarget.getAntTarget() == antTarget) {
          return buildTarget;
        }
      }
    }
    return null;
  }

  @Nullable
  public BuildTask findTask(final String targetName, final String taskName) {
    final AntBuildTargetBase buildTarget = findTarget(targetName);
    return (buildTarget == null) ? null : buildTarget.findTask(taskName);
  }

  public AntProject getAntProject() {
    return ((AntFile)getBuildFile().getAntFile()).getAntProject();
  }

  public boolean hasTargetWithActionId(final String id) {
    final List<AntBuildTargetBase> targetsList = getTargetsList();
    for (AntBuildTargetBase buildTarget : targetsList) {
      if (id.equals(buildTarget.getActionId())) return true;
    }
    return false;
  }

  private List<AntBuildTargetBase> getTargetsList() {
    final AntTarget[] targets = getAntProject().getTargets();
    final List<AntBuildTargetBase> list = new ArrayList<AntBuildTargetBase>(targets.length);
    for (final AntTarget target : targets) {
      list.add(new AntBuildTargetImpl(target, this));
    }
    return list;
  }
}