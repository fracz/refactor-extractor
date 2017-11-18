/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.codeInsight;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.codeInsight.completion.simple.CharTailType;
import com.intellij.codeInsight.completion.simple.RParenthTailType;

/**
 * @author peter
 */
public abstract class TailType {
  private final int myOldTailType;

  protected TailType(final int oldTailType) {
    myOldTailType = oldTailType;
  }

  public final int getOldTailType() {
    return myOldTailType;
  }

  public static int insertChar(final Editor editor, final int tailOffset, final char c) {
    Document document = editor.getDocument();
    int textLength = document.getTextLength();
    CharSequence chars = document.getCharsSequence();
    if (tailOffset == textLength || chars.charAt(tailOffset) != c){
      document.insertString(tailOffset, String.valueOf(c));
    }
    return moveCaret(editor, tailOffset, 1);
  }

  protected static int moveCaret(final Editor editor, final int tailOffset, final int delta) {
    final CaretModel model = editor.getCaretModel();
    if (model.getOffset() == tailOffset) {
      model.moveToOffset(tailOffset + delta);
    }
    return tailOffset + delta;
  }

  public static final TailType UNKNOWN = new TailType(-1){
    public int processTail(final Editor editor, final int tailOffset) {
      return tailOffset;
    }
  };

  public static final TailType NONE = new TailType(0){
    public int processTail(final Editor editor, final int tailOffset) {
      return tailOffset;
    }
  };

  public static final TailType SEMICOLON = new CharTailType(1, ';');

  public static final TailType COMMA = new TailType(2){
    public int processTail(final Editor editor, int tailOffset) {
      CodeStyleSettings styleSettings = CodeStyleSettingsManager.getSettings(editor.getProject());
      if (styleSettings.SPACE_BEFORE_COMMA) tailOffset = insertChar(editor, tailOffset, ' ');
      tailOffset = insertChar(editor, tailOffset, ',');
      if (styleSettings.SPACE_AFTER_COMMA) tailOffset = insertChar(editor, tailOffset, ' ');
      return tailOffset;
    }
  };
  public static final TailType SPACE = new CharTailType(3, ' ');
  public static final TailType DOT = new CharTailType(4, '.');

  public static final TailType CAST_RPARENTH = new RParenthTailType(5){
    protected boolean isSpaceWithinParentheses(final CodeStyleSettings styleSettings, final Editor editor, final int tailOffset) {
      editor.getCaretModel().moveToOffset(tailOffset);
      return styleSettings.SPACE_WITHIN_CAST_PARENTHESES;
    }

    public int processTail(final Editor editor, int tailOffset) {
      FeatureUsageTracker.getInstance().triggerFeatureUsed("editing.completion.smarttype.casting");
      tailOffset = super.processTail(editor, tailOffset);
      if (CodeStyleSettingsManager.getSettings(editor.getProject()).SPACE_AFTER_TYPE_CAST){
        tailOffset = insertChar(editor, tailOffset, ' ');
      }
      return tailOffset;
    }
  };
  public static final TailType CALL_RPARENTH = new RParenthTailType(6){
    protected boolean isSpaceWithinParentheses(final CodeStyleSettings styleSettings, final Editor editor, final int tailOffset) {
      return styleSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES;
    }
  };
  public static final TailType IF_RPARENTH = new RParenthTailType(7){
    protected boolean isSpaceWithinParentheses(final CodeStyleSettings styleSettings, final Editor editor, final int tailOffset) {
      return styleSettings.SPACE_WITHIN_IF_PARENTHESES;
    }
  };
  public static final TailType WHILE_RPARENTH = new RParenthTailType(8){
    protected boolean isSpaceWithinParentheses(final CodeStyleSettings styleSettings, final Editor editor, final int tailOffset) {
      return styleSettings.SPACE_WITHIN_WHILE_PARENTHESES;
    }
  };
  public static final TailType CALL_RPARENTH_SEMICOLON = new RParenthTailType(9){
    protected boolean isSpaceWithinParentheses(final CodeStyleSettings styleSettings, final Editor editor, final int tailOffset) {
      return styleSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES;
    }

    public int processTail(final Editor editor, int tailOffset) {
      return insertChar(editor, super.processTail(editor, tailOffset), ';');
    }
  };
  public static final TailType CASE_COLON = new CharTailType(10, ':');
  public static final TailType COND_EXPR_COLON = new TailType(11){
    public int processTail(final Editor editor, final int tailOffset) {
      Document document = editor.getDocument();
      int textLength = document.getTextLength();
      CharSequence chars = document.getCharsSequence();

      if (tailOffset < textLength - 1 && chars.charAt(tailOffset) == ' ' && chars.charAt(tailOffset + 1) == ':') {
        return moveCaret(editor, tailOffset, 2);
      }
      if (tailOffset < textLength && chars.charAt(tailOffset) == ':') {
        return moveCaret(editor, tailOffset, 1);
      }
      document.insertString(tailOffset, " : ");
      return moveCaret(editor, tailOffset, 3);
    }
  };
  public static final TailType EQ = new TailType(12){
    public int processTail(final Editor editor, int tailOffset) {
      CodeStyleSettings styleSettings = CodeStyleSettingsManager.getSettings(editor.getProject());
      Document document = editor.getDocument();
      int textLength = document.getTextLength();
      CharSequence chars = document.getCharsSequence();
      if (tailOffset < textLength - 1 && chars.charAt(tailOffset) == ' ' && chars.charAt(tailOffset + 1) == '='){
        return moveCaret(editor, tailOffset, 2);
      }
      if (tailOffset < textLength && chars.charAt(tailOffset) == '='){
        return moveCaret(editor, tailOffset, 1);
      }
      if (styleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS){
        document.insertString(tailOffset, " =");
        tailOffset = moveCaret(editor, tailOffset, 2);
      }
      else{
        document.insertString(tailOffset, "=");
        tailOffset = moveCaret(editor, tailOffset, 1);

      }
      if (styleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS){
        tailOffset = insertChar(editor, tailOffset, ' ');
      }
      return tailOffset;
    }
  };
  public static final TailType LPARENTH = new TailType(13){
    public int processTail(final Editor editor, final int tailOffset) {
      return tailOffset;
    }
  };

  public abstract int processTail(final Editor editor, int tailOffset);

  public static TailType getSimpleTailType(final int old) {
    switch (old) {
      case -1: return UNKNOWN;
      case 0: return NONE;
      case 1: return SEMICOLON;
      case 2: return COMMA;
      case 3: return SPACE;
      case 4: return DOT;
      case 5: return CAST_RPARENTH;
      case 6: return CALL_RPARENTH;
      case 7: return IF_RPARENTH;
      case 8: return WHILE_RPARENTH;
      case 9: return CALL_RPARENTH_SEMICOLON;
      case 10: return CASE_COLON;
      case 11: return COND_EXPR_COLON;
      case 12: return EQ;
      case 13: return LPARENTH;
    }
    return new TailType(old) {
      public int processTail(final Editor editor, final int tailOffset) {
        return insertChar(editor, tailOffset, (char) old);
      }
    };
  }
}