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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class StringReplaceableByStringBufferInspection
        extends ExpressionInspection {

    public String getID() {
        return "NonConstantStringShouldBeStringBuffer";
    }

    public String getGroupDisplayName() {
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "string.replaceable.by.string.buffer.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new StringReplaceableByStringBufferVisitor();
    }

    private static class StringReplaceableByStringBufferVisitor
            extends BaseInspectionVisitor {

        public void visitLocalVariable(@NotNull PsiLocalVariable variable) {
            super.visitLocalVariable(variable);
            final PsiCodeBlock codeBlock =
                    PsiTreeUtil.getParentOfType(variable, PsiCodeBlock.class);
            if (codeBlock == null) {
                return;
            }
            final PsiType type = variable.getType();
            if (!TypeUtils.typeEquals("java.lang.String", type)) {
                return;
            }
            if (!variableIsAppendedTo(variable, codeBlock)) {
                return;
            }
            registerVariableError(variable);
        }

        public static boolean variableIsAppendedTo(PsiVariable variable,
                                                   PsiElement context) {
            final StringVariableIsAppendedToVisitor visitor =
                    new StringVariableIsAppendedToVisitor(variable);
            context.accept(visitor);
            return visitor.isAppendedTo();
        }
    }
}