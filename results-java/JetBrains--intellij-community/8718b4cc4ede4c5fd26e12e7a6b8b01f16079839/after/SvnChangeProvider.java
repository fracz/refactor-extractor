package org.jetbrains.idea.svn;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.impl.ExcludedFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.CommonProcessors;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.actions.CleanupWorker;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;
import org.tmatesoft.svn.core.internal.wc.admin.SVNWCAccess;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.*;

/**
 * @author max
 * @author yole
 */
public class SvnChangeProvider implements ChangeProvider {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.SvnChangeProvider");
  public static final String ourDefaultListName = VcsBundle.message("changes.default.changlist.name");

  private final SvnVcs myVcs;
  private final VcsContextFactory myFactory;
  private final SvnBranchConfigurationManager myBranchConfigurationManager;
  private final ExcludedFileIndex myExcludedFileIndex;
  private final ChangeListManager myClManager;
  private final SvnFileUrlMapping myMapping;

  public SvnChangeProvider(final SvnVcs vcs) {
    myVcs = vcs;
    myFactory = VcsContextFactory.SERVICE.getInstance();
    myBranchConfigurationManager = SvnBranchConfigurationManager.getInstance(myVcs.getProject());
    myExcludedFileIndex = ExcludedFileIndex.getInstance(myVcs.getProject());
    myClManager = ChangeListManager.getInstance(myVcs.getProject());
    myMapping = myVcs.getSvnFileUrlMapping();
  }

  public void getChanges(final VcsDirtyScope dirtyScope, final ChangelistBuilder builder, ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    try {
      final SvnChangeProviderContext context = new SvnChangeProviderContext(myVcs, builder, progress);
      for (FilePath path : dirtyScope.getRecursivelyDirtyDirectories()) {
        processFile(path, context, null, true, context.getClient());
      }

      for (FilePath path : dirtyScope.getDirtyFiles()) {
        FileStatus status = getParentStatus(context, path);
        processFile(path, context, status, false, context.getClient());
      }

      for(SvnChangedFile copiedFile: context.getCopiedFiles()) {
        if (context.isCanceled()) {
          throw new ProcessCanceledException();
        }
        processCopiedFile(copiedFile, builder, context);
      }
      for(SvnChangedFile deletedFile: context.getDeletedFiles()) {
        if (context.isCanceled()) {
          throw new ProcessCanceledException();
        }
        processStatus(deletedFile.getFilePath(), deletedFile.getStatus(), builder, null);
      }
    }
    catch (SVNException e) {
      throw new VcsException(e);
    }
  }

  public void getChanges(final FilePath path, final boolean recursive, final ChangelistBuilder builder) throws SVNException {
    final SvnChangeProviderContext context = new SvnChangeProviderContext(myVcs, builder, null);
    processFile(path, context, null, recursive, context.getClient());
  }

  private String changeListNameFromStatus(final SVNStatus status) {
    if (WorkingCopyFormat.ONE_DOT_FIVE.getFormat() == status.getWorkingCopyFormat()) {
      if (SVNNodeKind.FILE.equals(status.getKind())) {
        final String clName = status.getChangelistName();
        return (clName == null) ? null : clName;
      }
    }
    // always null for earlier versions
    return null;
  }

  private void processCopiedFile(SvnChangedFile copiedFile, ChangelistBuilder builder, SvnChangeProviderContext context) throws SVNException {
    boolean foundRename = false;
    final SVNStatus copiedStatus = copiedFile.getStatus();
    final String copyFromURL = copiedFile.getCopyFromURL();
    final FilePath copiedToPath = copiedFile.getFilePath();

    // if copy target is _deleted_, treat like deleted, not moved!
    /*for (Iterator<SvnChangedFile> iterator = context.getDeletedFiles().iterator(); iterator.hasNext();) {
      final SvnChangedFile deletedFile = iterator.next();
      final FilePath deletedPath = deletedFile.getFilePath();

      if (Comparing.equal(deletedPath, copiedToPath)) {
        return;
      }
    }*/

    final Set<SvnChangedFile> deletedToDelete = new HashSet<SvnChangedFile>();

    for (Iterator<SvnChangedFile> iterator = context.getDeletedFiles().iterator(); iterator.hasNext();) {
      SvnChangedFile deletedFile = iterator.next();
      final SVNStatus deletedStatus = deletedFile.getStatus();
      if ((deletedStatus != null) && (deletedStatus.getURL() != null) && Comparing.equal(copyFromURL, deletedStatus.getURL().toString())) {
        final String clName = changeListNameFromStatus(copiedFile.getStatus());
        builder.processChangeInList(new Change(createBeforeRevision(deletedFile, true),
                                         CurrentContentRevision.create(copiedFile.getFilePath())), clName);
        deletedToDelete.add(deletedFile);
        for(Iterator<SvnChangedFile> iterChild = context.getDeletedFiles().iterator(); iterChild.hasNext();) {
          SvnChangedFile deletedChild = iterChild.next();
          final SVNStatus childStatus = deletedChild.getStatus();
          if (childStatus == null) {
            continue;
          }
          final SVNURL childUrl = childStatus.getURL();
          if (childUrl == null) {
            continue;
          }
          final String childURL = childUrl.toString();
          if (childURL.startsWith(copyFromURL + "/")) {
            String relativePath = childURL.substring(copyFromURL.length());
            File newPath = new File(copiedFile.getFilePath().getIOFile(), relativePath);
            FilePath newFilePath = myFactory.createFilePathOn(newPath);
            if (! context.isDeleted(newFilePath)) {
              builder.processChangeInList(new Change(createBeforeRevision(deletedChild, true),
                                             CurrentContentRevision.create(newFilePath)), clName);
              deletedToDelete.add(deletedChild);
            }
          }
        }
        foundRename = true;
        break;
      }
    }

    final List<SvnChangedFile> deletedFiles = context.getDeletedFiles();
    for (SvnChangedFile file : deletedToDelete) {
      deletedFiles.remove(file);
    }

    // handle the case when the deleted file wasn't included in the dirty scope - try searching for the local copy
    // by building a relative url
    if (!foundRename && copiedStatus.getURL() != null) {
      File wcPath = guessWorkingCopyPath(copiedStatus.getFile(), copiedStatus.getURL(), copyFromURL);
      SVNStatus status;
      try {
        status = context.getClient().doStatus(wcPath, false);
      }
      catch(SVNException ex) {
        status = null;
      }
      if (status != null && status.getContentsStatus() == SVNStatusType.STATUS_DELETED) {
        final FilePath filePath = myFactory.createFilePathOnDeleted(wcPath, false);
        final SvnContentRevision beforeRevision = SvnContentRevision.create(myVcs, filePath, status.getRevision());
        final ContentRevision afterRevision = CurrentContentRevision.create(copiedFile.getFilePath());
        builder.processChangeInList(new Change(beforeRevision, afterRevision), changeListNameFromStatus(status));
        foundRename = true;
      }
    }

    if (!foundRename) {
      // for debug
      LOG.info("Rename not found for " + copiedFile.getFilePath().getPresentableUrl());
      processStatus(copiedFile.getFilePath(), copiedStatus, builder, null);
    }
  }

  private SvnContentRevision createBeforeRevision(final SvnChangedFile changedFile, final boolean forDeleted) {
    return SvnContentRevision.create(myVcs,
        forDeleted ? FilePathImpl.createForDeletedFile(changedFile.getStatus().getFile(), changedFile.getFilePath().isDirectory()) :
                   changedFile.getFilePath(), changedFile.getStatus().getRevision());
  }

  /**
   * Check if some of the parents of the specified path are switched and/or ignored. These statuses should propagate to
   * the files for which the status was requested even if none of these files are switched or ignored by themselves.
   * (See IDEADEV-13393)
   */
  @Nullable
  private FileStatus getParentStatus(final SvnChangeProviderContext context, final FilePath path) {
    if (context.isCanceled()) {
      throw new ProcessCanceledException();
    }
    final FilePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return null;
    }
    VirtualFile file = parentPath.getVirtualFile();
    if (file == null) {
      return null;
    }
    FileStatus status = FileStatusManager.getInstance(myVcs.getProject()).getStatus(file);
    if (status == FileStatus.IGNORED) {
      return status;
    }
    // performance optimization which doesn't work in tests: ask SVN for status only if we know of some change to file
    final Change change = ChangeListManager.getInstance(myVcs.getProject()).getChange(file);
    if (ApplicationManager.getApplication().isUnitTestMode() || (change != null && (change.isRenamed() || change.isMoved()))) {
      try {
        final SVNStatus svnStatus = context.getClient().doStatus(parentPath.getIOFile(), false, false);
        if ((svnStatus != null) && (svnStatus.getCopyFromURL() != null)) {
          context.addCopyFromURL(parentPath, svnStatus.getCopyFromURL());
        }
      }
      catch (SVNException e) {
        // ignore
      }
    }
    FileStatus parentStatus = getParentStatus(context, parentPath);
    if (parentStatus != null) {
      return parentStatus;
    }
    // we care about switched only if none of the parents is ignored
    if (status == FileStatus.SWITCHED) {
      return status;
    }
    return null;
  }

  private static File guessWorkingCopyPath(final File file, @NotNull final SVNURL url, final String copyFromURL) throws SVNException {
    String copiedPath = url.getPath();
    String copyFromPath = SVNURL.parseURIEncoded(copyFromURL).getPath();
    String commonPathAncestor = SVNPathUtil.getCommonPathAncestor(copiedPath, copyFromPath);
    int pathSegmentCount = SVNPathUtil.getSegmentsCount(copiedPath);
    int ancestorSegmentCount = SVNPathUtil.getSegmentsCount(commonPathAncestor);
    List<String> segments = StringUtil.split(file.getPath(), File.separator);
    List<String> copyFromPathSegments = StringUtil.split(copyFromPath, "/");
    List<String> resultSegments = new ArrayList<String>();
    final int keepSegments = segments.size() - pathSegmentCount + ancestorSegmentCount;
    for(int i=0; i< keepSegments; i++) {
      resultSegments.add(segments.get(i));
    }
    for(int i=ancestorSegmentCount; i<copyFromPathSegments.size(); i++) {
      resultSegments.add(copyFromPathSegments.get(i));
    }
    return new File(StringUtil.join(resultSegments, "/"));
  }

  public boolean isModifiedDocumentTrackingRequired() {
    return true;
  }

  public void doCleanup(final List<VirtualFile> files) {
    new CleanupWorker(files.toArray(new VirtualFile[files.size()]), myVcs.getProject(), "action.Subversion.cleanup.progress.title").execute();
  }

  private void processFile(FilePath path, final SvnChangeProviderContext context,
                           final FileStatus parentStatus, final boolean recursively,
                           final SVNStatusClient statusClient) throws SVNException {
    if (context.isCanceled()) {
      throw new ProcessCanceledException();
    }
    try {
      if (path.isDirectory()) {
        statusClient.doStatus(path.getIOFile(), SVNRevision.WORKING, recursively ? SVNDepth.INFINITY : SVNDepth.IMMEDIATES,
                              false, true, true, false, new ISVNStatusHandler() {
          public void handleStatus(SVNStatus status) throws SVNException {
            if (context.isCanceled()) {
              throw new ProcessCanceledException();
            }
            FilePath path = VcsUtil.getFilePath(status.getFile(), status.getKind().equals(SVNNodeKind.DIR));
            final VirtualFile vFile = path.getVirtualFile();
            if (vFile != null && myExcludedFileIndex.isExcludedFile(vFile)) {
              return;
            }
            processStatusFirstPass(path, status, context, parentStatus);
            if ((vFile != null) && (status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED) && path.isDirectory()) {
              if (myClManager.isIgnoredFile(vFile)) {
                // for directory, this means recursively ignored by Idea
                reportIgnoredRecursively(vFile, context);
              } else {
                // process children of this file with another client.
                SVNStatusClient client = myVcs.createStatusClient();
                if (recursively && path.isDirectory()) {
                  VirtualFile[] children = vFile.getChildren();
                  for (VirtualFile aChildren : children) {
                    FilePath filePath = VcsUtil.getFilePath(aChildren.getPath(), aChildren.isDirectory());
                    processFile(filePath, context, parentStatus, recursively, client);
                  }
                }
              }
            }
          }
        }, null);
      } else {
        processFile(path, context, parentStatus);
      }
    } catch (SVNException e) {
      if (e.getErrorMessage().getErrorCode() == SVNErrorCode.WC_NOT_DIRECTORY) {
        final VirtualFile virtualFile = path.getVirtualFile();
        if (virtualFile != null) {
          if (myExcludedFileIndex.isExcludedFile(virtualFile)) return;
          if (parentStatus != FileStatus.IGNORED) {
            if (! SvnUtil.isWorkingCopyRoot(new File(virtualFile.getPath()))) {
              context.getBuilder().processUnversionedFile(virtualFile);
            }
          }
        }
        // process children recursively!
        if (recursively && path.isDirectory() && virtualFile != null) {
          VirtualFile[] children = virtualFile.getChildren();
          for (VirtualFile child : children) {
            FilePath filePath = VcsUtil.getFilePath(child.getPath(), child.isDirectory());
            processFile(filePath, context, parentStatus, recursively, statusClient);
          }
        }
      }
      else {
        throw e;
      }
    }
  }

  private void reportIgnoredRecursively(final VirtualFile vFile, final SvnChangeProviderContext context) {
    final CommonProcessors.CollectUniquesProcessor<VirtualFile> processor = new CommonProcessors.CollectUniquesProcessor<VirtualFile>();
    VfsUtil.processFilesRecursively(vFile, processor);
    final Collection<VirtualFile> ignoredFiles = processor.getResults();
    for (VirtualFile ignoredFile : ignoredFiles) {
      context.getBuilder().processIgnoredFile(ignoredFile);
    }
  }

  private void processFile(FilePath filePath, SvnChangeProviderContext context, FileStatus parentStatus) throws SVNException {
    SVNStatus status = context.getClient().doStatus(filePath.getIOFile(), false, false);
    processStatusFirstPass(filePath, status, context, parentStatus);
  }

  private void processStatusFirstPass(final FilePath filePath, final SVNStatus status, final SvnChangeProviderContext context,
                                      final FileStatus parentStatus) throws SVNException {
    if (status == null) {
      // external to wc
      return;
    }
    if (filePath.isDirectory() && status.isLocked()) {
      context.getBuilder().processLockedFolder(filePath.getVirtualFile());
    }
    if (status.getContentsStatus() == SVNStatusType.STATUS_ADDED && status.getCopyFromURL() != null) {
      context.addCopiedFile(filePath, status, status.getCopyFromURL());
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_DELETED) {
      context.getDeletedFiles().add(new SvnChangedFile(filePath, status));
    }
    else {
      String parentCopyFromURL = context.getParentCopyFromURL(filePath);
      if (parentCopyFromURL != null) {
        context.addCopiedFile(filePath, status, parentCopyFromURL);
      }
      processStatus(filePath, status, context.getBuilder(), parentStatus);
    }
  }

  private void processStatus(final FilePath filePath, final SVNStatus status, final ChangelistBuilder builder,
                             final FileStatus parentStatus) throws SVNException {
    loadEntriesFile(filePath);
    if (status != null) {
      FileStatus fStatus = convertStatus(status, filePath.getIOFile());

      final SVNStatusType statusType = status.getContentsStatus();
      final SVNStatusType propStatus = status.getPropertiesStatus();
      if (statusType == SVNStatusType.STATUS_UNVERSIONED || statusType == SVNStatusType.UNKNOWN) {
        final VirtualFile file = filePath.getVirtualFile();
        if (file != null) {
          builder.processUnversionedFile(file);
        }
      }
      else if (statusType == SVNStatusType.STATUS_CONFLICTED ||
               statusType == SVNStatusType.STATUS_MODIFIED ||
               statusType == SVNStatusType.STATUS_REPLACED ||
               propStatus == SVNStatusType.STATUS_MODIFIED) {
        builder.processChangeInList(new Change(SvnContentRevision.create(myVcs, filePath, status.getRevision()),
                                         CurrentContentRevision.create(filePath), fStatus), changeListNameFromStatus(status));
        checkSwitched(filePath, builder, status, fStatus);
      }
      else if (statusType == SVNStatusType.STATUS_ADDED) {
        builder.processChangeInList(new Change(null, CurrentContentRevision.create(filePath), fStatus), changeListNameFromStatus(status));
      }
      else if (statusType == SVNStatusType.STATUS_DELETED) {
        builder.processChangeInList(new Change(SvnContentRevision.create(myVcs, filePath, status.getRevision()), null, fStatus),
                                    changeListNameFromStatus(status));
      }
      else if (statusType == SVNStatusType.STATUS_MISSING) {
        builder.processLocallyDeletedFile(filePath);
      }
      else if (statusType == SVNStatusType.STATUS_IGNORED || parentStatus == FileStatus.IGNORED) {
        builder.processIgnoredFile(filePath.getVirtualFile());
      }
      else if (status.isCopied()) {
        //
      }
      else if ((fStatus == FileStatus.NOT_CHANGED || fStatus == FileStatus.SWITCHED) && statusType != SVNStatusType.STATUS_NONE) {
        VirtualFile file = filePath.getVirtualFile();
        if (file != null && FileDocumentManager.getInstance().isFileModified(file)) {
          builder.processChangeInList(new Change(SvnContentRevision.create(myVcs, filePath, status.getRevision()),
                                           CurrentContentRevision.create(filePath), FileStatus.MODIFIED), changeListNameFromStatus(status));
        }
        checkSwitched(filePath, builder, status, fStatus);
      }
    }
  }

  private void checkSwitched(final FilePath filePath, final ChangelistBuilder builder, final SVNStatus status,
                             final FileStatus convertedStatus) {
    if (status.isSwitched() || (convertedStatus == FileStatus.SWITCHED)) {
      final VirtualFile virtualFile = filePath.getVirtualFile();
      if (virtualFile == null) return;
      final String switchUrl = status.getURL().toString();
      final VirtualFile vcsRoot = ProjectLevelVcsManager.getInstance(myVcs.getProject()).getVcsRootFor(virtualFile);
      if (vcsRoot != null) {  // it will be null if we walked into an excluded directory
        String baseUrl = null;
        try {
          baseUrl = myBranchConfigurationManager.get(vcsRoot).getBaseName(switchUrl);
        }
        catch (VcsException e) {
          LOG.info(e);
        }
        builder.processSwitchedFile(virtualFile, baseUrl == null ? switchUrl : baseUrl, true);
      }
    }
  }

  private static FileStatus convertStatus(final SVNStatus status, final File file) throws SVNException {
    if (status == null) {
      return FileStatus.UNKNOWN;
    }
    if (status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED) {
      return FileStatus.UNKNOWN;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_MISSING) {
      return FileStatus.DELETED_FROM_FS;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_EXTERNAL) {
      return SvnFileStatus.EXTERNAL;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_OBSTRUCTED) {
      return SvnFileStatus.OBSTRUCTED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_IGNORED) {
      return FileStatus.IGNORED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_ADDED) {
      return FileStatus.ADDED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_DELETED) {
      return FileStatus.DELETED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_REPLACED) {
      return SvnFileStatus.REPLACED;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED ||
             status.getPropertiesStatus() == SVNStatusType.STATUS_CONFLICTED) {
      return FileStatus.MERGED_WITH_CONFLICTS;
    }
    else if (status.getContentsStatus() == SVNStatusType.STATUS_MODIFIED ||
             status.getPropertiesStatus() == SVNStatusType.STATUS_MODIFIED) {
      return FileStatus.MODIFIED;
    }
    else if (status.isSwitched()) {
      return FileStatus.SWITCHED;
    }
    else if (status.isCopied()) {
      return FileStatus.ADDED;
    }
    if (file.isDirectory() && file.getParentFile() != null) {
      String childURL = null;
      String parentURL = null;
      SVNWCAccess wcAccess = SVNWCAccess.newInstance(null);
      try {
        wcAccess.open(file, false, 0);
        SVNAdminArea parentDir = wcAccess.open(file.getParentFile(), false, 0);
        final SVNEntry childEntry = wcAccess.getEntry(file, false);
        if (childEntry != null) {
          childURL = childEntry.getURL();
        }
        if (parentDir != null) {
          final SVNEntry parentDirEntry = parentDir.getEntry("", false);
          if (parentDirEntry != null) {
            parentURL = parentDirEntry.getURL();
          }
        }
      } catch (SVNException e) {
        parentURL = null;
      }
      finally {
        try {
          wcAccess.close();
        } catch (SVNException e) {
          //
        }
      }
        try {
          // todo isWorking copy root ? maybe use already got area entry
            if (parentURL != null && !SVNWCUtil.isWorkingCopyRoot(file)) {
              parentURL = SVNPathUtil.append(parentURL, SVNEncodingUtil.uriEncode(file.getName()));
              if (childURL != null && !parentURL.equals(childURL)) {
                return FileStatus.SWITCHED;
              }
            }
        } catch (SVNException e) {
            //
        }
    }
    return FileStatus.NOT_CHANGED;
  }

  /**
   * Ensures that the contents of the 'entries' file is cached in the VFS, so that the VFS will send
   * correct events when the 'entries' file is changed externally (to be received by SvnEntriesFileListener)
   *
   * @param filePath the path of a changed file.
   */
  private static void loadEntriesFile(final FilePath filePath) {
    final FilePath parentPath = filePath.getParentPath();
    if (parentPath == null) {
      return;
    }
    File svnSubdirectory = new File(parentPath.getIOFile(), SvnUtil.SVN_ADMIN_DIR_NAME);
    LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    VirtualFile file = localFileSystem.refreshAndFindFileByIoFile(svnSubdirectory);
    if (file != null) {
      localFileSystem.refreshAndFindFileByIoFile(new File(svnSubdirectory, SvnUtil.ENTRIES_FILE_NAME));
    }
    if (filePath.isDirectory()) {
      svnSubdirectory = new File(filePath.getPath(), SvnUtil.SVN_ADMIN_DIR_NAME);
      file = localFileSystem.refreshAndFindFileByIoFile(svnSubdirectory);
      if (file != null) {
        localFileSystem.refreshAndFindFileByIoFile(new File(svnSubdirectory, SvnUtil.ENTRIES_FILE_NAME));
      }
    }
  }

  private static class SvnChangedFile {
    private final FilePath myFilePath;
    private final SVNStatus myStatus;
    private String myCopyFromURL;

    public SvnChangedFile(final FilePath filePath, final SVNStatus status) {
      myFilePath = filePath;
      myStatus = status;
    }

    public SvnChangedFile(final FilePath filePath, final SVNStatus status, final String copyFromURL) {
      myFilePath = filePath;
      myStatus = status;
      myCopyFromURL = copyFromURL;
    }

    public FilePath getFilePath() {
      return myFilePath;
    }

    public SVNStatus getStatus() {
      return myStatus;
    }

    public String getCopyFromURL() {
      if (myCopyFromURL == null) {
        return myStatus.getCopyFromURL();
      }
      return myCopyFromURL;
    }

    @Override
    public String toString() {
      return myFilePath.getPath();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SvnChangedFile that = (SvnChangedFile)o;

      if (myFilePath != null ? !myFilePath.equals(that.myFilePath) : that.myFilePath != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return myFilePath != null ? myFilePath.hashCode() : 0;
    }
  }

  private static class SvnChangeProviderContext {
    private final ChangelistBuilder myChangelistBuilder;
    private final SVNStatusClient myStatusClient;
    private List<SvnChangedFile> myCopiedFiles = null;
    private final List<SvnChangedFile> myDeletedFiles = new ArrayList<SvnChangedFile>();
    private Map<FilePath, String> myCopyFromURLs = null;

    private final ProgressIndicator myProgress;

    public SvnChangeProviderContext(SvnVcs vcs, final ChangelistBuilder changelistBuilder, final ProgressIndicator progress) {
      myStatusClient = vcs.createStatusClient();
      myChangelistBuilder = changelistBuilder;
      myProgress = progress;
    }

    public ChangelistBuilder getBuilder() {
      return myChangelistBuilder;
    }

    public SVNStatusClient getClient() {
      return myStatusClient;
    }

    @NotNull
    public List<SvnChangedFile> getCopiedFiles() {
      if (myCopiedFiles == null) {
        return Collections.emptyList();
      }
      return myCopiedFiles;
    }

    public List<SvnChangedFile> getDeletedFiles() {
      return myDeletedFiles;
    }

    public boolean isDeleted(final FilePath path) {
      for (SvnChangedFile deletedFile : myDeletedFiles) {
        if (Comparing.equal(path, deletedFile.getFilePath())) {
          return true;
        }
      }
      return false;
    }

    public boolean isCanceled() {
      return (myProgress != null) && myProgress.isCanceled();
    }

    /**
     * If the specified filepath or its parent was added with history, returns the URL of the copy source for this filepath.
     *
     * @param filePath the original filepath
     * @return the copy source url, or null if the file isn't a copy of anything
     */
    @Nullable
    public String getParentCopyFromURL(FilePath filePath) {
      if (myCopyFromURLs == null) {
        return null;
      }
      StringBuilder relPathBuilder = new StringBuilder();
      while(filePath != null) {
        String copyFromURL = myCopyFromURLs.get(filePath);
        if (copyFromURL != null) {
          return copyFromURL + relPathBuilder.toString();
        }
        relPathBuilder.insert(0, "/" + filePath.getName());
        filePath = filePath.getParentPath();
      }
      return null;
    }

    public void addCopiedFile(final FilePath filePath, final SVNStatus status, final String copyFromURL) {
      if (myCopiedFiles == null) {
        myCopiedFiles = new ArrayList<SvnChangedFile>();
      }
      myCopiedFiles.add(new SvnChangedFile(filePath, status, copyFromURL));
      final String url = status.getCopyFromURL();
      if (url != null) {
        addCopyFromURL(filePath, url);
      }
    }

    public void addCopyFromURL(final FilePath filePath, final String url) {
      if (myCopyFromURLs == null) {
        myCopyFromURLs = new HashMap<FilePath, String>();
      }
      myCopyFromURLs.put(filePath, url);
    }
  }
}