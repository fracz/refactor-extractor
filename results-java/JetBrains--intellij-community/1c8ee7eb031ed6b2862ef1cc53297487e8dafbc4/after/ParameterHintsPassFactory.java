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
package com.intellij.codeInsight.hints;

import com.intellij.codeHighlighting.EditorBoundHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.codeInsight.daemon.impl.ParameterHintsPresentationManager;
import com.intellij.codeInsight.hints.filtering.Matcher;
import com.intellij.codeInsight.hints.filtering.MatcherConstructor;
import com.intellij.codeInsight.hints.settings.Diff;
import com.intellij.codeInsight.hints.settings.ParameterNameHintsSettings;
import com.intellij.lang.Language;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.editor.ex.util.CaretVisualPositionKeeper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ParameterHintsPassFactory extends AbstractProjectComponent implements TextEditorHighlightingPassFactory {

  public ParameterHintsPassFactory(Project project, TextEditorHighlightingPassRegistrar registrar) {
    super(project);
    registrar.registerTextEditorHighlightingPass(this, null, null, false, -1);
  }

  @Nullable
  @Override
  public TextEditorHighlightingPass createHighlightingPass(@NotNull PsiFile file, @NotNull Editor editor) {
    if (editor.isOneLineMode()) return null;
    return new ParameterHintsPass(file, editor);
  }

  private static class ParameterHintsPass extends EditorBoundHighlightingPass {
    private final Map<Integer, String> myHints = new HashMap<>();
    private final Map<Integer, String> myShowOnlyIfExistedBeforeHints = new HashMap<>();

    private ParameterHintsPass(@NotNull PsiFile file, @NotNull Editor editor) {
      super(editor, file, true);
    }

    @Override
    public void doCollectInformation(@NotNull ProgressIndicator progress) {
      assert myDocument != null;
      myHints.clear();
      if (!isEnabled()) return;

      Language language = myFile.getLanguage();
      InlayParameterHintsProvider provider = InlayParameterHintsExtension.INSTANCE.forLanguage(language);
      if (provider == null) return;

      Set<String> blackList = getBlackList(language);
      Language dependentLanguage = provider.getBlackListDependencyLanguage();
      if (dependentLanguage != null) {
        blackList.addAll(getBlackList(dependentLanguage));
      }

      List<Matcher> matchers = blackList
        .stream()
        .map((item) -> MatcherConstructor.INSTANCE.createMatcher(item))
        .filter((e) -> e != null)
        .collect(Collectors.toList());

      SyntaxTraverser.psiTraverser(myFile).forEach(element -> process(element, provider, matchers));
    }

    private static Set<String> getBlackList(Language language) {
      InlayParameterHintsProvider provider = InlayParameterHintsExtension.INSTANCE.forLanguage(language);
      if (provider != null) {
        ParameterNameHintsSettings settings = ParameterNameHintsSettings.getInstance();
        Diff diff = settings.getBlackListDiff(language);
        return diff.applyOn(provider.getDefaultBlackList());
      }
      return ContainerUtil.newHashOrEmptySet(ContainerUtil.emptyIterable());
    }

    private static boolean isEnabled() {
      return EditorSettingsExternalizable.getInstance().isShowParameterNameHints();
    }

    private static boolean isMatchedByAny(HintInfo info, List<Matcher> matchers) {
      if (info instanceof HintInfo.MethodInfo) {
        HintInfo.MethodInfo methodInfo = (HintInfo.MethodInfo)info;
        return matchers.stream().anyMatch((e) -> e.isMatching(methodInfo.getFullyQualifiedName(), methodInfo.getParamNames()));
      }
      return false;
    }

    private void process(PsiElement element, InlayParameterHintsProvider provider, List<Matcher> blackListMatchers) {
      List<InlayInfo> hints = provider.getParameterHints(element);
      if (hints.isEmpty()) return;
      HintInfo info = provider.getHintInfo(element);
      if (info == null || !isMatchedByAny(info, blackListMatchers)) {
        hints.forEach((hint) -> {
          String presentation = provider.getInlayPresentation(hint.getText());
          int offset = hint.getOffset();
          if (hint.isShowOnlyIfExistedBefore()) {
            myShowOnlyIfExistedBeforeHints.put(offset, presentation);
          }
          else {
            myHints.put(offset, presentation);
          }
        });
      }
    }

    @Override
    public void doApplyInformationToEditor() {
      CaretVisualPositionKeeper keeper = new CaretVisualPositionKeeper(myEditor);
      ParameterHintsUpdater updater = new ParameterHintsUpdater(myEditor, getAllHints(), myHints, myShowOnlyIfExistedBeforeHints);
      updater.update();
      keeper.restoreOriginalLocation();
    }

    private Map<Integer, Inlay> getAllHints() {
      assert myDocument != null;
      ParameterHintsPresentationManager manager = ParameterHintsPresentationManager.getInstance();
      return myEditor.getInlayModel()
        .getInlineElementsInRange(0, myDocument.getTextLength())
        .stream()
        .filter((inlay) -> manager.isParameterHint(inlay))
        .collect(Collectors.toMap((inlay) -> inlay.getOffset(), (inlay) -> inlay));
    }
  }
}