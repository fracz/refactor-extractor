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
package org.gradle.api.internal.artifacts.ivyservice.dynamicversions;

import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.api.internal.artifacts.ModuleVersionIdentifierSerializer;
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.DefaultModuleVersions;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleVersionRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleVersions;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.messaging.serialize.Decoder;
import org.gradle.messaging.serialize.Encoder;
import org.gradle.messaging.serialize.Serializer;
import org.gradle.util.BuildCommencedTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SingleFileBackedModuleResolutionCache implements ModuleResolutionCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileBackedModuleResolutionCache.class);

    private final BuildCommencedTimeProvider timeProvider;
    private final CacheLockingManager cacheLockingManager;
    private PersistentIndexedCache<ModuleKey, ModuleResolutionCacheEntry> cache;

    public SingleFileBackedModuleResolutionCache(BuildCommencedTimeProvider timeProvider, CacheLockingManager cacheLockingManager) {
        this.timeProvider = timeProvider;
        this.cacheLockingManager = cacheLockingManager;
    }

    private PersistentIndexedCache<ModuleKey, ModuleResolutionCacheEntry> getCache() {
        if (cache == null) {
            cache = initCache();
        }
        return cache;
    }

    private PersistentIndexedCache<ModuleKey, ModuleResolutionCacheEntry> initCache() {
        return cacheLockingManager.createCache("dynamic-revisions", new ModuleKeySerializer(), new ModuleResolutionCacheEntrySerializer());
    }

    public void cacheModuleResolution(ModuleVersionRepository repository, ModuleIdentifier moduleId, ModuleVersions listedVersions) {
        LOGGER.debug("Caching version list in dynamic revision cache: Using '{}' for '{}'", listedVersions, moduleId);
        getCache().put(createKey(repository, moduleId), createEntry(listedVersions));
    }

    public CachedModuleResolution getCachedModuleResolution(ModuleVersionRepository repository, ModuleIdentifier moduleId) {
        ModuleResolutionCacheEntry moduleResolutionCacheEntry = getCache().get(createKey(repository, moduleId));
        if (moduleResolutionCacheEntry == null) {
            return null;
        }
        return new DefaultCachedModuleResolution(moduleId, moduleResolutionCacheEntry, timeProvider);
    }

    private ModuleKey createKey(ModuleVersionRepository repository, ModuleIdentifier moduleId) {
        return new ModuleKey(repository.getId(), moduleId);
    }

    private ModuleResolutionCacheEntry createEntry(ModuleVersions listedVersions) {
        return new ModuleResolutionCacheEntry(listedVersions, timeProvider.getCurrentTime());
    }

    private static class ModuleKey {
        private final String repositoryId;
        private final ModuleIdentifier moduleId;

        private ModuleKey(String repositoryId, ModuleIdentifier moduleId) {
            this.repositoryId = repositoryId;
            this.moduleId = moduleId;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ModuleKey)) {
                return false;
            }
            ModuleKey other = (ModuleKey) o;
            return repositoryId.equals(other.repositoryId) && moduleId.equals(other.moduleId);
        }

        @Override
        public int hashCode() {
            return repositoryId.hashCode() ^ moduleId.hashCode();
        }
    }

    private static class ModuleKeySerializer implements Serializer<ModuleKey> {
        public void write(Encoder encoder, ModuleKey value) throws Exception {
            encoder.writeString(value.repositoryId);
            encoder.writeString(value.moduleId.getGroup());
            encoder.writeString(value.moduleId.getName());
        }

        public ModuleKey read(Decoder decoder) throws Exception {
            String resolverId = decoder.readString();
            String group = decoder.readString();
            String module = decoder.readString();
            return new ModuleKey(resolverId, new DefaultModuleIdentifier(group, module));
        }
    }

    private static class ModuleResolutionCacheEntrySerializer implements Serializer<ModuleResolutionCacheEntry> {

        public void write(Encoder encoder, ModuleResolutionCacheEntry value) throws Exception {
            Set<ModuleVersions.AvailableVersion> versions = value.moduleVersions.getVersions();
            encoder.writeInt(versions.size());
            for (ModuleVersions.AvailableVersion version : versions) {
                encoder.writeString(version.getVersion());
            }
            encoder.writeLong(value.createTimestamp);
        }

        public ModuleResolutionCacheEntry read(Decoder decoder) throws Exception {
            int size = decoder.readInt();
            DefaultModuleVersions versions = new DefaultModuleVersions();
            for (int i = 0; i < size; i++) {
                versions.add(decoder.readString());
            }
            long createTimestamp = decoder.readLong();
            return new ModuleResolutionCacheEntry(versions, createTimestamp);
        }
    }

}