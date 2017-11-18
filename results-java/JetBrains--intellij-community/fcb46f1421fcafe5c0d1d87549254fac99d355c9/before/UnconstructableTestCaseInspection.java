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
package com.siyeh.ig.junit;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;

public class UnconstructableTestCaseInspection extends ClassInspection {

  public String getID() {
    return "UnconstructableJUnitTestCase";
  }

  public String getGroupDisplayName() {
    return GroupNames.JUNIT_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new UnconstructableTestCaseVisitor();
  }

  private static class UnconstructableTestCaseVisitor
    extends BaseInspectionVisitor {

    public void visitClass(@NotNull PsiClass aClass) {
      if (aClass.isInterface() || aClass.isEnum() ||
          aClass.isAnnotationType() ||
          aClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
        return;
      }
      if (aClass instanceof PsiTypeParameter ||
          aClass instanceof PsiAnonymousClass) {
        return;
      }
      if (!ClassUtils.isSubclass(aClass, "junit.framework.TestCase")) {
        return;
      }

      final PsiMethod[] constructors = aClass.getConstructors();
      boolean hasStringConstructor = false;
      boolean hasNoArgConstructor = false;
      boolean hasConstructor = false;
      for (final PsiMethod constructor : constructors) {
        hasConstructor = true;
        if (!constructor.hasModifierProperty(PsiModifier.PUBLIC)) {
          continue;
        }
        final PsiParameterList parameterList =
          constructor.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length == 0) {
          hasNoArgConstructor = true;
        }
        if (parameters.length == 1) {
          final PsiType type = parameters[0].getType();
          if (TypeUtils.typeEquals("java.lang.String", type)) {
            hasStringConstructor = true;
          }
        }
      }

      if (!hasConstructor) {
        return;
      }
      if (hasNoArgConstructor || hasStringConstructor) {
        return;
      }
      registerClassError(aClass);
    }
  }
}