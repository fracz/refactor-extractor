package com.intellij.psi.impl.source.xml;

import com.intellij.ant.impl.dom.xmlBridge.AntDOMNSDescriptor;
import com.intellij.j2ee.ExternalResourceManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.pom.PomModel;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.pom.impl.PomTransactionBase;
import com.intellij.pom.xml.XmlAspect;
import com.intellij.pom.xml.impl.events.XmlDocumentChangedImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.impl.meta.MetaRegistry;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.CachedValueImpl;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.util.XmlNSDescriptorSequence;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike
 */
public class XmlDocumentImpl extends XmlElementImpl implements XmlDocument {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.xml.XmlDocumentImpl");

  public XmlDocumentImpl() {
    this(XML_DOCUMENT);
  }

  protected XmlDocumentImpl(IElementType type) {
    super(type);
  }

  public void accept(PsiElementVisitor visitor) {
    visitor.visitXmlDocument(this);
  }

  public int getChildRole(ASTNode child) {
    LOG.assertTrue(child.getTreeParent() == this);
    IElementType i = child.getElementType();
    if (i == XML_PROLOG) {
      return ChildRole.XML_PROLOG;
    }
    else if (i == XML_TAG) {
      return ChildRole.XML_TAG;
    }
    else {
      return ChildRole.NONE;
    }
  }

  public XmlProlog getProlog() {
    return (XmlProlog)findElementByTokenType(XML_PROLOG);
  }

  public XmlTag getRootTag() {
    return (XmlTag)findElementByTokenType(XML_TAG);
  }

  public XmlNSDescriptor getRootTagNSDescriptor() {
    XmlTag rootTag = getRootTag();
    return rootTag != null ? rootTag.getNSDescriptor(rootTag.getNamespace(), false) : null;
  }

  private final Map<String, CachedValue<XmlNSDescriptor>> myDefaultDescriptorsCacheStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>();
  private final Map<String, CachedValue<XmlNSDescriptor>> myDefaultDescriptorsCacheNotStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>();

  public void clearCaches() {
    myDefaultDescriptorsCacheStrict.clear();
    myDefaultDescriptorsCacheNotStrict.clear();
    super.clearCaches();
  }

  public XmlNSDescriptor getDefaultNSDescriptor(final String namespace, final boolean strict) {
    final Map<String, CachedValue<XmlNSDescriptor>> defaultDescriptorsCache;
    if(strict) defaultDescriptorsCache = myDefaultDescriptorsCacheStrict;
    else defaultDescriptorsCache = myDefaultDescriptorsCacheNotStrict;
    if(defaultDescriptorsCache.containsKey(namespace)) return defaultDescriptorsCache.get(namespace).getValue();
    defaultDescriptorsCache.put(namespace, new CachedValueImpl<XmlNSDescriptor>(getManager(), new CachedValueProvider<XmlNSDescriptor>(){
      public Result<XmlNSDescriptor> compute() {
        final XmlNSDescriptor defaultNSDescriptorInner = getDefaultNSDescriptorInner(namespace, strict);
        return new Result<XmlNSDescriptor>(defaultNSDescriptorInner, defaultNSDescriptorInner != null ? defaultNSDescriptorInner.getDependences() : new Object[0]);
      }
    }, false));
    return getDefaultNSDescriptor(namespace, strict);
  }

  public XmlNSDescriptor getDefaultNSDescriptorInner(final String namespace, final boolean strict) {
    final XmlFile containingFile = XmlUtil.getContainingFile(this);
    final XmlDoctype doctype = getProlog().getDoctype();

    if (XmlUtil.ANT_URI.equals(namespace)){
      final AntDOMNSDescriptor antDOMNSDescriptor = new AntDOMNSDescriptor();
      antDOMNSDescriptor.init(this);
      return antDOMNSDescriptor;
    }
    else if(XmlUtil.HTML_URI.equals(namespace)){
      final XmlNSDescriptor xhtmlNSDescriptor = getDefaultNSDescriptor(XmlUtil.XHTML_URI, false);
      final XmlNSDescriptor htmlDescriptor = new HtmlNSDescriptorImpl(xhtmlNSDescriptor);
      return htmlDescriptor;
    }
    else if(namespace != null && namespace != XmlUtil.EMPTY_URI
            && (doctype == null || !namespace.equals(doctype.getDtdUri()))){
      final XmlFile xmlFile = XmlUtil.findXmlFile(containingFile,
                                                  ExternalResourceManager.getInstance().getResourceLocation(namespace));
      if(xmlFile != null){
        final XmlNSDescriptor descriptor = (XmlNSDescriptor)xmlFile.getDocument().getMetaData();
        return descriptor;
      }
    }
    if(strict) return null;

    if (doctype != null){
      XmlNSDescriptor descr = null;
      if (doctype.getMarkupDecl() != null){
        descr = (XmlNSDescriptor)doctype.getMarkupDecl().getMetaData();
      }
      if (doctype.getDtdUri() != null){
        final XmlFile xmlFile = XmlUtil.findXmlFile(containingFile, doctype.getDtdUri());
        final XmlNSDescriptor descr1 = xmlFile == null ? null : (XmlNSDescriptor)xmlFile.getDocument().getMetaData();
        if (descr != null && descr1 != null){
          descr = new XmlNSDescriptorSequence(new XmlNSDescriptor[]{descr, descr1});
        }
        else if (descr1 != null) {
          descr = descr1;
        }
      }

      if(descr != null) return descr;
    }

    try{
      return (XmlNSDescriptor)((XmlFile)getManager().getElementFactory().createFileFromText(
        containingFile.getName() + ".dtd",
        XmlUtil.generateDocumentDTD(this)
      )).getDocument().getMetaData();
    }
    catch(IncorrectOperationException e){
      LOG.error(e);
    }
    return null;
  }

  public PsiElement copy() {
    final XmlDocumentImpl copy = (XmlDocumentImpl)super.copy();
    return copy;
  }

  public PsiMetaData getMetaData() {
    return MetaRegistry.getMeta(this);
  }

  public boolean isMetaEnough() {
    return true;
  }

  public void dumpStatistics(){
    System.out.println("Statistics:");
    final TObjectIntHashMap map = new TObjectIntHashMap();

    final PsiRecursiveElementVisitor psiRecursiveElementVisitor = new PsiRecursiveElementVisitor(){
      public void visitXmlToken(XmlToken token) {
        inc("Tokens");
      }

      public void visitElement(PsiElement element) {
        inc("Elements");
        super.visitElement(element);
      }

      private void inc(final String key) {
        map.put(key, map.get(key) + 1);
      }
    };

    this.accept(psiRecursiveElementVisitor);

    final Object[] keys = map.keys();
    for (int i = 0; i < keys.length; i++) {
      final Object key = keys[i];
      System.out.println(key + ": " + map.get(key));
    }
  }

  public TreeElement addInternal(final TreeElement first, final ASTNode last, final ASTNode anchor, final Boolean before) {
    final PomModel model = getProject().getModel();
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    final TreeElement[] holder = new TreeElement[1];
    try{
      model.runTransaction(new PomTransactionBase(this) {
        public PomModelEvent runInner() throws IncorrectOperationException {
          holder[0] = XmlDocumentImpl.super.addInternal(first, last, anchor, before);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      }, aspect);
    }
    catch(IncorrectOperationException e){}
    return holder[0];
  }

  public void deleteChildInternal(final ASTNode child) {
    final PomModel model = getProject().getModel();
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    try{
      model.runTransaction(new PomTransactionBase(this) {
        public PomModelEvent runInner() throws IncorrectOperationException {
          XmlDocumentImpl.super.deleteChildInternal(child);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      }, aspect);
    }
    catch(IncorrectOperationException e){}
  }

  public void replaceChildInternal(final ASTNode child, final TreeElement newElement) {
    final PomModel model = getProject().getModel();
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    try{
      model.runTransaction(new PomTransactionBase(this) {
        public PomModelEvent runInner() throws IncorrectOperationException {
          XmlDocumentImpl.super.replaceChildInternal(child, newElement);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      }, aspect);
    }
    catch(IncorrectOperationException e){}
  }
}