/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.threading;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

public class SleepWhileHoldingLockInspection extends ExpressionInspection {

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "sleep.while.holding.lock.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new SleepWhileHoldingLockVisitor();
    }

    private static class SleepWhileHoldingLockVisitor
            extends BaseInspectionVisitor {

        public void visitMethodCallExpression(
                @NotNull PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiReferenceExpression methodExpression =
                    expression.getMethodExpression();
            @NonNls final String methodName = methodExpression.getReferenceName();
            if (!"sleep".equals(methodName)) {
                return;
            }
            final PsiMethod containingMethod =
                    PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
            boolean isSynced = false;
            if (containingMethod != null && containingMethod
                    .hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
                isSynced = true;
            }
            final PsiSynchronizedStatement containingSyncStatement =
                    PsiTreeUtil.getParentOfType(expression,
                            PsiSynchronizedStatement.class);
            if (containingSyncStatement != null) {
                isSynced = true;
            }
            if (!isSynced) {
                return;
            }
            final PsiMethod method = expression.resolveMethod();
            if (method == null) {
                return;
            }
            final PsiClass methodClass = method.getContainingClass();
            if (methodClass == null ||
                    !ClassUtils.isSubclass(methodClass, "java.lang.Thread")) {
                return;
            }
            registerMethodCallError(expression);
        }
    }
}