/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.openapi.vcs;

import com.intellij.codeInsight.CodeSmellInfo;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.localVcs.LvcsAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.merge.MergeProvider;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Component which provides means to invoke different VCS-related services.
 */
public abstract class AbstractVcsHelper {
  public static AbstractVcsHelper getInstance(Project project) {
    return project.getComponent(AbstractVcsHelper.class);
  }

  public abstract void showErrors(List<VcsException> abstractVcsExceptions, String tabDisplayName);

  public abstract void markFileAsUpToDate(VirtualFile file);

  public abstract LvcsAction startVcsAction(String actionName);

  public abstract void finishVcsAction(LvcsAction action);

  /**
   * Runs the runnable inside the vcs transaction (if needed), collects all exceptions, commits/rollbacks transaction
   * and returns all exceptions together.
   */
  public abstract List<VcsException> runTransactionRunnable(AbstractVcs vcs, TransactionRunnable runnable, Object vcsParameters);

  /**
   * If the file was moved/renamed, this method will return path
   * to the last known up-to-date revision
   */
  public abstract String getUpToDateFilePath(VirtualFile file);

  public abstract void optimizeImportsAndReformatCode(Collection<VirtualFile> files,
                                                      VcsConfiguration configuration,
                                                      Runnable finishAction,
                                                      boolean checkinProject);

  public void showError(final VcsException e, final String s) {
    showErrors(Arrays.asList(e), s);
  }

  public abstract void showAnnotation(FileAnnotation annotation, VirtualFile file);

  public abstract void showDifferences(final VcsFileRevision cvsVersionOn,
                                       final VcsFileRevision cvsVersionOn1,
                                       final File file);

  public abstract void showChangesBrowser(List<CommittedChangeList> changelists);
  public abstract void showChangesBrowser(List<CommittedChangeList> changelists, @Nls String title);
  public abstract void showChangesBrowser(CommittedChangeList changelist, @Nls String title);
  public abstract void showChangesBrowser(CommittedChangesProvider provider, final VirtualFile root, @Nls String title);

  @Nullable public abstract <T extends CommittedChangeList> T chooseCommittedChangeList(CommittedChangesProvider<T> provider);

  public abstract void showMergeDialog(List<VirtualFile> files, MergeProvider provider, final AnActionEvent e);

  /**
   * Performs pre-checkin code analysis on the specified files.
   *
   * @param files the files to analyze.
   * @return the list of problems found during the analysis.
   * @throws ProcessCanceledException if the analysis was cancelled by the user.
   * @since 5.1
   */
  public abstract List<CodeSmellInfo> findCodeSmells(List<VirtualFile> files) throws ProcessCanceledException;

  /**
   * Shows the specified list of problems found during pre-checkin code analysis in a Messages pane.
   *
   * @param smells the problems to show.
   * @since 5.1
   */
  public abstract void showCodeSmellErrors(final List<CodeSmellInfo> smells);

  public abstract void showFileHistory(VcsHistoryProvider vcsHistoryProvider, FilePath path);

  /**
   * Shows the "Rollback Changes" dialog with the specified list of changes.
   *
   * @param changes the changes to show in the dialog.
   */
  public abstract void showRollbackChangesDialog(List<Change> changes);

}