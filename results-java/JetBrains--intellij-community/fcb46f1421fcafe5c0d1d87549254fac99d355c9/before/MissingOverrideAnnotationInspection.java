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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MissingOverrideAnnotationInspection extends MethodInspection {

    public String getID() {
        return "override";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "missing.override.annotation.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    @Nullable
    protected String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "missing.override.annotation.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new MissingOverrideAnnotationFix();
    }

    private static class MissingOverrideAnnotationFix
            extends InspectionGadgetsFix {

        public String getName() {
            return InspectionGadgetsBundle.message(
                    "missing.override.annotation.add.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiElement identifier = descriptor.getPsiElement();
            final PsiModifierListOwner parent =
                    (PsiModifierListOwner)identifier.getParent();
            if (parent == null) {
                return;
            }
            final PsiManager psiManager = parent.getManager();
            final PsiElementFactory factory = psiManager.getElementFactory();
            final PsiAnnotation annotation =
                    factory.createAnnotationFromText("@java.lang.Override",
                            parent);
            final PsiModifierList modifierList =
                    parent.getModifierList();
            modifierList.addAfter(annotation, null);
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new MissingOverrideAnnotationVisitor();
    }

    private static class MissingOverrideAnnotationVisitor
            extends BaseInspectionVisitor{

        public void visitMethod(@NotNull PsiMethod method){
            if(method.isConstructor()){
                return;
            }
            if(method.hasModifierProperty(PsiModifier.PRIVATE) ||
               method.hasModifierProperty(PsiModifier.STATIC)){
                return;
            }
            final PsiManager manager = method.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if(languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0){
                return;
            }
            if(!isOverride(method)){
                return;
            }
            if(hasOverrideAnnotation(method)){
                return;
            }
            registerMethodError(method);
        }

        private static boolean hasOverrideAnnotation(
                PsiModifierListOwner element){
            final PsiModifierList modifierList = element.getModifierList();
            if(modifierList == null){
                return false;
            }
            final PsiAnnotation annotation =
                    modifierList.findAnnotation("java.lang.Override");
            return annotation != null;
        }

        private static boolean isOverride(PsiMethod method){
            final PsiMethod[] superMethods = method.findSuperMethods();
            for(PsiMethod superMethod : superMethods){
                final PsiClass containingClass =
                        superMethod.getContainingClass();
                if(containingClass == null || !containingClass.isInterface()){
                    return true;
                }
            }
            return false;
        }
    }
}