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
package org.gradle.api.internal.artifacts.repositories.resolver;

import org.gradle.api.artifacts.ComponentMetaDataDetails;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.MutableModuleVersionMetaData;

import java.util.List;

public class ComponentMetaDataDetailsAdapter implements ComponentMetaDataDetails {
    private final MutableModuleVersionMetaData metadata;

    public ComponentMetaDataDetailsAdapter(MutableModuleVersionMetaData metadata) {
        this.metadata = metadata;
    }

    public String getGroup() {
        return metadata.getId().getGroup();
    }

    public String getName() {
        return metadata.getId().getName();
    }

    public String getVersion() {
        return metadata.getId().getVersion();
    }

    public boolean isChanging() {
        return metadata.isChanging();
    }

    public String getStatus() {
        return metadata.getStatus();
    }

    public List<String> getStatusScheme() {
        return metadata.getStatusScheme();
    }

    public void setChanging(boolean changing) {
        metadata.setChanging(changing);
    }

    public void setStatus(String status) {
        metadata.setStatus(status);
    }

    public void setStatusScheme(List<String> statusScheme) {
        metadata.setStatusScheme(statusScheme);
    }
}