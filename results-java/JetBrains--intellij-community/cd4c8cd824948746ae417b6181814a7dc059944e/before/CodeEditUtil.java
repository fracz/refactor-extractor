/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.psi.impl.source.codeStyle;

import com.intellij.lang.*;
import com.intellij.openapi.command.AbnormalCommandTerminationException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CodeEditUtil {
  private static final Key<Boolean> GENERATED_FLAG = new Key<Boolean>("GENERATED_FLAG");
  private static final Key<Integer> INDENT_INFO = new Key<Integer>("INDENT_INFO");
  private static final Key<Boolean> REFORMAT_BEFORE_KEY = new Key<Boolean>("REFORMAT_BEFORE_KEY");
  private static final Key<Boolean> REFORMAT_KEY = new Key<Boolean>("REFORMAT_KEY");
  private static final Key<Boolean> DISABLE_POSTPONED_REFORMAT_KEY = new Key<Boolean>("DISABLE_POSTPONED_REFORMAT_KEY");
  private static final ThreadLocal<Boolean> ALLOW_TO_MARK_NODES_TO_REFORMAT = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.TRUE;
    }
  };
  private static final ThreadLocal<Boolean> ALLOW_NODES_REFORMATTING = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.TRUE;
    }
  };

  public static final Key<Boolean> OUTER_OK = new Key<Boolean>("OUTER_OK");

  private CodeEditUtil() {
  }

  public static void addChild(ASTNode parent, ASTNode child, ASTNode anchorBefore) {
    addChildren(parent, child, child, anchorBefore);
  }

  public static void removeChild(ASTNode parent, @NotNull ASTNode child) {
    removeChildren(parent, child, child);
  }

  public static ASTNode addChildren(ASTNode parent, @NotNull ASTNode first, @NotNull ASTNode last, ASTNode anchorBefore) {
    ASTNode lastChild = last.getTreeNext();
    ASTNode current = first;
    while(current != lastChild){
      saveWhitespacesInfo(current);
      checkForOuters(current);
      current = current.getTreeNext();
    }

    if (anchorBefore != null && isComment(anchorBefore.getElementType())) {
      final ASTNode anchorPrev = anchorBefore.getTreePrev();
      if (anchorPrev != null && anchorPrev.getElementType() == TokenType.WHITE_SPACE) {
        anchorBefore = anchorPrev;
        /*
        final int blCount = getBlankLines(anchorPrev.getText());
        if (bl)
        */
      }
    }

    parent.addChildren(first, lastChild, anchorBefore);
    final ASTNode firstAddedLeaf = findFirstLeaf(first, last);
    final ASTNode prevLeaf = TreeUtil.prevLeaf(first);
    if(firstAddedLeaf != null){
      ASTNode placeHolderEnd = makePlaceHolderBetweenTokens(prevLeaf, firstAddedLeaf, isFormattingRequiered(prevLeaf, first), false);
      if(placeHolderEnd != prevLeaf && first == firstAddedLeaf) first = placeHolderEnd;
      final ASTNode lastAddedLeaf = findLastLeaf(first, last);
      placeHolderEnd = makePlaceHolderBetweenTokens(lastAddedLeaf, TreeUtil.nextLeaf(last), true, false);
      if(placeHolderEnd != lastAddedLeaf && lastAddedLeaf == first) first = placeHolderEnd;
    }
    else makePlaceHolderBetweenTokens(prevLeaf, TreeUtil.nextLeaf(last), isFormattingRequiered(prevLeaf, first), false);
    return first;
  }

  private static boolean isComment(IElementType type) {
    final ParserDefinition def = LanguageParserDefinitions.INSTANCE.forLanguage(type.getLanguage());
    return def != null && def.getCommentTokens().contains(type);
  }

  private static boolean isFormattingRequiered(final ASTNode prevLeaf, ASTNode first) {
    while(first != null) {
      ASTNode current = prevLeaf;
      while (current != null) {
        if (current.getTreeNext() == first) return true;
        current = current.getTreeParent();
      }
      final ASTNode parent = first.getTreeParent();
      if (parent != null && parent.getTextRange().equals(first.getTextRange())) {
        first = parent;
      }
      else {
        break;
      }
    }
    return false;
  }

  public static void checkForOuters(final ASTNode element) {
    if (element instanceof OuterLanguageElement && element.getCopyableUserData(OUTER_OK) == null) throw new AbnormalCommandTerminationException();
    ASTNode child = element.getFirstChildNode();
    while (child != null) {
      checkForOuters(child);
      child = child.getTreeNext();
    }
  }

  public static void saveWhitespacesInfo(final ASTNode first) {
    if(first == null || isNodeGenerated(first) || getOldIndentation(first) >= 0) return;
    PsiElement psiElement = first.getPsi();
    if (psiElement == null) {
      return;
    }
    final PsiFile containingFile = psiElement.getContainingFile();
    setOldIndentation((TreeElement)first, IndentHelper.getInstance().getIndent(containingFile.getProject(), containingFile.getFileType(), first));
  }

  public static int getOldIndentation(ASTNode node){
    if(node == null) return -1;
    final Integer stored = node.getCopyableUserData(INDENT_INFO);
    return stored != null ? stored : -1;
  }

  public static void removeChildren(ASTNode parent, @NotNull ASTNode first, @NotNull ASTNode last) {
    final boolean tailingElement = last.getStartOffset() + last.getTextLength() == parent.getStartOffset() + parent.getTextLength();
    final boolean forceReformat = needToForceReformat(parent, first, last);
    saveWhitespacesInfo(first);

    TreeElement child = (TreeElement)first;
    while (child != null) {
      //checkForOuters(child);
      if (child == last) break;
      child = child.getTreeNext();
    }

    final ASTNode prevLeaf = TreeUtil.prevLeaf(first);
    final ASTNode nextLeaf = TreeUtil.nextLeaf(first);
    parent.removeRange(first, last.getTreeNext());
    ASTNode nextLeafToAdjust = nextLeaf;
    if (nextLeafToAdjust != null && prevLeaf != null && nextLeafToAdjust.getTreeParent() == null) {
      //next element has invalidated
      nextLeafToAdjust = prevLeaf.getTreeNext();
    }
    makePlaceHolderBetweenTokens(prevLeaf, nextLeafToAdjust, forceReformat, tailingElement);
  }

  private static boolean needToForceReformat(final ASTNode parent, final ASTNode first, final ASTNode last) {
    return parent == null || first.getStartOffset() != parent.getStartOffset() ||
           parent.getText().trim().length() == getTrimmedTextLength(first, last) && needToForceReformat(parent.getTreeParent(), parent, parent);
  }

  private static int getTrimmedTextLength(ASTNode first, final ASTNode last) {
    final StringBuilder buffer = new StringBuilder();
    while(first != last.getTreeNext()) {
      buffer.append(first.getText());
      first = first.getTreeNext();
    }
    return buffer.toString().trim().length();
  }

  public static void replaceChild(ASTNode parent, @NotNull ASTNode oldChild, @NotNull ASTNode newChild) {
    saveWhitespacesInfo(oldChild);
    saveWhitespacesInfo(newChild);
    checkForOuters(oldChild);
    checkForOuters(newChild);

    LeafElement oldFirst = TreeUtil.findFirstLeaf(oldChild);

    parent.replaceChild(oldChild, newChild);
    final LeafElement firstLeaf = TreeUtil.findFirstLeaf(newChild);
    final ASTNode prevToken = TreeUtil.prevLeaf(newChild);
    if (firstLeaf != null) {
      final ASTNode nextLeaf = TreeUtil.nextLeaf(newChild);
      makePlaceHolderBetweenTokens(prevToken, firstLeaf, isFormattingRequiered(prevToken, newChild), false);
      if (nextLeaf != null && !CharArrayUtil.containLineBreaks(nextLeaf.getText())) {
        makePlaceHolderBetweenTokens(TreeUtil.prevLeaf(nextLeaf), nextLeaf, false, false);
      }
    }
    else {
      if (oldFirst != null && prevToken == null) {
        ASTNode whitespaceNode = newChild.getTreeNext();
        if (whitespaceNode != null && whitespaceNode.getElementType() == TokenType.WHITE_SPACE) {
          // Replacing non-empty prefix to empty shall remove whitespace
          parent.removeChild(whitespaceNode);
        }
      }

      makePlaceHolderBetweenTokens(prevToken, TreeUtil.nextLeaf(newChild), isFormattingRequiered(prevToken, newChild), false);
    }
  }

  @Nullable
  private static ASTNode findFirstLeaf(ASTNode first, ASTNode last) {
    do{
      final LeafElement leaf = TreeUtil.findFirstLeaf(first);
      if(leaf != null) return leaf;
      first = first.getTreeNext();
      if (first == null) return null;
    }
    while(first != last);
    return null;
  }

  @Nullable
  private static ASTNode findLastLeaf(ASTNode first, ASTNode last) {
    do{
      final ASTNode leaf = TreeUtil.findLastLeaf(last);
      if(leaf != null) return leaf;
      last = last.getTreePrev();
      if (last == null) return null;
    }
    while(first != last);
    return null;
  }

  @Nullable
  private static ASTNode makePlaceHolderBetweenTokens(ASTNode left, final ASTNode right, boolean forceReformat, final boolean normalizeTailingWhitespace) {
    if(right == null) return left;

    markToReformatBefore(right, false);
    if(left == null){
      markToReformatBefore(right, true);
    }
    else if(left.getElementType() == TokenType.WHITE_SPACE && left.getTreeNext() == null && normalizeTailingWhitespace){
      // handle tailing whitespaces if element on the left has been removed
      final ASTNode prevLeaf = TreeUtil.prevLeaf(left);
      left.getTreeParent().removeChild(left);
      markToReformatBeforeOrInsertWhitespace(prevLeaf, right);
      left = right;
    }
    else if(left.getElementType() == TokenType.WHITE_SPACE && right.getElementType() == TokenType.WHITE_SPACE) {
      final String text;
      final int leftBlankLines = getBlankLines(left.getText());
      final int rightBlankLines = getBlankLines(right.getText());
      final boolean leaveRightText = leftBlankLines < rightBlankLines;
      if (leftBlankLines == 0 && rightBlankLines == 0) {
        text = left.getText() + right.getText();
      }
      else if (leaveRightText) {
        text = right.getText();
      }
      else {
        text = left.getText();
      }
      if(leaveRightText || forceReformat){
        final LeafElement merged = ASTFactory.whitespace(text);
        if(!leaveRightText){
          left.getTreeParent().replaceChild(left, merged);
          right.getTreeParent().removeChild(right);
        }
        else {
          right.getTreeParent().replaceChild(right, merged);
          left.getTreeParent().removeChild(left);
        }
        left = merged;
      }
      else right.getTreeParent().removeChild(right);
    }
    else if(left.getElementType() != TokenType.WHITE_SPACE || forceReformat){
      if(right.getElementType() == TokenType.WHITE_SPACE){
        markWhitespaceForReformat(right);
      }
      else if(left.getElementType() == TokenType.WHITE_SPACE){
        markWhitespaceForReformat(left);
      }
      else markToReformatBeforeOrInsertWhitespace(left, right);
    }
    return left;
  }

  private static void markWhitespaceForReformat(final ASTNode right) {
    final String text = right.getText();
    final LeafElement merged = ASTFactory.whitespace(text);
    right.getTreeParent().replaceChild(right, merged);
  }

  private static void markToReformatBeforeOrInsertWhitespace(final ASTNode left, @NotNull final ASTNode right) {
    final Language leftLang = left != null ? PsiUtilCore.getNotAnyLanguage(left) : null;
    final Language rightLang = PsiUtilCore.getNotAnyLanguage(right);

    ASTNode generatedWhitespace = null;
    if (leftLang != null && leftLang.isKindOf(rightLang)) {
      generatedWhitespace = LanguageTokenSeparatorGenerators.INSTANCE.forLanguage(leftLang).generateWhitespaceBetweenTokens(left, right);
    }
    else if (rightLang.isKindOf(leftLang)) {
      generatedWhitespace = LanguageTokenSeparatorGenerators.INSTANCE.forLanguage(rightLang).generateWhitespaceBetweenTokens(left, right);
    }

    if (generatedWhitespace != null) {
      final TreeUtil.CommonParentState parentState = new TreeUtil.CommonParentState();
      TreeUtil.prevLeaf((TreeElement)right, parentState);
      parentState.nextLeafBranchStart.getTreeParent().addChild(generatedWhitespace, parentState.nextLeafBranchStart);
    }
    else {
      markToReformatBefore(right, true);
    }
  }

  public static void markToReformatBefore(final ASTNode right, boolean value) {
    right.putCopyableUserData(REFORMAT_BEFORE_KEY, value ? true : null);
  }

  private static int getBlankLines(final String text) {
    int result = 0;
    int currentIndex = -1;
    while((currentIndex = text.indexOf('\n', currentIndex + 1)) >= 0) result++;
    return result;
  }

  public static boolean isNodeGenerated(final ASTNode node) {
    return node == null || node.getCopyableUserData(GENERATED_FLAG) != null;
  }

  public static void setNodeGenerated(final ASTNode next, final boolean value) {
    if (next == null) return;
    next.putCopyableUserData(GENERATED_FLAG, value ? true : null);
  }

  public static void setOldIndentation(final TreeElement treeElement, final int oldIndentation) {
    if(treeElement == null) return;
    treeElement.putCopyableUserData(INDENT_INFO, oldIndentation >= 0 ? oldIndentation : null);
  }

  public static boolean isMarkedToReformatBefore(final TreeElement element) {
    return element.getCopyableUserData(REFORMAT_BEFORE_KEY) != null;
  }

  @Nullable
  public static PsiElement createLineFeed(final PsiManager manager) {
    return Factory.createSingleLeafElement(TokenType.WHITE_SPACE, "\n", 0, 1, null, manager).getPsi();
  }

  /**
   * Allows to answer if given node is configured to be reformatted.
   *
   * @param node    node to check
   * @return        <code>true</code> if given node is configured to be reformatted; <code>false</code> otherwise
   */
  public static boolean isMarkedToReformat(final ASTNode node) {
    return node.getCopyableUserData(REFORMAT_KEY) != null && isSuspendedNodesReformattingAllowed();
  }

  /**
   * Allows to define if given element should be reformatted later.
   *
   * @param node      target element which <code>'reformat'</code> status should be changed
   * @param value     <code>true</code> if the element should be reformatted; <code>false</code> otherwise
   */
  public static void markToReformat(final ASTNode node, boolean value) {
    if (ALLOW_TO_MARK_NODES_TO_REFORMAT.get()) {
      node.putCopyableUserData(REFORMAT_KEY, value ? true : null);
    }
  }

  public static void disablePostponedFormatting(@NotNull final ASTNode node) {
    markToReformat(node, false);
    markToReformatBefore(node, false);
    node.putUserData(DISABLE_POSTPONED_REFORMAT_KEY, true);
  }

  public static void enablePostponedFormattingInTree(@NotNull ASTNode root) {
    ((TreeElement)root).acceptTree(new RecursiveTreeElementVisitor() {
      @Override
      protected boolean visitNode(TreeElement element) {
        element.putUserData(DISABLE_POSTPONED_REFORMAT_KEY, null);
        return true;
      }
    });

  }

  public static boolean isPostponedFormattingDisabled(@NotNull final ASTNode node) {
    return node.getUserData(DISABLE_POSTPONED_REFORMAT_KEY) != null;
  }

  /**
   * We allow to mark particular {@link ASTNode AST nodes} to be reformatted later (e.g. we may want method definition and calls
   * to be reformatted when we perform <code>'change method signature'</code> refactoring. Hence, we mark corresponding expressions
   * to be reformatted).
   * <p/>
   * For convenience that is made automatically on AST/PSI level, i.e. every time target element change it automatically marks itself
   * to be reformatted.
   * <p/>
   * However, there is a possible case that particular element is changed because of formatting, hence, there is no need to mark
   * itself for postponed formatting one more time. This method allows to configure allowance of reformat markers processing
   * for the calling thread. I.e. this method may be called with <code>'false'</code> as an argument, hence, all further attempts
   * to {@link #markToReformat(ASTNode, boolean) mark node for postponed formatting} will have no effect until current method is
   * called with <code>'true'</code> as an argument. Hence, following usage scenario is expected:
   * <ol>
   *   <li>This method is called with <code>'false'</code> argument;</li>
   *   <li>Formatting is performed at dedicated <code>'try'</code> block;</li>
   *   <li>This method is called with <code>'false'</code> argument from <code>'finally'</code> section;</li>
   * </ol>
   *
   * @param allow     flag that defines if new reformat markers can be added from the current thread
   * @see #markToReformat(ASTNode, boolean)
   */
  public static void allowToMarkNodesForPostponedFormatting(boolean allow) {
    ALLOW_TO_MARK_NODES_TO_REFORMAT.set(allow);
  }

  /**
   * @return    <code>'allow suspended formatting'</code> flag value
   * @see #setAllowSuspendNodesReformatting(boolean)
   */
  public static boolean isSuspendedNodesReformattingAllowed() {
    return ALLOW_NODES_REFORMATTING.get();
  }

  /**
   * There is a possible case that particular PSI tree node is {@link #markToReformat(ASTNode, boolean) marked for reformatting}.
   * That means that there is a big chance that the node will be re-formatted during corresponding document processing
   * (e.g. on call to {@link PsiDocumentManager#doPostponedOperationsAndUnblockDocument(Document)}).
   * <p/>
   * However, there is a possible case that particular activity that triggers such document processing is not ready to the
   * situation when the document is modified because of postponed formatting. Hence, it may ask to suspend postponed formatting
   * for a while. This method allows to do that at thread-local manner. I.e. it's expected to be called as follows:
   * <pre>
   * <ol>
   *   <li>This method is called with <code>'false'</code> argument;</li>
   *   <li>Document is processed at dedicated <code>'try'</code> block;</li>
   *   <li>This method is called with <code>'true'</code> argument from <code>'finally'</code> section;</li>
   * </ol>
   </pre>
   */
  public static void setAllowSuspendNodesReformatting(boolean allow) {
    ALLOW_NODES_REFORMATTING.set(allow);
  }
}