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
package com.siyeh.ig.classlayout;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.MoveAnonymousToInnerClassFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class AnonymousInnerClassInspection extends ClassInspection {

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "anonymous.inner.class.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new MoveAnonymousToInnerClassFix();
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new AnonymousInnerClassVisitor();
    }

    private static class AnonymousInnerClassVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            //no call to super here, to avoid double counting
        }

        public void visitAnonymousClass(@NotNull PsiAnonymousClass aClass) {
            super.visitAnonymousClass(aClass);
            registerClassError(aClass);
        }
    }
}