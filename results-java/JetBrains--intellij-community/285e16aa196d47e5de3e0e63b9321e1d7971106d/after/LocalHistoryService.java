package com.intellij.localvcs.integration;

import com.intellij.ide.startup.CacheUpdater;
import com.intellij.ide.startup.FileContent;
import com.intellij.ide.startup.FileSystemSynchronizer;
import com.intellij.localvcs.core.ILocalVcs;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;

public class LocalHistoryService {
  private ILocalVcs myVcs;
  private IdeaGateway myGateway;
  // todo get rid of all this managers...
  private StartupManager myStartupManager;
  private ProjectRootManagerEx myRootManager;
  private VirtualFileManagerEx myFileManager;
  private CommandProcessor myCommandProcessor;

  private CacheUpdater myCacheUpdater;
  private EventDispatcher myEventDispatcher;

  public LocalHistoryService(ILocalVcs vcs,
                             IdeaGateway gw,
                             StartupManager sm,
                             ProjectRootManagerEx rm,
                             VirtualFileManagerEx fm,
                             CommandProcessor cp) {
    myVcs = vcs;
    myGateway = gw;
    myStartupManager = sm;
    myRootManager = rm;
    myFileManager = fm;
    myCommandProcessor = cp;

    registerCacheUpdaters();
    registerListeners();
  }

  private void registerCacheUpdaters() {
    myCacheUpdater = new CacheUpdaterAdaptor();

    FileSystemSynchronizer fs = myStartupManager.getFileSystemSynchronizer();
    fs.registerCacheUpdater(myCacheUpdater);
    myRootManager.registerChangeUpdater(myCacheUpdater);
  }

  private void registerListeners() {
    myEventDispatcher = new EventDispatcher(myVcs, myGateway);

    myCommandProcessor.addCommandListener(myEventDispatcher);
    myFileManager.addVirtualFileListener(myEventDispatcher);
    myFileManager.addVirtualFileManagerListener(myEventDispatcher);
  }

  public void shutdown() {
    myFileManager.removeVirtualFileListener(myEventDispatcher);
    myFileManager.removeVirtualFileManagerListener(myEventDispatcher);
    myCommandProcessor.removeCommandListener(myEventDispatcher);

    myRootManager.unregisterChangeUpdater(myCacheUpdater);
  }

  public LocalHistoryAction startAction(String name) {
    LocalHistoryActionImpl a = new LocalHistoryActionImpl(myEventDispatcher, name);
    a.start();
    return a;
  }

  // todo only needed because we should calculate content roots each time
  // todo try to remove this class
  private class CacheUpdaterAdaptor implements CacheUpdater {
    private Updater myUpdater;

    public VirtualFile[] queryNeededFiles() {
      myUpdater = new Updater(myVcs, myGateway);
      return myUpdater.queryNeededFiles();
    }

    public void processFile(FileContent c) {
      myUpdater.processFile(c);
    }

    public void updatingDone() {
      myUpdater.updatingDone();
    }

    public void canceled() {
      myUpdater.canceled();
    }
  }
}