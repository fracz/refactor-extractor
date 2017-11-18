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
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.ig.psiutils.SwitchUtils;
import com.siyeh.ig.ui.SingleIntegerFieldOptionsPanel;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SwitchStatementDensityInspection extends StatementInspection {

    private static final int DEFAULT_DENSITY_LIMIT = 20;
    /**
     * this is public for the DefaultJDOMExternalizer thingy
     * @noinspection PublicField
     */
    public int m_limit = DEFAULT_DENSITY_LIMIT;

    public String getGroupDisplayName() {
        return GroupNames.CONTROL_FLOW_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        return new SingleIntegerFieldOptionsPanel(
                InspectionGadgetsBundle.message(
                        "switch.statement.density.min.option"), this, "m_limit");
    }

    protected String buildErrorString(PsiElement location) {
        final PsiSwitchStatement statement =
                (PsiSwitchStatement)location.getParent();
        final double density = calculateDensity(statement);
        final int intDensity = (int)(density * 100.0);
        return InspectionGadgetsBundle.message(
                "switch.statement.density.problem.descriptor", intDensity);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new SwitchStatementWithTooFewBranchesVisitor();
    }

    private class SwitchStatementWithTooFewBranchesVisitor
            extends StatementInspectionVisitor {

        public void visitSwitchStatement(@NotNull PsiSwitchStatement statement) {
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return;
            }
            final double density = calculateDensity(statement);
            if (density * 100.0 > m_limit) {
                return;
            }
            registerStatementError(statement);
        }
    }

    private static double calculateDensity(PsiSwitchStatement statement) {
        final PsiCodeBlock body = statement.getBody();
        if (body == null) {
            return -1.0;
        }
        final int numBranches = SwitchUtils.calculateBranchCount(statement);
        final StatementCountVisitor visitor = new StatementCountVisitor();
        body.accept(visitor);
        final int numStatements = visitor.getNumStatements();
        return (double)numBranches / (double)numStatements;
    }

    private static class StatementCountVisitor
            extends PsiRecursiveElementVisitor {

        private int numStatements = 0;

        public void visitStatement(@NotNull PsiStatement psiStatement) {
            super.visitStatement(psiStatement);
            numStatements++;
        }

        public int getNumStatements() {
            return numStatements;
        }
    }
}