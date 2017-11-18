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
package org.gradle.api.internal.dependencies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertThat;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JMock;
import org.gradle.api.dependencies.PublishArtifact;
import org.gradle.api.filter.FilterSpec;
import org.gradle.util.WrapUtil;
import static org.hamcrest.Matchers.*;

import java.util.Set;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class DefaultArtifactContainerTest {
    private DefaultArtifactContainer artifactContainer;

    private ConfigurationContainer configurationContainerMock;

    private JUnit4Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() {
        artifactContainer = new DefaultArtifactContainer(configurationContainerMock);
    }

    @Test
    public void init() {
        assertThat(artifactContainer.getConfigurationContainer(), sameInstance(configurationContainerMock));
    }

    @Test
    public void addGetArtifactsWithArtifactInstance() {
        PublishArtifact publishArtifact1 = context.mock(PublishArtifact.class, "artifact1");
        PublishArtifact publishArtifact2 = context.mock(PublishArtifact.class, "artifact2");
        artifactContainer.addArtifacts(publishArtifact1);
        assertThat(artifactContainer.getArtifacts(), equalTo(WrapUtil.toSet(publishArtifact1)));
        artifactContainer.addArtifacts(publishArtifact2);
        assertThat(artifactContainer.getArtifacts(), equalTo(WrapUtil.toSet(publishArtifact1, publishArtifact2)));
    }

    @Test
    public void getArtifactsWithFilterSpec() {
        final PublishArtifact publishArtifact1 = context.mock(PublishArtifact.class, "artifact1");
        final PublishArtifact publishArtifact2 = context.mock(PublishArtifact.class, "artifact2");
        artifactContainer.addArtifacts(publishArtifact1);
        artifactContainer.addArtifacts(publishArtifact2);
        final boolean[] artifact1Offered = new boolean[]{false};
        final boolean[] artifact2Offered = new boolean[]{false};
        Set<PublishArtifact> actualArtifacts = artifactContainer.getArtifacts(new FilterSpec<PublishArtifact>() {
            public boolean isSatisfiedBy(PublishArtifact filterCandidate) {
                if (filterCandidate == publishArtifact1) {
                    artifact1Offered[0] = true;
                }
                if (filterCandidate == publishArtifact2) {
                    artifact2Offered[0] = true;
                }
                return filterCandidate == publishArtifact1;
            }
        });
        assertThat(actualArtifacts, equalTo(WrapUtil.toSet(publishArtifact1)));
    }
}