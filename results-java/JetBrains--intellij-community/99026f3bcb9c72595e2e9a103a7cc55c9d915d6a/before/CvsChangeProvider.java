package com.intellij.cvsSupport2.cvsstatuses;

import com.intellij.CvsBundle;
import com.intellij.cvsSupport2.CvsVcs2;
import com.intellij.cvsSupport2.actions.AddFileOrDirectoryAction;
import com.intellij.cvsSupport2.actions.RemoveLocallyFileOrDirectoryAction;
import com.intellij.cvsSupport2.application.CvsEntriesManager;
import com.intellij.cvsSupport2.checkinProject.CvsRollbacker;
import com.intellij.cvsSupport2.checkinProject.DirectoryContent;
import com.intellij.cvsSupport2.checkinProject.VirtualFileEntry;
import com.intellij.cvsSupport2.config.CvsConfiguration;
import com.intellij.cvsSupport2.cvsExecution.CvsOperationExecutor;
import com.intellij.cvsSupport2.cvsExecution.CvsOperationExecutorCallback;
import com.intellij.cvsSupport2.cvshandlers.CommandCvsHandler;
import com.intellij.cvsSupport2.cvshandlers.CvsHandler;
import com.intellij.cvsSupport2.cvsoperations.cvsContent.GetFileContentOperation;
import com.intellij.cvsSupport2.history.CvsRevisionNumber;
import com.intellij.cvsSupport2.util.CvsVfsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.lib.cvsclient.admin.Entry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author max
 */
public class CvsChangeProvider implements ChangeProvider {
  private CvsVcs2 myVcs;

  public CvsChangeProvider(final CvsVcs2 vcs) {
    myVcs = vcs;
  }

  public void getChanges(final VcsDirtyScope dirtyScope, final ChangelistBuilder builder, final ProgressIndicator progress) {
    for (FilePath path : dirtyScope.getRecursivelyDirtyDirectories()) {
      final VirtualFile dir = path.getVirtualFile();
      if (dir != null) {
        processEntriesIn(dir, dirtyScope, builder, true);
      }
      else {
        processFile(path, builder);
      }
    }

    for (FilePath path : dirtyScope.getDirtyFiles()) {
      if (path.isDirectory()) {
        final VirtualFile dir = path.getVirtualFile();
        if (dir != null) {
          processEntriesIn(dir, dirtyScope, builder, false);
        }
        else {
          processFile(path, builder);
        }
      }
      else {
        processFile(path, builder);
      }
    }
  }

  public List<VcsException> commit(List<Change> changes, String preparedComment) {
    final List<FilePath> filesList = ChangesUtil.getPaths(changes);
    FilePath[] files = filesList.toArray(new FilePath[filesList.size()]);
    Project project = myVcs.getProject();
    final CvsOperationExecutor executor = new CvsOperationExecutor(project);
    executor.setShowErrors(false);

    final CvsConfiguration cvsConfiguration = CvsConfiguration.getInstance(project);

    CvsHandler handler = CommandCvsHandler.createCommitHandler(
          files,
          new File[]{},
          preparedComment,
          CvsBundle.message("operation.name.commit.file", files.length),
          CvsConfiguration.getInstance(project).MAKE_NEW_FILES_READONLY,
          project,
          cvsConfiguration.TAG_AFTER_PROJECT_COMMIT,
          cvsConfiguration.TAG_AFTER_PROJECT_COMMIT_NAME);

    executor.performActionSync(handler, CvsOperationExecutorCallback.EMPTY);
    return executor.getResult().getErrorsAndWarnings();
  }

  public List<VcsException> rollbackChanges(List<Change> changes) {
    List<VcsException> exceptions = new ArrayList<VcsException>();

    CvsRollbacker rollbacker = new CvsRollbacker(myVcs.getProject());
    for (Change change : changes) {
      final FilePath filePath = ChangesUtil.getFilePath(change);
      VirtualFile parent = filePath.getVirtualFileParent();
      String name = filePath.getName();

      try {
        switch (change.getType()) {
          case DELETED:
            rollbacker.rollbackFileDeleting(parent, name);
            break;

          case MODIFICATION:
            rollbacker.rollbackFileModifying(parent, name);
            break;

          case MOVED:
            rollbacker.rollbackFileCreating(parent, name);
            break;

          case NEW:
            rollbacker.rollbackFileCreating(parent, name);
            break;
        }
      }
      catch (IOException e) {
        exceptions.add(new VcsException(e));
      }
    }

    return exceptions;
  }

  public List<VcsException> scheduleMissingFileForDeletion(List<File> files) {
    final Project project = myVcs.getProject();
    final CvsHandler handler = RemoveLocallyFileOrDirectoryAction.getDefaultHandler(project, files);
    final CvsOperationExecutor executor = new CvsOperationExecutor(project);
    executor.performActionSync(handler, CvsOperationExecutorCallback.EMPTY);
    return Collections.emptyList();
  }

  public List<VcsException> rollbackMissingFileDeletion(List<File> files) {
    final Project project = myVcs.getProject();
    FilePath[] filePaths = new FilePath[files.size()];
    for(int i=0; i<files.size(); i++) {
      filePaths [i] = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(files.get(i));
    }
    final CvsHandler cvsHandler = CommandCvsHandler.createCheckoutFileHandler(filePaths, CvsConfiguration.getInstance(project));
    final CvsOperationExecutor executor = new CvsOperationExecutor(project);
    executor.performActionSync(cvsHandler, CvsOperationExecutorCallback.EMPTY);
    return Collections.emptyList();
  }

  public List<VcsException> scheduleUnversionedFilesForAddition(List<VirtualFile> files) {
    final Project project = myVcs.getProject();
    final CvsHandler handler = AddFileOrDirectoryAction.getDefaultHandler(project, files.toArray(new VirtualFile[files.size()]));
    final CvsOperationExecutor executor = new CvsOperationExecutor(project);
    executor.performActionSync(handler, CvsOperationExecutorCallback.EMPTY);
    return Collections.emptyList();
  }

  private void processEntriesIn(@NotNull VirtualFile dir, VcsDirtyScope scope, ChangelistBuilder builder, boolean recursively) {
    if (!scope.belongsTo(PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(dir))) return;
    final DirectoryContent dirContent = CvsStatusProvider.getDirectoryContent(dir, myVcs.getProject());

    for (VirtualFile file : dirContent.getUnknownFiles()) {
      builder.processUnversionedFile(file);
    }

    for(Entry entry: dirContent.getDeletedDirectories()) {
      builder.processLocallyDeletedFile(CvsVfsUtil.getFileFor(dir, entry.getFileName()));
    }

    for (Entry entry : dirContent.getDeletedFiles()) {
      builder.processLocallyDeletedFile(CvsVfsUtil.getFileFor(dir, entry.getFileName()));
    }

    /*
    final Collection<VirtualFile> unknownDirs = dirContent.getUnknownDirectories();
    for (VirtualFile file : unknownDirs) {
      builder.processUnversionedFile(file);
    }
    */

    for (VirtualFileEntry fileEntry : dirContent.getFiles()) {
      processFile(dir, fileEntry.getVirtualFile(), fileEntry.getEntry(), builder);
    }

    if (recursively) {
      for (VirtualFile file : dir.getChildren()) {
        if (file.isDirectory()) {
          processEntriesIn(file, scope, builder, true);
        }
      }
    }
  }


  private void processFile(final FilePath filePath, final ChangelistBuilder builder) {
    final VirtualFile dir = filePath.getVirtualFileParent();
    if (dir == null) return;

    final Entry entry = CvsEntriesManager.getInstance().getEntryFor(dir, filePath.getName());
    final FileStatus status = CvsStatusProvider.getStatus(filePath.getVirtualFile(), entry);
    VcsRevisionNumber number = entry != null ? new CvsRevisionNumber(entry.getRevision()) : VcsRevisionNumber.NULL;
    processStatus(filePath, status, number, builder);
  }

  private void processFile(final VirtualFile dir, @Nullable VirtualFile file, Entry entry, final ChangelistBuilder builder) {
    final FilePath filePath = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(dir, entry.getFileName());
    final FileStatus statis = CvsStatusProvider.getStatus(file, entry);
    final VcsRevisionNumber number = new CvsRevisionNumber(entry.getRevision());
    processStatus(filePath, statis, number, builder);
  }

  private void processStatus(final FilePath filePath,
                             final FileStatus status,
                             final VcsRevisionNumber number,
                             final ChangelistBuilder builder) {
    if (status == FileStatus.NOT_CHANGED) return;
    if (status == FileStatus.MODIFIED || status == FileStatus.MERGE || status == FileStatus.MERGED_WITH_CONFLICTS) {
      builder.processChange(new Change(new CvsUpToDateRevision(filePath, number), new CurrentContentRevision(filePath), status));
    }
    else if (status == FileStatus.ADDED) {
      builder.processChange(new Change(null, new CurrentContentRevision(filePath), status));
    }
    else if (status == FileStatus.DELETED) {
      builder.processChange(new Change(new CvsUpToDateRevision(filePath, number), null, status));
    }
    else if (status == FileStatus.DELETED_FROM_FS) {
      builder.processLocallyDeletedFile(filePath.getIOFile());
    }
    else if (status == FileStatus.UNKNOWN) {
      builder.processUnversionedFile(filePath.getVirtualFile());
    }
  }

  private class CvsUpToDateRevision implements ContentRevision {
    private FilePath myPath;
    private VcsRevisionNumber myRevisionNumber;
    private String myContent;

    public CvsUpToDateRevision(final FilePath path, final VcsRevisionNumber revisionNumber) {
      myRevisionNumber = revisionNumber;
      myPath = path;
    }

    @Nullable
    public String getContent() {
      if (myContent == null) {
        try {
          final GetFileContentOperation operation = GetFileContentOperation.createForFile(myPath);
          CvsVcs2.executeQuietOperation(CvsBundle.message("operation.name.get.file.content"), operation, myVcs.getProject());
          final byte[] fileBytes = operation.tryGetFileBytes();
          myContent = fileBytes == null ? null : new String(fileBytes, myPath.getCharset().name());
        }
        catch (Exception e) {
          myContent = null;
        }
      }
      return myContent;
    }

    @NotNull
    public FilePath getFile() {
      return myPath;
    }

    @NotNull
    public VcsRevisionNumber getRevisionNumber() {
      return myRevisionNumber;
    }
  }
}