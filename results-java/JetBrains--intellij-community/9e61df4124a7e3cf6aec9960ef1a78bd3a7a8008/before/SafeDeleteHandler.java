package com.intellij.refactoring.safeDelete;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.*;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * @author dsl
 */
public class SafeDeleteHandler implements RefactoringActionHandler {
  public static final String REFACTORING_NAME = RefactoringBundle.message("safe.delete.title");

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);
    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
    if (element == null || !SafeDeleteProcessor.validElement(element)) {
      String message = RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("is.not.supported.in.the.current.context", REFACTORING_NAME));
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, "refactoring.safeDelete", project);
      return;
    }
    invoke(project, new PsiElement[]{element}, dataContext);
  }

  public void invoke(@NotNull final Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
    invoke(project, elements, true);
  }

  public static void invoke(final Project project, PsiElement[] elements, boolean checkSuperMethods) {
    invoke(project, elements, checkSuperMethods, null);
  }

  public static void invoke(final Project project, PsiElement[] elements, boolean checkSuperMethods, final @Nullable Runnable successRunnable) {
    for (PsiElement element : elements) {
      if (!SafeDeleteProcessor.validElement(element)) {
        return;
      }
    }
    final PsiElement[] temptoDelete = PsiTreeUtil.filterAncestors(elements);
    Set<PsiElement> elementsSet = new HashSet<PsiElement>(Arrays.asList(temptoDelete));
    Set<PsiElement> fullElementsSet = new HashSet<PsiElement>();

    if (checkSuperMethods) {
      for (PsiElement element : temptoDelete) {
        boolean found = false;
        for(SafeDeleteProcessorDelegate delegate: Extensions.getExtensions(SafeDeleteProcessorDelegate.EP_NAME)) {
          if (delegate.handlesElement(element)) {
            found = true;
            Collection<? extends PsiElement> addElements = delegate.getElementsToSearch(element, elementsSet);
            if (addElements == null) return;
            fullElementsSet.addAll(addElements);
            break;
          }
        }
        if (!found) {
          fullElementsSet.add(element);
        }
      }
    } else {
      fullElementsSet.addAll(Arrays.asList(temptoDelete));
    }

    if (!CommonRefactoringUtil.checkReadOnlyStatusRecursively(project, fullElementsSet)) return;

    final PsiElement[] elementsToDelete = fullElementsSet.toArray(new PsiElement[fullElementsSet.size()]);

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      RefactoringSettings settings = RefactoringSettings.getInstance();
      SafeDeleteProcessor.createInstance(project, null, elementsToDelete, settings.SAFE_DELETE_SEARCH_IN_COMMENTS,
                                         settings.SAFE_DELETE_SEARCH_IN_NON_JAVA, true).run();
    }
    else {
      final SafeDeleteDialog.Callback callback = new SafeDeleteDialog.Callback() {
        public void run(final SafeDeleteDialog dialog) {
          SafeDeleteProcessor.createInstance(project, new Runnable() {
            public void run() {
              if (successRunnable != null) {
                successRunnable.run();
              }
              dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
            }
          }, elementsToDelete, dialog.isSearchInComments(), dialog.isSearchForTextOccurences(), true).run();
        }

      };

      SafeDeleteDialog dialog = new SafeDeleteDialog(project, elementsToDelete, callback);
      dialog.show();
    }
  }
}