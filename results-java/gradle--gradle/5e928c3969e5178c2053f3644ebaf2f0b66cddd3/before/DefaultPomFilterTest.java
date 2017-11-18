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
package org.gradle.api.internal.dependencies.maven.deploy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.gradle.api.dependencies.maven.MavenPom;
import org.gradle.api.dependencies.maven.PublishFilter;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.Expectations;

/**
 * @author Hans Dockter
 */
public class DefaultPomFilterTest {
    private static final String TEST_NAME = "TEST_NAME";

    private DefaultPomFilter pomFilter;
    private MavenPom mavenPomMock;

    private PublishFilter publishFilterMock;
    private JUnit4Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() {
        mavenPomMock = context.mock(MavenPom.class);
        publishFilterMock = context.mock(PublishFilter.class);
        pomFilter = new DefaultPomFilter(TEST_NAME, mavenPomMock, publishFilterMock);
    }

    @Test
    public void testGetName() {
        assertEquals(TEST_NAME, pomFilter.getName());
        assertSame(mavenPomMock, pomFilter.getPomTemplate());
        assertSame(publishFilterMock, pomFilter.getFilter());
    }

    @Test
    public void testCopy() {
        final MavenPom copiedMavenPomMock = context.mock(MavenPom.class, "mavenPomCopy");
        context.checking(new Expectations() {{
            allowing(mavenPomMock).copy(); will(returnValue(copiedMavenPomMock));
        }});
        DefaultPomFilter copiedFilter = (DefaultPomFilter) pomFilter.copy();
        assertEquals(TEST_NAME, copiedFilter.getName());
        assertSame(copiedMavenPomMock, copiedFilter.getPomTemplate());
        assertSame(publishFilterMock, copiedFilter.getFilter());
    }
}