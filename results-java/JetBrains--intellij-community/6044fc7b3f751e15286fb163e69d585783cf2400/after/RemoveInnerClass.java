package com.intellij.refactoring.extractclass;

import com.intellij.psi.PsiClass;
import com.intellij.refactoring.util.FixableUsageInfo;
import com.intellij.util.IncorrectOperationException;

class RemoveInnerClass extends FixableUsageInfo {
    private final PsiClass innerClass;

    RemoveInnerClass(PsiClass innerClass) {
        super(innerClass);
        this.innerClass = innerClass;
    }

    public void fixUsage() throws IncorrectOperationException{
        innerClass.delete();
    }
}