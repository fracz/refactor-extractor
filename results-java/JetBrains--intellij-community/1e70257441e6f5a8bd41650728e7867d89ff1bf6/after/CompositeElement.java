package com.intellij.psi.impl.source.tree;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLock;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.PsiElementArrayConstructor;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.tree.java.ReplaceExpressionUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompositeElement extends TreeElement implements Cloneable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.CompositeElement");

  public volatile TreeElement firstChild = null; // might be modified by transforming chameleons
  public volatile TreeElement lastChild = null; // might be modified by transforming chameleons
  private final IElementType type;
  private int myParentModifications = -1;
  private int myStartOffset = 0;
  private int myModificationsCount = 0;
  private int myCachedLength = -1;
  private int myHC = -1;
  private volatile PsiElement myWrapper = null;

  public CompositeElement(@NotNull IElementType type) {
    this.type = type;
  }

  public int getModificationCount() {
    return myModificationsCount;
  }

  public int getStartOffset() {
    assert prev != this: "Loop in tree";

    if(parent == null) return 0;
    synchronized(PsiLock.LOCK){
      CompositeElement parent = this.parent;
      int sum = 0;
      while (parent != null) {
        sum += parent.getModificationCount();
        parent = parent.getTreeParent();
      }
      recalcStartOffset(sum);
      return myStartOffset;
    }
  }

  private void recalcStartOffset(final int parentModificationsCount) {
    if(parentModificationsCount == myParentModifications || parent == null) return;

    // recalc on parent if needed
    final int parentParentModificationsCount = parentModificationsCount - parent.getModificationCount();
    parent.recalcStartOffset(parentParentModificationsCount);

    CompositeElement lastKnownStart = null;

    TreeElement treePrev = prev;
    TreeElement last = this;
    // Step 1: trying to find known startOffset in previous composites (getTreePrev for composite is cheap)
    while (treePrev instanceof CompositeElement) {
      final CompositeElement compositeElement = (CompositeElement)treePrev;
      if (compositeElement.myParentModifications == parentModificationsCount) {
        lastKnownStart = compositeElement;
        break;
      }
      last = treePrev;
      treePrev = treePrev.getTreePrev();
    }

    TreeElement current;
    if(lastKnownStart == null){
      // Step 2: if leaf found cheaper to start from begining to find known startOffset composite
      lastKnownStart = parent;
      current = parent.getFirstChildNode();

      while(current != last){
        assert current != null: "Invalid tree";
        if(current instanceof CompositeElement) {
          final CompositeElement compositeElement = (CompositeElement)current;
          if(compositeElement.myParentModifications == parentModificationsCount)
            lastKnownStart = compositeElement;
        }
        current = current.getTreeNext();
      }
    }
    current = lastKnownStart != parent ? lastKnownStart : parent.getFirstChildNode();
    int start = lastKnownStart.myStartOffset;
    while(current != this) {
      if(current instanceof CompositeElement){
        final CompositeElement compositeElement = (CompositeElement)current;
        compositeElement.myParentModifications = parentModificationsCount;
        compositeElement.myStartOffset = start;
      }
      start += current.getTextLength();
      current = current.getTreeNext();
    }

    myStartOffset = start;
    myParentModifications = parentModificationsCount;
  }

  public Object clone() {
    CompositeElement clone = (CompositeElement)super.clone();

    clone.clearCaches();
    clone.firstChild = null;
    clone.lastChild = null;
    clone.myModificationsCount = 0;
    clone.myParentModifications = -1;
    clone.myWrapper = null;
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      TreeUtil.addChildren(clone, (TreeElement)child.clone());
    }
    return clone;
  }

  public void subtreeChanged() {
    clearCaches();
    if (!(this instanceof PsiElement)) {
      final PsiElement psi = getPsi();
      if (psi instanceof ASTWrapperPsiElement) {
        ((ASTWrapperPsiElement)psi).subtreeChanged();
      }
    }

    CompositeElement treeParent = getTreeParent();
    if (treeParent != null) treeParent.subtreeChanged();
  }

  public void clearCaches() {
    setCachedLength(-1);

    myModificationsCount++;
    myParentModifications = -1;
    myHC = -1;
  }

  public void acceptTree(TreeElementVisitor visitor) {
    visitor.visitComposite(this);
  }

  public IElementType getElementType() {
    return type;
  }

  public LeafElement findLeafElementAt(int offset) {
    TreeElement child = firstChild;
    while (child != null) {
      final int textLength = child.getTextLength();
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

  public ASTNode findChildByType(IElementType type) {
    return TreeUtil.findChild(this, type);
  }

  @Nullable
  public ASTNode findChildByType(@NotNull TokenSet typesSet) {
    return TreeUtil.findChild(this, typesSet);
  }

  @Nullable
  public ASTNode findChildByType(@NotNull TokenSet typesSet, ASTNode anchor) {
    return TreeUtil.findSibling(anchor, typesSet);
  }

  @NotNull
  public String getText() {
    char[] buffer = new char[getTextLength()];
    SourceUtil.toBuffer(this, buffer, 0);
    return StringFactory.createStringFromConstantArray(buffer);
  }

  @NotNull
  public char[] textToCharArray() {
    char[] buffer = new char[getTextLength()];
    SourceUtil.toBuffer(this, buffer, 0);
    return buffer;
  }

  public boolean textContains(char c) {
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (child.textContains(c)) return true;
    }
    return false;
  }

  public final PsiElement findChildByRoleAsPsiElement(int role) {
    ASTNode element = findChildByRole(role);
    if (element == null) return null;
    if (element instanceof LeafElement && ((LeafElement)element).isChameleon()) {
      element = ChameleonTransforming.transform((LeafElement)element);
    }
    return SourceTreeToPsiMap.treeElementToPsi(element);
  }

  @Nullable
  public ASTNode findChildByRole(int role) {
    // assert ChildRole.isUnique(role);
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (getChildRole(child) == role) return child;
    }
    return null;
  }

  public int getChildRole(ASTNode child) {
    assert child.getTreeParent() == this;
    return 0; //ChildRole.NONE;
  }

  protected final int getChildRole(ASTNode child, int roleCandidate) {
    if (findChildByRole(roleCandidate) == child) {
      return roleCandidate;
    }
    else {
      return 0; //ChildRole.NONE;
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
      if (filter == null || filter.contains(child.getElementType())) {
        result[count++] = child;
      }
    }
    return result;
  }


  @NotNull
  public <T extends PsiElement> T[] getChildrenAsPsiElements(TokenSet filter, PsiElementArrayConstructor<T> constructor) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    int count = countChildren(filter);

    if (count == 0) {
      return constructor.newPsiElementArray(0);
    }

    T[] result = constructor.newPsiElementArray(count);
    int idx = 0;
    for (ASTNode child = getFirstChildNode(); child != null && idx < count; child = child.getTreeNext()) {
      if (filter == null || filter.contains(child.getElementType())) {
        T element = (T)SourceTreeToPsiMap.treeElementToPsi(child);
        assert element != null;
        result[idx++] = element;
      }
    }
    return result;
  }

  public int countChildren(TokenSet filter) {
    ChameleonTransforming.transformChildren(this);

    // no lock is needed because all chameleons are expanded already
    int count = 0;
    for (ASTNode child = getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (filter == null || filter.contains(child.getElementType())) {
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
    return (TreeElement)CodeEditUtil.addChildren(this, first, last, anchorBefore);
  }

  public void deleteChildInternal(@NotNull ASTNode child) {
    CodeEditUtil.removeChild(this, child);
  }

  public void replaceChildInternal(@NotNull ASTNode child, @NotNull TreeElement newElement) {
    if (ElementType.EXPRESSION_BIT_SET.contains(child.getElementType())) {
      boolean needParenth = ReplaceExpressionUtil.isNeedParenthesis(child, newElement);
      if (needParenth) {
        newElement = SourceUtil.addParenthToReplacedChild(JavaElementType.PARENTH_EXPRESSION, newElement, getManager());
      }
    }
    CodeEditUtil.replaceChild(this, child, newElement);
  }

  public int getTextLength() {
    if (myCachedLength < 0) {
      myCachedLength = getLengthInner();
    }
    if (DebugUtil.CHECK) {
      int trueLength = getLengthInner();
      if (myCachedLength != trueLength) {
        LOG.error("myCachedLength != trueLength");
        myCachedLength = trueLength;
      }
    }
    return myCachedLength;
  }

  public int hc() {
    if (myHC == -1) {
      int hc = 0;
      TreeElement child = firstChild;
      while (child != null) {
        hc += child.hc();
        child = child.next;
      }
      myHC = hc;
    }

    return myHC;
  }

  private int getLengthInner() {
    int length = 0;
    for (TreeElement child = firstChild; child != null; child = child.getTreeNext()) {
      length += child.getTextLength();
    }
    return length;
  }

  private void setCachedLength(int length) {
    myCachedLength = length;
  }

  public TreeElement getFirstChildNode() {
    return firstChild;
  }

  public TreeElement getLastChildNode() {
    return lastChild;
  }

  public void addChild(@NotNull ASTNode child, ASTNode anchorBefore) {
    ChangeUtil.addChild(this, (TreeElement)child, (TreeElement)anchorBefore);
  }

  public void addLeaf(@NotNull final IElementType leafType, final CharSequence leafText, final ASTNode anchorBefore) {
    FileElement holder = new DummyHolder(getManager(), null).getTreeElement();
    final LeafElement leaf = Factory.createLeafElement(leafType, leafText, 0, leafText.length(), holder.getCharTable());
    CodeEditUtil.setNodeGenerated(leaf, true);
    TreeUtil.addChildren(holder, leaf);

    ChangeUtil.addChild(this, leaf, (TreeElement)anchorBefore);
  }

  public void addChild(@NotNull ASTNode child) {
    ChangeUtil.addChild(this, (TreeElement)child, null);
  }

  public void removeChild(@NotNull ASTNode child) {
    ChangeUtil.removeChild(this, (TreeElement)child);
  }

  public void removeRange(@NotNull ASTNode first, ASTNode firstWhichStayInTree) {
    ChangeUtil.removeChildren(this, (TreeElement)first, (TreeElement)firstWhichStayInTree);
  }

  public void replaceChild(@NotNull ASTNode oldChild, @NotNull ASTNode newChild) {
    ChangeUtil.replaceChild(this, (TreeElement)oldChild, (TreeElement)newChild);
  }

  public void replaceAllChildrenToChildrenOf(ASTNode anotherParent) {
    ChangeUtil.replaceAllChildren(this, anotherParent);
  }

  public void addChildren(ASTNode firstChild, ASTNode lastChild, ASTNode anchorBefore) {
    ChangeUtil.addChildren(this, firstChild, lastChild, anchorBefore);
  }

  public PsiElement getPsi() {
    if (myWrapper != null) return myWrapper;
    synchronized (PsiLock.LOCK) {
      if (myWrapper != null) return myWrapper;

      final Language lang = getElementType().getLanguage();
      final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
      if (parserDefinition != null) {
        myWrapper = parserDefinition.createElement(this);
        //noinspection ConstantConditions
        LOG.assertTrue(myWrapper != null, "ParserDefinition.createElement() may not return null");
      }
      return myWrapper;
    }
  }
}