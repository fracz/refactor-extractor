/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.containers;

import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author peter
 */
public abstract class FactoryMap<K,V> {
  static final Object NULL = new Object();
  protected final Map<K,V> myMap = createMap();

  protected Map<K, V> createMap() {
    return new THashMap<K, V>();
  }

  @Nullable
  protected abstract V create(K key);

  @Nullable
  public V get(K key) {
    V v = myMap.get(getKey(key));
    if (v == null) {
      v = create(key);
      myMap.put(getKey(key), v == null ? (V)NULL : v);
    }
    return v == NULL ? null : v;
  }

  private static <K> K getKey(final K key) {
    return key == null ? (K)NULL : key;
  }

  public final boolean containsKey(K key) {
    return myMap.containsKey(getKey(key));
  }

  public void put(K key, V value) {
    myMap.put(key, value);
  }

  public void removeEntry(K key) {
    myMap.remove(key);
  }

  public Set<K> keySet() {
    final Set<K> ts = myMap.keySet();
    if (ts.contains(NULL)) {
      final HashSet<K> hashSet = new HashSet<K>(ts);
      hashSet.remove(NULL);
      hashSet.add(null);
      return hashSet;
    }
    return ts;
  }

  public void clear() {
    myMap.clear();
  }
}