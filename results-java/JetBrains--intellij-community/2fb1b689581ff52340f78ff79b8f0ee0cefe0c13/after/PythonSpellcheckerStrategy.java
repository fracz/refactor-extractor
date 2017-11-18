package com.jetbrains.python.spellchecker;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.Splitter;
import com.intellij.spellchecker.inspections.SplitterFactory;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Token;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.inspections.PyStringFormatParser;
import com.jetbrains.python.psi.PyBinaryExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class PythonSpellcheckerStrategy extends SpellcheckingStrategy {
  private static class StringLiteralTokenizer extends Tokenizer<PyStringLiteralExpression> {
    @Override
    public void tokenize(@NotNull PyStringLiteralExpression element, TokenConsumer consumer) {
      Splitter splitter = SplitterFactory.getInstance().getStringLiteralSplitter();
      String text = element.getText();
      if (text.startsWith("u") || text.startsWith("U") || text.startsWith("r") || text.startsWith("R") ||
          text.startsWith("b") || text.startsWith("B")) {
        String stringValue = element.getStringValue();
        List<TextRange> valueTextRanges = element.getStringValueTextRanges();
        consumer.consumeToken(element, stringValue, valueTextRanges.get(0).getStartOffset(), splitter);
      }
      else {
        consumer.consumeToken(element, splitter);
      }
    }
  }

  private static class FormatStringTokenizer extends Tokenizer<PyStringLiteralExpression> {
    @Override
    public void tokenize(@NotNull PyStringLiteralExpression element, TokenConsumer consumer) {
      // TODO this doesn't work correctly for a string with escaped characters
      String stringValue = element.getStringValue();
      List<TextRange> valueTextRanges = element.getStringValueTextRanges();
      List<PyStringFormatParser.FormatStringChunk> chunks = new PyStringFormatParser(stringValue).parse();
      Splitter splitter = SplitterFactory.getInstance().getStringLiteralSplitter();
      for (PyStringFormatParser.FormatStringChunk chunk : chunks) {
        if (chunk instanceof PyStringFormatParser.ConstantChunk) {
          String text = stringValue.substring(chunk.getStartIndex(), chunk.getEndIndex());
          consumer.consumeToken(element, text, valueTextRanges.get(0).getStartOffset() + chunk.getStartIndex(), splitter);
        }
      }
    }
  }

  private StringLiteralTokenizer myStringLiteralTokenizer = new StringLiteralTokenizer();
  private FormatStringTokenizer myFormatStringTokenizer = new FormatStringTokenizer();

  @NotNull
  @Override
  public Language getLanguage() {
    return PythonLanguage.getInstance();
  }

  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof PyStringLiteralExpression) {
      PsiElement parent = element.getParent();
      if (parent instanceof PyBinaryExpression) {
        PyBinaryExpression binaryExpression = (PyBinaryExpression)parent;
        if (element == binaryExpression.getLeftExpression() && binaryExpression.getOperator() == PyTokenTypes.PERC) {
          return myFormatStringTokenizer;
        }
      }
      return myStringLiteralTokenizer;
    }
    return super.getTokenizer(element);
  }
}