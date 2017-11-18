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
import com.intellij.psi.tree.IElementType;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.ig.psiutils.WellFormednessUtils;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.HardcodedMethodConstants;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ObjectToStringInspection extends ExpressionInspection {

    public String getDisplayName() {
      return InspectionGadgetsBundle.message(
              "default.tostring.call.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "default.tostring.call.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ObjectToStringVisitor();
    }

    private static class ObjectToStringVisitor
            extends BaseInspectionVisitor {

        public void visitBinaryExpression(
                @NotNull PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            final PsiType type = expression.getType();
            if(type == null) {
                return;
            }
            if(!TypeUtils.isJavaLangString(type)) {
                return;
            }
            final PsiExpression lhs = expression.getLOperand();
            checkExpression(lhs);
            final PsiExpression rhs = expression.getROperand();
            checkExpression(rhs);
        }

        public void visitAssignmentExpression(
                @NotNull PsiAssignmentExpression expression) {
            super.visitAssignmentExpression(expression);
            if(!WellFormednessUtils.isWellFormed(expression)) {
                return;
            }
            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if (!tokenType.equals(JavaTokenType.PLUSEQ)) {
                return;
            }
            final PsiExpression lhs = expression.getLExpression();
            final PsiType type = lhs.getType();
            if (type == null) {
                return;
            }
            if (!TypeUtils.isJavaLangString(type)) {
                return;
            }
            final PsiExpression rhs = expression.getRExpression();
            checkExpression(rhs);
        }

        public void visitMethodCallExpression(
                PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiReferenceExpression methodExpression =
                    expression.getMethodExpression();
            final String name = methodExpression.getReferenceName();
            if(!HardcodedMethodConstants.TO_STRING.equals(name)) {
                return;
            }
            final PsiExpressionList argList = expression.getArgumentList();
            final PsiExpression[] args = argList.getExpressions();
            if(args.length !=0) {
                return;
            }
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            checkExpression(qualifier);
        }

        private void checkExpression(PsiExpression expression) {
            if(expression == null) {
                return;
            }
            final PsiType type = expression.getType();
            if(type == null) {
                return;
            }
            if(type instanceof PsiArrayType) {
                registerError(expression);
                return;
            }
            if(!(type instanceof PsiClassType)) {
                return;
            }
            final PsiClassType classType = (PsiClassType) type;
            if (type.equalsToText("java.lang.Object")) {
                return;
            }
            final PsiClass referencedClass = classType.resolve();
            if(referencedClass == null) {
                return;
            }
            if(referencedClass.isEnum() || referencedClass.isInterface()) {
                return;
            }
            if(!hasGoodToString(referencedClass, new HashSet<PsiClass>())) {
                registerError(expression);
            }
        }

        private static boolean hasGoodToString(PsiClass aClass,
                                               Set<PsiClass> visitedClasses) {
            if(aClass == null) {
                return false;
            }
            if (!visitedClasses.add(aClass)) {
                return true;
            }
            final String className = aClass.getQualifiedName();
            if("java.lang.Object".equals(className)) {
                return false;
            }
            final PsiMethod[] methods = aClass.getMethods();
            for(PsiMethod method : methods) {
                if(isToString(method)) {
                    return true;
                }
            }
            final PsiClass superClass = aClass.getSuperClass();
            return hasGoodToString(superClass, visitedClasses);
        }

        private static boolean isToString(PsiMethod method) {
            final String methodName = method.getName();
            if(!HardcodedMethodConstants.TO_STRING.equals(methodName)){
                return false;
            }
            final PsiParameterList paramList = method.getParameterList();
            final PsiParameter[] params = paramList.getParameters();
            return params != null && params.length == 0;
        }
    }
}