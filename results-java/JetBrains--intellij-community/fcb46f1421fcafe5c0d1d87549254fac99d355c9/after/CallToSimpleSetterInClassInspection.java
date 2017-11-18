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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.MethodUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

public class CallToSimpleSetterInClassInspection extends ExpressionInspection{

    public String getID(){
        return "CallToSimpleSetterFromWithinClass";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "call.to.simple.setter.in.class.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos){
        return InspectionGadgetsBundle.message(
                "call.to.simple.setter.in.class.problem.descriptor");
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return new InlineCallFix();
    }

    private static class InlineCallFix extends InspectionGadgetsFix{

        public String getName(){
            return InspectionGadgetsBundle.message(
                    "call.to.simple.setter.in.class.inline.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException{
            final PsiElement methodIdentifier = descriptor.getPsiElement();
            final PsiReferenceExpression methodExpression =
                    (PsiReferenceExpression) methodIdentifier.getParent();
            if (methodExpression == null){
                return;
            }
            final PsiMethodCallExpression call =
                    (PsiMethodCallExpression) methodExpression.getParent();
            if (call == null){
                return;
            }
            final PsiExpressionList argList = call.getArgumentList();
            final PsiExpression[] args = argList.getExpressions();
            final PsiExpression arg = args[0];
            final PsiMethod method = call.resolveMethod();
            if (method == null){
                return;
            }
            final PsiCodeBlock body = method.getBody();
            if (body == null){
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            final PsiExpressionStatement assignmentStatement =
                    (PsiExpressionStatement) statements[0];
            final PsiAssignmentExpression assignment = (PsiAssignmentExpression)
                    assignmentStatement.getExpression();
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            final PsiReferenceExpression lhs = (PsiReferenceExpression)
                    assignment.getLExpression();
            final PsiField field = (PsiField)lhs.resolve();
            if (field == null){
                return;
            }
            final String fieldName = field.getName();
            if(qualifier == null){
                final PsiManager manager = call.getManager();
                final PsiResolveHelper resolveHelper =
                        manager.getResolveHelper();
                final PsiVariable variable =
                        resolveHelper.resolveReferencedVariable(fieldName,
                                                                call);
                if (variable == null) {
                    return;
                }
                @NonNls final String newExpression;
                if (variable.equals(field)){
                    newExpression = fieldName + " = " + arg.getText();
                } else{
                    newExpression = "this." + fieldName + " = " + arg.getText();
                }
                replaceExpression(call, newExpression);
            } else{
                final String newExpression = qualifier.getText() + '.' +
                                             fieldName + " = " + arg.getText();
                replaceExpression(call, newExpression);
            }
        }
    }

    public BaseInspectionVisitor buildVisitor(){
        return new CallToSimpleSetterInClassVisitor();
    }

    private static class CallToSimpleSetterInClassVisitor
            extends BaseInspectionVisitor{

        public void visitMethodCallExpression(
                @NotNull PsiMethodCallExpression call){
            super.visitMethodCallExpression(call);
            final PsiClass containingClass =
                    ClassUtils.getContainingClass(call);
            if(containingClass == null){
                return;
            }
            final PsiMethod method = call.resolveMethod();
            if(method == null){
                return;
            }
            if(!containingClass.equals(method.getContainingClass())){
                return;
            }
            if(!MethodUtils.isSimpleSetter(method)){
                return;
            }
            registerMethodCallError(call);
        }
    }
}