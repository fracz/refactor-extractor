/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 28.11.2006
 * Time: 18:41:01
 */
package org.jetbrains.idea.svn.history;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ChangeListColumn;
import com.intellij.openapi.vcs.CommittedChangesProvider;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.SvnUtil;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.io.File;

public class SvnCommittedChangesProvider implements CommittedChangesProvider<SvnChangeList> {
  private Project myProject;
  private String myLocation;

  public SvnCommittedChangesProvider(final Project project) {
    myProject = project;
  }

  public SvnCommittedChangesProvider(final Project project, final String location) {
    myProject = project;
    myLocation = location;
  }

  public ChangeBrowserSettings createDefaultSettings() {
    return new ChangeBrowserSettings();
  }

  public ChangesBrowserSettingsEditor createFilterUI() {
    return new SvnVersionFilterComponent(myProject);
  }

  public List<SvnChangeList> getAllCommittedChanges(ChangeBrowserSettings settings, final int maxCount) throws VcsException {
    ArrayList<SvnChangeList> result = new ArrayList<SvnChangeList>();
    final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
    VirtualFile[] roots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(SvnVcs.getInstance(myProject));
    for(VirtualFile root: roots) {
      final File path = new File(root.getPath());
      if (path.exists()) {
        String[] urls = SvnUtil.getLocationsForModule(SvnVcs.getInstance(myProject), path, progress);
        for(String url: urls) {
          result.addAll(getCommittedChanges(url, maxCount, settings));
        }
      }
    }
    return result;
  }

  public List<SvnChangeList> getCommittedChanges(ChangeBrowserSettings settings, VirtualFile root) throws VcsException {
    if (myLocation != null) {
      return getCommittedChanges(myLocation, 0, settings);
    }
    final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
    String[] urls = SvnUtil.getLocationsForModule(SvnVcs.getInstance(myProject), new File(root.getPath()), progress);
    if (urls.length == 1) {
      return getCommittedChanges(urls [0], 0, settings);
    }
    return Collections.emptyList();
  }

  public List<SvnChangeList> getCommittedChanges(String location, final int maxCount, final ChangeBrowserSettings settings) throws VcsException {
    final ArrayList<SvnChangeList> result = new ArrayList<SvnChangeList>();
    final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
    if (progress != null) {
      progress.setText(SvnBundle.message("progress.text.changes.collecting.changes"));
      progress.setText2(SvnBundle.message("progress.text2.changes.establishing.connection", location));
    }
    try {
      SVNLogClient logger = SvnVcs.getInstance(myProject).createLogClient();
      final SVNRepository repository = SvnVcs.getInstance(myProject).createRepository(location);

      final SvnChangesBrowserSettings svnSettings = SvnChangesBrowserSettings.getSettings(myProject);

      final String author = svnSettings.getAuthorFilter();
      final Date dateFrom = settings.getDateAfterFilter();
      final Long changeFrom = settings.getChangeAfterFilter();
      final Date dateTo = settings.getDateBeforeFilter();
      final Long changeTo = settings.getChangeBeforeFilter();

      final SVNRevision revisionBefore;
      if (dateTo != null) {
        revisionBefore = SVNRevision.create(dateTo);
      }
      else if (changeTo != null) {
        revisionBefore = SVNRevision.create(changeTo.longValue());
      }
      else {
        revisionBefore = SVNRevision.HEAD;
      }
      final SVNRevision revisionAfter;
      if (dateFrom != null) {
        revisionAfter = SVNRevision.create(dateFrom);
      }
      else if (changeFrom != null) {
        revisionAfter = SVNRevision.create(changeFrom.longValue());
      }
      else {
        revisionAfter = SVNRevision.create(1);
      }

      logger.doLog(SVNURL.parseURIEncoded(location), new String[]{""}, revisionBefore, revisionBefore, revisionAfter, false, true, maxCount,
                   new ISVNLogEntryHandler() {
                     public void handleLogEntry(SVNLogEntry logEntry) {
                       if (progress != null) {
                         progress.setText2(SvnBundle.message("progress.text2.processing.revision", logEntry.getRevision()));
                         progress.checkCanceled();
                       }
                       if (author == null || author.equalsIgnoreCase(logEntry.getAuthor())) {
                         result.add(new SvnChangeList(logEntry, repository));
                       }
                     }
                   });
      settings.filterChanges(result);
      return result;
    }
    catch (SVNException e) {
      throw new VcsException(e);
    }
  }

  public ChangeListColumn[] getColumns() {
    return new ChangeListColumn[] { ChangeListColumn.NUMBER, ChangeListColumn.NAME, ChangeListColumn.DATE, ChangeListColumn.DESCRIPTION };
  }
}