package org.jetbrains.idea.maven.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.embedder.MavenConsole;

public class MavenProjectsProcessorPluginsResolvingTask extends MavenProjectsProcessorBasicTask {
  private org.apache.maven.project.MavenProject myNativeMavenProject;

  public MavenProjectsProcessorPluginsResolvingTask(MavenProject project,
                                                    org.apache.maven.project.MavenProject nativeMavenProject,
                                                    MavenProjectsTree tree) {
    super(project, tree);
    myNativeMavenProject = nativeMavenProject;
  }

  public void perform(Project project, MavenEmbeddersManager embeddersManager, MavenConsole console, MavenProcess process)
    throws MavenProcessCanceledException {
    myTree.resolvePlugins(myMavenProject, myNativeMavenProject, embeddersManager, console, process);
  }
}