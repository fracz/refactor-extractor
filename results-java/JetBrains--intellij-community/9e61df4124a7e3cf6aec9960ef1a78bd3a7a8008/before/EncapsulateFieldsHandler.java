package com.intellij.refactoring.encapsulateFields;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;

import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

public class EncapsulateFieldsHandler implements RefactoringActionHandler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.encapsulateFields.EncapsulateFieldsHandler");
  public static final String REFACTORING_NAME = RefactoringBundle.message("encapsulate.fields.title");

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    int offset = editor.getCaretModel().getOffset();
    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
    PsiElement element = file.findElementAt(offset);
    while (true) {
      if (element == null || element instanceof PsiFile) {
        String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("error.wrong.caret.position.class"));
        CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.ENCAPSULATE_FIELDS, project);
        return;
      }
      if (element instanceof PsiField) {
        if (((PsiField) element).getContainingClass() == null) {
          String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("the.field.should.be.declared.in.a.class"));
          CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.ENCAPSULATE_FIELDS, project);
          return;
        }
        invoke(project, new PsiElement[]{element}, dataContext);
        return;
      }
      if (element instanceof PsiClass) {
        invoke(project, new PsiElement[]{element}, dataContext);
        return;
      }
      element = element.getParent();
    }
  }

  /**
   * if elements.length == 1 the expected value is either PsiClass or PsiField
   * if elements.length > 1 the expected values are PsiField objects only
   */
  public void invoke(@NotNull final Project project, @NotNull final PsiElement[] elements, DataContext dataContext) {
    PsiClass aClass = null;
    final HashSet<PsiField> preselectedFields = new HashSet<PsiField>();
    if (elements.length == 1) {
      if (elements[0] instanceof PsiClass) {
        aClass = (PsiClass) elements[0];
      } else if (elements[0] instanceof PsiField) {
        PsiField field = (PsiField) elements[0];
        aClass = field.getContainingClass();
        preselectedFields.add(field);
      } else {
        return;
      }
    } else {
      for (PsiElement element : elements) {
        if (!(element instanceof PsiField)) {
          return;
        }
        PsiField field = (PsiField)element;
        if (aClass == null) {
          aClass = field.getContainingClass();
          preselectedFields.add(field);
        }
        else {
          if (aClass.equals(field.getContainingClass())) {
            preselectedFields.add(field);
          }
          else {
            String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("fields.to.be.refactored.should.belong.to.the.same.class"));
            CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.ENCAPSULATE_FIELDS, project);
            return;
          }
        }
      }
    }

    LOG.assertTrue(aClass != null);

    if (aClass.isInterface()) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("encapsulate.fields.refactoring.cannot.be.applied.to.interface"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.ENCAPSULATE_FIELDS, project);
      return;
    }

    if (!CommonRefactoringUtil.checkReadOnlyStatus(project, aClass)) return;

    EncapsulateFieldsDialog dialog = new EncapsulateFieldsDialog(
            project,
            aClass,
            preselectedFields);
    dialog.show();
  }
}