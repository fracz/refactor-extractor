package com.jetbrains.python.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Ivanov
 * Date: 03.03.2010
 * Time: 16:49:59
 */
public class TransformClassicClassQuickFix implements LocalQuickFix {
  @NotNull
  public String getName() {
    return PyBundle.message("QFIX.classic.class.transform");
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INSP.GROUP.python");
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement psiElement = descriptor.getPsiElement();
    if (psiElement instanceof PyClass) {
      PyClass pyClass = (PyClass) psiElement;
      PyExpression[] superClassExpressions = pyClass.getSuperClassExpressions();
      PyElementGenerator generator = PyElementGenerator.getInstance(project);
      if (superClassExpressions.length == 0) {
        pyClass.replace(generator.createFromText(PyClass.class,
                                                 "class " + pyClass.getName() + "(" +
                                                 PyNames.OBJECT + "):\n    " + pyClass.getStatementList().getText()));
      } else {
        StringBuilder stringBuilder = new StringBuilder("class ");
        stringBuilder.append(pyClass.getName()).append("(");
        for (PyExpression expression: superClassExpressions) {
          stringBuilder.append(expression.getText()).append(", ");
        }
        stringBuilder.append(PyNames.OBJECT).append(":\n    ");
        stringBuilder.append(pyClass.getStatementList().getText());
        pyClass.replace(generator.createFromText(PyClass.class, stringBuilder.toString()));
      }
    }
  }
}