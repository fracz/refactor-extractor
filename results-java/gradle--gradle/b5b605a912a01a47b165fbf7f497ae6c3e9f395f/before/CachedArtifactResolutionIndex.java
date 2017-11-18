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
package org.gradle.api.internal.artifacts.resolutioncache;

import java.io.File;
import java.util.Date;

/**
 * Provides an indexed view into cached artifacts.
 *
 * Maintains references to the location of files in the persistent filestore. Does not deal with moving files into the filestore.
 * <p>
 *
 * @param <K> The type of the key to the index
 */
public interface CachedArtifactResolutionIndex<K> {

    /**
     * Adds a resolution to the index.
     *
     * The incoming file is expected to be in the persistent filestore. This method will not move/copy the file there.
     *
     * @param key The key to cache this resolution under in the index.
     * @param artifactFile The artifact file in the persistent file store
     * @param lastModified The date that the artifact was last modified <b>at the source</b> if known. May be null.
     * @param sourceUrl The URL that this artifact was resolved from, if known. May be null.
     */
    void store(K key, File artifactFile, Date lastModified, String sourceUrl);

    /**
     * Lookup a cached resolution.
     *
     * @param key The key to search the index for
     * @return The cached artifact resolution if one exists, otherwise null.
     */
    CachedArtifactResolution lookup(K key);

    /**
     * Remove the entry for the given key if it exists.
     *
     * @param key The key of the item to remove.
     */
    void clear(K key);

}