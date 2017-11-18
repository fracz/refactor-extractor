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

package org.jetbrains.jet.lang.resolve.java.scope;

import com.google.common.collect.Sets;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.java.*;
import org.jetbrains.jet.lang.resolve.java.data.ResolverScopeData;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScopeImpl;

import java.util.Collection;

import static org.jetbrains.jet.lang.resolve.java.scope.ScopeUtils.computeAllClassDeclarations;
import static org.jetbrains.jet.lang.resolve.java.scope.ScopeUtils.computeAllPackageDeclarations;

public abstract class JavaBaseScope extends JetScopeImpl {

    @NotNull
    protected final JavaSemanticServices semanticServices;
    @NotNull
    protected final ResolverScopeData resolverScopeData;

    @Nullable
    private Collection<DeclarationDescriptor> allDescriptors = null;

    protected JavaBaseScope(
            @NotNull JavaSemanticServices semanticServices,
            @NotNull ResolverScopeData resolverScopeData
    ) {
        this.semanticServices = semanticServices;
        this.resolverScopeData = resolverScopeData;
    }

    @NotNull
    @Override
    public DeclarationDescriptor getContainingDeclaration() {
        return resolverScopeData.getClassOrNamespaceDescriptor();
    }

    @NotNull
    @Override
    public Collection<VariableDescriptor> getProperties(@NotNull Name name) {
        return semanticServices.getDescriptorResolver().resolveFieldGroupByName(name, resolverScopeData);
    }

    @NotNull
    @Override
    public Collection<FunctionDescriptor> getFunctions(@NotNull Name name) {
        return semanticServices.getDescriptorResolver().resolveFunctionGroup(name, resolverScopeData);
    }

    @NotNull
    @Override
    public Collection<DeclarationDescriptor> getAllDescriptors() {
        if (allDescriptors != null) {
            return allDescriptors;
        }

        allDescriptors = computeAllDescriptors();

        return allDescriptors;
    }

    @NotNull
    private Collection<DeclarationDescriptor> computeAllDescriptors() {
        Collection<DeclarationDescriptor> result = Sets.newHashSet();
        PsiClass psiClass = resolverScopeData.getPsiClass();
        if (psiClass != null) {
            result.addAll(computeAllClassDeclarations(psiClass, semanticServices, resolverScopeData, getContainingDeclaration()));
        }

        PsiPackage psiPackage = resolverScopeData.getPsiPackage();
        if (psiPackage != null) {
            result.addAll(computeAllPackageDeclarations(psiPackage, semanticServices, resolverScopeData.getFqName()));
        }
        return result;
    }
}