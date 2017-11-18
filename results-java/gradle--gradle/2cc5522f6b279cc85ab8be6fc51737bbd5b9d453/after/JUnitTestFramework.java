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
package org.gradle.external.junit;

import org.gradle.api.tasks.testing.AbstractTestFramework;
import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.api.testing.fabric.TestFrameworkInstance;
import org.gradle.api.testing.fabric.TestProcessorFactory;
import org.gradle.api.testing.fabric.TestMethodProcessResultState;
import org.gradle.api.testing.fabric.TestMethodProcessResultStates;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Tom Eyckmans
 */
public class JUnitTestFramework extends AbstractTestFramework {

    private static final Map<TestMethodProcessResultState, TestMethodProcessResultState> methodProcessResultStateMapping = new HashMap<TestMethodProcessResultState, TestMethodProcessResultState>();
    static {
        methodProcessResultStateMapping.put(JUnitTestMethodProcessResultStates.SUCCESS, TestMethodProcessResultStates.SUCCESS);
        methodProcessResultStateMapping.put(JUnitTestMethodProcessResultStates.FAILURE, TestMethodProcessResultStates.FAILURE);
        methodProcessResultStateMapping.put(JUnitTestMethodProcessResultStates.ERROR, TestMethodProcessResultStates.ERROR);
    }
    public JUnitTestFramework() {
        super("junit", "JUnit");
    }

    public TestFrameworkInstance getInstance(AbstractTestTask testTask) {
        return new JUnitTestFrameworkInstance(testTask, this);
    }

    public TestProcessorFactory getProcessorFactory() {
        return new JUnitTestProcessorFactory();
    }

    public Map<TestMethodProcessResultState, TestMethodProcessResultState> getMethodProcessResultStateMapping() {
        return methodProcessResultStateMapping;
    }
}