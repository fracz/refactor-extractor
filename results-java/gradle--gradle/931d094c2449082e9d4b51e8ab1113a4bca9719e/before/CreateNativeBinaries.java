/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativebinaries.internal.configure;

import org.gradle.api.Transformer;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.base.BinaryContainer;
import org.gradle.model.ModelRule;
import org.gradle.nativebinaries.*;
import org.gradle.nativebinaries.internal.resolve.*;
import org.gradle.nativebinaries.platform.PlatformContainer;
import org.gradle.nativebinaries.toolchain.internal.ToolChainRegistryInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateNativeBinaries extends ModelRule {
    private final Instantiator instantiator;
    private final ProjectInternal project;
    private final CreatePrebuiltBinaries prebuiltBinariesRule;

    public CreateNativeBinaries(Instantiator instantiator, ProjectInternal project) {
        this.instantiator = instantiator;
        this.project = project;
        prebuiltBinariesRule = new CreatePrebuiltBinaries(instantiator);
    }

    public void create(BinaryContainer binaries, ToolChainRegistryInternal toolChains, Repositories repositories,
                       PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors) {
        // TODO:DAZ This should be executed as a separate rule.
        prebuiltBinariesRule.create(repositories, platforms, buildTypes, flavors);

        // TODO:DAZ Work out the right way to make these containers available to binaries.all
        project.getExtensions().add("platforms", platforms);
        project.getExtensions().add("buildTypes", buildTypes);
        project.getExtensions().add("flavors", flavors);

        NativeDependencyResolver resolver = createResolver(project, repositories);
        Transformer<Collection<NativeBinary>, NativeComponent> factory =
                new NativeBinaryFactory(instantiator, resolver, project, toolChains, platforms, buildTypes, flavors);

        for (NativeComponent component : allComponents()) {
            binaries.addAll(factory.transform(component));
        }
    }

    private static NativeDependencyResolver createResolver(ProjectInternal project, Repositories repositories) {
        List<LibraryBinaryLocator> locators = new ArrayList<LibraryBinaryLocator>();
        locators.add(new ProjectLibraryBinaryLocator(new RelativeProjectFinder(project)));
        for (PrebuiltLibraries prebuiltLibraries : repositories.withType(PrebuiltLibraries.class)) {
            locators.add(new PrebuiltLibraryBinaryLocator(prebuiltLibraries));
        }
        LibraryBinaryLocator locator = new ChainedLibraryBinaryLocator(locators);
        return new DefaultNativeDependencyResolver(locator);
    }

    private Collection<NativeComponent> allComponents() {
        ExecutableContainer executables = project.getExtensions().getByType(ExecutableContainer.class);
        LibraryContainer libraries = project.getExtensions().getByType(LibraryContainer.class);

        List<NativeComponent> components = new ArrayList<NativeComponent>();
        for (Library library : libraries) {
            components.add(library);
        }
        for (Executable executable : executables) {
            components.add(executable);
        }
        return components;
    }
}