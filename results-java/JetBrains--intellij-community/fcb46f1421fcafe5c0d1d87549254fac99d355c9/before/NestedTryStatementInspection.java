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
package com.siyeh.ig.errorhandling;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTryStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import org.jetbrains.annotations.NotNull;

public class NestedTryStatementInspection extends StatementInspection {

    public String getGroupDisplayName() {
        return GroupNames.ERRORHANDLING_GROUP_NAME;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new NestedTryStatementVisitor();
    }

    private static class NestedTryStatementVisitor
            extends StatementInspectionVisitor {

        public void visitTryStatement(@NotNull PsiTryStatement statement) {
            super.visitTryStatement(statement);
            final PsiTryStatement parentTry =
                    PsiTreeUtil.getParentOfType(statement,
                            PsiTryStatement.class);
            if (parentTry == null) {
                return;
            }
            final PsiCodeBlock tryBlock = parentTry.getTryBlock();
            if (tryBlock == null) {
                return;
            }
            if (!PsiTreeUtil.isAncestor(tryBlock, statement, true)) {
                return;
            }
            final PsiMethod containingMethod =
                    PsiTreeUtil.getParentOfType(statement, PsiMethod.class);
            final PsiMethod containingContainingMethod =
                    PsiTreeUtil.getParentOfType(parentTry, PsiMethod.class);
            if (containingMethod == null ||
                    containingContainingMethod == null ||
                    !containingMethod.equals(containingContainingMethod)) {
                return;
            }
            registerStatementError(statement);
        }

    }
}