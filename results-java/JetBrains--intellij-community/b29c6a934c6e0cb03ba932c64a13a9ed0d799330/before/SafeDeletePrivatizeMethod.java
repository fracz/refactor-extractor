package com.intellij.refactoring.safeDelete.usageInfo;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author dsl
 */
public class SafeDeletePrivatizeMethod extends SafeDeleteUsageInfo implements SafeDeleteCustomUsageInfo {
  public SafeDeletePrivatizeMethod(PsiMethod method, PsiMethod overridenMethod) {
    super(method, overridenMethod);
  }

  public PsiMethod getMethod() {
    return (PsiMethod) getElement();
  }

  public void performRefactoring() throws IncorrectOperationException {
    PsiUtil.setModifierProperty(getMethod(), PsiModifier.PRIVATE, true);
  }
}