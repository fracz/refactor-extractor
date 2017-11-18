/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.GrControlFlowOwner;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.groovy.lang.psi.controlFlow.impl.ControlFlowBuilder;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public abstract class GroovyFileBaseImpl extends PsiFileBase implements GroovyFileBase, GrControlFlowOwner {
  private GrVariable[] myTopLevelVariables;

  protected GroovyFileBaseImpl(FileViewProvider viewProvider, @NotNull Language language) {
    super(viewProvider, language);
  }

  public GroovyFileBaseImpl(IFileElementType root, IFileElementType root1, FileViewProvider provider) {
    this(provider, root.getLanguage());
    init(root, root1);
  }

  @NotNull
  public FileType getFileType() {
    return GroovyFileType.GROOVY_FILE_TYPE;
  }

  public String toString() {
    return "Groovy script";
  }

  public GrTypeDefinition[] getTypeDefinitions() {
    return findChildrenByClass(GrTypeDefinition.class);
  }

  public GrTopLevelDefintion[] getTopLevelDefinitions() {
    return findChildrenByClass(GrTopLevelDefintion.class);
  }

  public GrMethod[] getTopLevelMethods() {
    return findChildrenByClass(GrMethod.class);
  }

  public GrVariable[] getTopLevelVariables() {
    if (myTopLevelVariables == null) {
      GrVariableDeclaration[] declarations = findChildrenByClass(GrVariableDeclaration.class);
      if (declarations.length == 0) return GrField.EMPTY_ARRAY;
      ArrayList<GrVariable> result = new ArrayList<GrVariable>();
      for (GrVariableDeclaration declaration : declarations) {
        GrVariable[] variables = declaration.getVariables();
        for (GrVariable variable : variables) {
          result.add(variable);
        }
      }

      myTopLevelVariables = result.toArray(new GrVariable[result.size()]);
    }

    return myTopLevelVariables;
  }

  public GrTopStatement[] getTopStatements() {
    return findChildrenByClass(GrTopStatement.class);
  }

  public void removeImport(GrImportStatement importStatement) throws IncorrectOperationException {
    PsiElement next = PsiTreeUtil.skipSiblingsForward(importStatement, PsiWhiteSpace.class);
    while (next != null && next.getNode() != null && next.getNode().getElementType() == GroovyTokenTypes.mSEMI) {
      next = next.getNextSibling();
      if (next instanceof PsiWhiteSpace) {
        next = PsiTreeUtil.skipSiblingsForward(next, PsiWhiteSpace.class);
      }
    }
    if (next != null) {
      ASTNode astNode = next.getNode();
      if (astNode != null && astNode.getElementType() == GroovyTokenTypes.mNLS) {
        deleteChildRange(importStatement, next);
      } else {
        deleteChildRange(importStatement, importStatement);
      }
    }
  }

  public GrStatement addStatementBefore(@NotNull GrStatement statement, @NotNull GrStatement anchor) throws IncorrectOperationException {
    final PsiElement result = addBefore(statement, anchor);
    getNode().addLeaf(GroovyTokenTypes.mNLS, "\n", anchor.getNode());
    PsiDocumentManager manager = PsiDocumentManager.getInstance(getProject());
    Document document = manager.getDocument(this);
    if (document != null) {
      manager.doPostponedOperationsAndUnblockDocument(document);
    }
    CodeStyleManager.getInstance(getProject()).adjustLineIndent(this, result.getTextRange());
    return (GrStatement) result;
  }

  public void removeVariable(GrVariable variable) throws IncorrectOperationException {
    PsiImplUtil.removeVariable(variable);
  }

  public GrVariableDeclaration addVariableDeclarationBefore(GrVariableDeclaration declaration, GrStatement anchor) throws IncorrectOperationException {
    GrStatement statement = addStatementBefore(declaration, anchor);
    assert statement instanceof GrVariableDeclaration;
    return ((GrVariableDeclaration) statement);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitFile(this);
  }

  public void acceptChildren(GroovyElementVisitor visitor) {
    PsiElement child = getFirstChild();
    while (child != null) {
      if (child instanceof GroovyPsiElement) {
        ((GroovyPsiElement) child).accept(visitor);
      }

      child = child.getNextSibling();
    }
  }

  @NotNull
  public PsiClass[] getClasses() {
    return getTypeDefinitions();
  }

  public void clearCaches() {
    super.clearCaches();
    myControlFlow = null;
    myTopLevelVariables = null;
  }


  private Instruction[] myControlFlow = null;

  public Instruction[] getControlFlow() {
    if (myControlFlow == null) {
      myControlFlow = new ControlFlowBuilder().buildControlFlow(this, null, null);
    }

    return myControlFlow;
  }
}