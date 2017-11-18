package com.siyeh.ig.verbose;

import com.intellij.psi.*;

public class VariableUsedInInnerClassVisitor extends PsiRecursiveElementVisitor
{
    private final PsiVariable variable;
    private boolean usedInInnerClass = false;
    private boolean inInnerClass = false;

    public VariableUsedInInnerClassVisitor(PsiVariable variable)
    {
        super();
        this.variable = variable;
    }

    public void visitElement(PsiElement element){
        if(!usedInInnerClass){
            super.visitElement(element);
        }
    }

    public void visitAnonymousClass(PsiAnonymousClass psiAnonymousClass)
    {
        if(usedInInnerClass){
            return;
        }
        final boolean wasInInnerClass = inInnerClass;
        inInnerClass = true;
        super.visitAnonymousClass(psiAnonymousClass);
        inInnerClass = wasInInnerClass;
    }

    public void visitReferenceExpression(PsiReferenceExpression reference)
    {
        if(usedInInnerClass){
            return;
        }
        super.visitReferenceExpression(reference);
        if(inInnerClass){
            final PsiElement element = reference.resolve();
            if(variable.equals(element)){
                usedInInnerClass = true;
            }
        }
    }

    public boolean isUsedInInnerClass()
    {
        return usedInInnerClass;
    }
}