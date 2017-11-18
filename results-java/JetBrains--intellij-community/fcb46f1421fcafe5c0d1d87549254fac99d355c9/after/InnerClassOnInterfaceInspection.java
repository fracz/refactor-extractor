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
package com.siyeh.ig.classlayout;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTypeParameter;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.MoveClassFix;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class InnerClassOnInterfaceInspection extends ClassInspection {

    /**
     * @noinspection PublicField
     */
    public boolean m_ignoreInnerInterfaces = false;

    public String getID() {
        return "InnerClassOfInterface";
    }

    public String getGroupDisplayName() {
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(InspectionGadgetsBundle.message(
                "inner.class.on.interface.ignore.option"),
                this, "m_ignoreInnerInterfaces");
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        final PsiClass parentInterface = (PsiClass)infos[0];
        final String interfaceName = parentInterface.getName();
        return InspectionGadgetsBundle.message(
                "inner.class.on.interface.problem.descriptor", interfaceName);
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new MoveClassFix();
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
        return true;
    }

    public BaseInspectionVisitor buildVisitor() {
        return new InnerClassOnInterfaceVisitor();
    }

    private class InnerClassOnInterfaceVisitor extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so that it doesn't drill down to inner classes
            if (!aClass.isInterface() || aClass.isAnnotationType()) {
                return;
            }
            final PsiClass[] innerClasses = aClass.getInnerClasses();
            for (final PsiClass innerClass : innerClasses) {
                if (isInnerClass(innerClass)) {
                    registerClassError(innerClass, aClass);
                }
            }
        }

        private boolean isInnerClass(PsiClass innerClass) {
            if (innerClass.isEnum()) {
                return false;
            }
            if (innerClass.isAnnotationType()) {
                return false;
            }
            if (innerClass instanceof PsiTypeParameter ||
                    innerClass instanceof PsiAnonymousClass) {
                return false;
            }
            return !(innerClass.isInterface() && m_ignoreInnerInterfaces);
        }
    }
}