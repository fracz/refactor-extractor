
package com.intellij.refactoring.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.anonymousToInner.AnonymousToInnerHandler;

public class AnonymousToInnerAction extends BaseRefactoringAction {
  public boolean isAvailableInEditorOnly() {
    return true;
  }

  public boolean isEnabledOnElements(PsiElement[] elements) {
    return false;
  }

  public RefactoringActionHandler getHandler(DataContext dataContext) {
    return new AnonymousToInnerHandler();
  }
}