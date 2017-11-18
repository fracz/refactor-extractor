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

public class CallToSimpleGetterInClassInspection extends ExpressionInspection{

    private final InlineCallFix fix = new InlineCallFix();

    public String getID(){
        return "CallToSimpleGetterFromWithinClass";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "call.to.simple.getter.in.class.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return InspectionGadgetsBundle.message(
                "call.to.simple.getter.in.class.problem.descriptor");
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class InlineCallFix extends InspectionGadgetsFix{

        public String getName(){
            return InspectionGadgetsBundle.message(
                    "call.to.simple.getter.in.class.inline.quickfix");
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
            final PsiMethod method = call.resolveMethod();
            if (method == null){
                return;
            }
            final PsiCodeBlock body = method.getBody();
            if (body == null){
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            final PsiReturnStatement returnStatement =
                    (PsiReturnStatement) statements[0];
            final PsiReferenceExpression returnValue = (PsiReferenceExpression)
                    returnStatement.getReturnValue();
            if (returnValue == null){
                return;
            }
            final PsiField field = (PsiField)returnValue.resolve();
            if (field == null) {
                return;
            }
            final String fieldName = field.getName();
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
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
                if (variable.equals(field)){
                    replaceExpression(call, fieldName);
                } else{
                    replaceExpression(call, "this." + fieldName);
                }
            } else{
                replaceExpression(call, qualifier.getText() + '.' + fieldName);
            }
        }
    }

    public BaseInspectionVisitor buildVisitor(){
        return new CallToSimpleGetterInClassVisitor();
    }

    private static class CallToSimpleGetterInClassVisitor
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
            if(!MethodUtils.isSimpleGetter(method)){
                return;
            }
            registerMethodCallError(call);
        }
    }
}