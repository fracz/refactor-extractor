package com.intellij.psi.impl.source.tree;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLock;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.tree.java.ReplaceExpressionUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.CharTable;

public class CompositeElement extends TreeElement implements Cloneable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.CompositeElement");

  public TreeElement prev = null;
  public CompositeElement parent = null;
  public TreeElement firstChild = null; // might be modified by transforming chameleons
  public TreeElement lastChild = null; // might be modified by transforming chameleons
  private final IElementType type;
  private int myParentModifications = -1;
  private int myStartOffset = 0;
  private int myModificationsCount = 0;
  private PsiElement myWrapper = null;

  public CompositeElement(IElementType type) {
    this.type = type;
  }

  public int getModificationCount() {
    return myModificationsCount;
  }

  public int getStartOffset() {
    CompositeElement parent = this;
    int sum = 0;
    while (parent != null) {
      sum += parent.getModificationCount();
      parent = parent.getTreeParent();
    }
    if (sum != myParentModifications) {
      myParentModifications = sum;
      if (prev != null) {
        myStartOffset = prev.getStartOffset() + prev.getTextLength();
      }
      else {
        if (this.parent != null) {
          myStartOffset = this.parent.getStartOffset();
        }
        else {
          myStartOffset = 0;
        }
      }
    }
    return myStartOffset;
  }

  public Object clone() {
    CompositeElement clone = (CompositeElement)super.clone();

    synchronized (PsiLock.LOCK) {
      clone.clearCaches();
      clone.parent = null;
      clone.prev = null;
      clone.firstChild = null;
      clone.lastChild = null;
      clone.myModificationsCount = 0;
      clone.myParentModifications = -1;
      clone.myWrapper = null;
      for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
        TreeUtil.addChildren(clone, (TreeElement)child.clone());
      }
    }
    return clone;
  }

  public void subtreeChanged() {
    clearCaches();
    CompositeElement treeParent = getTreeParent();
    if (treeParent != null) treeParent.subtreeChanged();
  }

  public void clearCaches() {
    setCachedLength(-1);
    myModificationsCount++;
  }

  public IElementType getElementType() {
    return type;
  }

  public CompositeElement getTreeParent() {
    return parent;
  }

  public TreeElement getTreePrev() {
    return prev;
  }

  public void setTreePrev(TreeElement prev) {
    this.prev = prev;
  }

  public void setTreeParent(CompositeElement parent) {
    this.parent = parent;
  }

  public int getTextLength(CharTable charTableByTree) {
    return myCachedLength > 0 ? myCachedLength : getLengthInner(charTableByTree);
  }

  public LeafElement findLeafElementAt(int offset) {

    synchronized (PsiLock.LOCK) {
      final CharTable table = SharedImplUtil.findCharTableByTree(this);
      TreeElement child = firstChild;
      while (child != null) {
        final int textLength = child.getTextLength(table);
        if (textLength > offset) {
          if (child instanceof LeafElement && ((LeafElement)child).isChameleon()) {
            child = (TreeElement)ChameleonTransforming.transform((LeafElement)child);
            continue;
          }
          return child.findLeafElementAt(offset);
        }
        offset -= textLength;
        child = child.getTreeNext();
      }
      return null;
    }
  }

  public ASTNode findChildByType(IElementType type) {
    return TreeUtil.findChild(this, type);
  }

  public ASTNode[] findChildrenByFilter(TokenSet filter) {
    return TreeUtil.findChildren(this, filter);
  }

  public String getText() {
    synchronized (PsiLock.LOCK) {
      // check if all elements are laid out consequently in the same buffer (optimization):
      char[] buffer = new char[getTextLength()];
      SourceUtil.toBuffer(this, buffer, 0);
      //return StringFactory.createStringFromConstantArray(buffer);
      return new String(buffer);
    }
  }

  public char[] textToCharArray() {
    synchronized (PsiLock.LOCK) {
      char[] buffer = new char[getTextLength()];
      SourceUtil.toBuffer(this, buffer, 0);
      return buffer;
    }
  }

  public boolean textContains(char c) {
    synchronized (PsiLock.LOCK) {
      for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
        if (child.textContains(c)) return true;
      }
      return false;
    }
  }

  public final PsiElement findChildByRoleAsPsiElement(int role) {
    ASTNode element = findChildByRole(role);
    if (element == null) return null;
    if (element instanceof LeafElement && ((LeafElement)element).isChameleon()) {
      element = ChameleonTransforming.transform((LeafElement)element);
    }
    return SourceTreeToPsiMap.treeElementToPsi(element);
  }

  public ASTNode findChildByRole(int role) {
    LOG.assertTrue(ChildRole.isUnique(role));
    synchronized (PsiLock.LOCK) {
      for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
        if (getChildRole(child) == role) return child;
      }
    }
    return null;
  }

  public int getChildRole(ASTNode child) {
    LOG.assertTrue(child.getTreeParent() == this);
    return ChildRole.NONE;
  }

  protected final int getChildRole(ASTNode child, int roleCandidate) {
    if (findChildByRole(roleCandidate) == child) {
      return roleCandidate;
    }
    else {
      return ChildRole.NONE;
    }
  }

  public ASTNode[] getChildren(TokenSet filter) {
    int count = countChildren(filter);
    if (count == 0) {
      return TreeElement.EMPTY_ARRAY;
    }
    final ASTNode[] result = new ASTNode[count];
    count = 0;
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (filter == null || filter.isInSet(child.getElementType())) {
        result[count++] = child;
      }
    }
    return result;
  }


  public PsiElement[] getChildrenAsPsiElements(TokenSet filter, PsiElementArrayConstructor constructor) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    int count = countChildren(filter);

    if (count == 0) {
      return constructor.newPsiElementArray(0);
    }

    PsiElement[] result = constructor.newPsiElementArray(count);
    count = 0;
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (filter == null || filter.isInSet(child.getElementType())) {
        result[count++] = SourceTreeToPsiMap.treeElementToPsi(child);
      }
    }
    return result;
  }

  public int countChildren(TokenSet filter) {
    ChameleonTransforming.transformChildren(this);

    // no lock is needed because all chameleons are expanded already
    int count = 0;
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (filter == null || filter.isInSet(child.getElementType())) {
        count++;
      }
    }

    return count;
  }

  /**
   * @return First element that was appended (for example whitespaces could be skipped)
   */
  public TreeElement addInternal(TreeElement first, ASTNode last, ASTNode anchor, Boolean before) {
    ASTNode anchorBefore;
    if (anchor != null) {
      anchorBefore = before.booleanValue() ? anchor : anchor.getTreeNext();
    }
    else {
      if (before != null && !before.booleanValue()) {
        anchorBefore = firstChild;
      }
      else {
        anchorBefore = null;
      }
    }
    return CodeEditUtil.addChildren(this, first, last, anchorBefore);
  }

  public void deleteChildInternal(ASTNode child) {
    CodeEditUtil.removeChild(this, child);
  }

  public void replaceChildInternal(ASTNode child, TreeElement newElement) {
    if (ElementType.EXPRESSION_BIT_SET.isInSet(child.getElementType())) {
      boolean needParenth = ReplaceExpressionUtil.isNeedParenthesis(child, newElement);
      if (needParenth) {
        newElement = SourceUtil.addParenthToReplacedChild(JavaElementType.PARENTH_EXPRESSION, newElement, getManager());
      }
    }
    CodeEditUtil.replaceChild(this, child, newElement);
  }

  private int myCachedLength = -1;

  public int getTextLength() {
    if (myCachedLength < 0) {
      myCachedLength = getLengthInner(SharedImplUtil.findCharTableByTree(this));
    }
    if (DebugUtil.CHECK) {
      int trueLength = getLengthInner(SharedImplUtil.findCharTableByTree(this));
      if (myCachedLength != trueLength) {
        LOG.error("myCachedLength != trueLength");
        myCachedLength = trueLength;
      }
    }
    return myCachedLength;
  }

  public int getCachedLength() {
    return myCachedLength;
  }

  protected int getLengthInner(CharTable charTableByTree) {
    synchronized (PsiLock.LOCK) {
      int length = 0;
      for (TreeElement child = firstChild; child != null; child = child.getTreeNext()) {
        length += child.getTextLength(charTableByTree);
      }
      return length;
    }
  }

  public void setCachedLength(int length) {
    myCachedLength = length;
  }

  public ASTNode getFirstChildNode() {
    return firstChild;
  }

  public ASTNode getLastChildNode() {
    return lastChild;
  }

  public void addChild(ASTNode child, ASTNode anchorBefore) {
    ChangeUtil.addChild(this, (TreeElement)child, (TreeElement)anchorBefore);
  }

  public void addChild(ASTNode child) {
    ChangeUtil.addChild(this, (TreeElement)child, null);
  }

  public void removeChild(ASTNode child) {
    ChangeUtil.removeChild(this, (TreeElement)child);
  }

  public void replaceChild(ASTNode oldChild, ASTNode newChild) {
    ChangeUtil.replaceChild(this, (TreeElement)oldChild, (TreeElement)newChild);
  }

  public void replaceAllChildrenToChildrenOf(ASTNode anotherParent) {
    ChangeUtil.replaceAllChildren(this, anotherParent);
  }

  public void addChildren(ASTNode firstChild, ASTNode lastChild, ASTNode anchorBefore) {
    ChangeUtil.addChildren(this, firstChild, lastChild, anchorBefore);
  }

  public PsiElement getPsi() {
    synchronized (PsiLock.LOCK) {
      if (myWrapper != null) return myWrapper;
      final Language lang = getElementType().getLanguage();
      if (lang != null) {
        myWrapper = lang.getParserDefinition().createElement(this);
      }
      return myWrapper;
    }
  }
}