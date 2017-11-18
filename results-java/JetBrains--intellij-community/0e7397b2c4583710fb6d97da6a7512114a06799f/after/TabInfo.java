package com.intellij.ui.tabs;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;

public final class TabInfo {

  public static final String ACTION_GROUP = "actionGroup";
  public static final String ICON = "icon";
  public static final String TEXT = "text";
  public static final String TAB_ACTION_GROUP = "tabActionGroup";
  public static final String ALERT_ICON = "alertIcon";

  public static final String ALERT_STATUS = "alertStatus";
  public static final String HIDDEN = "hidden";

  private JComponent myComponent;
  private JComponent myPreferredFocusableComponent;

  private ActionGroup myGroup;

  private PropertyChangeSupport myChangeSupport = new PropertyChangeSupport(this);

  private Icon myIcon;
  private String myPlace;
  private Object myObject;
  private JComponent mySideComponent;
  private WeakReference<JComponent> myLastFocusOwner;


  private ActionGroup myTabLabelActions;
  private String myTabActionPlace;

  private Icon myAlertIcon;

  private int myBlinkCount;
  private boolean myAlertRequested;
  private boolean myHidden;
  private JComponent myActionsContextComponent;

  private SimpleColoredText myText = new SimpleColoredText();

  public TabInfo(final JComponent component) {
    myComponent = component;
    myPreferredFocusableComponent = component;
  }

  public PropertyChangeSupport getChangeSupport() {
    return myChangeSupport;
  }

  public TabInfo setText(String text) {
    clear();
    append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    return this;
  }

  public TabInfo clear() {
    final String old = myText.toString();
    myText.clear();
    myChangeSupport.firePropertyChange(TEXT, old, myText.toString());
    return this;
  }

  public TabInfo append(String fragment, SimpleTextAttributes attributes) {
    final String old = myText.toString();
    myText.append(fragment, attributes);
    myChangeSupport.firePropertyChange(TEXT, old, myText.toString());
    return this;
  }

  public TabInfo setIcon(Icon icon) {
    Icon old = myIcon;
    myIcon = icon;
    myChangeSupport.firePropertyChange(ICON, old, icon);
    return this;
  }



  public ActionGroup getGroup() {
    return myGroup;
  }

  public JComponent getComponent() {
    return myComponent;
  }

  public String getText() {
    return myText.toString();
  }

  public SimpleColoredText getColoredText() {
    return myText;
  }

  public Icon getIcon() {
    return myIcon;
  }

  public String getPlace() {
    return myPlace;
  }

  public TabInfo setSideComponent(JComponent comp) {
    mySideComponent = comp;
    return this;
  }

  public JComponent getSideComponent() {
    return mySideComponent;
  }

  public TabInfo setActions(ActionGroup group, String place) {
    ActionGroup old = myGroup;
    myGroup = group;
    myPlace = place;
    myChangeSupport.firePropertyChange(ACTION_GROUP, old, myGroup);
    return this;
  }

  public TabInfo setActionsContextComponent(JComponent c) {
    myActionsContextComponent = c;
    return this;
  }

  public JComponent getActionsContextComponent() {
    return myActionsContextComponent;
  }

  public TabInfo setObject(final Object object) {
    myObject = object;
    return this;
  }

  public Object getObject() {
    return myObject;
  }

  public JComponent getPreferredFocusableComponent() {
    return myPreferredFocusableComponent != null ? myPreferredFocusableComponent : myComponent;
  }

  public TabInfo setPreferredFocusableComponent(final JComponent component) {
    myPreferredFocusableComponent = component;
    return this;
  }

  public void setLastFocusOwner(final JComponent owner) {
    myLastFocusOwner = new WeakReference<JComponent>(owner);
  }

  public ActionGroup getTabLabelActions() {
    return myTabLabelActions;
  }

  public String getTabActionPlace() {
    return myTabActionPlace;
  }

  public void setTabLabelActions(final ActionGroup tabActions, String place) {
    ActionGroup old = myTabLabelActions;
    myTabLabelActions = tabActions;
    myTabActionPlace = place;
    myChangeSupport.firePropertyChange(TAB_ACTION_GROUP, old, myTabLabelActions);
  }

  @Nullable
  public JComponent getLastFocusOwner() {
    return myLastFocusOwner != null ? myLastFocusOwner.get() : null;
  }

  public TabInfo setAlertIcon(final Icon alertIcon) {
    Icon old = myAlertIcon;
    myAlertIcon = alertIcon;
    myChangeSupport.firePropertyChange(ALERT_ICON, old, myAlertIcon);
    return this;
  }

  public void fireAlert() {
    myAlertRequested = true;
    myChangeSupport.firePropertyChange(ALERT_STATUS, null, true);
  }

  public void stopAlerting() {
    myAlertRequested = false;
    myChangeSupport.firePropertyChange(ALERT_STATUS, null, false);
  }

  public int getBlinkCount() {
    return myBlinkCount;
  }

  public void setBlinkCount(final int blinkCount) {
    myBlinkCount = blinkCount;
  }

  public String toString() {
    return getText();
  }

  public Icon getAlertIcon() {
    return myAlertIcon == null ? IconLoader.getIcon("/nodes/tabAlert.png") : myAlertIcon;
  }

  public void resetAlertRequest() {
    myAlertRequested = false;
  }

  public boolean isAlertRequested() {
    return myAlertRequested;
  }

  public void setHidden(boolean hidden) {
    boolean old = myHidden;
    myHidden = hidden;
    myChangeSupport.firePropertyChange(HIDDEN, old, myHidden);
  }

  public boolean isHidden() {
    return myHidden;
  }
}