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
package com.siyeh.ig.encapsulation;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class ReturnOfDateFieldInspection extends StatementInspection{
    public String getDisplayName(){
        return InspectionGadgetsBundle.message("return.date.calendar.field.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.ENCAPSULATION_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        final PsiField field = (PsiField) ((PsiReference) location).resolve();
        assert field != null;
        final PsiType type = field.getType();
        return InspectionGadgetsBundle.message("return.date.calendar.field.problem.descriptor", type.getPresentableText());
    }

    public BaseInspectionVisitor buildVisitor(){
        return new ReturnOfDateFieldVisitor();
    }

    private static class ReturnOfDateFieldVisitor extends StatementInspectionVisitor{

        public void visitReturnStatement(@NotNull PsiReturnStatement statement){
            super.visitReturnStatement(statement);
            final PsiExpression returnValue = statement.getReturnValue();
            if(!(returnValue instanceof PsiReferenceExpression)){
                return;
            }
            final PsiReferenceExpression fieldReference =
                    (PsiReferenceExpression) returnValue;

            final PsiElement element = fieldReference.resolve();
            if(!(element instanceof PsiField)){
                return;
            }
            if(!TypeUtils.expressionHasTypeOrSubtype("java.util.Date",
                                                     returnValue) &&
                                                                        !TypeUtils.expressionHasTypeOrSubtype("java.util.Calendar",
                                                                                                              returnValue)){
                return;
            }
            registerError(returnValue);
        }
    }
}