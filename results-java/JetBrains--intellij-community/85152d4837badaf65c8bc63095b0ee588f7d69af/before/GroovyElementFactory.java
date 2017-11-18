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

package org.jetbrains.plugins.groovy.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrBlockStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrCodeBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrParenthesizedExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.util.MethodArgument;
import org.jetbrains.plugins.groovy.refactoring.extractMethod.ExtractMethodUtil;
import org.jetbrains.plugins.groovy.refactoring.extractMethod.ExtractMethodInfoHelper;
import org.jetbrains.plugins.groovy.refactoring.extractMethod.ExtractMethodUtil.*;

/**
 * @author dimaskin
 */
public abstract class GroovyElementFactory {

  public static GroovyElementFactory getInstance(Project project) {
    return project.getComponent(GroovyElementFactory.class);
  }

  /**
   * @param qName
   * @param isStatic
   * @param isOnDemand
   * @return import statement for given class
   */
  public abstract GrImportStatement createImportStatementFromText(String qName, boolean isStatic, boolean isOnDemand);

  public abstract PsiElement createWhiteSpace();

  @NotNull
  public abstract PsiElement createLineTerminator();

  public abstract GrArgumentList createExpressionArgumentList(GrExpression... expressions);

  public abstract GrBlockStatement createBlockStatement(GrStatement... statements);

  public abstract GrMethodCallExpression createMethodCallByAppCall(GrApplicationStatement callExpr);

  public abstract GrReferenceExpression createReferenceExpressionFromText(String exprText);

  public abstract GrExpression createExpressionFromText(String exprText);

  public abstract GrVariableDeclaration createFieldDeclaration(String[] modifiers, String identifier, GrExpression initializer, PsiType type);

  public abstract GrVariableDeclaration createVariableDeclaration(String[] modifiers, String identifier, GrExpression initializer, PsiType type);

  public abstract PsiElement createReferenceNameFromText(String idText);

  public abstract GrTopStatement createTopElementFromText(String text);

  public abstract GrClosableBlock createClosureFromText(String s) throws IncorrectOperationException;

  public abstract GrParameter createParameter(String name, @Nullable String typeText, GroovyPsiElement context) throws IncorrectOperationException;

  public abstract GrCodeReferenceElement createTypeOrPackageReference(String qName);

  public abstract GrTypeDefinition createTypeDefinition(String text) throws IncorrectOperationException;

  public abstract GrTypeElement createTypeElement(PsiType type);

  public abstract GrParenthesizedExpression createParenthesizedExpr(GrExpression newExpr);

  public abstract PsiElement createStringLiteral(String text);

  public abstract PsiElement createModifierFormText(String name);

  public abstract GrCodeBlock createMetodBodyFormText(String text);

  public abstract GrVariableDeclaration createSimpleVariableDeclaration(String name, String typeText);

  public abstract GrReferenceElement createPackegeReferenceElementFromText(String newPackageName);

  public abstract PsiElement createDotToken(String newDot);

  public abstract GrMethod createMethodFromText(String methodText);
}