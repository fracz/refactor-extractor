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
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.publish.PublishEngine;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.gradle.api.artifacts.PublishInstruction;
import org.gradle.util.WrapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * @author Hans Dockter
 */
public class DefaultIvyDependencyPublisher implements IvyDependencyPublisher {
    public static final String FILE_PATH_EXTRA_ATTRIBUTE = "filePath";
    public static final List<String> ARTIFACT_PATTERN = WrapUtil.toList(String.format("[%s]", FILE_PATH_EXTRA_ATTRIBUTE));

    private static Logger logger = LoggerFactory.getLogger(DefaultIvyDependencyPublisher.class);

    private PublishOptionsFactory publishOptionsFactory;

    private ModuleDescriptorForUploadConverter moduleDescriptorForUploadConverter;

    public DefaultIvyDependencyPublisher(ModuleDescriptorForUploadConverter moduleDescriptorForUploadConverter, PublishOptionsFactory publishOptionsFactory) {
        this.moduleDescriptorForUploadConverter = moduleDescriptorForUploadConverter;
        this.publishOptionsFactory = publishOptionsFactory;
    }

    public void publish(Set<String> configurations,
                        PublishInstruction publishInstruction,
                        List<DependencyResolver> publishResolvers,
                        ModuleDescriptor moduleDescriptor,
                        PublishEngine publishEngine) {
        try {
            if (publishInstruction.isUploadDescriptor()) {
                moduleDescriptorForUploadConverter.createModuleDescriptor(moduleDescriptor).toIvyFile(publishInstruction.getDescriptorDestination());
            }
            for (DependencyResolver resolver : publishResolvers) {
                logger.info("Publishing to Resolver {}", resolver);
                publishEngine.publish(moduleDescriptor, ARTIFACT_PATTERN, resolver,
                        publishOptionsFactory.createPublishOptions(configurations, publishInstruction));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public ModuleDescriptorForUploadConverter getModuleDescriptorForUploadConverter() {
        return moduleDescriptorForUploadConverter;
    }

    public void setModuleDescriptorForUploadConverter(ModuleDescriptorForUploadConverter moduleDescriptorForUploadConverter) {
        this.moduleDescriptorForUploadConverter = moduleDescriptorForUploadConverter;
    }
}