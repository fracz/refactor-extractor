/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.intellij.codeInsight.template.postfix.templates;

import com.intellij.codeInsight.CodeInsightServicesUtil;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;

public class NotExpressionPostfixTemplate extends JavaPostfixTemplateWithChooser {

  public NotExpressionPostfixTemplate() {
    super("not", "!expr");
  }

  public NotExpressionPostfixTemplate(String alias) {
    super(alias, alias, "!expr");
  }

  @Override
  protected void doIt(@NotNull Editor editor, @NotNull PsiElement expression) {
    expression.replace(CodeInsightServicesUtil.invertCondition((PsiExpression)expression));
  }

  @NotNull
  @Override
  protected Condition<PsiElement> getTypeCondition() {
    return JavaPostfixTemplatesUtils.IS_BOOLEAN;
  }
}