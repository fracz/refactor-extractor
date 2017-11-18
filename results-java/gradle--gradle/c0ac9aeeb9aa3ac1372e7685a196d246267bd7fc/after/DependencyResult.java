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

package org.gradle.api.artifacts.result;

import org.gradle.api.Incubating;
import org.gradle.api.Nullable;
import org.gradle.api.artifacts.ModuleVersionSelector;

/**
 * An edge in the dependency graph. Provides information on the origin of the dependency,
 * the requested module version, and the selected module version. Requested and selected version
 * can differ for several reasons: conflict resolution, forcing of a version, or use of a dynamic version.
 * For more information on these terms, see the user guide.
 *
 * @see ResolutionResult
 */
@Incubating
public interface DependencyResult {
    /**
     * Returns the requested module version.
     *
     * @return the requested module version
     */
    ModuleVersionSelector getRequested();

    /**
     * Returns the selected module version, or {@code null}
     * if the requested module version cannot be resolved.
     *
     * @return the selected module version, or {@code null}
     * if the requested module version cannot be resolved
     */
    @Nullable
    ResolvedModuleVersionResult getSelected();

    /**
     * Returns the origin of the dependency.
     *
     * @return the origin of the dependency
     */
    ResolvedModuleVersionResult getFrom();
}