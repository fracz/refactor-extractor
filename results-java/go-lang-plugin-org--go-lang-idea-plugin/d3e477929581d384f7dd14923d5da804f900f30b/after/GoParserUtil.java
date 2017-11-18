package com.goide.parser;

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.lang.impl.PsiBuilderAdapter;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class GoParserUtil extends GeneratedParserUtilBase {
  private static final Key<TObjectIntHashMap<String>> MODES_KEY = Key.create("MODES_KEY");

  private static TObjectIntHashMap<String> getParsingModes(PsiBuilder builder_) {
    TObjectIntHashMap<String> flags = builder_.getUserDataUnprotected(MODES_KEY);
    if (flags == null) builder_.putUserDataUnprotected(MODES_KEY, flags = new TObjectIntHashMap<String>());
    return flags;
  }

  public static boolean emptyImportList(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
    PsiBuilder.Marker marker = getCurrentMarker(builder_ instanceof PsiBuilderAdapter ? ((PsiBuilderAdapter)builder_).getDelegate() : builder_);
    if (marker != null) {
      marker.setCustomEdgeTokenBinders(new WhitespacesAndCommentsBinder() {
        public int getEdgePosition(List<IElementType> tokens, boolean atStreamEdge, TokenTextGetter getter) {
          return 0;
        }
      }, null);
    }
    return true;
  }

  public static boolean isModeOn(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
    return getParsingModes(builder_).get(mode) > 0;
  }

  public static boolean isModeOff(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
    return getParsingModes(builder_).get(mode) == 0;
  }

  public static boolean enterMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
    TObjectIntHashMap<String> flags = getParsingModes(builder_);
    if (!flags.increment(mode)) flags.put(mode, 1);
    return true;
  }

  public static boolean exitMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
    TObjectIntHashMap<String> flags = getParsingModes(builder_);
    int count = flags.get(mode);
    if (count == 1) flags.remove(mode);
    else if (count > 1) flags.put(mode, count -1 );
    else builder_.error("Could not exit inactive '" + mode + "' mode at offset " + builder_.getCurrentOffset());
    return true;
  }

  public static boolean isBuiltin(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
    LighterASTNode marker = builder_.getLatestDoneMarker();
    if (marker == null) return false;
    String text = String.valueOf(builder_.getOriginalText().subSequence(marker.getStartOffset(), marker.getEndOffset()));
    return "make".equals(text) || "new".equals(text);
  }

  @Nullable
  private static PsiBuilder.Marker getCurrentMarker(PsiBuilder builder_) {
    try {
      Field field = builder_.getClass().getDeclaredField("myProduction");
      field.setAccessible(true);
      List production = (List)field.get(builder_);
      return (PsiBuilder.Marker)ContainerUtil.getLastItem(production);
    }
    catch (NoSuchFieldException e) {
      return null;
    }
    catch (IllegalAccessException e) {
      return null;
    }
  }
}