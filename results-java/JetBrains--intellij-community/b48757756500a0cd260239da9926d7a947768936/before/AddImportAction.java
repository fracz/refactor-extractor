/*
 * User: anna
 * Date: 07-Mar-2008
 */
package com.jetbrains.python.actions;

import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import static com.jetbrains.python.PyNames.DOT_PY;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.ResolveImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddImportAction implements HintAction, QuestionAction, LocalQuickFix {
  private final PsiReference myReference;
  private Project myProject;
  private static final Logger LOG = Logger.getInstance("#" + AddImportAction.class.getName());

  public AddImportAction(final PsiReference reference) {
    myReference = reference;
    myProject = reference.getElement().getProject();
  }

  @NotNull
  public String getText() {
    return PyBundle.message("ACT.NAME.add.import");
  }

  @NotNull
  public String getName() {
    return getText();
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("ACT.FAMILY.import");
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    execute(descriptor.getPsiElement().getContainingFile());
  }

  @Nullable
  protected String getRefName() {
    return ((PyReferenceExpression)myReference).getReferencedName();
  }

  protected PsiFile[] getRefFiles(final String referenceName) {
    PsiFile[] files = FilenameIndex.getFilesByName(myProject, referenceName + DOT_PY, GlobalSearchScope.allScope(myProject));
    if (files == null) files = PsiFile.EMPTY_ARRAY;
    return files;
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    final PsiElement element = myReference.getElement();
    // not within import
    if (PsiTreeUtil.getParentOfType(element, PyImportStatement.class) != null) return false;
    if (PsiTreeUtil.getParentOfType(element, PyFromImportStatement.class) != null) return false;
    // don't propose to import unknown fields, etc qualified things
    if (myReference instanceof PyReferenceExpression) {
      final PyExpression qual = ((PyReferenceExpression)myReference).getQualifier();
      if (qual != null) return false;
    }
    // don't propose to import unimportable
    if (
      !(myReference instanceof PyReferenceExpression)  ||
      (ResolveImportUtil.resolvePythonImport2((PyReferenceExpression)myReference, null) == null)
    ) return false;
    //
    final String referenceName = getRefName();
    final PsiFile[] files = getRefFiles(referenceName);
    return files != null && files.length > 0;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    execute(file);
  }

  private void execute(final PsiFile file) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final String referenceName = getRefName();
        final PsiFile[] files = getRefFiles(referenceName);
        if (files.length == 1) {
          final PyImportStatement importNodeToInsert = PythonLanguage.getInstance().getElementGenerator().createImportStatementFromText(
              myProject, "import " + referenceName + "\n\n"
          );
          try {
            file.addBefore(importNodeToInsert, getInsertPosition(file));
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
      }
    });
  }

  private static PsiElement getInsertPosition(final PsiFile file) {
    PsiElement feeler = file.getFirstChild();
    LOG.assertTrue(feeler != null);
    // skip initial comments and whitespace and try to get just below the last import stmt
    PsiElement seeker = feeler;
    do {
      if (PyUtil.instanceOf(feeler, PyImportStatement.class, PyFromImportStatement.class)) {
        seeker = feeler;
        feeler = feeler.getNextSibling();
      }
      else if (PyUtil.instanceOf(feeler, PsiWhiteSpace.class, PsiComment.class)) {
        seeker = feeler;
        feeler = feeler.getNextSibling();
      }
      else break; // some other statement, stop
    } while (feeler != null);
    return seeker;
  }

  public boolean startInWriteAction() {
    return false;
  }

  public boolean showHint(final Editor editor) {
    final String referenceName = getRefName();
    final PsiFile[] files = getRefFiles(referenceName);
    if (!(files != null && files.length > 0)) return false;
    String hintText = ShowAutoImportPass.getMessage(false, getRefName());
    HintManager.getInstance().showQuestionHint(editor, hintText, myReference.getElement().getTextOffset(),
                                               myReference.getElement().getTextRange().getEndOffset(), this);
    return true;
  }

  public boolean execute() {
    execute(myReference.getElement().getContainingFile());
    return true;
  }

}