package com.intellij.codeInsight.folding.impl;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.editor.impl.FoldRegionImpl;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class FoldingUpdate {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.folding.impl.FoldingUpdate");

  private static final Key<Object> LAST_UPDATE_STAMP_KEY = Key.create("LAST_UPDATE_STAMP_KEY");

  private FoldingUpdate() {
  }

  @Nullable
  public static Runnable updateFoldRegions(final Editor editor, PsiElement file, boolean applyDefaultState) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    final Project project = file.getProject();
    Document document = editor.getDocument();
    LOG.assertTrue(!PsiDocumentManager.getInstance(project).isUncommited(document));

    final long timeStamp = document.getModificationStamp();
    Object lastTimeStamp = editor.getUserData(LAST_UPDATE_STAMP_KEY);
    if (lastTimeStamp instanceof Long && ((Long)lastTimeStamp).longValue() == timeStamp && !applyDefaultState) return null;

    if (file instanceof PsiCompiledElement){
      file = ((PsiCompiledElement)file).getMirror();
    }

    TreeMap<PsiElement, FoldingDescriptor> elementsToFoldMap = null;
    final PsiElement[] psiRoots = ((PsiFile)file).getPsiRoots();
    for (PsiElement psiRoot : psiRoots) {
      TreeMap<PsiElement, FoldingDescriptor> fileElementsToFoldMap = FoldingPolicy.getElementsToFold(psiRoot, document);
      if (elementsToFoldMap == null) {
        elementsToFoldMap = fileElementsToFoldMap;
      }
      else {
        elementsToFoldMap.putAll(fileElementsToFoldMap);
      }
    }

    final Runnable operation = new UpdateFoldRegionsOperation(editor, elementsToFoldMap, applyDefaultState);
    return new Runnable() {
      public void run() {
        editor.getFoldingModel().runBatchFoldingOperationDoNotCollapseCaret(operation);
        editor.putUserData(LAST_UPDATE_STAMP_KEY, timeStamp);
      }
    };
  }

  private static class UpdateFoldRegionsOperation implements Runnable {
    private final Editor myEditor;
    private final boolean myApplyDefaultState;
    private final TreeMap<PsiElement, FoldingDescriptor> myElementsToFoldMap;

    private UpdateFoldRegionsOperation(Editor editor, TreeMap<PsiElement, FoldingDescriptor> elementsToFoldMap, boolean applyDefaultState) {
      myEditor = editor;
      myApplyDefaultState = applyDefaultState;
      myElementsToFoldMap = elementsToFoldMap;
    }

    public void run() {
      EditorFoldingInfo info = EditorFoldingInfo.get(myEditor);
      FoldingModelEx foldingModel = (FoldingModelEx)myEditor.getFoldingModel();
      FoldRegion[] foldRegions = foldingModel.getAllFoldRegions();
      HashMap<TextRange,Boolean> rangeToExpandStatusMap = new HashMap<TextRange, Boolean>();

      List<FoldRegion> toRemove = CollectionFactory.arrayList();
      for (FoldRegion region : foldRegions) {
        PsiElement element = info.getPsiElement(region);
        if (element != null && myElementsToFoldMap.containsKey(element)) {
          final FoldingDescriptor descriptor = myElementsToFoldMap.get(element);
          if (!region.isValid() ||
              region.getStartOffset() != descriptor.getRange().getStartOffset() ||
              region.getEndOffset() != descriptor.getRange().getEndOffset() ||
              !region.getPlaceholderText().equals(descriptor.getPlaceholderText())) {
            rangeToExpandStatusMap.put(descriptor.getRange(), region.isExpanded());
            toRemove.add(region);
          }
          else {
            myElementsToFoldMap.remove(element);
          }
        }
        else {
          if (region.isValid() && info.isLightRegion(region)) {
            boolean isExpanded = region.isExpanded();
            rangeToExpandStatusMap.put(new TextRange(region.getStartOffset(), region.getEndOffset()),
                                       isExpanded ? Boolean.TRUE : Boolean.FALSE);
          }
          else {
            toRemove.add(region);
          }
        }
      }

      for (final FoldRegion region : toRemove) {
        foldingModel.removeFoldRegion(region);
        info.removeRegion(region);
      }

      Map<FoldRegion, Boolean> shouldExpand = CollectionFactory.newTroveMap();
      for (final Map.Entry<PsiElement, FoldingDescriptor> entry : myElementsToFoldMap.entrySet()) {
        ProgressManager.getInstance().checkCanceled();
        PsiElement element = entry.getKey();
        final FoldingDescriptor descriptor = entry.getValue();
        TextRange range = descriptor.getRange();
        FoldRegion region = new FoldRegionImpl(myEditor, range.getStartOffset(), range.getEndOffset(), descriptor.getPlaceholderText(),
                                               descriptor.getGroup());
        if (!foldingModel.addFoldRegion(region)) continue;

        info.addRegion(region, element);

        if (myApplyDefaultState) {
          shouldExpand.put(region, !FoldingPolicy.isCollapseByDefault(element) || FoldingUtil.caretInsideRange(myEditor, range));
        }
        else {
          shouldExpand.put(region, rangeToExpandStatusMap.get(range));
        }
      }

      for (final FoldRegion region : shouldExpand.keySet()) {
        final Boolean expanded = shouldExpand.get(region);
        if (expanded != null) {
          region.setExpanded(expanded.booleanValue());
        }
      }
    }

  }
}