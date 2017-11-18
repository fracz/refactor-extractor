/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package com.intellij.vcs.log.ui.actions.history;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.history.CurrentRevision;
import com.intellij.openapi.vcs.history.StandardDiffFromHistoryHandler;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ObjectUtils.notNull;

public class ShowDiffWithLocalFromHistoryAction extends FileHistorySingleCommitAction {

  @Override
  protected boolean isEnabled(@NotNull AnActionEvent e) {
    FilePath filePath = e.getData(VcsDataKeys.FILE_PATH);
    if (filePath == null || filePath.isDirectory() || filePath.getVirtualFile() == null) {
      // currently not working for directories, to be fixed later
      return false;
    }
    return e.getData(VcsDataKeys.VCS_FILE_REVISION) != null;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getRequiredData(CommonDataKeys.PROJECT);
    if (ChangeListManager.getInstance(project).isFreezedWithNotification(null)) return;

    FilePath path = e.getRequiredData(VcsDataKeys.FILE_PATH);
    VcsFileRevision revision = e.getRequiredData(VcsDataKeys.VCS_FILE_REVISION);

    StandardDiffFromHistoryHandler handler = new StandardDiffFromHistoryHandler();
    handler.showDiffForTwo(project, path, revision, new CurrentRevision(notNull(path.getVirtualFile()), VcsRevisionNumber.NULL));
  }
}