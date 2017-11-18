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
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.ClassUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ThreadStartInConstructionInspection extends ExpressionInspection {

    public String getID() {
        return "CallToThreadStartDuringObjectConstruction";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "thread.start.in.construction.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "thread.start.in.construction.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ThreadStartInConstructionVisitor();
    }

    private static class ThreadStartInConstructionVisitor
            extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            if (method.isConstructor()) {
                method.accept(new ThreadStartVisitor());
            }
        }

        public void visitClassInitializer(
                @NotNull PsiClassInitializer initializer) {
            if (!initializer.hasModifierProperty(PsiModifier.STATIC)) {
                initializer.accept(new ThreadStartVisitor());
            }
        }

        private class ThreadStartVisitor extends PsiRecursiveElementVisitor {

            public void visitClass(PsiClass aClass) {
                // Do not recurse into.
            }

            public void visitMethodCallExpression(
                    @NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                final PsiReferenceExpression methodExpression =
                        expression.getMethodExpression();
                @NonNls final String methodName =
                        methodExpression.getReferenceName();
                if (!"start".equals(methodName)) {
                    return;
                }
                final PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }
                final PsiParameterList paramList = method.getParameterList();
                final PsiParameter[] parameters = paramList.getParameters();
                if (parameters.length != 0) {
                    return;
                }
                final PsiClass methodClass = method.getContainingClass();
                if (methodClass == null ||
                        !ClassUtils.isSubclass(methodClass,
                                "java.lang.Thread")) {
                    return;
                }
                final PsiClass containingClass =
                        PsiTreeUtil.getParentOfType(expression, PsiClass.class);
                if (containingClass == null ||
                        containingClass.hasModifierProperty(
                                PsiModifier.FINAL)) {
                    return;
                }
                registerMethodCallError(expression);
            }
        }
    }
}