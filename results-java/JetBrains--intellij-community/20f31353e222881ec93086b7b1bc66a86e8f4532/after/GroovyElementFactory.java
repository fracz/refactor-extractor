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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;

/**
 * @author ven
 */
public abstract class GroovyElementFactory {

  private static String DUMMY = "dummy.";

  public static GroovyElementFactory getInstance(Project project) {
    return project.getComponent(GroovyElementFactory.class);
  }

  /**
   *
   * @param qName
   * @return import statement for given class
   */
  public abstract GrImportStatement createImportStatementFromText(String qName);

  /**
   * @return new line psi element
   */
  public abstract PsiElement createWhiteSpace();

  public abstract PsiElement createNewLine();

  public abstract GrReferenceExpression createReferenceExpressionFromText(String idText);

  public abstract GrVariableDeclaration createVariableDeclaration(String identifier, GrExpression initializer, PsiType type, boolean isFinal);

  public abstract PsiElement createIdentifierFromText(String idText);

  public abstract GroovyPsiElement createTopElementFromText(String text);

  public abstract GroovyPsiElement createClosureFromText(String s) throws IncorrectOperationException;

  public abstract GrParameter createParameter(String name, @Nullable String typeText) throws IncorrectOperationException;
}