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
package com.siyeh.ig.naming;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.HardcodedMethodConstants;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;

public class ConfusingMainMethodInspection extends MethodInspection {

    public String getGroupDisplayName() {
        return GroupNames.NAMING_CONVENTIONS_GROUP_NAME;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "confusing.main.method.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new RenameFix();
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ConfusingMainMethodVisitor();
    }

    private static class ConfusingMainMethodVisitor
            extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod aMethod) {
            // no call to super, so it doesn't drill down into inner classes
            final String methodName = aMethod.getName();
            if (!HardcodedMethodConstants.MAIN.equals(methodName)) {
                return;
            }
            if (!aMethod.hasModifierProperty(PsiModifier.PUBLIC)) {
                registerMethodError(aMethod);
                return;
            }
            if (!aMethod.hasModifierProperty(PsiModifier.STATIC)) {
                registerMethodError(aMethod);
                return;
            }
            final PsiType returnType = aMethod.getReturnType();

            if (!TypeUtils.typeEquals(PsiKeyword.VOID, returnType)) {
                registerMethodError(aMethod);
                return;
            }
            final PsiParameterList paramList = aMethod.getParameterList();
            final PsiParameter[] parameters = paramList.getParameters();
            if (parameters.length != 1) {
                registerMethodError(aMethod);
                return;
            }
            final PsiType paramType = parameters[0].getType();
            if (!TypeUtils.typeEquals("java.lang.String" + "[]", paramType)) {
                registerMethodError(aMethod);
            }
        }
    }
}