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
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.blocks.OpenOrClosableBlock;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.arguments.ArgumentList;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.primary.PrimaryExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.primary.RegexConstructorExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.primary.StringConstructorExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;
import org.jetbrains.plugins.groovy.lang.parser.parsing.types.TypeArguments;

/**
 * @author Ilya.Sergey
 */
public class PathExpression implements GroovyElementTypes {

  public static GroovyElementType parse(PsiBuilder builder) {

    PsiBuilder.Marker marker = builder.mark();
    GroovyElementType result = PrimaryExpression.parse(builder);
    if (!WRONGWAY.equals(result)) {
      if (isPathElementStart(builder)) {
        PsiBuilder.Marker newMarker = marker.precede();
        marker.drop();
        pathElementParse(builder, newMarker);
        return PATH_EXPRESSION;
      } else {
        marker.drop();
      }
    } else {
      marker.drop();
    }
    return result;
  }

  /**
   * Any path element parsing
   *
   * @param builder
   * @param marker
   * @return
   */
  private static GroovyElementType pathElementParse(PsiBuilder builder,
                                                    PsiBuilder.Marker marker) {
    TokenSet DOTS = TokenSet.create(
        mSPREAD_DOT,
        mOPTIONAL_DOT,
        mMEMBER_POINTER,
        mDOT
    );
    GroovyElementType res;

    // Property reference
    if (DOTS.contains(builder.getTokenType()) ||
        ParserUtils.lookAhead(builder, mNLS, mDOT)) {
      if (ParserUtils.lookAhead(builder, mNLS, mDOT)) {
        ParserUtils.getToken(builder, mNLS);
      }
      ParserUtils.getToken(builder, DOTS);
      ParserUtils.getToken(builder, mNLS);

      TypeArguments.parse(builder);

      res = namePartParse(builder);
      if (!res.equals(WRONGWAY)) {
        // If method call or appended block
        if (mLPAREN.equals(builder.getTokenType()) ||
            mLCURLY.equals(builder.getTokenType())) {
          pathElementParse(builder, marker);
        } else {
          PsiBuilder.Marker newMarker = marker.precede();
          marker.done(PATH_PROPERTY_REFERENCE);
          pathElementParse(builder, newMarker);
        }
      } else {
        builder.error(GroovyBundle.message("path.selector.expected"));
        marker.drop();
      }
    } else if (mLPAREN.equals(builder.getTokenType())) {
      methodCallArgsParse(builder);
      if (mLCURLY.equals(builder.getTokenType())) {
        pathElementParse(builder, marker);
      } else {
        PsiBuilder.Marker newMarker = marker.precede();
        marker.done(PATH_METHOD_CALL);
        pathElementParse(builder, newMarker);
      }
    } else if (mLCURLY.equals(builder.getTokenType())) {
      appendedBlockParse(builder);
      PsiBuilder.Marker newMarker = marker.precede();
      marker.done(PATH_METHOD_CALL);
      pathElementParse(builder, newMarker);
    } else if (mLBRACK.equals(builder.getTokenType()) &&
        !ParserUtils.lookAhead(builder, mLBRACK, mCOLON) &&
        !ParserUtils.lookAhead(builder, mLBRACK, mNLS, mCOLON)) {
      indexPropertyArgsParse(builder);
      PsiBuilder.Marker newMarker = marker.precede();
      marker.done(PATH_INDEX_PROPERTY);
      pathElementParse(builder, newMarker);
    } else {
      marker.drop();
    }
    return PATH_EXPRESSION;
  }

  /**
   * Property selector parsing
   *
   * @param builder
   * @return
   */
  private static GroovyElementType namePartParse(PsiBuilder builder) {
    ParserUtils.getToken(builder, mAT);
    if (mIDENT.equals(builder.getTokenType())) {
      ParserUtils.eatElement(builder, PATH_PROPERTY);
      return PATH_PROPERTY;
    }
    if (mSTRING_LITERAL.equals(builder.getTokenType()) ||
        mGSTRING_LITERAL.equals(builder.getTokenType()) ||
        mREGEX_LITERAL.equals(builder.getTokenType())) {
      ParserUtils.eatElement(builder, PATH_PROPERTY);
      return PATH_PROPERTY;
    }
    if (mGSTRING_SINGLE_BEGIN.equals(builder.getTokenType())) {
      StringConstructorExpression.parse(builder);
      return PATH_PROPERTY;
    }
    if (mREGEX_BEGIN.equals(builder.getTokenType())) {
      RegexConstructorExpression.parse(builder);
      return PATH_PROPERTY;
    }
    if (mLCURLY.equals(builder.getTokenType())) {
      OpenOrClosableBlock.parseOpenBlock(builder);
      return PATH_PROPERTY;
    }
    if (mLPAREN.equals(builder.getTokenType())) {
      PrimaryExpression.parenthesizedExprParse(builder);
      return PATH_PROPERTY;
    }
    if (TokenSets.KEYWORD_PROPERTY_NAMES.contains(builder.getTokenType())) {
      ParserUtils.eatElement(builder, PATH_PROPERTY);
      return PATH_PROPERTY;
    }
    return WRONGWAY;
  }

  /**
   * Method call parsing
   *
   * @param builder
   * @return
   */
  private static GroovyElementType indexPropertyArgsParse(PsiBuilder builder) {
    if (ParserUtils.getToken(builder, mLBRACK, GroovyBundle.message("lbrack.expected"))) {
      ParserUtils.getToken(builder, mNLS);
      if (ParserUtils.getToken(builder, mRBRACK)) {
        return PATH_INDEX_PROPERTY;
      }
      ArgumentList.parse(builder, mRBRACK);
      ParserUtils.getToken(builder, mNLS);
      ParserUtils.getToken(builder, mRBRACK, GroovyBundle.message("rbrack.expected"));
    }
    return PATH_INDEX_PROPERTY;
  }

  /**
   * Parses method arguments
   *
   * @param builder
   * @return
   */
  private static GroovyElementType methodCallArgsParse(PsiBuilder builder) {
    if (ParserUtils.getToken(builder, mLPAREN, GroovyBundle.message("lparen.expected"))) {
      ParserUtils.getToken(builder, mNLS);
      if (ParserUtils.getToken(builder, mRPAREN)) {
        return PATH_METHOD_CALL;
      }
      ArgumentList.parse(builder, mRPAREN);
      ParserUtils.getToken(builder, mNLS);
      ParserUtils.getToken(builder, mRPAREN, GroovyBundle.message("rparen.expected"));
    }
    return PATH_METHOD_CALL;
  }

  /**
   * Appended closure argument parsing
   *
   * @param builder
   * @return
   */
  private static GroovyElementType appendedBlockParse(PsiBuilder builder) {
    return OpenOrClosableBlock.parseClosableBlock(builder);
  }


  /**
   * Checks for path element start
   *
   * @param builder
   * @return
   */
  private static boolean isPathElementStart(PsiBuilder builder) {
    return (PATH_ELEMENT_START.contains(builder.getTokenType()) ||
        ParserUtils.lookAhead(builder, mNLS, mDOT) ||
        ParserUtils.lookAhead(builder, mNLS, mLCURLY));
  }

  /**
   * FIRST(1) of PathElement
   */
  private static TokenSet PATH_ELEMENT_START = TokenSet.create(
      mSPREAD_DOT,
      mOPTIONAL_DOT,
      mMEMBER_POINTER,
      mLBRACK,
      mLPAREN,
      mLCURLY,
      mDOT
  );


}