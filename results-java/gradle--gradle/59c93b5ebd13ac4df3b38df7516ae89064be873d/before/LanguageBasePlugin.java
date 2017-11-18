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
package org.gradle.api.plugins;

import org.gradle.api.*;
import org.gradle.api.internal.tasks.*;
import org.gradle.api.tasks.*;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

/**
 * Base plugin for language support.
 * Adds a {@link BinariesContainer} named {@code binaries} to the project.
 * Adds a {@link ProjectSourceSet} named {@code sources} to the project.
 * Registers the {@link ResourceSet} element type for each {@link FunctionalSourceSet} added to {@link ProjectSourceSet}.
 */
@Incubating
public class LanguageBasePlugin implements Plugin<Project> {
    private final Instantiator instantiator;


    @Inject
    public LanguageBasePlugin(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    public void apply(Project target) {
        target.getExtensions().create("binaries", DefaultBinariesContainer.class, instantiator);
        target.getExtensions().create("sources", DefaultProjectSourceSet.class, instantiator);
    }
}