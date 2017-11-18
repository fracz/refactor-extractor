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
package org.gradle.api.internal.dependencies.maven.dependencies;

import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.gradle.api.internal.dependencies.maven.dependencies.MavenDependency;
import org.gradle.api.internal.dependencies.maven.dependencies.PomDependenciesConverter;
import org.gradle.api.dependencies.maven.MavenPom;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Hans Dockter
 */
public class DefaultPomDependenciesConverter implements PomDependenciesConverter {
    private ExcludeRuleConverter excludeRuleConverter;

    public DefaultPomDependenciesConverter(ExcludeRuleConverter excludeRuleConverter) {
        this.excludeRuleConverter = excludeRuleConverter;
    }

    public List<MavenDependency> convert(MavenPom pom) {
        List<MavenDependency> mavenDependencies = new ArrayList<MavenDependency>();
        for (DependencyDescriptor dependencyDescriptor : pom.getDependencies()) {
            String scope = pom.getScopeMappings().getScope(dependencyDescriptor.getModuleConfigurations());
            if (scope == null && pom.getScopeMappings().isSkipUnmappedConfs()) {
                continue;
            }
            MavenDependency mavenDependency = createMavenDependencyFromDependencyDescriptor(dependencyDescriptor, scope);
            mavenDependencies.add(mavenDependency);
            addExclude(dependencyDescriptor, mavenDependency);
        }
        return mavenDependencies;
    }

    private void addExclude(DependencyDescriptor dependencyDescriptor, MavenDependency mavenDependency) {
        if (!dependencyDescriptor.canExclude()) {
            return;
        }
        for (ExcludeRule excludeRule : dependencyDescriptor.getExcludeRules(dependencyDescriptor.getModuleConfigurations())) {
            DefaultMavenExclude mavenExclude = excludeRuleConverter.convert(excludeRule);
            if (mavenExclude != null) {
                mavenDependency.getMavenExcludes().add(mavenExclude);
            }
        }
    }

    private MavenDependency createMavenDependencyFromDependencyDescriptor(DependencyDescriptor dependencyDescriptor, String scope) {
        ModuleRevisionId mrid = dependencyDescriptor.getDependencyRevisionId();
        return DefaultMavenDependency.newInstance(mrid.getOrganisation(), mrid.getName(), mrid.getRevision(), null, scope);
    }

    public ExcludeRuleConverter getExcludeRuleConverter() {
        return excludeRuleConverter;
    }
}