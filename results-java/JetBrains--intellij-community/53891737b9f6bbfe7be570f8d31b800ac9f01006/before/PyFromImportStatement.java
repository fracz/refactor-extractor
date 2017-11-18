package com.jetbrains.python.psi;

import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.python.psi.stubs.PyFromImportStatementStub;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Describes "from ... import" statements.
 */
public interface PyFromImportStatement extends PyStatement, StubBasedPsiElement<PyFromImportStatementStub> {
  boolean isStarImport();

  /**
   * Returns a reference the module from which import is required.
   * @return reference to module. If the 'from' reference is relative and consists entirely of dots, null is returned.
   */
  @Nullable PyReferenceExpression getImportSource();

  @Nullable
  List<String> getImportSourceQName();

  /**
   * @return elements that constitute the "import" clause
   */
  PyImportElement[] getImportElements();

  /**
   * @return the star in "from ... import *"
   */
  @Nullable PyStarImportElement getStarImportElement();

  /**
   * @return number of dots in relative "from" clause, or 0 in absolute import.
   */
  int getRelativeLevel();

  /**
   * @return true iff the statement is an import from __future__.
   */
  boolean isFromFuture();
}