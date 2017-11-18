package com.jetbrains.python.refactoring.introduce;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Function;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.refactoring.NameSuggestorUtil;
import com.jetbrains.python.refactoring.PyRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Alexey.Ivanov
 */
abstract public class IntroduceHandler implements RefactoringActionHandler {
  protected static PsiElement findAnchor(List<PsiElement> occurrences) {
    PsiElement anchor = occurrences.get(0);
    next:
    do {
      PyStatement statement = PsiTreeUtil.getParentOfType(anchor, PyStatement.class);

      final PsiElement parent = statement.getParent();
      for (PsiElement element : occurrences) {
        if (!PsiTreeUtil.isAncestor(parent, element, true)) {
          anchor = statement;
          continue next;
        }
      }

      return statement;
    }
    while (true);
  }

  public enum InitPlace {
    SAME_METHOD,
    CONSTRUCTOR,
    SET_UP
  }

  private static void replaceExpression(PyExpression newExpression, Project project, PsiElement expression) {
    PyExpressionStatement statement = PsiTreeUtil.getParentOfType(expression, PyExpressionStatement.class);
    if (statement != null) {
      if (statement.getExpression() == expression) {
        statement.delete();
        return;
      }
    }
    PyPsiUtils.replaceExpression(project, expression, newExpression);
  }

  private final IntroduceValidator myValidator;
  private final String myDialogTitle;

  protected IntroduceHandler(@NotNull final IntroduceValidator validator, @NotNull final String dialogTitle) {
    myValidator = validator;
    myDialogTitle = dialogTitle;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    performAction(project, editor, file, null, false, false, false);
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  public Collection<String> getSuggestedNames(@NotNull final PyExpression expression) {
    Collection<String> candidates = new HashSet<String>();
    String text = expression.getText();
    if (text != null) {
      candidates.addAll(NameSuggestorUtil.generateNames(text));
    }
    PyType type = expression.getType(TypeEvalContext.slow());
    if (type != null) {
      final String typeName = type.getName();
      if (typeName != null) {
        candidates.addAll(NameSuggestorUtil.generateNamesByType(typeName));
      }
    }
    final PyKeywordArgument kwArg = PsiTreeUtil.getParentOfType(expression, PyKeywordArgument.class);
    if (kwArg != null && kwArg.getValueExpression() == expression) {
      candidates.add(kwArg.getKeyword());
    }

    final PyArgumentList argList = PsiTreeUtil.getParentOfType(expression, PyArgumentList.class);
    if (argList != null) {
      final PyArgumentList.AnalysisResult result = argList.analyzeCall();
      if (result.getMarkedCallee() != null && !result.isImplicitlyResolved()) {
        final PyNamedParameter namedParameter = result.getPlainMappedParams().get(expression);
        if (namedParameter != null) {
          candidates.add(namedParameter.getName());
        }
      }
    }

    Collection<String> res = new HashSet<String>();
    for (String name : candidates) {
      if (myValidator.checkPossibleName(name, expression)) {
        res.add(name);
      }
    }
    return res;
  }

  public void performAction(@NotNull final Project project, Editor editor, PsiFile file, String name, boolean replaceAll, boolean hasConstructor, boolean isTestClass) {
    if (!CommonRefactoringUtil.checkReadOnlyStatus(file)) {
      return;
    }

    PsiElement element1 = null;
    PsiElement element2 = null;
    final SelectionModel selectionModel = editor.getSelectionModel();
    if (selectionModel.hasSelection()) {
      element1 = file.findElementAt(selectionModel.getSelectionStart());
      element2 = file.findElementAt(selectionModel.getSelectionEnd() - 1);
      if (element1 instanceof PsiWhiteSpace) {
        int startOffset = element1.getTextRange().getEndOffset();
        element1 = file.findElementAt(startOffset);
      }
      if (element2 instanceof PsiWhiteSpace) {
        int endOffset = element2.getTextRange().getStartOffset();
        element2 = file.findElementAt(endOffset - 1);
      }
    }
    else {
      final CaretModel caretModel = editor.getCaretModel();
      final Document document = editor.getDocument();
      int lineNumber = document.getLineNumber(caretModel.getOffset());
      if ((lineNumber >= 0) && (lineNumber < document.getLineCount())) {
        element1 = file.findElementAt(document.getLineStartOffset(lineNumber));
        element2 = file.findElementAt(document.getLineEndOffset(lineNumber) - 1);
      }
      if (element1 == null || element2 == null || PyRefactoringUtil.getSelectedExpression(project, file, element1, element2) == null) {
        if (smartIntroduce(file, editor, name, replaceAll, hasConstructor, isTestClass)) {
          return;
        }
      }
    }
    if (element1 == null || element2 == null) {
      CommonRefactoringUtil.showErrorHint(project, editor, PyBundle.message("refactoring.introduce.selection.error"), myDialogTitle,
                                          "refactoring.extractMethod");
      return;
    }

    element1 = PyRefactoringUtil.getSelectedExpression(project, file, element1, element2);
    if (element1 == null) {
      CommonRefactoringUtil.showErrorHint(project, editor, PyBundle.message("refactoring.introduce.selection.error"), myDialogTitle,
                                          "refactoring.extractMethod");
    }
    if (!checkIntroduceContext(file, editor, element1)) {
      return;
    }
    performActionOnElement(editor, element1, name, replaceAll, hasConstructor, isTestClass);
  }

  private boolean smartIntroduce(final PsiFile file, final Editor editor, final String name, final boolean replaceAll, final boolean hasConstructor, final boolean isTestClass) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement elementAtCaret = file.findElementAt(offset);
    if (!checkIntroduceContext(file, editor, elementAtCaret)) return true;
    final List<PyExpression> expressions = new ArrayList<PyExpression>();
    while (elementAtCaret != null) {
      if (elementAtCaret instanceof PyStatement) {
        break;
      }
      if (elementAtCaret instanceof PyExpression && isValidIntroduceVariant(elementAtCaret)) {
        expressions.add((PyExpression)elementAtCaret);
      }
      elementAtCaret = elementAtCaret.getParent();
    }
    if (expressions.size() == 1) {
      performActionOnElement(editor, expressions.get(0), name, replaceAll, hasConstructor, isTestClass);
      return true;
    }
    else if (expressions.size() > 1) {
      IntroduceTargetChooser.showChooser(editor, expressions, new Pass<PyExpression>() {
        @Override
        public void pass(PyExpression pyExpression) {
          performActionOnElement(editor, pyExpression, name, replaceAll, hasConstructor, isTestClass);
        }
      }, new Function<PyExpression, String>() {
        public String fun(PyExpression pyExpression) {
          return pyExpression.getText();
        }
      });
      return true;
    }
    return false;
  }

  protected boolean checkIntroduceContext(PsiFile file, Editor editor, PsiElement element) {
    if (!isValidIntroduceContext(element)) {
      CommonRefactoringUtil.showErrorHint(file.getProject(), editor, PyBundle.message("refactoring.introduce.selection.error"),
                                          myDialogTitle, "refactoring.extractMethod");
      return false;
    }
    return true;
  }

  protected boolean isValidIntroduceContext(PsiElement element) {
    PyDecorator decorator = PsiTreeUtil.getParentOfType(element, PyDecorator.class);
    if (decorator != null && PsiTreeUtil.isAncestor(decorator.getCallee(), element, false)) {
      return false;
    }
    return PsiTreeUtil.getParentOfType(element, PyParameterList.class) == null;
  }

  private static boolean isValidIntroduceVariant(PsiElement element) {
    final PyCallExpression call = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
    if (call != null && PsiTreeUtil.isAncestor(call.getCallee(), element, false)) {
      return false;
    }
    return true;
  }

  private void performActionOnElement(Editor editor,
                                      PsiElement element,
                                      String name,
                                      boolean replaceAll,
                                      boolean hasConstructor,
                                      boolean isTestClass) {
    final Project project = element.getProject();
    if (!checkEnabled(project, editor, element, myDialogTitle)) {
      return;
    }

    final PsiElement parent = element.getParent();
    final PyExpression expression = parent instanceof PyAssignmentStatement ?
                                    ((PyAssignmentStatement)parent).getAssignedValue() :
                                    (PyExpression)element;

    final List<PsiElement> occurrences;
    if (expression.getUserData(PyPsiUtils.SELECTION_BREAKS_AST_NODE) == null && !(expression instanceof PyCallExpression)) {
      occurrences = getOccurrences(expression);
    }
    else {
      occurrences = Collections.emptyList();
    }
    Collection<String> possibleNames = getSuggestedNames(expression);
    replaceAll &= occurrences.size() > 0;
    InitPlace initInConstructor = InitPlace.SAME_METHOD;
    if (name == null) {
      PyIntroduceDialog dialog = new PyIntroduceDialog(project, expression, myDialogTitle, myValidator, occurrences.size(), possibleNames, getHelpId(), hasConstructor, isTestClass);
      dialog.show();
      if (!dialog.isOK()) {
        return;
      }
      name = dialog.getName();
      replaceAll = dialog.doReplaceAllOccurrences();
      initInConstructor = dialog.getInitPlace();
    }
    String assignmentText = name + " = " + expression.getText();
    PsiElement anchor = replaceAll ? findAnchor(occurrences) : PsiTreeUtil.getParentOfType(expression, PyStatement.class);
    PyAssignmentStatement declaration = createDeclaration(project, assignmentText, anchor);

    assert name != null;
    declaration = performReplace(project, declaration, expression, occurrences, name, replaceAll, initInConstructor);
    declaration = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(declaration);
    editor.getCaretModel().moveToOffset(declaration.getTextRange().getEndOffset());
    editor.getSelectionModel().removeSelection();
  }

  protected abstract String getHelpId();

  protected PyAssignmentStatement createDeclaration(Project project, String assignmentText, PsiElement anchor) {
    return PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyAssignmentStatement.class, assignmentText);
  }

  protected boolean checkEnabled(Project project, Editor editor, PsiElement element1, String dialogTitle) {
    return true;
  }

  private static List<PsiElement> getOccurrences(@NotNull final PyExpression expression) {
    PsiElement context = PsiTreeUtil.getParentOfType(expression, PyFunction.class);
    if (context == null) {
      context = PsiTreeUtil.getParentOfType(expression, PyClass.class);
    }
    if (context == null) {
      context = expression.getContainingFile();
    }
    return PyRefactoringUtil.getOccurences(expression, context);
  }

  private PyAssignmentStatement performReplace(@NotNull final Project project,
                                               @NotNull final PyAssignmentStatement declaration,
                                               @NotNull final PsiElement expression,
                                               @NotNull final List<PsiElement> occurrences,
                                               @NotNull final String name,
                                               final boolean replaceAll,
                                               final InitPlace initInConstructor) {
    return new WriteCommandAction<PyAssignmentStatement>(project, expression.getContainingFile()) {
      protected void run(final Result<PyAssignmentStatement> result) throws Throwable {
        final Pair<PsiElement, TextRange> data = expression.getUserData(PyPsiUtils.SELECTION_BREAKS_AST_NODE);
        if (data == null) {
          result.setResult((PyAssignmentStatement)addDeclaration(expression, declaration, occurrences, replaceAll, initInConstructor));
        }
        else {
          result.setResult((PyAssignmentStatement)addDeclaration(data.first, declaration, occurrences, replaceAll, initInConstructor));
        }

        PyExpression newExpression = createExpression(project, name, declaration);

        if (replaceAll) {
          for (PsiElement occurrence : occurrences) {
            replaceExpression(newExpression, project, occurrence);
          }
        }
        else {
          replaceExpression(newExpression, project, expression);
        }
      }
    }.execute().getResultObject();
  }

  protected PyExpression createExpression(Project project, String name, PyAssignmentStatement declaration) {
    return PyElementGenerator.getInstance(project).createExpressionFromText(name);
  }

  @Nullable
  protected abstract PsiElement addDeclaration(@NotNull final PsiElement expression,
                                               @NotNull final PsiElement declaration,
                                               @NotNull final List<PsiElement> occurrences,
                                               final boolean replaceAll,
                                               final InitPlace initInConstructor);
}