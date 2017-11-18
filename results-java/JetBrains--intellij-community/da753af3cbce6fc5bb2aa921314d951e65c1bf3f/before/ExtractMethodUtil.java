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

package org.jetbrains.plugins.groovy.refactoring.extractMethod;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.ui.ConflictsDialog;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.intentions.utils.DuplicatesUtil;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.branch.GrReturnStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrParenthesizedExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrIndexProperty;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrMethodOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrStatementOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrVariableDeclarationOwner;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GroovyScriptClass;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author ilyas
 */
public class ExtractMethodUtil {

  public enum MethodAccessQualifier {
    PUBLIC, PRIVATE, PROTECTED
  }

  @NotNull
  static GrStatement createResultStatement(ExtractMethodInfoHelper helper, @NotNull String methodName) {
    String name = helper.getOutputName();
    PsiType type = helper.getOutputType();
    GrStatement[] statements = helper.getStatements();
    GrMethodCallExpression callExpression = createMethodCallByHelper(methodName, helper);
    if (name == null || type == PsiType.VOID) return callExpression;
    GroovyElementFactory factory = GroovyElementFactory.getInstance(helper.getProject());
    if (mustAddVariableDeclaration(statements, name)) {
      return factory.createVariableDeclaration(new String[0], name, callExpression, type.equalsToText("java.lang.Object") ? null : type, false);
    } else {
      return factory.createExpressionFromText(name + "= " + callExpression.getText());
    }
  }

  static boolean validateMethod(GrMethod method, ExtractMethodInfoHelper helper) {
    ArrayList<String> conflicts = new ArrayList<String>();
    GrMethodOwner owner = helper.getOwner();
    assert owner != null;
    PsiMethod[] methods = ArrayUtil.mergeArrays(owner.getAllMethods(), new PsiMethod[]{method}, PsiMethod.class);
    final Map<PsiMethod, List<PsiMethod>> map = DuplicatesUtil.factorDuplicates(methods, new TObjectHashingStrategy<PsiMethod>() {
      public int computeHashCode(PsiMethod method) {
        return method.getSignature(PsiSubstitutor.EMPTY).hashCode();
      }

      public boolean equals(PsiMethod method1, PsiMethod method2) {
        return method1.getSignature(PsiSubstitutor.EMPTY).equals(method2.getSignature(PsiSubstitutor.EMPTY));
      }
    });

    List<PsiMethod> list = map.get(method);
    if (list == null) return true;
    for (PsiMethod psiMethod : list) {
      if (psiMethod != method) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) return true;
        String message = containingClass instanceof GroovyScriptClass ?
            GroovyRefactoringBundle.message("method.is.alredy.defined.in.script", GroovyRefactoringUtil.getMethodSignature(method),
                CommonRefactoringUtil.htmlEmphasize(containingClass.getQualifiedName())) :
            GroovyRefactoringBundle.message("method.is.alredy.defined.in.class", GroovyRefactoringUtil.getMethodSignature(method),
                CommonRefactoringUtil.htmlEmphasize(containingClass.getQualifiedName()));
        conflicts.add(message);
      }
    }

    return conflicts.size() <= 0 || reportConflicts(conflicts, helper.getProject());
  }

  static boolean reportConflicts(final ArrayList<String> conflicts, final Project project) {
    ConflictsDialog conflictsDialog = new ConflictsDialog(project, conflicts);
    conflictsDialog.show();
    return conflictsDialog.isOK();
  }


  static void removeOldStatements(GrStatementOwner owner, ExtractMethodInfoHelper helper) throws IncorrectOperationException {
    ASTNode parentNode = owner.getNode();
    for (PsiElement element : helper.getInnerElements()) {
      ASTNode node = element.getNode();
      if (node == null || node.getTreeParent() != parentNode) {
        throw new IncorrectOperationException();
      }
      parentNode.removeChild(node);
    }
  }

  /*
  To declare or not a variable to which method call result will be assigned.
   */
  private static boolean mustAddVariableDeclaration(@NotNull GrStatement[] statements, @NotNull String varName) {
    return ResolveUtil.resolveProperty(statements[0], varName) == null;
  }

  private static boolean containVariableDeclaration(@NotNull GrStatement[] statements, @NotNull String varName) {
    GroovyPsiElement element = ResolveUtil.resolveProperty(statements[0], varName);
    if (element == null) return false;
    for (GrStatement statement : statements) {
      if (statement.getTextRange().contains(element.getTextRange())) {
        return true;
      }
    }
    return false;
  }

  static void renameParameterOccurrences(GrMethod method, ExtractMethodInfoHelper helper) throws IncorrectOperationException {
    GrOpenBlock block = method.getBlock();
    if (block == null) return;
    GrStatement[] statements = block.getStatements();

    final GroovyElementFactory factory = GroovyElementFactory.getInstance(helper.getProject());
    for (ParameterInfo info : helper.getParameterInfos()) {
      final String oldName = info.getOldName();
      final String newName = info.getName();
      final ArrayList<GrExpression> result = new ArrayList<GrExpression>();
      if (!oldName.equals(newName)) {
        for (final GrStatement statement : statements) {
          statement.accept(new PsiRecursiveElementVisitor() {
            public void visitElement(final PsiElement element) {
              super.visitElement(element);
              if (element instanceof GrReferenceExpression) {
                GrReferenceExpression expr = (GrReferenceExpression) element;
                if (!expr.isQualified() && oldName.equals(expr.getName())) {
                  result.add(expr);
                }
              }
            }
          });
          for (GrExpression expr : result) {
            expr.replaceWithExpression(factory.createExpressionFromText(newName), false);
          }
        }
      }
    }
  }

  static GrMethod createMethodByHelper(@NotNull String name, ExtractMethodInfoHelper helper) {
    StringBuffer buffer = new StringBuffer();

    //Add signature
    PsiType type = helper.getOutputType();
    final PsiPrimitiveType outUnboxed = PsiPrimitiveType.getUnboxedType(type);
    if (outUnboxed != null) type = outUnboxed;
    String typeText = getTypeString(helper, false);
    buffer.append(getModifierString(helper));
    buffer.append(typeText);
    buffer.append(name);
    buffer.append("(");
    for (String param : getParameterString(helper)) {
      buffer.append(param);
    }
    buffer.append(") { \n");

    GroovyElementFactory factory = GroovyElementFactory.getInstance(helper.getProject());
    String outputName = helper.getOutputName();

    ParameterInfo[] infos = helper.getParameterInfos();
    boolean outputIsParameter = false;
    if (outputName != null) {
      for (ParameterInfo info : infos) {
        if (outputName.equals(info.getOldName())) {
          outputIsParameter = true;
        }
      }
    }
    ArrayList<GrVariableDeclaration> missedDeclarations = new ArrayList<GrVariableDeclaration>();
    for (ParameterInfo info : infos) {
      if (!info.passAsParameter()) {
        missedDeclarations.add(factory.createVariableDeclaration(new String[0], info.getName(), null, info.getType(), true));
      }
    }

    if (type != PsiType.VOID && outputName != null && !outputIsParameter &&
        !mustAddVariableDeclaration(helper.getStatements(), outputName) &&
        !containVariableDeclaration(helper.getStatements(), outputName)) {
      GrVariableDeclaration decl = factory.createVariableDeclaration(new String[0], outputName, null, type, false);
      buffer.append(decl.getText()).append("\n");
    }
    // Add missed parameter declarations
    for (GrVariableDeclaration declaration : missedDeclarations) {
      buffer.append(declaration.getText()).append(" ;\n");
    }

    if (!ExtractMethodUtil.isSingleExpression(helper.getStatements())) {
      for (PsiElement element : helper.getInnerElements()) {
        buffer.append(element.getText());
      }
      //append return statement
      if (type != PsiType.VOID && outputName != null) {
        buffer.append("\n return ");
        buffer.append(outputName);
      }
    } else {
      GrExpression expr = (GrExpression) helper.getStatements()[0];
      while (expr instanceof GrParenthesizedExpression) {
        expr = ((GrParenthesizedExpression) expr).getOperand();
      }
      buffer.append(PsiType.VOID == type ? "" : "return ").append(expr != null ? expr.getText() : "");
    }

    buffer.append("\n}");

    String methodText = buffer.toString();
    GrMethod method = factory.createMethodFromText(methodText);
    assert method != null;
    return method;
  }

  static String[] getParameterString(ExtractMethodInfoHelper helper) {
    int i = 0;
    ParameterInfo[] infos = helper.getParameterInfos();
    int number = 0;
    for (ParameterInfo info : infos) {
      if (info.passAsParameter()) number++;
    }
    ArrayList<String> params = new ArrayList<String>();
    for (ParameterInfo info : infos) {
      if (info.passAsParameter()) {
        PsiType paramType = info.getType();
        final PsiPrimitiveType unboxed = PsiPrimitiveType.getUnboxedType(paramType);
        if (unboxed != null) paramType = unboxed;
        String paramTypeText = paramType == null || paramType.equalsToText("java.lang.Object") ? "" : paramType.getCanonicalText() + " ";
        params.add(paramTypeText + info.getName() + (i < number - 1 ? ", " : ""));
        i++;
      }
    }
    return params.toArray(new String[params.size()]);
  }

  static String getTypeString(ExtractMethodInfoHelper helper, boolean forPresentation) {
    PsiType type = helper.getOutputType();
    final PsiPrimitiveType outUnboxed = PsiPrimitiveType.getUnboxedType(type);
    if (outUnboxed != null) type = outUnboxed;
    String typeText = forPresentation ? type.getPresentableText() : type.getCanonicalText();
    String returnType = typeText.equals("void") || typeText.equals("Object") || !helper.specifyType() ? "" : typeText;
    if (returnType.length() == 0) {
      typeText = "def ";
    } else {
      typeText = returnType + " ";
    }
    return typeText;
  }

  static GrStatement[] getStatementsByElements(PsiElement[] elements) {
    ArrayList<GrStatement> statementList = new ArrayList<GrStatement>();
    for (PsiElement element : elements) {
      if (element instanceof GrStatement) {
        statementList.add(((GrStatement) element));
      }
    }
    return statementList.toArray(new GrStatement[statementList.size()]);
  }

  static PsiElement[] getElementsInOffset(PsiFile file, int startOffset, int endOffset) {
    PsiElement[] elements;
    GrExpression expr = GroovyRefactoringUtil.findElementInRange(((GroovyFileBase) file), startOffset, endOffset, GrExpression.class);

    if (expr != null) {
      PsiElement parent = expr.getParent();
      if (expr.getParent() instanceof GrMethodCallExpression || parent instanceof GrIndexProperty) {
        expr = ((GrExpression) expr.getParent());
      }
      elements = new PsiElement[]{expr};
    } else {
      elements = GroovyRefactoringUtil.findStatementsInRange(file, startOffset, endOffset);
    }
    return elements;
  }

  @Nullable
  static GrMethodOwner getMethodOwner(GrStatement statement) {
    PsiElement parent = statement.getParent();
    while (parent != null && !(parent instanceof GrMethodOwner) && !(parent instanceof PsiFile)) {
      parent = parent.getParent();
    }
    return parent instanceof GrMethodOwner ? ((GrMethodOwner) parent) : null;
  }

  @Nullable
  static GrVariableDeclarationOwner getDecalarationOwner(GrStatement statement) {
    PsiElement parent = statement.getParent();
    return parent instanceof GrVariableDeclarationOwner ? ((GrVariableDeclarationOwner) parent) : null;
  }

  static boolean isSingleExpression(GrStatement[] statements) {
    return statements.length == 1 && statements[0] instanceof GrExpression &&
        !(statements[0].getParent() instanceof GrVariableDeclarationOwner && statements[0] instanceof GrAssignmentExpression);
  }

  // Get declaration of variable to which method call expression will be assigned
  private static GroovyPsiElement getVariableDeclaration(@NotNull GrStatement[] statements, @NotNull String varName) {
    GroovyPsiElement variable = null;
    for (GrStatement statement : statements) {
      variable = getInnerVariableDeclaration(statement, varName);
      if (variable != null) break;
    }
    return variable;
  }

  private static GroovyPsiElement getInnerVariableDeclaration(@NotNull PsiElement element, @NotNull String name) {
    if (element instanceof GrReferenceExpression) {
      GrReferenceExpression expr = (GrReferenceExpression) element;
      if (name.equals(expr.getName())) {
        PsiReference ref = expr.getReference();
        if (ref != null) {
          PsiElement resolved = ref.resolve();
          if ((resolved instanceof GrVariable || resolved instanceof GrReferenceExpression) && !(resolved instanceof GrField)) {
            return ((GroovyPsiElement) resolved);
          }
        }
        return null;
      } else {
        return null;
      }
    }
    for (PsiElement child : element.getChildren()) {
      GroovyPsiElement res = getInnerVariableDeclaration(child, name);
      if (res != null) return res;
    }
    return null;
  }

  static GrMethodCallExpression createMethodCallByHelper(@NotNull String name, ExtractMethodInfoHelper helper) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(name).append("(");
    int number = 0;
    for (ParameterInfo info : helper.getParameterInfos()) {
      if (info.passAsParameter()) number++;
    }
    int i = 0;
    String[] argumentNames = helper.getArgumentNames();
    for (String argName : argumentNames) {
      if (argName.length() > 0) {
        buffer.append(argName);
        if (i < number - 1) {
          buffer.append(",");
        }
        i++;
      }
    }

    buffer.append(")");
    String callText = buffer.toString();
    GroovyElementFactory factory = GroovyElementFactory.getInstance(helper.getProject());
    GrExpression expr = factory.createExpressionFromText(callText);
    assert expr instanceof GrMethodCallExpression;
    return ((GrMethodCallExpression) expr);
  }

  /*
  Some cosmetics
   */
  static void removeNewLineAfter(@NotNull GrStatement statement) {
    ASTNode parentNode = statement.getParent().getNode();
    ASTNode next = statement.getNode().getTreeNext();
    if (parentNode != null && next != null && GroovyTokenTypes.mNLS == next.getElementType()) {
      parentNode.removeChild(next);
    }
  }

  static int getCaretOffset(@NotNull GrStatement statement) {
    if (statement instanceof GrVariableDeclaration) {
      GrVariable[] variables = ((GrVariableDeclaration) statement).getVariables();
      if (variables.length > 0) {
        GrExpression initializer = variables[0].getInitializerGroovy();
        if (initializer != null) {
          return initializer.getTextOffset();
        }
      }
    } else if (statement instanceof GrAssignmentExpression) {
      GrExpression value = ((GrAssignmentExpression) statement).getRValue();
      if (value != null) {
        return value.getTextOffset();
      }
    } else if (statement instanceof GrMethodCallExpression) {
      return statement.getTextOffset();
    }
    return statement.getTextOffset();
  }

  static boolean canBeStatic(GrStatement statement) {
    PsiElement parent = statement.getParent();
    while (parent != null && !(parent instanceof PsiFile)) {
      if (parent instanceof GrMethod) {
        return ((GrMethod) parent).hasModifierProperty(PsiModifier.STATIC);
      }
      parent = parent.getParent();
    }
    return false;
  }

  static String getModifierString(ExtractMethodInfoHelper helper) {
    String visibility = helper.getVisibility();
    assert visibility != null && visibility.length() > 0;
    visibility = visibility.equals(PsiModifier.PUBLIC) ? "" : visibility + " ";
    return visibility + (helper.isStatic() ? "static " : "");
  }

  static boolean isReturnStatement(GrStatement statement, Collection<GrReturnStatement> returnStatements){
    return false;
  }

}