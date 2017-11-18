package com.intellij.ide.util.treeView;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import com.intellij.ui.LoadingNode;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.Time;
import com.intellij.util.concurrency.WorkerThread;
import com.intellij.util.containers.HashSet;
import com.intellij.util.enumeration.EnumerationCopy;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.ui.update.Activatable;
import com.intellij.util.ui.update.UiNotifyConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ComponentEvent;
import java.security.AccessControlException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AbstractTreeUi {
  private static final Logger LOG = Logger.getInstance("#com.intellij.ide.util.treeView.AbstractTreeBuilder");
  protected JTree myTree;// protected for TestNG
  @SuppressWarnings({"WeakerAccess"}) protected DefaultTreeModel myTreeModel;
  private AbstractTreeStructure myTreeStructure;
  private AbstractTreeUpdater myUpdater;
  private Comparator<NodeDescriptor> myNodeDescriptorComparator;
  private final Comparator<TreeNode> myNodeComparator = new Comparator<TreeNode>() {
    public int compare(TreeNode n1, TreeNode n2) {
      if (isLoadingNode(n1) || isLoadingNode(n2)) return 0;
      NodeDescriptor nodeDescriptor1 = (NodeDescriptor)((DefaultMutableTreeNode)n1).getUserObject();
      NodeDescriptor nodeDescriptor2 = (NodeDescriptor)((DefaultMutableTreeNode)n2).getUserObject();
      return myNodeDescriptorComparator != null
             ? myNodeDescriptorComparator.compare(nodeDescriptor1, nodeDescriptor2)
             : nodeDescriptor1.getIndex() - nodeDescriptor2.getIndex();
    }
  };
  private DefaultMutableTreeNode myRootNode;
  private final HashMap<Object, Object> myElementToNodeMap = new HashMap<Object, Object>();
  private final HashSet<DefaultMutableTreeNode> myUnbuiltNodes = new HashSet<DefaultMutableTreeNode>();
  private TreeExpansionListener myExpansionListener;
  private MySelectionListener mySelectionListener;

  private WorkerThread myWorker = null;
  private final ArrayList<Runnable> myActiveWorkerTasks = new ArrayList<Runnable>();

  private ProgressIndicator myProgress;
  private static final int WAIT_CURSOR_DELAY = 100;
  private AbstractTreeNode<Object> TREE_NODE_WRAPPER;
  private boolean myRootNodeWasInitialized = false;
  private final Map<Object, List<NodeAction>> myNodeActions = new HashMap<Object, List<NodeAction>>();
  private boolean myUpdateFromRootRequested;
  private boolean myWasEverShown;
  private boolean myUpdateIfInactive;
  private final List<Object> myLoadingParents = new ArrayList<Object>();
  private long myClearOnHideDelay = -1;
  private ScheduledExecutorService ourClearanceService;
  private final Map<AbstractTreeUi, Long> ourUi2Countdown = Collections.synchronizedMap(new WeakHashMap<AbstractTreeUi, Long>());

  private final List<Runnable> myDeferredSelections = new ArrayList<Runnable>();
  private final List<Runnable> myDeferredExpansions = new ArrayList<Runnable>();

  private boolean myCanProcessDeferredSelections;

  private UpdaterTreeState myUpdaterState;
  private AbstractTreeBuilder myBuilder;

  private final Set<DefaultMutableTreeNode> myUpdatingChildren = new HashSet<DefaultMutableTreeNode>();
  private long myJanitorPollPeriod = Time.SECOND * 10;
  private boolean myCheckStructure = false;


  private boolean myCanYield = false;

  private final List<TreeUpdatePass> myYeildingPasses = new ArrayList<TreeUpdatePass>();

  private boolean myYeildingNow;

  private final List<DefaultMutableTreeNode> myPendingNodeActions = new ArrayList<DefaultMutableTreeNode>();
  private final List<Runnable> myYeildingDoneRunnables = new ArrayList<Runnable>();

  private final Alarm myBusyAlarm = new Alarm();
  private final Runnable myWaiterForReady = new Runnable() {
    public void run() {
      maybeSetBusyAndScheduleWaiterForReady(false);
    }
  };

  private final RegistryValue myYeildingUpdate = Registry.get("ide.tree.yeildingUiUpdate");
  private final RegistryValue myShowBusyIndicator = Registry.get("ide.tree.showBusyIndicator");
  private final RegistryValue myWaitForReadyTime = Registry.get("ide.tree.waitForReadyTimout");

  private boolean myWasEverIndexNotReady;
  private boolean myShowing;
  private final FocusAdapter myFocusListener = new FocusAdapter() {
    @Override
    public void focusGained(FocusEvent e) {
      maybeReady();
    }
  };

  protected final void init(AbstractTreeBuilder builder,
                            JTree tree,
                            DefaultTreeModel treeModel,
                            AbstractTreeStructure treeStructure,
                            @Nullable Comparator<NodeDescriptor> comparator) {

    init(builder, tree, treeModel, treeStructure, comparator, true);
  }

  protected void init(AbstractTreeBuilder builder,
                      JTree tree,
                      DefaultTreeModel treeModel,
                      AbstractTreeStructure treeStructure,
                      @Nullable Comparator<NodeDescriptor> comparator,
                      boolean updateIfInactive) {
    myBuilder = builder;
    myTree = tree;
    myTreeModel = treeModel;
    TREE_NODE_WRAPPER = getBuilder().createSearchingTreeNodeWrapper();
    myTree.setModel(myTreeModel);
    setRootNode((DefaultMutableTreeNode)treeModel.getRoot());
    setTreeStructure(treeStructure);
    myNodeDescriptorComparator = comparator;
    myUpdateIfInactive = updateIfInactive;

    myExpansionListener = new MyExpansionListener();
    myTree.addTreeExpansionListener(myExpansionListener);

    mySelectionListener = new MySelectionListener();
    myTree.addTreeSelectionListener(mySelectionListener);

    myTree.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        super.componentResized(e);
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        super.componentMoved(e);
      }

      @Override
      public void componentShown(ComponentEvent e) {
        super.componentShown(e);
      }

      @Override
      public void componentHidden(ComponentEvent e) {
        super.componentHidden(e);
      }
    });

    setUpdater(getBuilder().createUpdater());
    myProgress = getBuilder().createProgressIndicator();
    Disposer.register(getBuilder(), getUpdater());

    final UiNotifyConnector uiNotify = new UiNotifyConnector(tree, new Activatable() {
      public void showNotify() {
        if (!isReleased()) {
          AbstractTreeUi.this.showNotify();
        }
      }

      public void hideNotify() {
        if (!isReleased()) {
          AbstractTreeUi.this.hideNotify();
        }
      }
    });
    Disposer.register(getBuilder(), uiNotify);

    myTree.addFocusListener(myFocusListener);
  }

  protected void hideNotify() {
    myShowing = false;

    getUpdater().hideNotify();

    myBusyAlarm.cancelAllRequests();

    if (!myWasEverShown) return;

    if (!myNodeActions.isEmpty()) {
      cancelBackgroundLoading();
      myUpdateFromRootRequested = true;
    }

    if (getClearOnHideDelay() >= 0) {
      ourUi2Countdown.put(this, System.currentTimeMillis() + getClearOnHideDelay());
      initClearanceServiceIfNeeded();
    }
  }

  private void maybeSetBusyAndScheduleWaiterForReady(boolean forcedBusy) {
    if (!myShowBusyIndicator.asBoolean() || !canYield()) return;

    if (myTree instanceof Tree) {
      final Tree tree = (Tree)myTree;
      final boolean isBusy = !isReady() || forcedBusy;
      if (isBusy && tree.isShowing()) {
        tree.setPaintBusy(true);
        myBusyAlarm.cancelAllRequests();
        myBusyAlarm.addRequest(myWaiterForReady, myWaitForReadyTime.asInteger());
      }
      else {
        tree.setPaintBusy(false);
      }
    }
  }

  private void initClearanceServiceIfNeeded() {
    if (ourClearanceService != null) return;

    ourClearanceService = ConcurrencyUtil.newSingleScheduledThreadExecutor("AbstractTreeBuilder's janitor");
    ourClearanceService.scheduleWithFixedDelay(new Runnable() {
      public void run() {
        cleanUpAll();
      }
    }, myJanitorPollPeriod, myJanitorPollPeriod, TimeUnit.MILLISECONDS);
  }

  private void cleanUpAll() {
    final long now = System.currentTimeMillis();
    final AbstractTreeUi[] uis = ourUi2Countdown.keySet().toArray(new AbstractTreeUi[ourUi2Countdown.size()]);
    for (AbstractTreeUi eachUi : uis) {
      if (eachUi == null) continue;
      final Long timeToCleanup = ourUi2Countdown.get(eachUi);
      if (timeToCleanup == null) continue;
      if (now >= timeToCleanup.longValue()) {
        ourUi2Countdown.remove(eachUi);
        getBuilder().cleanUp();
      }
    }
  }

  protected void doCleanUp() {
    final Application app = ApplicationManager.getApplication();
    if (app != null && app.isUnitTestMode()) {
      cleanUpNow();
    }
    else {
      // we are not in EDT
      //noinspection SSBasedInspection
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (!isReleased()) {
            cleanUpNow();
          }
        }
      });
    }
  }

  private void disposeClearanceService() {
    try {
      if (ourClearanceService != null) {
        ourClearanceService.shutdown();
        ourClearanceService = null;
      }
    }
    catch (AccessControlException e) {
      LOG.warn(e);
    }
  }

  void showNotify() {
    myShowing = true;

    myCanProcessDeferredSelections = true;

    ourUi2Countdown.remove(this);

    if (!myWasEverShown || myUpdateFromRootRequested) {
      if (wasRootNodeInitialized()) {
        getBuilder().updateFromRoot();
      }
      else {
        initRootNodeNowIfNeeded(new TreeUpdatePass(getRootNode()));
        getBuilder().updateFromRoot();
      }
    }
    myWasEverShown = true;

    getUpdater().showNotify();
  }

  public void release() {
    if (isReleased()) return;

    myTree.removeTreeExpansionListener(myExpansionListener);
    myTree.removeTreeSelectionListener(mySelectionListener);
    myTree.removeFocusListener(myFocusListener);

    disposeNode(getRootNode());
    myElementToNodeMap.clear();
    getUpdater().cancelAllRequests();
    if (myWorker != null) {
      myWorker.dispose(true);
      clearWorkerTasks();
    }
    TREE_NODE_WRAPPER.setValue(null);
    if (myProgress != null) {
      myProgress.cancel();
    }
    disposeClearanceService();

    myTree = null;
    setUpdater(null);
    myWorker = null;
//todo [kirillk] afraid to do so just in release day, to uncomment
//    myTreeStructure = null;
    myBuilder = null;

    myNodeActions.clear();
    myPendingNodeActions.clear();
    myDeferredSelections.clear();
    myDeferredExpansions.clear();
    myYeildingDoneRunnables.clear();
  }

  public boolean isReleased() {
    return myBuilder == null;
  }

  protected void doExpandNodeChildren(final DefaultMutableTreeNode node) {
    getTreeStructure().commit();
    addNodeAction(getElementFor(node), new NodeAction() {
      public void onReady(final DefaultMutableTreeNode node) {
        processSmartExpand(node);
      }
    });
    getUpdater().addSubtreeToUpdate(node);
    getUpdater().performUpdate();
  }

  public final AbstractTreeStructure getTreeStructure() {
    return myTreeStructure;
  }

  public final JTree getTree() {
    return myTree;
  }

  @Nullable
  public final DefaultMutableTreeNode getNodeForElement(Object element, final boolean validateAgainstStructure) {
    DefaultMutableTreeNode result = null;
    if (validateAgainstStructure) {
      int index = 0;
      while (true) {
        final DefaultMutableTreeNode node = findNode(element, index);
        if (node == null) break;

        if (isNodeValidForElement(element, node)) {
          result = node;
          break;
        }

        index++;
      }
    }
    else {
      result = getFirstNode(element);
    }


    if (result != null && !isNodeInStructure(result)) {
      disposeNode(result);
      result = null;
    }

    return result;
  }

  private boolean isNodeInStructure(DefaultMutableTreeNode node) {
    return TreeUtil.isAncestor(getRootNode(), node) && getRootNode() == myTreeModel.getRoot();
  }

  private boolean isNodeValidForElement(final Object element, final DefaultMutableTreeNode node) {
    return isSameHierarchy(element, node) || isValidChildOfParent(element, node);
  }

  private boolean isValidChildOfParent(final Object element, final DefaultMutableTreeNode node) {
    final DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
    final Object parentElement = getElementFor(parent);
    if (!isInStructure(parentElement)) return false;

    if (parent instanceof ElementNode) {
      return ((ElementNode)parent).isValidChild(element);
    }
    for (int i = 0; i < parent.getChildCount(); i++) {
      final TreeNode child = parent.getChildAt(i);
      final Object eachElement = getElementFor(child);
      if (element.equals(eachElement)) return true;
    }

    return false;
  }

  private boolean isSameHierarchy(Object eachParent, DefaultMutableTreeNode eachParentNode) {
    boolean valid;
    while (true) {
      if (eachParent == null) {
        valid = eachParentNode == null;
        break;
      }

      if (!eachParent.equals(getElementFor(eachParentNode))) {
        valid = false;
        break;
      }

      eachParent = getTreeStructure().getParentElement(eachParent);
      eachParentNode = (DefaultMutableTreeNode)eachParentNode.getParent();
    }
    return valid;
  }

  public final DefaultMutableTreeNode getNodeForPath(Object[] path) {
    DefaultMutableTreeNode node = null;
    for (final Object pathElement : path) {
      node = node == null ? getFirstNode(pathElement) : findNodeForChildElement(node, pathElement);
      if (node == null) {
        break;
      }
    }
    return node;
  }

  public final void buildNodeForElement(Object element) {
    getUpdater().performUpdate();
    DefaultMutableTreeNode node = getNodeForElement(element, false);
    if (node == null) {
      final java.util.List<Object> elements = new ArrayList<Object>();
      while (true) {
        element = getTreeStructure().getParentElement(element);
        if (element == null) {
          break;
        }
        elements.add(0, element);
      }

      for (final Object element1 : elements) {
        node = getNodeForElement(element1, false);
        if (node != null) {
          expand(node);
        }
      }
    }
  }

  public final void buildNodeForPath(Object[] path) {
    getUpdater().performUpdate();
    DefaultMutableTreeNode node = null;
    for (final Object pathElement : path) {
      node = node == null ? getFirstNode(pathElement) : findNodeForChildElement(node, pathElement);
      if (node != null && node != path[path.length - 1]) {
        expand(node);
      }
    }
  }

  public final void setNodeDescriptorComparator(Comparator<NodeDescriptor> nodeDescriptorComparator) {
    myNodeDescriptorComparator = nodeDescriptorComparator;
    List<Object> pathsToExpand = new ArrayList<Object>();
    List<Object> selectionPaths = new ArrayList<Object>();
    TreeBuilderUtil.storePaths(getBuilder(), getRootNode(), pathsToExpand, selectionPaths, false);
    resortChildren(getRootNode());
    myTreeModel.nodeStructureChanged(getRootNode());
    TreeBuilderUtil.restorePaths(getBuilder(), pathsToExpand, selectionPaths, false);
  }

  protected AbstractTreeBuilder getBuilder() {
    return myBuilder;
  }

  private void resortChildren(DefaultMutableTreeNode node) {
    ArrayList<TreeNode> childNodes = TreeUtil.childrenToArray(node);
    node.removeAllChildren();
    Collections.sort(childNodes, myNodeComparator);
    for (TreeNode childNode1 : childNodes) {
      DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)childNode1;
      node.add(childNode);
      resortChildren(childNode);
    }
  }

  protected final void initRootNode() {
    final Activatable activatable = new Activatable() {
      public void showNotify() {
        if (!myRootNodeWasInitialized) {
          initRootNodeNowIfNeeded(new TreeUpdatePass(getRootNode()));
        }
      }

      public void hideNotify() {
      }
    };

    if (myUpdateIfInactive || ApplicationManager.getApplication().isUnitTestMode()) {
      activatable.showNotify();
    }
    else {
      new UiNotifyConnector.Once(myTree, activatable);
    }
  }

  private void initRootNodeNowIfNeeded(TreeUpdatePass pass) {
    if (myRootNodeWasInitialized) return;

    myRootNodeWasInitialized = true;
    Object rootElement = getTreeStructure().getRootElement();
    addNodeAction(rootElement, new NodeAction() {
      public void onReady(final DefaultMutableTreeNode node) {
        processDeferredActions();
      }
    });
    NodeDescriptor nodeDescriptor = getTreeStructure().createDescriptor(rootElement, null);
    getRootNode().setUserObject(nodeDescriptor);
    update(nodeDescriptor, false);
    if (getElementFromDescriptor(nodeDescriptor) != null) {
      createMapping(getElementFromDescriptor(nodeDescriptor), getRootNode());
    }
    addLoadingNode(getRootNode());
    boolean willUpdate = false;
    if (getBuilder().isAutoExpandNode(nodeDescriptor)) {
      willUpdate = myUnbuiltNodes.contains(getRootNode());
      expand(getRootNode());
    }
    if (!willUpdate) {
      updateNodeChildren(getRootNode(), pass, null, false);
    }
    if (getRootNode().getChildCount() == 0) {
      myTreeModel.nodeChanged(getRootNode());
    }

    if (!isLoadedInBackground(getTreeStructure().getRootElement())) {
      processDeferredActions();
    }
  }

  private boolean update(final NodeDescriptor nodeDescriptor, boolean canBeNonEdt) {
    if (!canBeNonEdt && myWasEverShown) {
      assertIsDispatchThread();
    }

    if (isEdt() || !myWasEverShown) {
      return getBuilder().updateNodeDescriptor(nodeDescriptor);
    }
    else {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        public void run() {
          if (!isReleased()) {
            getBuilder().updateNodeDescriptor(nodeDescriptor);
          }
        }
      });
      return true;
    }
  }

  private void assertIsDispatchThread() {
    if (isTreeShowing() && !isEdt()) {
      LOG.error("Must be in event-dispatch thread");
    }
  }

  private static boolean isEdt() {
    return SwingUtilities.isEventDispatchThread();
  }

  private boolean isTreeShowing() {
    return myShowing;
  }

  private static void assertNotDispatchThread() {
    if (isEdt()) {
      LOG.error("Must not be in event-dispatch thread");
    }
  }

  private void processDeferredActions() {
    processDeferredActions(myDeferredSelections);
    processDeferredActions(myDeferredExpansions);
  }

  private static void processDeferredActions(List<Runnable> actions) {
    final Runnable[] runnables = actions.toArray(new Runnable[actions.size()]);
    actions.clear();
    for (Runnable runnable : runnables) {
      runnable.run();
    }
  }

  public void doUpdateFromRoot() {
    updateSubtree(getRootNode());
  }

  public ActionCallback doUpdateFromRootCB() {
    final ActionCallback cb = new ActionCallback();
    getUpdater().runAfterUpdate(new Runnable() {
      public void run() {
        cb.setDone();
      }
    });
    updateSubtree(getRootNode());
    return cb;
  }

  public final void updateSubtree(DefaultMutableTreeNode node) {
    updateSubtree(new TreeUpdatePass(node));
  }

  public final void updateSubtree(TreeUpdatePass pass) {
    if (getUpdater() != null) {
      getUpdater().addSubtreeToUpdate(pass);
    } else {
      updateSubtreeNow(pass);
    }
  }

  final void updateSubtreeNow(TreeUpdatePass pass) {
    maybeSetBusyAndScheduleWaiterForReady(true);

    initRootNodeNowIfNeeded(pass);

    final DefaultMutableTreeNode node = pass.getNode();

    if (!(node.getUserObject() instanceof NodeDescriptor)) return;

    setUpdaterState(new UpdaterTreeState(this)).beforeSubtreeUpdate();

    final NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();

    if (!istToBuildInBackground(descriptor)) {
      getBuilder().updateNode(node);
    }

    updateNodeChildren(node, pass, null, false);
  }

  private boolean istToBuildInBackground(NodeDescriptor descriptor) {
    return getTreeStructure().isToBuildChildrenInBackground(getBuilder().getTreeStructureElement(descriptor));
  }

  @NotNull
  UpdaterTreeState setUpdaterState(UpdaterTreeState state) {
    final UpdaterTreeState oldState = myUpdaterState;
    if (oldState == null) {
      myUpdaterState = state;
      return state;
    }
    else {
      oldState.addAll(state);
      return oldState;
    }
  }

  protected void doUpdateNode(DefaultMutableTreeNode node) {
    if (!(node.getUserObject() instanceof NodeDescriptor)) return;
    NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();
    Object prevElement = getElementFromDescriptor(descriptor);
    if (prevElement == null) return;
    boolean changes = update(descriptor, false);
    if (!isValid(descriptor)) {
      if (isInStructure(prevElement)) {
        getUpdater().addSubtreeToUpdateByElement(getTreeStructure().getParentElement(prevElement));
        return;
      }
    }
    if (changes) {
      updateNodeImageAndPosition(node);
    }
  }

  public Object getElementFromDescriptor(NodeDescriptor descriptor) {
    return getBuilder().getTreeStructureElement(descriptor);
  }

  private void updateNodeChildren(final DefaultMutableTreeNode node,
                                  final TreeUpdatePass pass,
                                  @Nullable Object[] preloadedChildren,
                                  boolean forcedNow) {
    getTreeStructure().commit();
    final boolean wasExpanded = myTree.isExpanded(new TreePath(node.getPath()));
    final boolean wasLeaf = node.getChildCount() == 0;

    final NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();

    if (descriptor == null) return;

    if (myUnbuiltNodes.contains(node)) {
      processUnbuilt(node, descriptor, pass);
      processNodeActionsIfReady(node);
      return;
    }

    if (!forcedNow && istToBuildInBackground(descriptor)) {
      queueBackgroundUpdate(node, descriptor, pass);
      return;
    }

    final MutualMap<Object, Integer> elementToIndexMap = collectElementToIndexMap(descriptor, preloadedChildren);

    myUpdatingChildren.add(node);
    pass.setCurrentNode(node);
    processAllChildren(node, elementToIndexMap, pass).doWhenDone(new Runnable() {
      public void run() {
        if (isDisposed(node)) {
          return;
        }

        if (canYield()) {
          removeLoading(node, false);
        }

        ArrayList<TreeNode> nodesToInsert = collectNodesToInsert(descriptor, elementToIndexMap);

        insertNodesInto(nodesToInsert, node);

        updateNodesToInsert(nodesToInsert, pass);

        if (wasExpanded) {
          expand(node);
        }

        if (wasExpanded || wasLeaf) {
          expand(node, descriptor, wasLeaf);
        }

        myUpdatingChildren.remove(node);

        final Object element = getElementFor(node);
        addNodeAction(element, new NodeAction() {
          public void onReady(final DefaultMutableTreeNode node) {
            removeLoading(node, false);
          }
        });

        processNodeActionsIfReady(node);
      }
    });
  }

  private boolean isDisposed(DefaultMutableTreeNode node) {
    return !node.isNodeAncestor((DefaultMutableTreeNode)myTree.getModel().getRoot());
  }

  private void expand(DefaultMutableTreeNode node) {
    expand(new TreePath(node.getPath()));
  }

  private void expand(final TreePath path) {
    if (path == null) return;
    final Object last = path.getLastPathComponent();
    boolean isLeaf = myTree.getModel().isLeaf(path.getLastPathComponent());
    final boolean isRoot = last == myTree.getModel().getRoot();
    final TreePath parent = path.getParentPath();
    if (isRoot && !myTree.isExpanded(path)) {
      insertLoadingNode((DefaultMutableTreeNode)last, true);
      expandPath(path);
    } else if (myTree.isExpanded(path) || isLeaf && parent != null && myTree.isExpanded(parent) && !myUnbuiltNodes.contains(last)) {
      if (last instanceof DefaultMutableTreeNode) {
        processNodeActionsIfReady((DefaultMutableTreeNode)last);
      }
    }
    else {
      if (isLeaf && myUnbuiltNodes.contains(last)) {
        insertLoadingNode((DefaultMutableTreeNode)last, true);
        expandPath(path);
      } else if (isLeaf && parent != null) {
        final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parent.getLastPathComponent();
        if (parentNode != null) {
          addToUnbuilt(parentNode);
        }
        expandPath(parent);
      }
      else {
        expandPath(path);
      }
    }
  }

  private void addToUnbuilt(DefaultMutableTreeNode node) {
    myUnbuiltNodes.add(node);
  }

  private void removeFromUnbuilt(DefaultMutableTreeNode node) {
    myUnbuiltNodes.remove(node);
  }

  private boolean processUnbuilt(final DefaultMutableTreeNode node, final NodeDescriptor descriptor, final TreeUpdatePass pass) {
    if (getBuilder().isAlwaysShowPlus(descriptor)) return false; // check for isAlwaysShowPlus is important for e.g. changing Show Members state!

    final Object element = getBuilder().getTreeStructureElement(descriptor);

    if (getTreeStructure().isToBuildChildrenInBackground(element)) return false; //?

    final Object[] children = getChildrenFor(element);
    if (children.length == 0) {
      removeLoading(node, false);
    }
    else if (getBuilder().isAutoExpandNode((NodeDescriptor)node.getUserObject())) {
      addNodeAction(getElementFor(node), new NodeAction() {
        public void onReady(final DefaultMutableTreeNode node) {
          final TreePath path = new TreePath(node.getPath());
          if (getTree().isExpanded(path) || children.length == 0) {
            removeLoading(node, false);
          }
          else {
            maybeYeild(new ActiveRunnable() {
              public ActionCallback run() {
                expand(element, null);
                return new ActionCallback.Done();
              }
            }, pass, node);
          }
        }
      });
    }

    return true;
  }

  private boolean removeIfLoading(TreeNode node) {
    if (isLoadingNode(node)) {
      removeNodeFromParent((MutableTreeNode)node, false);
      return true;
    }

    return false;
  }

  //todo [kirillk] temporary consistency check
  private Object[] getChildrenFor(final Object element) {
    final Object[] passOne;
    try {
      passOne = getTreeStructure().getChildElements(element);
    }
    catch (IndexNotReadyException e) {
      if (!myWasEverIndexNotReady) {
        myWasEverIndexNotReady = true;
        LOG.warn("Tree is not dumb-mode-aware; treeBuilder=" + getBuilder() + " treeStructure=" + getTreeStructure());
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    if (!myCheckStructure) return passOne;

    final Object[] passTwo = getTreeStructure().getChildElements(element);

    final Set<Object> two = new HashSet<Object>(Arrays.asList(passTwo));

    if (passOne.length != passTwo.length) {
      LOG.error(
        "AbstractTreeStructure.getChildren() must either provide same objects or new objects but with correct hashCode() and equals() methods. Wrong parent element=" +
        element);
    }
    else {
      for (Object eachInOne : passOne) {
        if (!two.contains(eachInOne)) {
          LOG.error(
            "AbstractTreeStructure.getChildren() must either provide same objects or new objects but with correct hashCode() and equals() methods. Wrong parent element=" +
            element);
          break;
        }
      }
    }

    return passOne;
  }

  private void updateNodesToInsert(final ArrayList<TreeNode> nodesToInsert, TreeUpdatePass pass) {
    for (TreeNode aNodesToInsert : nodesToInsert) {
      DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)aNodesToInsert;
      addLoadingNode(childNode);
      updateNodeChildren(childNode, pass, null, false);
    }
  }

  private ActionCallback processAllChildren(final DefaultMutableTreeNode node,
                                            final MutualMap<Object, Integer> elementToIndexMap,
                                            final TreeUpdatePass pass) {

    final ArrayList<TreeNode> childNodes = TreeUtil.childrenToArray(node);
    return maybeYeild(new ActiveRunnable() {
      public ActionCallback run() {
        return processAllChildren(node, elementToIndexMap, pass, childNodes);
      }
    }, pass, node);
  }

  private boolean isRerunNeeded(TreeUpdatePass pass) {
    if (pass.isExpired()) return false;

    final boolean rerunBecauseTreeIsHidden = !pass.isExpired() && !isTreeShowing() && getUpdater().isInPostponeMode();

    return rerunBecauseTreeIsHidden || getUpdater().isRerunNeededFor(pass);
  }

  private ActionCallback maybeYeild(final ActiveRunnable processRunnable, final TreeUpdatePass pass, final DefaultMutableTreeNode node) {
    final ActionCallback result = new ActionCallback();

    if (isRerunNeeded(pass)) {
      getUpdater().addSubtreeToUpdate(pass);
      result.setRejected();
    } else {
      if (isToYieldUpdateFor(node)) {
        pass.setCurrentNode(node);
        yieldAndRun(new Runnable() {
          public void run() {
            if (pass.isExpired()) return;

            if (isRerunNeeded(pass)) {
              runDone(new Runnable() {
                public void run() {
                  if (!pass.isExpired()) {
                    getUpdater().addSubtreeToUpdate(pass);
                  }
                }
              });
              result.setRejected();
            }
            else {
              processRunnable.run().notify(result);
            }
          }
        }, pass);
      }
      else {
        processRunnable.run().notify(result);
      }
    }

    return result;
  }

  private void yieldAndRun(final Runnable runnable, final TreeUpdatePass pass) {
    myYeildingPasses.add(pass);
    myYeildingNow = true;
    yield(new Runnable() {
      public void run() {
        if (isReleased()) {
          return;
        }

        runOnYieldingDone(new Runnable() {
          public void run() {
            if (isReleased()) {
              return;
            }
            executeYieldingRequest(runnable, pass);
          }
        });
      }
    });
  }

  public boolean isYeildingNow() {
    return myYeildingNow;
  }

  private boolean hasSheduledUpdates() {
    return getUpdater().hasNodesToUpdate() || isLoadingInBackground();
  }

  private boolean hasExpandedUnbuiltNodes() {
    for (DefaultMutableTreeNode each : myUnbuiltNodes) {
      if (myTree.isExpanded(new TreePath(each.getPath()))) return true;
    }

    return false;
  }

  public boolean isReady() {
    return !isYeildingNow() && !isWorkerBusy() && (!hasSheduledUpdates() || getUpdater().isInPostponeMode()) && !hasExpandedUnbuiltNodes();
  }

  private void executeYieldingRequest(Runnable runnable, TreeUpdatePass pass) {
    try {
      myYeildingPasses.remove(pass);
      runnable.run();
    }
    finally {
      maybeYeildingFinished();
    }
  }

  private void maybeYeildingFinished() {
    if (myYeildingPasses.isEmpty()) {
      myYeildingNow = false;
      flushPendingNodeActions();
    }
  }

  void maybeReady() {
    if (!isReleased() &&
        isReady() &&
        myTree.isShowing() &&
        getBuilder().isToEnsureSelectionOnFocusGained() &&
        Registry.is("ide.tree.ensureSelectionOnFocusGained")) {
      TreeUtil.ensureSelection(myTree);
    }
  }

  private void flushPendingNodeActions() {
    final DefaultMutableTreeNode[] nodes = myPendingNodeActions.toArray(new DefaultMutableTreeNode[myPendingNodeActions.size()]);
    myPendingNodeActions.clear();

    for (DefaultMutableTreeNode each : nodes) {
      processNodeActionsIfReady(each);
    }

    final Runnable[] actions = myYeildingDoneRunnables.toArray(new Runnable[myYeildingDoneRunnables.size()]);
    for (Runnable each : actions) {
      if (!isYeildingNow()) {
        myYeildingDoneRunnables.remove(each);
        each.run();
      }
    }

    maybeReady();
  }

  protected void runOnYieldingDone(Runnable onDone) {
    getBuilder().runOnYeildingDone(onDone);
  }

  protected void yield(Runnable runnable) {
    getBuilder().yield(runnable);
  }

  private ActionCallback processAllChildren(final DefaultMutableTreeNode node,
                                            final MutualMap<Object, Integer> elementToIndexMap,
                                            final TreeUpdatePass pass,
                                            final ArrayList<TreeNode> childNodes) {


    if (pass.isExpired()) return new ActionCallback.Rejected();

    if (childNodes.isEmpty()) return new ActionCallback.Done();


    final ActionCallback result = new ActionCallback(childNodes.size());

    for (TreeNode childNode1 : childNodes) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)childNode1;
      if (isLoadingNode(childNode)) {
        result.setDone();
        continue;
      }

      maybeYeild(new ActiveRunnable() {
        @Override
        public ActionCallback run() {
          return processChildNode(childNode, (NodeDescriptor)childNode.getUserObject(), node, elementToIndexMap, pass);
        }
      }, pass, node).notify(result);

      if (result.isRejected()) break;
    }

    return result;
  }

  private boolean isToYieldUpdateFor(final DefaultMutableTreeNode node) {
    return canYield() && getBuilder().isToYieldUpdateFor(node);
  }

  private MutualMap<Object, Integer> collectElementToIndexMap(final NodeDescriptor descriptor, @Nullable Object[] preloadedChildren) {
    MutualMap<Object, Integer> elementToIndexMap = new MutualMap<Object, Integer>(true);
    Object[] children = preloadedChildren != null ? preloadedChildren : getChildrenFor(getBuilder().getTreeStructureElement(descriptor));
    int index = 0;
    for (Object child : children) {
      if (!isValid(child)) continue;
      elementToIndexMap.put(child, Integer.valueOf(index));
      index++;
    }
    return elementToIndexMap;
  }

  private void expand(final DefaultMutableTreeNode node, final NodeDescriptor descriptor, final boolean wasLeaf) {
    final Alarm alarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
    alarm.addRequest(new Runnable() {
      public void run() {
        myTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
    }, WAIT_CURSOR_DELAY);

    if (wasLeaf && getBuilder().isAutoExpandNode(descriptor)) {
      expand(node);
    }

    ArrayList<TreeNode> nodes = TreeUtil.childrenToArray(node);
    for (TreeNode node1 : nodes) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node1;
      if (isLoadingNode(childNode)) continue;
      NodeDescriptor childDescr = (NodeDescriptor)childNode.getUserObject();
      if (getBuilder().isAutoExpandNode(childDescr)) {
        addNodeAction(getElementFor(childNode), new NodeAction() {
          public void onReady(DefaultMutableTreeNode node) {
            expand(childNode);
          }
        });
        addSubtreeToUpdate(childNode);
      }
    }

    int n = alarm.cancelAllRequests();
    if (n == 0) {
      myTree.setCursor(Cursor.getDefaultCursor());
    }
  }

  public static boolean isLoadingNode(final Object node) {
    return node instanceof LoadingNode;
  }

  private ArrayList<TreeNode> collectNodesToInsert(final NodeDescriptor descriptor, final MutualMap<Object, Integer> elementToIndexMap) {
    ArrayList<TreeNode> nodesToInsert = new ArrayList<TreeNode>();
    final Collection<Object> allElements = elementToIndexMap.getKeys();
    for (Object child : allElements) {
      Integer index = elementToIndexMap.getValue(child);
      final NodeDescriptor childDescr = getTreeStructure().createDescriptor(child, descriptor);
      //noinspection ConstantConditions
      if (childDescr == null) {
        LOG.error("childDescr == null, treeStructure = " + getTreeStructure() + ", child = " + child);
        continue;
      }
      childDescr.setIndex(index.intValue());
      update(childDescr, false);
      if (getElementFromDescriptor(childDescr) == null) {
        LOG.error("childDescr.getElement() == null, child = " + child + ", builder = " + this);
        continue;
      }
      final DefaultMutableTreeNode childNode = createChildNode(childDescr);
      nodesToInsert.add(childNode);
      createMapping(getElementFromDescriptor(childDescr), childNode);
    }

    return nodesToInsert;
  }

  protected DefaultMutableTreeNode createChildNode(final NodeDescriptor descriptor) {
    return new ElementNode(this, descriptor);
  }

  protected boolean canYield() {
    return myCanYield && myYeildingUpdate.asBoolean();
  }

  public long getClearOnHideDelay() {
    return myClearOnHideDelay > 0 ? myClearOnHideDelay : Registry.intValue("ide.tree.clearOnHideTime");
  }

  static class ElementNode extends DefaultMutableTreeNode {

    Set<Object> myElements = new HashSet<Object>();
    AbstractTreeUi myUi;


    ElementNode(AbstractTreeUi ui, NodeDescriptor descriptor) {
      super(descriptor);
      myUi = ui;
    }

    @Override
    public void insert(final MutableTreeNode newChild, final int childIndex) {
      super.insert(newChild, childIndex);
      final Object element = myUi.getElementFor(newChild);
      if (element != null) {
        myElements.add(element);
      }
    }

    @Override
    public void remove(final int childIndex) {
      final TreeNode node = getChildAt(childIndex);
      super.remove(childIndex);
      final Object element = myUi.getElementFor(node);
      if (element != null) {
        myElements.remove(element);
      }
    }

    boolean isValidChild(Object childElement) {
      return myElements.contains(childElement);
    }

    @Override
    public String toString() {
      return String.valueOf(getUserObject());
    }
  }

  private boolean isUpdatingParent(DefaultMutableTreeNode kid) {
    DefaultMutableTreeNode eachParent = kid;
    while (eachParent != null) {
      if (myUpdatingChildren.contains(eachParent)) return true;
      eachParent = (DefaultMutableTreeNode)eachParent.getParent();
    }

    return false;
  }

  private boolean isLoadedInBackground(Object element) {
    synchronized (myLoadingParents) {
      return myLoadingParents.contains(element);
    }
  }

  private void addToLoadedInBackground(Object element) {
    synchronized (myLoadingParents) {
      myLoadingParents.add(element);
    }
  }

  private void removeFromLoadedInBackground(Object element) {
    synchronized (myLoadingParents) {
      myLoadingParents.remove(element);
    }
  }

  private boolean isLoadingInBackground() {
    synchronized (myLoadingParents) {
      return !myLoadingParents.isEmpty();
    }
  }

  private boolean queueBackgroundUpdate(final DefaultMutableTreeNode node, final NodeDescriptor descriptor, final TreeUpdatePass pass) {
    assertIsDispatchThread();

    final Object oldElementFromDescriptor = getElementFromDescriptor(descriptor);

    if (isLoadedInBackground(oldElementFromDescriptor)) return false;

    addToLoadedInBackground(oldElementFromDescriptor);

    if (!isNodeBeingBuilt(node)) {
      LoadingNode loadingNode = new LoadingNode(getLoadingNodeText());
      myTreeModel.insertNodeInto(loadingNode, node, node.getChildCount());
    }

    final Ref<Object[]> children = new Ref<Object[]>();
    final Ref<Object> elementFromDescriptor = new Ref<Object>();
    Runnable buildRunnable = new Runnable() {
      public void run() {
        if (isReleased()) {
          return;
        }

        update(descriptor, true);
        Object element = getElementFromDescriptor(descriptor);
        if (element == null) {
          removeFromLoadedInBackground(oldElementFromDescriptor);
          return;
        }

        elementFromDescriptor.set(element);
        children.set(getChildrenFor(getBuilder().getTreeStructureElement(descriptor))); // load children
      }
    };

    final DefaultMutableTreeNode[] nodeToProcessActions = new DefaultMutableTreeNode[1];
    Runnable updateRunnable = new Runnable() {
      public void run() {
        if (isReleased()) return;
        if (children.get() == null) return;

        if (isRerunNeeded(pass)) {
          removeFromLoadedInBackground(elementFromDescriptor.get());
          getUpdater().addSubtreeToUpdate(pass);
          return;
        }

        removeFromLoadedInBackground(elementFromDescriptor.get());
        updateNodeChildren(node, pass, children.get(), true);

        if (isRerunNeeded(pass)) {
          getUpdater().addSubtreeToUpdate(pass);
          return;
        }

        Object element = elementFromDescriptor.get();

        if (element != null) {
          removeLoading(node, true);

          nodeToProcessActions[0] = node;
        }
      }
    };
    addTaskToWorker(buildRunnable, true, updateRunnable, new Runnable() {
      public void run() {
        if (nodeToProcessActions[0] != null) {
          processNodeActionsIfReady(nodeToProcessActions[0]);
        }
      }
    });
    return true;
  }

  private void removeLoading(DefaultMutableTreeNode parent, boolean removeFromUnbuilt) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      TreeNode child = parent.getChildAt(i);
      if (removeIfLoading(child)) {
        i--;
      }
    }

    if (removeFromUnbuilt) {
      removeFromUnbuilt(parent);
    }

    maybeReady();
  }

  private void processNodeActionsIfReady(final DefaultMutableTreeNode node) {
    if (isNodeBeingBuilt(node)) return;

    final Object o = node.getUserObject();
    if (!(o instanceof NodeDescriptor)) return;


    if (isYeildingNow()) {
      myPendingNodeActions.add(node);
      return;
    }

    final Object element = getBuilder().getTreeStructureElement((NodeDescriptor)o);

    final List<NodeAction> actions = myNodeActions.get(element);
    if (actions != null) {
      myNodeActions.remove(element);
      for (NodeAction each : actions) {
        each.onReady(node);
      }
    }

    if (!isUpdatingParent(node) && !isWorkerBusy()) {
      final UpdaterTreeState state = myUpdaterState;
      if (myNodeActions.isEmpty() && state != null && !state.isProcessingNow()) {
        if (!state.restore()) {
          setUpdaterState(state);
        }
      }
    }

    maybeReady();
  }


  private void processSmartExpand(final DefaultMutableTreeNode node) {
    if (getBuilder().isSmartExpand() && node.getChildCount() == 1) { // "smart" expand
      TreeNode childNode = node.getChildAt(0);
      if (isLoadingNode(childNode)) return;
      final TreePath childPath = new TreePath(node.getPath()).pathByAddingChild(childNode);
      expand(childPath);
    }
  }

  public static boolean isLoadingChildrenFor(final Object nodeObject) {
    if (!(nodeObject instanceof DefaultMutableTreeNode)) return false;

    DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObject;

    int loadingNodes = 0;
    for (int i = 0; i < Math.min(node.getChildCount(), 2); i++) {
      TreeNode child = node.getChildAt(i);
      if (isLoadingNode(child)) {
        loadingNodes++;
      }
    }
    return loadingNodes > 0 && loadingNodes == node.getChildCount();
  }

  private boolean isParentLoading(Object nodeObject) {
    if (!(nodeObject instanceof DefaultMutableTreeNode)) return false;

    DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObject;

    TreeNode eachParent = node.getParent();

    while (eachParent != null) {
      eachParent = eachParent.getParent();
      if (eachParent instanceof DefaultMutableTreeNode) {
        final Object eachElement = getElementFor((DefaultMutableTreeNode)eachParent);
        if (isLoadedInBackground(eachElement)) return true;
      }
    }

    return false;
  }

  protected String getLoadingNodeText() {
    return IdeBundle.message("progress.searching");
  }

  private ActionCallback processChildNode(final DefaultMutableTreeNode childNode,
                                          final NodeDescriptor childDescriptor,
                                          final DefaultMutableTreeNode parentNode,
                                          final MutualMap<Object, Integer> elementToIndexMap,
                                          TreeUpdatePass pass) {

    if (pass.isExpired()) {
      return new ActionCallback.Rejected();
    }

    NodeDescriptor childDesc = childDescriptor;


    if (childDesc == null) {
      pass.expire();
      return new ActionCallback.Rejected();
    }
    Object oldElement = getElementFromDescriptor(childDesc);
    if (oldElement == null) {
      pass.expire();
      return new ActionCallback.Rejected();
    }
    boolean changes = update(childDesc, false);
    boolean forceRemapping = false;
    Object newElement = getElementFromDescriptor(childDesc);

    Integer index = newElement != null ? elementToIndexMap.getValue(getBuilder().getTreeStructureElement(childDesc)) : null;
    if (index != null) {
      final Object elementFromMap = elementToIndexMap.getKey(index);
      if (elementFromMap != newElement && elementFromMap.equals(newElement)) {
        if (isInStructure(elementFromMap) && isInStructure(newElement)) {
          if (parentNode.getUserObject() instanceof NodeDescriptor) {
            final NodeDescriptor parentDescriptor = (NodeDescriptor)parentNode.getUserObject();
            childDesc = getTreeStructure().createDescriptor(elementFromMap, parentDescriptor);
            childNode.setUserObject(childDesc);
            newElement = elementFromMap;
            forceRemapping = true;
            update(childDesc, false);
            changes = true;
          }
        }
      }

      if (childDesc.getIndex() != index.intValue()) {
        changes = true;
      }
      childDesc.setIndex(index.intValue());
    }

    if (index != null && changes) {
      updateNodeImageAndPosition(childNode);
    }
    if (forceRemapping || !oldElement.equals(newElement)) {
      removeMapping(oldElement, childNode, newElement);
      if (newElement != null) {
        createMapping(newElement, childNode);
      }
    }

    if (index == null) {
      int selectedIndex = -1;
      if (TreeBuilderUtil.isNodeOrChildSelected(myTree, childNode)) {
        selectedIndex = parentNode.getIndex(childNode);
      }

      if (childNode.getParent() instanceof DefaultMutableTreeNode) {
        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode)childNode.getParent();
        if (myTree.isExpanded(new TreePath(parent.getPath()))) {
          if (parent.getChildCount() == 1 && parent.getChildAt(0) == childNode) {
            insertLoadingNode(parent, false);
          }
        }
      }

      Object disposedElement = getElementFor(childNode);

      removeNodeFromParent(childNode, selectedIndex >= 0);
      disposeNode(childNode);

      if (selectedIndex >= 0) {
        if (parentNode.getChildCount() > 0) {
          if (parentNode.getChildCount() > selectedIndex) {
            TreeNode newChildNode = parentNode.getChildAt(selectedIndex);
            if (isValidForSelectionAdjusting(newChildNode)) {
              addSelectionPath(new TreePath(myTreeModel.getPathToRoot(newChildNode)), true, getExpiredElementCondition(disposedElement));
            }
          }
          else {
            TreeNode newChild = parentNode.getChildAt(parentNode.getChildCount() - 1);
            if (isValidForSelectionAdjusting(newChild)) {
              addSelectionPath(new TreePath(myTreeModel.getPathToRoot(newChild)), true, getExpiredElementCondition(disposedElement));
            }
          }
        }
        else {
          addSelectionPath(new TreePath(myTreeModel.getPathToRoot(parentNode)), true, getExpiredElementCondition(disposedElement));
        }
      }
    }
    else {
      elementToIndexMap.remove(getBuilder().getTreeStructureElement(childDesc));
      updateNodeChildren(childNode, pass, null, false);
    }

    if (parentNode.equals(getRootNode())) {
      myTreeModel.nodeChanged(getRootNode());
    }

    return new ActionCallback.Done();
  }

  private boolean isValidForSelectionAdjusting(TreeNode node) {
    if (isLoadingNode(node)) return true;

    final Object elementInTree = getElementFor(node);
    if (elementInTree == null) return false;

    final TreeNode parentNode = node.getParent();
    final Object parentElementInTree = getElementFor(parentNode);
    if (parentElementInTree == null) return false;

    final Object parentElement = getTreeStructure().getParentElement(elementInTree);

    return parentElementInTree.equals(parentElement);
  }

  private Condition getExpiredElementCondition(final Object element) {
    return new Condition() {
      public boolean value(final Object o) {
        return isInStructure(element);
      }
    };
  }

  private void addSelectionPath(final TreePath path, final boolean isAdjustedSelection, final Condition isExpiredAdjustement) {
    doWithUpdaterState(new Runnable() {
      public void run() {
        TreePath toSelect = null;

        if (isLoadingNode(path.getLastPathComponent())) {
          final TreePath parentPath = path.getParentPath();
          if (parentPath != null) {
            toSelect = parentPath;
          }
        }
        else {
          toSelect = path;
        }

        if (toSelect != null) {
          myTree.addSelectionPath(toSelect);

          if (isAdjustedSelection && myUpdaterState != null) {
            final Object toSelectElement = getElementFor(toSelect.getLastPathComponent());
            myUpdaterState.addAdjustedSelection(toSelectElement, isExpiredAdjustement);
          }
        }
      }
    });
  }

  private static TreePath getPathFor(TreeNode node) {
    if (node instanceof DefaultMutableTreeNode) {
      return new TreePath(((DefaultMutableTreeNode)node).getPath());
    }
    else {
      ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
      TreeNode eachParent = node;
      while (eachParent != null) {
        nodes.add(eachParent);
        eachParent = eachParent.getParent();
      }

      return new TreePath(ArrayUtil.toObjectArray(nodes));
    }
  }


  private void removeNodeFromParent(final MutableTreeNode node, final boolean willAdjustSelection) {
    doWithUpdaterState(new Runnable() {
      public void run() {
        if (willAdjustSelection) {
          final TreePath path = getPathFor(node);
          if (myTree.isPathSelected(path)) {
            myTree.removeSelectionPath(path);
          }
        }

        myTreeModel.removeNodeFromParent(node);
      }
    });
  }

  private void expandPath(final TreePath path) {
    doWithUpdaterState(new Runnable() {
      public void run() {
        myTree.expandPath(path);
      }
    });
  }

  private void doWithUpdaterState(Runnable runnable) {
    if (myUpdaterState != null) {
      myUpdaterState.process(runnable);
    }
    else {
      runnable.run();
    }
  }

  protected boolean doUpdateNodeDescriptor(final NodeDescriptor descriptor) {
    return descriptor.update();
  }

  private void addLoadingNode(final DefaultMutableTreeNode node) {
    final NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();
    if (!getBuilder().isAlwaysShowPlus(descriptor)) {
      final boolean[] hasNoChildren = new boolean[1];
      Runnable updateRunnable = new Runnable() {
        public void run() {
          if (isReleased()) return;

          if (hasNoChildren[0]) {
            update(descriptor, false);
            removeLoading(node, false);
          }
        }
      };

      if (istToBuildInBackground(descriptor)) {
        Runnable buildRunnable = new Runnable() {
          public void run() {
            if (isReleased()) return;

            update(descriptor, true);
            Object element = getBuilder().getTreeStructureElement(descriptor);
            if (element == null && !isValid(element)) return;

            Object[] children = getChildrenFor(element);
            hasNoChildren[0] = children.length == 0;
          }
        };
        addTaskToWorker(buildRunnable, false, updateRunnable, new Runnable() {
          public void run() {
            processNodeActionsIfReady(node);
          }
        });
      }
      else {
        Object[] children = getChildrenFor(getBuilder().getTreeStructureElement(descriptor));
        if (children.length == 0) return;
      }
    }

    insertLoadingNode(node, true);
  }


 private boolean isValid(DefaultMutableTreeNode node) {
    if (node == null) return false;
    final Object object = node.getUserObject();
    if (object instanceof NodeDescriptor) {
      return isValid((NodeDescriptor)object);
    }

    return false;
  }

  private boolean isValid(NodeDescriptor descriptor) {
    if (descriptor == null) return false;
    return isValid(getElementFromDescriptor(descriptor));
  }

  private boolean isValid(Object element) {
    if (element instanceof ValidateableNode) {
      if (!((ValidateableNode)element).isValid()) return false;
    }
    return getBuilder().validateNode(element);
  }

  private void insertLoadingNode(final DefaultMutableTreeNode node, boolean addToUnbuilt) {
    myTreeModel.insertNodeInto(new LoadingNode(), node, 0);
    if (addToUnbuilt) {
      addToUnbuilt(node);
    }
  }


  protected void addTaskToWorker(@NotNull final Runnable bgReadActionRunnable,
                                 boolean first,
                                 @Nullable final Runnable edtPostRunnable,
                                 @Nullable final Runnable finalizeEdtRunnable) {
    registerWorkerTask(bgReadActionRunnable);

    final Runnable pooledThreadWithProgressRunnable = new Runnable() {
      public void run() {
        if (isReleased()) {
          return;
        }

        final AbstractTreeBuilder builder = getBuilder();

        builder.runBackgroundLoading(new Runnable() {
          public void run() {
            assertNotDispatchThread();

            if (isReleased()) {
              return;
            }

            try {
              bgReadActionRunnable.run();

              if (edtPostRunnable != null && !isReleased()) {
                builder.updateAfterLoadedInBackground(new Runnable() {
                  public void run() {
                    try {
                      assertIsDispatchThread();

                      if (isReleased()) {
                        return;
                      }

                      edtPostRunnable.run();
                    }
                    finally {
                      unregisterWorkerTask(bgReadActionRunnable, finalizeEdtRunnable);
                    }
                  }
                });
              }
              else {
                unregisterWorkerTask(bgReadActionRunnable, finalizeEdtRunnable);
              }
            }
            catch (ProcessCanceledException e) {
              unregisterWorkerTask(bgReadActionRunnable, finalizeEdtRunnable);
            }
            catch (Throwable t) {
              unregisterWorkerTask(bgReadActionRunnable, finalizeEdtRunnable);
              throw new RuntimeException(t);
            }
          }
        });
      }
    };

    Runnable pooledThreadRunnable = new Runnable() {
      public void run() {
        if (isReleased()) return;

        try {
          if (myProgress != null) {
            ProgressManager.getInstance().runProcess(pooledThreadWithProgressRunnable, myProgress);
          }
          else {
            pooledThreadWithProgressRunnable.run();
          }
        }
        catch (ProcessCanceledException e) {
          //ignore
        }
      }
    };

    if (myWorker == null || myWorker.isDisposed()) {
      myWorker = new WorkerThread("AbstractTreeBuilder.Worker", 1);
      myWorker.start();
      if (first) {
        myWorker.addTaskFirst(pooledThreadRunnable);
      }
      else {
        myWorker.addTask(pooledThreadRunnable);
      }
      myWorker.dispose(false);
    }
    else {
      if (first) {
        myWorker.addTaskFirst(pooledThreadRunnable);
      }
      else {
        myWorker.addTask(pooledThreadRunnable);
      }
    }
  }

  private void registerWorkerTask(Runnable runnable) {
    synchronized (myActiveWorkerTasks) {
      myActiveWorkerTasks.add(runnable);
    }
  }

  private void unregisterWorkerTask(Runnable runnable, @Nullable Runnable finalizeRunnable) {
    boolean wasRemoved;
    synchronized (myActiveWorkerTasks) {
      wasRemoved = myActiveWorkerTasks.remove(runnable);
    }

    if (wasRemoved && finalizeRunnable != null) {
      finalizeRunnable.run();
    }

    maybeReady();
  }

  public boolean isWorkerBusy() {
    synchronized (myActiveWorkerTasks) {
      return !myActiveWorkerTasks.isEmpty();
    }
  }

  private void clearWorkerTasks() {
    synchronized (myActiveWorkerTasks) {
      myActiveWorkerTasks.clear();
    }
  }

  private void updateNodeImageAndPosition(final DefaultMutableTreeNode node) {
    if (!(node.getUserObject() instanceof NodeDescriptor)) return;
    NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();
    if (getElementFromDescriptor(descriptor) == null) return;
    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
    if (parentNode != null) {
      int oldIndex = parentNode.getIndex(node);
      int newIndex = oldIndex;
      if (isLoadingChildrenFor(node.getParent()) || getBuilder().isChildrenResortingNeeded(descriptor)) {
        final ArrayList<TreeNode> children = new ArrayList<TreeNode>(parentNode.getChildCount());
        for (int i = 0; i < parentNode.getChildCount(); i++) {
          children.add(parentNode.getChildAt(i));
        }
        Collections.sort(children, myNodeComparator);
        newIndex = children.indexOf(node);
      }

      if (oldIndex != newIndex) {
        List<Object> pathsToExpand = new ArrayList<Object>();
        List<Object> selectionPaths = new ArrayList<Object>();
        TreeBuilderUtil.storePaths(getBuilder(), node, pathsToExpand, selectionPaths, false);
        removeNodeFromParent(node, false);
        myTreeModel.insertNodeInto(node, parentNode, newIndex);
        TreeBuilderUtil.restorePaths(getBuilder(), pathsToExpand, selectionPaths, false);
      }
      else {
        myTreeModel.nodeChanged(node);
      }
    }
    else {
      myTreeModel.nodeChanged(node);
    }
  }

  public DefaultTreeModel getTreeModel() {
    return myTreeModel;
  }

  private void insertNodesInto(ArrayList<TreeNode> nodes, DefaultMutableTreeNode parentNode) {
    if (nodes.isEmpty()) return;

    nodes = new ArrayList<TreeNode>(nodes);
    Collections.sort(nodes, myNodeComparator);

    ArrayList<TreeNode> all = TreeUtil.childrenToArray(parentNode);
    all.addAll(nodes);
    Collections.sort(all, myNodeComparator);

    int[] indices = new int[nodes.size()];
    int idx = 0;
    for (int i = 0; i < nodes.size(); i++) {
      TreeNode node = nodes.get(i);
      while (all.get(idx) != node) idx++;
      indices[i] = idx;
      parentNode.insert((MutableTreeNode)node, idx);
    }

    myTreeModel.nodesWereInserted(parentNode, indices);
  }

  private void disposeNode(DefaultMutableTreeNode node) {
    myUpdatingChildren.remove(node);
    removeFromUnbuilt(node);

    if (node.getChildCount() > 0) {
      for (DefaultMutableTreeNode _node = (DefaultMutableTreeNode)node.getFirstChild(); _node != null; _node = _node.getNextSibling()) {
        disposeNode(_node);
      }
    }
    if (isLoadingNode(node)) return;
    NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();
    if (descriptor == null) return;
    final Object element = getElementFromDescriptor(descriptor);
    removeMapping(element, node, null);
    node.setUserObject(null);
    node.removeAllChildren();
  }

  public void addSubtreeToUpdate(final DefaultMutableTreeNode root) {
    addSubtreeToUpdate(root, null);
  }

  public void addSubtreeToUpdate(final DefaultMutableTreeNode root, Runnable runAfterUpdate) {
    getUpdater().runAfterUpdate(runAfterUpdate);
    getUpdater().addSubtreeToUpdate(root);
  }

  public boolean wasRootNodeInitialized() {
    return myRootNodeWasInitialized;
  }

  private boolean isRootNodeBuilt() {
    return myRootNodeWasInitialized && isNodeBeingBuilt(myRootNode);
  }

  public void select(final Object[] elements, @Nullable final Runnable onDone) {
    select(elements, onDone, false);
  }

  public void select(final Object[] elements, @Nullable final Runnable onDone, boolean addToSelection) {
    select(elements, onDone, addToSelection, false);
  }

  public void select(final Object[] elements, @Nullable final Runnable onDone, boolean addToSelection, boolean deferred) {
    _select(elements, onDone, addToSelection, true, false, true, deferred);
  }

  void _select(final Object[] elements,
               final Runnable onDone,
               final boolean addToSelection,
               final boolean checkCurrentSelection,
               final boolean checkIfInStructure) {

    _select(elements, onDone, addToSelection, checkCurrentSelection, checkIfInStructure, true, false);
  }

  void _select(final Object[] elements,
               final Runnable onDone,
               final boolean addToSelection,
               final boolean checkCurrentSelection,
               final boolean checkIfInStructure,
               final boolean scrollToVisible) {

    _select(elements, onDone, addToSelection, checkCurrentSelection, checkIfInStructure, scrollToVisible, false);
  }

  void _select(final Object[] elements,
               final Runnable onDone,
               final boolean addToSelection,
               final boolean checkCurrentSelection,
               final boolean checkIfInStructure, final boolean scrollToVisible, final boolean deferred) {

    boolean willAffectSelection = elements.length > 0 || elements.length == 0 && addToSelection;
    if (!willAffectSelection) {
      runDone(onDone);
      return;
    }

    final boolean oldCanProcessDeferredSelection = myCanProcessDeferredSelections;

    if (!deferred && wasRootNodeInitialized() && willAffectSelection) {
      myCanProcessDeferredSelections = false;
    }

    if (!checkDeferred(deferred, onDone)) return;

    if (!deferred && oldCanProcessDeferredSelection && !myCanProcessDeferredSelections) {
      getTree().clearSelection();
    }


    runDone(new Runnable() {
      public void run() {
        if (!checkDeferred(deferred, onDone)) return;

        final Set<Object> currentElements = getSelectedElements();

        if (checkCurrentSelection && !currentElements.isEmpty() && elements.length == currentElements.size()) {
          boolean runSelection = false;
          for (Object eachToSelect : elements) {
            if (!currentElements.contains(eachToSelect)) {
              runSelection = true;
              break;
            }
          }

          if (!runSelection) {
            if (elements.length > 0) {
              selectVisible(elements[0], onDone, true, true, scrollToVisible);
            }
            return;
          }
        }

        Set<Object> toSelect = new HashSet<Object>();
        myTree.clearSelection();
        toSelect.addAll(Arrays.asList(elements));
        if (addToSelection) {
          toSelect.addAll(currentElements);
        }

        if (checkIfInStructure) {
          final Iterator<Object> allToSelect = toSelect.iterator();
          while (allToSelect.hasNext()) {
            Object each = allToSelect.next();
            if (!isInStructure(each)) {
              allToSelect.remove();
            }
          }
        }

        final Object[] elementsToSelect = ArrayUtil.toObjectArray(toSelect);

        if (wasRootNodeInitialized()) {
          final int[] originalRows = myTree.getSelectionRows();
          if (!addToSelection) {
            myTree.clearSelection();
          }
          addNext(elementsToSelect, 0, onDone, originalRows, deferred, scrollToVisible);
        }
        else {
          addToDeferred(elementsToSelect, onDone);
        }
      }
    });
  }

  private void addToDeferred(final Object[] elementsToSelect, final Runnable onDone) {
    myDeferredSelections.clear();
    myDeferredSelections.add(new Runnable() {
      public void run() {
        select(elementsToSelect, onDone, false, true);
      }
    });
  }

  private boolean checkDeferred(boolean isDeferred, @Nullable Runnable onDone) {
    if (!isDeferred || myCanProcessDeferredSelections || !wasRootNodeInitialized()) {
      return true;
    } else {
      runDone(onDone);
      return false;
    }
  }

  @NotNull
  final Set<Object> getSelectedElements() {
    final TreePath[] paths = myTree.getSelectionPaths();

    Set<Object> result = new HashSet<Object>();
    if (paths != null) {
      for (TreePath eachPath : paths) {
        if (eachPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
          final DefaultMutableTreeNode eachNode = (DefaultMutableTreeNode)eachPath.getLastPathComponent();
          final Object eachElement = getElementFor(eachNode);
          if (eachElement != null) {
            result.add(eachElement);
          }
        }
      }
    }
    return result;
  }


  private void addNext(final Object[] elements, final int i, @Nullable final Runnable onDone, final int[] originalRows, final boolean deferred, final boolean scrollToVisible) {
    if (i >= elements.length) {
      if (myTree.isSelectionEmpty()) {
        myTree.setSelectionRows(originalRows);
      }
      runDone(onDone);
    }
    else {
      if (!checkDeferred(deferred, onDone)) {
        return;
      }

      doSelect(elements[i], new Runnable() {
        public void run() {
          if (!checkDeferred(deferred, onDone)) return;

          addNext(elements, i + 1, onDone, originalRows, deferred, scrollToVisible);
        }
      }, true, deferred, i == 0, scrollToVisible);
    }
  }

  public void select(final Object element, @Nullable final Runnable onDone) {
    select(element, onDone, false);
  }

  public void select(final Object element, @Nullable final Runnable onDone, boolean addToSelection) {
    _select(new Object[] {element}, onDone, addToSelection, true, false);
  }

  private void doSelect(final Object element, final Runnable onDone, final boolean addToSelection, final boolean deferred, final boolean canBeCentered, final boolean scrollToVisible) {
    final Runnable _onDone = new Runnable() {
      public void run() {
        if (!checkDeferred(deferred, onDone)) return;
        selectVisible(element, onDone, addToSelection, canBeCentered, scrollToVisible);
      }
    };
    _expand(element, _onDone, true, false);
  }

  private void selectVisible(Object element, final Runnable onDone, boolean addToSelection, boolean canBeCentered, final boolean scroll) {
    final DefaultMutableTreeNode toSelect = getNodeForElement(element, false);
    if (toSelect == null) {
      runDone(onDone);
      return;
    }
    final int row = myTree.getRowForPath(new TreePath(toSelect.getPath()));

    if (myUpdaterState != null) {
      myUpdaterState.addSelection(element);
    }

    if (Registry.is("ide.tree.autoscrollToVCenter") && canBeCentered) {
      runDone(new Runnable() {
        public void run() {
          TreeUtil.showRowCentered(myTree, row, false, scroll).doWhenDone(new Runnable() {
            public void run() {
              runDone(onDone);
            }
          });
        }
      });
    } else {
      TreeUtil.showAndSelect(myTree, row - 2, row + 2, row, -1, addToSelection, scroll).doWhenDone(new Runnable() {
        public void run() {
          runDone(onDone);
        }
      });
    }
  }

  public void expand(final Object element, @Nullable final Runnable onDone) {
    expand(element, onDone, false);
  }


  void expand(final Object element, @Nullable final Runnable onDone, boolean checkIfInStructure) {
    _expand(new Object[]{element}, onDone == null ? new EmptyRunnable() : onDone, false, checkIfInStructure);
  }

  void expand(final Object[] element, @Nullable final Runnable onDone, boolean checkIfInStructure) {
    _expand(element, onDone == null ? new EmptyRunnable() : onDone, false, checkIfInStructure);
  }

  void _expand(final Object[] element, @NotNull final Runnable onDone, final boolean parentsOnly, final boolean checkIfInStructure) {
    runDone(new Runnable() {
      public void run() {
        if (element.length == 0) {
          runDone(onDone);
          return;
        }

        if (myUpdaterState != null) {
          myUpdaterState.clearExpansion();
        }


        final ActionCallback done = new ActionCallback(element.length);
        done.doWhenDone(new Runnable() {
          public void run() {
            runDone(onDone);
          }
        });

        for (final Object toExpand : element) {
          _expand(toExpand, new Runnable() {
            public void run() {
              done.setDone();
            }
          }, parentsOnly, checkIfInStructure);
        }
      }
    });
  }

  public void collapseChildren(final Object element, @Nullable final Runnable onDone) {
    runDone(new Runnable() {
      public void run() {
        final DefaultMutableTreeNode node = getNodeForElement(element, false);
        if (node != null) {
          getTree().collapsePath(new TreePath(node.getPath()));
          runDone(onDone);
        }
      }
    });
  }

  private void runDone(@Nullable Runnable done) {
    if (isReleased()) return;
    if (done == null) return;

    if (isYeildingNow()) {
      if (!myYeildingDoneRunnables.contains(done)) {
        myYeildingDoneRunnables.add(done);
      }
    }
    else {
      done.run();
    }
  }

  private void _expand(final Object element, @NotNull final Runnable onDone, final boolean parentsOnly, boolean checkIfInStructure) {
    if (checkIfInStructure && !isInStructure(element)) {
      runDone(onDone);
      return;
    }

    if (wasRootNodeInitialized()) {
      List<Object> kidsToExpand = new ArrayList<Object>();
      Object eachElement = element;
      DefaultMutableTreeNode firstVisible = null;
      while (true) {
        if (!isValid(eachElement)) break;

        firstVisible = getNodeForElement(eachElement, true);
        if (eachElement != element || !parentsOnly) {
          assert !kidsToExpand.contains(eachElement) :
            "Not a valid tree structure, walking up the structure gives many entries for element=" +
            eachElement +
            ", root=" +
            getTreeStructure().getRootElement();
          kidsToExpand.add(eachElement);
        }
        if (firstVisible != null) break;
        eachElement = getTreeStructure().getParentElement(eachElement);
        if (eachElement == null) {
          firstVisible = null;
          break;
        }
      }

      if (firstVisible == null) {
        runDone(onDone);
      }
      else if (kidsToExpand.isEmpty()) {
        final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)firstVisible.getParent();
        if (parentNode != null) {
          final TreePath parentPath = new TreePath(parentNode.getPath());
          if (!myTree.isExpanded(parentPath)) {
            expand(parentPath);
          }
        }
        runDone(onDone);
      }
      else {
        processExpand(firstVisible, kidsToExpand, kidsToExpand.size() - 1, onDone);
      }
    }
    else {
      deferExpansion(element, onDone, parentsOnly);
    }
  }

  private void deferExpansion(final Object element, final Runnable onDone, final boolean parentsOnly) {
    myDeferredExpansions.add(new Runnable() {
      public void run() {
        _expand(element, onDone, parentsOnly, false);
      }
    });
  }

  private void processExpand(final DefaultMutableTreeNode toExpand,
                             final List kidsToExpand,
                             final int expandIndex,
                             @NotNull final Runnable onDone) {
    final Object element = getElementFor(toExpand);
    if (element == null) {
      runDone(onDone);
      return;
    }

    addNodeAction(element, new NodeAction() {
      public void onReady(final DefaultMutableTreeNode node) {
        if (node.getChildCount() >= 0 && !myTree.isExpanded(new TreePath(node.getPath()))) {
          expand(node);
        }

        if (expandIndex < 0) {
          runDone(onDone);
          return;
        }

        final DefaultMutableTreeNode nextNode = getNodeForElement(kidsToExpand.get(expandIndex), false);
        if (nextNode != null) {
          processExpand(nextNode, kidsToExpand, expandIndex - 1, onDone);
        }
        else {
          runDone(onDone);
        }
      }
    });

    expand(toExpand);
  }


  @Nullable
  private Object getElementFor(Object node) {
    if (!(node instanceof DefaultMutableTreeNode)) return null;
    return getElementFor((DefaultMutableTreeNode)node);
  }

  @Nullable
  private Object getElementFor(DefaultMutableTreeNode node) {
    if (node != null) {
      final Object o = node.getUserObject();
      if (o instanceof NodeDescriptor) {
        return getElementFromDescriptor((NodeDescriptor)o);
      }
    }

    return null;
  }

  public final boolean isNodeBeingBuilt(final TreePath path) {
    return isNodeBeingBuilt(path.getLastPathComponent());
  }

  public final boolean isNodeBeingBuilt(Object node) {
    if (isParentLoading(node) || isLoadingParent(node)) return true;

    final boolean childrenAreNoLoadedYet = isLoadingChildrenFor(node) && myUnbuiltNodes.contains(node);
    if (childrenAreNoLoadedYet) {
      if (node instanceof DefaultMutableTreeNode) {
        final TreePath nodePath = new TreePath(((DefaultMutableTreeNode)node).getPath());
        if (!myTree.isExpanded(nodePath)) return false;
      }

      return true;
    }


    return false;
  }

  private boolean isLoadingParent(Object node) {
    return node instanceof DefaultMutableTreeNode && isLoadedInBackground(getElementFor((DefaultMutableTreeNode)node));
  }

  public void setTreeStructure(final AbstractTreeStructure treeStructure) {
    myTreeStructure = treeStructure;
    clearUpdaterState();
  }

  public AbstractTreeUpdater getUpdater() {
    return myUpdater;
  }

  public void setUpdater(final AbstractTreeUpdater updater) {
    myUpdater = updater;
  }

  public DefaultMutableTreeNode getRootNode() {
    return myRootNode;
  }

  public void setRootNode(@NotNull final DefaultMutableTreeNode rootNode) {
    myRootNode = rootNode;
  }

  private void dropUpdaterStateIfExternalChange() {
    if (myUpdaterState != null && !myUpdaterState.isProcessingNow()) {
      clearUpdaterState();
    }
  }

  private void clearUpdaterState() {
    myUpdaterState = null;
  }

  private void createMapping(Object element, DefaultMutableTreeNode node) {
    if (!myElementToNodeMap.containsKey(element)) {
      myElementToNodeMap.put(element, node);
    }
    else {
      final Object value = myElementToNodeMap.get(element);
      final List<DefaultMutableTreeNode> nodes;
      if (value instanceof DefaultMutableTreeNode) {
        nodes = new ArrayList<DefaultMutableTreeNode>();
        nodes.add((DefaultMutableTreeNode)value);
        myElementToNodeMap.put(element, nodes);
      }
      else {
        nodes = (List<DefaultMutableTreeNode>)value;
      }
      nodes.add(node);
    }
  }

  private void removeMapping(Object element, DefaultMutableTreeNode node, @Nullable Object elementToPutNodeActionsFor) {
    final Object value = myElementToNodeMap.get(element);
    if (value != null) {
      if (value instanceof DefaultMutableTreeNode) {
        if (value.equals(node)) {
          myElementToNodeMap.remove(element);
        }
      }
      else {
        List<DefaultMutableTreeNode> nodes = (List<DefaultMutableTreeNode>)value;
        final boolean reallyRemoved = nodes.remove(node);
        if (reallyRemoved) {
          if (nodes.isEmpty()) {
            myElementToNodeMap.remove(element);
          }
        }
      }
    }

    final List<NodeAction> actions = myNodeActions.get(element);
    myNodeActions.remove(element);

    if (elementToPutNodeActionsFor != null && actions != null) {
      myNodeActions.put(elementToPutNodeActionsFor, actions);
    }
  }

  private DefaultMutableTreeNode getFirstNode(Object element) {
    return findNode(element, 0);
  }

  private DefaultMutableTreeNode findNode(final Object element, int startIndex) {
    final Object value = getBuilder().findNodeByElement(element);
    if (value == null) {
      return null;
    }
    if (value instanceof DefaultMutableTreeNode) {
      return startIndex == 0 ? (DefaultMutableTreeNode)value : null;
    }
    final List<DefaultMutableTreeNode> nodes = (List<DefaultMutableTreeNode>)value;
    return startIndex < nodes.size() ? nodes.get(startIndex) : null;
  }

  protected Object findNodeByElement(Object element) {
    if (myElementToNodeMap.containsKey(element)) {
      return myElementToNodeMap.get(element);
    }

    try {
      TREE_NODE_WRAPPER.setValue(element);
      return myElementToNodeMap.get(TREE_NODE_WRAPPER);
    }
    finally {
      TREE_NODE_WRAPPER.setValue(null);
    }
  }

  private DefaultMutableTreeNode findNodeForChildElement(DefaultMutableTreeNode parentNode, Object element) {
    final Object value = myElementToNodeMap.get(element);
    if (value == null) {
      return null;
    }

    if (value instanceof DefaultMutableTreeNode) {
      final DefaultMutableTreeNode elementNode = (DefaultMutableTreeNode)value;
      return parentNode.equals(elementNode.getParent()) ? elementNode : null;
    }

    final List<DefaultMutableTreeNode> allNodesForElement = (List<DefaultMutableTreeNode>)value;
    for (final DefaultMutableTreeNode elementNode : allNodesForElement) {
      if (parentNode.equals(elementNode.getParent())) {
        return elementNode;
      }
    }

    return null;
  }

  public void cancelBackgroundLoading() {
    if (myWorker != null) {
      myWorker.cancelTasks();
      clearWorkerTasks();
    }
    myNodeActions.clear();
  }

  private void addNodeAction(Object element, NodeAction action) {
    maybeSetBusyAndScheduleWaiterForReady(true);

    List<NodeAction> list = myNodeActions.get(element);
    if (list == null) {
      list = new ArrayList<NodeAction>();
      myNodeActions.put(element, list);
    }
    list.add(action);
  }

  private void cleanUpNow() {
    if (isReleased()) return;

    final UpdaterTreeState state = new UpdaterTreeState(this);

    myTree.collapsePath(new TreePath(myTree.getModel().getRoot()));
    myTree.clearSelection();
    getRootNode().removeAllChildren();

    myRootNodeWasInitialized = false;
    myNodeActions.clear();
    myElementToNodeMap.clear();
    myDeferredSelections.clear();
    myDeferredExpansions.clear();
    myLoadingParents.clear();
    myUnbuiltNodes.clear();
    myUpdateFromRootRequested = true;

    if (myWorker != null) {
      Disposer.dispose(myWorker);
      myWorker = null;
    }

    myTree.invalidate();

    state.restore();
  }

  public AbstractTreeUi setClearOnHideDelay(final long clearOnHideDelay) {
    myClearOnHideDelay = clearOnHideDelay;
    return this;
  }

  public void setJantorPollPeriod(final long time) {
    myJanitorPollPeriod = time;
  }

  public void setCheckStructure(final boolean checkStructure) {
    myCheckStructure = checkStructure;
  }

  private class MySelectionListener implements TreeSelectionListener {
    public void valueChanged(final TreeSelectionEvent e) {
      dropUpdaterStateIfExternalChange();
    }
  }

  private class MyExpansionListener implements TreeExpansionListener {
    public void treeExpanded(TreeExpansionEvent event) {
      dropUpdaterStateIfExternalChange();

      TreePath path = event.getPath();
      final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

      if (!myUnbuiltNodes.contains(node)) {
        return;
      }
      removeFromUnbuilt(node);


      getBuilder().expandNodeChildren(node);

      runDone(new Runnable() {
        public void run() {
          final Object element = getElementFor(node);

          removeLoading(node, false);

          if (node.getChildCount() == 0) {
            addNodeAction(element, new NodeAction() {
              public void onReady(final DefaultMutableTreeNode node) {
                expand(element, null);
              }
            });
          }

          processSmartExpand(node);
        }
      });
    }

    public void treeCollapsed(TreeExpansionEvent e) {
      final TreePath path = e.getPath();
      final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      if (!(node.getUserObject() instanceof NodeDescriptor)) return;

      TreePath pathToSelect = null;
      if (isSelectionInside(node)) {
        pathToSelect = new TreePath(node.getPath());
      }


      NodeDescriptor descriptor = (NodeDescriptor)node.getUserObject();
      if (getBuilder().isDisposeOnCollapsing(descriptor)) {
        runDone(new Runnable() {
          public void run() {
            if (isDisposed(node)) return;

            TreePath nodePath = new TreePath(node.getPath());
            if (myTree.isExpanded(nodePath)) return;

            removeChildren(node);
            addLoadingNode(node);
          }
        });
        if (node.equals(getRootNode())) {
          addSelectionPath(new TreePath(getRootNode().getPath()), true, Condition.FALSE);
        }
        else {
          myTreeModel.reload(node);
        }
      }

      if (pathToSelect != null && myTree.isSelectionEmpty()) {
        addSelectionPath(pathToSelect, true, Condition.FALSE);
      }
    }

    private void removeChildren(DefaultMutableTreeNode node) {
      EnumerationCopy copy = new EnumerationCopy(node.children());
      while (copy.hasMoreElements()) {
        disposeNode((DefaultMutableTreeNode)copy.nextElement());
      }
      node.removeAllChildren();
      myTreeModel.nodeStructureChanged(node);
    }

    private boolean isSelectionInside(DefaultMutableTreeNode parent) {
      TreePath path = new TreePath(myTreeModel.getPathToRoot(parent));
      TreePath[] paths = myTree.getSelectionPaths();
      if (paths == null) return false;
      for (TreePath path1 : paths) {
        if (path.isDescendant(path1)) return true;
      }
      return false;
    }
  }

  public boolean isInStructure(@Nullable Object element) {
    Object eachParent = element;
    while (eachParent != null) {
      if (getTreeStructure().getRootElement().equals(eachParent)) return true;
      eachParent = getTreeStructure().getParentElement(eachParent);
    }

    return false;
  }

  interface NodeAction {
    void onReady(DefaultMutableTreeNode node);
  }

  public void setCanYield(final boolean canYield) {
    myCanYield = canYield;
  }

  public Collection<TreeUpdatePass> getYeildingPasses() {
    return myYeildingPasses;
  }

  public boolean isBuilt(Object element) {
    if (!myElementToNodeMap.containsKey(element)) return false;
    final Object node = myElementToNodeMap.get(element);
    return !myUnbuiltNodes.contains(node);
  }
}