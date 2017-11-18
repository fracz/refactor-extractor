package com.intellij.codeInsight.editorActions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.completion.JspxCompletionData;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.HighlighterIterator;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.impl.source.jsp.jspJava.JspXmlTagBase;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.jsp.el.ELExpressionHolder;
import com.intellij.psi.jsp.el.ELTokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.tree.java.IJavaElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;

import java.util.HashMap;
import java.util.Map;

public class TypedHandler implements TypedActionHandler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.editorActions.TypedHandler");

  private TypedActionHandler myOriginalHandler;

  public interface QuoteHandler {
    boolean isClosingQuote(HighlighterIterator iterator, int offset);
    boolean isOpeningQuote(HighlighterIterator iterator, int offset);
    boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset);
    boolean isInsideLiteral(HighlighterIterator iterator);
  }

  private static final Map<FileType,QuoteHandler> quoteHandlers = new HashMap<FileType, QuoteHandler>();

  public static QuoteHandler getQuoteHandler(FileType fileType) {
    return quoteHandlers.get(fileType);
  }

  public static void registerQuoteHandler(FileType fileType, QuoteHandler quoteHandler) {
    quoteHandlers.put(fileType, quoteHandler);
  }

  static {
    registerQuoteHandler(StdFileTypes.JAVA, new JavaQuoteHandler());
    registerQuoteHandler(StdFileTypes.XML, new XmlQuoteHandler());
    HtmlQuoteHandler quoteHandler = new HtmlQuoteHandler();
    registerQuoteHandler(StdFileTypes.HTML, quoteHandler);
    registerQuoteHandler(StdFileTypes.XHTML, quoteHandler);
    registerQuoteHandler(StdFileTypes.JSP, new JspxQuoteHandler());
    registerQuoteHandler(StdFileTypes.JSPX, new JspxQuoteHandler());
  }

  public static class SimpleTokenSetQuoteHandler implements QuoteHandler {
    private TokenSet myLiteralTokenSet;

    public SimpleTokenSetQuoteHandler(IElementType[] _literalTokens) {
      myLiteralTokenSet = TokenSet.create(_literalTokens);
    }

    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
      final IElementType tokenType = iterator.getTokenType();

      if (myLiteralTokenSet.isInSet(tokenType)){
        int start = iterator.getStart();
        int end = iterator.getEnd();
        return end - start >= 1 && offset == end - 1;
      }

      return false;
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      if (myLiteralTokenSet.isInSet(iterator.getTokenType())){
        int start = iterator.getStart();
        return offset == start;
      }

      return false;
    }

    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
      try {
        Document doc = editor.getDocument();
        CharSequence chars = doc.getCharsSequence();
        int lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset));

        while (!iterator.atEnd() && iterator.getStart() < lineEnd) {
          IElementType tokenType = iterator.getTokenType();

          if (myLiteralTokenSet.isInSet(tokenType)) {
            if (iterator.getStart() >= iterator.getEnd() - 1 ||
                chars.charAt(iterator.getEnd() - 1) != '\"' && chars.charAt(iterator.getEnd() - 1) != '\'') {
              return true;
            }
          }
          iterator.advance();
        }
      }
      finally {
        while(iterator.atEnd() || iterator.getStart() != offset) iterator.retreat();
      }

      return false;
    }

    public boolean isInsideLiteral(HighlighterIterator iterator) {
      return myLiteralTokenSet.isInSet(iterator.getTokenType());
    }
  }

  public static class JspxQuoteHandler extends HtmlQuoteHandler {
    private SimpleTokenSetQuoteHandler myElHandler;
    private static final QuoteHandler ourJavaHandler = new JavaQuoteHandler();

    public JspxQuoteHandler() {
      myElHandler = new SimpleTokenSetQuoteHandler(
        new IElementType[] {ELTokenType.JSP_EL_STRING_LITERAL}
      );
    }

    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
      return myElHandler.isClosingQuote(iterator, offset) ||
             ourJavaHandler.isClosingQuote(iterator, offset) ||
             super.isClosingQuote(iterator, offset);
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      return myElHandler.isOpeningQuote(iterator, offset) ||
             ourJavaHandler.isOpeningQuote(iterator, offset) ||
             super.isOpeningQuote(iterator, offset);
    }

    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
      return myElHandler.hasNonClosedLiteral(editor, iterator, offset) ||
             ourJavaHandler.hasNonClosedLiteral(editor, iterator, offset) ||
             super.hasNonClosedLiteral(editor, iterator,offset);
    }

    public boolean isInsideLiteral(HighlighterIterator iterator) {
      return myElHandler.isInsideLiteral(iterator) ||
             ourJavaHandler.isInsideLiteral(iterator) ||
             super.isInsideLiteral(iterator);
    }
  }

  public static class HtmlQuoteHandler implements QuoteHandler {
    private static QuoteHandler ourStyleQuoteHandler;
    private QuoteHandler myBaseQuoteHandler;
    private static QuoteHandler ourScriptQuoteHandler;

    public HtmlQuoteHandler() {
      this(new XmlQuoteHandler());
    }

    public HtmlQuoteHandler(QuoteHandler _baseHandler) {
      myBaseQuoteHandler = _baseHandler;
    }

    public static void setStyleQuoteHandler(QuoteHandler quoteHandler) {
      ourStyleQuoteHandler = quoteHandler;
    }

    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
      if (myBaseQuoteHandler.isClosingQuote(iterator, offset)) return true;

      if(ourStyleQuoteHandler!=null && ourStyleQuoteHandler.isClosingQuote(iterator, offset)) {
        return true;
      }

      if(ourScriptQuoteHandler!=null && ourScriptQuoteHandler.isClosingQuote(iterator, offset)) {
        return true;
      }
      return false;
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      if (myBaseQuoteHandler.isOpeningQuote(iterator, offset)) return true;

      if(ourStyleQuoteHandler!=null && ourStyleQuoteHandler.isOpeningQuote(iterator, offset)) {
        return true;
      }

      if(ourScriptQuoteHandler!=null && ourScriptQuoteHandler.isOpeningQuote(iterator, offset)) {
        return true;
      }

      return false;
    }

    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
      if (myBaseQuoteHandler.hasNonClosedLiteral(editor,iterator, offset)) return true;

      if(ourStyleQuoteHandler!=null && ourStyleQuoteHandler.hasNonClosedLiteral(editor,iterator, offset)) {
        return true;
      }

      if(ourScriptQuoteHandler!=null && ourScriptQuoteHandler.hasNonClosedLiteral(editor,iterator, offset)) {
        return true;
      }

      return false;
    }

    public boolean isInsideLiteral(HighlighterIterator iterator) {
      if (myBaseQuoteHandler.isInsideLiteral(iterator)) return true;

      if(ourStyleQuoteHandler!=null && ourStyleQuoteHandler.isInsideLiteral(iterator)) {
        return true;
      }

      if(ourScriptQuoteHandler!=null && ourScriptQuoteHandler.isInsideLiteral(iterator)) {
        return true;
      }

      return false;
    }

    public static void setScriptQuoteHandler(QuoteHandler scriptQuoteHandler) {
      ourScriptQuoteHandler = scriptQuoteHandler;
    }
  }

  static class XmlQuoteHandler implements QuoteHandler {
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
      return iterator.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      return iterator.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;
    }

    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
      return true;
    }

    public boolean isInsideLiteral(HighlighterIterator iterator) {
      return false;
    }
  }

  static class JavaQuoteHandler extends SimpleTokenSetQuoteHandler {
    public JavaQuoteHandler() {
      super(new IElementType[] { JavaTokenType.STRING_LITERAL, JavaTokenType.CHARACTER_LITERAL});
    }

    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      boolean openingQuote = super.isOpeningQuote(iterator, offset);

      if (openingQuote) {
        // check escape next
        if (!iterator.atEnd()) {
          iterator.retreat();

          if (!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.isInSet( iterator.getTokenType() )) {
            openingQuote = false;
          }
          iterator.advance();
        }
      }
      return openingQuote;
    }

    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
      boolean closingQuote = super.isClosingQuote(iterator, offset);

      if (closingQuote) {
        // check escape next
        if (!iterator.atEnd()) {
          iterator.advance();

          if (!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.isInSet( iterator.getTokenType() )) {
            closingQuote = false;
          }
          iterator.retreat();
        }
      }
      return closingQuote;
    }
  }

  public TypedHandler(TypedActionHandler originalHandler){
    myOriginalHandler = originalHandler;
  }

  public void execute(Editor editor, char charTyped, DataContext dataContext) {
    Project project = (Project)dataContext.getData(DataConstants.PROJECT);
    if (project == null || editor.isColumnMode()){
      if (myOriginalHandler != null){
        myOriginalHandler.execute(editor, charTyped, dataContext);
      }
      return;
    }

    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

    if (file == null){
      if (myOriginalHandler != null){
        myOriginalHandler.execute(editor, charTyped, dataContext);
      }
      return;
    }

    if (editor.isViewer()) return;

    if (!editor.getDocument().isWritable()) {
      if (!FileDocumentManager.fileForDocumentCheckedOutSuccessfully(editor.getDocument(), project)) {
        return;
      }
    }

    AutoPopupController autoPopupController = AutoPopupController.getInstance(project);

    if (charTyped == '.') {
      autoPopupController.autoPopupMemberLookup(editor);
    }

    if (charTyped == '#') {
      autoPopupController.autoPopupMemberLookup(editor);
    }

    if (charTyped == '@' && file instanceof PsiJavaFile) {
      autoPopupController.autoPopupJavadocLookup(editor);
    }

    if (charTyped == '<' && file instanceof XmlFile) {
      autoPopupController.autoPopupXmlLookup(editor);
    }

    if (charTyped == '('){
      autoPopupController.autoPopupParameterInfo(editor, null);
    }

    if (!editor.isInsertMode()){
      myOriginalHandler.execute(editor, charTyped, dataContext);
      return;
    }

    if (editor.getSelectionModel().hasSelection()){
      EditorModificationUtil.deleteSelectedText(editor);
    }

    final VirtualFile virtualFile = file.getVirtualFile();
    FileType fileType;
    FileType originalFileType = null;

    if (virtualFile != null){
      originalFileType = fileType = FileTypeManager.getInstance().getFileTypeByFile(virtualFile);
    }
    else {
      fileType = file.getFileType();
    }

    if ('>' == charTyped){
      if (file instanceof XmlFile){
        handleXmlGreater(project, editor, fileType);
      }
    }
    else if (')' == charTyped){
      if (handleRParen(editor, fileType, ')', '(')) return;
    }
    else if (']' == charTyped){
      if (handleRParen(editor, fileType, ']', '[')) return;
    }
    else if (';' == charTyped) {
      if (handleSemicolon(editor, fileType)) return;
    }
    else if ('"' == charTyped || '\'' == charTyped){
      if (handleQuote(editor, fileType, charTyped, dataContext)) return;
    } else if ('}' == charTyped &&
               ( originalFileType == StdFileTypes.JSPX ||
                 originalFileType == StdFileTypes.JSP
               )
    ) {
      if (handleELClosingBrace(editor, file,project)) return;
    }

    myOriginalHandler.execute(editor, charTyped, dataContext);

    if ('(' == charTyped && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET){
      handleAfterLParen(editor, fileType, '(');
    }
    else if ('[' == charTyped && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET){
      handleAfterLParen(editor, fileType, '[');
    }
    else if ('}' == charTyped){
      indentClosingBrace(project, editor);
    }
    else if ('{' == charTyped){
      if (originalFileType == StdFileTypes.JSPX ||
          originalFileType == StdFileTypes.JSP
        ) {
        if(handleELOpeningBrace(editor, file,project)) return;
      }
      indentOpenedBrace(project, editor);
    }
    else if ('/' == charTyped){
      if (file instanceof XmlFile){
        handleXmlSlash(project, editor);
      }
    } else if ('=' == charTyped) {
      if (originalFileType == StdFileTypes.JSP) {
        handleJspEqual(project, editor);
      }
    }
  }

  private boolean handleELOpeningBrace(final Editor editor, final PsiFile file, final Project project) {
    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final int offset = editor.getCaretModel().getOffset();
    final PsiElement elementAt = file.findElementAt(offset-1);

    // TODO: handle it with insertAfterLParen(...)
    if (!JspxCompletionData.isJavaContext(elementAt) &&
        elementAt.getText().equals("${")
        ) {
      editor.getDocument().insertString(offset, "}");
      return true;
    }

    return false;
  }

  private boolean handleELClosingBrace(final Editor editor, final PsiFile file, final Project project) {
    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final int offset = editor.getCaretModel().getOffset();
    PsiElement elementAt = file.findElementAt(offset);
    PsiElement parent = PsiTreeUtil.getParentOfType(elementAt,ELExpressionHolder.class);

    // TODO: handle it with insertAfterRParen(...)
    if (parent != null) {
      if (elementAt != null && elementAt.getText().equals("}")) {
        editor.getCaretModel().moveToOffset(offset + 1);
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        return true;
      }
    }

    return false;
  }

  private void handleJspEqual(Project project, Editor editor) {
    final CharSequence chars = editor.getDocument().getCharsSequence();
    int current = editor.getCaretModel().getOffset();

    PsiDocumentManager.getInstance(project).commitAllDocuments();

    JspFile file = (JspFile)PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    PsiElement element = file.findElementAt(current);
    if (element == null) {
      element = file.findElementAt(editor.getDocument().getTextLength() - 1);
      if (element == null) return;
    }
    if (current >= 3 && chars.charAt(current-3) == '<' && chars.charAt(current-2)=='%')  {
      while (element instanceof PsiWhiteSpace) {
        element = element.getNextSibling();
      }

      int ptr = current;
      while(ptr < chars.length() && Character.isWhitespace(chars.charAt(ptr))) ++ptr;

      if (ptr + 1 >= chars.length() || (chars.charAt(ptr) != '%' || chars.charAt(ptr+1) != '>') ) {
        editor.getDocument().insertString(current,"%>");
      }
    }
  }

  private boolean handleSemicolon(Editor editor, FileType fileType) {
    if (fileType != StdFileTypes.JAVA) return false;
    int offset = editor.getCaretModel().getOffset();
    if (offset == editor.getDocument().getTextLength()) return false;

    char charAt = editor.getDocument().getCharsSequence().charAt(offset);
    if (charAt != ';') return false;

    editor.getCaretModel().moveToOffset(offset + 1);
    editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    return true;
  }

  private void handleXmlSlash(Project project, Editor editor){
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    XmlFile file = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset - 1);

    ASTNode prevLeaf = element.getNode();
    if (!"/".equals(prevLeaf.getText())) return;
    while((prevLeaf = TreeUtil.prevLeaf(prevLeaf)) != null && prevLeaf.getElementType() == XmlTokenType.XML_WHITE_SPACE);
    if(prevLeaf instanceof OuterLanguageElement) {
      element = file.getDocument().findElementAt(offset - 1);
      prevLeaf = element.getNode();
      while((prevLeaf = TreeUtil.prevLeaf(prevLeaf)) != null && prevLeaf.getElementType() == XmlTokenType.XML_WHITE_SPACE);
    }
    if(prevLeaf == null) return;

    XmlTag tag = PsiTreeUtil.getParentOfType(prevLeaf.getPsi(), XmlTag.class);
    if(tag == null) { // prevLeaf maybe in one tree and element in another
      PsiElement element2 = file.findElementAt(prevLeaf.getStartOffset());
      tag = PsiTreeUtil.getParentOfType(element2, XmlTag.class);
      if (tag == null) return;
    }

    if (tag instanceof JspXmlTagBase) return;
    if (XmlUtil.getTokenOfType(tag, XmlTokenType.XML_TAG_END) != null) return;
    if (XmlUtil.getTokenOfType(tag, XmlTokenType.XML_EMPTY_ELEMENT_END) != null) return;
    if (PsiTreeUtil.getParentOfType(element, XmlAttributeValue.class) != null) return;

    EditorModificationUtil.insertStringAtCaret(editor, ">");
  }

  private void handleXmlGreater(Project project, Editor editor, FileType fileType){
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    XmlFile file = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final int offset = editor.getCaretModel().getOffset();

    PsiElement element;

    if (offset < editor.getDocument().getTextLength()) {
      element = file.findElementAt(offset);
      if (!(element instanceof PsiWhiteSpace)) return;
      PsiElement parent = element.getParent();
      if (parent instanceof XmlText) {
        final String text = parent.getText();
        // check /
        final int index = offset - parent.getTextOffset() - 1;

        if (index >= 0 && text.charAt(index)=='/') {
          return; // already seen /
        }
        element = parent.getPrevSibling();
      } else if (parent instanceof XmlTag && !(element.getPrevSibling() instanceof XmlTag)) {
        element = parent;
      }
    }
    else {
      element = file.findElementAt(editor.getDocument().getTextLength() - 1);
      if (element == null) return;
      element = element.getParent();
    }

    if (element instanceof XmlAttributeValue) {
      element = element.getParent().getParent();
    }

    while(element instanceof PsiWhiteSpace) element = element.getPrevSibling();
    if (element == null) return;
    if (!(element instanceof XmlTag)) {
      if (element instanceof XmlTokenImpl &&
          element.getPrevSibling() !=null &&
          element.getPrevSibling().getText().equals("<")
         ) {
        // tag is started and there is another text in the end
        editor.getDocument().insertString(offset, "</" + element.getText() + ">");
      }
      return;
    }

    XmlTag tag = (XmlTag)element;
    if (XmlUtil.getTokenOfType(tag, XmlTokenType.XML_TAG_END) != null) return;
    if (XmlUtil.getTokenOfType(tag, XmlTokenType.XML_EMPTY_ELEMENT_END) != null) return;
    if (tag instanceof JspXmlTagBase) return;

    final String name = tag.getName();
    if (tag instanceof HtmlTag && HtmlUtil.isSingleHtmlTag(name)) return;
    if ("".equals(name)) return;

    int tagOffset = tag.getTextRange().getStartOffset();
    HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(tagOffset);
    if (BraceMatchingUtil.matchBrace(editor.getDocument().getCharsSequence(), fileType, iterator, true)) return;

    editor.getDocument().insertString(offset, "</" + name + ">");
  }

  private void handleAfterLParen(Editor editor, FileType fileType, char lparenChar){
    int offset = editor.getCaretModel().getOffset();
    HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);

    iterator.retreat();
    BraceMatchingUtil.BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType);
    IElementType braceTokenType = braceMatcher.getTokenType(lparenChar, iterator);
    if (iterator.atEnd() || iterator.getTokenType() != braceTokenType) return;
    iterator.advance();

    IElementType tokenType = !iterator.atEnd() ? iterator.getTokenType() : null;
    if (tokenType instanceof IJavaElementType) {
      if (!TokenTypeEx.WHITE_SPACE_OR_COMMENT_BIT_SET.isInSet(tokenType)
          && tokenType != JavaTokenType.SEMICOLON
          && tokenType != JavaTokenType.COMMA
          && tokenType != JavaTokenType.RPARENTH
          && tokenType != JavaTokenType.RBRACKET
          && tokenType != JavaTokenType.RBRACE
      ) {
        return;
      }
    }

    iterator.retreat();

    int lparenOffset = BraceMatchingUtil.findLeftmostLParen(iterator, braceTokenType, editor.getDocument().getCharsSequence(),fileType);
    if (lparenOffset < 0) lparenOffset = 0;

    iterator = ((EditorEx)editor).getHighlighter().createIterator(lparenOffset);
    boolean matched = BraceMatchingUtil.matchBrace(editor.getDocument().getCharsSequence(), fileType, iterator, true);

    if (!matched){
      String text;
      if (lparenChar == '(') {
        text = ")";
      }
      else if (lparenChar == '[') {
        text = "]";
      }
      else {
        LOG.assertTrue(false);

        return;
      }
      editor.getDocument().insertString(offset, text);
    }
  }

  private boolean handleRParen(Editor editor, FileType fileType, char rightParen, char leftParen){
    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return false;

    int offset = editor.getCaretModel().getOffset();

    if (offset == editor.getDocument().getTextLength()) return false;

    HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
    BraceMatchingUtil.BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType);
    if (iterator.atEnd() || braceMatcher.getTokenType(rightParen,iterator) != iterator.getTokenType()) {
      return false;
    }

    iterator.retreat();

    int lparenthOffset = BraceMatchingUtil.findLeftmostLParen(iterator, braceMatcher.getTokenType(leftParen, iterator),  editor.getDocument().getCharsSequence(),fileType);
    if (lparenthOffset < 0) return false;

    iterator = ((EditorEx) editor).getHighlighter().createIterator(lparenthOffset);
    boolean matched = BraceMatchingUtil.matchBrace(editor.getDocument().getCharsSequence(), fileType, iterator, true);

    if (!matched) return false;

    editor.getCaretModel().moveToOffset(offset + 1);
    editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    return true;
  }

  private boolean handleQuote(Editor editor, FileType fileType, char quote, DataContext dataContext) {
    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_QUOTE) return false;
    final QuoteHandler quoteHandler = quoteHandlers.get(fileType);
    if (quoteHandler == null) return false;

    int offset = editor.getCaretModel().getOffset();
    if (offset == editor.getDocument().getTextLength()) return false;

    CharSequence chars = editor.getDocument().getCharsSequence();
    int length = editor.getDocument().getTextLength();
    if (isTypingEscapeQuote(editor, quoteHandler, offset)) return false;

    if (offset < length && chars.charAt(offset) == quote){
      if (isClosingQuote(editor, quoteHandler, offset)){
        editor.getCaretModel().moveToOffset(offset + 1);
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        return true;
      }
    }

    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset);

    if (!iterator.atEnd()){
      IElementType tokenType = iterator.getTokenType();
      if (fileType == StdFileTypes.JAVA || fileType == StdFileTypes.JSP){
        if (tokenType instanceof IJavaElementType){
          if (!TokenTypeEx.WHITE_SPACE_OR_COMMENT_BIT_SET.isInSet(tokenType)
              && tokenType != JavaTokenType.SEMICOLON
              && tokenType != JavaTokenType.COMMA
              && tokenType != JavaTokenType.RPARENTH
              && tokenType != JavaTokenType.RBRACKET
              && tokenType != JavaTokenType.RBRACE
              && tokenType != JavaTokenType.STRING_LITERAL
              && tokenType != JavaTokenType.CHARACTER_LITERAL
          ) {
            return false;
          }
        }
      }
    }

    myOriginalHandler.execute(editor, quote, dataContext);
    offset = editor.getCaretModel().getOffset();

    if (isOpeningQuote(editor, quoteHandler, offset - 1) &&
        hasNonClosedLiterals(editor, quoteHandler, offset - 1)) {
      editor.getDocument().insertString(offset, "" + quote);
    }

    return true;
  }

  private boolean isClosingQuote(Editor editor, QuoteHandler quoteHandler, int offset) {
    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset);
    if (iterator.atEnd()){
      LOG.assertTrue(false);
      return false;
    }

    return quoteHandler.isClosingQuote(iterator,offset);
  }

  private boolean isOpeningQuote(Editor editor, QuoteHandler quoteHandler, int offset) {
    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset);
    if (iterator.atEnd()){
      LOG.assertTrue(false);
      return false;
    }

    return quoteHandler.isOpeningQuote(iterator, offset);
  }

  private boolean hasNonClosedLiterals(Editor editor, QuoteHandler quoteHandler, int offset) {
    HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
    if (iterator.atEnd()) {
      LOG.assertTrue(false);
      return false;
    }

    return quoteHandler.hasNonClosedLiteral(editor, iterator, offset);
  }

  private boolean isTypingEscapeQuote(Editor editor, QuoteHandler quoteHandler, int offset){
    if (offset == 0) return false;
    CharSequence chars = editor.getDocument().getCharsSequence();
    int offset1 = CharArrayUtil.shiftBackward(chars, offset - 1, "\\");
    int slashCount = (offset - 1) - offset1;
    if ((slashCount % 2) == 0) return false;
    return isInsideLiteral(editor, quoteHandler, offset);
  }

  private boolean isInsideLiteral(Editor editor, QuoteHandler quoteHandler, int offset){
    if (offset == 0) return false;

    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset - 1);
    if (iterator.atEnd()){
      LOG.assertTrue(false);
      return false;
    }

    return quoteHandler.isInsideLiteral(iterator);
  }

  private void indentClosingBrace(final Project project, final Editor editor){
    CodeInsightSettings settings = CodeInsightSettings.getInstance();
    if (!settings.AUTOINDENT_CLOSING_BRACE) return;

    final int offset = editor.getCaretModel().getOffset() - 1;
    final Document document = editor.getDocument();
    CharSequence chars = document.getCharsSequence();
    if (offset < 0 || chars.charAt(offset) != '}') return;
    int spaceStart = CharArrayUtil.shiftBackward(chars, offset - 1, " \t");
    if (spaceStart < 0 || chars.charAt(spaceStart) == '\n' || chars.charAt(spaceStart) == '\r'){
      PsiDocumentManager.getInstance(project).commitDocument(document);

      final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
      if (file == null || !file.isWritable()) return;
      PsiElement element = file.findElementAt(offset);
      if (element instanceof PsiJavaToken && ((PsiJavaToken)element).getTokenType() == JavaTokenType.RBRACE){
        final Runnable action = new Runnable() {
          public void run(){
            try{
              int newOffset = CodeStyleManager.getInstance(project).adjustLineIndent(file, offset);
              editor.getCaretModel().moveToOffset(newOffset + 1);
              editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
              editor.getSelectionModel().removeSelection();
            }
            catch(IncorrectOperationException e){
              LOG.error(e);
            }
          }
        };
        ApplicationManager.getApplication().runWriteAction(action);
      }
    }
  }

  private void indentOpenedBrace(final Project project, final Editor editor){
    final int offset = editor.getCaretModel().getOffset() - 1;
    final Document document = editor.getDocument();
    CharSequence chars = document.getCharsSequence();
    if (offset < 0 || chars.charAt(offset) != '{') return;

    int spaceStart = CharArrayUtil.shiftBackward(chars, offset - 1, " \t");
    if (spaceStart < 0 || chars.charAt(spaceStart) == '\n' || chars.charAt(spaceStart) == '\r'){
      PsiDocumentManager.getInstance(project).commitDocument(document);

      final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
      if (file == null || !file.isWritable()) return;
      PsiElement element = file.findElementAt(offset);
      if (element instanceof PsiJavaToken && ((PsiJavaToken)element).getTokenType() == JavaTokenType.LBRACE){
        final Runnable action = new Runnable() {
          public void run(){
            try{
              int newOffset = CodeStyleManager.getInstance(project).adjustLineIndent(file, offset);
              editor.getCaretModel().moveToOffset(newOffset + 1);
              editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
              editor.getSelectionModel().removeSelection();
            }
            catch(IncorrectOperationException e){
              LOG.error(e);
            }
          }
        };
        ApplicationManager.getApplication().runWriteAction(action);
      }
    }
  }
}
