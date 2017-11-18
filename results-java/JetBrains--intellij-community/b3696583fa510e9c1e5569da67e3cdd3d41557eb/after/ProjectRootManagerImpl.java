package com.intellij.openapi.roots.impl;

import com.intellij.AppTopics;
import com.intellij.ProjectTopics;
import com.intellij.ide.startup.CacheUpdater;
import com.intellij.ide.startup.FileSystemSynchronizer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleImpl;
import com.intellij.openapi.module.impl.scopes.JdkScope;
import com.intellij.openapi.module.impl.scopes.LibraryRuntimeClasspathScope;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerListener;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PendingEventDispatcher;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import com.intellij.util.messages.MessageBusConnection;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author max
 */
public class ProjectRootManagerImpl extends ProjectRootManagerEx implements ProjectComponent, JDOMExternalizable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.projectRoots.impl.ProjectRootManagerImpl");

  @NonNls private static final String PROJECT_JDK_NAME_ATTR = "project-jdk-name";
  @NonNls private static final String PROJECT_JDK_TYPE_ATTR = "project-jdk-type";

  private final ProjectEx myProject;
  private final ProjectFileIndex myProjectFileIndex;

  private final PendingEventDispatcher<ProjectJdkListener> myProjectJdkEventDispatcher = PendingEventDispatcher.create(ProjectJdkListener.class);

  private final MyVirtualFilePointerListener myVirtualFilePointerListener = new MyVirtualFilePointerListener();

  private MyVirtualFileManagerListener myVirtualFileManagerListener;

  private String myProjectJdkName;
  private String myProjectJdkType;

  private final ArrayList<CacheUpdater> myChangeUpdaters = new ArrayList<CacheUpdater>();

  private boolean myProjectOpened = false;

  private long myModificationCount = 0;
  private Set<LocalFileSystem.WatchRequest> myRootsToWatch = new HashSet<LocalFileSystem.WatchRequest>();
  @NonNls private static final String ATTRIBUTE_VERSION = "version";

  private final Map<List<Module>, GlobalSearchScope> myLibraryScopes = new ConcurrentHashMap<List<Module>, GlobalSearchScope>();
  private final Map<String, GlobalSearchScope> myJdkScopes = new HashMap<String, GlobalSearchScope>();

  private boolean myStartupActivityPerformed = false;

  private final MessageBusConnection myConnection;

  public static ProjectRootManagerImpl getInstanceImpl(Project project) {
    return (ProjectRootManagerImpl)getInstance(project);
  }

  public ProjectRootManagerImpl(Project project, FileTypeManager fileTypeManager, DirectoryIndex directoryIndex, StartupManager startupManager) {
    myProject = (ProjectEx)project;
    myConnection = project.getMessageBus().connect();
    myConnection.subscribe(AppTopics.FILE_TYPES, new FileTypeListener() {
      public void beforeFileTypesChanged(FileTypeEvent event) {
        beforeRootsChange(true);
      }

      public void fileTypesChanged(FileTypeEvent event) {
        rootsChanged(true);
      }
    });

    myProjectFileIndex = new ProjectFileIndexImpl(myProject, directoryIndex, fileTypeManager);
    startupManager.registerStartupActivity(new Runnable() {
      public void run() {
        myStartupActivityPerformed = true;
      }
    });
  }

  public void registerChangeUpdater(CacheUpdater updater) {
    myChangeUpdaters.add(updater);
  }

  public void unregisterChangeUpdater(CacheUpdater updater) {
    boolean success = myChangeUpdaters.remove(updater);
    LOG.assertTrue(success);
  }


  public void multiCommit(ModifiableRootModel[] rootModels) {
    ModuleRootManagerImpl.multiCommit(rootModels, ModuleManager.getInstance(myProject).getModifiableModel());
  }

  public void multiCommit(ModifiableModuleModel moduleModel, ModifiableRootModel[] rootModels) {
    ModuleRootManagerImpl.multiCommit(rootModels, moduleModel);
  }

  public VirtualFilePointerListener getVirtualFilePointerListener() {
    return myVirtualFilePointerListener;
  }

  @NotNull
  public ProjectFileIndex getFileIndex() {
    return myProjectFileIndex;
  }

  private final Map<ModuleRootListener, MessageBusConnection> myListenerAdapters = new HashMap<ModuleRootListener, MessageBusConnection>();

  public void addModuleRootListener(final ModuleRootListener listener) {
    final MessageBusConnection connection = myProject.getMessageBus().connect();
    myListenerAdapters.put(listener, connection);
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, listener);
  }

  public void addModuleRootListener(ModuleRootListener listener, Disposable parentDisposable) {
    final MessageBusConnection connection = myProject.getMessageBus().connect(parentDisposable);
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, listener);
  }

  public void removeModuleRootListener(ModuleRootListener listener) {
    final MessageBusConnection connection = myListenerAdapters.remove(listener);
    if (connection != null) {
      connection.disconnect();
    }
  }

  @NotNull
  public VirtualFile[] getContentRoots() {
    ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    final Module[] modules = getModuleManager().getModules();
    for (Module module : modules) {
      final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
      result.addAll(Arrays.asList(contentRoots));
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  public VirtualFile[] getContentSourceRoots() {
    ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    final Module[] modules = getModuleManager().getModules();
    for (Module module : modules) {
      final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
      result.addAll(Arrays.asList(sourceRoots));
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  public VirtualFile[] getFilesFromAllModules(OrderRootType type) {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    final Module[] modules = getModuleManager().getSortedModules();
    for (Module module : modules) {
      final VirtualFile[] files = ModuleRootManager.getInstance(module).getFiles(type);
      result.addAll(Arrays.asList(files));
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  private VirtualFile[] getExcludeRootsFromAllModules() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    final Module[] modules = getModuleManager().getSortedModules();
    for (Module module : modules) {
      final VirtualFile[] files = ModuleRootManager.getInstance(module).getExcludeRoots();
      result.addAll(Arrays.asList(files));
    }
    return result.toArray(new VirtualFile[result.size()]);
  }

  public VirtualFile[] getContentRootsFromAllModules() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    final Module[] modules = getModuleManager().getSortedModules();
    for (Module module : modules) {
      final VirtualFile[] files = ModuleRootManager.getInstance(module).getContentRoots();
      result.addAll(Arrays.asList(files));
    }
    result.add(myProject.getBaseDir());
    return result.toArray(new VirtualFile[result.size()]);
  }

  public VirtualFile[] getFullClassPath() {
    return getFilesFromAllModules(OrderRootType.CLASSES_AND_OUTPUT);
  }

  public Sdk getJdk() {
    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    if (modules.length > 0) {
      return ModuleRootManager.getInstance(modules[0]).getSdk();
    }
    else {
      return null;
    }
  }

  public Sdk getProjectJdk() {
    if (myProjectJdkName != null) {
      return ProjectJdkTable.getInstance().findJdk(myProjectJdkName, myProjectJdkType);
    }
    else {
      return null;
    }
  }

  public String getProjectJdkName() {
    return myProjectJdkName;
  }

  public void setProjectJdk(Sdk projectJdk) {
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    if (projectJdk != null) {
      myProjectJdkName = projectJdk.getName();
      myProjectJdkType = projectJdk.getSdkType().getName();
    }
    else {
      myProjectJdkName = null;
      myProjectJdkType = null;
    }
    doRootsChangedOnDemand(new Runnable() {
      public void run() {
        myProjectJdkEventDispatcher.getMulticaster().projectJdkChanged();
      }
    });
  }

  public void setProjectJdkName(String name) {
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    myProjectJdkName = name;

    doRootsChangedOnDemand(new Runnable() {
      public void run() {
        myProjectJdkEventDispatcher.getMulticaster().projectJdkChanged();
      }
    });
  }

  public void addProjectJdkListener(ProjectJdkListener listener) {
    myProjectJdkEventDispatcher.addListener(listener);
  }

  public void removeProjectJdkListener(ProjectJdkListener listener) {
    myProjectJdkEventDispatcher.removeListener(listener);
  }


  public void projectOpened() {
    addRootsToWatch();
    myVirtualFileManagerListener = new MyVirtualFileManagerListener();
    VirtualFileManager.getInstance().addVirtualFileManagerListener(myVirtualFileManagerListener);
    myProjectOpened = true;
  }

  public void projectClosed() {
    LocalFileSystem.getInstance().removeWatchedRoots(myRootsToWatch);
    VirtualFileManager.getInstance().removeVirtualFileManagerListener(myVirtualFileManagerListener);
    myProjectOpened = false;
  }

  @NotNull
  public String getComponentName() {
    return "ProjectRootManager";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
    if (myJdkTableMultilistener != null) {
      myJdkTableMultilistener.uninstallListner(false);
      myJdkTableMultilistener = null;
    }
    myConnection.disconnect();
  }

  public void readExternal(Element element) throws InvalidDataException {
    for (ProjectExtension extension : Extensions.getExtensions(ProjectExtension.EP_NAME, myProject)) {
      extension.readExternal(element);
    }
    myProjectJdkName = element.getAttributeValue(PROJECT_JDK_NAME_ATTR);
    myProjectJdkType = element.getAttributeValue(PROJECT_JDK_TYPE_ATTR);

  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(ATTRIBUTE_VERSION, "2");
    for (ProjectExtension extension : Extensions.getExtensions(ProjectExtension.EP_NAME, myProject)) {
      extension.writeExternal(element);
    }
    if (myProjectJdkName != null) {
      element.setAttribute(PROJECT_JDK_NAME_ATTR, myProjectJdkName);
    }
    if (myProjectJdkType != null){
      element.setAttribute(PROJECT_JDK_TYPE_ATTR, myProjectJdkType);
    }

  }


  private boolean myIsRootsChangedOnDemandStartedButNotDemanded = false;

  private int myRootsChangeCounter = 0;

  private void doRootsChangedOnDemand(Runnable r) {
    LOG.assertTrue(!myIsRootsChangedOnDemandStartedButNotDemanded, "Nested on-demand rootsChanged not supported");
    LOG.assertTrue(myRootsChangeCounter == 0, "On-demand rootsChanged not allowed inside rootsChanged");
    myIsRootsChangedOnDemandStartedButNotDemanded = true;
    try {
      r.run();
    }
    finally {
      if (myIsRootsChangedOnDemandStartedButNotDemanded) {
        myIsRootsChangedOnDemandStartedButNotDemanded = false;
      }
      else {
        if (myRootsChangeCounter != 1) {
          LOG.assertTrue(false, "myRootsChangedCounter = " + myRootsChangeCounter);
        }
        myIsRootsChangedOnDemandStartedButNotDemanded = false;
        rootsChanged(false);
      }
    }
  }

  public void beforeRootsChange(boolean filetypes) {
    if (myProject.isDisposed()) return;

    ApplicationManager.getApplication().assertWriteAccessAllowed();
    if (myRootsChangeCounter == 0) {
      if (myIsRootsChangedOnDemandStartedButNotDemanded) {
        myIsRootsChangedOnDemandStartedButNotDemanded = false;
        myRootsChangeCounter++; // blocks all firing until finishRootsChangedOnDemand
      }
      myProject.getMessageBus().syncPublisher(ProjectTopics.PROJECT_ROOTS).beforeRootsChange(new ModuleRootEventImpl(myProject, filetypes));
    }

    myRootsChangeCounter++;
  }

  private void clearScopesCaches () {
    clearScopesCachesForModules();
    myJdkScopes.clear();
    myLibraryScopes.clear();
  }

  public void clearScopesCachesForModules() {
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      ((ModuleRootManagerImpl)ModuleRootManager.getInstance(module)).dropCaches();
      ((ModuleImpl)module).clearScopesCache();
    }
  }

  public void rootsChanged(boolean filetypes) {
    if (myProject.isDisposed()) return;

    ApplicationManager.getApplication().assertWriteAccessAllowed();
    myRootsChangeCounter--;
    if (myRootsChangeCounter > 0) return;

    clearScopesCaches();

    myModificationCount++;

    myProject.getMessageBus().syncPublisher(ProjectTopics.PROJECT_ROOTS).rootsChanged(new ModuleRootEventImpl(myProject, filetypes));

    doSynchronize();

    addRootsToWatch();
  }

  public Project getProject() {
    return myProject;
  }

  private static class LibrariesOnlyScope extends GlobalSearchScope {
    private final GlobalSearchScope myOriginal;

    public LibrariesOnlyScope(final GlobalSearchScope original) {
      myOriginal = original;
    }

    public boolean contains(VirtualFile file) {
      return myOriginal.contains(file);
    }

    public int compare(VirtualFile file1, VirtualFile file2) {
      return myOriginal.compare(file1, file2);
    }

    public boolean isSearchInModuleContent(@NotNull Module aModule) {
      return false;
    }

    public boolean isSearchInLibraries() {
      return true;
    }
  }

  public GlobalSearchScope getScopeForLibraryUsedIn(List<Module> modulesLibraryIsUsedIn) {
    GlobalSearchScope scope = myLibraryScopes.get(modulesLibraryIsUsedIn);
    if (scope == null) {
      if (!modulesLibraryIsUsedIn.isEmpty()) {
        scope = new LibraryRuntimeClasspathScope(myProject, modulesLibraryIsUsedIn);
      }
      else {
        scope = new LibrariesOnlyScope(GlobalSearchScope.allScope(myProject));
      }
      myLibraryScopes.put(modulesLibraryIsUsedIn, scope);
    }
    return scope;
  }

  public GlobalSearchScope getScopeForJdk(final JdkOrderEntry jdkOrderEntry) {
    final String jdk = jdkOrderEntry.getJdkName();
    if (jdk == null) return GlobalSearchScope.allScope(myProject);
    GlobalSearchScope scope = myJdkScopes.get(jdk);
    if (scope == null) {
      scope = new JdkScope(myProject, jdkOrderEntry);
      myJdkScopes.put(jdk, scope);
    }
    return scope;
  }

  private void doSynchronize() {
    if (!myStartupActivityPerformed) return;

    final FileSystemSynchronizer synchronizer = new FileSystemSynchronizer();
    for (CacheUpdater updater : myChangeUpdaters) {
      synchronizer.registerCacheUpdater(updater);
    }

    if (!ApplicationManager.getApplication().isUnitTestMode() && myProjectOpened) {
      Runnable process = new Runnable() {
        public void run() {
          synchronizer.execute();
        }
      };
      ProgressManager.getInstance().runProcessWithProgressSynchronously(process, ProjectBundle.message("project.root.change.loading.progress"), false, myProject);
    }
    else {
      synchronizer.execute();
    }
  }

  private void addRootsToWatch() {
    if (myProject.isDefault()) {
      return;
    }
    final Set<String> rootPaths = new HashSet<String>();
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      final String[] contentRootUrls = moduleRootManager.getContentRootUrls();
      for (String url : contentRootUrls) {
        rootPaths.add(extractLocalPath(url));
      }
      rootPaths.add(module.getModuleFilePath());
    }

    for (WatchedRootsProvider extension : Extensions.getExtensions(WatchedRootsProvider.EP_NAME, myProject)) {
      rootPaths.addAll(extension.getRootsToWatch());
    }

    final String projectFile = myProject.getStateStore().getProjectFilePath();
    rootPaths.add(projectFile);
    final VirtualFile baseDir = myProject.getBaseDir();
    if (baseDir != null) {
      rootPaths.add(baseDir.getPath());
    }
    // No need to add workspace file separately since they're definetely on same directory with ipr.

    for (Module module : modules) {
      final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      final OrderEntry[] orderEntries = moduleRootManager.getOrderEntries();
      for (OrderEntry entry : orderEntries) {
        if (entry instanceof LibraryOrderEntry) {
          final Library library = ((LibraryOrderEntry)entry).getLibrary();
          for(OrderRootType orderRootType: OrderRootType.getAllTypes()) {
            rootPaths.addAll(getRootsToTrack(library, orderRootType));
          }
        }
        else if (entry instanceof JdkOrderEntry) {
          for(OrderRootType orderRootType: OrderRootType.getAllTypes()) {
            rootPaths.addAll(getRootsToTrack(entry, orderRootType));
          }
        }
      }
    }

    for (Module module : modules) {
      final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      final String explodedDirectory = moduleRootManager.getExplodedDirectoryUrl();
      if (explodedDirectory != null) {
        rootPaths.add(extractLocalPath(explodedDirectory));
      }
    }

    final Set<LocalFileSystem.WatchRequest> newRootsToWatch = LocalFileSystem.getInstance().addRootsToWatch(rootPaths, true);

    //remove old requests after adding new ones, helps avoiding unnecessary synchronizations
    LocalFileSystem.getInstance().removeWatchedRoots(myRootsToWatch);
    myRootsToWatch = newRootsToWatch;
  }

  private static Collection<String> getRootsToTrack(final Library library, final OrderRootType rootType) {
    return library != null ? getRootsToTrack(library.getUrls(rootType)) : Collections.<String>emptyList();
  }

  private static Collection<String> getRootsToTrack(final OrderEntry library, final OrderRootType rootType) {
    return library != null ? getRootsToTrack(library.getUrls(rootType)) : Collections.<String>emptyList();
  }

  private static List<String> getRootsToTrack(final String[] urls) {
    List<String> result = new ArrayList<String>();
    for (String url : urls) {
      if (url != null) {
        String path = extractLocalPath(url);
        result.add(path);
      }
    }

    return result;
  }

  public static String extractLocalPath(final String url) {
    final String path = VfsUtil.urlToPath(url);
    final int jarSeparatorIndex = path.indexOf(JarFileSystem.JAR_SEPARATOR);
    if (jarSeparatorIndex > 0) {
      return path.substring(0, jarSeparatorIndex);
    }
    return path;
  }

  private ModuleManager getModuleManager() {
    return ModuleManager.getInstance(myProject);
  }

  void addRootSetChangedListener(RootProvider.RootSetChangedListener rootSetChangedListener,
                                 final RootProvider provider) {
    RootSetChangedMulticaster multicaster = myRegisteredRootProviderListeners.get(provider);
    if (multicaster == null) {
      multicaster = new RootSetChangedMulticaster(provider);
    }
    multicaster.addListener(rootSetChangedListener);
  }

  void removeRootSetChangedListener(RootProvider.RootSetChangedListener rootSetChangedListener,
                                    final RootProvider provider) {
    RootSetChangedMulticaster multicaster = myRegisteredRootProviderListeners.get(provider);
    if (multicaster != null) {
      multicaster.removeListener(rootSetChangedListener);
    }
  }


  private class MyVirtualFilePointerListener implements VirtualFilePointerListener {
    public void beforeValidityChanged(VirtualFilePointer[] pointers) {
      if (!myProject.isDisposed()) {
        if (myInsideRefresh == 0) {
          beforeRootsChange(false);
        }
        else if (!myPointerChangesDetected) {
          //this is the first pointer changing validity
          myPointerChangesDetected = true;
          myProject.getMessageBus().syncPublisher(ProjectTopics.PROJECT_ROOTS).beforeRootsChange(new ModuleRootEventImpl(myProject, false));
        }
      }
    }

    public void validityChanged(VirtualFilePointer[] pointers) {
      if (!myProject.isDisposed()) {
        if (myInsideRefresh > 0) {
          clearScopesCaches();
        }
        else {
          rootsChanged(false);
        }
      }
    }
  }

  private int myInsideRefresh = 0;
  private boolean myPointerChangesDetected = false;

  private class MyVirtualFileManagerListener implements VirtualFileManagerListener {
    public void beforeRefreshStart(boolean asynchonous) {
      myInsideRefresh++;
    }

    public void afterRefreshFinish(boolean asynchonous) {
      myInsideRefresh--;
      if (myPointerChangesDetected) {
        myPointerChangesDetected = false;
        myProject.getMessageBus().syncPublisher(ProjectTopics.PROJECT_ROOTS).rootsChanged(new ModuleRootEventImpl(myProject, false));

        doSynchronize();

        addRootsToWatch();
      }
    }
  }


  void addListenerForTable(LibraryTable.Listener libraryListener,
                           final LibraryTable libraryTable) {
    LibraryTableMultilistener multilistener = myLibraryTableMultilisteners.get(libraryTable);
    if (multilistener == null) {
      multilistener = new LibraryTableMultilistener(libraryTable);
    }
    multilistener.addListener(libraryListener);
  }

  void removeListenerForTable(LibraryTable.Listener libraryListener,
                              final LibraryTable libraryTable) {
    LibraryTableMultilistener multilistener = myLibraryTableMultilisteners.get(libraryTable);
    if (multilistener == null) {
      multilistener = new LibraryTableMultilistener(libraryTable);
    }
    multilistener.removeListener(libraryListener);
  }

  private final Map<LibraryTable, LibraryTableMultilistener> myLibraryTableMultilisteners = new HashMap<LibraryTable, LibraryTableMultilistener>();

  private class LibraryTableMultilistener implements LibraryTable.Listener {
    final EventDispatcher<LibraryTable.Listener> myDispatcher = EventDispatcher.create(LibraryTable.Listener.class);
    final Set<LibraryTable.Listener> myListeners = new HashSet<LibraryTable.Listener>();
    private final LibraryTable myLibraryTable;

    private LibraryTableMultilistener(LibraryTable libraryTable) {
      myLibraryTable = libraryTable;
      myLibraryTable.addListener(this);
      myLibraryTableMultilisteners.put(myLibraryTable, this);
    }

    private void addListener(LibraryTable.Listener listener) {
      myListeners.add(listener);
      myDispatcher.addListener(listener);
    }

    private void removeListener(LibraryTable.Listener listener) {
      myDispatcher.removeListener(listener);
      myListeners.remove(listener);
      if (!myDispatcher.hasListeners()) {
        myLibraryTable.removeListener(this);
        myLibraryTableMultilisteners.remove(myLibraryTable);
      }
    }

    public void afterLibraryAdded(final Library newLibrary) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().afterLibraryAdded(newLibrary);
        }
      });
    }

    public void afterLibraryRenamed(final Library library) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().afterLibraryRenamed(library);
        }
      });
    }

    public void beforeLibraryRemoved(final Library library) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().beforeLibraryRemoved(library);
        }
      });
    }

    public void afterLibraryRemoved(final Library library) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().afterLibraryRemoved(library);
        }
      });
    }
  }

  private JdkTableMultilistener myJdkTableMultilistener = null;

  private class JdkTableMultilistener implements ProjectJdkTable.Listener {
    final EventDispatcher<ProjectJdkTable.Listener> myDispatcher = EventDispatcher.create(ProjectJdkTable.Listener.class);
    final Set<ProjectJdkTable.Listener> myListeners = new HashSet<ProjectJdkTable.Listener>();

    private JdkTableMultilistener() {
      ProjectJdkTable.getInstance().addListener(this);
    }

    private void addListener(ProjectJdkTable.Listener listener) {
      myDispatcher.addListener(listener);
      myListeners.add(listener);
    }

    private void removeListener(ProjectJdkTable.Listener listener) {
      myDispatcher.removeListener(listener);
      myListeners.remove(listener);
      uninstallListner(true);
    }

    public void jdkAdded(final Sdk jdk) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().jdkAdded(jdk);
        }
      });
    }

    public void jdkRemoved(final Sdk jdk) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().jdkRemoved(jdk);
        }
      });
    }

    public void jdkNameChanged(final Sdk jdk, final String previousName) {
      doRootsChangedOnDemand(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().jdkNameChanged(jdk, previousName);
        }
      });
      String currentName = getProjectJdkName();
      if (previousName != null && previousName.equals(currentName)) {
        // if already had jdk name and that name was the name of the jdk just changed
        myProjectJdkName = jdk.getName();
        myProjectJdkType = jdk.getSdkType().getName();
      }
    }

    public void uninstallListner(boolean soft) {
      if (!soft || !myDispatcher.hasListeners()) {
        ProjectJdkTable.getInstance().removeListener(this);
      }
    }
  }

  private final Map<RootProvider, RootSetChangedMulticaster> myRegisteredRootProviderListeners = new HashMap<RootProvider, RootSetChangedMulticaster>();

  void addJdkTableListener(ProjectJdkTable.Listener jdkTableListener) {
    getJdkTableMultiListener().addListener(jdkTableListener);
  }

  private JdkTableMultilistener getJdkTableMultiListener() {
    if (myJdkTableMultilistener == null) {
      myJdkTableMultilistener = new JdkTableMultilistener();
    }
    return myJdkTableMultilistener;
  }


  void removeJdkTableListener(ProjectJdkTable.Listener jdkTableListener) {
    if (myJdkTableMultilistener == null) return;
    myJdkTableMultilistener.removeListener(jdkTableListener);
  }

  private class RootSetChangedMulticaster implements RootProvider.RootSetChangedListener {
    private final EventDispatcher<RootProvider.RootSetChangedListener> myDispatcher = EventDispatcher.create(RootProvider.RootSetChangedListener.class);
    private final RootProvider myProvider;

    private RootSetChangedMulticaster(RootProvider provider) {
      myProvider = provider;
      provider.addRootSetChangedListener(this);
      myRegisteredRootProviderListeners.put(myProvider, this);
    }

    private void addListener(RootProvider.RootSetChangedListener listener) {
      myDispatcher.addListener(listener);
    }

    private void removeListener(RootProvider.RootSetChangedListener listener) {
      myDispatcher.removeListener(listener);
      if (!myDispatcher.hasListeners()) {
        myProvider.removeRootSetChangedListener(this);
        myRegisteredRootProviderListeners.remove(myProvider);
      }
    }

    public void rootSetChanged(final RootProvider wrapper) {
      LOG.assertTrue(myProvider.equals(wrapper));
      Runnable runnable = new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().rootSetChanged(wrapper);
        }
      };
      doRootsChangedOnDemand(runnable);
    }

  }

  public long getModificationCount() {
    return myModificationCount;
  }


}