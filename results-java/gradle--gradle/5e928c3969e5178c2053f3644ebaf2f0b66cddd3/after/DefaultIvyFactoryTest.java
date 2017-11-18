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
package org.gradle.api.internal.dependencies.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.IvySettings;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Hans Dockter
 */
public class DefaultIvyFactoryTest {
    @Test
    public void testCreateIvy() {
        IvySettings ivySettings = new IvySettings();
        Ivy ivy = new DefaultIvyFactory().createIvy(ivySettings);
        assertThat(ivy, notNullValue());
        assertThat(ivy.getSettings(), equalTo(ivySettings));
    }
}