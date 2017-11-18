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

package org.gradle.api.internal.artifacts;

import org.gradle.api.artifacts.ProjectDependenciesBuildInstruction;
import org.gradle.api.internal.artifacts.configurations.DefaultConfigurationContainer;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.artifacts.configurations.ResolverProvider;
import org.gradle.api.internal.artifacts.ivyservice.DefaultIvyService;
import org.gradle.api.internal.artifacts.ivyservice.ShortcircuitEmptyConfigsIvyService;
import static org.hamcrest.Matchers.*;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class DefaultConfigurationContainerFactoryTest {
    private JUnit4Mockery context = new JUnit4Mockery();

    @Test
    public void testCreate() {
        ResolverProvider resolverProviderDummy = context.mock(ResolverProvider.class);
        final DependencyMetaDataProvider dependencyMetaDataProviderStub = context.mock(DependencyMetaDataProvider.class);
        ProjectDependenciesBuildInstruction projectDependenciesBuildInstructionDummy = new ProjectDependenciesBuildInstruction(null);

        DefaultConfigurationContainer configurationContainer = (DefaultConfigurationContainer)
                new DefaultConfigurationContainerFactory(projectDependenciesBuildInstructionDummy).createConfigurationContainer(
                        resolverProviderDummy,
                        dependencyMetaDataProviderStub);

        assertThat(configurationContainer.getIvyService(), instanceOf(ShortcircuitEmptyConfigsIvyService.class));
        ShortcircuitEmptyConfigsIvyService service = (ShortcircuitEmptyConfigsIvyService) configurationContainer.getIvyService();
        assertThat(service.getIvyService(), instanceOf(DefaultIvyService.class));
        assertThat(((DefaultIvyService) service.getIvyService()).getMetaDataProvider(), sameInstance(dependencyMetaDataProviderStub));
        assertThat(((DefaultIvyService) service.getIvyService()).getResolverProvider(), sameInstance(resolverProviderDummy));
        assertThat(configurationContainer.getProjectDependenciesBuildInstruction(), sameInstance(projectDependenciesBuildInstructionDummy));
    }
}