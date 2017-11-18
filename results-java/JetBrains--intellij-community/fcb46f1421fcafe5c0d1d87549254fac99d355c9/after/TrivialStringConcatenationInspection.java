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
package com.siyeh.ig.performance;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TrivialStringConcatenationInspection extends ExpressionInspection {

    public String getID() {
        return "ConcatenationWithEmptyString";
    }

    public String getGroupDisplayName() {
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final String replacementString =
                calculateReplacementExpression((PsiElement)infos[0]);
        return InspectionGadgetsBundle.message(
                "string.can.be.simplified.problem.descriptor",
                replacementString);
    }

    @NonNls static String calculateReplacementExpression(
            PsiElement location) {
        final PsiBinaryExpression expression = (PsiBinaryExpression) location;
        final PsiExpression lOperand = expression.getLOperand();
        final PsiExpression rOperand = expression.getROperand();
        final PsiExpression replacement;
        if(ExpressionUtils.isEmptyStringLiteral(lOperand)) {
            replacement = rOperand;
        } else {
            replacement = lOperand;
        }
        @NonNls final String replacementText;
        if (replacement == null) {
            replacementText = "";
        } else {
            if (ExpressionUtils.isNullLiteral(replacement)) {
                replacementText = "(Object)null";
            } else {
                replacementText = replacement.getText();
            }
            if (TypeUtils.expressionHasType("java.lang.String", replacement)) {
                return replacementText;
            }
        }
        return "String.valueOf(" + replacementText + ')';
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return new UnnecessaryTemporaryObjectFix((PsiBinaryExpression)location);
    }

    private static class UnnecessaryTemporaryObjectFix
            extends InspectionGadgetsFix {

        private final String m_name;

        private UnnecessaryTemporaryObjectFix(PsiBinaryExpression expression) {
            super();
            m_name = InspectionGadgetsBundle.message("string.replace.quickfix",
                            calculateReplacementExpression(expression));
        }

        public String getName() {
            return m_name;
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiBinaryExpression expression =
                    (PsiBinaryExpression)descriptor.getPsiElement();
            final String newExpression =
                    calculateReplacementExpression(expression);
            replaceExpression(expression, newExpression);
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new TrivialStringConcatenationVisitor();
    }

    private static class TrivialStringConcatenationVisitor
            extends BaseInspectionVisitor {

        public void visitBinaryExpression(
                @NotNull PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            if (!(expression.getROperand() != null)) {
                return;
            }
            if (!TypeUtils.expressionHasType("java.lang.String", expression)) {
                return;
            }
            final PsiExpression lhs = expression.getLOperand();
            final PsiExpression rhs = expression.getROperand();
            if (ExpressionUtils.isEmptyStringLiteral(lhs)) {
                registerError(expression, expression);
            } else if (ExpressionUtils.isEmptyStringLiteral(rhs)) {
                registerError(expression, expression);
            }
        }
    }
}