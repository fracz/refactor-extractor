package com.jetbrains.python.psi.types;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyQualifiedExpression;

/**
 * @author yole
 */
public class PyNoneType implements PyType { // TODO must extend ClassType. It's an honest instance.
  public static final PyNoneType INSTANCE = new PyNoneType();

  private PyNoneType() {
  }

  public PsiElement resolveMember(final String name) {
    return null;
  }

  public Object[] getCompletionVariants(final PyQualifiedExpression referenceExpression, ProcessingContext context) {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public String getName() {
    return "None";
  }
}