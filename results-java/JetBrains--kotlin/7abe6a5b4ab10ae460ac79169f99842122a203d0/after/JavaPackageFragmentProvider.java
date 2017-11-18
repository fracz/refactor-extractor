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

package org.jetbrains.jet.lang.resolve.java.resolver;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptorImpl;
import org.jetbrains.jet.lang.descriptors.PackageFragmentDescriptor;
import org.jetbrains.jet.lang.descriptors.PackageFragmentProvider;
import org.jetbrains.jet.lang.resolve.java.JavaClassFinder;
import org.jetbrains.jet.lang.resolve.java.PackageClassUtils;
import org.jetbrains.jet.lang.resolve.java.descriptor.JavaPackageFragmentDescriptor;
import org.jetbrains.jet.lang.resolve.java.sam.SingleAbstractMethodUtils;
import org.jetbrains.jet.lang.resolve.java.scope.JavaClassStaticMembersScope;
import org.jetbrains.jet.lang.resolve.java.scope.JavaPackageScope;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.java.structure.JavaField;
import org.jetbrains.jet.lang.resolve.java.structure.JavaMethod;
import org.jetbrains.jet.lang.resolve.java.structure.JavaPackage;
import org.jetbrains.jet.lang.resolve.kotlin.DeserializedDescriptorResolver;
import org.jetbrains.jet.lang.resolve.kotlin.KotlinClassFinder;
import org.jetbrains.jet.lang.resolve.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;

import javax.inject.Inject;
import java.util.*;

public final class JavaPackageFragmentProvider implements PackageFragmentProvider {
    @NotNull
    private final Map<FqName, JetScope> packageFragments = new HashMap<FqName, JetScope>();
    @NotNull
    private final Set<FqName> unresolvedCache = new HashSet<FqName>();

    private JavaClassFinder javaClassFinder;
    private JavaResolverCache cache;
    private JavaMemberResolver memberResolver;

    private DeserializedDescriptorResolver deserializedDescriptorResolver;
    private KotlinClassFinder kotlinClassFinder;

    private ModuleDescriptor module;

    @Inject
    public void setKotlinClassFinder(KotlinClassFinder kotlinClassFinder) {
        this.kotlinClassFinder = kotlinClassFinder;
    }

    @Inject
    public void setJavaClassFinder(JavaClassFinder javaClassFinder) {
        this.javaClassFinder = javaClassFinder;
    }

    @Inject
    public void setCache(JavaResolverCache cache) {
        this.cache = cache;
    }

    @Inject
    public void setMemberResolver(@NotNull JavaMemberResolver memberResolver) {
        this.memberResolver = memberResolver;
    }

    @Inject
    public void setDeserializedDescriptorResolver(DeserializedDescriptorResolver deserializedDescriptorResolver) {
        this.deserializedDescriptorResolver = deserializedDescriptorResolver;
    }

    @Inject
    public void setModule(ModuleDescriptor module) {
        this.module = module;
        // TODO 1 hacky
        ((ModuleDescriptorImpl) module).addFragmentProvider(this);
    }

    @NotNull
    @Override
    public List<PackageFragmentDescriptor> getPackageFragments(@NotNull FqName fqName) {
        return ContainerUtil.<PackageFragmentDescriptor>createMaybeSingletonList(getOrCreatePackage(fqName));
    }

    @NotNull
    @Override
    public Collection<FqName> getSubPackagesOf(@NotNull FqName fqName) {
        PackageFragmentDescriptor packageFragment = getOrCreatePackage(fqName);
        if (packageFragment == null) {
            return Collections.emptyList();
        }

        JetScope scope = packageFragment.getMemberScope();
        // TODO 2 replace instanceof with interface
        if (scope instanceof JavaPackageScope) {
            return ((JavaPackageScope) scope).getSubPackages();
        }
        if (scope instanceof JavaClassStaticMembersScope) {
            return ((JavaClassStaticMembersScope) scope).getSubPackages();
        }
        return Collections.emptyList();
    }

    @Nullable
    public JavaPackageFragmentDescriptor getOrCreatePackage(@NotNull FqName qualifiedName) {
        // TODO 1 use one cache
        if (unresolvedCache.contains(qualifiedName)) {
            return null;
        }
        JetScope scope = packageFragments.get(qualifiedName);
        if (scope != null) {
            return (JavaPackageFragmentDescriptor) scope.getContainingDeclaration();
        }

        JavaPackageFragmentDescriptor packageFragment = new JavaPackageFragmentDescriptor(this, qualifiedName);

        JetScope namespaceScope = createPackageScope(qualifiedName, packageFragment, true);
        cache(qualifiedName, namespaceScope);
        if (namespaceScope == null) {
            return null;
        }

        packageFragment.setMemberScope(namespaceScope);

        return packageFragment;
    }

    @Nullable
    private JetScope createPackageScope(
            @NotNull FqName fqName,
            @NotNull PackageFragmentDescriptor packageFragment,
            boolean record
    ) {
        JavaPackage javaPackage = javaClassFinder.findPackage(fqName);
        if (javaPackage != null) {
            FqName packageClassFqName = PackageClassUtils.getPackageClassFqName(fqName);
            KotlinJvmBinaryClass kotlinClass = kotlinClassFinder.find(packageClassFqName);

            cache.recordProperPackage(packageFragment);

            if (kotlinClass != null) {
                JetScope kotlinPackageScope = deserializedDescriptorResolver.createKotlinPackageScope(packageFragment, kotlinClass);
                if (kotlinPackageScope != null) {
                    return kotlinPackageScope;
                }
            }

            return new JavaPackageScope(packageFragment, javaPackage, fqName, memberResolver);
        }

        JavaClass javaClass = javaClassFinder.findClass(fqName);
        if (javaClass != null && shouldCreateStaticMembersPackage(javaClass)) {
            cache.recordClassStaticMembersNamespace(packageFragment);
            return new JavaClassStaticMembersScope(packageFragment, javaClass, memberResolver);
        }
        return null;
    }

    // TODO 2 move to more decent place
    public static boolean shouldCreateStaticMembersPackage(@NotNull JavaClass javaClass) {
        return !DescriptorResolverUtils.isCompiledKotlinClassOrPackageClass(javaClass) && hasStaticMembers(javaClass);
    }

    private void cache(@NotNull FqName fqName, @Nullable JetScope packageScope) {
        if (packageScope == null) {
            unresolvedCache.add(fqName);
            return;
        }
        JetScope oldValue = packageFragments.put(fqName, packageScope);
        if (oldValue != null) {
            throw new IllegalStateException("rewrite at " + fqName);
        }
    }

    private static boolean hasStaticMembers(@NotNull JavaClass javaClass) {
        for (JavaMethod method : javaClass.getMethods()) {
            if (method.isStatic() && !DescriptorResolverUtils.shouldBeInEnumClassObject(method)) {
                return true;
            }
        }

        for (JavaField field : javaClass.getFields()) {
            if (field.isStatic() && !field.isEnumEntry()) {
                return true;
            }
        }

        for (JavaClass nestedClass : javaClass.getInnerClasses()) {
            if (SingleAbstractMethodUtils.isSamInterface(nestedClass)) {
                return true;
            }
            if (nestedClass.isStatic() && hasStaticMembers(nestedClass)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public Collection<Name> getClassNamesInPackage(@NotNull FqName packageName) {
        JavaPackage javaPackage = javaClassFinder.findPackage(packageName);
        if (javaPackage == null) return Collections.emptyList();

        Collection<JavaClass> classes = DescriptorResolverUtils.getClassesInPackage(javaPackage);
        List<Name> result = new ArrayList<Name>(classes.size());
        for (JavaClass javaClass : classes) {
            if (DescriptorResolverUtils.isCompiledKotlinClass(javaClass)) {
                result.add(javaClass.getName());
            }
        }

        return result;
    }

    @NotNull
    public ModuleDescriptor getModule() {
        return module;
    }
}