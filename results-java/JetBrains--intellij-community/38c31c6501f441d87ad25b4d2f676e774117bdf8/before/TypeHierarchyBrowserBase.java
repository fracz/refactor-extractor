package com.intellij.ide.hierarchy;

import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.ide.DeleteProvider;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.OccurenceNavigatorSupport;
import com.intellij.ide.dnd.DnDAction;
import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDSource;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.TreeToolTipHandler;
import com.intellij.util.Alarm;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public abstract class TypeHierarchyBrowserBase extends HierarchyBrowserBase implements DataProvider, OccurenceNavigator, Disposable {

  @SuppressWarnings({"UnresolvedPropertyKey"})
  public static final String TYPE_HIERARCHY_TYPE = IdeBundle.message("title.hierarchy.class");
  @SuppressWarnings({"UnresolvedPropertyKey"})
  public static final String SUBTYPES_HIERARCHY_TYPE = IdeBundle.message("title.hierarchy.subtypes");
  @SuppressWarnings({"UnresolvedPropertyKey"})
  public static final String SUPERTYPES_HIERARCHY_TYPE = IdeBundle.message("title.hierarchy.supertypes");


  @NonNls private static final String HELP_ID = "reference.toolWindows.hierarchy";
  @NonNls
  public static final String TYPE_HIERARCHY_BROWSER_ID = "TYPE_HIERARCHY_BROWSER_ID";

  protected final Project myProject;
  private final Hashtable<String, HierarchyTreeBuilder> myBuilders = new Hashtable<String, HierarchyTreeBuilder>();
  private final Hashtable<String, JTree> myTrees = new Hashtable<String, JTree>();

  private final RefreshAction myRefreshAction = new RefreshAction();
  private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
  private SmartPsiElementPointer mySmartPsiElementPointer;
  private boolean myIsInterface;
  private final CardLayout myCardLayout;
  private final JPanel myTreePanel;
  private String myCurrentViewName;

  private final AutoScrollToSourceHandler myAutoScrollToSourceHandler;
  private final MyDeleteProvider myDeleteElementProvider = new MyDeleteProvider();

  private boolean myCachedIsValidBase = false;

  private static final String TYPE_HIERARCHY_BROWSER_DATA_CONSTANT = "com.intellij.ide.hierarchy.TypeHierarchyBrowserBase";
  private final List<Runnable> myRunOnDisposeList = new ArrayList<Runnable>();
  private final HashMap<String, OccurenceNavigator> myOccurenceNavigators = new HashMap<String, OccurenceNavigator>();
  private static final OccurenceNavigator EMPTY_NAVIGATOR = new OccurenceNavigator() {
    public boolean hasNextOccurence() {
      return false;
    }

    public boolean hasPreviousOccurence() {
      return false;
    }

    public OccurenceInfo goNextOccurence() {
      return null;
    }

    public OccurenceInfo goPreviousOccurence() {
      return null;
    }

    public String getNextOccurenceActionName() {
      return "";
    }

    public String getPreviousOccurenceActionName() {
      return "";
    }
  };

  public TypeHierarchyBrowserBase(final Project project, final PsiElement psiClass) {
    myProject = project;

    myAutoScrollToSourceHandler = new AutoScrollToSourceHandler() {
      protected boolean isAutoScrollMode() {
        return HierarchyBrowserManager.getInstance(myProject).getState().IS_AUTOSCROLL_TO_SOURCE;
      }

      protected void setAutoScrollMode(final boolean state) {
        HierarchyBrowserManager.getInstance(myProject).getState().IS_AUTOSCROLL_TO_SOURCE = state;
      }
    };

    setHierarchyBase(psiClass);


    myCardLayout = new CardLayout();
    myTreePanel = new JPanel(myCardLayout);

    createTrees(myTrees);

    final Enumeration<String> keys = myTrees.keys();
    while (keys.hasMoreElements()) {
      final String key = keys.nextElement();
      final JTree tree = myTrees.get(key);
      myOccurenceNavigators.put(key, new OccurenceNavigatorSupport(tree) {
        @Nullable
        protected Navigatable createDescriptorForNode(final DefaultMutableTreeNode node) {
          final PsiElement psiElement = getPsiElementFromNodeDescriptor(node.getUserObject());
          if (psiElement == null || !psiElement.isValid()) return null;
          PsiElement navigationElement = psiElement.getNavigationElement();
          return new OpenFileDescriptor(psiElement.getProject(), navigationElement.getContainingFile().getVirtualFile(),
                                        navigationElement.getTextOffset());
        }

        public String getNextOccurenceActionName() {
          return IdeBundle.message("hierarchy.type.next.occurence.name");
        }

        public String getPreviousOccurenceActionName() {
          return IdeBundle.message("hierarchy.type.prev.occurence.name");
        }
      });
      myTreePanel.add(new JScrollPane(tree), key);
    }

    buildUi(createToolbar(ActionPlaces.TYPE_HIERARCHY_VIEW_TOOLBAR, HELP_ID).getComponent(), myTreePanel);
  }

  protected abstract void createTrees(Hashtable<String, JTree> trees);

  public String getCurrentViewName() {
    return myCurrentViewName;
  }

  protected abstract boolean isInterface(PsiElement psiElement);

  public boolean isInterface() {
    return myIsInterface;
  }

  protected JTree createTreeWithoutActions() {
    final DnDAwareTree tree = new DnDAwareTree(new DefaultTreeModel(new DefaultMutableTreeNode("")));
    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
      tree.enableDnd(this);
      DnDManager.getInstance().registerSource(new DnDSource() {
        public boolean canStartDragging(final DnDAction action, final Point dragOrigin) {
          return getSelectedElements().length > 0;
        }

        public DnDDragStartBean startDragging(final DnDAction action, final Point dragOrigin) {
          return new DnDDragStartBean(new AbstractProjectViewPane.TransferableWrapper() {
            public TreeNode[] getTreeNodes() {
              return tree.getSelectedNodes(TreeNode.class, null);
            }

            public PsiElement[] getPsiElements() {
              return getSelectedElements();
            }
          });
        }

        public Pair<Image, Point> createDraggedImage(final DnDAction action, final Point dragOrigin) {
          return null;
        }

        public void dragDropEnd() {
        }

        public void dropActionChanged(final int gestureModifiers) {
        }
      }, tree);
    }
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.setToggleClickCount(-1);
    tree.setCellRenderer(new HierarchyNodeRenderer());
    UIUtil.setLineStyleAngled(tree);
    EditSourceOnDoubleClickHandler.install(tree);

    myRefreshAction.registerShortcutOn(tree);
    myRunOnDisposeList.add(new Runnable() {
      public void run() {
        myRefreshAction.unregisterCustomShortcutSet(tree);
      }
    });

    new TreeSpeedSearch(tree);
    TreeUtil.installActions(tree);
    TreeToolTipHandler.install(tree);
    myAutoScrollToSourceHandler.install(tree);
    return tree;
  }

  private void setHierarchyBase(final PsiElement psiElement) {
    mySmartPsiElementPointer = SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(psiElement);
    myIsInterface = isInterface(psiElement);
  }

  private void restoreCursor() {
    /*int n =*/
    myAlarm.cancelAllRequests();
    //    if (n == 0) {
    getComponent().setCursor(Cursor.getDefaultCursor());
    //    }
  }

  private void setWaitCursor() {
    myAlarm.addRequest(new Runnable() {
      public void run() {
        getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
    }, 100);
  }

  public final void changeView(final String typeName) {
    myCurrentViewName = typeName;

    final PsiElement element = mySmartPsiElementPointer.getElement();
    if (!isApplicableElement(element)) {
      return;
    }

    if (myContent != null) {
      myContent.setDisplayName(MessageFormat.format(typeName, getNameForClass(element)));
    }

    myCardLayout.show(myTreePanel, typeName);

    if (!myBuilders.containsKey(typeName)) {
      try {
        setWaitCursor();

        // create builder
        final JTree tree = myTrees.get(typeName);
        final DefaultTreeModel model = /*(DefaultTreeModel)tree.getModel()*/ new DefaultTreeModel(new DefaultMutableTreeNode(""));
        tree.setModel(model);

        PsiDocumentManager.getInstance(myProject).commitAllDocuments();

        final HierarchyTreeStructure structure = createHierarchyTreeStructure(typeName, element);
        if (structure == null) return;
        final Comparator<NodeDescriptor> comparator = getComparator();
        final HierarchyTreeBuilder builder = new HierarchyTreeBuilder(myProject, tree, model, structure, comparator);

        myBuilders.put(typeName, builder);

        final HierarchyNodeDescriptor baseDescriptor = structure.getBaseDescriptor();
        builder.buildNodeForElement(baseDescriptor);
        final DefaultMutableTreeNode node = builder.getNodeForElement(baseDescriptor);
        if (node != null) {
          final TreePath path = new TreePath(node.getPath());
          tree.expandPath(path);
          TreeUtil.selectPath(tree, path);
        }
      }
      finally {
        restoreCursor();
      }
    }

    getCurrentTree().requestFocus();
  }

  protected abstract String getNameForClass(final PsiElement element);

  protected abstract boolean isApplicableElement(final PsiElement element);

  protected abstract HierarchyTreeStructure createHierarchyTreeStructure(final String typeName, final PsiElement psiElement);

  protected abstract Comparator<NodeDescriptor> getComparator();

  protected void addSpecificActions(final DefaultActionGroup actionGroup) {
    actionGroup.add(new ViewClassHierarchyAction());
    actionGroup.add(new ViewSupertypesHierarchyAction());
    actionGroup.add(new ViewSubtypesHierarchyAction());
    actionGroup.add(new AlphaSortAction());
    actionGroup.add(myRefreshAction);
    actionGroup.add(myAutoScrollToSourceHandler.createToggleAction());
  }

  public boolean hasNextOccurence() {
    return getOccurrenceNavigator().hasNextOccurence();
  }

  public boolean hasPreviousOccurence() {
    return getOccurrenceNavigator().hasPreviousOccurence();
  }

  public OccurenceInfo goNextOccurence() {
    return getOccurrenceNavigator().goNextOccurence();
  }

  public OccurenceInfo goPreviousOccurence() {
    return getOccurrenceNavigator().goPreviousOccurence();
  }

  public String getNextOccurenceActionName() {
    return getOccurrenceNavigator().getNextOccurenceActionName();
  }

  public String getPreviousOccurenceActionName() {
    return getOccurrenceNavigator().getPreviousOccurenceActionName();
  }

  private OccurenceNavigator getOccurrenceNavigator() {
    if (myCurrentViewName == null) {
      return EMPTY_NAVIGATOR;
    }
    final OccurenceNavigator navigator = myOccurenceNavigators.get(myCurrentViewName);
    return navigator != null ? navigator : EMPTY_NAVIGATOR;
  }

  final class RefreshAction extends com.intellij.ide.actions.RefreshAction {
    public RefreshAction() {
      super(IdeBundle.message("action.refresh"), IdeBundle.message("action.refresh"), IconLoader.getIcon("/actions/sync.png"));
    }

    public final void actionPerformed(final AnActionEvent e) {
      PsiDocumentManager.getInstance(myProject).commitAllDocuments();
      if (!isValidBase()) return;

      final Object[] storedInfo = new Object[1];
      if (myCurrentViewName != null) {
        final HierarchyTreeBuilder builder = myBuilders.get(myCurrentViewName);
        storedInfo[0] = builder.storeExpandedAndSelectedInfo();
      }

      final PsiElement base = mySmartPsiElementPointer.getElement();
      final String[] name = new String[]{myCurrentViewName};
      dispose();
      setHierarchyBase(base);
      getComponent().validate();
      if (myIsInterface && TYPE_HIERARCHY_TYPE.equals(name[0])) {
        name[0] = SUBTYPES_HIERARCHY_TYPE;
      }
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          changeView(name[0]);
          if (storedInfo != null) {
            final HierarchyTreeBuilder builder = myBuilders.get(myCurrentViewName);
            builder.restoreExpandedAndSelectedInfo(storedInfo[0]);
          }
        }
      });
    }

    public final void update(final AnActionEvent event) {
      final Presentation presentation = event.getPresentation();
      presentation.setEnabled(isValidBase());
    }
  }

  public boolean isValidBase() {
    if (PsiDocumentManager.getInstance(myProject).getUncommittedDocuments().length > 0) {
      return myCachedIsValidBase;
    }

    final PsiElement element = mySmartPsiElementPointer.getElement();
    myCachedIsValidBase = isApplicableElement(element) && element.isValid();
    return myCachedIsValidBase;
  }

  protected JTree getCurrentTree() {
    if (myCurrentViewName == null) return null;
    return myTrees.get(myCurrentViewName);
  }

  public final Object getData(final String dataId) {
    if (DataConstants.DELETE_ELEMENT_PROVIDER.equals(dataId)) {
      return myDeleteElementProvider;
    }
    if (DataConstants.HELP_ID.equals(dataId)) {
      return HELP_ID;
    }
    if (TYPE_HIERARCHY_BROWSER_DATA_CONSTANT.equals(dataId)) {
      return this;
    }
    if (TYPE_HIERARCHY_BROWSER_ID.equals(dataId)) {
      return this;
    }

    return super.getData(dataId);
  }

  public final void dispose() {
    final Collection<HierarchyTreeBuilder> builders = myBuilders.values();
    for (final HierarchyTreeBuilder builder : builders) {
      Disposer.dispose(builder);
    }
    for (final Runnable aMyRunOnDisposeList : myRunOnDisposeList) {
      aMyRunOnDisposeList.run();
    }
    myRunOnDisposeList.clear();
    myBuilders.clear();
  }

  private final class AlphaSortAction extends ToggleAction {
    public AlphaSortAction() {
      super(IdeBundle.message("action.sort.alphabetically"), IdeBundle.message("action.sort.alphabetically"),
            IconLoader.getIcon("/objectBrowser/sorted.png"));
    }

    public final boolean isSelected(final AnActionEvent event) {
      return HierarchyBrowserManager.getInstance(myProject).getState().SORT_ALPHABETICALLY;
    }

    public final void setSelected(final AnActionEvent event, final boolean flag) {
      final HierarchyBrowserManager hierarchyBrowserManager = HierarchyBrowserManager.getInstance(myProject);
      hierarchyBrowserManager.getState().SORT_ALPHABETICALLY = flag;
      final Comparator<NodeDescriptor> comparator = getComparator();
      final Collection<HierarchyTreeBuilder> builders = myBuilders.values();
      for (final HierarchyTreeBuilder builder : builders) {
        builder.setNodeDescriptorComparator(comparator);
      }
    }

    public final void update(final AnActionEvent event) {
      super.update(event);
      final Presentation presentation = event.getPresentation();
      presentation.setEnabled(isValidBase());
    }
  }

  public static class BaseOnThisTypeAction extends AnAction {
    public final void actionPerformed(final AnActionEvent event) {
      final DataContext dataContext = event.getDataContext();
      final TypeHierarchyBrowserBase browser = (TypeHierarchyBrowserBase)dataContext.getData(TYPE_HIERARCHY_BROWSER_DATA_CONSTANT);
      if (browser == null) return;

      final PsiElement selectedClass = browser.getSelectedElement();
      if (selectedClass == null) return;
      final String[] name = new String[]{browser.myCurrentViewName};
      browser.dispose();
      browser.setHierarchyBase(selectedClass);
      browser.getComponent().validate();
      if (browser.myIsInterface && TYPE_HIERARCHY_TYPE.equals(name[0])) {
        name[0] = SUBTYPES_HIERARCHY_TYPE;
      }
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          browser.changeView(name[0]);
        }
      });
    }

    public final void update(final AnActionEvent event) {
      final Presentation presentation = event.getPresentation();
      registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).getShortcutSet(), null);

      final DataContext dataContext = event.getDataContext();
      final TypeHierarchyBrowserBase browser = (TypeHierarchyBrowserBase)dataContext.getData(TYPE_HIERARCHY_BROWSER_DATA_CONSTANT);
      if (browser == null) {
        presentation.setVisible(false);
        presentation.setEnabled(false);
        return;
      }

      presentation.setVisible(true);

      final PsiElement selectedClass = browser.getSelectedElement();
      if (isEnabled(browser, selectedClass)) {
        presentation.setText(
          browser.isInterface(selectedClass) ? IdeBundle.message("action.base.on.this.interface") : IdeBundle.message("action.base.on.this.class"));
        presentation.setEnabled(true);
      }
      else {
        presentation.setEnabled(false);
      }
    }

    protected boolean isEnabled(final TypeHierarchyBrowserBase browser, final PsiElement psiElement) {
      return psiElement != null && !psiElement.equals(browser.mySmartPsiElementPointer.getElement()) && psiElement.isValid();
    }
  }

  private final class MyDeleteProvider implements DeleteProvider {
    public final void deleteElement(final DataContext dataContext) {
      final PsiElement aClass = getSelectedElement();
      if (!canBeDeleted(aClass)) return;
      LocalHistoryAction a = LocalHistory.startAction(myProject, IdeBundle.message("progress.deleting.class", getQualifiedName(aClass)));
      try {
        final PsiElement[] elements = new PsiElement[]{aClass};
        DeleteHandler.deletePsiElement(elements, myProject);
      }
      finally {
        a.finish();
      }
    }

    public final boolean canDeleteElement(final DataContext dataContext) {
      final PsiElement aClass = getSelectedElement();
      if (!canBeDeleted(aClass)) {
        return false;
      }
      final PsiElement[] elements = new PsiElement[]{aClass};
      return DeleteHandler.shouldEnableDeleteAction(elements);
    }
  }

  protected abstract boolean canBeDeleted(PsiElement psiElement);

  protected abstract String getQualifiedName(PsiElement psiElement);
}