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
package com.siyeh.ig.visibility;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.FieldInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.HardcodedMethodConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class FieldHidesSuperclassFieldInspection extends FieldInspection {
    /** @noinspection PublicField*/
    public boolean m_ignoreInvisibleFields = true;
    private final RenameFix fix = new RenameFix();

    public String getID(){
        return "FieldNameHidesFieldInSuperclass";
    }

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("field.name.hides.in.superclass.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.VISIBILITY_GROUP_NAME;
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors(){
        return true;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("field.name.hides.in.superclass.problem.descriptor");
    }

    public JComponent createOptionsPanel() {
        return new SingleCheckboxOptionsPanel(InspectionGadgetsBundle.message("field.name.hides.in.superclass.ignore.option"),
                this, "m_ignoreInvisibleFields");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new FieldHidesSuperclassFieldVisitor();
    }

    private class FieldHidesSuperclassFieldVisitor extends BaseInspectionVisitor {


        public void visitField(@NotNull PsiField field) {
            final PsiClass aClass = field.getContainingClass();
            if (aClass == null) {
                return;
            }
            final String fieldName = field.getName();
            if (HardcodedMethodConstants.SERIAL_VERSION_UID.equals(fieldName)) {
                return;    //special case
            }
            PsiClass ancestorClass = aClass.getSuperClass();
            final Set<PsiClass> visitedClasses = new HashSet<PsiClass>();
            while (ancestorClass != null) {
                if (!visitedClasses.add(ancestorClass)) {
                    return;
                }
                final PsiField ancestorField = ancestorClass.findFieldByName(fieldName, false);
                if (ancestorField != null) {
                    if (!m_ignoreInvisibleFields || ClassUtils.isFieldVisible(ancestorField, aClass)) {
                        registerFieldError(field);
                        return;
                    }
                }
                ancestorClass = ancestorClass.getSuperClass();
            }
        }

    }

}