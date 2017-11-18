/*
 *  Copyright 2000-2007 JetBrains s.r.o.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.jetbrains.plugins.groovy.lang.parser.parsing.types;

import com.intellij.lang.PsiBuilder;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.ReferenceElement;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @autor: Dmitry.Krasilschikov
 * @date: 16.03.2007
 */
public class TypeSpec implements GroovyElementTypes {
  public static GroovyElementType parse(PsiBuilder builder) {
    return parse(builder, false); //allow lower and upper case first letter
  }

  public static GroovyElementType parse(PsiBuilder builder, boolean isUpper) {
    if (TokenSets.BUILT_IN_TYPE.contains(builder.getTokenType())) {
      return parseBuiltInType(builder);
    } else if (ParserUtils.lookAhead(builder, mIDENT)) {
      return parseClassOrInterfaceType(builder, isUpper);
    }
    return WRONGWAY;
  }

  /**
   * For built-in types
   *
   * @param builder
   * @return
   */
  public static GroovyElementType parseBuiltInType(PsiBuilder builder) {
    PsiBuilder.Marker arrMarker = builder.mark();
    ParserUtils.eatElement(builder, BUILT_IN_TYPE);
    if (mLBRACK.equals(builder.getTokenType())) {
      declarationBracketsParse(builder, arrMarker);
    } else {
      arrMarker.drop();
    }
    return TYPE_SPECIFICATION;
  }


  /**
   * For array definitions
   *
   * @param builder
   * @param marker
   */
  private static void declarationBracketsParse(PsiBuilder builder, PsiBuilder.Marker marker) {
    ParserUtils.getToken(builder, mLBRACK);
    ParserUtils.getToken(builder, mRBRACK, GroovyBundle.message("rbrack.expected"));
    PsiBuilder.Marker newMarker = marker.precede();
    marker.done(ARRAY_TYPE);
    if (mLBRACK.equals(builder.getTokenType())) {
      declarationBracketsParse(builder, newMarker);
    } else {
      newMarker.drop();
    }
  }

  /*
   * Class or interface type
   * @param builder
   */

  private static GroovyElementType parseClassOrInterfaceType(PsiBuilder builder, boolean isUpper) {
    PsiBuilder.Marker arrMarker = builder.mark();

    if (WRONGWAY.equals(ReferenceElement.parseReferenceElement(builder, isUpper))) {
      arrMarker.rollbackTo();
      return WRONGWAY;
    }

    if (mLBRACK.equals(builder.getTokenType())) {
      declarationBracketsParse(builder, arrMarker);
    } else {
      arrMarker.done(TYPE_SPECIFICATION);
//      arrMarker.drop();
    }
    return TYPE_SPECIFICATION;
  }

  /**
   * ********************************************************************************************************
   * ****************  Strict type specification parsing
   * ********************************************************************************************************
   */

  /**
   * Strict parsing
   *
   * @param builder
   * @return
   */
  public static GroovyElementType parseStrict(PsiBuilder builder) {
    if (TokenSets.BUILT_IN_TYPE.contains(builder.getTokenType())) {
      return parseBuiltInTypeStrict(builder);
    } else if (ParserUtils.lookAhead(builder, mIDENT)) {
      return parseClassOrInterfaceTypeStrict(builder);
    }
    return WRONGWAY;
  }


  /**
   * For built-in types
   *
   * @param builder
   * @return
   */
  private static GroovyElementType parseBuiltInTypeStrict(PsiBuilder builder) {
    PsiBuilder.Marker arrMarker = builder.mark();
    ParserUtils.eatElement(builder, BUILT_IN_TYPE);
    if (mLBRACK.equals(builder.getTokenType())) {
      return declarationBracketsParseStrict(builder, arrMarker);
    } else {
      arrMarker.drop();
      return TYPE_SPECIFICATION;
    }
  }


  /**
   * Strict parsing of array definitions
   *
   * @param builder
   * @param marker
   */
  private static GroovyElementType declarationBracketsParseStrict(PsiBuilder builder, PsiBuilder.Marker marker) {
    ParserUtils.getToken(builder, mLBRACK);
    if (!ParserUtils.getToken(builder, mRBRACK, GroovyBundle.message("rbrack.expected"))) {
      marker.rollbackTo();
      return WRONGWAY;
    }
    PsiBuilder.Marker newMarker = marker.precede();
    marker.done(ARRAY_TYPE);
    if (mLBRACK.equals(builder.getTokenType())) {
      return declarationBracketsParseStrict(builder, newMarker);
    } else {
      newMarker.drop();
      return ARRAY_TYPE;
    }
  }


  /**
   * Strict class type parsing
   *
   * @param builder
   * @return
   */
  private static GroovyElementType parseClassOrInterfaceTypeStrict(PsiBuilder builder) {
    PsiBuilder.Marker arrMarker = builder.mark();
    if (WRONGWAY.equals(ReferenceElement.parseReferenceElement(builder))) {
      arrMarker.rollbackTo();
      return WRONGWAY;
    }
    if (mLBRACK.equals(builder.getTokenType())) {
      return declarationBracketsParseStrict(builder, arrMarker);
    } else {
      arrMarker.done(TYPE_SPECIFICATION);
//      arrMarker.drop();
      return TYPE_SPECIFICATION;
    }
  }
}