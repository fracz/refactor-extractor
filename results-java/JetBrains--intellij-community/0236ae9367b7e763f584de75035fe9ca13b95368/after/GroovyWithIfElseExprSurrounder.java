package org.jetbrains.plugins.groovy.lang.surroundWith.surrounders.surroundersImpl.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrIfStatement;

/**
 * User: Dmitry.Krasilschikov
 * Date: 25.05.2007
 */
public class GroovyWithIfElseExprSurrounder extends GroovyWithIfExprSurrounder {
  protected String getExpressionTemplateAsString(ASTNode node) {
    return super.getExpressionTemplateAsString(node) + " else { \n }";
  }

//  protected TextRange getSurroundSelectionRange(GroovyPsiElement element) {
//    assert element instanceof GrIfStatement;
//
//    GrIfStatement grIfStatement = (GrIfStatement) element;
//    int endOffset = grIfStatement.getThenBranch().getTextRange().getEndOffset();
//
//    return new TextRange(endOffset, endOffset);
//  }

  public String getTemplateDescription() {
    return "if (...) / else";
  }
}