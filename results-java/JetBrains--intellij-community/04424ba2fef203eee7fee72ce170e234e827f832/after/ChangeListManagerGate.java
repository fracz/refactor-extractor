/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.openapi.vcs.changes;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * only to be used by {@link ChangeProvider} in order to create IDEA's peer changelist
 * in response to finding not registered VCS native list
 * it can NOT be done through {@link ChangeListManager} interface; it is for external/IDEA user modifications
 */
public interface ChangeListManagerGate {
  List<LocalChangeList> getListsCopy();
  @Nullable
  LocalChangeList findChangeList(final String name);
  LocalChangeList addChangeList(final String name, final String comment);
  LocalChangeList findOrCreateList(final String name, final String comment);

  void editComment(final String name, final String comment);
  void editName(final String oldName, final String newName);
  // must be allowed only for perforce change synchronizer, not during normal update
  void moveChanges(final String toList, final Collection<Change> changes);
  void deleteIfEmpty(final String name);
}