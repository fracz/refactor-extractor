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

import java.util.HashSet;
import java.util.Set;

public class ClassNameSameAsAncestorNameInspection extends ClassInspection {

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
    return new ClassNameSameAsAncestorNameVisitor();
  }

  private static class ClassNameSameAsAncestorNameVisitor
    extends BaseInspectionVisitor {
    public void visitClass(@NotNull PsiClass aClass) {
      // no call to super, so it doesn't drill down into inner classes
      final String className = aClass.getName();
      if (className == null) {
        return;
      }
      final Set<PsiClass> alreadyVisited = new HashSet<PsiClass>(8);
      final PsiClass[] supers = aClass.getSupers();
      for (final PsiClass aSuper : supers) {
        if (hasMatchingName(aSuper, className, alreadyVisited)) {
          registerClassError(aClass);
        }
      }
    }

    private static boolean hasMatchingName(PsiClass aSuper,
                                           String className,
                                           Set<PsiClass> alreadyVisited) {
      if (aSuper == null) {
        return false;
      }
      if (alreadyVisited.contains(aSuper)) {
        return false;
      }
      alreadyVisited.add(aSuper);
      final String superName = aSuper.getName();
      if (className.equals(superName)) {
        return true;
      }
      final PsiClass[] supers = aSuper.getSupers();
      for (PsiClass aSupers : supers) {
        if (hasMatchingName(aSupers, className, alreadyVisited)) {
          return true;
        }
      }
      return false;
    }
  }
}