package com.intellij.psi.impl.source.xml;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.pom.PomManager;
import com.intellij.pom.PomModel;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.pom.impl.PomTransactionBase;
import com.intellij.pom.xml.XmlAspect;
import com.intellij.pom.xml.impl.events.XmlDocumentChangedImpl;
import com.intellij.psi.*;
import com.intellij.psi.impl.CachedValueImpl;
import com.intellij.psi.impl.meta.MetaRegistry;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.util.XmlNSDescriptorSequence;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike
 */
public class XmlDocumentImpl extends XmlElementImpl implements XmlDocument, XmlElementType {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.xml.XmlDocumentImpl");

  public XmlDocumentImpl() {
    this(XML_DOCUMENT);
  }

  protected XmlDocumentImpl(IElementType type) {
    super(type);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlDocument(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  public int getChildRole(ASTNode child) {
    LOG.assertTrue(child.getTreeParent() == this);
    IElementType i = child.getElementType();
    if (i == XML_PROLOG) {
      return XmlChildRole.XML_PROLOG;
    }
    else if (i == XML_TAG) {
      return XmlChildRole.XML_TAG;
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

  private ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>> myDefaultDescriptorsCacheStrict = new ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>>();
  private ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>> myDefaultDescriptorsCacheNotStrict = new ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>>();
  private static final CachedValue NULL = new CachedValueImpl(null,null,false);

  public void clearCaches() {
    myDefaultDescriptorsCacheStrict.clear();
    myDefaultDescriptorsCacheNotStrict.clear();
    super.clearCaches();
  }

  public XmlNSDescriptor getDefaultNSDescriptor(final String namespace, final boolean strict) {
    final ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>> defaultDescriptorsCache;
    if (strict) {
      defaultDescriptorsCache = myDefaultDescriptorsCacheStrict;
    }
    else {
      defaultDescriptorsCache = myDefaultDescriptorsCacheNotStrict;
    }
    if (defaultDescriptorsCache.containsKey(namespace)) {
      final CachedValue<XmlNSDescriptor> cachedValue = defaultDescriptorsCache.get(namespace);
      if (cachedValue != NULL) {
        return cachedValue.getValue();
      }
      else {
        return null;
      }
    }

    final XmlNSDescriptor defaultNSDescriptor;
    try {
      final CachedValueImpl<XmlNSDescriptor> value =
        new CachedValueImpl<XmlNSDescriptor>(getManager(), new CachedValueProvider<XmlNSDescriptor>() {
          public Result<XmlNSDescriptor> compute() {
            final XmlNSDescriptor defaultNSDescriptorInner = getDefaultNSDescriptorInner(namespace, strict);

            if (isGeneratedFromDtd(defaultNSDescriptorInner)) {
              return new Result<XmlNSDescriptor>(defaultNSDescriptorInner, XmlDocumentImpl.this, ExternalResourceManager.getInstance());
            }

            return new Result<XmlNSDescriptor>(defaultNSDescriptorInner, defaultNSDescriptorInner != null
                                                                         ? defaultNSDescriptorInner.getDependences()
                                                                         : ExternalResourceManager.getInstance());
          }
        }, false);
      defaultNSDescriptor = value.getValue();
      defaultDescriptorsCache.put(namespace, value == null ? NULL : value);
    }
    catch (ProcessCanceledException ex) {
      defaultDescriptorsCache.remove(namespace);
      throw ex;
    }
    return defaultNSDescriptor;
  }

  private boolean isGeneratedFromDtd(XmlNSDescriptor defaultNSDescriptorInner) {
    return defaultNSDescriptorInner != null &&
        defaultNSDescriptorInner.getDescriptorFile() != null &&
        defaultNSDescriptorInner.getDescriptorFile().getName().equals(
          XmlUtil.getContainingFile(XmlDocumentImpl.this).getName() + ".dtd"
        );
  }

  public XmlNSDescriptor getDefaultNSDescriptorInner(final String namespace, final boolean strict) {
    final XmlFile containingFile = XmlUtil.getContainingFile(this);
    final XmlProlog prolog = getProlog();
    final XmlDoctype doctype = (prolog != null) ? prolog.getDoctype() : null;
    boolean dtdUriFromDocTypeIsNamespace = false;

    if (XmlUtil.HTML_URI.equals(namespace)) {
      XmlNSDescriptor nsDescriptor = (doctype != null) ? getNsDescriptorFormDocType(doctype, containingFile) : null;
      if (nsDescriptor == null) nsDescriptor = getDefaultNSDescriptor(XmlUtil.XHTML_URI, false);
      return new HtmlNSDescriptorImpl(nsDescriptor);
    }
    else if (namespace != null && namespace != XmlUtil.EMPTY_URI) {
      if (doctype == null || !namespace.equals(doctype.getDtdUri())) {
        boolean documentIsSchemaThatDefinesNs = namespace.equals(XmlUtil.getTargetSchemaNsFromTag(getRootTag()));

        final XmlFile xmlFile = documentIsSchemaThatDefinesNs
                                ? containingFile
                                : XmlUtil.findNamespace(containingFile, ExternalResourceManager.getInstance().getResourceLocation(namespace));
        if (xmlFile != null) {
          return (XmlNSDescriptor)xmlFile.getDocument().getMetaData();
        }
      } else {
        dtdUriFromDocTypeIsNamespace = true;
      }
    }

    if (strict && !dtdUriFromDocTypeIsNamespace) return null;

    if (doctype != null) {
      XmlNSDescriptor descr = getNsDescriptorFormDocType(doctype, containingFile);

      if (descr != null) {
        final String prefix = containingFile.getDocument().getRootTag().getPrefixByNamespace(XmlUtil.FACELETS_URI);

        if (containingFile instanceof JspFile || prefix != null) {
          descr = new HtmlNSDescriptorImpl(descr, true, containingFile.getFileType() == StdFileTypes.JSP);
        }
        return descr;
      }
    }

    if (strict) return null;
    if (namespace == XmlUtil.EMPTY_URI) {
      final XmlFile xmlFile = XmlUtil.findNamespace(containingFile, namespace);
      if (xmlFile != null) {
        return (XmlNSDescriptor)xmlFile.getDocument().getMetaData();
      }
    }
    try {
      final PsiFile fileFromText = PsiFileFactory.getInstance(getManager().getProject())
        .createFileFromText(containingFile.getName() + ".dtd", XmlUtil.generateDocumentDTD(this));
      if (fileFromText instanceof XmlFile) {
        return (XmlNSDescriptor)((XmlFile)fileFromText).getDocument().getMetaData();
      }
    }
    catch (ProcessCanceledException ex) {
      throw ex;
    }
    catch (RuntimeException ex) {
    } // e.g. dtd isn't mapped to xml type

    return null;
  }

  private XmlNSDescriptor getNsDescriptorFormDocType(final XmlDoctype doctype, final XmlFile containingFile) {
    XmlNSDescriptor descr = null;
    if (doctype.getMarkupDecl() != null){
      descr = (XmlNSDescriptor)doctype.getMarkupDecl().getMetaData();
      final XmlElementDescriptor[] rootElementsDescriptors = descr.getRootElementsDescriptors(this);
      if (rootElementsDescriptors.length == 0) descr = null;
    }

    final String dtdUri = doctype.getDtdUri();
    if (dtdUri != null && dtdUri.length() > 0){
      final XmlFile xmlFile = XmlUtil.findNamespace(containingFile, dtdUri);
      final XmlNSDescriptor descr1 = xmlFile == null ? null : (XmlNSDescriptor)xmlFile.getDocument().getMetaData();
      if (descr != null && descr1 != null){
        descr = new XmlNSDescriptorSequence(new XmlNSDescriptor[]{descr, descr1});
      }
      else if (descr1 != null) {
        descr = descr1;
      }
    }
    return descr;
  }

  public Object clone() {
    HashMap<String, CachedValue<XmlNSDescriptor>> cacheStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>(
      myDefaultDescriptorsCacheStrict
    );
    HashMap<String, CachedValue<XmlNSDescriptor>> cacheNotStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>(
      myDefaultDescriptorsCacheNotStrict
    );
    final XmlDocumentImpl copy = (XmlDocumentImpl) super.clone();
    updateSelfDependentDtdDescriptors(copy, cacheStrict, cacheNotStrict);
    return copy;
  }

  public PsiElement copy() {
    HashMap<String, CachedValue<XmlNSDescriptor>> cacheStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>(
      myDefaultDescriptorsCacheStrict
    );
    HashMap<String, CachedValue<XmlNSDescriptor>> cacheNotStrict = new HashMap<String, CachedValue<XmlNSDescriptor>>(
      myDefaultDescriptorsCacheNotStrict
    );
    final XmlDocumentImpl copy = (XmlDocumentImpl)super.copy();
    updateSelfDependentDtdDescriptors(copy, cacheStrict, cacheNotStrict);
    return copy;
  }

  private void updateSelfDependentDtdDescriptors(XmlDocumentImpl copy, HashMap<String,
    CachedValue<XmlNSDescriptor>> cacheStrict, HashMap<String, CachedValue<XmlNSDescriptor>> cacheNotStrict) {
    copy.myDefaultDescriptorsCacheNotStrict = new ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>>();
    copy.myDefaultDescriptorsCacheStrict = new ConcurrentHashMap<String, CachedValue<XmlNSDescriptor>>();

    for(Map.Entry<String, CachedValue<XmlNSDescriptor>> e:cacheStrict.entrySet()) {
      if (e.getValue().hasUpToDateValue()) {
        final XmlNSDescriptor nsDescriptor = e.getValue().getValue();
        if (!isGeneratedFromDtd(nsDescriptor)) copy.myDefaultDescriptorsCacheStrict.put(e.getKey(), e.getValue());
      }
    }

    for(Map.Entry<String, CachedValue<XmlNSDescriptor>> e:cacheNotStrict.entrySet()) {
      if (e.getValue().hasUpToDateValue()) {
        final XmlNSDescriptor nsDescriptor = e.getValue().getValue();
        if (!isGeneratedFromDtd(nsDescriptor)) copy.myDefaultDescriptorsCacheNotStrict.put(e.getKey(), e.getValue());
      }
    }
  }

  public PsiMetaData getMetaData() {
    return MetaRegistry.getMeta(this);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public void dumpStatistics(){
    System.out.println("Statistics:");
    final TObjectIntHashMap<Object> map = new TObjectIntHashMap<Object>();

    final PsiElementVisitor psiRecursiveElementVisitor = new XmlRecursiveElementVisitor(){
      @NonNls private static final String TOKENS_KEY = "Tokens";
      @NonNls private static final String ELEMENTS_KEY = "Elements";

      @Override public void visitXmlToken(XmlToken token) {
        inc(TOKENS_KEY);
      }

      @Override public void visitElement(PsiElement element) {
        inc(ELEMENTS_KEY);
        super.visitElement(element);
      }

      private void inc(final String key) {
        map.put(key, map.get(key) + 1);
      }
    };

    this.accept(psiRecursiveElementVisitor);

    final Object[] keys = map.keys();
    for (final Object key : keys) {
      System.out.println(key + ": " + map.get(key));
    }
  }

  public TreeElement addInternal(final TreeElement first, final ASTNode last, final ASTNode anchor, final Boolean before) {
    final PomModel model = PomManager.getModel(getProject());
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    final TreeElement[] holder = new TreeElement[1];
    try{
      model.runTransaction(new PomTransactionBase(this, aspect) {
        public PomModelEvent runInner() {
          holder[0] = XmlDocumentImpl.super.addInternal(first, last, anchor, before);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      });
    }
    catch(IncorrectOperationException e){}
    return holder[0];
  }

  public void deleteChildInternal(@NotNull final ASTNode child) {
    final PomModel model = PomManager.getModel(getProject());
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    try{
      model.runTransaction(new PomTransactionBase(this, aspect) {
        public PomModelEvent runInner() {
          XmlDocumentImpl.super.deleteChildInternal(child);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      });
    }
    catch(IncorrectOperationException e){}
  }

  public void replaceChildInternal(@NotNull final ASTNode child, @NotNull final TreeElement newElement) {
    final PomModel model = PomManager.getModel(getProject());
    final XmlAspect aspect = model.getModelAspect(XmlAspect.class);
    try{
      model.runTransaction(new PomTransactionBase(this, aspect) {
        public PomModelEvent runInner() {
          XmlDocumentImpl.super.replaceChildInternal(child, newElement);
          return XmlDocumentChangedImpl.createXmlDocumentChanged(model, XmlDocumentImpl.this);
        }
      });
    }
    catch(IncorrectOperationException e){}
  }
}