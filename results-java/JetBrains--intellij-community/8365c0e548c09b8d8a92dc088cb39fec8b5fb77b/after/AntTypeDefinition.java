package com.intellij.lang.ant.psi.introspection;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface AntTypeDefinition {

  AntTypeId getTypeId();

  void setTypeId(final AntTypeId id);

  String getClassName();

  boolean isTask();

  String[] getAttributes();

  AntAttributeType getAttributeType(final String attr);

  AntTypeId[] getNestedElements();

  @Nullable
  String getNestedClassName(final AntTypeId id);

  void registerNestedType(final AntTypeId typeId, final String className);

  PsiElement getDefiningElement();
}