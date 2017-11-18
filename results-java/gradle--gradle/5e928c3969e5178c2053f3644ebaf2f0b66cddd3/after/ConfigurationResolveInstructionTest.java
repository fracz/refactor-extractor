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
package org.gradle.api.dependencies;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.hamcrest.Matchers;

/**
 * @author Hans Dockter
 */
public class ConfigurationResolveInstructionTest {
    private static final String TEST_CONF = "conf";
    private ConfigurationResolveInstructionModifier configurationResolveInstructionModifier;

    @Test
    public void init() {
        ConfigurationResolveInstructionModifier configurationResolveInstructionModifier = new ConfigurationResolveInstructionModifier(TEST_CONF);
        assertThat(configurationResolveInstructionModifier.getConfiguration(), equalTo(TEST_CONF));
        assertThat(configurationResolveInstructionModifier.getResolveInstructionModifier(), equalTo(ResolveInstructionModifiers.DO_NOTHING_MODIFIER));
    }

    @Test
    public void initWithModifier() {
        ResolveInstructionModifier testResolveInstructionModifier = new ResolveInstructionModifier() {
            public ResolveInstruction modify(ResolveInstruction resolveInstruction) {
                return null;
            }
        };
        ConfigurationResolveInstructionModifier configurationResolveInstructionModifier = new ConfigurationResolveInstructionModifier(TEST_CONF,
                testResolveInstructionModifier);
        assertThat(configurationResolveInstructionModifier.getConfiguration(), equalTo(TEST_CONF));
        assertThat(configurationResolveInstructionModifier.getResolveInstructionModifier(), Matchers.sameInstance(testResolveInstructionModifier));
    }
}