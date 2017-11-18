package com.intellij.psi.impl.source.xml;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementFactory;
import com.intellij.jsp.impl.TldAttributeDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomManager;
import com.intellij.pom.PomModel;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.pom.impl.PomTransactionBase;
import com.intellij.pom.xml.XmlAspect;
import com.intellij.pom.xml.impl.XmlAspectChangeSetImpl;
import com.intellij.pom.xml.impl.events.XmlAttributeSetImpl;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.meta.PsiMetaOwner;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Mike
 */
public class XmlAttributeImpl extends XmlElementImpl implements XmlAttribute {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.xml.XmlAttributeImpl");

  public XmlAttributeImpl() {
    super(XmlElementType.XML_ATTRIBUTE);
  }

  public int getChildRole(ASTNode child) {
    LOG.assertTrue(child.getTreeParent() == this);
    IElementType i = child.getElementType();
    if (i == XmlTokenType.XML_NAME) {
      return XmlChildRole.XML_NAME;
    }
    else if (i == XmlElementType.XML_ATTRIBUTE_VALUE) {
      return XmlChildRole.XML_ATTRIBUTE_VALUE;
    }
    else {
      return ChildRole.NONE;
    }
  }

  public XmlAttributeValue getValueElement() {
    return (XmlAttributeValue)XmlChildRole.ATTRIBUTE_VALUE_FINDER.findChild(this);
  }

  public void setValue(String valueText) throws IncorrectOperationException{
    final ASTNode value = XmlChildRole.ATTRIBUTE_VALUE_FINDER.findChild(this);
    final PomModel model = PomManager.getModel(getProject());
    final ASTNode newValue = XmlChildRole.ATTRIBUTE_VALUE_FINDER.findChild((CompositeElement)JavaPsiFacade
      .getInstance(getManager().getProject()).getElementFactory().createXmlAttribute("a", valueText));
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    model.runTransaction(new PomTransactionBase(this, aspect) {
      public PomModelEvent runInner(){
        final XmlAttributeImpl att = XmlAttributeImpl.this;
        if(value != null){
          if (newValue != null) {
            att.replaceChild(value, newValue.copyElement());
          }
          else {
            att.removeChild(value);
          }
        }
        else {
          if (newValue != null) {
            att.addChild(newValue.copyElement());
          }
        }
        return XmlAttributeSetImpl.createXmlAttributeSet(model, getParent(), getName(), newValue != null ? newValue.getText() : null);
      }
    });
  }

  public XmlElement getNameElement() {
    return (XmlElement)XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(this);
  }

  @NotNull
  public String getNamespace() {
    final String name = getName();
    final String prefixByQualifiedName = XmlUtil.findPrefixByQualifiedName(name);
    // The namespace name for an unprefixed attribute name always has no value. Namespace recommendation section 6.2, third paragraph
    if(prefixByQualifiedName.length() == 0) return XmlUtil.EMPTY_URI;
    return getParent().getNamespaceByPrefix(prefixByQualifiedName);
  }

  @NonNls
  @NotNull
  public String getNamespacePrefix() {
    return XmlUtil.findPrefixByQualifiedName(getName());
  }

  public XmlTag getParent(){
    final PsiElement parentTag = super.getParent();
    return parentTag instanceof XmlTag ? (XmlTag)parentTag : null; // Invalid elements might belong to DummyHolder instead.
  }

  @NotNull
  public String getLocalName() {
    return XmlUtil.findLocalNameByQualifiedName(getName());
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  public String getValue() {
    final XmlAttributeValue valueElement = getValueElement();
    return valueElement != null ? valueElement.getValue() : null;
  }

  private volatile String myDisplayText = null;
  private volatile int[] myGapDisplayStarts = null;
  private volatile int[] myGapPhysicalStarts = null;
  private volatile TextRange myValueTextRange; // text inside quotes, if there are any

  public String getDisplayValue() {
    String displayText = myDisplayText;
    if (displayText != null) return displayText;
    XmlAttributeValue value = getValueElement();
    if (value == null) return null;
    PsiElement firstChild = value.getFirstChild();
    if (firstChild == null) return null;
    ASTNode child = firstChild.getNode();
    myValueTextRange = new TextRange(0, value.getTextLength());
    if (child != null && child.getElementType() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      myValueTextRange = new TextRange(child.getTextLength(), myValueTextRange.getEndOffset());
      child = child.getTreeNext();
    }
    final TIntArrayList gapsStarts = new TIntArrayList();
    final TIntArrayList gapsShifts = new TIntArrayList();
    StringBuilder buffer = new StringBuilder(getTextLength());
    while (child != null) {
      final int start = buffer.length();
      IElementType elementType = child.getElementType();
      if (elementType == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        myValueTextRange = new TextRange(myValueTextRange.getStartOffset(), child.getTextRange().getStartOffset() - value.getTextRange().getStartOffset());
        break;
      }
      if (elementType == XmlTokenType.XML_CHAR_ENTITY_REF) {
        buffer.append(XmlUtil.getCharFromEntityRef(child.getText()));
      }
      else if (elementType == XmlElementType.XML_ENTITY_REF) {
        buffer.append(XmlUtil.getEntityValue((XmlEntityRef)child));
      }
      else {
        buffer.append(child.getText());
      }

      int end = buffer.length();
      int originalLength = child.getTextLength();
      if (end - start != originalLength) {
        gapsStarts.add(start);
        gapsShifts.add(originalLength - (end - start));
      }
      child = child.getTreeNext();
    }
    myGapDisplayStarts = new int[gapsShifts.size()];
    myGapPhysicalStarts = new int[gapsShifts.size()];
    int currentGapsSum = 0;
    for (int i=0; i<myGapDisplayStarts.length;i++) {
      currentGapsSum += gapsShifts.get(i);
      myGapDisplayStarts[i] = gapsStarts.get(i);
      myGapPhysicalStarts[i] = myGapDisplayStarts[i] + currentGapsSum;
    }

    return myDisplayText = buffer.toString();
  }

  public int physicalToDisplay(int physicalIndex) {
    getDisplayValue();
    if (physicalIndex < 0 || physicalIndex > myValueTextRange.getLength()) return -1;
    if (myGapPhysicalStarts.length == 0) return physicalIndex;

    final int bsResult = Arrays.binarySearch(myGapPhysicalStarts, physicalIndex);

    final int gapIndex;
    if(bsResult > 0) gapIndex = bsResult;
    else if(bsResult < -1) gapIndex = -bsResult - 2;
    else gapIndex = -1;

    if(gapIndex < 0) return physicalIndex;
    final int shift = myGapPhysicalStarts[gapIndex] - myGapDisplayStarts[gapIndex];
    return Math.max(myGapDisplayStarts[gapIndex], physicalIndex - shift);
  }

  public int displayToPhysical(int displayIndex) {
    String displayValue = getDisplayValue();
    if (displayValue == null || displayIndex < 0 || displayIndex > displayValue.length()) return -1;
    if (myGapDisplayStarts.length == 0) return displayIndex;

    final int bsResult = Arrays.binarySearch(myGapDisplayStarts, displayIndex);
    final int gapIndex;

    if(bsResult > 0) gapIndex = bsResult - 1;
    else if(bsResult < -1) gapIndex = -bsResult - 2;
    else gapIndex = -1;

    if(gapIndex < 0) return displayIndex;
    final int shift = myGapPhysicalStarts[gapIndex] - myGapDisplayStarts[gapIndex];
    return displayIndex + shift;
  }

  public TextRange getValueTextRange() {
    getDisplayValue();
    return myValueTextRange;
  }

  public void clearCaches() {
    super.clearCaches();
    myDisplayText = null;
    myGapDisplayStarts = null;
    myGapPhysicalStarts = null;
    myValueTextRange = null;
  }

  @NotNull
  public String getName() {
    XmlElement element = getNameElement();
    return element != null ? element.getText() : "";
  }

  public boolean isNamespaceDeclaration() {
    final @NonNls String name = getName();
    return name.startsWith("xmlns:") || name.equals("xmlns");
  }

  public PsiElement setName(@NotNull final String nameText) throws IncorrectOperationException {
    final ASTNode name = XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(this);
    final String oldName = name.getText();
    final PomModel model = PomManager.getModel(getProject());
    final ASTNode newName = XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild((CompositeElement)JavaPsiFacade
      .getInstance(getManager().getProject()).getElementFactory().createXmlAttribute(nameText, ""));
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    model.runTransaction(new PomTransactionBase(getParent(), aspect) {
      public PomModelEvent runInner(){
        final PomModelEvent event = new PomModelEvent(model);
        final XmlAspectChangeSetImpl xmlAspectChangeSet = new XmlAspectChangeSetImpl(model, (XmlFile)getContainingFile());
        xmlAspectChangeSet.add(new XmlAttributeSetImpl(getParent(), oldName, null));
        xmlAspectChangeSet.add(new XmlAttributeSetImpl(getParent(), nameText, getValue()));
        event.registerChangeSet(model.getModelAspect(XmlAspect.class), xmlAspectChangeSet);
        CodeEditUtil.replaceChild(XmlAttributeImpl.this, name, newName);
        return event;
      }
    });
    return this;
  }

  public PsiReference getReference() {
    final PsiReference[] refs = getReferences();
    if (refs.length > 0) return refs[0];
    return null;
  }

  @NotNull
  public PsiReference[] getReferences() {
    final PsiElement parentElement = getParent();
    if (!(parentElement instanceof XmlTag)) return PsiReference.EMPTY_ARRAY;
    final XmlElementDescriptor descr = ((XmlTag)parentElement).getDescriptor();
    if (descr != null){
      return new PsiReference[]{new MyPsiReference(descr)};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  public XmlAttributeDescriptor getDescriptor() {
    final PsiElement parentElement = getParent();
    if (parentElement instanceof XmlDecl) return null;
    final XmlTag tag = (XmlTag)parentElement;
    final XmlElementDescriptor descr = tag.getDescriptor();
    if (descr == null) return null;
    final XmlAttributeDescriptor attributeDescr = descr.getAttributeDescriptor(this);
    return attributeDescr == null ? descr.getAttributeDescriptor(getName(), tag) : attributeDescr;
  }

  private class MyPsiReference implements PsiReference {
    private final XmlElementDescriptor myTagDescr;
    private final XmlAttributeDescriptor myDescr;

    public MyPsiReference(XmlElementDescriptor descr) {
      myTagDescr = descr;
      myDescr = getAttributeDescriptor();
    }

    private XmlAttributeDescriptor getAttributeDescriptor() {
      return myTagDescr.getAttributeDescriptor(XmlAttributeImpl.this);
    }


    public PsiElement getElement() {
      return XmlAttributeImpl.this;
    }

    public TextRange getRangeInElement() {
      final int parentOffset = getNameElement().getStartOffsetInParent();
      return new TextRange(parentOffset, parentOffset + getNameElement().getTextLength());
    }

    public PsiElement resolve() {
      return myDescr != null ? myDescr.getDeclaration() : null;
    }

    public String getCanonicalText() {
      return getName();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      String newName = newElementName;
      if (myDescr instanceof XmlAttributeDescriptorEx) {
        final XmlAttributeDescriptorEx xmlAttributeDescriptorEx = (XmlAttributeDescriptorEx)myDescr;
        final String s = xmlAttributeDescriptorEx.handleTargetRename(newElementName);
        if (s != null) {
          final String prefix = getNamespacePrefix();
          newName = StringUtil.isEmpty(prefix) ? s : prefix + ":" + s;
        }
      }
      return setName(newName);
    }

    // TODO[ik]: namespace support
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      if (element instanceof PsiMetaOwner){
        final PsiMetaOwner owner = (PsiMetaOwner)element;
        if (owner.getMetaData() instanceof XmlElementDescriptor){
          setName(owner.getMetaData().getName());
        }
      }
      throw new IncorrectOperationException("Cant bind to not a xml element definition!");
    }

    public boolean isReferenceTo(PsiElement element) {
      return element.getManager().areElementsEquivalent(element, resolve());
    }

    public Object[] getVariants() {
      final List<LookupElement<String>> variants = new ArrayList<LookupElement<String>>();

      final XmlElementDescriptor parentDescriptor = getParent().getDescriptor();
      if (parentDescriptor != null){
        final XmlTag declarationTag = getParent();
        final XmlAttribute[] attributes = declarationTag.getAttributes();
        XmlAttributeDescriptor[] descriptors = parentDescriptor.getAttributesDescriptors(declarationTag);

        descriptors = HtmlUtil.appendHtmlSpecificAttributeCompletions(declarationTag, descriptors, XmlAttributeImpl.this);

        addVariants(variants, attributes, descriptors);
      }
      return variants.toArray();
    }

    private void addVariants(final Collection<LookupElement<String>> variants, final XmlAttribute[] attributes, final XmlAttributeDescriptor[] descriptors) {
      final XmlTag tag = getParent();
      for (XmlAttributeDescriptor descriptor : descriptors) {
        if (isValidVariant(descriptor, attributes)) {
          final LookupElement<String> element = LookupElementFactory.getInstance().createLookupElement(descriptor.getName(tag));
          if (descriptor instanceof PsiPresentableMetaData) {
            element.setIcon(((PsiPresentableMetaData)descriptor).getIcon());
          }
          variants.add(element);
        }
      }
    }

    private boolean isValidVariant(XmlAttributeDescriptor descriptor, final XmlAttribute[] attributes) {
      if (descriptor instanceof TldAttributeDescriptor && ((TldAttributeDescriptor)descriptor).isIndirectSyntax()) return false;
      for (final XmlAttribute attribute : attributes) {
        if (attribute != XmlAttributeImpl.this && attribute.getName().equals(descriptor.getName())) return false;
      }
      return true;
    }

    public boolean isSoft() {
      return false;
    }
  }

}