package com.intellij.refactoring.extractInterface;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.localVcs.LvcsAction;
import com.intellij.openapi.localVcs.impl.LvcsIntegration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.memberPullUp.PullUpHelper;
import com.intellij.refactoring.util.JavaDocPolicy;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.IncorrectOperationException;

public class ExtractInterfaceHandler implements RefactoringActionHandler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.extractInterface.ExtractInterfaceHandler");

  public static final String REFACTORING_NAME = RefactoringBundle.message("extract.interface.title");


  private Project myProject;
  private PsiClass myClass;

  private String myInterfaceName;
  private MemberInfo[] mySelectedMembers;
  private PsiDirectory myTargetDir;
  private JavaDocPolicy myJavaDocPolicy;

  public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
    int offset = editor.getCaretModel().getOffset();
    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
    PsiElement element = file.findElementAt(offset);
    while (true) {
      if (element == null || element instanceof PsiFile) {
        String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("error.wrong.caret.position.class"));
        CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.EXTRACT_INTERFACE, project);
        return;
      }
      if (element instanceof PsiClass && !(element instanceof PsiAnonymousClass)) {
        invoke(project, new PsiElement[]{element}, dataContext);
        return;
      }
      element = element.getParent();
    }
  }

  public void invoke(final Project project, PsiElement[] elements, DataContext dataContext) {
    if (elements.length != 1) return;

    myProject = project;
    myClass = (PsiClass) elements[0];


    if (!myClass.isWritable()) {
      if (!CommonRefactoringUtil.checkReadOnlyStatus(project, myClass)) return;
    }

    final ExtractInterfaceDialog dialog = new ExtractInterfaceDialog(
            myProject,
            myClass
    );
    dialog.show();
    if (!dialog.isOK() || !dialog.isExtractSuperclass()) return;

    CommandProcessor.getInstance().executeCommand(
        myProject, new Runnable() {
              public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                  public void run() {
                    myInterfaceName = dialog.getInterfaceName();
                    mySelectedMembers = dialog.getSelectedMembers();
                    myTargetDir = dialog.getTargetDirectory();
                    myJavaDocPolicy = new JavaDocPolicy(dialog.getJavaDocPolicy());
                    try {
                      doRefactoring();
                    } catch (IncorrectOperationException e) {
                      LOG.error(e);
                    }
                  }
                });
              }
            },
        REFACTORING_NAME,
        null
    );

  }


  private void doRefactoring() throws IncorrectOperationException {
    LvcsAction action = LvcsIntegration.checkinFilesBeforeRefactoring(myProject, getCommandName());
    PsiClass anInterface = null;
    try {
      anInterface = extractInterface(myTargetDir, myClass, myInterfaceName, mySelectedMembers, myJavaDocPolicy);
    } catch(IncorrectOperationException ex) {
      throw ex;
    } finally {
      LvcsIntegration.checkinFilesAfterRefactoring(myProject, action);
    }

    /*PsiJavaCodeReferenceElement ref = myManager.getElementFactory().createClassReferenceElement(anInterface);
    sourceClassRefList.add(ref);*/
    if (anInterface != null) {
      ExtractClassUtil.askAndTurnRefsToSuper(myProject, myClass, anInterface);
    }
  }

  static PsiClass extractInterface(PsiDirectory targetDir,
                                   PsiClass aClass,
                                   String interfaceName,
                                   MemberInfo[] selectedMembers,
                                   JavaDocPolicy javaDocPolicy) throws IncorrectOperationException {
    PsiClass anInterface;
    anInterface = targetDir.createInterface(interfaceName);
    PullUpHelper pullUpHelper = new PullUpHelper(aClass, anInterface, selectedMembers, javaDocPolicy);
    pullUpHelper.moveMembersToBase();
    final PsiManager manager = aClass.getManager();
    PsiJavaCodeReferenceElement ref = manager.getElementFactory().createClassReferenceElement(anInterface);
    final PsiReferenceList referenceList = aClass.isInterface() ? aClass.getExtendsList() : aClass.getImplementsList();
    referenceList.add(ref);
    return anInterface;
  }

  private String getCommandName() {
    return RefactoringBundle.message("extract.interface.command.name", myInterfaceName, UsageViewUtil.getDescriptiveName(myClass));
  }

}