/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
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
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class UseOfAnotherObjectsPrivateFieldInspection
        extends ExpressionInspection{

    public String getID(){
        return "AccessingNonPublicFieldOfAnotherObject";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "accessing.non.public.field.of.another.object.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.ENCAPSULATION_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos){
        return InspectionGadgetsBundle.message(
                "accessing.non.public.field.of.another.object.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor(){
        return new UseOfAnotherObjectsPrivateFieldVisitor();
    }

    private static class UseOfAnotherObjectsPrivateFieldVisitor
            extends BaseInspectionVisitor{
        public void visitReferenceExpression(
                @NotNull PsiReferenceExpression expression){
            super.visitReferenceExpression(expression);
            final PsiExpression qualifier = expression.getQualifierExpression();
            if(qualifier == null || qualifier instanceof PsiThisExpression){
                return;
            }
            final PsiElement referent = expression.resolve();
            if(!(referent instanceof PsiField)){
                return;
            }
            final PsiField field = (PsiField) referent;
            if(!field.hasModifierProperty(PsiModifier.PRIVATE) &&
                    !field.hasModifierProperty(PsiModifier.PROTECTED)){
                return;
            }
            if(field.hasModifierProperty(PsiModifier.STATIC)){
                return;
            }
            final PsiElement fieldNameElement =
                    expression.getReferenceNameElement();
            registerError(fieldNameElement);
        }
    }
}