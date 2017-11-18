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
package com.siyeh.ig.security;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.FieldInspection;
import com.siyeh.ig.psiutils.CollectionUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PublicStaticArrayFieldInspection extends FieldInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "public.static.array.field.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.SECURITY_GROUP_NAME;
    }

    @Nullable
    protected String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "public.static.array.field.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new PublicStaticArrayFieldVisitor();
    }

    private static class PublicStaticArrayFieldVisitor
            extends BaseInspectionVisitor {

        public void visitField(@NotNull PsiField field) {
            super.visitField(field);
            if (!field.hasModifierProperty(PsiModifier.PUBLIC)) {
                return;
            }
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            final PsiType type = field.getType();
            if (!(type instanceof PsiArrayType)) {
                return;
            }
            if (CollectionUtils.isConstantArrayOfZeroSize(field)) {
                return;
            }
            registerFieldError(field);
        }
    }
}