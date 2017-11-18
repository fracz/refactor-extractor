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
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.psiutils.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

public class TeardownIsPublicVoidNoArgInspection extends MethodInspection {

  public String getID() {
    return "TearDownWithIncorrectSignature";
  }

  public String getGroupDisplayName() {
    return GroupNames.JUNIT_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new TeardownIsPublicVoidNoArgVisitor();
  }

  private static class TeardownIsPublicVoidNoArgVisitor extends BaseInspectionVisitor {

    public void visitMethod(@NotNull PsiMethod method) {
      //note: no call to super;
      @NonNls final String methodName = method.getName();
      if (!"tearDown".equals(methodName)) {
        return;
      }
      final PsiType returnType = method.getReturnType();
      if (returnType == null) {
        return;
      }
      final PsiClass targetClass = method.getContainingClass();
      if (targetClass == null) {
        return;
      }
      if (!ClassUtils.isSubclass(targetClass, "junit.framework.TestCase")) {
        return;
      }
      final PsiParameterList parameterList = method.getParameterList();
      if (parameterList == null) {
        return;
      }
      final PsiParameter[] parameters = parameterList.getParameters();
      if (parameters == null) {
        return;
      }
      if (parameters.length != 0) {
        registerMethodError(method);
      }
      else if (!returnType.equals(PsiType.VOID)) {
        registerMethodError(method);
      }
      else if (!method.hasModifierProperty(PsiModifier.PUBLIC) &&
               !method.hasModifierProperty(PsiModifier.PROTECTED)) {
        registerMethodError(method);
      }
    }

  }
}