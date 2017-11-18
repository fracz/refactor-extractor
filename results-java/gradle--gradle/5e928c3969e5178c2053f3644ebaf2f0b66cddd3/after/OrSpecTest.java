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
package org.gradle.api.filter;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.gradle.api.filter.CompositeSpec;
import org.gradle.api.dependencies.Dependency;
import org.jmock.integration.junit4.JUnit4Mockery;

/**
 * @author Hans Dockter
 */
public class OrSpecTest extends AbstractCompositeTest {
    private JUnit4Mockery context = new JUnit4Mockery();

    public CompositeSpec createCompositeSpec(FilterSpec... filterSpecs) {
        return new OrSpec(filterSpecs);
    }

    @Test
    public void isSatisfiedByWithOneTrue() {
        assertTrue(new OrSpec(createAtomicElements(false, true, false)).isSatisfiedBy(context.mock(Dependency.class)));
    }

    @Test
    public void isSatisfiedByWithAllFalse() {
        assertFalse(new AndSpec(createAtomicElements(false, false, false)).isSatisfiedBy(context.mock(Dependency.class)));
    }
}