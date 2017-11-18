/*
 * Copyright 2007-2008 the original author or authors.
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

import org.gradle.api.dependencies.DependencyConfigurationMappingContainer;
import org.gradle.api.InvalidUserDataException;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

import java.util.*;

/**
 * @author Hans Dockter
 */
public class DefaultDependencyConfigurationMappingContainer implements DependencyConfigurationMappingContainer {
    private Map<String, List<String>> mappings = new HashMap<String, List<String>>();

    public DefaultDependencyConfigurationMappingContainer(Map<String, List<String>> mappings) {
        this.mappings = new HashMap<String, List<String>>(mappings);
    }

    public DefaultDependencyConfigurationMappingContainer() {
        super();
    }

    public void addMasters(String... masterConfigurations) {
        for (String masterConfiguration : masterConfigurations) {
            addToMapping(masterConfiguration, Arrays.asList(ModuleDescriptor.DEFAULT_CONFIGURATION));
        }
    }

    public void add(String... dependencyConfigurations) {
        addToMapping(WILDCARD, Arrays.asList(dependencyConfigurations));
    }

    public void add(Map<String, List<String>> dependencyConfigurations) {
        for (String masterConf : dependencyConfigurations.keySet()) {
            addToMapping(masterConf, dependencyConfigurations.get(masterConf));
        }
    }

    private void addToMapping(String masterConf, List<String> dependencyConfigurations) {
        throwExceptionIfNull(masterConf, "A master Conf");
        throwExceptionIfNull(dependencyConfigurations, "The dependency configuration list");
        if (mappings.get(masterConf) == null) {
            mappings.put(masterConf, new ArrayList<String>());
        }
        for (String dependencyConfiguration : dependencyConfigurations) {
            throwExceptionIfNull(dependencyConfiguration, "A dependency configuration");
            mappings.get(masterConf).add(dependencyConfiguration);
        }
    }

    private void throwExceptionIfNull(Object property, String text) {
        if (property == null) {
            throw new InvalidUserDataException(text + " must not be null");
        }
    }

    public Map<String, List<String>> getMappings() {
        return mappings;
    }

    public Set<String> getMasterConfigurations() {
        Set<String> masterConfs = new HashSet<String>(getMappings().keySet());
        masterConfs.remove(WILDCARD);
        return masterConfs;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultDependencyConfigurationMappingContainer that = (DefaultDependencyConfigurationMappingContainer) o;

        if (mappings != null ? !mappings.equals(that.mappings) : that.mappings != null) return false;

        return true;
    }

    public int hashCode() {
        return (mappings != null ? mappings.hashCode() : 0);
    }
}