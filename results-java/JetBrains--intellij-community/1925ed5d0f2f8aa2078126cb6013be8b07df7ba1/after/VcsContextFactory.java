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
package com.intellij.openapi.vcs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public interface VcsContextFactory {
  VcsContext createCachedContextOn(AnActionEvent event);

  VcsContext createContextOn(final AnActionEvent event);

  FilePath createFilePathOn(VirtualFile virtualFile);

  FilePath createFilePathOn(File file);

  FilePath createFilePathOnDeleted(File file, boolean isDirectory);

  FilePath createFilePathOn(File file, boolean isDirectory);

  FilePath createFilePathOnNonLocal(String path, boolean isDirectory);

  FilePath createFilePathOn(VirtualFile parent, String name);

  LocalChangeList createLocalChangeList(final String description);
}