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
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.RenameFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class InterfaceNamingConventionInspection extends ConventionInspection {

  private static final int DEFAULT_MIN_LENGTH = 8;
  private static final int DEFAULT_MAX_LENGTH = 64;
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
    final PsiClass aClass = (PsiClass)location.getParent();
    assert aClass != null;
    final String className = aClass.getName();
    if (className.length() < getMinLength()) {
      return InspectionGadgetsBundle.message("interface.name.convention.problem.descriptor.short");
    }
    else if (className.length() > getMaxLength()) {
      return InspectionGadgetsBundle.message("interface.name.convention.problem.descriptor.long");
    }
    return InspectionGadgetsBundle.message("interface.name.convention.problem.descriptor.regex.mismatch", getRegex());
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

  public ProblemDescriptor[] doCheckClass(PsiClass aClass,
                                          InspectionManager manager,
                                          boolean isOnTheFly) {

    if (!aClass.isPhysical()) {
      return super.doCheckClass(aClass, manager, isOnTheFly);
    }
    final BaseInspectionVisitor visitor = createVisitor(manager, isOnTheFly);
    aClass.accept(visitor);

    return visitor.getErrors();
  }

  private class NamingConventionsVisitor extends BaseInspectionVisitor {

    public void visitClass(@NotNull PsiClass aClass) {
      if (!aClass.isInterface() || aClass.isAnnotationType()) {
        return;
      }
      final String name = aClass.getName();
      if (name == null) {
        return;
      }
      if (isValid(name)) {
        return;
      }
      registerClassError(aClass);
    }

  }
}