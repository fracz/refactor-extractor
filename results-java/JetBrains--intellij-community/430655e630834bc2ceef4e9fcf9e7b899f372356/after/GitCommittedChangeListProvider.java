package git4idea.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.committed.DecoratorManager;
import com.intellij.openapi.vcs.changes.committed.VcsCommittedListsZipper;
import com.intellij.openapi.vcs.changes.committed.VcsCommittedViewAuxiliary;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitBranch;
import git4idea.GitRemote;
import git4idea.GitUtil;
import git4idea.commands.GitHandler;
import git4idea.commands.GitSimpleHandler;
import git4idea.commands.StringScanner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The provider for committed change lists
 */
public class GitCommittedChangeListProvider implements CachingCommittedChangesProvider<CommittedChangeList, ChangeBrowserSettings> {
  /**
   * the logger
   */
  private static final Logger LOG = Logger.getInstance(GitCommittedChangeListProvider.class.getName());


  /**
   * The project for the service
   */
  private Project myProject;

  /**
   * The constructor
   *
   * @param project the project instance for this provider
   */
  public GitCommittedChangeListProvider(Project project) {
    myProject = project;
  }

  /**
   * {@inheritDoc}
   */
  public ChangeBrowserSettings createDefaultSettings() {
    return new ChangeBrowserSettings();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesBrowserSettingsEditor<ChangeBrowserSettings> createFilterUI(boolean showDateFilter) {
    return new GitVersionFilterComponent(showDateFilter);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryLocation getLocationFor(FilePath root) {
    VirtualFile gitRoot = GitUtil.getGitRootOrNull(root);
    if (gitRoot == null) {
      return null;
    }
    try {
      GitBranch c = GitBranch.current(myProject, gitRoot);
      if (c == null) {
        return null;
      }
      String remote = c.getTrackedRemoteName(myProject, gitRoot);
      if (StringUtil.isEmpty(remote)) {
        return null;
      }
      File rootFile = new File(gitRoot.getPath());
      if (".".equals(remote)) {
        return new GitRepositoryLocation(gitRoot.getUrl(), rootFile);
      }
      else {
        GitRemote r = GitRemote.find(myProject, gitRoot, remote);
        return r == null ? null : new GitRepositoryLocation(r.fetchUrl(), rootFile);
      }
    }
    catch (VcsException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Exception for determining repository location", e);
      }
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryLocation getLocationFor(FilePath root, String repositoryPath) {
    return getLocationFor(root);
  }

  /**
   * {@inheritDoc}
   */
  public VcsCommittedListsZipper getZipper() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<CommittedChangeList> getCommittedChanges(ChangeBrowserSettings settings, RepositoryLocation location, int maxCount)
    throws VcsException {
    ArrayList<CommittedChangeList> rc = new ArrayList<CommittedChangeList>();
    GitRepositoryLocation l = (GitRepositoryLocation)location;
    Long beforeRev = settings.getChangeBeforeFilter();
    Long afterRev = settings.getChangeBeforeFilter();
    Date beforeDate = settings.getDateBeforeFilter();
    Date afterDate = settings.getDateBeforeFilter();
    String author = settings.getUserFilter();
    VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(l.getRoot());
    if (root == null) {
      throw new VcsException("The repository does not exists anymore: " + l.getRoot());
    }
    GitSimpleHandler h = new GitSimpleHandler(myProject, root, GitHandler.LOG);
    h.addParameters("--pretty=format:%x0C%n" + GitChangeUtils.COMMITTED_CHANGELIST_FORMAT);
    if (!StringUtil.isEmpty(author)) {
      h.addParameters("--author=" + author);
    }
    if (beforeDate != null) {
      h.addParameters("--before=" + GitUtil.gitTime(beforeDate));
    }
    if (afterDate != null) {
      h.addParameters("--after=" + GitUtil.gitTime(afterDate));
    }
    if (maxCount != getUnlimitedCountValue()) {
      h.addParameters("-n" + maxCount);
    }
    if (beforeRev != null && afterRev != null) {
      h.addParameters(GitUtil.formatLongRev(afterRev) + ".." + GitUtil.formatLongRev(beforeRev));
    }
    else if (beforeRev != null) {
      h.addParameters(GitUtil.formatLongRev(beforeRev));
    }
    else if (afterRev != null) {
      h.addParameters(GitUtil.formatLongRev(afterRev) + "..");
    }
    String output = h.run();
    StringScanner s = new StringScanner(output);
    while (s.hasMoreData() && s.startsWith('\u000C')) {
      s.nextLine();
      rc.add(GitChangeUtils.parseChangeList(myProject, root, s));
    }
    if (s.hasMoreData()) {
      throw new IllegalStateException("More input is avaialble: " + s.line());
    }
    return rc;
  }


  /**
   * {@inheritDoc}
   */
  public ChangeListColumn[] getColumns() {
    return new ChangeListColumn[]{ChangeListColumn.NUMBER, ChangeListColumn.DATE, ChangeListColumn.DESCRIPTION, ChangeListColumn.NAME};
  }

  /**
   * {@inheritDoc}
   */
  public VcsCommittedViewAuxiliary createActions(DecoratorManager manager, RepositoryLocation location) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public int getUnlimitedCountValue() {
    return -1;
  }

  public int getFormatVersion() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void writeChangeList(DataOutput stream, CommittedChangeList list) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public CommittedChangeList readChangeList(RepositoryLocation location, DataInput stream) throws IOException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean isMaxCountSupported() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Collection<FilePath> getIncomingFiles(RepositoryLocation location) throws VcsException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean refreshCacheByNumber() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Nls
  public String getChangelistTitle() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean isChangeLocallyAvailable(FilePath filePath,
                                          @Nullable VcsRevisionNumber localRevision,
                                          VcsRevisionNumber changeRevision,
                                          CommittedChangeList changeList) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean refreshIncomingWithCommitted() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}