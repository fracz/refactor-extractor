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
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import org.jetbrains.annotations.NotNull;

public class ExtendsAnnotationInspection extends ClassInspection {

    public String getID() {
        return "ClassExplicitlyAnnotation";
    }

    public String getGroupDisplayName() {
        return GroupNames.INHERITANCE_GROUP_NAME;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final PsiClass containingClass = (PsiClass)infos[0];
        return InspectionGadgetsBundle.message(
                "extends.annotation.problem.descriptor",
                containingClass.getName());
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ExtendsAnnotationVisitor();
    }

    private static class ExtendsAnnotationVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            final PsiManager manager = aClass.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if (languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0) {
                return;
            }
            if (aClass.isAnnotationType()) {
                return;
            }
            final PsiReferenceList extendsList = aClass.getExtendsList();
            checkReferenceList(extendsList, aClass);
            final PsiReferenceList implementsList = aClass.getImplementsList();
            checkReferenceList(implementsList, aClass);
        }

        private void checkReferenceList(PsiReferenceList referenceList,
                                        PsiClass containingClass) {
            if (referenceList == null) {
                return;
            }
            final PsiJavaCodeReferenceElement[] elements =
                    referenceList.getReferenceElements();
            for (final PsiJavaCodeReferenceElement element : elements) {
                final PsiElement referent = element.resolve();
                if (referent instanceof PsiClass) {
                    final PsiClass psiClass = (PsiClass) referent;
                    psiClass.isAnnotationType();
                    if(psiClass.isAnnotationType()){
                        registerError(element, containingClass);
                    }
                }
            }
        }
    }
}