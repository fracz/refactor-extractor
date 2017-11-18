package com.intellij.codeInsight.daemon.impl.analysis;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.RefCountHolder;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.codeInsight.daemon.impl.quickfix.FetchExtResourceAction;
import com.intellij.codeInsight.daemon.impl.quickfix.IgnoreExtResourceAction;
import com.intellij.codeInsight.daemon.impl.quickfix.ManuallySetupExtResourceAction;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.daemon.quickFix.TagFileQuickFixProvider;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.codeInspection.htmlInspections.HtmlStyleLocalInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.codeInspection.htmlInspections.XmlEntitiesInspection;
import com.intellij.ide.util.FQNameCellRenderer;
import com.intellij.idea.LoggerFactory;
import com.intellij.j2ee.openapi.ex.ExternalResourceManagerEx;
import com.intellij.jsp.impl.JspElementDescriptor;
import com.intellij.jsp.impl.JspNsDescriptor;
import com.intellij.jsp.impl.TldDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.cache.impl.idCache.IdTableBuilding;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspDirective;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.URIReferenceProvider;
import com.intellij.psi.jsp.JspDirectiveKind;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.meta.PsiMetaDataBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Mike
 */
public class XmlHighlightVisitor extends PsiElementVisitor implements Validator.ValidationHost {
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(
    "com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor"
  );
  public static final Key<String> DO_NOT_VALIDATE_KEY = Key.create("do not validate");
  private List<HighlightInfo> myResult;
  private RefCountHolder myRefCountHolder;

  private static boolean ourDoJaxpTesting;

  private static final @NonNls String AMP_ENTITY = "&amp;";
  private static final @NonNls String TAGLIB_DIRECTIVE = "taglib";
  private static final @NonNls String URI_ATT = "uri";
  private static final @NonNls String TAGDIR_ATT = "tagdir";
  private static final @NonNls String LOCATION_ATT_SUFFIX = "Location";
  @NonNls private static final String IMPORT_ATTR_NAME = "import";

  public void setRefCountHolder(RefCountHolder refCountHolder) {
    myRefCountHolder = refCountHolder;
  }

  public List<HighlightInfo> getResult() {
    return myResult;
  }

  public void clearResult() {
    myResult = null;
  }

  private void addElementsForTag(XmlTag tag,
                                 String localizedMessage,
                                 HighlightInfoType type,
                                 IntentionAction quickFixAction) {
    addElementsForTagWithManyQuickFixes(tag, localizedMessage, type, null, quickFixAction);
  }

  private void addElementsForTagWithManyQuickFixes(XmlTag tag,
                                                   String localizedMessage,
                                                   HighlightInfoType type,
                                                   HighlightDisplayKey key,
                                                   IntentionAction... quickFixActions) {
    bindMessageToTag(tag, type,  0, tag.getName().length(), localizedMessage, key, quickFixActions);
  }

  public void visitXmlToken(XmlToken token) {
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

    if (!checkTagIsClosed(tag)) return;

    if (!(tag.getParent() instanceof XmlTag)) {
      checkRootTag(tag);
    }

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
          final String name = tag.getName();
          XmlEntitiesInspection inspection = getInspectionProfile(tag, HtmlStyleLocalInspection.SHORT_NAME);

          reportOneTagProblem(
            tag,
            name,
            XmlErrorMessages.message("unknown.html.tag", name),
            null,
            HighlightDisplayKey.find(HtmlStyleLocalInspection.SHORT_NAME),
            inspection,
            XmlEntitiesInspection.UNKNOWN_TAG
          );

          return;
        }

        checkReferences(tag, QuickFixProvider.NULL);
      }
    }
  }

  private static XmlEntitiesInspection getInspectionProfile(final XmlTag tag, final String inspectionName) {
    final PsiFile psiFile = tag.getContainingFile();
    InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(psiFile.getProject()).getInspectionProfile(psiFile);
    XmlEntitiesInspection inspection = (XmlEntitiesInspection)((LocalInspectionToolWrapper)inspectionProfile.getInspectionTool(
      inspectionName)
    ).getTool();
    return inspection;
  }

  private void checkUnboundNamespacePrefix(final XmlElement element, final XmlTag context, String namespacePrefix) {
    if (namespacePrefix.length() > 0) {
      final String namespaceByPrefix = context.getNamespaceByPrefix(namespacePrefix);

      if (namespaceByPrefix.length() == 0) {
        final PsiFile containingFile = context.getContainingFile();
        if (!HighlightUtil.shouldInspect(containingFile)) return;

        if (!"xml".equals(namespacePrefix) ) {
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

          final boolean error = containingFile.getFileType() == StdFileTypes.JSPX || containingFile.getFileType() == StdFileTypes.XHTML ||
                                containingFile.getFileType() == StdFileTypes.XML;
          final String localizedMessage = XmlErrorMessages.message("unbound.namespace", namespacePrefix);
          final int messageLength = namespacePrefix.length();
          final HighlightInfoType infoType = error ? HighlightInfoType.ERROR:HighlightInfoType.WARNING;

          if (element instanceof XmlTag) {
            bindMessageToTag(
              (XmlTag)element,
              infoType,
              0,
              messageLength,
              localizedMessage, null, new CreateNSDeclarationIntentionAction(context, namespacePrefix,taglibDeclaration)
            );
          } else {
            bindMessageToAstNode(
              element.getNode(),
              infoType,
              0,
              messageLength,
              localizedMessage, null, new CreateNSDeclarationIntentionAction(element, namespacePrefix,false)
            );
          }
        }
      }
    }
  }

  private void bindMessageToTag(final XmlTag tag, final HighlightInfoType warning, final int offset,
                                final int messageLength, final String localizedMessage, final HighlightDisplayKey key, IntentionAction... quickFixActions) {
    ASTNode tagElement = SourceTreeToPsiMap.psiElementToTree(tag);
    ASTNode childByRole = XmlChildRole.START_TAG_NAME_FINDER.findChild(tagElement);

    bindMessageToAstNode(childByRole, warning, offset, messageLength, localizedMessage, key, quickFixActions);
    childByRole = XmlChildRole.CLOSING_TAG_NAME_FINDER.findChild(tagElement);
    bindMessageToAstNode(childByRole, warning, offset, messageLength, localizedMessage, key, quickFixActions);
  }

  private void bindMessageToAstNode(final ASTNode childByRole,
                                    final HighlightInfoType warning,
                                    final int offset,
                                    final int length,
                                    final String localizedMessage, final HighlightDisplayKey key, IntentionAction... quickFixActions) {
    if(childByRole != null) {
      final TextRange textRange = childByRole.getTextRange();
      final int startOffset = textRange.getStartOffset() + offset;

      HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
        warning,
        new TextRange(startOffset,startOffset + length),
        localizedMessage
      );

      for (final IntentionAction quickFixAction : quickFixActions) {
        if (quickFixAction == null) continue;
        List<IntentionAction> options = null;
        if (key != null) {
          options = new ArrayList<IntentionAction>();
          options.add(new EditInspectionToolsSettingsAction(key));
        }
        QuickFixAction.registerQuickFixAction(highlightInfo, textRange, quickFixAction, options, HighlightDisplayKey.getDisplayNameByKey(key));
      }
      addToResults(highlightInfo);
    }
  }

  public static void registerXmlErrorQuickFix(final PsiErrorElement element, final HighlightInfo highlightInfo) {
    final String text = element.getErrorDescription();
    if (text != null && text.startsWith(XmlErrorMessages.message("unescaped.ampersand"))) {
      QuickFixAction.registerQuickFixAction(highlightInfo, new IntentionAction() {
        @NotNull
        public String getText() {
          return XmlErrorMessages.message("escape.ampersand.quickfix");
        }

        @NotNull
        public String getFamilyName() {
          return getText();
        }

        public boolean isAvailable(Project project, Editor editor, PsiFile file) {
          return true;
        }

        public void invoke(Project project, Editor editor, PsiFile file) {
          if (!CodeInsightUtil.prepareFileForWrite(file)) return;
          final int textOffset = element.getTextOffset();
          editor.getDocument().replaceString(textOffset,textOffset + 1,AMP_ENTITY);
        }

        public boolean startInWriteAction() {
          return true;
        }
      });
    }
  }

  static class RenameTagBeginOrEndIntentionAction implements IntentionAction {
    private boolean myStart;
    private XmlTag myTagToChange;
    private String myName;

    RenameTagBeginOrEndIntentionAction(XmlTag tagToChange, String name, boolean start) {
      myStart = start;
      myTagToChange = tagToChange;
      myName = name;
    }

    @NotNull
    public String getText() {
      return myStart ? XmlErrorMessages.message("rename.start.tag.name.intention") : XmlErrorMessages.message("rename.end.tag.name.intention");
    }

    @NotNull
    public String getFamilyName() {
      return getText();
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      if (!CodeInsightUtil.prepareFileForWrite(file)) return;
      myTagToChange.setName(myName);
    }

    public boolean startInWriteAction() {
      return true;
    }
  }

  private boolean checkTagIsClosed(XmlTag tag) {
    PsiElement[] children = tag.getChildren();
    String name = tag.getName();

    boolean insideEndTag = false;
    XmlToken startTagNameToken = null;

    ProgressManager progressManager = ProgressManager.getInstance();
    for (PsiElement child : children) {
      progressManager.checkCanceled();
      if (child instanceof XmlToken) {
        final XmlToken xmlToken = (XmlToken)child;
        if (xmlToken.getTokenType() == XmlTokenType.XML_EMPTY_ELEMENT_END) return true;
        if (xmlToken.getTokenType() == XmlTokenType.XML_END_TAG_START) {
          insideEndTag = true;
        }

        if (xmlToken.getTokenType() == XmlTokenType.XML_NAME) {
          if (insideEndTag) {
            String text = xmlToken.getText();
            if (tag instanceof HtmlTag) {
              text = text.toLowerCase();
              name = name.toLowerCase();
            }

            boolean isExtraHtmlTagEnd = false;
            if (text.equals(name)) {
              isExtraHtmlTagEnd = tag instanceof HtmlTag && HtmlUtil.isSingleHtmlTag(name);
              if (!isExtraHtmlTagEnd) return true;
            }

            HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
              isExtraHtmlTagEnd ? HighlightInfoType.WARNING : HighlightInfoType.ERROR,
              xmlToken,
              isExtraHtmlTagEnd ? XmlErrorMessages.message("extra.closing.tag.for.empty.element") : XmlErrorMessages.message("wrong.closing.tag.name"));
            addToResults(highlightInfo);

            if (isExtraHtmlTagEnd) {
              QuickFixAction.registerQuickFixAction(highlightInfo, new RemoveExtraClosingTagIntentionAction(xmlToken));
            } else {
              IntentionAction intentionAction = new RenameTagBeginOrEndIntentionAction(tag, name, false);
              IntentionAction intentionAction2 = new RenameTagBeginOrEndIntentionAction(tag, text, true);

              QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction);
              QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction2);

              final ASTNode endOfTagStart = XmlChildRole.START_TAG_END_FINDER.findChild(tag.getNode());
              final ASTNode startOfTagStart = XmlChildRole.START_TAG_START_FINDER.findChild(tag.getNode());
              TextRange rangeForActionInStartTagName;
              if (endOfTagStart != null && startOfTagStart != null) {
                rangeForActionInStartTagName =
                  new TextRange(startOfTagStart.getStartOffset() + startOfTagStart.getTextLength(), endOfTagStart.getStartOffset());
              }
              else {
                rangeForActionInStartTagName = startTagNameToken.getTextRange();
              }

              QuickFixAction.registerQuickFixAction(highlightInfo, rangeForActionInStartTagName, intentionAction, null, null);
              QuickFixAction.registerQuickFixAction(highlightInfo, rangeForActionInStartTagName, intentionAction2, null, null);
            }

            return false;
          }
          else {
            startTagNameToken = xmlToken;
          }
        }
      }
    }

    return tag instanceof HtmlTag &&
           (HtmlUtil.isOptionalEndForHtmlTag(name) ||
            HtmlUtil.isSingleHtmlTag(name)
           );
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
          XmlEntitiesInspection inspection = getInspectionProfile(tag, HtmlStyleLocalInspection.SHORT_NAME);
          if (inspection != null /*&& isAdditionallyDeclared(inspection.getAdditionalEntries(XmlEntitiesInspection.UNKNOWN_TAG), name)*/) {
            return;
          }
        }

        addElementsForTag(
          tag,
          XmlErrorMessages.message("element.is.not.allowed.here", name),
          getTagProblemInfoType(tag),
          null
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

    XmlAttributeDescriptor[] attributeDescriptors = elementDescriptor.getAttributesDescriptors();
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
            final InsertRequiredAttributeIntention insertRequiredAttributeIntention = new InsertRequiredAttributeIntention(
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
      ((Validator)elementDescriptor).validate(tag,this);
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
        SeverityRegistrar.getHighlightInfoTypeBySeverity(profile.getErrorLevel(key).getSeverity()),
        key,
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
    if (additional.indexOf(name) == -1) return false;

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
          final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF,
            XmlChildRole.START_TAG_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(tag)),
            XmlErrorMessages.message("either.uri.or.tagdir.attribute.should.be.specified")
          );

          addToResults(highlightInfo);
          final JspFile jspFile = (JspFile)tag.getContainingFile();
          QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeIntention(
              tag,
              URI_ATT,
              JspManager.getInstance(jspFile.getProject()).getPossibleTldUris(jspFile)
            )
          );

          QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeIntention(tag, TAGDIR_ATT,null)
          );
        }
      }
    }
  }

  private static HighlightInfoType getTagProblemInfoType(XmlTag tag) {
    return tag instanceof HtmlTag && XmlUtil.HTML_URI.equals(tag.getNamespace()) ? HighlightInfoType.WARNING : HighlightInfoType.WRONG_REF;
  }

  private void checkRootTag(XmlTag tag) {
    XmlFile file = (XmlFile)tag.getContainingFile();

    XmlProlog prolog = file.getDocument().getProlog();

    if (prolog == null || prolog.getUserData(DO_NOT_VALIDATE_KEY) != null) {
      return;
    }

    XmlDoctype doctype = prolog.getDoctype();

    if (doctype == null) {
      return;
    }

    XmlElement nameElement = doctype.getNameElement();

    if (nameElement == null) {
      return;
    }

    String name = tag.getName();
    String text = nameElement.getText();
    if (tag instanceof HtmlTag) {
      name = name.toLowerCase();
      text = text.toLowerCase();
    }

    if (!name.equals(text)) {
      name = XmlUtil.findLocalNameByQualifiedName(name);

      if (!name.equals(text)) {
        addElementsForTag(tag, XmlErrorMessages.message("wrong.root.element"), HighlightInfoType.WRONG_REF, null);
      }
    }
  }

  public void visitXmlAttribute(XmlAttribute attribute) {
    XmlTag tag = attribute.getParent();

    if (attribute.isNamespaceDeclaration()) {
      checkNamespaceAttribute(attribute);
      return;
    } else {
      final String namespace = attribute.getNamespace();

      if (XmlUtil.XML_SCHEMA_INSTANCE_URI.equals(namespace)) {
        if (attribute.getName().endsWith(LOCATION_ATT_SUFFIX)) {
          checkSchemaLocationAttribute(attribute);
        } else {
          if(attribute.getValueElement() != null) {
            checkReferences(attribute.getValueElement(), QuickFixProvider.NULL);
          }
        }
        return;
      }
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
        TagFileQuickFixProvider.registerTagFileAttributeReferenceQuickFix(highlightInfo, attribute.getReference());
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

  private HighlightInfo reportAttributeProblem(final XmlTag tag,
                                               final String localName,
                                               final XmlAttribute attribute,
                                               final String localizedMessage) {
    final HighlightInfoType tagProblemInfoType;
    IntentionAction[] quickFixes;
    HighlightDisplayKey key = null;

    final RemoveAttributeIntentionAction removeAttributeIntention = new RemoveAttributeIntentionAction(localName,attribute);

    if (tag instanceof HtmlTag) {
      final InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(tag.getProject()).getInspectionProfile(tag);
      LocalInspectionToolWrapper toolWrapper =
        (LocalInspectionToolWrapper)inspectionProfile.getInspectionTool(HtmlStyleLocalInspection.SHORT_NAME);
      HtmlStyleLocalInspection inspection = (HtmlStyleLocalInspection)toolWrapper.getTool();
      if (isAdditionallyDeclared(inspection.getAdditionalEntries(XmlEntitiesInspection.UNKNOWN_ATTRIBUTE), localName)) return null;
      key = HighlightDisplayKey.find(HtmlStyleLocalInspection.SHORT_NAME);
      if (!inspectionProfile.isToolEnabled(key)) return null;

      quickFixes = new IntentionAction[]{inspection.getIntentionAction(localName, XmlEntitiesInspection.UNKNOWN_ATTRIBUTE),
                                         removeAttributeIntention};


      tagProblemInfoType = SeverityRegistrar.getHighlightInfoTypeBySeverity(inspectionProfile.getErrorLevel(key).getSeverity());
    }
    else {
      tagProblemInfoType = HighlightInfoType.WRONG_REF;
      quickFixes = new IntentionAction[]{removeAttributeIntention};
    }

    final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
      tagProblemInfoType,
      XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(attribute)),
      localizedMessage
    );
    addToResults(highlightInfo);

    for (IntentionAction quickFix : quickFixes) {
      if (key != null) {
        List<IntentionAction> options = new ArrayList<IntentionAction>();
        options.add(new EditInspectionToolsSettingsAction(key));
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFix, options, HighlightDisplayKey.getDisplayNameByKey(key));
      } else {
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFix);
      }
    }

    return highlightInfo;
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

        IntentionAction intentionAction = new RemoveAttributeIntentionAction(localName, attribute);

        QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction);
      }
    }
  }

  public void visitXmlElement(XmlElement element) {
    if (element instanceof XmlEntityRef) {
      checkReferences(element, QuickFixProvider.NULL);
    }
  }

  public void visitXmlTag(XmlTag tag) {
  }

  public void visitXmlAttributeValue(XmlAttributeValue value) {
    final PsiElement parent = value.getParent();
    if (!(parent instanceof XmlAttribute)) {
      checkReferences(value, QuickFixProvider.NULL);
      return;
    }

    XmlAttribute attribute = (XmlAttribute)parent;
    if (value.getUserData(DO_NOT_VALIDATE_KEY) != null) {
      return;
    }

    XmlTag tag = attribute.getParent();

    XmlElementDescriptor elementDescriptor = tag.getDescriptor();
    if (elementDescriptor == null) return;
    XmlAttributeDescriptor attributeDescriptor = elementDescriptor.getAttributeDescriptor(attribute);
    if (attributeDescriptor == null) return;

    String error = attributeDescriptor.validateValue(value, attribute.getValue());

    if (error != null) {
      addToResults(HighlightInfo.createHighlightInfo(
          getTagProblemInfoType(tag),
          value,
          error));
      return;
    }

    final RefCountHolder refCountHolder = myRefCountHolder;  // To make sure it doesn't get null in multi-threaded envir.
    if (refCountHolder != null &&
        attributeDescriptor.hasIdType()
      ) {
      final String unquotedValue = getUnquotedValue(value, tag);

      if (XmlUtil.isSimpleXmlAttributeValue(unquotedValue)) {
        final XmlAttribute attributeById = refCountHolder.getAttributeById(unquotedValue);

        if (attributeById == null ||
            !attributeById.isValid() ||
            attributeById == attribute
           ) {
          refCountHolder.registerAttributeWithId(unquotedValue,attribute);
        } else {
          final XmlAttributeValue valueElement = attributeById.getValueElement();

          if (valueElement != null && getUnquotedValue(valueElement, tag).equals(unquotedValue)) {
            if (tag.getParent().getUserData(DO_NOT_VALIDATE_KEY) == null) {
              addToResults(HighlightInfo.createHighlightInfo(
                HighlightInfoType.WRONG_REF,
                value,
                XmlErrorMessages.message("duplicate.id.reference")));
              addToResults(HighlightInfo.createHighlightInfo(
                HighlightInfoType.WRONG_REF,
                valueElement,
                XmlErrorMessages.message("duplicate.id.reference")));
            }
            return;
          } else {
            // attributeById previously has that id so reregister new one
            refCountHolder.registerAttributeWithId(unquotedValue,attribute);
          }
        }
      }
    }

    QuickFixProvider quickFixProvider = attributeDescriptor instanceof QuickFixProvider ?
                                        (QuickFixProvider)attributeDescriptor : QuickFixProvider.NULL;

    checkReferences(value, quickFixProvider);
  }

  public static HighlightInfo checkIdRefAttrValue(XmlAttributeValue value, RefCountHolder holder) {
    if (!(value.getParent() instanceof XmlAttribute) || holder==null) return null;
    XmlAttribute attribute = (XmlAttribute)value.getParent();

    XmlTag tag = attribute.getParent();

    XmlElementDescriptor elementDescriptor = tag.getDescriptor();
    if (elementDescriptor == null) return null;
    XmlAttributeDescriptor attributeDescriptor = elementDescriptor.getAttributeDescriptor(attribute);
    if (attributeDescriptor == null) return null;

    if (attributeDescriptor.hasIdRefType() &&
        tag.getParent().getUserData(DO_NOT_VALIDATE_KEY) == null
       ) {
      String unquotedValue = getUnquotedValue(value, tag);
      if (XmlUtil.isSimpleXmlAttributeValue(unquotedValue)) {
        XmlAttribute xmlAttribute = holder.getAttributeById(unquotedValue);

        if (xmlAttribute == null || !xmlAttribute.isValid()) {
          return HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF,
            value,
            XmlErrorMessages.message("invalid.id.reference")
          );
        }
      }
    }

    return null;
  }

  private static String getUnquotedValue(XmlAttributeValue value, XmlTag tag) {
    String unquotedValue = StringUtil.stripQuotesAroundValue(value.getText());

    if (tag instanceof HtmlTag) {
      unquotedValue = unquotedValue.toLowerCase();
    }

    return unquotedValue;
  }

  private void checkReferences(PsiElement value, QuickFixProvider quickFixProvider) {
    PsiReference[] references = value.getReferences();

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
          quickFixProvider.registerQuickfix(info, reference);
          if (reference instanceof QuickFixProvider) ((QuickFixProvider)reference).registerQuickfix(info, reference);
        }
      }
      if(reference instanceof PsiJavaReference && myRefCountHolder != null){
        final PsiJavaReference psiJavaReference = (PsiJavaReference)reference;
        myRefCountHolder.registerReference(psiJavaReference, psiJavaReference.advancedResolve(false));
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

  public void visitXmlDoctype(XmlDoctype xmlDoctype) {
    if (xmlDoctype.getUserData(DO_NOT_VALIDATE_KEY) != null) return;
    final PsiReference[] references = xmlDoctype.getReferences();
    if (references.length > 0 && references[0] instanceof URIReferenceProvider.URLReference) {
      checkUriReferenceProblem(references[0]);
    }
  }

  private void addToResults(final HighlightInfo info) {
    if (myResult == null) myResult = new SmartList<HighlightInfo>();
    myResult.add(info);
  }

  public void visitReferenceExpression(PsiReferenceExpression expression) {
    visitExpression(expression);
  }

  private void checkNamespaceAttribute(XmlAttribute attribute) {
    if (!attribute.isNamespaceDeclaration()) return;

    XmlAttributeValue element = attribute.getValueElement();
    if(element == null) return;
    final PsiReference[] references = element.getReferences();

    if (references.length > 0 && references[references.length - 1] instanceof URIReferenceProvider.URLReference) {
      checkUriReferenceProblem(references[references.length - 1]);
    }

    checkReferences(element, QuickFixProvider.NULL);
  }

  private void checkUriReferenceProblem(final PsiReference reference) {
    if (reference.resolve() == null) {
      final TextRange textRange = reference.getElement().getTextRange();
      final TextRange referenceRange = reference.getRangeInElement();
      int start = textRange.getStartOffset() + referenceRange.getStartOffset();
      int end = textRange.getStartOffset() + referenceRange.getEndOffset();

      reportURIProblem(start,end);
    }
  }

  private void checkSchemaLocationAttribute(XmlAttribute attribute) {
    if(attribute.getValueElement() == null) return;
    String location = attribute.getValue();

    if (attribute.getLocalName().equals(XmlUtil.NO_NAMESPACE_SCHEMA_LOCATION_ATT)) {
      if(ExternalResourceManagerEx.getInstanceEx().isIgnoredResource(location)) return;

      if(XmlUtil.findXmlFile(attribute.getContainingFile(),location) == null) {
        int start = attribute.getValueElement().getTextOffset();
        reportURIProblem(start,start + location.length());
      }
    } else if (attribute.getLocalName().equals(XmlUtil.SCHEMA_LOCATION_ATT)) {
      StringTokenizer tokenizer = new StringTokenizer(location);
      XmlFile file = null;
      final ExternalResourceManagerEx externalResourceManager = ExternalResourceManagerEx.getInstanceEx();

      while(tokenizer.hasMoreElements()) {
        final String namespace = tokenizer.nextToken(); // skip namespace
        if (!tokenizer.hasMoreElements()) return;
        String url = tokenizer.nextToken();

        if(externalResourceManager.isIgnoredResource(url)) continue;
        if (file == null) {
          file = (XmlFile)attribute.getContainingFile();
        }

        if(XmlUtil.findXmlFile(file,url) == null &&
           externalResourceManager.getResourceLocation(namespace).equals(namespace)
          ) {
          int start = attribute.getValueElement().getTextOffset() + location.indexOf(url);
          reportURIProblem(start,start+url.length());
        }
      }
    }
  }

  private void reportURIProblem(int start, int end) { // report the problem
    if (start > end) {
      end = start;
    }
    HighlightInfo info = HighlightInfo.createHighlightInfo(
      HighlightInfoType.WRONG_REF,
      start,
      end,
      XmlErrorMessages.message("uri.is.not.registered"));
    QuickFixAction.registerQuickFixAction(info, new FetchExtResourceAction());
    QuickFixAction.registerQuickFixAction(info, new ManuallySetupExtResourceAction());
    QuickFixAction.registerQuickFixAction(info, new IgnoreExtResourceAction());
    addToResults(info);
  }

  public static void setDoJaxpTesting(boolean doJaxpTesting) {
    ourDoJaxpTesting = doJaxpTesting;
  }

  public void addMessage(PsiElement context, String message, int type) {
    if (message != null && message.length() > 0) {
      if (context instanceof XmlTag) {
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

  private static class RemoveAttributeIntentionAction implements IntentionAction {
    private final String myLocalName;
    private final XmlAttribute myAttribute;

    public RemoveAttributeIntentionAction(final String localName, final XmlAttribute attribute) {
      myLocalName = localName;
      myAttribute = attribute;
    }

    @NotNull
    public String getText() {
      return XmlErrorMessages.message("remove.attribute.quickfix.text", myLocalName);
    }

    @NotNull
    public String getFamilyName() {
      return XmlErrorMessages.message("remove.attribute.quickfix.family");
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      if (!CodeInsightUtil.prepareFileForWrite(file)) return;
      PsiElement next = findNextAttribute(myAttribute);
      myAttribute.delete();

      if (next != null) {
        editor.getCaretModel().moveToOffset(next.getTextRange().getStartOffset());
      }
    }

    private static PsiElement findNextAttribute(final XmlAttribute attribute) {
      PsiElement nextSibling = attribute.getNextSibling();
      while (nextSibling != null) {
        if (nextSibling instanceof XmlAttribute) return nextSibling;
        nextSibling =  nextSibling.getNextSibling();
      }
      return null;
    }

    public boolean startInWriteAction() {
      return true;
    }
  }

  private static class RemoveExtraClosingTagIntentionAction implements IntentionAction {
    private final XmlToken myXmlToken;

    public RemoveExtraClosingTagIntentionAction(final XmlToken xmlToken) {
      myXmlToken = xmlToken;
    }

    @NotNull
    public String getText() {
      return XmlErrorMessages.message("remove.extra.closing.tag.quickfix");
    }

    @NotNull
    public String getFamilyName() {
      return XmlErrorMessages.message("remove.extra.closing.tag.quickfix");
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      if (!CodeInsightUtil.prepareFileForWrite(file)) return;

      XmlToken tagEndStart = myXmlToken;
      while(tagEndStart.getTokenType() != XmlTokenType.XML_END_TAG_START) {
        final PsiElement prevSibling = tagEndStart.getPrevSibling();
        if (!(prevSibling instanceof XmlToken)) break;
        tagEndStart = (XmlToken)prevSibling;
      }

      final PsiElement parent = tagEndStart.getParent();
      parent.deleteChildRange(tagEndStart,parent.getLastChild());
    }

    public boolean startInWriteAction() {
      return true;
    }
  }

  public static class CreateNSDeclarationIntentionAction implements IntentionAction {
    boolean myTaglibDeclaration;
    private final XmlElement myElement;
    private final String myNamespacePrefix;
    @NonNls private static final String MY_DEFAULT_XML_NS = "someuri";
    @NonNls private static final String URI_ATTR_NAME = "uri";

    public CreateNSDeclarationIntentionAction(final @NotNull XmlElement element, final @NotNull String namespacePrefix, boolean taglibDeclaration) {
      myElement = element;
      myNamespacePrefix = namespacePrefix;
      myTaglibDeclaration = taglibDeclaration;
    }

    @NotNull
    public String getText() {
      return XmlErrorMessages.message(
        myTaglibDeclaration ?
        "create.taglib.declaration.quickfix":
        "create.namespace.declaration.quickfix"
      );
    }

    @NotNull
    public String getFamilyName() {
      return getText();
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    private String[] guessNamespace(final PsiFile file, final Project project, final boolean acceptTaglib, final boolean acceptXmlNs) {
      final List<String> possibleUris = new LinkedList<String>();

      boolean unitTestMode = ApplicationManager.getApplication().isUnitTestMode();

      if (unitTestMode) doFindUris(acceptTaglib, project, file, possibleUris, acceptXmlNs);
      else {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
          new Runnable() {
            public void run() {
              doFindUris(acceptTaglib, project, file, possibleUris, acceptXmlNs);
            }
          },
          XmlErrorMessages.message("finding.acceptable.uri"),
          false,
          project
        );
      }

      return possibleUris.toArray( new String[possibleUris.size()] );
    }

    private void doFindUris(final boolean acceptTaglib,
                            final Project project,
                            final PsiFile file,
                            final List<String> possibleUris,
                            final boolean acceptXmlNs) {
      if (!(myElement instanceof XmlTag)) return;
      final XmlTag tag = (XmlTag)myElement;
      final ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();

      if (acceptTaglib) {
        if (pi != null) pi.setText(XmlErrorMessages.message("looking.in.tlds"));
        final JspManager instance = JspManager.getInstance(project);
        final JspFile jspFile = (JspFile)file;
        final String[] possibleTldUris = instance.getPossibleTldUris(jspFile);

        Arrays.sort(possibleTldUris);
        int i = 0;
        final String localName = tag.getLocalName();

        for(String uri:possibleTldUris) {
          if (pi != null) {
            pi.setFraction((double)i/possibleTldUris.length);
            pi.setText2(uri);
            ++i;
          }

          final XmlFile tldFileByUri = instance.getTldFileByUri(uri, jspFile);
          final boolean[] wordFound = new boolean[1];
          IdTableBuilding.ScanWordProcessor wordProcessor = new IdTableBuilding.ScanWordProcessor() {
            public void run(char[] chars, int start, int end) {
              if (end - start != localName.length() || wordFound[0]) return;
              for(int i = 0; i < localName.length(); ++i) {
                if (chars[start + i] != localName.charAt(i)) return;
              }
              wordFound[0] = true;
            }
          };

          if (tldFileByUri == null) continue;
          final CharSequence contents = tldFileByUri.getViewProvider().getContents();
          wordFound[0] = false;
          IdTableBuilding.scanWords(wordProcessor, CharArrayUtil.fromSequence(contents), 0, contents.length());
          if (!wordFound[0]) continue;
          final PsiMetaDataBase metaData = tldFileByUri.getDocument().getMetaData();

          if (metaData instanceof TldDescriptor) {
            if ( ((TldDescriptor)metaData).getElementDescriptor(tag) != null) {
              possibleUris.add(uri);
            }
          }
        }

        if (file.getFileType() == StdFileTypes.JSPX && possibleUris.size() == 0) {
          final JspManager jspManager = JspManager.getInstance(file.getProject());
          final XmlElementDescriptor descriptor = ((JspNsDescriptor)jspManager.getActionsLibrary()).getElementDescriptor(localName, XmlUtil.JSP_URI);
          if (descriptor != null) possibleUris.add(XmlUtil.JSP_URI);
        }
      }

      if (acceptXmlNs) {
        if (pi != null) pi.setText(XmlErrorMessages.message("looking.in.schemas"));
        final ExternalResourceManagerEx instanceEx = ExternalResourceManagerEx.getInstanceEx();
        final String[] availableUrls = instanceEx.getResourceUrls(null,true);
        int i = 0;

        for(String url:availableUrls) {
          if (pi != null) {
            pi.setFraction((double)i /availableUrls.length);
            pi.setText2(url);
            ++i;
          }
          final XmlFile xmlFile = XmlUtil.findXmlFile(file, url);

          if (xmlFile != null) {
            final PsiMetaDataBase metaData = xmlFile.getDocument().getMetaData();

            if (metaData instanceof XmlNSDescriptorImpl) {
              XmlElementDescriptor elementDescriptor = ((XmlNSDescriptorImpl)metaData).getElementDescriptor(tag.getLocalName(),url);

              if (elementDescriptor != null && !(elementDescriptor instanceof AnyXmlElementDescriptor)) {
                possibleUris.add(url);
              }
            }
          }
        }
      }
    }

    public void invoke(final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      if (!CodeInsightUtil.prepareFileForWrite(file)) return;
      final boolean taglib = myTaglibDeclaration || file instanceof JspFile;
      final String[] namespaces = guessNamespace(
        file,
        project,
        taglib,
        !(file instanceof JspFile)// || file.getFileType() == StdFileTypes.JSPX
      );

      if (namespaces.length > 1 && !ApplicationManager.getApplication().isUnitTestMode()) {
        final JList list = new JList(namespaces);
        list.setCellRenderer(new FQNameCellRenderer());
        Runnable runnable = new Runnable() {
          public void run() {
            final int index = list.getSelectedIndex();
            if (index < 0) return;
            PsiDocumentManager.getInstance(project).commitAllDocuments();

            CommandProcessor.getInstance().executeCommand(
              project,
              new Runnable() {
                public void run() {
                  ApplicationManager.getApplication().runWriteAction(
                    new Runnable() {
                      public void run() {
                        try {
                          insertNsDeclaration(file, namespaces[index], project);
                        }
                        catch (IncorrectOperationException e) {}
                      }
                    }
                  );
                }
              },
              getText(),
              getFamilyName()
            );
          }
        };

        new PopupChooserBuilder(list).
          setTitle(
            XmlErrorMessages.message(
                myTaglibDeclaration ? "select.taglib.title":"select.namespace.title"
           )).
          setItemChoosenCallback(runnable).
          createPopup().
          showInBestPositionFor(editor);
      } else {
        String defaultNs = ApplicationManager.getApplication().isUnitTestMode() ? (taglib ? XmlUtil.JSTL_CORE_URIS[0]:MY_DEFAULT_XML_NS):"";
        final XmlAttribute xmlAttribute = insertNsDeclaration(file, namespaces.length > 0 ? namespaces[0] : defaultNs, project);

        if (namespaces.length == 0) {
          final PsiElement valueToken = xmlAttribute.getValueElement().getChildren()[1];
          final TextRange textRange = valueToken.getTextRange();

          CommandProcessor.getInstance().executeCommand(
            project,
            new Runnable() {
              public void run() {
                if (valueToken instanceof XmlToken &&
                    ((XmlToken)valueToken).getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
                   ) {
                  editor.getSelectionModel().setSelection(textRange.getStartOffset(), textRange.getEndOffset());
                }
                editor.getCaretModel().moveToOffset(textRange.getStartOffset());
              }
            },
            getText(),
            getFamilyName()
          );
        }
      }
    }

    private XmlAttribute insertNsDeclaration(final PsiFile file, final String namespace, final Project project)
      throws IncorrectOperationException {
      final XmlTag rootTag = ((XmlFile)file).getDocument().getRootTag();
      final PsiElementFactory elementFactory = rootTag.getManager().getElementFactory();

      if (myTaglibDeclaration) {
        final XmlTag childTag = rootTag.createChildTag("directive.taglib", XmlUtil.JSP_URI, null, false);
        PsiElement element = childTag.add(elementFactory.createXmlAttribute("prefix", myNamespacePrefix));

        childTag.addAfter(
          elementFactory.createXmlAttribute(URI_ATTR_NAME,namespace),
          element
        );

        final XmlTag[] directives = ((JspFile)file).getDirectiveTags(JspDirectiveKind.TAGLIB, false);

        if (directives == null || directives.length == 0) {
          element = rootTag.addBefore(
            childTag, rootTag.getFirstChild()
          );
        } else {
          element = rootTag.addAfter(
            childTag, directives[directives.length - 1]
          );
        }

        CodeStyleManager.getInstance(project).reformat(element);
        return ((XmlTag)element).getAttribute(URI_ATTR_NAME,null);
      }
      else {
        @NonNls final String name = "xmlns:" + myNamespacePrefix;
        rootTag.add(
          elementFactory.createXmlAttribute(name,namespace)
        );
        return rootTag.getAttribute(name, null);
      }
    }

    public boolean startInWriteAction() {
      return true;
    }
  }
}