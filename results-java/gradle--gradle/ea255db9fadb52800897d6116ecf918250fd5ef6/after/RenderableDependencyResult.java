/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.tasks.diagnostics.internal.dependencies;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * by Szczepan Faber, created at: 7/27/12
 */
public class RenderableDependencyResult implements RenderableDependency {

    private final ResolvedDependencyResult dependency;

    public RenderableDependencyResult(ResolvedDependencyResult dependency) {
        this.dependency = dependency;
    }

    public String getName() {
        if (!requestedEqualsSelected(dependency)) {
            return requested() + " -> " + dependency.getSelected().getId().getVersion();
        } else {
            return requested();
        }
    }

    private static boolean requestedEqualsSelected(ResolvedDependencyResult dependency) {
        return DefaultModuleVersionIdentifier.newId(dependency.getRequested()).equals(dependency.getSelected().getId());
    }

    private String requested() {
        return dependency.getRequested().getGroup() + ":" + dependency.getRequested().getName() + ":" + dependency.getRequested().getVersion();
    }

    public ModuleVersionIdentifier getId() {
        return dependency.getSelected().getId();
    }

    public String getConfiguration() {
        return Joiner.on(",").join(dependency.getSelectedConfigurations());
    }

    public Set<RenderableDependency> getChildren() {
        return new LinkedHashSet(Collections2.transform(dependency.getSelected().getDependencies(), new Function<ResolvedDependencyResult, RenderableDependency>() {
            public RenderableDependency apply(ResolvedDependencyResult input) {
                return new RenderableDependencyResult(input);
            }
        }));
    }
}