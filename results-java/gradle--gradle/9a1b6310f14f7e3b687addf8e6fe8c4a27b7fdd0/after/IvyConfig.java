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

package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.conflict.AbstractConflictManager;
import org.apache.ivy.plugins.conflict.LatestConflictManager;
import org.apache.ivy.plugins.conflict.StrictConflictException;
import org.apache.ivy.plugins.latest.LatestRevisionStrategy;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.VersionConflictStrategy;
import org.gradle.api.internal.artifacts.configurations.conflicts.DependencySelector;
import org.gradle.api.internal.artifacts.configurations.conflicts.LatestStrategyType;
import org.gradle.api.internal.artifacts.configurations.conflicts.StrictStrategyType;

/**
 * Contains ivy settings and conflict management. The purpose of this class is to insulate from ivy a bit.
 *
 * @author: Szczepan Faber, created at: 9/29/11
 */
public class IvyConfig {

    private final IvySettings ivySettings;
    private final VersionConflictStrategy conflictStrategy;

    public IvyConfig(IvySettings ivySettings, VersionConflictStrategy conflictStrategy) {
        assert ivySettings != null : "ivySettings cannot be null!";
        assert conflictStrategy != null : "conflictStrategy cannot be null!";
        this.ivySettings = ivySettings;
        this.conflictStrategy = conflictStrategy;
    }

    public void applyConflictManager(DefaultModuleDescriptor moduleDescriptor) {
        AbstractConflictManager conflictManager = createIvyConflictManager();
        conflictManager.setSettings(ivySettings);
        moduleDescriptor.addConflictManager(new ModuleId(ExactPatternMatcher.ANY_EXPRESSION,
                ExactPatternMatcher.ANY_EXPRESSION), ExactPatternMatcher.INSTANCE,
                conflictManager);
    }

    private AbstractConflictManager createIvyConflictManager() {
        if (conflictStrategy.getType() instanceof LatestStrategyType) {
            return new LatestConflictManager(new LatestRevisionStrategy());
        } else if (conflictStrategy.getType() instanceof StrictStrategyType) {
            DependencySelector selector = new DependencySelector(((StrictStrategyType) conflictStrategy.getType()).getForce());
            return new ForceAwareStrictConflictManager(selector);
        } else {
            throw new RuntimeException("I don't know what ivy conflict manager to use for this VersionConflictStrategy: " + conflictStrategy);
        }
    }

    //TODO SF some tests around that
    public void maybeTranslateIvyResolveException(Exception e) {
        if (e instanceof StrictConflictException) {
            throw new GradleException("Your dependencies exhibit a version conflict. "
                    + "The conflict resolution strategy is set to: " + conflictStrategy
                    + ". Details: " + e.getMessage(), e);
        }
    }
}