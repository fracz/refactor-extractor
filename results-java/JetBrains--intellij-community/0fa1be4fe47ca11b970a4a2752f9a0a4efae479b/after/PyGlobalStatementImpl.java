package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.toolbox.ArrayIterable;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
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
    final PyElementGenerator pyElementGenerator = PyElementGenerator.getInstance(getProject());
    add(pyElementGenerator.createComma().getPsi());
    add(pyElementGenerator.createWhiteSpace(1));
    add(pyElementGenerator.createFromText(PyGlobalStatement.class, "global " + name).getGlobals()[0]);
  }
}