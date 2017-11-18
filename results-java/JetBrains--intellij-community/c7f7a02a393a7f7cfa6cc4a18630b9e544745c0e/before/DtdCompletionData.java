/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Oct 19, 2006
 * Time: 9:41:42 PM
 */
package com.intellij.codeInsight.completion;

import com.intellij.psi.xml.*;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.getters.XmlAttributeValueGetter;
import com.intellij.psi.filters.getters.AllWordsGetter;
import com.intellij.psi.filters.position.TokenTypeFilter;
import com.intellij.psi.filters.position.LeftNeighbour;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.macro.MacroFactory;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.XmlElementDescriptorImpl;
import com.intellij.xml.impl.schema.TypeDescriptor;
import com.intellij.xml.impl.schema.ComplexTypeDescriptor;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 05.06.2003
 * Time: 18:55:15
 * To change this template use Options | File Templates.
 */
public class DtdCompletionData extends CompletionData {
  public DtdCompletionData() {
    final LeftNeighbour entityFilter = new LeftNeighbour(new TextFilter("%"));

    declareFinalScope(XmlToken.class);

    {
      final CompletionVariant variant = new CompletionVariant(
        new AndFilter(
          new LeftNeighbour(
            new OrFilter(
              new TextFilter(new String[] {"#", "!", "(", ",", "|", "["}),
              new TokenTypeFilter(XmlTokenType.XML_NAME)
            )
          ),
          new NotFilter(entityFilter)
        )
      );
      variant.includeScopeClass(XmlToken.class, true);
      variant.addCompletion(
        new String[] {
          "#PCDATA","#IMPLIED","#REQUIRED","#FIXED","<!ATTLIST", "<!ELEMENT", "<!NOTATION", "INCLUDE", "IGNORE", "CDATA", "ID" , "IDREF", "EMPTY", "ANY",
          "IDREFS", "ENTITIES", "ENTITY", "<!ENTITY", "NMTOKEN", "NMTOKENS", "SYSTEM", "PUBLIC"
        },
        TailType.NONE
      );
      variant.setInsertHandler(new MyInsertHandler());
      registerVariant(variant);
    }

    {
      final CompletionVariant variant = new CompletionVariant(entityFilter);
      variant.includeScopeClass(XmlToken.class, true);
      variant.addCompletion(new DtdEntityGetter());
      variant.setInsertHandler(new XmlCompletionData.EntityRefInsertHandler());
      registerVariant(variant);
    }
  }

  public String findPrefix(PsiElement insertedElement, int offset) {
    final PsiElement prevLeaf = PsiTreeUtil.prevLeaf(insertedElement);
    final PsiElement prevPrevLeaf = prevLeaf != null ? PsiTreeUtil.prevLeaf(prevLeaf):null;
    String prefix = super.findPrefix(insertedElement, offset);

    if (prevLeaf != null) {
      final String prevLeafText = prevLeaf.getText();

      if("#".equals(prevLeafText)) {
        prefix = "#" + prefix;
      } else if ("!".equals(prevLeafText) && prevPrevLeaf != null && "<".equals(prevPrevLeaf.getText())) {
        prefix = "<!" + prefix;
      }
    }

    return prefix;
  }

  static class DtdEntityGetter implements ContextGetter {

    public Object[] get(final PsiElement context, CompletionContext completionContext) {
      final List<String> results = new LinkedList<String>();

      final PsiElementProcessor processor = new PsiElementProcessor() {
        public boolean execute(final PsiElement element) {
          if (element instanceof XmlEntityDecl) {
            final XmlEntityDecl xmlEntityDecl = (XmlEntityDecl)element;
            if (xmlEntityDecl.isInternalReference()) {
              results.add(xmlEntityDecl.getName());
            }
          }
          return true;
        }
      };

      XmlUtil.processXmlElements((XmlFile)context.getContainingFile().getOriginalFile(), processor, true);
      return results.toArray(new Object[results.size()]);
    }
  }
  static class MyInsertHandler extends BasicInsertHandler {

    public void handleInsert(CompletionContext context, int startOffset, LookupData data, LookupItem item, boolean signatureSelected,
                             char completionChar) {
      super.handleInsert(context, startOffset, data, item, signatureSelected,completionChar);

      if (item.getObject().toString().startsWith("<!")) {
        PsiDocumentManager.getInstance(context.project).commitAllDocuments();

        int caretOffset = context.editor.getCaretModel().getOffset();
        PsiElement tag = PsiTreeUtil.getParentOfType(context.file.findElementAt(caretOffset), PsiNamedElement.class);

        if (tag == null) {
          context.editor.getDocument().insertString(caretOffset, " >");
          context.editor.getCaretModel().moveToOffset(caretOffset + 1);
        }
      }
    }
  }
}