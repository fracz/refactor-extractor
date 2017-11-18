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
package com.siyeh.ig.encapsulation;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.ig.psiutils.WellFormednessUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class AssignmentToDateFieldFromParameterInspection extends ExpressionInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "assignment.to.date.calendar.field.from.parameter.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.ENCAPSULATION_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final PsiField field = (PsiField) infos[0];
        final PsiExpression rhs = (PsiExpression)infos[1];
        final PsiType type = field.getType();
        return InspectionGadgetsBundle.message(
                "assignment.to.date.calendar.field.from.parameter.problem.descriptor",
                type.getPresentableText(), rhs.getText());
    }

    public BaseInspectionVisitor buildVisitor() {
        return new AssignmentToDateFieldFromParameterVisitor();
    }

    private static class AssignmentToDateFieldFromParameterVisitor
            extends BaseInspectionVisitor {

        public void visitAssignmentExpression(
                @NotNull PsiAssignmentExpression expression) {
            super.visitAssignmentExpression(expression);
            if(!WellFormednessUtils.isWellFormed(expression)){
                return;
            }
            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if (!JavaTokenType.EQ.equals(tokenType)) {
                return;
            }
            final PsiExpression lhs = expression.getLExpression();
            if(!(lhs instanceof PsiReferenceExpression)){
                return;
            }
            if (!TypeUtils.expressionHasTypeOrSubtype("java.util.Date", lhs)
                    && !TypeUtils.expressionHasTypeOrSubtype(
                    "java.util.Calendar", lhs)) {
                return;
            }
            final PsiElement lhsReferent = ((PsiReference) lhs).resolve();
            if(!(lhsReferent instanceof PsiField)){
                return;
            }
            final PsiExpression rhs = expression.getRExpression();
            if (!(rhs instanceof PsiReferenceExpression)) {
                return;
            }
            final PsiElement rhsReferent = ((PsiReference) rhs).resolve();
            if (!(rhsReferent instanceof PsiParameter)) {
                return;
            }
            if (!(rhsReferent.getParent() instanceof PsiParameterList)) {
                return;
            }
            registerError(lhs, lhsReferent, rhs);
        }
    }
}