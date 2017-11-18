/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.intellij.codeInspection.streamMigration;

import com.intellij.codeInspection.streamMigration.StreamApiMigrationInspection.LimitOp;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tagir Valeev
 */
class CountMigration extends BaseStreamApiMigration {

  CountMigration() {super("count()");}

  @Override
  PsiElement migrate(@NotNull Project project,
                     @NotNull PsiLoopStatement loopStatement,
                     @NotNull PsiStatement body,
                     @NotNull StreamApiMigrationInspection.TerminalBlock tb) {
    tb = tb.tryPeelLimit(loopStatement);
    PsiExpression expression = tb.getSingleExpression(PsiExpression.class);
    StreamApiMigrationInspection.Operation lastOperation = tb.getLastOperation();
    if (expression == null && lastOperation instanceof LimitOp) {
      expression = ((LimitOp)lastOperation).getCountExpression();
    }
    PsiExpression operand = StreamApiMigrationInspection.extractIncrementedLValue(expression);
    if (!(operand instanceof PsiReferenceExpression)) return null;
    PsiElement element = ((PsiReferenceExpression)operand).resolve();
    if (!(element instanceof PsiLocalVariable)) return null;
    PsiLocalVariable var = (PsiLocalVariable)element;
    StringBuilder builder = generateStream(lastOperation).append(".count()");
    return replaceWithNumericAddition(project, loopStatement, var, builder, PsiType.LONG);
  }
}