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
package com.siyeh.ig.performance;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;

public class StringBufferMustHaveInitialCapacityInspection extends ExpressionInspection {

  public String getID() {
    return "StringBufferWithoutInitialCapacity";
  }

  public String getGroupDisplayName() {
    return GroupNames.PERFORMANCE_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new StringBufferInitialCapacityVisitor();
  }

  private static class StringBufferInitialCapacityVisitor extends BaseInspectionVisitor {


    public void visitNewExpression(@NotNull PsiNewExpression expression) {
      super.visitNewExpression(expression);
      final PsiType type = expression.getType();

      if (!TypeUtils.typeEquals("java.lang.StringBuffer", type) &&
          !TypeUtils.typeEquals("java.lang.StringBuilder", type)) {
        return;
      }
      final PsiExpressionList argumentList = expression.getArgumentList();
      if (argumentList == null) {
        return;
      }
      final PsiExpression[] args = argumentList.getExpressions();
      if (args == null) {
        return;
      }
      if (args.length != 0) {
        return;
      }
      registerError(expression);
    }
  }
}