package org.jetbrains.idea.maven.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import gnu.trove.THashMap;
import org.apache.maven.artifact.Artifact;
import org.jetbrains.idea.maven.embedder.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.utils.MavenConstants;

import java.io.File;
import java.util.*;

public class MavenArtifactDownloader {
  private final MavenDownloadingSettings mySettings;
  private final MavenEmbedderWrapper myEmbedder;
  private final MavenProcess myProgress;
  private final MavenProjectsTree myProjectsTree;
  private final List<MavenProject> myMavenProjects;

  public static void download(MavenProjectsTree projectsTree,
                              List<MavenProject> mavenProjects,
                              MavenDownloadingSettings settings,
                              boolean demand,
                              MavenEmbedderWrapper embedder,
                              MavenProcess p) throws MavenProcessCanceledException {
    new MavenArtifactDownloader(projectsTree, mavenProjects, settings, embedder, p).download(demand);
  }

  private MavenArtifactDownloader(MavenProjectsTree projectsTree,
                                  List<MavenProject> mavenProjects,
                                  MavenDownloadingSettings settings,
                                  MavenEmbedderWrapper embedder,
                                  MavenProcess p) {
    myProjectsTree = projectsTree;
    myMavenProjects = mavenProjects;
    mySettings = settings;
    myEmbedder = embedder;
    myProgress = p;
  }

  private void download(boolean demand) throws MavenProcessCanceledException {
    List<File> downloadedFiles = new ArrayList<File>();
    try {
      Map<MavenArtifact, Set<MavenRemoteRepository>> artifacts = collectArtifactsToDownload();

      if (shouldDownload(mySettings.getDownloadSources(), demand)) {
        download(MavenConstants.SOURCES_CLASSIFIER, artifacts, downloadedFiles);
      }

      if (shouldDownload(mySettings.getDownloadJavadoc(), demand)) {
        download(MavenConstants.JAVADOC_CLASSIFIER, artifacts, downloadedFiles);
      }
    }
    finally {
      scheduleFilesRefresh(downloadedFiles);
    }
  }

  private boolean shouldDownload(MavenDownloadingSettings.UPDATE_MODE level, boolean demand) {
    return level == MavenDownloadingSettings.UPDATE_MODE.ALWAYS || (level == MavenDownloadingSettings.UPDATE_MODE.ON_DEMAND && demand);
  }

  private void scheduleFilesRefresh(final List<File> downloadedFiles) {
    Runnable refreshTask = new Runnable() {
      public void run() {
        LocalFileSystem.getInstance().refreshIoFiles(downloadedFiles);
      }
    };

    if (ApplicationManager.getApplication().isUnitTestMode()
        || ApplicationManager.getApplication().isDispatchThread()) {
      refreshTask.run();
    }
    else {
      ApplicationManager.getApplication().invokeLater(refreshTask);
    }
  }

  private Map<MavenArtifact, Set<MavenRemoteRepository>> collectArtifactsToDownload() {
    Map<MavenArtifact, Set<MavenRemoteRepository>> result = new THashMap<MavenArtifact, Set<MavenRemoteRepository>>();

    for (MavenProject each : myMavenProjects) {
      List<MavenRemoteRepository> repositories = each.getRemoteRepositories();

      for (MavenArtifact eachDependency : each.getDependencies()) {
        if (!each.isSupportedDependency(eachDependency)) continue;
        if (Artifact.SCOPE_SYSTEM.equalsIgnoreCase(eachDependency.getScope())) continue;
        if (myProjectsTree.findProject(eachDependency.getMavenId()) != null) continue;

        Set<MavenRemoteRepository> registeredRepositories = result.get(eachDependency);
        if (registeredRepositories == null) {
          registeredRepositories = new LinkedHashSet<MavenRemoteRepository>();
          result.put(eachDependency, registeredRepositories);
        }
        registeredRepositories.addAll(repositories);
      }
    }
    return result;
  }

  private void download(String classifier,
                        Map<MavenArtifact, Set<MavenRemoteRepository>> libraryArtifacts,
                        List<File> downloadedFiles) throws MavenProcessCanceledException {
    myProgress.setText(ProjectBundle.message("maven.downloading.artifact", classifier));

    int step = 0;
    for (Map.Entry<MavenArtifact, Set<MavenRemoteRepository>> eachEntry : libraryArtifacts.entrySet()) {
      MavenArtifact eachArtifact = eachEntry.getKey();

      myProgress.checkCanceled();
      myProgress.setFraction(((double)step++) / libraryArtifacts.size());
      myProgress.setText2(eachArtifact.toString());

      Artifact a = myEmbedder.createArtifact(eachArtifact.getGroupId(),
                                             eachArtifact.getArtifactId(),
                                             eachArtifact.getVersion(),
                                             MavenConstants.TYPE_JAR,
                                             classifier);
      myEmbedder.resolve(a, new ArrayList<MavenRemoteRepository>(eachEntry.getValue()), myProgress);
      if (a.isResolved()) downloadedFiles.add(a.getFile());
    }
  }
}