package com.intellij.xml.util.documentation;

import com.intellij.codeInsight.javadoc.JavaDocUtil;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.documentation.MetaDataDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlElementDecl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlBundle;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import com.intellij.xml.impl.schema.ComplexTypeDescriptor;
import com.intellij.xml.impl.schema.TypeDescriptor;
import com.intellij.xml.impl.schema.XmlElementDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 25.12.2004
 * Time: 0:00:05
 * To change this template use File | Settings | File Templates.
 */
public class XmlDocumentationProvider implements DocumentationProvider {
  private static final Key<XmlElementDescriptor> DESCRIPTOR_KEY = Key.create("Original element");

  private static final Logger LOG = Logger.getInstance("#com.intellij.xml.util.documentation.XmlDocumentationProvider");

  private final DocumentationProvider myDocumentationProvider = new MetaDataDocumentationProvider();

  @Nullable
  public String getQuickNavigateInfo(PsiElement element) {
    return myDocumentationProvider.getQuickNavigateInfo(element);
  }

  public String getUrlFor(PsiElement element, PsiElement originalElement) {
    if (element instanceof XmlTag) {
      XmlTag tag = (XmlTag)element;

      MyPsiElementProcessor processor = new MyPsiElementProcessor();
      XmlUtil.processXmlElements(tag,processor, true);

      if (processor.url == null) {
        XmlTag declaration = getComplexTypeDefinition(element, originalElement);

        if (declaration != null) {
          XmlUtil.processXmlElements(declaration,processor, true);
        }
      }

      return processor.url;
    }

    return null;
  }

  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof XmlElementDecl) {
      PsiElement curElement = element;

      while(curElement!=null && !(curElement instanceof XmlComment)) {
        curElement = curElement.getPrevSibling();
        if (curElement!=null && curElement.getClass() == element.getClass()) {
          curElement = null; // finding comment fails, we found another similar declaration
          break;
        }
      }

      if (curElement!=null) {
        String text = curElement.getText();
        text = text.substring("<!--".length(),text.length()-"-->".length()).trim();
        return generateDoc(text,((XmlElementDecl)element).getNameElement().getText(),null);
      }
    } else if (element instanceof XmlTag) {
      XmlTag tag = (XmlTag)element;

      MyPsiElementProcessor processor = new MyPsiElementProcessor();
      XmlUtil.processXmlElements(tag,processor, true);
      String name = tag.getAttributeValue("name");
      String typeName = null;

      if (processor.result == null) {
        XmlTag declaration = getComplexTypeDefinition(element, originalElement);

        if (declaration != null) {
          XmlUtil.processXmlElements(declaration,processor, true);
          typeName = declaration.getName();
        }
      }

      return generateDoc(processor.result, name, typeName);
    }

    return null;
  }

  private XmlTag getComplexTypeDefinition(PsiElement element, PsiElement originalElement) {
    XmlElementDescriptor descriptor = element.getUserData(DESCRIPTOR_KEY);

    if (descriptor == null &&
        originalElement != null &&
        originalElement.getParent() instanceof XmlTag) {
      descriptor = ((XmlTag)originalElement.getParent()).getDescriptor();
    }

    if (descriptor instanceof XmlElementDescriptorImpl) {
      TypeDescriptor type = ((XmlElementDescriptorImpl)descriptor).getType();

      if (type instanceof ComplexTypeDescriptor) {
        return ((ComplexTypeDescriptor)type).getDeclaration();
      }
    }

    return null;
  }

  private String generateDoc(String str, String name, String typeName) {
    if (str == null) return null;
    StringBuffer buf = new StringBuffer(str.length() + 20);

    if (typeName==null) {
      JavaDocUtil.formatEntityName(XmlBundle.message("xml.javadoc.tag.name.message"),name,buf);
    } else {
      JavaDocUtil.formatEntityName(XmlBundle.message("xml.javadoc.complex.type.message"),name,buf);
    }

    return buf.append(XmlBundle.message("xml.javadoc.description.message")).append("  ").
      append(HtmlDocumentationProvider.NBSP).append(str).toString();
  }

  public PsiElement getDocumentationElementForLookupItem(final PsiManager psiManager, Object object, PsiElement element) {
    element = PsiTreeUtil.getParentOfType(element, XmlTag.class, false);

    if (element instanceof XmlTag) {
      XmlTag xmlTag = (XmlTag)element;
      XmlElementDescriptor elementDescriptor;

      try {
        @NonNls StringBuffer tagText = new StringBuffer(object.toString());
        String namespacePrefix = XmlUtil.findPrefixByQualifiedName(object.toString());
        String namespace = xmlTag.getNamespaceByPrefix(namespacePrefix);

        if (namespace!=null && namespace.length() > 0) {
          tagText.append(" xmlns");
          if (namespacePrefix.length() > 0) tagText.append(":").append(namespacePrefix);
          tagText.append("=\"").append(namespace).append("\"");
        }

        XmlTag tagFromText = xmlTag.getManager().getElementFactory().createTagFromText("<" + tagText +"/>");
        XmlElementDescriptor parentDescriptor = xmlTag.getDescriptor();
        elementDescriptor = (parentDescriptor!=null)?parentDescriptor.getElementDescriptor(tagFromText):null;

        if (elementDescriptor==null) {
          PsiElement parent = xmlTag.getParent();
          if (parent instanceof XmlTag) {
            parentDescriptor = ((XmlTag)parent).getDescriptor();
            elementDescriptor = (parentDescriptor!=null)?parentDescriptor.getElementDescriptor(tagFromText):null;
          }
        }

        if (elementDescriptor instanceof AnyXmlElementDescriptor) {
          final XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespaceByPrefix(namespacePrefix), true);
          elementDescriptor = (nsDescriptor != null)?nsDescriptor.getElementDescriptor(tagFromText):null;
        }

        // The very special case of xml file
        final PsiFile containingFile = xmlTag.getContainingFile();
        if (PsiUtil.isInJspFile(containingFile)) {
          final XmlTag rootTag = ((XmlFile)containingFile).getDocument().getRootTag();
          if (rootTag != null) {
            final XmlNSDescriptor nsDescriptor = rootTag.getNSDescriptor(rootTag.getNamespaceByPrefix(namespacePrefix), true);
            elementDescriptor = (nsDescriptor != null) ? nsDescriptor.getElementDescriptor(tagFromText) : null;
          }
        }

        if (elementDescriptor != null) {
          PsiElement declaration = elementDescriptor.getDeclaration();
          if (declaration!=null) declaration.putUserData(DESCRIPTOR_KEY,elementDescriptor);
          return declaration;
        }
      }
      catch (IncorrectOperationException e) {
        LOG.error(e);
      }
    }
    return null;
  }

  public PsiElement getDocumentationElementForLink(final PsiManager psiManager, String link, PsiElement context) {
    return null;
  }

  private static class MyPsiElementProcessor implements PsiElementProcessor {
    String result;
    String url;
    @NonNls public static final String DOCUMENTATION_ELEMENT_LOCAL_NAME = "documentation";

    public boolean execute(PsiElement element) {
      if (element instanceof XmlTag &&
          ((XmlTag)element).getLocalName().equals(DOCUMENTATION_ELEMENT_LOCAL_NAME)
      ) {
        final XmlTag tag = ((XmlTag)element);
        result = tag.getValue().getText().trim();
        url = tag.getAttributeValue("source");
        return false;
      }
      return true;
    }

  }
}