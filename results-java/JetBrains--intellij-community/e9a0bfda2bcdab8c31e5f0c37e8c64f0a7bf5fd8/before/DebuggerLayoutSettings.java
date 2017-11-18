package com.intellij.debugger.settings;

import com.intellij.debugger.ui.DebuggerContentInfo;
import com.intellij.debugger.ui.content.newUI.NewContentState;
import com.intellij.debugger.ui.content.newUI.PlaceInGrid;
import com.intellij.debugger.ui.content.newUI.Tab;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Key;
import com.intellij.ui.content.Content;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(
  name = "DebuggerLayoutSettings",
  storages = {
    @Storage(
      id ="other",
      file = "$APP_CONFIG$/debugger.layout.xml"
    )}
)
public class DebuggerLayoutSettings implements PersistentStateComponent<Element>, ApplicationComponent {

  private static final String VIEW_STATE = "viewState";

  private Map<String, NewContentState> myContentStates = new HashMap<String, NewContentState>();
  private Set<Tab> myTabs = new TreeSet<Tab>(new Comparator<Tab>() {
    public int compare(final Tab o1, final Tab o2) {
      return o1.getIndex() - o2.getIndex();
    }
  });

  private General myGeneral = new General();

  public Element getState() {
    return write(new Element("layout"));
  }

  public void loadState(final Element state) {
    read(state);
  }

  public Element read(final Element parentNode)  {
    final List newContents = parentNode.getChildren(VIEW_STATE);
    myContentStates.clear();
    for (Object content : newContents) {
      final NewContentState state = new NewContentState(this, (Element)content);
      myContentStates.put(state.getID(), state);
    }

    List tabs = parentNode.getChildren(Tab.TAB);
    for (Object eachTabElement : tabs) {
      Tab eachTab = new Tab((Element)eachTabElement);
      getOrCreateTab(eachTab.getIndex()).read((Element)eachTabElement);
    }

    XmlSerializer.deserializeInto(myGeneral, parentNode);

    return parentNode;
  }

  public Element write(final Element parentNode) {
    for (NewContentState eachState : myContentStates.values()) {
      final Element content = new Element(VIEW_STATE);
      parentNode.addContent(content);
      eachState.write(content);
    }

    for (Tab eachTab : myTabs) {
      eachTab.write(parentNode);
    }

    parentNode.addContent(XmlSerializer.serialize(myGeneral));

    return parentNode;
  }

  @NotNull
  public Tab getOrCreateTab(final int index) {
    Tab tab = findTab(index);
    if (tab != null) return tab;

    tab = createNewTab(index);

    return tab;
  }

  public Tab getDefaultTab() {
    return getOrCreateTab(0);
  }

  private Tab createNewTab(final int index) {
    final Tab tab;
    tab = new Tab(index, index == 0 ? "Debugger" : null, null);
    myTabs.add(tab);
    return tab;
  }

  public Tab createNewTab() {
    int index = 0;
    for (Tab each : myTabs) {
      if (!isUsed(each)) return each;

      if (each.getIndex() < Integer.MAX_VALUE) {
        index = each.getIndex() + 1;
      } else {
        break;
      }
    }

    return createNewTab(index);
  }

  private boolean isUsed(Tab tab) {
    for (NewContentState each : myContentStates.values()) {
      if (each.getTab() == tab) return true;
    }

    return false;
  }

  @Nullable
  private Tab findTab(int index) {
    for (Tab each : myTabs) {
      if (index == each.getIndex()) return each;
    }

    return null;
  }

  public NewContentState getStateFor(Content content) {
    Key key = getContentID(content);

    assert key != null : "Content for debugger UI must be specified with: " + DebuggerContentInfo.CONSOLE_CONTENT;

    NewContentState state = myContentStates.get(key.toString());
    return state != null ? state : getDefaultContentState(content);
  }

  private NewContentState getDefaultContentState(final Content content) {
    NewContentState state;

    final Key kind = getContentID(content);
    if (DebuggerContentInfo.FRAME_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), getDefaultGridPlace(kind), false);
    } else if (DebuggerContentInfo.VARIABLES_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), getDefaultGridPlace(kind), false);
    } else if (DebuggerContentInfo.WATCHES_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), getDefaultGridPlace(kind), false);
    } else if (DebuggerContentInfo.CONSOLE_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(1), getDefaultGridPlace(kind), false);
    } else {
      state =  new NewContentState(kind.toString(), getOrCreateTab(Integer.MAX_VALUE), getDefaultGridPlace(kind), false);
    }

    myContentStates.put(state.getID(), state);

    return state;
  }

  public PlaceInGrid getDefaultGridPlace(Content content) {
    Key id = getContentID(content);
    return getDefaultGridPlace(id);
  }

  public PlaceInGrid getDefaultGridPlace(Key id) {
    if (DebuggerContentInfo.FRAME_CONTENT.equals(id)) {
      return PlaceInGrid.left;
    } else if (DebuggerContentInfo.VARIABLES_CONTENT.equals(id)) {
      return PlaceInGrid.center;
    } else if (DebuggerContentInfo.WATCHES_CONTENT.equals(id)) {
      return PlaceInGrid.right;
    } else if (DebuggerContentInfo.CONSOLE_CONTENT.equals(id)) {
      return PlaceInGrid.bottom;
    } else {
      return PlaceInGrid.bottom;
    }
  }


  private Key getContentID(final Content content) {
    return content.getUserData(DebuggerContentInfo.CONTENT_ID);
  }


  public int getDefaultSelectedTabIndex() {
    return 0;
  }

  public void setSelectedTabIndex(int index) {
    myGeneral.selectedTab = index;
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "DebuggerLayoutSettings";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public void resetToDefault() {
    myContentStates.clear();
    myTabs.clear();
  }

  public boolean isToolbarHorizontal() {
    return myGeneral.horizontalToolbar;
  }

  public void setToolbarHorizontal(boolean horizontal) {
    myGeneral.horizontalToolbar = horizontal;
  }

  public static class General {
    public volatile boolean horizontalToolbar = false;
    public volatile int selectedTab = 0;
  }

}