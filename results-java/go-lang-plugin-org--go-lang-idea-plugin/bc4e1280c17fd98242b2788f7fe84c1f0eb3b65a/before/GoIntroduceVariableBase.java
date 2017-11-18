/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Mihai Toader, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.refactor;

import com.goide.inspections.GoInspectionUtil;
import com.goide.psi.*;
import com.goide.psi.impl.GoElementFactory;
import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class GoIntroduceVariableBase {
  protected void performAction(final GoIntroduceOperation operation) {
    SelectionModel selectionModel = operation.getEditor().getSelectionModel();
    GoExpression expression =
      selectionModel.hasSelection() ? findExpressionInSelection(operation, selectionModel) : findExpressionAtOffset(operation);
    if (expression instanceof GoParenthesesExpr) expression = ((GoParenthesesExpr)expression).getExpression();
    if (expression == null) {
      showCannotPerform(operation, "No expression found.");
      return;
    }

    List<GoExpression> expressions = collectNestedExpressions(expression);
    if (expressions.isEmpty()) {
      int resultCount = GoInspectionUtil.getExpressionResultCount(expression);
      String text = expression.getText();
      showCannotPerform(operation,
                        "Expression " + text + "()" + (resultCount == 0 ? " doesn't return a value." : " returns multiple values."));
      return;
    }
    if (expressions.size() == 1 || ApplicationManager.getApplication().isUnitTestMode()) {
      operation.setExpression(expression);
      performOnElement(operation);
    }
    else {
      IntroduceTargetChooser.showChooser(operation.getEditor(), expressions, new Pass<GoExpression>() {
        @Override
        public void pass(GoExpression expression) {
          operation.setExpression(expression);
          performOnElement(operation);
        }
      }, new Function<GoExpression, String>() {
        @Override
        public String fun(GoExpression expression) {
          return expression.getText();
        }
      });
    }
  }

  @Nullable
  protected static GoExpression findExpressionInSelection(GoIntroduceOperation operation, SelectionModel selectionModel) {
    PsiFile file = operation.getFile();
    PsiElement element1 = file.findElementAt(selectionModel.getSelectionStart());
    PsiElement element2 = file.findElementAt(selectionModel.getSelectionEnd() - 1);
    if (element1 instanceof PsiWhiteSpace) element1 = file.findElementAt(element1.getTextRange().getEndOffset());
    if (element2 instanceof PsiWhiteSpace) element2 = file.findElementAt(element2.getTextRange().getStartOffset() - 1);
    if (element1 == null || element2 == null) return null;

    PsiElement parent = element1 == element2 ? element1 : PsiTreeUtil.findCommonParent(element1, element2);
    return parent instanceof GoExpression ? (GoExpression)parent : PsiTreeUtil.getParentOfType(parent, GoExpression.class);
  }

  @Nullable
  protected static GoExpression findExpressionAtOffset(GoIntroduceOperation operation) {
    PsiFile file = operation.getFile();
    int offset = operation.getEditor().getCaretModel().getOffset();

    PsiElement element = file.findElementAt(offset);
    GoParenthesesExpr parenthesesParent = PsiTreeUtil.getParentOfType(element, GoParenthesesExpr.class);
    if (element instanceof PsiWhiteSpace || (parenthesesParent != null && parenthesesParent.getRparen() == element)) {
      element = file.findElementAt(offset - 1);
    }
    return element instanceof GoExpression ? (GoExpression)element : PsiTreeUtil.getParentOfType(element, GoExpression.class);
  }

  private static List<GoExpression> collectNestedExpressions(GoExpression expression) {
    List<GoExpression> expressions = ContainerUtil.newArrayList();
    while (expression != null) {
      if (!(expression instanceof GoParenthesesExpr) && GoInspectionUtil.getExpressionResultCount(expression) == 1) {
        expressions.add(expression);
      }
      expression = PsiTreeUtil.getParentOfType(expression, GoExpression.class);
    }
    return expressions;
  }

  protected static void performOnElement(final GoIntroduceOperation operation) {
    final GoExpression expression = operation.getExpression();
    PsiElement statement = PsiTreeUtil.getParentOfType(expression, GoStatement.class);
    if (statement == null) {
      showCannotPerform(operation, RefactoringBundle.message("refactoring.introduce.context.error"));
      return;
    }
    LinkedHashSet<String> suggestedNames = getSuggestedNames(expression);
    operation.setSuggestedNames(suggestedNames);
    operation.setOccurrences(getOccurrences(expression, PsiTreeUtil.getParentOfType(statement, GoBlock.class)));

    final Editor editor = operation.getEditor();
    if (editor.getSettings().isVariableInplaceRenameEnabled()) {
      operation.setName(ContainerUtil.getFirstItem(suggestedNames));
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        // todo rewrite for testing different replaceAll cases
        operation.setReplaceAll(true);
        performInplaceIntroduce(operation);
        return;
      }
      OccurrencesChooser.simpleChooser(editor)
        .showChooser(expression, operation.getOccurrences(), new Pass<OccurrencesChooser.ReplaceChoice>() {
          @Override
          public void pass(OccurrencesChooser.ReplaceChoice choice) {
            operation.setReplaceAll(choice == OccurrencesChooser.ReplaceChoice.ALL);
            performInplaceIntroduce(operation);
          }
        });
    }
    //else {
    //  // todo show dialog here; set name, replaceAll
    //  performReplace(operation);
    //}
  }

  @NotNull
  public static List<PsiElement> getOccurrences(@NotNull final PsiElement pattern, @Nullable PsiElement context) {
    if (context == null) return Collections.emptyList();
    final List<PsiElement> occurrences = ContainerUtil.newArrayList();
    PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
      public void visitElement(@NotNull PsiElement element) {
        if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
          occurrences.add(element);
          return;
        }
        super.visitElement(element);
      }
    };
    context.acceptChildren(visitor);
    return occurrences;
  }

  private static void performInplaceIntroduce(GoIntroduceOperation operation) {
    performReplace(operation);
    new GoInplaceVariableIntroducer(operation).performInplaceRefactoring(operation.getSuggestedNames());
    // todo doesn't validate after rename
  }

  protected static void performReplace(final GoIntroduceOperation operation) {
    final Project project = operation.getProject();
    final PsiElement expression = operation.getExpression();
    final String name = operation.getName();
    final boolean replaceAll = operation.isReplaceAll();
    final List<PsiElement> occurrences = replaceAll ? operation.getOccurrences() : ContainerUtil.newArrayList(expression);
    final PsiElement anchor = findAnchor(occurrences);
    if (anchor == null) {
      showCannotPerform(operation, RefactoringBundle.message("refactoring.introduce.context.error"));
      return;
    }
    final List<PsiElement> newOccurrences = ContainerUtil.newArrayList();

    WriteCommandAction.runWriteCommandAction(project, new Runnable() {
      @Override
      public void run() {
        PsiElement introducePlace = anchor.getParent();
        GoStatement declarationStatement = GoElementFactory.createShortVarDeclarationStatement(project, name, (GoExpression)expression);
        PsiElement newLine = GoElementFactory.createNewLine(project);
        PsiElement statement = introducePlace.addBefore(declarationStatement, introducePlace.addBefore(newLine, anchor));
        operation.setVar(PsiTreeUtil.findChildOfType(statement, GoVarDefinition.class));

        for (PsiElement occurrence : occurrences) {
          PsiElement occurrenceParent = occurrence.getParent();
          if (occurrenceParent instanceof GoParenthesesExpr) occurrence = occurrenceParent;
          newOccurrences.add(occurrence.replace(GoElementFactory.createVarReference(project, name)));
        }
      }
    });
    operation.setOccurrences(newOccurrences);
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(operation.getEditor().getDocument());
  }

  protected static boolean isCommonAncestor(@NotNull PsiElement ancestor, @NotNull List<PsiElement> occurrences) {
    for (PsiElement element : occurrences) {
      if (!PsiTreeUtil.isAncestor(ancestor, element, false)) return false;
    }
    return true;
  }

  private static PsiElement findAnchor(@NotNull List<PsiElement> occurrences) {
    PsiElement statement = ContainerUtil.getFirstItem(occurrences);
    while (statement != null) {
      statement = PsiTreeUtil.getParentOfType(statement, GoStatement.class);
      if (statement != null && isCommonAncestor(statement.getParent(), occurrences)) return statement;
    }
    return null;
  }

  private static LinkedHashSet<String> getNamesInContext(PsiElement context) {
    if (context == null) return ContainerUtil.newLinkedHashSet();
    final LinkedHashSet<String> names = ContainerUtil.newLinkedHashSet();

    for (GoNamedElement namedElement : PsiTreeUtil.findChildrenOfType(context, GoNamedElement.class)) {
      names.add(namedElement.getName());
    }

    names.addAll(((GoFile)context.getContainingFile()).getImportMap().keySet());

    GoFunctionDeclaration functionDeclaration = PsiTreeUtil.getParentOfType(context, GoFunctionDeclaration.class);
    GoSignature signature = PsiTreeUtil.getChildOfType(functionDeclaration, GoSignature.class);
    for (GoParamDefinition param : PsiTreeUtil.findChildrenOfType(signature, GoParamDefinition.class)) {
      names.add(param.getName());
    }
    return names;
  }

  @NotNull
  private static LinkedHashSet<String> getSuggestedNames(GoExpression expression) {
    // todo rewrite with names resolve; check occurrences contexts
    LinkedHashSet<String> usedNames = getNamesInContext(PsiTreeUtil.getParentOfType(expression, GoBlock.class));
    LinkedHashSet<String> names = ContainerUtil.newLinkedHashSet();
    if (expression instanceof GoCallExpr) {
      GoReferenceExpression callReference = PsiTreeUtil.getChildOfType(expression, GoReferenceExpression.class);
      if (callReference != null) {
        String name = StringUtil.decapitalize(callReference.getIdentifier().getText());
        for (String candidate : NameUtil.getSuggestionsByName(name, "", "", false, false, false)) {
          if (!usedNames.contains(candidate)) names.add(candidate);
        }
      }
    }
    if (names.isEmpty()) {
      if (usedNames.contains("i")) {
        int counter = 1;
        while (usedNames.contains("i" + counter)) counter++;
        names.add("i" + counter);
      }
      else {
        names.add("i");
      }
    }
    return names;
  }

  protected static void showCannotPerform(GoIntroduceOperation operation, String message) {
    message = RefactoringBundle.getCannotRefactorMessage(message);
    CommonRefactoringUtil
      .showErrorHint(operation.getProject(), operation.getEditor(), message, RefactoringBundle.getCannotRefactorMessage(null),
                     "refactoring.extractVariable");
  }

  private static class GoInplaceVariableIntroducer extends InplaceVariableIntroducer<PsiElement> {
    public GoInplaceVariableIntroducer(GoIntroduceOperation operation) {
      super(operation.getVar(), operation.getEditor(), operation.getProject(), "Introduce Variable",
            ArrayUtil.toObjectArray(operation.getOccurrences(), PsiElement.class), null);
    }

    @Nullable
    @Override
    protected PsiElement checkLocalScope() {
      return myElementToRename.getContainingFile();
    }
  }
}