package com.intellij.codeInsight.folding.impl;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

class FoldingUtil {
  private FoldingUtil() {}

  @Nullable
  public static FoldRegion findFoldRegion(Editor editor, int startOffset, int endOffset) {
    FoldRegion[] foldRegions = ((FoldingModelEx)editor.getFoldingModel()).getAllFoldRegionsIncludingInvalid();
    for (FoldRegion region : foldRegions) {
      if (region.isValid() &&
          region.getStartOffset() == startOffset
          && region.getEndOffset() == endOffset) {
        return region;
      }
    }

    return null;
  }

  public static FoldRegion findFoldRegionStartingAtLine(Editor editor, int line){
    FoldRegion[] regions = editor.getFoldingModel().getAllFoldRegions();
    FoldRegion result = null;
    for (FoldRegion region : regions) {
      if (region.getDocument().getLineNumber(region.getStartOffset()) == line) {
        if (result != null) return null;
        result = region;
      }
    }
    return result;
  }

  public static FoldRegion[] getFoldRegionsAtOffset(Editor editor, int offset){
    ArrayList<FoldRegion> list = new ArrayList<FoldRegion>();
    FoldRegion[] allRegions = editor.getFoldingModel().getAllFoldRegions();
    for (FoldRegion region : allRegions) {
      if (region.getStartOffset() <= offset && offset <= region.getEndOffset()) {
        list.add(region);
      }
    }

    FoldRegion[] regions = list.toArray(new FoldRegion[list.size()]);
    Arrays.sort(
      regions,
      new Comparator<FoldRegion>() {
        public int compare(FoldRegion region1, FoldRegion region2) {
          return region2.getStartOffset() - region1.getStartOffset();
        }
      }
    );

    return regions;
  }

  public static boolean caretInsideRange(final Editor editor, final TextRange range) {
    final int offset = editor.getCaretModel().getOffset();
    return range.contains(offset) && range.getStartOffset() != offset;
  }
}