/*
 * Copyright 2005 Bas Leijdekkers
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
package com.siyeh.ig.imports;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

class StaticImportsAreUsedVisitor extends PsiRecursiveElementVisitor {

    private Collection<PsiImportStaticStatement> importStatements;

    StaticImportsAreUsedVisitor(PsiImportStaticStatement[] importStatements) {
        super();
        this.importStatements = new ArrayList(Arrays.asList(importStatements));
    }

    public void visitElement(PsiElement element) {
        if (importStatements.isEmpty()) {
            return;
        }
        super.visitElement(element);
    }

    public void visitReferenceElement(
            @NotNull PsiJavaCodeReferenceElement reference) {
        followReferenceToImport(reference);
        super.visitReferenceElement(reference);
    }

    private void followReferenceToImport(
            PsiJavaCodeReferenceElement reference) {
        if (reference.getQualifier()!=null) {
            //it's already fully qualified, so the import statement wasn't
            // responsible
            return;
        }
        final String qualifiedName = reference.getQualifiedName();
        if (qualifiedName == null) {
            return;
        }
        final PsiElement element = reference.resolve();
        if (!(element instanceof PsiMember)) {
            return;
        }
        final PsiMember member = (PsiMember)element;
        final PsiClass containingClass = member.getContainingClass();
        if (containingClass == null) {
            return;
        }
        for (PsiImportStaticStatement importStatement : importStatements) {
            final String referenceName = importStatement.getReferenceName();
            if (!qualifiedName.equals(referenceName)) {
                return;
            }
            final PsiClass targetClass =
                    importStatement.resolveTargetClass();
            if (containingClass.equals(targetClass)) {
                importStatements.remove(importStatement);
                break;
            }
        }
    }

    public PsiImportStaticStatement[] getUnusedImportStaticStatements() {
        if (importStatements.isEmpty()) {
            return PsiImportStaticStatement.EMPTY_ARRAY;
        } else {
            return importStatements.toArray(
                    new PsiImportStaticStatement[importStatements.size()]);
        }
    }
}