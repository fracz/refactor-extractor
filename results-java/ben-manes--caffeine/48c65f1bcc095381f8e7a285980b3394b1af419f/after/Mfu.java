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
package com.github.benmanes.caffeine.cache.simulator.policy.sampled;

import com.github.benmanes.caffeine.cache.simulator.admission.Admittor;

/**
 * Implements a most-frequently-used cache based on sampling the entries.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class Mfu extends AbstractSamplingPolicy {

  public Mfu(String name, Admittor admittor) {
    super(name, admittor, EvictionPolicy.MFU);
  }
}