package com.intellij.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mike
 */
@State(
  name = "SystemNotifications",
  storages = {
    @Storage(
      id="DuplocatorSettings",
      file="$APP_CONFIG$/other.xml"
    )}
)
public class SystemNotificationsImpl implements SystemNotifications, PersistentStateComponent<SystemNotificationsImpl.State> {
  private State myState = new State();

  private SystemNotificationsImpl() {
  }

  public void notify(@NotNull String notificationName, @NotNull String title, @NotNull String text) {
    if (!SystemInfo.isMac) return;

    myState.myNotifications.add(notificationName);
    GrowlNotifications.getNofications().notify(myState.myNotifications, notificationName, title, text);
  }

  public State getState() {
    return myState;
  }

  public void loadState(final State state) {
    myState = state;
  }


  public static class State {
    private Set<String> myNotifications = new HashSet<String>();
  }
}