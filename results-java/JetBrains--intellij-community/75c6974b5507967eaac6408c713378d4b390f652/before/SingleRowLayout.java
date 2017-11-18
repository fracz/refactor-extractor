package com.intellij.ui.tabs.impl.singleRow;

import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.ui.tabs.impl.TabLabel;
import com.intellij.ui.tabs.impl.Layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SingleRowLayout {

  JBTabsImpl myTabs;
  public LineLayoutData myLastSingRowLayout;

  public MoreIcon myMoreIcon = new MoreIcon() {
    protected boolean isActive() {
      return myTabs.myFocused;
    }

    protected Rectangle getIconRec() {
      return myLastSingRowLayout != null ? myLastSingRowLayout.moreRect : null;
    }
  };
  public JPopupMenu myMorePopup;


  public GhostComponent myLeftGhost = new GhostComponent(RowDropPolicy.leftFirst, RowDropPolicy.leftFirst);
  public GhostComponent myRightGhost = new GhostComponent(RowDropPolicy.rightFirst, RowDropPolicy.leftFirst);

  private enum RowDropPolicy {
    leftFirst, rightFirst
  }

  private RowDropPolicy myRowDropPolicy = RowDropPolicy.leftFirst;


  public SingleRowLayout(final JBTabsImpl tabs) {
    myTabs = tabs;
  }

  public Layout layoutSingleRow() {
    final TabInfo selected = myTabs.getSelectedInfo();

    final JComponent selectedToolbar = myTabs.myInfo2Toolbar.get(selected);

    LineLayoutData data = new LineLayoutData(this);
    boolean layoutLabels = true;

    if (!myTabs.myForcedRelayout &&
        myLastSingRowLayout != null &&
        myLastSingRowLayout.contentCount == myTabs.getTabCount() &&
        myLastSingRowLayout.laayoutSize.equals(myTabs.getSize())) {
      for (TabInfo each : myTabs.myVisibleInfos) {
        final TabLabel eachLabel = myTabs.myInfo2Label.get(each);
        if (!eachLabel.isValid()) {
          break;
        }
        if (myTabs.getSelectedInfo() == each) {
          if (eachLabel.getBounds().width != 0) {
            data = myLastSingRowLayout;
            layoutLabels = false;
          }
        }
      }
    }


    data.insets = myTabs.getLayoutInsets();

    myTabs.resetLayout(layoutLabels || myTabs.isHideTabs());


    if (myTabs.isSideComponentVertical() && selectedToolbar != null) {
      data.xAddin = selectedToolbar.getPreferredSize().width + 1;
    }

    if (layoutLabels && !myTabs.isHideTabs()) {
      data.eachX = data.insets.left;

      recomputeToLayout(selectedToolbar, data);

      layoutLabelsAndGhosts(data);

      if (data.toDrop.size() > 0) {
        data.moreRect = new Rectangle(myTabs.getWidth() - data.insets.right - data.moreRectWidth, data.insets.top + myTabs.getSelectionTabVShift(),
                                      data.moreRectWidth - 1, myTabs.myHeaderFitSize.height - 1);
      }
    }

    data.yComp = myTabs.isHideTabs() ? data.insets.top : myTabs.myHeaderFitSize.height + data.insets.top + (myTabs.isStealthModeEffective() ? 0 : 1);
    if (selectedToolbar != null) {
      if (!myTabs.isSideComponentVertical() && !myTabs.isHideTabs()) {
        int toolbarX = data.eachX + myTabs.getToolbarInset() + (data.moreRect != null ? data.moreRect.width : 0);
        selectedToolbar.setBounds(toolbarX, data.insets.top, myTabs.getSize().width - data.insets.left - toolbarX, myTabs.myHeaderFitSize.height - 1);
      }
      else if (myTabs.isSideComponentVertical()) {
        selectedToolbar.setBounds(data.insets.left + 1, data.yComp + 1, selectedToolbar.getPreferredSize().width,
                                  myTabs.getSize().height - data.yComp - data.insets.bottom - 2);
      }
    }


    if (selected != null) {
      final JComponent comp = selected.getComponent();
      myTabs.layoutComp(data.xAddin, data.yComp, comp);
    }


    if (data.toLayout.size() > 0 && myTabs.myVisibleInfos.size() > 0) {
      final int left = myTabs.myVisibleInfos.indexOf(data.toLayout.get(0));
      final int right = myTabs.myVisibleInfos.indexOf(data.toLayout.get(data.toLayout.size() - 1));
      myMoreIcon.setPaintedIcons(left > 0, right < myTabs.myVisibleInfos.size() - 1);
    }
    else {
      myMoreIcon.setPaintedIcons(false, false);
    }


    myLastSingRowLayout = data;
    return data;
  }

  private void layoutLabelsAndGhosts(final LineLayoutData data) {
    int y = data.insets.top;
    boolean reachedBounds = false;


    if (data.leftGhostVisible || myTabs.isGhostsAlwaysVisible()) {
      data.leftGhost = new Rectangle(data.eachX, y, myTabs.getGhostTabWidth(), myTabs.myHeaderFitSize.height);
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
      final TabLabel label = myTabs.myInfo2Label.get(eachInfo);
      final Dimension eachSize = label.getPreferredSize();

      boolean isLast = data.toLayout.indexOf(eachInfo) == data.toLayout.size() - 1;

      if (!isLast || deltaToFit == 0) {
        label.setBounds(data.eachX, y, eachSize.width + deltaToFit, myTabs.myHeaderFitSize.height);
      }
      else {
        int width = data.toFitWidth - totalWidth;
        label.setBounds(data.eachX, y, width, myTabs.myHeaderFitSize.height);
      }

      label.setAligmentToCenter(deltaToFit > 0);

      data.eachX = (int)label.getBounds().getMaxX();
      data.eachX++;

      totalWidth = (int)(label.getBounds().getMaxX() - leftX);
    }

    for (TabInfo eachInfo : data.toDrop) {
      myTabs.myInfo2Label.get(eachInfo).setBounds(0, 0, 0, 0);
    }

    if (data.rightGhostVisible || myTabs.isGhostsAlwaysVisible()) {
      data.rightGhost = new Rectangle(data.eachX, y, myTabs.getGhostTabWidth(), myTabs.myHeaderFitSize.height);
      myRightGhost.setBounds(data.rightGhost);
    }
  }


  private void recomputeToLayout(final JComponent selectedToolbar, final LineLayoutData data) {
    final int toolbarInset = myTabs.getToolbarInset();
    data.displayedHToolbar = myTabs.myHorizontalSide && selectedToolbar != null;
    data.toFitWidth = myTabs.getWidth() - data.insets.left - data.insets.right - (data.displayedHToolbar ? toolbarInset : 0);

    if (myTabs.isGhostsAlwaysVisible()) {
      data.toFitWidth -= myTabs.getGhostTabWidth() * 2;
    }

    for (TabInfo eachInfo : myTabs.myVisibleInfos) {
      data.requiredWidth += myTabs.myInfo2Label.get(eachInfo).getPreferredSize().width;
      data.toLayout.add(eachInfo);
    }

    while (true) {
      if (data.requiredWidth <= data.toFitWidth - data.eachX) break;
      if (data.toLayout.size() == 0) break;

      final TabInfo first = data.toLayout.get(0);
      final TabInfo last = data.toLayout.get(data.toLayout.size() - 1);


      if (myRowDropPolicy == RowDropPolicy.leftFirst) {
        if (first != myTabs.getSelectedInfo()) {
          processDrop(data, first, true);
        }
        else if (last != myTabs.getSelectedInfo()) {
          processDrop(data, last, false);
        }
        else {
          break;
        }
      }
      else {
        if (last != myTabs.getSelectedInfo()) {
          processDrop(data, last, false);
        }
        else if (first != myTabs.getSelectedInfo()) {
          processDrop(data, first, true);
        }
        else {
          break;
        }
      }
    }

    for (int i = 1; i < myTabs.myVisibleInfos.size() - 1; i++) {
      final TabInfo each = myTabs.myVisibleInfos.get(i);
      final TabInfo prev = myTabs.myVisibleInfos.get(i - 1);
      final TabInfo next = myTabs.myVisibleInfos.get(i + 1);

      if (data.toLayout.contains(each) && data.toDrop.contains(prev)) {
        myLeftGhost.setInfo(prev);
      }
      else if (data.toLayout.contains(each) && data.toDrop.contains(next)) {
        myRightGhost.setInfo(next);
      }
    }


  }


  public class GhostComponent extends JLabel {
    private TabInfo myInfo;

    private GhostComponent(final RowDropPolicy before, final RowDropPolicy after) {
      addMouseListener(new MouseAdapter() {
        public void mousePressed(final MouseEvent e) {
          if (myTabs.isSelectionClick(e, true) && myInfo != null) {
            myRowDropPolicy = before;
            myTabs.select(myInfo, true).doWhenDone(new Runnable() {
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

  private void processDrop(final LineLayoutData data, final TabInfo info, boolean isLeftSide) {
    data.requiredWidth -= myTabs.myInfo2Label.get(info).getPreferredSize().width;
    data.toDrop.add(info);
    data.toLayout.remove(info);
    if (data.toDrop.size() == 1) {
      data.toFitWidth -= data.moreRectWidth;
    }

    if (!data.leftGhostVisible && isLeftSide) {
      data.leftGhostVisible = true;
      if (!myTabs.isGhostsAlwaysVisible()) {
        data.toFitWidth -= myTabs.getGhostTabWidth();
      }
    }
    else if (!data.rightGhostVisible && !isLeftSide) {
      data.rightGhostVisible = true;
      if (!myTabs.isGhostsAlwaysVisible()) {
        data.toFitWidth -= myTabs.getGhostTabWidth();
      }
    }
  }

}