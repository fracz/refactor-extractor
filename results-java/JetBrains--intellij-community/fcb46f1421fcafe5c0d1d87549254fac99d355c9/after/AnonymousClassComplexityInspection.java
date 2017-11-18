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
package com.siyeh.ig.classmetrics;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.MoveAnonymousToInnerClassFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class AnonymousClassComplexityInspection
        extends ClassMetricInspection {

    private static final int DEFAULT_COMPLEXITY_LIMIT = 3;

    public String getID() {
        return "OverlyComplexAnonymousInnerClass";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "overly.complex.anonymous.inner.class.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLASSMETRICS_GROUP_NAME;
    }

    protected int getDefaultLimit() {
        return DEFAULT_COMPLEXITY_LIMIT;
    }

    protected String getConfigurationLabel() {
        return InspectionGadgetsBundle.message(
                "cyclomatic.complexity.limit.option");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new MoveAnonymousToInnerClassFix();
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final Integer totalComplexity = (Integer)infos[0];
        return InspectionGadgetsBundle.message(
                "overly.complex.anonymous.inner.class.problem.descriptor",
                totalComplexity);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ClassComplexityVisitor();
    }

    private class ClassComplexityVisitor extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass psiClass) {
            // no call to super, to prevent double counting
        }

        public void visitAnonymousClass(@NotNull PsiAnonymousClass aClass) {
            final int totalComplexity = calculateTotalComplexity(aClass);
            if (totalComplexity <= getLimit()) {
                return;
            }
            registerClassError(aClass, Integer.valueOf(totalComplexity));
        }

        private int calculateTotalComplexity(PsiClass aClass) {
            if (aClass == null) {
                return 0;
            }
            final PsiMethod[] methods = aClass.getMethods();
            int totalComplexity = calculateComplexityForMethods(methods);
            totalComplexity += calculateInitializerComplexity(aClass);
            return totalComplexity;
        }

        private int calculateInitializerComplexity(PsiClass aClass) {
            final ComplexityVisitor visitor = new ComplexityVisitor();
            int complexity = 0;
            final PsiClassInitializer[] initializers = aClass.getInitializers();
            for(final PsiClassInitializer initializer : initializers){
                visitor.reset();
                initializer.accept(visitor);
                complexity += visitor.getComplexity();
            }
            return complexity;
        }

        private int calculateComplexityForMethods(PsiMethod[] methods) {
            final ComplexityVisitor visitor = new ComplexityVisitor();
            int complexity = 0;
            for(final PsiMethod method : methods){
                visitor.reset();
                method.accept(visitor);
                complexity += visitor.getComplexity();
            }
            return complexity;
        }
    }
}