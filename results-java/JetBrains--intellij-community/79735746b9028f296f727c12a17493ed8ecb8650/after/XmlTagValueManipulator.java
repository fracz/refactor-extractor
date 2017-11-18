package com.intellij.psi.impl.source.resolve.reference.impl.manipulators;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.impl.source.resolve.reference.AbstractElementManipulator;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;

/**
 * @author Maxim.Mossienko
 */
public class XmlTagValueManipulator extends AbstractElementManipulator<XmlTag> {

  public XmlTag handleContentChange(XmlTag tag, TextRange range, String newContent) throws IncorrectOperationException {

    final StringBuffer replacement = new StringBuffer( tag.getValue().getText() );
    final int valueOffset = tag.getValue().getTextRange().getStartOffset() - tag.getTextOffset();

    replacement.replace(
      range.getStartOffset() - valueOffset,
      range.getEndOffset() - valueOffset,
      newContent
    );
    tag.getValue().setText(replacement.toString());
    return tag;
  }

  public TextRange getRangeInElement(final XmlTag element) {
    TextRange valueRange = element.getValue().getTextRange();
    String url = element.getText();
    int start = valueRange.getStartOffset() - element.getTextOffset();
    while (url.charAt(start) <= ' ') {
      start++;
    }
    int end = valueRange.getEndOffset() - element.getTextOffset();
    while (end > start && url.charAt(end - 1) <= ' ') {
      end--;
    }

    return new TextRange(start, end);
  }
}