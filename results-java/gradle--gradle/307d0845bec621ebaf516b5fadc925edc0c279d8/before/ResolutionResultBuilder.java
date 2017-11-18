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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.result;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.ResolvedConfigurationIdentifier;
import org.gradle.api.internal.artifacts.result.DefaultResolutionResult;
import org.gradle.api.internal.artifacts.result.DefaultResolvedDependencyResult;
import org.gradle.api.internal.artifacts.result.DefaultResolvedModuleVersionResult;
import org.gradle.api.internal.artifacts.result.DefaultUnresolvedDependencyResult;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * by Szczepan Faber, created at: 7/26/12
 */
public class ResolutionResultBuilder implements ResolvedConfigurationListener {

    private DefaultResolvedModuleVersionResult rootModule;

    private Map<ModuleVersionIdentifier, DefaultResolvedModuleVersionResult> modules
            = new LinkedHashMap<ModuleVersionIdentifier, DefaultResolvedModuleVersionResult>();

    public void start(ResolvedConfigurationIdentifier root) {
        rootModule = getModule(root.getId());
    }

    public void resolvedConfiguration(ResolvedConfigurationIdentifier id, Collection<InternalDependencyResult> dependencies) {
        DefaultResolvedModuleVersionResult module = getModule(id.getId());

        for (InternalDependencyResult d : dependencies) {
            if (d.getFailure() != null) {
                module.addDependency(new DefaultUnresolvedDependencyResult(d.getRequested(), d.getFailure(), module));
            } else {
                DefaultResolvedModuleVersionResult submodule = getModule(d.getSelected());
                DefaultResolvedDependencyResult dependency = new DefaultResolvedDependencyResult(d.getRequested(), submodule).setFrom(module);
                module.addDependency(dependency);
                submodule.addDependent(dependency);
            }
        }
    }

    private DefaultResolvedModuleVersionResult getModule(ModuleVersionIdentifier id) {
        if (!modules.containsKey(id)) {
            modules.put(id, new DefaultResolvedModuleVersionResult(id));
        }
        return modules.get(id);
    }

    public DefaultResolutionResult getResult() {
        return new DefaultResolutionResult(rootModule);
    }
}