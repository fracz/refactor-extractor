package com.jetbrains.python.codeInsight.intentions;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.debugger.PySignature;
import com.jetbrains.python.debugger.PySignatureCacheManager;
import com.jetbrains.python.documentation.PyDocstringGenerator;
import com.jetbrains.python.documentation.doctest.PyDocstringFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;

/**
 * User: catherine
 * Intention to add documentation string for function
 * (with checked format)
 */
public class PyGenerateDocstringIntention extends BaseIntentionAction {
  private String myText;

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INTN.doc.string.stub");
  }

  @NotNull
  @Override
  public String getText() {
    return myText;
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (file instanceof PyDocstringFile) return false;
    PsiElement elementAt = PyUtil.findNonWhitespaceAtOffset(file, editor.getCaretModel().getOffset());
    if (elementAt == null) {
      return false;
    }
    PyFunction function = PsiTreeUtil.getParentOfType(elementAt, PyFunction.class);
    if (function == null) {
      return false;
    }
    return isAvailableForFunction(project, function);
  }

  private boolean isAvailableForFunction(Project project, PyFunction function) {
    if (function.getDocStringValue() != null) {
      PySignature signature = PySignatureCacheManager.getInstance(project).findSignature(function);

      PyDocstringGenerator docstringGenerator = new PyDocstringGenerator(function);

      docstringGenerator.addFunctionArguments(function, signature);


      if (docstringGenerator.haveParametersToAdd()) {
        myText = PyBundle.message("INTN.add.parameters.to.docstring");
        return true;
      }
      else {
        return false;
      }
    }
    else {
      myText = PyBundle.message("INTN.doc.string.stub");
      return true;
    }
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (!CodeInsightUtilBase.preparePsiElementForWrite(file)) {
      return;
    }

    PsiElement elementAt = PyUtil.findNonWhitespaceAtOffset(file, editor.getCaretModel().getOffset());
    PyFunction function = PsiTreeUtil.getParentOfType(elementAt, PyFunction.class);

    if (function == null) {
      return;
    }

    generateDocstringForFunction(project, editor, function);
  }

  public static void generateDocstringForFunction(Project project, Editor editor, PyFunction function) {
    PyDocstringGenerator docstringGenerator = new PyDocstringGenerator(function);

    PySignature signature = PySignatureCacheManager.getInstance(project).findSignature(function);

    docstringGenerator.addFunctionArguments(function, signature);
    if (function.getDocStringValue() == null) {
      docstringGenerator.withReturn();
    }

    docstringGenerator.build();
  }
}