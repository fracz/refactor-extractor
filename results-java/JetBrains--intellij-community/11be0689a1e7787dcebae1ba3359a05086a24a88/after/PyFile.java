/*
 * Copyright (c) 2005, Your Corporation. All Rights Reserved.
 */
package com.jetbrains.python.psi;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PyFile extends PyElement, PsiFile {

  Key<Boolean> KEY_IS_DIRECTORY = Key.create("Dir impersonated by __init__.py");

  List<PyStatement> getStatements();

  List<PyClass> getTopLevelClasses();

  List<PyFunction> getTopLevelFunctions();

  List<PyTargetExpression> getTopLevelAttributes();

  /**
   * Looks for a name exported by this file, preferably in an efficient way.
   * @param name what to find
   * @param project project to use (useful for out-of-hierarchy files)
   * @return found element, or null.
   */
  @Nullable
  PsiElement findExportedName(String name);

  /**
  @return an URL of file, maybe bogus if virtual file is not present.
  */
  @NotNull
  String getUrl();
}