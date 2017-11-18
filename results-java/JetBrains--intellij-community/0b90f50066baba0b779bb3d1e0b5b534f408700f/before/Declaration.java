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
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyElementType;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.modifiers.Modifiers;
import org.jetbrains.plugins.groovy.lang.parser.parsing.types.TypeSpec;

/**
 * @autor: Dmitry.Krasilschikov
 * @date: 14.03.2007
 */

/*
 * Declaration ::= modifiers [TypeSpec] VariableDefinitions
 *                  | TypeSpec VariableDefinitions
 */

public class Declaration implements GroovyElementTypes {
  public static GroovyElementType parse(PsiBuilder builder) {
    PsiBuilder.Marker declmMarker = builder.mark();
    //allows error messages
    IElementType modifiers = Modifiers.parse(builder);

    if (!WRONGWAY.equals(modifiers)) {

      PsiBuilder.Marker checkMarker = builder.mark(); //point to begin of type or variable

      if (WRONGWAY.equals(TypeSpec.parse(builder))) { //if type wasn't recognized trying poarse VaribleDeclaration
        checkMarker.rollbackTo();

        GroovyElementType varDecl = VariableDefinitions.parse(builder);
        if (WRONGWAY.equals(varDecl)) {
          builder.error(GroovyBundle.message("variable.definitions.expected"));
          declmMarker.rollbackTo();
          return WRONGWAY;
        } else {
          declmMarker.done(varDecl);
          return varDecl;
        }

      } else {  //type was recognezed
        GroovyElementType varDeclarationTop = VariableDefinitions.parse(builder);
        if (WRONGWAY.equals(varDeclarationTop)) {
          checkMarker.rollbackTo();

          GroovyElementType varDecl = VariableDefinitions.parse(builder);
          if (WRONGWAY.equals(varDecl)) {
            builder.error(GroovyBundle.message("variable.definitions.expected"));
            declmMarker.rollbackTo();
            return WRONGWAY;
          } else {
            declmMarker.done(varDecl);
            return varDecl;
          }
        } else {
          checkMarker.drop();
          declmMarker.done(varDeclarationTop);
          return varDeclarationTop;
        }
      }
    } else {
      if (WRONGWAY.equals(TypeSpec.parse(builder))) {
        builder.error(GroovyBundle.message("type.specification.expected"));
        declmMarker.rollbackTo();
        return WRONGWAY;
      }

      GroovyElementType varDef = VariableDefinitions.parse(builder);
      if (WRONGWAY.equals(varDef)) {
        builder.error(GroovyBundle.message("variable.definitions.expected"));
        declmMarker.rollbackTo();
        return WRONGWAY;
      }

      declmMarker.done(varDef);
      return varDef;
    }
  }
}