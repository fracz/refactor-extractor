package com.jetbrains.python.inspections;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author yole
 */
public class PyStringFormatParser {
  public static abstract class FormatStringChunk {
    private final int myStartIndex;
    protected int myEndIndex;

    public FormatStringChunk(int startIndex, int endIndex) {
      myStartIndex = startIndex;
      myEndIndex = endIndex;
    }

    public int getStartIndex() {
      return myStartIndex;
    }

    public int getEndIndex() {
      return myEndIndex;
    }
  }

  public static class ConstantChunk extends FormatStringChunk {

    public ConstantChunk(int startIndex, int endIndex) {
      super(startIndex, endIndex);
    }
  }

  public static class SubstitutionChunk extends FormatStringChunk {
    @Nullable private String myMappingKey;
    @Nullable private String myConversionFlags;
    @Nullable private String myWidth;
    @Nullable private String myPrecision;
    private char myLengthModifier;
    private char myConversionType;
    private boolean myUnclosedMapping;

    public SubstitutionChunk(int startIndex) {
      super(startIndex, startIndex);
    }

    private void setEndIndex(int endIndex) {
      myEndIndex = endIndex;
    }

    public char getConversionType() {
      return myConversionType;
    }

    private void setConversionType(char conversionType) {
      myConversionType = conversionType;
    }

    @Nullable
    public String getMappingKey() {
      return myMappingKey;
    }

    private void setMappingKey(@Nullable String mappingKey) {
      myMappingKey = mappingKey;
    }

    @Nullable
    public String getConversionFlags() {
      return myConversionFlags;
    }

    private void setConversionFlags(@Nullable String conversionFlags) {
      myConversionFlags = conversionFlags;
    }

    @Nullable
    public String getWidth() {
      return myWidth;
    }

    private void setWidth(@Nullable String width) {
      myWidth = width;
    }

    @Nullable
    public String getPrecision() {
      return myPrecision;
    }

    private void setPrecision(@Nullable String precision) {
      myPrecision = precision;
    }

    public char getLengthModifier() {
      return myLengthModifier;
    }

    private void setLengthModifier(char lengthModifier) {
      myLengthModifier = lengthModifier;
    }

    public boolean isUnclosedMapping() {
      return myUnclosedMapping;
    }

    private void setUnclosedMapping(boolean unclosedMapping) {
      myUnclosedMapping = unclosedMapping;
    }
  }

  @NotNull private final String myLiteral;
  @NotNull private final List<FormatStringChunk> myResult = new ArrayList<FormatStringChunk>();
  private int myPos;

  private static final String CONVERSION_FLAGS = "#0- +";
  private static final String DIGITS = "0123456789";
  private static final String LENGTH_MODIFIERS = "hlL";
  private static final String VALID_CONVERSION_TYPES = "diouxXeEfFgGcrs";

  public PyStringFormatParser(@NotNull String literal) {
    myLiteral = literal;
  }

  @NotNull
  public List<FormatStringChunk> parse() {
    myPos = 0;
    while(myPos < myLiteral.length()) {
      int next = myLiteral.indexOf('%', myPos);
      while(next >= 0 && next < myLiteral.length()-1 && myLiteral.charAt(next+1) == '%') {
        next = myLiteral.indexOf('%', next+2);
      }
      if (next < 0) break;
      if (next > myPos) {
        myResult.add(new ConstantChunk(myPos, next));
      }
      myPos = next;
      parseSubstitution();
    }
    if (myPos < myLiteral.length()) {
      myResult.add(new ConstantChunk(myPos, myLiteral.length()));
    }
    return myResult;
  }

  private void parseSubstitution() {
    assert myLiteral.charAt(myPos) == '%';
    SubstitutionChunk chunk = new SubstitutionChunk(myPos);
    myResult.add(chunk);
    myPos++;
    if (isAt('(')) {
      int mappingEnd = myLiteral.indexOf(')', myPos+1);
      if (mappingEnd < 0) {
        chunk.setEndIndex(myLiteral.length());
        chunk.setMappingKey(myLiteral.substring(myPos+1));
        chunk.setUnclosedMapping(true);
        myPos = myLiteral.length();
        return;
      }
      chunk.setMappingKey(myLiteral.substring(myPos+1, mappingEnd));
      myPos = mappingEnd+1;
    }
    chunk.setConversionFlags(parseWhileCharacterInSet(CONVERSION_FLAGS));
    chunk.setWidth(parseWidth());
    if (isAt('.')) {
      myPos++;
      chunk.setPrecision(parseWidth());
    }
    if (isAtSet(LENGTH_MODIFIERS)) {
      chunk.setLengthModifier(myLiteral.charAt(myPos));
      myPos++;
    }
    if (isAtSet(VALID_CONVERSION_TYPES)) {
      chunk.setConversionType(myLiteral.charAt(myPos));
      myPos++;
    }
    chunk.setEndIndex(myPos);
  }

  private boolean isAtSet(@NotNull final String characterSet) {
    return myPos < myLiteral.length() && characterSet.indexOf(myLiteral.charAt(myPos)) >= 0;
  }

  private boolean isAt(final char c) {
    return myPos < myLiteral.length() && myLiteral.charAt(myPos) == c;
  }

  @NotNull
  private String parseWidth() {
    if (isAt('*')) {
      myPos++;
      return "*";
    }
    return parseWhileCharacterInSet(DIGITS);
  }

  @NotNull
  private String parseWhileCharacterInSet(@NotNull final String characterSet) {
    int flagStart = myPos;
    while(isAtSet(characterSet)) {
      myPos++;
    }
    return myLiteral.substring(flagStart, myPos);
  }

  @NotNull
  public List<SubstitutionChunk> parseSubstitutions() {
    List<SubstitutionChunk> result = new ArrayList<SubstitutionChunk>();
    for (FormatStringChunk chunk : parse()) {
      if (chunk instanceof SubstitutionChunk) {
        result.add((SubstitutionChunk) chunk);
      }
    }
    return result;
  }

  @NotNull
  public static List<SubstitutionChunk> getPositionalSubstitutions(@NotNull List<SubstitutionChunk> substitutions) {
    final ArrayList<SubstitutionChunk> result = new ArrayList<SubstitutionChunk>();
    for (SubstitutionChunk s : substitutions) {
      if (s.getMappingKey() == null) {
        result.add(s);
      }
    }
    return result;
  }

  @NotNull
  public static Map<String, SubstitutionChunk> getKeywordSubstitutions(@NotNull List<SubstitutionChunk> substitutions) {
    final Map<String, SubstitutionChunk> result = new HashMap<String, SubstitutionChunk>();
    for (SubstitutionChunk s : substitutions) {
      final String key = s.getMappingKey();
      if (key != null) {
        result.put(key, s);
      }
    }
    return result;
  }


  @NotNull
  public static List<TextRange> substitutionsToRanges(@NotNull List<SubstitutionChunk> substitutions) {
    final List<TextRange> ranges = new ArrayList<TextRange>();
    for (SubstitutionChunk substitution : substitutions) {
      ranges.add(TextRange.create(substitution.getStartIndex(), substitution.getEndIndex()));
    }
    return ranges;
  }

  /**
   * Return the RHS operand of %-based string literal format expression.
   */
  @Nullable
  public static PyExpression getFormatValueExpression(@NotNull PyStringLiteralExpression element) {
    final PsiElement parent = element.getParent();
    if (parent instanceof PyBinaryExpression) {
      final PyBinaryExpression binaryExpr = (PyBinaryExpression)parent;
      if (binaryExpr.isOperator("%")) {
        PyExpression expr = binaryExpr.getRightExpression();
        while (expr instanceof PyParenthesizedExpression) {
          expr = ((PyParenthesizedExpression)expr).getContainedExpression();
        }
        return expr;
      }
    }
    return null;
  }

  /**
   * Return the argument list of the str.format() literal format expression.
   */
  @Nullable
  public static PyArgumentList getNewStyleFormatValueExpression(@NotNull PyStringLiteralExpression element) {
    final PsiElement parent = element.getParent();
    if (parent instanceof PyQualifiedExpression) {
      final PyQualifiedExpression qualifiedExpr = (PyQualifiedExpression)parent;
      final String name = qualifiedExpr.getReferencedName();
      if ("format".equals(name)) {
        final PsiElement parent2 = qualifiedExpr.getParent();
        if (parent2 instanceof PyCallExpression) {
          final PyCallExpression callExpr = (PyCallExpression)parent2;
          return callExpr.getArgumentList();
        }
      }
    }
    return null;
  }

  @NotNull
  public static List<TextRange> getEscapeRanges(@NotNull String s) {
    final List<TextRange> ranges = new ArrayList<TextRange>();
    Matcher matcher = PyStringLiteralExpressionImpl.PATTERN_ESCAPE.matcher(s);
    while (matcher.find()) {
      ranges.add(TextRange.create(matcher.start(), matcher.end()));
    }
    return ranges;
  }
}