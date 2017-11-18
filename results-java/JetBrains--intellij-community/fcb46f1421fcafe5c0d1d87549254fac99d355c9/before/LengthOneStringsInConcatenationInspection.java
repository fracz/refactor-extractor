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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class LengthOneStringsInConcatenationInspection
        extends ExpressionInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "length.one.strings.in.concatenation.display.name");
    }

    public String getID() {
        return "SingleCharacterStringConcatenation";
    }

    public String getGroupDisplayName() {
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        final String text = location.getText();
        final int length = text.length();
        final String transformedText =
                '\'' + text.substring(1, length - 1) + '\'';
        return InspectionGadgetsBundle.message(
                "length.one.strings.in.concatenation.problem.descriptor",
                transformedText);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new LengthOneStringsInConcatenationVisitor();
    }

    @Nullable
    protected String buildErrorString(Object arg) {
        return InspectionGadgetsBundle.message(
                "length.one.strings.in.concatenation.problem.descriptor");
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return new ReplaceStringsWithCharsFix();
    }

    private static class ReplaceStringsWithCharsFix
            extends InspectionGadgetsFix {

        public String getName() {
            return InspectionGadgetsBundle.message(
                    "length.one.strings.in.concatenation.replace.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiExpression expression =
                    (PsiExpression)descriptor.getPsiElement();
            final String text = expression.getText();
            final int length = text.length();
            final String character = text.substring(1, length - 1);
            final String charLiteral;
            if ("\'".equals(character)) {
                charLiteral = "'\\''";
            } else {
                charLiteral = '\'' + character + '\'';
            }
            replaceExpression(expression, charLiteral);
        }
    }

    private static class LengthOneStringsInConcatenationVisitor
            extends BaseInspectionVisitor {

        public void visitLiteralExpression(
                @NotNull PsiLiteralExpression expression) {
            super.visitLiteralExpression(expression);
            final PsiType type = expression.getType();
            if (!TypeUtils.isJavaLangString(type)) {
                return;
            }
            final String value = (String)expression.getValue();
            if (value == null || value.length() != 1) {
                return;
            }
            if (!isArgumentOfConcatenation(expression) &&
                !isArgumentOfStringAppend(expression)) {
                return;
            }
            registerError(expression);
        }

        private static boolean isArgumentOfConcatenation(
                PsiExpression expression) {
            final PsiElement parent = expression.getParent();
            if (!(parent instanceof PsiBinaryExpression)) {
                return false;
            }
            final PsiBinaryExpression binaryExp = (PsiBinaryExpression)parent;
            final PsiJavaToken sign = binaryExp.getOperationSign();
            if (!JavaTokenType.PLUS.equals(sign.getTokenType())) {
                return false;
            }
            final PsiExpression lhs = binaryExp.getLOperand();
            final PsiExpression sibling;
            if (lhs.equals(expression)) {
                sibling = binaryExp.getROperand();
            } else {
                sibling = lhs;
            }
            if (sibling == null) {
                return false;
            }
            final PsiType siblingType = sibling.getType();
            return TypeUtils.isJavaLangString(siblingType);
        }

        static boolean isArgumentOfStringAppend(PsiExpression expression) {
            final PsiElement parent = expression.getParent();
            if (parent == null) {
                return false;
            }
            if (!(parent instanceof PsiExpressionList)) {
                return false;
            }
            final PsiElement grandparent = parent.getParent();
            if (!(grandparent instanceof PsiMethodCallExpression)) {
                return false;
            }
            final PsiMethodCallExpression call =
                    (PsiMethodCallExpression)grandparent;
            final PsiReferenceExpression methodExpression =
                    call.getMethodExpression();
            @NonNls final String name = methodExpression.getReferenceName();
            if (!"append".equals(name) && !"insert".equals(name)) {
                return false;
            }
            final PsiMethod method = call.resolveMethod();
            if (method == null) {
                return false;
            }
            final PsiClass methodClass = method.getContainingClass();
            if (methodClass == null) {
                return false;
            }
            final String className = methodClass.getQualifiedName();
            return "java.lang.StringBuffer".equals(className) ||
                   "java.lang.StringBuilder".equals(className);
        }
    }
}