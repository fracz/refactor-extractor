/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.openapi.ui;

import com.intellij.CommonBundle;
import com.intellij.ide.ui.SplitterProportionsDataImpl;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.*;
import com.intellij.ui.navigation.History;
import com.intellij.ui.navigation.Place;
import com.intellij.util.Icons;
import com.intellij.util.containers.HashSet;
import com.intellij.util.ui.Tree;
import com.intellij.util.ui.UIUtil;
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
 * Date: 29-May-2006
 */
public abstract class MasterDetailsComponent implements Configurable, PersistentStateComponent<MasterDetailsComponent.UIState>, DetailsComponent.Facade {
  protected static final Logger LOG = Logger.getInstance("#com.intellij.openapi.ui.MasterDetailsComponent");
  protected static final Icon COPY_ICON = IconLoader.getIcon("/actions/copy.png");
  protected NamedConfigurable myCurrentConfigurable;
  private final Splitter mySplitter;


  @NonNls public static final String TREE_OBJECT = "treeObject";
  @NonNls public static final String TREE_NAME = "treeName";

  protected History myHistory = new History(new Place.Navigator() {
    public ActionCallback navigateTo(@Nullable final Place place, final boolean requestFocus) {
      return null;
    }

    public void queryPlace(@NotNull final Place place) {
    }
  });

  public void setHistory(final History history) {
    myHistory = history;
  }

  public static class UIState {
    public SplitterProportionsDataImpl proportions = new SplitterProportionsDataImpl();
    public String lastEditedConfigurable;
    public List<String> order = new ArrayList<String>();
  }

  protected UIState myState = new UIState();

  protected Runnable TREE_UPDATER;

  {
    TREE_UPDATER = new Runnable() {
      public void run() {
        MyNode node = (MyNode)myTree.getSelectionPath().getLastPathComponent();
        if (node != null) {
          myState.lastEditedConfigurable = node.getDisplayName(); //survive after rename
          myDetails.setText(node.getConfigurable().getBannerSlogan());
          ((DefaultTreeModel)myTree.getModel()).reload(node);
          fireItemsChangedExternally();
        }
      }
    };
  }

  protected MyNode myRoot = new MyRootNode();
  protected Tree myTree = new Tree();

  private DetailsComponent myDetails = new DetailsComponent();
  protected JPanel myWholePanel;
  public JPanel myNorthPanel = new JPanel(new BorderLayout());

  private ArrayList<ItemsChangeListener> myListners = new ArrayList<ItemsChangeListener>();

  private Set<NamedConfigurable> myInitializedConfigurables = new HashSet<NamedConfigurable>();

  private boolean myHasDeletedItems;
  protected AutoScrollToSourceHandler myAutoScrollHandler;

  protected MasterDetailsComponent() {
    myWholePanel = new JPanel(new BorderLayout()) {
      public void addNotify() {
        super.addNotify();
        MasterDetailsComponent.this.addNotify();
      }
    };
    mySplitter = new Splitter(false, .2f);
    mySplitter.setHonorComponentsMinimumSize(true);
    myWholePanel.add(mySplitter, BorderLayout.CENTER);

    JPanel left = new JPanel(new BorderLayout()) {
      public Dimension getMinimumSize() {
        final Dimension original = super.getMinimumSize();
        return new Dimension(Math.max(original.width, 100), original.height);
      }
    };

    left.add(myNorthPanel, BorderLayout.NORTH);
    left.add(new JScrollPane(myTree), BorderLayout.CENTER);
    mySplitter.setFirstComponent(left);

    final JPanel right = new JPanel(new BorderLayout());
    right.add(myDetails.getComponent(), BorderLayout.CENTER);

    mySplitter.setSecondComponent(right);

    myAutoScrollHandler = new AutoScrollToSourceHandler() {
      protected boolean isAutoScrollMode() {
        return isAutoScrollEnabled();
      }

      protected void setAutoScrollMode(boolean state) {
        //do nothing
      }

      protected void scrollToSource(Component tree) {
        updateSelectionFromTree();
      }

      protected boolean needToCheckFocus() {
        return false;
      }
    };
    myAutoScrollHandler.install(myTree);
    GuiUtils.replaceJSplitPaneWithIDEASplitter(myWholePanel);
  }

  protected void addNotify() {
    updateSelectionFromTree();
  }

  private void updateSelectionFromTree() {
    TreePath[] treePaths = myTree.getSelectionPaths();
    if (treePaths != null) {
      List<NamedConfigurable> selectedConfigurables = new ArrayList<NamedConfigurable>();
      for (TreePath path : treePaths) {
        Object lastPathComponent = path.getLastPathComponent();
        if (lastPathComponent instanceof MyNode) {
          selectedConfigurables.add(((MyNode)lastPathComponent).getConfigurable());
        }
      }
      if (selectedConfigurables.size() > 1 && updateMultiSelection(selectedConfigurables)) {
        return;
      }
    }

    final TreePath path = myTree.getSelectionPath();
    if (path != null) {
      final Object lastPathComp = path.getLastPathComponent();
      if (!(lastPathComp instanceof MyNode)) return;
      final MyNode node = (MyNode)lastPathComp;
      final NamedConfigurable configurable = node.getConfigurable();
      updateSelection(configurable);
    } else {
      updateSelection(null);
    }
  }

  protected boolean updateMultiSelection(final List<NamedConfigurable> selectedConfigurables) {
    return false;
  }

  public DetailsComponent getDetailsComponent() {
    return myDetails;
  }

  public Splitter getSplitter() {
    return mySplitter;
  }

  protected boolean isAutoScrollEnabled() {
    return myHistory != null ? !myHistory.isNavigatingNow() : true;
  }

  private void initToolbar() {
    final ArrayList<AnAction> actions = createActions(false);
    if (actions != null) {
      final DefaultActionGroup group = new DefaultActionGroup();
      for (AnAction action : actions) {
        if (action instanceof ActionGroupWithPreselection) {
          group.add(new MyActionGroupWrapper((ActionGroupWithPreselection)action));
        }
        else {
          group.add(action);
        }
      }
      final JComponent component = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true).getComponent();
      myNorthPanel.add(component, BorderLayout.NORTH);
    }
  }

  protected void addItemsChangeListener(ItemsChangeListener l) {
    myListners.add(l);
  }

  public JComponent createComponent() {
    SwingUtilities.updateComponentTreeUI(myWholePanel);
    final JPanel panel = new JPanel(new BorderLayout()) {
      public Dimension getPreferredSize() {
        return new Dimension(800, 600);
      }
    };
    panel.add(myWholePanel, BorderLayout.CENTER);
    return panel;
  }

  public boolean isModified() {
    if (myHasDeletedItems) return true;
    final boolean[] modified = new boolean[1];
    TreeUtil.traverseDepth(myRoot, new TreeUtil.Traverse() {
      public boolean accept(Object node) {
        if (node instanceof MyNode) {
          final NamedConfigurable configurable = ((MyNode)node).getConfigurable();
          if (isInitialized(configurable) && configurable.isModified()) {
            modified[0] = true;
            return false;
          }
        }
        return true;
      }
    });
    return modified[0];
  }

  protected boolean isInitialized(final NamedConfigurable configurable) {
    return myInitializedConfigurables.contains(configurable);
  }

  public void apply() throws ConfigurationException {
    processRemovedItems();
    final ConfigurationException[] ex = new ConfigurationException[1];
    TreeUtil.traverse(myRoot, new TreeUtil.Traverse() {
      public boolean accept(Object node) {
        if (node instanceof MyNode) {
          try {
            final NamedConfigurable configurable = ((MyNode)node).getConfigurable();
            if (isInitialized(configurable) && configurable.isModified()) {
              configurable.apply();
            }
          }
          catch (ConfigurationException e) {
            ex[0] = e;
            return false;
          }
        }
        return true;
      }
    });
    if (ex[0] != null) {
      throw ex[0];
    }
    myHasDeletedItems = false;
  }

  protected abstract void processRemovedItems();

  protected abstract boolean wasObjectStored(Object editableObject);

  public void reset() {
    myHasDeletedItems = false;
    ((DefaultTreeModel)myTree.getModel()).reload();
    myTree.requestFocus();
    myState.proportions.restoreSplitterProportions(myWholePanel);

    final Enumeration enumeration = myRoot.breadthFirstEnumeration();
    boolean selected = false;
    while (enumeration.hasMoreElements()) {
      final MyNode node = (MyNode)enumeration.nextElement();
      if (node instanceof MyRootNode) continue;
      final Object userObject = node.getUserObject();
      if (userObject instanceof Configurable) {
        final Configurable configurable = (Configurable)userObject;
        final String displayName = configurable.getDisplayName();
        if (!selected && Comparing.strEqual(displayName, myState.lastEditedConfigurable)) {
          TreeUtil.selectInTree(node, true, myTree);
          selected = true;
        }
      }
    }
    if (!selected) {
      TreeUtil.selectFirstNode(myTree);
    }
  }


  public UIState getState() {
    return myState;
  }

  public void loadState(final UIState object) {
    myState.lastEditedConfigurable = object.lastEditedConfigurable;
    myState.proportions = object.proportions;
    myState.order = object.order;
  }

  public void disposeUIResources() {
    myState.proportions.saveSplitterProportions(myWholePanel);
    myAutoScrollHandler.cancelAllRequests();
    myDetails.disposeUIResources();
    myInitializedConfigurables.clear();
    TreeUtil.traverseDepth(myRoot, new TreeUtil.Traverse() {
      public boolean accept(Object node) {
        if (node instanceof MyNode) {
          final MyNode treeNode = ((MyNode)node);
          treeNode.getConfigurable().disposeUIResources();
          if (!(treeNode instanceof MyRootNode)) {
            treeNode.setUserObject(null);
          }
        }
        return true;
      }
    });
    myRoot.removeAllChildren();
    myCurrentConfigurable = null;
  }

  @Nullable
  protected ArrayList<AnAction> createActions(final boolean fromPopup) {
    return null;
  }


  protected void initTree() {
    ((DefaultTreeModel)myTree.getModel()).setRoot(myRoot);
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    UIUtil.setLineStyleAngled(myTree);
    TreeUtil.installActions(myTree);
    myTree.setCellRenderer(new ColoredTreeCellRenderer() {
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        if (value instanceof MyNode) {
          final MyNode node = ((MyNode)value);
          setIcon(node.getConfigurable().getIcon(expanded));
          final Font font = UIUtil.getTreeFont();
          if (node.isDisplayInBold()) {
            setFont(font.deriveFont(Font.BOLD));
          }
          else {
            setFont(font.deriveFont(Font.PLAIN));
          }
          append(node.getDisplayName(),
                 node.isDisplayInBold() ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }
    });
    initToolbar();
    ArrayList<AnAction> actions = createActions(true);
    if (actions != null) {
      final DefaultActionGroup group = new DefaultActionGroup();
      for (AnAction action : actions) {
        group.add(action);
      }
      actions = getAdditionalActions();
      if (actions != null) {
        group.addSeparator();
        for (AnAction action : actions) {
          group.add(action);
        }
      }
      PopupHandler
        .installPopupHandler(myTree, group, ActionPlaces.UNKNOWN, ActionManager.getInstance()); //popup should follow the selection
    }
  }

  @Nullable
  protected ArrayList<AnAction> getAdditionalActions() {
    return null;
  }

  public void fireItemsChangeListener(final Object editableObject) {
    for (ItemsChangeListener listner : myListners) {
      listner.itemChanged(editableObject);
    }
  }

  private void fireItemsChangedExternally() {
    for (ItemsChangeListener listner : myListners) {
      listner.itemsExternallyChanged();
    }
  }

  private void createUIComponents() {
    myTree = new Tree() {
      public Dimension getPreferredScrollableViewportSize() {
        Dimension size = super.getPreferredScrollableViewportSize();
        size = new Dimension(size.width + 20, size.height);
        return size;
      }

      @SuppressWarnings({"NonStaticInitializer"})
      public JToolTip createToolTip() {
        final JToolTip toolTip = new JToolTip() {
          {
            setUI(new MultiLineTooltipUI());
          }
        };
        toolTip.setComponent(this);
        return toolTip;
      }
    };
  }

  protected void addNode(MyNode nodeToAdd, MyNode parent) {
    parent.add(nodeToAdd);
    TreeUtil.sort(parent, new Comparator() {
      public int compare(final Object o1, final Object o2) {
        MyNode node1 = (MyNode)o1;
        MyNode node2 = (MyNode)o2;
        return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
      }
    });
    ((DefaultTreeModel)myTree.getModel()).reload(parent);
  }

  public ActionCallback selectNodeInTree(final DefaultMutableTreeNode nodeToSelect) {
    return selectNodeInTree(nodeToSelect, true);
  }

  public ActionCallback selectNodeInTree(final DefaultMutableTreeNode nodeToSelect, boolean center) {
    myTree.requestFocus();
    if (nodeToSelect != null) {
      return TreeUtil.selectInTree(nodeToSelect, true, myTree, center);
    }
    else {
      return TreeUtil.selectFirstNode(myTree);
    }
  }

  @Nullable
  public Object getSelectedObject() {
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null && selectionPath.getLastPathComponent() instanceof MyNode) {
      MyNode node = (MyNode)selectionPath.getLastPathComponent();
      final NamedConfigurable configurable = node.getConfigurable();
      LOG.assertTrue(configurable != null, "already disposed");
      return configurable.getEditableObject();
    }
    return null;
  }

  @Nullable
  public NamedConfigurable getSelectedConfugurable() {
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      MyNode node = (MyNode)selectionPath.getLastPathComponent();
      final NamedConfigurable configurable = node.getConfigurable();
      LOG.assertTrue(configurable != null, "already disposed");
      return configurable;
    }
    return null;
  }

  public void selectNodeInTree(String displayName) {
    final MyNode nodeByName = findNodeByName(myRoot, displayName);
    selectNodeInTree(nodeByName);
  }

  public void selectNodeInTree(final Object object) {
    selectNodeInTree(findNodeByObject(myRoot, object));
  }

  @Nullable
  protected static MyNode findNodeByName(final TreeNode root, final String profileName) {
    if (profileName == null) return null; //do not suggest root node
    return findNodeByCondition(root, new Condition<NamedConfigurable>() {
      public boolean value(final NamedConfigurable configurable) {
        return Comparing.strEqual(profileName, configurable.getDisplayName());
      }
    });
  }

  @Nullable
  public static MyNode findNodeByObject(final TreeNode root, final Object editableObject) {
    if (editableObject == null) return null; //do not suggest root node
    return findNodeByCondition(root, new Condition<NamedConfigurable>() {
      public boolean value(final NamedConfigurable configurable) {
        return Comparing.equal(editableObject, configurable.getEditableObject());
      }
    });
  }

  protected static MyNode findNodeByCondition(final TreeNode root, final Condition<NamedConfigurable> condition) {
    final MyNode[] nodeToSelect = new MyNode[1];
    TreeUtil.traverseDepth(root, new TreeUtil.Traverse() {
      public boolean accept(Object node) {
        if (condition.value(((MyNode)node).getConfigurable())) {
          nodeToSelect[0] = (MyNode)node;
          return false;
        }
        return true;
      }
    });
    return nodeToSelect[0];
  }

  protected void updateSelection(@Nullable NamedConfigurable configurable) {
    if (configurable != null) {
      myState.lastEditedConfigurable = configurable.getDisplayName();
    }
    myDetails.setText(configurable != null ? configurable.getBannerSlogan() : null);

    myCurrentConfigurable = configurable;

    if (configurable != null) {
      myDetails.setContent(configurable.createComponent());
      if (!isInitialized(configurable)) {
        configurable.reset();
        initializeConfigurable(configurable);
      }
       myHistory.pushPlaceForElement(TREE_OBJECT, configurable.getEditableObject());
    } else {
      myDetails.setContent(null);
      myDetails.setEmptyContentText(getEmptySelectionString());
    }
  }

  protected @Nullable String getEmptySelectionString() {
    return null;
  }

  protected void initializeConfigurable(final NamedConfigurable configurable) {
    myInitializedConfigurables.add(configurable);
  }

  protected void checkApply(Set<MyNode> rootNodes, String prefix, String title) throws ConfigurationException {
    for (MyNode rootNode : rootNodes) {
      final Set<String> names = new HashSet<String>();
      for (int i = 0; i < rootNode.getChildCount(); i++) {
        final MyNode node = (MyNode)rootNode.getChildAt(i);
        final NamedConfigurable scopeConfigurable = node.getConfigurable();
        final String name = scopeConfigurable.getDisplayName();
        if (name.trim().length() == 0) {
          selectNodeInTree(node);
          throw new ConfigurationException("Name should contain non-space characters");
        }
        if (names.contains(name)) {
          final NamedConfigurable selectedConfugurable = getSelectedConfugurable();
          if (selectedConfugurable == null || !Comparing.strEqual(selectedConfugurable.getDisplayName(), name)) {
            selectNodeInTree(node);
          }
          throw new ConfigurationException(CommonBundle.message("smth.already.exist.error.message", prefix, name), title);
        }
        names.add(name);
      }
    }
  }

  public Tree getTree() {
    return myTree;
  }

  protected class MyDeleteAction extends AnAction {
    private final Condition<Object> myCondition;

    public MyDeleteAction(Condition<Object> availableCondition) {
      super(CommonBundle.message("button.delete"), CommonBundle.message("button.delete"), Icons.DELETE_ICON);
      registerCustomShortcutSet(CommonShortcuts.DELETE, myTree);
      myCondition = availableCondition;
    }

    public void update(AnActionEvent e) {
      final Presentation presentation = e.getPresentation();
      presentation.setEnabled(false);
      final TreePath[] selectionPath = myTree.getSelectionPaths();
      if (selectionPath != null) {
        for (TreePath path : selectionPath) {
          if (!myCondition.value(path.getLastPathComponent())) return;
        }
        presentation.setEnabled(true);
      }
    }

    public void actionPerformed(AnActionEvent e) {
      removePaths(myTree.getSelectionPaths());
    }

    protected void removePaths(final TreePath[] paths) {
      MyNode parentNode = null;
      int idx = -1;
      for (TreePath path : paths) {
        final MyNode node = (MyNode)path.getLastPathComponent();
        final NamedConfigurable namedConfigurable = node.getConfigurable();
        final Object editableObject = namedConfigurable.getEditableObject();
        parentNode = (MyNode)node.getParent();
        idx = parentNode.getIndex(node);
        parentNode.remove(node);
        myHasDeletedItems |= wasObjectStored(editableObject);
        fireItemsChangeListener(editableObject);
        namedConfigurable.disposeUIResources();
      }
      ((DefaultTreeModel)myTree.getModel()).reload();
      if (parentNode != null && idx != -1) {
        TreeUtil
          .selectInTree((DefaultMutableTreeNode)(idx < parentNode.getChildCount() ? parentNode.getChildAt(idx) : parentNode), true, myTree);
      }
      else {
        TreeUtil.selectFirstNode(myTree);
      }
    }
  }

  public static class MyNode extends DefaultMutableTreeNode {
    private boolean myDisplayInBold;

    public MyNode(NamedConfigurable userObject) {
      super(userObject);
    }

    public MyNode(NamedConfigurable userObject, boolean displayInBold) {
      super(userObject);
      myDisplayInBold = displayInBold;
    }

    @NotNull
    public String getDisplayName() {
      final NamedConfigurable configurable = ((NamedConfigurable)getUserObject());
      LOG.assertTrue(configurable != null, "Tree was already disposed");
      return configurable.getDisplayName();
    }

    public NamedConfigurable getConfigurable() {
      return (NamedConfigurable)getUserObject();
    }

    public boolean isDisplayInBold() {
      return myDisplayInBold;
    }
  }

  @SuppressWarnings({"ConstantConditions"})
  private static class MyRootNode extends MyNode {
    public MyRootNode() {
      super(new NamedConfigurable(false, null) {
        public void setDisplayName(String name) {
        }

        public Object getEditableObject() {
          return null;
        }

        public String getBannerSlogan() {
          return null;
        }

        public String getDisplayName() {
          return "";
        }

        public Icon getIcon() {
          return IconLoader.getIcon("/general/applicationSettings.png");
        }

        @Nullable
        @NonNls
        public String getHelpTopic() {
          return null;
        }

        public JComponent createOptionsPanel() {
          return null;
        }

        public boolean isModified() {
          return false;
        }

        public void apply() throws ConfigurationException {
        }

        public void reset() {
        }

        public void disposeUIResources() {
        }

      }, false);
    }
  }

  protected interface ItemsChangeListener {
    void itemChanged(@Nullable Object deletedItem);

    void itemsExternallyChanged();
  }

  public static interface ActionGroupWithPreselection {
    ActionGroup getActionGroup();

    int getDefaultIndex();
  }

  protected class MyActionGroupWrapper extends AnAction {
    private ActionGroup myActionGroup;
    private ActionGroupWithPreselection myPreselection;

    public MyActionGroupWrapper(final ActionGroupWithPreselection actionGroup) {
      this(actionGroup.getActionGroup());
      myPreselection = actionGroup;
    }

    public MyActionGroupWrapper(final ActionGroup actionGroup) {
      super(actionGroup.getTemplatePresentation().getText(), actionGroup.getTemplatePresentation().getDescription(),
            actionGroup.getTemplatePresentation().getIcon());
      myActionGroup = actionGroup;
      registerCustomShortcutSet(actionGroup.getShortcutSet(), myTree);
    }

    public void actionPerformed(AnActionEvent e) {
      final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
      final ListPopupStep step = popupFactory.createActionsStep(myActionGroup, e.getDataContext(), false, false,
                                                                myActionGroup.getTemplatePresentation().getText(), myTree, true,
                                                                myPreselection != null ? myPreselection.getDefaultIndex() : 0, true);
      final ListPopup listPopup = popupFactory.createListPopup(step);
      listPopup.setHandleAutoSelectionBeforeShow(true);
      listPopup.showUnderneathOf(myNorthPanel);
    }
  }

}