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
package org.gradle.nativebinaries.language.c.internal.incremental;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FileState implements Serializable {
    private byte[] hash;
    // TODO:DAZ We should cache the source includes separately, shared by all compile tasks.
    // The resolved includes should continue to be cached per compile task.
    private SourceIncludes sourceIncludes;
    private Set<ResolvedInclude> resolvedIncludes = new HashSet<ResolvedInclude>();

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public SourceIncludes getIncludes() {
        return sourceIncludes;
    }

    public void setIncludes(SourceIncludes sourceIncludes) {
        this.sourceIncludes = sourceIncludes;
    }

    public Set<ResolvedInclude> getResolvedIncludes() {
        return resolvedIncludes;
    }

    public void setResolvedIncludes(Set<ResolvedInclude> resolvedIncludes) {
        this.resolvedIncludes = resolvedIncludes;
    }
}