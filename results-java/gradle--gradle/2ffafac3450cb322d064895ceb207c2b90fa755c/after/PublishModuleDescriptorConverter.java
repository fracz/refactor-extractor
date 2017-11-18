/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.moduleconverter;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Module;
import org.gradle.api.internal.artifacts.ivyservice.ModuleDescriptorConverter;
import org.gradle.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Hans Dockter
 */
public class PublishModuleDescriptorConverter implements ModuleDescriptorConverter {
    private static Logger logger = LoggerFactory.getLogger(PublishModuleDescriptorConverter.class);
    private ModuleDescriptorConverter resolveModuleDescriptorConverter;
    private ArtifactsToModuleDescriptorConverter artifactsToModuleDescriptorConverter;

    public PublishModuleDescriptorConverter(ModuleDescriptorConverter resolveModuleDescriptorConverter,
                                            ArtifactsToModuleDescriptorConverter artifactsToModuleDescriptorConverter) {
        this.resolveModuleDescriptorConverter = resolveModuleDescriptorConverter;
        this.artifactsToModuleDescriptorConverter = artifactsToModuleDescriptorConverter;
    }

    public ModuleDescriptor convert(Set<Configuration> configurations, Module module, IvySettings settings) {
        Clock clock = new Clock();
        ModuleDescriptor moduleDescriptor = resolveModuleDescriptorConverter.convert(configurations, module, settings);
        artifactsToModuleDescriptorConverter.addArtifacts((DefaultModuleDescriptor) moduleDescriptor, configurations);
        logger.debug("Timing: Ivy convert for publish took {}", clock.getTime());
        return moduleDescriptor;
    }

    public ArtifactsToModuleDescriptorConverter getArtifactsToModuleDescriptorConverter() {
        return artifactsToModuleDescriptorConverter;
    }

    public void setArtifactsToModuleDescriptorConverter(ArtifactsToModuleDescriptorConverter artifactsToModuleDescriptorConverter) {
        this.artifactsToModuleDescriptorConverter = artifactsToModuleDescriptorConverter;
    }
}