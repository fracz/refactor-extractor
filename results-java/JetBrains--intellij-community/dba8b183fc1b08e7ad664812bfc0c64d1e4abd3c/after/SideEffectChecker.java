package com.siyeh.ig.psiutils;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

public class SideEffectChecker{
    private SideEffectChecker(){
        super();
    }

    public static boolean mayHaveSideEffects(PsiExpression exp){
        final SideEffectsVisitor visitor = new SideEffectsVisitor();
        exp.accept(visitor);
        return visitor.mayHaveSideEffects();
    }

    private static class SideEffectsVisitor extends PsiRecursiveElementVisitor{
        private boolean mayHaveSideEffects = false;

        public void visitElement(PsiElement element){
            if(!mayHaveSideEffects){
                super.visitElement(element);
            }
        }

        public void visitAssignmentExpression(PsiAssignmentExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitAssignmentExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitMethodCallExpression(PsiMethodCallExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitMethodCallExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitNewExpression(PsiNewExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitNewExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitPostfixExpression(PsiPostfixExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitPostfixExpression(expression);
            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if(tokenType.equals(JavaTokenType.PLUSPLUS) ||
                       tokenType.equals(JavaTokenType.MINUSMINUS)){
                mayHaveSideEffects = true;
            }
        }

        public void visitPrefixExpression(PsiPrefixExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitPrefixExpression(expression);
            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if(tokenType.equals(JavaTokenType.PLUSPLUS) ||
                       tokenType.equals(JavaTokenType.MINUSMINUS)){
                mayHaveSideEffects = true;
            }
        }

        private boolean mayHaveSideEffects(){
            return mayHaveSideEffects;
        }
    }
}