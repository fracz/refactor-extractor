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

import org.gradle.api.internal.artifacts.ImmutableVersionConstraint;
import org.gradle.api.internal.artifacts.VersionConstraintInternal;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionSelectorScheme;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelector;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

public class DefaultVersionConstraint extends AbstractVersionConstraint implements VersionConstraintInternal {
    private String prefer;
    private List<String> rejects;

    public DefaultVersionConstraint(String version, boolean strict) {
        this.prefer = version;
        if (strict) {
            doStrict();
        } else {
            this.rejects = Collections.emptyList();
        }
    }

    private void doStrict() {
        // When strict version is used, we need to parse the preferred selector early, in order to compute its complement.
        // Hopefully this shouldn't happen too often. If it happens to become a performance problem, we need to reconsider
        // how we compute the "reject" clause
        DefaultVersionSelectorScheme versionSelectorScheme = new DefaultVersionSelectorScheme(new DefaultVersionComparator());
        VersionSelector preferredSelector = versionSelectorScheme.parseSelector(prefer);
        VersionSelector rejectedSelector = versionSelectorScheme.complementForRejection(preferredSelector);
        this.rejects = Collections.singletonList(rejectedSelector.getSelector());
    }

    public DefaultVersionConstraint(String version, List<String> rejects) {
        this.prefer = nullToEmpty(version);
        this.rejects = rejects;
    }

    public DefaultVersionConstraint(String version) {
        this(version, false);
    }

    @Override
    public ImmutableVersionConstraint asImmutable(VersionSelectorScheme scheme) {
        String v = prefer == null ? "" : prefer;
        VersionSelector preferredSelector = scheme.parseSelector(v);
        VersionSelector rejectedSelector = null;
        if (rejects.size() == 1) {
            rejectedSelector = scheme.parseSelector(rejects.get(0));
        } else if (!rejects.isEmpty()) {
            throw new UnsupportedOperationException("Multiple rejects are not yet supported");
        }
        return new DefaultImmutableVersionConstraint(v, rejects, preferredSelector, rejectedSelector);
    }

    @Override
    public String getPreferredVersion() {
        return prefer;
    }

    @Override
    public void prefer(String version) {
        this.prefer = version;
        this.rejects = Collections.emptyList();
    }

    @Override
    public void strictly(String version) {
        this.prefer = version;
        doStrict();
    }

    @Override
    public List<String> getRejectedVersions() {
       return rejects;
    }
}