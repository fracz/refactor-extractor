/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.naming;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class MethodNamesDifferOnlyByCaseInspection extends MethodInspection {

    public String getID() {
        return "MethodNamesDifferingOnlyByCase";
    }

    public String getGroupDisplayName() {
        return GroupNames.NAMING_CONVENTIONS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "method.names.differ.only.by.case.problem.descriptor",
                infos[0]);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new OverloadedMethodsWithSameNumberOfParametersVisitor();
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new RenameFix();
    }

    private static class OverloadedMethodsWithSameNumberOfParametersVisitor
            extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            if (method.isConstructor()) {
                return;
            }
            final PsiIdentifier nameIdentifier = method.getNameIdentifier();
            if (nameIdentifier == null) {
                return;
            }
            final String methodName = method.getName();
            final PsiClass aClass = method.getContainingClass();
            if (aClass == null) {
                return;
            }
            final PsiMethod[] methods = aClass.getMethods();
            for (PsiMethod testMethod : methods) {
                final String testMethodName = testMethod.getName();
                if (!methodName.equals(testMethodName) &&
                        methodName.equalsIgnoreCase(testMethodName)) {
                    registerError(nameIdentifier, testMethodName);
                }
            }
        }
    }
}