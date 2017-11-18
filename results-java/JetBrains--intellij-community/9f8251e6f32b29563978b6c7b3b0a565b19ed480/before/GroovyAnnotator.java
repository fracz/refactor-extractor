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

package org.jetbrains.plugins.groovy.annotator;

import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.annotator.DomainClassAnnotator;
import org.jetbrains.plugins.grails.perspectives.DomainClassUtils;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.annotator.gutter.OverrideGutter;
import org.jetbrains.plugins.groovy.annotator.intentions.*;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.DElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.DynamicFix;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.DynamicManager;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.MyPair;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DItemElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DMethodElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DPropertyElement;
import org.jetbrains.plugins.groovy.codeInspection.GroovyImportsTracker;
import org.jetbrains.plugins.groovy.highlighter.DefaultHighlighter;
import org.jetbrains.plugins.groovy.intentions.utils.DuplicatesUtil;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocComment;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMemberReference;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocReferenceElement;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.branch.GrReturnStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.packaging.GrPackageDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrVariableDeclarationOwner;
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClosureType;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GroovyScriptClass;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.overrideImplement.quickFix.ImplementMethodsQuickFix;
import org.jetbrains.plugins.groovy.structure.GroovyElementPresentation;

import java.util.*;

/**
 * @author ven
 */
public class GroovyAnnotator implements Annotator {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.annotator.GroovyAnnotator");

  private GroovyAnnotator() {
  }

  public static final GroovyAnnotator INSTANCE = new GroovyAnnotator();

  public void annotate(PsiElement element, AnnotationHolder holder) {
    if (element instanceof GrCodeReferenceElement) {
      checkReferenceElement(holder, (GrCodeReferenceElement) element);
    } else if (element instanceof GrReferenceExpression) {
      checkReferenceExpression(holder, (GrReferenceExpression) element);
    } else if (element instanceof GrTypeDefinition) {
      final GrTypeDefinition typeDefinition = (GrTypeDefinition) element;
      checkTypeDefinition(holder, typeDefinition);
      checkTypeDefinitionModifiers(holder, typeDefinition);
      final GrTypeDefinitionBody body = typeDefinition.getBody();
      if (body != null) checkDuplicateMethod(body.getGroovyMethods(), holder);
      checkImplementedMethodsOfClass(holder, typeDefinition);
    } else if (element instanceof GrMethod) {
      final GrMethod method = (GrMethod) element;
      checkMethodDefinitionModifiers(holder, method);
      checkInnerMethod(holder, method);
      checkMethodReturnExpression(holder, method);
      addOverrideGutter(holder, method);
    } else if (element instanceof GrVariableDeclaration) {
      checkVariableDeclaration(holder, (GrVariableDeclaration) element);
    } else if (element instanceof GrVariable) {
      if (element instanceof GrMember) highlightMember(holder, ((GrMember) element));
      checkVariable(holder, (GrVariable) element);
    } else if (element instanceof GrAssignmentExpression) {
      checkAssignmentExpression((GrAssignmentExpression) element, holder);
    } else if (element instanceof GrNamedArgument) {
      checkCommandArgument((GrNamedArgument) element, holder);
    } else if (element instanceof GrReturnStatement) {
      checkReturnStatement((GrReturnStatement) element, holder);
    } else if (element instanceof GrListOrMap) {
      checkMap(((GrListOrMap) element).getNamedArguments(), holder);
    } else if (element instanceof GrNewExpression) {
      checkNewExpression(holder, (GrNewExpression) element);
    } else if (element instanceof GrDocMemberReference) {
      checkGrDocMemberReference((GrDocMemberReference) element, holder);
    } else if (element instanceof GrConstructorInvocation) {
      checkConstructorInvocation(holder, (GrConstructorInvocation) element);
    } else if (element.getParent() instanceof GrDocReferenceElement) {
      checkGrDocReferenceElement(holder, element);
    } else if (element instanceof GrPackageDefinition) {
      //todo: if reference isn't resolved it construct package definition
      checkPackageReference(holder, (GrPackageDefinition) element);
    } else if (element instanceof GroovyFile) {
      final GroovyFile file = (GroovyFile) element;
      if (file.isScript()) {
        checkScriptDuplicateMethod(file.getTopLevelDefinitions(), holder);
      }
      if (DomainClassUtils.isDomainClassFile(element.getContainingFile().getVirtualFile())) {
        checkDomainClass((GroovyFile) element, holder);
      }
    } else {
      final ASTNode node = element.getNode();
      if (node != null && !(element instanceof PsiWhiteSpace) &&
          !GroovyTokenTypes.COMMENT_SET.contains(node.getElementType()) &&
          element.getContainingFile() instanceof GroovyFile &&
          !isDocCommentElement(element)) {
        GroovyImportsTracker.getInstance(element.getProject()).markFileAnnotated((GroovyFile) element.getContainingFile());
      }
    }
  }

  private static boolean isDocCommentElement(PsiElement element) {
    if (element == null) return false;
    ASTNode node = element.getNode();
    return node != null && PsiTreeUtil.getParentOfType(element, GrDocComment.class) != null ||
        element instanceof GrDocComment;
  }

  private static void checkGrDocReferenceElement(AnnotationHolder holder, PsiElement element) {
    ASTNode node = element.getNode();
    if (node != null && TokenSets.BUILT_IN_TYPE.contains(node.getElementType())) {
      Annotation annotation = holder.createInfoAnnotation(element, null);
      annotation.setTextAttributes(DefaultHighlighter.KEYWORD);
    }
  }

  private void checkPackageReference(AnnotationHolder holder, GrPackageDefinition packageDefinition) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(packageDefinition.getProject()).getFileIndex();
    final PsiFile file = packageDefinition.getContainingFile();
    assert file != null;

    final VirtualFile virtualFile = file.getVirtualFile();
    assert virtualFile != null;

    final VirtualFile sourceRootForFile = projectFileIndex.getSourceRootForFile(virtualFile);
    if (sourceRootForFile == null) return;
    assert virtualFile.getPath().startsWith(sourceRootForFile.getPath());

    final VirtualFile containingDirectory = virtualFile.getParent();
    assert containingDirectory != null;

    final String packageName = VfsUtil.getRelativePath(containingDirectory, sourceRootForFile, '.');

    if (!packageDefinition.getPackageName().equals(packageName)) {
      final Annotation annotation = holder.createWarningAnnotation(packageDefinition, "wrong package name");
      annotation.registerFix(new ChangePackageQuickFix((GroovyFile)packageDefinition.getContainingFile(), packageName));
    }
  }

  private void checkMethodReturnExpression(AnnotationHolder holder, GrMethod method) {
    final GrOpenBlock block = method.getBlock();
    if (block != null) {
      final GrStatement[] statements = block.getStatements();
      if (statements.length > 0) {
        final GrStatement lastStatement = statements[statements.length - 1];
        if (lastStatement instanceof GrExpression) {
          final PsiType methodType = method.getReturnType();
          final PsiType returnType = ((GrExpression) lastStatement).getType();
          if (returnType != null && methodType != null && !PsiType.VOID.equals(methodType)) {
            checkAssignability(holder, methodType, returnType, lastStatement);
          }
        }
      }
    }
  }

  private void checkImplementedMethodsOfClass(AnnotationHolder holder, GrTypeDefinition typeDefinition) {
    if (typeDefinition.hasModifierProperty(PsiModifier.ABSTRACT)) return;
    if (typeDefinition.isEnum() || typeDefinition.isAnnotationType()) return;

    final Collection<CandidateInfo> methodsToImplement = OverrideImplementUtil.getMethodsToOverrideImplement(typeDefinition, true);

    if (methodsToImplement.isEmpty()) return;

    final PsiElement methodCandidateInfo = methodsToImplement.toArray(CandidateInfo.EMPTY_ARRAY)[0].getElement();
    assert methodCandidateInfo instanceof PsiMethod;

    String notImplementedMethodName = ((PsiMethod) methodCandidateInfo).getName();

    final int startOffset = typeDefinition.getTextRange().getStartOffset();
    final GrTypeDefinitionBody body = typeDefinition.getBody();
    int endOffset = body != null ? body.getTextRange().getStartOffset() : typeDefinition.getTextRange().getEndOffset();
    final Annotation annotation = holder.createErrorAnnotation(new TextRange(startOffset, endOffset), GroovyBundle.message("method.is.not.implemented", notImplementedMethodName));
    registerImplementsMethodsFix(typeDefinition, annotation);
  }

  private void registerImplementsMethodsFix(GrTypeDefinition typeDefinition, Annotation annotation) {
    annotation.registerFix(new ImplementMethodsQuickFix(typeDefinition));
  }

  private void addOverrideGutter(AnnotationHolder holder, GrMethod method) {
    final Annotation annotation = holder.createInfoAnnotation(method, null);

    final PsiMethod[] superMethods = method.findSuperMethods();
    if (superMethods.length > 0) {
      boolean isImplements = !method.hasModifierProperty(PsiModifier.ABSTRACT) && superMethods[0].hasModifierProperty(PsiModifier.ABSTRACT);
      annotation.setGutterIconRenderer(new OverrideGutter(superMethods, isImplements));
    }
  }

  private void checkConstructorInvocation(AnnotationHolder holder, GrConstructorInvocation invocation) {
    final GroovyResolveResult resolveResult = invocation.resolveConstructorGenerics();
    if (resolveResult != null) {
      checkMethodApplicability(resolveResult, invocation.getThisOrSuperKeyword(), holder);
    } else {
      final GroovyResolveResult[] results = invocation.multiResolveConstructor();
      final GrArgumentList argList = invocation.getArgumentList();
      if (results.length > 0) {
        String message = GroovyBundle.message("ambiguous.constructor.call");
        holder.createWarningAnnotation(argList, message);
      } else {
        final PsiClass clazz = invocation.getDelegatedClass();
        if (clazz != null) {
          //default constructor invocation
          PsiType[] argumentTypes = PsiUtil.getArgumentTypes(invocation.getThisOrSuperKeyword(), true);
          if (argumentTypes != null && argumentTypes.length > 0) {
            String message = GroovyBundle.message("cannot.find.default.constructor", clazz.getName());
            holder.createWarningAnnotation(argList, message);
          }
        }
      }
    }
  }

  private void checkInnerMethod(AnnotationHolder holder, GrMethod grMethod) {
    if (grMethod.getParent() instanceof GrOpenBlock)
      holder.createErrorAnnotation(grMethod, GroovyBundle.message("Inner.methods.are.not.supported"));
  }

  private void checkDomainClass(GroovyFile file, AnnotationHolder holder) {
    DomainClassAnnotator domainClassAnnotator = new DomainClassAnnotator();
    domainClassAnnotator.annotate(file, holder);
  }

  private void checkMap(GrNamedArgument[] namedArguments, AnnotationHolder holder) {
    final Map<GrNamedArgument, List<GrNamedArgument>> map = DuplicatesUtil.factorDuplicates(namedArguments, new TObjectHashingStrategy<GrNamedArgument>() {
      public int computeHashCode(GrNamedArgument arg) {
        final GrArgumentLabel label = arg.getLabel();
        if (label == null) return 0;
        return label.getName().hashCode();
      }

      public boolean equals(GrNamedArgument arg1, GrNamedArgument arg2) {
        final GrArgumentLabel label1 = arg1.getLabel();
        final GrArgumentLabel label2 = arg2.getLabel();
        if (label1 == null || label2 == null) {
          return label1 == null && label2 == null;
        }

        return label1.getName().equals(label2.getName());
      }
    });

    processDuplicates(map, holder);
  }

  protected void processDuplicates(Map<GrNamedArgument, List<GrNamedArgument>> map, AnnotationHolder holder) {
    for (List<GrNamedArgument> args : map.values()) {
      for (int i = 1; i < args.size(); i++) {
        GrNamedArgument namedArgument = args.get(i);
        holder.createErrorAnnotation(namedArgument, GroovyBundle.message("duplicate.element.in.the.map"));
      }
    }
  }

  private void checkVariableDeclaration(AnnotationHolder holder, GrVariableDeclaration variableDeclaration) {

    PsiElement parent = variableDeclaration.getParent();
    assert parent != null;

    PsiElement typeDef = parent.getParent();
    if (typeDef != null && typeDef instanceof GrTypeDefinition) {
      PsiModifierList modifiersList = variableDeclaration.getModifierListGroovy();
      checkAccessModifiers(holder, modifiersList);

      if (modifiersList.hasExplicitModifier(PsiModifier.VOLATILE)
          && modifiersList.hasExplicitModifier(PsiModifier.FINAL)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("illegal.combination.of.modifiers.volatile.and.final"));
      }

      if (modifiersList.hasExplicitModifier(PsiModifier.NATIVE)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("variable.cannot.be.native"));
      }

      if (modifiersList.hasExplicitModifier(PsiModifier.ABSTRACT)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("variable.cannot.be.abstract"));
      }
    }
  }

  private void checkMethodDefinitionModifiers(AnnotationHolder holder, GrMethod grMethod) {
    final PsiModifierList modifiersList = grMethod.getModifierList();
    checkAccessModifiers(holder, modifiersList);

    //script methods
    boolean isMethodAbstract = modifiersList.hasExplicitModifier(PsiModifier.ABSTRACT);
    if (grMethod.getParent() instanceof GroovyFileBase) {
      if (isMethodAbstract) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("script.cannot.have.modifier.abstract"));
      }

      if (modifiersList.hasExplicitModifier(PsiModifier.NATIVE)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("script.cannot.have.modifier.native"));
      }
    } else  //type definition methods
      if (grMethod.getParent() != null && grMethod.getParent().getParent() instanceof GrTypeDefinition) {
        GrTypeDefinition containingTypeDef = ((GrTypeDefinition) grMethod.getParent().getParent());

        //interface
        if (containingTypeDef.isInterface()) {
          if (modifiersList.hasExplicitModifier(PsiModifier.STATIC)) {
            holder.createErrorAnnotation(modifiersList, GroovyBundle.message("interface.must.have.no.static.method"));
          }

          if (modifiersList.hasExplicitModifier(PsiModifier.PRIVATE)) {
            holder.createErrorAnnotation(modifiersList, GroovyBundle.message("interface.must.have.no.private.method"));
          }

        } else if (containingTypeDef.isEnum()) {
          //enumeration
          //todo
        } else if (containingTypeDef.isAnnotationType()) {
          //annotation
          //todo
        } else {
          //class
          PsiModifierList typeDefModifiersList = containingTypeDef.getModifierList();
          LOG.assertTrue(typeDefModifiersList != null, "modifiers list must be not null");

          if (!typeDefModifiersList.hasExplicitModifier(PsiModifier.ABSTRACT)) {
            if (isMethodAbstract) {
              holder.createErrorAnnotation(modifiersList, GroovyBundle.message("not.abstract.class.cannot.have.abstract.method"));
            }
          }

          if (!isMethodAbstract) {
            if (grMethod.getBlock() == null) {
              holder.createErrorAnnotation(grMethod.getNameIdentifierGroovy(), GroovyBundle.message("not.abstract.method.should.have.body"));
            }
          }

        }
      }
  }

  private void checkTypeDefinitionModifiers(AnnotationHolder holder, GrTypeDefinition typeDefinition) {
    PsiModifierList modifiersList = typeDefinition.getModifierList();

    if (modifiersList == null) return;

    /**** class ****/
    checkAccessModifiers(holder, modifiersList);

    PsiClassType[] extendsListTypes = typeDefinition.getExtendsListTypes();

    for (PsiClassType classType : extendsListTypes) {
      PsiClass psiClass = classType.resolve();

      if (psiClass != null) {
        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList != null) {
          if (modifierList.hasExplicitModifier(PsiModifier.FINAL)) {
            holder.createErrorAnnotation(typeDefinition.getNameIdentifierGroovy(), GroovyBundle.message("final.class.cannot.be.extended"));
          }
        }
      }
    }

    if (modifiersList.hasExplicitModifier(PsiModifier.ABSTRACT)
        && modifiersList.hasExplicitModifier(PsiModifier.FINAL)) {
      holder.createErrorAnnotation(modifiersList, GroovyBundle.message("illegal.combination.of.modifiers.abstract.and.final"));
    }

    if (modifiersList.hasExplicitModifier(PsiModifier.TRANSIENT)) {
      holder.createErrorAnnotation(modifiersList, GroovyBundle.message("modifier.transient.not.allowed.here"));
    }
    if (modifiersList.hasExplicitModifier(PsiModifier.VOLATILE)) {
      holder.createErrorAnnotation(modifiersList, GroovyBundle.message("modifier.volatile.not.allowed.here"));
    }

    /**** interface ****/
    if (typeDefinition.isInterface()) {
      if (modifiersList.hasExplicitModifier(PsiModifier.FINAL)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("intarface.cannot.have.modifier.final"));
      }

      if (modifiersList.hasExplicitModifier(PsiModifier.VOLATILE)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("modifier.volatile.not.allowed.here"));
      }

      if (modifiersList.hasExplicitModifier(PsiModifier.TRANSIENT)) {
        holder.createErrorAnnotation(modifiersList, GroovyBundle.message("modifier.transient.not.allowed.here"));
      }
    }
  }

  private void checkAccessModifiers(AnnotationHolder holder, @NotNull PsiModifierList modifierList) {
    boolean hasPrivate = modifierList.hasExplicitModifier(PsiModifier.PRIVATE);
    boolean hasPublic = modifierList.hasExplicitModifier(PsiModifier.PUBLIC);
    boolean hasProtected = modifierList.hasExplicitModifier(PsiModifier.PROTECTED);

    if (hasPrivate && hasPublic
        || hasPrivate && hasProtected
        || hasPublic && hasProtected) {
      holder.createErrorAnnotation(modifierList, GroovyBundle.message("illegal.combination.of.modifiers"));
    }
  }

  private void checkScriptDuplicateMethod(GrTopLevelDefintion[] topLevelDefinitions, AnnotationHolder holder) {
    List<GrMethod> methods = new ArrayList<GrMethod>();

    for (GrTopLevelDefintion topLevelDefinition : topLevelDefinitions) {
      if (topLevelDefinition instanceof GrMethod) {
        methods.add(((GrMethod) topLevelDefinition));
      }
    }

    checkDuplicateMethod(methods.toArray(new GrMethod[methods.size()]), holder);
  }

  private void checkDuplicateMethod(GrMethod[] methods, AnnotationHolder holder) {
    final Map<GrMethod, List<GrMethod>> map = DuplicatesUtil.factorDuplicates(methods, new TObjectHashingStrategy<GrMethod>() {
      public int computeHashCode(GrMethod method) {
        return method.getSignature(PsiSubstitutor.EMPTY).hashCode();
      }

      public boolean equals(GrMethod method1, GrMethod method2) {
        return method1.getSignature(PsiSubstitutor.EMPTY).equals(method2.getSignature(PsiSubstitutor.EMPTY));
      }
    });
    processMethodDuplicates(map, holder);
  }

  protected void processMethodDuplicates(Map<GrMethod, List<GrMethod>> map, AnnotationHolder holder) {
    HashSet<GrMethod> duplicateMethodsWarning = new HashSet<GrMethod>();
    HashSet<GrMethod> duplicateMethodsErrors = new HashSet<GrMethod>();

    DuplicatesUtil.collectMethodDuplicates(map, duplicateMethodsWarning, duplicateMethodsErrors);

    for (GrMethod duplicateMethod : duplicateMethodsErrors) {
      holder.createErrorAnnotation(duplicateMethod.getNameIdentifierGroovy(), GroovyBundle.message("repetitive.method.name.signature.and.return.type"));
    }

    for (GrMethod duplicateMethod : duplicateMethodsWarning) {
      holder.createWarningAnnotation(duplicateMethod.getNameIdentifierGroovy(), GroovyBundle.message("repetitive.method.name.signature"));
    }
  }


  private void checkReturnStatement(GrReturnStatement returnStatement, AnnotationHolder holder) {
    final GrExpression value = returnStatement.getReturnValue();
    if (value != null) {
      final PsiType type = value.getType();
      if (type != null) {
        final GrParametersOwner owner = PsiTreeUtil.getParentOfType(returnStatement, GrMethod.class, GrClosableBlock.class);
        if (owner instanceof PsiMethod) {
          final PsiMethod method = (PsiMethod) owner;
          if (method.isConstructor()) {
            holder.createErrorAnnotation(value, GroovyBundle.message("cannot.return.from.constructor"));
          } else {
            final PsiType methodType = method.getReturnType();
            final PsiType returnType = value.getType();
            if (methodType != null) {
              if (PsiType.VOID.equals(methodType)) {
                holder.createErrorAnnotation(value, GroovyBundle.message("cannot.return.from.void.method"));
              } else if (returnType != null) {
                checkAssignability(holder, methodType, returnType, value);
              }
            }
          }
        }
      }
    }
  }

  private void checkCommandArgument(GrNamedArgument namedArgument, AnnotationHolder holder) {
    final GrArgumentLabel label = namedArgument.getLabel();
    if (label != null) {
      PsiType expectedType = label.getExpectedArgumentType();
      if (expectedType != null) {
        expectedType = TypeConversionUtil.erasure(expectedType);
        final GrExpression expr = namedArgument.getExpression();
        if (expr != null) {
          final PsiType argType = expr.getType();
          if (argType != null) {
            final PsiClassType listType = namedArgument.getManager().getElementFactory().createTypeByFQClassName("java.util.List", namedArgument.getResolveScope());
            if (listType.isAssignableFrom(argType)) return; //this is constructor arguments list
            checkAssignability(holder, expectedType, argType, namedArgument);
          }
        }
      }
    }
  }

  private void checkAssignmentExpression(GrAssignmentExpression assignment, AnnotationHolder holder) {
    IElementType opToken = assignment.getOperationToken();
    if (opToken == GroovyTokenTypes.mASSIGN) {
      GrExpression lValue = assignment.getLValue();
      GrExpression rValue = assignment.getRValue();
      if (rValue != null) {
        PsiType lType = lValue.getNominalType();
        PsiType rType = rValue.getType();
        if (lType != null && rType != null) {
          checkAssignability(holder, lType, rType, rValue);
        }
      }
    }
  }

  private void checkVariable(AnnotationHolder holder, GrVariable variable) {
    final GroovyPsiElement duplicate = ResolveUtil.resolveProperty(variable, variable.getName());
    if (duplicate instanceof GrVariable) {
      if (duplicate instanceof GrField && !(variable instanceof GrField)) {
        holder.createWarningAnnotation(variable.getNameIdentifierGroovy(), GroovyBundle.message("field.already.defined", variable.getName()));
      } else {
        final String key = duplicate instanceof GrField ? "field.already.defined" : "variable.already.defined";
        holder.createErrorAnnotation(variable.getNameIdentifierGroovy(), GroovyBundle.message(key, variable.getName()));
      }
    }

    PsiType varType = variable.getType();
    GrExpression initializer = variable.getInitializerGroovy();
    if (initializer != null) {
      PsiType rType = initializer.getType();
      if (rType != null) {
        checkAssignability(holder, varType, rType, initializer);
      }
    }
  }

  private void checkAssignability(AnnotationHolder holder, @NotNull PsiType lType, @NotNull PsiType rType, GroovyPsiElement element) {
    if (!TypesUtil.isAssignable(lType, rType, element.getManager(), element.getResolveScope())) {
      holder.createWarningAnnotation(element, GroovyBundle.message("cannot.assign", rType.getInternalCanonicalText(), lType.getInternalCanonicalText()));
    }
  }

  private void checkTypeDefinition(AnnotationHolder holder, GrTypeDefinition typeDefinition) {
    if (typeDefinition.isAnnotationType()) {
      Annotation annotation = holder.createInfoAnnotation(typeDefinition.getNameIdentifierGroovy(), null);
      annotation.setTextAttributes(DefaultHighlighter.ANNOTATION);
    }

    if (typeDefinition.getParent() instanceof GrTypeDefinitionBody) {
      holder.createErrorAnnotation(typeDefinition.getNameIdentifierGroovy(), "Inner classes are not supported in Groovy");
    }

    final GrImplementsClause implementsClause = typeDefinition.getImplementsClause();
    final GrExtendsClause extendsClause = typeDefinition.getExtendsClause();

    if (implementsClause != null) {
      checkForImplementingClass(holder, extendsClause, implementsClause, ((GrTypeDefinition) implementsClause.getParent()));
    }

    if (extendsClause != null) {
      checkForExtendingInterface(holder, extendsClause, implementsClause, ((GrTypeDefinition) extendsClause.getParent()));
    }

    checkDuplicateClass(typeDefinition, holder);
  }

  private void checkDuplicateClass(GrTypeDefinition typeDefinition, AnnotationHolder holder) {
    final String qName = typeDefinition.getQualifiedName();
    if (qName != null) {
      final PsiClass[] classes = typeDefinition.getManager().findClasses(qName, typeDefinition.getResolveScope());
      if (classes.length > 1) {
        final PsiFile file = typeDefinition.getContainingFile();
        String packageName = "<default package>";
        if (file instanceof GroovyFile) {
          final String name = ((GroovyFile) file).getPackageName();
          if (name.length() > 0) packageName = name;
        }

        if (!isScriptGeneratedClass(classes)) {
          holder.createErrorAnnotation(typeDefinition.getNameIdentifierGroovy(), GroovyBundle.message("duplicate.class", typeDefinition.getName(), packageName));
        } else {
          holder.createErrorAnnotation(typeDefinition.getNameIdentifierGroovy(), GroovyBundle.message("script.generated.with.same.name", qName));
        }
      }
    }
  }

  private boolean isScriptGeneratedClass(PsiClass[] allClasses) {
    return allClasses.length == 2 &&
        (allClasses[0] instanceof GroovyScriptClass || allClasses[1] instanceof GroovyScriptClass);
  }

  private void checkForExtendingInterface(AnnotationHolder holder, GrExtendsClause extendsClause, GrImplementsClause implementsClause, GrTypeDefinition myClass) {
    for (GrCodeReferenceElement ref : extendsClause.getReferenceElements()) {
      final PsiElement clazz = ref.resolve();
      if (clazz == null) continue;

      if (myClass.isInterface() && clazz instanceof PsiClass && !((PsiClass) clazz).isInterface()) {
        final Annotation annotation = holder.createErrorAnnotation(ref, GroovyBundle.message("class.is.not.expected.here"));
        annotation.registerFix(new ChangeExtendsImplementsQuickFix(extendsClause, implementsClause));
      }
    }
  }

  private void checkForImplementingClass(AnnotationHolder holder, GrExtendsClause extendsClause, GrImplementsClause implementsClause, GrTypeDefinition myClass) {
    if (myClass.isInterface()) {
      final Annotation annotation = holder.createErrorAnnotation(implementsClause, GroovyBundle.message("interface.cannot.contain.implements.clause"));
      annotation.registerFix(new ChangeExtendsImplementsQuickFix(extendsClause, implementsClause));
      return;
    }

    for (GrCodeReferenceElement ref : implementsClause.getReferenceElements()) {
      final PsiElement clazz = ref.resolve();
      if (clazz == null) continue;

      if (!((PsiClass) clazz).isInterface()) {
        final Annotation annotation = holder.createErrorAnnotation(ref, GroovyBundle.message("interface.expected.here"));
        annotation.registerFix(new ChangeExtendsImplementsQuickFix(extendsClause, implementsClause));
      }
    }
  }

  private static void checkGrDocMemberReference(final GrDocMemberReference reference, AnnotationHolder holder){
    PsiElement resolved = reference.resolve();
    if (resolved == null) {
      Annotation annotation = holder.createErrorAnnotation(reference, GroovyBundle.message("cannot.resolve", reference.getReferenceName()));
      annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
    }
  }

  private void checkReferenceExpression(AnnotationHolder holder, final GrReferenceExpression refExpr) {
    GroovyResolveResult resolveResult = refExpr.advancedResolve();
    registerUsedImport(refExpr, resolveResult);
    PsiElement resolved = resolveResult.getElement();
    if (resolved != null) {
      if (resolved instanceof PsiMember) {
        highlightMemberResolved(holder, refExpr, ((PsiMember) resolved));
      }
      if (!resolveResult.isAccessible()) {
        String message = GroovyBundle.message("cannot.access", refExpr.getReferenceName());
        holder.createWarningAnnotation(refExpr.getReferenceNameElement(), message);
      }
      if (!resolveResult.isStaticsOK() && resolved instanceof PsiModifierListOwner) {
        final String key = ((PsiModifierListOwner) resolved).hasModifierProperty(PsiModifier.STATIC) ?
            "cannot.reference.static" :
            "cannot.reference.nonstatic";
        String message = GroovyBundle.message(key, refExpr.getReferenceName());
        holder.createWarningAnnotation(refExpr, message);
      } else if (refExpr.getParent() instanceof GrCall) {
        if (resolved instanceof PsiMethod && resolved.getUserData(GrMethod.BUILDER_METHOD) == null) {
          checkMethodApplicability(resolveResult, refExpr, holder);
        } else {
          checkClosureApplicability(resolveResult, refExpr, holder);
        }
      }
      if (isAssignmentLHS(refExpr) || resolved instanceof PsiPackage) return;
    } else {
      GrExpression qualifier = refExpr.getQualifierExpression();
      if (qualifier == null) {
        if (isAssignmentLHS(refExpr)) return;

        GroovyPsiElement context = PsiTreeUtil.getParentOfType(refExpr, GrMethod.class, GrField.class, GrClosableBlock.class);
        if (context instanceof PsiModifierListOwner && ((PsiModifierListOwner) context).hasModifierProperty(PsiModifier.STATIC)) {
          Annotation annotation = holder.createErrorAnnotation(refExpr, GroovyBundle.message("cannot.resolve", refExpr.getReferenceName()));
          annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
          registerReferenceFixes(refExpr, annotation);
          return;
        }
      }
    }

    final PsiType refExprType = refExpr.getType();
    PsiElement refNameElement = refExpr.getReferenceNameElement();
    PsiElement elt = refNameElement == null ? refExpr : refNameElement;
    Annotation annotation = holder.createInfoAnnotation(elt, null);

    if (refExprType == null) {
      if (resolved == null && refExpr.getQualifierExpression() == null) {
        if (!(refExpr.getParent() instanceof GrCallExpression)) {
          registerCreateClassByTypeFix(refExpr, annotation, false);
        }
        registerAddImportFixes(refExpr, annotation);
      }

      registerReferenceFixes(refExpr, annotation);

      annotation.setTextAttributes(DefaultHighlighter.UNTYPED_ACCESS);
    } else if (refExprType instanceof PsiClassType && ((PsiClassType) refExprType).resolve() == null) {
      annotation.setTextAttributes(DefaultHighlighter.UNTYPED_ACCESS);
    }
  }

  private void registerReferenceFixes(GrReferenceExpression refExpr, Annotation annotation) {
    PsiClass targetClass = QuickfixUtil.findTargetClass(refExpr);
    if (targetClass == null) return;

    final DItemElement virtualElement = getDynamicAnnotation(refExpr, targetClass);

    if (virtualElement != null && refExpr.resolve() == null) {
      addDynamicAnnotation(annotation, refExpr, virtualElement);
    }
    if (targetClass instanceof GrMemberOwner) {
      if (!(targetClass instanceof GroovyScriptClass)) {
        annotation.registerFix(new CreateFieldFromUsageFix(refExpr, (GrMemberOwner) targetClass));
      }

      if (refExpr.getParent() instanceof GrCallExpression) {
        annotation.registerFix(new CreateMethodFromUsageFix(refExpr, (GrMemberOwner) targetClass));
      }
    }

    if (!refExpr.isQualified()) {
      GrVariableDeclarationOwner owner = PsiTreeUtil.getParentOfType(refExpr, GrVariableDeclarationOwner.class);
      if (!(owner instanceof GroovyFileBase) || ((GroovyFileBase) owner).isScript()) {
        annotation.registerFix(new CreateLocalVariableFromUsageFix(refExpr, owner));
      }
    }
  }

  private DItemElement getDynamicAnnotation(@NotNull GrReferenceExpression referenceExpression, final @NotNull PsiClass targetClass) {
    if (targetClass instanceof GrTypeDefinition &&
        PsiUtil.isInStaticContext(referenceExpression, (GrMemberOwner) targetClass)) return null;

    final PsiFile containingFile = referenceExpression.getContainingFile();

    VirtualFile file;
    if (containingFile != null) {
      file = containingFile.getVirtualFile();
      if (file == null) return null;
    } else return null;

    Module module = ProjectRootManager.getInstance(referenceExpression.getProject()).getFileIndex().getModuleForFile(file);

    if (module == null) return null;

    final String qualifiedName = targetClass.getQualifiedName();
    if (qualifiedName == null) return null;

    if (referenceExpression.getParent() instanceof GrMethodCallExpression) {
      final DMethodElement methodVirtualElement = getDynamicMethodElement(referenceExpression, targetClass, qualifiedName);
      if (methodVirtualElement != null) return methodVirtualElement;

    } else {
      final DPropertyElement propertyVirtualElement = getDynamicPropertyElement(referenceExpression, targetClass, module, qualifiedName);
      if (propertyVirtualElement != null) return propertyVirtualElement;
    }

    return null;
  }

  private DMethodElement getDynamicMethodElement(GrReferenceExpression referenceExpression, PsiClass targetClass, String qualifiedName) {
    final PsiElement parent = referenceExpression.getParent();
    if (!(parent instanceof GrMethodCallExpression)) return null;

    GrExpression[] expressionArgument = ((GrMethodCallExpression) parent).getExpressionArguments();
    List<MyPair> pairs = new ArrayList<MyPair>();

    for (GrExpression expression : expressionArgument) {
      final PsiType type = expression.getType();
      if (type == null) return null;

      pairs.add(new MyPair(GroovyElementPresentation.getExpressionPresentableText(expression), type.getCanonicalText()));
    }

    final String[] types = QuickfixUtil.getArgumentsTypes(pairs);
    final Project project = referenceExpression.getProject();

    //TODO:add targetClass
//    classes.add(targetClass);
    DElement superDynamicMethod;
    for (PsiClass clazz : PsiUtil.iterateSupers(targetClass, true)) {
      superDynamicMethod = DynamicManager.getInstance(project).findConcreteDynamicMethod(clazz.getQualifiedName(), referenceExpression.getName(), types);

      if (superDynamicMethod != null) return null;
    }
//    DynamicManager.getInstance(project).findConcreteDynamicMethod(targetClass.getQualifiedName(), referenceExpression.getName(), QuickfixUtil.getArgumentsTypes(pairs));
    return new DMethodElement(referenceExpression.getName(), null, pairs);
  }

  private DPropertyElement getDynamicPropertyElement(GrReferenceExpression referenceExpression, PsiClass targetClass, Module module, String qualifiedName) {
    final Project project = referenceExpression.getProject();

    DElement superDynamicProperty;
    for (PsiClass aSuper : PsiUtil.iterateSupers(targetClass, true)) {
      superDynamicProperty = DynamicManager.getInstance(project).findConcreteDynamicProperty(aSuper.getQualifiedName(), referenceExpression.getName());

      if (superDynamicProperty != null) return null;
    }

    return new DPropertyElement(referenceExpression.getName(),  null);
  }

  private void addDynamicAnnotation(Annotation annotation, GrReferenceExpression referenceExpression, final DItemElement itemElement) {
    final PsiFile containingFile = referenceExpression.getContainingFile();
    VirtualFile file;
    if (containingFile != null) {
      file = containingFile.getVirtualFile();
      if (file == null) return;
    } else return;

    annotation.registerFix(new DynamicFix(itemElement, referenceExpression), referenceExpression.getTextRange());
  }

  private void highlightMemberResolved(AnnotationHolder holder, GrReferenceExpression refExpr, PsiMember member) {
    boolean isStatic = member.hasModifierProperty(PsiModifier.STATIC);
    Annotation annotation = holder.createInfoAnnotation(refExpr.getReferenceNameElement(), null);
    if (member instanceof GrAccessorMethod) member = ((GrAccessorMethod) member).getProperty();

    if (member instanceof PsiField && isStatic) {
      annotation.setTextAttributes(DefaultHighlighter.STATIC_FIELD);
      return;
    }
    if (member instanceof PsiMethod) {
      annotation.setTextAttributes(!isStatic ?
          DefaultHighlighter.METHOD_CALL :
          DefaultHighlighter.STATIC_METHOD_ACCESS
      );
    }
  }


  private void registerUsedImport(GrReferenceElement referenceElement, GroovyResolveResult resolveResult) {
    GroovyPsiElement context = resolveResult.getCurrentFileResolveContext();
    if (context instanceof GrImportStatement) {
      PsiFile file = referenceElement.getContainingFile();
      if (file instanceof GroovyFile) {
        GroovyImportsTracker importsTracker = GroovyImportsTracker.getInstance(referenceElement.getProject());
        importsTracker.registerImportUsed((GrImportStatement) context);
      }
    }
  }

  private void checkMethodApplicability(GroovyResolveResult methodResolveResult, PsiElement place, AnnotationHolder holder) {
    final PsiElement element = methodResolveResult.getElement();
    if (!(element instanceof PsiMethod)) return;
    final PsiMethod method = (PsiMethod) element;
    PsiType[] argumentTypes = PsiUtil.getArgumentTypes(place, method.isConstructor());
    if (argumentTypes != null && !PsiUtil.isApplicable(argumentTypes, method, methodResolveResult.getSubstitutor(), methodResolveResult.getCurrentFileResolveContext() instanceof GrMethodCallExpression)) {
      PsiElement elementToHighlight = PsiUtil.getArgumentsElement(place);
      if (elementToHighlight == null) {
        elementToHighlight = place;
      }

      final String typesString = buildArgTypesList(argumentTypes);
      String message;
      final PsiClass containingClass = method.getContainingClass();
      if (containingClass != null) {
        final PsiClassType containingType = method.getManager().getElementFactory().createType(containingClass, methodResolveResult.getSubstitutor());
        message = GroovyBundle.message("cannot.apply.method1", method.getName(), containingType.getInternalCanonicalText(), typesString);
      } else {
        message = GroovyBundle.message("cannot.apply.method.or.closure", method.getName(), typesString);
      }
      holder.createWarningAnnotation(elementToHighlight, message);
    }
  }

  private boolean isAssignmentLHS(GrReferenceExpression refExpr) {
    return refExpr.getParent() instanceof GrAssignmentExpression &&
        refExpr.equals(((GrAssignmentExpression) refExpr.getParent()).getLValue());
  }

  private void checkReferenceElement(AnnotationHolder holder, final GrCodeReferenceElement refElement) {
    final PsiElement parent = refElement.getParent();
    GroovyResolveResult resolveResult = refElement.advancedResolve();
    highlightAnnotation(holder, refElement, resolveResult);
    registerUsedImport(refElement, resolveResult);
    highlightAnnotation(holder, refElement, resolveResult);
    if (refElement.getReferenceName() != null) {

      if (parent instanceof GrImportStatement &&
          ((GrImportStatement) parent).isStatic() &&
          refElement.multiResolve(false).length > 0) {
        return;
      }

      checkSingleResolvedElement(holder, refElement, resolveResult);
    }

  }

  private void checkSingleResolvedElement(AnnotationHolder holder, GrCodeReferenceElement refElement, GroovyResolveResult resolveResult) {
    final PsiElement resolved = resolveResult.getElement();
    if (resolved == null) {
      String message = GroovyBundle.message("cannot.resolve", refElement.getReferenceName());

      // Register quickfix
      final PsiElement nameElement = refElement.getReferenceNameElement();
      final PsiElement toHighlight = nameElement != null ? nameElement : refElement;
      final Annotation annotation = holder.createErrorAnnotation(toHighlight, message);
      // todo implement for nested classes
      if (refElement.getQualifier() == null) {
        registerCreateClassByTypeFix(refElement, annotation, false);
        registerAddImportFixes(refElement, annotation);
      }
      annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
    } else if (!resolveResult.isAccessible()) {
      String message = GroovyBundle.message("cannot.access", refElement.getReferenceName());
      holder.createWarningAnnotation(refElement.getReferenceNameElement(), message);
    }
  }

  private void checkNewExpression(AnnotationHolder holder, GrNewExpression newExpression) {
    if (newExpression.getArrayCount() > 0) return;
    GrCodeReferenceElement refElement = newExpression.getReferenceElement();
    if (refElement == null) return;
    final PsiElement element = refElement.resolve();
    if (element instanceof PsiClass) {
      PsiClass clazz = (PsiClass) element;
      if (clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
        String message = clazz.isInterface() ? GroovyBundle.message("cannot.instantiate.interface", clazz.getName()) :
            GroovyBundle.message("cannot.instantiate.abstract.class", clazz.getName());
        holder.createErrorAnnotation(refElement, message);
        return;
      }
    }

    final GroovyResolveResult constructorResolveResult = newExpression.resolveConstructorGenerics();
    if (constructorResolveResult != null) {
      checkMethodApplicability(constructorResolveResult, refElement, holder);
    } else {
      final GroovyResolveResult[] results = newExpression.multiResolveConstructor();
      final GrArgumentList argList = newExpression.getArgumentList();
      PsiElement toHighlight = argList != null ? argList : refElement.getReferenceNameElement();

      if (results.length > 0) {
        String message = GroovyBundle.message("ambiguous.constructor.call");
        holder.createWarningAnnotation(toHighlight, message);
      } else {
        if (element instanceof PsiClass) {
          //default constructor invocation
          PsiType[] argumentTypes = PsiUtil.getArgumentTypes(refElement, true);
          if (argumentTypes != null && argumentTypes.length > 0) {
            String message = GroovyBundle.message("cannot.find.default.constructor", ((PsiClass) element).getName());
            holder.createWarningAnnotation(toHighlight, message);
          }
        }
      }
    }
  }

  private void checkClosureApplicability(GroovyResolveResult resolveResult, PsiElement place, AnnotationHolder holder) {
    final PsiElement element = resolveResult.getElement();
    if (!(element instanceof GrVariable)) return;
    final GrVariable variable = (GrVariable) element;
    final PsiType type = variable.getTypeGroovy();
    if (!(type instanceof GrClosureType)) return;
    PsiParameter[] parameters = ((GrClosureType) type).getClosureParameters();
    PsiType[] argumentTypes = PsiUtil.getArgumentTypes(place, false);
    if (argumentTypes == null) return;

    final PsiType[] paramTypes = PsiUtil.skipOptionalParametersAndSubstitute(argumentTypes.length, parameters, PsiSubstitutor.EMPTY);
    if (!areTypesCompatibleForCallingClosure(argumentTypes, paramTypes, place.getManager(), place.getResolveScope())) {
      final String typesString = buildArgTypesList(argumentTypes);
      String message = GroovyBundle.message("cannot.apply.method.or.closure", variable.getName(), typesString);
      PsiElement elementToHighlight = PsiUtil.getArgumentsElement(place);
      if (elementToHighlight == null) elementToHighlight = place;
      holder.createWarningAnnotation(elementToHighlight, message);
    }
  }

  private boolean areTypesCompatibleForCallingClosure(PsiType[] argumentTypes, PsiType[] paramTypes, PsiManager manager, GlobalSearchScope resolveScope) {
    if (argumentTypes.length != paramTypes.length) return false;
    for (int i = 0; i < argumentTypes.length; i++) {
      final PsiType paramType = TypesUtil.boxPrimitiveType(paramTypes[i], manager, resolveScope);
      final PsiType argType = argumentTypes[i];
      if (!paramType.isAssignableFrom(argType)) return false;
    }
    return true;
  }

  private void registerAddImportFixes(GrReferenceElement refElement, Annotation annotation) {
    final IntentionAction[] actions = OuterImportsActionCreator.getOuterImportFixes(refElement, refElement.getProject());
    for (IntentionAction action : actions) {
      annotation.registerFix(action);
    }
  }

  private void registerCreateClassByTypeFix(GrReferenceElement refElement, Annotation annotation, boolean createConstructor) {
    if (PsiTreeUtil.getParentOfType(refElement, GrPackageDefinition.class) == null) {
      annotation.registerFix(CreateClassFix.createClassFixAction(refElement, createConstructor));
    }
  }

  private void highlightMember
      (AnnotationHolder
          holder, GrMember
          member) {
    if (member instanceof PsiField) {
      GrField field = (GrField) member;
      PsiElement identifier = field.getNameIdentifierGroovy();
      if (field.hasModifierProperty(PsiModifier.STATIC)) {
        Annotation annotation = holder.createInfoAnnotation(identifier, null);
        annotation.setTextAttributes(DefaultHighlighter.STATIC_FIELD);
      }
    }
  }

  private void highlightAnnotation(AnnotationHolder holder, PsiElement refElement, GroovyResolveResult result) {
    PsiElement element = result.getElement();
    PsiElement parent = refElement.getParent();
    if (element instanceof PsiClass &&
        ((PsiClass) element).isAnnotationType() &&
        !(parent instanceof GrImportStatement)) {
      Annotation annotation = holder.createInfoAnnotation(parent, null);
      annotation.setTextAttributes(DefaultHighlighter.ANNOTATION);
      GroovyPsiElement context = result.getCurrentFileResolveContext();
      if (context instanceof GrImportStatement) {
        annotation = holder.createInfoAnnotation(((GrImportStatement) context).getImportReference(), null);
        annotation.setTextAttributes(DefaultHighlighter.ANNOTATION);
      }
    }

  }


  private static String buildArgTypesList(PsiType[] argTypes) {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    for (int i = 0; i < argTypes.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      PsiType argType = argTypes[i];
      builder.append(argType != null ? argType.getInternalCanonicalText() : "?");
    }
    builder.append(")");
    return builder.toString();
  }
}
