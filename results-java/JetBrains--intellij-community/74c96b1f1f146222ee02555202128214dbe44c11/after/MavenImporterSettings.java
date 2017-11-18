package org.jetbrains.idea.maven.project;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladislav.Kaznacheev
 */
public class MavenImporterSettings implements Cloneable {
  @NotNull private String dedicatedModuleDir = "";
  private boolean lookForNested = false;
  private boolean createModuleGroups = false;
  private boolean useMavenOutput = true;
  private List<String> myIgnoredDependencies = new ArrayList<String>();

  @NotNull
  public String getDedicatedModuleDir() {
    return dedicatedModuleDir;
  }

  public void setDedicatedModuleDir(@NotNull final String dedicatedModuleDir) {
    this.dedicatedModuleDir = dedicatedModuleDir;
  }

  public boolean isLookForNested() {
    return lookForNested;
  }

  public void setLookForNested(final boolean lookForNested) {
    this.lookForNested = lookForNested;
  }

  public boolean isCreateModuleGroups() {
    return createModuleGroups;
  }

  public void setCreateModuleGroups(final boolean createModuleGroups) {
    this.createModuleGroups = createModuleGroups;
  }

  public boolean isUseMavenOutput() {
    return useMavenOutput;
  }

  public void setUseMavenOutput(final boolean useMavenOutput) {
    this.useMavenOutput = useMavenOutput;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final MavenImporterSettings that = (MavenImporterSettings)o;

    if (createModuleGroups != that.createModuleGroups) return false;
    if (lookForNested != that.lookForNested) return false;
    if (useMavenOutput != that.useMavenOutput) return false;
    if (!dedicatedModuleDir.equals(that.dedicatedModuleDir)) return false;
    if (myIgnoredDependencies != null ? !myIgnoredDependencies.equals(that.myIgnoredDependencies) : that.myIgnoredDependencies != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = dedicatedModuleDir.hashCode();
    result = 31 * result + (lookForNested ? 1 : 0);
    result = 31 * result + (createModuleGroups ? 1 : 0);
    result = 31 * result + (useMavenOutput ? 1 : 0);
    result = 31 * result + (myIgnoredDependencies != null ? myIgnoredDependencies.hashCode() : 0);
    return result;
  }

  @Override
  public MavenImporterSettings clone() {
    try {
      return (MavenImporterSettings)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

  public List<String> getIgnoredDependencies() {
    return myIgnoredDependencies;
  }

  public void setIgnoredDependencies(List<String> ignoredDependencies) {
    myIgnoredDependencies = ignoredDependencies;
  }
}