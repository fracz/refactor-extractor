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

package org.gradle.api.internal.dependencies;

import groovy.lang.Closure;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.gradle.api.Project;
import org.gradle.api.dependencies.*;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.GUtil;

import java.util.*;

/**
 * @author Hans Dockter
 */
public class DefaultDependencyContainer implements DependencyContainer {
    private Map clientModuleRegistry = new HashMap();

    private List<String> defaultConfs = new ArrayList<String>();

    private List<Dependency> dependencies = new ArrayList<Dependency>();

    private List<DependencyDescriptor> dependencyDescriptors = new ArrayList<DependencyDescriptor>();

    private DependencyFactory dependencyFactory;

    private Project project;

    public DefaultDependencyContainer() {
    }

    public DefaultDependencyContainer(DependencyFactory dependencyFactory, List defaultConfs) {
        this.dependencyFactory = dependencyFactory;
        this.defaultConfs = defaultConfs;
    }

    public void dependencies(List<String> confs, Object... dependencies) {
        dependencies(getStandardConfigurationMapping(confs).getMappings(), dependencies);
    }

    public void dependencies(Object... dependencies) {
        dependencies(defaultConfs, dependencies);
    }

    public void dependencyDescriptors(DependencyDescriptor... dependencyDescriptors) {
        this.dependencyDescriptors.addAll(Arrays.asList(dependencyDescriptors));
    }

    public Dependency dependency(List<String> confs, Object id) {
        return dependency(confs, id, null);
    }

    public Dependency dependency(List<String> confs, Object id, Closure configureClosure) {
        return dependency(getStandardConfigurationMapping(confs).getMappings(), id, configureClosure);
    }

    public Dependency dependency(String id) {
        return dependency(id, null);
    }

    public Dependency dependency(String id, Closure configureClosure) {
        return dependency(defaultConfs, id, configureClosure);
    }

    public void dependencies(Map<String, List<String>> configurationMappings, Object... dependencies) {
        for (Object dependency : GUtil.flatten(Arrays.asList(dependencies))) {
            this.dependencies.add(dependencyFactory.createDependency(getStandardConfigurationMapping(configurationMappings), dependency, project));
        }
    }

    public Dependency dependency(Map<String, List<String>> configurationMappings, Object userDependencyDescription) {
        return dependency(configurationMappings, userDependencyDescription, null);
    }

    public Dependency dependency(Map<String, List<String>> configurationMappings, Object userDependencyDescription, Closure configureClosure) {
        Dependency dependency = dependencyFactory.createDependency(getStandardConfigurationMapping(configurationMappings), userDependencyDescription, project);
        dependencies.add(dependency);
        ConfigureUtil.configure(configureClosure, dependency);
        return dependency;
    }


    public ClientModule clientModule(List<String> confs, String id) {
        return clientModule(confs, id, null);
    }

    public ClientModule clientModule(List<String> confs, String id, Closure configureClosure) {
        // todo: We might better have a client module factory here
        ClientModule clientModule = new ClientModule(dependencyFactory, getStandardConfigurationMapping(confs), id, clientModuleRegistry);
        dependencies.add(clientModule);
        ConfigureUtil.configure(configureClosure, clientModule);
        return clientModule;
    }

    public ClientModule clientModule(String artifact) {
        return clientModule(artifact, null);
    }

    public ClientModule clientModule(String artifact, Closure configureClosure) {
        return clientModule(defaultConfs, artifact, configureClosure);
    }

    public List<Dependency> getDependencies(Filter filter) {
        List<Dependency> result = new ArrayList<Dependency>();
        for (Dependency dependency : getDependencies()) {
            if (filter.includeDependency(dependency)) {
                result.add(dependency);
            }
        }
        return result;
    }

    // todo Do we really need this method?
    public Object configure(Closure closure) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(this);
        closure.call();
        return this;
    }

    public Map getClientModuleRegistry() {
        return clientModuleRegistry;
    }

    public void setClientModuleRegistry(Map clientModuleRegistry) {
        this.clientModuleRegistry = clientModuleRegistry;
    }

    public List<String> getDefaultConfs() {
        return defaultConfs;
    }

    public void setDefaultConfs(List<String> defaultConfs) {
        this.defaultConfs = defaultConfs;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<DependencyDescriptor> getDependencyDescriptors() {
        return dependencyDescriptors;
    }

    public void setDependencyDescriptors(List<DependencyDescriptor> dependencyDescriptors) {
        this.dependencyDescriptors = dependencyDescriptors;
    }

    public DependencyFactory getDependencyFactory() {
        return dependencyFactory;
    }

    public void setDependencyFactory(DependencyFactory dependencyFactory) {
        this.dependencyFactory = dependencyFactory;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private DependencyConfigurationMappingContainer getStandardConfigurationMapping(final List<String> masterConfs) {
        DependencyConfigurationMappingContainer mappingContainer =  new DefaultDependencyConfigurationMappingContainer();
        mappingContainer.addMasters((String[]) masterConfs.toArray(new String[masterConfs.size()]));
        return mappingContainer;
    }

    private DependencyConfigurationMappingContainer getStandardConfigurationMapping(final Map<String, List<String>> confMapping) {
        return new DefaultDependencyConfigurationMappingContainer(confMapping);
    }
}