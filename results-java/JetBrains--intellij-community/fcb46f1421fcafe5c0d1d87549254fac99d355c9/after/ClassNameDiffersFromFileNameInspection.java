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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassNameDiffersFromFileNameInspection extends ClassInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "class.name.differs.from.file.name.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "class.name.differs.from.file.name.problem.descriptor");
    }

    @Nullable
    protected InspectionGadgetsFix buildFix(PsiElement location) {
        final PsiClass aClass = (PsiClass)location.getParent();
        final PsiJavaFile file = (PsiJavaFile)aClass.getParent();
        final String fileName = file.getName();
        if (fileName == null) {
            return null;
        }
        final int prefixIndex = fileName.indexOf((int)'.');
        final String filenameWithoutPrefix = fileName.substring(0, prefixIndex);
        final PsiClass[] classes = file.getClasses();
        for (PsiClass psiClass : classes) {
            final String className = psiClass.getName();
            if (filenameWithoutPrefix.equals(className)) {
                return null;
            }
        }
        return new RenameFix(filenameWithoutPrefix);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ClassNameDiffersFromFileNameVisitor();
    }

    private static class ClassNameDiffersFromFileNameVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so that it doesn't drill down to inner classes
            final PsiElement parent = aClass.getParent();
            if (!(parent instanceof PsiJavaFile)) {
                return;
            }
            final PsiJavaFile file = (PsiJavaFile)parent;
            final String className = aClass.getName();
            if (className == null) {
                return;
            }
            final String fileName = file.getName();
            if (fileName == null) {
                return;
            }
            final int prefixIndex = fileName.indexOf((int)'.');
            if (prefixIndex < 0) {
                return;
            }
            final String filenameWithoutPrefix =
                    fileName.substring(0, prefixIndex);
            if (className.equals(filenameWithoutPrefix)) {
                return;
            }
            registerClassError(aClass);
        }
    }
}