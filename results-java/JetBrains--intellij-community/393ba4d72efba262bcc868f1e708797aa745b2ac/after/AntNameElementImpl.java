package com.intellij.lang.ant.psi.impl;

import com.intellij.lang.ant.psi.AntElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AntNameElementImpl extends AntElementImpl {

  private @Nullable AntElement myElementToRename;

  public AntNameElementImpl(final AntElement parent, final XmlAttributeValue sourceElement) {
    super(parent, sourceElement);
  }

  @NotNull
  public XmlAttributeValue getSourceElement() {
    return (XmlAttributeValue)super.getSourceElement();
  }

  public String getName() {
    return getSourceElement().getValue();
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    final XmlAttribute attr = PsiTreeUtil.getParentOfType(getSourceElement(), XmlAttribute.class);
    if (attr != null) {
      attr.setValue(name);
    }
    return this;
  }

  public final void setElementToRename(@Nullable final AntElement renameElement) {
    myElementToRename = renameElement;
  }

  @NotNull
  public final AntElement getElementToRename() {
    if (myElementToRename != null) {
      return myElementToRename;
    }
    return this;
  }
}