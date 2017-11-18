package com.intellij.openapi.vcs.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.readOnlyHandler.ReadonlyStatusHandlerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mike
 */
public class FileStatusManagerImpl extends FileStatusManager implements ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.impl.FileStatusManagerImpl");

  private final HashMap<VirtualFile, FileStatus> myCachedStatuses = new HashMap<VirtualFile, FileStatus>();

  private Project myProject;
  private final ProjectLevelVcsManager myVcsManager;
  private List<FileStatusListener> myListeners = new ArrayList<FileStatusListener>();
  private MyDocumentAdapter myDocumentListener;

  public FileStatusManagerImpl(Project project,
                               StartupManager startupManager,
                               ChangeListManager changeListManager,
                               ProjectLevelVcsManager vcsManager) {
    myProject = project;
    myVcsManager = vcsManager;

    startupManager.registerPostStartupActivity(new Runnable() {
      public void run() {
        fileStatusesChanged();
      }
    });

    changeListManager.addChangeListListener(new ChangeListAdapter() {
      public void changeListAdded(ChangeList list) {
        fileStatusesChanged();
      }

      public void changeListRemoved(ChangeList list) {
        fileStatusesChanged();
      }

      public void changeListChanged(ChangeList list) {
        fileStatusesChanged();
      }

      public void changeListUpdateDone() {
        ProjectLevelVcsManagerImpl vcsManager = (ProjectLevelVcsManagerImpl) myVcsManager;
        if (vcsManager.hasEmptyContentRevisions()) {
          vcsManager.resetHaveEmptyContentRevisions();
          fileStatusesChanged();
        }
      }

      @Override public void unchangedFileStatusChanged() {
        fileStatusesChanged();
      }
    });
  }

  public FileStatus calcStatus(@NotNull VirtualFile virtualFile) {
    if (virtualFile.isInLocalFileSystem()) {
      return calcLocalFileStatus(virtualFile);
    } else {
      return FileStatus.NOT_CHANGED;
    }
  }

  private boolean fileIsInContent(VirtualFile file) {
    return ProjectRootManager.getInstance(myProject).getFileIndex().isInContent(file);
  }

  private FileStatus calcLocalFileStatus(final VirtualFile virtualFile) {
    if (!fileIsInContent(virtualFile)) return FileStatus.NOT_CHANGED;
    final AbstractVcs vcs = myVcsManager.getVcsFor(virtualFile);
    if (vcs == null) return FileStatus.NOT_CHANGED;

    final FileStatus status = ChangeListManager.getInstance(myProject).getStatus(virtualFile);
    if (status == FileStatus.NOT_CHANGED && isDocumentModified(virtualFile)) return FileStatus.MODIFIED;
    return status;
  }

  private static boolean isDocumentModified(VirtualFile virtualFile) {
    if (virtualFile.isDirectory()) return false;
    final Document editorDocument = FileDocumentManager.getInstance().getCachedDocument(virtualFile);

    if (editorDocument != null && editorDocument.getModificationStamp() != virtualFile.getModificationStamp()) {
      return true;
    }

    return false;
  }

  public void projectClosed() {
    EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(myDocumentListener);
  }

  public void projectOpened() {
    myDocumentListener = new MyDocumentAdapter();
    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(myDocumentListener);
  }

  public void disposeComponent() {
    myCachedStatuses.clear();
  }

  @NotNull
  public String getComponentName() {
    return "FileStatusManager";
  }

  public void initComponent() { }

  public void addFileStatusListener(FileStatusListener listener) {
    myListeners.add(listener);
  }

  public void addFileStatusListener(final FileStatusListener listener, Disposable parentDisposable) {
    addFileStatusListener(listener);
    Disposer.register(parentDisposable, new Disposable() {
      public void dispose() {
        removeFileStatusListener(listener);
      }
    });
  }

  public void fileStatusesChanged() {
    if (myProject.isDisposed()) {
      return;
    }
    if (!ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          fileStatusesChanged();
        }
      }, ModalityState.NON_MODAL);
      return;
    }

    myCachedStatuses.clear();

    final FileStatusListener[] listeners = myListeners.toArray(new FileStatusListener[myListeners.size()]);
    for (FileStatusListener listener : listeners) {
      listener.fileStatusesChanged();
    }
  }

  public void fileStatusChanged(final VirtualFile file) {
    final Application application = ApplicationManager.getApplication();
    if (!application.isDispatchThread() && !application.isUnitTestMode()) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          fileStatusChanged(file);
        }
      });
      return;
    }

    if (!file.isValid()) return;
    FileStatus cachedStatus = getCachedStatus(file);
    if (cachedStatus == null) return;
    FileStatus newStatus = calcStatus(file);
    if (cachedStatus == newStatus) return;
    myCachedStatuses.put(file, newStatus);

    final FileStatusListener[] listeners = myListeners.toArray(new FileStatusListener[myListeners.size()]);
    for (FileStatusListener listener : listeners) {
      listener.fileStatusChanged(file);
    }
  }

  public FileStatus getStatus(final VirtualFile file) {
    FileStatus status = getCachedStatus(file);
    if (status == null) {
      status = calcStatus(file);
      myCachedStatuses.put(file, status);
    }

    return status;
  }

  private FileStatus getCachedStatus(final VirtualFile file) {
    return myCachedStatuses.get(file);
  }

  public void removeFileStatusListener(FileStatusListener listener) {
    myListeners.remove(listener);
  }

  public void refreshFileStatusFromDocument(final VirtualFile file, final Document doc) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("refreshFileStatusFromDocument: file.getModificationStamp()=" + file.getModificationStamp() + ", document.getModificationStamp()=" + doc.getModificationStamp());
    }
    FileStatus cachedStatus = getCachedStatus(file);
    if (cachedStatus == FileStatus.NOT_CHANGED || file.getModificationStamp() == doc.getModificationStamp()) {
      final AbstractVcs vcs = myVcsManager.getVcsFor(file);
      if (vcs == null) return;
      if (cachedStatus == FileStatus.MODIFIED && file.getModificationStamp() == doc.getModificationStamp()) {
        if (!((ReadonlyStatusHandlerImpl) ReadonlyStatusHandlerImpl.getInstance(myProject)).getState().SHOW_DIALOG) {
          vcs.rollbackIfUnchanged(file);
        }
      }
      fileStatusChanged(file);
      ChangeProvider cp = vcs.getChangeProvider();
      if (cp != null && cp.isModifiedDocumentTrackingRequired()) {
        VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
      }
    }
  }

  private class MyDocumentAdapter extends DocumentAdapter {
    public void documentChanged(DocumentEvent event) {
      VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
      if (file != null) {
        refreshFileStatusFromDocument(file, event.getDocument());
      }
    }
  }
}