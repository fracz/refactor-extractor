/**
 * class InlineAction
 * created Aug 28, 2001
 * @author Jeka
 */
package com.intellij.refactoring.actions;

import com.intellij.aspects.psi.PsiPointcutDef;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.inline.InlineHandler;
import com.intellij.lang.Language;

public class InlineAction extends BaseRefactoringAction {
  public boolean isAvailableInEditorOnly() {
    return false;
  }

  public boolean isEnabledOnElements(PsiElement[] elements) {
    return elements.length == 1 &&
           (elements[0] instanceof PsiMethod || elements[0] instanceof PsiPointcutDef);
  }

  public RefactoringActionHandler getHandler(DataContext dataContext) {
    return new InlineHandler();
  }

  protected boolean isAvailableForLanguage(Language language) {
    return language.equals(StdFileTypes.JAVA.getLanguage()) ||
           language.equals(StdFileTypes.JSPX.getLanguage()) ||
           language.equals(StdFileTypes.JSP.getLanguage());
  }
}