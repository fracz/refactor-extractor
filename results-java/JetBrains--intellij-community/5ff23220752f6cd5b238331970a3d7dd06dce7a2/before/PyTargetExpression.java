package com.jetbrains.python.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.stubs.PyTargetExpressionStub;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public interface PyTargetExpression extends PyQualifiedExpression, PsiNamedElement, PsiNameIdentifierOwner, StubBasedPsiElement<PyTargetExpressionStub> {
  PyTargetExpression[] EMPTY_ARRAY = new PyTargetExpression[0];

  /**
   * Find the value that maps to this target expression in an enclosing assignment expression.
   * Does not work with other expressions (e.g. if the target is in a 'for' loop).
   *
   * @return the expression assigned to target via an enclosing assignment expression, or null.
   */
  @Nullable
  PyExpression findAssignedValue();

  @Nullable
  PyQualifiedName getAssignedQName();
}