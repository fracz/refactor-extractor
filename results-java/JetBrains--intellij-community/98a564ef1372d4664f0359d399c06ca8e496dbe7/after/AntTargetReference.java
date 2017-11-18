package com.intellij.lang.ant.psi.impl.reference;

import com.intellij.lang.ant.psi.AntCall;
import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntTarget;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.StringBuilderSpinAllocator;

public class AntTargetReference extends AntGenericReference {

  private static final ReferenceType ourRefType = new ReferenceType(ReferenceType.ANT_TARGET);

  public AntTargetReference(final GenericReferenceProvider provider,
                            final AntElement antElement,
                            final String str,
                            final TextRange textRange,
                            final XmlAttribute attribute) {
    super(provider, antElement, str, textRange, attribute);
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final AntElement element = getElement();
    if (element instanceof AntProject || element instanceof AntCall) {
      getAttribute().setValue(newElementName);
      element.subtreeChanged();
    }
    else if (element instanceof AntTarget) {
      int start = getElementStartOffset() + getReferenceStartOffset() - getAttributeValueStartOffset();
      final String value = getAttribute().getValue();
      final StringBuilder builder = StringBuilderSpinAllocator.alloc();
      try {
        if (start > 0) {
          builder.append(value.substring(0, start));
        }
        builder.append(newElementName);
        if (value.length() > start + getRangeInElement().getLength()) {
          builder.append(value.substring(start + getRangeInElement().getLength()));
        }
        getAttribute().setValue(builder.toString());
      }
      finally {
        StringBuilderSpinAllocator.dispose(builder);
      }
      element.subtreeChanged();
    }
    return element;
  }

  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    if (element instanceof AntTarget) {
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

  private int getElementStartOffset() {
    return getElement().getTextRange().getStartOffset();
  }

  private int getReferenceStartOffset() {
    return getRangeInElement().getStartOffset();
  }

  private int getAttributeValueStartOffset() {
    return getAttribute().getValueElement().getTextRange().getStartOffset() + 1;
  }
}