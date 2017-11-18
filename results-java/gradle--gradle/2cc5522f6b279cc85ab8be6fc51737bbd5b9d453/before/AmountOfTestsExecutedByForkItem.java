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
package org.gradle.api.testing.execution.control.refork;

/**
 * @author Tom Eyckmans
 */
public class AmountOfTestsExecutedByForkItem implements DecisionContextItem {

    public DecisionContextItemKey getKey() {
        return DecisionContextItemKeys.AMOUNT_OF_TEST_EXECUTED_BY_FORK;
    }

    public DecisionContextItemDataGatherer getDataGatherer() {
        return new AmountOfTestsExecutedByForkDataGatherer();
    }

    public DecisionContextItemDataProcessor getDataProcessor() {
        return new AmountOfTestsExecutedByForkDataProcessor();
    }

    public DecisionContextItemConfig getConfig() {
        return null;
    }
}