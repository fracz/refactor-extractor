/*
 * Copyright 2003-2005 Dave Griffith
 *
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
package com.siyeh.ig.style;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import com.siyeh.InspectionGadgetsBundle;

public class UnnecessaryParenthesesInspection extends ExpressionInspection {

  private final UnnecessaryParenthesesFix fix = new UnnecessaryParenthesesFix();

  public String getGroupDisplayName() {
    return GroupNames.STYLE_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new UnnecessaryParenthesesVisitor();
  }

  public InspectionGadgetsFix buildFix(PsiElement location) {
    return fix;
  }

  private static class UnnecessaryParenthesesFix extends InspectionGadgetsFix {
    public String getName() {
      return InspectionGadgetsBundle.message("unnecessary.parentheses.remove.quickfix");
    }

    public void doFix(Project project, ProblemDescriptor descriptor)
      throws IncorrectOperationException {
      final PsiExpression exp = (PsiExpression)descriptor.getPsiElement();
      final String newExpression = ParenthesesUtils.removeParentheses(exp);
      replaceExpression(exp, newExpression);
    }

  }

  private static class UnnecessaryParenthesesVisitor extends BaseInspectionVisitor {

    public void visitParenthesizedExpression(PsiParenthesizedExpression expression) {
      final PsiElement parent = expression.getParent();
      final PsiExpression child = expression.getExpression();
      if (child == null) {
        return;
      }
      if (!(parent instanceof PsiExpression)) {
        registerError(expression);
        return;
      }
      final int parentPrecedence = ParenthesesUtils.getPrecendence((PsiExpression)parent);
      final int childPrecedence = ParenthesesUtils.getPrecendence(child);
      if (parentPrecedence > childPrecedence) {
        registerError(expression);
        return;
      }
      if (parentPrecedence == childPrecedence) {
        if (parent instanceof PsiBinaryExpression &&
            child instanceof PsiBinaryExpression) {
          final PsiJavaToken parentSign =
            ((PsiBinaryExpression)parent).getOperationSign();
          final IElementType parentOperator = parentSign.getTokenType();
          final PsiJavaToken childSign = ((PsiBinaryExpression)child).getOperationSign();
          final IElementType childOperator = childSign.getTokenType();

          final PsiBinaryExpression binaryExpression = (PsiBinaryExpression)parent;
          final PsiExpression lhs = binaryExpression.getLOperand();
          if (lhs.equals(expression) && parentOperator.equals(childOperator)) {
            registerError(expression);
            return;
          }
        }
        else {
          registerError(expression);
          return;
        }
      }
      super.visitParenthesizedExpression(expression);
    }
  }
}