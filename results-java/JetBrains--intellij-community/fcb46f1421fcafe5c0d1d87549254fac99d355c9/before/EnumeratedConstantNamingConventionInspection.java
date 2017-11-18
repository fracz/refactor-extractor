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
package com.siyeh.ig.naming;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.InspectionGadgetsBundle;

public class EnumeratedConstantNamingConventionInspection extends ConventionInspection {

  private static final int DEFAULT_MIN_LENGTH = 5;
  private static final int DEFAULT_MAX_LENGTH = 32;
  private final RenameFix fix = new RenameFix();

  public String getGroupDisplayName() {
    return GroupNames.NAMING_CONVENTIONS_GROUP_NAME;
  }

  protected InspectionGadgetsFix buildFix(PsiElement location) {
    return fix;
  }

  protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
    return true;
  }

  public String buildErrorString(PsiElement location) {
    final PsiField field = (PsiField)location.getParent();
    assert field != null;
    final String fieldName = field.getName();
    if (fieldName.length() < getMinLength()) {
      return InspectionGadgetsBundle.message("enumerated.constant.naming.convention.problem.descriptor.short");
    }
    else if (fieldName.length() > getMaxLength()) {
      return InspectionGadgetsBundle.message("enumerated.constant.naming.convention.problem.descriptor.long");
    }
    return InspectionGadgetsBundle.message("enumerated.constant.naming.convention.problem.descriptor.regex.mismatch", getRegex());
  }

  protected String getDefaultRegex() {
    return "[A-Z][A-Za-z]*";
  }

  protected int getDefaultMinLength() {
    return DEFAULT_MIN_LENGTH;
  }

  protected int getDefaultMaxLength() {
    return DEFAULT_MAX_LENGTH;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new NamingConventionsVisitor();
  }

  public ProblemDescriptor[] doCheckField(PsiField field,
                                          InspectionManager manager,
                                          boolean isOnTheFly) {
    final PsiClass containingClass = field.getContainingClass();
    if (containingClass == null) {
      return super.doCheckField(field, manager, isOnTheFly);
    }
    if (!containingClass.isPhysical()) {
      return super.doCheckField(field, manager, isOnTheFly);
    }
    final BaseInspectionVisitor visitor = createVisitor(manager,
                                                        isOnTheFly);
    field.accept(visitor);
    return visitor.getErrors();
  }

  private class NamingConventionsVisitor extends BaseInspectionVisitor {
    public void visitEnumConstant(PsiEnumConstant constant) {
      super.visitEnumConstant(constant);
      final String name = constant.getName();
      if (name == null) {
        return;
      }
      if (isValid(name)) {
        return;
      }
      registerFieldError(constant);
    }
  }
}