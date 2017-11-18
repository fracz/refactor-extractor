package com.intellij.extapi.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public abstract class MetadataPsiElementBase extends PsiElementBase{

  private final PsiElement mySourceElement;

  public MetadataPsiElementBase( PsiElement sourceElement ) {
    mySourceElement = sourceElement;
  }

  public TextRange getTextRange() {
    return mySourceElement.getTextRange();
  }

  public int getStartOffsetInParent() {
    final PsiElement parent = getParent();
    return (parent == null) ? 0 : parent.getTextOffset() - getTextOffset();
  }

  public int getTextLength() {
    return mySourceElement.getTextLength();
  }

  public int getTextOffset() {
    return mySourceElement.getTextOffset();
  }

  public String getText() {
    return mySourceElement.getText();
  }

  @NotNull
  public char[] textToCharArray() {
    return mySourceElement.textToCharArray();
  }

  public boolean textContains(char c) {
    return mySourceElement.textContains(c);
  }

  public ASTNode getNode() {
    return null;
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }
}