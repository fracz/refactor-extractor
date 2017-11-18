package com.intellij.codeInsight.highlighting;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public abstract class HighlightUsagesHandlerBase<T extends PsiElement> {
  protected final Editor myEditor;
  protected final PsiFile myFile;

  protected List<TextRange> myReadUsages = new ArrayList<TextRange>();
  protected List<TextRange> myWriteUsages = new ArrayList<TextRange>();
  protected String myStatusText;
  protected String myHintText;

  protected HighlightUsagesHandlerBase(final Editor editor, final PsiFile file) {
    myEditor = editor;
    myFile = file;
  }

  public void highlightUsages() {
    List<T> targets = getTargets();
    if (targets == null) {
      return;
    }
    selectTargets(targets, new Consumer<List<T>>() {
      public void consume(final List<T> targets) {
        computeUsages(targets);
        performHighlighting();
      }
    });
  }

  protected void performHighlighting() {
    boolean clearHighlights = HighlightUsagesHandler.isClearHighlights(myEditor);
    EditorColorsManager manager = EditorColorsManager.getInstance();
    TextAttributes attributes = manager.getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
    TextAttributes writeAttributes = manager.getGlobalScheme().getAttributes(EditorColors.WRITE_SEARCH_RESULT_ATTRIBUTES);
    HighlightUsagesHandler.highlightRanges(HighlightManager.getInstance(myEditor.getProject()),
                                           myEditor, attributes, clearHighlights, myReadUsages);
    HighlightUsagesHandler.highlightRanges(HighlightManager.getInstance(myEditor.getProject()),
                                           myEditor, writeAttributes, clearHighlights, myWriteUsages);
    if (!clearHighlights) {
      WindowManager.getInstance().getStatusBar(myEditor.getProject()).setInfo(myStatusText);
    }
    if (myHintText != null) {
      HintManager.getInstance().showInformationHint(myEditor, myHintText);
    }
  }

  protected void buildStatusText(@Nullable String elementName, int refCount) {
    if (refCount > 0) {
      myStatusText = CodeInsightBundle.message(elementName != null ?
                                        "status.bar.highlighted.usages.message" :
                                        "status.bar.highlighted.usages.no.target.message", refCount, elementName,
                                               HighlightUsagesHandler.getShortcutText());
    }
    else {
      myStatusText = CodeInsightBundle.message(elementName != null ?
                                          "status.bar.highlighted.usages.not.found.message" :
                                          "status.bar.highlighted.usages.not.found.no.target.message", elementName);
    }
  }

  protected abstract List<T> getTargets();

  protected abstract void selectTargets(List<T> targets, Consumer<List<T>> selectionConsumer);

  protected abstract void computeUsages(List<T> targets);

  protected void addOccurrence(final PsiElement element) {
    TextRange range = element.getTextRange();
    range = InjectedLanguageManager.getInstance(element.getProject()).injectedToHost(element, range);
    myReadUsages.add(range);
  }
}