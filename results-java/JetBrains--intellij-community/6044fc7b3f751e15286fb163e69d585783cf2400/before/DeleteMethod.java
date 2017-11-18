package com.intellij.refactoring.removemiddleman;

import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.base.RefactorJUsageInfo;
import com.intellij.util.IncorrectOperationException;

class DeleteMethod extends RefactorJUsageInfo {
    private final PsiMethod method;

    DeleteMethod(PsiMethod method) {
        super(method);
        this.method = method;
    }

    public void fixUsage() throws IncorrectOperationException {
        method.delete();
    }
}