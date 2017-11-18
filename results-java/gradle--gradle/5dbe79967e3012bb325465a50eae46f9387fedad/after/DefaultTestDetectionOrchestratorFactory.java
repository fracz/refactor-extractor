/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.testing.detection;

import org.gradle.api.tasks.testing.AbstractTestTask;
import org.gradle.api.testing.TestOrchestratorFactory;
import org.gradle.api.testing.fabric.TestClassRunInfo;
import org.gradle.util.queues.BlockingQueueItemProducer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Creates the objects needed by the TestDetectionOrchestrator.
 *
 * @author Tom Eyckmans
 */
public class DefaultTestDetectionOrchestratorFactory implements TestDetectionOrchestratorFactory {
    private final TestOrchestratorFactory testOrchestratorFactory;
    private final TestClassScannerFactory testClassScannerFactory;
    private final BlockingQueueItemProducer<TestClassRunInfo> testDetectionQueueProducer;

    public DefaultTestDetectionOrchestratorFactory(final TestOrchestratorFactory testOrchestratorFactory) {
        if (testOrchestratorFactory == null) {
            throw new IllegalArgumentException("testOrchestratorFactory == null!");
        }

        this.testOrchestratorFactory = testOrchestratorFactory;

        testClassScannerFactory = new DefaultTestClassScannerFactory();
        final BlockingQueue<TestClassRunInfo> testDetectionQueue = testOrchestratorFactory.getTestDetectionQueue();
        testDetectionQueueProducer = new BlockingQueueItemProducer<TestClassRunInfo>(testDetectionQueue, 100L,
                TimeUnit.MILLISECONDS);
    }

    public TestClassScanner createDetectionRunner() {
        final AbstractTestTask testTask = testOrchestratorFactory.getTestTask();
        final TestClassProcessor testClassProcessor = new QueueItemProducingTestClassProcessor(testDetectionQueueProducer);

        return testClassScannerFactory.createTestClassScanner(testTask, testClassProcessor);
    }
}