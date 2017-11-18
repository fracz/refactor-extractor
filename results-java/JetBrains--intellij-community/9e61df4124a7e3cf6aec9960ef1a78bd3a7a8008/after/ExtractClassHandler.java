package com.intellij.refactoring.extractclass;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactorJBundle;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;

public class ExtractClassHandler implements RefactoringActionHandler {

  protected static String getRefactoringName() {
    return RefactorJBundle.message("extract.class");
  }

  protected static String getHelpID() {
    return HelpID.ExtractClass;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final ScrollingModel scrollingModel = editor.getScrollingModel();
    scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
    final CaretModel caretModel = editor.getCaretModel();
    final int position = caretModel.getOffset();
    final PsiElement element = file.findElementAt(position);

    final PsiMember selectedMember = PsiTreeUtil.getParentOfType(element, PsiMember.class, true);
    if (selectedMember == null) {
      //todo
      return;
    }
    final PsiClass containingClass = selectedMember instanceof PsiClass ? (PsiClass)selectedMember : selectedMember.getContainingClass();

    if (containingClass == null) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.caret.should.be.positioned.within.a.class.to.be.refactored"),
                                          null, getHelpID());
      return;
    }
    if (containingClass.isInterface()) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.selected.class.is.an.interface"), null,
                                          getHelpID());
      return;
    }
    if (containingClass.isEnum()) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.selected.class.is.an.enumeration"), null,
                                          getHelpID());
      return;
    }
    if (containingClass.isAnnotationType()) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.selected.class.is.an.annotation.type"), null,
                                          getHelpID());
      return;
    }
    if (classIsInner(containingClass) && !containingClass.hasModifierProperty(PsiModifier.STATIC)) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.refactoring.is.not.supported.on.non.static.inner.classes"),
                                          null, getHelpID());
      return;
    }
    if (classIsTrivial(containingClass)) {
      CommonRefactoringUtil.showErrorHint(project, editor, RefactorJBundle.message("cannot.perform.the.refactoring") + RefactorJBundle.message("the.selected.class.has.no.members.to.extract"), null,
                                          getHelpID());
      return;
    }
    new ExtractClassDialog(containingClass, selectedMember).show();
  }

  private static boolean classIsInner(PsiClass aClass) {
    return PsiTreeUtil.getParentOfType(aClass, PsiClass.class, true) != null;
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
    if (elements.length != 1) {
      return;
    }
    final PsiClass containingClass = PsiTreeUtil.getParentOfType(elements[0], PsiClass.class, false);

    final PsiMember selectedMember = PsiTreeUtil.getParentOfType(elements[0], PsiMember.class, false);
    if (containingClass == null) {
      return;
    }
    if (classIsTrivial(containingClass)) {
      return;
    }
    new ExtractClassDialog(containingClass, selectedMember).show();
  }

  private static boolean classIsTrivial(PsiClass containingClass) {
    return containingClass.getFields().length == 0 && containingClass.getMethods().length == 0;
  }
}