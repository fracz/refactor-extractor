package com.siyeh.ig.junit;

import com.intellij.psi.*;

class CallToSuperSetupVisitor extends PsiRecursiveElementVisitor{
    private boolean callToSuperSetupFound = false;

    public void visitElement(PsiElement element){
        if(!callToSuperSetupFound){
            super.visitElement(element);
        }
    }

    public void visitMethodCallExpression(PsiMethodCallExpression expression){
        if(callToSuperSetupFound){
            return;
        }
        super.visitMethodCallExpression(expression);
        final PsiReferenceExpression methodExpression =
                expression.getMethodExpression();
        if(methodExpression == null){
            return;
        }
        final String methodName = methodExpression.getReferenceName();
        if(!"setUp".equals(methodName)){
            return;
        }
        final PsiExpression target = methodExpression.getQualifierExpression();
        if(!(target instanceof PsiSuperExpression)){
            return;
        }

        callToSuperSetupFound = true;
    }

    public boolean isCallToSuperSetupFound(){
        return callToSuperSetupFound;
    }
}