package com.siyeh.ig.psiutils;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

public class VariableAssignedVisitor extends PsiRecursiveElementVisitor{
    private boolean assigned = false;
    private final PsiVariable variable;

    public VariableAssignedVisitor(PsiVariable variable){
        super();
        this.variable = variable;
    }

    public void visitElement(PsiElement element){
        if(!assigned){
            super.visitElement(element);
        }
    }

    public void visitAssignmentExpression(PsiAssignmentExpression assignment){
        if(assigned){
            return;
        }
        super.visitAssignmentExpression(assignment);
        final PsiExpression arg = assignment.getLExpression();
        if(!(arg instanceof PsiReferenceExpression)){
            return;
        }
        final PsiElement referent = ((PsiReference) arg).resolve();
        if(referent == null){
            return;
        }
        if(referent.equals(variable)){
            assigned = true;
        }
    }

    public void visitPrefixExpression(PsiPrefixExpression prefixExpression){
        if(assigned){
            return;
        }
        super.visitPrefixExpression(prefixExpression);
        final PsiJavaToken operationSign = prefixExpression.getOperationSign();
        if(operationSign == null){
            return;
        }
        final IElementType tokenType = operationSign.getTokenType();
        if(!tokenType.equals(JavaTokenType.PLUSPLUS) &&
                   !tokenType.equals(JavaTokenType.MINUSMINUS)){
            return;
        }
        final PsiExpression operand = prefixExpression.getOperand();
        if(!(operand instanceof PsiReferenceExpression)){
            return;
        }
        final PsiElement referent = ((PsiReference) operand).resolve();
        if(referent == null){
            return;
        }
        if(referent.equals(variable)){
            assigned = true;
        }
    }

    public void visitPostfixExpression(PsiPostfixExpression postfixExpression){
        if(assigned){
            return;
        }
        super.visitPostfixExpression(postfixExpression);
        final PsiJavaToken operationSign = postfixExpression.getOperationSign();
        if(operationSign == null){
            return;
        }
        final IElementType tokenType = operationSign.getTokenType();
        if(!tokenType.equals(JavaTokenType.PLUSPLUS) &&
                   !tokenType.equals(JavaTokenType.MINUSMINUS)){
            return;
        }
        final PsiExpression operand = postfixExpression.getOperand();
        if(!(operand instanceof PsiReferenceExpression)){
            return;
        }
        final PsiElement referent = ((PsiReference) operand).resolve();
        if(referent == null){
            return;
        }
        if(referent.equals(variable)){
            assigned = true;
        }
    }

    public boolean isAssigned(){
        return assigned;
    }
}