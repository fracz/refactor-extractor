/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
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
package com.intellij.codeInspection.reference;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Application component which provides utility methods for working with the reference
 * graph.
 *
 * @author anna
 * @since 6.0
 */
public abstract class RefUtil implements ApplicationComponent {
  public static RefUtil getInstance(){
    return ApplicationManager.getApplication().getComponent(RefUtil.class);
  }

  public abstract boolean belongsToScope(PsiElement psiElement, RefManager refManager);

  public abstract RefPackage getPackage(RefEntity refEntity);

  public abstract RefClass getTopLevelClass(RefElement refElement);

  public abstract boolean isInheritor(RefClass subClass, RefClass superClass);

  public abstract String getPackageName(RefEntity refEntity);

  public abstract String getQualifiedName(RefEntity refEntity);

  public abstract String getAccessModifier(PsiModifierListOwner psiElement);

  public abstract @Nullable RefClass getOwnerClass(RefManager refManager, PsiElement psiElement);

  public abstract @Nullable RefClass getOwnerClass(RefElement refElement);

  public abstract int compareAccess(String a1, String a2);

  public abstract void removeRefElement(RefElement refElement, List<RefElement> deletedRefs);

  public abstract void setAccessModifier(RefElement refElement, String newAccess);

  public abstract void setIsStatic(RefElement refElement, boolean isStatic);

  public abstract void setIsFinal(RefElement refElement, boolean isFinal);

  public abstract boolean isMethodOnlyCallsSuper(final PsiMethod derivedMethod);
}