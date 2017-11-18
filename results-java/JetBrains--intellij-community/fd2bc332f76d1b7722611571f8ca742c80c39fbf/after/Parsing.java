package com.intellij.psi.impl.source.parsing;

import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.lang.ASTFactory;
import com.intellij.lexer.FilterLexer;
import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.Constants;
import com.intellij.psi.impl.source.DummyHolderFactory;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.Nullable;

public class Parsing implements Constants{
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.parsing.Parsing");
  protected static final boolean DEEP_PARSE_BLOCKS_IN_STATEMENTS = false;
  protected final JavaParsingContext myContext;

  public Parsing(JavaParsingContext context) {
    myContext = context;
  }

  @Nullable
  public static CompositeElement parseJavaCodeReferenceText(PsiManager manager, CharSequence buffer, CharTable table) {
    return (CompositeElement)parseJavaCodeReferenceText(manager, buffer, 0, buffer.length(), table, false);
  }

  //Since we are to parse greedily (up to the end) in case eatAll=true,
  //  we are not guaranteed to return reference actually
  @Nullable
  public static TreeElement parseJavaCodeReferenceText(PsiManager manager,
                                                       CharSequence buffer,
                                                       int startOffset,
                                                       int endOffset,
                                                       CharTable table,
                                                       boolean eatAll) {
    Lexer originalLexer = new JavaLexer(LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    FilterLexer lexer = new FilterLexer(originalLexer, new FilterLexer.SetFilter(StdTokenSets.WHITE_SPACE_OR_COMMENT_BIT_SET));
    lexer.start(buffer, startOffset, endOffset,0);

    JavaParsingContext context = new JavaParsingContext(table, LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    CompositeElement ref = context.getStatementParsing().parseJavaCodeReference(lexer, false, true);
    final FileElement dummyRoot = DummyHolderFactory.createHolder(manager, null, table).getTreeElement();
    if (ref == null) {
      if (!eatAll) return null;
    } else {
      TreeUtil.addChildren(dummyRoot, ref);
    }

    if (lexer.getTokenType() != null) {
      if (!eatAll) return null;
      final CompositeElement errorElement = Factory.createErrorElement(JavaErrorMessages.message("unexpected.tokens"));
      while (lexer.getTokenType() != null) {
        final TreeElement token = ParseUtil.createTokenElement(lexer, context.getCharTable());
        TreeUtil.addChildren(errorElement, token);
        lexer.advance();
      }
      TreeUtil.addChildren(dummyRoot, errorElement);
    }

    ParseUtil.insertMissingTokens(dummyRoot, originalLexer, startOffset, endOffset, -1, WhiteSpaceAndCommentsProcessor.INSTANCE, context);
    return dummyRoot.getFirstChildNode();
  }

  public CompositeElement parseJavaCodeReference(Lexer lexer, boolean allowIncomplete, final boolean parseParameterList) {
    if (lexer.getTokenType() != IDENTIFIER) return null;

    TreeElement identifier = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
    lexer.advance();

    CompositeElement refElement = ASTFactory.composite(JAVA_CODE_REFERENCE);
    TreeUtil.addChildren(refElement, identifier);
    CompositeElement parameterList;
    if (parseParameterList) {
      parameterList = parseReferenceParameterList(lexer, true);
    }
    else {
      parameterList = ASTFactory.composite(REFERENCE_PARAMETER_LIST);
    }
    TreeUtil.addChildren(refElement, parameterList);

    while (lexer.getTokenType() == DOT) {
      final LexerPosition dotPos = lexer.getCurrentPosition();
      TreeElement dot = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
      lexer.advance();
      if (lexer.getTokenType() == IDENTIFIER) {
        identifier = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
        lexer.advance();
      }
      else{
        if (!allowIncomplete){
          lexer.restore(dotPos);
          return refElement;
        }
        identifier = null;
      }

      CompositeElement refElement1 = ASTFactory.composite(JAVA_CODE_REFERENCE);
      TreeUtil.addChildren(refElement1, refElement);
      TreeUtil.addChildren(refElement1, dot);
      if (identifier == null){
        TreeUtil.addChildren(refElement1, Factory.createErrorElement(JavaErrorMessages.message("expected.identifier")));
        TreeUtil.addChildren(refElement1, ASTFactory.composite(REFERENCE_PARAMETER_LIST));
        return refElement1;
      }
      TreeUtil.addChildren(refElement1, identifier);
      CompositeElement parameterList1;
      if (parseParameterList) {
        parameterList1 = parseReferenceParameterList(lexer, true);
      }
      else {
        parameterList1 = ASTFactory.composite(REFERENCE_PARAMETER_LIST);
      }
      TreeUtil.addChildren(refElement1, parameterList1);
      refElement = refElement1;
    }

    return refElement;
  }

  public CompositeElement parseReferenceParameterList(Lexer lexer, boolean allowWildcard) {
    final CompositeElement list = ASTFactory.composite(REFERENCE_PARAMETER_LIST);
    if (lexer.getTokenType() != LT) return list;
    final TreeElement lt = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
    TreeUtil.addChildren(list, lt);
    lexer.advance();
    while (true) {
      final CompositeElement typeElement = parseType(lexer, true, allowWildcard);
      if (typeElement != null) {
        TreeUtil.addChildren(list, typeElement);
      } else {
        final CompositeElement errorElement = Factory.createErrorElement(JavaErrorMessages.message("expected.identifier"));
        TreeUtil.addChildren(list, errorElement);
      }

      if (lexer.getTokenType() == GT) {
        final TreeElement gt = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
        TreeUtil.addChildren(list, gt);
        lexer.advance();
        return list;
      } else if (lexer.getTokenType() == COMMA) {
        final TreeElement comma = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
        TreeUtil.addChildren(list, comma);
        lexer.advance();
      } else {
        final CompositeElement errorElement = Factory.createErrorElement(JavaErrorMessages.message("expected.gt.or.comma"));
        TreeUtil.addChildren(list, errorElement);
        return list;
      }
    }
  }

  public static CompositeElement parseTypeText(PsiManager manager, CharSequence buffer, int startOffset, int endOffset, CharTable table) {
    Lexer originalLexer = new JavaLexer(LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    FilterLexer lexer = new FilterLexer(originalLexer, new FilterLexer.SetFilter(StdTokenSets.WHITE_SPACE_OR_COMMENT_BIT_SET));
    lexer.start(buffer, startOffset, endOffset, 0);
    final JavaParsingContext context = new JavaParsingContext(table,
                                                              LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    CompositeElement type = context.getStatementParsing().parseTypeWithEllipsis(lexer);
    if (type == null) return null;
    if (lexer.getTokenType() != null) return null;
    final FileElement dummyRoot = DummyHolderFactory.createHolder(manager, null, table).getTreeElement();
    TreeUtil.addChildren(dummyRoot, type);

    ParseUtil.insertMissingTokens(dummyRoot, originalLexer, startOffset, endOffset, -1, WhiteSpaceAndCommentsProcessor.INSTANCE, context);
    return type;
  }

  public CompositeElement parseTypeWithEllipsis(Lexer lexer, boolean eatLastDot, boolean allowWilcard) {
    CompositeElement type = parseType(lexer, eatLastDot, allowWilcard);
    if (type == null) return null;
    if (lexer.getTokenType() == ELLIPSIS) {
      CompositeElement type1 = ASTFactory.composite(TYPE);
      TreeUtil.addChildren(type1, type);
      TreeUtil.addChildren(type1, ParseUtil.createTokenElement(lexer, myContext.getCharTable()));
      lexer.advance();
      type = type1;
    }

    return type;

  }
  public CompositeElement parseTypeWithEllipsis(Lexer lexer) {
    return parseTypeWithEllipsis(lexer, true, true);
  }

  public CompositeElement parseType(Lexer lexer){
    return parseType(lexer, true, true);
  }

  public CompositeElement parseType(Lexer lexer, boolean eatLastDot, boolean allowWildcard){
    IElementType tokenType = lexer.getTokenType();
    TreeElement refElement;
    if (tokenType == null){
      return null;
    }
    else if (PRIMITIVE_TYPE_BIT_SET.contains(tokenType)){
      refElement = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
      lexer.advance();
    }
    else if (tokenType == IDENTIFIER){
      refElement = parseJavaCodeReference(lexer, eatLastDot, true);
    }
    else if (allowWildcard && lexer.getTokenType() == QUEST) {
      return parseWildcardType(lexer);
    }
    else{
      return null;
    }
    CompositeElement type = ASTFactory.composite(TYPE);
    TreeUtil.addChildren(type, refElement);
    while(lexer.getTokenType() == LBRACKET){
      final LexerPosition lbracketPos = lexer.getCurrentPosition();
      TreeElement lbracket = ParseUtil.createTokenElement(lexer, myContext.getCharTable());
      lexer.advance();
      if (lexer.getTokenType() != RBRACKET){
        lexer.restore(lbracketPos);
        break;
      }
      CompositeElement type1 = ASTFactory.composite(TYPE);
      TreeUtil.addChildren(type1, type);
      TreeUtil.addChildren(type1, lbracket);
      TreeUtil.addChildren(type1, ParseUtil.createTokenElement(lexer, myContext.getCharTable()));
      lexer.advance();
      type = type1;
    }
    return type;
  }

  private CompositeElement parseWildcardType(Lexer lexer) {
    LOG.assertTrue(lexer.getTokenType() == QUEST);
    CompositeElement type = ASTFactory.composite(TYPE);
    TreeUtil.addChildren(type, ParseUtil.createTokenElement(lexer, myContext.getCharTable()));
    lexer.advance();
    if (lexer.getTokenType() == SUPER_KEYWORD || lexer.getTokenType() == EXTENDS_KEYWORD) {
      TreeUtil.addChildren(type, ParseUtil.createTokenElement(lexer, myContext.getCharTable()));
      lexer.advance();
      CompositeElement boundType = parseType(lexer, true, false);
      if (boundType != null) {
        TreeUtil.addChildren(type, boundType);
      }
      else {
        TreeUtil.addChildren(type, Factory.createErrorElement(JavaErrorMessages.message("expected.type")));
      }
    }
    return type;
  }

  public static TreeElement parseTypeText(PsiManager manager,
                                          Lexer lexer,
                                          CharSequence buffer,
                                          int startOffset,
                                          int endOffset,
                                          int state,
                                          CharTable table) {
    if (lexer == null){
      lexer = new JavaLexer(LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    }
    FilterLexer filterLexer = new FilterLexer(lexer, new FilterLexer.SetFilter(StdTokenSets.WHITE_SPACE_OR_COMMENT_BIT_SET));
    if (state < 0) filterLexer.start(buffer, startOffset, endOffset,0);
    else filterLexer.start(buffer, startOffset, endOffset, state);
    final JavaParsingContext context = new JavaParsingContext(table,
                                                              LanguageLevelProjectExtension.getInstance(manager.getProject()).getLanguageLevel());
    final FileElement dummyRoot = DummyHolderFactory.createHolder(manager, null, context.getCharTable()).getTreeElement();
    final CompositeElement root = context.getStatementParsing().parseType(filterLexer);

    if (root != null) {
      TreeUtil.addChildren(dummyRoot, root);
    }

    if (filterLexer.getTokenType() == ELLIPSIS) {
      TreeUtil.addChildren(dummyRoot, ParseUtil.createTokenElement(filterLexer, context.getCharTable()));
      filterLexer.advance();
    }

    if (filterLexer.getTokenType() != null) {
      final CompositeElement errorElement = Factory.createErrorElement(JavaErrorMessages.message("unexpected.tokens"));
      while (filterLexer.getTokenType() != null) {
        final TreeElement token = ParseUtil.createTokenElement(lexer, context.getCharTable());
        TreeUtil.addChildren(errorElement, token);
        filterLexer.advance();
      }
      TreeUtil.addChildren(dummyRoot, errorElement);
    }

    ParseUtil.insertMissingTokens(
      dummyRoot,
      lexer,
      startOffset,
      endOffset, state,
      WhiteSpaceAndCommentsProcessor.INSTANCE, context);
    return dummyRoot.getFirstChildNode();
 }
}