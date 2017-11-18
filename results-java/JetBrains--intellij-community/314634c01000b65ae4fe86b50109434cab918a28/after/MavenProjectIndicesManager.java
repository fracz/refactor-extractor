package org.jetbrains.idea.maven.indices;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import gnu.trove.THashSet;
import org.apache.lucene.search.Query;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.project.MavenId;
import org.jetbrains.idea.maven.utils.MavenMergingUpdateQueue;
import org.jetbrains.idea.maven.utils.SimpleProjectComponent;
import org.sonatype.nexus.index.ArtifactInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MavenProjectIndicesManager extends SimpleProjectComponent {
  private volatile List<MavenIndex> myProjectIndices = new ArrayList<MavenIndex>();
  private final MergingUpdateQueue myUpdateQueue;

  public static MavenProjectIndicesManager getInstance(Project p) {
    return p.getComponent(MavenProjectIndicesManager.class);
  }

  public MavenProjectIndicesManager(Project project) {
    super(project);
    myUpdateQueue = new MavenMergingUpdateQueue(getClass().getSimpleName(), 1000, true, project);
  }

  @Override
  public void initComponent() {
    if (!isNormalProject()) return;
    doInit();
  }

  public void doInit() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      scheduleUpdateIndicesList();
    }

    getMavenProjectManager().addManagerListener(new MavenProjectsManager.Listener() {
      public void activated() {
        scheduleUpdateIndicesList();
      }
    });

    getMavenProjectManager().addProjectsTreeListener(new MavenProjectsTree.ListenerAdapter() {
      @Override
      public void projectsUpdated(List<MavenProject> updated, List<MavenProject> deleted) {
        scheduleUpdateIndicesList();
      }

      @Override
      public void projectResolved(MavenProject project, org.apache.maven.project.MavenProject nativeMavenProject) {
        scheduleUpdateIndicesList();
      }
    });
  }

  private void scheduleUpdateIndicesList() {
    myUpdateQueue.queue(new Update(MavenProjectIndicesManager.this) {
      public void run() {
        List<MavenIndex> newIndices = MavenIndicesManager.getInstance().ensureIndicesExist(
          getLocalRepository(), collectRemoteRepositories());
        myProjectIndices = newIndices;
      }
    });
  }

  private File getLocalRepository() {
    return MavenProjectsManager.getInstance(myProject).getLocalRepository();
  }

  private Set<String> collectRemoteRepositories() {
    Set<String> result = new THashSet<String>();

    for (MavenProject each : getMavenProjectManager().getProjects()) {
      for (MavenRemoteRepository eachRepository : each.getRemoteRepositories()) {
        String url = eachRepository.getUrl();
        if (url == null) continue;
        result.add(url);
      }
    }

    return result;
  }

  public List<MavenIndex> getIndices() {
    return new ArrayList<MavenIndex>(myProjectIndices);
  }

  public void scheduleUpdateAll() {
    MavenIndicesManager.getInstance().scheduleUpdate(myProjectIndices);
  }

  public void scheduleUpdate(List<MavenIndex> indices) {
    MavenIndicesManager.getInstance().scheduleUpdate(indices);
  }

  public MavenIndicesManager.IndexUpdatingState getUpdatingState(MavenIndex index) {
    return MavenIndicesManager.getInstance().getUpdatingState(index);
  }

  private MavenProjectsManager getMavenProjectManager() {
    return MavenProjectsManager.getInstance(myProject);
  }

  public Set<String> getGroupIds() {
    Set<String> result = getProjectGroupIds();
    for (MavenIndex each : myProjectIndices) {
      result.addAll(each.getGroupIds());
    }
    return result;
  }

  public Set<String> getArtifactIds(String groupId) {
    Set<String> result = getProjectArtifactIds(groupId);
    for (MavenIndex each : myProjectIndices) {
      result.addAll(each.getArtifactIds(groupId));
    }
    return result;
  }

  public Set<String> getVersions(String groupId, String artifactId) {
    Set<String> result = getProjectVersions(groupId, artifactId);
    for (MavenIndex each : myProjectIndices) {
      result.addAll(each.getVersions(groupId, artifactId));
    }
    return result;
  }

  public boolean hasGroupId(String groupId) {
    if (hasProjectGroupId(groupId)) return true;
    for (MavenIndex each : myProjectIndices) {
      if (each.hasGroupId(groupId)) return true;
    }
    return checkLocalRepository(groupId, null, null);
  }

  private boolean checkLocalRepository(String groupId, String artifactId, String version) {
    if (StringUtil.isEmpty(groupId)) return false;

    String relPath = groupId.replace('.', '/');

    if (artifactId != null) {
      relPath += "/" + artifactId;
      if (version != null) {
        relPath += "/" + version + "/" + artifactId + "-" + version + ".pom";
      }
    }
    File file = new File(getLocalRepository(), relPath);

    return file.exists();
  }

  public boolean hasArtifactId(String groupId, String artifactId) {
    if (hasProjectArtifactId(groupId, artifactId)) return true;
    for (MavenIndex each : myProjectIndices) {
      if (each.hasArtifactId(groupId, artifactId)) return true;
    }
    return checkLocalRepository(groupId, artifactId, null);
  }

  public boolean hasVersion(String groupId, String artifactId, String version) {
    if (hasProjectVersion(groupId, artifactId, version)) return true;
    for (MavenIndex each : myProjectIndices) {
      if (each.hasVersion(groupId, artifactId, version)) return true;
    }
    return checkLocalRepository(groupId, artifactId, version);
  }

  public Set<ArtifactInfo> search(Query query, int maxResult) {
    Set<ArtifactInfo> result = new THashSet<ArtifactInfo>();

    for (MavenIndex each : myProjectIndices) {
      int remained = maxResult - result.size();
      if (remained <= 0) break;
      result.addAll(each.search(query, remained));
    }

    return result;
  }

  private Set<String> getProjectGroupIds() {
    Set<String> result = new THashSet<String>();
    for (MavenId each : getProjectsIds()) {
      result.add(each.getGroupId());
    }
    return result;
  }

  private Set<String> getProjectArtifactIds(String groupId) {
    Set<String> result = new THashSet<String>();
    for (MavenId each : getProjectsIds()) {
      if (groupId.equals(each.getGroupId())) {
        result.add(each.getArtifactId());
      }
    }
    return result;
  }

  private Set<String> getProjectVersions(String groupId, String artifactId) {
    Set<String> result = new THashSet<String>();
    for (MavenId each : getProjectsIds()) {
      if (groupId.equals(each.getGroupId()) && artifactId.equals(each.getArtifactId())) {
        result.add(each.getVersion());
      }
    }
    return result;
  }

  private boolean hasProjectGroupId(String groupId) {
    return getProjectGroupIds().contains(groupId);
  }

  private boolean hasProjectArtifactId(String groupId, String artifactId) {
    return getProjectArtifactIds(groupId).contains(artifactId);
  }

  private boolean hasProjectVersion(String groupId, String artifactId, String version) {
    return getProjectVersions(groupId, artifactId).contains(version);
  }

  private Set<MavenId> getProjectsIds() {
    Set<MavenId> result = new THashSet<MavenId>();
    for (MavenProject each : MavenProjectsManager.getInstance(myProject).getProjects()) {
      result.add(each.getMavenId());
    }
    return result;
  }
}