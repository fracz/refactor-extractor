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
package com.siyeh.ig.j2me;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrivateMemberAccessBetweenOuterAndInnerClassInspection
        extends ClassInspection{

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "private.member.access.between.outer.and.inner.classes.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.J2ME_GROUP_NAME;
    }

    protected String buildErrorString(Object arg){
        return InspectionGadgetsBundle.message(
                "private.member.access.between.outer.and.inner.classes.problem.descriptor",
                arg);
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return new MakePackagePrivateFix(location);
    }

    private static class MakePackagePrivateFix extends InspectionGadgetsFix{

        private String elementName;

        private MakePackagePrivateFix(PsiElement location){
            super();
            final PsiReferenceExpression reference =
                    (PsiReferenceExpression) location;
            final PsiMember member = (PsiMember) reference.resolve();
            assert member != null;
            final String memberName = member.getName();
            final PsiClass containingClass = member.getContainingClass();
            assert containingClass != null;
            final String containingClassName = containingClass.getName();
            elementName = containingClassName + '.' + memberName;
        }

        public String getName(){
            return InspectionGadgetsBundle.message(
                    "private.member.access.between.outer.and.inner.classes.make.local.quickfix",
                    elementName);
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException{
                final PsiReferenceExpression reference =
                        (PsiReferenceExpression) descriptor.getPsiElement();
                final PsiModifierListOwner member =
                        (PsiModifierListOwner) reference.resolve();
                assert member != null;
                final PsiModifierList modifiers = member.getModifierList();
                modifiers.setModifierProperty(PsiModifier.PUBLIC, false);
                modifiers.setModifierProperty(PsiModifier.PROTECTED, false);
                modifiers.setModifierProperty(PsiModifier.PRIVATE, false);
        }
    }

    public BaseInspectionVisitor buildVisitor(){
        return new PrivateMemberAccessFromInnerClassVisior();
    }

    private static class PrivateMemberAccessFromInnerClassVisior
            extends BaseInspectionVisitor{

        public void visitReferenceExpression(
                @NotNull PsiReferenceExpression expression){
            super.visitReferenceExpression(expression);
            final PsiElement containingClass =
                    getContainingContextClass(expression);
            if(containingClass == null){
                return;
            }
            final PsiElement element = expression.resolve();
            if(!(element instanceof PsiMethod || element instanceof PsiField)){
                return;
            }
            final PsiMember member = (PsiMember) element;
            if(!member.hasModifierProperty(PsiModifier.PRIVATE)){
                return;
            }
            final PsiClass memberClass =
                    ClassUtils.getContainingClass(member);
            if(memberClass == null){
                return;
            }
            if(memberClass.equals(containingClass)){
                return;
            }
            final String memberClassName = memberClass.getName();
            registerError(expression, memberClassName);
        }

        @Nullable
        private static PsiClass getContainingContextClass(
                PsiReferenceExpression expression){
            final PsiClass aClass =
                    PsiTreeUtil.getParentOfType(expression, PsiClass.class);
            if(aClass instanceof PsiAnonymousClass){
                final PsiAnonymousClass anonymousClass =
                        (PsiAnonymousClass) aClass;
                final PsiExpressionList args = anonymousClass.getArgumentList();
                if(args!=null &&
                        PsiTreeUtil.isAncestor(args, expression, true)){
                    return PsiTreeUtil.getParentOfType(aClass, PsiClass.class);
                }
            }
            return aClass;
        }
    }
}