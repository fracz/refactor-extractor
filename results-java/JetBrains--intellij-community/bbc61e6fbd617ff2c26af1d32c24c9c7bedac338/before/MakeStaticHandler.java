/*
* Created by IntelliJ IDEA.
* User: dsl
* Date: Apr 15, 2002
* Time: 1:25:37 PM
* To change template for new class use
* Code Style | Class Templates options (Tools | IDE Options).
*/
package com.intellij.refactoring.makeStatic;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
/*import com.intellij.refactoring.util.ParameterTablePanel;*/
import com.intellij.refactoring.util.CommonRefactoringUtil;

public class MakeStaticHandler implements RefactoringActionHandler {
  public static final String REFACTORING_NAME = RefactoringBundle.message("make.method.static.title");
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.makeMethodStatic.MakeMethodStaticHandler");

  public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
    PsiElement element = (PsiElement)dataContext.getData(DataConstants.PSI_ELEMENT);
    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
    if (element == null) {
      element = file.findElementAt(editor.getCaretModel().getOffset());
    }

    if (element == null) return;
    if (element instanceof PsiIdentifier) element = element.getParent();

    if(!(element instanceof PsiTypeParameterListOwner)) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("error.wrong.caret.position.method.or.class.name"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }
    if(LOG.isDebugEnabled()) {
      LOG.debug("MakeStaticHandler invoked");
    }
    invoke(project, new PsiElement[]{element}, dataContext);
  }

  public void invoke(final Project project, PsiElement[] elements, DataContext dataContext) {
    if(elements.length != 1 || !(elements[0] instanceof PsiTypeParameterListOwner)) return;

    final PsiTypeParameterListOwner member = (PsiTypeParameterListOwner)elements[0];
    if (!member.isWritable()) {
      if (!CommonRefactoringUtil.checkReadOnlyStatus(project, member)) return;
    }

    final PsiClass containingClass;


    // Checking various preconditions
    if(member instanceof PsiMethod && ((PsiMethod)member).isConstructor()) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("constructor.cannot.be.made.static"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }

    if(member.getContainingClass() == null) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("this.member.does.not.seem.to.belong.to.any.class"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }
    containingClass = member.getContainingClass();

    if(member.hasModifierProperty(PsiModifier.STATIC)) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("member.is.already.static"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }

    if(member.hasModifierProperty(PsiModifier.ABSTRACT)) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("cannot.make.abstract.method.static"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }

    if(containingClass.getContainingClass() != null
       && !containingClass.hasModifierProperty(PsiModifier.STATIC)) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("inner.classes.cannot.have.static.members"));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.MAKE_METHOD_STATIC, project);
      return;
    }

    final InternalUsageInfo[] classRefsInMember = MakeStaticUtil.findClassRefsInMember(member, false);

    /*
    String classParameterName = "anObject";
    ParameterTablePanel.VariableData[] fieldParameterData = null;

    */
    AbstractMakeStaticDialog dialog;
    if (!ApplicationManager.getApplication().isUnitTestMode()) {

      if (classRefsInMember.length > 0) {
        final PsiType type =
                containingClass.getManager().getElementFactory().createType(containingClass);
        //TODO: callback
        String[] nameSuggestions =
                CodeStyleManager.getInstance(project).suggestVariableName(VariableKind.PARAMETER, null, null, type).names;

        dialog = new MakeParameterizedStaticDialog(project, member,
                                                   nameSuggestions,
                                                   classRefsInMember);


      }
      else {
        dialog = new SimpleMakeStaticDialog(project, member);
      }

      dialog.show();
      return;
    }
  }
}