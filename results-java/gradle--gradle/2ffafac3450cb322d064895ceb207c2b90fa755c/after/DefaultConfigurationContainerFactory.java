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

package org.gradle.api.internal.artifacts;

import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.artifacts.configurations.ResolverProvider;
import org.gradle.api.internal.artifacts.configurations.DefaultConfigurationContainer;
import org.gradle.api.internal.artifacts.ivyservice.*;

import java.util.Map;

/**
 * @author Hans Dockter
 */
public class DefaultConfigurationContainerFactory implements ConfigurationContainerFactory {
    private Map clientModuleRegistry;
    private SettingsConverter settingsConverter;
    private ModuleDescriptorConverter resolveModuleDescriptorConverter;
    private ModuleDescriptorConverter publishModuleDescriptorConverter;
    private IvyFileConverter ivyFileConverter;
    private IvyFactory ivyFactory;
    private IvyDependencyResolver dependencyResolver;
    private IvyDependencyPublisher dependencyPublisher;

    public DefaultConfigurationContainerFactory(Map clientModuleRegistry, SettingsConverter settingsConverter,
                                                ModuleDescriptorConverter resolveModuleDescriptorConverter,
                                                ModuleDescriptorConverter publishModuleDescriptorConverter,
                                                IvyFileConverter ivyFileConverter, IvyFactory ivyFactory,
                                                IvyDependencyResolver dependencyResolver, IvyDependencyPublisher dependencyPublisher) {
        this.clientModuleRegistry = clientModuleRegistry;
        this.settingsConverter = settingsConverter;
        this.resolveModuleDescriptorConverter = resolveModuleDescriptorConverter;
        this.publishModuleDescriptorConverter = publishModuleDescriptorConverter;
        this.ivyFileConverter = ivyFileConverter;
        this.ivyFactory = ivyFactory;
        this.dependencyResolver = dependencyResolver;
        this.dependencyPublisher = dependencyPublisher;
    }

    public ConfigurationContainer createConfigurationContainer(ResolverProvider resolverProvider,
                                                             DependencyMetaDataProvider dependencyMetaDataProvider) {
        IvyService ivyService = new ErrorHandlingIvyService(
                new ShortcircuitEmptyConfigsIvyService(
                        new DefaultIvyService(
                                dependencyMetaDataProvider,
                                resolverProvider,
                                settingsConverter,
                                resolveModuleDescriptorConverter,
                                publishModuleDescriptorConverter,
                                ivyFileConverter,
                                ivyFactory,
                                dependencyResolver,
                                dependencyPublisher,
                                clientModuleRegistry)));
        return new DefaultConfigurationContainer(ivyService);
    }
}