package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PyListLiteralExpressionImpl extends PyElementImpl implements PyListLiteralExpression {
  public PyListLiteralExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyListLiteralExpression(this);
  }

  @PsiCached
  @NotNull
  public PyExpression[] getElements() {
    return childrenToPsi(PyElementTypes.EXPRESSIONS, PyExpression.EMPTY_ARRAY);
  }

  public PsiElement add(@NotNull PsiElement psiElement) throws IncorrectOperationException {
    PyUtil.ensureWritable(this);
    checkPyExpression(psiElement);
    PyExpression element = (PyExpression)psiElement;
    PyExpression[] els = getElements();
    PyExpression lastArg = els.length == 0 ? null : els[els.length - 1];
    return PyElementGenerator.getInstance(getProject()).insertItemIntoList(this, lastArg, element);
  }

  private static void checkPyExpression(PsiElement psiElement) throws IncorrectOperationException {
    if (!(psiElement instanceof PyExpression)) {
      throw new IncorrectOperationException("Element must be PyExpression: " + psiElement);
    }
  }

  public PsiElement addAfter(@NotNull PsiElement psiElement, PsiElement afterThis) throws IncorrectOperationException {
    PyUtil.ensureWritable(this);
    checkPyExpression(psiElement);
    checkPyExpression(afterThis);
    return PyElementGenerator.getInstance(getProject()).insertItemIntoList(this, (PyExpression)afterThis, (PyExpression)psiElement);
  }

  public PsiElement addBefore(@NotNull PsiElement psiElement, PsiElement beforeThis) throws IncorrectOperationException {
    PyUtil.ensureWritable(this);
    checkPyExpression(psiElement);
    return PyElementGenerator.getInstance(getProject()).insertItemIntoList(this, null, (PyExpression)psiElement);
  }

  @Override
  public void deleteChildInternal(@NotNull ASTNode child) {
    if (child.getPsi() instanceof PyExpression) {
      PyExpression expression = (PyExpression)child.getPsi();
      ASTNode node = getNode();
      ASTNode exprNode = expression.getNode();
      ASTNode next = PyPsiUtils.getNextComma(exprNode);
      ASTNode prev = PyPsiUtils.getPrevComma(exprNode);
      super.deleteChildInternal(child);
      if (next != null) {
        deleteChildInternal(next);
      }
      else if (prev != null) {
        deleteChildInternal(prev);
      }
    }
    else {
      super.deleteChildInternal(child);
    }
  }


  @Nullable
  public PyType getType() {
    return PyBuiltinCache.getInstance(this).getListType();
  }
}