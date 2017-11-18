/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.intellij.dvcs.actions;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.dvcs.repo.AbstractRepositoryManager;
import com.intellij.dvcs.repo.Repository;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Compares selected file/folder with itself in another branch.
 */
public abstract class DvcsCompareWithBranchAction<T extends Repository> extends DumbAwareAction {

  protected static final Logger LOG = Logger.getInstance(DvcsCompareWithBranchAction.class.getName());

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getRequiredData(CommonDataKeys.PROJECT);
    VirtualFile file = getAffectedFile(event);
    T repository = getRepositoryManager(project).getRepositoryForFile(file);
    assert repository != null;

    String currentBranchName = repository.getCurrentBranchName();
    String head = currentBranchName;
    if (currentBranchName == null) {
      String currentRevision = repository.getCurrentRevision();
      if (currentRevision == null) {
        LOG.error("Current revision is null for " + repository + ". Compare with branch shouldn't be available for fresh repository");
        return;
      }
      head = DvcsUtil.getShortHash(currentRevision);
    }
    List<String> branchNames = getBranchNamesExceptCurrent(repository);

    JBList list = new JBList(branchNames);
    JBPopupFactory.getInstance()
      .createListPopupBuilder(list)
      .setTitle("Select branch to compare")
      .setItemChoosenCallback(new OnBranchChooseRunnable(project, file, head, list))
      .setAutoselectOnMouseMove(true)
      .setFilteringEnabled(new Function<Object, String>() {
        @Override
        public String fun(Object o) {
          return o.toString();
        }
      })
      .createPopup()
      .showInBestPositionFor(event.getDataContext());
  }

  @NotNull
  protected abstract List<String> getBranchNamesExceptCurrent(@NotNull T repository);

  private static VirtualFile getAffectedFile(@NotNull AnActionEvent event) {
    final VirtualFile[] vFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    assert vFiles != null && vFiles.length == 1 && vFiles[0] != null : "Illegal virtual files selected: " + Arrays.toString(vFiles);
    return vFiles[0];
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    Project project = e.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }

    VirtualFile[] vFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (vFiles == null || vFiles.length != 1 || vFiles[0] == null) { // only 1 file for now
      presentation.setEnabled(false);
      presentation.setVisible(true);
      return;
    }

    AbstractRepositoryManager<T> manager = getRepositoryManager(project);

    T repository = manager.getRepositoryForFile(vFiles[0]);
    if (repository == null || repository.isFresh() || noBranchesToCompare(repository)) {
      presentation.setEnabled(false);
      presentation.setVisible(true);
      return;
    }

    presentation.setEnabled(true);
    presentation.setVisible(true);
  }

  @NotNull
  protected abstract AbstractRepositoryManager<T> getRepositoryManager(@NotNull Project project);

  protected abstract boolean noBranchesToCompare(@NotNull T repository);

  protected abstract void showDiffWithBranch(@NotNull Project project, @NotNull VirtualFile file, @NotNull String head,
                                             @NotNull String branchToCompare) throws VcsException;

  private class OnBranchChooseRunnable implements Runnable {
    private final Project myProject;
    private final VirtualFile myFile;
    private final String myHead;
    private final JList myList;

    OnBranchChooseRunnable(@NotNull Project project, @NotNull VirtualFile file, @NotNull String head, @NotNull JList list) {
      myProject = project;
      myFile = file;
      myHead = head;
      myList = list;
    }

    @Override
    public void run() {
      Object selectedValue = myList.getSelectedValue();
      if (selectedValue == null) {
        LOG.error("Selected value is unexpectedly null");
        return;
      }
      String branchToCompare = selectedValue.toString();
      try {
        showDiffWithBranch(myProject, myFile, myHead, branchToCompare);
      }
      catch (VcsException e) {
        if (e.getMessage().contains("exists on disk, but not in")) {
          fileDoesntExistInBranchError(myProject, myFile, branchToCompare);
        }
        else {
          VcsNotifier.getInstance(myProject).notifyImportantWarning("Couldn't compare with branch",
                                                                    String.format("Couldn't compare file [%s] with selected branch [%s] ;",
                                                                                  myFile, selectedValue) + e.getMessage());
        }
      }
    }
  }

  public static void fileDoesntExistInBranchError(@NotNull Project project, @NotNull VirtualFile file, @NotNull String branchToCompare) {
    VcsNotifier.getInstance(project).notifyImportantWarning(DvcsUtil.fileOrFolder(file) + " doesn't exist in branch",
                                                            String.format("%s <code>%s</code> doesn't exist in branch <code>%s</code>",
                                                                          DvcsUtil.fileOrFolder(file), file.getPresentableUrl(),
                                                                          branchToCompare));
  }
}