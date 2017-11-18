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
package com.siyeh.ig.bugs;

import com.intellij.psi.*;
import com.siyeh.HardcodedMethodConstants;
import org.jetbrains.annotations.NotNull;

class ParameterClassCheckVisitor
        extends PsiRecursiveElementVisitor{
    private final PsiParameter parameter;

    private boolean checked = false;

    ParameterClassCheckVisitor(PsiParameter parameter){
        super();
        this.parameter = parameter;
    }

    public void visitElement(@NotNull PsiElement element){
        if(!checked){
            super.visitElement(element);
        }
    }

    public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression){
        if(checked){
            return;
        }
        super.visitMethodCallExpression(expression);
        final PsiReferenceExpression methodExpression =
                expression.getMethodExpression();
        if(methodExpression == null){
            return;
        }
        final String methodName = methodExpression.getReferenceName();
        if(!HardcodedMethodConstants.GET_CLASS.equals(methodName)){
            return;
        }
        final PsiExpressionList argList = expression.getArgumentList();
        if(argList == null){
            return;
        }
        final PsiExpression[] args = argList.getExpressions();
        if(args == null || args.length != 0){
            return;
        }
        final PsiExpression qualifier =
                methodExpression.getQualifierExpression();

        if(isParameterReference(qualifier)){
            checked = true;
        }
    }

    public void visitInstanceOfExpression(@NotNull PsiInstanceOfExpression expression){
        if(checked){
            return;
        }
        super.visitInstanceOfExpression(expression);
        final PsiExpression operand = expression.getOperand();
        if(isParameterReference(operand)){
            checked = true;
        }
    }

    private boolean isParameterReference(PsiExpression operand){
        if(operand == null){
            return false;
        }
        if(!(operand instanceof PsiReferenceExpression)){
            return false;
        }
        final PsiElement referent = ((PsiReference) operand).resolve();
        if(referent == null){
            return false;
        }
        return referent.equals(parameter);
    }

    public boolean isChecked(){
        return checked;
    }
}