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
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class TextLabelInSwitchStatementInspection extends StatementInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "text.label.in.switch.statement.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "text.label.in.switch.statement.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new TextLabelInSwitchStatementVisitor();
    }

    private static class TextLabelInSwitchStatementVisitor
            extends StatementInspectionVisitor {

        public void visitSwitchStatement(
                @NotNull PsiSwitchStatement statement) {
            super.visitSwitchStatement(statement);
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            for(PsiStatement statement1 : statements){
                checkForLabel(statement1);
            }
        }

        private void checkForLabel(PsiStatement statement) {
            if (!(statement instanceof PsiLabeledStatement)) {
                return;
            }
            final PsiLabeledStatement labeledStatement =
                    (PsiLabeledStatement)statement;
            final PsiIdentifier label = labeledStatement.getLabelIdentifier();
            registerError(label);
        }
    }
}