package com.intellij.psi.impl.source.tree.java;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.jsp.JspContextManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspCodeBlock;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.jsp.JspElementType;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.jsp.JspSpiUtil;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.RowIcon;
import com.intellij.util.CharTable;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;

public class PsiLocalVariableImpl extends CompositePsiElement implements PsiLocalVariable, PsiVariableEx {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl");

  private volatile String myCachedName = null;

  public PsiLocalVariableImpl() {
    super(LOCAL_VARIABLE);
  }

  public void clearCaches() {
    super.clearCaches();
    myCachedName = null;
  }

  @NotNull
  public final PsiIdentifier getNameIdentifier() {
    return (PsiIdentifier)findChildByRoleAsPsiElement(ChildRole.NAME);
  }

  public final String getName() {
    String cachedName = myCachedName;
    if (cachedName == null){
      myCachedName = cachedName = getNameIdentifier().getText();
    }
    return cachedName;
  }

  public void setInitializer(PsiExpression initializer) throws IncorrectOperationException {
    SharedImplUtil.setInitializer(this, initializer);
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    SharedPsiElementImplUtil.setName(getNameIdentifier(), name);
    return this;
  }

  @NotNull
  public final PsiType getType() {
    return SharedImplUtil.getType(this);
  }

  @NotNull
  public PsiTypeElement getTypeElement() {
    ASTNode first = TreeUtil.findChild(getTreeParent(), LOCAL_VARIABLE);
    return (PsiTypeElement)SourceTreeToPsiMap.treeElementToPsi(TreeUtil.findChild(first, TYPE));
  }

  public PsiModifierList getModifierList() {
    CompositeElement parent = getTreeParent();
    if (parent == null) return null;
    CompositeElement first = (CompositeElement)TreeUtil.findChild(parent, LOCAL_VARIABLE);
    return (PsiModifierList)first.findChildByRoleAsPsiElement(ChildRole.MODIFIER_LIST);
  }

  public boolean hasModifierProperty(@NotNull String name) {
    return getModifierList().hasModifierProperty(name);
  }

  public PsiExpression getInitializer() {
    return (PsiExpression)findChildByRoleAsPsiElement(ChildRole.INITIALIZER);
  }

  public boolean hasInitializer() {
    return getInitializer() != null;
  }

  public Object computeConstantValue() {
    return computeConstantValue(new THashSet<PsiVariable>());
  }

  public Object computeConstantValue(Set<PsiVariable> visitedVars) {
    if (!hasModifierProperty(PsiModifier.FINAL)) return null;

    PsiType type = getType();
    // javac rejects all non primitive and non String constants, although JLS states constants "variables whose initializers are constant expressions"
    if (!(type instanceof PsiPrimitiveType) && !type.equalsToText("java.lang.String")) return null;

    PsiExpression initializer = getInitializer();
    if (initializer == null) return null;
    return PsiConstantEvaluationHelperImpl.computeCastTo(initializer, getType(), visitedVars);
  }

  public int getTextOffset() {
    return getNameIdentifier().getTextOffset();
  }

  public void normalizeDeclaration() throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    final CharTable treeCharTab = SharedImplUtil.findCharTableByTree(this);

    CompositeElement statement = getTreeParent();
    PsiElement[] variables = ((PsiDeclarationStatement)SourceTreeToPsiMap.treeElementToPsi(statement)).getDeclaredElements();
    if (variables.length > 1){
      //CodeStyleManagerImpl codeStyleManager = (CodeStyleManagerImpl)getManager().getCodeStyleManager();
      ASTNode last = statement;
      for(int i = 1; i < variables.length; i++){
        ASTNode typeCopy = getTypeElement().copy().getNode();
        ASTNode modifierListCopy = getModifierList().copy().getNode();
        CompositeElement variable = (CompositeElement)SourceTreeToPsiMap.psiElementToTree(variables[i]);

        ASTNode comma = TreeUtil.skipElementsBack(variable.getTreePrev(), StdTokenSets.WHITE_SPACE_OR_COMMENT_BIT_SET);
        if (comma != null && comma.getElementType() == JavaTokenType.COMMA){
          CodeEditUtil.removeChildren(statement, comma, variable.getTreePrev());
        }

        CodeEditUtil.removeChild(statement, variable);
        final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(statement);
        CompositeElement statement1 = Factory.createCompositeElement(DECLARATION_STATEMENT, charTableByTree, getManager());
        statement1.addChild(variable, null);

        ASTNode space = Factory.createSingleLeafElement(JavaTokenType.WHITE_SPACE, " ", 0, 1, treeCharTab, getManager());
        variable.addChild(space, variable.getFirstChildNode());

        variable.addChild(typeCopy, variable.getFirstChildNode());

        if (modifierListCopy.getTextLength() > 0){
          space = Factory.createSingleLeafElement(JavaTokenType.WHITE_SPACE, " ", 0, 1, treeCharTab, getManager());
          variable.addChild(space, variable.getFirstChildNode());
        }

        variable.addChild(modifierListCopy, variable.getFirstChildNode());

        ASTNode semicolon = Factory.createSingleLeafElement(JavaTokenType.SEMICOLON, ";", 0, 1, treeCharTab, getManager());
        SourceTreeToPsiMap.psiElementToTree(variables[i - 1]).addChild(semicolon, null);

        CodeEditUtil.addChild(statement.getTreeParent(), statement1, last.getTreeNext());

        //?
        //codeStyleManager.adjustInsertedCode(statement1);
        last = statement1;
      }
    }

    SharedImplUtil.normalizeBrackets(this);
  }

  public void deleteChildInternal(@NotNull ASTNode child) {
    if (getChildRole(child) == ChildRole.INITIALIZER){
      ASTNode eq = findChildByRole(ChildRole.INITIALIZER_EQ);
      if (eq != null){
        deleteChildInternal(eq);
      }
    }
    super.deleteChildInternal(child);
  }

  public ASTNode findChildByRole(int role) {
    LOG.assertTrue(ChildRole.isUnique(role));
    switch(role){
      default:
        return null;

      case ChildRole.MODIFIER_LIST:
        return TreeUtil.findChild(this, MODIFIER_LIST);

      case ChildRole.TYPE:
        return TreeUtil.findChild(this, TYPE);

      case ChildRole.NAME:
        return TreeUtil.findChild(this, JavaTokenType.IDENTIFIER);

      case ChildRole.INITIALIZER_EQ:
        return TreeUtil.findChild(this, JavaTokenType.EQ);

      case ChildRole.INITIALIZER:
        return TreeUtil.findChild(this, ElementType.EXPRESSION_BIT_SET);

      case ChildRole.CLOSING_SEMICOLON:
        return TreeUtil.findChildBackward(this, JavaTokenType.SEMICOLON);
    }
  }

  public int getChildRole(ASTNode child) {
    LOG.assertTrue(child.getTreeParent() == this);
    IElementType i = child.getElementType();
    if (i == MODIFIER_LIST) {
      return ChildRole.MODIFIER_LIST;
    }
    else if (i == TYPE) {
      return getChildRole(child, ChildRole.TYPE);
    }
    else if (i == JavaTokenType.IDENTIFIER) {
      return getChildRole(child, ChildRole.NAME);
    }
    else if (i == JavaTokenType.EQ) {
      return getChildRole(child, ChildRole.INITIALIZER_EQ);
    }
    else if (i == JavaTokenType.SEMICOLON) {
      return getChildRole(child, ChildRole.CLOSING_SEMICOLON);
    }
    else {
      if (ElementType.EXPRESSION_BIT_SET.contains(child.getElementType())) {
        return ChildRole.INITIALIZER;
      }
      return ChildRole.NONE;
    }
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    visitor.visitLocalVariable(this);
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull PsiSubstitutor substitutor, PsiElement lastParent, @NotNull PsiElement place) {
    if (lastParent == null) return true;
    if (lastParent.getContext() instanceof DummyHolder) {
      return processor.execute(this, substitutor);
    }

    if (lastParent.getParent() != this) return true;
    final ASTNode lastParentTree = SourceTreeToPsiMap.psiElementToTree(lastParent);

    return getChildRole(lastParentTree) != ChildRole.INITIALIZER ||
           processor.execute(this, substitutor);
  }

  public ItemPresentation getPresentation() {
    return SymbolPresentationUtil.getVariablePresentation(this);
  }

  public String toString() {
    return "PsiLocalVariable:" + getName();
  }

  @NotNull
  public SearchScope getUseScope() {
    if (PsiUtil.isInJspFile(this)) {
      if (getTreeParent().getElementType() == JavaElementType.DECLARATION_STATEMENT &&
          getTreeParent().getTreeParent() instanceof JspCodeBlock &&
          getTreeParent().getTreeParent().getTreeParent().getElementType() == JspElementType.HOLDER_METHOD) { //?
        final JspFile jspFile = PsiUtil.getJspFile(this);
        final JspContextManager contextManager = JspContextManager.getInstance(getProject());
        if (contextManager == null) {
          return super.getUseScope();
        }

        final Set<PsiFile> allIncluded = new THashSet<PsiFile>(10);
        final JspFile rootContext = contextManager.getRootContextFile(jspFile);
        allIncluded.add(rootContext);
        JspSpiUtil.visitAllIncludedFilesRecursively(rootContext, new PsiElementVisitor() {
          @Override public void visitReferenceExpression(final PsiReferenceExpression expression) {
          }

          @Override public void visitFile(final PsiFile file) {
            allIncluded.add(file);
          }
        });

        return new LocalSearchScope(allIncluded.toArray(new PsiFile[allIncluded.size()]));
      }
    }

    return super.getUseScope();
  }

  public Icon getElementIcon(final int flags) {
    final RowIcon baseIcon = createLayeredIcon(Icons.VARIABLE_ICON, ElementPresentationUtil.getFlags(this, false));
    return ElementPresentationUtil.addVisibilityIcon(this, flags, baseIcon);
  }
}