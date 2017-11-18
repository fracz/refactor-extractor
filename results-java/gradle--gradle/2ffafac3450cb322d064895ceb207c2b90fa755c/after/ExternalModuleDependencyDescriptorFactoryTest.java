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
package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies;

import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.artifacts.ivyservice.IvyUtil;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.equalTo;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Hans Dockter
 */
public class ExternalModuleDependencyDescriptorFactoryTest extends AbstractDependencyDescriptorFactoryInternalTest {
    private JUnit4Mockery context = new JUnit4Mockery();

    ExternalModuleDependencyDescriptorFactory externalModuleDependencyDescriptorFactory =
            new ExternalModuleDependencyDescriptorFactory(excludeRuleConverterStub);

    @Test
    public void canConvert() {
        assertThat(externalModuleDependencyDescriptorFactory.canConvert(context.mock(ProjectDependency.class)), Matchers.equalTo(false));
        assertThat(externalModuleDependencyDescriptorFactory.canConvert(context.mock(ExternalModuleDependency.class)), Matchers.equalTo(true));
    }

    @Test
    public void testAddWithNullGroupAndNullVersion_shouldHaveEmptyStringModuleRevisionValues() {
        ModuleDependency dependency = new DefaultExternalModuleDependency(null, "gradle-core", null, TEST_DEP_CONF);
        externalModuleDependencyDescriptorFactory.addDependencyDescriptor(TEST_CONF, moduleDescriptor, dependency);
        DefaultDependencyDescriptor dependencyDescriptor = (DefaultDependencyDescriptor) moduleDescriptor.getDependencies()[0];
        assertThat(dependencyDescriptor.getDependencyRevisionId(), equalTo(IvyUtil.createModuleRevisionId(dependency)));
    }

    @Test
    public void testCreateFromModuleDependency() {
        DefaultExternalModuleDependency moduleDependency = ((DefaultExternalModuleDependency)
                setUpDependency(new DefaultExternalModuleDependency("org.gradle", "gradle-core", "1.0", TEST_DEP_CONF)));

        externalModuleDependencyDescriptorFactory.addDependencyDescriptor(TEST_CONF, moduleDescriptor, moduleDependency);
        DefaultDependencyDescriptor dependencyDescriptor = (DefaultDependencyDescriptor) moduleDescriptor.getDependencies()[0];

        assertEquals(moduleDependency.isChanging(), dependencyDescriptor.isChanging());
        assertEquals(dependencyDescriptor.isForce(), moduleDependency.isForce());
        assertEquals(IvyUtil.createModuleRevisionId(moduleDependency),
                dependencyDescriptor.getDependencyRevisionId());
        assertDependencyDescriptorHasCommonFixtureValues(dependencyDescriptor);
    }

    @Test
    public void addExternalModuleDependenciesWithSameModuleRevisionIdAndDifferentConfs_shouldBePartOfSameDependencyDescriptor() {
        ModuleDependency dependency1 = new DefaultExternalModuleDependency("org.gradle", "gradle-core", "1.0", TEST_DEP_CONF);
        ModuleDependency dependency2 = new DefaultExternalModuleDependency("org.gradle", "gradle-core", "1.0", TEST_OTHER_DEP_CONF);

        assertThataddDependenciesWithSameModuleRevisionIdAndDifferentConfs_shouldBePartOfSameDependencyDescriptor(
                dependency1, dependency2, externalModuleDependencyDescriptorFactory
        );
    }

}