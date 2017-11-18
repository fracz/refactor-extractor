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

package org.gradle.api.dependencies;

import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.dependencies.DependencyDescriptorFactory;
import org.gradle.util.GUtil;
import org.gradle.util.WrapUtil;

import java.util.HashSet;
import java.util.Set;

/**
* @author Hans Dockter
*/
public class ProjectDependency extends AbstractExcludeAwareDependency {
    private String dependencyConfiguration = Dependency.DEFAULT_CONFIGURATION;

    private Project project;

    private DependencyDescriptorFactory dependencyDescriptorFactory = new DependencyDescriptorFactory();

    // todo Remove this constructor eventually as a addDependency with Closure notation is the recommended way.
    public ProjectDependency(Project dependencyProject, String dependencyConfiguration) {
        this(null, dependencyProject, null);
        this.dependencyConfiguration = dependencyConfiguration;
    }

    public ProjectDependency(Set confs, Object userDependencyDescription, Project project) {
        super(confs, userDependencyDescription);
        this.project = project;
    }

    public boolean isValidDescription(Object userDependencyDescription) {
        return true;
    }

    public Class[] userDepencencyDescriptionType() {
        return WrapUtil.toArray(Project.class);
    }

    public DependencyDescriptor createDepencencyDescriptor(ModuleDescriptor parent) {
        return dependencyDescriptorFactory.createDescriptor(parent, getDependencyProject().getDependencies().createModuleRevisionId(),
                false, true, true, getConfs(), getExcludeRules().getRules(), dependencyConfiguration);
    }

    public ProjectDependency dependencyConfiguration(String dependencyConfiguration) {
        this.dependencyConfiguration = dependencyConfiguration;
        return this;
    }

    public Project getDependencyProject() {
        return (Project) getUserDependencyDescription();
    }

    public void initialize() {
        for (String conf : getConfs()) {
            Set<String> tasks = GUtil.elvis(getProject().getDependencies().getTasks4Conf().get(conf), new HashSet<String>());
            for (String taskName : tasks) {
                ((ProjectInternal) getDependencyProject()).evaluate();
                getProject().task(taskName).dependsOn(getDependencyProject().task(getDependencyProject().getDependencies().getArtifactProductionTaskName()).getPath());
            }
        }
    }

    public String getDependencyConfiguration() {
        return dependencyConfiguration;
    }

    public void setDependencyConfiguration(String dependencyConfiguration) {
        this.dependencyConfiguration = dependencyConfiguration;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}