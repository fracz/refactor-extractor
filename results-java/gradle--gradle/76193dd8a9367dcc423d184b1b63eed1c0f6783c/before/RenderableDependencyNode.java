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
import com.google.common.collect.Sets;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * by Szczepan Faber, created at: 7/27/12
 */
public class RenderableDependencyNode implements RenderableDependency {

    private final DependencyInfoCollector.DependencyNode dependencyNode;

    public RenderableDependencyNode(DependencyInfoCollector.DependencyNode dependencyNode) {
        this.dependencyNode = dependencyNode;
    }

    public String getName() {
        return dependencyNode.getId().toString();
    }

    public String getId() {
        return dependencyNode.getId().getAsked().toString();
    }

    public String getConfiguration() {
        return Joiner.on(",").join(dependencyNode.getId().getConfigurations());
    }

    public Set<RenderableDependency> getChildren() {
        if (dependencyNode == null) {
            return Sets.newHashSet();
        }
        return new LinkedHashSet(Collections2.transform(dependencyNode.getDependencies(), new Function<DependencyInfoCollector.DependencyNode, Object>() {
            public Object apply(DependencyInfoCollector.DependencyNode input) {
                return new RenderableDependencyNode(input);
            }
        }));
    }
}