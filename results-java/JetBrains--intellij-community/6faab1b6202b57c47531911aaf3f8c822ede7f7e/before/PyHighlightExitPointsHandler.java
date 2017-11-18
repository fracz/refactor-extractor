package com.jetbrains.python.codeInsight.highlighting;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.controlflow.ControlFlow;
import com.intellij.codeInsight.controlflow.Instruction;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author oleg
 */
public class PyHighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {
  private final PsiElement myTarget;

  public PyHighlightExitPointsHandler(final Editor editor, final PsiFile file, final PsiElement target) {
    super(editor, file);
    myTarget = target;
  }

  public List<PsiElement> getTargets() {
    return Collections.singletonList(myTarget);
  }

  protected void selectTargets(final List<PsiElement> targets, final Consumer<List<PsiElement>> selectionConsumer) {
    selectionConsumer.consume(targets);
  }

  public void computeUsages(final List<PsiElement> targets) {
    FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.highlight.return");

    final PsiElement parent = myTarget.getParent();
    if (!(parent instanceof PyReturnStatement)) {
      return;
    }

    final PyFunction function = PsiTreeUtil.getParentOfType(myTarget, PyFunction.class);
    if (function == null) {
      return;
    }

    highlightExitPoints((PyReturnStatement)parent, function);
  }

  @Nullable
  private static PsiElement getExitTarget(PsiElement exitStatement) {
    if (exitStatement instanceof PyReturnStatement) {
      return PsiTreeUtil.getParentOfType(exitStatement, PyFunction.class);
    }
    else if (exitStatement instanceof PyBreakStatement) {
      return ((PyBreakStatement)exitStatement).getLoopStatement();
    }
    else if (exitStatement instanceof PyContinueStatement) {
      return ((PyContinueStatement)exitStatement).getLoopStatement();
    }
    else if (exitStatement instanceof PyRaiseStatement) {
      // TODO[oleg]: Implement better logic here!
      return null;
    }
    return null;
  }

  private void highlightExitPoints(final PyReturnStatement statement,
                                   final PyFunction function) {
    final ControlFlow flow = function.getControlFlow();
    final Collection<PsiElement> exitStatements = findExitPointsAndStatements(flow);
    if (!exitStatements.contains(statement)) {
      return;
    }

    final PsiElement originalTarget = getExitTarget(statement);
    for (PsiElement exitStatement : exitStatements) {
      if (getExitTarget(exitStatement) == originalTarget) {
        addOccurrence(exitStatement);
      }
    }
    myStatusText = CodeInsightBundle.message("status.bar.exit.points.highlighted.message",
                                             exitStatements.size(),
                                             HighlightUsagesHandler.getShortcutText());
  }

  private static Collection<PsiElement> findExitPointsAndStatements(final ControlFlow flow) {
    final List<PsiElement> statements = new ArrayList<PsiElement>();
    final Instruction[] instructions = flow.getInstructions();
    for (Instruction instruction : instructions[instructions.length - 1].allPred()){
      final PsiElement element = instruction.getElement();
      if (element == null){
        continue;
      }
      final PsiElement statement = PyPsiUtils.getStatement(element);
      if (statement != null){
        statements.add(statement);
      }
    }
    return statements;
  }
}