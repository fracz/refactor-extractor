package com.jetbrains.python.documentation;

import com.google.common.base.CharMatcher;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyDocStringOwner;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class DocStringReferenceProvider extends PsiReferenceProvider {
  private final String[] ALL_PARAM_TAGS;

  public DocStringReferenceProvider() {
    List<String> allParamTags = new ArrayList<String>();
    for (String tag : EpydocString.PARAM_TAGS) {
      allParamTags.add("@" + tag);
      allParamTags.add(":" + tag);
    }
    for (String tag : EpydocString.RTYPE_TAGS) {
      allParamTags.add("@" + tag);
      allParamTags.add(":" + tag);
    }
    allParamTags.add("@type");
    allParamTags.add(":type");
    ALL_PARAM_TAGS = ArrayUtil.toStringArray(allParamTags);
  }

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull ProcessingContext context) {
    final PyDocStringOwner docStringOwner = PsiTreeUtil.getParentOfType(element, PyDocStringOwner.class);
    if (docStringOwner != null && element == docStringOwner.getDocStringExpression()) {
      final List<PsiReference> result = new ArrayList<PsiReference>();
      String docString = element.getText();
      int pos = 0;
      while (pos < docString.length()) {
        final TextRange tagRange = findNextTag(docString, pos, ALL_PARAM_TAGS);
        if (tagRange == null) {
          break;
        }
        pos = CharMatcher.anyOf(" \t*").negate().indexIn(docString, tagRange.getEndOffset());
        CharMatcher identifierMatcher = new CharMatcher() {
                                        @Override public boolean matches(char c) {
                                          return Character.isLetterOrDigit(c) || c == '_';
                                        }}.negate();
        final String tagName = docString.substring(tagRange.getStartOffset(), tagRange.getEndOffset());
        boolean isRType = isReturnType(tagName);
        if (tagName.startsWith(":") && !isRType) {           // if ReST parameter tag
          int ws = CharMatcher.anyOf(" \t*").indexIn(docString, pos+1);
          if (ws != -1) {
            int next = CharMatcher.anyOf(" \t*").negate().indexIn(docString, ws);
            if (next != -1 && !docString.substring(pos, next).contains(":")) {
              int endPos = identifierMatcher.indexIn(docString, pos);
              result.addAll(parseTypeReferences(element, docString.substring(pos, endPos), pos));
              pos = next;
            }
          }
        }
        int endPos = identifierMatcher.indexIn(docString, pos);
        if (endPos < 0) {
          endPos = docString.length();
        }
        if (!isRType)
          result.add(new DocStringParameterReference(element, new TextRange(pos, endPos)));
        if (tagName.equals(":type") || tagName.equals("@type") || isRType) {
          pos = CharMatcher.anyOf(" \t*").negate().indexIn(docString, endPos+1);
          endPos = CharMatcher.anyOf("\n\r").indexIn(docString, pos+1);
          if (endPos == -1) {
            endPos = pos;
          }
          result.addAll(parseTypeReferences(element, docString.substring(pos, endPos), pos));
        }
        pos = endPos;
      }

      return result.toArray(new PsiReference[result.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static List<PsiReference> parseTypeReferences(PsiElement anchor, String s, int offset) {
    final List<PsiReference> result = new ArrayList<PsiReference>();
    final PyTypeParser.ParseResult parseResult = PyTypeParser.parse(anchor, s);
    final Map<TextRange, PyType> types = parseResult.getTypes();
    final Map<PyType, TextRange> fullRanges = parseResult.getFullRanges();
    for (Map.Entry<TextRange, PyType> pair : types.entrySet()) {
      final PyType t = pair.getValue();
      final TextRange range = pair.getKey().shiftRight(offset);
      final TextRange fullRange = fullRanges.containsKey(t) ? fullRanges.get(t).shiftRight(offset) : range;
      result.add(new DocStringTypeReference(anchor, range, fullRange, t));
    }
    return result;
  }

  private static boolean isReturnType(String tagName) {
    return tagName.equals(":rtype") || tagName.equals("@rtype") || tagName.equals("@returntype");
  }

  @Nullable
  public static TextRange findNextTag(String docString, int pos, String[] paramTags) {
    int result = Integer.MAX_VALUE;
    String foundTag = null;
    for (String paramTag : paramTags) {
      int tagPos = docString.indexOf(paramTag, pos);
      while(tagPos >= 0 && tagPos + paramTag.length() < docString.length() &&
            Character.isLetterOrDigit(docString.charAt(tagPos + paramTag.length()))) {
        tagPos = docString.indexOf(paramTag, tagPos+1);
      }
      if (tagPos >= 0 && tagPos < result) {
        foundTag = paramTag;
        result = tagPos;
      }
    }
    return foundTag == null ? null : new TextRange(result, result + foundTag.length());
  }
}