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

package org.gradle.api.publish.ivy.tasks.internal;

import org.gradle.api.Action;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.ivy.internal.IvyPublicationInternal;
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor;
import org.gradle.api.tasks.TaskContainer;

import java.util.concurrent.Callable;

import static org.apache.commons.lang.StringUtils.capitalize;

public class IvyPublicationDynamicDescriptorGenerationTaskCreator {

    final private TaskContainer tasks;

    public IvyPublicationDynamicDescriptorGenerationTaskCreator(TaskContainer tasks) {
        this.tasks = tasks;
    }

    public void monitor(PublicationContainer publications) {
        publications.all(new Action<Publication>() {
            public void execute(Publication publication) {
                maybeCreate(publication);
            }
        });
    }

    private void maybeCreate(Publication publication) {
        if (!(publication instanceof IvyPublicationInternal)) {
            return;
        }

        final IvyPublicationInternal publicationInternal = (IvyPublicationInternal) publication;

        String publicationName = publication.getName();

        String descriptorTaskName = calculateDescriptorTaskName(publicationName);
        GenerateIvyDescriptor descriptorTask = tasks.add(descriptorTaskName, GenerateIvyDescriptor.class);
        ConventionMapping descriptorTaskConventionMapping = new DslObject(descriptorTask).getConventionMapping();
        descriptorTaskConventionMapping.map("destination", new Callable<Object>() {
            public Object call() throws Exception {
                return publicationInternal.getDescriptor().getFile();
            }
        });
        descriptorTaskConventionMapping.map("module", new Callable<Object>() {
            public Object call() throws Exception {
                return publicationInternal.getModule();
            }
        });
        descriptorTaskConventionMapping.map("configurations", new Callable<Object>() {
            public Object call() throws Exception {
                return publicationInternal.getConfigurations().iterator().next().getAll();
            }
        });
        descriptorTaskConventionMapping.map("xmlAction", new Callable<Object>() {
            public Object call() throws Exception {
                return publicationInternal.getDescriptor().getXmlAction();
            }
        });
        publicationInternal.getDescriptor().builtBy(descriptorTask);
    }

    private String calculateDescriptorTaskName(String publicationName) {
        return String.format("generate%sPublicationModuleDescriptor", capitalize(publicationName));
    }

}