/*
 * @author max
 */
package com.intellij.openapi.vfs.newvfs;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ide.startup.CacheUpdater;
import org.jetbrains.annotations.Nullable;

public abstract class RefreshQueue {
  public static RefreshQueue getInstance() {
    return ServiceManager.getService(RefreshQueue.class);
  }

  public abstract void refresh(boolean async, boolean recursive, @Nullable Runnable finishRunnable, VirtualFile... files);

  public abstract void registerRefreshUpdater(CacheUpdater updater);
  public abstract void unregisterRefreshUpdater(CacheUpdater updater);

  public abstract void processSingleEvent(Runnable finishRunnable, VFileEvent event);
}