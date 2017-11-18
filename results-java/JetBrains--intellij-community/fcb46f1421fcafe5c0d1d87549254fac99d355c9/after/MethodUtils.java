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
package com.siyeh.ig.psiutils;

import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.siyeh.HardcodedMethodConstants;

public class MethodUtils{

    private MethodUtils(){
        super();
    }

    public static boolean isCompareTo(PsiMethod method){
        return methodMatches(method, HardcodedMethodConstants.COMPARE_TO, 1,
                PsiType.INT);
    }

    public static boolean isHashCode(PsiMethod method){
        return methodMatches(method, HardcodedMethodConstants.HASH_CODE, 0,
                PsiType.INT);
    }

    public static boolean isEquals(PsiMethod method){
        return methodMatches(method, HardcodedMethodConstants.EQUALS, 1,
                PsiType.BOOLEAN);
    }

    public static boolean methodMatches(
            PsiMethod method, @NotNull String methodNameP,
            int parameterCount, @NotNull PsiType returnTypeP) {
        if(method == null){
            return false;
        }
        if (parameterCount < 0) {
            throw new IllegalArgumentException(
                    "parameterCount must be greater or equals to zero");
        }
        final String methodName = method.getName();
        if(!methodNameP.equals(methodName)){
            return false;
        }
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if(parameters.length != parameterCount){
            return false;
        }
        final PsiType returnType = method.getReturnType();
        return returnTypeP.equals(returnType);
    }

    public static boolean isOverridden(final PsiMethod method){
        final PsiManager manager = method.getManager();
        final Project project = manager.getProject();
        final GlobalSearchScope globalScope =
                GlobalSearchScope.allScope(project);
        final PsiSearchHelper searchHelper = manager.getSearchHelper();
        final PsiElementProcessor.FindElement<PsiMethod> processor =
                new PsiElementProcessor.FindElement<PsiMethod>();
        final ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.runProcess(new Runnable() {
            public void run() {
                searchHelper.processOverridingMethods(processor, method,
                                                      globalScope, true);
            }
        }, null);
        return processor.isFound();
    }

    public static boolean isEmpty(PsiMethod method){
        final PsiCodeBlock body = method.getBody();
        if (body == null){
            return true;
        }
        final PsiStatement[] statements = body.getStatements();
        return statements.length == 0;
    }

    @Nullable
    public static PsiField getFieldOfGetter(PsiMethod method) {
        if (method == null) {
            return null;
        }
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length != 0){
            return null;
        }
        @NonNls final String name = method.getName();
        if (!name.startsWith("get") && !name.startsWith("is")) {
            return null;
        }
        if(method.hasModifierProperty(PsiModifier.SYNCHRONIZED)){
            return null;
        }
        final PsiCodeBlock body = method.getBody();
        if(body == null){
            return null;
        }
        final PsiStatement[] statements = body.getStatements();
        if(statements.length != 1){
            return null;
        }
        final PsiStatement statement = statements[0];
        if(!(statement instanceof PsiReturnStatement)){
            return null;
        }
        final PsiReturnStatement returnStatement =
                (PsiReturnStatement) statement;
        final PsiExpression value = returnStatement.getReturnValue();
        if(value == null){
            return null;
        }
        if(!(value instanceof PsiReferenceExpression)){
            return null;
        }
        final PsiReferenceExpression reference = (PsiReferenceExpression) value;
        final PsiExpression qualifier = reference.getQualifierExpression();
        if(qualifier != null && !(qualifier instanceof PsiThisExpression)
           && !(qualifier instanceof PsiSuperExpression)){
            return null;
        }
        final PsiElement referent = reference.resolve();
        if(referent == null){
            return null;
        }
        if(!(referent instanceof PsiField)){
            return null;
        }
        final PsiField field = (PsiField) referent;
        final PsiType fieldType = field.getType();
        final PsiType returnType = method.getReturnType();
        if(returnType == null){
            return null;
        }
        if(!fieldType.equalsToText(returnType.getCanonicalText())){
            return null;
        }
        final PsiClass fieldContainingClass = field.getContainingClass();
        final PsiClass methodContainingClass = method.getContainingClass();
        if (InheritanceUtil.isCorrectDescendant(methodContainingClass,
                fieldContainingClass, true)) {
            return field;
        } else {
            return null;
        }
    }

    public static boolean isSimpleGetter(PsiMethod method){
        return getFieldOfGetter(method) != null;
    }

    @Nullable
    public static PsiField getFieldOfSetter(PsiMethod method) {
        if (method == null) {
            return null;
        }
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length != 1){
            return null;
        }
        @NonNls final String name = method.getName();
        if (!name.startsWith("set")) {
            return null;
        }
        if(method.hasModifierProperty(PsiModifier.SYNCHRONIZED)){
            return null;
        }
        final PsiCodeBlock body = method.getBody();
        if(body == null){
            return null;
        }
        final PsiStatement[] statements = body.getStatements();
        if(statements.length != 1){
            return null;
        }
        final PsiStatement statement = statements[0];
        if(!(statement instanceof PsiExpressionStatement)){
            return null;
        }
        final PsiExpressionStatement possibleAssignmentStatement =
                (PsiExpressionStatement) statement;
        final PsiExpression possibleAssignment =
                possibleAssignmentStatement.getExpression();
        if(!(possibleAssignment instanceof PsiAssignmentExpression)){
            return null;
        }
        final PsiAssignmentExpression assignment =
                (PsiAssignmentExpression) possibleAssignment;
        final PsiJavaToken sign = assignment.getOperationSign();
        if(!JavaTokenType.EQ.equals(sign.getTokenType())){
            return null;
        }
        final PsiExpression lhs = assignment.getLExpression();
        if(!(lhs instanceof PsiReferenceExpression)){
            return null;
        }
        final PsiReferenceExpression reference = (PsiReferenceExpression) lhs;
        final PsiExpression qualifier = reference.getQualifierExpression();
        if(qualifier != null && !(qualifier instanceof PsiThisExpression) &&
           !(qualifier instanceof PsiSuperExpression)){
            return null;
        }
        final PsiElement referent = reference.resolve();
        if(referent == null){
            return null;
        }
        if(!(referent instanceof PsiField)){
            return null;
        }
        final PsiField field = (PsiField) referent;
        final PsiClass fieldContainingClass = field.getContainingClass();
        final PsiClass methodContainingClass = method.getContainingClass();
        if(!InheritanceUtil.isCorrectDescendant(methodContainingClass,
                fieldContainingClass, true)){
            return null;
        }
        final PsiExpression rhs = assignment.getRExpression();
        if(!(rhs instanceof PsiReferenceExpression)){
            return null;
        }
        final PsiReferenceExpression rReference = (PsiReferenceExpression) rhs;
        final PsiExpression rQualifier = rReference.getQualifierExpression();
        if(rQualifier != null){
            return null;
        }
        final PsiElement rReferent = rReference.resolve();
        if(rReferent == null){
            return null;
        }
        if(!(rReferent instanceof PsiParameter)){
            return null;
        }
        final PsiType fieldType = field.getType();
        final PsiType parameterType = ((PsiVariable) rReferent).getType();
        if (fieldType.equalsToText(parameterType.getCanonicalText())) {
            return field;
        } else {
            return null;
        }
    }

    public static boolean isSimpleSetter(PsiMethod method){
        return getFieldOfSetter(method) != null;
    }
}