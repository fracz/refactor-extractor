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
package org.gradle.api.internal.dependencies.ivy.moduleconverter;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.gradle.api.dependencies.Configuration;
import org.gradle.api.dependencies.PublishArtifact;
import org.gradle.api.filter.FilterSpec;
import org.gradle.api.internal.dependencies.ArtifactContainer;

/**
 * @author Hans Dockter
 */
public class DefaultArtifactsToModuleDescriptorConverter implements ArtifactsToModuleDescriptorConverter {
    public void addArtifacts(DefaultModuleDescriptor moduleDescriptor, ArtifactContainer artifactContainer, FilterSpec filter) {
        for (PublishArtifact publishArtifact : artifactContainer.getArtifacts(filter)) {
            Artifact ivyArtifact = publishArtifact.createIvyArtifact(moduleDescriptor.getModuleRevisionId());
            for (Configuration configuration : publishArtifact.getConfigurations()) {
                moduleDescriptor.addArtifact(configuration.getName(), ivyArtifact);
            }
        }
    }
}