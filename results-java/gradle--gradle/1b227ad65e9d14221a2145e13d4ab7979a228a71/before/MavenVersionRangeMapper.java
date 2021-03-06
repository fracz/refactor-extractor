/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.api.publication.maven.internal;

import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.MavenVersionMatcherScheme;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionMatcherScheme;

public class MavenVersionRangeMapper implements VersionRangeMapper {
    private final VersionMatcherScheme defaultVersionMatcherScheme;
    private final VersionMatcherScheme mavenVersionMatcherScheme;

    public MavenVersionRangeMapper(VersionMatcherScheme defaultVersionSelector) {
        this.defaultVersionMatcherScheme = defaultVersionSelector;
        mavenVersionMatcherScheme = new MavenVersionMatcherScheme(defaultVersionMatcherScheme);
    }

    public String map(String version) {
        if(version == null) {
            return null;
        }
        return mavenVersionMatcherScheme.renderSelector(defaultVersionMatcherScheme.parseSelector(version));
    }
}