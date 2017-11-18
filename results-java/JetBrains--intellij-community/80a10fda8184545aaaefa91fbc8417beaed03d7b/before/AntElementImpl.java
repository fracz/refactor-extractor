package com.intellij.lang.ant.psi.impl;

import com.intellij.extapi.psi.MetadataPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ant.AntLanguage;
import com.intellij.lang.ant.AntSupport;
import com.intellij.lang.ant.misc.AntPsiUtil;
import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.AntFile;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntProperty;
import com.intellij.lang.ant.psi.impl.reference.AntReferenceProvidersRegistry;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLock;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntElementImpl extends MetadataPsiElementBase implements AntElement {

  protected static AntElement ourNull = new AntElementImpl(null, null) {
    @NonNls
    public String getName() {
      return "AntNull";
    }
  };

  private final AntElement myParent;
  private AntElement[] myChildren = null;
  private PsiReference[] myReferences = null;
  protected Map<String, PsiElement> myProperties;
  private PsiElement[] myPropertiesArray;

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

  public String toString() {
    @NonNls StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append("AntElement[");
      builder.append((this == ourNull) ? "null" : getSourceElement().toString());
      builder.append("]");
      return builder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public PsiElement setName(String name) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename ant element");
  }

  public AntElement getAntParent() {
    return myParent;
  }

  public AntProject getAntProject() {
    return (AntProject)((this instanceof AntProject) ? this : PsiTreeUtil.getParentOfType(this, AntProject.class));
  }

  public AntFile getAntFile() {
    return PsiTreeUtil.getParentOfType(this, AntFile.class);
  }

  public PsiElement getParent() {
    return myParent;
  }

  @NotNull
  public AntElement[] getChildren() {
    synchronized (PsiLock.LOCK) {
      if (myChildren != null) return myChildren;
      return myChildren = getChildrenInner();
    }
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
    synchronized (PsiLock.LOCK) {
      myChildren = null;
    }
    myReferences = null;
    myProperties = null;
    myPropertiesArray = null;
  }

  public void setProperty(final String name, final PsiElement element) {
    if (myProperties == null) {
      myProperties = new HashMap<String, PsiElement>();
    }
    myProperties.put(name, element);
    myPropertiesArray = null;
  }

  @Nullable
  public PsiElement getProperty(final String name) {
    return (myProperties == null) ? null : myProperties.get(name);
  }

  @NotNull
  public PsiElement[] getProperties() {
    if (myProperties == null) {
      return PsiElement.EMPTY_ARRAY;
    }
    if (myPropertiesArray == null) {
      myPropertiesArray = myProperties.values().toArray(new PsiElement[myProperties.size()]);
    }
    return myPropertiesArray;
  }

  public AntElement lightFindElementAt(int offset) {
    synchronized (PsiLock.LOCK) {
      if (myChildren == null) return this;
      final int offsetInFile = offset + getTextRange().getStartOffset();
      for (final AntElement element : getChildren()) {
        final TextRange textRange = element.getTextRange();
        if (textRange.contains(offsetInFile)) {
          return element.lightFindElementAt(offsetInFile - textRange.getStartOffset());
        }
      }
      return getTextRange().contains(offsetInFile) ? this : null;
    }
  }

  public PsiElement findElementAt(int offset) {
    final int offsetInFile = offset + getTextRange().getStartOffset();
    for (final AntElement element : getChildren()) {
      final TextRange textRange = element.getTextRange();
      if (textRange.contains(offsetInFile)) {
        return element.findElementAt(offsetInFile - textRange.getStartOffset());
      }
    }
    return getTextRange().contains(offsetInFile) ? this : null;
  }

  public ASTNode getNode() {
    return null;
  }

  @NotNull
  public PsiReference[] getReferences() {
    if (myReferences != null) {
      return myReferences;
    }
    final GenericReferenceProvider[] providers = AntReferenceProvidersRegistry.getProvidersByElement(this);
    final List<PsiReference> result = new ArrayList<PsiReference>();
    for (final GenericReferenceProvider provider : providers) {
      final PsiReference[] refs = provider.getReferencesByElement(this);
      for (PsiReference ref : refs) {
        result.add(ref);
      }
    }
    return myReferences = result.toArray(new PsiReference[result.size()]);
  }

  public boolean isPhysical() {
    return getSourceElement().isPhysical();
  }

  protected AntElement[] getChildrenInner() {
    return AntElement.EMPTY_ARRAY;
  }

  protected AntElement clone() {
    final AntElementImpl element = (AntElementImpl)super.clone();
    element.clearCaches();
    return element;
  }

  public static PsiElement resolveProperty(@NotNull final AntElement element, final String name) {
    AntElement temp = element;
    while (temp != null) {
      final PsiElement psiElement = temp.getProperty(name);
      if (psiElement != null) {
        return psiElement;
      }
      temp = temp.getAntParent();
    }
    final AntElement anchor = AntPsiUtil.getSubProjectElement(element);
    for (PsiElement child : element.getAntProject().getChildren()) {
      if (child == anchor) break;
      if (child instanceof AntProperty) {
        AntProperty prop = (AntProperty)child;
        final PropertiesFile propFile = prop.getPropertiesFile();
        if (propFile != null) {
          final Property property = propFile.findPropertyByKey(name);
          if (property != null) {
            return property;
          }
        }
      }
    }
    return null;
  }
}