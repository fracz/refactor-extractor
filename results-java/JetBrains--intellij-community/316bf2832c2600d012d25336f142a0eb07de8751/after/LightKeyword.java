package com.intellij.psi.impl.light;

import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class LightKeyword extends LightElement implements PsiKeyword, PsiJavaToken {
  private final String myText;

  public LightKeyword(PsiManager manager, String text) {
    super(manager, StdFileTypes.JAVA.getLanguage());
    myText = text;
  }

  public String getText(){
    return myText;
  }

  public IElementType getTokenType(){
    Lexer lexer = new JavaLexer(LanguageLevel.HIGHEST);
    lexer.start(myText);
    return lexer.getTokenType();
  }

  public void accept(@NotNull PsiElementVisitor visitor){
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitKeyword(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  public PsiElement copy(){
    return new LightKeyword(getManager(), myText);
  }

  public String toString(){
    return "PsiKeyword:" + getText();
  }
}