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
public class UnaryExpression implements GroovyElementTypes
{

  private static TokenSet PREFIXES = TokenSet.create(
          mPLUS,
          mMINUS,
          mINC,
          mDEC,
          mBNOT,
          mLNOT
  );

  public static GroovyElementType parse(PsiBuilder builder)
  {

    PsiBuilder.Marker marker = builder.mark();
    if (ParserUtils.getToken(builder, PREFIXES))
    {
      ParserUtils.getToken(builder, mNLS);
      parse(builder);
      marker.done(UNARY_EXPRESSION);
      return UNARY_EXPRESSION;
    }
    else
    {
      marker.drop();
      GroovyElementType result = UnaryExpressionNotPlusMinus.parse(builder);
      if (result.equals(WRONGWAY))
      {
        builder.error(GroovyBundle.message("expression.expected"));
      }
      return result;
    }
  }

}