package com.intellij.localvcs.integration;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.localvcs.core.ILocalVcs;
import com.intellij.localvcs.core.LocalVcs;
import com.intellij.localvcs.core.ThreadSafeLocalVcs;
import com.intellij.localvcs.core.storage.Storage;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.localVcs.LvcsFile;
import com.intellij.openapi.localVcs.LvcsFileRevision;
import com.intellij.openapi.localVcs.LvcsRevision;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LocalHistoryComponent extends LocalHistory implements ProjectComponent {
  private Project myProject;
  private StartupManagerEx myStartupManager;
  private ProjectRootManagerEx myRootManager;
  private VirtualFileManagerEx myFileManager;
  private CommandProcessor myCommandProcessor;
  private Storage myStorage;
  private ILocalVcs myVcs;
  private LocalVcsService myService;

  // todo bad method - extend interface instead
  public static ILocalVcs getLocalVcsFor(Project p) {
    return ((LocalHistoryComponent)getInstance(p)).getLocalVcs();
  }

  public LocalHistoryComponent(Project p, StartupManager sm, ProjectRootManagerEx rm, VirtualFileManagerEx fm, CommandProcessor cp) {
    myProject = p;
    myStartupManager = (StartupManagerEx)sm;
    myRootManager = rm;
    myFileManager = fm;
    myCommandProcessor = cp;
  }

  public void initComponent() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;

    // todo review startup order
    myStartupManager.registerPreStartupActivity(new Runnable() {
      public void run() {
        initVcs();
        initService();
      }
    });
  }

  protected void initVcs() {
    myStorage = new Storage(getStorageDir());
    myVcs = new ThreadSafeLocalVcs(new LocalVcs(myStorage));
  }

  protected void initService() {
    myService = new LocalVcsService(myVcs, createIdeaGateway(), myStartupManager, myRootManager, myFileManager, myCommandProcessor);
  }

  public File getStorageDir() {
    // todo dont forget to change folder name
    File vcsDir = new File(getSystemPath(), "vcs_new");
    return new File(vcsDir, myProject.getLocationHash());
  }

  protected String getSystemPath() {
    return PathManager.getSystemPath();
  }

  public void save() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;
    if (myVcs != null) myVcs.save();
  }

  public void disposeComponent() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;
    closeVcs();
    closeService();

    cleanupStorageAfterTestCase();
  }

  protected void cleanupStorageAfterTestCase() {
    if (isUnitTestMode()) FileUtil.delete(getStorageDir());
  }

  protected boolean isUnitTestMode() {
    return ApplicationManagerEx.getApplicationEx().isUnitTestMode();
  }

  protected void closeVcs() {
    myStorage.close();
  }

  protected void closeService() {
    myService.shutdown();
  }

  protected boolean isDefaultProject() {
    return myProject.isDefault();
  }

  public boolean isEnabled() {
    return isEnabled(myProject);
  }

  @Override
  protected LocalHistoryAction startAction(String name) {
    if (!isEnabled()) return LocalHistoryActionImpl.NULL;
    return myService.startAction(name);
  }

  @Override
  protected void putLabel(String name) {
    myVcs.putLabel(name);
  }

  @Override
  protected void putLabel(String path, String name) {
    myVcs.putLabel(path, name);
  }

  @Override
  protected Checkpoint putCheckpoint() {
    return new CheckpointImpl(createIdeaGateway(), myVcs);
  }

  @Override
  protected void mark(VirtualFile f) {
    if (!isEnabled()) {
      LvcsFile ff = getOldVcs().findFile(f.getPath());
      if (ff != null && ff.getRevision() != null) {
        ff.getRevision().setUpToDate(true);
      }
      return;
    }

    if (!isUnderControl(f)) return;
    myVcs.mark(f.getPath());
  }

  @Override
  protected byte[] getLastMarkedByteContent(VirtualFile f) {
    if (!isEnabled()) {
      LvcsFile ff = getOldVcs().findFile(f.getPath());
      if (ff != null && ff.getRevision() != null) {
        LvcsRevision uptoDate = ff.getRevision().findNearestPreviousUpToDateRevision();
        return ((LvcsFileRevision)uptoDate).getByteContent();
      }
      return null;
    }
    if (!isUnderControl(f)) return null;
    return myVcs.getLastMarkedByteContent(f.getPath());
  }

  @Override
  protected byte[] getByteContent(VirtualFile f, RevisionTimestampComparator c) {
    if (!isEnabled()) {
      LvcsFile ff = getOldVcs().findFile(f.getPath());
      if (ff == null) return null;
      LvcsFileRevision r = ff.getRevision();
      while (r != null) {
        if (c.isSuitable(r.getDate())) return r.getByteContent();
        r = (LvcsFileRevision)r.getPrevRevision();
      }
      return null;
    }

    if (!isUnderControl(f)) return null;
    return myVcs.getByteContent(f.getPath(), c);
  }

  private com.intellij.openapi.localVcs.LocalVcs getOldVcs() {
    return com.intellij.openapi.localVcs.LocalVcs.getInstance(myProject);
  }

  @Override
  protected boolean isUnderControl(VirtualFile f) {
    return createIdeaGateway().getFileFilter().isAllowedAndUnderContentRoot(f);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    // todo dont forget to change name
    return "NewLocalVcs";
  }

  private IdeaGateway createIdeaGateway() {
    return new IdeaGateway(myProject);
  }

  public ILocalVcs getLocalVcs() {
    if (!isEnabled()) throw new RuntimeException("new local vcs is disabled");
    return myVcs;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }
}