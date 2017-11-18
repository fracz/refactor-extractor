/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.refactoring.introduceVariable;

import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.introduceVariable.InputValidator;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrCodeBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.clauses.GrCaseBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;

/**
 * @author ilyas
 */
public abstract class GroovyIntroduceVariableBase implements RefactoringActionHandler {

  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.refactoring.introduceVariable.groovyIntroduceVariableBase");
  protected static String REFACTORING_NAME = GroovyRefactoringBundle.message("introduce.variable.title");
  private PsiElement positionElement = null;

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, @Nullable DataContext dataContext) {
    if (!editor.getSelectionModel().hasSelection()) {
      editor.getSelectionModel().selectLineAtCaret();
    }
    trimSpaces(editor, file);
    if (invoke(project, editor, file, editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd())) {
      editor.getSelectionModel().removeSelection();
    }
  }

  private boolean invoke(final Project project, final Editor editor, PsiFile file, int startOffset, int endOffset) {
    PsiDocumentManager.getInstance(project).commitAllDocuments();
    if (!(file instanceof GroovyFile)) {
      return false;
    }
    // Expression or block to be introduced as a variable
    GrExpression tempExpr = GroovyRefactoringUtil.findElementInRange(((GroovyFile) file), startOffset, endOffset, GrExpression.class);
    return invokeImpl(project, tempExpr, editor);
  }

  private boolean invokeImpl(final Project project, final GrExpression selectedExpr, final Editor editor) {

    if (selectedExpr == null) {
      String message = RefactoringBundle.getCannotRefactorMessage(GroovyRefactoringBundle.message("selected.block.should.represent.an.expression"));
      showErrorMessage(message, project);
      return false;
    }

    final PsiFile file = selectedExpr.getContainingFile();
    LOG.assertTrue(file != null, "expr.getContainingFile() == null");
    final GroovyElementFactory factory = GroovyElementFactory.getInstance(project);


    PsiType type = selectedExpr.getType();
    if (type != null) type = TypeConversionUtil.erasure(type);

    if (type == PsiType.VOID) {
      String message = RefactoringBundle.getCannotRefactorMessage(GroovyRefactoringBundle.message("selected.expression.has.void.type"));
      showErrorMessage(message, project);
      return false;
    }

    // Get container element
    final PsiElement eclosingContainer = GroovyRefactoringUtil.getEnclosingContainer(selectedExpr);
    if (eclosingContainer == null || !(eclosingContainer instanceof GroovyPsiElement)) {
      return tempContainerNotFound(project);
    }
    final GroovyPsiElement tempContainer = ((GroovyPsiElement) eclosingContainer);
    if (!isAppropriateContainer(tempContainer)) {
      String message = RefactoringBundle.getCannotRefactorMessage(GroovyRefactoringBundle.message("refactoring.is.not.supported.in.the.current.context", REFACTORING_NAME));
      showErrorMessage(message, project);
      return false;
    }
    if (!CommonRefactoringUtil.checkReadOnlyStatus(project, file)) return false;

    // Find occurences
    final PsiElement[] occurences = GroovyRefactoringUtil.getExpressionOccurences(GroovyRefactoringUtil.getUnparenthesizedExpr(selectedExpr), tempContainer);
    // Getting settings
    GroovyIntroduceVariableSettings settings = getSettings(project, editor, selectedExpr, type, occurences, false, null);

    if (!settings.isOK()) {
      return false;
    }

    final String varName = settings.getEnteredName();
    PsiType varType = settings.getSelectedType();
    final boolean isFinal = settings.isDeclareFinal();
    final boolean replaceAllOccurences = settings.isReplaceAllOccurrences();

    // Generating varibable declaration
    final GrVariableDeclaration varDecl = factory.createVariableDeclaration(varName,
        GroovyRefactoringUtil.getUnparenthesizedExpr(selectedExpr), varType, isFinal);

    runRefactoring(selectedExpr, editor, tempContainer, occurences, varName, replaceAllOccurences, varDecl);

    return true;

  }

  /**
   * Inserts new variable declaratrions and replaces occurences
   */
  void runRefactoring(final GrExpression selectedExpr,
                      final Editor editor,
                      final GroovyPsiElement tempContainer,
                      final PsiElement[] occurences,
                      final String varName,
                      final boolean replaceAllOccurences,
                      final GrVariableDeclaration varDecl) {
    final Project project = selectedExpr.getProject();
    final GroovyElementFactory factory = GroovyElementFactory.getInstance(project);
    // Marker for caret posiotion
    final Runnable runnable = new Runnable() {
      public void run() {
        try {
          /* insert new variable */
          sortOccurences(occurences);
          if (occurences.length == 0 || !(occurences[0] instanceof GrExpression)) {
            throw new IncorrectOperationException("Wrong expression occurence");
          }
          GrExpression firstOccurence;
          if (replaceAllOccurences) {
            firstOccurence = ((GrExpression) occurences[0]);
          } else {
            firstOccurence = selectedExpr;
          }
          // Replace at the place of first occurence
          boolean alreadyDefined = replaceAloneExpression(firstOccurence, selectedExpr, tempContainer, varDecl);
          if (!alreadyDefined) {
            // Insert before first occurence
            insertVariableDefinition(tempContainer, selectedExpr, occurences, replaceAllOccurences, varDecl, factory);
          }

          //Replace other occurences
          GrReferenceExpression refExpr = factory.createReferenceExpressionFromText(varName);
          if (replaceAllOccurences) {
            ArrayList<PsiElement> replaced = new ArrayList<PsiElement>();
            for (PsiElement occurence : occurences) {
              if (!(alreadyDefined && firstOccurence.equals(occurence))) {
                if (occurence instanceof GrExpression) {
                  GrExpression element = (GrExpression) occurence;
                  if (element instanceof GrClosableBlock && element.getParent() instanceof GrMethodCall) {
                    replaced.add(((GrMethodCall) element.getParent()).replaceClosureArgument(((GrClosableBlock) element), refExpr));
                  } else {
                    replaced.add(element.replaceWithExpression(refExpr));
                  }
                  // For caret position
                  if (occurence.equals(selectedExpr)) {
                    refreshPositionMarker(replaced.get(replaced.size() - 1));
                  }
                  refExpr = factory.createReferenceExpressionFromText(varName);
                } else {
                  throw new IncorrectOperationException("Expression occurence to be replaced is not instance of GroovyPsiElement");
                }
              }
            }
            if (editor != null) {
              // todo implement it...
//              final PsiElement[] replacedOccurences = replaced.toArray(new PsiElement[replaced.size()]);
//              highlightReplacedOccurences(project, editor, replacedOccurences);
            }
          } else {
            if (!alreadyDefined) {
              if (selectedExpr instanceof GrClosableBlock && selectedExpr.getParent() instanceof GrMethodCall) {
                refreshPositionMarker(((GrMethodCall) selectedExpr.getParent()).replaceClosureArgument(((GrClosableBlock) selectedExpr), refExpr));
              } else {
                refreshPositionMarker(selectedExpr.replaceWithExpression(refExpr));
              }
            }
          }
          // Setting caret to ogical position
          if (editor != null && getPositionMarker() != null) {
            editor.getCaretModel().moveToOffset(getPositionMarker().getTextRange().getEndOffset());
          }
        } catch (IncorrectOperationException e) {
          LOG.error(e);
        }
      }
    };

    CommandProcessor.getInstance().executeCommand(
        project,
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(runnable);
          }
        }, REFACTORING_NAME, null);
  }

  private void refreshPositionMarker(PsiElement position) {
    if (positionElement == null && position != null) {
      positionElement = position;
    }
  }

  private PsiElement getPositionMarker() {
    return positionElement;
  }


  /**
   * Calculates position to which new variable definition will be inserted.
   *
   * @param container
   * @param occurences
   * @param replaceAllOccurences
   * @param expr                 expression to be introduced as a variable
   * @return PsiElement, before what new definition will be inserted
   */
  @Nullable
  private PsiElement calculatePositionToInsertBefore(@NotNull PsiElement container,
                                                     PsiElement expr,
                                                     PsiElement[] occurences,
                                                     boolean replaceAllOccurences) {
    if (occurences.length == 0) return null;
    PsiElement candidate;
    if (occurences.length == 1 || !replaceAllOccurences) {
      candidate = expr;
    } else {
      sortOccurences(occurences);
      candidate = occurences[0];
    }
    while (candidate != null && !container.equals(candidate.getParent())) {
      candidate = candidate.getParent();
    }
    if (candidate == null) {
      return null;
    }
    if ((container instanceof GrWhileStatement) &&
        candidate.equals(((GrWhileStatement) container).getCondition())) {
      return container;
    }
    if ((container instanceof GrIfStatement) &&
        candidate.equals(((GrIfStatement) container).getCondition())) {
      return container;
    }
    if ((container instanceof GrForStatement) &&
        candidate.equals(((GrForStatement) container).getClause())) {
      return container;
    }
    return candidate;
  }

  /**
   * Inserts new variable definiton according the contex
   */
  private void insertVariableDefinition(GroovyPsiElement tempContainer, GrExpression selectedExpr,
                                        PsiElement[] occurences, boolean replaceAllOccurences,
                                        GrVariableDeclaration varDecl, GroovyElementFactory factory) throws IncorrectOperationException {
    PsiElement anchorElement = calculatePositionToInsertBefore(tempContainer, selectedExpr, occurences, replaceAllOccurences);
    assert anchorElement instanceof GrStatement;
    PsiElement realContainer;
    if (anchorElement.equals(tempContainer)) {
      realContainer = tempContainer.getParent();
    } else {
      realContainer = tempContainer;
    }

    assert isAppropriateContainer(realContainer);

    if (!GroovyRefactoringUtil.isLoopOrForkStatement(realContainer)) {
      if (realContainer instanceof GrCodeBlock) {
        GrCodeBlock grCodeBlock = (GrCodeBlock) realContainer;
        grCodeBlock.addStatementBefore(varDecl, (GrStatement) anchorElement);
        return;
      }
      if (realContainer instanceof GroovyFile) {
        realContainer.addBefore(varDecl, anchorElement);
        realContainer.addBefore(factory.createNewLine(), anchorElement);
        return;
      }
    } else {
      GrStatement tempStatement = ((GrStatement) anchorElement);
      // To replace branch body correctly
      boolean inThenIfBranch = realContainer instanceof GrIfStatement &&
          anchorElement.equals(((GrIfStatement) realContainer).getThenBranch());
      String refId = varDecl.getVariables()[0].getNameIdentifierGroovy().getText();
      if (tempStatement.equals(selectedExpr)) {
        tempStatement = factory.createReferenceExpressionFromText(refId);
      } else {
        replaceExpressionOccurencesInStatement(tempStatement, selectedExpr, refId, replaceAllOccurences);
      }

      GrOpenBlock newBody = factory.createOpenBlockFromStatements(varDecl, tempStatement);
      assert newBody.getStatements().length > 1;

      if (realContainer instanceof GrLoopStatement) {
        refreshPositionMarker(((GrCodeBlock) ((GrLoopStatement) realContainer).replaceBody(newBody)).getStatements()[1]);
      } else if (realContainer instanceof GrIfStatement) {
        GrIfStatement ifStatement = ((GrIfStatement) realContainer);
        if (inThenIfBranch) {
          refreshPositionMarker(((GrCodeBlock) ifStatement.replaceThenBranch(newBody)).getStatements()[1]);
        } else {
          refreshPositionMarker(((GrCodeBlock) ifStatement.replaceElseBranch(newBody)).getStatements()[1]);
        }
      }
    }
  }

  private void replaceExpressionOccurencesInStatement(GrStatement stmt, GrExpression expr, String refText, boolean replaceAllOccurences)
      throws IncorrectOperationException {
    GroovyElementFactory factory = GroovyElementFactory.getInstance(stmt.getProject());
    GrReferenceExpression refExpr = factory.createReferenceExpressionFromText(refText);
    if (!replaceAllOccurences) {
      expr.replaceWithExpression(refExpr);
    } else {
      PsiElement[] occurences = GroovyRefactoringUtil.getExpressionOccurences(expr, stmt);
      for (PsiElement occurence : occurences) {
        if (occurence instanceof GrExpression) {
          GrExpression grExpression = (GrExpression) occurence;
          grExpression.replaceWithExpression(refExpr);
          refExpr = factory.createReferenceExpressionFromText(refText);
        } else {
          throw new IncorrectOperationException();
        }
      }
    }
  }

  private void sortOccurences(PsiElement[] occurences) {
    Arrays.sort(occurences, new Comparator<PsiElement>() {
      public int compare(PsiElement elem1, PsiElement elem2) {
        if (elem1.getTextOffset() < elem2.getTextOffset()) {
          return -1;
        } else if (elem1.getTextOffset() > elem2.getTextOffset()) {
          return 1;
        } else {
          return 0;
        }
      }
    });
  }

  /**
   * Replaces an expression occurence by appropriate variable declaration
   */
  private boolean replaceAloneExpression(@NotNull GrExpression expr,
                                         GrExpression selectedExpr,
                                         @NotNull PsiElement context,
                                         @NotNull GrVariableDeclaration definition) throws IncorrectOperationException {
    if (expr instanceof GrMethodCall ||
        expr instanceof GrApplicationExpression) {
      return false;
    }
    if (context.equals(expr.getParent()) &&
        !GroovyRefactoringUtil.isResultExpression(expr) &&
        !GroovyRefactoringUtil.isLoopOrForkStatement(context)) {
      if (expr.equals(selectedExpr)) {
        refreshPositionMarker(expr.replaceWithStatement(definition));
      } else {
        expr.replaceWithStatement(definition);
      }
      return true;
    }
    return false;
  }

  private boolean tempContainerNotFound(final Project project) {
    String message = GroovyRefactoringBundle.message("refactoring.is.not.supported.in.the.current.context", REFACTORING_NAME);
    showErrorMessage(message, project);
    return false;
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, @Nullable DataContext dataContext) {
    // Does nothing
  }

  protected abstract GroovyIntroduceVariableSettings getSettings(final Project project, Editor editor, PsiElement expr,
                                                                 PsiType type, PsiElement[] occurrences, boolean decalreFinal,
                                                                 InputValidator validator);

  protected abstract void showErrorMessage(String message, Project project);

  protected abstract void highlightReplacedOccurences(final Project project, Editor editor, final PsiElement[] replacedOccurences);

  private static boolean isAppropriateContainer(PsiElement tempContainer) {
    return tempContainer instanceof GrOpenBlock ||
        tempContainer instanceof GrClosableBlock ||
        tempContainer instanceof GroovyFile ||
        tempContainer instanceof GrCaseBlock ||
        GroovyRefactoringUtil.isLoopOrForkStatement(tempContainer);
  }

  private static void trimSpaces(Editor editor, PsiFile file) {
    int start = editor.getSelectionModel().getSelectionStart();
    int end = editor.getSelectionModel().getSelectionEnd();
    while (file.findElementAt(start) instanceof PsiWhiteSpace ||
        file.findElementAt(start) instanceof PsiComment ||
        (file.findElementAt(start) != null &&
            GroovyTokenTypes.mNLS.equals(file.findElementAt(start).getNode().getElementType()))) {
      start++;
    }
    while (file.findElementAt(end - 1) instanceof PsiWhiteSpace ||
        file.findElementAt(end - 1) instanceof PsiComment ||
        (file.findElementAt(end - 1) != null &&
            (GroovyTokenTypes.mNLS.equals(file.findElementAt(end - 1).getNode().getElementType()) ||
                GroovyTokenTypes.mSEMI.equals(file.findElementAt(end - 1).getNode().getElementType())))) {
      end--;
    }

    editor.getSelectionModel().setSelection(start, end);

  }
}