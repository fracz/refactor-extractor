/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.roots.ui.configuration.projectRoot;

import com.intellij.CommonBundle;
import com.intellij.find.FindBundle;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.ModuleGroupUtil;
import com.intellij.javaee.serverInstances.ApplicationServersManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.roots.impl.libraries.LibraryTableImplUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.LibraryTableModifiableModelProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurable;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryTableEditor;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.Icons;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: anna
 * Date: 02-Jun-2006
 */
public class ProjectRootConfigurable extends MasterDetailsComponent implements ProjectComponent {
  private static final Icon ICON = IconLoader.getIcon("/modules/modules.png");

  private MyNode myJdksNode;

  private MyNode myGlobalLibrariesNode;
  private LibrariesModifiableModel myGlobalLibrariesProvider;

  private LibrariesModifiableModel myProjectLibrariesProvider;

  private Map<Module, LibrariesModifiableModel> myModule2LibrariesMap = new HashMap<Module, LibrariesModifiableModel>();

  private MyNode myProjectNode;
  private MyNode myProjectLibrariesNode;
  private Project myProject;

  private ModuleManager myModuleManager;
  private ModulesConfigurator myModulesConfigurator;
  private ModulesConfigurable myModulesConfigurable;
  private ProjectJdksModel myJdksTreeModel = new ProjectJdksModel(this);

  private MyNode myApplicationServerLibrariesNode;
  private LibrariesModifiableModel myApplicationServerLibrariesProvider;

  private boolean myDisposed = true;

  public ProjectRootConfigurable(Project project, ModuleManager manager) {
    myProject = project;
    myModuleManager = manager;
    addItemsChangeListener(new ItemsChangeListener() {
      public void itemChanged(@Nullable Object deletedItem) {
        if (deletedItem instanceof Library) {
          final Library library = (Library)deletedItem;
          final MyNode node = findNodeByObject(myRoot, library);
          if (node != null) {
            final TreeNode parent = node.getParent();
            node.removeFromParent();
            ((DefaultTreeModel)myTree.getModel()).reload(parent);
          }
        }
      }

      public void itemsExternallyChanged() {
        //do nothing
      }
    });
    initTree();
  }


  protected void initTree() {
    super.initTree();
    new TreeSpeedSearch(myTree, new Convertor<TreePath, String>() {
      public String convert(final TreePath treePath) {
        return ((MyNode)treePath.getLastPathComponent()).getDisplayName();
      }
    });
  }


  protected ArrayList<AnAction> getAdditionalActions() {
    final ArrayList<AnAction> result = new ArrayList<AnAction>();
    result.add(new MyRenameAction() {
      @Nullable
      protected String getRenameTitleSuffix() {
        final Object selectedObject = getSelectedObject();
        if (selectedObject instanceof Module){
          return ProjectBundle.message("add.new.module.text");
        } else if (selectedObject instanceof Library){
          return ProjectBundle.message("add.new.library.text");
        } else if (selectedObject instanceof ProjectJdk){
          return ProjectBundle.message("add.new.jdk.text");
        }
        return null;
      }
    });
    final AnAction findUsages = new AnAction(ProjectBundle.message("find.usages.action.text")) {
      public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        final TreePath selectionPath = myTree.getSelectionPath();
        if (selectionPath != null){
          final MyNode node = (MyNode)selectionPath.getLastPathComponent();
          presentation.setEnabled(!node.isDisplayInBold());
        } else {
          presentation.setEnabled(false);
        }
      }

      public void actionPerformed(AnActionEvent e) {
        showDependencies();
      }
    };
    findUsages.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_FIND_USAGES).getShortcutSet(), myTree);
    result.add(findUsages);
    result.add(ActionManager.getInstance().getAction(IdeActions.GROUP_MOVE_MODULE_TO_GROUP));
    return result;
  }

  protected void reloadTree() {

    myRoot.removeAllChildren();

    createProjectNodes();

    createProjectJdks();

    myGlobalLibrariesNode = createLibrariesNode(LibraryTablesRegistrar.getInstance().getLibraryTable(), myGlobalLibrariesProvider, getGlobalLibrariesProvider());

    myApplicationServerLibrariesNode = createLibrariesNode(ApplicationServersManager.getInstance().getLibraryTable(), myApplicationServerLibrariesProvider, getApplicationServerLibrariesProvider());

    ((DefaultTreeModel)myTree.getModel()).reload();

    myDisposed = false;
  }

  protected void updateSelection(NamedConfigurable configurable) {
    final String selectedTab = ModuleEditor.getSelectedTab();
    super.updateSelection(configurable);
    if (configurable instanceof ModuleConfigurable){
      final ModuleConfigurable moduleConfigurable = (ModuleConfigurable)configurable;
      moduleConfigurable.getModuleEditor().setSelectedTabName(selectedTab);
    }
  }

  private MyNode createLibrariesNode(final LibraryTable table,
                                     LibrariesModifiableModel provider,
                                     final LibraryTableModifiableModelProvider modelProvider) {
    provider = new LibrariesModifiableModel(table.getModifiableModel());
    LibrariesConfigurable librariesConfigurable = new LibrariesConfigurable(table.getTableLevel(), provider);
    MyNode node = new MyNode(librariesConfigurable, false);
    final Library[] libraries = provider.getLibraries();
    for (Library library : libraries) {
      addNode(new MyNode(new LibraryConfigurable(modelProvider, library, myProject), true), node);
    }
    myRoot.add(node);
    return node;
  }

  private void createProjectJdks() {
    myJdksNode = new MyNode(new JdksConfigurable(myJdksTreeModel), false);
    final TreeMap<ProjectJdk, ProjectJdk> sdks = myJdksTreeModel.getProjectJdks();
    for (ProjectJdk sdk : sdks.keySet()) {
      final JdkConfigurable configurable = new JdkConfigurable((ProjectJdkImpl)sdks.get(sdk), myJdksTreeModel);
      addNode(new MyNode(configurable, true), myJdksNode);
    }
    myRoot.add(myJdksNode);
  }

  private void createProjectNodes() {
    myProjectNode = new MyNode(myModulesConfigurable, false);
    final Map<ModuleGroup, MyNode> moduleGroup2NodeMap = new HashMap<ModuleGroup, MyNode>();
    final Module[] modules = myModuleManager.getModules();
    for (final Module module : modules) {
      ModuleConfigurable configurable = new ModuleConfigurable(myModulesConfigurator, module);
      final MyNode moduleNode = new MyNode(configurable, true);
      createModuleLibraries(module, moduleNode);
      final String[] groupPath = myModulesConfigurator.getModuleModel().getModuleGroupPath(module);
      if (groupPath == null || groupPath.length == 0){
        addNode(moduleNode, myProjectNode);
      } else {
        final MyNode moduleGroupNode = ModuleGroupUtil
          .buildModuleGroupPath(new ModuleGroup(groupPath), myProjectNode, moduleGroup2NodeMap,
                                new Consumer<ModuleGroupUtil.ParentChildRelation<MyNode>>() {
                                  public void consume(final ModuleGroupUtil.ParentChildRelation<MyNode> parentChildRelation) {
                                    addNode(parentChildRelation.getChild(), parentChildRelation.getParent());
                                  }
                                },
                                new Function<ModuleGroup, MyNode>() {
                                  public MyNode fun(final ModuleGroup moduleGroup) {
                                    final NamedConfigurable moduleGroupConfigurable = new ModuleGroupConfigurable(moduleGroup);
                                    return new MyNode(moduleGroupConfigurable, false, true);
                                  }
                                });
        addNode(moduleNode, moduleGroupNode);
      }
    }

    final LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(myProject);
    myProjectLibrariesProvider = new LibrariesModifiableModel(table.getModifiableModel());
    final LibrariesConfigurable librariesConfigurable = new LibrariesConfigurable(table.getTableLevel(), myProjectLibrariesProvider);

    myProjectLibrariesNode = new MyNode(librariesConfigurable, false);
    final Library[] libraries = myProjectLibrariesProvider.getLibraries();
    for (Library library1 : libraries) {
      addNode(new MyNode(new LibraryConfigurable(getProjectLibrariesProvider(), library1, myProject), true), myProjectLibrariesNode);
    }
    myProjectNode.add(myProjectLibrariesNode);

    myRoot.add(myProjectNode);
  }

  public boolean updateProjectTree(final Module[] modules, final ModuleGroup group) {
    if (myRoot.getChildCount() == 0) return false; //isn't visible
    final MyNode [] nodes = new MyNode[modules.length];
    int i = 0;
    for (Module module : modules) {
      MyNode node = findNodeByObject(myProjectNode, module);
      node.removeFromParent();
      nodes[i ++] = node;
    }
    for (final MyNode moduleNode : nodes) {
      final String[] groupPath = group != null ? group.getGroupPath() : null;
      if (groupPath == null || groupPath.length == 0){
        addNode(moduleNode, myProjectNode);
      } else {
        final MyNode moduleGroupNode = ModuleGroupUtil
          .updateModuleGroupPath(new ModuleGroup(groupPath), myProjectNode, new Function<ModuleGroup, MyNode>() {
            public MyNode fun(final ModuleGroup group) {
              return findNodeByObject(myProjectNode, group);
            }
          }, new Consumer<ModuleGroupUtil.ParentChildRelation<MyNode>>() {
            public void consume(final ModuleGroupUtil.ParentChildRelation<MyNode> parentChildRelation) {
              addNode(parentChildRelation.getChild(), parentChildRelation.getParent());
            }
          }, new Function<ModuleGroup, MyNode>() {
            public MyNode fun(final ModuleGroup moduleGroup) {
              final NamedConfigurable moduleGroupConfigurable = new ModuleGroupConfigurable(moduleGroup);
              return new MyNode(moduleGroupConfigurable, false, true);
            }
          });
        addNode(moduleNode, moduleGroupNode);
      }
    }
    ((DefaultTreeModel)myTree.getModel()).reload(myProjectNode);
    return true;
  }

  protected void addNode(MyNode nodeToAdd, MyNode parent) {
    parent.add(nodeToAdd);
    TreeUtil.sort(parent, new Comparator() {
      public int compare(final Object o1, final Object o2) {
        final MyNode node1 = (MyNode)o1;
        final MyNode node2 = (MyNode)o2;
        final Object editableObject1 = node1.getConfigurable().getEditableObject();
        final Object editableObject2 = node2.getConfigurable().getEditableObject();
        if (editableObject1.getClass() == editableObject2.getClass()) {
          return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
        }

        if (editableObject2 instanceof Module && editableObject1 instanceof ModuleGroup) return -1;
        if (editableObject1 instanceof Module && editableObject2 instanceof ModuleGroup) return 1;

        if (editableObject2 instanceof Module && editableObject1 instanceof String) return 1;
        if (editableObject1 instanceof Module && editableObject2 instanceof String) return -1;

        if (editableObject2 instanceof ModuleGroup && editableObject1 instanceof String) return 1;
        if (editableObject1 instanceof ModuleGroup && editableObject2 instanceof String) return -1;

        return 0;
      }
    });
    ((DefaultTreeModel)myTree.getModel()).reload(parent);
  }

  private void createModuleLibraries(final Module module, final MyNode moduleNode) {
    final LibraryTableModifiableModelProvider libraryTableModelProvider = new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        return myModule2LibrariesMap.get(module);
      }

      public String getTableLevel() {
        return LibraryTableImplUtil.MODULE_LEVEL;
      }
    };

    final OrderEntry[] entries = myModulesConfigurator.getModuleEditor(module).getModifiableRootModel().getOrderEntries();
    for (OrderEntry entry : entries) {
      if (entry instanceof LibraryOrderEntry) {
        final LibraryOrderEntry orderEntry = (LibraryOrderEntry)entry;
        if (orderEntry.isModuleLevel()) {
          final Library library = orderEntry.getLibrary();
          if (library.getName() == null && orderEntry.getPresentableName() == null) continue;
          final LibraryConfigurable libraryConfigurable =
            new LibraryConfigurable(libraryTableModelProvider, library, trancateModuleLibraryName(orderEntry), myProject);
          addNode(new MyNode(libraryConfigurable, false, false), moduleNode);
        }
      }
    }
  }

  public static String trancateModuleLibraryName(LibraryOrderEntry entry) {
    final String presentableName = entry.getPresentableName();
    String independantName = FileUtil.toSystemIndependentName(presentableName);
    if (independantName.lastIndexOf('/') + 1 == independantName.length() && independantName.length() > 1){
      independantName = independantName.substring(0, independantName.length() - 2);
    }
    return independantName.substring(independantName.lastIndexOf("/") + 1);
  }


  public ProjectJdksModel getProjectJdksModel() {
    return myJdksTreeModel;
  }

  public LibraryTableModifiableModelProvider getGlobalLibrariesProvider() {
    return new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        return myGlobalLibrariesProvider;
      }

      public String getTableLevel() {
        return LibraryTablesRegistrar.APPLICATION_LEVEL;
      }
    };
  }

  public LibraryTableModifiableModelProvider getProjectLibrariesProvider() {
    return new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        return myProjectLibrariesProvider;
      }

      public String getTableLevel() {
        return LibraryTablesRegistrar.PROJECT_LEVEL;
      }
    };
  }

  public void reset() {
    myJdksTreeModel.reset();
    myModulesConfigurator = new ModulesConfigurator(myProject, this);
    myModulesConfigurator.resetModuleEditors();
    myModulesConfigurable = myModulesConfigurator.getModulesConfigurable();
    final LibraryTablesRegistrar tablesRegistrar = LibraryTablesRegistrar.getInstance();
    myProjectLibrariesProvider = new LibrariesModifiableModel(tablesRegistrar.getLibraryTable(myProject).getModifiableModel());
    myGlobalLibrariesProvider = new LibrariesModifiableModel(tablesRegistrar.getLibraryTable().getModifiableModel());
    myApplicationServerLibrariesProvider =
      new LibrariesModifiableModel(ApplicationServersManager.getInstance().getLibraryTable().getModifiableModel());
    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      final ModifiableRootModel modelProxy = myModulesConfigurator.getModuleEditor(module).getModifiableRootModelProxy();
      myModule2LibrariesMap.put(module, new LibrariesModifiableModel(modelProxy.getModuleLibraryTable().getModifiableModel()));
    }
    reloadTree();
    super.reset();
  }


  public void apply() throws ConfigurationException {
    boolean modifiedJdks = false;
    for (int i = 0; i < myJdksNode.getChildCount(); i++) {
      final NamedConfigurable configurable = ((MyNode)myJdksNode.getChildAt(i)).getConfigurable();
      if (configurable.isModified()) {
        configurable.apply();
        modifiedJdks = true;
      }
    }

    if (myJdksTreeModel.isModified() || modifiedJdks) myJdksTreeModel.apply();
    myJdksTreeModel.setProjectJdk(ProjectRootManager.getInstance(myProject).getProjectJdk());
    if (isInitialized(myModulesConfigurable) && myModulesConfigurable.isModified()) myModulesConfigurable.apply();
    if (myModulesConfigurator.isModified()) myModulesConfigurator.apply();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        myProjectLibrariesProvider.deferredCommit();
        myGlobalLibrariesProvider.deferredCommit();
        myApplicationServerLibrariesProvider.deferredCommit();
        for (Module module : myModule2LibrariesMap.keySet()) {
          if (!module.isDisposed()){ //do not update deleted modules
            myModule2LibrariesMap.get(module).deferredCommit();
          }
        }
      }
    });

    //cleanup
    disposeUIResources();
    reset();
  }

  public boolean isModified() {
    boolean isModified = myModulesConfigurator.isModified();
    for (LibrariesModifiableModel model : myModule2LibrariesMap.values()) {
      final Library[] libraries = model.getLibraries();
      for (Library library : libraries) {
        if (model.hasLibraryEditor(library) && model.getLibraryEditor(library).hasChanges()) return true;
      }
    }
    for (int i = 0; i < myJdksNode.getChildCount(); i++) {
      final NamedConfigurable configurable = ((MyNode)myJdksNode.getChildAt(i)).getConfigurable();
      if (configurable.isModified()) {
        return true;
      }
    }
    isModified |= isInitialized(myModulesConfigurable) && myModulesConfigurable.isModified();
    isModified |= myJdksTreeModel.isModified();
    isModified |= myGlobalLibrariesProvider.isChanged();
    isModified |= myApplicationServerLibrariesProvider.isChanged();
    isModified |= myProjectLibrariesProvider.isChanged();
    return isModified;
  }

  public void disposeUIResources() {
    myDisposed = true;
    myJdksTreeModel.disposeUIResources();
    myModulesConfigurator.disposeUIResources();
    myModule2LibrariesMap.clear();
    myProjectLibrariesProvider = null;
    myGlobalLibrariesProvider = null;
    myApplicationServerLibrariesProvider = null;
    super.disposeUIResources();
  }


  public JComponent createComponent() {
    return new MyDataProviderWrapper(super.createComponent());
  }

  protected void processRemovedItems() {
    // do nothing
  }

  protected boolean wasObjectStored(Object editableObject) {
    return false;
  }

  public String getDisplayName() {
    return ProjectBundle.message("project.roots.display.name");
  }

  public Icon getIcon() {
    return ICON;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      MyNode node = (MyNode)selectionPath.getLastPathComponent();
      final NamedConfigurable configurable = node.getConfigurable();
      if (configurable != null) {
        return configurable.getHelpTopic();
      }
    }
    return "root.settings";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }


  protected ArrayList<AnAction> createActions() {
    final ArrayList<AnAction> result = new ArrayList<AnAction>();
    result.add(new MyAddAction());
    result.add(new MyRemoveAction(new Condition<Object>() {
      public boolean value(final Object object) {
        if (object instanceof MyNode) {
          final NamedConfigurable namedConfigurable = ((MyNode)object).getConfigurable();
          final Object editableObject = namedConfigurable.getEditableObject();
          if (editableObject instanceof ProjectJdk ||
            editableObject instanceof Module) return true;
          if (editableObject instanceof Library){
            return true;
          }
        }
        return false;
      }
    }));
    return result;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectRootMasterDetailsConfigurable";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public static ProjectRootConfigurable getInstance(final Project project) {
    return project.getComponent(ProjectRootConfigurable.class);
  }

  public void createNode(final NamedConfigurable<ProjectJdk> configurable, final MyNode parentNode) {
    final MyNode node = new MyNode(configurable, true);
    addNode(node, parentNode);
    selectNodeInTree(node);
  }

  public MyNode createLibraryNode(Library library, String presentableName) {
    final LibraryTable table = library.getTable();
    if (table != null){
      final String level = table.getTableLevel();
      if (level == LibraryTablesRegistrar.APPLICATION_LEVEL) {
        final LibraryConfigurable configurable = new LibraryConfigurable(getGlobalLibrariesProvider(), library, myProject);
        final MyNode node = new MyNode(configurable, true);
        addNode(node, myGlobalLibrariesNode);
        return node;
      }
      else if (level == LibraryTablesRegistrar.PROJECT_LEVEL) {
        final LibraryConfigurable configurable = new LibraryConfigurable(getProjectLibrariesProvider(), library, myProject);
        final MyNode node = new MyNode(configurable, true);
        addNode(node, myProjectLibrariesNode);
        return node;
      }
      else {
        final LibraryConfigurable configurable = new LibraryConfigurable(getApplicationServerLibrariesProvider(), library, myProject);
        final MyNode node = new MyNode(configurable, true);
        addNode(node, myApplicationServerLibrariesNode);
        return node;
      }
    } else { //module library
      Module module = (Module)getSelectedObject();
      final LibraryConfigurable configurable = new LibraryConfigurable(getModifiableModelProvider(myModulesConfigurator.getModuleEditor(module).getModifiableRootModelProxy()), library, presentableName, myProject);
      final MyNode node = new MyNode(configurable, true);
      addNode(node, (MyNode)myTree.getSelectionPath().getLastPathComponent());
      return node;
    }
  }

  private void showDependencies() {
    final List<String> dependencies = getDependencies();
    if (dependencies == null || dependencies.size() == 0){
      Messages.showInfoMessage(myTree, FindBundle.message("find.usage.view.no.usages.text"), FindBundle.message("find.pointcut.applications.not.found.title"));
      return;
    }
    final int selectedRow = myTree.getSelectionRows()[0];
    final Rectangle rowBounds = myTree.getRowBounds(selectedRow);
    final Point location = rowBounds.getLocation();
    location.x += rowBounds.width;
    JBPopupFactory.getInstance().createWizardStep(new BaseListPopupStep<String>(ProjectBundle.message("dependencies.used.in.popup.title"), dependencies) {

      public PopupStep onChosen(final String nameToSelect, final boolean finalChoice) {
        selectNodeInTree(nameToSelect);
        return PopupStep.FINAL_CHOICE;
      }

      public Icon getIconFor(String selection){
        return myModulesConfigurator.getModule(selection).getModuleType().getNodeIcon(false);
      }

    }).show(new RelativePoint(myTree, location));
  }

  @Nullable
  private List<String> getDependencies() {
    final Object selectedObject = getSelectedObject();
    if (selectedObject instanceof Module) {
      return getDependencies(new Condition<OrderEntry>() {
        public boolean value(final OrderEntry orderEntry) {
          return orderEntry instanceof ModuleOrderEntry && Comparing.equal(((ModuleOrderEntry)orderEntry).getModule(), selectedObject);
        }
      });
    }
    else if (selectedObject instanceof Library) {
      if (((Library)selectedObject).getTable() == null) { //module library navigation
        final MyNode node = (MyNode)myTree.getSelectionPath().getLastPathComponent();
        final List<String> list = new ArrayList<String>();
        list.add(((MyNode)node.getParent()).getDisplayName());
        return list;
      }
      return getDependencies(new Condition<OrderEntry>() {
        public boolean value(final OrderEntry orderEntry) {
          if (orderEntry instanceof LibraryOrderEntry){
            final LibraryImpl library = (LibraryImpl)((LibraryOrderEntry)orderEntry).getLibrary();
            return library != null && Comparing.equal(library.getSource(), selectedObject);
          }
          return false;
        }
      });
    }
    else if (selectedObject instanceof ProjectJdk) {
      return getDependencies(new Condition<OrderEntry>() {
        public boolean value(final OrderEntry orderEntry) {
          return orderEntry instanceof JdkOrderEntry && Comparing.equal(((JdkOrderEntry)orderEntry).getJdk(), selectedObject);
        }
      });
    }
    return null;
  }

  private List<String> getDependencies(Condition<OrderEntry> condition) {
    final List<String> result = new ArrayList<String>();
    final Module[] modules = myModulesConfigurator.getModules();
    for (Module module : modules) {
      final ModifiableRootModel rootModel = myModulesConfigurator.getModuleEditor(module).getModifiableRootModel();
      final OrderEntry[] entries = rootModel.getOrderEntries();
      for (OrderEntry entry : entries) {
        if (condition.value(entry)) {
          result.add(module.getName());
          break;
        }
      }
    }
    return result;
  }

  @Nullable
  public ProjectJdk getSelectedJdk() {
    final Object object = getSelectedObject();
    if (object instanceof ProjectJdk){
      return myJdksTreeModel.findSdk((ProjectJdk)object);
    }
    return null;
  }

  public void setStartModuleWizard(final boolean show) {
    myModulesConfigurator.getModulesConfigurable().setStartModuleWizardOnShow(show);
  }

  public LibraryTableModifiableModelProvider getApplicationServerLibrariesProvider() {
    return new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        return myApplicationServerLibrariesProvider;
      }

      public String getTableLevel() {
        return ApplicationServersManager.APPLICATION_SERVER_MODULE_LIBRARIES;
      }
    };
  }

  public DefaultMutableTreeNode createLibraryNode(final LibraryOrderEntry libraryOrderEntry, final ModifiableRootModel model) {
    final LibraryConfigurable configurable = new LibraryConfigurable(getModifiableModelProvider(model), libraryOrderEntry.getLibrary(), trancateModuleLibraryName(libraryOrderEntry), myProject);
    final MyNode node = new MyNode(configurable, true);
    addNode(node, findNodeByObject(myProjectNode, libraryOrderEntry.getOwnerModule()));
    return node;
  }

  public void deleteLibraryNode(LibraryOrderEntry libraryOrderEntry) {
    final MyNode node = findNodeByObject(myProjectNode, libraryOrderEntry.getLibrary());
    if (node != null) {
      final TreeNode parent = node.getParent();
      node.removeFromParent();
      ((DefaultTreeModel)myTree.getModel()).reload(parent);
      final Module module = libraryOrderEntry.getOwnerModule();
      myModule2LibrariesMap.get(module).removeLibrary(libraryOrderEntry.getLibrary());
    }
  }

  public Project getProject() {
    return myProject;
  }

  @Nullable
  public Library getLibrary(final Library library) {
    final String level = library.getTable().getTableLevel();
    if (level == LibraryTablesRegistrar.PROJECT_LEVEL) {
      return findLibraryModel(library, myProjectLibrariesProvider);
    }
    else if (level == LibraryTablesRegistrar.APPLICATION_LEVEL) {
      return findLibraryModel(library, myGlobalLibrariesProvider);
    }
    return findLibraryModel(library, myApplicationServerLibrariesProvider);
  }

  @Nullable
  private static Library findLibraryModel(final Library library, LibrariesModifiableModel tableModel) {
    if (tableModel == null) return library;
    if (tableModel.wasLibraryRemoved(library)) return null;
    return tableModel.hasLibraryEditor(library) ? (Library)tableModel.getLibraryEditor(library).getModel() : library;
  }

  public void selectModuleTab(@NotNull final String moduleName, final String tabName) {
    final MyNode node = findNodeByObject(myProjectNode, ModuleManager.getInstance(myProject).findModuleByName(moduleName));
    if (node != null) {
      selectNodeInTree(node);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ModuleConfigurable moduleConfigurable = (ModuleConfigurable)node.getConfigurable();
          moduleConfigurable.getModuleEditor().setSelectedTabName(tabName);
        }
      });
    }
  }

  public boolean addJdkNode(final ProjectJdk jdk) {
    if (!myDisposed) {
      addNode(new MyNode(new JdkConfigurable((ProjectJdkImpl)jdk, myJdksTreeModel), true), myJdksNode);
      return true;
    }
    return false;
  }

  private class MyDataProviderWrapper extends JPanel implements DataProvider {
    public MyDataProviderWrapper(final JComponent component) {
      super(new BorderLayout());
      add(component, BorderLayout.CENTER);
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
      if (DataConstants.MODULE_CONTEXT_ARRAY.equals(dataId)){
        final Object o = getSelectedObject();
        if (o instanceof Module){
          return new Module[]{(Module)o};
        }
      }
      if (DataConstantsEx.MODIFIABLE_MODULE_MODEL.equals(dataId)){
        return myModulesConfigurator.getModuleModel();
      }
      return null;
    }
  }

  private class MyRemoveAction extends MyDeleteAction {
    public MyRemoveAction(final Condition<Object> availableCondition) {
      super(availableCondition);
    }

    public void actionPerformed(AnActionEvent e) {
      final TreePath selectionPath = myTree.getSelectionPath();
      final MyNode node = (MyNode)selectionPath.getLastPathComponent();
      final NamedConfigurable configurable = node.getConfigurable();
      final Object editableObject = configurable.getEditableObject();
      if (editableObject instanceof ProjectJdk) {
        myJdksTreeModel.removeJdk((ProjectJdk)editableObject);
      }
      else if (editableObject instanceof Module) {
        if (!myModulesConfigurator.deleteModule((Module)editableObject)){
          //wait for confirmation
          return;
        }
      }
      else if (editableObject instanceof Library) {
        final Library library = (Library)editableObject;
        final LibraryTable table = library.getTable();
        if (table != null) {
          final String level = table.getTableLevel();
          if (level == LibraryTablesRegistrar.APPLICATION_LEVEL) {
            myGlobalLibrariesProvider.removeLibrary(library);
          }
          else if (level == LibraryTablesRegistrar.PROJECT_LEVEL) {
            myProjectLibrariesProvider.removeLibrary(library);
          }
          else {
            myApplicationServerLibrariesProvider.removeLibrary(library);
          }
        }
        else {
          Module module = (Module)((MyNode)node.getParent()).getConfigurable().getEditableObject();
          myModule2LibrariesMap.get(module).removeLibrary(library);
          myModulesConfigurator.getModuleEditor(module).updateOrderEntriesInEditors(); //in order to update classpath panel
        }
      }
      super.actionPerformed(e);
    }
  }

  private PopupStep createJdksStep(DataContext dataContext) {
    DefaultActionGroup group = new DefaultActionGroup();
    myJdksTreeModel.createAddActions(group, myTree, new Consumer<ProjectJdk>() {
      public void consume(final ProjectJdk projectJdk) {
        addJdkNode(projectJdk);
        selectNodeInTree(findNodeByObject(myJdksNode, projectJdk));
      }
    });
    final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
    return popupFactory.createActionsStep(group, dataContext, false, false, ProjectBundle.message("add.new.jdk.title"), myTree, true);
  }

  private PopupStep createLibrariesStep(AnActionEvent e) {
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new AnAction(ProjectBundle.message("add.new.global.library.text")) {
      public void actionPerformed(AnActionEvent e) {
        final LibraryTableEditor editor = LibraryTableEditor.editLibraryTable(getGlobalLibrariesProvider(), myProject);
        editor.createAddLibraryAction(true, myWholePanel).actionPerformed(null);
        Disposer.dispose(editor);
      }
    });
    group.add(new AnAction(ProjectBundle.message("add.new.project.library.text")) {
      public void actionPerformed(AnActionEvent e) {
        LibraryTableEditor.editLibraryTable(getProjectLibrariesProvider(), myProject).createAddLibraryAction(true, myWholePanel).actionPerformed(null);
      }

      public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(!myProject.isDefault());
      }
    });
    group.add(new AnAction(ProjectBundle.message("add.new.application.server.library.text")) {
      public void actionPerformed(AnActionEvent e) {
        LibraryTableEditor.editLibraryTable(getApplicationServerLibrariesProvider(), myProject).createAddLibraryAction(true, myWholePanel)
          .actionPerformed(null);
      }
    });
    group.add(new AnAction(ProjectBundle.message("add.new.module.library.text")) {
      public void actionPerformed(AnActionEvent e) {
        Module module = (Module)getSelectedObject();
        final LibraryTableModifiableModelProvider modifiableModelProvider = getModifiableModelProvider(myModulesConfigurator.getModuleEditor(module).getModifiableRootModelProxy());
        LibraryTableEditor.editLibraryTable(modifiableModelProvider, myProject).createAddLibraryAction(true, myWholePanel).actionPerformed(null);
      }

      public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(getSelectedObject() instanceof Module);
      }
    });
    final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
    final int defaultOptionIndex;
    final Object selectedObject = getSelectedObject();
    if (selectedObject instanceof String){
      final String libraryTable = (String)selectedObject;
      defaultOptionIndex = Comparing.strEqual(libraryTable, LibraryTablesRegistrar.APPLICATION_LEVEL)
                           ? 0
                           : Comparing.strEqual(libraryTable, LibraryTablesRegistrar.PROJECT_LEVEL) ? 1 : 2;
    } else {
      defaultOptionIndex = 1;  //do not create too many module libraries ;)
    }
    return popupFactory.createActionsStep(group, e.getDataContext(), false, false, ProjectBundle.message("add.new.library.title"), myTree, true, defaultOptionIndex);
  }

  private LibraryTableModifiableModelProvider getModifiableModelProvider(final ModifiableRootModel model) {
    return new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        final LibraryTable.ModifiableModel modifiableModel = model.getModuleLibraryTable().getModifiableModel();
        myModule2LibrariesMap.put(model.getModule(), new LibrariesModifiableModel(modifiableModel));
        return modifiableModel;
      }

      public String getTableLevel() {
        return LibraryTableImplUtil.MODULE_LEVEL;
      }
    };
  }

  private class MyAddAction extends AnAction {

    public MyAddAction() {
      super(CommonBundle.message("button.add"), CommonBundle.message("button.add"), Icons.ADD_ICON);
      registerCustomShortcutSet(CommonShortcuts.INSERT, myTree);
    }

    public void actionPerformed(final AnActionEvent e) {
      JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
      List<String> actions = new ArrayList<String>();
      final String libraryChoice = ProjectBundle.message("add.new.library.text");
      actions.add(libraryChoice);
      final String jdkChoice = ProjectBundle.message("add.new.jdk.text");
      actions.add(jdkChoice);
      final String moduleChoice = ProjectBundle.message("add.new.module.text");
      if (!myProject.isDefault()) {
        actions.add(moduleChoice);
      }
      List<Icon> icons = new ArrayList<Icon>();
      final ListPopup listPopup = jbPopupFactory.createWizardStep(new BaseListPopupStep<String>(ProjectBundle.message("add.action.name"), actions, icons) {
        public boolean hasSubstep(final String selectedValue) {
          return selectedValue.compareTo(moduleChoice) != 0;
        }

        public PopupStep onChosen(final String selectedValue, final boolean finalChoice) {
          if (selectedValue.compareTo(libraryChoice) == 0) {
            return createLibrariesStep(e);
          }
          else if (selectedValue.compareTo(jdkChoice) == 0) {
            return createJdksStep(e.getDataContext());
          }
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              final Module module = myModulesConfigurator.addModule(myTree);
              if (module != null) {
                final MyNode node = new MyNode(new ModuleConfigurable(myModulesConfigurator, module), true);
                addNode(node, myProjectNode);
                selectNodeInTree(node);
              }
            }
          });
          return PopupStep.FINAL_CHOICE;
        }

        public int getDefaultOptionIndex() {
          final Object selectedObject = getSelectedObject();
          if (selectedObject instanceof Library || selectedObject instanceof String){
            return 0;
          } else if (selectedObject instanceof ProjectJdk || selectedObject instanceof ProjectJdksModel){
            return 1;
          } else if (selectedObject instanceof Module){
            return 2;
          }
          return 0;
        }
      });
      listPopup.showUnderneathOf(myNorthPanel);
      final int defaultOptionIndex = listPopup.getListStep().getDefaultOptionIndex();
      if (defaultOptionIndex == 0 || defaultOptionIndex == 1) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ((ListPopupImpl)listPopup).handleSelect(true);
          }
        });
      }
    }

  }
}