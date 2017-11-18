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
package com.siyeh.ig.bugs;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.UtilityClassUtil;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class InstantiationOfUtilityClassInspection extends ExpressionInspection{

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "instantiation.utility.class.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos){
        return InspectionGadgetsBundle.message(
                "instantiation.utility.class.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor(){
        return new InstantiationOfUtilityClassVisitor();
    }

    private static class InstantiationOfUtilityClassVisitor
            extends BaseInspectionVisitor{

        public void visitNewExpression(@NotNull PsiNewExpression expression){
            final PsiType type = expression.getType();
            if(!(type instanceof PsiClassType)){
                return;
            }
            final PsiClass aClass = ((PsiClassType) type).resolve();
            if(aClass == null){
                return;
            }
            if(!UtilityClassUtil.isUtilityClass(aClass)){
                return;
            }
            final PsiJavaCodeReferenceElement classReference =
                    expression.getClassReference();
            registerError(classReference);
        }
    }
}