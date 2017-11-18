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
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.modifiers.Modifiers;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.ExpressionStatement;
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

      if (WRONGWAY.equals(TypeSpec.parse(builder, true))) { //if type wasn't recognized trying poarse VaribleDeclaration
        checkMarker.rollbackTo();

        GroovyElementType varDecl = VariableDefinitions.parse(builder);
        if (IDENTIFIER.equals(varDecl)) varDecl = VARIABLE_DEFINITION;

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
        if (IDENTIFIER.equals(varDeclarationTop)) varDeclarationTop = VARIABLE_DEFINITION;

        if (WRONGWAY.equals(varDeclarationTop)) {
          checkMarker.rollbackTo();

          GroovyElementType varDecl = VariableDefinitions.parse(builder);
          if (IDENTIFIER.equals(varDeclarationTop)) varDecl = VARIABLE_DEFINITION;

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

      //if definition starts with lower case letter than it can be just call expression

      if (!builder.eof()
          && !TokenSets.BUILT_IN_TYPE.contains(builder.getTokenType())
          && Character.isLowerCase(builder.getTokenText().charAt(0))) {
        GroovyElementType exprType = ExpressionStatement.parse(builder);
        if (EXPRESSION_STATEMENT.equals(exprType)) {
          declmMarker.drop();
          return exprType;
        } else {
          declmMarker.drop();
          return WRONGWAY;
        }
      }

//      PsiBuilder.Marker methCallMarker = builder.mark();
      //type specification starts with upper case letter
      if (WRONGWAY.equals(TypeSpec.parse(builder, true))) {
        builder.error(GroovyBundle.message("type.specification.expected"));
//        methCallMarker.drop();
        declmMarker.rollbackTo();
        return WRONGWAY;
      }

      PsiBuilder.Marker varDefMarker = builder.mark();
      GroovyElementType varDef = VariableDefinitions.parse(builder);
      varDefMarker.rollbackTo();


      PsiBuilder.Marker exprStmtMarker = builder.mark();
      GroovyElementType exprType = ExpressionStatement.parse(builder); //todo: check it
      exprStmtMarker.rollbackTo();

      if (IDENTIFIER.equals(varDef) && REFERENCE_EXPRESSION.equals(exprType)) {
        VariableDefinitions.parse(builder);
        declmMarker.done(VARIABLE_DEFINITION);
        return VARIABLE_DEFINITION;
      }

      //handle "A a = "
      if (IDENTIFIER.equals(varDef) && ASSIGNMENT_EXPRESSION.equals(exprType)) {
        VariableDefinitions.parse(builder);
        declmMarker.done(VARIABLE_DEFINITION);
        return VARIABLE_DEFINITION;
      }

      if (!IDENTIFIER.equals(varDef) && !WRONGWAY.equals(varDef)) {
        varDef = VariableDefinitions.parse(builder);
        declmMarker.done(varDef);
        return varDef;
      }

//      if (!IDENTIFIER.equals(exprType) && !WRONGWAY.equals(exprType)) {
//        ExpressionStatement.parse(builder);
//        declmMarker.drop();
//        return REFERENCE_EXPRESSION;
//      }

      return WRONGWAY;
    }

//      if (VARIABLE_DEFINITION_OR_METHOD_CALL.equals(varDef)) {
//        methCallMarker.rollbackTo();
//
//        GroovyElementType exprType = ExpressionStatement.parse(builder);
//        if (!WRONGWAY.equals(exprType) && !IDENTIFIER.equals(exprType)) {
//          declmMarker.done(EXPRESSION_STATEMENT);
//          return EXPRESSION_STATEMENT;
//        }
//
//        declmMarker.done(VARIABLE_DEFINITION_OR_METHOD_CALL);
//        return VARIABLE_DEFINITION_OR_METHOD_CALL;
//
//      } else {
//        methCallMarker.drop();
//
//        if (WRONGWAY.equals(varDef)) {
//          builder.error(GroovyBundle.message("variable.definitions.expected"));
//          declmMarker.rollbackTo();
//          return WRONGWAY;
//        }
//
//        declmMarker.done(varDef);
//        return varDef;
//      }
//    }
  }

  private static GroovyElementType wrapVariableIfNeeds(PsiBuilder builder) {
    PsiBuilder.Marker identMarker = builder.mark();
    GroovyElementType varDecl = VariableDefinitions.parse(builder);

    if (IDENTIFIER.equals(varDecl)) {
      varDecl = VARIABLE_DEFINITION;
      identMarker.done(VARIABLE);
    } else {
      identMarker.drop();
    }
    return varDecl;
  }
}