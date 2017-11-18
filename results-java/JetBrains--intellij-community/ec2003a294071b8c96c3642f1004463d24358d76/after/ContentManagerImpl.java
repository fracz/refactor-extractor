package com.intellij.ui.content.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.content.*;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public class ContentManagerImpl implements ContentManager, PropertyChangeListener, Disposable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.ui.content.impl.ContentManagerImpl");

  private ContentUI myUI;
  private ArrayList<Content> myContents;
  private EventListenerList myListeners;
  private List<Content> mySelection = new ArrayList<Content>();
  private boolean myCanCloseContents;

  private final Project myProject;

  private MyComponent myComponent = new MyComponent();

  private Set<Content> myContentWithChangedComponent = new HashSet<Content>();

  /**
   * WARNING: as this class adds listener to the ProjectManager which is removed on projectClosed event, all instances of this class
   * must be created on already OPENED projects, otherwise there will be memory leak!
   */
  public ContentManagerImpl(ContentUI contentUI, boolean canCloseContents, Project project) {
    myCanCloseContents = canCloseContents;
    myContents = new ArrayList<Content>();
    myListeners = new EventListenerList();
    myUI = contentUI;
    myUI.setManager(this);
    myProject = project;

    Disposer.register(project, this);
  }

  public boolean canCloseContents() {
    return myCanCloseContents;
  }

  public JComponent getComponent() {
    if (myComponent.getComponentCount() == 0) {
      myComponent.setContent(myUI.getComponent());
    }
    return myComponent;
  }

  private class MyComponent extends Wrapper.FocusHolder implements DataProvider {
    private List<DataProvider> myProviders = new ArrayList<DataProvider>();


    public MyComponent() {
      setOpaque(false);
    }

    public void addProvider(final DataProvider provider) {
      myProviders.add(provider);
    }

    @Nullable
    public Object getData(@NonNls final String dataId) {
      if (DataConstantsEx.CONTENT_MANAGER.equals(dataId)) return ContentManagerImpl.this;

      for (DataProvider each : myProviders) {
        final Object data = each.getData(dataId);
        if (data != null) return data;
      }
      return null;
    }
  }

  public void addContent(Content content) {
    addContent(content, null);
  }

  public void addContent(final Content content, final Object constraints) {
    ((ContentImpl)content).setManager(this);
    myContents.add(content);
    content.addPropertyChangeListener(this);
    fireContentAdded(content, myContents.size() - 1);
    if (myUI.isToSelectAddedContent() || mySelection.size() == 0) {
      if (myUI.isSingleSelection()) {
        setSelectedContent(content);
      }
      else {
        addSelectedContent(content);
      }
    }

    Disposer.register(this, content);
  }

  public boolean removeContent(Content content) {
    return removeContent(content, true);
  }

  private boolean removeContent(final Content content, boolean trackSelection) {
    try {
      Content selection = mySelection.size() > 0 ? mySelection.get(mySelection.size() - 1) : null;
      int selectedIndex = selection != null ? myContents.indexOf(selection) : -1;

      int indexToBeRemoved = myContents.indexOf(content);
      if (indexToBeRemoved < 0) {
        return false;
      }
      if (!fireContentRemoveQuery(content, indexToBeRemoved)) {
        return false;
      }
      if (!content.isValid()) {
        return false; // the content has already been invalidated by another thread or something
      }


      boolean wasSelected = isSelected(content);
      if (wasSelected) {
        removeSelectedContent(content);
      }

      int indexToSelect = -1;
      if (wasSelected) {
        int i = indexToBeRemoved - 1;
        if (i >= 0) {
          indexToSelect = i;
        }
        else if (getContentCount() > 1) {
          indexToSelect = 0;
        }
      }
      else if (selectedIndex > indexToBeRemoved) {
        indexToSelect = selectedIndex - 1;
      }

      myContents.remove(content);
      content.removePropertyChangeListener(this);

      int newSize = myContents.size();
      if (newSize > 0 && trackSelection) {
        if (indexToSelect > -1) {
          final Content toSelect = myContents.get(indexToSelect);
          if (!isSelected(toSelect)) {
            if (myUI.isSingleSelection()) {
              setSelectedContent(toSelect);
            }
            else {
              addSelectedContent(toSelect);
            }
          }
        }
      }
      else {
        mySelection.clear();
      }
      fireContentRemoved(content, indexToBeRemoved);
      ((ContentImpl)content).setManager(null);


      Disposer.dispose(content);

      return true;
    }
    finally {
      if (ApplicationManager.getApplication().isDispatchThread()) {
        myUI.getComponent().updateUI(); //cleanup visibleComponent from Alloy...TabbedPaneUI
      }
    }
  }

  public void removeAllContents() {
    Content[] contents = getContents();
    for (Content content : contents) {
      removeContent(content);
    }
  }

  public int getContentCount() {
    return myContents.size();
  }

  public Content[] getContents() {
    return myContents.toArray(new ContentImpl[myContents.size()]);
  }

  //TODO[anton,vova] is this method needed?
  public Content findContent(String displayName) {
    for (Content content : myContents) {
      if (content.getDisplayName().equals(displayName)) {
        return content;
      }
    }
    return null;
  }

  public Content getContent(int index) {
    if (index >= 0 && index < myContents.size()) {
      return myContents.get(index);
    }
    else {
      return null;
    }
  }

  public Content getContent(JComponent component) {
    Content[] contents = getContents();
    for (Content content : contents) {
      if (Comparing.equal(component, content.getComponent())) {
        return content;
      }
    }
    return null;
  }

  public int getIndexOfContent(Content content) {
    return myContents.indexOf(content);
  }

  public String getCloseActionName() {
    return UIBundle.message("tabbed.pane.close.tab.action.name");
  }


  public String getCloseAllButThisActionName() {
    return UIBundle.message("tabbed.pane.close.all.tabs.but.this.action.name");
  }

  public List<AnAction> getAdditionalPopupActions(final Content content) {
    return null;
  }

  public boolean canCloseAllContents() {
    if (!canCloseContents()) {
      return false;
    }
    for (Content content : myContents) {
      if (content.isCloseable()) {
        return true;
      }
    }
    return false;
  }

  public void addSelectedContent(final Content content) {
    if (!checkSelectionChangeShouldBeProcessed(content)) return;

    int index;
    if (content != null) {
      index = getIndexOfContent(content);
      if (index == -1) {
        throw new IllegalArgumentException("content not found: " + content);
      }
    }
    else {
      index = -1;
    }
    if (!isSelected(content)) {
      mySelection.add(content);
      fireSelectionChanged(content);
    }
  }

  private boolean checkSelectionChangeShouldBeProcessed(Content content) {
    final boolean result = !isSelected(content) || myContentWithChangedComponent.contains(content);
    myContentWithChangedComponent.remove(content);
    return result;
  }

  public void removeSelectedContent(Content content) {
    if (!isSelected(content)) return;
    mySelection.remove(content);
    fireSelectionChanged(content);
  }

  public boolean isSelected(Content content) {
    return mySelection.contains(content);
  }

  public Content[] getSelectedContents() {
    return mySelection.toArray(new Content[mySelection.size()]);
  }

  @Nullable
  public Content getSelectedContent() {
    return mySelection.size() > 0 ? mySelection.get(0) : null;
  }

  public void setSelectedContent(final Content content, final boolean requestFocus) {
    if (!checkSelectionChangeShouldBeProcessed(content)) return;
    if (!myContents.contains(content)) {
      throw new IllegalArgumentException("Cannot find content:" + content.getDisplayName());
    }

    final boolean focused = isSelectionHoldsFocus();

    final Content[] old = getSelectedContents();

    Runnable selection = new Runnable() {
      public void run() {
        if (getIndexOfContent(content) == -1) return;

        for (Content each : old) {
          removeSelectedContent(each);
          mySelection.clear();
        }

        addSelectedContent(content);
        requestFocus(content);
      }
    };

    if (focused || requestFocus) {
      myComponent.requestFocus(selection);
    }
    else {
      selection.run();
    }
  }

  private boolean isSelectionHoldsFocus() {
    boolean focused = false;
    final Content[] selection = getSelectedContents();
    for (Content each : selection) {
      if (UIUtil.isFocusAncestor(each.getComponent())) {
        focused = true;
        break;
      }
    }
    return focused;
  }


  public void setSelectedContent(final Content content) {
    setSelectedContent(content, false);
  }

  public void selectPreviousContent() {
    int contentCount = getContentCount();
    LOG.assertTrue(contentCount > 1);
    Content selectedContent = getSelectedContent();
    int index = getIndexOfContent(selectedContent);
    index = (index - 1 + contentCount) % contentCount;
    setSelectedContent(getContent(index));
  }

  public void selectNextContent() {
    int contentCount = getContentCount();
    LOG.assertTrue(contentCount > 1);
    Content selectedContent = getSelectedContent();
    int index = getIndexOfContent(selectedContent);
    index = (index + 1) % contentCount;
    setSelectedContent(getContent(index));
  }

  public void addContentManagerListener(ContentManagerListener l) {
    myListeners.add(ContentManagerListener.class, l);
  }

  public void removeContentManagerListener(ContentManagerListener l) {
    myListeners.remove(ContentManagerListener.class, l);
  }


  protected void fireContentAdded(Content content, int newIndex) {
    ContentManagerEvent event = new ContentManagerEvent(this, content, newIndex);
    ContentManagerListener[] listeners = myListeners.getListeners(ContentManagerListener.class);
    for (ContentManagerListener listener : listeners) {
      listener.contentAdded(event);
    }
  }

  protected void fireContentRemoved(Content content, int oldIndex) {
    ContentManagerEvent event = new ContentManagerEvent(this, content, oldIndex);
    ContentManagerListener[] listeners = myListeners.getListeners(ContentManagerListener.class);
    for (ContentManagerListener listener : listeners) {
      listener.contentRemoved(event);
    }
  }

  protected void fireSelectionChanged(Content content) {
    ContentManagerEvent event = new ContentManagerEvent(this, content, myContents.indexOf(content));
    ContentManagerListener[] listeners = myListeners.getListeners(ContentManagerListener.class);
    for (ContentManagerListener listener : listeners) {
      listener.selectionChanged(event);
    }
  }

  protected boolean fireContentRemoveQuery(Content content, int oldIndex) {
    ContentManagerEvent event = new ContentManagerEvent(this, content, oldIndex);
    ContentManagerListener[] listeners = myListeners.getListeners(ContentManagerListener.class);
    for (ContentManagerListener listener : listeners) {
      listener.contentRemoveQuery(event);
      if (event.isConsumed()) {
        return false;
      }
    }
    return true;
  }

  public void requestFocus(Content content) {
    Content toSelect = content == null ? getSelectedContent() : content;
    if (toSelect == null) return;
    assert myContents.contains(toSelect);

    JComponent toFocus = toSelect.getPreferredFocusableComponent();
    toFocus = IdeFocusTraversalPolicy.getPreferredFocusedComponent(toFocus);

    if (toFocus == null) toFocus = toSelect.getPreferredFocusableComponent();

    if (toFocus != null) {
      toFocus.requestFocus();
    }
  }

  public void addDataProvider(final DataProvider provider) {
    myComponent.addProvider(provider);
  }

  public void propertyChange(final PropertyChangeEvent evt) {
    if (Content.PROP_COMPONENT.equals(evt.getPropertyName())) {
      myContentWithChangedComponent.add((Content)evt.getSource());
    }
  }

  public ContentFactory getFactory() {
    return PeerFactory.getInstance().getContentFactory();
  }

  public void dispose() {
    myContents = null;
    mySelection = null;
    myContentWithChangedComponent.clear();
    myUI = null;
    myListeners = null;
  }
}