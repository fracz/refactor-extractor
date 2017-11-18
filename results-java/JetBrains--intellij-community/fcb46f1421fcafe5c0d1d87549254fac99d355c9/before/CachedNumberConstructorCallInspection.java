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
package com.siyeh.ig.numeric;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.HashSet;

public class CachedNumberConstructorCallInspection
        extends ExpressionInspection {

    private static final Set<String> cachedNumberTypes = new HashSet<String>();
    static {
        cachedNumberTypes.add("java.lang.Long");
        cachedNumberTypes.add("java.lang.Byte");
        cachedNumberTypes.add("java.lang.Integer");
        cachedNumberTypes.add("java.lang.Short");
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("cached.number.constructor.call.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.NUMERIC_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("cached.number.constructor.call.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new LongConstructorVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        final PsiNewExpression expression = (PsiNewExpression) location;
        final PsiJavaCodeReferenceElement classReference =
                expression.getClassReference();
        assert classReference != null;
        final String className = classReference.getText();
        return new CachedNumberConstructorCallFix(className);
    }

    private static class CachedNumberConstructorCallFix
            extends InspectionGadgetsFix {

        private final String className;

        CachedNumberConstructorCallFix(String className) {
            this.className = className;
        }

        public String getName() {
            return InspectionGadgetsBundle.message("cached.number.constructor.call.quickfix", className);
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiNewExpression expression =
                    (PsiNewExpression) descriptor.getPsiElement();
            final PsiExpressionList argList = expression.getArgumentList();
            assert argList != null;
            final PsiExpression[] args = argList.getExpressions();
            final PsiExpression arg = args[0];
            final String text = arg.getText();
          replaceExpression(expression, className + ".valueOf(" + text + ')');
        }
    }

    private static class LongConstructorVisitor
            extends BaseInspectionVisitor {

        public void visitNewExpression(@NotNull PsiNewExpression expression) {
            final PsiManager manager = expression.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if (languageLevel.compareTo(LanguageLevel.JDK_1_5) < 0) {
                return;
            }
            super.visitNewExpression(expression);
            final PsiType type = expression.getType();
            if (type == null) {
                return;
            }
            final String canonicalText = type.getCanonicalText();
            if (!cachedNumberTypes.contains(canonicalText)) {
                return;
            }
            final PsiClass aClass = ClassUtils.getContainingClass(expression);
            if(aClass!=null &&
                    cachedNumberTypes.contains(aClass.getQualifiedName())){
                return;
            }
            final PsiExpressionList argumentList = expression.getArgumentList();
            if (argumentList == null) {
                return;
            }
            final PsiExpression[] arguments = argumentList.getExpressions();
            if (arguments.length != 1) {
                return;
            }
            final PsiExpression argument = arguments[0];
            final PsiType argumentType = argument.getType();
            if (argumentType == null ||
                    argumentType.equalsToText("java.lang.String")) {
                return;
            }
            registerError(expression);
        }
    }
}