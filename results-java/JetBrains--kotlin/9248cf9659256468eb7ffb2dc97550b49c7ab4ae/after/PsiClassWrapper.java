/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.resolve.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.java.kt.JetClassAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stepan Koltsov
 */
public class PsiClassWrapper {

    @NotNull
    private final PsiClass psiClass;

    public PsiClassWrapper(@NotNull PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    private List<PsiMethodWrapper> ownMethods;
    @NotNull
    public List<PsiMethodWrapper> getOwnMethods() {
        if (ownMethods == null) {
            PsiMethod[] psiMethods = psiClass.getMethods();
            List<PsiMethodWrapper> methods = new ArrayList<PsiMethodWrapper>(psiMethods.length);
            for (PsiMethod psiMethod : psiMethods) {
                if (psiMethod.getContainingClass() != psiClass)
                    continue;
                methods.add(new PsiMethodWrapper(psiMethod));
            }
            this.ownMethods = methods;
        }
        return ownMethods;
    }

    private List<PsiFieldWrapper> ownFields;
    @NotNull
    public List<PsiFieldWrapper> getOwnFields() {
        if (ownFields == null) {
            PsiField[] psiFields = psiClass.getFields();
            List<PsiFieldWrapper> fields = new ArrayList<PsiFieldWrapper>(psiFields.length);
            for (PsiField psiField : psiFields) {
                if (psiField.getContainingClass() != psiClass)
                    continue;
                fields.add(new PsiFieldWrapper(psiField));
            }
            this.ownFields = fields;
        }
        return ownFields;
    }

    public String getQualifiedName() {
        return psiClass.getQualifiedName();
    }

    @NotNull
    public PsiClass getPsiClass() {
        return psiClass;
    }

    private JetClassAnnotation jetClass;
    @NotNull
    public JetClassAnnotation getJetClass() {
        if (jetClass == null) {
            jetClass = JetClassAnnotation.get(psiClass);
        }
        return jetClass;
    }
}