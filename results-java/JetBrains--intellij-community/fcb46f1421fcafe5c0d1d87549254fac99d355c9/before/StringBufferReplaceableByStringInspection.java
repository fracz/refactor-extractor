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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.ig.psiutils.VariableAccessUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class StringBufferReplaceableByStringInspection
        extends ExpressionInspection{
    public String getDisplayName(){
        return InspectionGadgetsBundle.message("string.buffer.replaceable.by.string.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return InspectionGadgetsBundle.message("string.buffer.replaceable.by.string.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor(){
        return new StringBufferReplaceableByStringBuilderVisitor();
    }

    private static class StringBufferReplaceableByStringBuilderVisitor
            extends BaseInspectionVisitor{

        public void visitLocalVariable(@NotNull PsiLocalVariable variable){
            super.visitLocalVariable(variable);

            final PsiCodeBlock codeBlock =
                    PsiTreeUtil.getParentOfType(variable, PsiCodeBlock.class);
            if(codeBlock == null){
            return;
            }
            final PsiType type = variable.getType();
            if(!TypeUtils.typeEquals("java.lang.StringBuffer", type) &&
                       !TypeUtils.typeEquals("java.lang.StringBuilder", type)){
                return;
            }
            final PsiExpression initializer = variable.getInitializer();
            if(initializer == null){
                return;
            }
            if(!isNewStringBufferOrStringBuilder(initializer)){
                return;
            }
            if(VariableAccessUtils.variableIsAssigned(variable, codeBlock)){
                return;
            }
            if(VariableAccessUtils.variableIsAssignedFrom(variable, codeBlock)){
                return;
            }
            if(VariableAccessUtils.variableIsReturned(variable, codeBlock)){
                return;
            }
            if(VariableAccessUtils.variableIsPassedAsMethodArgument(variable,
                                                                    codeBlock)){
                return;
            }
            if(variableIsModified(variable, codeBlock)){
                return;
            }
            registerVariableError(variable);
        }

        public static boolean variableIsModified(PsiVariable variable,
                                                 PsiElement context){
            final VariableIsModifiedVisitor visitor =
                    new VariableIsModifiedVisitor(variable);
            context.accept(visitor);
            return visitor.isModified();
        }

        private static boolean isNewStringBufferOrStringBuilder(PsiExpression expression){
            if(expression == null){
                return false;
            } else if(expression instanceof PsiNewExpression){
                return true;
            } else if(expression instanceof PsiMethodCallExpression){
                final PsiMethodCallExpression methodCallExpression =
                        (PsiMethodCallExpression)expression;
                if (!VariableIsModifiedVisitor.isStringBufferUpdate(methodCallExpression)) {
                    return false;
                }
                final PsiReferenceExpression methodExpression =
                        ((PsiMethodCallExpression) expression).getMethodExpression();
                final PsiExpression qualifier =
                        methodExpression.getQualifierExpression();
                return isNewStringBufferOrStringBuilder(qualifier);
            }
            return false;
        }
    }
}