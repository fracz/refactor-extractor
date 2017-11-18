/*
 *  Copyright 2000-2007 JetBrains s.r.o.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.members;

import com.intellij.lang.ASTNode;
import com.intellij.pom.java.PomMethod;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.*;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.*;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspDeclarationHolder;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrThrowsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameterList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.types.*;
import org.jetbrains.plugins.groovy.lang.psi.impl.*;
import org.jetbrains.plugins.groovy.lang.psi.impl.auxiliary.modifiers.GrModifierListImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.params.GrParameterListImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.JavaIdentifier;
import org.jetbrains.plugins.groovy.lang.resolve.MethodTypeInferencer;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.*;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 26.03.2007
 */

public class GrMethodDefinitionImpl extends GroovyPsiElementImpl implements GrMethod {
  public GrMethodDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitMethod(this);
  }

  public int getTextOffset() {
    return getNameIdentifierGroovy().getTextRange().getStartOffset();
  }

  public PsiElement getNameIdentifierGroovy() {
    return findChildByType(GroovyElementTypes.mIDENT);
  }

  public String toString() {
    return "Method";
  }

  @Nullable
  public GrOpenBlock getBlock() {
    return this.findChildByClass(GrOpenBlock.class);
  }

  public GrParameter[] getParameters() {
    GrParameterListImpl parameterList = findChildByClass(GrParameterListImpl.class);
    if (parameterList != null) {
      return parameterList.getParameters();
    }

    return GrParameter.EMPTY_ARRAY;
  }

  public GrTypeElement getReturnTypeElementGroovy() {
    return findChildByClass(GrTypeElement.class);
  }

  @Nullable
  public PsiType getDeclaredReturnType() {
    final GrTypeElement typeElement = getReturnTypeElementGroovy();
    if (typeElement != null) return typeElement.getType();
    return null;
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull PsiSubstitutor substitutor, PsiElement lastParent, @NotNull PsiElement place) {
    for (final GrTypeParameter typeParameter : getTypeParameters()) {
      if (!ResolveUtil.processElement(processor, typeParameter)) return false;
    }

    for (final GrParameter parameter : getParameters()) {
      if (!ResolveUtil.processElement(processor, parameter)) return false;
    }

    return true;
  }

  public GrMember[] getMembers() {
    return new GrMember[]{this};
  }

  private static class MyTypeCalculator implements Function<GrMethod, PsiType> {

    public PsiType fun(GrMethod method) {
      GrTypeElement element = method.getReturnTypeElementGroovy();
      if (element == null) {
        final GrOpenBlock block = method.getBlock();
        if (block == null) return null;
        return GroovyPsiManager.getInstance(method.getProject()).inferType(method, new MethodTypeInferencer(block));
      }
      return element.getType();
    }
  }

  private static MyTypeCalculator ourTypesCalculator = new MyTypeCalculator();

  //PsiMethod implementation
  @Nullable
  public PsiType getReturnType() {
    return GroovyPsiManager.getInstance(getProject()).getType(this, ourTypesCalculator);
  }

  @Nullable
  public PsiTypeElement getReturnTypeElement() {
    return null;
  }

  @NotNull
  public PsiParameterList getParameterList() {
    return findChildByClass(GrParameterList.class);
  }

  @NotNull
  public PsiReferenceList getThrowsList() {
    return findChildByClass(GrThrowsClause.class);
  }

  @Nullable
  public PsiCodeBlock getBody() {
    return null;
  }

  public boolean isConstructor() {
    return false;
  }

  public boolean isVarArgs() {
    GrParameter[] parameters = getParameters();
    return parameters.length > 0 && parameters[parameters.length - 1].isVarArgs();
  }

  @NotNull
  public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }

  @Nullable
  public PsiIdentifier getNameIdentifier() {
    return new JavaIdentifier(getManager(), getContainingFile(), getNameIdentifierGroovy().getTextRange());
  }

  private void findSuperMethodRecursilvely(Set<PsiMethod> methods, PsiClass psiClass, boolean allowStatic,
                                           Set<PsiClass> visited, MethodSignature signature, @NotNull Set<MethodSignature> discoveredSupers) {
    if (visited.contains(psiClass)) return;
    visited.add(psiClass);
    PsiClassType[] superClassTypes = psiClass.getSuperTypes();

    for (PsiClassType superClassType : superClassTypes) {
      PsiClass resolvedSuperClass = superClassType.resolve();

      if (resolvedSuperClass == null) continue;
      PsiMethod[] superClassMethods = resolvedSuperClass.getMethods();
      final HashSet<MethodSignature> supers = new HashSet<MethodSignature>(3);

      for (PsiMethod superClassMethod : superClassMethods) {
        MethodSignature superMethodSignature = createMethodSignature(superClassMethod);

        if (PsiImplUtil.isExtendsSignature(superMethodSignature, signature) && !dominated(superMethodSignature, discoveredSupers)) {
          if (allowStatic || !superClassMethod.getModifierList().hasExplicitModifier(PsiModifier.STATIC)) {
            methods.add(superClassMethod);
            supers.add(superMethodSignature);
            discoveredSupers.add(superMethodSignature);
          }
        }
      }

      findSuperMethodRecursilvely(methods, resolvedSuperClass, allowStatic, visited, signature, discoveredSupers);
      discoveredSupers.removeAll(supers);
    }
  }

  private boolean dominated(MethodSignature signature, Iterable<MethodSignature> supersInInheritor) {
    for (MethodSignature sig1 : supersInInheritor) {
      if (PsiImplUtil.isExtendsSignature(signature, sig1)) return true;
    }
    return false;
  }

  @NotNull
  public PsiMethod[] findDeepestSuperMethods() {
        List<PsiMethod> methods = new ArrayList<PsiMethod>();
        findDeepestSuperMethodsForClass(methods, this);
        return methods.toArray(PsiMethod.EMPTY_ARRAY);
    }

    private void findDeepestSuperMethodsForClass(List<PsiMethod> collectedMethods, PsiMethod method) {
        PsiClassType[] superClassTypes = method.getContainingClass().getSuperTypes();

        for (PsiClassType superClassType : superClassTypes) {
            PsiClass resolvedSuperClass = superClassType.resolve();

            if (resolvedSuperClass == null) continue;
            PsiMethod[] superClassMethods = resolvedSuperClass.getMethods();

            for (PsiMethod superClassMethod : superClassMethods) {
                MethodSignature superMethodSignature = superClassMethod.getHierarchicalMethodSignature();
                final HierarchicalMethodSignature thisMethodSignature = getHierarchicalMethodSignature();

                if (superMethodSignature.equals(thisMethodSignature) && !superClassMethod.getModifierList().hasExplicitModifier(PsiModifier.STATIC)) {
                    checkForMethodOverriding(collectedMethods, superClassMethod);
                }
                findDeepestSuperMethodsForClass(collectedMethods, superClassMethod);
            }
        }
    }

    private void checkForMethodOverriding(List<PsiMethod> collectedMethods, PsiMethod superClassMethod) {
        int i = 0;
        while (i < collectedMethods.size()) {
            PsiMethod collectedMethod = collectedMethods.get(i);
            if (collectedMethod.getContainingClass().equals(superClassMethod.getContainingClass()) || collectedMethod.getContainingClass().isInheritor(superClassMethod.getContainingClass(), true)) {
                collectedMethods.remove(collectedMethod);
                continue;
            }
            i++;
        }
        collectedMethods.add(superClassMethod);
    }

    @NotNull
    public PsiMethod[] findSuperMethods(boolean checkAccess) {
        PsiClass containingClass = getContainingClass();

    Set<PsiMethod> methods = new HashSet<PsiMethod>();
    findSuperMethodRecursilvely(methods, containingClass, false, new HashSet<PsiClass>(), createMethodSignature(this), new HashSet<MethodSignature>());

    return methods.toArray(PsiMethod.EMPTY_ARRAY);
  }

  @NotNull
  public PsiMethod[] findSuperMethods(PsiClass parentClass) {
    Set<PsiMethod> methods = new HashSet<PsiMethod>();
    findSuperMethodRecursilvely(methods, parentClass, false, new HashSet<PsiClass>(), createMethodSignature(this), new HashSet<MethodSignature>());
    return methods.toArray(PsiMethod.EMPTY_ARRAY);
  }

  @NotNull
  public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    PsiClass containingClass = getContainingClass();

    Set<PsiMethod> methods = new HashSet<PsiMethod>();
    final MethodSignature signature = createMethodSignature(this);
    findSuperMethodRecursilvely(methods, containingClass, true, new HashSet<PsiClass>(), signature, new HashSet<MethodSignature>());

    List<MethodSignatureBackedByPsiMethod> result = new ArrayList<MethodSignatureBackedByPsiMethod>();
    for (PsiMethod method : methods) {
      result.add(method.getHierarchicalMethodSignature());
    }

    return result;
  }

  private static MethodSignature createMethodSignature(PsiMethod method) {
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    PsiType[] types = new PsiType[parameters.length];
    for (int i = 0; i < types.length; i++) {
      types[i] = parameters[i].getType();
    }
    return MethodSignatureUtil.createMethodSignature(method.getName(), types, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY);
  }

  @NotNull
  public PsiMethod[] findSuperMethods() {
    PsiClass containingClass = getContainingClass();
    if (containingClass == null) return PsiMethod.EMPTY_ARRAY;

    Set<PsiMethod> methods = new HashSet<PsiMethod>();
    findSuperMethodRecursilvely(methods, containingClass, false, new HashSet<PsiClass>(), createMethodSignature(this), new HashSet<MethodSignature>());

    return methods.toArray(PsiMethod.EMPTY_ARRAY);
  }

  /*
  * @deprecated use {@link #findDeepestSuperMethods()} instead
  */

  @Nullable
  public PsiMethod findDeepestSuperMethod() {
    return null;
  }

  public PomMethod getPom() {
    return null;
  }

  @NotNull
  public PsiModifierList getModifierList() {
    return findChildByClass(GrModifierListImpl.class);
  }

  public boolean hasModifierProperty(@NonNls @NotNull String name) {
    if (name.equals(PsiModifier.ABSTRACT)) {
      final PsiClass containingClass = getContainingClass();
      if (containingClass != null && containingClass.isInterface()) return true;
    }

    return getModifierList().hasModifierProperty(name);
  }

  @NotNull
  public String getName() {
    PsiElement nameElement = getNameIdentifierGroovy();
    if (nameElement == null) {
      nameElement = findChildByType(GroovyTokenTypes.mSTRING_LITERAL);
    }
    if (nameElement == null) {
      nameElement = findChildByType(GroovyTokenTypes.mGSTRING_LITERAL);
    }

    assert nameElement != null;
    return nameElement.getText();
  }

  @NotNull
  public HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiImplUtil.setName(name, getNameIdentifierGroovy());

    return this;
  }

  public boolean hasTypeParameters() {
    return getTypeParameters().length > 0;
  }

  @Nullable
  public GrTypeParameterList getTypeParameterList() {
    return findChildByClass(GrTypeParameterList.class);
  }

  @NotNull
  public GrTypeParameter[] getTypeParameters() {
    final GrTypeParameterList list = getTypeParameterList();
    if (list != null) {
      return list.getTypeParameters();
    }

    return GrTypeParameter.EMPTY_ARRAY;
  }

  public PsiClass getContainingClass() {
    PsiElement parent = getParent();
    if (parent instanceof GrTypeDefinitionBody) {
      final PsiElement pparent = parent.getParent();
      if (pparent instanceof PsiClass) {
        return (PsiClass) pparent;
      }
    }


    final PsiFile file = getContainingFile();
    if (file instanceof GroovyFileBase) {
      return ((GroovyFileBase) file).getScriptClass();
    }

    return null;
  }

  @Nullable
  public PsiDocComment getDocComment() {
    return null;
  }

  public boolean isDeprecated() {
    return false;
  }

  @NotNull
  public SearchScope getUseScope() {
    return PsiImplUtil.getUseScope(this);
  }

  public PsiElement getOriginalElement() {
    final PsiClass containingClass = getContainingClass();
    if (containingClass == null) return this;
    PsiClass originalClass = (PsiClass) containingClass.getOriginalElement();
    final PsiMethod originalMethod = originalClass.findMethodBySignature(this, false);
    return originalMethod != null ? originalMethod : this;
  }
}