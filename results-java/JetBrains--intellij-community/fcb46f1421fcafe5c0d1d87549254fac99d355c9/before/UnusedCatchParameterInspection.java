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
import com.intellij.psi.*;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.ui.MultipleCheckboxOptionsPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class UnusedCatchParameterInspection extends StatementInspection {

    /**
     * @noinspection PublicField
     */
    public boolean m_ignoreCatchBlocksWithComments = false;
    /**
     * @noinspection PublicField
     */
    public boolean m_ignoreTestCases = false;

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "unused.catch.parameter.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.ERRORHANDLING_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        final MultipleCheckboxOptionsPanel optionsPanel =
                new MultipleCheckboxOptionsPanel(this);
        optionsPanel.addCheckbox(InspectionGadgetsBundle.message(
                "unused.catch.parameter.ignore.catch.option"),
                "m_ignoreCatchBlocksWithComments");
        optionsPanel.addCheckbox(InspectionGadgetsBundle.message(
                "unused.catch.parameter.ignore.empty.option"),
                "m_ignoreTestCases");
        return optionsPanel;
    }

    @Nullable
    protected String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "unused.catch.parameter.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new EmptyCatchBlockVisitor();
    }

    private class EmptyCatchBlockVisitor extends StatementInspectionVisitor {

        public void visitTryStatement(@NotNull PsiTryStatement statement) {
            super.visitTryStatement(statement);
            if (m_ignoreTestCases) {
                final PsiClass aClass =
                        ClassUtils.getContainingClass(statement);
                if (aClass != null &&
                        ClassUtils.isSubclass(aClass,
                                "junit.framework.TestCase")) {
                    return;
                }
            }
            final PsiCatchSection[] catchSections =
                    statement.getCatchSections();
            for (PsiCatchSection catchSection : catchSections) {
                checkCatchSection(catchSection);
            }
        }

        private void checkCatchSection(PsiCatchSection section) {
            final PsiParameter param = section.getParameter();
            final PsiCodeBlock block = section.getCatchBlock();
            if (param == null || block == null) {
                return;
            }
            @NonNls final String paramName = param.getName();
            if ("ignore".equals(paramName) || "ignored".equals(paramName)) {
                return;
            }
            if (m_ignoreCatchBlocksWithComments) {
                final PsiElement[] children = block.getChildren();
                for (final PsiElement child : children) {
                    if (child instanceof PsiComment) {
                        return;
                    }
                }
            }
            final CatchParameterUsedVisitor visitor =
                    new CatchParameterUsedVisitor(param);
            block.accept(visitor);
            if (!visitor.isUsed()) {
                registerVariableError(param);
            }
        }
    }
}