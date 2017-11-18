package com.intellij.openapi.vcs.impl;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandAdapter;
import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.HashMap;

import java.util.*;

/**
 * @author mike
 */
public class FileStatusManagerImpl extends FileStatusManager implements ProjectComponent {

  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.impl.FileStatusManagerImpl");

  private final HashMap<VirtualFile, FileStatus> myCachedStatuses = new HashMap<VirtualFile, FileStatus>();

  private Project myProject;
  private List<FileStatusListener> myListeners = new ArrayList<FileStatusListener>();
  private MyVirtualFileListener myVirtualFileListener;
  private MyCommandListener myCommandListener;
  private boolean myInsideCommand;
  private Set<VirtualFile> myChangedFiles = new HashSet<VirtualFile>();
  private MyDocumentAdapter myDocumentListener;
  private ModuleRootListener myModuleRootListener;

  private final Map<VirtualFileSystem, FileStatusProvider> myVFSToProviderMap = new HashMap<VirtualFileSystem, FileStatusProvider>();

  public FileStatusManagerImpl(Project project, StartupManager startupManager) {
    myProject = project;

    startupManager.registerPostStartupActivity(new Runnable() {
      public void run() {
        fileStatusesChanged();
      }
    });
  }

  private boolean fileIsInContent(VirtualFile file) {
    return ProjectRootManager.getInstance(myProject).getFileIndex().isInContent(file);
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
    if (!fileIsInContent(virtualFile)) return FileStatus.NOT_CHANGED;

    final AbstractVcs vcs = ProjectLevelVcsManager.getInstance(myProject).getVcsFor(virtualFile);
    if (vcs == null) return FileStatus.NOT_CHANGED;


    FileStatusProvider vcsStatusProvider = vcs.getFileStatusProvider();
    if (vcsStatusProvider != null) {
      if (!ChangeListManager.getInstance(myProject).isFileAffected(virtualFile)) {
        return isDocumentModified(virtualFile) ? FileStatus.MODIFIED : FileStatus.NOT_CHANGED;
      }

      FileStatus status = vcsStatusProvider.getStatus(virtualFile);
      if (status.equals(FileStatus.NOT_CHANGED) && isDocumentModified(virtualFile)) {
        return FileStatus.MODIFIED;
      }
      return status;
    }
    else {
      return FileStatus.NOT_CHANGED;
    }
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
    CommandProcessor.getInstance().removeCommandListener(myCommandListener);
    VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileListener);
    EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(myDocumentListener);
    ProjectRootManager.getInstance(myProject).removeModuleRootListener(myModuleRootListener);
  }

  public void projectOpened() {
    myCommandListener = new MyCommandListener();
    CommandProcessor.getInstance().addCommandListener(myCommandListener);

    myVirtualFileListener = new MyVirtualFileListener();
    VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileListener);

    myDocumentListener = new MyDocumentAdapter();
    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(myDocumentListener);

    myModuleRootListener = new ModuleRootListener() {
      public void rootsChanged(ModuleRootEvent event) {
        fileStatusesChanged();
      }

      public void beforeRootsChange(ModuleRootEvent event) {
      }
    };
    ProjectRootManager.getInstance(myProject).addModuleRootListener(myModuleRootListener);
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

  private class MyCommandListener extends CommandAdapter {
    public void commandStarted(CommandEvent event) {
      myInsideCommand = true;
    }

    public void commandFinished(CommandEvent event) {
      myInsideCommand = false;

      if (!myChangedFiles.isEmpty()) {
        for (final VirtualFile file : myChangedFiles) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              fileStatusChanged(file);
            }
          });
        }
      }

      myChangedFiles.clear();
    }
  }

  private class MyVirtualFileListener implements VirtualFileListener {
    public void propertyChanged(VirtualFilePropertyEvent event) {
      onFileChanged(event.getFile());
    }

    public void contentsChanged(VirtualFileEvent event) {
      onFileChanged(event.getFile());
    }

    public void fileCreated(VirtualFileEvent event) {
      onFileChanged(event.getFile());
    }

    public void fileDeleted(VirtualFileEvent event) {
      onFileChanged(event.getFile());
    }

    public void fileMoved(VirtualFileMoveEvent event) {
      onFileChanged(event.getFile());
    }

    public void beforePropertyChange(VirtualFilePropertyEvent event) {
      onFileChanged(event.getFile());
    }

    public void beforeContentsChange(VirtualFileEvent event) {
      onFileChanged(event.getFile());
    }

    public void beforeFileDeletion(VirtualFileEvent event) {
      onFileChanged(event.getFile());
    }

    public void beforeFileMovement(VirtualFileMoveEvent event) {
      onFileChanged(event.getFile());
    }

    private void onFileChanged(VirtualFile file) {
      if (myInsideCommand) {
        myChangedFiles.add(file);
      }
      else {
        fileStatusChanged(file);
      }
    }
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