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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.fixes.RemoveModifierFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class FinalMethodInspection extends MethodInspection {

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "final.method.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new FinalMethodVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return new RemoveModifierFix(location);
    }

    private static class FinalMethodVisitor extends BaseInspectionVisitor {
        public void visitMethod(@NotNull PsiMethod method) {
            //no call to super, so we don't drill into anonymous classes
            if (!method.hasModifierProperty(PsiModifier.FINAL)) {
                return;
            }
            registerModifierError(PsiModifier.FINAL, method);
        }
    }
}