package org.jetbrains.plugins.groovy.lang.parser.parsing.util;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Utility class, that contains various useful methods for
 * parser needs.
 *
 * @author Ilya Sergey
 */
public abstract class ParserUtils {

  /**
   * Auxiliary method for strict token appearance
   *
   * @param builder  current builder
   * @param elem     given element
   * @param errorMsg Message, that displays if element was not found; if errorMsg == null nothing displays
   * @return true if element parsed
   */
  public static boolean getToken(PsiBuilder builder, IElementType elem, String errorMsg) {
    if (elem.equals(builder.getTokenType())) {
      builder.advanceLexer();
      return true;
    } else {
      if (errorMsg != null)
        builder.error(errorMsg);
      return false;
    }
  }

  /**
   * Auxiliary method for construction like
   * <BNF>
   * token?
   * </BNF>
   * parsing
   *
   * @param builder current builder
   * @param elem    given element
   * @return true if element parsed
   */
  public static boolean getToken(PsiBuilder builder, IElementType elem) {
    return getToken(builder, elem, null);
  }

  /**
   * Checks, that following element sequence is like given
   *
   * @param builder Given PsiBuilder
   * @param elems   Array of need elements in order
   * @return true if following sequence is like a given
   */
  public static boolean lookAhead(PsiBuilder builder, IElementType... elems) {

    if (elems.length == 1) {
      return elems[0].equals(builder.getTokenType());
    }

    PsiBuilder.Marker rb = builder.mark();
    int i = 0;
    for (; !builder.eof() && i < elems.length && elems[i].equals(builder.getTokenType());) {
      builder.advanceLexer();
      i++;
    }
    rb.rollbackTo();
    return i == elems.length;
  }

  /**
   * Wraps current token to node with specified element type
   *
   * @param builder Given builder
   * @param elem Node element
   */
  public static void eatElement(PsiBuilder builder, IElementType elem){
    PsiBuilder.Marker marker = builder.mark();
    builder.advanceLexer();
    marker.done(elem);
  }

}