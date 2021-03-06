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
package com.siyeh.ig.cloneable;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.psiutils.CloneUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class CloneCallsSuperCloneInspection extends MethodInspection {

    public String getID() {
        return "CloneDoesntCallSuperClone";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "clone.doesnt.call.super.clone.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLONEABLE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
      return InspectionGadgetsBundle.message(
              "clone.doesnt.call.super.clone.problem.descriptor");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new NoExplicitCloneCallsVisitor();
    }

    private static class NoExplicitCloneCallsVisitor
            extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            //note: no call to super;
          if (!CloneUtils.isClone(method)) {
              return;
            }
            if(method.hasModifierProperty(PsiModifier.ABSTRACT) ||
                    method.hasModifierProperty(PsiModifier.NATIVE)) {
                return;
            }
            final PsiClass containingClass = method.getContainingClass();
            if(containingClass == null) {
                return;
            }
            if (containingClass.isInterface() ||
                containingClass.isAnnotationType()) {
                return;
            }
            if (CloneUtils.onlyThrowsCloneNotSupportedException(method)) {
                if (method.hasModifierProperty(PsiModifier.FINAL) ||
                        containingClass.hasModifierProperty(
                                PsiModifier.FINAL)) {
                    return;
                }
            }
            final CallToSuperCloneVisitor visitor =
                    new CallToSuperCloneVisitor();
            method.accept(visitor);
            if (visitor.isCallToSuperCloneFound()) {
                return;
            }
            registerMethodError(method);
        }
    }
}