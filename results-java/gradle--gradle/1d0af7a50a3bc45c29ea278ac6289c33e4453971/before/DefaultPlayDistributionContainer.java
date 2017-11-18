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

package org.gradle.play.internal.distribution;

import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.internal.Actions;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.play.distribution.PlayDistribution;
import org.gradle.play.distribution.PlayDistributionContainer;

public class DefaultPlayDistributionContainer extends AbstractNamedDomainObjectContainer<PlayDistribution> implements PlayDistributionContainer {
    private final FileOperations fileOperations;

    public DefaultPlayDistributionContainer(Class<PlayDistribution> type, Instantiator instantiator, FileOperations fileOperations) {
        super(type, instantiator);
        this.fileOperations = fileOperations;
    }

    protected PlayDistribution doCreate(String name) {
        return getInstantiator().newInstance(DefaultPlayDistribution.class, name, fileOperations.copySpec(Actions.doNothing()));
    }
}