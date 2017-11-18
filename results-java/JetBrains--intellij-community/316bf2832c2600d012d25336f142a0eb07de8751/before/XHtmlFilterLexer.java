package com.intellij.psi.impl.cache.impl.idCache;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.CacheUtil;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElementType;

public class XHtmlFilterLexer extends BaseFilterLexer {

  public XHtmlFilterLexer(Lexer originalLexer, OccurrenceConsumer table) {
    super(originalLexer, table);
  }

  public void advance() {
    final IElementType tokenType = myOriginalLexer.getTokenType();

    if (tokenType == XmlElementType.XML_COMMENT_CHARACTERS) {
      scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false);
      advanceTodoItemCountsInToken();
    } else if (tokenType == XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN ||
        tokenType == XmlElementType.XML_NAME ||
        tokenType == XmlElementType.XML_TAG_NAME
       ) {
      scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT | UsageSearchContext.IN_FOREIGN_LANGUAGES, tokenType == XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN,
                       false);
    } else if (tokenType.getLanguage() != XMLLanguage.INSTANCE &&
      tokenType.getLanguage() != Language.ANY
    ) {
      boolean inComments = CacheUtil.isInComments(tokenType);
      scanWordsInToken((inComments)?UsageSearchContext.IN_COMMENTS:UsageSearchContext.IN_PLAIN_TEXT | UsageSearchContext.IN_FOREIGN_LANGUAGES, true,
                       false);

      if (inComments) advanceTodoItemCountsInToken();
    } else {
      scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT, false, false);
    }

    myOriginalLexer.advance();
  }

}