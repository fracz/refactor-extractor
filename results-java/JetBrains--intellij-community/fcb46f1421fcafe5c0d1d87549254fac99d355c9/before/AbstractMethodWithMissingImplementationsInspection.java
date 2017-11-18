/*
 * Copyright 2003-2005 Dave Griffith
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
package com.siyeh.ig.classlayout;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.MethodInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbstractMethodWithMissingImplementationsInspection
        extends MethodInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "abstract.method.with.missing.implementations.display.name");
    }

  public String getGroupDisplayName() {
    return GroupNames.INHERITANCE_GROUP_NAME;
  }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "abstract.method.with.missing.implementations.problem.descriptor");
    }

  public BaseInspectionVisitor buildVisitor() {
    return new AbstactMethodWithMissingImplementationsVisitor();
  }

  private static class AbstactMethodWithMissingImplementationsVisitor
    extends BaseInspectionVisitor {

    public void visitMethod(PsiMethod method) {
      super.visitMethod(method);
      final PsiClass containingClass = method.getContainingClass();
      if (containingClass == null) {
        return;
      }
      if (!containingClass.isInterface() &&
          !method.hasModifierProperty(PsiModifier.ABSTRACT)) {
        return;
      }
            final InheritorFinder inheritorFinder =
                    new InheritorFinder(containingClass);
            final PsiClass[] inheritors = inheritorFinder.getInheritors();
      for (final PsiClass inheritor : inheritors) {
        if (!inheritor.isInterface() &&
            !inheritor.hasModifierProperty(PsiModifier.ABSTRACT)) {
          if (!hasMatchingImplementation(inheritor, method)) {
            registerMethodError(method);
            return;
          }
        }
      }
    }

        private static boolean hasMatchingImplementation(
                @NotNull PsiClass aClass,
                @NotNull PsiMethod method) {
            final PsiMethod overridingMethod =
                    findOverridingMethod(aClass, method);
            if (overridingMethod == null ||
                overridingMethod.hasModifierProperty(PsiModifier.STATIC)) {
                return false;
            }
            if (!method.hasModifierProperty(PsiModifier.PACKAGE_LOCAL)) {
          return true;
        }
            final PsiClass superClass = method.getContainingClass();
            final PsiManager manager = overridingMethod.getManager();
            return manager.arePackagesTheSame(superClass, aClass);
      }

        @Nullable
        private static PsiMethod findOverridingMethod(
                PsiClass aClass, @NotNull PsiMethod method) {
            final PsiClass superClass = method.getContainingClass();
            if (aClass.equals(superClass)) {
                return null;
            }
            final PsiSubstitutor substitutor =
                    TypeConversionUtil.getSuperClassSubstitutor(superClass,
                            aClass, PsiSubstitutor.EMPTY);
            final MethodSignature signature = method.getSignature(substitutor);
            final List<Pair<PsiMethod, PsiSubstitutor>> pairs =
                    aClass.findMethodsAndTheirSubstitutorsByName(
                            signature.getName(), true);
            for (Pair<PsiMethod, PsiSubstitutor> pair : pairs) {
                final PsiMethod overridingMethod = pair.first;
                final PsiSubstitutor overridingSubstitutor = pair.second;
                final MethodSignature foundMethodSignature =
                        overridingMethod.getSignature(overridingSubstitutor);
                if (MethodSignatureUtil.isSubsignature(signature,
                        foundMethodSignature) && overridingMethod != method) {
                    return overridingMethod;
                }
            }
            return null;
        }
    }

    private static class InheritorFinder implements Runnable {

        private final PsiClass aClass;
        PsiClass[] inheritors = null;

        InheritorFinder(PsiClass aClass) {
            this.aClass = aClass;
        }

        public void run() {
            final PsiManager manager = aClass.getManager();
            final PsiSearchHelper searchHelper = manager.getSearchHelper();
            final SearchScope searchScope = aClass.getUseScope();
            inheritors = searchHelper.findInheritors(aClass,
                    searchScope, true);
        }

        public PsiClass[] getInheritors() {
            final ProgressManager progressManager =
                    ProgressManager.getInstance();
            // do not display progress
            progressManager.runProcess(this, null);
            return inheritors;
    }
  }
}