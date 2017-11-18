/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.codeInspection.accessStaticViaInstance;

import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightMessageUtil;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightUtil;
import com.intellij.codeInsight.daemon.impl.quickfix.AccessStaticViaInstanceFix;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * User: anna
 * Date: 15-Nov-2005
 */
public class AccessStaticViaInstance extends LocalInspectionTool {
  public String getGroupDisplayName() {
    return "";
  }

  public String getDisplayName() {
    return InspectionsBundle.message("access.static.via.instance");
  }

  @NonNls
  public String getShortName() {
    return "AccessStaticViaInstance";
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @Nullable
  public PsiElementVisitor buildVisitor(final ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        checkAccessStaticMemberViaInstanceReference(expression, expression.advancedResolve(false), holder);
      }
    };
  }

  @Nullable
  static void checkAccessStaticMemberViaInstanceReference(PsiReferenceExpression expr, JavaResolveResult result, ProblemsHolder holder) {
    PsiElement resolved = result.getElement();

    if (!(resolved instanceof PsiMember)) return;
    PsiExpression qualifierExpression = expr.getQualifierExpression();
    if (qualifierExpression == null) return;

    if (qualifierExpression instanceof PsiReferenceExpression &&
        ((PsiReferenceExpression)qualifierExpression).resolve() instanceof PsiClass) {
      return;
    }
    if (!((PsiMember)resolved).hasModifierProperty(PsiModifier.STATIC)) return;

    String description = JavaErrorMessages.message("static.member.accessed.via.instance.reference",
                                                   HighlightUtil.formatType(qualifierExpression.getType()),
                                                   HighlightMessageUtil.getSymbolName(resolved, result.getSubstitutor()));
    holder.registerProblem(expr, description, new AccessStaticViaInstanceFix(expr, result));
  }
}