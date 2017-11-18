/*
 * @author max
 */
package com.intellij.openapi.vfs.newvfs.events;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;

public class VFilePropertyChangeEvent extends VFileEvent {
  private VirtualFile myFile;
  private final String myPropertyName;
  private final Object myOldValue;
  private final Object myNewValue;

  public VFilePropertyChangeEvent(final VirtualFile file, String propertyName, Object oldValue, Object newValue, boolean isFromRefresh) {
    super(isFromRefresh);
    myFile = file;
    myPropertyName = propertyName;
    myOldValue = oldValue;
    myNewValue = newValue;
  }

  public VirtualFile getFile() {
    return myFile;
  }

  public Object getNewValue() {
    return myNewValue;
  }

  public Object getOldValue() {
    return myOldValue;
  }

  public String getPropertyName() {
    return myPropertyName;
  }

  @NonNls
  public String toString() {
    return "VfsEvent[property( " + myPropertyName + ") changed for '" + myFile + "': oldValue = " + myOldValue + ", newValue = " + myNewValue + "]";
  }
}