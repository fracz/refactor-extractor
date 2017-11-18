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

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.typeDef;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.InterfaceExtends;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.blocks.InterfaceBlock;
import org.jetbrains.plugins.groovy.lang.parser.parsing.types.TypeParameters;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @autor: Dmitry.Krasilschikov
 * @date: 16.03.2007
 */
public class InterfaceDefinition implements GroovyElementTypes
{
  public static IElementType parse(PsiBuilder builder)
  {
    if (!ParserUtils.getToken(builder, kINTERFACE))
    {
      return WRONGWAY;
    }

    if (!ParserUtils.getToken(builder, mIDENT))
    {
      return WRONGWAY;
    }

    ParserUtils.getToken(builder, mNLS);

    TypeParameters.parse(builder);

    if (WRONGWAY.equals(InterfaceExtends.parse(builder)))
    {
      return WRONGWAY;
    }

    if (WRONGWAY.equals(InterfaceBlock.parse(builder)))
    {
      return WRONGWAY;
    }

    return INTERFACE_DEFINITION;
  }
}