/*
 * Copyright 2008 the original author or authors.
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
package org.gradle.api.dependencies;

import org.gradle.api.Transformer;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.io.File;

import groovy.lang.Closure;

/**
 * <p>A {@code Configuration} represents a group of artifacts and their dependencies.</p>
 */
public interface Configuration {
    /**
     * Returns the name of this configuration.
     *
     * @return The configuration name, never null.
     */
    String getName();

    /**
     * Returns true if this is a visible configuration. A visible configuration is usable outside the project it belongs
     * to. The default value is true.
     *
     * @return true if this is a visible configuration.
     */
    boolean isVisible();

    /**
     * Sets the visibility of this configuration. When visible is set to true, this configuration is visibile outside
     * the project it belongs to. The default value is true.
     *
     * @param visible true if this is a visible configuration
     * @return this configuration
     */
    Configuration setVisible(boolean visible);

    /**
     * Returns the names of the configurations which this configuration extends from. The artifacts of the super
     * configurations are also available in this configuration.
     *
     * @return The super configurations. Returns an empty set when this configuration does not extend any others.
     */
    Set<? extends Configuration> getExtendsFrom();

    /**
     * Sets the configurations which this configuration extends from.
     *
     * @param superConfigs The super configuration. Should not be null.
     * @return this configuration
     */
    Configuration setExtendsFrom(Set<String> superConfigs);

    /**
     * Adds the given configurations to the set of configuration which this configuration extends from.
     *
     * @param superConfigs The super configurations.
     * @return this configuration
     */
    Configuration extendsFrom(String... superConfigs);

    /**
     * Returns the transitivity of this configuration. A transitive configuration contains the transitive closure of its
     * direct dependencies, and all their dependencies. An intransitive configuration contains only the direct
     * dependencies. The default value is true.
     *
     * @return true if this is a transitive configuration, false otherwise.
     */
    boolean isTransitive();

    /**
     * Sets the transitivity of this configuration. When set to true, this configuration will contain the transitive
     * closure of its dependencies and their dependencies. The default value is true.
     *
     * @param t true if this is a transitive configuration.
     * @return this configuration
     */
    Configuration setTransitive(boolean t);

    /**
     * Returns the description for this configuration.
     *
     * @return the description. May be null.
     */
    String getDescription();

    /**
     * Sets the description for this configuration.
     *
     * @param description the description. May be null
     * @return this configuration
     */
    Configuration setDescription(String description);

    Set<? extends Configuration> getChain();

    org.apache.ivy.core.module.descriptor.Configuration getIvyConfiguration(boolean transitive);

    void addIvyTransformer(Transformer<org.apache.ivy.core.module.descriptor.Configuration> transformer);

    void addIvyTransformer(Closure transformer);

    ResolveInstruction getResolveInstruction();
}