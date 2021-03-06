/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.resolve.java.structure;

import com.intellij.psi.PsiTypeParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Collection;

import static org.jetbrains.jet.lang.resolve.java.structure.JavaElementCollectionFromPsiArrayUtil.classTypes;

public class JavaTypeParameter extends JavaElementImpl implements JavaNamedElement {
    public JavaTypeParameter(@NotNull PsiTypeParameter psiTypeParameter) {
        super(psiTypeParameter);
    }

    @NotNull
    @Override
    public PsiTypeParameter getPsi() {
        return (PsiTypeParameter) super.getPsi();
    }

    public int getIndex() {
        return getPsi().getIndex();
    }

    @NotNull
    @Override
    public Name getName() {
        return Name.identifier(getPsi().getName());
    }

    @NotNull
    public Collection<JavaClassType> getUpperBounds() {
        return classTypes(getPsi().getExtendsList().getReferencedTypes());
    }
}