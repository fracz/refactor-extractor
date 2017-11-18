package com.intellij.codeInsight.daemon.impl.analysis;

import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.daemon.quickFix.TagFileQuickFixProvider;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.codeInspection.htmlInspections.XmlEntitiesInspection;
import com.intellij.idea.LoggerFactory;
import com.intellij.jsp.impl.JspElementDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.*;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspDirective;
import com.intellij.psi.impl.source.jsp.jspJava.JspXmlTagBase;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.IdReferenceProvider;
import com.intellij.psi.jsp.JspDirectiveKind;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlTagUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Mike
 */
public class XmlHighlightVisitor extends XmlElementVisitor implements HighlightVisitor, Validator.ValidationHost {
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(
    "com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor"
  );
  public static final Key<String> DO_NOT_VALIDATE_KEY = Key.create("do not validate");
  private List<HighlightInfo> myResult;
  @Nullable private XmlRefCountHolder myRefCountHolder;

  private static boolean ourDoJaxpTesting;

  @NonNls private static final String TAGLIB_DIRECTIVE = "taglib";
  @NonNls private static final String URI_ATT = "uri";
  @NonNls private static final String TAGDIR_ATT = "tagdir";
  @NonNls private static final String IMPORT_ATTR_NAME = "import";
  @NonNls private static final String XML = "xml";

  public List<HighlightInfo> getResult() {
    return myResult;
  }

  private void addElementsForTag(XmlTag tag,
                                 String localizedMessage,
                                 HighlightInfoType type,
                                 IntentionAction quickFixAction) {
    addElementsForTagWithManyQuickFixes(tag, localizedMessage, type, quickFixAction);
  }

  private void addElementsForTagWithManyQuickFixes(XmlTag tag,
                                                   String localizedMessage,
                                                   HighlightInfoType type, IntentionAction... quickFixActions) {
    bindMessageToTag(tag, type, -1, localizedMessage, quickFixActions);
  }

  @Override public void visitXmlToken(XmlToken token) {
    if (token.getTokenType() == XmlTokenType.XML_NAME) {
      PsiElement element = token.getPrevSibling();
      while(element instanceof PsiWhiteSpace) element = element.getPrevSibling();

      if (element instanceof XmlToken && ((XmlToken)element).getTokenType() == XmlTokenType.XML_START_TAG_START) {
        PsiElement parent = element.getParent();

        if (parent instanceof XmlTag && !(token.getNextSibling() instanceof OuterLanguageElement)) {
          XmlTag tag = (XmlTag)parent;
          checkTag(tag);
        }
      }
    }
  }

  private void checkTag(XmlTag tag) {
    if (ourDoJaxpTesting) return;

    if (myResult == null) {
      checkTagByDescriptor(tag);
    }

    if (myResult == null) {
      checkUnboundNamespacePrefix(tag, tag, tag.getNamespacePrefix());
    }

    if (myResult == null) {
      if (tag.getUserData(DO_NOT_VALIDATE_KEY) == null) {
        final XmlElementDescriptor descriptor = tag.getDescriptor();

        if (tag instanceof HtmlTag &&
            ( descriptor instanceof AnyXmlElementDescriptor ||
              descriptor == null
            )
           ) {
          return;
        }

        checkReferences(tag);
      }
    }
  }

  private void checkUnboundNamespacePrefix(final XmlElement element, final XmlTag context, String namespacePrefix) {
    if (namespacePrefix.length() > 0 || ( element instanceof XmlTag && element.getParent() instanceof XmlDocument)) {
      final String namespaceByPrefix = context.getNamespaceByPrefix(namespacePrefix);

      if (namespaceByPrefix.length() == 0) {
        final PsiFile containingFile = context.getContainingFile();
        if (!HighlightLevelUtil.shouldInspect(containingFile)) return;

        if (!XML.equals(namespacePrefix) ) {
          boolean taglibDeclaration = containingFile.getFileType() == StdFileTypes.JSP;

          ProgressManager progressManager = ProgressManager.getInstance();

          // check if there is invalid ns declaration
          if (taglibDeclaration) {
            final XmlTag[] directiveTags = PsiUtil.getJspFile(containingFile).getDirectiveTags(JspDirectiveKind.TAGLIB, false);
            for(XmlTag t:directiveTags) {
              progressManager.checkCanceled();
              if (namespacePrefix.equals(t.getAttributeValue("prefix"))) return;
            }
          } else {
            @NonNls String nsDeclarationAttrName = null;
            for(XmlTag t = context; t != null; t = t.getParentTag()) {
              progressManager.checkCanceled();
              if (t.hasNamespaceDeclarations()) {
                if (nsDeclarationAttrName == null) nsDeclarationAttrName = "xmlns:"+namespacePrefix;
                if (t.getAttributeValue(nsDeclarationAttrName) != null) return;
              }
            }
          }

          final String localizedMessage = XmlErrorMessages.message("unbound.namespace", namespacePrefix);

          if (namespacePrefix.length() == 0) {
            final XmlTag tag = (XmlTag)element;
            if (!XmlUtil.JSP_URI.equals(tag.getNamespace())) {
              addElementsForTag(tag,
                localizedMessage,
                HighlightInfoType.INFORMATION,
                new CreateNSDeclarationIntentionFix(context, namespacePrefix)
              );
            }

            return;
          }

          final boolean error = containingFile.getFileType() == StdFileTypes.JSPX || containingFile.getFileType() == StdFileTypes.XHTML ||
                                containingFile.getFileType() == StdFileTypes.XML;

          final int messageLength = namespacePrefix.length();
          final HighlightInfoType infoType = error ? HighlightInfoType.ERROR:HighlightInfoType.WARNING;

          if (element instanceof XmlTag) {
            bindMessageToTag(
              (XmlTag)element,
              infoType, messageLength,
              localizedMessage, null, new CreateNSDeclarationIntentionFix(context, namespacePrefix)
            );
          } else {
            bindMessageToAstNode(
              element,
              infoType,
              0,
              messageLength,
              localizedMessage);
          }
        }
      }
    }
  }

  private void bindMessageToTag(final XmlTag tag, final HighlightInfoType warning, final int messageLength, final String localizedMessage, IntentionAction... quickFixActions) {
    XmlToken childByRole = XmlTagUtil.getStartTagNameElement(tag);

    bindMessageToAstNode(childByRole, warning, 0, messageLength, localizedMessage, quickFixActions);
    childByRole = XmlTagUtil.getEndTagNameElement(tag);
    bindMessageToAstNode(childByRole, warning, 0, messageLength, localizedMessage, quickFixActions);
  }

  private void bindMessageToAstNode(final PsiElement childByRole,
                                    final HighlightInfoType warning,
                                    final int offset,
                                    int length,
                                    final String localizedMessage, IntentionAction... quickFixActions) {
    if(childByRole != null) {
      final TextRange textRange = childByRole.getTextRange();
      if (length == -1) length = textRange.getLength();
      final int startOffset = textRange.getStartOffset() + offset;

      HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
        warning,
        childByRole, startOffset, startOffset + length,
        localizedMessage, HighlightInfo.htmlEscapeToolTip(localizedMessage)
      );

      if (highlightInfo == null) {
        highlightInfo = HighlightInfo.createHighlightInfo(
          warning,
          new TextRange(startOffset, startOffset + length),
          localizedMessage,
          new TextAttributes() {
            public boolean isEmpty() {
              return false;
            }
          }
        );
      }

      for (final IntentionAction quickFixAction : quickFixActions) {
        if (quickFixAction == null) continue;
        QuickFixAction.registerQuickFixAction(highlightInfo, textRange, quickFixAction, null);
      }
      addToResults(highlightInfo);
    }
  }

  private void checkTagByDescriptor(final XmlTag tag) {
    String name = tag.getName();

    if (tag instanceof JspDirective) {
      checkDirective(name, tag);
    }

    XmlElementDescriptor elementDescriptor = null;

    final PsiElement parent = tag.getParent();
    if (parent instanceof XmlTag) {
      XmlTag parentTag = (XmlTag)parent;
      final XmlElementDescriptor parentDescriptor = parentTag.getDescriptor();

      if (parentDescriptor != null) {
        elementDescriptor = tag instanceof JspDirective ? tag.getDescriptor() : parentDescriptor.getElementDescriptor(tag);
      }

      if (parentDescriptor != null &&
          elementDescriptor == null &&
          parentTag.getUserData(DO_NOT_VALIDATE_KEY) == null &&
          !XmlUtil.tagFromTemplateFramework(tag)
      ) {
        if (tag instanceof HtmlTag) {
          //XmlEntitiesInspection inspection = getInspectionProfile(tag, HtmlStyleLocalInspection.SHORT_NAME);
          //if (inspection != null /*&& isAdditionallyDeclared(inspection.getAdditionalEntries(XmlEntitiesInspection.UNKNOWN_TAG), name)*/) {
            return;
          //}
        }

        addElementsForTag(
          tag,
          XmlErrorMessages.message("element.is.not.allowed.here", name),
          getTagProblemInfoType(tag),
          new CreateNSDeclarationIntentionFix(tag, "")
        );
        return;
      }

      if (elementDescriptor instanceof AnyXmlElementDescriptor ||
          elementDescriptor == null
         ) {
        elementDescriptor = tag.getDescriptor();
      }

      if (elementDescriptor == null) return;
    }
    else {
      //root tag
      elementDescriptor = tag.getDescriptor();

     if (elementDescriptor == null) {
       addElementsForTag(tag, XmlErrorMessages.message("element.must.be.declared", name), HighlightInfoType.WRONG_REF, null);
       return;
      }
    }

    XmlAttributeDescriptor[] attributeDescriptors = elementDescriptor.getAttributesDescriptors(tag);
    Set<String> requiredAttributes = null;

    for (XmlAttributeDescriptor attribute : attributeDescriptors) {
      if (attribute != null && attribute.isRequired()) {
        if (requiredAttributes == null) {
          requiredAttributes = new HashSet<String>();
        }
        requiredAttributes.add(attribute.getName(tag));
      }
    }

    if (requiredAttributes != null) {
      for (final String attrName : requiredAttributes) {
        if (tag.getAttribute(attrName, tag.getNamespace()) == null) {
          if (!(elementDescriptor instanceof JspElementDescriptor) ||
              !((JspElementDescriptor)elementDescriptor).isRequiredAttributeImplicitlyPresent(tag, attrName)
              ) {
            final InsertRequiredAttributeFix insertRequiredAttributeIntention = new InsertRequiredAttributeFix(
                tag, attrName, null);
            final String localizedMessage = XmlErrorMessages.message("element.doesnt.have.required.attribute", name, attrName);
            final InspectionProfile profile = InspectionProjectProfileManager.getInstance(tag.getProject()).getInspectionProfile(tag);
            final LocalInspectionToolWrapper toolWrapper =
              (LocalInspectionToolWrapper)profile.getInspectionTool(RequiredAttributesInspection.SHORT_NAME);
            if (toolWrapper != null) {
              RequiredAttributesInspection inspection = (RequiredAttributesInspection)toolWrapper.getTool();
              reportOneTagProblem(
                tag,
                attrName,
                localizedMessage,
                insertRequiredAttributeIntention,
                HighlightDisplayKey.find(RequiredAttributesInspection.SHORT_NAME),
                inspection,
                XmlEntitiesInspection.NOT_REQUIRED_ATTRIBUTE
              );
            }
          }
        }
      }
    }

    if (elementDescriptor instanceof Validator) {
      ((Validator<XmlTag>)elementDescriptor).validate(tag,this);
    }
  }

  private void reportOneTagProblem(final XmlTag tag,
                                   final String name,
                                   final String localizedMessage,
                                   final IntentionAction basicIntention,
                                   final HighlightDisplayKey key,
                                   final XmlEntitiesInspection inspection,
                                   final int type) {
    boolean htmlTag = false;

    if (tag instanceof HtmlTag) {
      htmlTag = true;
      if(isAdditionallyDeclared(inspection.getAdditionalEntries(type), name)) return;
    }

    final InspectionProfile profile = InspectionProjectProfileManager.getInstance(tag.getProject()).getInspectionProfile(tag);
    final IntentionAction intentionAction = inspection.getIntentionAction(name, type);
    if (htmlTag && profile.isToolEnabled(key)) {
      addElementsForTagWithManyQuickFixes(
        tag,
        localizedMessage,
        SeverityRegistrar.getInstance(tag.getProject()).getHighlightInfoTypeBySeverity(profile.getErrorLevel(key).getSeverity()),
        intentionAction,
        basicIntention);
    } else if (!htmlTag) {
      addElementsForTag(
        tag,
        localizedMessage,
        HighlightInfoType.WRONG_REF,
        basicIntention
      );
    }
  }

  private static boolean isAdditionallyDeclared(final String additional, String name) {
    name = name.toLowerCase();
    if (!additional.contains(name)) return false;

    StringTokenizer tokenizer = new StringTokenizer(additional, ", ");
    while (tokenizer.hasMoreTokens()) {
      if (name.equals(tokenizer.nextToken())) {
        return true;
      }
    }

    return false;
  }

  private void checkDirective(final String name, final XmlTag tag) {
    if (TAGLIB_DIRECTIVE.equals(name)) {
      final String uri = tag.getAttributeValue(URI_ATT);

      if (uri == null) {
        if (tag.getAttributeValue(TAGDIR_ATT) == null) {
          final XmlToken element = XmlTagUtil.getStartTagNameElement(tag);
          assert element != null;
          final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF, element,
            XmlErrorMessages.message("either.uri.or.tagdir.attribute.should.be.specified")
          );

          addToResults(highlightInfo);
          final JspFile jspFile = (JspFile)tag.getContainingFile();
          final JspManager jspManager = JspManager.getInstance(jspFile.getProject());
          if (jspManager != null) {
            QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeFix(
                tag,
                URI_ATT,
                jspManager.getPossibleTldUris(jspFile)
              )
            );
          }

          QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeFix(tag, TAGDIR_ATT,null)
          );
        }
      }
    }
  }

  private static HighlightInfoType getTagProblemInfoType(XmlTag tag) {
    return tag instanceof HtmlTag && XmlUtil.HTML_URI.equals(tag.getNamespace()) ? HighlightInfoType.WARNING : HighlightInfoType.WRONG_REF;
  }

  @Override public void visitXmlAttribute(XmlAttribute attribute) {
    XmlTag tag = attribute.getParent();

    if (attribute.isNamespaceDeclaration()) {
      checkReferences(attribute.getValueElement());
      return;
    }
    final String namespace = attribute.getNamespace();

    if (XmlUtil.XML_SCHEMA_INSTANCE_URI.equals(namespace)) {
      checkReferences(attribute.getValueElement());
      return;
    }

    XmlElementDescriptor elementDescriptor = tag.getDescriptor();
    if (elementDescriptor == null ||
        elementDescriptor instanceof AnyXmlElementDescriptor ||
        ourDoJaxpTesting) {
      return;
    }

    XmlAttributeDescriptor attributeDescriptor = elementDescriptor.getAttributeDescriptor(attribute);

    final String name = attribute.getName();

    checkUnboundNamespacePrefix(attribute, tag, XmlUtil.findPrefixByQualifiedName(name));

    if (attributeDescriptor == null) {
      if (!XmlUtil.attributeFromTemplateFramework(name, tag)) {
        final String localizedMessage = XmlErrorMessages.message("attribute.is.not.allowed.here", name);
        final HighlightInfo highlightInfo = reportAttributeProblem(tag, name, attribute, localizedMessage);
        if (highlightInfo != null) {
          TagFileQuickFixProvider.registerTagFileAttributeReferenceQuickFix(highlightInfo, attribute.getReference());
        }
      }
    }
    else {
      checkDuplicateAttribute(tag, attribute);

      if (tag instanceof HtmlTag &&
          attribute.getValueElement() == null &&
          !HtmlUtil.isSingleHtmlAttribute(name)
         ) {
        final String localizedMessage = XmlErrorMessages.message("empty.attribute.is.not.allowed", name);
        reportAttributeProblem(tag, name, attribute, localizedMessage);
      }
    }
  }

  @Nullable
  private HighlightInfo reportAttributeProblem(final XmlTag tag,
                                               final String localName,
                                               final XmlAttribute attribute,
                                               final String localizedMessage) {

    final RemoveAttributeIntentionFix removeAttributeIntention = new RemoveAttributeIntentionFix(localName,attribute);

    if (tag instanceof HtmlTag) {
      //final InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(tag.getProject()).getInspectionProfile(tag);
      //LocalInspectionToolWrapper toolWrapper =
      //  (LocalInspectionToolWrapper)inspectionProfile.getInspectionTool(HtmlStyleLocalInspection.SHORT_NAME);
      //HtmlStyleLocalInspection inspection = (HtmlStyleLocalInspection)toolWrapper.getTool();
      //if (isAdditionallyDeclared(inspection.getAdditionalEntries(XmlEntitiesInspection.UNKNOWN_ATTRIBUTE), localName)) return null;
      //key = HighlightDisplayKey.find(HtmlStyleLocalInspection.SHORT_NAME);
      //if (!inspectionProfile.isToolEnabled(key)) return null;
      //
      //quickFixes = new IntentionAction[]{inspection.getIntentionAction(localName, XmlEntitiesInspection.UNKNOWN_ATTRIBUTE),
      //                                   removeAttributeIntention};
      //
      //
      //tagProblemInfoType = SeverityRegistrar.getHighlightInfoTypeBySeverity(inspectionProfile.getErrorLevel(key).getSeverity());
    }
    else {
      final HighlightInfoType tagProblemInfoType = HighlightInfoType.WRONG_REF;
      IntentionAction[] quickFixes = new IntentionAction[]{removeAttributeIntention};

      final ASTNode node = SourceTreeToPsiMap.psiElementToTree(attribute);
      assert node != null;
      final ASTNode child = XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(node);
      assert child != null;
      final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
        tagProblemInfoType, child,
        localizedMessage
      );
      addToResults(highlightInfo);

      for (IntentionAction quickFix : quickFixes) {
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFix);
      }

      return highlightInfo;
    }

    return null;
  }

  private void checkDuplicateAttribute(XmlTag tag, final XmlAttribute attribute) {
    if (tag.getUserData(DO_NOT_VALIDATE_KEY) != null) {
      return;
    }

    final XmlAttribute[] attributes = tag.getAttributes();
    final boolean jspDirective = tag instanceof JspDirective;

    ProgressManager progressManager = ProgressManager.getInstance();
    for (XmlAttribute tagAttribute : attributes) {
      progressManager.checkCanceled();
      if (attribute != tagAttribute && Comparing.strEqual(attribute.getName(), tagAttribute.getName())) {
        final String localName = attribute.getLocalName();

        if (jspDirective && IMPORT_ATTR_NAME.equals(localName)) continue; // multiple import attributes are allowed in jsp directive

        HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
          getTagProblemInfoType(tag),
          XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(attribute)),
          XmlErrorMessages.message("duplicate.attribute", localName));
        addToResults(highlightInfo);

        IntentionAction intentionAction = new RemoveAttributeIntentionFix(localName, attribute);

        QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction);
      }
    }
  }

  @Override public void visitXmlDocument(final XmlDocument document) {
    if (document.getLanguage() == StdLanguages.DTD) {
      final PsiMetaData psiMetaData = document.getMetaData();
      if (psiMetaData instanceof Validator) {
        ((Validator<XmlDocument>)psiMetaData).validate(document, this);
      }
    }
  }

  @Override public void visitXmlTag(XmlTag tag) {
  }

  @Override public void visitXmlAttributeValue(XmlAttributeValue value) {
    final PsiElement parent = value.getParent();
    if (!(parent instanceof XmlAttribute)) {
      checkReferences(value);
      return;
    }

    XmlAttribute attribute = (XmlAttribute)parent;

    XmlTag tag = attribute.getParent();

    XmlElementDescriptor elementDescriptor = tag.getDescriptor();
    if (elementDescriptor == null) return;
    XmlAttributeDescriptor attributeDescriptor = elementDescriptor.getAttributeDescriptor(attribute);
    if (attributeDescriptor == null) return;

    if (value.getUserData(DO_NOT_VALIDATE_KEY) == null) {
      String error = attributeDescriptor.validateValue(value, attribute.getValue());

      if (error != null) {
        addToResults(HighlightInfo.createHighlightInfo(
            getTagProblemInfoType(tag),
            value,
            error));
        return;
      }
    }

    PsiReference[] refs = null;
    final XmlRefCountHolder refCountHolder = myRefCountHolder;  // To make sure it doesn't get null in multi-threaded envir.

    if (refCountHolder != null && value.getUserData(DO_NOT_VALIDATE_KEY) == null) {
      if (attributeDescriptor.hasIdType()) {
        if (doAddValueWithIdType(value, refCountHolder, false)) return;
      } else {
        refs = value.getReferences();
        for(PsiReference r:refs) {
          if (r instanceof IdReferenceProvider.GlobalAttributeValueSelfReference) {
            if (doAddValueWithIdType(value, refCountHolder, r.isSoft())) return;
          }
        }
      }
    }

    if (refs == null) refs = value.getReferences();

    doCheckRefs(value, refs);
  }

  private static boolean doAddValueWithIdType(final XmlAttributeValue value,
                                       final XmlRefCountHolder refCountHolder, boolean soft) {
    refCountHolder.registerPossiblyDuplicateElement(value, soft ? Boolean.TRUE: Boolean.FALSE);

    return false;
  }

  private void checkReferences(PsiElement value) {
    if (value == null) return;

    doCheckRefs(value, value.getReferences());
  }

  private void doCheckRefs(final PsiElement value, final PsiReference[] references) {
    ProgressManager progressManager = ProgressManager.getInstance();
    for (final PsiReference reference : references) {
      progressManager.checkCanceled();
      if (reference == null) {
        continue;
      }
      if (!reference.isSoft()) {
        if(hasBadResolve(reference)) {
          String description = getErrorDescription(reference);

          HighlightInfo info = HighlightInfo.createHighlightInfo(
            getTagProblemInfoType(PsiTreeUtil.getParentOfType(value, XmlTag.class)),
            reference.getElement().getTextRange().getStartOffset() + reference.getRangeInElement().getStartOffset(),
            reference.getElement().getTextRange().getStartOffset() + reference.getRangeInElement().getEndOffset(),
            description
          );
          addToResults(info);
          if (reference instanceof QuickFixProvider) ((QuickFixProvider)reference).registerQuickfix(info, reference);
        }
      }
    }
  }

  public static String getErrorDescription(final PsiReference reference) {
    String message;
    if (reference instanceof EmptyResolveMessageProvider) {
      message = ((EmptyResolveMessageProvider)reference).getUnresolvedMessagePattern();
    }
    else {
      message = XmlErrorMessages.message("cannot.resolve.symbol");
    }

    String description;
    try {
      description = MessageFormat.format(message, reference.getCanonicalText());
    } catch(IllegalArgumentException ex) {
      // unresolvedMessage provided by third-party reference contains wrong format string (e.g. {}), tolerate it
      description = message;
      LOG.warn(XmlErrorMessages.message("plugin.reference.message.problem",reference.getClass().getName(),message));
    }
    return description;
  }

  public static boolean hasBadResolve(final PsiReference reference) {
    if (reference instanceof PsiPolyVariantReference) {
      return ((PsiPolyVariantReference)reference).multiResolve(false).length == 0;
    }
    return reference.resolve() == null;
  }

  @Override public void visitXmlDoctype(XmlDoctype xmlDoctype) {
    if (xmlDoctype.getUserData(DO_NOT_VALIDATE_KEY) != null) return;
    checkReferences(xmlDoctype);
  }

  private void addToResults(final HighlightInfo info) {
    if (myResult == null) myResult = new SmartList<HighlightInfo>();
    myResult.add(info);
  }

  public static void setDoJaxpTesting(boolean doJaxpTesting) {
    ourDoJaxpTesting = doJaxpTesting;
  }

  public void addMessage(PsiElement context, String message, int type) {
    if (message != null && message.length() > 0) {
      if (context instanceof XmlTag && !(context instanceof JspXmlTagBase)) {
        addElementsForTag((XmlTag)context, message, type == ERROR ? HighlightInfoType.ERROR : type == WARNING ? HighlightInfoType.WARNING : HighlightInfoType.INFO, null);
      }
      else {
        addToResults(HighlightInfo.createHighlightInfo(HighlightInfoType.WRONG_REF, context, message));
      }
    }
  }

  public static void visitJspElement(OuterLanguageElement text) {
    PsiElement parent = text.getParent();

    if (parent instanceof XmlText) {
      parent = parent.getParent();
    }

    parent.putUserData(DO_NOT_VALIDATE_KEY, "");
  }

  public boolean suitableForFile(final PsiFile file) {
    return file instanceof XmlFile;
  }

  public void visit(final PsiElement element, final HighlightInfoHolder holder) {
    element.accept(this);

    List<HighlightInfo> result = getResult();
    holder.addAll(result);
    myResult = null;
  }

  public boolean init(final boolean updateWholeFile, final PsiFile file) {
    if (!(file instanceof XmlFile))
      return false;
    if (updateWholeFile) {
      final XmlRefCountHolder countHolder = XmlRefCountHolder.getInstance((XmlFile)file);
      countHolder.clear();
      myRefCountHolder = countHolder;
    }
    else {
      myRefCountHolder = null;
    }
    return true;
  }

  public void cleanup(final boolean finishedSuccessfully, final PsiFile file) {
    myResult = null;
  }

  public HighlightVisitor clone() {
    return new XmlHighlightVisitor();
  }

  public static String getUnquotedValue(XmlAttributeValue value, XmlTag tag) {
    String unquotedValue = StringUtil.stripQuotesAroundValue(value.getText());

    if (tag instanceof HtmlTag) {
      unquotedValue = unquotedValue.toLowerCase();
    }

    return unquotedValue;
  }
}