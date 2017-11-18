/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.api.internal.tasks.testing.selection;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.testing.TestSelection;

import java.util.LinkedList;
import java.util.List;

public class DefaultTestSelection implements TestSelection {

    private List<DefaultTestSelectionSpec> includedTests = new LinkedList<DefaultTestSelectionSpec>();

    public DefaultTestSelection includeTest(String testClass, String testMethod) {
        includedTests.add(new DefaultTestSelectionSpec(testClass, testMethod));
        return this;
    }

    @Input
    public List<DefaultTestSelectionSpec> getIncludedTests() {
        return includedTests;
    }
}