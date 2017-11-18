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
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class ResultOfObjectAllocationIgnoredInspection
        extends ExpressionInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "result.of.object.allocation.ignored.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "result.of.object.allocation.ignored.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new IgnoreResultOfCallVisitor();
    }

    private static class IgnoreResultOfCallVisitor
            extends BaseInspectionVisitor {

        public void visitExpressionStatement(
                @NotNull PsiExpressionStatement statement) {
            super.visitExpressionStatement(statement);
            if (!(statement.getExpression() instanceof PsiNewExpression)) {
                return;
            }
            final PsiNewExpression newExpression =
                    (PsiNewExpression) statement.getExpression();
            final PsiExpression[] arrayDimensions =
                    newExpression.getArrayDimensions();
            if (arrayDimensions.length != 0) {
                return;
            }
            if (newExpression.getArrayInitializer() != null) {
                return;
            }
            final PsiJavaCodeReferenceElement classReference =
                    newExpression.getClassReference();
            if (classReference == null) {
                return;
            }
            registerError(classReference);
        }
    }
}