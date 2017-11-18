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
package com.siyeh.ig.controlflow;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiConditionalExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.BoolUtils;

public class UnnecessaryConditionalExpressionInspection
        extends ExpressionInspection {

    private final TrivialConditionalFix fix = new TrivialConditionalFix();

    public String getID() {
        return "RedundantConditionalExpression";
    }

    public String getGroupDisplayName() {
        return GroupNames.CONTROL_FLOW_GROUP_NAME;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new UnnecessaryConditionalExpressionVisitor();
    }

    public String buildErrorString(PsiElement location) {
        final PsiConditionalExpression exp = (PsiConditionalExpression)location;
        return InspectionGadgetsBundle.message(
                "simplifiable.conditional.expression.problem.descriptor",
                '\'' + exp.getText(), calculateReplacementExpression(exp));
    }

    private static String calculateReplacementExpression(
            PsiConditionalExpression exp) {
        final PsiExpression thenExpression = exp.getThenExpression();
        final PsiExpression elseExpression = exp.getElseExpression();
        final PsiExpression condition = exp.getCondition();

        if (BoolUtils.isFalse(thenExpression) &&
                BoolUtils.isTrue(elseExpression)) {
            return BoolUtils.getNegatedExpressionText(condition);
        }
        else {
            return condition.getText();
        }
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class TrivialConditionalFix extends InspectionGadgetsFix {

        public String getName() {
            return InspectionGadgetsBundle.message(
                    "constant.conditional.expression.simplify.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiConditionalExpression expression =
                    (PsiConditionalExpression)descriptor.getPsiElement();
            final String newExpression =
                    calculateReplacementExpression(expression);
            replaceExpression(expression, newExpression);
        }
    }

    private static class UnnecessaryConditionalExpressionVisitor
            extends BaseInspectionVisitor {

        public void visitConditionalExpression(PsiConditionalExpression exp) {
            super.visitConditionalExpression(exp);
            final PsiExpression thenExpression = exp.getThenExpression();
            if (thenExpression == null) {
                return;
            }
            final PsiExpression elseExpression = exp.getElseExpression();
            if (elseExpression == null) {
                return;
            }
            if (BoolUtils.isFalse(thenExpression) &&
                    BoolUtils.isTrue(elseExpression) ||
                    BoolUtils.isTrue(thenExpression) &&
                            BoolUtils.isFalse(elseExpression)) {
                registerError(exp);
            }
        }
    }
}