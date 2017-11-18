package com.intellij.psi.impl.cache.impl;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.impl.cache.impl.id.IdTableBuilding;
import com.intellij.psi.search.IndexPattern;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.util.text.CharSequenceSubSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseFilterLexer extends LexerBase implements IdTableBuilding.ScanWordProcessor {
  protected final Lexer myOriginalLexer;

  private final OccurrenceConsumer myOccurrenceConsumer;

  private int myTodoScannedBound = 0;
  private int myOccurenceMask;
  private CharSequence myBuffer;
  private char[] myBufferArray;
  private TodoScanningData[] myTodoScanningData;

  public static interface OccurrenceConsumer {
    void addOccurrence(CharSequence charSequence, int start, int end, int occurrenceMask);
    void addOccurrence(char[] chars, int start, int end, int occurrenceMask);

    boolean canConsumeTodoOccurrences();
    void incTodoOccurrence(IndexPattern pattern);
  }

  protected BaseFilterLexer(Lexer originalLexer, OccurrenceConsumer occurrenceConsumer) {
    myOriginalLexer = originalLexer;
    myOccurrenceConsumer = occurrenceConsumer;
  }

  public OccurrenceConsumer getOccurrenceConsumer() {
    return myOccurrenceConsumer;
  }

  public void start(char[] buffer, int startOffset, int endOffset, int initialState) {
    myOriginalLexer.start(buffer, startOffset, endOffset, initialState);
  }

  public final CharSequence getBufferSequence() {
    return myBuffer;
  }

  public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    myBuffer = buffer;
    myBufferArray = CharArrayUtil.fromSequenceWithoutCopying(myBuffer);
    myOriginalLexer.start(buffer, startOffset, endOffset, initialState);
  }

  public int getState() {
    return myOriginalLexer.getState();
  }

  public final IElementType getTokenType() {
    return myOriginalLexer.getTokenType();
  }

  public final int getTokenStart() {
    return myOriginalLexer.getTokenStart();
  }

  public final int getTokenEnd() {
    return myOriginalLexer.getTokenEnd();
  }

  public final char[] getBuffer() {
    return myOriginalLexer.getBuffer();
  }

  public int getBufferEnd() {
    return myOriginalLexer.getBufferEnd();
  }

  protected final void advanceTodoItemCountsInToken() {
    if (myOccurrenceConsumer.canConsumeTodoOccurrences()){
      int start = getTokenStart();
      int end = getTokenEnd();
      start = Math.max(start, myTodoScannedBound);
      if (start >= end) return; // this prevents scanning of the same comment twice

      CharSequence input = new CharSequenceSubSequence(myBuffer, start, end);
      myTodoScanningData = advanceTodoItemsCount(input, myOccurrenceConsumer, myTodoScanningData);

      myTodoScannedBound = end;
    }
  }

  public static class TodoScanningData {
    final IndexPattern pattern;
    final Matcher matcher;

    public TodoScanningData(IndexPattern pattern, Matcher matcher) {
      this.matcher = matcher;
      this.pattern = pattern;
    }
  }

  public static TodoScanningData[] advanceTodoItemsCount(final CharSequence input, final OccurrenceConsumer consumer, TodoScanningData[] todoScanningData) {
    if (todoScanningData == null) {
      IndexPattern[] patterns = CacheUtil.getIndexPatterns();
      todoScanningData = new TodoScanningData[patterns.length];

      for (int i = 0; i < patterns.length; ++i) {
        IndexPattern indexPattern = patterns[i];
        Pattern pattern = indexPattern.getPattern();

        if (pattern != null) {
          todoScanningData [i] = new TodoScanningData(indexPattern, pattern.matcher(""));
        }
      }
    }

    for (TodoScanningData data:todoScanningData) {
      if (data == null) continue;
      Matcher matcher = data.matcher;
      matcher.reset(input);

      while (matcher.find()) {
        if (matcher.start() != matcher.end()) {
          consumer.incTodoOccurrence(data.pattern);
        }
      }
    }

    return todoScanningData;
  }

  public final void run(CharSequence chars, int start, int end, char[] charArray) {
    if (charArray != null) {
      myOccurrenceConsumer.addOccurrence(charArray, start, end, myOccurenceMask);
    } else {
      myOccurrenceConsumer.addOccurrence(chars, start, end, myOccurenceMask);
    }
  }

  protected final void addOccurrenceInToken(final int occurrenceMask) {
    if (myBufferArray != null) {
      myOccurrenceConsumer.addOccurrence(myBufferArray, getTokenStart(), getTokenEnd(), occurrenceMask);
    }
    else {
      myOccurrenceConsumer.addOccurrence(myBuffer, getTokenStart(), getTokenEnd(), occurrenceMask);
    }
  }

  protected final void scanWordsInToken(final int occurrenceMask, boolean mayHaveFileRefs, final boolean mayHaveEscapes) {
    myOccurenceMask = occurrenceMask;
    final int start = getTokenStart();
    final int end = getTokenEnd();
    IdTableBuilding.scanWords(this, myBuffer, myBufferArray, start, end, mayHaveEscapes);

    if (mayHaveFileRefs) {
      processPossibleComplexFileName(myBuffer, myBufferArray, start, end);
    }
  }

  private void processPossibleComplexFileName(CharSequence chars, char[] charArray, int startOffset, int endOffset) {
    int offset = findCharsWithinRange(chars, charArray,startOffset, endOffset, "/\\");
    offset = Math.min(offset, endOffset);
    int start = startOffset;

    while(start < endOffset) {
      if (start != offset) {
        if (charArray != null) {
          myOccurrenceConsumer.addOccurrence(charArray, start, offset, UsageSearchContext.IN_FOREIGN_LANGUAGES);
        } else {
          myOccurrenceConsumer.addOccurrence(chars, start, offset,UsageSearchContext.IN_FOREIGN_LANGUAGES);
        }
      }
      start = offset + 1;
      offset = Math.min(endOffset, findCharsWithinRange(chars, charArray, start, endOffset, "/\\"));
    }
  }

  private static int findCharsWithinRange(final CharSequence chars, final char[] charArray, int startOffset, int endOffset, String charsToFind) {
    while(startOffset < endOffset) {
      if (charsToFind.indexOf(charArray != null ? charArray[startOffset]:chars.charAt(startOffset)) != -1) {
        return startOffset;
      }
      ++startOffset;
    }

    return startOffset;
  }

}