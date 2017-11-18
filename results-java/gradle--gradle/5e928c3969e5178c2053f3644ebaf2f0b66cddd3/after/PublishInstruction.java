/*
 * Copyright 2007-2009 the original author or authors.
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

import org.gradle.api.filter.FilterSpec;
import org.gradle.api.filter.Filters;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class PublishInstruction {
    private ModuleDescriptorInstruction moduleDescriptor = new ModuleDescriptorInstruction();

    private FilterSpec<PublishArtifact> artifactFilter = Filters.noFilter();

    public ModuleDescriptorInstruction getModuleDescriptor() {
        return moduleDescriptor;
    }

    public FilterSpec<PublishArtifact> getArtifactFilter() {
        return artifactFilter;
    }

    public void setArtifactFilter(FilterSpec<PublishArtifact> artifactFilter) {
        this.artifactFilter = artifactFilter;
    }

    public static class ModuleDescriptorInstruction {
        private boolean publish = true;

        private FilterSpec<Configuration> configurationFilter = Filters.noFilter();

        private FilterSpec<Dependency> dependencyFilter = Filters.noFilter();

        private File ivyFileParentDir = null;

        public boolean isPublish() {
            return publish;
        }

        public void setPublish(boolean publish) {
            this.publish = publish;
        }

        public FilterSpec<Configuration> getConfigurationFilter() {
            return configurationFilter;
        }

        public void setConfigurationFilter(FilterSpec<Configuration> configurationFilter) {
            this.configurationFilter = configurationFilter;
        }

        public FilterSpec<Dependency> getDependencyFilter() {
            return dependencyFilter;
        }

        public void setDependencyFilter(FilterSpec<Dependency> dependencyFilter) {
            this.dependencyFilter = dependencyFilter;
        }

        public File getIvyFileParentDir() {
            return ivyFileParentDir;
        }

        public void setIvyFileParentDir(File ivyFileParentDir) {
            this.ivyFileParentDir = ivyFileParentDir;
        }
    }
}