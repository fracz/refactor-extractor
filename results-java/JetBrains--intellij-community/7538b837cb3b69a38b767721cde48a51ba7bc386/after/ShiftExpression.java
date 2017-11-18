/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.arithmetic;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author Ilya.Sergey
 */
public class ShiftExpression implements GroovyElementTypes {

  private static TokenSet RANGES = TokenSet.create(
      mRANGE_EXCLUSIVE,
      mRANGE_INCLUSIVE
  );

  public static GroovyElementType parse(PsiBuilder builder) {

    PsiBuilder.Marker marker = builder.mark();
    GroovyElementType result = AdditiveExpression.parse(builder);

    if (!result.equals(WRONGWAY)) {
      if (ParserUtils.getToken(builder, RANGES) ||
          getCompositeSign(builder)) {
        ParserUtils.getToken(builder, mNLS);
        result = AdditiveExpression.parse(builder);
        if (result.equals(WRONGWAY)) {
          builder.error(GroovyBundle.message("expression.expected"));
        }
        PsiBuilder.Marker newMarker = marker.precede();
        marker.done(SHIFT_EXPRESSION);
        result = SHIFT_EXPRESSION;
        if (RANGES.contains(builder.getTokenType())) {
          subParse(builder, newMarker);
        } else {
          newMarker.drop();
        }
      } else {
        marker.drop();
      }
    } else {
      marker.drop();
    }
    return result;
  }

  /**
   * For composite shift operators like >>>
   *
   * @param builder
   * @return
   */
  private static boolean getCompositeSign(PsiBuilder builder) {
    if (ParserUtils.lookAhead(builder, mGT, mGT, mGT)) {
      PsiBuilder.Marker marker = builder.mark();
      for (int i = 0; i < 3; i++) {
        builder.advanceLexer();
      }
      marker.done(COMPOSITE_SHIFT_SIGN);
      return true;
    } else if (ParserUtils.lookAhead(builder, mLT, mLT) ||
        ParserUtils.lookAhead(builder, mGT, mGT)) {
      PsiBuilder.Marker marker = builder.mark();
      for (int i = 0; i < 2; i++) {
        builder.advanceLexer();
      }
      marker.done(COMPOSITE_SHIFT_SIGN);
      return true;
    } else {
      return false;
    }
  }

  private static GroovyElementType subParse(PsiBuilder builder, PsiBuilder.Marker marker) {
    ParserUtils.getToken(builder, RANGES);
    ParserUtils.getToken(builder, mNLS);
    GroovyElementType result = AdditiveExpression.parse(builder);
    if (result.equals(WRONGWAY)) {
      builder.error(GroovyBundle.message("expression.expected"));
    }
    PsiBuilder.Marker newMarker = marker.precede();
    marker.done(SHIFT_EXPRESSION);
    if (RANGES.contains(builder.getTokenType())) {
      subParse(builder, newMarker);
    } else {
      newMarker.drop();
    }
    return SHIFT_EXPRESSION;
  }

}