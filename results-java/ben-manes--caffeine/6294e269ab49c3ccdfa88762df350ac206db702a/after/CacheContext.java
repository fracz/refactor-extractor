/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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
package com.github.benmanes.caffeine.cache.testing;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.RemovalNotification;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Advance;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheExecutor;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.CacheWeigher;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Compute;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Expire;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Implementation;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.InitialCapacity;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Listener;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Loader;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.MaximumSize;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Population;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.ReferenceType;
import com.github.benmanes.caffeine.cache.testing.CacheSpec.Stats;
import com.github.benmanes.caffeine.cache.testing.RemovalListeners.ConsumingRemovalListener;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

/**
 * The cache configuration context for a test case.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class CacheContext {
  final InitialCapacity initialCapacity;
  final Implementation implementation;
  final Listener removalListenerType;
  final CacheExecutor cacheExecutor;
  final ReferenceType valueStrength;
  final ReferenceType keyStrength;
  final MaximumSize maximumSize;
  final Population population;
  final CacheWeigher weigher;
  final Expire afterAccess;
  final Expire afterWrite;
  final Executor executor;
  final Advance advance;
  final Expire refresh;
  final Loader loader;
  final Stats stats;

  final Compute compute;
  final FakeTicker ticker;
  final Map<Integer, Integer> original;
  final RemovalListener<Integer, Integer> removalListener;

  Cache<?, ?> cache;
  AsyncLoadingCache<?, ?> asyncCache;

  @Nullable Integer firstKey;
  @Nullable Integer middleKey;
  @Nullable Integer lastKey;
  long initialSize;

  // Generated on-demand
  Integer absentKey;
  Integer absentValue;

  Map<Integer, Integer> absent;

  public CacheContext(InitialCapacity initialCapacity, Stats stats, CacheWeigher weigher,
      MaximumSize maximumSize, Expire afterAccess, Expire afterWrite, Expire refresh,
      Advance advance, ReferenceType keyStrength, ReferenceType valueStrength,
      CacheExecutor cacheExecutor, Listener removalListenerType, Population population,
      boolean isLoading, Compute compute, Loader loader, Implementation implementation) {
    this.initialCapacity = requireNonNull(initialCapacity);
    this.stats = requireNonNull(stats);
    this.weigher = requireNonNull(weigher);
    this.maximumSize = requireNonNull(maximumSize);
    this.afterAccess = requireNonNull(afterAccess);
    this.afterWrite = requireNonNull(afterWrite);
    this.refresh = requireNonNull(refresh);
    this.advance = requireNonNull(advance);
    this.keyStrength = requireNonNull(keyStrength);
    this.valueStrength = requireNonNull(valueStrength);
    this.cacheExecutor = requireNonNull(cacheExecutor);
    this.executor = cacheExecutor.get();
    this.removalListenerType = removalListenerType;
    this.removalListener = removalListenerType.create();
    this.population = requireNonNull(population);
    this.loader = isLoading ? requireNonNull(loader) : null;
    this.ticker = new FakeTicker();
    this.implementation = requireNonNull(implementation);
    this.original = new LinkedHashMap<>();
    this.initialSize = -1;
    this.compute = compute;
  }

  public boolean isAsync() {
    return (compute == Compute.ASYNC);
  }

  public Population population() {
    return population;
  }

  public Integer firstKey() {
    assertThat("Invalid usage of context", firstKey, is(not(nullValue())));
    return firstKey;
  }

  public Integer middleKey() {
    assertThat("Invalid usage of context", middleKey, is(not(nullValue())));
    return middleKey;
  }

  public Integer lastKey() {
    assertThat("Invalid usage of context", lastKey, is(not(nullValue())));
    return lastKey;
  }

  public Set<Integer> firstMiddleLastKeys() {
    return ImmutableSet.of(firstKey, middleKey, lastKey);
  }

  public void clear() {
    original.clear();
    absent = null;
    absentKey = null;
    firstKey = null;
    middleKey = null;
    lastKey = null;
  }

  public Integer absentKey() {
    return (absentKey == null) ? (absentKey = nextAbsentKey()) : absentKey;
  }

  public Integer absentValue() {
    return (absentValue == null) ? (absentValue = -absentKey()) : absentValue;
  }

  public Map<Integer, Integer> absent() {
    if (absent != null) {
      return absent;
    }
    absent = new LinkedHashMap<>();
    do {
      Integer key = nextAbsentKey();
      absent.put(key, -key);
    } while (absent.size() < 10);
    return absent;
  }

  public Set<Integer> absentKeys() {
    return absent().keySet();
  }

  private Integer nextAbsentKey() {
    int base = original.isEmpty() ? 0 : (lastKey + 1);
    return ThreadLocalRandom.current().nextInt(base, Integer.MAX_VALUE);
  }

  public long initialSize() {
    return (initialSize < 0) ? (initialSize = original.size()) : initialSize;
  }

  public long maximumSize() {
    assertThat("Invalid usage of context", maximumSize, is(not(nullValue())));
    return maximumSize.max();
  }

  public long maximumWeight() {
    assertThat("Invalid usage of context", isWeighted(), is(not(nullValue())));
    long maximum = weigher.unitsPerEntry() * maximumSize.max();
    return (maximum < 0) ? Long.MAX_VALUE : maximum;
  }

  public long maximumWeightOrSize() {
    return isWeighted() ? maximumWeight() : maximumSize();
  }

  public boolean isWeighted() {
    return (weigher != CacheWeigher.DEFAULT);
  }

  public boolean isUnbounded() {
    return (maximumSize == MaximumSize.DISABLED);
  }

  public boolean refreshes() {
    return (refresh != Expire.DISABLED);
  }

  /** The initial entries in the cache, iterable in insertion order. */
  public Map<Integer, Integer> original() {
    initialSize(); // lazy initialize
    return original;
  }

  public ReferenceType keyStrength() {
    return keyStrength;
  }

  public ReferenceType valueStrength() {
    return valueStrength;
  }

  public boolean isLoading() {
    return (loader != null);
  }

  public Loader loader() {
    return loader;
  }

  public Listener removalListenerType() {
    return removalListenerType;
  }

  public RemovalListener<Integer, Integer> removalListener() {
    return requireNonNull(removalListener);
  }

  public List<RemovalNotification<Integer, Integer>> consumedNotifications() {
    return (removalListenerType() == Listener.CONSUMING)
        ? ((ConsumingRemovalListener<Integer, Integer>) removalListener).evicted()
        : Collections.emptyList();
  }

  public boolean isRecordingStats() {
    return (stats == Stats.ENABLED);
  }

  public CacheStats stats() {
    return cache.stats();
  }

  public boolean expires() {
    return (afterAccess != Expire.DISABLED) || (afterWrite != Expire.DISABLED);
  }

  public Expire expireAfterAccess() {
    return afterAccess;
  }

  public Expire expireAfterWrite() {
    return afterWrite;
  }

  public FakeTicker ticker() {
    return ticker;
  }

  public Implementation implementation() {
    return implementation;
  }

  public CacheExecutor executorType() {
    return cacheExecutor;
  }

  public Executor executor() {
    return executor;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("population", population)
        .add("maximumSize", maximumSize)
        .add("weigher", weigher)
        .add("afterAccess", afterAccess)
        .add("afterWrite", afterWrite)
        .add("refreshAfterWrite", refresh)
        .add("keyStrength", keyStrength)
        .add("valueStrength", valueStrength)
        .add("compute", compute)
        .add("loader", loader)
        .add("cacheExecutor", cacheExecutor)
        .add("removalListener", removalListenerType)
        .add("initialCapacity", initialCapacity)
        .add("stats", stats)
        .add("implementation", implementation)
        .toString();
  }
}