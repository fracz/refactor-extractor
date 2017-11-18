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
package com.siyeh.ig.j2me;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class MethodCallInLoopConditionInspection extends StatementInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("method.call.in.loop.condition.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.J2ME_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("method.call.in.loop.condition.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new MethodCallInLoopConditionVisitor();
    }

    private static class MethodCallInLoopConditionVisitor extends StatementInspectionVisitor {
        public void visitForStatement(@NotNull PsiForStatement statement) {
            super.visitForStatement(statement);
            final PsiExpression condition = statement.getCondition();
            if(condition== null)
            {
                return;
            }
            checkForMethodCalls(condition);
        }

        public void visitWhileStatement(@NotNull PsiWhileStatement statement) {
            super.visitWhileStatement(statement);
            final PsiExpression condition = statement.getCondition();
            if(condition == null){
                return;
            }
            checkForMethodCalls(condition);
        }


        public void visitDoWhileStatement(@NotNull PsiDoWhileStatement statement) {
            super.visitDoWhileStatement(statement);
            final PsiExpression condition = statement.getCondition();
            if(condition == null){
                return;
            }
            checkForMethodCalls(condition);;
        }

        private void checkForMethodCalls(PsiExpression condition){
            final PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor(){
                public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression){
                    super.visitMethodCallExpression(expression);
                    registerMethodCallError(expression);
                }
            };
            condition.accept(visitor);
        }

    }

}