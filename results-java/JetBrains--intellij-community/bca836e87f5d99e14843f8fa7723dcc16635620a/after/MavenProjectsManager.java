package org.jetbrains.idea.maven.project;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.importing.*;
import org.jetbrains.idea.maven.runner.SoutMavenConsole;
import org.jetbrains.idea.maven.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@State(name = "MavenProjectsManager", storages = {@Storage(id = "default", file = "$PROJECT_FILE$")})
public class MavenProjectsManager extends SimpleProjectComponent implements PersistentStateComponent<MavenProjectsManagerState>,
                                                                            SettingsSavingComponent {
  private static final int IMPORT_DELAY = 1000;

  private final AtomicBoolean isInitialized = new AtomicBoolean();

  private MavenProjectsManagerState myState = new MavenProjectsManagerState();

  private MavenEmbeddersManager myEmbeddersManager;

  private MavenProjectsTree myProjectsTree;
  private MavenProjectsManagerWatcher myWatcher;

  private MavenProjectsProcessor myReadingProcessor;
  private MavenProjectsProcessor myQuickResolvingProcessor;
  private MavenProjectsProcessor myResolvingProcessor;
  private MavenProjectsProcessor myPluginsResolvingProcessor;
  private MavenProjectsProcessor myFoldersResolvingProcessor;
  private MavenProjectsProcessor myArtifactsDownloadingProcessor;
  private MavenProjectsProcessor myPostProcessor;

  private MergingUpdateQueue myImportingQueue;
  private final Set<MavenProject> myProjectsToImport = new THashSet<MavenProject>();

  private final EventDispatcher<MavenProjectsTree.Listener> myProjectsTreeDispatcher
    = EventDispatcher.create(MavenProjectsTree.Listener.class);
  private final List<Listener> myManagerListeners = ContainerUtil.createEmptyCOWList();

  public static MavenProjectsManager getInstance(Project p) {
    return p.getComponent(MavenProjectsManager.class);
  }

  public MavenProjectsManager(Project project) {
    super(project);
  }

  public MavenProjectsManagerState getState() {
    if (isInitialized()) {
      applyTreeToState();
    }
    return myState;
  }

  public void loadState(MavenProjectsManagerState state) {
    myState = state;
    if (isInitialized()) {
      applyStateToTree();
      scheduleReadAllProjects();
    }
  }

  public MavenGeneralSettings getGeneralSettings() {
    return getWorkspaceSettings().generalSettings;
  }

  public MavenImportingSettings getImportingSettings() {
    return getWorkspaceSettings().importingSettings;
  }

  public MavenDownloadingSettings getDownloadingSettings() {
    return getWorkspaceSettings().downloadingSettings;
  }

  private MavenWorkspaceSettings getWorkspaceSettings() {
    return MavenWorkspaceSettingsComponent.getInstance(myProject).getState();
  }

  public File getLocalRepository() {
    return getGeneralSettings().getEffectiveLocalRepository();
  }

  @Override
  public void initComponent() {
    if (!isNormalProject()) return;

    StartupManagerEx.getInstanceEx(myProject).registerStartupActivity(new Runnable() {
      public void run() {
        boolean wasMavenized = !myState.originalFiles.isEmpty();
        if (!wasMavenized) return;
        initMavenized();
      }
    });
  }

  private void initMavenized() {
    doInit(true);
  }

  private void initNew(List<VirtualFile> files, List<String> profiles) {
    myState.originalFiles = MavenUtil.collectPaths(files);
    myState.activeProfiles = profiles;
    doInit(false);
  }

  @TestOnly
  public void initForTests() {
    doInit(true);
  }

  private void doInit(boolean tryToLoadExistingTree) {
    if (isInitialized.getAndSet(true)) return;

    initProjectsTree(tryToLoadExistingTree);

    initWorkers();
    listenForSettingsChanges();
    listenForProjectsTreeChanges();

    runWhenInitialized(new DumbAwareRunnable() {
      public void run() {
        if (!isUnitTestMode()) {
          fireActivated();
          listenForExternalChanges();
        }
        scheduleReadAllProjects();
      }
    });
  }

  private void initProjectsTree(boolean tryToLoadExisting) {
    if (tryToLoadExisting) {
      File file = getProjectsTreeFile();
      try {
        if (file.exists()) {
          myProjectsTree = MavenProjectsTree.read(file);
        }
      }
      catch (IOException e) {
        MavenLog.LOG.info(e);
      }
    }

    if (myProjectsTree == null) myProjectsTree = new MavenProjectsTree();
    applyStateToTree();
    myProjectsTree.addListener(myProjectsTreeDispatcher.getMulticaster());
  }

  private void applyTreeToState() {
    myState.originalFiles = myProjectsTree.getManagedFilesPaths();
    myState.activeProfiles = myProjectsTree.getActiveProfiles();
    myState.ignoredFiles = new THashSet<String>(myProjectsTree.getIgnoredFilesPaths());
    myState.ignoredPathMasks = myProjectsTree.getIgnoredFilesPatterns();
  }

  private void applyStateToTree() {
    myProjectsTree.resetManagedFilesPathsAndProfiles(myState.originalFiles, myState.activeProfiles);
    myProjectsTree.setIgnoredFilesPaths(new ArrayList<String>(myState.ignoredFiles));
    myProjectsTree.setIgnoredFilesPatterns(myState.ignoredPathMasks);
  }

  public void save() {
    if (myProjectsTree != null) {
      try {
        myProjectsTree.save(getProjectsTreeFile());
      }
      catch (IOException e) {
        MavenLog.LOG.error(e);
      }
    }
  }

  private File getProjectsTreeFile() {
    File dir = MavenUtil.getPluginSystemDir("Projects/" + myProject.getLocationHash());
    dir.mkdirs();
    return new File(dir, "tree.dat");
  }

  private void initWorkers() {
    myEmbeddersManager = new MavenEmbeddersManager(getGeneralSettings());

    myReadingProcessor = new MavenProjectsProcessor(myProject, ProjectBundle.message("maven.reading"), myEmbeddersManager);
    myQuickResolvingProcessor = new MavenProjectsProcessor(myProject, ProjectBundle.message("maven.resolving"), myEmbeddersManager);
    myResolvingProcessor = new MavenProjectsProcessor(myProject, ProjectBundle.message("maven.resolving"), myEmbeddersManager);
    myPluginsResolvingProcessor = new MavenProjectsProcessor(myProject,
                                                             ProjectBundle.message("maven.downloading.plugins"),
                                                             myEmbeddersManager);
    myFoldersResolvingProcessor = new MavenProjectsProcessor(myProject,
                                                             ProjectBundle.message("maven.updating.folders"),
                                                             myEmbeddersManager);
    myArtifactsDownloadingProcessor = new MavenProjectsProcessor(myProject, ProjectBundle.message("maven.downloading"), myEmbeddersManager);
    myPostProcessor = new MavenProjectsProcessor(myProject, ProjectBundle.message("maven.post.processing"), myEmbeddersManager);

    myWatcher = new MavenProjectsManagerWatcher(myProject, myProjectsTree, getGeneralSettings(), myReadingProcessor, myEmbeddersManager);

    myImportingQueue = new MavenMergingUpdateQueue(getComponentName() + ": Importing queue", IMPORT_DELAY, !isUnitTestMode());
    myImportingQueue.setPassThrough(false); // by default in unit-test mode it executes request right-away
    MavenUserAwareMegringUpdateQueueHelper.attachTo(myProject, myImportingQueue);
  }

  private void listenForSettingsChanges() {
    getImportingSettings().addListener(new MavenImportingSettings.Listener() {
      public void createModuleGroupsChanged() {
        scheduleImport();
      }

      public void createModuleForAggregatorsChanged() {
        scheduleImport();
      }
    });
  }

  private void listenForProjectsTreeChanges() {
    myProjectsTree.addListener(new MavenProjectsTree.ListenerAdapter() {
      public void projectsRead(List<MavenProject> projects) {
        for (MavenProject each : projects) {
          scheduleResolvingTasks(each);
        }
        scheduleImport(projects);
      }

      public void projectResolved(boolean quickResolve, MavenProject project, org.apache.maven.project.MavenProject nativeMavenProject) {
        if (!project.isValid()) return;
        if (quickResolve) {
          schedulePluginsResolving(project, nativeMavenProject);
        }
        scheduleImport(project);
      }

      public void projectRemoved(MavenProject project) {
        unscheduleAllTasks(project);
      }

      @Override
      public void projectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored) {
        scheduleImport();
      }
    });
  }

  public void listenForExternalChanges() {
    myWatcher.start();
  }

  @Override
  public void projectClosed() {
    if (!isInitialized.getAndSet(false)) return;

    Disposer.dispose(myImportingQueue);

    myWatcher.stop();

    myReadingProcessor.cancelAndStop();
    myQuickResolvingProcessor.cancelAndStop();
    myResolvingProcessor.cancelAndStop();
    myPluginsResolvingProcessor.cancelAndStop();
    myFoldersResolvingProcessor.cancelAndStop();
    myArtifactsDownloadingProcessor.cancelAndStop();
    myPostProcessor.cancelAndStop();

    myEmbeddersManager.release();
  }

  private boolean isInitialized() {
    return isInitialized.get();
  }

  public boolean isMavenizedProject() {
    return isInitialized();
  }

  public boolean isMavenizedModule(Module m) {
    return "true".equals(m.getOptionValue(getMavenizedModuleOptionName()));
  }

  public void setMavenizedModules(List<Module> modules, boolean mavenized) {
    for (Module m : modules) {
      if (mavenized) {
        m.setOption(getMavenizedModuleOptionName(), "true");
      }
      else {
        m.clearOption(getMavenizedModuleOptionName());
      }
    }
  }

  private String getMavenizedModuleOptionName() {
    return getComponentName() + ".isMavenModule";
  }

  @TestOnly
  public void resetManagedFilesAndProfilesInTests(List<VirtualFile> files, List<String> profiles) {
    myWatcher.resetManagedFilesAndProfilesInTests(files, profiles);
  }

  public void addManagedFilesWithProfiles(List<VirtualFile> files, List<String> profiles) {
    if (!isInitialized()) {
      initNew(files, profiles);
    }
    else {
      myWatcher.addManagedFilesWithProfiles(files, profiles);
    }
  }

  public void addManagedFiles(List<VirtualFile> files) {
    addManagedFilesWithProfiles(files, Collections.<String>emptyList());
  }

  public void removeManagedFiles(List<VirtualFile> files) {
    myWatcher.removeManagedFiles(files);
  }

  public boolean isManagedFile(VirtualFile f) {
    if (!isInitialized()) return false;
    return myProjectsTree.isManagedFile(f);
  }

  public List<String> getActiveProfiles() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getActiveProfiles();
  }

  public List<String> getAvailableProfiles() {
    Set<String> result = new THashSet<String>();
    for (MavenProject each : getProjects()) {
      result.addAll(each.getProfilesIds());
    }
    return new ArrayList<String>(result);
  }

  public void setActiveProfiles(List<String> profiles) {
    myWatcher.setActiveProfiles(profiles);
  }

  public List<MavenProject> getProjects() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getProjects();
  }

  public List<MavenProject> getNonIgnoredProjects() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getNonIgnoredProjects();
  }

  public List<VirtualFile> getProjectsFiles() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getProjectsFiles();
  }

  public MavenProject findProject(VirtualFile f) {
    if (!isInitialized()) return null;
    return myProjectsTree.findProject(f);
  }

  public MavenProject findProject(MavenId id) {
    if (!isInitialized()) return null;
    return myProjectsTree.findProject(id);
  }

  public MavenProject findProject(Module module) {
    VirtualFile f = findPomFile(module);
    return f == null ? null : findProject(f);
  }

  private VirtualFile findPomFile(Module module) {
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      final VirtualFile virtualFile = root.findChild(MavenConstants.POM_XML);
      if (virtualFile != null) {
        return virtualFile;
      }
    }
    return null;
  }

  public MavenProject findAggregator(MavenProject module) {
    if (!isInitialized()) return null;
    return myProjectsTree.findAggregator(module);
  }

  public List<MavenProject> getModules(MavenProject aggregator) {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getModules(aggregator);
  }

  public List<String> getIgnoredFilesPaths() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getIgnoredFilesPaths();
  }

  public void setIgnoredFilesPaths(List<String> paths) {
    if (!isInitialized()) return;
    myProjectsTree.setIgnoredFilesPaths(paths);
  }

  public boolean getIgnoredState(MavenProject project) {
    if (!isInitialized()) return false;
    return myProjectsTree.getIgnoredState(project);
  }

  public void setIgnoredState(List<MavenProject> projects, boolean ignored) {
    if (!isInitialized()) return;
    myProjectsTree.setIgnoredState(projects, ignored);
  }

  public List<String> getIgnoredFilesPatterns() {
    if (!isInitialized()) return Collections.emptyList();
    return myProjectsTree.getIgnoredFilesPatterns();
  }

  public void setIgnoredFilesPatterns(List<String> patterns) {
    if (!isInitialized()) return;
    myProjectsTree.setIgnoredFilesPatterns(patterns);
  }

  public boolean isIgnored(MavenProject project) {
    if (!isInitialized()) return false;
    return myProjectsTree.isIgnored(project);
  }

  @TestOnly
  public MavenProjectsTree getProjectsTreeForTests() {
    return myProjectsTree;
  }

  private void scheduleReadAllProjects() {
    myReadingProcessor.scheduleTask(new MavenProjectsProcessorReadingTask(myProject, myProjectsTree, getGeneralSettings()));
  }

  private void scheduleResolvingTasks(final MavenProject project) {
    runWhenInitialized(new DumbAwareRunnable() {
      public void run() {
        myQuickResolvingProcessor.scheduleTask(new MavenProjectsProcessorResolvingTask(true,
                                                                                       project,
                                                                                       myProjectsTree,
                                                                                       getGeneralSettings()));
        myResolvingProcessor.scheduleTask(new MavenProjectsProcessorResolvingTask(false,
                                                                                  project,
                                                                                  myProjectsTree,
                                                                                  getGeneralSettings()));
      }
    });
  }

  public void scheduleFoldersResolving() {
    runWhenInitialized(new DumbAwareRunnable() {
      public void run() {
        for (MavenProject each : getProjects()) {
          myFoldersResolvingProcessor.scheduleTask(new MavenProjectsProcessorFoldersResolvingTask(each,
                                                                                                  getImportingSettings(),
                                                                                                  myProjectsTree));
        }
      }
    });
  }

  private void schedulePluginsResolving(final MavenProject project, final org.apache.maven.project.MavenProject nativeMavenProject) {
    runWhenInitialized(new DumbAwareRunnable() {
      public void run() {
        myPluginsResolvingProcessor.scheduleTask(new MavenProjectsProcessorPluginsResolvingTask(project,
                                                                                                nativeMavenProject,
                                                                                                myProjectsTree));
      }
    });
  }

  public void scheduleArtifactsDownloading() {
    runWhenInitialized(new DumbAwareRunnable() {
      public void run() {
        for (MavenProject each : getProjects()) {
          myArtifactsDownloadingProcessor.scheduleTask(new MavenProjectsProcessorArtifactsDownloadingTask(each,
                                                                                                          myProjectsTree,
                                                                                                          getDownloadingSettings()));
        }
      }
    });
  }

  private void scheduleImport(MavenProject project) {
    scheduleImport(Collections.singletonList(project));
  }

  private void scheduleImport(List<MavenProject> projects) {
    synchronized (myProjectsToImport) {
      myProjectsToImport.addAll(projects);
    }
    scheduleImport();
  }

  private void scheduleImport() {
    runWhenInitialized(new Runnable() {
      public void run() {
        myImportingQueue.queue(new Update(this) {
          public void run() {
            importProjects();
          }
        });
      }
    });
  }

  private void runWhenInitialized(Runnable runnable) {
    MavenUtil.runWhenInitialized(myProject, runnable);
  }

  @TestOnly
  public void scheduleImportInTests(List<VirtualFile> projectFiles) {
    for (VirtualFile each : projectFiles) {
      MavenProject project = findProject(each);
      if (project != null) {
        scheduleImport(project);
      }
    }
  }

  private void schedulePostImportTasts(List<MavenProjectsProcessorPostConfigurationTask> postTasks) {
    for (MavenProjectsProcessorPostConfigurationTask each : postTasks) {
      myPostProcessor.scheduleTask(each);
    }
  }

  private void unscheduleAllTasks(MavenProject project) {
    MavenProjectsProcessorEmptyTask dummyTask = new MavenProjectsProcessorEmptyTask(project);

    myProjectsToImport.remove(project);

    myQuickResolvingProcessor.removeTask(dummyTask);
    myResolvingProcessor.removeTask(dummyTask);
    myPluginsResolvingProcessor.removeTask(dummyTask);
    myFoldersResolvingProcessor.removeTask(dummyTask);
    myArtifactsDownloadingProcessor.removeTask(dummyTask);
    myPostProcessor.removeTask(dummyTask);
  }

  public void waitForReadingCompletion() {
    waitForTasksCompletionAndDo(Collections.<MavenProjectsProcessor>emptyList(), null);
  }

  public void waitForQuickResolvingCompletion() {
    waitForTasksCompletionAndDo(myQuickResolvingProcessor, null);
  }

  public void waitForQuickResolvingCompletionAndImport() {
    waitForTasksCompletionAndImport(myQuickResolvingProcessor);
  }

  public void waitForResolvingCompletionAndImport() {
    waitForTasksCompletionAndImport(myResolvingProcessor);
  }

  public void waitForFoldersResolvingCompletionAndImport() {
    waitForTasksCompletionAndDo(myFoldersResolvingProcessor, new Runnable() {
      public void run() {
        updateProjectFolders(false);
      }
    });
  }

  public void waitForPluginsResolvingCompletion() {
    waitForTasksCompletionAndDo(myPluginsResolvingProcessor, null);
  }

  public void waitForArtifactsDownloadingCompletion() {
    waitForTasksCompletionAndDo(Arrays.asList(myQuickResolvingProcessor, myArtifactsDownloadingProcessor), null);
  }

  public void waitForPostImportTasksCompletion() {
    myPostProcessor.waitForCompletion();
  }

  private void waitForTasksCompletionAndImport(MavenProjectsProcessor processor) {
    waitForTasksCompletionAndDo(processor, new Runnable() {
      public void run() {
        MavenUtil.invokeInDispatchThread(myProject, new Runnable() {
          public void run() {
            importProjects();
          }
        });
      }
    });
  }

  private void waitForTasksCompletionAndDo(MavenProjectsProcessor processor, Runnable runnable) {
    waitForTasksCompletionAndDo(Collections.singletonList(processor), runnable);
  }

  private void waitForTasksCompletionAndDo(List<MavenProjectsProcessor> processors, Runnable runnable) {
    FileDocumentManager.getInstance().saveAllDocuments();

    myReadingProcessor.waitForCompletion();
    for (MavenProjectsProcessor each : processors) {
      each.waitForCompletion();
    }

    if (runnable != null) runnable.run();
  }

  public void flushPendingImportRequestsInTests() {
    myImportingQueue.flush();
  }

  public void updateProjectTargetFolders() {
    updateProjectFolders(true);
  }

  private void updateProjectFolders(final boolean targetFoldersOnly) {
    MavenUtil.invokeInDispatchThread(myProject, new Runnable() {
      public void run() {
        MavenFoldersConfigurator.updateProjectFolders(myProject, targetFoldersOnly);
        VirtualFileManager.getInstance().refresh(false);
      }
    });
  }

  public void importProjectOrAllAvailablePomFiles() {
    if (!isMavenizedProject()) {
      addManagedFiles(collectAllAvailablePomFiles());
    }
    waitForReadingCompletion();
    importProjects();
  }

  public List<Module> importProjects() {
    return importProjects(new MavenDefaultModuleModelsProvider(myProject),
                          new MavenDefaultProjectLibrariesProvider(myProject));
  }

  public List<Module> importProjects(final MavenModuleModelsProvider moduleModelsProvider,
                                     final MavenProjectLibrariesProvider librariesProvider) {
    List<Module> result = new WriteAction<List<Module>>() {
      protected void run(Result<List<Module>> result) throws Throwable {
        Set<MavenProject> projectsToImport;
        synchronized (myProjectsToImport) {
          projectsToImport = new THashSet<MavenProject>(myProjectsToImport);
          myProjectsToImport.clear();
        }

        MavenProjectImporter importer = new MavenProjectImporter(myProject,
                                                                 myProjectsTree,
                                                                 getFileToModuleMapping(),
                                                                 projectsToImport,
                                                                 moduleModelsProvider,
                                                                 librariesProvider,
                                                                 getImportingSettings());

        schedulePostImportTasts(importer.importProject());
        result.setResult(importer.getCreatedModules());
      }
    }.execute().getResultObject();
    VirtualFileManager.getInstance().refresh(isNormalProject());
    return result;
  }

  private Map<VirtualFile, Module> getFileToModuleMapping() {
    Map<VirtualFile, Module> result = new THashMap<VirtualFile, Module>();
    for (Module each : ModuleManager.getInstance(myProject).getModules()) {
      VirtualFile f = findPomFile(each);
      if (f != null) result.put(f, each);
    }
    return result;
  }

  private List<VirtualFile> collectAllAvailablePomFiles() {
    List<VirtualFile> result = new ArrayList<VirtualFile>(getFileToModuleMapping().keySet());

    VirtualFile pom = myProject.getBaseDir().findChild(MavenConstants.POM_XML);
    if (pom != null) result.add(pom);

    return result;
  }

  public MavenDomDependency addDependency(final MavenProject mavenProject, final MavenId id) {
    final MavenArtifact[] artifact = new MavenArtifact[1];

    try {
      MavenUtil.run(myProject, "Downloading dependency...", new MavenTask() {
        public void run(MavenProgressIndicator process) throws MavenProcessCanceledException {
          artifact[0] = myProjectsTree.downloadArtifact(mavenProject, id, myEmbeddersManager, new SoutMavenConsole(), process);
        }
      });
    }
    catch (MavenProcessCanceledException ignore) {
      return null;
    }

    VirtualFile file = mavenProject.getFile();
    PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);

    MavenDomDependency result = new WriteCommandAction<MavenDomDependency>(myProject, "Add Maven Dependency", psiFile) {
      protected void run(Result<MavenDomDependency> result) throws Throwable {
        result.setResult(mavenProject.addDependency(myProject, artifact[0]));
      }
    }.execute().getResultObject();

    return result;
  }

  public void addManagerListener(Listener listener) {
    myManagerListeners.add(listener);
  }

  public void addProjectsTreeListener(MavenProjectsTree.Listener listener) {
    myProjectsTreeDispatcher.addListener(listener);
  }

  public MavenProjectsProcessor.Handler getQuickResolvingProcessorHandler() {
    return myQuickResolvingProcessor.getHandler();
  }

  public MavenProjectsProcessor.Handler getResolvingProcessorHandler() {
    return myResolvingProcessor.getHandler();
  }

  public MavenProjectsProcessor.Handler getFoldersUpdatingProcessorHandler() {
    return myFoldersResolvingProcessor.getHandler();
  }

  public MavenProjectsProcessor.Handler getPluginDownloadingProcessorHandler() {
    return myPluginsResolvingProcessor.getHandler();
  }

  @TestOnly
  public void fireActivatedInTests() {
    fireActivated();
  }

  private void fireActivated() {
    for (Listener each : myManagerListeners) {
      each.activated();
    }
  }

  public interface Listener {
    void activated();
  }
}