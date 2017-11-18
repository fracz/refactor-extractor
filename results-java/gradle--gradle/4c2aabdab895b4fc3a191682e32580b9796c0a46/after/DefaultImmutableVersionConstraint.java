/*
 * Copyright 2017 the original author or authors.
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
package org.gradle.api.internal.artifacts.dependencies;

import com.google.common.collect.ImmutableList;
import org.gradle.api.internal.artifacts.ImmutableVersionConstraint;

import java.util.List;

public class DefaultImmutableVersionConstraint extends AbstractVersionConstraint implements ImmutableVersionConstraint {
    private final String preferredVersion;
    private final ImmutableList<String> rejectedVersions;

    public DefaultImmutableVersionConstraint(String preferredVersion,
                                             List<String> rejectedVersions) {
        this.preferredVersion = preferredVersion;
        this.rejectedVersions = ImmutableList.copyOf(rejectedVersions);
    }

    @Override
    public String getPreferredVersion() {
        return preferredVersion;
    }

    @Override
    public List<String> getRejectedVersions() {
        return rejectedVersions;
    }

}