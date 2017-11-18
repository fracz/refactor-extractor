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
package com.siyeh.ig.jdk;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiAssertStatement;
import com.intellij.psi.PsiElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.InspectionGadgetsBundle;

public class AssertStatementInspection extends StatementInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "assert.statement.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.JDK_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "assert.statement.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new AssertStatementVisitor();
    }

    private static class AssertStatementVisitor
            extends StatementInspectionVisitor {

        public void visitAssertStatement(PsiAssertStatement statement) {
            super.visitAssertStatement(statement);
            registerStatementError(statement);
        }
    }
}