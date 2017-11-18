package com.jetbrains.python.refactoring.surround.surrounders.statements;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyIfStatement;
import com.jetbrains.python.psi.PyStatementList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Ivanov
 * Date: Aug 27, 2009
 * Time: 8:45:08 PM
 */
public class PyWithIfElseSurrounder extends PyStatementSurrounder {
  @Override
  @Nullable
  protected TextRange surroundStatement(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements)
    throws IncorrectOperationException {
    PyIfStatement ifStatement =
      PyElementGenerator.getInstance(project).createFromText(PyIfStatement.class, "if True:\n    pass\nelse:    pass\n");
    final PsiElement parent = elements[0].getParent();
    final PyStatementList statementList = ifStatement.getIfPart().getStatementList();
    assert statementList != null;
    statementList.addRange(elements[0], elements[elements.length - 1]);
    statementList.deleteChildRange(statementList.getFirstChild(), statementList.getFirstChild());

    ifStatement = (PyIfStatement) parent.addBefore(ifStatement, elements[0]);
    parent.deleteChildRange(elements[0], elements[elements.length - 1]);

    ifStatement = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(ifStatement);
    if (ifStatement == null) {
      return null;
    }
    return ifStatement.getTextRange();
  }

  public String getTemplateDescription() {
    return CodeInsightBundle.message("surround.with.ifelse.template");
  }
}