package com.intellij.lang.ant.psi.impl;

import com.intellij.extapi.psi.MetadataPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ant.AntLanguage;
import com.intellij.lang.ant.AntSupport;
import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.impl.reference.AntReferenceProvidersRegistry;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AntElementImpl extends MetadataPsiElementBase implements AntElement {

  private final AntElement myParent;
  private AntElement[] myChildren = null;
  private PsiReference[] myReferences = null;

  public AntElementImpl(final AntElement parent, final XmlElement sourceElement) {
    super(sourceElement);
    myParent = parent;
  }

  @NotNull
  public AntLanguage getLanguage() {
    return AntSupport.getLanguage();
  }

  @NotNull
  public XmlElement getSourceElement() {
    return (XmlElement)super.getSourceElement();
  }

  public AntElement getAntParent() {
    return myParent;
  }

  public PsiElement getParent(){
    return myParent;
  }

  @NotNull
  public AntElement[] getChildren(){
    if(myChildren != null) return myChildren;
    return myChildren = getChildrenInner();
  }

  @Nullable
  public AntElement getFirstChild() {
    final AntElement[] children = getChildren();
    return (children.length == 0) ? null : children[0];
  }

  @Nullable
  public PsiElement getLastChild() {
    final PsiElement[] children = getChildren();
    return (children.length == 0) ? null : children[children.length - 1];
  }

  @Nullable
  public PsiElement getNextSibling() {
    final PsiElement parent = getAntParent();
    if (parent != null) {
      final PsiElement[] thisLevelElements = parent.getChildren();
      PsiElement thisElement = null;
      for (PsiElement element : thisLevelElements) {
        if (thisElement != null) {
          return element;
        }
        if (element == this) {
          thisElement = element;
        }
      }
    }
    return null;
  }

  @Nullable
  public PsiElement getPrevSibling() {
    PsiElement prev = null;
    final PsiElement parent = getAntParent();
    if (parent != null) {
      final PsiElement[] thisLevelElements = parent.getChildren();
      for (PsiElement element : thisLevelElements) {
        if (element == this) {
          break;
        }
        prev = element;
      }
    }
    return prev;
  }

  public void clearCaches() {
    myReferences = null;
    myChildren = null;
  }

  public void subtreeChanged() {
    final AntElement parent = getAntParent();
    clearCaches();
    if(parent != null) parent.subtreeChanged();
  }

  public PsiElement findElementAt(int offset) {
    final int offsetInFile = offset + getTextRange().getStartOffset();
    for (final AntElement element : getChildren()) {
      final TextRange textRange = element.getTextRange();
      if(textRange.contains(offsetInFile))
        return element.findElementAt(offsetInFile - textRange.getStartOffset());
    }
    return getTextRange().contains(offsetInFile) ? this : null;
  }

  public ASTNode getNode() {
    return null;
  }

  @NotNull
  public PsiReference[] getReferences() {
    if(myReferences != null) {
      return myReferences;
    }
    final GenericReferenceProvider[] providers = AntReferenceProvidersRegistry.getProvidersByElement(this);
    final List<PsiReference> result = new ArrayList<PsiReference>();
    for (final GenericReferenceProvider provider : providers) {
      result.addAll(Arrays.asList(provider.getReferencesByElement(this)));
    }
    return myReferences = result.toArray(new PsiReference[result.size()]);
  }

  protected AntElement[] getChildrenInner() {
    return AntElement.EMPTY_ARRAY;
  }

  protected AntElement clone() {
    final AntElementImpl element = (AntElementImpl)super.clone();
    element.clearCaches();
    return element;
  }
}