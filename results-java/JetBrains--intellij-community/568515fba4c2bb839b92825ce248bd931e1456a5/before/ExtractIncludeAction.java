package com.intellij.refactoring.actions;

import com.intellij.lang.Language;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.jsp.JspFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.lang.html.ExtractIncludeFromHTMLHandler;
import com.intellij.refactoring.lang.jsp.extractInclude.ExtractJspIncludeFileHandler;

/**
 * @author ven
 */
public class ExtractIncludeAction extends BaseRefactoringAction {
  public boolean isAvailableInEditorOnly() {
    return true;
  }

  public boolean isEnabledOnElements(PsiElement[] elements) {
    return false;
  }

  protected boolean isAvailableForLanguage(Language language) {
    return true;
  }

  protected boolean isAvailableForFile(PsiFile file) {
    return file instanceof JspFile || Language.findInstance(HTMLLanguage.class).equals(file.getLanguage()) ||
      Language.findInstance(XHTMLLanguage.class).equals(file.getLanguage());
  }

  public RefactoringActionHandler getHandler(DataContext dataContext) {
    PsiFile file = (PsiFile)dataContext.getData(DataConstants.PSI_FILE);
    if (file instanceof JspFile) {
      return new ExtractJspIncludeFileHandler(file);
    }
    else if (Language.findInstance(HTMLLanguage.class).equals(file.getLanguage()) ||
             Language.findInstance(XHTMLLanguage.class).equals(file.getLanguage())) {
      return new ExtractIncludeFromHTMLHandler(file);
    }
    return null;
  }
}