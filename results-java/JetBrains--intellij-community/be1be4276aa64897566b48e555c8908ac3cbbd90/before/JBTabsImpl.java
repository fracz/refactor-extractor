package com.intellij.ui.tabs.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ShadowAction;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.impl.content.GraphicsConfig;
import com.intellij.ui.CaptionPanel;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.util.ui.Animator;
import com.intellij.util.ui.AwtVisitor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

public class JBTabsImpl extends JComponent
    implements JBTabs, PropertyChangeListener, TimerListener, DataProvider, PopupMenuListener, Disposable {

  static DataKey<JBTabsImpl> NAVIGATION_ACTIONS_KEY = DataKey.create("JBTabs");

  ActionManager myActionManager;
  final List<TabInfo> myVisibleInfos = new ArrayList<TabInfo>();
  final Set<TabInfo> myHiddenInfos = new HashSet<TabInfo>();

  TabInfo mySelectedInfo;
  final Map<TabInfo, TabLabel> myInfo2Label = new HashMap<TabInfo, TabLabel>();
  final Map<TabInfo, JComponent> myInfo2Toolbar = new HashMap<TabInfo, JComponent>();
  Dimension myHeaderFitSize;

  Insets myInnerInsets = new Insets(0, 0, 0, 0);

  final List<MouseListener> myTabMouseListeners = new ArrayList<MouseListener>();
  final List<TabsListener> myTabListeners = new ArrayList<TabsListener>();
  boolean myFocused;

  Getter<ActionGroup> myPopupGroup;
  String myPopupPlace;

  TabInfo myPopupInfo;
  DefaultActionGroup myNavigationActions;

  PopupMenuListener myPopupListener;
  JPopupMenu myActivePopup;

  boolean myHorizontalSide = true;
  LineLayoutData myLastSingRowLayout;

  boolean myStealthTabMode = false;

  DataProvider myDataProvider;

  WeakReference<Component> myDeferredToRemove = new WeakReference<Component>(null);

  private MoreIcon myMoreIcon = new MoreIcon() {
    protected boolean isActive() {
      return myFocused;
    }

    protected Rectangle getIconRec() {
      return myLastSingRowLayout != null ? myLastSingRowLayout.moreRect : null;
    }
  };
  private JPopupMenu myMorePopup;

  private boolean mySingleRow = true;
  private TableLayoutData myLastTableLayout;

  private boolean myForcedRelayout;

  private UiDecorator myUiDecorator;
  static final UiDecorator ourDefaultDecorator = new DefautDecorator();

  private boolean myPaintFocus = true;

  private boolean myHideTabs = false;
  private @Nullable Project myProject;

  private boolean myRequestFocusOnLastFocusedComponent = false;
  private boolean myListenerAdded;
  final Set<TabInfo> myAttractions = new HashSet<TabInfo>();
  Animator myAnimator;
  static final String DEFERRED_REMOVE_FLAG = "JBTabs.deferredRemove";
  List<TabInfo> myAllTabs;
  boolean myPaintBlocked;
  BufferedImage myImage;
  IdeFocusManager myFocusManager;
  boolean myAdjustBorders = true;

  int myTopBorderSize;
  int myLeftBorderSize;
  int myRightBorderSize;
  int myBottomBorderSize;
  boolean myAddNavigationGroup = true;
  Layout myLastLayoutData;

  GhostComponent myLeftGhost = new GhostComponent(RowDropPolicy.leftFirst, RowDropPolicy.leftFirst);
  GhostComponent myRightGhost = new GhostComponent(RowDropPolicy.rightFirst, RowDropPolicy.leftFirst);

  boolean myGhostsAlwaysVisible = false;

  private enum RowDropPolicy {
    leftFirst, rightFirst
  }

  private RowDropPolicy myRowDropPolicy = RowDropPolicy.leftFirst;

  public JBTabsImpl(@Nullable Project project, ActionManager actionManager, IdeFocusManager focusManager, Disposable parent) {
    myProject = project;
    myActionManager = actionManager;
    myFocusManager = focusManager;
    setOpaque(true);
    setPaintBorder(-1, -1, -1, -1);

    Disposer.register(parent, this);

    myNavigationActions = new DefaultActionGroup();

    if (myActionManager != null) {
      myNavigationActions.add(new SelectNextAction(this));
      myNavigationActions.add(new SelectPreviousAction(this));
    }

    setUiDecorator(null);

    UIUtil.addAwtListener(new AWTEventListener() {
      public void eventDispatched(final AWTEvent event) {
        if (myMorePopup != null) return;
        processFocusChange();
      }
    }, FocusEvent.FOCUS_EVENT_MASK, this);

    myPopupListener = new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        disposePopupListener();
      }

      public void popupMenuCanceled(final PopupMenuEvent e) {
        disposePopupListener();
      }
    };

    addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent e) {
        if (myLastSingRowLayout != null && myLastSingRowLayout.moreRect != null && myLastSingRowLayout.moreRect.contains(e.getPoint())) {
          showMorePopup(e);
        }
      }
    });

    Disposer.register(this, new Disposable() {
      public void dispose() {
        removeTimerUpdate();
      }
    });

    myAnimator = new Animator("JBTabs Attractions", 2, 500, true, 0, -1) {
      public void paintNow(final int frame) {
        repaintAttractions();
      }
    };
    myAnimator.setTakInitialDelay(false);

    Disposer.register(this, myAnimator);

    setFocusCycleRoot(true);

    setFocusTraversalPolicy(new FocusTraversalPolicy() {
      public Component getComponentAfter(final Container aContainer, final Component aComponent) {
        return getToFocus();
      }

      public Component getComponentBefore(final Container aContainer, final Component aComponent) {
        return getToFocus();
      }

      public Component getFirstComponent(final Container aContainer) {
        return getToFocus();
      }

      public Component getLastComponent(final Container aContainer) {
        return getToFocus();
      }

      public Component getDefaultComponent(final Container aContainer) {
        return getToFocus();
      }
    });

    add(myLeftGhost);
    add(myRightGhost);
  }

  public void dispose() {
    mySelectedInfo = null;
    myAllTabs = null;
    myAttractions.clear();
    myVisibleInfos.clear();
    myUiDecorator = null;
    myImage = null;
    myActivePopup = null;
    myInfo2Label.clear();
    myInfo2Toolbar.clear();
  }


  private void processFocusChange() {
    Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (owner == null) {
      setFocused(false);
      return;
    }

    if (owner == JBTabsImpl.this || SwingUtilities.isDescendingFrom(owner, JBTabsImpl.this)) {
      setFocused(true);
    }
    else {
      setFocused(false);
    }
  }

  private void repaintAttractions() {
    boolean needsUpdate = false;
    for (TabInfo each : myVisibleInfos) {
      TabLabel eachLabel = myInfo2Label.get(each);
      needsUpdate |= eachLabel.repaintAttraction();
    }

    if (needsUpdate) {
      relayout(true, false);
    }
  }

  public void addNotify() {
    super.addNotify();

    if (myActionManager != null && !myListenerAdded) {
      myActionManager.addTimerListener(500, this);
      myListenerAdded = true;
    }
  }

  public void removeNotify() {
    super.removeNotify();

    setFocused(false);

    removeTimerUpdate();
  }

  private void removeTimerUpdate() {
    if (myActionManager != null && myListenerAdded) {
      myActionManager.removeTimerListener(this);
      myListenerAdded = false;
    }
  }

  public ModalityState getModalityState() {
    return ModalityState.stateForComponent(this);
  }

  public void run() {
    updateTabActions(false);
  }

  public void updateTabActions(final boolean validateNow) {
    boolean changed = false;
    for (TabLabel label : myInfo2Label.values()) {
      changed |= label.updateTabActions();
    }

    if (changed) {
      if (validateNow) {
        validate();
        paintImmediately(0, 0, getWidth(), getHeight());
      }
      else {
        revalidateAndRepaint(false);
      }
    }
  }

  private void showMorePopup(final MouseEvent e) {
    myMorePopup = new JPopupMenu();
    for (final TabInfo each : myVisibleInfos) {
      final JCheckBoxMenuItem item = new JCheckBoxMenuItem(each.getText());
      myMorePopup.add(item);
      if (getSelectedInfo() == each) {
        item.setSelected(true);
      }
      item.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          select(each, true);
        }
      });
    }

    myMorePopup.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        myMorePopup = null;
      }

      public void popupMenuCanceled(final PopupMenuEvent e) {
        myMorePopup = null;
      }
    });

    myMorePopup.show(this, e.getX(), e.getY());
  }


  private JComponent getToFocus() {
    final TabInfo info = getSelectedInfo();

    if (info == null) return null;

    JComponent toFocus = null;

    if (isRequestFocusOnLastFocusedComponent() && info.getLastFocusOwner() != null && !isMyChildIsFocusedNow()) {
      toFocus = info.getLastFocusOwner();
    }

    if (toFocus == null && (info == null || info.getPreferredFocusableComponent() == null)) {
      return null;
    }


    if (toFocus == null) {
      toFocus = info.getPreferredFocusableComponent();
      final JComponent policyToFocus = myFocusManager != null ? myFocusManager.getFocusTargetFor(toFocus) : null;
      if (policyToFocus != null) {
        toFocus = policyToFocus;
      }
    }

    return toFocus;
  }

  public void requestFocus() {
    final JComponent toFocus = getToFocus();
    if (toFocus != null) {
      toFocus.requestFocus();
    }
    else {
      super.requestFocus();
    }
  }

  public boolean requestFocusInWindow() {
    final JComponent toFocus = getToFocus();
    if (toFocus != null) {
      return toFocus.requestFocusInWindow();
    }
    else {
      return super.requestFocusInWindow();
    }
  }

  private JBTabsImpl findTabs(Component c) {
    Component eachParent = c;
    while (eachParent != null) {
      if (eachParent instanceof JBTabsImpl) {
        return (JBTabsImpl)eachParent;
      }
      eachParent = eachParent.getParent();
    }

    return null;
  }


  @NotNull
  public TabInfo addTab(TabInfo info, int index) {
    if (getTabs().contains(info)) {
      return getTabs().get(getTabs().indexOf(info));
    }

    info.getChangeSupport().addPropertyChangeListener(this);
    final TabLabel label = new TabLabel(this, info);
    myInfo2Label.put(info, label);

    if (index < 0) {
      myVisibleInfos.add(info);
    }
    else if (index > myVisibleInfos.size() - 1) {
      myVisibleInfos.add(info);
    }
    else {
      myVisibleInfos.add(index, info);
    }

    myAllTabs = null;

    add(label);

    updateText(info);
    updateIcon(info);
    updateSideComponent(info);
    updateTabActions(info);

    updateAll(false, true);

    if (info.isHidden()) {
      updateHiding();
    }


    adjust(info);

    revalidateAndRepaint(false);

    return info;
  }


  @NotNull
  public TabInfo addTab(TabInfo info) {
    return addTab(info, -1);
  }

  public ActionGroup getPopupGroup() {
    return myPopupGroup.get();
  }

  public String getPopupPlace() {
    return myPopupPlace;
  }

  public JBTabs setPopupGroup(@NotNull final ActionGroup popupGroup, @NotNull String place, final boolean addNavigationGroup) {
    return setPopupGroup(new Getter<ActionGroup>() {
      public ActionGroup get() {
        return popupGroup;
      }
    }, place, addNavigationGroup);
  }

  public JBTabs setPopupGroup(@NotNull final Getter<ActionGroup> popupGroup,
                              @NotNull final String place,
                              final boolean addNavigationGroup) {
    myPopupGroup = popupGroup;
    myPopupPlace = place;
    myAddNavigationGroup = addNavigationGroup;
    return this;
  }

  private void updateAll(final boolean forcedRelayout, final boolean now) {
    mySelectedInfo = getSelectedInfo();
    removeDeferred(updateContainer(forcedRelayout, now));
    updateListeners();
    updateTabActions(false);
  }

  private boolean isMyChildIsFocusedNow() {
    final Component owner = getFocusOwner();
    return owner != null && SwingUtilities.isDescendingFrom(owner, this);
  }

  @Nullable
  private JComponent getFocusOwner() {
    final Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return (JComponent)(owner instanceof JComponent ? owner : null);
  }

  public ActionCallback select(@NotNull TabInfo info, boolean requestFocus) {
    return _setSelected(info, requestFocus);
  }

  private ActionCallback _setSelected(final TabInfo info, final boolean requestFocus) {
    if (mySelectedInfo != null && mySelectedInfo.equals(info)) {
      if (!requestFocus) {
        return new ActionCallback.Done();
      }
      else {
        requestFocus(getToFocus());
      }
    }


    if (myRequestFocusOnLastFocusedComponent && mySelectedInfo != null) {
      if (isMyChildIsFocusedNow()) {
        mySelectedInfo.setLastFocusOwner(getFocusOwner());
      }
    }

    TabInfo oldInfo = mySelectedInfo;
    mySelectedInfo = info;
    final TabInfo newInfo = getSelectedInfo();

    final Component deferredRemove = updateContainer(false, true);

    if (oldInfo != newInfo) {
      for (TabsListener eachListener : myTabListeners) {
        if (eachListener != null) {
          eachListener.selectionChanged(oldInfo, newInfo);
        }
      }
    }

    if (requestFocus) {
      final JComponent toFocus = getToFocus();
      if (myProject != null && toFocus != null) {
        final ActionCallback result = new ActionCallback();
        requestFocus(toFocus).doWhenProcessed(new Runnable() {
          public void run() {
            removeDeferred(deferredRemove).notifyWhenDone(result);
          }
        });
        return result;
      }
      else {
        requestFocus();
        return removeDeferred(deferredRemove);
      }
    }
    else {
      return removeDeferred(deferredRemove);
    }
  }

  private ActionCallback requestFocus(final JComponent toFocus) {
    if (toFocus == null) return new ActionCallback.Done();

    return myFocusManager.requestFocus(new ActionCallback.Runnable(toFocus) {
      public ActionCallback run() {
        toFocus.requestFocus();
        return new ActionCallback.Done();
      }
    }, true);
  }

  private ActionCallback removeDeferred(final Component deferredRemove) {
    final ActionCallback callback = new ActionCallback();
    if (deferredRemove != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (isForDeferredRemove(deferredRemove)) {
            remove(deferredRemove);
          }
          callback.setDone();
        }
      });
    }
    else {
      callback.setDone();
    }

    return callback;
  }

  private boolean isForDeferredRemove(Component c) {
    if (c instanceof JComponent) {
      if (((JComponent)c).getClientProperty(DEFERRED_REMOVE_FLAG) == null) return false;

      if (mySelectedInfo != null && mySelectedInfo.getComponent() == c) {
        return false;
      }
      else {
        return true;
      }

    }

    return false;
  }

  private void setForDeferredRemove(Component c, boolean toRemove) {
    if (c instanceof JComponent) {
      ((JComponent)c).putClientProperty(DEFERRED_REMOVE_FLAG, toRemove ? Boolean.TRUE : null);
      c.setBounds(0, 0, 0, 0);
      if (toRemove) {
        removeCurrentDeferred();
        setDeferredToRemove(c);
      }
      else if (getDeferredToRemove() != null && getDeferredToRemove() == c) {
        setDeferredToRemove(null);
      }
    }
  }

  private void removeCurrentDeferred() {
    if (getDeferredToRemove() != null) {
      remove(getDeferredToRemove());
      setDeferredToRemove(null);
    }
  }

  @Nullable
  public void propertyChange(final PropertyChangeEvent evt) {
    final TabInfo tabInfo = (TabInfo)evt.getSource();
    if (TabInfo.ACTION_GROUP.equals(evt.getPropertyName())) {
      updateSideComponent(tabInfo);
    }
    else if (TabInfo.TEXT.equals(evt.getPropertyName())) {
      updateText(tabInfo);
    }
    else if (TabInfo.ICON.equals(evt.getPropertyName())) {
      updateIcon(tabInfo);
    }
    else if (TabInfo.ALERT_STATUS.equals(evt.getPropertyName())) {
      boolean start = ((Boolean)evt.getNewValue()).booleanValue();
      updateAttraction(tabInfo, start);
    }
    else if (TabInfo.TAB_ACTION_GROUP.equals(evt.getPropertyName())) {
      updateTabActions(tabInfo);
    }
    else if (TabInfo.HIDDEN.equals(evt.getPropertyName())) {
      updateHiding();
    }

    relayout(false, false);
  }

  private void updateHiding() {
    boolean update = false;

    Iterator<TabInfo> visible = myVisibleInfos.iterator();
    while (visible.hasNext()) {
      TabInfo each = visible.next();
      if (each.isHidden() && !myHiddenInfos.contains(each)) {
        myHiddenInfos.add(each);
        visible.remove();
        update = true;
      }
    }


    Iterator<TabInfo> hidden = myHiddenInfos.iterator();
    while (hidden.hasNext()) {
      TabInfo each = hidden.next();
      if (!each.isHidden() && myHiddenInfos.contains(each)) {
        myVisibleInfos.add(each);
        hidden.remove();
        update = true;
      }
    }


    if (update) {
      myAllTabs = null;
      if (mySelectedInfo != null && myHiddenInfos.contains(mySelectedInfo)) {
        mySelectedInfo = getToSelectOnRemoveOf(mySelectedInfo);
      }
      updateAll(true, false);
    }
  }

  private void updateIcon(final TabInfo tabInfo) {
    myInfo2Label.get(tabInfo).setIcon(tabInfo.getIcon());
    revalidateAndRepaint(false);
  }

  void revalidateAndRepaint(final boolean layoutNow) {
    if (myVisibleInfos.size() == 0) {
      setOpaque(false);
      final Component nonOpaque = UIUtil.findUltimateParent(this);
      if (nonOpaque != null && getParent() != null) {
        final Rectangle toRepaint = SwingUtilities.convertRectangle(getParent(), getBounds(), nonOpaque);
        nonOpaque.repaint(toRepaint.x, toRepaint.y, toRepaint.width, toRepaint.height);
      }
    }
    else {
      setOpaque(true);
    }

    if (layoutNow) {
      validate();
    }
    else {
      revalidate();
    }

    repaint();
  }


  private void updateAttraction(final TabInfo tabInfo, boolean start) {
    if (start) {
      myAttractions.add(tabInfo);
    }
    else {
      myAttractions.remove(tabInfo);
      tabInfo.setBlinkCount(0);
    }

    if (start && !myAnimator.isRunning()) {
      myAnimator.resume();
    }
    else if (!start && myAttractions.size() == 0) {
      myAnimator.suspend();
      repaintAttractions();
    }
  }

  private void updateText(final TabInfo tabInfo) {
    final TabLabel label = myInfo2Label.get(tabInfo);
    label.setText(tabInfo.getColoredText());
    label.setToolTipText(tabInfo.getTooltipText());
    revalidateAndRepaint(false);
  }

  private void updateSideComponent(final TabInfo tabInfo) {
    final JComponent old = myInfo2Toolbar.get(tabInfo);
    if (old != null) {
      remove(old);
    }
    final JComponent toolbar = createToolbarComponent(tabInfo);
    if (toolbar != null) {
      myInfo2Toolbar.put(tabInfo, toolbar);
      add(toolbar);
    }
  }

  private void updateTabActions(final TabInfo info) {
    myInfo2Label.get(info).setTabActions(info.getTabLabelActions());
  }

  @Nullable
  public TabInfo getSelectedInfo() {
    if (!myVisibleInfos.contains(mySelectedInfo)) {
      mySelectedInfo = null;
    }
    return mySelectedInfo != null ? mySelectedInfo : (myVisibleInfos.size() > 0 ? myVisibleInfos.get(0) : null);
  }

  @Nullable
  private TabInfo getToSelectOnRemoveOf(TabInfo info) {
    if (!myVisibleInfos.contains(info)) return null;
    if (mySelectedInfo != info) return null;

    if (myVisibleInfos.size() == 1) return null;

    int index = myVisibleInfos.indexOf(info);
    if (index > 0) return myVisibleInfos.get(index - 1);
    if (index < myVisibleInfos.size() - 1) return myVisibleInfos.get(index + 1);

    return null;
  }

  protected JComponent createToolbarComponent(final TabInfo tabInfo) {
    return new Toolbar(this, tabInfo);
  }

  @NotNull
  public TabInfo getTabAt(final int tabIndex) {
    return getTabs().get(tabIndex);
  }

  @NotNull
  public List<TabInfo> getTabs() {
    if (myAllTabs != null) return myAllTabs;

    ArrayList<TabInfo> result = new ArrayList<TabInfo>();
    result.addAll(myVisibleInfos);
    result.addAll(myHiddenInfos);

    myAllTabs = result;

    return result;
  }

  public TabInfo getTargetInfo() {
    return myPopupInfo != null ? myPopupInfo : getSelectedInfo();
  }

  public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
  }

  public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
    resetPopup();
  }

  public void popupMenuCanceled(final PopupMenuEvent e) {
    resetPopup();
  }

  private void resetPopup() {
//todo [kirillk] dirty hack, should rely on ActionManager to understand that menu item was either chosen on or cancelled
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        myPopupInfo = null;
      }
    });
  }

  public void setPaintBlocked(boolean blocked) {
    if (blocked && !myPaintBlocked) {
      myImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g = myImage.createGraphics();
      super.paint(g);
      g.dispose();
    }

    myPaintBlocked = blocked;

    if (!myPaintBlocked) {
      myImage.flush();
      myImage = null;
      repaint();
    }
  }

  @Nullable
  private Component getDeferredToRemove() {
    return myDeferredToRemove != null ? myDeferredToRemove.get() : null;
  }

  private void setDeferredToRemove(final Component c) {
    myDeferredToRemove = new WeakReference<Component>(c);
  }

  public static class Toolbar extends JPanel {
    private JBTabsImpl myTabs;

    public Toolbar(JBTabsImpl tabs, TabInfo info) {
      myTabs = tabs;

      setLayout(new BorderLayout());

      final ActionGroup group = info.getGroup();
      final JComponent side = info.getSideComponent();

      if (group != null && myTabs.myActionManager != null) {
        final String place = info.getPlace();
        ActionToolbar toolbar = myTabs.myActionManager.createActionToolbar(place != null ? place : ActionPlaces.UNKNOWN, group, myTabs.myHorizontalSide);
        toolbar.setTargetComponent(info.getActionsContextComponent());
        final JComponent actionToolbar = toolbar.getComponent();
        add(actionToolbar, BorderLayout.CENTER);
      }

      if (side != null) {
        if (group != null) {
          add(side, BorderLayout.EAST);
        }
        else {
          add(side, BorderLayout.CENTER);
        }
      }
    }
  }

  class LineLayoutData extends Layout {
    Dimension laayoutSize = getSize();
    int contentCount = getTabCount();
    int eachX;
    int xAddin;
    int requiredWidth;
    int toFitWidth;
    List<TabInfo> toLayout = new ArrayList<TabInfo>();
    List<TabInfo> toDrop = new ArrayList<TabInfo>();
    int moreRectWidth = myMoreIcon.getIconWidth() + 6;
    Rectangle moreRect;
    public boolean displayedHToolbar;
    public int yComp;

    Rectangle leftGhost;
    boolean leftGhostVisible;

    public Rectangle rightGhost;
    boolean rightGhostVisible;

    public Insets insets;

    public TabInfo getPreviousFor(final TabInfo info) {
      return getPrevious(myVisibleInfos, myVisibleInfos.indexOf(info));
    }

    public TabInfo getNextFor(final TabInfo info) {
      return getNext(myVisibleInfos, myVisibleInfos.indexOf(info));
    }
  }

  class TableLayoutData extends Layout {
    List<TableRow> table = new ArrayList<TableRow>();
    public Rectangle toFitRec;
    Map<TabInfo, TableRow> myInfo2Row = new HashMap<TabInfo, TableRow>();

    public TabInfo getPreviousFor(final TabInfo info) {
      final TableRow row = myInfo2Row.get(info);
      return getPrevious(row.myColumns, row.myColumns.indexOf(info));
    }

    public TabInfo getNextFor(final TabInfo info) {
      final TableRow row = myInfo2Row.get(info);
      return getNext(row.myColumns, row.myColumns.indexOf(info));
    }
  }

  private abstract class Layout {
    abstract TabInfo getPreviousFor(TabInfo info);

    abstract TabInfo getNextFor(TabInfo info);

    public TabInfo getPrevious(List<TabInfo> list, int i) {
      return i > 0 ? list.get(i - 1) : null;
    }

    public TabInfo getNext(List<TabInfo> list, int i) {
      return i < list.size() - 1 ? list.get(i + 1) : null;
    }

  }

  private class TableRow {

    private TableLayoutData myData;
    private List<TabInfo> myColumns = new ArrayList<TabInfo>();
    private int width;

    private TableRow(final TableLayoutData data) {
      myData = data;
    }

    void add(TabInfo info) {
      myColumns.add(info);
      width += myInfo2Label.get(info).getPreferredSize().width;
      myData.myInfo2Row.put(info, this);
    }

  }

  public void doLayout() {
    try {
      final Max max = computeMaxSize();
      myHeaderFitSize =
          new Dimension(getSize().width, myHorizontalSide ? Math.max(max.myLabel.height, max.myToolbar.height) : max.myLabel.height);

      if (mySingleRow) {
        myLastLayoutData = layoutSingleRow();
        myLastTableLayout = null;
      }
      else {
        myLastLayoutData = layoutTable();
        myLastSingRowLayout = null;
      }

      if (isStealthModeEffective()) {
        final TabLabel label = myInfo2Label.get(getSelectedInfo());
        final Rectangle bounds = label.getBounds();
        final Insets insets = getLayoutInsets();
        label.setBounds(bounds.x, bounds.y, getWidth() - insets.right - insets.left, bounds.height);
      }

    }
    finally {
      myForcedRelayout = false;
    }
  }

  public void validate() {
    super.validate();
  }

  public void invalidate() {
    super.invalidate();
  }


  public boolean isValid() {
    return super.isValid();
  }

  private Layout layoutTable() {
    resetLayout(true);
    final TableLayoutData data = computeLayoutTable();
    final Insets insets = getLayoutInsets();
    int eachY = insets.top;
    int eachX;
    for (TableRow eachRow : data.table) {
      eachX = insets.left;

      int deltaToFit = 0;
      if (eachRow.width < data.toFitRec.width && data.table.size() > 1) {
        deltaToFit = (int)Math.floor((double)(data.toFitRec.width - eachRow.width) / (double)eachRow.myColumns.size());
      }

      for (int i = 0; i < eachRow.myColumns.size(); i++) {
        TabInfo tabInfo = eachRow.myColumns.get(i);
        final TabLabel label = myInfo2Label.get(tabInfo);

        int width;
        if (i < eachRow.myColumns.size() - 1 || deltaToFit == 0) {
          width = label.getPreferredSize().width + deltaToFit;
        }
        else {
          width = data.toFitRec.width + insets.left - eachX - 1;
        }


        label.setBounds(eachX, eachY, width, myHeaderFitSize.height);
        label.setAligmentToCenter(deltaToFit > 0);

        eachX += width;
      }
      eachY += myHeaderFitSize.height - 1;
    }

    if (getSelectedInfo() != null) {
      final JComponent selectedToolbar = myInfo2Toolbar.get(getSelectedInfo());

      int xAddin = 0;
      if (!myHorizontalSide && selectedToolbar != null) {
        xAddin = selectedToolbar.getPreferredSize().width + 1;
        selectedToolbar
            .setBounds(insets.left + 1, eachY + 1, selectedToolbar.getPreferredSize().width, getHeight() - eachY - insets.bottom - 2);
      }

      layoutComp(xAddin, eachY + 3, getSelectedInfo().getComponent());
    }

    myLastTableLayout = data;
    return data;
  }

  private TableLayoutData computeLayoutTable() {
    final TableLayoutData data = new TableLayoutData();

    final Insets insets = getLayoutInsets();
    data.toFitRec =
        new Rectangle(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
    int eachRow = 0, eachX = data.toFitRec.x;
    TableRow eachTableRow = new TableRow(data);
    data.table.add(eachTableRow);

    int selectedRow = -1;
    for (TabInfo eachInfo : myVisibleInfos) {
      final TabLabel eachLabel = myInfo2Label.get(eachInfo);
      final Dimension size = eachLabel.getPreferredSize();
      if (eachX + size.width <= data.toFitRec.getMaxX()) {
        eachTableRow.add(eachInfo);
        if (getSelectedInfo() == eachInfo) {
          selectedRow = eachRow;
        }
        eachX += size.width;
      }
      else {
        eachTableRow = new TableRow(data);
        data.table.add(eachTableRow);
        eachRow++;
        eachX = insets.left;
        eachTableRow.add(eachInfo);
        if (getSelectedInfo() == eachInfo) {
          selectedRow = eachRow;
        }
        if (eachX + size.width <= data.toFitRec.getMaxX()) {
          eachX += size.width;
        }
        else {
          eachTableRow = new TableRow(data);
          eachRow++;
          eachX = insets.left;
        }
      }
    }

    List<TableRow> toMove = new ArrayList<TableRow>();
    for (int i = selectedRow + 1; i < data.table.size(); i++) {
      toMove.add(data.table.get(i));
    }

    for (TableRow eachMove : toMove) {
      data.table.remove(eachMove);
      data.table.add(0, eachMove);
    }


    return data;
  }

  private Layout layoutSingleRow() {
    final TabInfo selected = getSelectedInfo();

    final JComponent selectedToolbar = myInfo2Toolbar.get(selected);

    LineLayoutData data = new LineLayoutData();
    boolean layoutLabels = true;

    if (!myForcedRelayout &&
        myLastSingRowLayout != null &&
        myLastSingRowLayout.contentCount == getTabCount() &&
        myLastSingRowLayout.laayoutSize.equals(getSize())) {
      for (TabInfo each : myVisibleInfos) {
        final TabLabel eachLabel = myInfo2Label.get(each);
        if (!eachLabel.isValid()) {
          break;
        }
        if (getSelectedInfo() == each) {
          if (eachLabel.getBounds().width != 0) {
            data = myLastSingRowLayout;
            layoutLabels = false;
          }
        }
      }
    }


    data.insets = getLayoutInsets();

    resetLayout(layoutLabels || isHideTabs());


    if (isSideComponentVertical() && selectedToolbar != null) {
      data.xAddin = selectedToolbar.getPreferredSize().width + 1;
    }

    if (layoutLabels && !isHideTabs()) {
      data.eachX = data.insets.left;

      recomputeToLayout(selectedToolbar, data);

      layoutLabelsAndGhosts(data);

      if (data.toDrop.size() > 0) {
        data.moreRect = new Rectangle(getWidth() - data.insets.right - data.moreRectWidth, data.insets.top + getSelectionTabVShift(),
                                      data.moreRectWidth - 1, myHeaderFitSize.height - 1);
      }
    }

    data.yComp = isHideTabs() ? data.insets.top : myHeaderFitSize.height + data.insets.top + (isStealthModeEffective() ? 0 : 1);
    if (selectedToolbar != null) {
      if (!isSideComponentVertical() && !isHideTabs()) {
        int toolbarX = data.eachX + getToolbarInset() + (data.moreRect != null ? data.moreRect.width : 0);
        selectedToolbar.setBounds(toolbarX, data.insets.top, getSize().width - data.insets.left - toolbarX, myHeaderFitSize.height - 1);
      }
      else if (isSideComponentVertical()) {
        selectedToolbar.setBounds(data.insets.left + 1, data.yComp + 1, selectedToolbar.getPreferredSize().width,
                                  getSize().height - data.yComp - data.insets.bottom - 2);
      }
    }


    if (selected != null) {
      final JComponent comp = selected.getComponent();
      layoutComp(data.xAddin, data.yComp, comp);
    }


    if (data.toLayout.size() > 0 && myVisibleInfos.size() > 0) {
      final int left = myVisibleInfos.indexOf(data.toLayout.get(0));
      final int right = myVisibleInfos.indexOf(data.toLayout.get(data.toLayout.size() - 1));
      myMoreIcon.setPaintedIcons(left > 0, right < myVisibleInfos.size() - 1);
    }
    else {
      myMoreIcon.setPaintedIcons(false, false);
    }


    myLastSingRowLayout = data;
    return data;
  }

  private void layoutComp(int xAddin, int yComp, final JComponent comp) {
    final Insets insets = getLayoutInsets();

    final Insets border =
        isHideTabs() ? new Insets(0, 0, 0, 0) : new Insets(myTopBorderSize, myLeftBorderSize, myBottomBorderSize, myRightBorderSize);
    if (isStealthModeEffective() || isHideTabs()) {
      border.top = getBorder(-1);
      border.bottom = getBorder(-1);
      border.left = getBorder(-1);
      border.right = getBorder(-1);
    }

    final Insets inner = getInnerInsets();
    border.top += inner.top;
    border.bottom += inner.bottom;
    border.left += inner.left;
    border.right += inner.right;

    comp.setBounds(insets.left + xAddin + border.left, yComp + border.top,
                   getWidth() - insets.left - insets.right - xAddin - border.left - border.right,
                   getHeight() - insets.bottom - yComp - border.top - border.bottom - 1);
  }


  public JBTabs setInnerInsets(final Insets innerInsets) {
    myInnerInsets = innerInsets;
    return this;
  }

  public Insets getInnerInsets() {
    return myInnerInsets;
  }

  private Insets getLayoutInsets() {
    Insets insets = getInsets();
    if (insets == null) {
      insets = new Insets(0, 0, 0, 0);
    }
    return insets;
  }

  private int fixInset(int inset, int addin) {
    return inset + addin;
  }

  private void layoutLabelsAndGhosts(final LineLayoutData data) {
    int y = data.insets.top;
    boolean reachedBounds = false;


    if (data.leftGhostVisible || isGhostsAlwaysVisible()) {
      data.leftGhost = new Rectangle(data.eachX, y, getGhostTabWidth(), myHeaderFitSize.height);
      myLeftGhost.setBounds(data.leftGhost);
      data.eachX += data.leftGhost.width;
    }

    int deltaToFit = 0;
    if (data.leftGhostVisible || data.rightGhostVisible) {
      if (data.requiredWidth < data.toFitWidth) {
        deltaToFit = (int)Math.floor((data.toFitWidth - data.requiredWidth) / (double)data.toLayout.size());
      }
    }

    int totalWidth = 0;
    int leftX = data.eachX;
    for (TabInfo eachInfo : data.toLayout) {
      final TabLabel label = myInfo2Label.get(eachInfo);
      final Dimension eachSize = label.getPreferredSize();

      boolean isLast = data.toLayout.indexOf(eachInfo) == data.toLayout.size() - 1;

      if (!isLast || deltaToFit == 0) {
        label.setBounds(data.eachX, y, eachSize.width + deltaToFit, myHeaderFitSize.height);
      }
      else {
        int width = data.toFitWidth - totalWidth;
        label.setBounds(data.eachX, y, width, myHeaderFitSize.height);
      }

      label.setAligmentToCenter(deltaToFit > 0);

      data.eachX = (int)label.getBounds().getMaxX();
      data.eachX++;

      totalWidth = (int)(label.getBounds().getMaxX() - leftX);
    }

    for (TabInfo eachInfo : data.toDrop) {
      myInfo2Label.get(eachInfo).setBounds(0, 0, 0, 0);
    }

    if (data.rightGhostVisible || isGhostsAlwaysVisible()) {
      data.rightGhost = new Rectangle(data.eachX, y, getGhostTabWidth(), myHeaderFitSize.height);
      myRightGhost.setBounds(data.rightGhost);
    }
  }

  private void recomputeToLayout(final JComponent selectedToolbar, final LineLayoutData data) {
    final int toolbarInset = getToolbarInset();
    data.displayedHToolbar = myHorizontalSide && selectedToolbar != null;
    data.toFitWidth = getWidth() - data.insets.left - data.insets.right - (data.displayedHToolbar ? toolbarInset : 0);

    if (isGhostsAlwaysVisible()) {
      data.toFitWidth -= getGhostTabWidth() * 2;
    }

    for (TabInfo eachInfo : myVisibleInfos) {
      data.requiredWidth += myInfo2Label.get(eachInfo).getPreferredSize().width;
      data.toLayout.add(eachInfo);
    }

    while (true) {
      if (data.requiredWidth <= data.toFitWidth - data.eachX) break;
      if (data.toLayout.size() == 0) break;

      final TabInfo first = data.toLayout.get(0);
      final TabInfo last = data.toLayout.get(data.toLayout.size() - 1);


      if (myRowDropPolicy == RowDropPolicy.leftFirst) {
        if (first != getSelectedInfo()) {
          processDrop(data, first, true);
        }
        else if (last != getSelectedInfo()) {
          processDrop(data, last, false);
        }
        else {
          break;
        }
      }
      else {
        if (last != getSelectedInfo()) {
          processDrop(data, last, false);
        }
        else if (first != getSelectedInfo()) {
          processDrop(data, first, true);
        }
        else {
          break;
        }
      }
    }

    for (int i = 1; i < myVisibleInfos.size() - 1; i++) {
      final TabInfo each = myVisibleInfos.get(i);
      final TabInfo prev = myVisibleInfos.get(i - 1);
      final TabInfo next = myVisibleInfos.get(i + 1);

      if (data.toLayout.contains(each) && data.toDrop.contains(prev)) {
        myLeftGhost.setInfo(prev);
      }
      else if (data.toLayout.contains(each) && data.toDrop.contains(next)) {
        myRightGhost.setInfo(next);
      }
    }


  }

  private int getToolbarInset() {
    return getArcSize() + 1;
  }

  private void resetLayout(boolean resetLabels) {
    if (resetLabels) {
      myLeftGhost.reset();
      myRightGhost.reset();
    }

    for (TabInfo each : myVisibleInfos) {
      reset(each, resetLabels);
    }

    for (TabInfo each : myHiddenInfos) {
      reset(each, resetLabels);
    }
  }

  private void reset(final TabInfo each, final boolean resetLabels) {
    final JComponent c = each.getComponent();
    if (c != null) {
      c.setBounds(0, 0, 0, 0);
    }

    final JComponent toolbar = myInfo2Toolbar.get(each);
    if (toolbar != null) {
      toolbar.setBounds(0, 0, 0, 0);
    }

    if (resetLabels) {
      myInfo2Label.get(each).setBounds(0, 0, 0, 0);
    }
  }

  private void processDrop(final LineLayoutData data, final TabInfo info, boolean isLeftSide) {
    data.requiredWidth -= myInfo2Label.get(info).getPreferredSize().width;
    data.toDrop.add(info);
    data.toLayout.remove(info);
    if (data.toDrop.size() == 1) {
      data.toFitWidth -= data.moreRectWidth;
    }

    if (!data.leftGhostVisible && isLeftSide) {
      data.leftGhostVisible = true;
      if (!isGhostsAlwaysVisible()) {
        data.toFitWidth -= getGhostTabWidth();
      }
    }
    else if (!data.rightGhostVisible && !isLeftSide) {
      data.rightGhostVisible = true;
      if (!isGhostsAlwaysVisible()) {
        data.toFitWidth -= getGhostTabWidth();
      }
    }
  }

  private int getArcSize() {
    return 4;
  }

  private int getGhostTabWidth() {
    return 15;
  }


  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);

    if (myVisibleInfos.size() == 0) return;

    final GraphicsConfig config = new GraphicsConfig(g);
    config.setAntialiasing(true);

    Graphics2D g2d = (Graphics2D)g;


    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());

    int arc = getArcSize();

    final Color topBlickColor = Color.white;
    final Color rightBlockColor = Color.lightGray;
    final Color boundsColor = Color.gray;

    Insets insets = getLayoutInsets();

    final TabInfo selected = getSelectedInfo();

    final int selectionTabVShift = getSelectionTabVShift();
    int curveArc = 2;

    boolean leftGhostExists = isSingleRow();
    boolean rightGhostExists = isSingleRow();

    if (!isStealthModeEffective() && !isHideTabs()) {
      if (isSingleRow() && myLastSingRowLayout.rightGhostVisible) {
        int topX = myLastSingRowLayout.rightGhost.x - arc;
        int topY = myLastSingRowLayout.rightGhost.y + selectionTabVShift;
        int bottomX = (int)(myLastSingRowLayout.rightGhost.getMaxX() - curveArc);
        int bottomY = (int)myLastSingRowLayout.rightGhost.getMaxY() + 1;

        final GeneralPath path = new GeneralPath();
        path.moveTo(topX, topY);
        path.lineTo(bottomX, topY);
        path.quadTo(bottomX - curveArc, topY + (bottomY - topY) / 4, bottomX, topY + (bottomY - topY) / 2);
        path.quadTo(bottomX + curveArc, bottomY - (bottomY - topY) / 4, bottomX, bottomY);
        path.lineTo(topX, bottomY);

        path.closePath();

        g2d.setColor(getBackground());
        g2d.fill(path);

        g2d.setColor(boundsColor);
        g2d.draw(path);

        g2d.setColor(topBlickColor);
        g2d.drawLine(topX, topY + 1, bottomX - curveArc, topY + 1);
      }

      for (int i = myVisibleInfos.size() - 1; i >= 0; i--) {
        TabInfo each = myVisibleInfos.get(i);
        if (getSelectedInfo() == each) continue;

        final TabLabel eachLabel = myInfo2Label.get(each);
        if (eachLabel.getBounds().width == 0) continue;


        final TabInfo prev = myLastLayoutData.getPreviousFor(myVisibleInfos.get(i));
        final TabInfo next = myLastLayoutData.getNextFor(myVisibleInfos.get(i));

        final Rectangle eachBounds = eachLabel.getBounds();
        final GeneralPath path = new GeneralPath();

        boolean firstShowing = prev == null;
        if (!firstShowing && !leftGhostExists) {
          firstShowing = myInfo2Label.get(prev).getBounds().width == 0;
        }

        boolean lastShowing = next == null;
        if (!lastShowing) {
          lastShowing = myInfo2Label.get(next).getBounds().width == 0;
        }

        boolean leftFromSelection = selected != null && i == myVisibleInfos.indexOf(selected) - 1;


        int leftX = firstShowing ? eachBounds.x : eachBounds.x - arc - 1;
        int topY = eachBounds.y + selectionTabVShift;
        int rigthX = !lastShowing && leftFromSelection ? (int)eachBounds.getMaxX() + arc + 1 : (int)eachBounds.getMaxX();
        int bottomY = (int)eachBounds.getMaxY() + 1;

        path.moveTo(leftX, bottomY);
        path.lineTo(leftX, topY + arc);
        path.quadTo(leftX, topY, leftX + arc, topY);
        path.lineTo(rigthX - arc, topY);
        path.quadTo(rigthX, topY, rigthX, topY + arc);
        path.lineTo(rigthX, bottomY);
        path.closePath();

        g2d.setColor(getBackground());
        g2d.fill(path);

        g.setColor(topBlickColor);
        g.drawLine(leftX, topY + 1, rigthX - arc, topY + 1);

        g.setColor(rightBlockColor);
        g.drawLine(rigthX - 1, topY + arc - 1, rigthX - 1, bottomY);

        g2d.setColor(boundsColor);
        g2d.draw(path);
      }

      if (isSingleRow() && myLastSingRowLayout.leftGhostVisible) {
        final GeneralPath path = new GeneralPath();

        int topX = myLastSingRowLayout.leftGhost.x + curveArc;
        int topY = myLastSingRowLayout.leftGhost.y + selectionTabVShift;
        int bottomX = (int)myLastSingRowLayout.leftGhost.getMaxX() + 1;
        int bottomY = (int)(myLastSingRowLayout.leftGhost.getMaxY() + 1);

        path.moveTo(topX, topY);

        final boolean isLeftFromSelection = myLastSingRowLayout.toLayout.indexOf(getSelectedInfo()) == 0;

        if (isLeftFromSelection) {
          path.lineTo(bottomX, topY);
        }
        else {
          path.lineTo(bottomX - arc, topY);
          path.quadTo(bottomX, topY, bottomX, topY + arc);
        }

        path.lineTo(bottomX, bottomY);
        path.lineTo(topX, bottomY);

        path.quadTo(topX - curveArc * 2 + 1, bottomY - (bottomY - topY) / 4, topX, (bottomY - topY) / 2);

        path.quadTo(topX + curveArc - 1, topY + (bottomY - topY) / 4, topX, topY);

        path.closePath();

        g2d.setColor(getBackground());
        g2d.fill(path);

        g.setColor(boundsColor);
        g2d.draw(path);

        g.setColor(topBlickColor);
        g.drawLine(topX + 1, topY + 1, bottomX - arc, topY + 1);

        g.setColor(rightBlockColor);
        g2d.drawLine(bottomX - 1, topY + arc, bottomX - 1, bottomY - 1);
      }

    }

    if (selected == null) return;


    final TabLabel selectedLabel = myInfo2Label.get(selected);
    if (selectedLabel == null) return;

    Rectangle selectedTabBounds = selectedLabel.getBounds();


    final GeneralPath path = new GeneralPath();
    final int bottomY = (int)selectedTabBounds.getMaxY() + 1;
    final int topY = selectedTabBounds.y;
    int leftX = selectedTabBounds.x;

    int rightX = selectedTabBounds.x + selectedTabBounds.width;

    path.moveTo(insets.left, bottomY);
    path.lineTo(leftX, bottomY);
    path.lineTo(leftX, topY + arc);
    path.quadTo(leftX, topY, leftX + arc, topY);

    int lastX = getWidth() - insets.right - 1;

    if (isStealthModeEffective()) {
      path.lineTo(lastX - arc, topY);
      path.quadTo(lastX, topY, lastX, topY + arc);
      path.lineTo(lastX, bottomY);
    }
    else {
      path.lineTo(rightX - arc, topY);
      path.quadTo(rightX, topY, rightX, topY + arc);
      path.lineTo(rightX, bottomY - arc);
      path.quadTo(rightX, bottomY, rightX + arc, bottomY);
    }

    path.lineTo(lastX, bottomY);

    if (isStealthModeEffective()) {
      path.closePath();
    }

    final GeneralPath fillPath = (GeneralPath)path.clone();
    if (!isHideTabs()) {
      fillPath.lineTo(lastX, bottomY + 1);
      fillPath.lineTo(leftX, bottomY + 1);
      fillPath.closePath();
      g2d.setColor(getBackground());
      g2d.fill(fillPath);
    }


    final Color from;
    final Color to;
    final int alpha;
    final boolean paintFocused = myPaintFocus && (myFocused || myActivePopup != null);
    if (paintFocused) {
      from = UIUtil.getFocusedFillColor();
      to = UIUtil.getFocusedFillColor();
    }
    else {
      if (isPaintFocus()) {
        alpha = 150;
        from = UIUtil.toAlpha(UIUtil.getPanelBackgound().brighter(), alpha);
        to = UIUtil.toAlpha(UIUtil.getPanelBackgound(), alpha);
      }
      else {
        alpha = 255;
        from = UIUtil.toAlpha(Color.white, alpha);
        to = UIUtil.toAlpha(Color.white, alpha);
      }
    }


    g2d.setPaint(new GradientPaint(selectedTabBounds.x, topY, from, selectedTabBounds.x, bottomY, to));

    if (!isHideTabs()) {
      g2d.fill(fillPath);
    }

    Color borderColor = UIUtil.getBoundsColor(paintFocused);
    g2d.setColor(borderColor);

    if (!isHideTabs()) {
      g2d.draw(path);
    }

    if (isHideTabs()) {
      paintBorder(g2d, insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.bottom - insets.top,
                  borderColor, from, to, paintFocused);
    }
    else {
      paintBorder(g2d, insets.left, bottomY, getWidth() - insets.left - insets.right, getHeight() - bottomY - insets.bottom, borderColor,
                  from, to, paintFocused);
    }

    config.setAntialiasing(false);
    if (isSideComponentVertical()) {
      JComponent toolbarComp = myInfo2Toolbar.get(mySelectedInfo);
      if (toolbarComp != null) {
        Rectangle toolBounds = toolbarComp.getBounds();
        g2d.setColor(CaptionPanel.CNT_ACTIVE_COLOR);
        g.drawLine((int)toolBounds.getMaxX(), toolBounds.y, (int)toolBounds.getMaxX(), (int)toolBounds.getMaxY() - 1);
      }
    }

    config.restore();
  }

  private int getSelectionTabVShift() {
    return 2;
  }

  private void paintBorder(Graphics2D g2d,
                           int x,
                           int y,
                           int width,
                           int height,
                           final Color borderColor,
                           final Color fillFrom,
                           final Color fillTo,
                           boolean isFocused) {
    int topY = y + 1;
    int bottomY = y + myTopBorderSize - 2;
    int middleY = topY + (bottomY - topY) / 2;


    if (myTopBorderSize > 0) {
      if (isHideTabs()) {
        g2d.setColor(borderColor);
        g2d.drawLine(x, y, x + width - 1, y);
      }
      else if (isStealthModeEffective()) {
        g2d.setColor(borderColor);
        g2d.drawLine(x, y - 1, x + width - 1, y - 1);
      }
      else {
        if (myTopBorderSize > 1) {
          g2d.setColor(Color.white);
          g2d.fillRect(x, topY, width, bottomY - topY);

          g2d.setColor(fillTo);
          g2d.fillRect(x, topY, width, middleY - topY);

          final Color relfectionStartColor =
              isFocused ? UIUtil.toAlpha(UIUtil.getListSelectionBackground().darker(), 125) : UIUtil.toAlpha(borderColor, 75);
          g2d.setPaint(new GradientPaint(x, middleY, relfectionStartColor, x, bottomY, UIUtil.toAlpha(Color.white, 255)));
          g2d.fillRect(x, middleY, width, bottomY - middleY);

          g2d.setColor(UIUtil.toAlpha(Color.white, 100));
          g2d.drawLine(x, topY, x + width - 1, topY);


          g2d.setColor(Color.lightGray);
          g2d.drawLine(x, bottomY, x + width - 1, bottomY);
        }
        else if (myTopBorderSize == 1) {
          g2d.setColor(borderColor);
          g2d.drawLine(x, y, x + width - 1, y);
        }
      }
    }

    g2d.setColor(borderColor);
    g2d.fillRect(x, y + height - myBottomBorderSize, width, myBottomBorderSize);

    g2d.fillRect(x, y, myLeftBorderSize, height);
    g2d.fillRect(x + width - myRightBorderSize, y, myRightBorderSize, height);
  }

  private boolean isStealthModeEffective() {
    return myStealthTabMode && getTabCount() == 1 && isSideComponentVertical();
  }


  private boolean isNavigationVisible() {
    if (myStealthTabMode && getTabCount() == 1) return false;
    return myVisibleInfos.size() > 0;
  }


  public void paint(final Graphics g) {
    if (myPaintBlocked) {
      g.drawImage(myImage, 0, 0, getWidth(), getHeight(), null);
      return;
    }

    super.paint(g);
  }

  protected void paintChildren(final Graphics g) {
    super.paintChildren(g);

    //if (isSingleRow() && myLastSingRowLayout != null) {
    //  final List<TabInfo> infos = myLastSingRowLayout.toLayout;
    //  for (int i = 1; i < infos.size(); i++) {
    //    final TabInfo each = infos.get(i);
    //    if (getSelectedInfo() != each && getSelectedInfo() != infos.get(i - 1)) {
    //      drawSeparator(g, each);
    //    }
    //  }
    //}
    //else if (!isSingleRow() && myLastTableLayout != null) {
    //  final List<TableRow> table = myLastTableLayout.table;
    //  for (TableRow eachRow : table) {
    //    final List<TabInfo> infos = eachRow.myColumns;
    //    for (int i = 1; i < infos.size(); i++) {
    //      final TabInfo each = infos.get(i);
    //      if (getSelectedInfo() != each && getSelectedInfo() != infos.get(i - 1)) {
    //        drawSeparator(g, each);
    //      }
    //    }
    //  }
    //}

    myMoreIcon.paintIcon(this, g);
  }

  private void drawSeparator(Graphics g, TabInfo info) {
    final TabLabel label = myInfo2Label.get(info);
    if (label == null) return;
    final Rectangle bounds = label.getBounds();

    final double height = bounds.height * 0.85d;
    final double delta = bounds.height - height;

    final int y1 = (int)(bounds.y + delta) + 1;
    final int x1 = bounds.x;
    final int y2 = (int)(bounds.y + bounds.height - delta);
    UIUtil.drawVDottedLine((Graphics2D)g, x1, y1, y2, getBackground(), Color.gray);
  }

  private Max computeMaxSize() {
    Max max = new Max();
    for (TabInfo eachInfo : myVisibleInfos) {
      final TabLabel label = myInfo2Label.get(eachInfo);
      max.myLabel.height = Math.max(max.myLabel.height, label.getPreferredSize().height);
      max.myLabel.width = Math.max(max.myLabel.width, label.getPreferredSize().width);
      final JComponent toolbar = myInfo2Toolbar.get(eachInfo);
      if (toolbar != null) {
        max.myToolbar.height = Math.max(max.myToolbar.height, toolbar.getPreferredSize().height);
        max.myToolbar.width = Math.max(max.myToolbar.width, toolbar.getPreferredSize().width);
      }
    }

    max.myToolbar.height++;

    return max;
  }

  public int getTabCount() {
    return getTabs().size();
  }

  public ActionCallback removeTab(final JComponent component) {
    return removeTab(findInfo(component));
  }

  public ActionCallback removeTab(final TabInfo info) {
    return removeTab(info, true);
  }

  public ActionCallback removeTab(final TabInfo info, boolean transferFocus) {
    if (info == null || !getTabs().contains(info)) return new ActionCallback.Done();

    final ActionCallback result = new ActionCallback();

    TabInfo toSelect = transferFocus ? getToSelectOnRemoveOf(info) : null;


    if (toSelect != null) {
      final JComponent deferred = processRemove(info, false);
      _setSelected(toSelect, true).doWhenProcessed(new Runnable() {
        public void run() {
          removeDeferred(deferred);
        }
      }).notifyWhenDone(result);
    }
    else {
      removeDeferred(processRemove(info, true)).notifyWhenDone(result);
    }

    if (myVisibleInfos.size() == 0) {
      removeCurrentDeferred();
    }

    revalidateAndRepaint(true);

    return result;
  }

  @Nullable
  private JComponent processRemove(final TabInfo info, boolean forcedNow) {
    remove(myInfo2Label.get(info));
    final JComponent tb = myInfo2Toolbar.get(info);
    if (tb != null) {
      remove(tb);
    }

    JComponent tabComponent = info.getComponent();

    if (!isFocused(tabComponent) || forcedNow) {
      remove(tabComponent);
      tabComponent = null;
    }
    else {
      setForDeferredRemove(tabComponent, true);
    }

    myVisibleInfos.remove(info);
    myHiddenInfos.remove(info);
    myInfo2Label.remove(info);
    myInfo2Toolbar.remove(info);
    myAllTabs = null;

    updateAll(false, false);

    return tabComponent;
  }

  public TabInfo findInfo(Component component) {
    for (TabInfo each : getTabs()) {
      if (each.getComponent() == component) return each;
    }

    return null;
  }

  public TabInfo findInfo(String text) {
    if (text == null) return null;

    for (TabInfo each : getTabs()) {
      if (text.equals(each.getText())) return each;
    }

    return null;
  }

  public TabInfo findInfo(MouseEvent event) {
    final Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), this);
    return _findInfo(point, false);
  }

  public TabInfo findInfo(final Object object) {
    for (int i = 0; i < getTabCount(); i++) {
      final TabInfo each = getTabAt(i);
      final Object eachObject = each.getObject();
      if (eachObject != null && eachObject.equals(object)) return each;
    }
    return null;
  }

  public TabInfo findTabLabelBy(final Point point) {
    return _findInfo(point, true);
  }

  private TabInfo _findInfo(final Point point, boolean labelsOnly) {
    Component component = findComponentAt(point);
    if (component == null) return null;
    while (component != this || component != null) {
      if (component instanceof TabLabel) {
        return ((TabLabel)component).getInfo();
      }
      else if (!labelsOnly) {
        final TabInfo info = findInfo(component);
        if (info != null) return info;
      }
      if (component == null) break;
      component = component.getParent();
    }

    return null;
  }

  public void removeAllTabs() {
    for (TabInfo each : getTabs()) {
      removeTab(each);
    }
  }


  private class Max {
    Dimension myLabel = new Dimension();
    Dimension myToolbar = new Dimension();
  }

  @Nullable
  private Component updateContainer(boolean forced, final boolean layoutNow) {
    Component deferredRemove = null;

    for (TabInfo each : myVisibleInfos) {
      final JComponent eachComponent = each.getComponent();
      if (getSelectedInfo() == each && getSelectedInfo() != null) {
        final Container parent = eachComponent.getParent();
        if (parent != null && parent != this) {
          parent.remove(eachComponent);
        }

        if (eachComponent.getParent() == null) {
          add(eachComponent);
        }
      }
      else {
        if (eachComponent.getParent() == null) continue;
        if (isFocused(eachComponent)) {
          deferredRemove = eachComponent;
        }
        else {
          remove(eachComponent);
        }
      }
    }

    if (deferredRemove != null) {
      setForDeferredRemove(deferredRemove, true);
    }


    relayout(forced, layoutNow);

    return deferredRemove;
  }

  protected void addImpl(final Component comp, final Object constraints, final int index) {
    setForDeferredRemove(comp, false);

    if (comp instanceof TabLabel) {
      ((TabLabel)comp).apply(myUiDecorator.getDecoration());
    }

    super.addImpl(comp, constraints, index);
  }

  private boolean isFocused(JComponent c) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return focusOwner != null && (focusOwner == c || SwingUtilities.isDescendingFrom(focusOwner, c));
  }

  private void relayout(boolean forced, final boolean layoutNow) {
    if (!myForcedRelayout) {
      myForcedRelayout = forced;
    }
    revalidateAndRepaint(layoutNow);
  }

  ActionManager getActionManager() {
    return myActionManager;
  }

  @NotNull
  public JBTabs addTabMouseListener(@NotNull MouseListener listener) {
    removeListeners();
    myTabMouseListeners.add(listener);
    addListeners();
    return this;
  }

  @NotNull
  public JComponent getComponent() {
    return this;
  }

  @NotNull
  public JBTabs removeTabMouseListener(@NotNull MouseListener listener) {
    removeListeners();
    myTabMouseListeners.remove(listener);
    addListeners();
    return this;
  }

  private void addListeners() {
    for (TabInfo eachInfo : myVisibleInfos) {
      final TabLabel label = myInfo2Label.get(eachInfo);
      for (MouseListener eachListener : myTabMouseListeners) {
        label.addMouseListener(eachListener);
      }
    }
  }

  private void removeListeners() {
    for (TabInfo eachInfo : myVisibleInfos) {
      final TabLabel label = myInfo2Label.get(eachInfo);
      for (MouseListener eachListener : myTabMouseListeners) {
        label.removeMouseListener(eachListener);
      }
    }
  }

  private void updateListeners() {
    removeListeners();
    addListeners();
  }

  public JBTabs addListener(@NotNull TabsListener listener) {
    myTabListeners.add(listener);
    return this;
  }

  private class GhostComponent extends JLabel {
    private TabInfo myInfo;

    private GhostComponent(final RowDropPolicy before, final RowDropPolicy after) {
      addMouseListener(new MouseAdapter() {
        public void mousePressed(final MouseEvent e) {
          if (isSelectionClick(e, true) && myInfo != null) {
            myRowDropPolicy = before;
            select(myInfo, true).doWhenDone(new Runnable() {
              public void run() {
                myRowDropPolicy = after;
              }
            });
          }
        }
      });
    }

    public void setInfo(final TabInfo info) {
      myInfo = info;
      setToolTipText(info != null ? info.getTooltipText() : null);
    }

    public void reset() {
      setBounds(0, 0, 0, 0);
      setInfo(null);
    }
  }

  protected void onPopup(final TabInfo popupInfo) {
  }

  public void setFocused(final boolean focused) {
    myFocused = focused;
    repaint();
  }

  public int getIndexOf(@Nullable final TabInfo tabInfo) {
    return myVisibleInfos.indexOf(tabInfo);
  }

  public boolean isHideTabs() {
    return myHideTabs;
  }

  public void setHideTabs(final boolean hideTabs) {
    if (isHideTabs() == hideTabs) return;

    myHideTabs = hideTabs;

    relayout(true, false);
  }

  public JBTabs setPaintBorder(int top, int left, int right, int bottom) {
    if (myTopBorderSize == top && myLeftBorderSize == left && myRightBorderSize == right && myBottomBorderSize == bottom) return this;

    myTopBorderSize = getBorder(top);
    myLeftBorderSize = getBorder(left);
    myRightBorderSize = getBorder(right);
    myBottomBorderSize = getBorder(bottom);

    revalidateAndRepaint(false);

    return this;
  }

  private static int getBorder(int size) {
    return size == -1 ? 1 : size;
  }

  public boolean isPaintFocus() {
    return myPaintFocus;
  }

  @NotNull
  public JBTabs setAdjustBorders(final boolean adjust) {
    myAdjustBorders = adjust;
    return this;
  }

  public static void removeScrollBorder(final Component c) {
    new AwtVisitor(c) {
      public boolean visit(final Component component) {
        if (component instanceof JScrollPane) {
          if (!hasNonPrimitiveParents(c, component)) {
            ((JScrollPane)component).setBorder(null);
          }
        }
        return false;
      }
    };
  }

  private static boolean hasNonPrimitiveParents(Component stopParent, Component c) {
    Component eachParent = c.getParent();
    while (true) {
      if (eachParent == null || eachParent == stopParent) return false;
      if (!isPrimitive(eachParent)) return true;
      eachParent = eachParent.getParent();
    }
  }


  private static boolean isPrimitive(Component c) {
    return c instanceof JPanel;
  }


  public void setPaintFocus(final boolean paintFocus) {
    myPaintFocus = paintFocus;
  }

  private static abstract class BaseNavigationAction extends AnAction {

    private ShadowAction myShadow;

    protected BaseNavigationAction(final String copyFromID, JComponent c) {
      myShadow = new ShadowAction(this, ActionManager.getInstance().getAction(copyFromID), c);
    }

    public final void update(final AnActionEvent e) {
      JBTabsImpl tabs = e.getData(NAVIGATION_ACTIONS_KEY);
      e.getPresentation().setVisible(tabs != null);
      if (tabs == null) return;

      final int selectedIndex = tabs.myVisibleInfos.indexOf(tabs.getSelectedInfo());
      final boolean enabled = tabs.myVisibleInfos.size() > 0 && selectedIndex >= 0;
      e.getPresentation().setEnabled(enabled);
      if (enabled) {
        _update(e, tabs, selectedIndex);
      }
    }

    protected abstract void _update(AnActionEvent e, final JBTabsImpl tabs, int selectedIndex);

    public final void actionPerformed(final AnActionEvent e) {
      JBTabsImpl tabs = e.getData(NAVIGATION_ACTIONS_KEY);
      if (tabs == null) return;

      final int index = tabs.myVisibleInfos.indexOf(tabs.getSelectedInfo());
      if (index == -1) return;
      _actionPerformed(e, tabs, index);
    }

    protected abstract void _actionPerformed(final AnActionEvent e, final JBTabsImpl tabs, final int selectedIndex);
  }

  private static class SelectNextAction extends BaseNavigationAction {

    public SelectNextAction(JComponent c) {
      super(IdeActions.ACTION_NEXT_TAB, c);
    }

    protected void _update(final AnActionEvent e, final JBTabsImpl tabs, int selectedIndex) {
      e.getPresentation().setEnabled(tabs.myVisibleInfos.size() > 0 && selectedIndex < tabs.myVisibleInfos.size() - 1);
    }

    protected void _actionPerformed(final AnActionEvent e, final JBTabsImpl tabs, final int selectedIndex) {
      tabs.select(tabs.myVisibleInfos.get(selectedIndex + 1), true);
    }
  }

  private static class SelectPreviousAction extends BaseNavigationAction {
    public SelectPreviousAction(JComponent c) {
      super(IdeActions.ACTION_PREVIOUS_TAB, c);
    }

    protected void _update(final AnActionEvent e, final JBTabsImpl tabs, int selectedIndex) {
      e.getPresentation().setEnabled(tabs.myVisibleInfos.size() > 0 && selectedIndex > 0);
    }

    protected void _actionPerformed(final AnActionEvent e, final JBTabsImpl tabs, final int selectedIndex) {
      tabs.select(tabs.myVisibleInfos.get(selectedIndex - 1), true);
    }
  }

  private void disposePopupListener() {
    if (myActivePopup != null) {
      myActivePopup.removePopupMenuListener(myPopupListener);
      myActivePopup = null;
    }
  }

  public void setStealthTabMode(final boolean stealthTabMode) {
    myStealthTabMode = stealthTabMode;

    relayout(true, false);
  }

  public boolean isStealthTabMode() {
    return myStealthTabMode;
  }

  public void setSideComponentVertical(final boolean vertical) {
    myHorizontalSide = !vertical;

    for (TabInfo each : myVisibleInfos) {
      each.getChangeSupport().firePropertyChange(TabInfo.ACTION_GROUP, "new1", "new2");
    }


    relayout(true, false);
  }

  public void setSingleRow(boolean singleRow) {
    mySingleRow = singleRow;

    relayout(true, false);
  }

  public JBTabs setGhostsAlwaysVisible(final boolean visible) {
    myGhostsAlwaysVisible = visible;

    relayout(true, false);

    return this;
  }

  public boolean isGhostsAlwaysVisible() {
    return myGhostsAlwaysVisible;
  }

  public boolean isSingleRow() {
    return mySingleRow;
  }

  public boolean isSideComponentVertical() {
    return !myHorizontalSide;
  }

  public JBTabs setUiDecorator(UiDecorator decorator) {
    myUiDecorator = decorator == null ? ourDefaultDecorator : decorator;
    applyDecoration();
    return this;
  }

  protected void setUI(final ComponentUI newUI) {
    super.setUI(newUI);
    applyDecoration();
  }

  public void updateUI() {
    super.updateUI();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        applyDecoration();

        revalidateAndRepaint(false);
      }
    });
  }

  private void applyDecoration() {
    if (myUiDecorator != null) {
      UiDecoration uiDecoration = myUiDecorator.getDecoration();
      for (TabLabel each : myInfo2Label.values()) {
        each.apply(uiDecoration);
      }
    }


    for (TabInfo each : getTabs()) {
      adjust(each);
    }

    relayout(true, false);
  }

  private void adjust(final TabInfo each) {
    if (myAdjustBorders) {
      removeScrollBorder(each.getComponent());
    }
  }

  public void sortTabs(Comparator<TabInfo> comparator) {
    Collections.sort(myVisibleInfos, comparator);

    relayout(true, false);
  }

  public boolean isRequestFocusOnLastFocusedComponent() {
    return myRequestFocusOnLastFocusedComponent;
  }

  public void setRequestFocusOnLastFocusedComponent(final boolean requestFocusOnLastFocusedComponent) {
    myRequestFocusOnLastFocusedComponent = requestFocusOnLastFocusedComponent;
  }


  @Nullable
  public Object getData(@NonNls final String dataId) {
    if (myDataProvider != null) {
      final Object value = myDataProvider.getData(dataId);
      if (value != null) return value;
    }

    if (!NAVIGATION_ACTIONS_KEY.getName().equals(dataId)) return null;
    return isNavigationVisible() ? this : null;
  }


  public DataProvider getDataProvider() {
    return myDataProvider;
  }

  public JBTabsImpl setDataProvider(@NotNull final DataProvider dataProvider) {
    myDataProvider = dataProvider;
    return this;
  }


  boolean isSelectionClick(final MouseEvent e, boolean canBeQuick) {
    return (e.getClickCount() == 1 || canBeQuick) && e.getButton() == MouseEvent.BUTTON1 && !e.isPopupTrigger();
  }


  private static class DefautDecorator implements UiDecorator {
    @NotNull
    public UiDecoration getDecoration() {
      return new UiDecoration(null, new Insets(2, 8, 2, 8));
    }
  }
}