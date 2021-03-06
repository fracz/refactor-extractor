package com.jetbrains.python.refactoring.introduce.field;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Function;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.actions.AddFieldQuickFix;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFunctionBuilder;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.refactoring.introduce.IntroduceHandler;
import com.jetbrains.python.refactoring.introduce.IntroduceOperation;
import com.jetbrains.python.refactoring.introduce.variable.PyIntroduceVariableHandler;
import com.jetbrains.python.testing.PythonUnitTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class PyIntroduceFieldHandler extends IntroduceHandler {

  public PyIntroduceFieldHandler() {
    super(new IntroduceFieldValidator(), RefactoringBundle.message("introduce.field.title"));
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    performAction(new IntroduceOperation(project, editor, file, null, true, isTestClass(file, editor)));
  }

  private static boolean isTestClass(PsiFile file, Editor editor) {
    PsiElement element1 = null;
    final SelectionModel selectionModel = editor.getSelectionModel();
    if (selectionModel.hasSelection()) {
      element1 = file.findElementAt(selectionModel.getSelectionStart());
    }
    else {
      final CaretModel caretModel = editor.getCaretModel();
      final Document document = editor.getDocument();
      int lineNumber = document.getLineNumber(caretModel.getOffset());
      if ((lineNumber >= 0) && (lineNumber < document.getLineCount())) {
        element1 = file.findElementAt(document.getLineStartOffset(lineNumber));
      }
    }
    if (element1 != null) {
      final PyClass clazz = PyUtil.getContainingClassOrSelf(element1);
      if (clazz != null && PythonUnitTestUtil.isTestCaseClass(clazz)) return true;
    }
    return false;
  }

  @Override
  protected PsiElement replaceExpression(PsiElement expression, PyExpression newExpression, IntroduceOperation operation) {
    if (operation.getInitPlace() != InitPlace.SAME_METHOD) {
      return PyPsiUtils.replaceExpression(expression, newExpression);
    }
    return super.replaceExpression(expression, newExpression, operation);
  }

  @Override
  protected boolean checkEnabled(Project project, Editor editor, PsiElement element1, String dialogTitle) {
    if (PyUtil.getContainingClassOrSelf(element1) == null) {
      CommonRefactoringUtil.showErrorHint(project, editor, "Cannot introduce field: not in class", dialogTitle,
                                          "refactoring.extractMethod");
      return false;
    }
    return true;
  }

  @Nullable
  @Override
  protected PsiElement addDeclaration(@NotNull PsiElement expression, @NotNull PsiElement declaration, @NotNull IntroduceOperation operation) {
    final PsiElement expr = expression instanceof PyClass ? expression : expression.getParent();
    PsiElement anchor = PyUtil.getContainingClassOrSelf(expr);
    assert anchor instanceof PyClass;
    final PyClass clazz = (PyClass)anchor;
    final Project project = anchor.getProject();
    if (operation.getInitPlace() == InitPlace.CONSTRUCTOR && !inConstructor(expression)) {
      return AddFieldQuickFix.addFieldToInit(project, clazz, "", new AddFieldDeclaration(declaration));
    } else if (operation.getInitPlace() == InitPlace.SET_UP) {
      return addFieldToSetUp(clazz, new AddFieldDeclaration(declaration));
    }
    return PyIntroduceVariableHandler.doIntroduceVariable(expression, declaration, operation.getOccurrences(), operation.isReplaceAll());
  }

  private static boolean inConstructor(@NotNull PsiElement expression) {
    final PsiElement expr = expression instanceof PyClass ? expression : expression.getParent();
    PyClass clazz = PyUtil.getContainingClassOrSelf(expr);
    PsiElement current = PyUtil.getConcealingParent(expression);
    if (clazz != null && current != null && current instanceof PyFunction) {
      PyFunction init = clazz.findMethodByName(PyNames.INIT, false);
      if (current == init) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  private static PsiElement addFieldToSetUp(PyClass clazz, final Function<String, PyStatement> callback) {
    final PyFunction init = clazz.findMethodByName(PythonUnitTestUtil.TESTCASE_SETUP_NAME, false);
    if (init != null) {
      return AddFieldQuickFix.appendToMethod(init, callback);
    }
    final PyFunctionBuilder builder = new PyFunctionBuilder(PythonUnitTestUtil.TESTCASE_SETUP_NAME);
    builder.parameter(PyNames.CANONICAL_SELF);
    PyFunction setUp = builder.buildFunction(clazz.getProject(), LanguageLevel.getDefault());
    final PyStatementList statements = clazz.getStatementList();
    final PsiElement anchor = statements.getFirstChild();
    setUp = (PyFunction)statements.addBefore(setUp, anchor);
    return AddFieldQuickFix.appendToMethod(setUp, callback);
  }

  @Override
  protected List<PsiElement> getOccurrences(PsiElement element, @NotNull PyExpression expression) {
    if (isAssignedLocalVariable(element)) {
      PyFunction function = PsiTreeUtil.getParentOfType(element, PyFunction.class);
      Collection<PsiReference> references = ReferencesSearch.search(element, new LocalSearchScope(function)).findAll();
      ArrayList<PsiElement> result = new ArrayList<PsiElement>();
      for (PsiReference reference : references) {
        PsiElement refElement = reference.getElement();
        if (refElement != element) {
          result.add(refElement);
        }
      }
      return result;
    }
    return super.getOccurrences(element, expression);
  }

  @Override
  protected PyExpression createExpression(Project project, String name, PyAssignmentStatement declaration) {
    final String text = declaration.getText();
    final String self_name = text.substring(0, text.indexOf('.'));
    return PyElementGenerator.getInstance(project).createExpressionFromText(self_name + "." + name);
  }

  @Override
  protected PyAssignmentStatement createDeclaration(Project project, String assignmentText, PsiElement anchor) {
    final PyFunction container = PsiTreeUtil.getParentOfType(anchor, PyFunction.class);
    String selfName = PyUtil.getFirstParameterName(container);
    final LanguageLevel langLevel = LanguageLevel.forElement(anchor);
    return PyElementGenerator.getInstance(project).createFromText(langLevel, PyAssignmentStatement.class, selfName + "." + assignmentText);
  }

  @Override
  protected void postRefactoring(PsiElement element) {
    if (isAssignedLocalVariable(element)) {
      element.getParent().delete();
    }
  }

  private static boolean isAssignedLocalVariable(PsiElement element) {
    if (element instanceof PyTargetExpression && element.getParent() instanceof PyAssignmentStatement &&
        PsiTreeUtil.getParentOfType(element, PyFunction.class) != null) {
      PyAssignmentStatement stmt = (PyAssignmentStatement) element.getParent();
      if (stmt.getTargets().length == 1) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected String getHelpId() {
    return "python.reference.introduceField";
  }

  @Override
  protected boolean checkIntroduceContext(PsiFile file, Editor editor, PsiElement element) {
    if (element != null && isInStaticMethod(element)) {
      CommonRefactoringUtil.showErrorHint(file.getProject(), editor, "Introduce Field refactoring cannot be used in static methods",
                                          RefactoringBundle.message("introduce.field.title"),
                                          "refactoring.extractMethod");
      return false;
    }
    return super.checkIntroduceContext(file, editor, element);
  }

  private static boolean isInStaticMethod(PsiElement element) {
    PyFunction containingMethod = PsiTreeUtil.getParentOfType(element, PyFunction.class, false, PyClass.class);
    if (containingMethod != null) {
      final Set<PyFunction.Flag> flags = PyUtil.detectDecorationsAndWrappersOf(containingMethod);
      return flags.contains(PyFunction.Flag.STATICMETHOD);
    }
    return false;
  }

  @Override
  protected boolean isValidIntroduceContext(PsiElement element) {
    return super.isValidIntroduceContext(element) &&
           PsiTreeUtil.getParentOfType(element, PyFunction.class, false, PyClass.class) != null &&
           PsiTreeUtil.getParentOfType(element, PyDecoratorList.class) == null &&
           !isInStaticMethod(element);
  }

  private static class AddFieldDeclaration implements Function<String, PyStatement> {
    private final PsiElement myDeclaration;

    private AddFieldDeclaration(PsiElement declaration) {
      myDeclaration = declaration;
    }

    public PyStatement fun(String self_name) {
      if (PyNames.CANONICAL_SELF.equals(self_name)) {
        return (PyStatement)myDeclaration;
      }
      final String text = myDeclaration.getText();
      final Project project = myDeclaration.getProject();
      return PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyStatement.class,
                                                                    text.replaceFirst(PyNames.CANONICAL_SELF + "\\.", self_name + "."));
    }
  }

  @Override
  protected void performInplaceIntroduce(IntroduceOperation operation) {
    final PyAssignmentStatement statement = performRefactoring(operation);
    // put caret on identifier after "self."
    final List<PsiElement> occurrences = operation.getOccurrences();
    final PsiElement occurrence = findOccurrenceUnderCaret(occurrences, operation.getEditor());
    PyTargetExpression target = (PyTargetExpression) statement.getTargets() [0];
    putCaretOnFieldName(operation.getEditor(), occurrence != null ? occurrence : target);
    final InplaceVariableIntroducer<PsiElement> introducer = new PyInplaceFieldIntroducer(target, operation, occurrences);
    introducer.performInplaceRename(false, new LinkedHashSet<String>(operation.getSuggestedNames()));
  }

  @Nullable
  private static PsiElement findOccurrenceUnderCaret(List<PsiElement> occurrences, Editor editor) {
    if (occurrences.isEmpty()) {
      return null;
    }
    int offset = editor.getCaretModel().getOffset();
    for (PsiElement occurrence : occurrences) {
      if (occurrence.getTextRange().contains(offset)) {
        return occurrence;
      }
    }
    int line = editor.getDocument().getLineNumber(offset);
    for (PsiElement occurrence : occurrences) {
      if (editor.getDocument().getLineNumber(occurrence.getTextRange().getStartOffset()) == line) {
        return occurrence;
      }
    }
    return occurrences.get(0);
  }

  private static void putCaretOnFieldName(Editor editor, PsiElement occurrence) {
    PyQualifiedExpression qExpr = PsiTreeUtil.getParentOfType(occurrence, PyQualifiedExpression.class, false);
    if (qExpr != null && qExpr.getQualifier() == null) {
      qExpr = PsiTreeUtil.getParentOfType(qExpr, PyQualifiedExpression.class);
    }
    if (qExpr != null) {
      final ASTNode nameElement = qExpr.getNameElement();
      if (nameElement != null) {
        final int offset = nameElement.getTextRange().getStartOffset();
        editor.getCaretModel().moveToOffset(offset);
      }
    }
  }

  private static class PyInplaceFieldIntroducer extends InplaceVariableIntroducer<PsiElement> {
    private final PyTargetExpression myTarget;
    private final PyIntroduceFieldPanel myPanel;

    public PyInplaceFieldIntroducer(PyTargetExpression target,
                                    IntroduceOperation operation,
                                    List<PsiElement> occurrences) {
      super(target, operation.getEditor(), operation.getProject(), "Introduce Field",
            occurrences.toArray(new PsiElement[occurrences.size()]), null);
      myTarget = target;
      if (!inConstructor(target)) {
        myPanel = new PyIntroduceFieldPanel(myProject, operation.isTestClass());
      }
      else {
        myPanel = null;
      }
    }

    @Override
    protected PsiElement checkLocalScope() {
      return myTarget.getContainingFile();
    }

    @Override
    protected JComponent getComponent() {
      return myPanel == null ? null : myPanel.getRootPanel();
    }

    @Override
    protected void moveOffsetAfter(boolean success) {
      if (success && myPanel != null && myPanel.getInitPlace() != InitPlace.SAME_METHOD) {
        final AccessToken accessToken = ApplicationManager.getApplication().acquireWriteActionLock(getClass());
        try {
          final PyAssignmentStatement initializer = PsiTreeUtil.getParentOfType(myTarget, PyAssignmentStatement.class);
          assert initializer != null;
          final Function<String, PyStatement> callback = new Function<String, PyStatement>() {
            @Override
            public PyStatement fun(String s) {
              return initializer;
            }
          };
          final PyClass pyClass = PyUtil.getContainingClassOrSelf(initializer);
          if (myPanel.getInitPlace() == InitPlace.CONSTRUCTOR) {
            AddFieldQuickFix.addFieldToInit(myProject, pyClass, "", callback);
          }
          else if (myPanel.getInitPlace() == InitPlace.SET_UP) {
            addFieldToSetUp(pyClass, callback);
          }
          initializer.delete();
        }
        finally {
          accessToken.finish();
        }
      }
    }
  }
}