/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.notations;

import org.gradle.api.IllegalDependencyNotation;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.Instantiator;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;

public class SelfResolvingDependencyFactory implements IDependencyImplementationFactory, NotationParser<SelfResolvingDependency> {
    private final Instantiator instantiator;

    public SelfResolvingDependencyFactory(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    public <T extends Dependency> T createDependency(Class<T> type, Object userDependencyDescription)
            throws IllegalDependencyNotation {
        if (!canParse(userDependencyDescription)) {
            throw new IllegalDependencyNotation();
        }

        return type.cast(parseNotation(userDependencyDescription));
    }

    public boolean canParse(Object notation) {
        return notation instanceof FileCollection;
    }

    public SelfResolvingDependency parseNotation(Object notation) {
        assert canParse(notation) : "This parser only accepts FileCollection.";
        FileCollection fileCollection = (FileCollection) notation;
        return instantiator.newInstance(DefaultSelfResolvingDependency.class, fileCollection);
    }
}