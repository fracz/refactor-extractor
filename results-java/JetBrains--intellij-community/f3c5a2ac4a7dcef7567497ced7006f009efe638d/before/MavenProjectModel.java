package org.jetbrains.idea.maven.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.core.MavenCoreSettings;
import org.jetbrains.idea.maven.core.MavenLog;
import org.jetbrains.idea.maven.core.util.MavenId;
import org.jetbrains.idea.maven.core.util.Path;
import org.jetbrains.idea.maven.core.util.ProjectId;
import org.jetbrains.idea.maven.core.util.Tree;
import org.jetbrains.idea.maven.embedder.CustomArtifact;
import org.jetbrains.idea.maven.embedder.CustomWagonManager;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;

import java.io.File;
import java.util.*;

public class MavenProjectModel {
  private List<Node> myRootProjects = new ArrayList<Node>();
  private HashMap<ProjectId, VirtualFile> myProjectIdToFileMapping = new HashMap<ProjectId, VirtualFile>();
  private List<String> myProfiles = new ArrayList<String>();

  private List<Listener> myListeners = new ArrayList<Listener>();

  public void read(Collection<VirtualFile> filesToImport,
                   Map<VirtualFile, Module> existingModules,
                   List<String> activeProfiles,
                   MavenCoreSettings mavenSettings,
                   MavenImporterSettings importerSettings,
                   MavenProcess p) throws CanceledException {
    myProfiles = activeProfiles;

    update(filesToImport, mavenSettings, p);

    updateModules(existingModules);
    resolveIntermoduleDependencies();
    MavenModuleNameMapper.map(this, importerSettings.getDedicatedModuleDir());
  }

  private void updateModules(final Map<VirtualFile, Module> existingModules) {
    visit(new PlainNodeVisitor() {
      public void visit(Node node) {
        Module module = existingModules.get(node.getFile());
        if (module != null) node.setModule(module);
      }
    });
  }

  private void resolveIntermoduleDependencies() {
    visit(new PlainNodeVisitor() {
      public void visit(Node node) {
        for (Artifact each : node.getDependencies()) {
          VirtualFile pomFile = myProjectIdToFileMapping.get(new ProjectId(each));
          if (pomFile != null) {
            node.removeUnresolvedDependency(each);
            each.setFile(new File(pomFile.getPath()));
            each.setResolved(true);
          }
        }
      }
    });
  }

  public void update(VirtualFile f, MavenCoreSettings mavenSettings, MavenProcess p) throws CanceledException {
    update(Collections.singleton(f), mavenSettings, p);
  }

  private void update(Collection<VirtualFile> files, MavenCoreSettings mavenSettings, MavenProcess p) throws CanceledException {
    MavenEmbedder e = MavenEmbedderFactory.createEmbedderForRead(mavenSettings, myProjectIdToFileMapping);

    try {
      MavenProjectReader reader = new MavenProjectReader(e);
      Set<VirtualFile> updatedFiles = new HashSet<VirtualFile>();

      for (VirtualFile each : files) {
        Node n = findProject(each);
        if (n == null) {
          doAdd(each, reader, updatedFiles, p);
        }
        else {
          doUpdate(n, reader, false, updatedFiles, p);
        }
      }
    }
    finally {
      MavenEmbedderFactory.releaseEmbedder(e);
    }
  }

  private void doAdd(final VirtualFile f, MavenProjectReader reader, Set<VirtualFile> updatedFiles, MavenProcess p)
      throws CanceledException {
    Node newProject = new Node(f, null);

    Node parent = visit(new NodeVisitor<Node>() {
      public void visit(Node node) {
        if (node.getExistingModuleFiles().contains(f)) {
          setResult(node);
        }
      }
    });

    if (parent != null) {
      parent.mySubProjects.add(newProject);
    }
    else {
      myRootProjects.add(newProject);
    }

    doUpdate(newProject, reader, true, updatedFiles, p);
  }

  private void doUpdate(Node n, MavenProjectReader reader, boolean isNew, Set<VirtualFile> updatedFiles, MavenProcess p)
      throws CanceledException {
    if (updatedFiles.contains(n.getFile())) return;
    p.checkCanceled();
    p.setText(ProjectBundle.message("maven.reading", n.getPath()));
    p.setText2("");

    List<Node> oldModules = n.mySubProjects;
    List<Node> newModules = new ArrayList<Node>();

    if (!isNew) myProjectIdToFileMapping.remove(n.getProjectId());
    n.read(reader, myProfiles);
    myProjectIdToFileMapping.put(n.getProjectId(), n.getFile());
    if (isNew) {
      fireAdded(n);
    }
    else {
      fireUpdated(n);
    }

    for (VirtualFile each : n.getExistingModuleFiles()) {
      Node child = findProject(each);
      boolean isNewChild = child == null;
      if (isNewChild) {
        child = new Node(each, null);
      }
      doUpdate(child, reader, isNewChild, updatedFiles, p);
      newModules.add(child);
      myRootProjects.remove(child);
    }

    oldModules.removeAll(newModules);
    for (Node each : oldModules) {
      doRemove(each);
    }

    n.mySubProjects = newModules;
  }

  public void remove(VirtualFile f) {
    final Node n = findProject(f);
    if (n == null) return;

    List<Node> list;
    if (myRootProjects.contains(n)) {
      list = myRootProjects;
    }
    else {
      list = visit(new NodeVisitor<List<Node>>() {
        public void visit(Node node) {
          if (node.mySubProjects.contains(n)) {
            setResult(node.mySubProjects);
          }
        }
      });
    }
    if (list == null) return;

    list.remove(n);
    doRemove(n);
  }

  private void doRemove(Node n) {
    for (Node each : n.getSubProjects()) {
      doRemove(each);
    }
    myProjectIdToFileMapping.remove(n.getProjectId());
    fireRemoved(n);
  }

  public List<Node> getRootProjects() {
    return myRootProjects;
  }

  public List<Node> getProjects() {
    final List<Node> result = new ArrayList<Node>();
    visit(new PlainNodeVisitor() {
      public void visit(Node node) {
        result.add(node);
      }
    });
    return result;
  }

  public Node findProject(final VirtualFile f) {
    return findProject(f, false);
  }

  private Node findProject(final VirtualFile f, final boolean rootsOnly) {
    return visit(new NodeVisitor<Node>() {
      public void visit(final Node node) {
        if (node.getFile() == f) {
          setResult(node);
        }
      }

      public Iterable<Node> getChildren(final Node node) {
        return rootsOnly ? null : super.getChildren(node);
      }
    });
  }

  public Node findProject(final ProjectId id) {
    return visit(new NodeVisitor<Node>() {
      public void visit(Node node) {
        if (node.getProjectId().equals(id)) setResult(node);
      }
    });
  }

  public void resolve(Project project,
                      MavenProcess p,
                      MavenCoreSettings coreSettings,
                      MavenArtifactSettings artifactSettings) throws CanceledException {

    MavenEmbedder embedder = MavenEmbedderFactory.createEmbedderForResolve(coreSettings, myProjectIdToFileMapping);

    try {
      List<Node> projects = getProjects();
      List<Artifact> allArtifacts = new ArrayList<Artifact>();

      MavenProjectReader projectReader = new MavenProjectReader(embedder);
      for (Node each : projects) {
        p.checkCanceled();
        p.setText(ProjectBundle.message("maven.resolving.pom", FileUtil.toSystemDependentName(each.getPath())));
        p.setText2("");
        each.resolve(projectReader);
      }

      for (Node each : projects) {
        p.checkCanceled();
        p.setText(ProjectBundle.message("maven.generating.sources.pom", FileUtil.toSystemDependentName(each.getPath())));
        p.setText2("");
        each.generateSources(projectReader);
      }

      // We have to refresh all the resolved artifacts manually in order to
      // update all the VirtualFilePointers. It is not enough to call
      // VirtualFileManager.refresh() since the newly created files will be only
      // picked by FS when FileWathcer finiches its work. And in the case of import
      // it doesn't finics in time.
      // I couldn't manage to write a test for this since behaviour of VirtualFileManager
      // and FileWatcher differs from real-life execution.
      refreshResolvedArtifacts(allArtifacts);

      MavenArtifactDownloader d = new MavenArtifactDownloader(artifactSettings, embedder, p);
      d.download(project, projects, false);
    }
    finally {
      MavenEmbedderFactory.releaseEmbedder(embedder);
    }
  }

  private void refreshResolvedArtifacts(List<Artifact> artifacts) {
    for (Artifact a : artifacts) {
      if (!a.isResolved()) continue;
      LocalFileSystem.getInstance().refreshAndFindFileByIoFile(a.getFile());
    }
  }

  public abstract static class NodeVisitor<Result> extends Tree.VisitorAdapter<Node, Result> {
    public boolean shouldVisit(Node node) {
      return node.isIncluded();
    }

    public Iterable<Node> getChildren(Node node) {
      return node.mySubProjects;
    }
  }

  public abstract static class PlainNodeVisitor extends NodeVisitor<Object> {
  }

  public <Result> Result visit(NodeVisitor<Result> visitor) {
    return Tree.visit(myRootProjects, visitor);
  }


  public void addListener(Listener l) {
    myListeners.add(l);
  }

  private void fireAdded(Node n) {
    for (Listener each : myListeners) {
      each.projectAdded(n);
    }
  }

  private void fireUpdated(Node n) {
    for (Listener each : myListeners) {
      each.projectUpdated(n);
    }
  }

  private void fireRemoved(Node n) {
    for (Listener each : myListeners) {
      each.projectRemoved(n);
    }
  }

  public static class Node {
    private VirtualFile myPomFile;
    private Module myModule;

    private boolean myIncluded = true;

    private MavenProjectHolder myMavenProjectHolder;
    private List<Node> mySubProjects = new ArrayList<Node>();

    private List<String> myProfiles;

    private String myModuleName;
    private String myModulePath;

    private Set<Artifact> myUnresolvedArtifacts;

    private Node(@NotNull VirtualFile pomFile, Module module) {
      myPomFile = pomFile;
      myModule = module;
    }

    public List<String> getActiveProfiles() {
      return myProfiles;
    }

    public boolean isValid() {
      return myMavenProjectHolder.isValid();
    }

    public VirtualFile getFile() {
      return myPomFile;
    }

    @NotNull
    public String getPath() {
      return myPomFile.getPath();
    }

    public String getDirectory() {
      return myPomFile.getParent().getPath();
    }

    public VirtualFile getDirectoryFile() {
      return myPomFile.getParent();
    }

    public String getModuleName() {
      return myModuleName;
    }

    public void setModuleName(String moduleName) {
      myModuleName = moduleName;
    }

    public void setModuleFilePath(String modulePath) {
      myModulePath = modulePath;
    }

    public String getModuleFilePath() {
      return myModulePath;
    }

    @NotNull
    public MavenProject getMavenProject() {
      return myMavenProjectHolder.getMavenProject();
    }

    public String getProjectName() {
      return getMavenProject().getModel().getName();
    }

    public MavenId getMavenId() {
      return myMavenProjectHolder.getMavenId();
    }

    public ProjectId getProjectId() {
      return myMavenProjectHolder.getProjectId();
    }

    public String getPackaging() {
      String result = getMavenProject().getPackaging();
      return result == null ? "jar" : result;
    }

    public String getFinalName() {
      if (!isValid()) return getMavenId().artifactId + ".jar";
      return getMavenProject().getBuild().getFinalName();
    }

    public String getBuildDirectory() {
      if (!isValid()) return getDirectory() + File.separator + "target";
      return getMavenProject().getBuild().getDirectory();
    }

    public String getOutputDirectory() {
      if (!isValid()) return getDirectory() + File.separator + "target/classes";
      return getMavenProject().getBuild().getOutputDirectory();
    }

    public String getTestOutputDirectory() {
      if (!isValid()) return getDirectory() + File.separator + "target/test-classes";
      return getMavenProject().getBuild().getTestOutputDirectory();
    }

    public List<String> getCompileSourceRoots() {
      if (!isValid()) return Collections.emptyList();
      return getMavenProject().getCompileSourceRoots();
    }

    public List<String> getTestCompileSourceRoots() {
      if (!isValid()) return Collections.emptyList();
      return getMavenProject().getTestCompileSourceRoots();
    }

    public List<Resource> getResources() {
      if (!isValid()) return Collections.emptyList();
      return getMavenProject().getResources();
    }

    public List<Resource> getTestResources() {
      if (!isValid()) return Collections.emptyList();
      return getMavenProject().getTestResources();
    }

    public boolean isIncluded() {
      return myIncluded;
    }

    public void setIncluded(boolean included) {
      myIncluded = included;
    }

    public Module getModule() {
      return myModule;
    }

    public void setModule(Module m) {
      myModule = m;
    }

    public void read(MavenProjectReader r, List<String> profiles) throws CanceledException {
      myProfiles = profiles;
      myMavenProjectHolder = r.readProject(myPomFile.getPath(), myProfiles);

      try {
        CustomWagonManager wagonManager = (CustomWagonManager)r.getEmbedder().getPlexusContainer().lookup(WagonManager.ROLE);
        myUnresolvedArtifacts = wagonManager.getUnresolvedArtifacts();
        wagonManager.resetUnresolvedArtifacts();
      }
      catch (ComponentLookupException e) {
        myUnresolvedArtifacts = new HashSet<Artifact>();
        MavenLog.LOG.info(e);
      }

    }

    public void resolve(MavenProjectReader projectReader) throws CanceledException {
      MavenProjectHolder newHolder = projectReader.readProject(getPath(), myProfiles);
      if (newHolder.isValid()) myMavenProjectHolder = newHolder;
    }

    public void generateSources(MavenProjectReader projectReader) throws CanceledException {
      projectReader.generateSources(getMavenProject(), myProfiles);
    }

    public List<String> getProblems() {
      List<String> result = new ArrayList<String>();
      if (!isValid()) {
        result.add("Invalid Maven Model");
      }

      result.addAll(myMavenProjectHolder.getProblems());

      Artifact parent = getMavenProject().getParentArtifact();
      if (myUnresolvedArtifacts.contains(parent)) {
        result.add("Parent '" + parent + "' not found");
      }

      for (Map.Entry<String, String> each : collectAbsoluteModulePaths(myProfiles).entrySet()) {
        if (LocalFileSystem.getInstance().findFileByPath(each.getKey()) == null) {
          result.add("Missing module: '" + each.getValue() + "'");
        }
      }

      validate(getAllDependencies(), "dependency", result);
      validate(getExtensions(), "build extension", result);
      //validate(getPluginArtifacts(), "plugin", result);

      return result;
    }

    private void validate(List<Artifact> artifacts, String type, List<String> result) {
      for (Artifact each : artifacts) {
        if (!isResolved(each)) {
          result.add("Unresolved " + type + ": " + each);
        }
      }
    }

    private boolean isResolved(Artifact each) {
      if (!each.isResolved() || each.getFile() == null) return false;
      if (myUnresolvedArtifacts.contains(each)) return false;
      return !(each instanceof CustomArtifact && ((CustomArtifact)each).isStub());
    }

    public void removeUnresolvedDependency(Artifact a) {
      myUnresolvedArtifacts.remove(a);
    }

    public List<Node> getSubProjects() {
      return mySubProjects;
    }

    public List<VirtualFile> getExistingModuleFiles() {
      LocalFileSystem fs = LocalFileSystem.getInstance();

      List<VirtualFile> result = new ArrayList<VirtualFile>();
      for (String each : getModulePaths()) {
        VirtualFile f = fs.findFileByPath(each);
        if (f != null) result.add(f);

      }
      return result;
    }

    public List<String> getModulePaths() {
      return getModulePaths(myProfiles);
    }

    public List<String> getModulePaths(Collection<String> profiles) {
      return new ArrayList<String>(collectAbsoluteModulePaths(profiles).keySet());
    }

    private Map<String, String> collectAbsoluteModulePaths(Collection<String> profiles) {
      String basePath = getDirectory() + File.separator;
      Map<String, String> result = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> each : collectRelativeModulePaths(profiles).entrySet()) {
        result.put(new Path(basePath + each.getKey()).getPath(), each.getValue());
      }
      return result;
    }

    private Map<String, String> collectRelativeModulePaths(Collection<String> profiles) {
      LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
      Model model = getMavenProject().getModel();
      addModulesToList(model.getModules(), result);
      for (Profile profile : (List<Profile>)model.getProfiles()) {
        if (profiles == null || profiles.contains(profile.getId())) {
          addModulesToList(profile.getModules(), result);
        }
      }
      return result;
    }

    private void addModulesToList(List moduleNames, LinkedHashMap<String, String> result) {
      for (String name : (List<String>)moduleNames) {
        if (name.trim().length() == 0) continue;

        String originalName = name;
        // module name can be relative and contain either / of \\ separators

        name = FileUtil.toSystemIndependentName(name);
        if (!name.endsWith("/")) name += "/";
        name += Constants.POM_XML;

        result.put(name, originalName);
      }
    }

    public List<String> getAllProfiles() {
      if (!isValid()) return Collections.emptyList();

      Set<String> result = new HashSet<String>();
      for (Profile profile : (List<Profile>)getMavenProject().getModel().getProfiles()) {
        result.add(profile.getId());
      }
      return new ArrayList<String>(result);
    }

    public List<Artifact> getDependencies() {
      List<Artifact> result = new ArrayList<Artifact>();
      for (Artifact each : getAllDependencies()) {
        if (!isSupportedArtifact(each)) continue;
        result.add(each);
      }
      return result;
    }

    public List<Artifact> getExportableDependencies() {
      List<Artifact> result = new ArrayList<Artifact>();
      for (Artifact each : getDependencies()) {
        if (isExportableDependency(each)) result.add(each);
      }
      return result;
    }

    public List<Artifact> getAllDependencies() {
      if (!isValid()) return Collections.emptyList();

      Map<String, Artifact> projectIdToArtifact = new LinkedHashMap<String, Artifact>();

      for (Artifact artifact : (Collection<Artifact>)getMavenProject().getArtifacts()) {
        String projectId = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getClassifier();

        Artifact existing = projectIdToArtifact.get(projectId);
        if (existing == null ||
            new DefaultArtifactVersion(existing.getVersion()).compareTo(new DefaultArtifactVersion(artifact.getVersion())) < 0) {
          projectIdToArtifact.put(projectId, artifact);
        }
      }
      return new ArrayList<Artifact>(projectIdToArtifact.values());
    }

    private static boolean isSupportedArtifact(Artifact a) {
      String t = a.getType();
      return t.equalsIgnoreCase(Constants.JAR_TYPE) ||
             t.equalsIgnoreCase("test-jar") ||
             t.equalsIgnoreCase("ejb") ||
             t.equalsIgnoreCase("ear") ||
             t.equalsIgnoreCase("sar") ||
             t.equalsIgnoreCase("war") ||
             t.equalsIgnoreCase("ejb-client");
    }

    public boolean isExportableDependency(Artifact a) {
      if (a.isOptional()) return false;

      String scope = a.getScope();
      return scope.equals(Artifact.SCOPE_COMPILE) || scope.equals(Artifact.SCOPE_RUNTIME);
    }

    public List<MavenId> getPluginsIds() {
      List<MavenId> result = new ArrayList<MavenId>();
      for (Plugin plugin : getPlugins()) {
        result.add(new MavenId(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion()));
      }
      return result;
    }

    public List<Artifact> getPluginArtifacts() {
      if (!isValid()) return Collections.emptyList();

      List<Artifact> result = new ArrayList<Artifact>();
      for (Object each : getMavenProject().getPluginArtifacts()) {
        result.add((Artifact)each);
      }
      return result;
    }

    public List<Plugin> getPlugins() {
      if (!isValid()) return Collections.emptyList();

      List<Plugin> result = new ArrayList<Plugin>();

      collectPlugins(getMavenProject().getBuild(), result);
      //noinspection unchecked
      List<Profile> profiles = (List<Profile>)getMavenProject().getModel().getProfiles();
      if (profiles == null) return result;

      for (Profile profile : profiles) {
        if (profiles.contains(profile.getId())) {
          collectPlugins(profile.getBuild(), result);
        }
      }
      return result;
    }

    private void collectPlugins(BuildBase build, List<Plugin> result) {
      if (build == null) return;
      List<Plugin> plugins = (List<Plugin>)build.getPlugins();
      if (plugins == null) return;
      result.addAll(plugins);
    }

    public List<Artifact> getExtensions() {
      if (!isValid()) return Collections.emptyList();
      return new ArrayList<Artifact>(getMavenProject().getExtensionArtifacts());
    }


    @Nullable
    public String findPluginConfigurationValue(String groupId,
                                               String artifactId,
                                               String... nodeNames) {
      Xpp3Dom node = findPluginConfigurationNode(groupId, artifactId, nodeNames);
      if (node == null) return null;
      return node.getValue();
    }

    @Nullable
    public Xpp3Dom findPluginConfigurationNode(
        String groupId,
        String artifactId,
        String... nodeNames) {
      Plugin plugin = findPlugin(groupId, artifactId);
      if (plugin == null) return null;

      Xpp3Dom node = (Xpp3Dom)plugin.getConfiguration();
      if (node == null) return null;

      for (String name : nodeNames) {
        node = node.getChild(name);
        if (node == null) return null;
      }

      return node;
    }

    @Nullable
    public Plugin findPlugin(String groupId, String artifactId) {
      for (Plugin each : getPlugins()) {
        if (groupId.equals(each.getGroupId()) && artifactId.equals(each.getArtifactId())) return each;
      }
      return null;
    }

    public Properties getProperties() {
      if (!isValid()) return new Properties();
      return getMavenProject().getProperties();
    }
  }

  public static interface Listener {
    void projectAdded(Node n);

    void projectUpdated(Node n);

    void projectRemoved(Node n);
  }
}