package com.siyeh.ig.performance;

import com.intellij.psi.*;

class CanBeStaticVisitor extends PsiRecursiveElementVisitor {
    private boolean m_canBeStatic = true;

    CanBeStaticVisitor() {
        super();
    }

    public void visitReferenceExpression(PsiReferenceExpression ref) {
        super.visitReferenceExpression(ref);
        final PsiElement element = ref.resolve();
        if (element instanceof PsiField) {
            final PsiField field = (PsiField) element;
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                m_canBeStatic = false;
            }
        }
        else if (element instanceof PsiVariable) {
            //can happen with initializers of inner classes referencing
            //local variables or parameters from outer class
            m_canBeStatic = false;
        }
    }

    public boolean canBeStatic() {
        return m_canBeStatic;
    }
}