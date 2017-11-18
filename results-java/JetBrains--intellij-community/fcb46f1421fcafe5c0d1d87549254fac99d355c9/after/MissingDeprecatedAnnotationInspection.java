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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import org.jetbrains.annotations.NotNull;

public class MissingDeprecatedAnnotationInspection extends ClassInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "missing.deprecated.annotation.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "missing.deprecated.annotation.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new MissingDeprecatedAnnotationFix();
    }

    private static class MissingDeprecatedAnnotationFix
            extends InspectionGadgetsFix {

        public String getName() {
            return InspectionGadgetsBundle.message(
                    "missing.deprecated.annotation.add.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {

            final PsiElement identifier = descriptor.getPsiElement();
            final PsiModifierListOwner parent =
                    (PsiModifierListOwner)identifier.getParent();
            assert parent != null;
            final PsiManager psiManager = parent.getManager();
            final PsiElementFactory factory = psiManager.getElementFactory();
            final PsiAnnotation annotation =
                    factory.createAnnotationFromText("@java.lang.Deprecated",
                            parent);
            final PsiModifierList modifierList = parent.getModifierList();
            modifierList.addAfter(annotation, null);
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new MissingDeprecatedAnnotationVisitor();
    }

    private static class MissingDeprecatedAnnotationVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            super.visitClass(aClass);
            final PsiManager manager = aClass.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if (languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0) {
                return;
            }
            if (!hasDeprecatedComment(aClass)) {
                return;
            }
            if (hasDeprecatedAnnotation(aClass)) {
                return;
            }
            registerClassError(aClass);
        }

        public void visitMethod(@NotNull PsiMethod method) {
            final PsiManager manager = method.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if (languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0) {
                return;
            }
            if (!hasDeprecatedComment(method)) {
                return;
            }
            if (hasDeprecatedAnnotation(method)) {
                return;
            }
            registerMethodError(method);
        }

        public void visitField(@NotNull PsiField field) {
            final PsiManager manager = field.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if (languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0) {
                return;
            }
            if (!hasDeprecatedComment(field)) {
                return;
            }
            if (hasDeprecatedAnnotation(field)) {
                return;
            }
            registerFieldError(field);
        }

        private static boolean hasDeprecatedAnnotation(
                PsiModifierListOwner element){
            final PsiModifierList modifierList = element.getModifierList();
            if(modifierList == null){
                return false;
            }
            final PsiAnnotation annotation =
                    modifierList.findAnnotation("java.lang.Deprecated");
            return annotation != null;
        }

        private static boolean hasDeprecatedComment(
                PsiDocCommentOwner element) {
            final PsiDocComment comment = element.getDocComment();
            if (comment == null) {
                return false;
            }
            final PsiDocTag deprecatedTag = comment.findTagByName("deprecated");
            return deprecatedTag != null;
        }
    }
}