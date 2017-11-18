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
public class AndSpecTest extends AbstractCompositeTest {
    public CompositeSpec createCompositeSpec(FilterSpec... filterSpecs) {
        return new AndSpec(filterSpecs);
    }

    @Test
    public void isSatisfiedByWithAllTrue() {
        assertTrue(new AndSpec(createAtomicElements(true, true, true)).isSatisfiedBy(new Object()));
    }

    @Test
    public void isSatisfiedByWithOneFalse() {
        assertFalse(new AndSpec(createAtomicElements(true, false, true)).isSatisfiedBy(new Object()));
    }
}