package com.jetbrains.python.codeInsight.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.NotNullFunction;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.PythonStringUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Alexey.Ivanov
 */
public class PyStringConcatenationToFormatIntention extends BaseIntentionAction {

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INTN.string.concatenation.to.format");
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    PsiElement element = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), PyBinaryExpression.class, false);

    if (element == null) {
      return false;
    }
    while (element.getParent() instanceof PyBinaryExpression) {
      element = element.getParent();
    }

    final Collection<PyElementType> operators = getOperators((PyBinaryExpression)element);
    for (PyElementType operator : operators) {
      if (operator != PyTokenTypes.PLUS) {
        return false;
      }
    }

    final Collection<PyExpression> expressions = getSimpleExpressions((PyBinaryExpression)element);
    if (expressions.size() == 0) {
      return false;
    }
    final PyBuiltinCache cache = PyBuiltinCache.getInstance(element);
    for (PyExpression expression: expressions) {
      if (expression == null) {
        return false;
      }
      final boolean isStringLiteral = expression instanceof PyStringLiteralExpression;
      final PyType type = expression.getType(TypeEvalContext.slow());
      final boolean isStringReference = PyTypeChecker.match(cache.getStringType(LanguageLevel.forElement(expression)),
                                                            type, TypeEvalContext.slow()) && type != null;
      if (!(isStringLiteral  || ((expression instanceof PyReferenceExpression || expression instanceof PyCallExpression) &&
                                                           isStringReference))) {
        return false;
      }
    }

    setText(PyBundle.message("INTN.replace.plus.with.format.operator"));
    return true;
  }

  private static Collection<PyExpression> getSimpleExpressions(@NotNull PyBinaryExpression expression) {
    List<PyExpression> res = new ArrayList<PyExpression>();
    if (expression.getLeftExpression() instanceof PyBinaryExpression) {
      res.addAll(getSimpleExpressions((PyBinaryExpression) expression.getLeftExpression()));
    } else {
      res.add(expression.getLeftExpression());
    }
    if (expression.getRightExpression() instanceof PyBinaryExpression) {
      res.addAll(getSimpleExpressions((PyBinaryExpression) expression.getRightExpression()));
    } else {
      res.add(expression.getRightExpression());
    }
    return res;
  }

  private static Collection<PyElementType> getOperators(@NotNull PyBinaryExpression expression) {
    List<PyElementType> res = new ArrayList<PyElementType>();
    if (expression.getLeftExpression() instanceof PyBinaryExpression) {
      res.addAll(getOperators((PyBinaryExpression)expression.getLeftExpression()));
    }
    if (expression.getRightExpression() instanceof PyBinaryExpression) {
      res.addAll(getOperators((PyBinaryExpression)expression.getRightExpression()));
    }
    res.add(expression.getOperator());
    return res;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiElement element = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), PyBinaryExpression.class, false);
    while (element.getParent() instanceof PyBinaryExpression) {
      element = element.getParent();
    }
    StringBuilder stringLiteral = new StringBuilder();
    StringBuilder parameters = new StringBuilder();
    NotNullFunction<String,String> escaper = StringUtil.escaper(false, null);

    int addParens = 0;
    Pair<String, String> quotes = new Pair<String, String>("\"", "\"");
    boolean quotesDetected = false;
    for (PyExpression expression : getSimpleExpressions((PyBinaryExpression) element)) {
      if (expression instanceof PyStringLiteralExpression) {
        if (!quotesDetected) {
          quotes = PythonStringUtil.getQuotes(expression.getText());
          quotesDetected = true;
        }
        stringLiteral.append(escaper.fun(((PyStringLiteralExpression)expression).getStringValue()));
      } else {
        ++addParens;
        stringLiteral.append("%s");
        parameters.append(expression.getText()).append(", ");
      }
    }
    if (quotes == null)
      quotes = new Pair<String, String>("\"", "\"");

    PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
    PyStringLiteralExpression stringLiteralExpression =
      elementGenerator.createStringLiteralAlreadyEscaped(quotes.getFirst() + stringLiteral.toString() + quotes.getSecond());

    if (addParens > 0) {
      final String paramString = addParens > 1? "(" + parameters.substring(0, parameters.length() - 2) +")"
                                                     : parameters.substring(0, parameters.length() - 2);
      final PyExpression expression = elementGenerator.createFromText(LanguageLevel.getDefault(),
                                                              PyExpressionStatement.class, paramString).getExpression();
      element.replace(elementGenerator.createBinaryExpression("%", stringLiteralExpression, expression));
    }
    else {
      element.replace(stringLiteralExpression);
    }
  }
}