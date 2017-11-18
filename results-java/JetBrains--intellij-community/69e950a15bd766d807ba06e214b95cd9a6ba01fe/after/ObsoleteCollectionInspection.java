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
package com.siyeh.ig.maturity;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.VariableInspection;
import org.jetbrains.annotations.NotNull;

public class ObsoleteCollectionInspection extends VariableInspection{
    public String getID(){
        return "UseOfObsoleteCollectionType";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message("use.obsolete.collection.type.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.MATURITY_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return InspectionGadgetsBundle.message("use.obsolete.collection.type.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor(){
        return new ObsoleteCollectionVisitor();
    }

    private static class ObsoleteCollectionVisitor extends BaseInspectionVisitor{
        public void visitVariable(@NotNull PsiVariable variable){
            super.visitVariable(variable);
            final PsiType type = variable.getType();
            if(!isObsoleteCollectionType(type)){
                return;
            }
            final PsiTypeElement typeElement = variable.getTypeElement();
            registerError(typeElement);
        }

        public void visitNewExpression(@NotNull PsiNewExpression newExpression){
            super.visitNewExpression(newExpression);
            final PsiType type = newExpression.getType();
            if(!isObsoleteCollectionType(type)){
                return;
            }
            final PsiJavaCodeReferenceElement classNameElement = newExpression.getClassReference();
            registerError(classNameElement);
        }

        @SuppressWarnings({"HardCodedStringLiteral"})
        private static boolean isObsoleteCollectionType(PsiType type){
            if(type == null){
                return false;
            }

            type = type.getDeepComponentType();
            if (!(type instanceof PsiClassType)) return false;
            PsiClassType classType = (PsiClassType) type;

            if ("Vector".equals(classType.getClassName()) && classType.equalsToText("java.util.Vector")) {
                return true;
            }

            if ("Hashtable".equals(classType.getClassName()) && classType.equalsToText("java.util.Hashtable")) {
                return true;
            }

            return false;
        }
    }
}