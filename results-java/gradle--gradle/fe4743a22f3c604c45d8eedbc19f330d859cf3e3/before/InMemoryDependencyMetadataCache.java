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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve.memcache;

import com.google.common.collect.MapMaker;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.LocalAwareModuleVersionRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.Stoppable;

import java.util.Map;

/**
 * Caches the dependency metadata (descriptors, artifact files) in memory. Uses soft maps to reduce heap pressure.
 */
public class InMemoryDependencyMetadataCache implements Stoppable {

    private final static Logger LOG = Logging.getLogger(InMemoryDependencyMetadataCache.class);

    private Map<String, DependencyMetadataCache> cachePerRepo = new MapMaker().softValues().makeMap();

    private final DependencyMetadataCacheStats stats = new DependencyMetadataCacheStats();

    public LocalAwareModuleVersionRepository cached(LocalAwareModuleVersionRepository input) {
        DependencyMetadataCache dataCache = cachePerRepo.get(input.getId());
        stats.reposWrapped++;
        if (dataCache == null) {
            LOG.debug("Creating new in-memory cache for repo '{}' [{}].", input.getName(), input.getId());
            dataCache = new DependencyMetadataCache();
            stats.cacheInstances++;
            cachePerRepo.put(input.getId(), dataCache);
        } else {
            LOG.debug("Reusing in-memory cache for repo '{}' [{}].", input.getName(), input.getId());
        }
        return new CachedRepository(dataCache, input, stats);
    }

    public void stop() {
        cachePerRepo.clear();
        LOG.info("In-memory dependency metadata cache closed. {}", stats);
    }
}