package com.intellij.psi.impl.source.tree;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomManager;
import com.intellij.pom.PomModel;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.pom.impl.PomTransactionBase;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.pom.tree.events.ChangeInfo;
import com.intellij.pom.tree.events.TreeChangeEvent;
import com.intellij.pom.tree.events.impl.ChangeInfoImpl;
import com.intellij.pom.tree.events.impl.ReplaceChangeInfoImpl;
import com.intellij.pom.tree.events.impl.TreeChangeEventImpl;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.GeneratedMarkerVisitor;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.cache.RepositoryManager;
import com.intellij.psi.impl.light.LightTypeElement;
import com.intellij.psi.impl.smartPointers.SmartPointerManagerImpl;
import com.intellij.psi.impl.source.JavaDummyHolder;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.parsing.ExpressionParsing;
import com.intellij.psi.impl.source.parsing.ParseUtil;
import com.intellij.psi.impl.source.parsing.Parsing;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ChangeUtil {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.ChangeUtil");

  private ChangeUtil() { }

  public static void addChild(final CompositeElement parent, TreeElement child, final TreeElement anchorBefore) {
    LOG.assertTrue(anchorBefore == null || anchorBefore.getTreeParent() == parent, "anchorBefore == null || anchorBefore.getTreeParent() == parent");
    transformAll(parent.getFirstChildNode());
    final TreeElement last = child.getTreeNext();
    final TreeElement first = transformAll(child);

    final CharTable newCharTab = SharedImplUtil.findCharTableByTree(parent);
    final CharTable oldCharTab = SharedImplUtil.findCharTableByTree(child);

    removeChildrenInner(first, last, oldCharTab);

    if (newCharTab != oldCharTab) registerLeafsInCharTab(newCharTab, child, oldCharTab);

    prepareAndRunChangeAction(new ChangeAction(){
      public void makeChange(TreeChangeEvent destinationTreeChange) {
        if (anchorBefore != null) {
          insertBefore(destinationTreeChange, anchorBefore, first);
        }
        else {
          add(destinationTreeChange, parent, first);
        }
      }
    }, parent);
  }

  public static void removeChild(final CompositeElement parent, final TreeElement child) {
    final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(parent);
    removeChildInner(child, charTableByTree);
  }

  public static void removeChildren(final CompositeElement parent, final TreeElement first, final TreeElement last) {
    if(first == null) return;
    final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(parent);
    removeChildrenInner(first, last, charTableByTree);
  }

  public static void replaceChild(final CompositeElement parent, @NotNull final TreeElement old, @NotNull final TreeElement newC) {
    LOG.assertTrue(old.getTreeParent() == parent);
    final TreeElement oldChild = transformAll(old);
    final TreeElement newChildNext = newC.getTreeNext();
    final TreeElement newChild = transformAll(newC);

    if(oldChild == newChild) return;
    final CharTable newCharTable = SharedImplUtil.findCharTableByTree(parent);
    final CharTable oldCharTable = SharedImplUtil.findCharTableByTree(newChild);

    removeChildrenInner(newChild, newChildNext, oldCharTable);

    if (oldCharTable != newCharTable) registerLeafsInCharTab(newCharTable, newChild, oldCharTable);

    prepareAndRunChangeAction(new ChangeAction(){
      public void makeChange(TreeChangeEvent destinationTreeChange) {
        replace(destinationTreeChange, oldChild, newChild);
        repairRemovedElement(parent, newCharTable, oldChild);
      }
    }, parent);
  }

  public static void replaceAllChildren(final CompositeElement parent, final ASTNode newChildrenParent) {
    transformAll(parent.getFirstChildNode());
    transformAll((TreeElement)newChildrenParent.getFirstChildNode());

    final CharTable newCharTab = SharedImplUtil.findCharTableByTree(parent);
    final CharTable oldCharTab = SharedImplUtil.findCharTableByTree(newChildrenParent);

    final ASTNode firstChild = newChildrenParent.getFirstChildNode();
    prepareAndRunChangeAction(new ChangeAction(){
      public void makeChange(TreeChangeEvent destinationTreeChange) {
        destinationTreeChange.addElementaryChange(newChildrenParent, ChangeInfoImpl.create(ChangeInfo.CONTENTS_CHANGED, newChildrenParent));
        TreeUtil.removeRange((TreeElement)newChildrenParent.getFirstChildNode(), null);
      }
    }, (CompositeElement)newChildrenParent);

    if (firstChild != null) {
      registerLeafsInCharTab(newCharTab, firstChild, oldCharTab);
      prepareAndRunChangeAction(new ChangeAction(){
        public void makeChange(TreeChangeEvent destinationTreeChange) {
          if(parent.getTreeParent() != null){
            final ChangeInfoImpl changeInfo = ChangeInfoImpl.create(ChangeInfo.CONTENTS_CHANGED, parent);
            changeInfo.setOldLength(parent.getTextLength());
            destinationTreeChange.addElementaryChange(parent, changeInfo);
            TreeUtil.removeRange(parent.getFirstChildNode(), null);
            TreeUtil.addChildren(parent, (TreeElement)firstChild);
          }
          else{
            final TreeElement first = parent.getFirstChildNode();
            remove(destinationTreeChange, first, null);
            add(destinationTreeChange, parent, (TreeElement)firstChild);
            repairRemovedElement(parent, newCharTab, first);
          }
        }
      }, parent);
    }
    else {
      removeChildren(parent, parent.getFirstChildNode(), null);
    }
  }

  private static TreeElement transformAll(TreeElement first){
    ASTNode newFirst = null;
    ASTNode child = first;
    while (child != null) {
      if (child instanceof ChameleonElement) {
        ASTNode next = child.getTreeNext();
        child = ChameleonTransforming.transform((ChameleonElement)child);
        if (child == null) {
          child = next;
        }
        continue;
      }
      if(newFirst == null) newFirst = child;
      child = child.getTreeNext();
    }
    return (TreeElement)newFirst;
  }

  private static void repairRemovedElement(final CompositeElement oldParent, final CharTable newCharTable, final TreeElement oldChild) {
    if(oldChild == null) return;
    final FileElement treeElement = new JavaDummyHolder(oldParent.getManager(), newCharTable, false).getTreeElement();
    TreeUtil.addChildren(treeElement, oldChild);
  }

  private static void add(final TreeChangeEvent destinationTreeChange,
                          final CompositeElement parent,
                          final TreeElement first) {
    TreeUtil.addChildren(parent, first);
    TreeElement child = first;
    while(child != null){
      destinationTreeChange.addElementaryChange(child, ChangeInfoImpl.create(ChangeInfo.ADD, child));
      child = child.getTreeNext();
    }
  }

  private static void remove(final TreeChangeEvent destinationTreeChange,
                             final TreeElement first,
                             final TreeElement last) {
    TreeElement child = first;
    while(child != last && child != null){
      destinationTreeChange.addElementaryChange(child, ChangeInfoImpl.create(ChangeInfo.REMOVED, child));
      child = child.getTreeNext();
    }
    TreeUtil.removeRange(first, last);
  }

  private static void insertBefore(final TreeChangeEvent destinationTreeChange,
                                   final TreeElement anchorBefore,
                                   final TreeElement first) {
    TreeUtil.insertBefore(anchorBefore, first);
    TreeElement child = first;
    while(child != anchorBefore){
      destinationTreeChange.addElementaryChange(child, ChangeInfoImpl.create(ChangeInfo.ADD, child));
      child = child.getTreeNext();
    }
  }

  private static void replace(final TreeChangeEvent sourceTreeChange,
                              final TreeElement oldChild,
                              final TreeElement newChild) {
    TreeUtil.replaceWithList(oldChild, newChild);
    final ReplaceChangeInfoImpl change = (ReplaceChangeInfoImpl)ChangeInfoImpl.create(ChangeInfo.REPLACE, newChild);
    sourceTreeChange.addElementaryChange(newChild, change);
    change.setReplaced(oldChild);
  }

  private static void registerLeafsInCharTab(CharTable newCharTab, ASTNode child, CharTable oldCharTab) {
    if (newCharTab == oldCharTab) return;
    while (child != null) {
      CharTable charTable = child.getUserData(CharTable.CHAR_TABLE_KEY);
      if (child instanceof LeafElement) {
          ((LeafElement)child).registerInCharTable(newCharTab);
          ((LeafElement)child).registerInCharTable(newCharTab);
        ((LeafElement)child).registerInCharTable(newCharTab);
      }
      else {
        registerLeafsInCharTab(newCharTab, child.getFirstChildNode(), charTable != null ? charTable : oldCharTab);
      }
      if (charTable != null) {
        child.putUserData(CharTable.CHAR_TABLE_KEY, null);
      }
      child = child.getTreeNext();
    }
  }

  private static void removeChildInner(final TreeElement child, final CharTable oldCharTab) {
    removeChildrenInner(child, child.getTreeNext(), oldCharTab);
  }

  private static void removeChildrenInner(final TreeElement first, final TreeElement last, final CharTable oldCharTab) {
    final FileElement fileElement = TreeUtil.getFileElement(first);
    if (fileElement != null) {
      prepareAndRunChangeAction(new ChangeAction() {
        public void makeChange(TreeChangeEvent destinationTreeChange) {
          remove(destinationTreeChange, first, last);
          repairRemovedElement(fileElement, oldCharTab, first);
        }
      }, first.getTreeParent());
    }
    else {
      TreeUtil.removeRange(first, last);
    }
  }

  public static void changeElementInPlace(final ASTNode element, final ChangeAction action){
    prepareAndRunChangeAction(new ChangeAction() {
      public void makeChange(TreeChangeEvent destinationTreeChange) {
        destinationTreeChange.addElementaryChange(element, ChangeInfoImpl.create(ChangeInfo.CONTENTS_CHANGED, element));
        action.makeChange(destinationTreeChange);
      }
    }, (TreeElement) element);
    ASTNode node = element;
    while (node != null) {
      ASTNode parent = node.getTreeParent();
      ((TreeElement) node).clearCaches();
      node = parent;
    }
  }

  public static interface ChangeAction{
    void makeChange(TreeChangeEvent destinationTreeChange);
  }

  private static void prepareAndRunChangeAction(final ChangeAction action, final TreeElement changedElement){
    final FileElement changedFile = TreeUtil.getFileElement(changedElement);
    final PsiManager manager = changedFile.getManager();
    final PomModel model = PomManager.getModel(manager.getProject());
    try{
      final TreeAspect treeAspect = model.getModelAspect(TreeAspect.class);
      model.runTransaction(new PomTransactionBase(changedElement.getPsi(), treeAspect) {
        public PomModelEvent runInner() {
          final PomModelEvent event = new PomModelEvent(model);
          final TreeChangeEvent destinationTreeChange = new TreeChangeEventImpl(treeAspect, changedFile);
          event.registerChangeSet(treeAspect, destinationTreeChange);
          final PsiManagerEx psiManager = (PsiManagerEx) manager;
          RepositoryManager repositoryManager = JavaPsiFacadeEx.getInstanceEx(psiManager.getProject()).getRepositoryManager();
          final PsiFile file = (PsiFile)changedFile.getPsi();

          if (file.isPhysical()) {
            SmartPointerManagerImpl.fastenBelts(file);
          }

          if (repositoryManager != null) {
            repositoryManager.beforeChildAddedOrRemoved(file, changedElement);
            action.makeChange(destinationTreeChange);
            repositoryManager.beforeChildAddedOrRemoved(file, changedElement);
          }
          else {
            action.makeChange(destinationTreeChange);
          }
          psiManager.invalidateFile(file);
          TreeUtil.clearCaches(changedElement);
          if (changedElement instanceof CompositeElement) {
            ((CompositeElement) changedElement).subtreeChanged();
          }
          return event;
        }
      });
    }
    catch(IncorrectOperationException ioe){
      LOG.error(ioe);
    }
  }

  public static void encodeInformation(TreeElement element) {
    encodeInformation(element, element);
  }

  private static void encodeInformation(TreeElement element, ASTNode original) {
    encodeInformation(element, original, conversionMayApply(original));
  }

  private static void encodeInformation(TreeElement element, ASTNode original, boolean shallEncodeEscapedTexts) {
    if (original instanceof CompositeElement) {
      if (original.getElementType() == JavaElementType.JAVA_CODE_REFERENCE || original.getElementType() == JavaElementType.REFERENCE_EXPRESSION) {
        encodeInformationInRef(element, original);
      }
      else if (original.getElementType() == JavaElementType.MODIFIER_LIST
               && (original.getTreeParent().getElementType() == JavaElementType.FIELD || original.getTreeParent().getElementType() == JavaElementType.METHOD || original.getTreeParent().getElementType() == JavaElementType.ANNOTATION_METHOD)
               && original.getTreeParent().getTreeParent().getElementType() == JavaElementType.CLASS
               && (((PsiClass)SourceTreeToPsiMap.treeElementToPsi(original.getTreeParent().getTreeParent())).isInterface()
                   || ((PsiClass)SourceTreeToPsiMap.treeElementToPsi(original.getTreeParent().getTreeParent())).isAnnotationType())) {
        element.putUserData(INTERFACE_MODIFIERS_FLAG_KEY, Boolean.TRUE);
      }

      ChameleonTransforming.transformChildren(element);
      ChameleonTransforming.transformChildren(original);
      TreeElement child = element.getFirstChildNode();
      ASTNode child1 = original.getFirstChildNode();
      while (child != null) {
        encodeInformation(child, child1, shallEncodeEscapedTexts);
        child = child.getTreeNext();
        child1 = child1.getTreeNext();
      }
    }
    else if (shallEncodeEscapedTexts && original instanceof LeafElement && !(original instanceof OuterLanguageElement)) {
      if (!isInCData(original)) {
        final String originalText = element.getText();
        final String unescapedText = StringUtil.unescapeXml(originalText);
        if (!Comparing.equal(originalText, unescapedText)) {
          ((LeafElement)element).setText(unescapedText);
          element.putCopyableUserData(ALREADY_ESCAPED, null);
        }
      }
    }
  }

  private static boolean isInCData(ASTNode element) {
    ASTNode leaf = element;
    while (leaf != null) {
      if (leaf instanceof OuterLanguageElement) {
        return leaf.getText().indexOf("<![CDATA[") >= 0;
      }

      leaf = TreeUtil.prevLeaf(leaf);
    }

    return false;
  }

  private static void encodeInformationInRef(TreeElement ref, ASTNode original) {
    if (original.getElementType() == JavaElementType.REFERENCE_EXPRESSION) {
      final PsiJavaCodeReferenceElement javaRefElement = (PsiJavaCodeReferenceElement)SourceTreeToPsiMap.treeElementToPsi(original);
      assert javaRefElement != null;
      final JavaResolveResult resolveResult = javaRefElement.advancedResolve(false);
      final PsiElement target = resolveResult.getElement();
      if (target instanceof PsiClass &&
          original.getTreeParent().getElementType() == JavaElementType.REFERENCE_EXPRESSION) {
        ref.putCopyableUserData(REFERENCED_CLASS_KEY, (PsiClass)target);
      }
      else if ((target instanceof PsiMethod || target instanceof PsiField) &&
               ((PsiMember) target).hasModifierProperty(PsiModifier.STATIC) &&
                resolveResult.getCurrentFileResolveScope() instanceof PsiImportStaticStatement) {
        ref.putCopyableUserData(REFERENCED_MEMBER_KEY, (PsiMember) target);
      }
    }
    else if (original.getElementType() == JavaElementType.JAVA_CODE_REFERENCE) {
      switch (((PsiJavaCodeReferenceElementImpl)original).getKind()) {
      case PsiJavaCodeReferenceElementImpl.CLASS_NAME_KIND:
      case PsiJavaCodeReferenceElementImpl.CLASS_OR_PACKAGE_NAME_KIND:
      case PsiJavaCodeReferenceElementImpl.CLASS_IN_QUALIFIED_NEW_KIND:
        final PsiElement target = ((PsiJavaCodeReferenceElement)SourceTreeToPsiMap.treeElementToPsi(original)).resolve();
        if (target instanceof PsiClass) {
          ref.putCopyableUserData(REFERENCED_CLASS_KEY, (PsiClass)target);
        }
        break;

      case PsiJavaCodeReferenceElementImpl.PACKAGE_NAME_KIND:
      case PsiJavaCodeReferenceElementImpl.CLASS_FQ_NAME_KIND:
      case PsiJavaCodeReferenceElementImpl.CLASS_FQ_OR_PACKAGE_NAME_KIND:
             break;

      default:
             LOG.assertTrue(false);
      }
    }
    else {
      LOG.assertTrue(false, "Wrong element type: " + original.getElementType());
    }
  }

  public static TreeElement decodeInformation(TreeElement element) {
    return decodeInformation(element, conversionMayApply(element));
  }

  private static boolean conversionMayApply(ASTNode element) {
    PsiElement psi = element.getPsi();
    if (psi == null || !psi.isValid()) return false;

    final PsiFile file = psi.getContainingFile();
    final Language baseLanguage = file.getViewProvider().getBaseLanguage();
    return baseLanguage == StdLanguages.JSPX && file.getLanguage() != baseLanguage;
  }

  private static TreeElement decodeInformation(TreeElement element, boolean shallDecodeEscapedTexts) {
    if (element instanceof CompositeElement) {
      ChameleonTransforming.transformChildren(element);
      TreeElement child = element.getFirstChildNode();
      while (child != null) {
        child = decodeInformation(child, shallDecodeEscapedTexts);
        child = child.getTreeNext();
      }

      if (element.getElementType() == JavaElementType.JAVA_CODE_REFERENCE ||
          element.getElementType() == JavaElementType.REFERENCE_EXPRESSION) {
        PsiJavaCodeReferenceElement ref = (PsiJavaCodeReferenceElement)SourceTreeToPsiMap.treeElementToPsi(element);
        final PsiClass refClass = element.getCopyableUserData(REFERENCED_CLASS_KEY);
        if (refClass != null) {
          element.putCopyableUserData(REFERENCED_CLASS_KEY, null);

          PsiManager manager = refClass.getManager();
          JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(refClass.getProject());
          PsiElement refElement1 = ref.resolve();
          try {
            if (refClass != refElement1 && !manager.areElementsEquivalent(refClass, refElement1)) {
              if (((CompositeElement)element).findChildByRole(ChildRole.QUALIFIER) == null) {
                // can restore only if short (otherwise qualifier should be already restored)
                ref = (PsiJavaCodeReferenceElement)ref.bindToElement(refClass);
              }
            }
            else {
              // shorten references to the same package and to inner classes that can be accessed by short name
              ref = (PsiJavaCodeReferenceElement)codeStyleManager.shortenClassReferences(ref, JavaCodeStyleManager.DO_NOT_ADD_IMPORTS);
            }
            element = (TreeElement)SourceTreeToPsiMap.psiElementToTree(ref);
          }
          catch (IncorrectOperationException e) {
            ((PsiImportHolder) ref.getContainingFile()).importClass(refClass);
          }
        }
        else {
          final PsiMember refMember = element.getCopyableUserData(REFERENCED_MEMBER_KEY);
          if (refMember != null) {
            element.putCopyableUserData(REFERENCED_MEMBER_KEY, null);
            PsiElement refElement1 = ref.resolve();
            if (refMember != refElement1 && !refMember.getManager().areElementsEquivalent(refMember, refElement1)) {
              try {
                ref = (PsiJavaCodeReferenceElement) ref.bindToElement(refMember);
              }
              catch (IncorrectOperationException e) {
                // TODO[yole] ignore?
              }
              element = (TreeElement)SourceTreeToPsiMap.psiElementToTree(ref);
            }
          }
        }
      }
      else if (element.getElementType() == JavaElementType.MODIFIER_LIST) {
        if (element.getUserData(INTERFACE_MODIFIERS_FLAG_KEY) != null) {
          element.putUserData(INTERFACE_MODIFIERS_FLAG_KEY, null);
          try {
            PsiModifierList modifierList = (PsiModifierList)SourceTreeToPsiMap.treeElementToPsi(element);
            if (element.getTreeParent().getElementType() == JavaElementType.FIELD) {
              modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
              modifierList.setModifierProperty(PsiModifier.STATIC, true);
              modifierList.setModifierProperty(PsiModifier.FINAL, true);
            }
            else if (element.getTreeParent().getElementType() == JavaElementType.METHOD) {
              modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
              modifierList.setModifierProperty(PsiModifier.ABSTRACT, true);
            }
            else if (element.getTreeParent().getElementType() == JavaElementType.ANNOTATION_METHOD) {
              modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
              modifierList.setModifierProperty(PsiModifier.ABSTRACT, true);
            }
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
      }
    }
    else if (shallDecodeEscapedTexts && element instanceof LeafElement && !(element instanceof OuterLanguageElement)) {
      if (!isInCData(element)) {
        final String original = element.getText();
        final String escaped = StringUtil.escapeXml(original);
        if (!Comparing.equal(original, escaped) && element.getCopyableUserData(ALREADY_ESCAPED) == null) {
          final LeafElement copy = (LeafElement)element.copyElement();
          copy.setText(escaped);
          element.getTreeParent().replaceChild(element, copy);
          copy.putCopyableUserData(ALREADY_ESCAPED, Boolean.TRUE);
          return copy;
        }
      }
    }

    return element;
  }

  private static final Key<Boolean> ALREADY_ESCAPED = new Key<Boolean>("ALREADY_ESCAPED");

  public static TreeElement copyElement(TreeElement original, CharTable table) {
    final TreeElement element = (TreeElement)original.clone();
    final PsiManager manager = original.getManager();
    final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(original);
    registerLeafsInCharTab(table, element, charTableByTree);
    CompositeElement treeParent = original.getTreeParent();
    new JavaDummyHolder(manager, element, treeParent == null ? null : treeParent.getPsi(), table).getTreeElement();
    encodeInformation(element, original);
    TreeUtil.clearCaches(element);
    saveIndentationToCopy(original, element);
    return element;
  }

  private static void saveIndentationToCopy(final TreeElement original, final TreeElement element) {
    if(original == null || element == null || CodeEditUtil.isNodeGenerated(original)) return;
    final int indentation = CodeEditUtil.getOldIndentation(original);
    if(indentation < 0) CodeEditUtil.saveWhitespacesInfo(original);
    CodeEditUtil.setOldIndentation(element, CodeEditUtil.getOldIndentation(original));
    if(indentation < 0) CodeEditUtil.setOldIndentation(original, -1);
  }

  public static TreeElement copyToElement(PsiElement original) {
    final JavaDummyHolder holder = new JavaDummyHolder(original.getManager(), null);
    holder.setLanguage(original.getLanguage());
    final FileElement holderElement = holder.getTreeElement();
    final TreeElement treeElement = _copyToElement(original, holderElement.getCharTable(), original.getManager());
    //  TreeElement treePrev = treeElement.getTreePrev(); // This is hack to support bug used in formater
    TreeUtil.addChildren(holderElement, treeElement);
    TreeUtil.clearCaches(holderElement);
    //  treeElement.setTreePrev(treePrev);
    saveIndentationToCopy((TreeElement)original.getNode(), treeElement);
    return treeElement;
  }

  private static TreeElement _copyToElement(PsiElement original, CharTable table, final PsiManager manager) {
    LOG.assertTrue(original.isValid());
    if (SourceTreeToPsiMap.hasTreeElement(original)) {
      return copyElement((TreeElement)SourceTreeToPsiMap.psiElementToTree(original), table);
    }
    else if (original instanceof PsiIdentifier) {
      final String text = original.getText();
      return createLeafFromText(text, table, manager, original, JavaTokenType.IDENTIFIER);
    }
    else if (original instanceof PsiKeyword) {
      final String text = original.getText();
      return createLeafFromText(text, table, manager, original, ((PsiKeyword)original).getTokenType());
    }
    else if (original instanceof PsiReferenceExpression) {
      TreeElement element = createReferenceExpression(original.getManager(), original.getText(), table);
      PsiElement refElement = ((PsiJavaCodeReferenceElement)original).resolve();
      if (refElement instanceof PsiClass) {
        element.putCopyableUserData(REFERENCED_CLASS_KEY, (PsiClass)refElement);
      }
      return element;
    }
    else if (original instanceof PsiJavaCodeReferenceElement) {
      PsiElement refElement = ((PsiJavaCodeReferenceElement)original).resolve();
      final boolean generated = refElement != null && CodeEditUtil.isNodeGenerated(refElement.getNode());
      if (refElement instanceof PsiClass) {
        if (refElement instanceof PsiAnonymousClass) {
          PsiJavaCodeReferenceElement ref = ((PsiAnonymousClass)refElement).getBaseClassReference();
          original = ref;
          refElement = ref.resolve();
        }

        boolean isFQ = false;
        if (original instanceof PsiJavaCodeReferenceElementImpl) {
          int kind = ((PsiJavaCodeReferenceElementImpl)original).getKind();
          switch (kind) {
          case PsiJavaCodeReferenceElementImpl.CLASS_OR_PACKAGE_NAME_KIND:
          case PsiJavaCodeReferenceElementImpl.CLASS_NAME_KIND:
          case PsiJavaCodeReferenceElementImpl.CLASS_IN_QUALIFIED_NEW_KIND:
                 isFQ = false;
                 break;

          case PsiJavaCodeReferenceElementImpl.CLASS_FQ_NAME_KIND:
          case PsiJavaCodeReferenceElementImpl.CLASS_FQ_OR_PACKAGE_NAME_KIND:
                 isFQ = true;
                 break;

          default:
                 LOG.assertTrue(false);
          }
        }

        String text = isFQ ? ((PsiClass)refElement).getQualifiedName() : original.getText();
        TreeElement element = createReference(original.getManager(), text, table, generated);
        element.putCopyableUserData(REFERENCED_CLASS_KEY, (PsiClass)refElement);
        return element;
      }
      else {
        return createReference(original.getManager(), original.getText(), table, generated);
      }
    }
    else if (original instanceof PsiCompiledElement) {
      PsiElement sourceVersion = original.getNavigationElement();
      if (sourceVersion != original) {
        return _copyToElement(sourceVersion, table,manager);
      }
      ASTNode mirror = SourceTreeToPsiMap.psiElementToTree(((PsiCompiledElement)original).getMirror());
      return _copyToElement(SourceTreeToPsiMap.treeElementToPsi(mirror), table,manager);
    }
    else if (original instanceof PsiTypeElement) {
      final boolean generated = CodeEditUtil.isNodeGenerated(original.getNode());
      PsiTypeElement typeElement = (PsiTypeElement)original;
      PsiType type = typeElement.getType();
      if (type instanceof PsiEllipsisType) {
        TreeElement componentTypeCopy = _copyToElement(
          new LightTypeElement(original.getManager(), ((PsiEllipsisType)type).getComponentType()),
          table,
        manager);
        if (componentTypeCopy == null) return null;
        CompositeElement element = Factory.createCompositeElement(JavaElementType.TYPE);
        CodeEditUtil.setNodeGenerated(element, generated);
        TreeUtil.addChildren(element, componentTypeCopy);
        TreeUtil.addChildren(element, createLeafFromText("...", table, manager, original, JavaTokenType.ELLIPSIS));
        return element;
      }
      else if (type instanceof PsiArrayType) {
        TreeElement componentTypeCopy = _copyToElement(
          new LightTypeElement(original.getManager(), ((PsiArrayType)type).getComponentType()),
          table,
        manager);
        if (componentTypeCopy == null) return null;
        CompositeElement element = Factory.createCompositeElement(JavaElementType.TYPE);
        CodeEditUtil.setNodeGenerated(element, generated);
        TreeUtil.addChildren(element, componentTypeCopy);
        TreeUtil.addChildren(element, createLeafFromText("[", table, manager, original, JavaTokenType.LBRACKET));
        TreeUtil.addChildren(element, createLeafFromText("]", table, manager, original, JavaTokenType.LBRACKET));
        return element;
      }
      else if (type instanceof PsiPrimitiveType) {
        @NonNls String text = typeElement.getText();
        if (text.equals("null")) return null;
        Lexer lexer = new JavaLexer(LanguageLevel.JDK_1_3);
        lexer.start(text,0, text.length(),0);
        TreeElement keyword = ParseUtil.createTokenElement(lexer, table);
        CodeEditUtil.setNodeGenerated(keyword, generated);
        CompositeElement element = Factory.createCompositeElement(JavaElementType.TYPE);
        CodeEditUtil.setNodeGenerated(element, generated);
        TreeUtil.addChildren(element, keyword);
        return element;
      }
      else if (type instanceof PsiWildcardType) {
        String originalText = original.getText();
        final CompositeElement element = Parsing.parseTypeText(original.getManager(), originalText, 0, originalText.length(), table);
        if(generated) element.getTreeParent().acceptTree(new GeneratedMarkerVisitor());
        return element;
      }
      else {
        PsiClassType classType = (PsiClassType)type;
        final PsiJavaCodeReferenceElement ref;
        if (classType instanceof PsiClassReferenceType) {
          ref = ((PsiClassReferenceType)type).getReference();
        }
        else {
          final CompositeElement reference = createReference(original.getManager(), classType.getPresentableText(), table, generated);
          final CompositeElement immediateTypeElement = Factory.createCompositeElement(JavaElementType.TYPE);
          CodeEditUtil.setNodeGenerated(immediateTypeElement, generated);
          TreeUtil.addChildren(immediateTypeElement, reference);
          encodeInfoInTypeElement(immediateTypeElement, classType);
          return immediateTypeElement;
        }

        CompositeElement element = Factory.createCompositeElement(JavaElementType.TYPE);
        CodeEditUtil.setNodeGenerated(element, generated);
        TreeUtil.addChildren(element, _copyToElement(ref, table,manager));
        return element;
      }
    }
    else {
      LOG.error("ChangeUtil.copyToElement() unknown element " + original + " of type " + original.getClass());
      return null;
    }
  }

  private static LeafElement createLeafFromText(final String text,
                                                final CharTable table,
                                                final PsiManager manager,
                                                final PsiElement original,
                                                final IElementType type) {
    return Factory.createSingleLeafElement(type, text, 0, text.length(), table, manager, CodeEditUtil.isNodeGenerated(original.getNode()));
  }

  private static CompositeElement createReference(PsiManager manager, String text, CharTable table, boolean generatedFlag) {
    final CompositeElement element = Parsing.parseJavaCodeReferenceText(manager, text, table);
    if(generatedFlag) element.acceptTree(new GeneratedMarkerVisitor());
    return element;
  }

  private static TreeElement createReferenceExpression(PsiManager manager, String text, CharTable table) {
    return ExpressionParsing.parseExpressionText(manager, text, 0, text.length(), table);
  }


  private static void encodeInfoInTypeElement(ASTNode typeElement, PsiType type) {
    if (type instanceof PsiPrimitiveType) return;
    LOG.assertTrue(typeElement.getElementType() == JavaElementType.TYPE);
    if (type instanceof PsiArrayType) {
      final ASTNode firstChild = typeElement.getFirstChildNode();
      LOG.assertTrue(firstChild.getElementType() == JavaElementType.TYPE);
      encodeInfoInTypeElement(firstChild, ((PsiArrayType)type).getComponentType());
    }
    else if (type instanceof PsiWildcardType) {
      final PsiType bound = ((PsiWildcardType)type).getBound();
      if (bound == null) return;
      final ASTNode lastChild = typeElement.getLastChildNode();
      if (lastChild.getElementType() != JavaElementType.TYPE) return;
      encodeInfoInTypeElement(lastChild, bound);
    }
    else if (type instanceof PsiCapturedWildcardType) {
      final PsiType bound = ((PsiCapturedWildcardType)type).getWildcard().getBound();
      if (bound == null) return;
      final ASTNode lastChild = typeElement.getLastChildNode();
      if (lastChild.getElementType() != JavaElementType.TYPE) return;
      encodeInfoInTypeElement(lastChild, bound);
    }
    else if (type instanceof PsiIntersectionType) {
      encodeInfoInTypeElement(typeElement, ((PsiIntersectionType)type).getRepresentative());
    }
    else {
      LOG.assertTrue(type instanceof PsiClassType);
      final PsiClassType classType = (PsiClassType)type;
      final PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
      PsiClass referencedClass = resolveResult.getElement();
      if (referencedClass == null) return;
      if (referencedClass instanceof PsiAnonymousClass) {
        encodeInfoInTypeElement(typeElement, ((PsiAnonymousClass)referencedClass).getBaseClassType());
      }
      else {
        final ASTNode reference = typeElement.getFirstChildNode();
        LOG.assertTrue(reference.getElementType() == JavaElementType.JAVA_CODE_REFERENCE);

        encodeClassTypeInfoInReference((CompositeElement)reference, resolveResult.getElement(), resolveResult.getSubstitutor());
      }
    }
  }

  private static void encodeClassTypeInfoInReference(CompositeElement reference, PsiClass referencedClass, PsiSubstitutor substitutor) {
    LOG.assertTrue(referencedClass != null);
    reference.putCopyableUserData(REFERENCED_CLASS_KEY, referencedClass);

    final PsiTypeParameter[] typeParameters = referencedClass.getTypeParameters();
    if (typeParameters.length == 0) return;

    final ASTNode referenceParameterList = reference.findChildByRole(ChildRole.REFERENCE_PARAMETER_LIST);
    int index = 0;
    for (ASTNode child = referenceParameterList.getFirstChildNode(); child != null && index < typeParameters.length; child = child.getTreeNext()) {
      if (child.getElementType() == JavaElementType.TYPE) {
        final PsiType substitutedType = substitutor.substitute(typeParameters[index]);
        if (substitutedType != null) {
          encodeInfoInTypeElement(child, substitutedType);
        }
        index++;
      }
    }

    final ASTNode qualifier = reference.findChildByRole(ChildRole.QUALIFIER);
    if (qualifier != null) {
      if (referencedClass.hasModifierProperty(PsiModifier.STATIC)) return;
      final PsiClass outerClass = referencedClass.getContainingClass();
      if (outerClass != null) {
        encodeClassTypeInfoInReference((CompositeElement)qualifier, outerClass, substitutor);
      }
    }
  }

  private static final Key<PsiClass> REFERENCED_CLASS_KEY = Key.create("REFERENCED_CLASS_KEY");
  private static final Key<PsiMember> REFERENCED_MEMBER_KEY = Key.create("REFERENCED_MEMBER_KEY");
  private static final Key<Boolean> INTERFACE_MODIFIERS_FLAG_KEY = Key.create("INTERFACE_MODIFIERS_FLAG_KEY");

  public static void addChildren(final ASTNode parent,
                                 ASTNode firstChild,
                                 final ASTNode lastChild,
                                 final ASTNode anchorBefore) {
    while (firstChild != lastChild) {
      final ASTNode next = firstChild.getTreeNext();
      parent.addChild(firstChild, anchorBefore);
      firstChild = next;
    }
  }

//  public static int checkTextRanges(PsiElement root) {
//    if (true) return 0;
//    TextRange range = root.getTextRange();
//    int off = range.getStartOffset();
//    PsiElement[] children = root.getChildren();
//    if (children.length != 0) {
//      for (PsiElement child : children) {
//        off += checkTextRanges(child);
//      }
//    }
//    else {
//      off += root.getTextLength();
//    }
//    LOG.assertTrue(off == range.getEndOffset());
//
//    String fileText = root.getContainingFile().getText();
//    LOG.assertTrue(root.getText().equals(fileText.substring(range.getStartOffset(), range.getEndOffset())));
//    if (root instanceof XmlTag) {
//      XmlTagValue value = ((XmlTag)root).getValue();
//      TextRange textRange = value.getTextRange();
//      LOG.assertTrue(value.getText().equals(fileText.substring(textRange.getStartOffset(), textRange.getEndOffset())));
//    }
//    return range.getLength();
//  }
}