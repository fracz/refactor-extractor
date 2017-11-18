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

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.declaration;

import com.intellij.lang.PsiBuilder;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.NlsWarn;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.ThrowClause;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.annotations.Annotation;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.parameters.ParameterDeclarationList;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.blocks.OpenOrClosableBlock;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.AssignmentExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.ConditionalExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @autor: Dmitry.Krasilschikov
 * @date: 16.03.2007
 */
public class VariableDefinitions implements GroovyElementTypes {
  public static GroovyElementType parse(PsiBuilder builder) {
    if (!(ParserUtils.lookAhead(builder, mIDENT) || ParserUtils.lookAhead(builder, mSTRING_LITERAL))) {
      builder.error(GroovyBundle.message("indentifier.or.string.literal.expected"));
      return WRONGWAY;
    }

    PsiBuilder.Marker varMarker = builder.mark();
    boolean isStringName = ParserUtils.lookAhead(builder, mSTRING_LITERAL);

    if ((ParserUtils.getToken(builder, mIDENT) || ParserUtils.getToken(builder, mSTRING_LITERAL)) && ParserUtils.getToken(builder, mLPAREN)) {

      GroovyElementType paramDeclList = ParameterDeclarationList.parse(builder, mRPAREN);

      //todo: define whether param list is empty or not
      boolean isEmptyParamDeclList = NONE.equals(paramDeclList);

      if (!ParserUtils.getToken(builder, mRPAREN)) {
        ParserUtils.waitNextRCurly(builder);

        builder.error(GroovyBundle.message("rparen.expected"));
      }

      if (!isStringName && isEmptyParamDeclList && ParserUtils.getToken(builder, kDEFAULT)) {
        ParserUtils.getToken(builder, mNLS);

        if (parseAnnotationMemberValueInitializer(builder)) {
          varMarker.done(DEFAULT_ANNOTATION_MEMBER);
          return DEFAULT_ANNOTATION_MEMBER;
        }
      }

      ThrowClause.parse(builder);

      NlsWarn.parse(builder);

      OpenOrClosableBlock.parseOpenBlock(builder);

      varMarker.drop();
      return METHOD_DEFINITION;
    } else {
      varMarker.rollbackTo();
    }

    if (parseVariableDeclarator(builder)) {
      while (ParserUtils.getToken(builder, mCOMMA)) {
        ParserUtils.getToken(builder, mNLS);

        parseVariableDeclarator(builder);
      }

      return VARIABLE_DEFINITION;
    }

    builder.error(GroovyBundle.message("indentifier.or.string.literal.expected"));
    return WRONGWAY;

  }

  private static boolean parseVariableDeclarator(PsiBuilder builder) {
    if (!(ParserUtils.getToken(builder, mIDENT))) {
      return false;
    }

    if (ParserUtils.getToken(builder, mASSIGN)) {
      ParserUtils.getToken(builder, mNLS);
      if (WRONGWAY.equals(AssignmentExpression.parse(builder))) {
        return false;
      }
    }

    return true;
  }

  private static boolean parseAnnotationMemberValueInitializer(PsiBuilder builder) {
    return !WRONGWAY.equals(Annotation.parse(builder)) || !WRONGWAY.equals(ConditionalExpression.parse(builder));
  }
}