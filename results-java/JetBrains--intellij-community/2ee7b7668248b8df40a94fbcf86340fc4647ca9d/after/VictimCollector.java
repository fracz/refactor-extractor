package com.intellij.refactoring.typeCook.deductive.util;

import com.intellij.refactoring.typeCook.Util;
import com.intellij.refactoring.typeCook.Settings;
import com.intellij.psi.*;
import com.intellij.psi.jsp.JspFile;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: Jul 5, 2004
 * Time: 6:45:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class VictimCollector extends Visitor {
  final HashSet<PsiElement> myVictims = new HashSet<PsiElement>();
  final PsiElement[] myElements;
  final Settings mySettings;

  public VictimCollector(final PsiElement[] elements, final Settings settings) {
    myElements = elements;
    mySettings = settings;
  }

  private void testNAdd(final PsiElement element, final PsiType t) {
    if (Util.isRaw(t, mySettings)) {
      if (element instanceof PsiNewExpression && t.getCanonicalText().equals("java.lang.Object")){
        return;
      }

      myVictims.add(element);
    }
  }

  public void visitLocalVariable(final PsiLocalVariable variable) {
    testNAdd(variable, variable.getType());

    super.visitLocalVariable(variable);
  }

  public void visitField(final PsiField field) {
    testNAdd(field, field.getType());

    super.visitField(field);
  }

  public void visitMethod(final PsiMethod method) {
    final PsiParameter[] parms = method.getParameterList().getParameters();

    for (PsiParameter parm : parms) {
      testNAdd(parm, parm.getType());
    }

    if (Util.isRaw(method.getReturnType(), mySettings)) {
      myVictims.add(method);
    }

    final PsiCodeBlock body = method.getBody();

    if (body != null) {
      body.accept(this);
    }
  }

  public void visitNewExpression(final PsiNewExpression expression) {
    if (expression.getClassReference() != null) {
      testNAdd(expression, expression.getType());
    }

    super.visitNewExpression(expression);
  }

  public void visitTypeCastExpression (final PsiTypeCastExpression cast){
    testNAdd(cast, cast.getCastType().getType());

    super.visitTypeCastExpression(cast);
  }

  public void visitReferenceExpression(final PsiReferenceExpression expression) {
  }

  public void visitFile(PsiFile file) {
    if (file instanceof PsiJavaFile || file instanceof JspFile) {
      super.visitFile(file);
    }
  }

  public HashSet<PsiElement> getVictims() {
    for (PsiElement element : myElements) {
      element.accept(this);
    }

    return myVictims;
  }
}