/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.groovydoc.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMethodReference;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMethodParams;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.resolve.processors.MethodResolverProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * @author ilyas
 */
public class GrDocMethodReferenceImpl extends GrDocMemberReferenceImpl implements GrDocMethodReference {

  public GrDocMethodReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "GrDocMethodReference";
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitDocMethodReference(this);
  }

  @NotNull
  public GrDocMethodParams getParameterList() {
    GrDocMethodParams child = findChildByClass(GrDocMethodParams.class);
    assert child != null;
    return child;
  }

  protected ResolveResult[] multiResolveImpl() {
    String name = getReferenceName();
    GrDocReferenceElement holder = getReferenceHolder();
    if (holder == null) return new ResolveResult[0];

    GrCodeReferenceElement referenceElement = holder.getReferenceElement();
    PsiElement resolved = referenceElement.resolve();
    if (resolved != null) {
      PsiType[] parameterTypes = getParameterList().getParameterTypes();
      MethodResolverProcessor processor = new MethodResolverProcessor(name, this, false, false, parameterTypes, PsiType.EMPTY_ARRAY);
      resolved.processDeclarations(processor, PsiSubstitutor.EMPTY, resolved, this);
      return processor.getCandidates();
    }
    return new ResolveResult[0];
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    //todo implement me!
    return null;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    //todo implement me!
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    if (element instanceof PsiNamedElement && Comparing.equal(((PsiNamedElement) element).getName(), getReferenceName())) {
      return getManager().areElementsEquivalent(element, resolve());
    }
    return false;
  }

}