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
package com.siyeh.ig.portability;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.VariableInspection;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class UseOfAWTPeerClassInspection extends VariableInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("use.of.awt.peer.class.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.PORTABILITY_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("use.of.awt.peer.class.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new UseOfAWTPeerClassVisitor();
    }

    private static class UseOfAWTPeerClassVisitor extends BaseInspectionVisitor {


        public void visitVariable(@NotNull PsiVariable variable) {
            super.visitVariable(variable);
            final PsiType type = variable.getType();
            if (type == null) {
                return;
            }

            if (!(type instanceof PsiClassType)) {
                return;
            }
            final PsiType deepComponentType = type.getDeepComponentType();
            if (deepComponentType == null) {
                return;
            }
            if(!(deepComponentType instanceof PsiClassType)) {
                return;
            }
            final PsiClass resolveClass = ((PsiClassType) deepComponentType).resolve();
            if(resolveClass == null)
            {
                return;
            }
            if(resolveClass.isEnum()||resolveClass.isInterface() || resolveClass.isAnnotationType())
            {
                return;
            }
            if(resolveClass instanceof PsiTypeParameter ||
                    resolveClass instanceof PsiAnonymousClass){
                return;
            }
            if(!ClassUtils.isSubclass(resolveClass, "java.awt.peer.ComponentPeer"))
            {
                return;
            }

            final PsiTypeElement typeElement = variable.getTypeElement();
            registerError(typeElement);
        }

        public void visitNewExpression(@NotNull PsiNewExpression newExpression) {
            super.visitNewExpression(newExpression);
            final PsiType type = newExpression.getType();
            if (type == null) {
                return;
            }
            if(!(type instanceof PsiClassType))
            {
                return;
            }
            final PsiClass resolveClass = ((PsiClassType) type).resolve();
            if(resolveClass == null) {
                return;
            }
            if(resolveClass.isEnum() || resolveClass.isInterface() ||
                    resolveClass.isAnnotationType()) {
                return;
            }
            if(resolveClass instanceof PsiTypeParameter ||
                    resolveClass instanceof PsiAnonymousClass)
            {
                return;
            }
            if(!ClassUtils.isSubclass(resolveClass, "java.awt.peer.ComponentPeer")) {
                return;
            }
            final PsiJavaCodeReferenceElement classNameElement = newExpression.getClassReference();
            registerError(classNameElement);
        }

    }

}