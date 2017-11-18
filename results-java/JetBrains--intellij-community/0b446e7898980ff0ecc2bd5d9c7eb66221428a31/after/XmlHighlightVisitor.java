package com.intellij.codeInsight.daemon.impl.analysis;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.QuickFixProvider;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.RefCountHolder;
import com.intellij.codeInsight.daemon.impl.SwitchOffToolAction;
import com.intellij.codeInsight.daemon.impl.quickfix.AddHtmlTagOrAttributeToCustoms;
import com.intellij.codeInsight.daemon.impl.quickfix.FetchExtResourceAction;
import com.intellij.codeInsight.daemon.impl.quickfix.IgnoreExtResourceAction;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.j2ee.openapi.ex.ExternalResourceManagerEx;
import com.intellij.jsp.impl.JspElementDescriptor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.jsp.jspJava.JspDirective;
import com.intellij.psi.impl.source.jsp.jspJava.JspText;
import com.intellij.psi.impl.source.jsp.tagLibrary.TldUtil;
import com.intellij.psi.impl.source.resolve.reference.impl.GenericReference;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Mike
 */
public class XmlHighlightVisitor extends PsiElementVisitor implements Validator.ValidationHost {
  private static final String UNKNOWN_SYMBOL = "Cannot resolve symbol {0}";
  static final Key<String> DO_NOT_VALIDATE_KEY = Key.create("do not validate");
  private List<HighlightInfo> myResult = new SmartList<HighlightInfo>();
  private RefCountHolder myRefCountHolder;
  private DaemonCodeAnalyzerSettings mySettings;

  private static boolean ourDoJaxpTesting;

  public XmlHighlightVisitor(DaemonCodeAnalyzerSettings settings) {
    mySettings = settings;
  }

  public void setRefCountHolder(RefCountHolder refCountHolder) {
    myRefCountHolder = refCountHolder;
  }

  public List<HighlightInfo> getResult() {
    return myResult;
  }

  public void clearResult() {
    myResult.clear();
    myRefCountHolder = null;
  }

  private static void addElementsForTag(XmlTag tag,
                                        String localizedMessage,
                                        List<HighlightInfo> result,
                                        HighlightInfoType type,
                                        IntentionAction quickFixAction) {
    addElementsForTagWithManyQuickFixes(tag, localizedMessage, result, type, quickFixAction);
  }
  private static void addElementsForTagWithManyQuickFixes(XmlTag tag,
                                        String localizedMessage,
                                        List<HighlightInfo> result,
                                        HighlightInfoType type,
                                        IntentionAction... quickFixActions) {
    ASTNode tagElement = SourceTreeToPsiMap.psiElementToTree(tag);
    ASTNode childByRole = XmlChildRole.START_TAG_NAME_FINDER.findChild(tagElement);

    if(childByRole != null) {
      HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(type, childByRole, localizedMessage);
      result.add(highlightInfo);

      for (final IntentionAction quickFixAction : quickFixActions) {
        if (quickFixAction == null) continue;
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFixAction, null);
      }
    }

    childByRole = XmlChildRole.CLOSING_TAG_NAME_FINDER.findChild(tagElement);

    if(childByRole != null) {
      HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(type, childByRole, localizedMessage);
      for (final IntentionAction quickFixAction : quickFixActions) {
        if (quickFixAction == null) continue;
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFixAction, null);
      }

      result.add(highlightInfo);
    }
  }

  public void visitXmlToken(XmlToken token) {
    if (token.getTokenType() == XmlTokenType.XML_NAME) {
      PsiElement element = token.getPrevSibling();
      while(element instanceof PsiWhiteSpace) element = element.getPrevSibling();

      if (element instanceof XmlToken && ((XmlToken)element).getTokenType() == XmlTokenType.XML_START_TAG_START) {
        PsiElement parent = element.getParent();

        if (parent instanceof XmlTag) {
          XmlTag tag = (XmlTag)parent;
          checkTag(tag);
        }
      }
    }
  }

  //public void visitXmlText(XmlText text) {
  //  final String textString = text.getText();
  //  int ampInd = textString.indexOf('&');
  //  if (ampInd!=-1) {
  //
  //  }
  //  super.visitXmlText(text);
  //}


  private void checkTag(XmlTag tag) {
    if (ourDoJaxpTesting) return;

    if (tag.getName() == null) {
      return;
    }

    if (!checkTagIsClosed(tag)) return;

    if (!(tag.getParent() instanceof XmlTag)) {
      checkRootTag(tag);
    }

    if (myResult.isEmpty()) {
      checkTagByDescriptor(tag);
    }
  }

  public void registerXmlErrorQuickFix(final PsiErrorElement element, final HighlightInfo highlightInfo) {
    final String text = element.getErrorDescription();
    if (text != null && text.startsWith("Unescaped &")) {
      QuickFixAction.registerQuickFixAction(highlightInfo, new IntentionAction() {
        public String getText() {
          return "Escape Ampersand";
        }

        public String getFamilyName() {
          return getText();
        }

        public boolean isAvailable(Project project, Editor editor, PsiFile file) {
          return true;
        }

        public void invoke(Project project, Editor editor, PsiFile file) {
          if (!CodeInsightUtil.prepareFileForWrite(file)) return;
          final int textOffset = element.getTextOffset();
          editor.getDocument().replaceString(textOffset,textOffset + 1,"&amp;");
        }

        public boolean startInWriteAction() {
          return true;
        }
      }, null);
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

    public String getText() {
      return "Rename " + ((myStart)?"Start":"End") + " Tag Name";
    }

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

    for (PsiElement child : children) {
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
              isExtraHtmlTagEnd = (tag instanceof HtmlTag && HtmlUtil.isSingleHtmlTag(name));
              if (!isExtraHtmlTagEnd) return true;
            }

            HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
              (isExtraHtmlTagEnd)?HighlightInfoType.WARNING:HighlightInfoType.ERROR,
              xmlToken,
              (isExtraHtmlTagEnd)?"Extra closing tag for empty element":"Wrong closing tag name");
            myResult.add(highlightInfo);

            if (isExtraHtmlTagEnd) {
              QuickFixAction.registerQuickFixAction(highlightInfo, new IntentionAction() {
                public String getText() {
                  return "Remove Extra Closing Tag";
                }

                public String getFamilyName() {
                  return "Remove Extra Closing Tag";
                }

                public boolean isAvailable(Project project, Editor editor, PsiFile file) {
                  return true;
                }

                public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                  if (!CodeInsightUtil.prepareFileForWrite(file)) return;

                  XmlToken tagEndStart = xmlToken;
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
              }, null);
            } else {
              IntentionAction intentionAction = new RenameTagBeginOrEndIntentionAction(tag, name, false);
              IntentionAction intentionAction2 = new RenameTagBeginOrEndIntentionAction(tag, text, true);

              QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction, null);
              QuickFixAction.registerQuickFixAction(highlightInfo, startTagNameToken.getTextRange(), intentionAction2, null);
            }

            return false;
          }
          else {
            startTagNameToken = xmlToken;
          }
        }
      }
    }

    if (tag instanceof HtmlTag &&
        ( HtmlUtil.isOptionalEndForHtmlTag(name) ||
          HtmlUtil.isSingleHtmlTag(name)
        )
       ) {
      return true;
    }
    return false;
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
        elementDescriptor = parentDescriptor.getElementDescriptor(tag);
      }

      if (parentDescriptor != null && elementDescriptor == null) {
        addElementsForTag(
          tag,
          "Element " + name + " is not allowed here",
          myResult,
          getTagProblemInfoType(tag),
          null
        );
        return;
      }

      if (elementDescriptor instanceof AnyXmlElementDescriptor || parentDescriptor == null) {
        elementDescriptor = tag.getDescriptor();
      }

      if (elementDescriptor == null) return;
    }
    else {
      //root tag
      elementDescriptor = tag.getDescriptor();

     if (elementDescriptor == null) {
       addElementsForTag(tag, "Element " + name + " must be declared", myResult, HighlightInfoType.WRONG_REF, null);
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
            final String localizedMessage = "Element " + name + " doesn't have required attribute " + attrName;

            reportOneTagProblem(
              tag,
              attrName,
              localizedMessage,
              insertRequiredAttributeIntention,
              mySettings.getInspectionProfile(tag).getAdditionalNotRequiredHtmlAttributes(),
              HighlightInfoType.REQUIRED_HTML_ATTRIBUTE,
              HighlightDisplayKey.REQUIRED_HTML_ATTRIBUTE
            );
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
                                   String additional,
                                   HighlightInfoType type,
                                   HighlightDisplayKey key) {
    boolean htmlTag = false;

    if (tag instanceof HtmlTag) {
      htmlTag = true;
      if(isAdditionallyDeclared(additional, name)) return;
    }

    if (htmlTag && mySettings.getInspectionProfile(tag).isToolEnabled(key)) {
      addElementsForTagWithManyQuickFixes(
        tag,
        localizedMessage,
        myResult,
        type,
        new IntentionAction[] {
          new AddHtmlTagOrAttributeToCustoms(name,type),
          new SwitchOffToolAction(key),
          basicIntention
        }
      );
    } else if (!htmlTag) {
      addElementsForTag(
        tag,
        localizedMessage,
        myResult,
        HighlightInfoType.WRONG_REF,
        basicIntention
      );
    }
  }

  private boolean isAdditionallyDeclared(final String additional, final String name) {
    StringTokenizer tokenizer = new StringTokenizer(additional, ", ");
    while (tokenizer.hasMoreTokens()) {
      if (name.equals(tokenizer.nextToken())) {
        return true;
      }
    }

    return false;
  }

  private void checkDirective(final String name, final XmlTag tag) {
    if ("taglib".equals(name)) {
      final String uri = tag.getAttributeValue("uri");

      if (uri == null) {
        if (tag.getAttributeValue("tagdir") == null) {
          final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF,
            XmlChildRole.START_TAG_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(tag)),
            "Either uri or tagdir attribute should be specified"
          );

          myResult.add(highlightInfo);
          QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeIntention(
              tag,
              "uri",
              TldUtil.getPossibleTldUris((JspFile)tag.getContainingFile())
            ),
            null
          );

          QuickFixAction.registerQuickFixAction(
            highlightInfo,
            new InsertRequiredAttributeIntention(tag, "tagdir",null),
            null
          );
        }
      }
    }
  }

  private static HighlightInfoType getTagProblemInfoType(XmlTag tag) {
    return (tag instanceof HtmlTag && XmlUtil.HTML_URI.equals(tag.getNamespace()))?
           HighlightInfoType.WARNING:HighlightInfoType.WRONG_REF;
  }

  private void checkRootTag(XmlTag tag) {
    XmlFile file = (XmlFile)tag.getContainingFile();

    XmlProlog prolog = file.getDocument().getProlog();

    if (prolog == null) {
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
      addElementsForTag(tag, "Wrong root element", myResult, HighlightInfoType.WRONG_REF, null);
    }
  }

  public void visitXmlAttribute(XmlAttribute attribute) {
    XmlTag tag = attribute.getParent();

    if (attribute.isNamespaceDeclaration()) {
      checkNamespaceAttribute(attribute);
      return;
    } else if (attribute.getName().endsWith("Location")) {
      String namespace = attribute.getNamespace();

      if (namespace.equals(XmlUtil.XML_SCHEMA_INSTANCE_URI)) {
        checkSchemaLocationAttribute(attribute);
        return;
      }
    }

    XmlElementDescriptor elementDescriptor = tag.getDescriptor();
    if (elementDescriptor == null || ourDoJaxpTesting) return;
    XmlAttributeDescriptor attributeDescriptor = elementDescriptor.getAttributeDescriptor(attribute);

    String name = attribute.getName();

    if (attributeDescriptor == null) {
      final String localizedMessage = "Attribute " + name + " is not allowed here";
      reportAttributeProblem(tag, name, attribute, localizedMessage);
    }
    else {
      checkDuplicateAttribute(tag, attribute);

      if (tag instanceof HtmlTag &&
          attribute.getValueElement() == null &&
          !HtmlUtil.isSingleHtmlAttribute(attribute.getName())
         ) {
        final String localizedMessage = "Empty attribute " + name + " is not allowed";
        reportAttributeProblem(tag, name, attribute, localizedMessage);
      }
    }
  }

  private void reportAttributeProblem(final XmlTag tag,
                                      final String localName,
                                      final XmlAttribute attribute,
                                      final String localizedMessage) {
    final HighlightInfoType tagProblemInfoType;
    IntentionAction[] quickFixes;

    if (tag instanceof HtmlTag) {
      final InspectionProfileImpl inspectionProfile = mySettings.getInspectionProfile(tag);
      if(isAdditionallyDeclared(inspectionProfile.getAdditionalHtmlAttributes(),localName)) return;
      if (!inspectionProfile.isToolEnabled(HighlightDisplayKey.UNKNOWN_HTML_ATTRIBUTES)) return;
      tagProblemInfoType = HighlightInfoType.UNKNOWN_HTML_ATTRIBUTE;

      quickFixes = new IntentionAction[] {
        new AddHtmlTagOrAttributeToCustoms(localName,tagProblemInfoType),
        new SwitchOffToolAction(HighlightDisplayKey.UNKNOWN_HTML_ATTRIBUTES)
      };
    } else {
      tagProblemInfoType = HighlightInfoType.WRONG_REF; quickFixes = null;
    }

    final HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
      tagProblemInfoType,
      XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(attribute)),
      localizedMessage
    );
    myResult.add(highlightInfo);

    if (quickFixes != null) {
      for (IntentionAction quickFix : quickFixes) {
        QuickFixAction.registerQuickFixAction(highlightInfo, quickFix, null);
      }
    }
  }

  private void checkDuplicateAttribute(XmlTag tag, final XmlAttribute attribute) {
    if (tag.getUserData(DO_NOT_VALIDATE_KEY) != null) {
      return;
    }
    XmlAttribute[] attributes = tag.getAttributes();

    for (XmlAttribute tagAttribute : attributes) {
      if (attribute != tagAttribute && Comparing.strEqual(attribute.getName(), tagAttribute.getName())) {
        final String localName = attribute.getLocalName();
        HighlightInfo highlightInfo = HighlightInfo.createHighlightInfo(
          getTagProblemInfoType(tag),
          XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(SourceTreeToPsiMap.psiElementToTree(attribute)),
          "Duplicate attribute " + localName);
        myResult.add(highlightInfo);

        IntentionAction intentionAction = new RemoveDuplicatedAttributeIntentionAction(localName, attribute);

        QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction, null);
      }
    }
  }

  public void visitXmlTag(XmlTag tag) {
    if (tag.getUserData(DO_NOT_VALIDATE_KEY) == null) {
      if (tag instanceof HtmlTag && tag.getDescriptor() instanceof AnyXmlElementDescriptor) {
        final String name = tag.getName();
        reportOneTagProblem(
          tag,
          name,
          "Unknown html tag " + name,
          null,
          mySettings.getInspectionProfile(tag).getAdditionalHtmlTags(),
          HighlightInfoType.UNKNOWN_HTML_TAG,
          HighlightDisplayKey.UNKNOWN_HTML_TAG
        );

        return;
      }

      checkReferences(tag, QuickFixProvider.NULL);
    }
  }

  public void visitXmlAttributeValue(XmlAttributeValue value) {
    if (!(value.getParent() instanceof XmlAttribute)) {
      checkReferences(value, QuickFixProvider.NULL);
      return;
    }

    XmlAttribute attribute = (XmlAttribute)value.getParent();
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
      myResult.add(HighlightInfo.createHighlightInfo(
          getTagProblemInfoType(tag),
          value,
          error));
      return;
    }

    if (myRefCountHolder != null && attributeDescriptor.hasIdType()) {
      final String unquotedValue = getUnquotedValue(value, tag);

      if (XmlUtil.isSimpleXmlAttributeValue(unquotedValue)) {
        XmlTag xmlTag = myRefCountHolder.getTagById(unquotedValue);

        if (xmlTag == null ||
            !xmlTag.isValid() ||
            xmlTag == tag
           ) {
          myRefCountHolder.registerTagWithId(unquotedValue,tag);
        } else {
          XmlAttribute anotherTagIdValue = xmlTag.getAttribute("id", null);

          if (anotherTagIdValue!=null &&
              getUnquotedValue(anotherTagIdValue.getValueElement(), xmlTag).equals(unquotedValue)
             ) {
            myResult.add(HighlightInfo.createHighlightInfo(
              HighlightInfoType.WRONG_REF,
              value,
              "Duplicate id reference")
            );
            myResult.add(HighlightInfo.createHighlightInfo(
              HighlightInfoType.WRONG_REF,
              xmlTag.getAttribute("id",null).getValueElement(),
              "Duplicate id reference")
            );
            return;
          } else {
            // tag previously has that id
            myRefCountHolder.registerTagWithId(unquotedValue,tag);
          }
        }
      }
    }

    QuickFixProvider quickFixProvider = QuickFixProvider.NULL;
    if (attributeDescriptor instanceof QuickFixProvider) quickFixProvider = (QuickFixProvider)attributeDescriptor;

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

    if (attributeDescriptor.hasIdRefType()) {
      String unquotedValue = getUnquotedValue(value, tag);
      if (XmlUtil.isSimpleXmlAttributeValue(unquotedValue)) {
        XmlTag xmlTag = holder.getTagById(unquotedValue);

        if (xmlTag == null || !xmlTag.isValid()) {
          return HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF,
            value,
            "Invalid id reference"
          );
        }
      }
    }

    return null;
  }

  private static String getUnquotedValue(XmlAttributeValue value, XmlTag tag) {
    String unquotedValue = value.getText();

    if (unquotedValue.length() > 0 &&
        ( unquotedValue.charAt(0)=='"' ||
          unquotedValue.charAt(0)=='\''
        )
       ) {
      unquotedValue = unquotedValue.substring(1,unquotedValue.length()-1);
    }

    if (tag instanceof HtmlTag) {
      unquotedValue = unquotedValue.toLowerCase();
    }

    return unquotedValue;
  }

  private void checkReferences(PsiElement value, QuickFixProvider quickFixProvider) {
    PsiReference[] references = value.getReferences();

    for (final PsiReference reference : references) {
      if (reference != null) {
        if (!reference.isSoft()) {
          boolean hasBadResolve;

          if (reference instanceof PsiPolyVariantReference) {
            hasBadResolve = ((PsiPolyVariantReference)reference).multiResolve(false).length == 0;
          } else {
            hasBadResolve = reference.resolve() == null;
          }

          if(hasBadResolve) {
            String message;
            if (reference instanceof GenericReference) {
              message = ((GenericReference)reference).getUnresolvedMessage();
            }
            else {
              message = UNKNOWN_SYMBOL;
            }

            HighlightInfo info = HighlightInfo.createHighlightInfo(
              getTagProblemInfoType(PsiTreeUtil.getParentOfType(value, XmlTag.class)),
              reference.getElement().getTextRange().getStartOffset() + reference.getRangeInElement().getStartOffset(),
              reference.getElement().getTextRange().getStartOffset() + reference.getRangeInElement().getEndOffset(),
              MessageFormat.format(message, new Object[]{reference.getCanonicalText()}));
            myResult.add(info);
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
  }

  public void visitXmlDoctype(XmlDoctype xmlDoctype) {
    String uri = xmlDoctype.getDtdUri();
    if (uri == null || ExternalResourceManagerEx.getInstanceEx().isIgnoredResource(uri)) return;

    XmlFile xmlFile = XmlUtil.findXmlFile(xmlDoctype.getContainingFile(), uri);
    if (xmlFile == null) {
      HighlightInfo info = HighlightInfo.createHighlightInfo(
            HighlightInfoType.WRONG_REF,
            xmlDoctype.getDtdUrlElement().getTextRange().getStartOffset() + 1,
            xmlDoctype.getDtdUrlElement().getTextRange().getEndOffset() - 1,
            "URI is not registered (Settings | IDE Settings | Resources)");
      myResult.add(info);
      QuickFixAction.registerQuickFixAction(info, new FetchExtResourceAction(), null);
      QuickFixAction.registerQuickFixAction(info, new IgnoreExtResourceAction(), null);
    }
  }

  public void visitReferenceExpression(PsiReferenceExpression expression) {
    visitExpression(expression);
  }

  private void checkNamespaceAttribute(XmlAttribute attribute) {
    String namespace = null;

    if (attribute.isNamespaceDeclaration()) {
      namespace = attribute.getValue();
    }

    if(namespace == null || namespace.length() < 1|| ExternalResourceManagerEx.getInstanceEx().isIgnoredResource(namespace)) return;
    XmlTag declarationTag = attribute.getParent();

    if(declarationTag.getNSDescriptor(namespace, true) != null) return;

    String attributeValue = declarationTag.getAttributeValue("targetNamespace");
    if (attributeValue != null && attributeValue.equals(namespace)) {
      // we referencing ns while defining it
      return;
    }

    XmlAttributeValue element = attribute.getValueElement();
    if(element == null) return;
    int start = element.getTextRange().getStartOffset() + 1;
    int end = element.getTextRange().getEndOffset() - 1;

    reportURIProblem(start,end);
  }

  private void checkSchemaLocationAttribute(XmlAttribute attribute) {
    if(attribute.getValueElement() == null) return;
    String location = attribute.getValue();

    if (attribute.getLocalName().equals("noNamespaceSchemaLocation")) {
      if(ExternalResourceManagerEx.getInstanceEx().isIgnoredResource(location)) return;

      if(XmlUtil.findXmlFile(attribute.getContainingFile(),location) == null) {
        int start = attribute.getValueElement().getTextOffset();
        reportURIProblem(start,start + location.length());
      }
    } else if (attribute.getLocalName().equals("schemaLocation")) {
      StringTokenizer tokenizer = new StringTokenizer(location);
      XmlFile file = null;

      while(tokenizer.hasMoreElements()) {
        tokenizer.nextToken(); // skip namespace
        if (!tokenizer.hasMoreElements()) return;
        String url = tokenizer.nextToken();

        if(ExternalResourceManagerEx.getInstanceEx().isIgnoredResource(url)) continue;
        if (file == null) {
          file = (XmlFile)attribute.getContainingFile();
        }

        if(XmlUtil.findXmlFile(file,url) == null) {
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
      "URI is not registered (Settings | IDE Settings | Resources)");
    QuickFixAction.registerQuickFixAction(info, new FetchExtResourceAction(), null);
    QuickFixAction.registerQuickFixAction(info, new IgnoreExtResourceAction(), null);
    myResult.add(info);
  }

  public static void setDoJaxpTesting(boolean doJaxpTesting) {
    ourDoJaxpTesting = doJaxpTesting;
  }

  public void addMessage(PsiElement context, String message, int type) {
    if (message != null && message.length() > 0) {
      if (context instanceof XmlTag) {
        addElementsForTag((XmlTag)context,message,myResult, type == ERROR ? HighlightInfoType.ERROR:HighlightInfoType.WARNING,null);
      } else {
        myResult.add(HighlightInfo.createHighlightInfo(HighlightInfoType.WRONG_REF,context,message));
      }
    }
  }

  public void visitJspElement(JspText text) {
    final PsiElement parent = text.getParent();

    if (!(parent instanceof XmlText) &&
        !(parent instanceof XmlDocument)
        ) { // optimization
      parent.putUserData(DO_NOT_VALIDATE_KEY, "");
    }
  }

  private static class InsertRequiredAttributeIntention implements IntentionAction {
    private final XmlTag myTag;
    private final String myAttrName;
    private String[] myValues;

    public InsertRequiredAttributeIntention(final XmlTag tag, final String attrName,final String[] values) {
      myTag = tag;
      myAttrName = attrName;
      myValues = values;
    }

    public String getText() {
      return "Insert Required Attribute " + myAttrName;
    }

    public String getFamilyName() {
      return "Insert Required Attribute";
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(final Project project, final Editor editor, PsiFile file) {
      if (!CodeInsightUtil.prepareFileForWrite(file)) return;
      ASTNode treeElement = SourceTreeToPsiMap.psiElementToTree(myTag);
      PsiElement anchor = SourceTreeToPsiMap.treeElementToPsi(
        XmlChildRole.EMPTY_TAG_END_FINDER.findChild(treeElement)
      );

      if (anchor == null) {
        anchor = SourceTreeToPsiMap.treeElementToPsi(
          XmlChildRole.START_TAG_END_FINDER.findChild(treeElement)
        );
      }

      if (anchor == null) return;

      final Template template = TemplateManager.getInstance(project).createTemplate("", "");
      template.addTextSegment(" " + myAttrName + "=\"");

      Expression expression = new Expression() {
        TextResult result = new TextResult("");

        public Result calculateResult(ExpressionContext context) {
          return result;
        }

        public Result calculateQuickResult(ExpressionContext context) {
          return null;
        }

        public LookupItem[] calculateLookupItems(ExpressionContext context) {
          final LookupItem items[] = new LookupItem[(myValues != null)?myValues.length:0];

          if (myValues != null) {
            for (int i = 0; i < items.length; i++) {
              items[i] = LookupItemUtil.objectToLookupItem(myValues[i]);
            }
          }
          return items;
        }
      };
      template.addVariable("name", expression, expression, true);
      template.addTextSegment("\"");

      final PsiElement anchor1 = anchor;

      final Runnable runnable = new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
              public void run() {
                int textOffset = anchor1.getTextOffset();
                editor.getCaretModel().moveToOffset(textOffset);
                TemplateManager.getInstance(project).startTemplate(editor, template, null);
              }
            }
          );
        }
      };

      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        Runnable commandRunnable = new Runnable() {
          public void run() {
            CommandProcessor.getInstance().executeCommand(
              project,
              runnable,
              getText(),
              getFamilyName()
            );
          }
        };

        ApplicationManager.getApplication().invokeLater(commandRunnable);
      }
      else {
        runnable.run();
      }
    }

    public boolean startInWriteAction() {
      return true;
    }
  }

  private static class RemoveDuplicatedAttributeIntentionAction implements IntentionAction {
    private final String myLocalName;
    private final XmlAttribute myAttribute;

    public RemoveDuplicatedAttributeIntentionAction(final String localName, final XmlAttribute attribute) {
      myLocalName = localName;
      myAttribute = attribute;
    }

    public String getText() {
      return "Remove Duplicated Attribute " + myLocalName;
    }

    public String getFamilyName() {
      return "Remove Duplicated Attribute";
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

    private PsiElement findNextAttribute(final XmlAttribute attribute) {
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
}