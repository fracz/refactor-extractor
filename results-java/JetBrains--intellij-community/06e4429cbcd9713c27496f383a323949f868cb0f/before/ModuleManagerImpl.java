package com.intellij.openapi.module.impl;

import com.intellij.CommonBundle;
import com.intellij.ProjectTopics;
import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleCircularDependencyException;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.PomModel;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import com.intellij.util.graph.CachingSemiGraph;
import com.intellij.util.graph.DFSTBuilder;
import com.intellij.util.graph.Graph;
import com.intellij.util.graph.GraphGenerator;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.MessageHandler;
import gnu.trove.THashMap;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author max
 */
public class ModuleManagerImpl extends ModuleManager implements ProjectComponent, JDOMExternalizable,ModificationTracker {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.module.impl.ModuleManagerImpl");
  private final Project myProject;
  private ModuleModelImpl myModuleModel = new ModuleModelImpl();
  private PomModel myPomModel;

  @NonNls public static final String COMPONENT_NAME = "ProjectModuleManager";
  private static final String MODULE_GROUP_SEPARATOR = "/";
  private ModulePath[] myModulePaths;
  private List<ModulePath> myFailedModulePaths = new ArrayList<ModulePath>();
  @NonNls private static final String ELEMENT_MODULES = "modules";
  @NonNls private static final String ELEMENT_MODULE = "module";
  @NonNls private static final String ATTRIBUTE_FILEURL = "fileurl";
  @NonNls private static final String ATTRIBUTE_FILEPATH = "filepath";
  @NonNls private static final String ATTRIBUTE_GROUP = "group";
  private long myModificationCount;
  private MessageBusConnection myConnection;
  private final MessageBus myMessageBus;

  public static ModuleManagerImpl getInstanceImpl(Project project) {
    return (ModuleManagerImpl)getInstance(project);
  }

  private void cleanCachedStuff() {
    myCachedModuleComparator = null;
    myCachedSortedModules = null;
  }

  public ModuleManagerImpl(Project project, PomModel pomModel, MessageBus bus) {
    myProject = project;
    myMessageBus = bus;
    myConnection = bus.connect();

    myConnection.setDefaultHandler(new MessageHandler() {
      public void handle(Method event, Object... params) {
        cleanCachedStuff();
      }
    });
    myConnection.subscribe(ProjectTopics.PROJECT_ROOTS);
    myPomModel = pomModel;
  }


  @NotNull
  public String getComponentName() {
    return COMPONENT_NAME;
  }

  public void initComponent() {
  }

  public void disposeComponent() {
    myModuleModel.disposeModel();
    myConnection.disconnect();
  }

  public long getModificationCount() {
    return myModificationCount;
  }

  public static final class ModulePath {
    private final String myPath;
    private final String myModuleGroup;

    public ModulePath(String path, String moduleGroup) {
      myPath = path;
      myModuleGroup = moduleGroup;
    }

    public String getPath() {
      return myPath;
    }

    public String getModuleGroup() {
      return myModuleGroup;
    }
  }

  public static ModulePath[] getPathsToModuleFiles(Element element) {
    final List<ModulePath> paths = new ArrayList<ModulePath>();
    final Element modules = element.getChild(ELEMENT_MODULES);
    if (modules != null) {
      for (final Object value : modules.getChildren(ELEMENT_MODULE)) {
        Element moduleElement = (Element)value;
        final String fileUrlValue = moduleElement.getAttributeValue(ATTRIBUTE_FILEURL);
        final String filepath;
        if (fileUrlValue != null) {
          filepath = VirtualFileManager.extractPath(fileUrlValue).replace('/', File.separatorChar);
        }
        else {
          // [dsl] support for older formats
          filepath = moduleElement.getAttributeValue(ATTRIBUTE_FILEPATH).replace('/', File.separatorChar);
        }
        final String group = moduleElement.getAttributeValue(ATTRIBUTE_GROUP);
        paths.add(new ModulePath(filepath, group));
      }
    }
    return paths.toArray(new ModulePath[paths.size()]);
  }

  public void readExternal(final Element element) throws InvalidDataException {
    myModulePaths = getPathsToModuleFiles(element);
  }

  public void loadModules() {
    if (myModulePaths != null && myModulePaths.length > 0) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          myFailedModulePaths.clear();
          myFailedModulePaths.addAll(Arrays.asList(myModulePaths));
          final List<Module> modulesWithUnknownTypes = new ArrayList<Module>();
          for (final ModulePath modulePath : myModulePaths) {
            try {
              final Module module = myModuleModel.loadModuleInternal(modulePath.getPath());
              if (module.getModuleType() instanceof UnknownModuleType) {
                modulesWithUnknownTypes.add(module);
              }
              final String groupPathString = modulePath.getModuleGroup();
              if (groupPathString != null) {
                final String[] groupPath = groupPathString.split(MODULE_GROUP_SEPARATOR);
                myModuleModel.setModuleGroupPath(module, groupPath); //model should be updated too
              }
              myFailedModulePaths.remove(modulePath);
            }
            catch (final IOException e) {
              fireError(ProjectBundle.message("module.cannot.load.error", e.getMessage()), modulePath);
            }
            catch (JDOMException e) {
              fireError(ProjectBundle.message("module.corrupted.file.error", modulePath.getPath(), e.getMessage()), modulePath);
            }
            catch (InvalidDataException e) {
              fireError(ProjectBundle.message("module.corrupted.data.error", modulePath.getPath()), modulePath);
            }
            catch (final ModuleWithNameAlreadyExists moduleWithNameAlreadyExists) {
              fireError(moduleWithNameAlreadyExists.getMessage(), modulePath);
            }
          }
          if (!ApplicationManager.getApplication().isHeadlessEnvironment() && !modulesWithUnknownTypes.isEmpty()) {
            String message;
            if (modulesWithUnknownTypes.size() == 1) {
              message = ProjectBundle.message("module.unknown.type.single.error", modulesWithUnknownTypes.get(0).getName());
            }
            else {
              StringBuilder modulesBuilder = new StringBuilder();
              for (final Module module : modulesWithUnknownTypes) {
                modulesBuilder.append("\n\"");
                modulesBuilder.append(module.getName());
                modulesBuilder.append("\"");
              }
              message = ProjectBundle.message("module.unknown.type.multiple.error", modulesBuilder.toString());
            }
            Messages.showWarningDialog(myProject, message, ProjectBundle.message("module.unknown.type.title"));
          }
        }
      });
    }
  }

  private void fireError(final String message, final ModulePath modulePath) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        final int answer = Messages.showDialog(
          ProjectBundle.message("module.remove.from.project.confirmation", message),
          ProjectBundle.message("module.remove.from.project.title"),
          new String[]{CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()},
          1,
          Messages.getErrorIcon()
        );
        if (answer == 0) { // yes
          myFailedModulePaths.remove(modulePath);
        }
      }
    });
  }

  @NotNull
  public ModifiableModuleModel getModifiableModel() {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return new ModuleModelImpl(myModuleModel);
  }


  private static abstract class SaveItem {

    protected abstract String getModuleName();
    protected abstract String getGroupPathString();
    protected abstract String getModuleFilePath();
    public boolean isDefaultModule() {
      return false;
    }

    public final void writeExternal(Element parentElement) {
      Element moduleElement = new Element(ELEMENT_MODULE);
      final String moduleFilePath = getModuleFilePath();
      final String url = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, moduleFilePath);
      moduleElement.setAttribute(ATTRIBUTE_FILEURL, url);
      // [dsl] support for older builds
      moduleElement.setAttribute(ATTRIBUTE_FILEPATH, moduleFilePath);

      final String groupPath = getGroupPathString();
      if (groupPath != null) {
        moduleElement.setAttribute(ATTRIBUTE_GROUP, groupPath);
      }
      parentElement.addContent(moduleElement);
    }
  }

  private class ModuleSaveItem extends SaveItem{
    private final Module myModule;

    public ModuleSaveItem(Module module) {
      myModule = module;
    }

    protected String getModuleName() {
      return myModule.getName();
    }

    protected String getGroupPathString() {
      String[] groupPath = getModuleGroupPath(myModule);
      return groupPath != null ? StringUtil.join(groupPath, MODULE_GROUP_SEPARATOR) : null;
    }

    protected String getModuleFilePath() {
      return myModule.getModuleFilePath().replace(File.separatorChar, '/');
    }
  }

  private static class ModulePathSaveItem extends SaveItem{
    private final ModulePath myModulePath;
    private String myFilePath;
    private final String myName;

    public ModulePathSaveItem(ModulePath modulePath) {
      myModulePath = modulePath;
      myFilePath = modulePath.getPath().replace(File.separatorChar, '/');

      final int slashIndex = myFilePath.lastIndexOf('/');
      final int startIndex = slashIndex >= 0 && slashIndex + 1 < myFilePath.length() ? slashIndex + 1 : 0;
      final int endIndex = myFilePath.endsWith(ModuleFileType.DOT_DEFAULT_EXTENSION)
                           ? myFilePath.length() - ModuleFileType.DOT_DEFAULT_EXTENSION.length()
                           : myFilePath.length();
      myName = myFilePath.substring(startIndex, endIndex);
    }

    protected String getModuleName() {
      return myName;
    }

    protected String getGroupPathString() {
      return myModulePath.getModuleGroup();
    }

    protected String getModuleFilePath() {
      return myFilePath;
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    final Element modules = new Element(ELEMENT_MODULES);
    final Collection<Module> collection = getModulesToWrite();

    ArrayList<SaveItem> sorted = new ArrayList<SaveItem>(collection.size() + myFailedModulePaths.size());
    for (Module module : collection) {
      sorted.add(new ModuleSaveItem(module));
    }
    for (ModulePath modulePath : myFailedModulePaths) {
      sorted.add(new ModulePathSaveItem(modulePath));
    }
    Collections.sort(sorted, new Comparator<SaveItem>() {
      public int compare(SaveItem item1, SaveItem item2) {
        return item1.getModuleName().compareTo(item2.getModuleName());
      }
    });
    for (SaveItem saveItem : sorted) {
      if (!saveItem.isDefaultModule()) {
        saveItem.writeExternal(modules);
      }
    }

    element.addContent(modules);
  }

  private Collection<Module> getModulesToWrite() {
    Collection<Module> actual = new ArrayList<Module>();
    actual.addAll(myModuleModel.myPathToModule.values());

    for (String cancelledPath : myModuleModel.myPathToCancelledModule.keySet()) {
      if (!myModuleModel.myPathToModule.containsKey(cancelledPath)) {
        actual.add(myModuleModel.myPathToCancelledModule.get(cancelledPath));
      }
    }
    return actual;
  }

  private void fireModuleAdded(Module module) {
    myMessageBus.syncPublisher(ProjectTopics.MODULES).moduleAdded(myProject, module);
  }

  private void fireModuleRemoved(Module module) {
    myMessageBus.syncPublisher(ProjectTopics.MODULES).moduleRemoved(myProject, module);
  }

  private void fireBeforeModuleRemoved(Module module) {
    myMessageBus.syncPublisher(ProjectTopics.MODULES).beforeModuleRemoved(myProject, module);
  }

  private Map<ModuleListener, MessageBusConnection> myAdapters = new HashMap<ModuleListener, MessageBusConnection>();
  public void addModuleListener(@NotNull ModuleListener listener) {
    final MessageBusConnection connection = myMessageBus.connect();
    connection.subscribe(ProjectTopics.MODULES, listener);
    myAdapters.put(listener, connection);
  }

  public void addModuleListener(@NotNull ModuleListener listener, Disposable parentDisposable) {
    final MessageBusConnection connection = myMessageBus.connect(parentDisposable);
    connection.subscribe(ProjectTopics.MODULES, listener);
  }

  public void removeModuleListener(@NotNull ModuleListener listener) {
    final MessageBusConnection adapter = myAdapters.remove(listener);
    if (adapter != null) {
      adapter.disconnect();
    }
  }

  @NotNull
  public Module newModule(@NotNull String filePath) {
    myModificationCount++;
    final ModifiableModuleModel modifiableModel = getModifiableModel();
    final Module module = modifiableModel.newModule(filePath);
    modifiableModel.commitAssertingNoCircularDependency();
    return module;
  }

  @NotNull
  public Module newModule(@NotNull String filePath, @NotNull ModuleType moduleType) {
    myModificationCount++;
    final ModifiableModuleModel modifiableModel = getModifiableModel();
    final Module module = modifiableModel.newModule(filePath, moduleType);
    modifiableModel.commitAssertingNoCircularDependency();
    return module;
  }

  @NotNull
  public Module loadModule(@NotNull String filePath) throws InvalidDataException,
                                                   IOException,
                                                   JDOMException,
                                                   ModuleWithNameAlreadyExists,
                                                   ModuleCircularDependencyException {
    myModificationCount++;
    final ModifiableModuleModel modifiableModel = getModifiableModel();
    final Module module = modifiableModel.loadModule(filePath);
    modifiableModel.commit();
    return module;
  }

  public void disposeModule(@NotNull final Module module) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final ModifiableModuleModel modifiableModel = getModifiableModel();
        modifiableModel.disposeModule(module);
        modifiableModel.commitAssertingNoCircularDependency();
      }
    });
  }

  @NotNull
  public Module[] getModules() {
    if (myModuleModel.myIsWritable) {
      ApplicationManager.getApplication().assertReadAccessAllowed();
    }
    return myModuleModel.getModules();
  }

  private Module[] myCachedSortedModules = null;

  @NotNull
  public Module[] getSortedModules() {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    myConnection.deliverImmediately();
    if (myCachedSortedModules == null) {
      myCachedSortedModules = myModuleModel.getSortedModules();
    }
    return myCachedSortedModules;
  }

  public Module findModuleByName(@NotNull String name) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return myModuleModel.findModuleByName(name);
  }

  private Comparator<Module> myCachedModuleComparator = null;

  @NotNull
  public Comparator<Module> moduleDependencyComparator() {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    myConnection.deliverImmediately();
    if (myCachedModuleComparator == null) {
      myCachedModuleComparator = myModuleModel.moduleDependencyComparator();
    }
    return myCachedModuleComparator;
  }

  @NotNull
  public Graph<Module> moduleGraph() {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return myModuleModel.moduleGraph();
  }

  @NotNull public List<Module> getModuleDependentModules(@NotNull Module module) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return myModuleModel.getModuleDependentModules(module);
  }

  public boolean isModuleDependent(@NotNull Module module, @NotNull Module onModule) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    return myModuleModel.isModuleDependent(module, onModule);
  }

  public void projectOpened() {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        for (Module module : myModuleModel.myPathToModule.values()) {
          ((ModuleImpl)module).moduleAdded();
          fireModuleAdded(module);
        }
      }
    });
    myModuleModel.projectOpened();
  }

  public void projectClosed() {
    myModuleModel.projectClosed();
  }

  public static void commitModelWithRunnable(ModifiableModuleModel model, Runnable runnable) {
    ((ModuleModelImpl)model).commitWithRunnable(runnable);
  }

  class ModuleModelImpl implements ModifiableModuleModel {
    private Map<String, Module> myPathToModule = new LinkedHashMap<String, Module>();
    private Map<String, Module> myPathToCancelledModule = new HashMap<String, Module>();

    private List<Module> myModulesToDispose = new ArrayList<Module>();
    private Map<Module, String> myModuleToNewName = new HashMap<Module, String>();
    private Map<String, Module> myNewNameToModule = new HashMap<String, Module>();
    private boolean myIsWritable;
    private Map<Module, String []> myModuleGroupPath;

    ModuleModelImpl() {
      myIsWritable = false;
    }

    ModuleModelImpl(ModuleModelImpl that) {
      myPathToModule.putAll(that.myPathToModule);
      final Map<Module, String[]> groupPath = that.myModuleGroupPath;
      if (groupPath != null){
        myModuleGroupPath = new THashMap<Module, String[]>();
        myModuleGroupPath.putAll(that.myModuleGroupPath);
      }
      myIsWritable = true;
      myPomModel = myProject.getModel();
    }

    private void assertWritable() {
      LOG.assertTrue(myIsWritable, "Attempt to modify committed ModifiableModuleModel");
    }

    @NotNull
    public Module[] getModules() {
      Collection<Module> modules = myPathToModule.values();
      return modules.toArray(new Module[modules.size()]);
    }

    private Module[] getSortedModules() {
      Module[] allModules = getModules();
      Arrays.sort(allModules, moduleDependencyComparator());
      return allModules;
    }

    @NotNull
    public Module newModule(@NotNull String filePath) {
      assertWritable();
      return newModule(filePath, ModuleType.JAVA);
    }

    public void renameModule(@NotNull Module module, @NotNull String newName) throws ModuleWithNameAlreadyExists {
      final Module oldModule = getModuleByNewName(newName);
      if (oldModule != null) {
        throw new ModuleWithNameAlreadyExists(ProjectBundle.message("module.already.exists.error", newName), newName);
      }
      final String oldName = myModuleToNewName.get(module);
      myModuleToNewName.put(module, newName);
      myNewNameToModule.remove(oldName);
      myNewNameToModule.put(newName, module);
    }

    public Module getModuleToBeRenamed(@NotNull String newName) {
      return myNewNameToModule.get(newName);
    }

    public Module getModuleByNewName(String newName) {
      final Module moduleToBeRenamed = getModuleToBeRenamed(newName);
      if (moduleToBeRenamed != null) {
        return moduleToBeRenamed;
      }
      final Module moduleWithOldName = findModuleByName(newName);
      if (myModuleToNewName.get(moduleWithOldName) == null) {
        return moduleWithOldName;
      }
      else {
        return null;
      }
    }

    public String getNewName(@NotNull Module module) {
      return myModuleToNewName.get(module);
    }

    @NotNull
    public Module newModule(@NotNull String filePath, @NotNull ModuleType moduleType) {
      return newModule(filePath,moduleType,null);
    }

    @NotNull
    public Module newModule(@NotNull String filePath, @NotNull ModuleType moduleType, @Nullable Map<String, String> options) {
      assertWritable();
      try {
        filePath = FileUtil.resolveShortWindowsName(filePath);
      }
      catch (IOException e) {
      }

      ModuleImpl module = getModuleByFilePath(filePath);
      if (module == null) {
        module = new ModuleImpl(filePath, myProject, myPomModel);
        module.setModuleType(moduleType);
        if (options != null) {
          for ( Map.Entry<String,String> option : options.entrySet()) {
            module.setOption(option.getKey(),option.getValue());
          }
        }
        module.loadModuleComponents();
        initModule(module, false);
      }
      return module;
    }

    private ModuleImpl getModuleByFilePath(String filePath) {
      final Collection<Module> modules = myPathToModule.values();
      for (Module module : modules) {
        if (filePath.equals(module.getModuleFilePath())) {
          return (ModuleImpl)module;
        }
      }
      return null;
    }

    @NotNull
    public Module loadModule(@NotNull String filePath) throws InvalidDataException,
                                                     IOException,
                                                     ModuleWithNameAlreadyExists {
      assertWritable();
      try {
        return loadModuleInternal(filePath);
      }
      catch (JDOMException e) {
        throw new IOException(ProjectBundle.message("module.corrupted.file.error", FileUtil.toSystemDependentName(filePath), e.getMessage()));
      }
    }

    private Module loadModuleInternal(String filePath) throws ModuleWithNameAlreadyExists,
                                                              JDOMException,
                                                              IOException,
                                                              InvalidDataException
                                                               {
      final File moduleFile = new File(filePath);
      try {
        filePath = FileUtil.resolveShortWindowsName(filePath);
      }
      catch (IOException e) {
      }

      final String name = moduleFile.getName();
      if (name.endsWith(ModuleFileType.DOT_DEFAULT_EXTENSION)) {
        final String moduleName = name.substring(0, name.length() - 4);
        for (Module module : myPathToModule.values()) {
          if (module.getName().equals(moduleName)) {
            throw new ModuleWithNameAlreadyExists(ProjectBundle.message("module.already.exists.error", moduleName), moduleName);
          }
        }
      }
      if (!moduleFile.exists()) {
        throw new IOException(ProjectBundle.message("module.file.does.not.exist.error", moduleFile.getPath()));
      }
      ModuleImpl module = getModuleByFilePath(filePath);
      if (module == null) {
        module = new ModuleImpl(filePath, myProject, myPomModel);
        module.getStateStore().load();
        module.loadModuleComponents();
        initModule(module, true);
      }
      return module;
    }

    private void initModule(ModuleImpl module, boolean saveToCancelled) {
      String path = module.getModuleFilePath();
      myPathToModule.put(path, module);
      module.init();
    }

    public void disposeModule(@NotNull Module module) {
      assertWritable();
      if (myPathToModule.values().contains(module)) {
        myPathToModule.values().remove(module);
        myModulesToDispose.add(module);
      }
      if (myModuleGroupPath != null){
        myModuleGroupPath.remove(module);
      }
    }

    public Module findModuleByName(@NotNull String name) {
      for (Module module : myPathToModule.values()) {
        if (module.getName().equals(name)) {
          return module;
        }
      }
      return null;
    }

    private Comparator<Module> moduleDependencyComparator() {
      DFSTBuilder<Module> builder = new DFSTBuilder<Module>(moduleGraph());
      return builder.comparator();
    }

    private Graph<Module> moduleGraph() {
      return GraphGenerator.create(CachingSemiGraph.create(new GraphGenerator.SemiGraph<Module>() {
        public Collection<Module> getNodes() {
          return myPathToModule.values();
        }

        public Iterator<Module> getIn(Module m) {
          Module[] dependentModules = ModuleRootManager.getInstance(m).getDependencies();
          return Arrays.asList(dependentModules).iterator();
        }
      }));
    }

    @NotNull private List<Module> getModuleDependentModules(Module module) {
      List<Module> result = new ArrayList<Module>();
      for (Module aModule : myPathToModule.values()) {
        if (isModuleDependent(aModule, module)) {
          result.add(aModule);
        }
      }
      return result;
    }

    private boolean isModuleDependent(Module module, Module onModule) {
      return ModuleRootManager.getInstance(module).isDependsOn(onModule);
    }

    public void commitAssertingNoCircularDependency() {
      try {
        commit();
      }
      catch (ModuleCircularDependencyException e) {
        LOG.error(e);
      }
    }

    public void commit() throws ModuleCircularDependencyException {
      ProjectRootManagerEx.getInstanceEx(myProject).multiCommit(this, new ModifiableRootModel[0]);
    }

    public void commitWithRunnable(Runnable runnable) {
      ModuleManagerImpl.this.commitModel(this, runnable);
      myIsWritable = false;
      clearRenamingStuff();
    }

    private void clearRenamingStuff() {
      myModuleToNewName.clear();
      myNewNameToModule.clear();
    }

    public void dispose() {
      assertWritable();
      ApplicationManager.getApplication().assertWriteAccessAllowed();
      final Collection<Module> list = ModuleManagerImpl.this.myModuleModel.myPathToModule.values();
      final Collection<Module> thisModules = myPathToModule.values();
      for (Module thisModule1 : thisModules) {
        ModuleImpl thisModule = (ModuleImpl)thisModule1;
        if (!list.contains(thisModule)) {
          Disposer.dispose(thisModule);
        }
      }
      for (Module moduleToDispose : myModulesToDispose) {
        if (!list.contains(moduleToDispose)) {
          Disposer.dispose(moduleToDispose);
        }
      }
      clearRenamingStuff();
    }

    public boolean isChanged() {
      if (!myIsWritable) {
        return false;
      }
      Set<Module> thisModules = new HashSet<Module>(myPathToModule.values());
      Set<Module> thatModules = new HashSet<Module>(ModuleManagerImpl.this.myModuleModel.myPathToModule.values());
      return !thisModules.equals(thatModules) || !Comparing.equal(ModuleManagerImpl.this.myModuleModel.myModuleGroupPath, myModuleGroupPath);
    }

    private void disposeModel() {
      final Collection collection = myPathToModule.values();
      for (final Object aCollection : collection) {
        ModuleImpl module = (ModuleImpl)aCollection;
        Disposer.dispose(module);
      }
      myPathToModule.clear();
      myModuleGroupPath = null;
    }

    public void projectOpened() {
      final Collection<Module> collection = myPathToModule.values();
      for (final Module aCollection : collection) {
        ModuleImpl module = (ModuleImpl)aCollection;
        module.projectOpened();
      }
    }

    public void projectClosed() {
      final Collection<Module> collection = myPathToModule.values();
      for (final Module aCollection : collection) {
        ModuleImpl module = (ModuleImpl)aCollection;
        module.projectClosed();
      }
    }

    public String[] getModuleGroupPath(Module module) {
      return myModuleGroupPath == null ? null : myModuleGroupPath.get(module);
    }

    public boolean hasModuleGroups() {
      return myModuleGroupPath != null && !myModuleGroupPath.isEmpty();
    }

    public void setModuleGroupPath(Module module, String[] groupPath) {
      if (myModuleGroupPath == null) {
        myModuleGroupPath = new THashMap<Module, String[]>();
      }
      if (groupPath == null) {
        myModuleGroupPath.remove(module);
      }
      else {
        myModuleGroupPath.put(module, groupPath);
      }
    }
  }

  private void commitModel(ModuleModelImpl moduleModel, Runnable runnable) {
    myModificationCount++;
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    final Collection<Module> oldModules = myModuleModel.myPathToModule.values();
    final Collection<Module> newModules = moduleModel.myPathToModule.values();
    List<Module> removedModules = new ArrayList<Module>(oldModules);
    removedModules.removeAll(newModules);
    List<Module> addedModules = new ArrayList<Module>(newModules);
    addedModules.removeAll(oldModules);

    ProjectRootManagerEx.getInstanceEx(myProject).beforeRootsChange(false);

    try {
      for (Module removedModule : removedModules) {
        fireBeforeModuleRemoved(removedModule);
        cleanCachedStuff();
      }

      List<Module> neverAddedModules = new ArrayList<Module>(moduleModel.myModulesToDispose);
      neverAddedModules.removeAll(myModuleModel.myPathToModule.values());
      for (final Module neverAddedModule : neverAddedModules) {
        ModuleImpl module = (ModuleImpl)neverAddedModule;
        Disposer.dispose(module);
      }

      myModuleModel = moduleModel;

      if (runnable != null) {
        runnable.run();
      }

      for (Module module : removedModules) {
        fireModuleRemoved(module);
        cleanCachedStuff();
        Disposer.dispose(module);
        cleanCachedStuff();
      }

      for (Module addedModule : addedModules) {
        ((ModuleImpl)addedModule).moduleAdded();
        cleanCachedStuff();
        fireModuleAdded(addedModule);
        cleanCachedStuff();
      }
      final Map<Module, String> modulesToNewNamesMap = moduleModel.myModuleToNewName;
      final Set<Module> modulesToBeRenamed = modulesToNewNamesMap.keySet();
      final List<Module> modules = new ArrayList<Module>();
      for (final Module aModulesToBeRenamed : modulesToBeRenamed) {
        ModuleImpl module = (ModuleImpl)aModulesToBeRenamed;
        modules.add(module);
        module.rename(modulesToNewNamesMap.get(module));
        cleanCachedStuff();
      }
      fireModulesRenamed(modules);
      cleanCachedStuff();
    }
    finally {
      ProjectRootManagerEx.getInstanceEx(myProject).rootsChanged(false);
    }
  }

  private void fireModulesRenamed(List<Module> modules) {
    if (!modules.isEmpty()) {
      myMessageBus.syncPublisher(ProjectTopics.MODULES).modulesRenamed(myProject, modules);
    }
  }

  void fireModuleRenamedByVfsEvent(Module module) {
    ProjectRootManagerEx.getInstanceEx(myProject).beforeRootsChange(false);
    try {
      fireModulesRenamed(Collections.singletonList(module));
    }
    finally {
      ProjectRootManagerEx.getInstanceEx(myProject).rootsChanged(false);
    }
  }

  public String[] getModuleGroupPath(@NotNull Module module) {
    return myModuleModel.getModuleGroupPath(module);
  }

  public void setModuleGroupPath(Module module, String[] groupPath) {
    myModuleModel.setModuleGroupPath(module, groupPath);
  }
}
