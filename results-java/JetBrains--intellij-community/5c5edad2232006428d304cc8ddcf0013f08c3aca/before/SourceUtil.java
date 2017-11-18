package com.intellij.psi.impl.source.tree;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.Constants;
import com.intellij.psi.impl.source.SourceJavaCodeReference;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.parsing.ExpressionParsing;
import com.intellij.psi.impl.source.parsing.Parsing;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import com.intellij.lang.ASTNode;

/**
 *
 */
public class SourceUtil implements Constants {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.SourceUtil");

  public static int toBuffer(ASTNode element, char[] buffer, int offset) {
    return toBuffer(element, buffer, offset, null, SharedImplUtil.findCharTableByTree(element));
  }

  public static int toBuffer(ASTNode element, char[] buffer, int offset, CharTable table) {
    return toBuffer(element, buffer, offset, null, table);
  }

  private static int toBuffer(ASTNode element, char[] buffer, int offset, TokenSet skipTypes, CharTable table) {
    synchronized (PsiLock.LOCK) {
      if (skipTypes != null && skipTypes.isInSet(element.getElementType())) return offset;
      if (element instanceof LeafElement) {
        return ((LeafElement)element).copyTo(buffer, offset, table);
      }
      else {
        int curOffset = offset;
        for (ASTNode child = element.getFirstChildNode(); child != null; child = child.getTreeNext()) {
          curOffset = toBuffer(child, buffer, curOffset, skipTypes, table);
        }
        return curOffset;
      }
    }
  }

  public static String getTextSkipWhiteSpaceAndComments(ASTNode element) {
    final CharTable table = SharedImplUtil.findCharTableByTree(element);
    int length = toBuffer(element, null, 0, WHITE_SPACE_OR_COMMENT_BIT_SET, table);
    char[] buffer = new char[length];
    toBuffer(element, buffer, 0, WHITE_SPACE_OR_COMMENT_BIT_SET, table);
    return new String(buffer);
  }

  public static TreeElement addParenthToReplacedChild(final IElementType parenthType,
                                                      TreeElement newChild,
                                                      PsiManager manager) {
    CompositeElement parenthExpr = Factory.createCompositeElement(parenthType);

    TreeElement dummyExpr = (TreeElement)newChild.clone();
    final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(newChild);
    new DummyHolder(manager, parenthExpr, null, charTableByTree);
    parenthExpr.putUserData(CharTable.CHAR_TABLE_KEY, charTableByTree);
    TreeUtil.addChildren(parenthExpr, Factory.createLeafElement(JavaTokenType.LPARENTH, new char[]{'('}, 0, 1, -1, charTableByTree));
    TreeUtil.addChildren(parenthExpr, dummyExpr);
    TreeUtil.addChildren(parenthExpr, Factory.createLeafElement(JavaTokenType.RPARENTH, new char[]{')'}, 0, 1, -1, charTableByTree));

    try {
      CodeStyleManager codeStyleManager = manager.getCodeStyleManager();
      parenthExpr =
      (CompositeElement)SourceTreeToPsiMap.psiElementToTree(
        codeStyleManager.reformat(SourceTreeToPsiMap.treeElementToPsi(parenthExpr)));
    }
    catch (IncorrectOperationException e) {
      LOG.error(e); // should not happen
    }

    newChild.putUserData(CharTable.CHAR_TABLE_KEY, SharedImplUtil.findCharTableByTree(newChild));
    TreeUtil.replaceWithList(dummyExpr, newChild);

    newChild = parenthExpr;
    // TODO remove explicit caches drop since this should be ok iff we will use ChangeUtil for the modification
    TreeUtil.clearCaches(newChild);
    return newChild;
  }

  public static void fullyQualifyReference(CompositeElement reference, PsiClass targetClass) {
    if (((SourceJavaCodeReference)reference).isQualified()) { // qualifed reference
      final PsiClass parentClass = targetClass.getContainingClass();
      if (parentClass == null) return;
      final ASTNode qualifier = reference.findChildByRole(ChildRole.QUALIFIER);
      if (qualifier instanceof SourceJavaCodeReference) {
        ((SourceJavaCodeReference)qualifier).fullyQualify(parentClass);
      }
    }
    else { // unqualified reference, need to qualify with package name
      final String qName = targetClass.getQualifiedName();
      if (qName == null) {
        return; // todo: local classes?
      }
      final int i = qName.lastIndexOf('.');
      if (i > 0) {
        final String prefix = qName.substring(0, i);
        PsiManager manager = reference.getManager();
        char[] chars = prefix.toCharArray();
        final CharTable table = SharedImplUtil.findCharTableByTree(reference);
        final CompositeElement qualifier;
        if (reference instanceof PsiReferenceExpression) {
          qualifier = ExpressionParsing.parseExpressionText(manager, chars, 0, chars.length, table);
        }
        else {
          qualifier = Parsing.parseJavaCodeReferenceText(manager, chars, table);
        }
        if (qualifier != null) {
          final CharTable systemCharTab = SharedImplUtil.findCharTableByTree(qualifier);
          final LeafElement dot = Factory.createSingleLeafElement(DOT, new char[]{'.'}, 0, 1, systemCharTab, SharedImplUtil.getManagerByTree(qualifier));
          TreeUtil.insertAfter(qualifier, dot);
          reference.addInternal(qualifier, dot, null, Boolean.FALSE);
        }
      }
    }
  }

  public static void dequalifyImpl(CompositeElement reference) {
    final ASTNode qualifier = reference.findChildByRole(ChildRole.QUALIFIER);
    if (qualifier == null) return;
    reference.deleteChildInternal(qualifier);
  }
}