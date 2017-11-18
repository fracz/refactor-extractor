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
package com.siyeh.ig.bugs;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class CompareToUsesNonFinalVariableInspection
        extends ExpressionInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "non.final.field.compareto.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "non.final.field.compareto.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new CompareToUsesNonFinalVariableVisitor();
    }

    private static class CompareToUsesNonFinalVariableVisitor
            extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            final boolean isCompareTo = MethodUtils.isCompareTo(method);
            if (isCompareTo) {
                method.accept(new PsiRecursiveElementVisitor() {
                    public void visitClass(PsiClass aClass) {
                        // Do not recurse into.
                    }

                    public void visitReferenceExpression(
                            @NotNull PsiReferenceExpression expression) {
                        super.visitReferenceExpression(expression);
                        final PsiElement element = expression.resolve();
                        if (!(element instanceof PsiField)) {
                            return;
                        }
                        final PsiField field = (PsiField) element;
                        if (field.hasModifierProperty(PsiModifier.FINAL)) {
                            return;
                        }
                        registerError(expression);
                    }
                });
            }
        }
    }
}