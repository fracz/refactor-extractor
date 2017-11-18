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
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import org.jetbrains.annotations.NotNull;

public class ReturnThisInspection extends ExpressionInspection {

  public String getID() {
    return "ReturnOfThis";
  }

  public String getGroupDisplayName() {
    return GroupNames.STYLE_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new ReturnThisVisitor();
  }

  private static class ReturnThisVisitor extends BaseInspectionVisitor {

    public void visitThisExpression(@NotNull PsiThisExpression thisValue) {
      super.visitThisExpression(thisValue);
      if (thisValue.getQualifier() != null) {
        return;
      }
      PsiElement parent = thisValue.getParent();
      while (parent != null &&
             (parent instanceof PsiParenthesizedExpression ||
              parent instanceof PsiConditionalExpression ||
              parent instanceof PsiTypeCastExpression)) {
        parent = parent.getParent();
      }
      if (parent == null || !(parent instanceof PsiReturnStatement)) {
        return;
      }
      registerError(thisValue);
    }
  }
}