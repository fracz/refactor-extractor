package com.intellij.ui.tabs.impl;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.util.ui.Centerizer;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.ide.DataManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TabLabel extends JPanel {
  private SimpleColoredComponent myLabel = new SimpleColoredComponent();
  private LayeredIcon myIcon;
  private TabInfo myInfo;
  private ActionPanel myActionPanel;
  private boolean myCentered;

  private Wrapper myLabelPlaceholder = new Wrapper();
  private JBTabsImpl myTabs;

  public TabLabel(JBTabsImpl tabs, final TabInfo info) {
    myTabs = tabs;
    myInfo = info;
    myLabel.setOpaque(false);
    myLabel.setIconTextGap(new JLabel().getIconTextGap());
    myLabel.setIconOpaque(false);
    setOpaque(false);
    setLayout(new BorderLayout(myLabel.getIconTextGap() * 2, 0));

    myLabelPlaceholder.setOpaque(false);
    add(myLabelPlaceholder, BorderLayout.CENTER);

    setAligmentToCenter(false);

    myIcon = new LayeredIcon(2);
    myLabel.setIcon(myIcon);

    addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent e) {
        if (myTabs.isSelectionClick(e, false)) {
          myTabs.select(info, true);
        }
        else {
          handlePopup(e);
        }
      }

      public void mouseClicked(final MouseEvent e) {
        handlePopup(e);
      }

      public void mouseReleased(final MouseEvent e) {
        handlePopup(e);
      }
    });

    myLabel.setBorder(new LineBorder(Color.red));
  }

  public void setAligmentToCenter(boolean toCenter) {
    if (myCentered == toCenter && myLabel.getParent() != null) return;

    myLabelPlaceholder.removeAll();

    if (toCenter) {
      final Centerizer center = new Centerizer(myLabel);
      myLabelPlaceholder.setContent(center);
    } else {
      myLabelPlaceholder.setContent(myLabel);
    }

    myCentered = toCenter;
  }

  public void paint(final Graphics g) {
    if (myTabs.getSelectedInfo() != myInfo) {
      g.translate(0, 2);
    }

    super.paint(g);

    if (myTabs.getSelectedInfo() != myInfo) {
      g.translate(0, -2);
    }
  }

  private void handlePopup(final MouseEvent e) {
    if (e.getClickCount() != 1 || !e.isPopupTrigger()) return;

    String place = myTabs.getPopupPlace();
    place = place != null ? place : ActionPlaces.UNKNOWN;
    myTabs.myPopupInfo = myInfo;

    final DefaultActionGroup toShow = new DefaultActionGroup();
    if (myTabs.getPopupGroup() != null) {
      toShow.addAll(myTabs.getPopupGroup());
      toShow.addSeparator();
    }

    Object tabs =
      DataManager.getInstance().getDataContext(e.getComponent(), e.getX(), e.getY()).getData(JBTabsImpl.NAVIGATION_ACTIONS_KEY.getName());
    if (tabs == myTabs && myTabs.myAddNavigationGroup) {
      toShow.addAll(myTabs.myNavigationActions);
    }

    if (toShow.getChildrenCount() == 0) return;

    myTabs.myActivePopup = myTabs.myActionManager.createActionPopupMenu(place, toShow).getComponent();
    myTabs.myActivePopup.addPopupMenuListener(myTabs.myPopupListener);

    myTabs.myActivePopup.addPopupMenuListener(myTabs);
    myTabs.myActivePopup.show(e.getComponent(), e.getX(), e.getY());
    myTabs.onPopup(myTabs.myPopupInfo);
  }


  public void setText(final SimpleColoredText text) {
    clear(false);
    if (text != null) {
      text.appendToComponent(myLabel);
    }
    invalidateIfNeeded();
  }

  private void clear(final boolean invalidate) {
    myLabel.clear();
    myLabel.setIcon(myIcon);

    if (invalidate) {
      invalidateIfNeeded();
    }
  }

  private void invalidateIfNeeded() {
    if (myLabel.getSize() != null && myLabel.getSize().equals(myLabel.getPreferredSize())) return;
    myLabel.getParent().invalidate();

    myTabs.revalidateAndRepaint(false);
  }

  public void setIcon(final Icon icon) {
    getLayeredIcon().setIcon(icon, 0);
    invalidateIfNeeded();
  }

  private LayeredIcon getLayeredIcon() {
    return myIcon;
  }

  public void setAttraction(boolean enabled) {
    getLayeredIcon().setLayerEnabled(1, enabled);
  }

  public boolean isAttractionEnabled() {
    return getLayeredIcon().isLayerEnabled(1);
  }

  public TabInfo getInfo() {
    return myInfo;
  }

  public void apply(JBTabs.UiDecoration decoration) {
    if (decoration.getLabelFont() != null) {
      setFont(decoration.getLabelFont());
    }

    Insets insets = decoration.getLabelInsets();
    if (insets != null) {
      Insets current = JBTabsImpl.ourDefaultDecorator.getDecoration().getLabelInsets();
      setBorder(new EmptyBorder(getValue(current.top, insets.top), getValue(current.left, insets.left),
                                getValue(current.bottom, insets.bottom), getValue(current.right, insets.right)));
      //setBorder(new EmptyBorder(getValue(current.top, insets.top) + 2, getValue(current.left, insets.left),
      //                          getValue(current.bottom, insets.bottom) + 1, getValue(current.right, insets.right)));
    }
  }

  private int getValue(int curentValue, int newValue) {
    return newValue != -1 ? newValue : curentValue;
  }

  public void setTabActions(ActionGroup group) {
    removeOldActionPanel();

    if (group == null) return;

    myActionPanel = new ActionPanel(myTabs, myInfo);

    NonOpaquePanel wrapper = new NonOpaquePanel(new GridBagLayout());
    wrapper.add(myActionPanel);

    add(wrapper, BorderLayout.EAST);

    myTabs.revalidateAndRepaint(false);
  }

  private void removeOldActionPanel() {
    if (myActionPanel != null) {
      myActionPanel.getParent().remove(myActionPanel);
      myActionPanel = null;
    }
  }

  public boolean updateTabActions() {
    return myActionPanel != null && myActionPanel.update();

  }

  public boolean repaintAttraction() {
    if (!myTabs.myAttractions.contains(myInfo)) {
      if (getLayeredIcon().isLayerEnabled(1)) {
        getLayeredIcon().setLayerEnabled(1, false);
        getLayeredIcon().setIcon(null, 1);
        return true;
      }
      return false;
    }

    boolean needsUpdate = false;

    if (getLayeredIcon().getIcon(1) != myInfo.getAlertIcon()) {
      getLayeredIcon().setIcon(myInfo.getAlertIcon(), 1);
      needsUpdate = true;
    }

    int maxInitialBlinkCount = 5;
    int maxRefireBlinkCount = maxInitialBlinkCount + 2;
    if (myInfo.getBlinkCount() < maxInitialBlinkCount && myInfo.isAlertRequested()) {
      getLayeredIcon().setLayerEnabled(1, !getLayeredIcon().isLayerEnabled(1));
      if (myInfo.getBlinkCount() == 0) {
        needsUpdate = true;
      }
      myInfo.setBlinkCount(myInfo.getBlinkCount() + 1);

      if (myInfo.getBlinkCount() == maxInitialBlinkCount) {
        myInfo.resetAlertRequest();
      }

      repaint();
    }
    else {
      if (myInfo.getBlinkCount() < maxRefireBlinkCount && myInfo.isAlertRequested()) {
        getLayeredIcon().setLayerEnabled(1, !getLayeredIcon().isLayerEnabled(1));
        myInfo.setBlinkCount(myInfo.getBlinkCount() + 1);

        if (myInfo.getBlinkCount() == maxRefireBlinkCount) {
          myInfo.setBlinkCount(maxInitialBlinkCount);
          myInfo.resetAlertRequest();
        }

        repaint();
      }
      else {
        needsUpdate = !getLayeredIcon().isLayerEnabled(1);
        getLayeredIcon().setLayerEnabled(1, true);
      }
    }

    return needsUpdate;
  }
}