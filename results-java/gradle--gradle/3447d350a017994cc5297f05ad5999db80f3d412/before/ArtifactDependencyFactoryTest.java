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
package org.gradle.api.internal.dependencies;

import org.gradle.api.dependencies.ArtifactDependency;
import org.gradle.util.WrapUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Set;

/**
 * @author Hans Dockter
 */
public class ArtifactDependencyFactoryTest {
    private ArtifactDependencyFactory artifactDependencyFactory;

    @Test
    public void testCreateDependency() {
        Set<String> expectedConfs = WrapUtil.toSet("conf1");
        String expectedDescription = "junit:junit:4.0@jar";
        artifactDependencyFactory = new ArtifactDependencyFactory();
        ArtifactDependency artifactDependency = (ArtifactDependency)
                artifactDependencyFactory.createDependency(expectedConfs, expectedDescription, null);
        assertEquals(expectedConfs, artifactDependency.getConfs());
        assertEquals(expectedDescription, artifactDependency.getUserDependencyDescription());
    }

}