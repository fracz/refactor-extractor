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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.StatementInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class EmptyInitializerInspection extends StatementInspection{

    public String getID(){
        return "EmptyClassInitializer";
    }

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "empty.class.initializer.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos){
        return InspectionGadgetsBundle.message(
                "empty.class.initializer.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location){
        return new EmptyInitializerFix();
    }

    private static class EmptyInitializerFix extends InspectionGadgetsFix{

        public String getName(){
            return InspectionGadgetsBundle.message(
                    "empty.class.initializer.delete.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException{
            final PsiElement element = descriptor.getPsiElement();
            final PsiElement codeBlock = element.getParent();
            assert codeBlock != null;
            final PsiElement classInitializer = codeBlock.getParent();
            assert classInitializer!=null;
            deleteElement(classInitializer);
        }

    }

    public BaseInspectionVisitor buildVisitor(){
        return new EmptyInitializerVisitor();
    }

    private static class EmptyInitializerVisitor extends BaseInspectionVisitor{

        public void visitClassInitializer(
                @NotNull PsiClassInitializer initializer){
            super.visitClassInitializer(initializer);
            final PsiCodeBlock body = initializer.getBody();
            if(!codeBlockIsEmpty(body)){
                return;
            }
            final PsiJavaToken startingBrace = body.getLBrace();
            registerError(startingBrace);
        }

        private static boolean codeBlockIsEmpty(PsiCodeBlock codeBlock){
            final PsiStatement[] statements = codeBlock.getStatements();
            return statements.length == 0;
        }
    }
}