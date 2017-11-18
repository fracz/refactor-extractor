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
package com.siyeh.ig.jdk15;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.VariableInspection;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class RawUseOfParameterizedTypeInspection extends VariableInspection {

  /** @noinspection PublicField*/
  public boolean ignoreObjectConstruction = true;

    public String getGroupDisplayName() {
        return GroupNames.JDK15_SPECIFIC_GROUP_NAME;
    }

    @Nullable
    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(
          InspectionGadgetsBundle.message(
                  "raw.use.of.parameterized.type.ignore.new.objects"), this,
                "ignoreObjectConstruction");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new RawUseOfParameterizedTypeVisitor();
    }

    private class RawUseOfParameterizedTypeVisitor
            extends BaseInspectionVisitor {

        public void visitNewExpression(
                @NotNull PsiNewExpression newExpression) {
            if (!hasNeededLanguageLevel(newExpression)) {
                return;
            }
            super.visitNewExpression(newExpression);
            if (ignoreObjectConstruction) {
                return;
            }
            if (newExpression.getArrayInitializer() != null ||
                newExpression.getArrayDimensions().length > 0) {
                // array creation cannot be generic
                return;
            }
            final PsiJavaCodeReferenceElement classReference =
                    newExpression.getClassReference();
            if (classReference == null) {
                return;
            }
            final PsiType[] typeParameters = classReference.getTypeParameters();
            if (typeParameters.length > 0) {
                return;
            }
            final PsiElement referent = classReference.resolve();
            if (!(referent instanceof PsiClass)) {
                return;
            }
            final PsiClass referredClass = (PsiClass) referent;
            if (!referredClass.hasTypeParameters()) {
                return;
            }
            registerError(classReference);
        }

        public void visitTypeElement(@NotNull PsiTypeElement typeElement) {
            if (!hasNeededLanguageLevel(typeElement)) {
                return;
            }
            super.visitTypeElement(typeElement);
            final PsiElement parent = typeElement.getParent();
            if (parent instanceof PsiInstanceOfExpression ||
                    parent instanceof PsiClassObjectAccessExpression ||
                    parent instanceof PsiReferenceParameterList) {
                return;
            }
            if (PsiTreeUtil.getParentOfType(typeElement, PsiComment.class)
                    != null) {
                return;
            }
            final PsiType type = typeElement.getType();
            if(!(type instanceof PsiClassType)) {
                return;
            }
            final PsiClassType classType = (PsiClassType) type;
            if(classType.hasParameters()) {
                return;
            }
            final PsiClass aClass = classType.resolve();
            if(aClass == null) {
                return;
            }
            if(!aClass.hasTypeParameters()) {
                return;
            }
            final PsiElement typeNameElement =
                    typeElement.getInnermostComponentReferenceElement();
            registerError(typeNameElement);
        }

        //public void visitReferenceElement(
        //        @NotNull PsiJavaCodeReferenceElement reference) {
        //    super.visitReferenceElement(reference);
        //    final PsiElement parent = reference.getParent();
        //    if (!(parent instanceof PsiReferenceList)) {
        //        return;
        //    }
        //    final PsiElement element = reference.resolve();
        //    if (!(element instanceof PsiClass)) {
        //        return;
        //    }
        //    final PsiClass aClass = (PsiClass)element;
        //    if (!aClass.hasTypeParameters()) {
        //        return;
        //    }
        //    final PsiTypeParameter[] classTypeParameters =
        //            aClass.getTypeParameters();
        //    final PsiType[] referenceTypeParameters =
        //            reference.getTypeParameters();
        //    if (classTypeParameters == referenceTypeParameters) {
        //        return;
        //    }
        //    registerError(reference);
        //}

        private boolean hasNeededLanguageLevel(
                @NotNull PsiElement element) {
            final PsiManager manager = element.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            return languageLevel.compareTo(LanguageLevel.JDK_1_5) >= 0;
        }
    }
}