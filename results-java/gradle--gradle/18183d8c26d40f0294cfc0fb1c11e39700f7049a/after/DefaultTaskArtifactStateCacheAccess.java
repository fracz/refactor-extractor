/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.changedetection.state;

import org.gradle.api.Transformer;
import org.gradle.cache.internal.MultiProcessSafePersistentIndexedCache;
import org.gradle.cache.internal.PersistentIndexedCacheParameters;
import org.gradle.internal.Factory;
import org.gradle.api.invocation.Gradle;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.messaging.serialize.Serializer;
import org.gradle.cache.internal.FileLockManager;
import org.gradle.listener.LazyCreationProxy;

import java.io.File;

public class DefaultTaskArtifactStateCacheAccess implements TaskArtifactStateCacheAccess {
    private final Gradle gradle;
    private final CacheRepository cacheRepository;
    private InMemoryPersistentCacheDecorator inMemoryDecorator;
    private PersistentCache cache;
    private final Object lock = new Object();

    public DefaultTaskArtifactStateCacheAccess(Gradle gradle, CacheRepository cacheRepository, Factory<InMemoryPersistentCacheDecorator> decoratorFactory) {
        this.gradle = gradle;
        this.cacheRepository = cacheRepository;
        this.inMemoryDecorator = decoratorFactory.create();
    }

    private PersistentCache getCache() {
        //TODO SF just do it in the constructor
        synchronized (lock) {
            if (cache == null) {
                cache = inMemoryDecorator.withMemoryCaching(cacheRepository
                        .cache("taskArtifacts")
                        .forObject(gradle)
                        .withDisplayName("task artifact state cache")
                        .withLockMode(FileLockManager.LockMode.Exclusive)
                        .open());
            }
            return cache;
        }
    }

    public <K, V> PersistentIndexedCache<K, V> createCache(final String cacheName, final Class<K> keyType, final Serializer<V> valueSerializer) {
        Factory<PersistentIndexedCache> factory = new Factory<PersistentIndexedCache>() {
            public PersistentIndexedCache create() {
                PersistentIndexedCacheParameters parameters =
                        new PersistentIndexedCacheParameters(cacheFile(cacheName), keyType, valueSerializer)
                            .cacheDecorator(withInMemoryCaching());
                return getCache().createCache(parameters);
            }
        };
        return new LazyCreationProxy<PersistentIndexedCache>(PersistentIndexedCache.class, factory).getSource();

    }

    private <K, V> Transformer<MultiProcessSafePersistentIndexedCache<K, V>, MultiProcessSafePersistentIndexedCache<K, V>> withInMemoryCaching() {
        //TODO SF this is the hook for in-memory caching capabilities, now synced with DefaultCacheAccess
        return new Transformer<MultiProcessSafePersistentIndexedCache<K, V>, MultiProcessSafePersistentIndexedCache<K, V>>() {
            public MultiProcessSafePersistentIndexedCache<K, V> transform(MultiProcessSafePersistentIndexedCache<K, V> original) {
                return original;
            }
        };
    }

    private File cacheFile(String cacheName) {
        return new File(getCache().getBaseDir(), cacheName + ".bin");
    }

    public <T> T useCache(String operationDisplayName, Factory<? extends T> action) {
        return getCache().useCache(operationDisplayName, action);
    }

    public void useCache(String operationDisplayName, Runnable action) {
        getCache().useCache(operationDisplayName, action);
    }

    public void longRunningOperation(String operationDisplayName, Runnable action) {
        getCache().longRunningOperation(operationDisplayName, action);
    }
}