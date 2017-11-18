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
package com.siyeh.ig.threading;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.ReplaceInheritanceWithDelegationFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class ExtendsThreadInspection extends ClassInspection {

    public String getID() {
        return "ClassExplicitlyExtendsThread";
    }

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "extends.thread.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new ReplaceInheritanceWithDelegationFix();
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ExtendsThreadVisitor();
    }

    private static class ExtendsThreadVisitor extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            if (aClass.isInterface() || aClass.isAnnotationType() ||
                    aClass.isEnum()) {
                return;
            }
            final PsiClass superClass = aClass.getSuperClass();
            if (superClass == null) {
                return;
            }
            final String superclassName = superClass.getQualifiedName();
            if (!"java.lang.Thread".equals(superclassName)) {
                return;
            }
            registerClassError(aClass);
        }
    }
}