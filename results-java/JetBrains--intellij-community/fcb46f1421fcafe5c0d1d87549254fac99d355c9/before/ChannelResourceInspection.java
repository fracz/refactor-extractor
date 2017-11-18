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
package com.siyeh.ig.resources;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.HardcodedMethodConstants;
import org.jetbrains.annotations.NotNull;

public class ChannelResourceInspection extends ExpressionInspection{
    public String getID(){
        return "ChannelOpenedButNotSafelyClosed";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message("channel.opened.not.closed.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.RESOURCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        final PsiExpression expression = (PsiExpression) location;
        final PsiType type = expression.getType();
        final String text = type.getPresentableText();
        return InspectionGadgetsBundle.message("channel.opened.not.closed.problem.descriptor", text);
    }

    public BaseInspectionVisitor buildVisitor(){
        return new ChannelResourceVisitor();
    }

    private static class ChannelResourceVisitor extends BaseInspectionVisitor{

        public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression){
            super.visitMethodCallExpression(expression);
            if(!isChannelFactoryMethod(expression)) {
                return;
            }
            final PsiElement parent = expression.getParent();
            if(!(parent instanceof PsiAssignmentExpression)) {
                registerError(expression);
                return;
            }
            final PsiAssignmentExpression assignment =
                    (PsiAssignmentExpression) parent;
            final PsiExpression lhs = assignment.getLExpression();
            if(!(lhs instanceof PsiReferenceExpression)) {
                return;
            }
            final PsiElement referent =
                    ((PsiReference) lhs).resolve();
            if(referent == null || !(referent instanceof PsiVariable)) {
                return;
            }
            final PsiVariable boundVariable = (PsiVariable) referent;

            PsiElement currentContext = expression;
            while(true){
                final PsiTryStatement tryStatement =
                        PsiTreeUtil.getParentOfType(currentContext,
                                                                      PsiTryStatement.class);
                if(tryStatement == null) {
                    registerError(expression);
                    return;
                }
                if(resourceIsOpenedInTryAndClosedInFinally(tryStatement,
                                                           expression,
                                                           boundVariable)) {
                    return;
                }
                currentContext = tryStatement;
            }
        }


        private static boolean resourceIsOpenedInTryAndClosedInFinally(PsiTryStatement tryStatement,
                                                                       PsiExpression lhs,
                                                                       PsiVariable boundVariable){
            final PsiCodeBlock finallyBlock = tryStatement.getFinallyBlock();
            if(finallyBlock == null){
                return false;
            }
            final PsiCodeBlock tryBlock = tryStatement.getTryBlock();
            if(tryBlock == null){
                return false;
            }
            if(!PsiTreeUtil.isAncestor(tryBlock, lhs, true)){
                return false;
            }
            return containsResourceClose(finallyBlock, boundVariable);
        }

        private static boolean containsResourceClose(PsiCodeBlock finallyBlock,
                                                     PsiVariable boundVariable){
            final CloseVisitor visitor =
                    new CloseVisitor(boundVariable);
            finallyBlock.accept(visitor);
            return visitor.containsStreamClose();
        }
    }

    private static class CloseVisitor extends PsiRecursiveElementVisitor{
        private boolean containsClose = false;
        private PsiVariable objectToClose;

        private CloseVisitor(PsiVariable objectToClose){
            super();
            this.objectToClose = objectToClose;
        }

        public void visitElement(@NotNull PsiElement element){
            if(!containsClose){
                super.visitElement(element);
            }
        }

        public void visitMethodCallExpression(@NotNull PsiMethodCallExpression call){
            if(containsClose){
                return;
            }
            super.visitMethodCallExpression(call);
            final PsiReferenceExpression methodExpression =
                    call.getMethodExpression();
            if(methodExpression == null){
                return;
            }
            final String methodName = methodExpression.getReferenceName();
            if(!HardcodedMethodConstants.CLOSE.equals(methodName)){
                return;
            }
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            if(!(qualifier instanceof PsiReferenceExpression)){
                return;
            }
            final PsiElement referent =
                    ((PsiReference) qualifier).resolve();
            if(referent == null)
            {
                return;
            }
            if(referent.equals(objectToClose)){
                containsClose = true;
            }
        }

        public boolean containsStreamClose(){
            return containsClose;
        }
    }

    private static boolean isChannelFactoryMethod(PsiMethodCallExpression expression){
        final PsiReferenceExpression methodExpression = expression.getMethodExpression();
        if(methodExpression == null) {
            return false;
        }
        final String methodName = methodExpression.getReferenceName();
        if(!HardcodedMethodConstants.GET_CHANNEL.equals(methodName)) {
            return false;
        }
        final PsiExpression qualifier = methodExpression.getQualifierExpression();
        if(qualifier == null) {
            return false;
        }
        return TypeUtils.expressionHasTypeOrSubtype("java.net.Socket",
                                                    qualifier)||
                TypeUtils.expressionHasTypeOrSubtype("java.net.DatagramSocket",
                                                    qualifier)||
                TypeUtils.expressionHasTypeOrSubtype("java.net.ServerSocket",
                                                    qualifier)||
                TypeUtils.expressionHasTypeOrSubtype("java.io.FileInputStream",
                                                    qualifier)||
                TypeUtils.expressionHasTypeOrSubtype("java.io.FileOutputStream",
                                                    qualifier)||
                TypeUtils.expressionHasTypeOrSubtype("java.io.RandomAccessFile",
                                                    qualifier);
    }

}