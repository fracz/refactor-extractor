package com.intellij.lang.ant.psi.impl.reference;

import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.AntTarget;
import com.intellij.lang.ant.psi.impl.AntProjectImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.GenericReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.util.IncorrectOperationException;

public class AntTargetReference extends GenericReference {

  private static final ReferenceType ourRefType = new ReferenceType(ReferenceType.ANT_TARGET);

  private AntElement myAntElement;
  private String myText;
  private TextRange myTextRange;

  public AntTargetReference(GenericReferenceProvider provider, final AntElement antElement, final String str, final TextRange textRange) {
    super(provider);
    myAntElement = antElement;
    myText = str.trim();
    myTextRange = textRange;
  }

  public PsiElement getElement() {
    return myAntElement;
  }

  public TextRange getRangeInElement() {
    return myTextRange;
  }

  public String getCanonicalText() {
    return myText;
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final AntProjectImpl project = (AntProjectImpl)getElement();
    project.getSourceElement().setAttribute("default", newElementName);
    project.subtreeChanged();
    return getElement();
  }

  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    if(element instanceof AntTarget) {
      final PsiNamedElement psiNamedElement = (PsiNamedElement)element;
      return handleElementRename(psiNamedElement.getName());
    }
    throw new IncorrectOperationException("Can bind only to ant targets.");
  }

  public PsiElement getContext() {
    return null;
  }

  public PsiReference getContextReference() {
    return null;
  }

  public static ReferenceType getReferenceType() {
    return ourRefType;
  }

  public ReferenceType getType() {
    return getReferenceType();
  }

  public ReferenceType getSoftenType() {
    return getReferenceType();
  }

  public boolean needToCheckAccessibility() {
    return false;
  }
}