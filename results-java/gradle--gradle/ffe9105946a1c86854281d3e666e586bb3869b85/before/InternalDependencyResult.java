/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.result;

import org.gradle.api.Nullable;
import org.gradle.api.artifacts.ModuleVersionSelector;

/**
 * by Szczepan Faber, created at: 8/24/12
 */
public class InternalDependencyResult {

    //TODO SF/AM change to an interface and make the DependencyEdge implement it.

    private final ModuleVersionSelector requested;
    private final Exception failure;
    private final ModuleVersionSelection selected;

    public InternalDependencyResult(ModuleVersionSelector requested, ModuleVersionSelection selected, Exception failure) {
        assert requested != null;
        this.requested = requested;
        this.selected = selected;
        this.failure = failure;
    }

    public ModuleVersionSelector getRequested() {
        return requested;
    }

    @Nullable
    public Exception getFailure() {
        return failure;
    }

    @Nullable
    public ModuleVersionSelection getSelected() {
        return selected;
    }
}