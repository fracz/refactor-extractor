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
package com.siyeh.ig.abstraction;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiCatchSection;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiTypeElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.MethodInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class ParameterOfConcreteClassInspection extends MethodInspection {

    public String getID() {
        return "MethodParameterOfConcreteClass";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "concrete.class.method.parameter.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.ABSTRACTION_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "concrete.class.method.parameter.problem.descriptor",
                infos);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ParameterOfConcreteClassVisitor();
    }

    private static class ParameterOfConcreteClassVisitor
            extends BaseInspectionVisitor {

        public void visitParameter(@NotNull PsiParameter parameter) {
            super.visitParameter(parameter);

            if (parameter.getDeclarationScope() instanceof PsiCatchSection) {
                return;
            }
            final PsiTypeElement typeElement = parameter.getTypeElement();
            if (!ConcreteClassUtil.typeIsConcreteClass(typeElement)) {
                return;
            }
            final String variableName = parameter.getName();
            registerError(typeElement, variableName);
        }
    }
}