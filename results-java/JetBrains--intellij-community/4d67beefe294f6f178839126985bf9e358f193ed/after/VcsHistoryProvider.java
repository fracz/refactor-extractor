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
package com.intellij.openapi.vcs.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;


public interface VcsHistoryProvider {

  ColumnInfo[] getRevisionColumns();

  AnAction[] getAdditionalActions(final FileHistoryPanel panel);

  @Nullable
  @NonNls String getHelpId();

  VcsHistorySession createSessionFor(FilePath filePath) throws VcsException;

  //return null if your revisions cannot be tree
  @Nullable
  HistoryAsTreeProvider getTreeHistoryProvider();
}