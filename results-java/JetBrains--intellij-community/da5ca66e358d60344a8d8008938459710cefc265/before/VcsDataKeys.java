/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
 * Date: 23.10.2006
 * Time: 18:13:58
 */
package com.intellij.openapi.vcs;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeRequestChain;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.List;

public interface VcsDataKeys {
  DataKey<File[]> IO_FILE_ARRAY = DataKey.create(VcsDataConstants.IO_FILE_ARRAY);
  DataKey<File> IO_FILE = DataKey.create(VcsDataConstants.IO_FILE);
  DataKey<VcsFileRevision> VCS_FILE_REVISION = DataKey.create(VcsDataConstants.VCS_FILE_REVISION);
  DataKey<VcsFileRevision[]> VCS_FILE_REVISIONS = DataKey.create(VcsDataConstants.VCS_FILE_REVISIONS);
  DataKey<VirtualFile> VCS_VIRTUAL_FILE = DataKey.create(VcsDataConstants.VCS_VIRTUAL_FILE);
  DataKey<FilePath> FILE_PATH = DataKey.create(VcsDataConstants.FILE_PATH);
  DataKey<FilePath[]> FILE_PATH_ARRAY = DataKey.create(VcsDataConstants.FILE_PATH_ARRAY);
  DataKey<ChangeList[]> CHANGE_LISTS = DataKey.create(DataConstants.CHANGE_LISTS);
  DataKey<Change[]> CHANGES = DataKey.create(DataConstants.CHANGES);
  DataKey<Change[]> CHANGES_WITH_MOVED_CHILDREN = DataKey.create("ChangeListView.ChangesWithDetails");
  @NonNls DataKey<List<Change>> CHANGES_IN_LIST_KEY = DataKey.create("ChangeListView.ChangesInList");
  @NonNls DataKey<List<VirtualFile>> MODIFIED_WITHOUT_EDITING_DATA_KEY = DataKey.create("ChangeListView.ModifiedWithoutEditing");
  DataKey<Change[]> SELECTED_CHANGES = DataKey.create("ChangeListView.SelectedChange");
  DataKey<Change[]> CHANGE_LEAD_SELECTION = DataKey.create("ChangeListView.ChangeLeadSelection");
  DataKey<ChangeRequestChain> DIFF_REQUEST_CHAIN = DataKey.create("diffRequestChain");
  DataKey<String> UPDATE_VIEW_SELECTED_PATH = DataKey.create("AbstractCommonUpdateAction.UpdateViewSelectedPath");
  DataKey<Iterable<VirtualFilePointer>> UPDATE_VIEW_FILES_ITERABLE = DataKey.create("AbstractCommonUpdateAction.UpdatedFilesIterable");
  DataKey<Object> CHECKPOINT_BEFORE = DataKey.create("CHECKPOINT_BEFORE");
  DataKey<Object> CHECKPOINT_AFTER = DataKey.create("CHECKPOINT_AFTER");
}