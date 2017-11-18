/*
 *  Copyright 2005 Pythonid Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS"; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.toolbox.ArrayIterable;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyGlobalStatement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.PyReferenceExpression;

/**
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 05.06.2005
 * Time: 10:29:42
 * To change this template use File | Settings | File Templates.
 */
public class PyGlobalStatementImpl extends PyElementImpl implements PyGlobalStatement {
  public PyGlobalStatementImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyGlobalStatement(this);
  }

  @NotNull
  public PyReferenceExpression[] getGlobals() {
    return childrenToPsi(PyElementTypes.REFERENCE_EXPRESSION_SET, PyReferenceExpression.EMPTY_ARRAY);
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState substitutor,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    for (PyExpression expression : getGlobals()) {
      if (expression == lastParent) continue;
      if (!expression.processDeclarations(processor, substitutor, lastParent, place)) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  public Iterable<PyElement> iterateNames() {
    return new ArrayIterable<PyElement>(getGlobals());
  }

  public PyElement getElementNamed(final String the_name) {
    return IterHelper.findName(iterateNames(), the_name);
  }

  public boolean mustResolveOutside() {
    return true;
  }

  public void addGlobal(final String name) {
    final PyElementGenerator pyElementGenerator = PythonLanguage.getInstance().getElementGenerator();
    add(pyElementGenerator.createComma(getProject()).getPsi());
    add(pyElementGenerator.createWhiteSpace(getProject(), 1));
    add(pyElementGenerator.createFromText(getProject(), PyGlobalStatement.class, "global " + name).getGlobals()[0]);
  }
}