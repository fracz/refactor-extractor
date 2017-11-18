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
package com.siyeh.ig.psiutils;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class SideEffectChecker{

    private SideEffectChecker(){
        super();
    }

    public static boolean mayHaveSideEffects(@NotNull PsiExpression exp){
        final SideEffectsVisitor visitor = new SideEffectsVisitor();
        exp.accept(visitor);
        return visitor.mayHaveSideEffects();
    }

    private static class SideEffectsVisitor extends PsiRecursiveElementVisitor{

        private boolean mayHaveSideEffects = false;

        public void visitElement(@NotNull PsiElement element){
            if(!mayHaveSideEffects){
                super.visitElement(element);
            }
        }

        public void visitAssignmentExpression(
                @NotNull PsiAssignmentExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitAssignmentExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitMethodCallExpression(
                @NotNull PsiMethodCallExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitMethodCallExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitNewExpression(@NotNull PsiNewExpression expression){
            if(mayHaveSideEffects){
                return;
            }
            super.visitNewExpression(expression);
            mayHaveSideEffects = true;
        }

        public void visitPostfixExpression(
                @NotNull PsiPostfixExpression expression){
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

        public void visitPrefixExpression(
                @NotNull PsiPrefixExpression expression){
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

        public boolean mayHaveSideEffects(){
            return mayHaveSideEffects;
        }
    }
}