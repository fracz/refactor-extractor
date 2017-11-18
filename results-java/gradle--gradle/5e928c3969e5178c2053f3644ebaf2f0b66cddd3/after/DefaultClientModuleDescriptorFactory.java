/*
 * Copyright 2007-2009 the original author or authors.
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
package org.gradle.api.internal.dependencies.ivy;

import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.gradle.api.dependencies.DependencyContainer;
import org.gradle.api.dependencies.Dependency;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Hans Dockter
 */
public class DefaultClientModuleDescriptorFactory implements ClientModuleDescriptorFactory {
    public ModuleDescriptor createModuleDescriptor(ModuleRevisionId moduleRevisionId, DependencyContainer dependencyContainer) {
        DefaultModuleDescriptor moduleDescriptor = new DefaultModuleDescriptor(moduleRevisionId,
                "release", null);
        moduleDescriptor.addConfiguration(new Configuration(Dependency.DEFAULT_CONFIGURATION));
        addDependencyDescriptors(moduleDescriptor, dependencyContainer);
        moduleDescriptor.addArtifact(Dependency.DEFAULT_CONFIGURATION, new DefaultArtifact(moduleRevisionId, null, moduleRevisionId.getName(), "jar", "jar"));
        return moduleDescriptor;
    }

    private void addDependencyDescriptors(DefaultModuleDescriptor moduleDescriptor, DependencyContainer dependencyContainer) {
        List<DependencyDescriptor> dependencyDescriptors = new ArrayList<DependencyDescriptor>();
        for (Dependency dependency : dependencyContainer.getDependencies()) {
            dependencyDescriptors.add(dependency.createDependencyDescriptor(moduleDescriptor));
        }
        for (DependencyDescriptor dependencyDescriptor : dependencyDescriptors) {
            moduleDescriptor.addDependency(dependencyDescriptor);
        }
    }
}