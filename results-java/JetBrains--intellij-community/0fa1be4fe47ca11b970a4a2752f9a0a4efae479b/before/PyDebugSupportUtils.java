package com.jetbrains.python.debugger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nullable;


public class PyDebugSupportUtils {

  private PyDebugSupportUtils() { }

  // can expression be evaluated, or should be executed
  public static boolean isExpression(final Project project, final String expression) {
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {

        final PsiFile file = PythonLanguage.getInstance().createDummyFile(project, expression);
        return file.getFirstChild() instanceof PyExpressionStatement && file.getFirstChild() == file.getLastChild();

      }
    });
  }

  public static TextRange getExpressionRangeAtOffset(final Project project, final Document document, final int offset) {
    return ApplicationManager.getApplication().runReadAction(new Computable<TextRange>() {
      @Nullable public TextRange compute() {

        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile != null) {
          PsiElement element = psiFile.findElementAt(offset);
          if (!(element instanceof PyExpression)) {
            element = PsiTreeUtil.getParentOfType(element, PyExpression.class);
          }
          if (element != null && isSimpleEnough(element)) {
            return element.getTextRange();
          }
        }
        return null;

      }
    });
  }

  // is expression suitable to quick evaluate/display tooltip
  private static boolean isSimpleEnough(final PsiElement element) {
    return element instanceof PyLiteralExpression ||
           element instanceof PyQualifiedExpression ||
           element instanceof PyBinaryExpression ||
           element instanceof PyPrefixExpression ||
           element instanceof PySliceExpression ||
           element instanceof PyNamedParameter;
  }

  // is expression a variable reference
  // todo: use patterns (?)
  public static boolean isVariable(final Project project, final String expression) {
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {

        final PsiFile file = PythonLanguage.getInstance().createDummyFile(project, expression);
        final PsiElement root = file.getFirstChild();
        return root instanceof PyExpressionStatement &&
               root.getFirstChild() instanceof PyReferenceExpression &&
               root.getFirstChild() == root.getLastChild() &&
               root.getFirstChild().getFirstChild() != null &&
               root.getFirstChild().getFirstChild().getNode().getElementType() == PyTokenTypes.IDENTIFIER &&
               root.getFirstChild().getFirstChild() == root.getFirstChild().getLastChild() &&
               root.getFirstChild().getFirstChild().getFirstChild() == null;

      }
    });
  }

}