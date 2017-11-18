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
package com.siyeh.ig.cloneable;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.psiutils.CloneUtils;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CloneableImplementsCloneInspection extends ClassInspection {

    /** @noinspection PublicField*/
    public boolean m_ignoreCloneableDueToInheritance = false;

    public String getID() {
        return "CloneableClassWithoutClone";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("cloneable.class.without.clone.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.CLONEABLE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("cloneable.class.without.clone.problem.descriptor");
    }

    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(InspectionGadgetsBundle.message("cloneable.class.without.clone.ignore.option"),
                this, "m_ignoreCloneableDueToInheritance");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new CloneableDefinesCloneVisitor();
    }

    private class CloneableDefinesCloneVisitor extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so it doesn't drill down
            if (aClass.isInterface()  || aClass.isAnnotationType()
                    || aClass.isEnum()) {
                return;
            }
            if(aClass instanceof PsiTypeParameter ||
                    aClass instanceof PsiAnonymousClass){
                return;
            }
            if (m_ignoreCloneableDueToInheritance) {
                if (!CloneUtils.isDirectlyCloneable(aClass)) {
                    return;
                }
            } else if (!CloneUtils.isCloneable(aClass)) {
                return;
            }
            final PsiMethod[] methods = aClass.getMethods();
            for(final PsiMethod method : methods) {
                if(CloneUtils.isClone(method)) {
                    return;
                }
            }
            registerClassError(aClass);
        }
    }
}