package org.jetbrains.idea.maven.project.action;

import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

public class DownloadArtifactsForProjectAction extends MavenProjectsAction {
  protected void perform(MavenProjectsManager manager, List<MavenProject> mavenProjects) {
    manager.scheduleArtifactsDownloading(mavenProjects);
  }
}