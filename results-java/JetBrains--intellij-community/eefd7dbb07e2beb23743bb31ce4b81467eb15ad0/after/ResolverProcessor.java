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

package org.jetbrains.plugins.groovy.lang.resolve.processors;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;

import java.util.*;

import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyResolveResultImpl;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;

/**
 * @author ven
 */
public class ResolverProcessor implements PsiScopeProcessor, NameHint, ClassHint {
  protected String myName;
  private EnumSet<ResolveKind> myResolveTargetKinds;
  protected PsiElement myPlace;
  protected boolean myForCompletion;

  protected Set<GroovyResolveResult> myCandidates = new HashSet<GroovyResolveResult>();

  public ResolverProcessor(String name, EnumSet<ResolveKind> resolveTargets, GroovyPsiElement place, boolean forCompletion) {
    myName = name;
    myResolveTargetKinds = resolveTargets;
    myPlace = place;
    myForCompletion = forCompletion;
  }

  public boolean execute(PsiElement element, PsiSubstitutor substitutor) {
    if (myResolveTargetKinds.contains(ResolveUtil.getResolveKind(element))) {
      PsiNamedElement namedElement = (PsiNamedElement) element;
      boolean isAccessible = isAccessible(namedElement);
      myCandidates.add(new GroovyResolveResultImpl(namedElement, isAccessible));
      return myForCompletion || !isAccessible;
    }

    return true;
  }

  protected boolean isAccessible(PsiNamedElement namedElement) {
    return !(namedElement instanceof PsiMember) || PsiUtil.isAccessible((PsiMember) namedElement, myPlace, null);
  }

  public GroovyResolveResult[] getCandidates() {
    return myCandidates.toArray(GroovyResolveResult.EMPTY_ARRAY);
  }

  public <T> T getHint(Class<T> hintClass) {
    if (NameHint.class == hintClass && myName != null){
      return (T) this;
    }
    else if (ClassHint.class == hintClass) {
      return (T) this;
    }

    return null;
  }

  public void handleEvent(Event event, Object associated) {
  }

  public String getName() {
    return myName;
  }

  public boolean shouldProcess(ResolveKind resolveKind) {
    return myResolveTargetKinds.contains(resolveKind);
  }
}