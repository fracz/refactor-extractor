package com.intellij.openapi.vcs.impl;

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
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusListener;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.FileStatusProvider;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.containers.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mike
 */
public class FileStatusManagerImpl extends FileStatusManager implements ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.impl.FileStatusManagerImpl");

  private final HashMap<VirtualFile, FileStatus> myCachedStatuses = new HashMap<VirtualFile, FileStatus>();

  private Project myProject;
  private List<FileStatusListener> myListeners = new ArrayList<FileStatusListener>();
  private MyDocumentAdapter myDocumentListener;

  private final Map<VirtualFileSystem, FileStatusProvider> myVFSToProviderMap = new HashMap<VirtualFileSystem, FileStatusProvider>();

  public FileStatusManagerImpl(Project project, StartupManager startupManager, ChangeListManager changeListManager) {
    myProject = project;

    startupManager.registerPostStartupActivity(new Runnable() {
      public void run() {
        fileStatusesChanged();
      }
    });

    changeListManager.addChangeListListner(new ChangeListListener() {
      public void changeListAdded(ChangeList list) {
        fileStatusesChanged();
      }

      public void changeListRemoved(ChangeList list) {
        fileStatusesChanged();
      }

      public void changeListChanged(ChangeList list) {
        fileStatusesChanged();
      }

      public void defaultListChanged(ChangeList newDefaultList) {
        fileStatusesChanged();
      }
    });
  }

  public FileStatus calcStatus(VirtualFile virtualFile) {
    LOG.assertTrue(virtualFile != null);

    final VirtualFileSystem fileSystem = virtualFile.getFileSystem();
    if (fileSystem == LocalFileSystem.getInstance()) {
      return calcLocalFileStatus(virtualFile);
    } else {
      final FileStatusProvider fileStatusProvider = myVFSToProviderMap.get(fileSystem);
      if (fileStatusProvider!= null) {
        return fileStatusProvider.getStatus(virtualFile);
      } else {
        return FileStatus.NOT_CHANGED;
      }
    }
  }

  private FileStatus calcLocalFileStatus(final VirtualFile virtualFile) {
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
    myVFSToProviderMap.clear();
  }

  public String getComponentName() {
    return "FileStatusManager";
  }

  public void initComponent() { }

  public void addFileStatusListener(FileStatusListener listener) {
    myListeners.add(listener);
  }

  public void fileStatusesChanged() {
    if (!ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          fileStatusesChanged();
        }
      }, ModalityState.NON_MMODAL);
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

  public void registerProvider(FileStatusProvider provider, VirtualFileSystem fileSystem) {
    myVFSToProviderMap.put(fileSystem, provider);
  }

  public void unregisterProvider(FileStatusProvider provider, VirtualFileSystem fileSystem) {
    final FileStatusProvider currentProvider = myVFSToProviderMap.get(fileSystem);
    if (currentProvider == provider) {
      myVFSToProviderMap.remove(fileSystem);
    }
  }

  private FileStatus getCachedStatus(final VirtualFile file) {
    return myCachedStatuses.get(file);
  }

  public void removeFileStatusListener(FileStatusListener listener) {
    myListeners.remove(listener);
  }

  private class MyDocumentAdapter extends DocumentAdapter {
    public void documentChanged(DocumentEvent event) {
      VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
      if (file != null) {
        FileStatus cachedStatus = getCachedStatus(file);
        if (cachedStatus == FileStatus.NOT_CHANGED) {
          fileStatusChanged(file);
        }
      }
    }
  }
}