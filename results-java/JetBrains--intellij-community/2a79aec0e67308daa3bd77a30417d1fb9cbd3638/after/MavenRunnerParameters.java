package org.jetbrains.idea.maven.runner.executor;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.core.MavenDataKeys;
import org.jetbrains.idea.maven.core.util.MavenEnv;
import org.jetbrains.idea.maven.state.MavenProjectsState;

import java.io.File;
import java.util.*;

/**
 * @author Vladislav.Kaznacheev
*/
public class MavenRunnerParameters implements Cloneable{

  private String pomPath;
  private List<String> goals;
  private SortedSet<String> profiles;

  public MavenRunnerParameters() {
    this("", null, null);
  }

  public MavenRunnerParameters(final String pomPath, final List<String> goals, final Collection<String> profiles) {
    setPomPath(pomPath);
    setGoals(goals);
    setProfiles(profiles);
  }

  public MavenRunnerParameters(final MavenRunnerParameters that) {
    this(that.pomPath, that.goals, that.profiles);
  }

  public String getPomPath() {
    return pomPath;
  }

  public void setPomPath(final String pomPath) {
    this.pomPath = pomPath;
  }

  public List<String> getGoals() {
    return goals;
  }

  public void setGoals(List<String> goals) {
    this.goals= new ArrayList<String>();
    if(goals!=null){
      this.goals.addAll(goals);
    }
  }

  public Set<String> getProfiles() {
    return profiles;
  }

  public void setProfiles(final Collection<String> profiles) {
    this.profiles = new TreeSet<String>();
    if(profiles!=null){
      this.profiles.addAll(profiles);
    }
  }

  public File getPomFile () {
    return new File ( pomPath);
  }

  public File getWorkingDir () {
    return getPomFile().getParentFile();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final MavenRunnerParameters that = (MavenRunnerParameters)o;

    if (goals != null ? !goals.equals(that.goals) : that.goals != null) return false;
    if (pomPath != null ? !pomPath.equals(that.pomPath) : that.pomPath != null) return false;
    if (profiles != null ? !profiles.equals(that.profiles) : that.profiles != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (pomPath != null ? pomPath.hashCode() : 0);
    result = 31 * result + (goals != null ? goals.hashCode() : 0);
    result = 31 * result + (profiles != null ? profiles.hashCode() : 0);
    return result;
  }

  @Nullable
  public MavenRunnerParameters clone () {
    return new MavenRunnerParameters(this);
  }

  @Nullable
  public static MavenRunnerParameters createBuildParameters(DataContext dataContext) {
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    final VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
    final List<String> goals = MavenDataKeys.MAVEN_GOALS_KEY.getData(dataContext);
    if (project != null && virtualFile != null && MavenEnv.POM_FILE.equals(virtualFile.getName()) && goals != null) {
      return new MavenRunnerParameters(virtualFile.getPath(), goals,
                                      project.getComponent(MavenProjectsState.class).getProfiles(virtualFile));
    }
    return null;
  }
}