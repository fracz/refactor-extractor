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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.java.data.ResolverClassData;
import org.jetbrains.jet.lang.resolve.java.data.ResolverScopeData;
import org.jetbrains.jet.lang.resolve.java.resolver.*;
import org.jetbrains.jet.lang.resolve.java.scope.JavaPackageScope;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.types.DependencyClassByQualifiedNameResolver;
import org.jetbrains.jet.lang.types.JetType;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author abreslav
 */
public class JavaDescriptorResolver implements DependencyClassByQualifiedNameResolver {

    public static final Name JAVA_ROOT = Name.special("<java_root>");

    public static Visibility PACKAGE_VISIBILITY = new Visibility("package", false) {
        @Override
        protected boolean isVisible(@NotNull DeclarationDescriptorWithVisibility what, @NotNull DeclarationDescriptor from) {
            NamespaceDescriptor parentPackage = DescriptorUtils.getParentOfType(what, NamespaceDescriptor.class);
            NamespaceDescriptor fromPackage = DescriptorUtils.getParentOfType(from, NamespaceDescriptor.class, false);
            assert parentPackage != null;
            return parentPackage.equals(fromPackage);
        }

        @Override
        protected Integer compareTo(@NotNull Visibility visibility) {
            if (this == visibility) return 0;
            if (visibility == Visibilities.PRIVATE) return 1;
            return -1;
        }
    };

    private JavaPropertiesResolver propertiesResolver;
    private JavaClassResolver classResolver;
    private JavaConstructorResolver constructorResolver;
    private JavaFunctionResolver functionResolver;
    private JavaNamespaceResolver namespaceResolver;
    private JavaInnerClassResolver innerClassResolver;

    @Inject
    public void setFunctionResolver(JavaFunctionResolver functionResolver) {
        this.functionResolver = functionResolver;
    }

    @Inject
    public void setClassResolver(JavaClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Inject
    public void setNamespaceResolver(JavaNamespaceResolver namespaceResolver) {
        this.namespaceResolver = namespaceResolver;
    }

    @Inject
    public void setPropertiesResolver(JavaPropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    @Inject
    public void setConstructorResolver(JavaConstructorResolver constructorResolver) {
        this.constructorResolver = constructorResolver;
    }

    @Inject
    public void setInnerClassResolver(JavaInnerClassResolver innerClassResolver) {
        this.innerClassResolver = innerClassResolver;
    }

    @Nullable
    public ClassDescriptor resolveClass(@NotNull FqName qualifiedName, @NotNull DescriptorSearchRule searchRule) {
        return classResolver.resolveClass(qualifiedName, searchRule);
    }

    @Override
    public ClassDescriptor resolveClass(@NotNull FqName qualifiedName) {
        return classResolver.resolveClass(qualifiedName);
    }

    @NotNull
    public Collection<ConstructorDescriptor> resolveConstructors(@NotNull ResolverClassData classData) {
        return constructorResolver.resolveConstructors(classData);
    }

    @Nullable
    public NamespaceDescriptor resolveNamespace(@NotNull FqName qualifiedName, @NotNull DescriptorSearchRule searchRule) {
        return namespaceResolver.resolveNamespace(qualifiedName, searchRule);
    }

    @Override
    public NamespaceDescriptor resolveNamespace(@NotNull FqName qualifiedName) {
        return namespaceResolver.resolveNamespace(qualifiedName);
    }

    @Nullable
    public JavaPackageScope getJavaPackageScope(@NotNull FqName fqName, @NotNull NamespaceDescriptor ns) {
        return namespaceResolver.getJavaPackageScope(fqName, ns);
    }

    public Set<VariableDescriptor> resolveFieldGroupByName(Name name, ResolverScopeData data) {
        return propertiesResolver.resolveFieldGroupByName(name, data);
    }

    public Set<VariableDescriptor> resolveFieldGroup(ResolverScopeData data) {
        return propertiesResolver.resolveFieldGroup(data);
    }

    @Nullable
    public ClassDescriptor resolveClass(@NotNull FqName name, @NotNull DescriptorSearchRule searchRule, @NotNull PostponedTasks tasks) {
        return classResolver.resolveClass(name, searchRule, tasks);
    }

    public static class ValueParameterDescriptors {
        private final JetType receiverType;
        private final List<ValueParameterDescriptor> descriptors;

        public ValueParameterDescriptors(@Nullable JetType receiverType, @NotNull List<ValueParameterDescriptor> descriptors) {
            this.receiverType = receiverType;
            this.descriptors = descriptors;
        }

        @Nullable
        public JetType getReceiverType() {
            return receiverType;
        }

        @NotNull
        public List<ValueParameterDescriptor> getDescriptors() {
            return descriptors;
        }
    }

    @NotNull
    public Set<FunctionDescriptor> resolveFunctionGroup(@NotNull Name methodName, @NotNull ResolverScopeData scopeData) {
        return functionResolver.resolveFunctionGroup(methodName, scopeData);
    }

    public List<FunctionDescriptor> resolveMethods(@NotNull ResolverScopeData scopeData) {
        return functionResolver.resolveMethods(scopeData);
    }

    public List<ClassDescriptor> resolveInnerClasses(DeclarationDescriptor owner, PsiClass psiClass, boolean staticMembers) {
        return innerClassResolver.resolveInnerClasses(owner, psiClass, staticMembers);
    }
}