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
package com.siyeh.ig.controlflow;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiSwitchStatement;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.ig.psiutils.SwitchUtils;
import com.siyeh.ig.ui.SingleIntegerFieldOptionsPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class SwitchStatementWithTooFewBranchesInspection
        extends StatementInspection {

    private static final int DEFAULT_BRANCH_LIMIT = 2;
    /**
     * this is public for the DefaultJDOMExternalizer thingy
     * @noinspection PublicField
     */
    public int m_limit = DEFAULT_BRANCH_LIMIT;

    public String getGroupDisplayName() {
        return GroupNames.CONTROL_FLOW_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        return new SingleIntegerFieldOptionsPanel(
                InspectionGadgetsBundle.message(
                        "switch.statement.with.too.few.branches.min.option"),
                this, "m_limit");
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        final Integer branchCount = (Integer)infos[0];
        return InspectionGadgetsBundle.message(
                "switch.statement.with.too.few.branches.problem.descriptor",
                branchCount);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new SwitchStatementWithTooFewBranchesVisitor();
    }

    private class SwitchStatementWithTooFewBranchesVisitor
            extends StatementInspectionVisitor {

        public void visitSwitchStatement(
                @NotNull PsiSwitchStatement statement) {
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return;
            }
            final int branchCount = SwitchUtils.calculateBranchCount(statement);
            if (branchCount >= m_limit) {
                return;
            }
            registerStatementError(statement, Integer.valueOf(branchCount));
        }
    }
}