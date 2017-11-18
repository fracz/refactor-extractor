package com.jetbrains.python.refactoring;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.containers.HashSet;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Ivanov
 * Date: Aug 20, 2009
 * Time: 7:07:02 PM
 */
public class PyRefactoringUtil {
  @NotNull
  public static List<PsiElement> getOccurences(@NotNull final PsiElement pattern, @Nullable final PsiElement context) {
    if (context == null) {
      return Collections.emptyList();
    }
    final List<PsiElement> occurences = new ArrayList<PsiElement>();
    final PyElementVisitor visitor = new PyElementVisitor() {
      public void visitElement(@NotNull final PsiElement element) {
        if (element instanceof PyParameter) {
          return;
        }
        if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
          occurences.add(element);
          return;
        }
        element.acceptChildren(this);
      }
    };
    context.acceptChildren(visitor);
    return occurences;
  }

  @Nullable
  public static PsiElement getSelectedExpression(@NotNull final Project project,
                                                 @NotNull PsiFile file,
                                                 @NotNull final PsiElement element1,
                                                 @NotNull final PsiElement element2) {
    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent != null && !(parent instanceof PyElement)) {
      parent = PsiTreeUtil.getParentOfType(parent, PyElement.class);
    }
    if (parent == null) {
      return null;
    }
    // If it is PyIfPart for example, parent if statement, we should deny
    if (!(parent instanceof PyExpression)){
      return null;
    }
    // We cannot extract anything within import statements
    if (PsiTreeUtil.getParentOfType(parent, PyImportStatement.class, PyFromImportStatement.class) != null){
      return null;
    }
    if ((element1 == PsiTreeUtil.getDeepestFirst(parent)) && (element2 == PsiTreeUtil.getDeepestLast(parent))) {
      return parent;
    }

    // Check if selection breaks AST node in binary expression
    if (parent instanceof PyBinaryExpression) {
      final String selection = file.getText().substring(element1.getTextOffset(), element2.getTextOffset() + element2.getTextLength());
      final PyExpression expression =
        PythonLanguage.getInstance().getElementGenerator().createFromText(project, PyAssignmentStatement.class, "z=" + selection)
          .getAssignedValue();
      if (PsiUtilBase.hasErrorElementChild(expression)) {
        return null;
      }
      final String parentText = parent.getText();
      final int startOffset = element1.getTextOffset() - parent.getTextOffset() - 1;
      final int endOffset = element2.getTextOffset() + element2.getTextLength() - parent.getTextOffset();

      final String prefix = parentText.substring(0, startOffset);
      final String suffix = parentText.substring(endOffset, parentText.length());
      final TextRange textRange = TextRange.from(startOffset, endOffset - startOffset);
      final PsiElement fakeExpression =
        PythonLanguage.getInstance().getElementGenerator().createFromText(project, parent.getClass(), prefix + "python" + suffix);
      if (PsiUtilBase.hasErrorElementChild(fakeExpression)) {
        return null;
      }

      assert expression != null;
      expression.putUserData(PyPsiUtils.SELECTION_BREAKS_AST_NODE, Pair.create(parent, textRange));
      return expression;
    }
    return null;
  }

  @NotNull
  public static Collection<String> collectScopeVariables(@Nullable final PsiElement scope) {
    if (!(scope instanceof PyClass) && !(scope instanceof PyFile) && !(scope instanceof PyFunction)) {
      return Collections.emptyList();
    }
    final Set<String> variables = new HashSet<String>();
    scope.acceptChildren(new PyRecursiveElementVisitor() {
      @Override
      public void visitPyTargetExpression(@NotNull final PyTargetExpression node) {
        variables.add(node.getName());
      }

      @Override
      public void visitPyNamedParameter(@NotNull final PyNamedParameter node) {
        variables.add(node.getName());
      }

      @Override
      public void visitPyStatement(@NotNull final PyStatement node) {
        if ((node instanceof PyAssignmentStatement) || (scope instanceof PyFunction)) {
          node.acceptChildren(this);
        }
      }

      @Override
      public void visitPyFunction(@NotNull final PyFunction node) {
      }

      @Override
      public void visitPyClass(@NotNull final PyClass node) {
      }
    });
    return variables;
  }

  @Nullable
  public static PsiElement findExpressionInRange(@NotNull final PsiFile file, int startOffset, int endOffset) {
    PsiElement element1 = file.findElementAt(startOffset);
    PsiElement element2 = file.findElementAt(endOffset - 1);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.findElementAt(startOffset);
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.findElementAt(endOffset - 1);
    }
    if (element1 == null || element2 == null) {
      return null;
    }
    return getSelectedExpression(file.getProject(), file, element1, element2);
  }

  @NotNull
  public static PsiElement[] findStatementsInRange(@NotNull final PsiFile file, int startOffset, int endOffset) {
    PsiElement element1 = file.findElementAt(startOffset);
    PsiElement element2 = file.findElementAt(endOffset - 1);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.findElementAt(startOffset);
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.findElementAt(endOffset - 1);
    }
    if (element1 == null || element2 == null) {
      return PsiElement.EMPTY_ARRAY;
    }

    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent == null) {
      return PsiElement.EMPTY_ARRAY;
    }

    while (true) {
      if (parent instanceof PyStatement) {
        parent = parent.getParent();
        break;
      }
      if (parent instanceof PyStatementList) {
        break;
      }
      if (parent == null || parent instanceof PsiFile) {
        return PsiElement.EMPTY_ARRAY;
      }
      parent = parent.getParent();
    }

    if (!parent.equals(element1)) {
      while (!parent.equals(element1.getParent())) {
        element1 = element1.getParent();
      }
    }
    if (startOffset != element1.getTextRange().getStartOffset()) {
      return PsiElement.EMPTY_ARRAY;
    }

    if (!parent.equals(element2)) {
      while (!parent.equals(element2.getParent())) {
        element2 = element2.getParent();
      }
    }
    if (endOffset != element2.getTextRange().getEndOffset()) {
      return PsiElement.EMPTY_ARRAY;
    }

    if (element1 instanceof PyFunction || element1 instanceof PyClass) {
      return PsiElement.EMPTY_ARRAY;
    }
    if (element2 instanceof PyFunction || element2 instanceof PyClass) {
      return PsiElement.EMPTY_ARRAY;
    }

    PsiElement[] children = parent.getChildren();
    ArrayList<PsiElement> array = new ArrayList<PsiElement>();
    boolean flag = false;
    for (PsiElement child : children) {
      if (child.equals(element1)) {
        flag = true;
      }
      if (flag && !(child instanceof PsiWhiteSpace)) {
        array.add(child);
      }
      if (child.equals(element2)) {
        break;
      }
    }

    for (PsiElement element : array) {
      if (!(element instanceof PyStatement || element instanceof PsiWhiteSpace || element instanceof PsiComment)) {
        return PsiElement.EMPTY_ARRAY;
      }
    }
    return array.toArray(new PsiElement[array.size()]);
  }

  private PyRefactoringUtil() {
  }

  public static boolean areConflictingMethods(PyFunction pyFunction, PyFunction pyFunction1) {
    final PyParameter[] firstParams = pyFunction.getParameterList().getParameters();
    final PyParameter[] secondParams = pyFunction1.getParameterList().getParameters();
    final String firstName = pyFunction.getName();
    final String secondName = pyFunction1.getName();

    return Comparing.strEqual(firstName, secondName) && firstParams.length == secondParams.length;
  }
}