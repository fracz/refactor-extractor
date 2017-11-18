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
package com.siyeh.ig.threading;

import com.intellij.psi.*;

class ContainsSynchronizationVisitor extends PsiRecursiveElementVisitor{

    private boolean containsSynchronization = false;

    public void visitElement(PsiElement element){
        if(containsSynchronization){
            return;
        }
        super.visitElement(element);
    }

    public void visitSynchronizedStatement(PsiSynchronizedStatement statement){
        containsSynchronization = true;
    }

    public void visitMethod(PsiMethod method){
        if(method.hasModifierProperty(PsiModifier.SYNCHRONIZED)){
            containsSynchronization = true;
            return;
        }
        super.visitMethod(method);
    }

    public boolean containsSynchronization(){
        return containsSynchronization;
    }
}