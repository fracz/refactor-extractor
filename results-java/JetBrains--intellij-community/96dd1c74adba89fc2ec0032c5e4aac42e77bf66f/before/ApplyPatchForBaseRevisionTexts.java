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
package com.intellij.openapi.vcs.changes.patch;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.PatchHunk;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.diff.impl.patch.apply.GenericPatchApplier;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.CalledInAny;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.util.ObjectUtils.chooseNotNull;

public class ApplyPatchForBaseRevisionTexts {

  private static final Logger LOG = Logger.getInstance(ApplyPatchForBaseRevisionTexts.class);

  @NotNull private final String myLocal;
  @Nullable private String myBase;
  private String myPatched;
  private boolean myIsAppliedSomehow;
  private final List<String> myWarnings;
  private boolean myBaseRevisionLoaded;

  @NotNull
  @CalledInAny
  public static ApplyPatchForBaseRevisionTexts create(final Project project, @NotNull final VirtualFile file, final FilePath pathBeforeRename,
                                                       final TextFilePatch patch, @Nullable final CharSequence baseContents) {
    assert !patch.isNewFile();
    final String beforeVersionId = patch.getBeforeVersionId();
    DefaultPatchBaseVersionProvider provider = null;
    if (beforeVersionId != null) {
      provider = new DefaultPatchBaseVersionProvider(project, file, beforeVersionId);
    }
    if (provider != null && provider.canProvideContent()) {
      return new ApplyPatchForBaseRevisionTexts(provider, pathBeforeRename, patch, file, baseContents);
    } else {
      return new ApplyPatchForBaseRevisionTexts(null, pathBeforeRename, patch, file, baseContents);
    }
  }

  @CalledInAny
  private ApplyPatchForBaseRevisionTexts(final DefaultPatchBaseVersionProvider provider,
                                         final FilePath pathBeforeRename,
                                         final TextFilePatch patch,
                                         @NotNull final VirtualFile file,
                                         @Nullable CharSequence baseContents) {
    myWarnings = new ArrayList<>();
    myLocal = getLocalFileContent(file);

    final List<PatchHunk> hunks = patch.getHunks();

    if (baseContents != null) {
      myBase = StringUtil.convertLineSeparators(baseContents.toString());
      myBaseRevisionLoaded = true;
      final GenericPatchApplier applier = new GenericPatchApplier(myBase, hunks);
      if (!applier.execute()) {
        myIsAppliedSomehow = true;
        LOG.warn(
          String.format("Patch for %s has wrong base and can't be applied properly",
                        chooseNotNull(patch.getBeforeName(), patch.getAfterName())));
        applier.trySolveSomehow();
      }
      setPatched(applier.getAfter());
      return;
    }

    if (provider != null) {
      try {
        provider.getBaseVersionContent(pathBeforeRename, text -> {
          final GenericPatchApplier applier = new GenericPatchApplier(text, hunks);
          if (!applier.execute()) {
            return true;
          }
          myBase = text;
          myBaseRevisionLoaded = true;
          setPatched(applier.getAfter());
          return false;
        }, myWarnings);
      }
      catch (VcsException e) {
        myWarnings.add(e.getMessage());
      }
      if (myPatched != null) return;
    }

    final GenericPatchApplier applier = new GenericPatchApplier(myLocal, hunks);
    if (!applier.execute()) {
      myIsAppliedSomehow = true;
      applier.trySolveSomehow();
    }
    setPatched(applier.getAfter());
  }

  @NotNull
  private static String getLocalFileContent(@NotNull VirtualFile file) {
    return ReadAction.compute(() -> {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (document != null) {
        return document.getText();
      }
      return LoadTextUtil.loadText(file).toString();
    });
  }

  @NotNull
  public String getLocal() {
    return myLocal;
  }

  @Nullable
  public String getBase() {
    return myBase;
  }

  public void clearBase() {
    myBase = null;
  }

  private void setPatched(final String text) {
    myPatched = StringUtil.convertLineSeparators(text);
  }

  public String getPatched() {
    return myPatched;
  }

  public boolean isAppliedSomehow() {
    return myIsAppliedSomehow;
  }

  public boolean isBaseRevisionLoaded() {
    return myBaseRevisionLoaded;
  }
}