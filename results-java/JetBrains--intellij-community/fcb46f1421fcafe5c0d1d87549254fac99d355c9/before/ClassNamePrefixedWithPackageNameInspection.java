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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.RenameFix;
import org.jetbrains.annotations.NotNull;

import java.util.StringTokenizer;

public class ClassNamePrefixedWithPackageNameInspection extends ClassInspection {

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

  public BaseInspectionVisitor buildVisitor() {
    return new ClassNameBePrefixedWithPackageNameVisitor();
  }

  private static class ClassNameBePrefixedWithPackageNameVisitor extends BaseInspectionVisitor {


    public void visitClass(@NotNull PsiClass aClass) {
      // no call to super, so it doesn't drill down into inner classes
      final String className = aClass.getName();
      final String qualifiedName = aClass.getQualifiedName();
      if (className == null) {
        return;
      }
      if (qualifiedName == null) {
        return;
      }
      if (className.equals(qualifiedName)) {
        return;
      }
      final StringTokenizer tokenizer = new StringTokenizer(qualifiedName, ".");
      String currentPackageName = null;
      String lastPackageName = null;
      while (tokenizer.hasMoreTokens()) {
        lastPackageName = currentPackageName;
        currentPackageName = tokenizer.nextToken();
      }

      if (lastPackageName == null) {
        return;
      }
      final String lowercaseClassName = className.toLowerCase();
      final String lowercasePackageName = lastPackageName.toLowerCase();
      if (!lowercaseClassName.startsWith(lowercasePackageName)) {
        return;
      }
      registerClassError(aClass);
    }

  }
}