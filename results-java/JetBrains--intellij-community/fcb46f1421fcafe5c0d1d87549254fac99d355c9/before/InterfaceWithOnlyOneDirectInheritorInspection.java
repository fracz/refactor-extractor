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
package com.siyeh.ig.j2me;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.openapi.progress.ProgressManager;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class InterfaceWithOnlyOneDirectInheritorInspection
        extends ClassInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "interface.one.inheritor.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.J2ME_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message(
                "interface.one.inheritor.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new InterfaceWithOnlyOneDirectInheritorVisitor();
    }

    private static class InterfaceWithOnlyOneDirectInheritorVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so that it doesn't drill down to inner classes
            if (!aClass.isInterface() || aClass.isAnnotationType()) {
                return;
            }
            if (!hasOneInheritor(aClass)) {
                return;
            }
            registerClassError(aClass);
        }

        private static boolean hasOneInheritor(final PsiClass aClass) {
            final PsiManager manager = aClass.getManager();
            final PsiSearchHelper searchHelper = manager.getSearchHelper();
            final SearchScope searchScope = aClass.getUseScope();
            final PsiElementProcessor.CollectElementsWithLimit<PsiClass> processor =
                    new PsiElementProcessor.CollectElementsWithLimit<PsiClass>(2);
            final ProgressManager instance = ProgressManager.getInstance();
            instance.runProcess(new Runnable() {
                public void run() {
                    searchHelper.processInheritors(processor, aClass,
                            searchScope, false);
                }
            }, null);
            final Collection<PsiClass> collection = processor.getCollection();
            return collection.size() == 1;
        }
    }
}