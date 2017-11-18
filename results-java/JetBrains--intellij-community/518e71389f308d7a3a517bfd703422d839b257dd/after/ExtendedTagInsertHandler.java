package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlExtension;
import com.intellij.xml.XmlSchemaProvider;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author Dmitry Avdeev
*/
class ExtendedTagInsertHandler extends XmlTagInsertHandler {

  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.completion.ExtendedTagInsertHandler");

  protected final String myElementName;
  @Nullable protected final String myNamespace;
  @Nullable protected final String myNamespacePrefix;

  public ExtendedTagInsertHandler(final String elementName, @Nullable final String namespace, @Nullable final String namespacePrefix) {
    myElementName = elementName;
    myNamespace = namespace;
    myNamespacePrefix = namespacePrefix;
  }

  public void handleInsert(final CompletionContext context,
                           final int startOffset,
                           final LookupData data,
                           final LookupItem item,
                           final boolean signatureSelected,
                           final char completionChar) {

    final XmlFile file = (XmlFile)context.file;
    final Project project = context.project;

    final PsiElement psiElement = file.findElementAt(startOffset);
    assert psiElement != null;
    if (isNamespaceBound(psiElement)) {
      doDefault(context, startOffset, data, item, signatureSelected, completionChar);
      return;
    }

    final Editor editor = context.editor;
    final Document document = editor.getDocument();
    PsiDocumentManager.getInstance(project).commitDocument(document);

    final int caretOffset = editor.getCaretModel().getOffset();
    final RangeMarker caretMarker = document.createRangeMarker(caretOffset, caretOffset);

    final XmlExtension.Runner<String, IncorrectOperationException> runAfter =
      new XmlExtension.Runner<String, IncorrectOperationException>() {

        public void run(final String namespacePrefix) {

          PsiDocumentManager.getInstance(project).commitDocument(document);
          final PsiElement element = file.findElementAt(context.getStartOffset());
          if (element != null) {
            qualifyWithPrefix(namespacePrefix, element, document);
            PsiDocumentManager.getInstance(project).commitDocument(document);
          }
          final int offset = context.getStartOffset();
          editor.getCaretModel().moveToOffset(caretMarker.getStartOffset());
          PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
          doDefault(context, offset, data, item, signatureSelected, completionChar);
        }
      };

    try {
      final String prefixByNamespace = getPrefixByNamespace(file);
      if (myNamespacePrefix != null || StringUtil.isEmpty(prefixByNamespace)) {
        final String nsPrefix = myNamespacePrefix == null ? suggestPrefix(file) : myNamespacePrefix;
        XmlExtension.getExtension(file).insertNamespaceDeclaration(file, editor, Collections.singleton(myNamespace), nsPrefix, runAfter);
      } else {
        runAfter.run(prefixByNamespace);    // qualify && complete
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  protected void doDefault(final CompletionContext context, final int startOffset, final LookupData data, final LookupItem item,
                         final boolean signatureSelected,
                         final char completionChar) {
    ExtendedTagInsertHandler.super.handleInsert(context, startOffset, data, item, signatureSelected, completionChar);
  }

  protected boolean isNamespaceBound(PsiElement psiElement) {
    final XmlTag tag = (XmlTag)psiElement.getParent();
    final XmlElementDescriptor tagDescriptor = tag.getDescriptor();
    final String tagNamespace = tag.getNamespace();
    return (tagDescriptor != null && !(tagDescriptor instanceof AnyXmlElementDescriptor) && !StringUtil.isEmpty(tagNamespace));
  }

  @Nullable
  private String getPrefixByNamespace(XmlFile file) {
    final XmlDocument document = file.getDocument();
    assert document != null;
    final XmlTag tag = document.getRootTag();
    return tag == null ? null : tag.getPrefixByNamespace(myNamespace);
  }

  @Nullable
  protected String suggestPrefix(XmlFile file) {
    if (myNamespace == null) {
      return null;
    }
    final XmlSchemaProvider provider = XmlSchemaProvider.getAvailableProvider(file);
    return provider == null ? null : provider.getDefaultPrefix(myNamespace, file);
  }

  protected Set<String> getNamespaces(final XmlFile file) {
    return XmlExtension.getExtension(file).getNamespacesByTagName(myElementName, file);
  }

  protected void qualifyWithPrefix(final String namespacePrefix, final PsiElement element, final Document document) {
    final PsiElement tag = element.getParent();
    if (tag instanceof XmlTag) {
      final String prefix = ((XmlTag)tag).getNamespacePrefix();
      if (!prefix.equals(namespacePrefix)) {
        final String name = namespacePrefix + ":" + ((XmlTag)tag).getLocalName();
        try {
          ((XmlTag)tag).setName(name);
        }
        catch (IncorrectOperationException e) {
          LOG.error(e);
        }
      }
    }
  }
}