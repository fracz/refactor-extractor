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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMemberReference;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocReferenceElement;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocTagValueToken;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.resolve.processors.PropertyResolverProcessor;
import org.jetbrains.plugins.groovy.lang.resolve.processors.MethodResolverProcessor;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author ilyas
 */
public abstract class GrDocMemberReferenceImpl extends GroovyDocPsiElementImpl implements GrDocMemberReference {
  public GrDocMemberReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  public GrDocReferenceElement getReferenceHolder() {
    return findChildByClass(GrDocReferenceElement.class);
  }

  @NotNull
  public GrDocTagValueToken getReferenceNameElement() {
    GrDocTagValueToken token = findChildByClass(GrDocTagValueToken.class);
    assert token != null;
    return token;
  }

  public PsiElement getElement() {
    return this;
  }

  public PsiReference getReference() {
    return this;
  }

  public TextRange getRangeInElement() {
    final PsiElement refNameElement = getReferenceNameElement();
    final int offsetInParent = refNameElement.getStartOffsetInParent();
    return new TextRange(offsetInParent, offsetInParent + refNameElement.getTextLength());
  }

  public String getCanonicalText() {
    return null;
  }

  public boolean isSoft() {
    return false;
  }

  @Nullable
  public PsiElement getQualifier() {
    return getReferenceHolder();
  }

  @Nullable
  @NonNls
  public String getReferenceName() {
    return getReferenceNameElement().getText();
  }

  @Nullable
  public PsiElement resolve() {
    ResolveResult[] results = multiResolve(false);
    if (results.length == 1) {
      return results[0].getElement();
    }
    return null;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return multiResolveImpl();
  }

  public Object[] getVariants() {
    ArrayList<ResolveResult> candidates = new ArrayList<ResolveResult>();
    GrDocReferenceElement holder = getReferenceHolder();
    PsiElement resolved;
    if (holder != null) {
      GrCodeReferenceElement referenceElement = holder.getReferenceElement();
      resolved = referenceElement.resolve();
    } else {
      resolved = getEnclosingClassOrFile(this);
    }
    if (resolved != null) {
      PropertyResolverProcessor processor = new PropertyResolverProcessor(null, this, true);
      resolved.processDeclarations(processor, PsiSubstitutor.EMPTY, resolved, this);
      candidates.addAll(Arrays.asList(processor.getCandidates()));
    }
    if (holder != null && resolved != null) {
      MethodResolverProcessor processor = new MethodResolverProcessor(null, this, true, true, null, PsiType.EMPTY_ARRAY);
      resolved.processDeclarations(processor, PsiSubstitutor.EMPTY, resolved, this);
      candidates.addAll(Arrays.asList(processor.getCandidates()));
    }

    ArrayList<PsiElement> variants = new ArrayList<PsiElement>();
    for (ResolveResult candidate : candidates) {
      variants.add(candidate.getElement());
    }

    return variants.toArray(new PsiElement[variants.size()]);

  }

  protected PsiElement getEnclosingClassOrFile(PsiElement element) {
    PsiElement parent = element.getParent();
    while (!(parent instanceof PsiClass) && !(parent instanceof PsiFile) && parent != null) {
      parent = parent.getParent();
    }
    if (parent instanceof GroovyFile) {
      parent = ((GroovyFile) parent).getScriptClass();
    }
    return parent;
  }


  protected abstract ResolveResult[] multiResolveImpl();

}