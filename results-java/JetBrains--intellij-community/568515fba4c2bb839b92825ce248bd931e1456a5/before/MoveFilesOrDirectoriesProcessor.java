package com.intellij.refactoring.move.moveFilesOrDirectories;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.usageView.FindUsagesCommand;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NonNls;

public class MoveFilesOrDirectoriesProcessor extends BaseRefactoringProcessor {
  private static final Logger LOG = Logger.getInstance(
    "#com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor");

  private PsiElement[] myElementsToMove;
  private final boolean mySearchInComments;
  private final boolean mySearchInNonJavaFiles;
  private PsiDirectory myNewParent;
  private MoveCallback myMoveCallback;

  public MoveFilesOrDirectoriesProcessor(
    Project project,
    PsiElement[] elements,
    PsiDirectory newParent,
    boolean searchInComments,
    boolean searchInNonJavaFiles,
    MoveCallback moveCallback,
    Runnable prepareSuccessfulCallback) {
    super(project, prepareSuccessfulCallback);
    myElementsToMove = elements;
    myNewParent = newParent;
    mySearchInComments = searchInComments;
    mySearchInNonJavaFiles = searchInNonJavaFiles;
    myMoveCallback = moveCallback;
  }

  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usages, FindUsagesCommand refreshCommand) {
    PsiElement[] elements = new PsiElement[myElementsToMove.length];
    for (int idx = 0; idx < myElementsToMove.length; idx++) {
      elements[idx] = myElementsToMove[idx];
    }
    return new MoveFilesOrDirectoriesViewDescriptor(elements, mySearchInComments, mySearchInNonJavaFiles, myNewParent,
                                                    usages, refreshCommand);
  }

  protected UsageInfo[] findUsages() {
    final PsiSearchHelper searchHelper = PsiManager.getInstance(myProject).getSearchHelper();
    List<UsageInfo> result = new ArrayList<UsageInfo>();
    for (int i = 0; i < myElementsToMove.length; i++) {
      PsiElement element = myElementsToMove[i];
      final PsiReference[] refs = searchHelper.findReferences(element, GlobalSearchScope.allScope(myProject), false);
      for (PsiReference reference : refs) {
        result.add(new MyUsageInfo(reference.getElement(), i, reference));
      }
    }

    return result.toArray(new UsageInfo[result.size()]);
  }

  protected boolean preprocessUsages(Ref<UsageInfo[]> refUsages) {
    prepareSuccessful();
    return true;
  }

  protected void refreshElements(PsiElement[] elements) {
    LOG.assertTrue(elements.length == myElementsToMove.length);
    for (int idx = 0; idx < elements.length; idx++) {
      myElementsToMove[idx] = elements[idx];
    }
  }

  protected void performRefactoring(UsageInfo[] usages) {
    // If files are being moved then I need to collect some information to delete these
    // filese from CVS. I need to know all common parents of the moved files and releative
    // paths.

    // Move files with correction of references.

    try {
      for (int idx = 0; idx < myElementsToMove.length; idx++) {
        PsiElement element = myElementsToMove[idx];
        final RefactoringElementListener elementListener = getTransaction().getElementListener(element);
        if (element instanceof PsiDirectory) {

          element = MoveFilesOrDirectoriesUtil.doMoveDirectory((PsiDirectory)element, myNewParent);

        }
        else if (element instanceof PsiFile) {
          element = MoveFilesOrDirectoriesUtil.doMoveFile((PsiFile)element, myNewParent);
        }

        elementListener.elementMoved(element);
        myElementsToMove[idx] = element;
      }

      for (UsageInfo usageInfo : usages) {
        final MyUsageInfo info = (MyUsageInfo)usageInfo;
        info.myReference.bindToElement(myElementsToMove[info.myIndex]);
      }

      // Perform CVS "add", "remove" commands on moved files.

      if (myMoveCallback != null) {
        myMoveCallback.refactoringCompleted();
      }

    }
    catch (IncorrectOperationException e) {
      final @NonNls String message = e.getMessage();
      final int index = (message != null) ? message.indexOf("java.io.IOException") : -1;
      if (index >= 0) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                Messages.showMessageDialog(myProject, message.substring(index + "java.io.IOException".length()),
                                           RefactoringBundle.message("error.title"),
                                           Messages.getErrorIcon());
              }
            });
      }
      else {
        LOG.error(e);
      }
    }
  }

  protected String getCommandName() {
    return RefactoringBundle.message("move.tltle"); //TODO!!
  }

  protected boolean isPreviewUsages(UsageInfo[] usages) {
    return true;
  }

  class MyUsageInfo extends UsageInfo {
    int myIndex;
    PsiReference myReference;

    public MyUsageInfo(PsiElement element, final int index, PsiReference reference) {
      super(element);
      myIndex = index;
      myReference = reference;
    }
  }
}