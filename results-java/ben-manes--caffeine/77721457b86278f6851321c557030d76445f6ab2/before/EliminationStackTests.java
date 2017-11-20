/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine;

import java.util.Queue;

import junit.framework.Test;
import junit.framework.TestCase;

import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.QueueTestSuiteBuilder;
import com.google.common.collect.testing.TestStringQueueGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

/**
 * Guava testlib map tests for {@link EliminationStack}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class EliminationStackTests extends TestCase {

  public static Test suite() throws NoSuchMethodException, SecurityException {
    return QueueTestSuiteBuilder
        .using(new TestStringQueueGenerator() {
            @Override public Queue<String> create(String[] elements) {
              return new EliminationStack<>(MinimalCollection.of(elements)).asLifoQueue();
            }
          })
        .named(EliminationStack.class.getSimpleName())
        .withFeatures(
            CollectionFeature.GENERAL_PURPOSE,
            CollectionSize.ANY)
        .createTestSuite();
  }
}