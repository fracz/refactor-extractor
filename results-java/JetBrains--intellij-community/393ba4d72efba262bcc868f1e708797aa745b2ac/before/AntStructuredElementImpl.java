package com.intellij.lang.ant.psi.impl;

import com.intellij.lang.ant.misc.AntStringInterner;
import com.intellij.lang.ant.misc.PsiElementSetSpinAllocator;
import com.intellij.lang.ant.psi.*;
import com.intellij.lang.ant.psi.introspection.AntTypeDefinition;
import com.intellij.lang.ant.psi.introspection.AntTypeId;
import com.intellij.lang.ant.psi.introspection.impl.AntTypeDefinitionImpl;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLock;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SpinAllocator;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.StringSetSpinAllocator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AntStructuredElementImpl extends AntElementImpl implements AntStructuredElement {

  protected volatile AntTypeDefinition myDefinition;
  private boolean myDefinitionCloned;
  private volatile AntElement myIdElement;
  private volatile AntElement myNameElement;
  @NonNls private volatile String myNameElementAttribute;
  private volatile int myLastFoundElementOffset = -1;
  private volatile AntElement myLastFoundElement;
  private volatile boolean myIsImported;
  private volatile Set<String> myComputingAttrValue;
  protected volatile boolean myInGettingChildren;
  @NonNls private static final String ANTLIB_NS_PREFIX = "antlib:";
  @NonNls private static final String ANTLIB_XML = "antlib.xml";

  public AntStructuredElementImpl(final AntElement parent, final XmlTag sourceElement, @NonNls final String nameElementAttribute) {
    super(parent, sourceElement);
    myNameElementAttribute = AntStringInterner.intern(nameElementAttribute);
    getIdElement();
    getNameElement();
    invalidateAntlibNamespace();
  }

  public AntStructuredElementImpl(final AntElement parent, final XmlTag sourceElement) {
    this(parent, sourceElement, AntFileImpl.NAME_ATTR);
  }

  public AntStructuredElementImpl(final AntElement parent,
                                  final XmlTag sourceElement,
                                  final AntTypeDefinition definition,
                                  @NonNls final String nameElementAttribute) {
    this(parent, sourceElement, nameElementAttribute);
    myDefinition = definition;
    final AntTypeId id = new AntTypeId(sourceElement.getName());
    if (definition != null) {
      if (!definition.getTypeId().equals(id)) {
        myDefinition = new AntTypeDefinitionImpl((AntTypeDefinitionImpl)myDefinition);
        myDefinition.setTypeId(id);
        myDefinitionCloned = true;
      }
    }
    else {
      /**
       * This branch reloads of type definition in case if it could be
       * registered during invalidation of the "antlib:..." namespace
       */
      final AntFile file = getAntFile();
      if (file != null) {
        final AntTypeDefinition targetDef = file.getTargetDefinition();
        final String className = targetDef.getNestedClassName(id);
        if (className != null) {
          myDefinition = file.getBaseTypeDefinition(className);
        }
      }
    }
  }

  public AntStructuredElementImpl(final AntElement parent, final XmlTag sourceElement, final AntTypeDefinition definition) {
    this(parent, sourceElement, definition, AntFileImpl.NAME_ATTR);
  }

  @NotNull
  public XmlTag getSourceElement() {
    return (XmlTag)super.getSourceElement();
  }

  public String toString() {
    @NonNls final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append("AntStructuredElement[");
      builder.append(getSourceElement().getName());
      builder.append("]");
      return builder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public String getName() {
    if (hasNameElement()) {
      return computeAttributeValue(getNameElement().getName());
    }
    if (hasIdElement()) {
      return computeAttributeValue(getIdElement().getName());
    }
    return super.getName();
  }

  public PsiElement setName(@NotNull final String name) throws IncorrectOperationException {
    if (hasNameElement()) {
      getNameElement().setName(name);
    }
    else if (hasIdElement()) {
      getIdElement().setName(name);
    }
    else {
      super.setName(name);
    }
    return this;
  }

  public boolean canRename() {
    return super.canRename() && (hasNameElement() || hasIdElement());
  }

  public PsiElement findElementAt(int offset) {
    synchronized (PsiLock.LOCK) {
      if (offset != myLastFoundElementOffset) {
        final PsiElement foundElement = super.findElementAt(offset);
        if (foundElement == null) {
          return null;
        }
        myLastFoundElement = (AntElement)foundElement;
        myLastFoundElementOffset = offset;
      }
      assert myLastFoundElement == null || myLastFoundElement.isValid() : myLastFoundElement + ": " + offset;
      return myLastFoundElement;
    }
  }

  public AntTypeDefinition getTypeDefinition() {
    return myDefinition;
  }

  public void registerCustomType(final AntTypeDefinition def) {
    synchronized (PsiLock.LOCK) {
      if (myDefinition != null) {
        if (!myDefinitionCloned) {
          myDefinition = new AntTypeDefinitionImpl((AntTypeDefinitionImpl)myDefinition);
          myDefinitionCloned = true;
        }
        myDefinition.registerNestedType(def.getTypeId(), def.getClassName());
      }
      getAntFile().registerCustomType(def);
    }
  }

  public void unregisterCustomType(final AntTypeDefinition def) {
    synchronized (PsiLock.LOCK) {
      if (myDefinition != null && myDefinitionCloned) {
        myDefinition.unregisterNestedType(def.getTypeId());
      }
      getAntFile().unregisterCustomType(def);
    }
  }

  public boolean hasImportedTypeDefinition() {
    return myIsImported;
  }

  void setImportedTypeDefinition(boolean imported) {
    myIsImported = imported;
  }

  @Nullable
  public PsiFile findFileByName(final String name) {
    return findFileByName(name, "");
  }

  @Nullable
  public PsiFile findFileByName(final String name, @Nullable final String baseDir) {
    if (name == null) {
      return null;
    }
    final AntFile antFile = getAntFile();
    if (antFile == null) {
      return null;
    }
    VirtualFile vFile = antFile.getContainingPath();
    if (vFile == null) {
      return null;
    }
    String projectPath = vFile.getPath();
    String dir = baseDir;
    if (dir == null) {
      final AntProject project = antFile.getAntProject();
      dir = project == null ? null : project.getBaseDir();
    }
    if (dir != null && dir.length() > 0) {
      projectPath = new File(projectPath, dir).getAbsolutePath();
    }
    final String fileName = computeAttributeValue(name);
    if (fileName == null) {
      return null;
    }
    File file = new File(fileName);
    if (!file.isAbsolute()) {
      file = new File(projectPath, fileName);
    }
    vFile = LocalFileSystem.getInstance().findFileByPath(file.getAbsolutePath().replace(File.separatorChar, '/'));
    if (vFile == null) {
      return null;
    }
    return antFile.getViewProvider().getManager().findFile(vFile);
  }

  @Nullable
  public String computeAttributeValue(final String value) {
    synchronized (PsiLock.LOCK) {
      if (value != null) {
        if (myComputingAttrValue == null || !myComputingAttrValue.contains(value)) {
          try {
            if (myComputingAttrValue == null) {
              myComputingAttrValue = StringSetSpinAllocator.alloc();
            }
            myComputingAttrValue.add(value);
            try {
              final Set<PsiElement> set = PsiElementSetSpinAllocator.alloc();
              try {
                return computeAttributeValue(value, set);
              }
              finally {
                PsiElementSetSpinAllocator.dispose(set);
              }
            }
            catch (SpinAllocator.AllocatorExhaustedException e) {
              return computeAttributeValue(value, new HashSet<PsiElement>());
            }
          }
          finally {
            myComputingAttrValue.remove(value);
            if (myComputingAttrValue.size() == 0) {
              final Set<String> _strSet = myComputingAttrValue;
              myComputingAttrValue = null;
              StringSetSpinAllocator.dispose(_strSet);
            }
          }
        }
        else if (value.indexOf('$') < 0){
          return value;
        }
      }
      return null;
    }
  }

  public boolean hasNameElement() {
    return getNameElement() != ourNull;
  }

  public boolean hasIdElement() {
    return getIdElement() != ourNull;
  }

  public String getFileReferenceAttribute() {
    return null;
  }

  public boolean isTypeDefined() {
    return myDefinition != null && myDefinition.getDefiningElement() instanceof AntTypeDefImpl;
  }

  public boolean isPresetDefined() {
    return myDefinition != null && myDefinition.getClassName().startsWith(AntPresetDefImpl.ANT_PRESETDEF_NAME);
  }

  public void clearCaches() {
    synchronized (PsiLock.LOCK) {
      super.clearCaches();
      myIdElement = null;
      myNameElement = null;
      myLastFoundElementOffset = -1;
      myLastFoundElement = null;
      invalidateAntlibNamespace();
    }
  }

  public AntElement lightFindElementAt(int offset) {
    if (offset == myLastFoundElementOffset) {
      return myLastFoundElement;
    }
    return super.lightFindElementAt(offset);
  }

  public int getTextOffset() {
    if (hasNameElement()) {
      return getNameElement().getTextOffset();
    }
    if (hasIdElement()) {
      return getIdElement().getTextOffset();
    }
    return super.getTextOffset();
  }

  protected AntElement[] getChildrenInner() {
    synchronized (PsiLock.LOCK) {
      if (!myInGettingChildren) {
        myInGettingChildren = true;
        try {
          final List<AntElement> children = new ArrayList<AntElement>();
          if (hasIdElement()) {
            children.add(getIdElement());
          }
          if (hasNameElement()) {
            children.add(getNameElement());
          }
          for (final PsiElement element : getSourceElement().getChildren()) {
            if (element instanceof XmlElement) {
              final AntElement antElement = AntElementFactory.createAntElement(this, (XmlElement)element);
              if (antElement != null) {
                children.add(antElement);
                if (antElement instanceof AntStructuredElement) {
                  antElement.getChildren();
                }
              }
            }
          }
          final int count = children.size();
          return count > 0 ? children.toArray(new AntElement[count]) : AntElement.EMPTY_ARRAY;
        }
        finally {
          myInGettingChildren = false;
        }
      }
    }
    return AntElement.EMPTY_ARRAY;
  }

  @NotNull
  private AntElement getIdElement() {
    synchronized (PsiLock.LOCK) {
      if (myIdElement == null) {
        myIdElement = ourNull;
        final XmlTag se = getSourceElement();
        if (se.isValid()) {
          final XmlAttribute idAttr = se.getAttribute(AntFileImpl.ID_ATTR, null);
          if (idAttr != null) {
            final XmlAttributeValue valueElement = idAttr.getValueElement();
            if (valueElement != null) {
              myIdElement = new AntNameElementImpl(this, valueElement);
              getAntProject().registerRefId(myIdElement.getName(), this);
            }
          }
        }
      }
      return myIdElement;
    }
  }

  @NotNull
  private AntElement getNameElement() {
    synchronized (PsiLock.LOCK) {
      if (myNameElement == null) {
        myNameElement = ourNull;
        final XmlTag se = getSourceElement();
        if (se.isValid()) {
          final XmlAttribute nameAttr = se.getAttribute(myNameElementAttribute, null);
          if (nameAttr != null) {
            final XmlAttributeValue valueElement = nameAttr.getValueElement();
            if (valueElement != null) {
              myNameElement = new AntNameElementImpl(this, valueElement);
            }
          }
        }
      }
      return myNameElement;
    }
  }

  @NonNls
  protected String getNameElementAttribute() {
    return myNameElementAttribute;
  }

  /**
   * Cycle-safe computation of an attribute value with resolving properties.
   *
   * @param value
   * @param elementStack
   * @return
   */
  private String computeAttributeValue(String value, final Set<PsiElement> elementStack) {
    elementStack.add(this);
    int startProp = 0;
    while ((startProp = value.indexOf("${", startProp)) >= 0) {
      final int endProp = value.indexOf('}', startProp + 2);
      if (endProp <= startProp + 2) {
        startProp += 2;
        continue;
      }
      final String prop = value.substring(startProp + 2, endProp);
      final PsiElement propElement = resolveProperty(this, prop);
      if (elementStack.contains(propElement)) {
        return value;
      }
      String resolvedValue = null;
      if (propElement instanceof AntProperty) {
        final AntProperty antProperty = (AntProperty)propElement;
        resolvedValue = antProperty.getValue(prop);
        if (resolvedValue != null) {
          resolvedValue = ((AntStructuredElementImpl)antProperty).computeAttributeValue(resolvedValue, elementStack);
        }
      }
      else if (propElement instanceof Property) {
        resolvedValue = ((Property)propElement).getValue();
      }
      if (resolvedValue == null) {
        startProp += 2;
      }
      else {
        final StringBuilder builder = StringBuilderSpinAllocator.alloc();
        try {
          builder.append(value, 0, startProp);
          builder.append(resolvedValue);
          if (endProp < value.length() - 1) {
            builder.append(value, endProp + 1, value.length());
          }
          value = builder.toString();
        }
        finally {
          StringBuilderSpinAllocator.dispose(builder);
        }
      }
    }
    return value;
  }

  private String getNamespace() {
    return getSourceElement().getNamespace();
  }

  private void invalidateAntlibNamespace() {
    final AntFile file = getAntFile();
    if (file == null) return;
    final ClassLoader loader = file.getClassLoader().getClassloader();
    if (loader == null) return;
    final String ns = getNamespace();
    if (!ns.startsWith(ANTLIB_NS_PREFIX)) return;
    final AntElement parent = getAntParent();
    if (!(this instanceof AntProject)) {
      if (parent instanceof AntStructuredElementImpl && ns.equals(((AntStructuredElementImpl)parent).getNamespace())) return;
    }

    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append(ns.substring(ANTLIB_NS_PREFIX.length()).replace('.', '/'));
      builder.append('/');
      builder.append(ANTLIB_XML);
      final InputStream antlibStream = loader.getResourceAsStream(builder.toString());
      if (antlibStream != null) {
        AntTypeDefImpl.loadAntlibStream(antlibStream, this, ns);
      }
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }
}