package com.jetbrains.python.refactoring.introduce.constant;

import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashSet;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.codeInsight.imports.AddImportHelper;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.refactoring.introduce.IntroduceHandler;
import com.jetbrains.python.refactoring.introduce.IntroduceOperation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey.Ivanov
 */
public class PyIntroduceConstantHandler extends IntroduceHandler {
  public PyIntroduceConstantHandler() {
    super(new ConstantValidator(), PyBundle.message("refactoring.introduce.constant.dialog.title"));
  }

  @Override
  protected PsiElement addDeclaration(@NotNull final PsiElement expression,
                                      @NotNull final PsiElement declaration,
                                      @NotNull final IntroduceOperation operation) {
    final PsiElement anchor = expression.getContainingFile();
    assert anchor instanceof PyFile;
    return anchor.addBefore(declaration, AddImportHelper.getFileInsertPosition((PyFile)anchor));
  }

  public Collection<String> getSuggestedNames(@NotNull final PyExpression expression) {
    Collection<String> names = new HashSet<String>();
    for (String name : super.getSuggestedNames(expression)) {
      names.add(name.toUpperCase());
    }
    return names;
  }

  @Override
  protected String getHelpId() {
    return "python.reference.introduceConstant";
  }
}